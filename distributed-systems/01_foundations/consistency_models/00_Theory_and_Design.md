# Consistency Models in Distributed Systems — Deep Dive

> **Core Focus:** Linearizability, Sequential Consistency, Causal Consistency, Session Consistency, Eventual Consistency, and Formal Guarantees.

---

## 1. The Consistency Spectrum

In a distributed system, a **Consistency Model** is a contract between the data store and its client processes. It defines the rules governing the values that a `read` operation is allowed to return relative to prior `write` operations across nodes.

```
  STRONGEST GUARANTEES (Highest Latency)                     WEAKEST GUARANTEES (Lowest Latency)
  ┌──────────────────┬─────────────────┬──────────────┬──────────────┬──────────────────┐
  │ Linearizability  │  Sequential     │    Causal    │  Read-Your-  │     Eventual     │
  │ (Real-Time Order)│ (Logical Order) │ (Cause-Effect│   Writes     │  (Convergence)   │
  └──────────────────┴─────────────────┴──────────────┴──────────────┴──────────────────┘
   • Google Spanner    • Single-Leader   • Vector       • User Session • Apache Cassandra
   • etcd / Raft       • Kafka Partition • Clocks       • Sticky Router • DynamoDB (Eventually)
```

---

## 2. Detailed Breakdown of Consistency Models

### 1. Linearizability (Strict / External Consistency)
- **Definition:** Every operation appears to take effect atomically at a specific point in time between its invocation and its response, according to a **global physical real-time clock**.
- **Key Guarantee:** Once a read returns a value $V$, all subsequent reads (in real time) across **any node** must return $V$ or a newer value.
- **Implementations:** Google Spanner (TrueTime), etcd (Raft ReadIndex), Apache Zookeeper (Sync read).
- **Trade-off:** High write latency, CP under CAP theorem.

### 2. Sequential Consistency (Lamport, 1979)
- **Definition:** The result of any execution is the same as if the operations of all processes were executed in some **sequential order**, and the operations of each individual process appear in this sequence in the order specified by its program.
- **Key Guarantee:** Does **not** require real-time clock alignment. All nodes agree on the *same global sequence* of events.
- **Implementations:** Single partition in Apache Kafka, Single-Leader MySQL/Postgres synchronous replication.

### 3. Causal Consistency
- **Definition:** Operations that are **causally related** must be seen by every node in the same order. Operations that are **concurrent** (not causally linked) may be seen in different orders by different nodes.
- **Key Guarantee:** Preserves cause-and-effect relationships (e.g., Question posted $\rightarrow$ Answer posted).
- **Implementations:** Vector Clocks / Version Vectors in Riak KV, MongoDB (Causal Sessions).

### 4. Read-Your-Own-Writes (Session Consistency)
- **Definition:** A process that writes a value $V$ to key $K$ will always observe value $V$ on subsequent reads to key $K$.
- **Key Guarantee:** Prevents user frustration where a user posts a comment, refreshes the page, and the comment vanishes due to reading from a lagged secondary node.
- **Implementations:** Web gateways using Sticky Sessions or reading from Primary for recently written keys.

### 5. Eventual Consistency (Vogel, 2008)
- **Definition:** If no new updates are made to a given data item, eventually all accesses to that item will return the last updated value.
- **Key Guarantee:** No real-time bound on convergence. High availability and low latency.
- **Implementations:** Apache Cassandra (LWW / Read Repair), Amazon DynamoDB (Default reads), DNS.

---

## 3. Consistency Model Comparison Matrix

| Model | Real-Time Clock Required? | All Nodes See Same Order? | Latency Overhead | CAP Classification | Primary Use Case |
|---|---|---|---|---|---|
| **Linearizability** | Yes | Yes (Strict Real-Time) | 🔴 High | **CP** | Banking balances, Distributed locks, Leader election |
| **Sequential** | No | Yes (Logical Stream) | 🟡 Medium | **CP** | Kafka partition logs, Leader-based DBs |
| **Causal** | No | Only for Causally Related | 🟢 Low | **AP** | Chat messages, Social media threads, Collaborative editing |
| **Read-Your-Writes**| No | Per-Session Only | 🟢 Low | **AP** | User profile edits, E-Commerce cart |
| **Eventual** | No | No (Eventual Convergence) | ⚡ Lowest | **AP** | Product catalogs, Metrics, Telemetry, DNS |
