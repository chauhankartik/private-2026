# Two-Phase Commit (2PC) — Deep Dive & Implementation

> **Core Focus:** Distributed Atomic Commitment, 2PC Protocol Phases, Coordinator Failures, Participant In-Doubt Blocking, 3PC, and Real-World Distributed Databases.

---

## 1. The Distributed Transaction Problem

In a sharded database, a single business operation may need to update records stored on separate physical nodes (e.g. Transfer \$100 from Account A on Node 1 to Account B on Node 2).

### The Atomicity Guarantee:
Either **ALL** participant nodes commit their local writes, or **ALL** participant nodes roll back. Partial commits violate database integrity!

---

## 2. Two-Phase Commit (2PC) Protocol

2PC uses a central node called the **Coordinator** to orchestrate transaction outcome across **Participant Nodes**.

```
Coordinator                        Participant 1              Participant 2
    │                                    │                          │
    ├─── Phase 1: PREPARE ──────────────►│                          │
    ├─── Phase 1: PREPARE ───────────────┼─────────────────────────►│
    │                                    │                          │
    │◄── Vote: YES (Locked & Logged) ────┤                          │
    │◄── Vote: YES (Locked & Logged) ────┼──────────────────────────┤
    │                                    │                          │
    ├─── Phase 2: COMMIT ───────────────►│                          │
    ├─── Phase 2: COMMIT ────────────────┼─────────────────────────►│
    │                                    │                          │
    │◄── ACK ────────────────────────────┤                          │
    │◄── ACK ────────────────────────────┼──────────────────────────┤
```

### Phase 1: Prepare Phase
1. Coordinator assigns unique `TxID` and sends `PREPARE` request to all participants.
2. Each participant executes transaction locally up to commit: acquires row locks, writes redo/undo logs to WAL disk.
3. Participant votes `YES` (if ready to commit) or `NO` (if lock failed or resource constraint).

### Phase 2: Commit Phase
1. **If ALL participants vote YES:**
   - Coordinator writes `COMMIT` to its transaction log on disk.
   - Sends `COMMIT` command to all participants.
   - Participants commit writes, release locks, and return `ACK`.
2. **If ANY participant votes NO (or times out):**
   - Coordinator writes `ABORT` to its log on disk.
   - Sends `ABORT` command to all participants to rollback locks.

---

## 3. The Critical Flaw of 2PC: Blocking In-Doubt States

If the Coordinator crashes after Phase 1 (after participants voted `YES`) but before broadcasting Phase 2 `COMMIT`/`ABORT`:

- Participants are trapped in an **In-Doubt State**.
- Participants **CANNOT UNILATERALLY COMMIT OR ABORT** because they don't know how other nodes voted.
- Participants **MUST HOLD EXCLUSIVE ROW LOCKS INDEFINITELY**, blocking other transactions until the coordinator recovers or an administrator intervenes!

---

## 4. Three-Phase Commit (3PC) & Paxos Commit

- **3PC (Non-Blocking):** Introduces a `Pre-Commit` state and timeouts to prevent blocking, but requires a synchronous, zero-partition network model (unrealistic in real hardware).
- **Paxos-Assisted 2PC (Spanner / CockroachDB):** Replicates the 2PC Coordinator across a fault-tolerant Paxos/Raft group so coordinator crashes never cause blocking!
