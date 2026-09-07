# In-Memory Cache (with Eviction Policies) — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Strategy, Builder, Generic Types, Template Method, Observer  
> **SOLID Coverage:** All 5 principles applied  

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design an In-Memory Cache library that supports:
> - Generic key-value storage
> - Configurable capacity (max entries)
> - Multiple eviction policies: LRU, LFU, FIFO
> - TTL (Time-To-Live) expiry per entry
> - Thread-safe reads and writes
> - Cache statistics (hit rate, miss rate, evictions)"

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| What is the **key and value type**? | Generics vs. Object-based design |
| Is **thread safety** required? | ReadWriteLock vs. no sync |
| Can **TTL** be set per-entry or globally? | Entry-level vs. cache-level expiry |
| Do eviction policies need to be **swappable at runtime**? | Strategy pattern scope |
| Should eviction be **synchronous** (on put) or **async** (background thread)? | Latency vs. accuracy trade-off |
| Do we need **cache events/callbacks** (on evict, on expire)? | Listener / Observer extension |
| Is **persistence** needed (Redis-like)? | Scopes the problem to pure in-memory |
| Should **cache stats** (hit/miss/eviction) be tracked? | CacheStats object design |

---

## 3. Core Entities Identification

```
Cache<K,V>           → public interface clients program to
CacheEntry<K,V>      → value + expiry metadata wrapper
EvictionPolicy<K>    → pluggable strategy: which key to evict next?
InMemoryCache<K,V>   → main implementation (thread-safe)
LRUEvictionPolicy    → Least Recently Used (O(1) via DLL + HashMap)
LFUEvictionPolicy    → Least Frequently Used (O(1) via freq maps)
FIFOEvictionPolicy   → First In First Out (Queue-based)
CacheStats           → hit, miss, eviction counters (metrics)
CacheBuilder<K,V>    → fluent construction of InMemoryCache
```

---

## 4. Eviction Policy Deep Dive (The Hardest Part)

### 4a. LRU — Least Recently Used

```
Data Structures:  HashMap<K, Node<K,V>> + Doubly Linked List
Time Complexity:  O(1) get, O(1) put, O(1) evict
Space Complexity: O(capacity)

Invariant: Most-Recently-Used at HEAD, Least-Recently-Used at TAIL.

GET: Move accessed node to HEAD → O(1)
PUT: Add new node at HEAD → O(1)
     If over capacity: evict TAIL node → O(1)
```

```
HEAD ←→ [D(4)] ←→ [B(2)] ←→ [A(1)] ←→ TAIL (sentinel nodes)
         MRU                    LRU

Capacity = 3. PUT(E):
  Evict TAIL (A), insert E at HEAD.
HEAD ←→ [E(5)] ←→ [D(4)] ←→ [B(2)] ←→ TAIL
```

### 4b. LFU — Least Frequently Used

```
Data Structures:  HashMap<K, CacheEntry>           (key → entry)
                  HashMap<K, Integer>               (key → frequency)
                  HashMap<Int, LinkedHashSet<K>>    (freq → keys at that freq)
                  int minFrequency                  (tracked explicitly)
Time Complexity:  O(1) get, O(1) put, O(1) evict
Space Complexity: O(capacity)

GET: increment freq[key], move key from freqMap[f] to freqMap[f+1]
PUT: insert with freq=1, reset minFrequency=1
EVICT: remove any key from freqMap[minFrequency] (LinkedHashSet = FIFO tiebreak)
```

### 4c. FIFO — First In First Out

```
Data Structures:  Queue<K> + HashMap<K, CacheEntry>
Time Complexity:  O(1) get, O(1) put, O(1) evict
Space Complexity: O(capacity)

EVICT: always dequeue the key that was inserted first
```

### 4d. TTL — Time-To-Live (orthogonal to eviction)

```
Per-entry expiry timestamp stored in CacheEntry.
Lazy expiry: checked on every GET — return null if expired.
Background cleaner: daemon thread sweeps map periodically.
Eager eviction: expired entries count toward capacity.
```

---

## 5. Class Diagram (UML)

```
┌──────────────────────────────────────────────────┐
│              Cache<K,V> <<interface>>             │
│  + get(K key) : Optional<V>                      │
│  + put(K key, V value)                           │
│  + put(K key, V value, long ttlMillis)           │
│  + remove(K key) : boolean                       │
│  + containsKey(K key) : boolean                  │
│  + size() : int                                  │
│  + clear()                                       │
│  + getStats() : CacheStats                       │
└──────────────────────────────────────────────────┘
                         ▲ implements
                         │
┌──────────────────────────────────────────────────┐
│            InMemoryCache<K,V>                    │
│  - capacity : int                                │
│  - store : Map<K, CacheEntry<K,V>>              │
│  - evictionPolicy : EvictionPolicy<K>            │
│  - stats : CacheStats                            │
│  - lock : ReadWriteLock                          │
└──────────────────────────────────────────────────┘
         │ uses                  │ uses
         ▼                       ▼
┌──────────────────┐   ┌─────────────────────────┐
│  CacheEntry<K,V> │   │   EvictionPolicy<K>     │
│  - key           │   │      <<interface>>       │
│  - value         │   │  + onGet(K key)         │
│  - expiryAt      │   │  + onPut(K key)         │
│  - createdAt     │   │  + evict() : K          │
└──────────────────┘   │  + remove(K key)        │
                       └─────────────────────────┘
                             ▲      ▲       ▲
                      LRUPolicy  LFUPolicy  FIFOPolicy

┌──────────────────────────────────┐
│         CacheStats               │
│  - hits    : AtomicLong          │
│  - misses  : AtomicLong          │
│  - evictions : AtomicLong        │
│  - expirations : AtomicLong      │
│  + hitRate() : double            │
└──────────────────────────────────┘
```

---

## 6. Design Patterns Applied

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `EvictionPolicy<K>` | Swap LRU/LFU/FIFO without changing `InMemoryCache` |
| **Builder** | `CacheBuilder<K,V>` | Fluent, readable construction; handles optional TTL/stats |
| **Generic Types** | `Cache<K,V>`, `CacheEntry<K,V>` | Type-safe; no casting; compile-time safety |
| **Template Method** | `AbstractCache` (base behavior) | Common lock/stats logic, subclasses differ on eviction |
| **Decorator** | `TTLEvictionPolicy` wraps another policy | Adds expiry to any base eviction policy |
| **Null Object** | `NoEvictionPolicy` | Cache with no size limit (unbounded) |

---

## 7. SOLID Principles Applied

| Principle | Application |
|---|---|
| **SRP** | `InMemoryCache` stores/retrieves; `EvictionPolicy` decides who to evict; `CacheStats` tracks metrics |
| **OCP** | New eviction policy = new class implementing `EvictionPolicy<K>`. Zero change to `InMemoryCache` |
| **LSP** | `LRUEvictionPolicy`, `LFUEvictionPolicy`, `FIFOEvictionPolicy` are all drop-in for `EvictionPolicy<K>` |
| **ISP** | `EvictionPolicy` has only eviction-related methods; `Cache` has only access methods |
| **DIP** | `InMemoryCache` depends on `EvictionPolicy<K>` interface, not on `LRUEvictionPolicy` concretely |

---

## 8. Thread Safety Strategy

```
ReadWriteLock (ReentrantReadWriteLock):
  - Read lock:  get(), containsKey(), size(), getStats()
  - Write lock: put(), remove(), clear(), evict()

  WHY: Multiple threads can read simultaneously (no data mutation).
       Only writes need exclusive access. Higher throughput than full sync.

CacheStats:
  - AtomicLong for counters → lock-free increment, no contention.

CacheEntry:
  - Immutable after construction (except value for mutable caches).
  - expiryAt is final → thread-safe expiry check.

Background TTL Cleaner:
  - Daemon ScheduledExecutorService → won't block JVM shutdown.
  - Acquires write lock before sweeping expired entries.
```

---

## 9. LRU O(1) Trick — The Core Interview Algorithm

```java
// Why O(1)?
// HashMap gives O(1) node access by key.
// DoublyLinkedList gives O(1) insert/delete (once you have the node reference).
// HashMap stores the node reference → skip O(n) list traversal.

// GET(key):
//   node = map.get(key)        // O(1) lookup
//   moveToHead(node)           // O(1) DLL operation
//   return node.value

// PUT(key, value):
//   if key exists: update node.value, moveToHead → O(1)
//   else:          create new node, addToHead, map.put → O(1)
//   if size > capacity: evict tail node, map.remove → O(1)
```

---

## 10. Interview Trade-off Discussion Script

> **"Why ReadWriteLock instead of synchronized?"**  
> `synchronized` is an exclusive lock — even concurrent reads block each other.  
> ReadWriteLock allows unlimited concurrent reads (no write in progress) while  
> writes are still exclusive. Cache reads vastly outnumber writes → big win.

> **"Why LRU over LFU in practice?"**  
> LRU is simpler, has less overhead, and handles workload shifts (a once-popular  
> key that's no longer needed gets evicted). LFU retains historically frequent keys  
> even if they're no longer accessed — can be stale for bursty workloads.

> **"How would you add TTL expiry?"**  
> Each `CacheEntry` stores an `expiryAt` timestamp. On `get()`, check  
> `Instant.now().isAfter(expiryAt)` → return empty. A background daemon thread  
> periodically acquires the write lock and sweeps expired entries.

> **"How would you scale this to a distributed cache?"**  
> Add consistent hashing to shard keys across nodes. Add a replication layer  
> (primary-replica). Add a network protocol layer (TCP/gRPC). This is Redis/Memcached  
> territory. For this LLD, we scope to single-node in-memory.

> **"What's the eviction policy in Java's LinkedHashMap?"**  
> `LinkedHashMap(capacity, loadFactor, accessOrder=true)` with `removeEldestEntry()`  
> overridden is a quick LRU impl. Good to mention, but interviewer wants you to  
> build it from scratch to demonstrate the DLL + HashMap insight.

---

## 11. File Structure

```
cache/
├── 00_Theory_and_Design.md       ← This file (theory, UML, algorithms, patterns)
├── 01_CacheEntry.java            ← Generic immutable entry with TTL metadata
├── 02_Interfaces.java            ← Cache<K,V>, EvictionPolicy<K> interfaces
├── 03_LRUEvictionPolicy.java     ← O(1) LRU via DoublyLinkedList + HashMap
├── 04_LFUEvictionPolicy.java     ← O(1) LFU via freq-map + LinkedHashSet
├── 05_FIFOEvictionPolicy.java    ← FIFO via Queue + HashSet
├── 06_CacheStats.java            ← AtomicLong counters + hit rate computation
├── 07_InMemoryCache.java         ← Main impl: ReadWriteLock + TTL + CacheBuilder
└── 08_Demo.java                  ← End-to-end: LRU, LFU, FIFO, TTL, concurrency
```
