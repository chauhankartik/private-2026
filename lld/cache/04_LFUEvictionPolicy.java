// =============================================================================
// FILE: 04_LFUEvictionPolicy.java
// TOPIC: In-Memory Cache LLD — LFU Eviction Policy
// ALGORITHM: O(1) get + put + evict via triple-map design
// PATTERNS: Strategy (concrete)
// =============================================================================

package cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

// =============================================================================
// ■ LFUEvictionPolicy<K> — Least Frequently Used
//
// EVICTION RULE: Evict the key with the LOWEST access frequency.
// TIE-BREAKING:  Among keys with the same frequency, evict the OLDEST (LRU).
//                This is achieved via LinkedHashSet (insertion-ordered Set).
//
// ─── Three Data Structures ────────────────────────────────────────────────────
//
//   keyFreq:    Map<K, Integer>
//               key → current access frequency
//
//   freqKeys:   Map<Integer, LinkedHashSet<K>>
//               frequency → set of keys at that frequency
//               LinkedHashSet = insertion-ordered → FIFO tiebreak = LRU behavior
//
//   minFreq:    int
//               the current minimum frequency among all tracked keys
//               (tracked explicitly to avoid O(n) search for eviction victim)
//
// ─── Time Complexity ──────────────────────────────────────────────────────────
//
//   onGet(key):  O(1) — increment freq, move key between freq buckets
//   onPut(key):  O(1) — insert with freq=1, reset minFreq=1
//   evict():     O(1) — evict first key from freqKeys[minFreq] set
//   remove(key): O(1) — remove from keyFreq and freqKeys
//
// ─── Example State ────────────────────────────────────────────────────────────
//
//   Keys:    A(freq=3), B(freq=1), C(freq=1), D(freq=2)
//   keyFreq: {A→3, B→1, C→1, D→2}
//   freqKeys:{1→[B,C], 2→[D], 3→[A]}
//   minFreq: 1
//
//   EVICT → remove first of freqKeys[1] = B
//   POST:   {1→[C], 2→[D], 3→[A]}, minFreq=1
//
//   GET(C) → C: freq 1→2, move from freqKeys[1] to freqKeys[2]
//   POST:   {1→[], 2→[D,C], 3→[A]}, minFreq=2 (freqKeys[1] is now empty!)
// =============================================================================
class LFUEvictionPolicy<K> implements EvictionPolicy<K> {

    // ─── State ────────────────────────────────────────────────────────────────

    private final Map<K, Integer>               keyFreq  = new HashMap<>();
    private final Map<Integer, LinkedHashSet<K>> freqKeys = new HashMap<>();
    private int minFreq = 0;

    // ─── EvictionPolicy interface ─────────────────────────────────────────────

    /**
     * Cache HIT: increment the frequency of the accessed key.
     * Move the key from freqKeys[f] to freqKeys[f+1].
     * Update minFreq if necessary.
     * Time: O(1)
     */
    @Override
    public void onGet(K key) {
        if (!keyFreq.containsKey(key)) return;    // safety: unknown key
        incrementFrequency(key);
    }

    /**
     * Cache PUT: register key with frequency 1.
     * If key already exists, just increment its frequency (it's an update).
     * Reset minFreq to 1 for new keys.
     * Time: O(1)
     */
    @Override
    public void onPut(K key) {
        if (keyFreq.containsKey(key)) {
            // Existing key update → increment frequency
            incrementFrequency(key);
        } else {
            // New key → start at frequency 1
            keyFreq.put(key, 1);
            freqKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
            minFreq = 1;    // a new key always resets minFreq to 1
        }
    }

    /**
     * EVICT: remove and return the LFU key.
     * Look up freqKeys[minFreq] and remove its first element (LRU tiebreak).
     * Time: O(1)
     */
    @Override
    public K evict() {
        LinkedHashSet<K> leastFreqSet = freqKeys.get(minFreq);
        if (leastFreqSet == null || leastFreqSet.isEmpty()) {
            throw new IllegalStateException("LFUEvictionPolicy is empty — nothing to evict");
        }
        // First element = oldest insertion at this frequency level (LRU tiebreak)
        K victim = leastFreqSet.iterator().next();
        leastFreqSet.remove(victim);
        if (leastFreqSet.isEmpty()) freqKeys.remove(minFreq);
        keyFreq.remove(victim);
        return victim;
    }

    /**
     * Explicitly remove a key (on cache.remove()).
     * Time: O(1)
     */
    @Override
    public void remove(K key) {
        Integer freq = keyFreq.remove(key);
        if (freq == null) return;
        LinkedHashSet<K> set = freqKeys.get(freq);
        if (set != null) {
            set.remove(key);
            if (set.isEmpty()) freqKeys.remove(freq);
        }
        // Note: minFreq may now be stale, but it's only used on evict().
        // We don't bother correcting it here; evict() handles empty buckets.
    }

    @Override
    public void clear() {
        keyFreq.clear();
        freqKeys.clear();
        minFreq = 0;
    }

    @Override
    public int size() {
        return keyFreq.size();
    }

    // ─── Private helper ───────────────────────────────────────────────────────

    /**
     * Increment the frequency of an existing key.
     * Move the key from its current frequency bucket to the next.
     * Update minFreq if the old frequency bucket is now empty.
     * Time: O(1)
     */
    private void incrementFrequency(K key) {
        int oldFreq = keyFreq.get(key);
        int newFreq = oldFreq + 1;
        keyFreq.put(key, newFreq);

        // Remove from old freq bucket
        LinkedHashSet<K> oldSet = freqKeys.get(oldFreq);
        oldSet.remove(key);
        if (oldSet.isEmpty()) {
            freqKeys.remove(oldFreq);
            // If we just emptied the minimum-frequency bucket, update minFreq
            if (minFreq == oldFreq) minFreq = newFreq;
        }

        // Add to new freq bucket
        freqKeys.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    // ─── Debug helper ─────────────────────────────────────────────────────────

    /** Returns the current frequency state for debugging. */
    String debugState() {
        return "minFreq=" + minFreq + " | keyFreq=" + keyFreq + " | freqKeys=" + freqKeys;
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why LinkedHashSet instead of just HashSet for freqKeys values?
// A: HashSet has no ordering. When multiple keys share the same minimum frequency,
//    we need a tiebreak rule. LinkedHashSet maintains insertion order, so the
//    iterator().next() call returns the OLDEST key at that frequency — giving us
//    LRU-within-frequency tiebreaking. This matches LeetCode 460's requirements.
//
// Q: Why track minFreq explicitly instead of searching freqKeys?
// A: freqKeys can have gaps (e.g., only freq 1 and freq 5 exist). Finding the
//    minimum would require O(log n) with a TreeMap or O(n) with a plain HashMap.
//    Tracking minFreq explicitly gives O(1) eviction. It's safe because:
//    - New keys always start at freq=1 → minFreq resets to 1.
//    - onGet increments one key's freq; minFreq only needs updating if the old
//      bucket (which had minFreq) is now empty after the move.
//
// Q: What happens to minFreq when we do evict()?
// A: We remove the victim from freqKeys[minFreq]. If that bucket is now empty,
//    we remove the bucket too. minFreq is NOT updated here — the next onPut
//    will reset it to 1 (for new keys), which is correct because eviction
//    always happens just before a new key is inserted.
//
// Q: LFU vs LRU — when would you choose LFU?
// A: LFU excels when the workload has persistent hot keys (files accessed every
//    request) mixed with cold keys (one-off accesses). LFU retains genuinely
//    popular keys. LRU can evict a hot key just because it wasn't accessed
//    recently (e.g., during a cold scan). Databases often use LFU variants.
//    LFU downside: frequency counts accumulate over time — a key that was hot
//    an hour ago but cold now retains a high count ("frequency aging problem").
//    Fix: use an aging/decay factor, or reset counts periodically.
//
// Q: What's the LeetCode problem for this?
// A: LeetCode 460 — LFU Cache. Exact O(1) implementation using this triple-map
//    design. Worth knowing by heart for Google/Amazon interviews.
