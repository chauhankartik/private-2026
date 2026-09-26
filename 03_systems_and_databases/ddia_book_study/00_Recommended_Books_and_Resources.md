# Designing Data-Intensive Applications (DDIA) — Recommended Companion Books & Classic Papers

A curated reading list of companion textbooks, foundational distributed systems research papers, and industrial architecture write-ups that expand upon the concepts presented in Martin Kleppmann's *Designing Data-Intensive Applications*.

---

## 📚 Essential Companion Textbooks

1. **_Designing Data-Intensive Applications: The Big Ideas Behind Reliable, Scalable, and Maintainable Systems_** — Martin Kleppmann (O'Reilly)
   * **The Primary Benchmark:** The quintessential textbook for distributed data systems engineering.

2. **_Database Internals: A Deep Dive into How Distributed Data Systems Work_** — Alex Petrov (O'Reilly)
   * **Why Read It:** Provides deep mechanical code-level detail for data structures (B+ trees vs LSM-trees), transaction logs, consensus protocols (Raft, Paxos), and distributed storage layouts.

3. **_Distributed Systems (4th Edition)_** — Maarten van Steen & Andrew S. Tanenbaum
   * **Why Read It:** Comprehensive computer science textbook on distributed processes, RPC communication, logical time (Lamport & Vector Clocks), fault tolerance, and consistency models.

4. **_Readings in Database Systems (5th Edition - The Red Book)_** — Edited by Joseph M. Hellerstein & Michael Stonebraker
   * **Why Read It:** A curated collection of the most influential database research papers in history, annotated by leading database pioneers.

---

## 📄 Foundational Distributed Systems & Storage Papers

These classic papers directly form the foundation for DDIA's chapters:

1. **Dynamo (Amazon Key-Value Store):**
   * *DeCandia et al. (2007)* — *Dynamo: Amazon’s Highly Available Key-value Store* (SOSP). Introduces consistent hashing, vector clocks, sloppy quorums, and hinted handoff.
2. **Google Spanner (Global Consistency):**
   * *Corbett et al. (2012)* — *Spanner: Google’s Globally-Distributed Database* (OSDI). Introduces TrueTime API (atomic clocks + GPS), External Consistency, and 2PC over Paxos.
3. **Raft Consensus Protocol:**
   * *Ongaro & Ousterhout (2014)* — *In Search of an Understandable Consensus Algorithm* (USENIX ATC). The understandable consensus algorithm powering etcd, CockroachDB, and KRaft.
4. **Google Bigtable:**
   * *Chang et al. (2006)* — *Bigtable: A Distributed Storage System for Structured Data* (OSDI). Defines the LSM-tree based sparse, distributed multi-dimensional sorted map design.
5. **The Log as a Unifying Abstraction:**
   * *Jay Kreps (2013)* — *The Log: What every software engineer should know about real-time data's unifying abstraction*.
