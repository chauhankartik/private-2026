# Chapter 1: Distributed Cluster Architecture & Shard Routing Mechanics

## 1. Node Roles & Cluster Specialization

An Elasticsearch cluster is composed of one or more specialized nodes. Node specialization prevents resource contention by separating CPU-heavy tasks (full-text analysis, ingestion) from RAM-heavy tasks (aggregations, indexing).

```
                     +----------------------------------+
                     | Coordinating Node (Load Balancer)|
                     +-----------------+----------------+
                                       |
           +---------------------------+---------------------------+
           |                           |                           |
           v                           v                           v
+--------------------+      +--------------------+      +--------------------+
| Dedicated Master   |      | Ingest Node        |      | Data Node          |
| (Raft Consensus)   |      | (Grok / Pipelines) |      | (Hot / Warm / Cold)|
+--------------------+      +--------------------+      +--------------------+
```

### Node Specialization Matrix:
1. **Master-Eligible Nodes (`node.roles: [master]`):** Responsible for cluster-wide actions: creating/deleting indexes, tracking active cluster nodes, and assigning shards to data nodes. Master nodes elect a single Primary Master using a Raft-based consensus protocol.
2. **Data Nodes (`node.roles: [data_hot, data_warm, data_cold, data_frozen]`):** Hold Lucene index segments and execute document I/O, search queries, and aggregations.
3. **Ingest Nodes (`node.roles: [ingest]`):** Pre-process documents before indexing by applying transform pipelines (grok, JSON parsing, field mutation).
4. **Coordinating Nodes (`node.roles: []`):** Stateless routers that intercept client HTTP REST requests, route query requests to data nodes via scatter-gather, and aggregate top-$K$ search results.

---

## 2. Primary vs Replica Shards & Routing Mathematics

An Elasticsearch index is partitioned logically into one or more **Primary Shards**, where each primary shard is an independent **Apache Lucene instance**.

```
Index: "logs-2026" (3 Primary Shards, 1 Replica Copy)

    Shard 0: [ Primary 0 (Node A) ] <--- Replication ---> [ Replica 0 (Node B) ]
    Shard 1: [ Primary 1 (Node B) ] <--- Replication ---> [ Replica 1 (Node C) ]
    Shard 2: [ Primary 2 (Node C) ] <--- Replication ---> [ Replica 2 (Node A) ]
```

### Document Shard Routing Formula
When a client indexes or retrieves a document, Elasticsearch determines the exact primary shard using a deterministic hashing formula:

$$\text{Shard ID} = \left| \text{Murmur3Hash}(\text{routing\_key}) \right| \pmod{\text{number\_of\_primary\_shards}}$$

* **Default Routing Key:** The document `_id`.
* **Immutability of Primary Shard Count:** Because the number of primary shards is the modulus divisor in the routing formula, **`number_of_primary_shards` cannot be changed after index creation**. To alter shard count, the index must be reindexed (`_reindex` API).

---

## 3. Cluster State Publishing & Consensus

The cluster state contains node membership lists, index mappings, routing tables, and shard allocations.

```
  Primary Master Node                                      Data Nodes (Peers)
           |                                                      |
           | ---------- 1. Publish Cluster State (Diff) --------->|
           |                                                      |
           |                                             Applies State to RAM
           |                                             Returns Ack
           |<---------- 2. Acknowledgment (Ack) ------------------|
           |                                                      |
    (Waits for Majority Acks)                                      |
           |                                                      |
           | ---------- 3. Commit Cluster State ----------------->|
           v                                                      v
```

### Consensus & Failover:
* **Cluster State Versioning:** Monotonically increasing version counter. State changes are sent as compressed delta diffs.
* **Split-Brain Prevention:** Master elections enforce strict majority quorums ($V_{\text{majority}} = \lfloor N / 2 \rfloor + 1$).
