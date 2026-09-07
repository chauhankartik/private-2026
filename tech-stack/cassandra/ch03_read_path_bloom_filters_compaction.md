# Chapter 3: Read Path Execution & Compaction Strategies

## 1. The Multi-Tiered Read Path

Because writes are distributed across multiple immutable SSTables, executing a read query requires inspecting both RAM memory structures and multiple disk files to reconstruct the latest row state based on cell timestamps.

```
                   Read Query for (Key K)
                             |
                             v
                  Check Row Cache (RAM) ----------(Hit)----------> Return Row Payload
                             | (Miss)
                             v
                  Check Memtable (RAM) -----------(Hit)----------> Merge Cell State
                             |
                             v
           Check Bloom Filter (Filter.db in RAM) --(Negative)----> Skip SSTable File
                             | (Positive - May be False Positive)
                             v
               Check Key Cache (RAM Offsets)
                             |
               (Miss)        v        (Hit)
     Check Partition Summary (Summary.db in RAM)
                             |
                             v
       Check Partition Index (Index.db on Disk)
                             |
                             v
       Read SSTable Data File (Data.db on Disk)
                             |
                             v
            Merge Results from All SSTables
          (Resolve Highest Cell Timestamps)
                             |
                             v
                     Return Final Row
```

---

## 2. Bloom Filters Mechanics (`Filter.db`)

A **Bloom Filter** is a probabilistic, space-efficient data structure stored in memory for every SSTable file to determine whether a given partition key definitely does *not* exist in that SSTable.

### False Positive Probability Formula
$$\text{False Positive Rate } P \approx \left(1 - e^{-kn/m}\right)^k$$
Where $m$ is bit array length, $n$ is inserted keys count, and $k$ is hash functions count.

* **Negative Result:** Partition key is **guaranteed not to exist** in this SSTable file (0 disk I/O operations executed).
* **Positive Result:** Key *probably* exists in this SSTable (proceed to inspect Key Cache & Disk Index).

```sql
-- Tuning Bloom Filter False Positive Chance in CQL
ALTER TABLE users WITH bloom_filter_fp_chance = 0.01;
```

---

## 3. SSTable Compaction Strategies

Compaction is the background process of merging multiple SSTables into a single new SSTable, purging overwritten cell versions, and removing tombstoned records whose age exceeds `gc_grace_seconds`.

```
 SSTable 1: [ Key A (v1, t=1), Key B (v1, t=2) ]
 SSTable 2: [ Key A (v2, t=5), Key C (v1, t=3) ]
 SSTable 3: [ Key B (Tombstone, t=6)           ]
                       |
                       v Merged SSTable Compaction
 SSTable New: [ Key A (v2, t=5), Key C (v1, t=3) ]  (Key B purged!)
```

### 1. Size-Tiered Compaction Strategy (STCS)
* **Mechanics:** Groups SSTables of similar file sizes into sets. When a set contains 4 SSTables (default), they are merged into 1 larger SSTable.
* **Best Used For:** Heavy write workloads.
* **Drawback:** High **Disk Space Overhead**. Requires up to $50\%$ free disk space during major compactions.

### 2. Leveled Compaction Strategy (LCS)
* **Mechanics:** Divides SSTables into fixed-size levels ($L_0, L_1, L_2 \dots$). Each level is 10x larger than the previous level ($L_1 = 10\text{MB}, L_2 = 100\text{MB}, L_3 = 1\text{GB}$). SSTables in $L_1+$ have non-overlapping partition key ranges.
* **Best Used For:** Heavy read workloads. $90\%$ of reads are satisfied from a single SSTable per level.
* **Drawback:** High **Write Amplification** (significant CPU and disk I/O cost during merges).

### 3. Time-Window Compaction Strategy (TWCS)
* **Mechanics:** Groups SSTables into discrete time windows based on document insertion timestamps. Compaction only merges SSTables within the same time window.
* **Best Used For:** Time-series datasets, append-only logs, and data with Time-To-Live (TTL) expiration.
