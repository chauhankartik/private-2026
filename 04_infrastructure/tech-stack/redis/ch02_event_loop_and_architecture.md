# Chapter 2: Single-Threaded Architecture & Reactor Event Loop — Deep Dive Notes

> **Core Theme:** How Redis achieves 100,000+ QPS on a single thread using **I/O Multiplexing**, the **Reactor Pattern**, and `ae.c` event loop.

---

## 1. Why is Redis Single-Threaded for Command Execution?

A common misconception is that high-performance databases require multithreading. Redis executes all key-value operations on a **single main thread**.

### Key Reasons:
1. **Memory-Bound Bottleneck:** Redis operations are bound by RAM speed and network throughput, NOT CPU computation.
2. **Zero Lock Contention:** Eliminates expensive mutexes, read/write locks, deadlocks, and context-switching overhead.
3. **Determinism:** Simplifies implementation of atomic operations (`INCR`, `LPOP`, `HSET`).

---

## 2. Non-Blocking I/O & I/O Multiplexing

Redis uses OS-level I/O Multiplexing primitives (`epoll` on Linux, `kqueue` on macOS/BSD) to monitor thousands of client socket connections simultaneously on one thread.

```
Client Sockets (10,000 Connections)
    │
    ▼
[ OS Kernel Multiplexer: epoll / kqueue ]
    │
    ▼ (Returns list of active file descriptors)
[ Redis Event Loop: ae.c ]
    │
    ├─ File Event: Read Request  ──► Execute Command ──► Write Buffer
    └─ Time Event: Server Cron   ──► Key Expiration / Rehash
```

---

## 3. The `ae.c` Event Loop

Redis implements its custom event library in `ae.c`.

### Two Event Categories:
1. **File Events (Socket I/O):**
   - **Readable Events (`AE_READABLE`):** Triggers when client sends new command or establishes connection.
   - **Writable Events (`AE_WRITABLE`):** Triggers when socket send buffer is ready to transmit output back to client.
2. **Time Events (Cron Jobs):**
   - Scheduled tasks executed periodically (e.g. `serverCron()` runs at 10Hz to handle key expiration, AOF rewrite, and cluster gossip).

---

## 4. Multi-Threaded I/O in Redis 6.0+

Starting in Redis 6.0, Redis introduced **Multi-Threaded I/O (`io-threads`)**.

```
                           REDIS 6.0 MULTI-THREADED I/O
                           
Client Requests ──► [ I/O Thread 1 ] ┐
Client Requests ──► [ I/O Thread 2 ] ┼──► (Parsed Commands) ──► [ MAIN THREAD ]
Client Requests ──► [ I/O Thread 3 ] ┘                              │ (Executes Command)
                                                                    ▼
Client Responses ◄─ [ I/O Thread 1..3 ] ◄── (Write Buffers) ────────┘
```

### Important Distinctions:
- **Network I/O Parsing & Serialization** (which consumes ~50% of CPU time) is delegated to parallel worker threads.
- **Command Execution** remains **100% SINGLE-THREADED** on the main thread, preserving absolute atomicity!
