// =============================================================================
// FILE: 06_CacheStats.java
// TOPIC: In-Memory Cache LLD — Statistics / Metrics
// PATTERNS: Value Object, Thread-safe counters via AtomicLong
// SOLID: SRP — only tracks metrics, nothing else
// =============================================================================

package cache;

import java.util.concurrent.atomic.AtomicLong;

// =============================================================================
// ■ CacheStats — Lock-free metrics collector
//
// Tracks four fundamental cache performance counters:
//   hits:        number of successful get() calls (key present and not expired)
//   misses:      number of failed get() calls (key absent or expired)
//   evictions:   number of entries removed by the eviction policy
//   expirations: number of entries removed due to TTL expiry
//
// Thread-safety:
//   AtomicLong uses CAS (Compare-And-Swap) hardware instructions.
//   No locking needed — counters can be incremented from any thread
//   without holding InMemoryCache's ReadWriteLock.
//
// Design choice:
//   Separate from InMemoryCache (SRP). Could later be wired to a metrics
//   framework (Micrometer, Prometheus) without touching cache logic.
// =============================================================================
class CacheStats {

    private final AtomicLong hits        = new AtomicLong(0);
    private final AtomicLong misses      = new AtomicLong(0);
    private final AtomicLong evictions   = new AtomicLong(0);
    private final AtomicLong expirations = new AtomicLong(0);

    // ─── Increment methods (called by InMemoryCache) ─────────────────────────

    void recordHit()        { hits.incrementAndGet(); }
    void recordMiss()       { misses.incrementAndGet(); }
    void recordEviction()   { evictions.incrementAndGet(); }
    void recordExpiration() { expirations.incrementAndGet(); misses.incrementAndGet(); }

    // ─── Read methods ─────────────────────────────────────────────────────────

    public long getHits()        { return hits.get(); }
    public long getMisses()      { return misses.get(); }
    public long getEvictions()   { return evictions.get(); }
    public long getExpirations() { return expirations.get(); }
    public long getTotalRequests() { return hits.get() + misses.get(); }

    /**
     * Cache hit rate: ratio of hits to total requests.
     * Returns 0.0 if no requests have been made yet (avoids divide-by-zero).
     */
    public double hitRate() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) hits.get() / total;
    }

    /**
     * Cache miss rate: complement of hit rate.
     */
    public double missRate() {
        return 1.0 - hitRate();
    }

    /** Reset all counters (useful in tests or after configuration change). */
    void reset() {
        hits.set(0);
        misses.set(0);
        evictions.set(0);
        expirations.set(0);
    }

    @Override
    public String toString() {
        return String.format(
            "CacheStats{hits=%d, misses=%d, evictions=%d, expirations=%d, hitRate=%.1f%%}",
            hits.get(), misses.get(), evictions.get(), expirations.get(), hitRate() * 100);
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why AtomicLong instead of just 'long' with synchronization?
// A: AtomicLong.incrementAndGet() uses a single CAS instruction on the CPU.
//    No thread blocking, no lock acquisition overhead. For pure counters,
//    this is the fastest correct approach in Java.
//    synchronized(this) { hits++ } would serialize all stat updates — a
//    bottleneck under high concurrency for something as simple as a counter.
//
// Q: Why is hitRate() a computed property rather than a stored counter?
// A: Storing a running hitRate would require either a floating-point AtomicXxx
//    (not available in Java) or a lock. Computing it on demand from two
//    AtomicLong values is simpler, always consistent, and has no overhead for
//    cache operations. Stats are read infrequently (monitoring, logging).
//
// Q: How would you expose these stats to Prometheus/Grafana?
// A: Register a Micrometer Gauge for each counter:
//    Metrics.gauge("cache.hits", stats, CacheStats::getHits)
//    Metrics.gauge("cache.hit_rate", stats, s -> s.hitRate() * 100)
//    Zero changes to InMemoryCache or CacheStats — just wire them up in config.
//
// Q: Should recordExpiration() also increment misses?
// A: Yes — an expired entry is a logical miss from the caller's perspective.
//    They asked for a value and got nothing. We track expirations separately
//    to distinguish "never cached" misses from "was cached but expired" misses.
//    This helps diagnose whether TTL is set too aggressively.
