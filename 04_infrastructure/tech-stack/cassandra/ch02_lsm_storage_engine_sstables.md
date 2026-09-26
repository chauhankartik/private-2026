# Chapter 2: LSM Storage Engine & SSTable Data Files

## 1. LSM-Tree Write Path Mechanics

Apache Cassandra uses a **Log-Structured Merge-tree (LSM-Tree)** storage engine optimized for ultra-high write throughput. Sequential write performance is achieved by avoiding random in-place disk mutations.

```
 Client Mutation
        |
        +-----------------------------------+
        |                                   |
        v Sequential Append                 v In-Memory Insertion
+-------------------+               +-------------------+
| CommitLog File    |               | Memtable Buffer   |
| (Disk WAL File)   |               | (SkipList Map)    |
+-------------------+               +---------+---------+
                                              |
                                 Memtable Flush (Threshold Exceeded)
                                              v
                                    +-------------------+
                                    | SSTable Files     |
                                    | (Immutable Disk)  |
                                    +-------------------+
```

---

## 2. CommitLog & Memtable Lifecycle

### 1. CommitLog (Write-Ahead Log)
* All incoming write mutations are sequentially appended to a disk **CommitLog** (`/var/lib/cassandra/commitlog/CommitLog-*.log`).
* `commitlog_sync`:
  * `periodic` (Default): Flushes CommitLog to disk every 10,000ms. High write throughput, 10-second potential data loss window on hardware failure.
  * `batch`: `fsync` executed every time a batch of writes accumulates or within `commitlog_sync_batch_window_in_ms` (e.g., 2ms). Guaranteed durability.

### 2. Memtable
* Concurrently, the mutation is written to the **Memtable**, an in-memory data structure backed by a `ConcurrentSkipListMap`.
* Data inside the Memtable is sorted strictly by **Partition Key** and **Clustering Column**.

---

## 3. SSTable File Components & On-Disk Format

When a Memtable exceeds its RAM threshold (`memtable_heap_space_in_mb` or `memtable_offheap_space_in_mb`), it flushes to disk as an immutable **SSTable** (Sorted String Table).

An SSTable consists of multiple co-located files on disk (`/var/lib/cassandra/data/keyspace/table-id/`):

```
+-----------------------------------------------------------------------+
| SSTable Component Files                                               |
+-------------------+---------------------------------------------------+
| nb-1-big-Data.db  | Raw row payloads, cell timestamps, deleted flags. |
+-------------------+---------------------------------------------------+
| nb-1-big-Index.db | Promoted index mapping Partition Key -> Byte Pos. |
+-------------------+---------------------------------------------------+
| nb-1-big-Summary  | In-memory sampled index pointers (every 128 keys) |
+-------------------+---------------------------------------------------+
| nb-1-big-Filter.db| Bloom Filter bitset array file.                  |
+-------------------+---------------------------------------------------+
| nb-1-big-TOC.txt  | Table of contents listing file component manifests|
+-------------------+---------------------------------------------------+
```

---

## 4. Tombstones & Deletion Mechanics

In an LSM-Tree, deletions cannot modify immutable SSTable disk files in place. Instead, deletions generate a special marker called a **Tombstone**.

```json
{
  "key": "user_101",
  "deletion_time": 1700000000,
  "tombstone": true,
  "gc_grace_seconds": 864000
}
```

### Tombstone Lifecycle & Garbage Collection
* **`gc_grace_seconds`** (default **864,000 seconds / 10 days**): The grace period during which a tombstone must remain intact across the cluster before being physically purged during SSTable compaction.
* **Tombstone Hazard:** If a tombstone is garbage collected *before* all replica nodes receive the delete mutation (e.g., a node was offline for 11 days), the resurrecting node will re-replicate the deleted row back to the cluster (**Zombie Data**).
