# Chapter 5: Replication — Deep Dive Notes

> **Core Theme:** Keeping a copy of the same data on multiple machines connected via network. Comparing **Single-Leader**, **Multi-Leader**, and **Leaderless (Dynamo)** architectures, Quorum math ($R + W > N$), and solving replication lag anomalies.

---

## 1. Why Replicate Data?

Replication serves three primary purposes:
1. **High Availability:** Keep system running even if some nodes crash.
2. **Latency Reduction:** Place data geographically closer to users.
3. **Scalability:** Increase read throughput by spreading read queries across multiple read replicas.

---

## 2. Leader-Based Replication (Single-Leader / Master-Slave)

All writes must go to a single node called the **Leader (Master / Primary)**. Followers (Replicas / Slaves) consume the leader’s data change stream and update their local storage.

```
Client ──── Write ────► Leader
                          │
             ┌────────────┴────────────┐
             ▼ (Replication Stream)    ▼
        Follower 1                Follower 2
```

### Synchronous vs. Asynchronous Replication:
- **Synchronous:** Leader waits for replica acknowledgment before returning success to client.
  - *Pro:* Replica is guaranteed to have up-to-date copy. Zero data loss on failover.
  - *Con:* If replica fails or network spikes, the write blocks indefinitely!
- **Asynchronous:** Leader returns success immediately after writing locally. Replica pulls updates asynchronously.
  - *Pro:* High performance; writes succeed even if all followers fail.
  - *Con:* If leader crashes before replicating, un-replicated writes are permanently lost!
- **Semi-Synchronous (Common Hybrid):** One follower is synchronous, remaining followers are asynchronous.

### Leader Failover Mechanics:
1. **Failure Detection:** Heartbeat timeout (e.g., node doesn't respond for 30s).
2. **Leader Election:** Replicas elect a new leader using consensus (e.g., Raft/Paxos) or coordinator (e.g., ZooKeeper).
3. **Split-Brain Risk:** Two nodes both believe they are the legitimate leader. Both accept writes $\to$ catastrophic data corruption! Must enforce **Fencing Tokens**.

---

## 3. Replication Lag Anomalies & Guarantees

When reading from asynchronous followers, replication lag causes temporary inconsistencies.

### 1. Read-Your-Own-Writes Consistency (Monotonic Writes)
- **Problem:** User submits a comment, reloads page, but reads from a lagging follower $\to$ comment seems to have vanished!
- **Mitigation:**
  - Read user's own editable data (e.g., profile settings) from the **Leader**; read other users' data from followers.
  - Track last update timestamp on client and force follower reads to block until follower catches up to client's write timestamp.

### 2. Monotonic Reads
- **Problem:** User refreshes page multiple times; first query hits follower with 1s lag (sees update), second query hits follower with 10s lag (update disappears!). Time appears to move backwards.
- **Mitigation:** Ensure each user always reads from the same replica (hash user ID to replica).

### 3. Consistent Prefix Reads
- **Problem:** Violates causality. If Question $A$ occurs before Answer $B$, a lagging follower might show Answer $B$ before Question $A$.
- **Mitigation:** Write causally dependent updates to the same partition or use causal tracking vector clocks.

---

## 4. Multi-Leader Replication (Active-Active)

Allow multiple nodes to accept writes simultaneously. Used in **Multi-Datacenter Setups**, **Offline Mobile Clients**, and **Collaborative Editing (Google Docs)**.

### Conflict Resolution Strategies:
Because writes happen concurrently on different leaders, conflicts are inevitable.

1. **Conflict Avoidance (Recommended):** Route all writes for a given record to the exact same leader (e.g., bind user to nearest datacenter).
2. **Last-Write-Wins (LWW):** Attach physical wall-clock timestamp to every write. Highest timestamp wins; older writes discarded silently.
   - *Danger:* Wall-clock NTP skew causes silent data loss!
3. **Operational Transformation (OT) & CRDTs:**
   - **CRDTs (Conflict-free Replicated Data Types):** Data structures (sets, maps, counters) that automatically merge concurrent modifications deterministically without locks.

---

## 5. Leaderless Replication (Dynamo-Style)

Pioneered by **Amazon Dynamo**, **Apache Cassandra**, **Riak**, and **Voldemort**.
- Any replica can accept writes and reads directly from clients. No single point of failure.

### Quorum Math ($R + W > N$):
Given:
- $N$ = Total number of replicas.
- $W$ = Number of replicas that must acknowledge a write for it to be successful.
- $R$ = Number of replicas that must be queried for a read operation.

```
       Quorum Condition for Consistency: R + W > N
       
       Total Replicas N = 5
       Write Quorum  W = 3
       Read Quorum   R = 3
       
       Overlap: (3 + 3) > 5 -> At least 1 node in Read Quorum is guaranteed to have latest Write!
```

### Keeping Replicas Up-to-Date:
1. **Read Repair:** Client reads from $R$ nodes in parallel. If node 3 returns stale version `v1` while nodes 1 & 2 return `v2`, client writes `v2` back to node 3 inline.
2. **Anti-Entropy Process:** Background process constantly compares **Merkle Trees** (hash trees) between nodes to find and repair missing data range differences quickly.

### Sloppy Quorums and Hinted Handoff:
If a network partition isolates a client from the designated $N$ nodes, system accepts writes on reachable non-designated nodes (**Sloppy Quorum**). Once network heals, temporary nodes hand off writes to primary nodes (**Hinted Handoff**).
