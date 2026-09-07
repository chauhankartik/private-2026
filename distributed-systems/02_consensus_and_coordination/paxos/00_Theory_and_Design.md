# Paxos Protocol — Deep Dive & Implementation

> **Core Focus:** Basic Paxos, Multi-Paxos, Proposer/Acceptor/Learner roles, Two-Phase Consensus (Prepare/Promise, Accept/Accepted), Ballot Numbers, and Dueling Proposer Live Locks.

---

## 1. The Paxos Consensus Problem

Paxos (Leslie Lamport, 1998) allows a set of unreliable processes to agree on a single value over an asynchronous, non-Byzantine network.

### Three Node Roles:
1. **Proposers:** Advocates for a client request value. Tries to get Acceptors to choose a proposal.
2. **Acceptors:** Acts as the memory of the consensus algorithm. Forms Quorums ($> N/2$) to accept proposed values.
3. **Learners:** Observes agreed-upon consensus decisions.

---

## 2. Basic Paxos Algorithm (Two Phases)

Each proposal is identified by a unique, monotonically increasing **Ballot / Proposal Number** $n = (\text{sequence}, \text{nodeId})$.

```
  Proposer                           Acceptor Quorum (> N/2)
     │                                         │
     ├─── Phase 1a: Prepare(n) ───────────────►│
     │◄── Phase 1b: Promise(n, max_val) ───────┤
     │                                         │
     ├─── Phase 2a: Accept(n, value) ─────────►│
     │◄── Phase 2b: Accepted ──────────────────┤
```

### Phase 1 (Prepare / Promise):
- **1a. Prepare(n):** Proposer selects proposal number $n$ and broadcasts `Prepare(n)` to a majority of Acceptors.
- **1b. Promise(n):** An Acceptor responds with a `Promise(n)` ONLY IF $n > \text{highest ballot seen}$.
  - The Acceptor promises: "I will reject any future proposals $< n$, and I return the highest proposal number $n_v$ and value $v$ I have previously accepted."

### Phase 2 (Accept / Accepted):
- **2a. Accept(n, v):** Once Proposer receives Promises from a majority:
  - If any Acceptor returned a previously accepted value $v$, the Proposer MUST pick the value $v$ associated with the highest ballot number $n_v$.
  - Otherwise, Proposer chooses its own client value $v$.
  - Proposer broadcasts `Accept(n, v)` to Acceptors.
- **2b. Accepted:** An Acceptor accepts `Accept(n, v)` if $n \ge \text{highest promised ballot}$. It notifies Learners and Proposers.

---

## 3. Dueling Proposers (Live Lock)

If two Proposers ($P_1$ with $n_1$ and $P_2$ with $n_2$) continuously issue interleaved `Prepare` requests before Phase 2 completes, neither proposal will ever be accepted!

- **Solution:** Introduce a **Distinguished Leader** (Multi-Paxos) or randomized election back-offs.

---

## 4. Multi-Paxos

Basic Paxos requires **2 full round-trips** for every single consensus decision.

- **Multi-Paxos Optimization:** Run Phase 1 (Prepare/Promise) **once** to elect a single Leader for a sequence of log instances ($i = 1, 2, 3 \dots$).
- Subsequent writes only require Phase 2 (`Accept` $\to$ `Accepted`), reducing latency from 2 RTTs to **1 RTT** per log entry!
