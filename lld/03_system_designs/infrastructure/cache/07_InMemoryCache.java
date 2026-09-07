// =============================================================================
// FILE: 07_InMemoryCache.java
// TOPIC: In-Memory Cache LLD — Main Implementation
// PATTERNS: Builder, Strategy (uses EvictionPolicy), Template Method
// THREAD-SAFETY: ReentrantReadWriteLock + AtomicLong stats
// =============================================================================

package cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// =============================================================================
// ■ InMemoryCache<K, V> — Thread-safe, eviction-policy-agnostic cache
//
// Core pipeline for GET:
//   [1] Acquire read lock
//   [2] Lookup key in store
//   [3] If absent → miss
//   [4] If expired → logically delete (upgrade to write lock), miss
//   [5] Hit: notify policy (onGet), record hit, return value
//
// Core pipeline for PUT:
//   [1] Acquire write lock
//   [2] If at capacity and key is new → evict victim (policy.evict())
//   [3] Create CacheEntry (with optional TTL)
//   [4] Insert into store
//   [5] Notify policy (onPut)
//
// Thread-safety model:
//   ReadWriteLock allows multiple concurrent readers.
//   Only writes (put/remove/evict) need exclusive access.
//   Stats use AtomicLong — lock-free, no contention with read/write lock.
// =============================================================================
final class InMemoryCache<K, V> implements Cache<K, V> {

    // ─── Core state ───────────────────────────────────────────────────────────

    private final int                      capacity;
    private final Map<K, CacheEntry<K, V>> store;          // the cache store
    private final EvictionPolicy<K>        evictionPolicy;
    private final CacheStats               stats;

    // ─── Concurrency ──────────────────────────────────────────────────────────

    private final ReadWriteLock lock       = new ReentrantReadWriteLock();
    private final java.util.concurrent.locks.Lock readLock  = lock.readLock();
    private final java.util.concurrent.locks.Lock writeLock = lock.writeLock();

    // ─── Background TTL cleaner (optional) ───────────────────────────────────

    private final ScheduledExecutorService ttlCleaner;  // null if TTL not needed

    // Private constructor — only CacheBuilder can create instances
    private InMemoryCache(Builder<K, V> builder) {
        this.capacity       = builder.capacity;
        this.store          = new HashMap<>(builder.capacity * 2);   // initial headroom
        this.evictionPolicy = builder.evictionPolicy;
        this.stats          = new CacheStats();

        if (builder.ttlCleanupIntervalMs > 0) {
            this.ttlCleaner = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cache-ttl-cleaner");
                t.setDaemon(true);   // won't block JVM shutdown
                return t;
            });
            this.ttlCleaner.scheduleAtFixedRate(
                    this::sweepExpiredEntries,
                    builder.ttlCleanupIntervalMs,
                    builder.ttlCleanupIntervalMs,
                    TimeUnit.MILLISECONDS);
        } else {
            this.ttlCleaner = null;
        }
    }

    // =========================================================================
    // ■ Cache<K, V> Interface Implementation
    // =========================================================================

    /**
     * GET pipeline: lock → lookup → expiry check → hit/miss → update policy.
     *
     * TTL expiry uses LAZY checking: we check on every GET.
     * Expired entries are removed eagerly (upgrade to write lock) on first detection.
     * Background sweeper handles entries that are never accessed.
     */
    @Override
    public Optional<V> get(K key) {
        if (key == null) throw new IllegalArgumentException("Cache key must not be null");

        // Fast path: try read lock first
        readLock.lock();
        try {
            CacheEntry<K, V> entry = store.get(key);

            if (entry == null) {
                stats.recordMiss();
                return Optional.empty();
            }

            if (entry.isExpired()) {
                // Entry exists but expired — need write lock to remove it
                // Must release read lock before acquiring write lock (avoid deadlock)
                readLock.unlock();
                writeLock.lock();
                try {
                    // Re-check under write lock (another thread may have removed it)
                    CacheEntry<K, V> recheck = store.get(key);
                    if (recheck != null && recheck.isExpired()) {
                        store.remove(key);
                        evictionPolicy.remove(key);
                        stats.recordExpiration();
                    } else if (recheck != null) {
                        // Another thread refreshed this key — return the new value
                        evictionPolicy.onGet(key);
                        stats.recordHit();
                        return Optional.ofNullable(recheck.getValue());
                    }
                    stats.recordMiss();
                    return Optional.empty();
                } finally {
                    // Restore read lock semantics: release write lock only
                    writeLock.unlock();
                    // We already released the read lock above — re-acquire for finally
                    readLock.lock();
                }
            }

            // Non-expired hit
            evictionPolicy.onGet(key);
            stats.recordHit();
            return Optional.ofNullable(entry.getValue());

        } finally {
            readLock.unlock();
        }
    }

    /**
     * PUT without TTL — entry never expires.
     */
    @Override
    public void put(K key, V value) {
        putInternal(key, value, 0);
    }

    /**
     * PUT with TTL — entry expires after ttlMillis milliseconds.
     *
     * @param ttlMillis <= 0 means no expiry (same as put(key, value))
     */
    @Override
    public void put(K key, V value, long ttlMillis) {
        putInternal(key, value, ttlMillis);
    }

    /**
     * PUT pipeline: lock → evict if needed → create entry → insert → notify policy.
     */
    private void putInternal(K key, V value, long ttlMillis) {
        if (key == null) throw new IllegalArgumentException("Cache key must not be null");

        writeLock.lock();
        try {
            boolean isNewKey = !store.containsKey(key);

            // Evict if at capacity and this is a new key (not an update)
            if (isNewKey && store.size() >= capacity) {
                K victim = evictionPolicy.evict();
                store.remove(victim);
                stats.recordEviction();
            }

            // Create entry (with or without TTL)
            CacheEntry<K, V> entry = CacheEntry.withTTL(key, value, ttlMillis);
            store.put(key, entry);
            evictionPolicy.onPut(key);

        } finally {
            writeLock.unlock();
        }
    }

    /**
     * REMOVE: explicitly evict a specific key.
     * @return true if the key was present; false if absent
     */
    @Override
    public boolean remove(K key) {
        if (key == null) return false;

        writeLock.lock();
        try {
            if (store.remove(key) != null) {
                evictionPolicy.remove(key);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Check existence without updating eviction order.
     * Also returns false for expired entries.
     */
    @Override
    public boolean containsKey(K key) {
        if (key == null) return false;
        readLock.lock();
        try {
            CacheEntry<K, V> entry = store.get(key);
            return entry != null && !entry.isExpired();
        } finally {
            readLock.unlock();
        }
    }

    /** Returns live (non-expired) entry count. */
    @Override
    public int size() {
        readLock.lock();
        try {
            // Count non-expired entries (may differ from store.size() during lazy expiry)
            int count = 0;
            for (CacheEntry<K, V> e : store.values()) {
                if (!e.isExpired()) count++;
            }
            return count;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            store.clear();
            evictionPolicy.clear();
            stats.reset();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public CacheStats getStats() {
        return stats;
    }

    // ─── TTL background sweeper ───────────────────────────────────────────────

    /**
     * Proactively sweep and remove expired entries.
     * Called by the background ScheduledExecutorService.
     * Must acquire write lock to safely remove from store.
     */
    private void sweepExpiredEntries() {
        writeLock.lock();
        try {
            List<K> expired = new ArrayList<>();
            for (Map.Entry<K, CacheEntry<K, V>> entry : store.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expired.add(entry.getKey());
                }
            }
            for (K key : expired) {
                store.remove(key);
                evictionPolicy.remove(key);
                stats.recordExpiration();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /** Shutdown the background TTL cleaner (call on application shutdown). */
    public void shutdown() {
        if (ttlCleaner != null) {
            ttlCleaner.shutdown();
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return "InMemoryCache{capacity=" + capacity +
                   ", size=" + store.size() +
                   ", policy=" + evictionPolicy.getClass().getSimpleName() +
                   ", " + stats + "}";
        } finally {
            readLock.unlock();
        }
    }


    // =========================================================================
    // ■ CacheBuilder<K, V> — Fluent cache construction
    //
    // Usage:
    //   Cache<String, User> cache = new InMemoryCache.Builder<String, User>()
    //       .capacity(100)
    //       .evictionPolicy(new LRUEvictionPolicy<>())
    //       .ttlCleanupIntervalMs(5000)   // sweep every 5 seconds
    //       .build();
    // =========================================================================
    static final class Builder<K, V> {

        private int               capacity               = 16;          // sensible default
        private EvictionPolicy<K> evictionPolicy         = new LRUEvictionPolicy<>();  // LRU default
        private long              ttlCleanupIntervalMs   = 0;           // 0 = no background cleaner

        Builder<K, V> capacity(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
            this.capacity = capacity;
            return this;
        }

        Builder<K, V> evictionPolicy(EvictionPolicy<K> policy) {
            if (policy == null) throw new IllegalArgumentException("policy must not be null");
            this.evictionPolicy = policy;
            return this;
        }

        /**
         * Enable background TTL sweeping at a fixed interval.
         * Set to 0 (default) to rely on lazy expiry only.
         */
        Builder<K, V> ttlCleanupIntervalMs(long intervalMs) {
            this.ttlCleanupIntervalMs = intervalMs;
            return this;
        }

        InMemoryCache<K, V> build() {
            return new InMemoryCache<>(this);
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why ReadWriteLock over synchronized?
// A: Caches are read-heavy (many more gets than puts). ReadWriteLock allows
//    concurrent reads — N threads can call get() simultaneously with no blocking.
//    Only puts/removes require exclusive access. synchronized(this) would
//    serialize ALL access, including concurrent gets. ReadWriteLock gives
//    significantly higher throughput for read-dominated workloads.
//
// Q: Explain the lock upgrade problem in get() for expired entries.
// A: Java's ReentrantReadWriteLock does NOT support lock upgrading (read → write)
//    atomically. If you hold a read lock and try to acquire the write lock, you
//    DEADLOCK (the write lock waits for all read locks to release, including yours).
//    Solution: release the read lock first, then acquire the write lock, then
//    re-check the condition (double-check idiom — another thread may have acted
//    between releasing read and acquiring write).
//
// Q: What's the difference between lazy TTL expiry and proactive TTL sweeping?
// A: Lazy: expired entries are only removed when accessed. Zero overhead between
//    accesses. Drawback: expired entries count toward capacity until accessed.
//    Proactive (background sweeper): removed even if never accessed. Prevents
//    capacity waste. Drawback: background thread overhead and periodic write-lock
//    contention. Best practice: use both — lazy for correctness, sweeper for memory.
//
// Q: Why use HashMap instead of ConcurrentHashMap for the store?
// A: We already have a ReadWriteLock guarding all access to 'store'. Using
//    ConcurrentHashMap would add a second layer of locking (redundant overhead).
//    One locking mechanism is simpler and avoids the "double-lock" problem.
//    ConcurrentHashMap would be correct if we removed the ReadWriteLock entirely,
//    but then we'd lose the "multiple concurrent readers" optimization for complex
//    read operations like size() that iterate the entire map.
//
// Q: What if capacity is reached and eviction policy has a bug (returns wrong key)?
// A: store.remove(victim) would remove the wrong entry. No crash, but data loss.
//    In production, add an assertion: assert store.containsKey(victim).
//    The eviction policy contract (evict() must return a tracked key) is enforced
//    by convention — tests are the safety net.
