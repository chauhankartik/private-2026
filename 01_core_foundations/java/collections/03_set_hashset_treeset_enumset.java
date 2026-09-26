package collections;

import java.util.*;

/**
 * Set Interface Deep-Dive: HashSet, TreeSet, LinkedHashSet, EnumSet & CopyOnWriteArraySet
 *
 * Demonstrates:
 * 1. HashSet backing HashMap dummy PRESENT value mechanism
 * 2. TreeSet Red-Black tree NavigableSet range queries (headSet, tailSet, subSet)
 * 3. LinkedHashSet insertion-order retention
 * 4. EnumSet bitwise bitmask representations (RegularEnumSet vs JumboEnumSet)
 * 5. CopyOnWriteArraySet thread-safe set mechanics
 */
class SetImplementationsDemo {

    enum Status {
        NEW, PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }

    public static void main(String[] args) {
        System.out.println("=== 1. HashSet vs LinkedHashSet Insertion Ordering ===");
        demonstrateHashSetVsLinkedHashSet();

        System.out.println("\n=== 2. TreeSet NavigableSet Range Queries & Comparators ===");
        demonstrateTreeSetNavigable();

        System.out.println("\n=== 3. High-Performance EnumSet Bit Vectors ===");
        demonstrateEnumSet();

        System.out.println("\n[SUCCESS] Set implementations demonstration completed cleanly.");
    }

    /**
     * Demonstrates ordering difference between HashSet (unordered) and LinkedHashSet (insertion order).
     */
    private static void demonstrateHashSetVsLinkedHashSet() {
        Set<String> hashSet = new HashSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();

        String[] elements = {"Zebra", "Apple", "Mango", "Banana", "Cherry"};
        for (String el : elements) {
            hashSet.add(el);
            linkedHashSet.add(el);
        }

        System.out.println("  HashSet Order (Unordered Bucket Distribution): " + hashSet);
        System.out.println("  LinkedHashSet Order (Preserves Insertion Order): " + linkedHashSet);
    }

    /**
     * Demonstrates TreeSet NavigableSet operations.
     */
    private static void demonstrateTreeSetNavigable() {
        NavigableSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 50, 30, 20, 40));

        System.out.println("  TreeSet Natural Sorted Order: " + treeSet);
        System.out.println("  first()  [Min Element]:       " + treeSet.first());
        System.out.println("  last()   [Max Element]:       " + treeSet.last());
        System.out.println("  lower(30)  [Strictly < 30]:   " + treeSet.lower(30));
        System.out.println("  higher(30) [Strictly > 30]:   " + treeSet.higher(30));
        System.out.println("  subSet(20, true, 40, true):   " + treeSet.subSet(20, true, 40, true));
    }

    /**
     * Demonstrates EnumSet bitwise bitmask performance.
     */
    private static void demonstrateEnumSet() {
        // Create EnumSet of active working statuses
        EnumSet<Status> activeStatuses = EnumSet.of(Status.NEW, Status.PENDING, Status.IN_PROGRESS);
        System.out.println("  Initial EnumSet: " + activeStatuses);

        // Add COMPLETED status
        activeStatuses.add(Status.COMPLETED);
        System.out.println("  Updated EnumSet: " + activeStatuses);

        // Complement of activeStatuses (FAILED, CANCELLED)
        EnumSet<Status> inactiveStatuses = EnumSet.complementOf(activeStatuses);
        System.out.println("  Complement EnumSet (Inactive): " + inactiveStatuses);

        System.out.println("  EnumSet Class: " + activeStatuses.getClass().getName());
        System.out.println("  >>> Key Insight: RegularEnumSet represents set elements as bits in a single 64-bit long integer!");
    }
}
