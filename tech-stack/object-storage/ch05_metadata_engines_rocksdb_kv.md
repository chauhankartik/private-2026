# Chapter 5: Decoupled Data & Metadata Engines & Indexing Mechanics

## 1. Decoupled Architecture: Raw Data vs Metadata Storage

Scalable object stores separate the physical storage of binary data payloads from object metadata catalog operations.

```
                         Client HTTP REST Request
                                    |
                                    v
                         S3 Proxy Gateway Layer
                                    |
            +-----------------------+-----------------------+
            |                                               |
            v Key Lookup & Permission Check                 v Raw Stream Read/Write
 +-------------------------------------+         +-------------------------------------+
 | Metadata Engine                     |         | Data Payload Storage                |
 | (RocksDB / LSM KV Store)            |         | (Raw Disk Blocks / Ceph BlueStore)  |
 | - Key: "bucket/photo.jpg"           |         | - Block 010101...                   |
 | - Value: JSON (Size, ETag, ACLs,    |         | - Block 010102...                   |
 |   Block Map Array Pointers)         |         |                                     |
 +-------------------------------------+         +-------------------------------------+
```

### Why Decouple Metadata?
* **Optimized I/O Patterns:** Metadata updates require low-latency, random key lookups and atomic mutations. Payload storage requires high-throughput sequential block streaming.
* **Independent Scaling:** Metadata clusters can be hosted on fast NVMe SSD drives, while raw data payloads are placed on dense HDDs or erasure-coded storage nodes.

---

## 2. RocksDB & LSM-Tree Metadata Indexing

Modern object storage backends (e.g., Ceph RGW, MinIO, open-source object stores) utilize **Log-Structured Merge-tree (LSM-Tree)** key-value stores like **RocksDB** to store bucket manifests.

```
 Keys inside RocksDB Metadata Index (Sorted Lexicographically):
 Key: "mybucket/logs/2026-01-01.json"  => Value: { size: 4096, etag: "...", blocks: [102, 103] }
 Key: "mybucket/logs/2026-01-02.json"  => Value: { size: 8192, etag: "...", blocks: [104, 105] }
 Key: "mybucket/user/avatar.png"       => Value: { size: 1024, etag: "...", blocks: [106] }
```

### Bucket Key Listing & Prefix Pagination (`ListObjectsV2`)
Because LSM-Trees maintain keys in strict **lexicographical order**, executing an S3 bucket listing request with a `prefix` parameter (e.g., `GET /mybucket?prefix=logs/`) executes a fast range scan over contiguous RocksDB index keys:

$$\text{Range Scan Target}: [\text{"mybucket/logs/"}, \text{"mybucket/logs/\xFF"})$$

* **Directory Emulation (`delimiter="/"`)**: Object stores emulate folders without directory inodes by scanning lexicographical key strings up to the specified delimiter character (`/`).

---

## 3. S3 Select & Computational Pushdown Filtering

When querying large analytical data files (such as 10GB CSV, JSON, or Apache Parquet objects), downloading the full object over HTTP to client memory creates severe network bottlenecks.

**S3 Select** enables pushing computational filtering down to the object storage nodes.

```
 Client Application                                             Object Storage Node
        |                                                               |
        | --- S3 Select Query Request --------------------------------->|
        |     SELECT * FROM s3object s WHERE s.age > 30                 |
        |                                                               |
        |                                                     1. Scans Parquet / CSV File on Storage Disk
        |                                                     2. Executes SIMD Filter (Apache Arrow / Velox)
        |                                                     3. Extracts matching rows only
        |                                                               |
        |<--- Stream Filtered Result Records Only (e.g. 50KB Payload) --|
```

* **Performance Improvement:** Reduces network bandwidth consumption and client deserialization overhead by up to **99%**.
