# Designing Data-Intensive Applications — Mind Map & Architecture Guide

> **Book Author:** Martin Kleppmann  
> **Purpose:** Detailed mental model, trade-off matrix, and interview reference guide covering all 12 chapters of DDIA.

---

## 🧠 Interactive DDIA Mind Map (Mermaid Diagram)

```mermaid
mindmap
  root(("Designing Data Intensive Applications"))
    "Part I - Foundations"
      "Ch 01 - Reliability Scalability Maintainability"
        "Percentiles p99 and p999"
        "SLOs and SLAs"
      "Ch 02 - Data Models and Query Languages"
        "Relational Model"
        "Document Model"
        "Graph Model"
      "Ch 03 - Storage and Retrieval"
        "LSM Trees and SSTables - RocksDB"
        "B Trees - InnoDB Postgres"
        "OLTP vs OLAP Columnar Storage"
      "Ch 04 - Encoding and Evolution"
        "Protocol Buffers and Thrift"
        "Apache Avro and Schema Registry"
    "Part II - Distributed Data"
      "Ch 05 - Replication"
        "Single Leader and Multi Leader"
        "Leaderless Dynamo Quorums"
      "Ch 06 - Partitioning"
        "Consistent Hashing and Virtual Nodes"
        "Secondary Index Partitioning"
      "Ch 07 - Transactions"
        "ACID Guarantees"
        "Snapshot Isolation and MVCC"
        "Serializable Snapshot Isolation - SSI"
      "Ch 08 - Trouble with Distributed Systems"
        "Unreliable Networks and Packet Loss"
        "Unreliable Clocks and Skew"
      "Ch 09 - Consistency and Consensus"
        "Linearizability"
        "Two Phase Commit - 2PC"
        "Raft and Paxos Consensus"
    "Part III - Derived Data"
      "Ch 10 - Batch Processing"
        "MapReduce and Hadoop"
        "Dataflow Engines - Spark and Flink"
      "Ch 11 - Stream Processing"
        "Change Data Capture - CDC"
        "Event Sourcing and Kafka"
      "Ch 12 - Future of Data Systems"
        "Unbundling Databases"
        "Data Integration and Correctness"
```

---

## 📊 Comprehensive Chapter Summary & Concepts Matrix

| Part | Chapter | Core Problem | Key Technologies / Algorithms | Master Takeaway |
|---|---|---|---|---|
| **Part I** | **Ch 1** | System Guarantees | Percentiles ($p99$, $p999$), Load Parameters, SLOs | Tail latency matters more than average latency for SLA compliance. |
| **Part I** | **Ch 2** | Data Representation | Relational (SQL), Document (JSON), Graph (Cypher) | Document for 1:N data; Relational for N:M data; Graph for deep N:M connections. |
| **Part I** | **Ch 3** | Disk Data Layout | **LSM-Tree + SSTable** vs **B+ Tree**, Column-Store | LSM-Trees maximize write throughput; B-Trees maximize random read performance. |
| **Part I** | **Ch 4** | Serialization Format | Protocol Buffers, Apache Avro, JSON, XML | Binary encodings with schema evolution enable backward/forward compatibility. |
| **Part II** | **Ch 5** | Data Redundancy | Single-Leader, Multi-Leader, Leaderless Quorums ($R+W>N$) | Synchronous replication guarantees consistency; Async replication risks lag. |
| **Part II** | **Ch 6** | Scale Out | Consistent Hashing, Key-Range Sharding, Secondary Indexes | Partitioning enables horizontal scale; Virtual nodes prevent hot spots. |
| **Part II** | **Ch 7** | Concurrency Control | **MVCC**, Snapshot Isolation, 2PL, **SSI** | Read Committed prevents dirty reads; SSI provides true serializability without blocking. |
| **Part II** | **Ch 8** | Network & Clock Truth | NTP Skew, Network Delays, Byzantine Faults | Distributed systems cannot rely on physical clocks; must assume unreliable networks. |
| **Part II** | **Ch 9** | Consensus & Agreement | **Linearizability**, 2PC, **Raft / Paxos** | Atomic Broadcast & Consensus are equivalent to Linearizable storage. |
| **Part III** | **Ch 10** | Offline Data Processing | MapReduce, Unix Pipes, Apache Spark, Flink | Batch processing inputs are immutable; output is derived data (recomputable). |
| **Part III** | **Ch 11** | Real-Time Processing | **CDC (Change Data Capture)**, Event Sourcing, Kafka | Streams treat databases as state logs; CDC streams state mutations in real time. |
| **Part III** | **Ch 12** | Database Unbundling | Data Integration, Dual Writes, End-to-End Correctness | Unbundle databases into specialized components connected by event logs. |
