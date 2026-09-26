package collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * Concurrent Collections & Iterator Fail-Fast vs Fail-Safe Masterclass
 *
 * Demonstrates:
 * 1. Fail-Fast Iterator mechanics (ArrayList modCount structural change detection)
 * 2. Fail-Safe / Weakly Consistent Iterators (CopyOnWriteArrayList, ConcurrentHashMap)
 * 3. ConcurrentHashMap lock-free CAS reads and synchronized bucket bin head writes
 * 4. ConcurrentSkipListMap lock-free skip list ordering
 */
class ConcurrentCollectionsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Fail-Fast Iterators (ArrayList modCount Mismatch) ===");
        demonstrateFailFast();

        System.out.println("\n=== 2. Fail-Safe Iterators (CopyOnWriteArrayList Snapshot) ===");
        demonstrateFailSafeCopyOnWrite();

        System.out.println("\n=== 3. ConcurrentHashMap Fine-Grained Synchronization & Iteration ===");
        demonstrateConcurrentHashMap();

        System.out.println("\n=== 4. ConcurrentSkipListMap Thread-Safe Sorted Map ===");
        demonstrateConcurrentSkipListMap();

        System.out.println("\n[SUCCESS] Concurrent Collections & Iterators demonstration completed cleanly.");
    }

    /**
     * Demonstrates Fail-Fast iterator throwing ConcurrentModificationException.
     */
    private static void demonstrateFailFast() {
        List<String> list = new ArrayList<>(Arrays.asList("One", "Two", "Three"));

        try {
            System.out.print("  Iterating ArrayList while modifying: ");
            for (String item : list) {
                if (item.equals("Two")) {
                    list.remove(item); // Direct collection mutation invalidates Iterator modCount!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("CAUGHT ConcurrentModificationException! (Fail-Fast Iterator detected modCount mismatch)");
        }

        // Correct way to mutate during iteration: Iterator.remove()
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equals("Two")) {
                it.remove(); // Safely updates expectedModCount in Iterator
            }
        }
        System.out.println("  ArrayList after Iterator.remove(): " + list);
    }

    /**
     * Demonstrates Fail-Safe / Weakly Consistent Iterator using CopyOnWriteArrayList.
     */
    private static void demonstrateFailSafeCopyOnWrite() {
        List<String> cowList = new CopyOnWriteArrayList<>(Arrays.asList("Alpha", "Beta", "Gamma"));

        System.out.println("  Iterating CopyOnWriteArrayList while adding elements:");
        for (String item : cowList) {
            System.out.println("    Visited Item: " + item);
            if (item.equals("Beta")) {
                cowList.add("Delta"); // Creates a new underlying Object[] array copy; Iterator operates on snapshot
            }
        }
        System.out.println("  Final CopyOnWriteArrayList Content: " + cowList);
        System.out.println("  >>> Notice: 'Delta' was added to main list but omitted from snapshot iteration without Exception!");
    }

    /**
     * Demonstrates ConcurrentHashMap weakly consistent iteration.
     */
    private static void demonstrateConcurrentHashMap() {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put("A", 100);
        map.put("B", 200);
        map.put("C", 300);

        System.out.println("  Iterating ConcurrentHashMap with concurrent put:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.printf("    Key: %s, Val: %d%n", entry.getKey(), entry.getValue());
            if (entry.getKey().equals("B")) {
                map.put("D", 400); // Concurrent insertion supported safely
            }
        }
        System.out.println("  Final ConcurrentHashMap: " + map);
    }

    /**
     * Demonstrates ConcurrentSkipListMap.
     */
    private static void demonstrateConcurrentSkipListMap() {
        ConcurrentNavigableMap<Integer, String> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put(30, "Thirty");
        skipListMap.put(10, "Ten");
        skipListMap.put(20, "Twenty");

        System.out.println("  ConcurrentSkipListMap Sorted Order: " + skipListMap);
        System.out.println("  subMap(10, true, 25, true):          " + skipListMap.subMap(10, true, 25, true));
        System.out.println("  >>> ConcurrentSkipListMap achieves O(log N) lock-free reads/writes via Skip List data structure!");
    }
}
