// =============================================================================
// FILE: 05_FIFOEvictionPolicy.java
// TOPIC: In-Memory Cache LLD — FIFO Eviction Policy
// ALGORITHM: O(1) enqueue/dequeue via LinkedList as Queue
// PATTERNS: Strategy (concrete), Queue data structure
// =============================================================================

package cache;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// =============================================================================
// ■ FIFOEvictionPolicy<K> — First In, First Out
//
// EVICTION RULE: The key that was inserted FIRST is evicted first.
// ACCESS ORDER:  Does NOT change eviction order. Getting a key that was inserted
//                first will still evict it first (pure FIFO, unlike LRU).
//
// Data Structures:
//   insertionQueue: Queue<K> — maintains insertion order
//   keySet:         HashSet<K> — O(1) existence checks + deduplication on remove
//
// Time Complexity:
//   onGet(key):  O(1) — FIFO ignores access, no-op
//   onPut(key):  O(1) — enqueue key (only if new)
//   evict():     O(1) — dequeue head of queue
//   remove(key): O(1) amortized — mark deleted; lazy removal during evict
//
// The "lazy removal" trick:
//   When a key is explicitly removed (cache.remove()), we don't scan the
//   queue for it (O(n)). Instead, we mark it absent in keySet.
//   During evict(), we dequeue and skip keys absent from keySet.
// =============================================================================
class FIFOEvictionPolicy<K> implements EvictionPolicy<K> {

    // ─── State ────────────────────────────────────────────────────────────────

    // Queue of keys in insertion order
    private final Queue<K> insertionQueue = new LinkedList<>();

    // Live keys — O(1) membership test; acts as a "soft delete" marker
    private final Set<K> keySet = new HashSet<>();

    // ─── EvictionPolicy interface ─────────────────────────────────────────────

    /**
     * Cache HIT: FIFO ignores access order — no-op.
     * The key's eviction position is fixed at insertion time.
     */
    @Override
    public void onGet(K key) {
        // Intentional no-op: FIFO does not update order on access
    }

    /**
     * Cache PUT: enqueue the key.
     * If the key already exists (update), we don't re-enqueue it.
     * Its original insertion order is preserved.
     * Time: O(1)
     */
    @Override
    public void onPut(K key) {
        if (!keySet.contains(key)) {
            insertionQueue.offer(key);  // add to tail of queue
            keySet.add(key);
        }
        // If key already present, it's an update — FIFO position unchanged
    }

    /**
     * EVICT: dequeue the oldest key, skipping any that were lazily removed.
     * Time: O(1) amortized (may skip a few deleted entries)
     *
     * @throws IllegalStateException if no evictable keys remain
     */
    @Override
    public K evict() {
        // Drain lazily-deleted entries from the front of the queue
        while (!insertionQueue.isEmpty()) {
            K candidate = insertionQueue.poll();
            if (keySet.contains(candidate)) {
                // This key is still live — it's our eviction victim
                keySet.remove(candidate);
                return candidate;
            }
            // candidate was already removed (lazy deletion) — skip and continue
        }
        throw new IllegalStateException("FIFOEvictionPolicy is empty — nothing to evict");
    }

    /**
     * Soft-delete: mark the key as absent in keySet.
     * The queue still holds the key, but evict() will skip it.
     * Time: O(1)
     */
    @Override
    public void remove(K key) {
        keySet.remove(key);
        // Don't remove from insertionQueue (O(n)) — lazy removal handles it
    }

    @Override
    public void clear() {
        insertionQueue.clear();
        keySet.clear();
    }

    @Override
    public int size() {
        return keySet.size();   // keySet holds only live keys
    }

    // ─── Debug helper ─────────────────────────────────────────────────────────

    /** Returns insertion order (oldest first) for debugging. */
    String insertionOrder() {
        return "FIFO order (oldest→newest): " + insertionQueue +
               " [live=" + keySet + "]";
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why would you ever use FIFO over LRU or LFU?
// A: FIFO is the simplest policy — O(1) everything, minimal overhead.
//    Good for use cases where recency/frequency don't matter:
//    - Time-series data where older data naturally becomes stale
//    - Buffered streaming pipelines (circular buffer semantics)
//    - Testing/debugging where deterministic eviction order is needed
//
// Q: Explain the lazy removal trick.
// A: When cache.remove("key") is called, removing from the Queue would be O(n)
//    because Queue doesn't support random removal. Instead, we just delete from
//    keySet (O(1)) and leave the stale entry in the queue. During evict(), we
//    poll from the queue and check keySet membership — if the dequeued key is
//    not in keySet, it was already removed, so we skip it and poll again.
//    This gives O(1) amortized evict (each entry is enqueued/dequeued once).
//
// Q: Could insertionQueue grow larger than the cache capacity?
// A: Yes — it accumulates lazily-deleted entries. Bounded by: capacity + number
//    of explicit removes. In practice, this is small. If it's a concern,
//    use a CircularBuffer (array-based queue) of fixed size equal to capacity.
//
// Q: What's the difference between FIFO and LRU for a PUT?
// A: FIFO: eviction order is fixed at insertion — re-putting a key doesn't
//    reset its eviction position.
//    LRU: re-putting or re-getting a key moves it to MRU — it "resets" its
//    eviction urgency.
//
// Q: Why use LinkedList instead of ArrayDeque for the queue?
// A: Both give O(1) offer/poll. ArrayDeque is generally faster (array-backed,
//    better cache locality). In an interview, either is acceptable. Production
//    code would prefer ArrayDeque. LinkedList is fine to mention but note
//    the trade-off (pointer overhead per node vs. amortized array resizing).
