# Chapter 3: Document Indexing & Write Execution Path

## 1. End-to-End Write Execution Flow

Executing a document index request (`POST /index/_doc/101`) triggers a multi-stage pipeline across coordinating nodes, primary shards, and replica shards.

```
 Client Index Request
        |
        v
 +----------------------------+
 | Coordinating Node          |
 | 1. Computes Shard Routing  |
 +--------------+-------------+
                |
                v Forward Write Request to Primary Shard
 +--------------+--------------------------------------------------------+
 | Primary Shard Data Node                                               |
 |                                                                       |
 |  +-----------------------+              +--------------------------+  |
 |  | In-Memory Index Buffer|              | Transaction Log (Translog|  |
 |  | (Unsearchable RAM)    |              | (Write-Ahead Disk File)  |  |
 |  +-----------+-----------+              +-------------+------------+  |
 |              |                                        |               |
 +--------------|----------------------------------------|---------------+
                |                                        |
      2. Concurrent Replica Writes                       |
                |                                        |
                +-------------------+- - - - - - - - - - +
                                    |
                                    v 3. Acknowledgment
                           Client Response Ack
```

---

## 2. Refresh vs Flush Mechanics

To provide real-time search capabilities while preserving SSD durability, Lucene decouples segment creation from physical disk synchronization (`fsync`).

```
 +------------------------+
 | In-Memory Index Buffer |
 +-----------+------------+
             |
             | Refresh (Every 1s by default)
             v
 +------------------------+
 | OS Page Cache Segment  |  <--- SEARCHABLE AT THIS POINT! (Near Real-Time Search)
 | (Immutable Memory)     |
 +-----------+------------+
             |
             | Flush (Every 30m or Translog > 512MB)
             v
 +------------------------+
 | Physical SSD Disk      |  <--- PERSISTENT & COMMITTED TO DISK
 | Lucene Segment File    |
 +------------------------+
```

### 1. Refresh (`index.refresh_interval: 1s`)
* Flushes the in-memory index buffer to a new **Lucene Segment** inside the **OS Page Cache**.
* The newly created segment is opened for reading, making the document searchable within **1 second** of indexing (**Near Real-Time Search**).
* **Durability Note:** Documents in OS page cache segments are *not* yet written to physical disk.

### 2. Flush (`index.translog.flush_threshold_size: 512mb`)
* Executes a Lucene `commit()`.
* Issue an `fsync()` system call to write all OS Page Cache segments permanently to physical storage SSDs.
* Truncates and clears the Transaction Log (`Translog`).

---

## 3. Transaction Log (Translog) & Crash Recovery

Because `Refresh` writes segments to volatile RAM (OS page cache), an un-flushed node crash would result in data loss. The **Translog** prevents data loss:

* Every write operation is appended to the `Translog` on disk before returning success to the primary shard.
* **Translog Durability (`index.translog.durability`):**
  * `request` (Default): `fsync` Translog to disk after *every* indexing request/bulk batch. Zero data loss guarantee.
  * `async`: `fsync` Translog periodically (`index.translog.sync_interval: 5s`). High indexing throughput, 5-second potential data loss window.
* **Crash Recovery Flow:** On restart, Lucene opens the last committed disk checkpoint and replays all operations recorded in the Translog since that checkpoint timestamp.

---

## 4. Segment Merging (`TieredMergePolicy`)

Because Lucene segments are immutable, every `Refresh` operation creates a new mini-segment. Unchecked segment growth would degrade search performance as queries would have to scan thousands of small files.

### Background Segment Merging:
* Background merge threads select similarly-sized small segments and merge them into a single larger segment based on `TieredMergePolicy`.
* **Garbage Collection of Deleted Documents:** When documents are updated or deleted, Lucene writes a bit marker to a `.del` bitset file. Deleted documents are physically purged from disk only during segment merging.
