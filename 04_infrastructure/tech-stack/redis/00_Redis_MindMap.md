# Redis Architecture Mind Map & Decision Matrix

> **Purpose:** Structural decision-making for choosing Redis caching strategies, eviction models, persistence configurations, and cluster topologies.

---

## 📊 Feature & Architecture Summary Matrix

| Module | Core Mechanism | Trade-off / Limit | System Configuration |
| :--- | :--- | :--- | :--- |
| **Data Structures** | In-memory C structs (SDS, SkipList) | Memory bound by RAM size | `maxmemory <RAM>` |
| **Event Loop** | Single-threaded Reactor (`epoll`) | Long-running queries ($O(N)$ `KEYS *`) block single thread | Multi-threaded I/O (`io-threads 4`) |
| **Eviction** | Approximated LRU/LFU pool (5-sample) | Slight variance from exact LRU | `maxmemory-policy allkeys-lru` |
| **RDB Persistence** | `fork()` Copy-on-Write snapshot | Potential data loss between snapshots | `save 900 1` |
| **AOF Persistence** | Append-only file with `fsync` | Higher disk I/O overhead | `appendfsync everysec` |
| **Replication** | Asynchronous master-replica stream | Non-zero replication lag | `repl-backlog-size 64mb` |
| **Sentinel HA** | Quorum monitoring & auto-failover | Requires at least 3 Sentinels | `sentinel monitor mymaster <ip> <port> 2` |
| **Redis Cluster** | 16,384 Hash Slots & Gossip Protocol | Multi-key queries require same `{hash_tag}` | `cluster-enabled yes` |
