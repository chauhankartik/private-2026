# Chapter 1: BSON Format & WiredTiger Storage Engine Architecture

## 1. BSON Serialization Mechanics

MongoDB stores and processes documents in **BSON** (Binary JSON). Unlike plain text JSON, BSON provides binary encoding that adds type information, element length prefixes, and optimized memory traversal capabilities.

```
+-----------------------------------------------------------------------+
|  Total Length (int32: 4 Bytes)                                        |
+---------------+---------------------+---------------------------------+
| Type (1 Byte) | Field Name (CString)| Value (Fixed/Variable Payload)  |
+---------------+---------------------+---------------------------------+
| Type (1 Byte) | Field Name (CString)| Value (Fixed/Variable Payload)  |
+---------------+---------------------+---------------------------------+
| 0x00 (Null Byte - End of Document)                                   |
+-----------------------------------------------------------------------+
```

### Why BSON over JSON?
1. **Fast Skipping / Zero-Copy Traversal:** Every variable-length field (strings, arrays, embedded documents) begins with a 4-byte length header. The MongoDB query engine can jump over fields without parsing internal characters.
2. **Rich Type Support:** Native support for `ObjectId` (12-byte unique key containing timestamp, machine identifier, process ID, incrementing counter), `Date` (64-bit UTC epoch ms), `Timestamp`, `Decimal128`, `BinData` (raw binary bytes).
3. **In-Memory Alignment:** Facilitates direct translation into C++ struct instances inside `mongod`.

---

## 2. WiredTiger Architecture Engine

Since MongoDB 3.2, **WiredTiger** is the default pluggable storage engine. WiredTiger is a high-performance, multi-threaded storage engine utilizing Multi-Version Concurrency Control (MVCC) and lock-free CPU primitives.

```
                   +-----------------------------------+
                   |     WiredTiger In-Memory Cache     |
                   |   (Uncompressed B-Tree Pages)     |
                   +-----------------+-----------------+
                                     |
                Eviction Thread      | Checkpoint Thread
              (LRU / Dirty Limit)   | (Every 60s)
                                     v
                   +-----------------------------------+
                   |     Disk File System Cache        |
                   |     (Compressed Block Pages)      |
                   +-----------------+-----------------+
                                     |
                                     | OS fsync
                                     v
                   +-----------------------------------+
                   |       Physical Storage SSD        |
                   |  (collection.wt / journal.log)    |
                   +-----------------------------------+
```

### WiredTiger RAM Cache Management
WiredTiger manages two separate memory areas:
1. **WiredTiger Cache:** Holds uncompressed, raw in-memory B-Tree pages.
2. **OS Page Cache:** Holds compressed disk blocks (Snappy or ZStandard compression).

By default, WiredTiger allocates cache memory based on:
$$\text{WiredTiger Cache GB} = \max\left(0.5 \times (\text{Total RAM} - 1\text{GB}), 256\text{MB}\right)$$

---

## 3. Lock-Free CPU Operations & Hazard Pointers

WiredTiger avoids coarse reader/writer locks on B-Tree memory pages using **Hazard Pointers**:
* **Hazard Pointers:** Reader threads register a atomic pointer to a memory page before reading.
* **Eviction Safety:** An eviction thread checking a dirty or clean page cannot free or modify a page if its hazard pointer count is $> 0$.
* This allows concurrent read operations to execute lock-free alongside active eviction routines.

---

## 4. Eviction Algorithms & Dirty Page Thresholds

WiredTiger's background eviction threads continuously maintain cache health by writing dirty pages to disk and freeing clean pages.

Key Configuration Parameters (`storage.wiredtiger.engineConfig`):
* `eviction_target` (default 80%): Target percentage of cache memory used before background eviction triggers.
* `eviction_trigger` (default 95%): Cache percentage threshold where client application threads are forced to assist in eviction (causes write latency spikes).
* `eviction_dirty_target` (default 5%): Target dirty page threshold.
* `eviction_dirty_trigger` (default 20%): Dirty page percentage where client writes stall to let eviction catch up.

---

## 5. Checkpoints & Write-Ahead Logging (WAL / Journal)

To maintain durability and guarantee crash resilience, WiredTiger combines **Checkpoints** with **Journaling**.

### Checkpoint Lifecycle
* Every 60 seconds (or when 2GB of journal data is written), WiredTiger creates a disk **Checkpoint**.
* A checkpoint presents a consistent, point-in-time snapshot of the dataset flushed to disk files (`collection*.wt`).
* Pages in checkpoints are immutable once committed.

### Journaling (Write-Ahead Log)
* Writes occur in memory and are appended to the in-memory journal buffer.
* The journal buffer is flushed to disk file `journal/WiredTigerLog.*` every 50ms (or immediately upon a write request with `j: true`).
* **Crash Recovery Flow:** On unexpected process death, MongoDB restores the last successful 60-second checkpoint, then replays the journal log entries committed since that checkpoint timestamp.
