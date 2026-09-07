# Chapter 6: Replication, High Availability & Connection Pooling (PgBouncer)

Relational databases in high-throughput enterprise environments rely on replication for fault tolerance, read scaling, and disaster recovery. Connection pooling is equally vital to manage client connection overhead.

This chapter compares **PostgreSQL Replication & PgBouncer** with **MS SQL Server Always On Availability Groups**.

---

## 1. PostgreSQL Replication Architecture

PostgreSQL provides two primary replication models: **Physical Streaming Replication** and **Logical Replication**.

```
+------------------+                    +------------------+
| Primary Node     | -- WAL Stream -->  | Physical Standby |
| (Read / Write)   | (Byte-for-Byte)    | (Read-Only)      |
+------------------+                    +------------------+
         |
    Logical Decoding (pgoutput)
         |
         v
+------------------+
| Subscriber Node  | (Table-level DML replication)
+------------------+
```

### 1.1 Physical Streaming Replication
* **Mechanics:** Primary node streams raw byte-level WAL records over TCP via `walsender` process to `walreceiver` on standby nodes.
* **Standby Mode (Hot Standby):** Standby continuously replays WAL records into its local data files, remaining open for read-only queries.
* **`synchronous_commit` Levels:**
  * `off`: Transaction commits in memory; zero wait for WAL disk flush.
  * `local` (Default): Transaction commits after WAL is flushed to local primary disk.
  * `remote_write`: Primary waits until standby receives and writes WAL to OS buffer.
  * `on`: Primary waits until standby receives and flushes WAL to disk.
  * `remote_apply`: Primary waits until standby receives, flushes, AND applies WAL record to data pages (guarantees immediate read-after-write consistency on standby).

### 1.2 Logical Replication (Publish / Subscribe)
* **Mechanics:** A logical decoding plugin (`pgoutput`) reads raw WAL bytes and translates them into logical DML change streams (`INSERT`, `UPDATE`, `DELETE`).
* **Use Cases:** Replicating specific tables across different PostgreSQL major versions, consolidating data from multiple databases into a central data warehouse, or zero-downtime database upgrades.

---

## 2. PostgreSQL Connection Pooling: PgBouncer

Because PostgreSQL forks a separate OS process per client connection, memory overhead and process context-switching degrade performance when connections exceed ~500-1,000 active clients.

```
Client Apps (10,000 Threads) ---> [ PgBouncer (epoll) ] ---> Postgres Server (100 Process Backends)
```

### 2.1 PgBouncer Architecture
`PgBouncer` is a lightweight, event-driven (libevent/epoll) single-threaded proxy that sits in front of PostgreSQL, multiplexing thousands of client connections into a tiny pool of backend server connections.

### 2.2 Pooling Modes

| Mode | Mechanism | Trade-off / Compatibility |
| :--- | :--- | :--- |
| **Session Pooling** (Default) | Assigns backend server connection to client when client logs in; releases on disconnect. | Fully compatible with all SQL features; minimal connection reduction benefit. |
| **Transaction Pooling** | Assigns backend connection to client **only for the duration of a single transaction** (`BEGIN` to `COMMIT`). | **Recommended for microservices**. Disallows named prepared statements, `LISTEN/NOTIFY`, and `SET` session variables across transactions. |
| **Statement Pooling** | Recycles connection after **every single SQL statement**. | Breaks multi-statement transactions (`BEGIN ... COMMIT`). Useful only for single-statement read workloads. |

---

## 3. MS SQL Server Always On Availability Groups (AG)

MS SQL Server achieves enterprise High Availability (HA) and Disaster Recovery (DR) via **Always On Availability Groups**, built on top of Windows Server Failover Clustering (WSFC).

```
+-----------------------+                         +-----------------------+
| Primary Replica       | --- Log Stream (.ldf) ->| Secondary Replica     |
| (Read / Write)        | (Sync or Async Commit)  | (Readable Secondary)  |
+-----------------------+                         +-----------------------+
            \                                                /
             +-------------> [ AG Listener ] <---------------+
                       (Virtual IP / Network Name)
```

### 3.1 Architecture & Flow
1. **Replication Unit:** Operates at the **Database Group** level rather than the entire SQL Server instance.
2. **Log Transport:** Changes made on the Primary replica are streamed as physical transaction log (`.ldf`) blocks to Secondary replicas.
3. **Redo Thread:** Secondary replicas continuously apply log blocks to secondary data files. If configured as **Readable Secondary**, read queries can execute concurrently.
4. **Availability Group Listener:** A virtual network name and IP address that routes client application traffic automatically to the active Primary or to Readable Secondaries via **Read-Only Routing**.

---

## 4. Architectural Comparison Matrix

| Operational Dimension | PostgreSQL Replication & Pooling | MS SQL Server Always On AG |
| :--- | :--- | :--- |
| **Replication Scope** | Cluster-level (Physical) or Table-level (Logical) | Database Group level (Availability Group) |
| **Connection Pooling** | External component required (**PgBouncer**, `pgpool-II`) | Internal driver connection pooling (System.Data / OLE DB) |
| **Failover Management** | Managed via external orchestrators (**Patroni** + `etcd` / `consul`) | Native WSFC Cluster Failover & AG Listener |
| **Read-Only Routing** | Managed via PgBouncer / HAProxy routing targets | Built-in AG Listener Read-Only Routing parameters |

---

## 5. Staff Engineer HA Architecture Recommendations
1. **Always Deploy PgBouncer in Transaction Mode:** Place PgBouncer sidecars or dedicated instances between app pods and Postgres. Set `max_client_conn = 10000` and `default_pool_size = 50-100`.
2. **Patroni for Automated Postgres Failover:** Combine Physical Streaming Replication with **Patroni** and `etcd` to provide consensus-driven leader election and automatic failover.
3. **SQL Server Synchronous Commit Latency:** In Always On AGs, placing secondary replicas across high-latency WAN links in `SYNCHRONOUS_COMMIT` mode will stall write transactions on the primary (`HADR_SYNC_COMMIT` wait type). Use `ASYNCHRONOUS_COMMIT` for cross-region DR replicas.
