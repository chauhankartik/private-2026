# Lucene in Action: 30-Second Engineering Cheatsheet

This cheatsheet aggregates Lucene low-level index file layouts, `TokenStream` attributes, `Directory` I/O characteristics, BKD spatial tree math, and segment merge bounds.

---

## 1. Lucene Index File Formats Architecture

| Extension | File Role | Data Structure & Contents |
| :--- | :--- | :--- |
| **`segments_N`** | Commit Point File | List of active immutable index segments and commit metadata. |
| **`.tim`** | Term Dictionary | Sorted term dictionary stored as an FST (Finite State Transducer). |
| **`.tip`** | Term Index | Index into `.tim` file for fast $O(1)$ RAM-cached term lookup. |
| **`.doc`** | Postings File | DocIDs containing terms + Term Frequencies (compressed via Variable Byte). |
| **`.pos`** | Positions File | Positions of terms within each document for phrase queries. |
| **`.pay`** | Payloads File | Arbitrary byte payloads stored per term position. |
| **`.fdt` / `.fdx`** | Stored Fields Data / Index | Stored document attributes compressed in blocks (LZ4 / ZSTD). |
| **`.dvd` / `.dvm`** | DocValues Data / Metadata | Column-oriented columnar data for sorting, faceting, and aggregations. |
| **`.kdm` / `.kdi`** | BKD Points Data / Index | Block K-d tree data for 1D-8D numeric & spatial range queries. |

---

## 2. Directory I/O Implementations Comparison

```
MMapDirectory  : Uses kernel mmap() syscall. Zero-copy page cache access. BEST FOR PRODUCTION LINUX!
NIOFSDirectory : Uses FileChannel.read(). Avoids thread lock contention on concurrent reads.
RAMDirectory   : Stores index entirely in JVM heap. Obsolete (High GC pause risk). Use MMapDirectory!
```

---

## 3. TokenStream Attributes Reference

```
Attribute Interface                         Function & Data Payload
------------------------------------------------------------------------------------------------------
CharTermAttribute                           Text string of current token (e.g. "algorithm")
PositionIncrementAttribute                  Distance from previous token (1 for normal, 0 for synonyms)
OffsetAttribute                             Character start & end offset in raw source document
PayloadAttribute                            Arbitrary BytesRef payload associated with token
```

---

## 4. Block K-d (BKD) Spatial Tree Bounds

For $N$ points in $D$-dimensional space (e.g., Lat/Lon 2D spatial points):
* **Build Time**: $O(N \log N)$
* **Space Complexity**: $O(N)$
* **Range Query Complexity**: $O(N^{1 - 1/D})$ for orthogonal range queries.

---

## 5. Segment Merging Mathematics

Under `TieredMergePolicy` with `maxMergeAtOnce = 10`:
$$\text{Merge Score} = \text{Roughness} \times \left( \text{Total Size} \right)^{\text{skew}}$$
Segments of roughly equal size are merged together into larger segments, maintaining $O(\log N)$ total segment count.
