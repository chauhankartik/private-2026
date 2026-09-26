// =============================================================================
// FILE: 03_LRUEvictionPolicy.java
// TOPIC: In-Memory Cache LLD — LRU Eviction Policy
// ALGORITHM: O(1) get + put + evict via DoublyLinkedList + HashMap
// PATTERNS: Strategy (concrete), Internal DSA
// =============================================================================

package cache;

import java.util.HashMap;
import java.util.Map;

// =============================================================================
// ■ LRUEvictionPolicy<K> — Least Recently Used
//
// INVARIANT: Head = Most Recently Used, Tail = Least Recently Used.
//
// GET(key)  → move node to head                  O(1)
// PUT(key)  → add new node at head               O(1)
// EVICT()   → remove tail node                   O(1)
// REMOVE(k) → unlink node directly from list     O(1)
//
// WHY O(1)?
//   HashMap stores key → DLL node reference.
//   Having the direct node reference eliminates O(n) list traversal.
//   Doubly-linked list supports O(1) unlink given a node reference.
//
// We use SENTINEL head/tail nodes (dummy nodes) to eliminate null checks
// on every insert/delete operation — a common interview technique.
// =============================================================================
class LRUEvictionPolicy<K> implements EvictionPolicy<K> {

    // ─── Internal Doubly-Linked List Node ─────────────────────────────────────
    private static class Node<K> {
        K       key;
        Node<K> prev;
        Node<K> next;

        Node(K key) { this.key = key; }
    }

    // ─── State ────────────────────────────────────────────────────────────────

    // key → node reference for O(1) lookup
    private final Map<K, Node<K>> map  = new HashMap<>();

    // Sentinel nodes: head.next = MRU, tail.prev = LRU
    private final Node<K> head = new Node<>(null);  // dummy head (most recent)
    private final Node<K> tail = new Node<>(null);  // dummy tail (least recent)

    LRUEvictionPolicy() {
        // Wire sentinels together at start
        head.next = tail;
        tail.prev = head;
    }

    // ─── EvictionPolicy interface ─────────────────────────────────────────────

    /**
     * Cache HIT: move accessed node to head (most recently used).
     * Time: O(1)
     */
    @Override
    public void onGet(K key) {
        Node<K> node = map.get(key);
        if (node != null) {
            unlink(node);
            insertAfterHead(node);
        }
    }

    /**
     * Cache PUT: if key exists, move to head; otherwise insert new node at head.
     * Time: O(1)
     */
    @Override
    public void onPut(K key) {
        if (map.containsKey(key)) {
            // Update existing: move to MRU position
            onGet(key);
        } else {
            // New entry: create node and add at head
            Node<K> node = new Node<>(key);
            insertAfterHead(node);
            map.put(key, node);
        }
    }

    /**
     * EVICT: remove and return the LRU key (tail.prev).
     * Time: O(1)
     *
     * @throws IllegalStateException if the policy is empty
     */
    @Override
    public K evict() {
        if (head.next == tail) {
            throw new IllegalStateException("LRUEvictionPolicy is empty — nothing to evict");
        }
        Node<K> lru = tail.prev;    // LRU node is just before dummy tail
        unlink(lru);
        map.remove(lru.key);
        return lru.key;
    }

    /**
     * Explicitly remove a key (on cache.remove() or after eviction by InMemoryCache).
     * Time: O(1)
     */
    @Override
    public void remove(K key) {
        Node<K> node = map.remove(key);
        if (node != null) unlink(node);
    }

    @Override
    public void clear() {
        map.clear();
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public int size() {
        return map.size();
    }

    // ─── Private DLL helpers ─────────────────────────────────────────────────

    /**
     * Unlink a node from the doubly-linked list.
     * Connects its prev and next neighbours directly.
     * Time: O(1) — no traversal, we have direct node references.
     */
    private void unlink(Node<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    /**
     * Insert a node immediately after the dummy head (MRU position).
     * Time: O(1)
     */
    private void insertAfterHead(Node<K> node) {
        node.next      = head.next;
        node.prev      = head;
        head.next.prev = node;
        head.next      = node;
    }

    // ─── Debug helper ─────────────────────────────────────────────────────────

    /** Returns the current MRU→LRU order as a string. Useful in demos/tests. */
    String orderMruToLru() {
        StringBuilder sb = new StringBuilder("MRU → ");
        Node<K> curr = head.next;
        while (curr != tail) {
            sb.append(curr.key);
            if (curr.next != tail) sb.append(" → ");
            curr = curr.next;
        }
        sb.append(" ← LRU");
        return sb.toString();
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Walk me through an LRU cache with capacity 3. Access sequence: A, B, C, A, D.
// A:
//   PUT A: [A]           MRU=A LRU=A
//   PUT B: [B, A]        MRU=B LRU=A
//   PUT C: [C, B, A]     MRU=C LRU=A  (full)
//   GET A: [A, C, B]     MRU=A LRU=B  (A moved to head)
//   PUT D: evict B → [D, A, C]   MRU=D LRU=C
//
// Q: Why use sentinel (dummy) head and tail nodes?
// A: Without sentinels, insertAfterHead and unlink need null checks for the
//    boundary conditions (first node, last node). Sentinels eliminate all
//    edge cases — the logic is uniform for every node. Production DLL code
//    always uses sentinels for this reason.
//
// Q: Could you use Java's LinkedHashMap instead?
// A: Yes: LinkedHashMap(capacity, 0.75f, true) with removeEldestEntry() override.
//    But the interviewer usually wants you to build from scratch to demonstrate
//    you understand WHY it works (DLL + HashMap). LinkedHashMap is a valid
//    callout for "how would you do this in 5 lines in production?"
//
// Q: Is LRUEvictionPolicy thread-safe?
// A: No — it is intentionally NOT thread-safe. InMemoryCache acquires a write
//    lock before calling any onGet/onPut/evict methods. Making the policy
//    thread-safe independently would create redundant locking (lock within lock).
//    The policy relies on the outer InMemoryCache lock. This is the correct
//    design: let the cache own concurrency, policies own ordering logic.
//
// Q: What if the same key is put twice?
// A: onPut detects the existing node (map.containsKey), calls onGet to move it
//    to MRU, and the map entry is updated by InMemoryCache. No duplicate nodes.
