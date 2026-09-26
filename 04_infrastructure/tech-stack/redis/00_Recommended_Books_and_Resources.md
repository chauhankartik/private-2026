# Redis Recommended Reading List & Reference Manuals

A curated list of books, technical specifications, source code pointers, and foundational engineering papers for mastering Redis architecture and internal mechanics.

---

## 📚 Recommended Books

1. **_Redis in Action_** — Josiah L. Carlson (Manning Publications)
   * **Why Read It:** Comprehensive coverage of real-world Redis application patterns, data structure selection, pub/sub queues, caching strategies, and Lua script execution.
   * **Key Focus:** Practical system design patterns using Redis primitives.

2. **_System Design Interview – An Insider's Guide (Volume 1 & 2)_** — Alex Xu
   * **Why Read It:** Excellent chapters on Distributed Caching, Rate Limiters, Distributed Key-Value Stores, and News Feed Systems built on Redis.
   * **Key Focus:** Architectural trade-offs of Redis in large-scale system designs.

3. **_Database Internals: A Deep Dive into How Distributed Data Systems Work_** — Alex Petrov (O'Reilly)
   * **Why Read It:** Provides foundational context for memory storage engines, hash tables, skip lists, WAL persistence, and distributed consensus (relevant to Redis Sentinel & Cluster).

---

## 📄 Official Specifications & Architecture Papers

1. **[Redis Cluster Specification](https://redis.io/docs/reference/cluster-spec/)**
   * **Topics:** 16,384 hash slots, CRC16 hashing, Gossip protocol (`MEET`/`PING`/`PONG`), `MOVED` vs `ASK` redirections, partition handling.
2. **[Redis Sentinel Documentation & Specification](https://redis.io/docs/management/sentinel/)**
   * **Topics:** Subjective Down (`SDOWN`) vs Objective Down (`ODOWN`), Sentinel leader election via Raft-like consensus, epoch management.
3. **[Redis RDB & AOF Persistence Specifications](https://redis.io/docs/management/persistence/)**
   * **Topics:** `fork()` Copy-on-Write mechanism, background AOF rewrite (`BGREWRITEAOF`), `fsync` policies (`always`, `everysec`, `no`).

---

## 💻 Source Code References (Redis C Repository)

For low-level C internals, explore the following files in the [Redis GitHub Repository](https://github.com/redis/redis):

* **`src/sds.c` & `src/sds.h`:** Simple Dynamic String implementation (header, length, alloc, flags).
* **`src/dict.c` & `src/dict.h`:** Hashtable design, dual table (`ht[0]`/`ht[1]`), and incremental rehash (`dictRehash`).
* **`src/t_zset.c`:** Sorted Set implementation combining SkipList (`zskiplist`) and Dict hashtable.
* **`src/ae.c` & `src/ae_epoll.c`:** The core single-threaded event loop engine wrapping Linux `epoll()` / BSD `kqueue()`.
* **`src/expire.c` & `src/evict.c`:** Active/Passive TTL expiration algorithms and approximated LRU/LFU eviction.
