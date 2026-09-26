# Relational Database Architecture Mind Map & Decision Matrix

> **Purpose:** Structural decision-making for choosing PostgreSQL vs MS SQL Server storage configurations, indexing strategies, isolation levels, and high availability topologies.

---

## 📊 PostgreSQL vs. MS SQL Server Engine Comparison Matrix

| Architecture Component | PostgreSQL Engine | Microsoft SQL Server Engine |
| :--- | :--- | :--- |
| **Process Model** | Multi-Process (`postmaster` forks backend worker processes) | Single-Process Multi-Threaded (`sqlservr.exe` via SQLOS) |
| **Default Storage Layout**| Heap Files (Unordered rows with `ctid` pointers) | Clustered Index Tables (Rows physically ordered by cluster key) |
| **Page Size** | 8 KB (Fixed page size) | 8 KB (Data pages grouped into 64KB Extents) |
| **MVCC Implementation** | Out-of-place tuple versioning inside table Heap (`xmin`/`xmax`) | In-place update with row versions stored in `tempdb` (RCSI) |
| **Garbage Cleanup** | Autovacuum (Cleans dead tuples & freezes TxIDs) | Automatic Ghost Record cleanup & `tempdb` version store cleanup |
| **Indexing Primitives** | B+ Tree, Hash, GIN (Inverted), GiST, BRIN, SP-GiST | B+ Tree (Clustered / Non-Clustered), Columnstore, Spatial |
| **Connection Pooler** | Recommended external pooler (`PgBouncer`) | Built-in client connection pool (`System.Data.SqlClient`) |
| **High Availability** | Streaming Replication + Patroni / Repmgr | Always On Availability Groups (AG) / Failover Cluster (FCI) |
