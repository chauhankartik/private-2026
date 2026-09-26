# Distributed Locks — Deep Dive & Implementation

> **Core Focus:** Redis Redlock Algorithm, ZooKeeper Curator Ephemeral Sequential Nodes, Fencing Tokens, Expiration Leases, and Edge Cases.

---

## 1. Why Distributed Locking is Hard

In single-process applications, mutexes rely on shared memory. In distributed systems, processes run on separate nodes connected by unpredictable networks and unreliable clocks.

### Core Guarantees of Distributed Locks:
1. **Safety (Mutual Exclusion):** At most one process can hold the lock at any given time.
2. **Liveness A (No Deadlocks):** Lock is eventually released even if client holding lock crashes or network partitions occur (using TTL / Leases).
3. **Liveness B (Fault Tolerance):** Lock service continues functioning as long as majority nodes are alive.

---

## 2. Lock Architectures: Redis Redlock vs. ZooKeeper Curator

### 1. Redis Redlock Algorithm
- **How Redlock Works:**
  1. Client gets current time in milliseconds.
  2. Tries to acquire lock on $N$ independent Redis instances sequentially using `SET resource_name my_random_value NX PX 30000`.
  3. Client calculates elapsed time. If lock acquired on majority ($\ge \frac{N}{2} + 1$) instances AND elapsed time $< \text{TTL}$, lock is granted!
- **Martin Kleppmann’s Critique of Redlock:**
  - Redlock depends on physical system clocks (NTP drift!). If a node's clock jumps forward, the lock lease expires prematurely while client is still executing inside critical section!

---

### 2. ZooKeeper / etcd (Fencing Tokens)
ZooKeeper uses **Ephemeral Sequential Nodes** and consensus (Zab/Raft) to eliminate clock dependencies.

```
Lock Request Sequence:
1. Client creates ephemeral sequential node: /locks/guid-lock-0000000034
2. Client lists children under /locks.
3. If client's node has lowest sequence number -> LOCK GRANTED!
4. If not lowest -> Client places WATCH on node with next-lowest sequence number.
```

- **Fencing Tokens (Preventing Split-Brain Data Corruption):**
  - Storage server enforces monotonically increasing fencing tokens ($101, 102 \dots$). If a paused client resumes and attempts to write with an older fencing token, storage server rejects write!

```
Client 1 (Lock Token = 33) ──► Pause (GC) ─────────────────► Attempt Write (Token 33) ──X (REJECTED!)
Client 2 (Lock Token = 34) ─────────────────► Write (Token 34) ──► Storage Server (ACCEPTED)
```

---

## 3. Comparison Matrix

| Lock Service | Redlock (Redis) | ZooKeeper Curator / etcd |
| :--- | :--- | :--- |
| **Clock Dependence** | High (Relies on physical TTL clock) | Zero (Relies on Consensus Heartbeats) |
| **Fencing Token Support** | No native token generation | Built-in monotonic sequence IDs |
| **Throughput** | High | Medium |
| **Safety Guarantees** | Probabilistic (CP under non-drifting clock) | Strong CP (Linearizable lock state) |
