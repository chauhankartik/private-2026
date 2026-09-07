// =============================================================================
// FILE: 02_Interfaces.java
// TOPIC: In-Memory Cache LLD — Core Interfaces
// PATTERNS: Strategy (EvictionPolicy), ISP, DIP
// =============================================================================

package cache;

import java.util.Optional;

// =============================================================================
// ■ Cache<K, V> — The public contract for all cache implementations
//
// SOLID: ISP — only cache-access methods, nothing implementation-specific.
// SOLID: DIP — client code depends on this interface, not InMemoryCache.
//
// Interview point: Programming to an interface lets you swap implementations
// (InMemoryCache, GuavaCache, RedisCache) without changing client code.
// =============================================================================
interface Cache<K, V> {

    /**
     * Retrieve a value by key.
     *
     * @param key the lookup key (never null)
     * @return Optional containing the value, or Optional.empty() on miss / expiry
     *
     * Interview: Why Optional? Avoids NullPointerException. Forces callers to
     * explicitly handle the miss case. More expressive than returning null.
     */
    Optional<V> get(K key);

    /**
     * Store a key-value pair with no expiry.
     * If the cache is at capacity, eviction runs first.
     *
     * @param key   non-null cache key
     * @param value value to cache (may be null for null-caching)
     */
    void put(K key, V value);

    /**
     * Store a key-value pair with a TTL.
     * Entry will be invisible (logically deleted) after ttlMillis elapses.
     *
     * @param key       non-null cache key
     * @param value     value to cache
     * @param ttlMillis time-to-live in milliseconds; <= 0 = no expiry
     */
    void put(K key, V value, long ttlMillis);

    /**
     * Explicitly remove a key from the cache.
     *
     * @return true if the key was present and removed; false if absent
     */
    boolean remove(K key);

    /**
     * Check if a non-expired entry exists for the given key.
     * Does NOT update access order (unlike get).
     */
    boolean containsKey(K key);

    /** Returns the number of non-expired entries currently in the cache. */
    int size();

    /** Wipe all entries and reset eviction state. */
    void clear();

    /**
     * Return a snapshot of cache performance statistics.
     * @see CacheStats
     */
    CacheStats getStats();
}


// =============================================================================
// ■ EvictionPolicy<K> — Strategy interface for "who gets evicted?"
//
// SOLID: ISP — only eviction-lifecycle methods.
// SOLID: OCP — new policy = new class; InMemoryCache never changes.
// Pattern: Strategy — plugged into InMemoryCache at construction time.
//
// The policy maintains its own internal ordering data structure.
// InMemoryCache calls the three lifecycle hooks:
//   onGet()  → key was accessed (e.g., LRU moves it to head)
//   onPut()  → new key inserted (e.g., FIFO enqueues it)
//   evict()  → cache is full; policy nominates the victim key
//   remove() → key explicitly removed; policy cleans its internal state
// =============================================================================
interface EvictionPolicy<K> {

    /**
     * Called AFTER a successful cache GET (hit).
     * Allows policies to update access metadata (e.g., LRU moves key to MRU).
     *
     * @param key the key that was accessed
     */
    void onGet(K key);

    /**
     * Called AFTER a PUT (new key inserted or existing key updated).
     * Policy should track the key for future eviction decisions.
     *
     * @param key the key that was inserted/updated
     */
    void onPut(K key);

    /**
     * Select and return the key that should be evicted.
     * Called by InMemoryCache when size > capacity BEFORE inserting a new entry.
     *
     * Contract: must return a key currently tracked by this policy.
     *           InMemoryCache will then remove that key from the store.
     *
     * @return the key to evict; never null
     */
    K evict();

    /**
     * Remove a key from the policy's tracking structures.
     * Called when a key is explicitly removed (Cache.remove()) or evicted.
     *
     * @param key the key to stop tracking
     */
    void remove(K key);

    /**
     * Reset all internal state. Called when Cache.clear() is invoked.
     */
    void clear();

    /** Returns the number of keys currently tracked by this policy. */
    int size();
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why does EvictionPolicy not hold the values, only the keys?
// A: Separation of concerns. The cache store (HashMap) owns key→value mappings.
//    The eviction policy only cares about ORDERING of keys for eviction decisions.
//    Having the policy hold values would violate SRP and create duplication.
//
// Q: What's the lifecycle order for a PUT that triggers eviction?
// A: 1. InMemoryCache acquires write lock
//    2. Check if key exists → if so, update (no eviction needed)
//    3. If new key and size >= capacity:
//         K victim = policy.evict()    → get victim
//         store.remove(victim)         → remove from map
//         policy.remove(victim)        → clean policy state
//    4. store.put(key, entry)          → insert new entry
//    5. policy.onPut(key)              → register with policy
//    6. Release write lock
//
// Q: Why have separate onGet() and onPut() instead of one "onAccess()"?
// A: LFU must differentiate: a GET increments frequency of an existing key,
//    while PUT initializes frequency to 1. Merging them would force LFU to
//    check if a key is new on every call — adds complexity to the policy.
//    Separate hooks keep each policy's logic cleaner.
//
// Q: Could you use java.util.function.BiFunction for the eviction strategy?
// A: For a single eviction decision, yes. But policies need internal state
//    (DLL for LRU, freq maps for LFU, queue for FIFO) maintained across calls.
//    A functional interface can't hold mutable state cleanly — a stateful
//    class implementing EvictionPolicy<K> is the right abstraction.
