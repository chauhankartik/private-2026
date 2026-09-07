import java.util.*;

/**
 * ============================================================
 *  JAVA COLLECTIONS — MEDIUM PROBLEMS
 *  Applying Collections to solve non-trivial logic.
 * ============================================================
 *
 *  Problems:
 *   M1. Custom Iterator (PeekingIterator)
 *   M2. Top K Frequent Elements (PriorityQueue)
 *   M3. Calendar / Interval Booking (TreeMap)
 *   M4. Basic LRU Cache (LinkedHashMap)
 *
 * ============================================================
 */
public class Medium {

    // =========================================================
    // M1. CUSTOM ITERATOR
    // Pattern: Wrapping Iterators
    // =========================================================
    /**
     * Problem: Design an iterator that supports the peek operation on an
     * existing iterator in addition to the hasNext and the next operations.
     * (LeetCode 284: Peeking Iterator)
     *
     * Concept: Cache the next element.
     */
    class PeekingIterator implements Iterator<Integer> {
        private Iterator<Integer> iterator;
        private Integer nextElement;

        public PeekingIterator(Iterator<Integer> iterator) {
            this.iterator = iterator;
            if (this.iterator.hasNext()) {
                nextElement = this.iterator.next();
            }
        }

        public Integer peek() {
            return nextElement;
        }

        @Override
        public Integer next() {
            Integer current = nextElement;
            if (iterator.hasNext()) {
                nextElement = iterator.next();
            } else {
                nextElement = null;
            }
            return current;
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }
    }

    // =========================================================
    // M2. TOP K FREQUENT ELEMENTS
    // Pattern: HashMap + PriorityQueue
    // =========================================================
    /**
     * Problem: Given an integer array nums and an integer k, return the k
     * most frequent elements.
     *
     * Approach: Build a frequency map, then use a Min-Heap (PriorityQueue)
     * to keep track of the top k elements.
     *
     * Time Complexity: O(n log k)
     * Space Complexity: O(n) for map + O(k) for heap
     */
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int num : nums) {
            count.put(num, count.getOrDefault(num, 0) + 1);
        }

        // Min-heap ordered by frequency
        PriorityQueue<Integer> heap = new PriorityQueue<>(
            (n1, n2) -> count.get(n1) - count.get(n2)
        );

        for (int n : count.keySet()) {
            heap.add(n);
            if (heap.size() > k) {
                heap.poll(); // remove the least frequent element
            }
        }

        int[] top = new int[k];
        for (int i = k - 1; i >= 0; i--) {
            top[i] = heap.poll();
        }
        return top;
    }

    /**
     * Follow-up: Can you do it in O(n) time?
     *   → Yes, using Bucket Sort. Create an array of Lists where index = frequency.
     */

    // =========================================================
    // M3. CALENDAR / INTERVAL BOOKING
    // Pattern: TreeMap (floorKey, ceilingKey)
    // =========================================================
    /**
     * Problem: Implement a Calendar class that can add a new event [start, end).
     * Returns true if the event can be added without a double booking.
     * (LeetCode 729: My Calendar I)
     *
     * Concept: A TreeMap keeps keys sorted.
     * floorKey(start) gives the event that starts immediately before or at 'start'.
     * ceilingKey(start) gives the event that starts immediately after 'start'.
     *
     * Time Complexity: O(log n) per booking
     * Space Complexity: O(n)
     */
    class MyCalendar {
        TreeMap<Integer, Integer> calendar;

        public MyCalendar() {
            calendar = new TreeMap<>();
        }

        public boolean book(int start, int end) {
            Integer prev = calendar.floorKey(start);
            Integer next = calendar.ceilingKey(start);

            // Check overlap with previous event
            if (prev != null && calendar.get(prev) > start) return false;
            
            // Check overlap with next event
            if (next != null && next < end) return false;

            calendar.put(start, end);
            return true;
        }
    }

    // =========================================================
    // M4. BASIC LRU CACHE
    // Pattern: LinkedHashMap overrides
    // =========================================================
    /**
     * Problem: Design a data structure that follows the constraints of a 
     * Least Recently Used (LRU) cache.
     *
     * Concept: Java's LinkedHashMap has a built-in LRU feature.
     * By passing `accessOrder = true` to the constructor, the map orders elements
     * by access (get/put) rather than insertion.
     * Overriding `removeEldestEntry` controls eviction.
     *
     * Time Complexity: O(1) for both get and put
     * Space Complexity: O(capacity)
     */
    class LRUCache {
        private LinkedHashMap<Integer, Integer> map;
        private final int CAPACITY;

        public LRUCache(int capacity) {
            this.CAPACITY = capacity;
            
            // capacity, loadFactor, accessOrder (true = LRU order)
            this.map = new LinkedHashMap<Integer, Integer>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                    return size() > CAPACITY; 
                }
            };
        }

        public int get(int key) {
            return map.getOrDefault(key, -1);
        }

        public void put(int key, int value) {
            map.put(key, value);
        }
    }
}
