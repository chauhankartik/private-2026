# Raft Consensus Protocol — Deep Dive & Implementation

> **Core Focus:** State Machine Replication, Leader Election, Log Replication, Term Numbers, Safety Invariants, and Joint Consensus Reconfiguration.

---

## 1. Overview of Raft Consensus

Raft is a consensus algorithm designed for **understandability** and operational simplicity, offering equivalent fault tolerance and performance to Paxos.

A Raft cluster typically consists of $N = 2f + 1$ nodes (allowing tolerance of $f$ node failures).

```
   ┌──────────┐      Timeout / Starts Election     ┌──────────┐
   │ Follower │ ─────────────────────────────────► │ Candidate│
   └──────────┘                                    └────┬─────┘
        ▲                                               │ Votes from
        │                 Discovers Leader              │ Majority
        └───────────────────────────────────────────────┘
                                │
                                ▼
                           ┌──────────┐
                           │  Leader  │
                           └──────────┘
```

---

## 2. Key Components of Raft

### 1. Three Node States:
- **Follower:** Completely passive. Responds to RPCs from Leader/Candidate. If election timeout expires without heartbeats, transitions to Candidate.
- **Candidate:** Requests votes from peers to become Leader.
- **Leader:** Handles all client requests, replicates log entries, and sends periodic `AppendEntries` heartbeats.

### 2. Term Numbers (Logical Clock):
- Time is divided into **Terms** ($T_1, T_2, \dots$), numbered with monotonically increasing integers.
- Terms act as logical clocks to detect stale nodes. If a node sees an RPC with a higher term, it updates its term and steps down to Follower.

### 3. Log Invariants (Safety Properties):
- **Election Safety:** At most one leader can be elected per term.
- **Leader Append-Only:** A leader never overwrites or truncates its own log entries.
- **Log Matching Property:** If two logs contain an entry with the same index and term, then the logs are identical in all entries up to that index.
- **Leader Completeness:** If a log entry is committed in a given term, that entry will be present in the logs of the leaders for all higher-numbered terms.

---

## 3. Raft RPC Specifications

### 1. `RequestVote` RPC:
- **Arguments:** `term`, `candidateId`, `lastLogIndex`, `lastLogTerm`.
- **Voting Rule:** A follower grants vote ONLY IF `candidate.term >= currentTerm` AND candidate's log is **at least as up-to-date** as receiver's log (`lastLogTerm > receiver.lastLogTerm` OR `lastLogTerm == receiver.lastLogTerm && lastLogIndex >= receiver.lastLogIndex`).

### 2. `AppendEntries` RPC (Heartbeat & Replication):
- **Arguments:** `term`, `leaderId`, `prevLogIndex`, `prevLogTerm`, `entries[]`, `leaderCommit`.
- **Consistency Check:** Receiver rejects request if it does not contain an entry at `prevLogIndex` matching `prevLogTerm`.

---

## 4. Raft vs. Paxos Comparison

| Feature | Raft | Multi-Paxos |
| :--- | :--- | :--- |
| **Understandability** | High (Decomposed into Election, Replication, Safety) | Low (Complex phase interactions) |
| **Leader Role** | Strong Leader (Logs only flow from Leader to Followers) | Weak / Optional Leader |
| **Membership Changes** | Joint Consensus ($C_{\text{old}} \to C_{\text{old,new}} \to C_{\text{new}}$) | Complex Reconfiguration |
| **Production Implementations** | **etcd** (Kubernetes), **HashiCorp Consul**, **Apache Kafka** (KRaft) | **Google Chubby**, **Google Spanner** |
