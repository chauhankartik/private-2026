package collections;

import java.util.*;

/**
 * List Interface Deep-Dive: ArrayList, LinkedList, Vector & SubList Mechanics
 *
 * Demonstrates:
 * 1. ArrayList internal array expansion algorithm (1.5x growing factor)
 * 2. LinkedList doubly-linked node pointer memory overhead vs ArrayList CPU cache locality
 * 3. subList() view behavior, structural mutations, and modCount ConcurrentModificationException
 * 4. Legacy Vector & Stack synchronization locking penalties
 */
class ListImplementationsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. ArrayList Dynamic Capacity Expansion & SubList Mechanics ===");
        demonstrateArrayListSubList();

        System.out.println("\n=== 2. ArrayList vs LinkedList Iteration & Access Benchmark ===");
        benchmarkArrayListVsLinkedList();

        System.out.println("\n=== 3. Legacy Vector & Stack Synchronization Overview ===");
        demonstrateLegacyVectorStack();

        System.out.println("\n[SUCCESS] List implementations demonstration completed cleanly.");
    }

    /**
     * Demonstrates ArrayList subList backing view behavior and modCount mutation rules.
     */
    private static void demonstrateArrayListSubList() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        System.out.println("Original ArrayList: " + list);

        // 1. Create subList view (Index range 1 to 4 -> "B", "C", "D")
        List<String> subList = list.subList(1, 4);
        System.out.println("  subList(1, 4):    " + subList);

        // 2. Modifying subList mutates the backing ArrayList
        subList.set(0, "B_MODIFIED");
        System.out.println("  After subList.set(0): Original List = " + list);

        // 3. Modifying backing list directly invalidates subList view (Triggers ConcurrentModificationException on subList access)
        list.add("F"); // Structural modification updates modCount in parent list
        System.out.println("  Added 'F' directly to parent list. Parent List = " + list);

        try {
            System.out.print("  Attempting to read subList after parent mutation: ");
            subList.get(0);
        } catch (ConcurrentModificationException e) {
            System.out.println("CAUGHT ConcurrentModificationException! (subList expected parent modCount mismatch)");
        }
    }

    /**
     * Compares random access and iteration performance between ArrayList and LinkedList.
     */
    private static void benchmarkArrayListVsLinkedList() {
        int count = 100_000;
        List<Integer> arrayList = new ArrayList<>(count);
        List<Integer> linkedList = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // Random Access Benchmark (get(i))
        long start = System.currentTimeMillis();
        long sumArray = 0;
        for (int i = 0; i < 10_000; i++) {
            sumArray += arrayList.get(i * 10);
        }
        long arrayTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        long sumLinked = 0;
        for (int i = 0; i < 10_000; i++) {
            sumLinked += linkedList.get(i * 10); // O(N) pointer traversal for each get call!
        }
        long linkedTime = System.currentTimeMillis() - start;

        System.out.printf("  Random Access get() [10,000 ops] -> ArrayList: %d ms, LinkedList: %d ms%n",
                arrayTime, linkedTime);

        // Iterator Sequential Traversal Benchmark
        start = System.currentTimeMillis();
        long iterSumArray = 0;
        for (int val : arrayList) {
            iterSumArray += val;
        }
        long iterArrayTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        long iterSumLinked = 0;
        for (int val : linkedList) {
            iterSumLinked += val;
        }
        long iterLinkedTime = System.currentTimeMillis() - start;

        System.out.printf("  Sequential Iteration [100,000 elements] -> ArrayList: %d ms, LinkedList: %d ms%n",
                iterArrayTime, iterLinkedTime);
        System.out.println("  >>> Key Insight: LinkedList node pointer chasing breaches CPU cache lines.");
    }

    /**
     * Demonstrates legacy Vector and Stack.
     */
    private static void demonstrateLegacyVectorStack() {
        Vector<String> vector = new Vector<>();
        vector.add("ThreadSafe1");
        vector.add("ThreadSafe2");

        // Stack extends Vector (Synchronized methods on every pop/push)
        Stack<String> stack = new Stack<>();
        stack.push("Bottom");
        stack.push("Top");

        System.out.printf("  Vector Size: %d, Stack Pop: '%s'%n", vector.size(), stack.pop());
        System.out.println("  >>> Legacy Recommendation: Prefer ArrayDeque for Stack/Queue operations and ArrayList for unsynchronized lists.");
    }
}
