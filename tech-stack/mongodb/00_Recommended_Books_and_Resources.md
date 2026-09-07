# Recommended Books & Resources: MongoDB & Document Databases

A curated collection of books, technical papers, official documentation, and open-source codebase links for mastering MongoDB architecture and operations.

---

## 1. Essential Books

1. **MongoDB: The Definitive Guide (3rd Edition)**  
   *Authors:* Shannon Bradshaw, Eoin Brazil, Kristina Chodorow  
   *Focus:* Complete operational and development guide to MongoDB, covering aggregation, replication, sharding, and WiredTiger tuning.

2. **Designing Data-Intensive Applications**  
   *Author:* Martin Kleppmann  
   *Focus:* Chapter 2 (Data Models), Chapter 5 (Replication), and Chapter 6 (Partitioning). Essential theoretical foundation for understanding MongoDB's document model, master-slave replication, and sharding.

3. **Database Internals: A Deep Dive into How Distributed Data Systems Work**  
   *Author:* Alex Petrov  
   *Focus:* B-Trees, LSM-Trees, Write-Ahead Logging, and Distributed Consensus—the underlying computer science mechanics of WiredTiger and Replica Sets.

---

## 2. Official Documentation & Whitepapers

* **[WiredTiger Storage Engine Architecture Docs](http://source.wiredtiger.com/3.2.1/index.html)** — Comprehensive internal mechanics of WiredTiger hazard pointers, checkpoints, and eviction algorithms.
* **[MongoDB Manual: Replication Mechanics](https://www.mongodb.com/docs/manual/replication/)** — In-depth details on Raft-like election protocol, Oplog format, Write Concerns, and Read Concerns.
* **[MongoDB Manual: Sharding Architecture](https://www.mongodb.com/docs/manual/sharding/)** — Config server Replica Sets, chunk splitting, shard keys, and routing logic.
* **[MongoDB Server Source Code (GitHub)](https://github.com/mongodb/mongo)** — The C++ source code repository for MongoDB server (`mongod` and `mongos`).

---

## 3. Source Code Exploration Guide

When reading the `mongodb/mongo` codebase:
* **Storage Engine Interface:** `src/mongo/db/storage/wiredtiger/` (WiredTiger wrapper implementation)
* **Replication & Consensus:** `src/mongo/db/repl/` (Raft election logic, Oplog application, replication coordinator)
* **Sharding & Routing:** `src/mongo/db/s/` (`mongos` routing, chunk manager, catalog manager)
* **Transactions:** `src/mongo/db/transaction/` (Transaction coordinator, 2PC prepare/commit logic)
