# LSM-Trees vs. B+ Trees — Deep Dive & Implementation

> **Core Focus:** Storage Primitives, Write-Optimized LSM-Trees (MemTable, SSTables, Bloom Filters, Compaction) vs Read-Optimized B+ Trees, Write Amplification, and Disk I/O.

---

## 1. Storage Primitives Overview

Database storage engines fall into two primary categories based on how they write to disk:

1. **Write-Optimized (Log-Structured Merge-Tree / LSM-Tree):** Appends writes sequentially to memory and flushes sorted immutable segments to disk.
2. **Read-Optimized (B+ Tree):** Updates data in-place by breaking storage into fixed-size disk pages (typically 4KB–8KB).

---

## 2. LSM-Tree Architecture (Log-Structured Merge-Tree)

Used in high-throughput write databases like **RocksDB**, **LevelDB**, **Apache Cassandra**, and **Google Bigtable**.

```
Write Request ────► Write-Ahead Log (WAL on disk for durability)
                       │
                       ▼
                 MemTable (In-memory SkipList/Red-Black Tree)
                       │
            Flush when full (e.g. 64MB)
                       │
                       ▼
                 SSTable Level 0 (Sorted String Table on Disk)
                       │
            Background Compaction
                       │
                       ▼
                 SSTable Level 1, 2 ...
```

### Components of LSM-Tree:
1. **MemTable:** In-memory sorted data structure (SkipList or Red-Black Tree). All writes and updates land here first.
2. **Write-Ahead Log (WAL):** Sequential log file on disk used to restore MemTable if node crashes before flushing.
3. **SSTable (Sorted String Table):** Immutable on-disk file containing key-value pairs sorted by key.
4. **Bloom Filters:** Probabilistic data structure used on read path to check if a key exists in an SSTable before performing expensive disk reads.
5. **Compaction:** Background process that merges overlapping SSTables using MergeSort, removing tombstoned (deleted) or overwritten keys.
   - **Size-Tiered Compaction:** Merges SSTables of similar sizes (Cassandra default for write-heavy workloads).
   - **Leveled Compaction:** Organizes SSTables into key-range non-overlapping levels (RocksDB default for read-heavy workloads).

---

## 3. B+ Tree Architecture

Used in traditional RDBMS engines like **MySQL InnoDB**, **PostgreSQL**, and **Oracle**.

```
                           B+ TREE INDEX STRUCTURE
                                   [ 50 ]
                                 /        \
                            [ 20 ]        [ 70 ]
                           /      \      /      \
                      [10,15]   [30,40] [60]   [80,90]
                      (Leaf nodes linked sequentially -> Range Scan)
```

- Data is organized into a balanced tree of fixed-size disk pages.
- **Leaf Pages:** Store actual values or pointers to rows, connected sequentially via doubly-linked list for fast range scans.
- **In-Place Overwrites:** Updates modify existing pages directly on disk (or in Buffer Pool), requiring random I/O.

---

## 4. Architectural Comparison Matrix

| Property | LSM-Tree (Log-Structured) | B+ Tree (Page-Based) |
| :--- | :--- | :--- |
| **Write Performance** | ⚡ $O(1)$ Sequential I/O (Ultra-Fast) | 🐢 $O(\log N)$ Random I/O (Slower) |
| **Read Performance** | 🟡 $O(\log N)$ (May search multiple SSTables) | ⚡ $O(\log N)$ (Direct page lookup) |
| **Space Overhead** | Low (No page fragmentation, compressed) | Higher (Internal page fragmentation) |
| **Write Amplification** | Higher (Due to background compaction) | Lower (Only writes updated pages) |
| **Primary Use Cases** | Time-series, Logging, Analytics | Transactional OLTP, Financial DBs |
| **Example Systems** | RocksDB, LevelDB, Cassandra | MySQL (InnoDB), PostgreSQL, SQLite |
