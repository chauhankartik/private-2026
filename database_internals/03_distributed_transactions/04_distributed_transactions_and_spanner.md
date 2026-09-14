# Part III: Distributed Transactions & Google Spanner

Executing transactions across multiple sharded nodes without sacrificing serializability or consistency.

---

## 📌 Two-Phase Commit (2PC) Protocol

2PC guarantees that all participant nodes in a distributed transaction either commit or abort together.

```mermaid
sequenceDiagram
    participant C as Coordinator
    participant P1 as Participant Node 1
    participant P2 as Participant Node 2

    Note over C,P2: Phase 1: Prepare Phase
    C->>P1: PREPARE (Tx 100)
    C->>P2: PREPARE (Tx 100)
    P1->>P1: Log Prepared to WAL & Acquire Locks
    P2->>P2: Log Prepared to WAL & Acquire Locks
    P1-->>C: VOTE_COMMIT
    P2-->>C: VOTE_COMMIT

    Note over C,P2: Phase 2: Commit Phase
    C->>C: Log GLOBAL_COMMIT to WAL
    C->>P1: GLOBAL_COMMIT
    C->>P2: GLOBAL_COMMIT
    P1->>P1: Commit & Release Locks
    P2->>P2: Commit & Release Locks
    P1-->>C: ACK
    P2-->>C: ACK
```

---

## 📌 Google Spanner & TrueTime API

Google Spanner achieves **External Consistency (Strict Serializability)** globally across data centers without central lock coordination by utilizing the **TrueTime API**.

### TrueTime API & Atomic Clocks
TrueTime exposes time as a bounded interval $[t_{\text{earliest}}, t_{\text{latest}}]$ where $t_{\text{latest}} - t_{\text{earliest}} = 2\epsilon$ ($\epsilon \approx 1\text{ to } 7\text{ ms}$ via synchronized GPS and Atomic clocks in every datacenter).

```mermaid
sequenceDiagram
    participant Tx as Transaction Tx1
    participant TT as TrueTime API
    participant Engine as Spanner Storage Engine

    Tx->>TT: TT.now() -> Returns [100, 108] (epsilon = 4ms)
    Tx->>Engine: Assign Commit Timestamp s = 108 (latest bound)
    
    Note over Engine: Commit Wait Rule: Engine delays client return until TT.now().earliest > 108!
    
    Engine->>TT: TT.now() -> Returns [109, 117] (109 > 108!)
    Engine-->>Tx: Return Success to Client!
```

> **Commit Wait Rule**: The coordinator delays returning the commit result until $TT.now().earliest > s$. This guarantees that any subsequent transaction $Tx_2$ anywhere in the world will get a timestamp $s_2 > s_1$!

---

## 📌 Deterministic Transactions (Calvin Engine Architecture)

Traditional 2PC holds locks across network roundtrips during the Prepare phase, stalling throughput.

**Calvin** eliminates 2PC overhead by using a **Global Sequencer** to order transactions *before* acquiring locks. Because transaction ordering is deterministic, replica nodes execute transactions concurrently without lock negotiation!

```mermaid
flowchart LR
    Tx["Client Transactions"] --> Sequencer["1. Global Sequencer Layer (Raft / Paxos Order Batching)"]
    Sequencer --> Scheduler["2. Deterministic Lock Manager (Lock in exact Sequencer Order)"]
    Scheduler --> Worker["3. Execution Workers (Run to completion without 2PC stalls!)"]
```
