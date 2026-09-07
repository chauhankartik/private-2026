// =============================================================================
// FILE: 01_CacheEntry.java
// TOPIC: In-Memory Cache LLD — Generic Cache Entry
// PATTERNS: Generic Types, Immutable Object
// =============================================================================

package cache;

import java.time.Instant;

// =============================================================================
// ■ CacheEntry<K, V> — Immutable metadata wrapper for a cached value
//
// Stores the value plus lifecycle metadata:
//   - createdAt:  when the entry was inserted (for FIFO, metrics)
//   - expiryAt:   null = never expires; otherwise Instant-based TTL
//
// Why immutable?
//   Multiple threads read entries concurrently after a write-lock-protected put().
//   Immutable entries are inherently thread-safe — no defensive copying needed.
//
// Why generic?
//   Avoids raw Object casts, gives compile-time type safety, and allows
//   InMemoryCache<String, User> vs InMemoryCache<Integer, Product> etc.
// =============================================================================
final class CacheEntry<K, V> {

    private final K       key;
    private final V       value;
    private final Instant createdAt;
    private final Instant expiryAt;    // null = immortal (no TTL)

    // ─── Constructors ─────────────────────────────────────────────────────────

    /** Entry with no TTL — never expires. */
    CacheEntry(K key, V value) {
        this(key, value, null);
    }

    /**
     * Entry with explicit TTL.
     *
     * @param key       cache key (never null)
     * @param value     cached value (may be null — representing "null cached")
     * @param expiryAt  expiry instant, or null for no expiry
     */
    CacheEntry(K key, V value, Instant expiryAt) {
        if (key == null) throw new IllegalArgumentException("Cache key must not be null");
        this.key       = key;
        this.value     = value;
        this.createdAt = Instant.now();
        this.expiryAt  = expiryAt;
    }

    // ─── Factory method for TTL entries ──────────────────────────────────────

    /**
     * Create a TTL-based entry.
     *
     * @param key         cache key
     * @param value       cached value
     * @param ttlMillis   time-to-live in milliseconds; <= 0 = no expiry
     */
    static <K, V> CacheEntry<K, V> withTTL(K key, V value, long ttlMillis) {
        if (ttlMillis <= 0) return new CacheEntry<>(key, value);  // no TTL
        return new CacheEntry<>(key, value, Instant.now().plusMillis(ttlMillis));
    }

    // ─── Expiry check ─────────────────────────────────────────────────────────

    /**
     * Returns true if this entry has expired and should not be returned to callers.
     *
     * Thread-safety: expiryAt is final; Instant.now() read is safe anywhere.
     */
    boolean isExpired() {
        return expiryAt != null && Instant.now().isAfter(expiryAt);
    }

    boolean hasExpiry() {
        return expiryAt != null;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    K       getKey()       { return key; }
    V       getValue()     { return value; }
    Instant getCreatedAt() { return createdAt; }
    Instant getExpiryAt()  { return expiryAt; }   // may be null

    // ─── Remaining TTL (useful for monitoring / debugging) ───────────────────

    /**
     * Milliseconds until expiry. Returns Long.MAX_VALUE if no expiry set.
     * Returns 0 (or negative) if already expired.
     */
    long remainingTtlMillis() {
        if (expiryAt == null) return Long.MAX_VALUE;
        return expiryAt.toEpochMilli() - Instant.now().toEpochMilli();
    }

    @Override
    public String toString() {
        return "CacheEntry{key=" + key +
               ", value=" + value +
               ", expired=" + isExpired() +
               (expiryAt != null ? ", ttlRemaining=" + remainingTtlMillis() + "ms" : "") +
               "}";
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why store 'key' inside CacheEntry if the map already has it?
// A: Eviction policies need to return "which key to evict?" The policy's
//    internal data structures (DLL nodes, queues) hold CacheEntry references.
//    Having the key inside CacheEntry lets the policy hand back the key without
//    needing a reverse-lookup map.
//
// Q: Why is null a valid value?
// A: "Null caching" is a real pattern: cache a null result (e.g., user not found)
//    to avoid repeatedly hitting the DB for a known-absent key. The entry being
//    present in the map (even with null value) means "we checked, result is null".
//    Distinguish: Optional.empty() = cache miss (key absent); present with null
//    value = cached null result.
//
// Q: Why Instant over System.currentTimeMillis()?
// A: Instant is part of Java's modern time API (java.time), is monotonic within
//    a JVM run, is timezone-agnostic, and has nanosecond precision. More readable
//    and less error-prone than raw millis arithmetic.
//
// Q: Could you use a record (Java 16+) here instead?
// A: Records are concise but don't support custom validation in the constructor
//    without overriding the compact constructor. Also, we need the withTTL()
//    factory method. A record would work for the basic case but is less flexible.
