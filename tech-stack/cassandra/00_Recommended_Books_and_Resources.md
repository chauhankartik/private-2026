# Recommended Books & Resources: Apache Cassandra & ScyllaDB

A curated list of authoritative books, engineering whitepapers, official documentation, and open-source GitHub repositories for mastering Cassandra and ScyllaDB.

---

## 1. Essential Books

1. **Cassandra: The Definitive Guide (3rd Edition)**  
   *Authors:* Jeff Carpenter, Eben Hewitt  
   *Focus:* Complete guide to Apache Cassandra architecture, CQL data modeling, compaction strategies, node operations, multi-datacenter replication, and tuning.

2. **Designing Data-Intensive Applications**  
   *Author:* Martin Kleppmann  
   *Focus:* Chapter 5 (Replication), Chapter 6 (Partitioning), and Chapter 3 (SSTables & LSM-Trees). Essential background on leaderless replication, Dynamo-style consensus, and LSM engines.

3. **Database Internals: A Deep Dive into How Distributed Data Systems Work**  
   *Author:* Alex Petrov  
   *Focus:* Detailed coverage of LSM-Tree disk formats, Bloom Filters, SSTable compaction algorithms, and peer-to-peer gossip algorithms.

---

## 2. Classic Papers & Technical Documentation

* **[The Dynamo Paper (Amazon 2007)](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp07.pdf)** — *Dynamo: Amazon's Highly Available Key-value Store* by Giuseppe DeCandia et al. The seminal foundation paper for Cassandra's masterless design, consistent hashing, vector clocks, and quorum consensus.
* **[Apache Cassandra Official Documentation](https://cassandra.apache.org/doc/latest/)** — Core architecture reference, CQL language specification, and operations manuals.
* **[ScyllaDB Architecture Docs](https://docs.scylladb.com/stable/architecture/)** — Detailed explanation of Seastar C++ framework, thread-per-core design, and memory allocation.
* **[ScyllaDB Open-Source Code Repository (GitHub)](https://github.com/scylladb/scylladb)** — C++ source code for ScyllaDB.
* **[Apache Cassandra Source Code Repository (GitHub)](https://github.com/apache/cassandra)** — Java source code for Apache Cassandra.

---

## 3. Source Code Reading Guide (Apache Cassandra)

When navigating the `apache/cassandra` codebase:
* **Gossip & Cluster State:** `src/java/org/apache/cassandra/gms/` (`GossipDigestSyn`, `FailureDetector`)
* **Storage Engine & Memtable:** `src/java/org/apache/cassandra/db/memtable/`
* **Compaction Engine:** `src/java/org/apache/cassandra/db/compaction/` (`SizeTieredCompactionStrategy`, `LeveledCompactionStrategy`)
* **Consistent Hashing:** `src/java/org/apache/cassandra/dht/` (`Murmur3Partitioner`, `Token`)
