# Chapter 1: Relational Process Models & Engine Architecture — Deep Dive Notes

> **Core Theme:** Comparing the **Multi-Process Architecture of PostgreSQL** with the **Single-Process Multi-Threaded SQLOS Architecture of MS SQL Server**.

---

## 1. PostgreSQL Process & Shared Memory Model

PostgreSQL uses a process-per-connection architecture.

```
                           POSTGRESQL PROCESS MODEL
                           
Client Connections ──► [ Postmaster Supervisor Process (Port 5432) ]
                             │ (Forks dedicated OS process per connection)
               ┌─────────────┼─────────────┐
               ▼             ▼             ▼
          Backend 1     Backend 2     Backend 3
               │             │             │
        ┌──────┴─────────────┴─────────────┴──────┐
        │        SHARED MEMORY (shared_buffers)   │
        │  ├── Buffer Pool (8KB Pages)            │
        │  ├── WAL Buffers                        │
        │  └── Lock Table & Caches                │
        └────────────────────┬────────────────────┘
                             │
     ┌───────────────────────┼───────────────────────┐
     ▼                       ▼                       ▼
[ Background Writer ]   [ Checkpointer ]     [ Autovacuum Worker ]
```

### Core Components:
1. **`postmaster`:** Parent supervisor process listening for incoming TCP requests.
2. **Backend Processes:** `postmaster` forks a distinct OS process per client connection. (High memory cost $\to$ Requires `PgBouncer` connection pooler).
3. **Shared Memory (`shared_buffers`):** Shared RAM region holding cached 8KB database pages, WAL buffers, and global lock tables.
4. **Background Processes:**
   - **Background Writer (`bgwriter`):** Flushes dirty 8KB pages from `shared_buffers` to disk gradually.
   - **Checkpointer:** Executes periodic checkpoints to bound WAL recovery time.
   - **Autovacuum Workers:** Scans tables to clean dead tuple versions and freeze transaction IDs.

---

## 2. MS SQL Server Engine & SQLOS Architecture

Unlike Postgres, MS SQL Server runs as a single process (`sqlservr.exe`) containing hundreds of internal threads managed by **SQLOS (SQL Server Operating System)**.

```
                      MS SQL SERVER ARCHITECTURE (sqlservr.exe)
                      
Client Request ──► [ SNI (SQL Server Network Interface) ]
                          │
                          ▼
            [ SQLOS (User-Mode Scheduler - UMS) ]
            (Cooperative Non-Preemptive Scheduling)
                          │
       ┌──────────────────┼──────────────────┐
       ▼                  ▼                  ▼
[ Query Parser ] ──► [ Optimizer ] ──► [ Execution Engine ]
                                             │
                                             ▼
                                    [ Buffer Pool (8KB) ]
```

### The Role of SQLOS:
SQLOS is a user-mode software layer embedded inside `sqlservr.exe` that bypasses Windows kernel scheduling.
- **User-Mode Scheduler (UMS):** Implements cooperative non-preemptive thread scheduling (`yield`), eliminating expensive OS kernel context switches.
- **Buffer Pool:** Manages 8KB data page caching, query workspace memory, and plan caching.

---

## 3. Architecture Comparison Matrix

| Property | PostgreSQL | MS SQL Server |
| :--- | :--- | :--- |
| **Model** | Multi-Process (Process per Connection) | Single-Process Multi-Threaded (`sqlservr.exe`) |
| **Scheduler** | OS Kernel Process Scheduler | User-Mode Scheduler (SQLOS UMS) |
| **Memory Buffer** | `shared_buffers` (typically 25% of RAM) | Buffer Pool (Max Server Memory config) |
| **Connection Overhead** | High (~5MB to 10MB per process) | Low (Lightweight thread worker) |
