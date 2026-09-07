# Chapter 9: Consistency and Consensus — Deep Dive Notes

> **Core Theme:** Linearizability vs. Serializability, Total Order Broadcast, Two-Phase Commit (2PC), Fault-Tolerant Consensus Algorithms (**Raft, Paxos, Zab**), and Coordination Services (**etcd, ZooKeeper**).

---

## 1. Linearizability (Strict Consistency)

Linearizability is a recency guarantee: it makes a multi-replica system appear as if there is **only a single copy of the data**, and all operations execute atomically in real time.

```
       LINEARIZABLE READ / WRITE TIMELINE
       
Client A ├─── Write(x = 1) ───────┤
Client B ├──────── Read(x) -> 0 ─────┤─── Read(x) -> 1 ────┤
                                       ▲
                           Linearizability Point
           Once a read returns value '1', all subsequent reads 
           MUST return '1' globally!
```

### Linearizability vs. Serializability:
- **Serializability:** An isolation property of multi-operation transactions. Guarantees execution outcome is equivalent to *some* serial order (does not guarantee real-time ordering).
- **Linearizability:** A real-time recency guarantee on individual register reads and writes.
- **Strict Serializability (External Consistency):** Combines both Serializability and Linearizability (Used in Google Spanner, CockroachDB).

---

## 2. Total Order Broadcast (Atomic Broadcast)

Total Order Broadcast requires two fundamental safety properties:
1. **Reliable Delivery:** If a message is delivered to one node, it is delivered to all non-faulty nodes.
2. **Total Ordered Delivery:** Messages are delivered to all nodes in the exact same sequence.

### Equivalence to Linearizable Storage:
- Total Order Broadcast is equivalent to linearizable storage.
- You can build linearizable storage on top of Total Order Broadcast by appending state modifications to an ordered log (e.g. state machine replication).

---

## 3. Distributed Transactions & Two-Phase Commit (2PC)

Two-Phase Commit (2PC) ensures atomic commitment across multiple database nodes.

```
Coordinator                  Participant 1              Participant 2
    │                              │                          │
    ├─── Phase 1: PREPARE ────────►│                          │
    ├─── Phase 1: PREPARE ─────────┼─────────────────────────►│
    │                              │                          │
    │◄── Vote: YES ────────────────┤                          │
    │◄── Vote: YES ────────────────┼──────────────────────────┤
    │                              │                          │
    ├─── Phase 2: COMMIT ─────────►│                          │
    ├─── Phase 2: COMMIT ──────────┼─────────────────────────►│
```

### The 2PC Protocol Steps:
- **Phase 1 (Prepare / Voting):** Coordinator sends `PREPARE` request to all participants. Each participant checks if it can commit (locks rows, writes WAL) and responds `YES` or `NO`.
- **Phase 2 (Commit / Abort):** If ALL participants voted `YES`, coordinator logs `COMMIT` to its log and sends `COMMIT` command. If ANY participant voted `NO` or timed out, coordinator sends `ABORT`.

### The Critical Flaw of 2PC: Coordinator Failure & In-Doubt States
If the coordinator crashes after Phase 1 (after participants vote `YES`) but before sending `COMMIT`/`ABORT`:
- Participants are left in an **In-Doubt State**.
- Participants **MUST HOLD LOCKS INDEFINITELY** and cannot unilaterally commit or abort because they do not know how other nodes voted! Human administrator intervention is required to unlock blocked transactions.

---

## 4. Fault-Tolerant Consensus Algorithms

Consensus means getting multiple nodes to agree on a single value or sequence of values.

### Formal Properties of Consensus:
1. **Uniform Agreement:** No two nodes decide on different values.
2. **Integrity:** No node decides twice.
3. **Validity:** If a node decides value $v$, $v$ was proposed by some node.
4. **Termination:** Every non-faulty node eventually decides a value (Guarantees Liveness).

### Popular Consensus Protocols:
- **Paxos:** Classic consensus algorithm (Single-decree Paxos & Multi-Paxos).
- **Raft:** Designed for understandability. Uses strong leader, randomized election timers, and log matching properties.
- **Zab (ZooKeeper Atomic Broadcast):** Consensus protocol used by Apache ZooKeeper.

### Core Mechanics of Raft / Paxos:
1. **Epoch Numbers (Term Numbers / Ballots):** Monotonically increasing numbers ($e_1, e_2, \dots$). Each epoch has at most one leader.
2. **Quorum Elections:** Leader election and log entry commits require approval from a majority quorum ($> N/2$ nodes).
3. **Log Matching:** If two nodes have a log entry with the same index and term, the logs are identical up to that index.

---

## 5. Coordination Services (etcd, ZooKeeper, Consul)

Distributed systems rarely execute Paxos/Raft inside application code directly. Instead, they rely on specialized coordination services like **etcd** or **Apache ZooKeeper**.

### Key Features Provided:
- **Linearizable Key-Value Storage:** Storing cluster configuration metadata.
- **Leases & Heartbeats:** Detecting dead nodes and triggering failovers.
- **Distributed Locks & Fencing Tokens:** Preventing concurrent split-brain execution.
- **Change Notifications (Watches):** Clients subscribe to key changes (e.g. Kubernetes API server listening to etcd updates).
