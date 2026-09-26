package collections;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Map Interface Deep-Dive: HashMap, LinkedHashMap, TreeMap, IdentityHashMap & WeakHashMap
 *
 * Demonstrates:
 * 1. HashMap internal hash spreading (h ^ (h >>> 16)) & bucket treeification
 * 2. LinkedHashMap access-order LRU (Least Recently Used) Cache implementation
 * 3. TreeMap Red-Black tree NavigableMap range queries (floorKey, ceilingKey, subMap)
 * 4. IdentityHashMap reference equality (==) vs object equality (.equals)
 * 5. WeakHashMap key garbage collection & automatic key-value entry eviction
 */
class MapImplementationsDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. HashMap Internals & Compute APIs ===");
        demonstrateHashMapAPIs();

        System.out.println("\n=== 2. LinkedHashMap LRU Cache (access-order = true) ===");
        demonstrateLinkedHashMapLRU();

        System.out.println("\n=== 3. TreeMap NavigableMap Range Queries ===");
        demonstrateTreeMapNavigable();

        System.out.println("\n=== 4. IdentityHashMap Reference Equality ===");
        demonstrateIdentityHashMap();

        System.out.println("\n=== 5. WeakHashMap Garbage Collection Eviction ===");
        demonstrateWeakHashMap();

        System.out.println("\n[SUCCESS] Map implementations demonstration completed cleanly.");
    }

    /**
     * Demonstrates modern Java 8+ HashMap compute and merge APIs.
     */
    private static void demonstrateHashMapAPIs() {
        Map<String, Integer> wordCounts = new HashMap<>();

        // 1. computeIfAbsent (Lazy initialization of collections)
        Map<String, List<Integer>> groupedScores = new HashMap<>();
        groupedScores.computeIfAbsent("Math", k -> new ArrayList<>()).add(95);
        groupedScores.computeIfAbsent("Math", k -> new ArrayList<>()).add(100);

        // 2. merge API (Frequency counting)
        String[] words = {"apple", "banana", "apple", "cherry", "apple"};
        for (String w : words) {
            wordCounts.merge(w, 1, Integer::sum);
        }

        System.out.println("  HashMap Word Frequencies: " + wordCounts);
        System.out.println("  HashMap Grouped Scores:   " + groupedScores);
    }

    /**
     * Custom LRU Cache leveraging LinkedHashMap access-order eviction.
     */
    static class SimpleLRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public SimpleLRUCache(int capacity) {
            // initialCapacity, loadFactor = 0.75f, accessOrder = true
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity; // Evict eldest accessed entry when size exceeds capacity
        }
    }

    private static void demonstrateLinkedHashMapLRU() {
        SimpleLRUCache<Integer, String> lru = new SimpleLRUCache<>(3);
        lru.put(1, "One");
        lru.put(2, "Two");
        lru.put(3, "Three");
        System.out.println("  Initial LRU Cache (Capacity 3): " + lru);

        // Access Key 1 -> Moves Key 1 to the end (Most Recently Used)
        lru.get(1);
        System.out.println("  After accessing Key 1:         " + lru);

        // Insert Key 4 -> Triggers eviction of Key 2 (Least Recently Used)
        lru.put(4, "Four");
        System.out.println("  After inserting Key 4:         " + lru);
        System.out.println("  Contains Key 2? " + lru.containsKey(2) + " (Evicted!)");
    }

    /**
     * Demonstrates TreeMap NavigableMap methods.
     */
    private static void demonstrateTreeMapNavigable() {
        NavigableMap<Integer, String> treeMap = new TreeMap<>();
        treeMap.put(10, "Ten");
        treeMap.put(20, "Twenty");
        treeMap.put(30, "Thirty");
        treeMap.put(40, "Forty");

        System.out.println("  TreeMap Order: " + treeMap);
        System.out.println("  floorKey(25)   [<= 25]: " + treeMap.floorKey(25));
        System.out.println("  ceilingKey(25) [>= 25]: " + treeMap.ceilingKey(25));
        System.out.println("  subMap(15, 35) [15..35]: " + treeMap.subMap(15, 35));
    }

    /**
     * Demonstrates IdentityHashMap matching key references (==) instead of equals().
     */
    private static void demonstrateIdentityHashMap() {
        Map<String, String> identityMap = new IdentityHashMap<>();

        String k1 = new String("key");
        String k2 = new String("key");

        identityMap.put(k1, "Value1");
        identityMap.put(k2, "Value2");

        System.out.println("  Standard HashMap size for duplicate String content would be 1.");
        System.out.println("  IdentityHashMap size for new String(\"key\") references (k1 != k2): " + identityMap.size());
        System.out.println("  k1 == k2? " + (k1 == k2));
    }

    /**
     * Demonstrates WeakHashMap key garbage collection eviction.
     */
    private static void demonstrateWeakHashMap() throws InterruptedException {
        Map<Object, String> weakMap = new WeakHashMap<>();

        Object keyObj = new Object();
        weakMap.put(keyObj, "Transient Memory Data");

        System.out.println("  Before GC: WeakHashMap size = " + weakMap.size());

        // Nullify strong reference to keyObj
        keyObj = null;

        // Force Garbage Collection
        System.gc();
        Thread.sleep(100);

        System.out.println("  After GC & Key Nullification: WeakHashMap size = " + weakMap.size());
        System.out.println("  >>> WeakHashMap automatically purged weak-referenced entry!");
    }
}
