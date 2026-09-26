# Replication Topologies & Leaderless Quorums — Deep Dive & Implementation

> **Core Focus:** Single-Leader, Multi-Leader, Leaderless Dynamo Replication, Quorum Math ($R + W > N$), Read Repair, Sloppy Quorums, and Merkle Tree Anti-Entropy.

---

## 1. Overview of Replication Topologies

Replication copies data across multiple machines connected via network to provide High Availability and Latency Reduction.

### Three Primary Replication Architectures:
1. **Single-Leader (Master-Slave):** Writes routed to leader; followers stream log updates.
2. **Multi-Leader (Active-Active):** Multiple leaders accept writes (Used in multi-datacenter setups).
3. **Leaderless (Dynamo-Style):** Clients write directly to $N$ replicas in parallel (Pioneered by Amazon Dynamo & Apache Cassandra).

---

## 2. Leaderless Quorum Mathematics ($R + W > N$)

In a leaderless system with $N$ total replicas:
- **$W$ (Write Quorum):** Minimum number of replicas that must acknowledge a write for it to succeed.
- **$R$ (Read Quorum):** Minimum number of replicas that must respond to a read query.

```
                  QUORUM OVERLAP PRINCIPLE
                  
Total Replicas N = 5
Write Quorum  W = 3  (Replicas 1, 2, 3 updated)
Read Quorum   R = 3  (Replicas 3, 4, 5 queried)

Overlap Node = Replica 3  ──► Guaranteed to hold the latest Version!
```

### Quorum Condition Formula:

$$R + W > N \implies \text{Guaranteed Strong Consistency (Overlapping Replicas)}$$

- If $R + W \le N$, reads may return stale data without hitting any replica updated by the most recent write.

---

## 3. Replica Synchronization & Repair Mechanisms

### 1. Read Repair:
When a client reads from $R$ nodes in parallel:
- Client compares returned timestamps/version vectors across nodes.
- If Node 3 returns stale version `v1` while Nodes 1 & 2 return `v2`, client asynchronously writes `v2` back to Node 3.

### 2. Sloppy Quorums and Hinted Handoff:
If primary replicas are unreachable during a network partition:
- System accepts writes on alternate healthy nodes (**Sloppy Quorum**).
- Once network heals, temporary nodes deliver buffered writes back to primary nodes (**Hinted Handoff**).

### 3. Anti-Entropy with Merkle Trees:
Background process compares **Merkle Trees** (cryptographic hash trees of key ranges) between replicas to locate divergent keys rapidly without scanning raw data.

---

## 4. Architectural Comparison Matrix

| Property | Single-Leader | Multi-Leader | Leaderless Dynamo |
| :--- | :--- | :--- | :--- |
| **Write Availability** | Low (Fails if Leader down) | High | Highest (Any $W$ nodes accept) |
| **Write Latency** | Low | Low | Medium (Waits for $W$ ACKs) |
| **Conflict Resolution** | Not needed (Leader sequences) | Complex (LWW / CRDTs) | Read Repair / LWW |
| **Example Systems** | PostgreSQL, MySQL | GoldenGate, CouchDB | Apache Cassandra, DynamoDB |
