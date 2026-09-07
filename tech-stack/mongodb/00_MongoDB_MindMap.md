# MongoDB Architecture Mind Map

This document presents a structured visual and conceptual breakdown of MongoDB's distributed document database internals.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((MongoDB Deep Dive))
    WiredTiger Storage Engine
      BSON Binary Serialization
        Type Identifiers
        Field Length Headers
        Zero-Copy Traversal
      In-Memory RAM Cache
        Internal Pages
        Leaf Pages
        Hazard Pointers
      Page Eviction
        LRU Clean Eviction
        Dirty Page Eviction
        Worker Threads
      Journaling and Checkpoints
        Write-Ahead Log WAL
        Checkpoint Window 60 Seconds
        Crash Recovery
    Replication Engine
      Consensus Protocol
        Raft Variant Protocol
        Heartbeat Ping 2s
        Election Timeout 10s
        Priority and Votes
      Oplog Replication
        Capped Collection
        Idempotent Operations
        Secondary Pull Model
      Consistency Tunables
        Write Concern w1 wMajority
        Read Concern Local Majority Linearizable
        Read Preference Primary Secondary
    Sharding Architecture
      Routing Layer
        Mongos Stateless Router
        Query Targeting Scatter-Gather
      Metadata Layer
        Config Server CSRS
        Routing Table Cache
      Partitioning Layer
        Shard Key Selection
        Range Partitioning
        Hashed Partitioning
        Chunk Splitter and Balancer
    Indexing and Execution
      Index Data Structures
        B-Tree Leaf Nodes
        Compound Indexes ESR Rule
        Multikey Indexes Array Flattening
        Wildcard Indexes Path Indexing
      Aggregation Pipeline
        Pipeline Optimization Stage Folding
        Index Pushdown Stage
        Memory Limit 100MB Disk Spillover
    Transactions Engine
      MVCC Snapshot
        Read Snapshot Timestamp
        Commit Timestamp
      Distributed 2PC
        Transaction Coordinator
        Prepare Phase
        Commit Phase
```

---

## 2. Component Reference Table

| Component | Responsibility | Performance Bottlenecks |
| :--- | :--- | :--- |
| **WiredTiger Cache** | Caches uncompressed raw data pages in RAM | High dirty-page ratio blocking client threads |
| **Journal (WAL)** | Disk persistence log flushed every 50ms | Disk I/O ops limits (IOPS) causing write stalls |
| **Oplog (`local.oplog.rs`)** | Capped collection recording all database mutations | Small oplog window leading to `Stale` secondaries |
| **`mongos` Router** | Routes queries to shards using Config metadata | Memory overhead when caching high chunk count metadata |
| **Config Server CSRS** | Stores cluster routing rules and chunk mappings | Master failure blocking chunk splits/migrations |
| **Balancer** | Migrates chunks between shards for even load | High network saturation during active chunk migrations |
