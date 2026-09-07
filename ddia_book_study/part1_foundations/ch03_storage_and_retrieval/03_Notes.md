# Chapter 3: Storage and Retrieval — Deep Dive Notes

> **Core Theme:** How databases store data on disk and how they retrieve it. The fundamental trade-off between **Write Throughput** (LSM-Trees) and **Read Throughput** (B-Trees), plus OLTP vs OLAP column-oriented storage.

---

## 1. The World’s Simplest Database

```bash
#!/bin/bash
db_set () {
    echo "$1,$2" >> database.db
}

db_get () {
    grep "^$1," database.db | tail -n 1 | cut -d,# -f 2
}
```

- **Write Performance:** $O(1)$ append-only. Extremely fast!
- **Read Performance:** $O(N)$ full scan. Extremely slow as database grows.
- **Solution:** Add an **Index** (A side data structure that speeds up reads, but slows down writes).

---

## 2. Hash Indexes (Bitcask Engine)

- Keeps an in-memory hash map of `key -> byte_offset` in an append-only log file.
- **Pros:** Ultra-fast $O(1)$ reads and writes.
- **Cons:** 
  1. Hash map must fit entirely in RAM.
  2. Range queries (e.g. `key >= 100 AND key <= 200`) are inefficient ($O(N)$ scan).

---

## 3. SSTables and LSM-Trees (Log-Structured Merge-Tree)

Used in: **RocksDB, LevelDB, Apache Cassandra, Google Bigtable**.

### Key Concept:
Sorted String Tables (**SSTables**) require keys to be sorted within log segments.

### Operation Workflow:
1. **In-Memory Write (MemTable):** Incoming writes are added to an in-memory balanced tree (Red-Black Tree or SkipList) called the **MemTable**.
2. **Flush to Disk (SSTable):** When the MemTable exceeds a threshold (e.g. 64MB), it is written to disk as a new **SSTable file**, sorted by key.
3. **Read Path:** Check MemTable first $\rightarrow$ if not found, check recent SSTables using **Bloom Filters** to avoid unnecessary disk reads.
4. **Compaction:** Background process merges overlapping SSTable segments using merge sort ($O(N)$), discarding deleted (tombstoned) or overwritten values.

```
Write Request ──► MemTable (SkipList in RAM)
                       │
             Flush when full (64MB)
                       │
                       ▼
             SSTable Segment (Sorted on Disk)
                       │
              Background Compaction
```

---

## 4. B-Trees (The Standard OLTP Storage Engine)

Used in: **PostgreSQL, MySQL (InnoDB), Oracle, SQLite**.

### Key Concept:
Break database down into fixed-size **pages** (traditionally 4KB or 8KB) and update pages **in-place** on disk.

```
                           [ Root Page ]
                          /      |      \
                  [ Page 1 ] [ Page 2 ] [ Page 3 ]
                 /   |   \
            [ Leaf 1 ][ Leaf 2 ][ Leaf 3 ]  <-- Contains actual rows or pointers
```

- **Lookups:** $O(\log N)$ traversal from root page down to leaf page.
- **Writes:** Find page containing key, overwrite in-place. If page is full, split page into two half-full pages.
- **Crash Recovery:** Uses a **Write-Ahead Log (WAL)** / Redo Log. Every write must append to WAL on disk before modifying the B-Tree page.

---

## 5. LSM-Tree vs B-Tree Direct Comparison

| Dimension | LSM-Tree (RocksDB / Cassandra) | B-Tree (InnoDB / Postgres) |
|---|---|---|
| **Write Throughput** | 🚀 **Significantly Higher** (Sequential WAL & MemTable) | 🐢 Lower (Random disk overwrites + WAL) |
| **Read Throughput** | 🐢 Slower (May check multiple SSTables + Bloom Filters) | 🚀 **Faster** (Direct single page lookup) |
| **Space Overhead** | 🚀 Lower (Sequential SSTable compression) | 🐢 Higher (Page fragmentation / empty space) |
| **Write Amplification** | Compaction repeatedly rewrites SSTable files | Page splitting and WAL writes |
| **Primary Advantage** | High-volume write workloads | Transactional OLTP point reads & range scans |

---

## 6. OLTP vs OLAP & Column-Oriented Storage

| Property | OLTP (Online Transaction Processing) | OLAP (Online Analytics Processing) |
|---|---|---|
| **Read Pattern** | Small number of records per query (by key) | Aggregate over millions of rows for few columns |
| **Write Pattern** | Random low-latency user updates | Bulk load or real-time event streaming |
| **Storage Layout** | **Row-Oriented** (Store all column values of a row together) | **Column-Oriented** (Store all values of a column together) |
| **Db Systems** | PostgreSQL, MySQL, Spanner | ClickHouse, Snowflake, Redshift, BigQuery |

### Why Column-Oriented Storage Wins for Analytics:
If a query evaluates `SELECT AVG(age), SUM(income) FROM users`, row-oriented storage must load all 100 columns from disk into RAM. Column-oriented storage reads **only** the `age` and `income` column byte files from disk, resulting in 90%+ disk I/O savings and hyper-efficient SIMD vector compression!
