# Chapter 3: Memory Management & Eviction Policies — Deep Dive Notes

> **Core Theme:** Redis stores all data in RAM. How Redis manages key expiration (Passive vs Active) and enforces **Approximated LRU / LFU Eviction Policies** when `maxmemory` limit is reached.

---

## 1. Key Expiration Mechanics

When setting a TTL (`EXPIRE key 60`), Redis does NOT set an individual timer for each key (which would consume excessive CPU). Instead, it uses a hybrid approach:

### 1. Passive Expiration (Lazy Evaluation):
- When a client reads a key (`GET key`), Redis checks if the key's TTL has expired.
- If expired, Redis deletes the key inline and returns `nil`.

### 2. Active Expiration (Probabilistic Sampling):
- Executed inside `serverCron()` 10 times per second (10Hz).
- **Algorithm:**
  1. Randomly sample 20 keys from the expiration dictionary.
  2. Delete all expired keys found.
  3. If **more than 25%** (5 keys) were expired, repeat step 1 immediately!
  4. Bounded to a maximum CPU run time of 25ms per iteration.

---

## 2. Eviction Policies (`maxmemory`)

When RAM usage reaches `maxmemory`, Redis evicts keys according to `maxmemory-policy`.

| Policy | Behavior |
| :--- | :--- |
| **`noeviction`** | Default. Returns `OOM command not allowed` error on writes; reads succeed. |
| **`allkeys-lru`** | Evicts Least Recently Used (LRU) keys across **all** keys. |
| **`volatile-lru`** | Evicts LRU keys only among keys with an **expire (TTL)** set. |
| **`allkeys-lfu`** | Evicts Least Frequently Used (LFU) keys across **all** keys. |
| **`volatile-lfu`** | Evicts LFU keys only among keys with an **expire (TTL)** set. |
| **`allkeys-random`** | Evicts random keys across all keys. |
| **`volatile-random`**| Evicts random keys among keys with an expire set. |
| **`volatile-ttl`** | Evicts keys with the shortest remaining TTL. |

---

## 3. Approximated LRU Algorithm in Redis

Exact LRU requires maintaining a doubly-linked list of all keys in RAM, updating pointers on every single read!

### Redis Approximated LRU (5-Sample Pool):
- Redis attaches a 24-bit timestamp field to every `redisObject`.
- When eviction is triggered:
  1. Redis samples $K$ random keys (Default $K = 5$).
  2. Maintains an **Eviction Pool** of candidate keys sorted by idle time.
  3. Evicts the key with the longest idle time from the pool.
- **Result:** $K = 5$ achieves performance virtually indistinguishable from exact LRU with **zero memory overhead**!

---

## 4. Approximated LFU (Least Frequently Used)

Redis LFU uses an 8-bit **Morris Counter** (`redisObject.lru`) split into:
1. **Logarithmic Counter (8 bits):** Increment probability decreases as count increases ($0 \dots 255$).
2. **Decay Time (16 bits):** Counter decays over time based on `lfu-decay-time`.
