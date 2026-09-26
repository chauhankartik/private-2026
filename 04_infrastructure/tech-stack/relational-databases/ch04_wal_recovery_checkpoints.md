# Chapter 4: Write-Ahead Logging (WAL), Recovery & Checkpoints

To ensure ACID **Durability** and **Atomicity**, relational database engines never write modified data pages directly to primary storage on every transaction commit. Instead, changes are recorded sequentially in an append-only transaction log first.

This chapter analyzes the internal logging engines, crash recovery mechanisms, and checkpoint algorithms of **PostgreSQL** and **Microsoft SQL Server**.

---

## 1. The WAL Principle & ARIES Recovery

Both engines adhere to the **WAL (Write-Ahead Logging)** rule and the **ARIES (Algorithm for Recovery and Isolation Exploiting Semantics)** crash recovery protocol.

### 1.1 The Write-Ahead Logging Rule
> **Rule:** Dirty data pages cached in the Buffer Pool **must NOT** be written to disk until the WAL records describing the modifications have been safely flushed to persistent storage (disk `fsync`).

### 1.2 The 3 Phases of ARIES Recovery
When a database engine crashes and restarts, it executes 3 distinct phases over the WAL records:

```
Crash Point
    |
    v
[1. Analysis Phase]   --> Scans WAL forward from last Checkpoint to identify:
                          - Active transactions at crash time (Dirty Tx Table)
                          - Dirty pages in buffer pool (Dirty Page Table)
    |
    v
[2. Redo Phase]       --> Rolls FORWARD from lowest Redo LSN. Replays ALL changes 
                          (committed & uncommitted) to restore DB state to crash point.
    |
    v
[3. Undo Phase]       --> Rolls BACKWARD, reversing changes made by active, 
                          uncommitted transactions at the time of crash.
```

---

## 2. PostgreSQL WAL Architecture

In PostgreSQL, WAL records are written to 16MB segment files located in the `pg_wal/` directory.

### 2.1 Log Sequence Numbers (LSN)
* Every WAL record is assigned a 64-bit integer called a **Log Sequence Number (LSN)**, representing the exact byte offset within the database's log stream (e.g., `0/16B3748`).
* Every 8KB data page header stores `pd_lsn`, the LSN of the latest WAL record that updated that specific page.
* **Buffer Flush Gatekeeper:** During a checkpoint or page flush, the buffer manager checks `page.pd_lsn <= flushed_wal_lsn`. If false, the engine blocks the page flush until the WAL buffer is flushed up to `pd_lsn`.

### 2.2 PostgreSQL Checkpoints & Background Processes
A **Checkpoint** flushes all dirty buffer pool pages modified prior to the checkpoint start point to disk and advances the **Redo LSN**.

```
Checkpointer Process Flow
+--------------------+      Flushes Dirty Pages      +------------------+
| Buffer Pool        | ---------------------------> | OS File System   |
| (Dirty 8KB Pages)  |     (Spread Over Time)       | Disk Storage     |
+--------------------+                              +------------------+
         ^                                                   ^
         | Write WAL Logs                                    | fsync()
+--------------------+                              +------------------+
| WAL Buffers        | ---------------------------> | pg_wal/ Segment  |
+--------------------+                              +------------------+
```

* **Checkpointer Process:** Performs scheduled checkpoints based on `checkpoint_timeout` (e.g., 5 min) or `max_wal_size` (e.g., 1GB).
* **`checkpoint_completion_target` (default `0.9`):** Smooths I/O spikes by spreading dirty page writes across 90% of the duration between checkpoints.
* **Background Writer Process:** Continually flushes dirty pages in small batches independent of checkpoints, ensuring clean pages are readily available in the buffer pool for new reads.

---

## 3. MS SQL Server Transaction Log & ADR

SQL Server manages transaction logs inside dedicated `.ldf` files using **Virtual Log Files (VLFs)**.

```
+-------------------------------------------------------------------+
| database_log.ldf                                                  |
| +----------+----------+----------+----------+----------+---------+ |
| | VLF 1    | VLF 2    | VLF 3    | VLF 4    | VLF 5    | VLF 6   | |
| | (Active) | (Active) | (Active) | (Truncated)| (Free) | (Free)  | |
| +----------+----------+----------+----------+----------+---------+ |
+-------------------------------------------------------------------+
```

### 3.1 VLF Architecture & Fragmentation
* A transaction log file is internally divided into multiple **Virtual Log Files (VLFs)**.
* **Log Truncation:** When a checkpoint occurs in `SIMPLE` recovery mode or after a log backup in `FULL` recovery mode, inactive VLFs are marked as reusable (truncated).
* **VLF Fragmentation Hazard:** If a log file grows in small incremental steps (e.g., 1MB auto-growth), SQL Server creates thousands of tiny VLFs, causing extremely slow database startup, backup, and crash recovery times.

### 3.2 Accelerated Database Recovery (ADR)
Introduced in SQL Server 2019, **ADR** dramatically redesigns the ARIES recovery process, enabling **instant database recovery** and instantaneous rollback of long-running transactions regardless of log size.

**Key ADR Components:**
1. **Persistent Version Store (PVS):** Row versioning stored inside the user database (not `tempdb`).
2. **sLog (Secondary Log):** An in-memory stream tracking non-versioned operations (e.g., locks, DDL).
3. **Logical Revert:** Instead of scanning the physical `.ldf` backwards row-by-row during the ARIES Undo phase, ADR instantly reverts uncommitted rows using the PVS pointers.

---

## 4. Architectural Comparison Matrix

| Feature | PostgreSQL WAL Engine | MS SQL Server Transaction Log |
| :--- | :--- | :--- |
| **Physical File Organization** | Multiple 16MB file segments (`pg_wal/0000000100...`) | Single or multiple `.ldf` container files divided into **VLFs** |
| **Log Truncation Mechanism** | Automated removal/recycling of segments past `pg_control` Redo LSN | Log Truncation triggered by Checkpoint (SIMPLE) or Log Backup (FULL) |
| **Long-Running Tx Rollback** | Must sequentially undo WAL records backward (slow) | Instantaneous with **Accelerated Database Recovery (ADR)** |
| **Full Page Writes (FPW)** | Writes full 8KB page to WAL on first modification after checkpoint (prevents torn pages) | Uses **Torn Page Detection** / **Checksums** on 8KB page headers |

---

## 5. Staff Engineer Operational Playbook
1. **Postgres Full Page Writes Overhead:** High write workload after a checkpoint can cause WAL spikes due to `full_page_writes = on`. Ensure `checkpoint_completion_target = 0.9` to spread I/O load.
2. **VLF Monitoring in SQL Server:** Run `DBCC LOGINFO` or query `sys.dm_db_log_info(db_id())`. Keep total VLF count under **500**. Fix fragmentation by shrinking log and pre-allocating `.ldf` in larger chunks (e.g., 4GB or 8GB increments).
3. **Sizing WAL Allocation:** In Postgres, set `max_wal_size` large enough (e.g., 16GB - 64GB) to prevent premature checkpoint triggering during bulk load spikes.
