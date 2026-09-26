# Part I: Storage Engines — LSM-Trees, MemTables & Compaction

Log-Structured Merge-Trees (LSM-Trees) convert random disk writes into high-throughput sequential appends.

---

## 📌 LSM-Tree Write and Read Data Paths

```mermaid
flowchart TD
    subgraph Write Path
        W["Client Write (Put Key, Value)"] --> WAL["1. Append to WAL (Disk)"]
        W --> MemTable["2. Insert into MemTable (SkipList in RAM)"]
        MemTable -- Full (~64MB) --> Flush["3. Flush to Disk as Immutable L0 SSTable"]
    end

    subgraph Read Path
        R["Client Read (Get Key)"] --> R_Mem["1. Search MemTable"]
        R_Mem -- Found --> Return["Return Value"]
        R_Mem -- Not Found --> R_Bloom["2. Check Bloom Filters for SSTables"]
        R_Bloom -- Positive --> R_SST["3. Search Index Block in L0..LN SSTables"]
        R_SST --> Return
    end
```

---

## 📌 SSTable Binary Layout

An **SSTable (Sorted String Table)** is an immutable disk file storing key-value pairs sorted by key.

```mermaid
flowchart LR
    subgraph SSTable File Architecture
        DataBlock1["Data Block 0 (K/V Pairs)"]
        DataBlock2["Data Block 1 (K/V Pairs)"]
        IndexBlock["Index Block (Routing Keys & Offsets)"]
        BloomFilter["Bloom Filter Block"]
        Footer["Footer (Fixed Size: Pointers to Index & Bloom)"]
    end

    Footer --> IndexBlock
    Footer --> BloomFilter
    IndexBlock --> DataBlock1
    IndexBlock --> DataBlock2
```

---

## 📌 Compaction Strategies: Size-Tiered vs Leveled Compaction

Because SSTables are immutable, multiple SSTables can contain duplicate or deleted (tombstone) entries for the same key. **Compaction** merges SSTables in the background to reclaim space and maintain read performance.

### 1. Size-Tiered Compaction (STCS — Cassandra Default)
Accumulates SSTables of similar sizes. When $N$ SSTables of similar size exist, merge them into 1 larger SSTable.
- **Pros**: Fast writes, low write amplification.
- **Cons**: High space amplification (requires up to 50% free disk space for compaction!).

### 2. Leveled Compaction (LCS — RocksDB / LevelDB Default)
Organizes SSTables into discrete levels ($L_0, L_1, L_2, \dots$). Each level has a fixed total size cap ($L_1 = 10\text{MB}, L_2 = 100\text{MB}, L_3 = 1\text{GB}$).
- Key ranges within Level 1..$N$ are **strictly non-overlapping**.
- When Level $N$ exceeds its size cap, select an SSTable and merge-sort it with overlapping key-range SSTables in Level $N+1$.

```mermaid
flowchart TD
    subgraph Leveled Compaction Flow
        L0["Level 0: [1..50], [20..70] (Overlapping Ranges)"]
        L1["Level 1 (10MB Cap): [1..20], [21..40], [41..60], [61..80] (Non-overlapping)"]
        L2["Level 2 (100MB Cap): [1..100], [101..200]..."]
    end

    L0 -- Merge Compaction --> L1
    L1 -- Merge Compaction --> L2
```
