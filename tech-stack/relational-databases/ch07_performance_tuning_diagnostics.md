# Chapter 7: Performance Diagnostics, Wait Statistics & Troubleshooting

Performance tuning requires empirical diagnostics rather than guesswork. This chapter covers query plan analysis, lock contention identification, memory buffer stats, and wait statistics in **PostgreSQL** and **Microsoft SQL Server**.

---

## 1. PostgreSQL Diagnostics & Query Profiling

### 1.1 `EXPLAIN (ANALYZE, BUFFERS)`
The primary tool for diagnosing Postgres query execution is `EXPLAIN (ANALYZE, BUFFERS)`.
* `ANALYZE`: Executes the query and compares estimated row counts against actual row counts.
* `BUFFERS`: Reports exact buffer pool access:
  * `Shared Hit`: Pages found in `shared_buffers` memory.
  * `Shared Read`: Pages read from OS disk cache or persistent storage.
  * `Shared Dirtied`: Clean pages modified during execution.

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT u.id, o.total_amount
FROM users u
JOIN orders o ON u.id = o.user_id
WHERE u.created_at >= '2026-01-01';
```

**Diagnostic Indicators:**
* **High `Shared Read`:** Indicates missing indexes or insufficient `shared_buffers`.
* **Row Count Misestimate (Estimated 1 vs Actual 500,000):** Indicates stale statistics (`ANALYZE` needed) or unindexed multi-column correlations.

### 1.2 `pg_stat_statements` & Activity Monitoring
* **`pg_stat_statements`:** Tracking view aggregating normalized query performance across the database cluster (calls, total_exec_time, mean_exec_time, shared_blks_hit, shared_blks_read).
* **Identifying Locks (`pg_stat_activity`):**
  ```sql
  SELECT pid, query, state, wait_event_type, wait_event, pg_blocking_pids(pid)
  FROM pg_stat_activity
  WHERE wait_event_type = 'Lock';
  ```

---

## 2. MS SQL Server Diagnostics: DMVs & Wait Statistics

MS SQL Server uses a **Wait Statistics** framework to isolate performance bottlenecks. Every thread waiting for CPU, memory, or disk I/O registers a specific **Wait Type**.

### 2.1 Key SQL Server Wait Statistics

| Wait Type | Primary Root Cause | Remediation Strategy |
| :--- | :--- | :--- |
| **`PAGEIOLATCH_SH` / `PAGEIOLATCH_EX`** | Thread is waiting to read/write a data page from disk into the Buffer Pool. | Missing index causing full table scans, or inadequate disk I/O throughput / RAM. |
| **`PAGELATCH_UP` / `PAGELATCH_EX`** | In-memory contention on metadata pages (`PFS`, `GAM`) inside `tempdb`. | Add more `tempdb` data files split evenly; enable trace flag 1118/1117. |
| **`CXPACKET` / `CXCONSUMER`** | Parallel query execution threads waiting for asynchronous worker sync. | Check `Max Degree of Parallelism (MAXDOP)` and `Cost Threshold for Parallelism`. |
| **`ASYNC_NETWORK_IO`** | SQL Server sent result rows to application client, but client application is slow to process/consume data. | Fix application row fetching loop; avoid fetching massive unpaginated datasets. |

### 2.2 Dynamic Management Views (DMVs)
* **`sys.dm_exec_requests`:** Real-time snapshot of active queries, wait types, blocking session IDs, and CPU time.
* **`sys.dm_exec_query_stats`:** Aggregated execution metrics from the compiled Plan Cache.
* **Query Store:** Built-in performance repository recording query runtime history and execution plan changes over time, enabling explicit plan forcing.

---

## 3. High-Impact Performance Antipatterns

### 3.1 Implicit Type Conversion (SARGability Loss)
* **Issue:** Passing a `NVARCHAR` parameter to a column stored as `VARCHAR` in SQL Server forces the engine to run `CONVERT_IMPLICIT(col)` on every row, disabling B+ tree range scans (**Index Scan** instead of **Index Seek**).
* **Fix:** Align application parameter types with exact database schema types.

### 3.2 SQL Server Parameter Sniffing
* **Issue:** SQL Server compiles a query plan based on the literal parameter passed during initial execution. If the initial parameter targets 1 row (Seek) and subsequent calls target 1,000,000 rows, the cached plan experiences severe execution degradation.
* **Fix:** Use `OPTION (RECOMPILE)` or `OPTIMIZE FOR (@param UNKNOWN)`.

### 3.3 PostgreSQL Table & Index Bloat
* **Issue:** High `UPDATE`/`DELETE` volume without aggressive autovacuum creates dead tuples, forcing scans to read thousands of empty 8KB page slots.
* **Diagnostics:** Query `pgstattuple` extension.
* **Fix:** Perform zero-downtime table defragmentation using `pg_repack` (rebuilds heap file and secondary indexes without acquiring table exclusive locks).

---

## 4. Master Diagnostic Comparison Table

| Diagnostic Workflow | PostgreSQL Engine | MS SQL Server Engine |
| :--- | :--- | :--- |
| **Query Plan Analysis** | `EXPLAIN (ANALYZE, BUFFERS)` | Execution Plan (XML / SSMS Visual Plan) |
| **Historical Query Metrics** | `pg_stat_statements` view | **Query Store** / `sys.dm_exec_query_stats` |
| **Wait Event Identification** | `pg_stat_activity.wait_event` | **`sys.dm_os_wait_stats`** DMV |
| **Index Defragmentation** | `VACUUM (FULL)` or `pg_repack` | `ALTER INDEX REBUILD` / `REORGANIZE` |
| **Lock Blocking Tree** | `pg_blocking_pids(pid)` | `sys.dm_exec_requests` (`blocking_session_id`) |

---

## 5. Staff Engineer Performance Tuning SLA Framework
1. **Identify Top 5 Queries by Total Time:** Periodically query `pg_stat_statements` (Postgres) or Query Store (SQL Server) to optimize queries consuming the highest aggregate CPU/IO time.
2. **Buffer Cache Hit Ratio SLA:** Maintain `Buffer Cache Hit Ratio > 99%`. If hit ratio drops below 95%, scale up memory or optimize I/O-heavy queries.
3. **Automate Index Maintenance:** In SQL Server, reindex fragmented tables (`> 30%` fragmentation) using automated scripts (e.g., Ola Hallengren's Maintenance Solution). In Postgres, monitor bloat via `pgstattuple` and schedule `pg_repack`.
