# Chapter 3: Sharding Architecture & Chunk Balancing Mechanics

## 1. Distributed Sharding Architecture

MongoDB Sharding distributes dataset collections horizontally across multiple Replica Sets (Shards) to support datasets exceeding single-node RAM and storage capacities.

```
                            +--------------------+
                            |   Client Application|
                            +---------+----------+
                                      |
                                      v
                            +--------------------+
                            |   mongos Router    |
                            | (Stateless Proxy)  |
                            +---+------------+---+
                                |            |
         Cache Metadata Lookup  |            | Direct Query Routing
                                v            |
                      +-------------------+  |
                      | Config Server CSRS|  |
                      | (3-node Repl Set) |  |
                      +-------------------+  |
                                             |
         +-----------------------------------+-----------------------------------+
         |                                   |                                   |
         v                                   v                                   v
+------------------+                +------------------+                +------------------+
|     Shard A      |                |     Shard B      |                |     Shard C      |
|  (Replica Set)   |                |  (Replica Set)   |                |  (Replica Set)   |
+------------------+                +------------------+                +------------------+
```

### Core Architecture Components:
1. **`mongos` Routing Layer:** Stateless routing service that intercepts client queries, inspects the query shard key, and routes requests to the appropriate target shard(s).
2. **Config Server Replica Set (CSRS):** High-availability 3-node replica set storing cluster metadata, chunk ranges, and collection routing tables.
3. **Shards:** Independent replica sets containing a subset of the total sharded data.

---

## 2. Shard Key Selection Strategies

A **Shard Key** is an immutable indexed field or combination of fields present in every document of a sharded collection.

### Range-Based Sharding
Documents are partitioned based on continuous ranges of the shard key value.
$$\text{Chunk Range}: [\text{MinKey}, v_1), [v_1, v_2), [v_2, \text{MaxKey})$$
* **Pros:** Efficient range queries targeting contiguous data ranges (`$gte`, `$lte`).
* **Cons:** Monotonically increasing keys (e.g., auto-incrementing `_id` or `timestamp`) cause write hotspotting on the highest range chunk on a single shard.

### Hashed Sharding
MongoDB computes an MD5 hash of the shard key value to determine the target chunk range.
$$\text{Shard Location} = \text{MD5}(\text{shard\_key\_value})$$
* **Pros:** Uniform random distribution of writes across all cluster shards. Eliminates write hotspots for sequential keys.
* **Cons:** Range queries cannot target specific shards; forces a **Scatter-Gather** operation across all shards.

---

## 3. Chunks, Chunk Splitting & Jumbo Chunks

Data in a sharded collection is logically partitioned into **Chunks**.

* **Default Chunk Size:** 64 MB.
* **Auto-Splitting:** When writes increase a chunk's size beyond 64 MB, the primary shard `mongod` automatically splits the chunk into two smaller chunk ranges in the Config Server metadata.
* **Jumbo Chunks:** If a single shard key value occurs in documents whose total size exceeds the maximum chunk size (e.g., millions of documents with `country: "US"`), the chunk cannot be split. It is flagged as **Jumbo** and cannot be migrated by the balancer.

---

## 4. Balancer Mechanics & Migration Protocol

The **Balancer** is a background process that manages chunk distribution across shards to maintain storage equilibrium.

```
+----------------+        1. Lock Migration        +----------------+
|  Source Shard  |-------------------------------->|  Target Shard  |
+-------+--------+                                 +-------+--------+
        |                                                  |
        | 2. Clone Chunk Documents via Cursor              |
        +------------------------------------------------->|
        |                                                  |
        | 3. Catch-up: Replay Delta Writes from Oplog      |
        +------------------------------------------------->|
        |                                                  |
        | 4. Update Config Server Metadata Routing Map     |
        |<------------------------------------------------>|
        |                                                  |
        | 5. Garbage Collect Old Chunk Docs                |
        v                                                  v
```

### Balancer Threshold Rules
The balancer initiates chunk migrations only when the chunk count imbalance between the shard with the most chunks and the shard with the fewest chunks exceeds the migration threshold:

| Total Cluster Chunks | Migration Threshold (Difference) |
| :--- | :--- |
| $< 20$ | 2 chunks |
| $20 - 79$ | 4 chunks |
| $\ge 80$ | 8 chunks |

### Performance Impact of Balancer
Active chunk migration consumes significant CPU, network bandwidth, and storage I/O. Best Practice: Configure **Balancer Window Schedules** to run during off-peak hours only.
