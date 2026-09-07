# Chapter 6: CQL Data Modeling & Production Anti-Patterns

## 1. Query-First Data Modeling Philosophy

Data modeling in Cassandra follows a completely inverse methodology compared to relational database modeling (RDBMS):

```
 RDBMS Modeling Flow:  Entities -> 3NF Normalization -> Foreign Keys -> Write Generic SQL Queries
 Cassandra Flow:       Identify Application Queries -> Model 1 Table Per Query -> Denormalize Data
```

### Core Design Rules:
1. **Model Around Access Patterns:** Every table should be custom-designed to satisfy one specific CQL query application access pattern.
2. **Embrace Denormalization:** Duplicate data across multiple tables to eliminate cross-table JOINs at query runtime (Cassandra does not support relational JOIN operations).
3. **Partition Size Management:** Keep partition file size on disk under **100 MB** and partition row count under **100,000 rows**.

---

## 2. Production Anti-Patterns

### Anti-Pattern 1: Unbounded Partitions (Hot Partitions)
```sql
-- BAD SCHEMA: All log records written to a single static partition key!
CREATE TABLE app_logs (
    log_type text,         -- e.g., 'ERROR' (All errors go to 1 partition!)
    log_time timestamp,
    message text,
    PRIMARY KEY (log_type, log_time)
);
```
* **Consequence:** Millions of writes flood a single cluster node. The partition grows to tens of gigabytes, causing JVM GC pauses, heap exhaustion, and SSTable compaction failures.
* **Fix (Bucketing):** Add a date or random bucket ID to split partitions across multiple nodes:
```sql
-- GOOD SCHEMA: Partition key bucketed by day!
CREATE TABLE app_logs (
    log_type text,
    log_date text,         -- e.g., '2026-09-07'
    log_time timestamp,
    message text,
    PRIMARY KEY ((log_type, log_date), log_time)
);
```

---

### Anti-Pattern 2: `ALLOW FILTERING` Hazard
```sql
-- HIGHLY DANGEROUS QUERY IN PRODUCTION
SELECT * FROM user_orders WHERE order_status = 'COMPLETED' ALLOW FILTERING;
```
* **Consequence:** Forces Cassandra to bypass token ring partition routing and execute a full table scan across **all SSTables on every node in the cluster**, scanning gigabytes of disk data to filter a few matching records.

---

### Anti-Pattern 3: Secondary Indexes on High/Low Cardinality Fields
```sql
-- BAD PRACTICE: Secondary index on boolean or high-cardinality GUID fields
CREATE INDEX ON users (is_active);
```
* **Consequence:** Secondary indexes in Cassandra are **Local Secondary Indexes** built per node. Querying an indexed field forces a scatter-gather query to every node in the cluster, completely destroying performance.

---

### Anti-Pattern 4: Queue/State-Machine Table Anti-Pattern
Using a Cassandra table as a message queue with frequent `INSERT` followed by immediate `DELETE` of processed tasks.
* **Consequence:** Generates millions of **Tombstones** per day. Read queries scanning for active tasks encounter thousands of tombstone markers, resulting in:
  `InvalidRequestException: Read 10001 tombstones in app_queue... (see tombstone_failure_threshold)`
