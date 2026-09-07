# Cassandra & ScyllaDB Architecture Mind Map

This document presents a structured visual and conceptual breakdown of Apache Cassandra and ScyllaDB internal architecture.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((Cassandra ScyllaDB Deep Dive))
    Masterless Ring Network
      Gossip Protocol
        P2P State Exchange
        Scuttlebutt Reconciler
        Heartbeat Generation
      Failure Detection
        Phi Accrual Detector
        History Sliding Window
        Dynamic Threshold Suspicion
      Token Ring Partitioning
        Murmur3 Hash 64-bit Range
        Virtual Nodes vnodes
        Token Ranges Allocation
    LSM Storage Engine
      Write Path
        CommitLog Append Only
        Memtable In-Memory ConcurrentSkipListMap
        SSTable Flush Immutable File
      SSTable Components
        Data.db Raw Cell Payload
        Index.db Key Offset Map
        Summary.db RAM Index Sampling
        Filter.db Bloom Filter Bitset
      Tombstone Management
        Delete Markers
        gc_grace_seconds Expiration
        Tombstone Garbage Collection
    Read Path & Compaction
      Read Tiering
        Row Cache Off-Heap
        Key Cache RAM Offsets
        Bloom Filter False Positive Check
        Partition Summary Search
        Partition Index Binary Search
      Compaction Engine
        Size Tiered STCS High Write Throughput
        Leveled LCS High Read Performance
        Time Window TWCS Time-Series Data
    Tunable Distributed Consistency
      Consistency Options
        ONE LOCAL_QUORUM QUORUM ALL
      Anti-Entropy Mechanisms
        Hinted Handoff 3-Day Buffer
        Read Repair Async Speculative
        Merkle Tree Full Repair
    ScyllaDB Architecture
      Seastar Framework
        C Plus Plus 17 Core Engine
        Thread Per Core Non-Blocking
        Zero Context Switching
        Direct DMA Asynchronous Disk IO
```

---

## 2. Component Reference Table

| Component | Functionality | Key Bottlenecks / Failures |
| :--- | :--- | :--- |
| **CommitLog** | Durability WAL append-only disk log | Slow disk writes blocking all incoming node mutation requests |
| **Memtable** | In-memory write buffer before SSTable flush | JVM GC allocation pressure during high write spikes |
| **Bloom Filter** | Probabilistic bitset checking key existence in SSTable | High false-positive rate ($> 1\%$) causing excessive disk reads |
| **Tombstones** | Special markers recording record deletions | Accumulation causing `TombstoneOverwhelmingException` during scans |
| **Gossip Thread** | Exposes node state and schema versions across peers | CPU starvation delaying failure detection during heavy GC pauses |
| **Compaction Executor** | Merges SSTables and purges tombstoned records | Excessive disk read/write amplification starving operational queries |
