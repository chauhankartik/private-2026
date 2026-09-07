# Chapter 4: Persistence Mechanisms (RDB, AOF & Hybrid) — Deep Dive Notes

> **Core Theme:** Redis is an in-memory database that provides durability via **RDB (Point-in-Time Snapshots)**, **AOF (Append-Only File Logs)**, and **Hybrid Persistence**.

---

## 1. RDB (Redis Database Snapshot)

RDB creates a compact, single-file binary representation of the entire dataset at a point in time (`dump.rdb`).

```
Trigger BGSAVE ──► fork() ──► Child Process ──► Writes dump.rdb
                                │
                        Copy-on-Write (COW)
                                │
Parent Process (Main Thread) ───┴──► Accepts new client writes in parallel!
```

### How `BGSAVE` Works via Copy-on-Write (COW):
1. Main process calls Linux `fork()` to create a child process.
2. `fork()` duplicates page tables (not physical RAM). Both processes share the same physical memory pages.
3. If the parent process writes to a page, OS kernel duplicates that specific 4KB page (**Copy-on-Write**).
4. Child process writes its immutable point-in-time snapshot to disk safely without lock contention!

---

## 2. AOF (Append-Only File)

AOF logs every write operation (`SET`, `HSET`, `DEL`) sequentially in RESP (Redis Serialization Protocol) format.

### Three `appendfsync` Options:
- **`appendfsync always`:** Calls `fsync()` after every single write.
  - *Pro:* Maximum safety (zero data loss).
  - *Con:* Extremely slow (bound by disk write latency).
- **`appendfsync everysec` (Default & Recommended):** Calls `fsync()` once per second on a background thread.
  - *Pro:* High performance with at most 1–2 seconds of data loss on power outage.
- **`appendfsync no`:** Delegates `fsync()` timing to the OS (typically every 30 seconds).

---

## 3. AOF Rewrite (`bgrewriteaof`)

As writes accumulate, the AOF file grows massive. AOF Rewrite rebuilds the AOF file from current memory state rather than reading the old log file!

```
Old AOF Log (100 MB):            AOF Rewrite Result (1 KB):
SET counter 1                     SET counter 100
INCR counter
INCR counter
... 100 times
```

---

## 4. Hybrid Persistence (Redis 4.0+)

Configured via `aof-use-rdb-preamble yes`.

```
Hybrid AOF File Layout:
┌──────────────────────────────────────┬──────────────────────────────────┐
│ RDB Preamble (Binary Fast Snapshot)  │ AOF Tail (Incremental RESP Logs) │
└──────────────────────────────────────┴──────────────────────────────────┘
```

- **Benefits:** Fast server restart time (RDB preamble) combined with minimal data loss (AOF tail).
