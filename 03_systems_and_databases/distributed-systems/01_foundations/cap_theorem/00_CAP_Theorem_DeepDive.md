# CAP & PACELC Theorems — Deep Dive

> **Core Focus:** Proof of CAP Theorem, Network Partitions, PACELC Theorem, and Real-World Database Classification.

---

## 1. CAP Theorem (Brewer's Theorem)

In a distributed data store, you can only simultaneously provide **at most two** out of the following three guarantees:

1. **Consistency (C):** Every read receives the most recent write or an error (Equivalent to Linearizability).
2. **Availability (A):** Every non-failing node returns a non-error response for every request (without guarantee that it contains the most recent write).
3. **Partition Tolerance (P):** The system continues to operate despite arbitrary network dropouts or packet loss between nodes.

---

## 2. Why "CA Systems" Do Not Exist in Distributed Networks

Network partitions (**P**) are a physical reality of distributed hardware (switches fail, cables get cut, GC pauses occur). Therefore:

$$\text{Choice} = \text{Partition occurs} \implies \text{Choose Consistency (CP) OR Availability (AP)}$$

- **CP (Consistency + Partition Tolerance):** If a network partition occurs, refuse writes/reads on isolated nodes to prevent stale/divergent data.
- **AP (Availability + Partition Tolerance):** If a network partition occurs, allow nodes to accept writes/reads locally, resulting in eventual consistency and data reconciliation (Vector Clocks / LWW).

---

## 3. PACELC Theorem (Abadi's Extension)

CAP only describes behavior **if there is a network partition (P)**. PACELC addresses system behavior during **normal operations (Else - E)**:

$$\text{If } \mathbf{P} \text{ (Partition)} \implies \text{Choose } \mathbf{A} \text{ (Availability) OR } \mathbf{C} \text{ (Consistency)}$$
$$\text{Else } \mathbf{E} \text{ (Normal Operation)} \implies \text{Choose } \mathbf{L} \text{ (Latency) OR } \mathbf{C} \text{ (Consistency)}$$

### Real-World PACELC Classifications:
- **PC/EC (Spanner, HBase, MongoDB, etcd):** Prefers Consistency both during partitions and normal operations.
- **PA/EL (Amazon DynamoDB, Cassandra, CouchDB):** Prefers Availability during partitions and Low Latency during normal operations.
