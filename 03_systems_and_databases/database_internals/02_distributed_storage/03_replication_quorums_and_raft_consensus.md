# Part II: Distributed Storage — Replication, Quorums & Raft Consensus

Distributed storage engines replicate state across multiple nodes for fault tolerance and high availability.

---

## 📌 Quorum Consistency ($R + W > N$)

In Dynamo-style leaderless replication (Cassandra, Amazon Dynamo):
- $N$ = Total number of replica nodes.
- $W$ = Number of replicas that must acknowledge a write before it succeeds.
- $R$ = Number of replicas that must respond to a read query.

```mermaid
flowchart LR
    Client["Client Read Request (R=3)"] --> N1["Node 1 (Version 10)"]
    Client --> N2["Node 2 (Version 10)"]
    Client --> N3["Node 3 (Version 9 - Stale)"]

    Client --> Repair["Client picks Version 10 & Triggers Background Read Repair on Node 3!"]
```

> **Strong Consistency Rule**: If $R + W > N$, the read quorum $R$ and write quorum $W$ are guaranteed to overlap on at least one node, ensuring reads always see the latest write.

---

## 📌 Raft Consensus Algorithm Deep Dive

Raft decomposes consensus into 3 sub-problems: **Leader Election**, **Log Replication**, and **Safety**.

### Raft Node State Transitions

```mermaid
stateDiagram-v2
    [*] --> Follower : Initial Boot
    Follower --> Candidate : Election Timeout Expires (No Heartbeat from Leader)
    Candidate --> Leader : Receives Votes from Majority of Nodes
    Candidate --> Candidate : Election Times Out (Split Vote -> Retry)
    Candidate --> Follower : Discovers Leader with Higher Term
    Leader --> Follower : Discovers Node with Higher Term
```

### Raft Log Replication Sequence
1. Leader receives command from client.
2. Leader appends entry to its local log.
3. Leader sends `AppendEntries` RPC to all follower nodes.
4. Once entry is replicated to a **majority of nodes**, Leader commits entry and applies it to its state machine.
5. Leader notifies followers of commit in subsequent `AppendEntries` RPCs.

```mermaid
sequenceDiagram
    participant C as Client
    participant L as Raft Leader (Term 1)
    participant F1 as Follower 1
    participant F2 as Follower 2

    C->>L: Client Request: set x=42
    L->>L: Append (Index 10, Term 1, x=42) to Local Log
    L->>F1: AppendEntries(Term 1, PrevIndex 9, Log[10])
    L->>F2: AppendEntries(Term 1, PrevIndex 9, Log[10])
    F1-->>L: Success ACK
    F2-->>L: Success ACK
    
    Note over L: Replicated on Majority (2/3 nodes)! ENTRY COMMITTED!
    L->>L: Apply x=42 to State Machine
    L-->>C: Return Success to Client
```
