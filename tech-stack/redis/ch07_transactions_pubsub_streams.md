# Chapter 7: Transactions, Pub/Sub, Streams & Lua Scripting — Deep Dive Notes

> **Core Theme:** Advanced Redis features: **Optimistic Transactions (`WATCH`)**, **Pub/Sub vs. Streams**, Consumer Groups, and **Atomic Server-Side Lua Scripting**.

---

## 1. Redis Transactions (`MULTI`, `EXEC`, `WATCH`)

Redis provides command queuing and isolation via `MULTI`, `EXEC`, `DISCARD`, and `WATCH`.

```redis
WATCH account:100
MULTI
DECRBY account:100 50
INCRBY account:200 50
EXEC
```

### Optimistic Locking via `WATCH` (CAS - Compare And Swap):
- `WATCH` monitors keys for changes prior to `EXEC`.
- If another client modifies `account:100` before `EXEC` is called, the transaction is **aborted completely** and `EXEC` returns `nil`.

### No Rollback on Execution Errors:
Redis transactions do **NOT** support rollbacks for runtime errors (e.g. executing `INCR` on a String containing text). Commands prior to the syntax/runtime error remain committed!

---

## 2. Messaging: Pub/Sub vs. Redis Streams

### 1. Traditional Pub/Sub (`PUBLISH`, `SUBSCRIBE`)
- **Fire-and-Forget:** Messages are delivered to online subscriber sockets immediately and discarded.
- **Zero Persistence:** If a consumer is offline for 1 second, it loses all messages published during that outage.

### 2. Redis Streams (`XADD`, `XREADGROUP`)
Introduced in Redis 5.0 as an append-only log modeled after Apache Kafka.

```
Stream Key: 'orders'
┌──────────────────┬──────────────────┬──────────────────┐
│ 1526372000000-0  │ 1526372000000-1  │ 1526372005000-0  │ ...
└──────────────────┴──────────────────┴──────────────────┘
         ▲
         └─ Consumer Group A (PEL: Pending Entries List)
```

- **Persistence & Replay:** Events are persisted on disk and can be read by offset or consumer groups.
- **At-Least-Once Delivery:** Uses `XACK` to confirm message processing. Unacknowledged messages remain in the **Pending Entries List (PEL)** for retry.

---

## 3. Server-Side Lua Scripting & Functions

Redis executes Lua scripts (`EVAL`) **atomically** on the main thread.

```lua
-- Atomic Rate Limiter Script in Lua
local current = redis.call('INCR', KEYS[1])
if tonumber(current) == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end
return current
```

### Advantages:
1. **Atomicity:** Entire script runs without interruption from other commands.
2. **Network Efficiency:** Replaces multiple sequential network round-trips (RTTs) with a single atomic execution.
