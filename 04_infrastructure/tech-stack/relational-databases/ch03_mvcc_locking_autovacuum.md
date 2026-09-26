# Chapter 3: MVCC, Concurrency Control, Locking & Autovacuum

Multi-Version Concurrency Control (MVCC) enables database engines to allow readers to read data without blocking writers, and writers to write data without blocking readers. However, **PostgreSQL** and **Microsoft SQL Server** achieve MVCC and locking through radically different internal designs.

---

## 1. PostgreSQL MVCC Architecture

PostgreSQL implements **out-of-place tuple versioning**. Every `UPDATE` is physically executed as an `INSERT` of a new tuple version and a logical soft-delete of the old tuple version within the table heap.

```
+-------------------------------------------------------------------------+
| HeapTupleHeaderData                                                     |
|  - t_xmin : Transaction ID that inserted this tuple                     |
|  - t_xmax : Transaction ID that deleted or updated this tuple (0 if live)|
|  - t_cid  : Command Identifier within the transaction                   |
|  - t_ctid : Points to latest tuple version (self ctid or newer ctid)   |
+-------------------------------------------------------------------------+
| Data Payload                                                            |
+-------------------------------------------------------------------------+
```

### 1.1 Tuple Header & Visibility Rules
Every tuple header stores internal control fields:
* **`xmin`:** 32-bit Transaction ID (TxID) of the inserting transaction.
* **`xmax`:** 32-bit TxID of the deleting transaction (or updated-to transaction). If `xmax == 0`, the tuple has not been deleted.
* **`t_ctid`:** If updated, `t_ctid` points to the `(block, offset)` of the newly inserted tuple version.

#### Transaction Snapshots (`xmin:xmax:xip_list`)
When a query executes, PostgreSQL builds a **Snapshot** represented as `xmin:xmax:xip_list`:
* `xmin`: Lowest TxID still active. All TxIDs < `xmin` are committed and visible.
* `xmax`: First unassigned TxID. All TxIDs >= `xmax` are uncommitted and invisible.
* `xip_list`: Array of active TxIDs between `xmin` and `xmax` at snapshot creation time.

**Visibility Matrix:**
A tuple is visible to snapshot `S` if:
1. `t_xmin` is committed AND `t_xmin` < `S.xmax` AND `t_xmin` is NOT in `S.xip_list`.
2. AND (`t_xmax` is 0 OR `t_xmax` is uncommitted OR `t_xmax` > `S.xmax` OR `t_xmax` is in `S.xip_list`).

---

## 2. PostgreSQL Autovacuum & Transaction Wraparound

Because Postgres writes new tuple versions into the heap, old versions become **dead tuples** when no active snapshot can see them.

### 2.1 Responsibilities of Autovacuum
1. **Reclaim Dead Tuples:** Reclaims dead tuple line pointers within 8KB pages so new inserts can reuse space.
2. **Update Visibility Map (`_vm`):** Sets the `all-visible` flag on pages where all tuples are committed, allowing **Index-Only Scans**.
3. **Update Optimizer Statistics:** Triggers `ANALYZE` to refresh `pg_statistic` histograms used by the query planner.
4. **Transaction ID Freezing (Wraparound Protection):**
   * PostgreSQL TxIDs are 32-bit integers (~4.2 billion values).
   * Epoch comparison uses modulo arithmetic: half the space (2 billion IDs) is in the past, half in the future.
   * If a database processes 2 billion transactions without freezing, TxIDs wrap around, causing historic data to suddenly become invisible (catastrophic data corruption).
   * **Freeze Mechanism:** Vacuum marks older tuples with `FrozenTransactionId` (`2`), moving their `xmin` permanently into the infinite past.

---

## 3. MS SQL Server Concurrency: Locks & RCSI

Unlike Postgres, MS SQL Server defaults to **Pessimistic Locking**, but supports **Read Committed Snapshot Isolation (RCSI)** using an **in-place update + Version Store in `tempdb`**.

```
SQL Server Tuple (Heap/Clustered Index Page)
+---------------------------------------------------+
| Row Data (Updated In-Place)                       |
+---------------------------------------------------+
| 14-Byte Version Pointer ---> TempDB Version Store |
|                              (Old tuple version)  |
+---------------------------------------------------+
```

### 3.1 SQL Server Lock Hierarchy & Lock Escalation
SQL Server manages concurrency using explicit lock structures in memory.

#### Lock Hierarchy
`Database` -> `Table` -> `Extent` -> `Page` -> `Row / Key`

#### Lock Modes
* **Shared (S):** Read operations.
* **Exclusive (X):** Write operations.
* **Update (U):** Prevents deadlocks during read-before-write scan patterns.
* **Intent Locks (IS, IX, SIX):** Placed at higher levels (Table/Page) to signify that granular locks exist below, preventing full-table exclusive locks by concurrent transactions.

#### Lock Escalation
When a single query acquires more than **5,000 granular locks** on a single table, SQL Server automatically escalates Page/Row locks into a single **Table Lock (X or S)** to save memory.
* **Impact:** Causes sudden concurrency bottlenecks where non-overlapping row updates block each other.

### 3.2 Read Committed Snapshot Isolation (RCSI)
When `READ_COMMITTED_SNAPSHOT ON` is enabled:
1. Writers update rows **in-place** in the main data page.
2. Before updating, SQL Server copies the pre-image (old row version) to the **Version Store in `tempdb`**.
3. A 14-byte version pointer is appended to the row header pointing to `tempdb`.
4. Readers read non-blocking snapshot versions from `tempdb` while writers hold exclusive locks on the primary data page.

---

## 4. Architectural Comparison: Vacuum vs TempDB Version Store

| Metric | PostgreSQL Out-of-Place MVCC | MS SQL Server RCSI (`tempdb`) |
| :--- | :--- | :--- |
| **Write Amplification** | **High in Heap & Secondary Indexes** (unless HOT applies) | Low in Primary Page (In-place); Version written to `tempdb` |
| **Garbage Collection Overhead** | Background `Autovacuum` processes heap pages periodically | Asynchronous Cleanup Thread purges `tempdb` version store |
| **Risk Factor** | Table Bloat & TxID Wraparound if autovacuum lags | `tempdb` space exhaustion / disk saturation under long transactions |
| **Lock Escalation Risk** | **None** (Postgres uses lockless MVCC for tuples; memory for locks is minimal) | **High** (Default locking escalates to Table locks at 5,000 locks) |

---

## 5. Staff Engineer Summary & Tuning Recommendations
1. **Postgres Autovacuum Tuning:** Never disable autovacuum. Lower `autovacuum_vacuum_scale_factor` from 0.2 (20%) to 0.05 (5%) on high-throughput write tables to prevent massive table bloat.
2. **Preventing TxID Wraparound Outages:** Monitor `pg_database.datfrozenxid`. Alert if `age(datfrozenxid) > 150,000,000`.
3. **MS SQL Server Escalation Prevention:** Use `ALTER TABLE tbl SET (LOCK_ESCALATION = DISABLE)` or partition large tables to restrict lock escalation to partition level (`LOCK_ESCALATION = AUTO`).
4. **`tempdb` Sizing for RCSI:** Pre-allocate `tempdb` data files split evenly across CPU cores to avoid allocation bitmap contention (`PFS`/`GAM` page latch waits).
