# Chapter 8: Database Connection Pooling & Multi-Database Architecture (`zeyt`, `zeyt_ta_data`, `zeyt_deleted`)

> **Executive Overview:** Connection pooling is the single most critical performance component in database-backed applications. This chapter breaks down how connection pooling works under the hood (at the TCP, JVM, and SQL engine level), how to architect multi-database connection pools for complex schemas containing hundreds of tables (`zeyt`, `zeyt_ta_data`, `zeyt_deleted`), and how to implement production-grade Spring Boot HikariCP configurations.

---

## 1. How Database Connection Pooling Works (Under the Hood)

### The Hidden Cost of Raw Physical Connections

Creating a raw physical database connection without a pool is extremely expensive. Every `DriverManager.getConnection()` invocation triggers the following sequence:

```
Client App                                                              Database Server
    │                                                                          │
    ├─────────── 1. TCP 3-Way Handshake (SYN -> SYN-ACK -> ACK) ──────────────>│ (1 RTT)
    │                                                                          │
    ├─────────── 2. TLS / SSL Security Negotiation ───────────────────────────>│ (1-2 RTT)
    │                                                                          │
    ├─────────── 3. Database Authentication & Handshake ──────────────────────>│ (1 RTT)
    │                                                                          │
    │            4. Backend Process/Thread Allocation                           │ (Postgres forks backend process)
    │               - Postgres: forks OS process (allocates 2-10MB RAM)       │ (MySQL assigns worker thread)
    │               - Sets GUC variables, encoding, timezone, search_path     │
    │                                                                          │
    ├─────────── 5. Connection Ready for SQL Execution ────────────────────────>│
```

- **Total Time per Connection Creation:** 30ms – 250ms (depending on network latency and TLS).
- **Memory Cost:** In PostgreSQL, each backend process consumes **2–10MB of native RAM**. Opening 1,000 raw connections burns 2–10GB of RAM on the database host before executing a single query!

---

## 2. HikariCP Pool Mechanics & `ConcurrentBag` Architecture

Modern high-performance connection pools (like **HikariCP**) eliminate lock contention when borrowing and returning connections using a lock-free data structure called **`ConcurrentBag`**.

```
                           HikariCP Connection Borrow Pipeline
                                           │
                                           ▼
                   ┌───────────────────────────────────────────────┐
                   │  1. Check ThreadLocal List (Zero Locks)       │
                   │     - Has this thread borrowed a connection   │
                   │       recently that is currently idle?        │
                   └───────────────────────────────────────────────┘
                                     /           \
                                FOUND             NOT FOUND
                                 /                 \
                                ▼                   ▼
                   ┌──────────────────────┐  ┌───────────────────────────────┐
                   │ Borrow in < 250ns    │  │ 2. Check Shared CopyOnWrite   │
                   │ (Lock-free handoff)  │  │    ArrayList (Lock-free Loop)  │
                   └──────────────────────┘  └───────────────────────────────┘
                                                       /           \
                                                  FOUND             NOT FOUND
                                                   /                 \
                                                  ▼                   ▼
                                     ┌──────────────────────┐  ┌─────────────────────────────┐
                                     │ Borrow via CAS       │  │ 3. Handoff Queue (Wait up to│
                                     │ State Transition     │  │    connectionTimeout)       │
                                     └──────────────────────┘  └─────────────────────────────┘
```

### HikariCP State Transitions
Each physical connection in the pool transitions between 4 atomic states:
1. `STATE_NOT_IN_USE` (0): Connection is idle in the pool and ready to be borrowed.
2. `STATE_IN_USE` (-1): Connection is currently borrowed by a worker thread.
3. `STATE_REMOVED` (-2): Connection has expired (`maxLifetime`) or failed validation and is marked for eviction.
4. `STATE_RESERVED` (-3): Connection is temporarily reserved for eviction or testing.

---

## 3. Critical Pool Configuration Parameters

| HikariCP Parameter | Recommended Value | Purpose & Operational Impact |
| :--- | :--- | :--- |
| `maximumPoolSize` | Calculated via formula | Maximum physical DB connections allowed in the pool. |
| `minimumIdle` | Equal to `maximumPoolSize` | Keeps pool fixed-size. Prevents pool resizing churn and latency spikes. |
| `connectionTimeout` | `3000ms` (OLTP), `10000ms` (Batch) | Max time a thread waits for a connection before throwing `SQLTransientConnectionException`. |
| `idleTimeout` | `600000ms` (10 mins) | Max time an idle connection stays in pool before retirement (only applies if `minimumIdle < maximumPoolSize`). |
| `maxLifetime` | `1800000ms` (30 mins) | Max total lifespan of a connection in pool. HikariCP automatically retires connections older than `maxLifetime` to prevent stale socket state and firewall drops. |
| `leakDetectionThreshold` | `2000ms` (OLTP), `60000ms` (Analytics) | Emits stack trace log warning if a thread holds a connection outside the pool longer than this threshold. Identifies unclosed JDBC connections! |

---

## 4. Multi-Database Architecture (`zeyt`, `zeyt_ta_data`, `zeyt_deleted`)

When your enterprise system spans multiple database schemas—each managing hundreds of tables—**never use a single connection pool**.

### Why Separate Pools per Database / Schema?

1. **Query Isolation & Starvation Protection:**
   A long-running analytical query or bulk purge on `zeyt_ta_data` or `zeyt_deleted` will **never starve** high-priority user-facing transactions on `zeyt`!
2. **Workload-Tailored Pool Sizing:**
   - `zeyt` (OLTP): High QPS, fast 5ms queries $\to$ Tight timeouts, large fixed pool size.
   - `zeyt_ta_data` (Analytics/Audit): Low QPS, large result sets $\to$ Larger fetch size, longer connection timeout.
   - `zeyt_deleted` (Archive): Rare access, background purge jobs $\to$ Small pool size, high idle timeout.
3. **Database Catalog Overhead:**
   Each PostgreSQL / MySQL database maintains distinct schema metadata caches. Isolating pools per database limits metadata lock contention across hundreds of tables.

```
                                  Enterprise Multi-Pool Architecture
                                                  │
                 ┌────────────────────────────────┼────────────────────────────────┐
                 ▼                                ▼                                ▼
   ┌───────────────────────────┐    ┌───────────────────────────┐    ┌───────────────────────────┐
   │    zeyt_pool (OLTP)       │    │   zeyt_ta_data_pool (OLAP)│    │   zeyt_deleted_pool (Cold)│
   │  - Max Pool: 20           │    │  - Max Pool: 10           │    │  - Max Pool: 5            │
   │  - Timeout: 3000ms        │    │  - Timeout: 10000ms       │    │  - Timeout: 5000ms        │
   │  - Leak Threshold: 2000ms │    │  - FetchSize: 1000        │    │  - IdleTimeout: 10 mins   │
   └─────────────┬─────────────┘    └─────────────┬─────────────┘    └─────────────┬─────────────┘
                 │                                │                                │
                 ▼                                ▼                                ▼
     ┌───────────────────────┐        ┌───────────────────────┐        ┌───────────────────────┐
     │ Database: zeyt        │        │ Database: zeyt_ta_data│        │ Database: zeyt_deleted│
     │ (100s of Core Tables) │        │ (100s of Audit/Logs)  │        │ (100s of Purged/Cold) │
     └───────────────────────┘        └───────────────────────┘        └───────────────────────┘
```

---

## 5. Optimal Pool Sizing Formula

The PostgreSQL / HikariCP team established the canonical formula for optimal DB pool sizing:

$$\text{Maximum Pool Size} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count}$$

- **Example:** For an 8-core DB server with SSD storage (spindle = 1):
  $$\text{Pool Size} = (8 \times 2) + 1 = 17 \approx 20 \text{ connections}$$

> [!IMPORTANT]
> **Counter-Intuitive Truth:** Adding more DB connections **does NOT increase throughput** when the database CPU is saturated. Increasing pool size beyond hardware capacity increases OS thread context switching, disk I/O queueing, and lock contention, causing throughput to **drop** and latency to **spike**!
