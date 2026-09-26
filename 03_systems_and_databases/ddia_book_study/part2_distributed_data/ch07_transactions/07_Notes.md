# Chapter 7: Transactions — Deep Dive Notes

> **Core Theme:** ACID guarantees, Race Conditions, Isolation Levels (**Read Committed**, **Snapshot Isolation / MVCC**, **Serializability**), and modern concurrency mechanisms (2PL, SSI).

---

## 1. The Meaning of ACID

- **Atomicity:** All-or-nothing execution. If an error occurs mid-transaction, the transaction is **aborted** and all writes are rolled back.
- **Consistency:** Preserves application invariants (e.g. Total credits = Total debits). Application responsibility aided by database constraints.
- **Isolation:** Concurrent transactions execute without interfering with each other. Idealy, serializable isolation makes concurrent execution behave as if run serially.
- **Durability:** Once a transaction commits, its data will not be lost, even in the event of hardware failure or crash (WAL / Redo log).

---

## 2. Weak Isolation Levels & Race Conditions

### 1. Read Committed (Default in Postgres, Oracle, SQL Server)
- **Guarantees:**
  1. No Dirty Reads (Only read committed data).
  2. No Dirty Writes (Only overwrite committed data using row-level locks).
- **Vulnerability:** **Non-Repeatable Read (Read Skew)**. Reading the same row twice within a transaction might return different values if another transaction committed in between.

### 2. Snapshot Isolation and MVCC (Multi-Version Concurrency Control)
- **Core Principle:** **"Readers never block writers, and writers never block readers."**
- **How MVCC Works:**
  - Each transaction has a monotonically increasing `Transaction ID` ($TxID$).
  - When a row is modified, the DB does not overwrite it; it appends a new version tagged with `created_by_tx` and `deleted_by_tx`.
  - A transaction only sees data versions created by transactions that committed *before* its own start timestamp.

```
Row ID 101 Version History:
• v1: created_by=Tx10, deleted_by=Tx25  (Val: $100)
• v2: created_by=Tx25, deleted_by=null  (Val: $150)

Tx20 reads Row 101 -> Sees v1 ($100) because Tx25 committed AFTER Tx20 started.
Tx30 reads Row 101 -> Sees v2 ($150).
```

- **Vulnerability:** **Write Skew & Phantom Reads**. Occurs when two transactions read the same premise, decide independently to take action, and commit conflicting state updates (e.g., Two doctors on call both check `count(on_call) >= 2`, both press `leave_shift()`, resulting in 0 doctors on call!).

---

## 3. Serializability (The Strongest Isolation Level)

Guarantees that the outcome of concurrent execution is identical to some strictly serial (one-at-a-time) execution.

### Three Ways to Achieve Serializability:

1. **Actual Serial Execution (Single-Threaded Loop):**
   - Execute all transactions sequentially on a single thread (VoltDB, Redis).
   - Require transactions to be short stored procedures that fit in memory.

2. **Two-Phase Locking (2PL / Pessimistic Locking):**
   - **Shared Lock (S):** Required for reading. Multiple transactions can hold S-locks.
   - **Exclusive Lock (X):** Required for writing. Blocks both readers and writers.
   - **Phases:** Phase 1 (Acquire locks) $\rightarrow$ Phase 2 (Release all locks at commit/abort).
   - **Cons:** High lock contention overhead, risk of deadlocks.

3. **Serializable Snapshot Isolation (SSI / Optimistic Locking):**
   - Used in **PostgreSQL** and **CockroachDB**.
   - Transactions run concurrently under Snapshot Isolation without blocking.
   - The database tracks read dependencies (premises). Before committing, the DB checks if any premises read by the transaction were modified by a concurrent committed transaction.
   - If a premise conflict (Write Skew) is detected, the transaction is **aborted** and retried.

---

## 4. Isolation Level & Race Condition Summary Matrix

| Isolation Level | Dirty Read | Non-Repeatable Read | Read Skew | Write Skew | Phantom Read | Implementation |
|---|---|---|---|---|---|---|
| **Read Uncommitted** | ❌ Allowed | ❌ Allowed | ❌ Allowed | ❌ Allowed | ❌ Allowed | Raw memory access |
| **Read Committed** | ✅ Prevented | ❌ Allowed | ❌ Allowed | ❌ Allowed | ❌ Allowed | Row locks & Read locks |
| **Snapshot Isolation** | ✅ Prevented | ✅ Prevented | ✅ Prevented | ❌ Allowed | ❌ Allowed | MVCC |
| **Serializability** | ✅ Prevented | ✅ Prevented | ✅ Prevented | ✅ Prevented | ✅ Prevented | 2PL or SSI or Single-thread |
