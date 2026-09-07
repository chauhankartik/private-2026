# Distributed Systems — Recommended Books & Landmark Papers

> **Curated Reading List:** Essential literature, textbooks, and landmark research papers that pair directly with the notes and reference implementations in this repository.

---

## 📚 1. Core Recommended Books

### 1. *Designing Data-Intensive Applications (DDIA)* — Martin Kleppmann
- **Primary Category:** Distributed Systems Architecture & Storage
- **Direct Repo Alignment:** Maps 1-to-1 with [`ddia_book_study/`](../ddia_book_study/README.md) and [`04_replication_and_storage/`](04_replication_and_storage/), [`05_distributed_transactions/`](05_distributed_transactions/).
- **Key Topics:** Storage Engines (LSM-Trees vs B-Trees), Replication topologies, Partitioning, Transactions (MVCC, SSI), and Fault-tolerant Consensus.

### 2. *Database Internals: A Deep Dive into How Distributed Data Systems Work* — Alex Petrov
- **Primary Category:** Storage Engines & Consensus Protocols
- **Direct Repo Alignment:** Maps to [`04_replication_and_storage/lsm_tree_vs_b_tree/`](04_replication_and_storage/lsm_tree_vs_b_tree/) and [`02_consensus_and_coordination/`](02_consensus_and_coordination/).
- **Key Topics:** B+ Tree page layouts, LSM-Trees (MemTable, SSTables, Compaction), Paxos, Raft, 2PC/3PC implementation mechanics.

### 3. *Distributed Systems (4th Edition)* — Maarten van Steen & Andrew S. Tanenbaum
- **Primary Category:** Theoretical Foundations & Formal Models
- **Direct Repo Alignment:** Maps to [`01_foundations/`](01_foundations/).
- **Key Topics:** Physical & Logical Clocks (Lamport, Vector Clocks), Consistency Models (Linearizability to Eventual Consistency), Fault Models, and RPC.

### 4. *Designing Distributed Systems* — Brendan Burns
- **Primary Category:** Cloud-Native Patterns & Microservices
- **Direct Repo Alignment:** Maps to [`tech-stack/docker/`](../tech-stack/docker/README.md) and LLD microservice architectures.
- **Key Topics:** Sidecars, Ambassadors, Adapters, Replicated Services, Sharded Clusters, and Work Queue patterns.

---

## 📜 2. Landmark Research Papers (The "Gold Standard" List)

| Topic | Landmark Paper | Key Takeaway / System |
| :--- | :--- | :--- |
| **Consensus Protocol** | [*In Search of an Understandable Consensus Algorithm (Raft)*](https://raft.github.io/raft.pdf) — Diego Ongaro & John Ousterhout | Leader election, Log replication, Term safety (etcd, Consul) |
| **Classic Consensus** | [*Paxos Made Simple*](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf) — Leslie Lamport | Prepare/Promise, Accept/Accepted phases |
| **Time & Ordering** | [*Time, Clocks, and the Ordering of Events in a Distributed System*](https://lamport.azurewebsites.net/pubs/time-clocks.pdf) — Leslie Lamport | Logical clocks, Total ordering of events |
| **High Availability** | [*Dynamo: Amazon's Highly Available Key-Value Store*](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp07.pdf) — DeCandia et al. | Leaderless replication, Quorums ($R+W>N$), Consistent Hashing |
| **Distributed SQL** | [*Spanner: Google’s Globally-Distributed Database*](https://research.google/pubs/pub39966.pdf) — Corbett et al. | TrueTime API, GPS/Atomic clocks, External Consistency |
| **Storage Engines** | [*The Log-Structured Merge-Tree (LSM-Tree)*](https://www.cs.umb.edu/~poneil/lsmtree.pdf) — Patrick O'Neil et al. | Append-only sequential disk writes, MemTable & SSTable compaction |

---

## 🎯 Recommended Study Sequence

$$\text{DDIA (Kleppmann)} \implies \text{Database Internals (Petrov)} \implies \text{Raft \& Dynamo Papers} \implies \text{Tanenbaum}$$
