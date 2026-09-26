# Range-Based vs. Hash Sharding — Deep Dive & Implementation

> **Core Focus:** Key-Range Sharding vs Hash Sharding, Hotspot Mitigation, Scatter-Gather Queries, Dynamic Resharding, and Compound Sharding Keys.

---

## 1. Overview of Data Sharding

Sharding divides a large dataset horizontally into independent database partitions called **Shards**.

```
Database Dataset: Records 1 .. 1,000,000
 ├── Shard 1: Node A (Records 1 .. 333,000)
 ├── Shard 2: Node B (Records 333,001 .. 666,000)
 └── Shard 3: Node C (Records 666,001 .. 1,000,000)
```

---

## 2. Key-Range Sharding

Assigns contiguous ranges of keys to each shard (e.g. `A-D` to Shard 1, `E-H` to Shard 2).

- **Pros:** Efficient Range Queries (`WHERE timestamp >= '2026-01-01' AND timestamp <= '2026-01-31'`).
- **Cons:** **Hotspots on Monotonic Keys.** Inserting auto-incrementing IDs or current timestamps causes 100% of write traffic to hit the last shard.
- **Implementations:** Google Bigtable, Apache HBase, CockroachDB ranges.

---

## 3. Hash Sharding

Applies a hash function (MD5, Murmur3) to the shard key:

$$\text{Shard ID} = \text{hash}(\text{Shard Key}) \pmod{\text{Total Shards}}$$

- **Pros:** Uniform write distribution across all shards; eliminates timestamp hotspots.
- **Cons:** **Scatter-Gather Range Scans.** Range queries must search **ALL** shards in parallel and merge results.
- **Implementations:** Apache Cassandra, Amazon DynamoDB, MongoDB Hash Keys.

---

## 4. Compound Sharding Keys (Hybrid Approach)

To support both uniform write distribution AND efficient range queries for a subset of data, databases like Cassandra use **Compound Keys**:

$$\text{Key} = (\underbrace{\text{Partition Key}}_{\text{Hashed for Routing}}, \quad \underbrace{\text{Clustering Columns}}_{\text{Sorted inside Partition}})$$

### Example Schema (Cassandra Sensor Data):
- `Partition Key = user_id` $\to$ Hashes and routes all data for `user_id` to a single shard.
- `Clustering Key = timestamp` $\to$ Sorts data chronologically inside that shard.
- **Result:** Range queries for a specific `user_id` hit only 1 shard!

---

## 5. Architectural Comparison Matrix

| Feature | Range Sharding | Hash Sharding | Compound Sharding Key |
| :--- | :--- | :--- | :--- |
| **Write Distribution** | Skewed on monotonic keys | Highly Uniform | Uniform across Partition Keys |
| **Range Queries** | Fast single-shard lookup | Slow Scatter-Gather scan | Fast within Partition Key |
| **Resharding Complexity** | Dynamic Partition Splitting | Consistent Hashing Vnodes | Vnodes per Partition Key |
| **Best For** | Time-series, Analytical scans | High-throughput OLTP key-value | User-centric timeline feeds |
