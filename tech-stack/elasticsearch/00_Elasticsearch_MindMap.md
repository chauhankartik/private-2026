# Elasticsearch & Apache Lucene Architecture Mind Map

This document presents a structured visual breakdown of Elasticsearch and Apache Lucene distributed search internals.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((Elasticsearch Lucene Deep Dive))
    Cluster Architecture
      Node Specialization
        Master Eligible Raft Consensus
        Data Nodes Hot Warm Cold Frozen
        Ingest Nodes Ingest Pipelines
        Coordinating Router Nodes
      Shard Management
        Primary Shards Immutable Count
        Replica Shards Dynamic Copy
        Shard Routing Hash Formula
    Lucene Inverted Index
      Index Components
        Term Dictionary Sorted Tokens
        Posting Lists Doc ID Array
        FST Finite State Transducer RAM
      Compression Algorithms
        Frame of Reference FOR
        Roaring Bitmaps
      Storage Structures
        Source Raw Document JSON
        DocValues Disk Columnar
        Del Bitset Tombstones
    Write and Read Pathways
      Write Pipeline
        Index Buffer Memory
        Translog Write Ahead Log
        Refresh Searchable OS Cache 1s
        Flush Fsync to Storage Disk
        Segment Tiered Merge Policy
      Search Pipeline
        Scatter Phase Coordinating to Shards
        Query Context BM25 Scoring
        Filter Context Bitset Caching
        Gather Phase Merge Top K
        Fetch Phase Source Hydration
    Aggregations Engine
      Bucket Aggregations Terms Histogram
      Metric Aggregations Stats HyperLogLog
      DocValues Memory Efficient Disk
      Fielddata Heap Risk Text Fields
    Operational Management
      JVM Memory Management
        32GB Compressed OOPs Boundary
        50 Percent System RAM Allocation
      Circuit Breakers
        Parent Breaker
        Fielddata Breaker
      Index Lifecycle ILM
        Hot Write Active
        Warm Read Only Merged
        Cold Frozen Searchable Snapshot
```

---

## 2. Component Reference Table

| Component | Responsibility | Failure Modes / Hazards |
| :--- | :--- | :--- |
| **Master Node** | Manages cluster state publishing & index metadata | Master node GC pauses causing split-brain or cluster stalls |
| **Coordinating Node** | Scatter-gather query routing & response aggregation | OutOfMemory (OOM) during large result set aggregation merges |
| **Lucene Segment** | Immutable mini-inverted index file on disk | High segment count causing file descriptor exhaustion |
| **Translog** | Write-ahead durability log replayed on crash | Unflushed translog data loss on power failure if `index.translog.durability = async` |
| **FST (Term Index)** | Off-heap/RAM prefix tree for fast term lookup | Extremely high unique term count consuming node RAM |
| **Fielddata** | Heap-based un-inverted data structure for `text` aggs | Triggers `CircuitBreakingException` or JVM Heap OOM crash |
