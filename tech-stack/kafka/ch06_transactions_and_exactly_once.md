# Chapter 6: Transactions & Exactly-Once Semantics (EOS) — Deep Dive Notes

> **Core Theme:** Achieving **Exactly-Once Semantics (EOS)** across read-process-write streams using **Transactional Producers**, **Two-Phase Commit (2PC)**, and **Commit Markers**.

---

## 1. The Exactly-Once Equation

Achieving end-to-end Exactly-Once processing across Kafka pipelines requires three coordinated components:

$$\text{Exactly-Once Semantics (EOS)} = \text{Idempotent Producer} + \text{Transactional Coordinator} + \text{Read-Committed Consumer}$$

```
Input Topic ──► Consumer (read_committed) ──► Processor ──► Transactional Producer ──► Output Topic
                      ▲                                              │
                      └───────────── Atomic Offset Commit ───────────┘
```

---

## 2. Transaction Coordinator & `__transaction_state`

Kafka uses a designated broker called the **Transaction Coordinator** to orchestrate transactions identified by a unique `transactional.id`.

- The coordinator manages transaction states (`Empty`, `Ongoing`, `PrepareCommit`, `CompleteCommit`) stored inside the internal compacted topic `__transaction_state`.

---

## 3. Two-Phase Commit (2PC) Execution Workflow

```
Producer                     Transaction Coordinator               Partition Log
   │                                   │                                │
   ├── 1. InitTransactions() ─────────►│                                │
   ├── 2. AddPartitionsToTxn() ───────►│                                │
   ├── 3. Send Messages ───────────────┼───────────────────────────────►│
   ├── 4. SendOffsetsToTxn() ──────────►│                                │
   │                                   │                                │
   ├── 5. CommitTransaction() ────────►│                                │
   │                                   ├── 6. Write PREPARE_COMMIT ────►│
   │                                   ├── 7. Write COMMIT MARKER ─────►│
   │                                   └── 8. Write COMMITTED ─────────►│
```

1. **`initTransactions()`:** Producer registers `transactional.id`. Coordinator increments Epoch to fence out zombie producers.
2. **`beginTransaction()`:** Producer begins local transaction loop.
3. **`send()` & `sendOffsetsToTransaction()`:** Producer writes messages to target topics AND sends consumed offsets to the coordinator.
4. **`commitTransaction()`:**
   - **Phase 1:** Coordinator writes `PREPARE_COMMIT` to `__transaction_state`.
   - **Phase 2:** Coordinator appends a 2-byte **Commit Marker** to all topic partitions involved in the transaction.
   - Coordinator writes `COMMITTED` to `__transaction_state`.

---

## 4. Consumer Isolation Level (`isolation.level`)

- **`read_uncommitted` (Default):** Returns all messages in log order, including messages from active or aborted transactions.
- **`read_committed`:** Consumer buffers uncommitted messages in memory. It filters out aborted transactions and **ONLY yields messages up to the last Commit Marker**!
