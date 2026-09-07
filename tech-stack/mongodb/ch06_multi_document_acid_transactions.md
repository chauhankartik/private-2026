# Chapter 6: Multi-Document ACID Transactions & Distributed Consensus

## 1. Single-Document vs Multi-Document Transactions

MongoDB has always guaranteed atomic, ACID operations on single documents (including embedded documents and arrays). Introduced in MongoDB 4.0 (for Replica Sets) and 4.2 (for Sharded Clusters), **Multi-Document ACID Transactions** provide multi-statement cross-collection and cross-shard transaction guarantees.

---

## 2. Multi-Version Concurrency Control (MVCC) Timestamps

MongoDB leverages WiredTiger's internal timestamping mechanism to enforce Snapshot Isolation without acquiring global database locks.

```
       Global Transaction Start Timestamp: T_10
                         |
  +----------------------+----------------------+
  |                                             |
  v Snapshot Read                               v Snapshot Read
Collection A (Read state at T_10)             Collection B (Read state at T_10)
  |                                             |
  v Write Lock Node A                           v Write Lock Node B
Update Doc A (Prepare Commit T_15)            Update Doc B (Prepare Commit T_15)
  |                                             |
  +----------------------+----------------------+
                         |
                         v
       Global Transaction Commit Timestamp: T_15
```

### WiredTiger Timestamp Hierarchy:
1. **Read Timestamp ($T_{\text{read}}$):** Transaction sees a consistent snapshot of data committed prior to $T_{\text{read}}$. Reads are unblocked by concurrent writes.
2. **Commit Timestamp ($T_{\text{commit}}$):** Write operations are tagged with $T_{\text{commit}}$ when written to WiredTiger.
3. **Durable Timestamp ($T_{\text{durable}}$):** Highest timestamp persisted to majority journal disk storage.

---

## 3. Two-Phase Commit (2PC) Protocol across Shards

For sharded clusters, multi-document transactions span multiple independent shard replica sets coordinated by a **Transaction Coordinator** `mongos` or primary shard node.

```
  Transaction Coordinator                   Shard 1 (Participant)                 Shard 2 (Participant)
           |                                         |                                     |
           | ---------- 1. PREPARE ----------------->|                                     |
           | ---------- 1. PREPARE ------------------------------------------------------->|
           |                                         |                                     |
           |                                 Applies Write Locks                   Applies Write Locks
           |                                 Returns Prepared Ack                  Returns Prepared Ack
           |<---------- Prepared Ack ----------------|                                     |
           |<---------- Prepared Ack ------------------------------------------------------|
           |                                         |                                     |
    Writes Transaction                               |                                     |
    Commit Decision to Oplog                         |                                     |
           |                                         |                                     |
           | ---------- 2. COMMIT ------------------>|                                     |
           | ---------- 2. COMMIT -------------------------------------------------------->|
           |                                         |                                     |
                                             Releases Locks                        Releases Locks
```

---

## 4. Performance Overheads & Transaction Constraints

While multi-document transactions provide strict ACID isolation, they impose operational trade-offs:

1. **Transaction Time Limit (`transactionLifetimeLimitSeconds`):** Transactions have a maximum lifetime (default **60 seconds**). If a transaction stays active past this limit, the server automatically aborts the transaction and releases WiredTiger write locks.
2. **WiredTiger Cache Pressure:** Uncommitted updates are retained in memory. Long-running transactions prevent WiredTiger clean eviction, causing RAM pressure and dirty page spikes.
3. **Lock Conflicts (`TransientTransactionError`):** Concurrent transactions modifying the same document fail immediately with write conflicts. Applications must implement retry loops:

```python
def run_transaction_with_retry(session, txn_body):
    while True:
        try:
            session.start_transaction()
            txn_body(session)
            session.commit_transaction()
            break
        except (PyMongoError) as e:
            if e.has_error_label("TransientTransactionError"):
                continue
            elif e.has_error_label("UnknownTransactionCommitResult"):
                continue
            else:
                raise
```
