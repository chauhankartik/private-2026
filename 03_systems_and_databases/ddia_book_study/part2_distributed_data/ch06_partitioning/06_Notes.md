# Chapter 6: Partitioning — Deep Dive Notes

> **Core Theme:** Splitting a large database into smaller subsets called **Partitions (Shards)** across multiple nodes. Comparing **Key-Range** vs. **Hash** partitioning, **Consistent Hashing**, **Secondary Indexing**, and **Rebalancing** strategies.

---

## 1. Partitioning vs. Replication

- **Replication:** Copies of the same data are stored on multiple nodes (For redundancy & availability).
- **Partitioning (Sharding):** Dataset is divided into distinct subsets so different nodes store different pieces of data (For scalability).
- A partition is called a **Shard** in MongoDB/Elasticsearch, a **Tablet** in Bigtable, or a **Vnode** in Cassandra.

```
Node 1: [Partition A] [Partition B]
Node 2: [Partition C] [Partition D]
```

---

## 2. Partitioning Strategies

The goal of partitioning is to spread data and query load evenly across nodes. If partitioning is uneven, it creates a **Hotspot** / **Skewed Load**.

### 1. Key-Range Partitioning
Assign a continuous range of keys (from min to max) to each partition (e.g., Keys `A-C` on Node 1, `D-F` on Node 2).

- **Used In:** Google Bigtable, Apache HBase, RethinkDB.
- **Pros:** Efficient Range Queries (`SELECT * FROM sensor_data WHERE timestamp >= '2026-01-01' AND timestamp <= '2026-01-31'`).
- **Cons:** **Hotspots on Sequential Writes.** If key is timestamp, all write traffic for "today" hits a single partition while other nodes remain idle.

---

### 2. Hash Partitioning (Hash of Key)
Pass the key through a good hash function (e.g., Murmur3, MD5) and assign range of hashes to each partition.

- **Used In:** Apache Cassandra, DynamoDB, MongoDB.
- **Pros:** Evenly distributes load across partitions; eliminates sequential timestamp hotspots.
- **Cons:** **Destroys Range Queries.** Adjacent keys are scattered across different partitions. A range query becomes a costly **Scatter-Gather** scan across all nodes.

---

### 3. Consistent Hashing
A specialized hash partitioning approach where partition boundaries are mapped along a circular hash ring ($0 \dots 2^{32}-1$).

- **Key Principle:** Nodes and data keys are mapped to positions on the ring. A key is assigned to the first node encountered moving clockwise.
- **Benefit:** When a node joins or leaves the cluster, only $1/N$ of keys are moved between nodes, avoiding massive data shuffling.

---

### 4. Skewed Workloads & Hotspot Mitigation
If a single key is extremely popular (e.g., Taylor Swift's social media post with millions of comments), hash partitioning still routes all writes for that key to one partition.

- **Solution (Salting the Key):** Append a 2-digit random number (`00` to `99`) to the partition key. Writes for that key are distributed across 100 separate partitions. Reads must read from all 100 partitions and combine results.

---

## 3. Partitioning and Secondary Indexes

Secondary indexes do not identify a record uniquely; they search for occurrences of a specific attribute (e.g., `color = 'red'`).

```
                              DOCUMENT vs TERM INDEXING
                              
      Document-Partitioned (Local)                 Term-Partitioned (Global)
      ┌─────────────────────────┐                 ┌─────────────────────────┐
      │ Partition 1             │                 │ Partition 1             │
      │ Data: Cars 1..100       │                 │ Data: Cars 1..300       │
      │ Index: Local cars 1..100│                 │ Index: Term 'Red' (All) │
      └─────────────────────────┘                 └─────────────────────────┘
```

### 1. Document-Partitioned Index (Local Index)
Each partition maintains its own secondary indexes independently for the documents inside that partition.
- **Write Path:** Ultra-fast ($O(1)$ local index update).
- **Read Path:** **Scatter-Gather.** To search `color = 'red'`, client must query **EVERY** partition in parallel and merge results. Highly susceptible to tail-latency amplification!

### 2. Term-Partitioned Index (Global Index)
The secondary index is partitioned globally across all nodes by the index term itself (e.g., terms starting with `A-N` on Node 1, `O-Z` on Node 2).
- **Read Path:** Ultra-fast (Query hits a single partition that owns the search term).
- **Write Path:** Slow & Complex. Writing a record requires updating global index partitions on remote nodes via asynchronous pipelines or 2PC.

---

## 4. Rebalancing Strategies

As dataset size grows or nodes fail/join, data must be rebalanced across nodes.

### Rebalancing Anti-Pattern: `hash(key) mod N`
If $N$ (number of nodes) changes from 10 to 11, `hash(key) mod 10` $\neq$ `hash(key) mod 11` for almost every key. Rebalancing causes **massive data movement** of ~90% of the entire database!

### Good Rebalancing Strategies:

1. **Fixed Number of Partitions (Elasticsearch, Riak, Couchbase):**
   - Create far more partitions than nodes (e.g., 1000 partitions for 10 nodes = 100 partitions/node).
   - When a new node joins, it steals a few whole partitions from each existing node. Total number of partitions remains constant.

2. **Dynamic Partitioning (HBase, MongoDB):**
   - When a partition grows beyond a configured size limit (e.g. 10GB), it splits into two equal partitions.
   - When partitions shrink, they are merged.

3. **Partitioning Proportional to Nodes (Cassandra):**
   - Keep a fixed number of partitions *per node*. Total number of partitions grows proportionally as nodes are added.
