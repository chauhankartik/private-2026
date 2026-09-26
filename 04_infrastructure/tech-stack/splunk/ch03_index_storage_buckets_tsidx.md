# Chapter 3: Index Storage Engine, Bucket Lifecycle & `.tsidx` Files

Splunk's indexing engine organizes event data on disk inside directories called **Buckets**, pairing compressed raw log data with time-series keyword indexes.

---

## 1. Bucket Lifecycle Architecture

An Index consists of a collection of buckets moving through five distinct lifecycle states:

```
[ Ingest Stream ]
       |
       v
  +----------+   Roll (Size > 10GB / Age > 24h)   +----------+
  |   HOT    | ---------------------------------> |   WARM   |
  | (Writes) |                                    | (Read)   |
  +----------+                                    +----------+
                                                       |
                                                       | Roll (Warm Count > maxWarmDBCount)
                                                       v
  +----------+   Unfreeze / Rebuild               +----------+
  |  THAWED  | <--------------------------------- |   COLD   |
  | (Search) |                                    | (ColdDir)|
  +----------+                                    +----------+
                                                       |
                                                       | Age > frozenTimePeriodInSecs
                                                       v
                                                  +----------+
                                                  |  FROZEN  | (Archived or Deleted)
                                                  +----------+
```

### 1.1 Bucket Directory Naming Convention
Bucket directories follow strict naming patterns encoding timestamp spans and bucket IDs:
```text
db_<latest_time>_<earliest_time>_<bucket_id>
Example: db_1710086400_1710000000_12
```

---

## 2. Anatomy of a Splunk Bucket Directory

Inside a bucket directory on an Indexer node:

```
/var/lib/splunk/defaultdb/db/db_1710086400_1710000000_12/
├── rawdata/
│   └── journal.gz         <--- Compressed raw log text & metadata headers
├── Hosts.meta             <--- Metadata mapping for host values
├── Sources.meta           <--- Metadata mapping for source values
├── Sourcetypes.meta       <--- Metadata mapping for sourcetype values
├── Strings.meta           <--- Lexicographical token string dictionary
└── 1710000000-1710086400-123456789.tsidx  <--- Inverted Keyword Index File
```

### 2.1 The `.tsidx` File (Time-Series Index)
* The `.tsidx` file is a B-tree inverted index.
* It breaks log events into individual keywords (lexicographical tokens) and maps each keyword to the **exact byte offset within `journal.gz`**.
* When a user runs `search index=main "Connection Refused"`, the Search Head queries `.tsidx` first, identifies matching byte offsets, and reads *only* those specific compressed bytes from `journal.gz`, avoiding full disk scans.

---

## 3. Index Clustering: Replication & Search Factors

In a Splunk Indexer Cluster, the Manager Node orchestrates bucket placement across Indexer instances using **Replication Factor (RF)** and **Search Factor (SF)**.

```
Replication Factor (RF = 3)  ===> 3 Copies of rawdata/journal.gz
Search Factor (SF = 2)       ===> 2 Copies of .tsidx Index Files
```

```
[ Manager Node (Cluster Manager) ]
       |
       +---> Indexer 1 : Bucket 12 (PRIMARY - Contains journal.gz + .tsidx)
       +---> Indexer 2 : Bucket 12 (SEARCHABLE COPY - Contains journal.gz + .tsidx)
       +---> Indexer 3 : Bucket 12 (NON-SEARCHABLE COPY - Contains journal.gz ONLY)
```

* **Primary Copy:** Handles active search queries.
* **Searchable Copy:** Ready to take over searches instantly if Indexer 1 crashes.
* **Non-Searchable Copy:** Storage-optimized rawdata copy. If Indexer 2 also fails, Indexer 3 generates `.tsidx` from `journal.gz` to restore Search Factor.

---

## 4. Staff Engineer Index Storage Rules
1. **Size Hot Buckets for RAM Cache:** Keep Hot bucket counts reasonable (`maxMemMB = 20`) so index metadata fits in Linux OS Page Cache.
2. **Use SmartStore for Cloud Scale:** Migrate local Cold bucket paths to **Splunk SmartStore** (S3/GCS object storage backend), allowing Indexer compute nodes to scale independently of storage capacity.
3. **Never Manually Delete Bucket Files:** Deleting `.tsidx` files directly via `rm` corrupts index manifests. Use `splunk clean` or adjust retention policies in `indexes.conf`.
