import java.util.*;

/**
 * ============================================================
 *  JAVA COLLECTIONS — HARD PROBLEMS
 *  Designing complex data structures combining multiple collections.
 * ============================================================
 *
 *  Problems:
 *   H1. LFU Cache
 *   H2. Insert Delete GetRandom O(1)
 *   H3. Design HashMap (Separate Chaining)
 *
 * ============================================================
 */
public class Hard {

    // =========================================================
    // H1. LFU CACHE
    // Pattern: Map of LinkedHashSets
    // =========================================================
    /**
     * Problem: Design and implement a data structure for a Least Frequently Used (LFU) cache.
     * (LeetCode 460: LFU Cache)
     *
     * Concept: 
     * 1. A key-value map.
     * 2. A key-frequency map.
     * 3. A frequency to LinkedHashSet of keys map (to maintain LRU order among keys with the same frequency).
     * 4. A min-frequency tracker.
     */
    class LFUCache {
        private final int capacity;
        private int minFreq;
        private Map<Integer, Integer> keyToVal;
        private Map<Integer, Integer> keyToFreq;
        private Map<Integer, LinkedHashSet<Integer>> freqToKeys;

        public LFUCache(int capacity) {
            this.capacity = capacity;
            this.minFreq = 0;
            this.keyToVal = new HashMap<>();
            this.keyToFreq = new HashMap<>();
            this.freqToKeys = new HashMap<>();
        }

        public int get(int key) {
            if (!keyToVal.containsKey(key)) return -1;
            
            int freq = keyToFreq.get(key);
            keyToFreq.put(key, freq + 1);
            
            freqToKeys.get(freq).remove(key);
            if (freq == minFreq && freqToKeys.get(freq).isEmpty()) {
                minFreq++;
            }
            
            freqToKeys.computeIfAbsent(freq + 1, k -> new LinkedHashSet<>()).add(key);
            return keyToVal.get(key);
        }

        public void put(int key, int value) {
            if (capacity <= 0) return;

            if (keyToVal.containsKey(key)) {
                keyToVal.put(key, value);
                get(key); // update frequency
                return;
            }

            if (keyToVal.size() >= capacity) {
                // Evict LFU (and LRU if tie)
                int evictKey = freqToKeys.get(minFreq).iterator().next();
                freqToKeys.get(minFreq).remove(evictKey);
                keyToVal.remove(evictKey);
                keyToFreq.remove(evictKey);
            }

            // Add new key
            keyToVal.put(key, value);
            keyToFreq.put(key, 1);
            minFreq = 1;
            freqToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        }
    }

    // =========================================================
    // H2. INSERT DELETE GETRANDOM O(1)
    // Pattern: ArrayList + HashMap
    // =========================================================
    /**
     * Problem: Implement the RandomizedSet class which supports insert, remove, 
     * and getRandom in O(1) average time.
     * (LeetCode 380: Insert Delete GetRandom O(1))
     *
     * Concept: 
     * - ArrayList allows O(1) getRandom and O(1) append.
     * - HashMap allows O(1) lookups to find the index of an element in the ArrayList.
     * - To remove in O(1), swap the element to remove with the last element in the ArrayList,
     *   update the map, and remove the last element.
     */
    class RandomizedSet {
        private Map<Integer, Integer> map; // value -> index
        private List<Integer> list;
        private Random random;

        public RandomizedSet() {
            map = new HashMap<>();
            list = new ArrayList<>();
            random = new Random();
        }

        public boolean insert(int val) {
            if (map.containsKey(val)) return false;
            
            map.put(val, list.size());
            list.add(val);
            return true;
        }

        public boolean remove(int val) {
            if (!map.containsKey(val)) return false;
            
            int index = map.get(val);
            int lastElement = list.get(list.size() - 1);
            
            // Move last element to the deleted slot
            list.set(index, lastElement);
            map.put(lastElement, index);
            
            // Delete the last element
            list.remove(list.size() - 1);
            map.remove(val);
            return true;
        }

        public int getRandom() {
            return list.get(random.nextInt(list.size()));
        }
    }

    // =========================================================
    // H3. DESIGN HASHMAP
    // Pattern: Array of LinkedLists (Separate Chaining)
    // =========================================================
    /**
     * Problem: Design a HashMap without using any built-in hash table libraries.
     * (LeetCode 706: Design HashMap)
     */
    class MyHashMap {
        private class Node {
            int key, val;
            Node next;
            Node(int key, int val) {
                this.key = key;
                this.val = val;
            }
        }

        private final int SIZE = 10000;
        private Node[] nodes;

        public MyHashMap() {
            nodes = new Node[SIZE];
        }

        private int getIndex(int key) {
            return Integer.hashCode(key) % SIZE;
        }

        // Helper to find node BEFORE the target node
        private Node find(Node head, int key) {
            Node curr = head, prev = null;
            while (curr != null && curr.key != key) {
                prev = curr;
                curr = curr.next;
            }
            return prev;
        }

        public void put(int key, int value) {
            int index = getIndex(key);
            if (nodes[index] == null) {
                nodes[index] = new Node(-1, -1); // Dummy head
            }
            Node prev = find(nodes[index], key);
            if (prev.next == null) {
                prev.next = new Node(key, value);
            } else {
                prev.next.val = value;
            }
        }

        public int get(int key) {
            int index = getIndex(key);
            if (nodes[index] == null) return -1;
            Node prev = find(nodes[index], key);
            return prev.next == null ? -1 : prev.next.val;
        }

        public void remove(int key) {
            int index = getIndex(key);
            if (nodes[index] == null) return;
            Node prev = find(nodes[index], key);
            if (prev.next != null) {
                prev.next = prev.next.next;
            }
        }
    }
}
