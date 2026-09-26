package interview;

import java.util.*;
import java.util.concurrent.*;

/**
 * Collections & Concurrency Interview Gotchas Masterclass
 *
 * Demonstrates:
 * 1. Mutating HashMap Key Objects (Lost Map Entry Trap)
 * 2. Arrays.asList() Fixed-Size Backing Array Traps
 * 3. ConcurrentHashMap Null Key/Value NullPointerException Strict Constraint
 * 4. ThreadLocal Task Memory Leak across Worker Threads
 */
class CollectionsConcurrencyGotchasDemo {

    // Mutable Key Class
    static class MutableKey {
        int id;

        MutableKey(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MutableKey key = (MutableKey) o;
            return id == key.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Mutating HashMap Key Object Trap ===");
        demonstrateMutableHashMapKey();

        System.out.println("\n=== 2. Arrays.asList() Fixed-Size View Traps ===");
        demonstrateArraysAsListTrap();

        System.out.println("\n=== 3. ConcurrentHashMap Null Restrictions ===");
        demonstrateConcurrentHashMapNullCheck();

        System.out.println("\n=== 4. ThreadLocal Memory Leak in Worker Thread Pool ===");
        demonstrateThreadLocalLeak();

        System.out.println("\n[SUCCESS] Collections & Concurrency gotchas demonstration completed cleanly.");
    }

    private static void demonstrateMutableHashMapKey() {
        Map<MutableKey, String> map = new HashMap<>();
        MutableKey key = new MutableKey(101);

        map.put(key, "Staff Engineer Profile");
        System.out.println("  Inserted MutableKey(101). map.get(key): '" + map.get(key) + "'");

        // Mutate key id
        key.id = 999; // Alters key.hashCode()!

        System.out.println("  Mutated key.id = 999.");
        System.out.println("  map.get(key) after mutation: " + map.get(key) + " // Returned NULL! Entry is trapped in old bucket.");
        System.out.println("  map.size():                   " + map.size() + " // Map size still shows 1 element!");
    }

    private static void demonstrateArraysAsListTrap() {
        String[] arr = {"Apple", "Banana", "Cherry"};
        List<String> list = Arrays.asList(arr);

        System.out.println("  List created via Arrays.asList(arr): " + list);

        // Mutating element works
        list.set(0, "Avocado");
        System.out.println("  list.set(0, \"Avocado\") -> Original Array arr[0]: '" + arr[0] + "' (Backing array mutated!)");

        // Adding or removing elements throws UnsupportedOperationException
        try {
            System.out.print("  Attempting list.add(\"Date\"): ");
            list.add("Date");
        } catch (UnsupportedOperationException e) {
            System.out.println("CAUGHT UnsupportedOperationException! (Arrays.asList creates a fixed-size wrapper)");
        }
    }

    private static void demonstrateConcurrentHashMapNullCheck() {
        Map<String, String> chm = new ConcurrentHashMap<>();

        try {
            System.out.print("  Attempting ConcurrentHashMap.put(null, \"value\"): ");
            chm.put(null, "value");
        } catch (NullPointerException e) {
            System.out.println("CAUGHT NullPointerException! (ConcurrentHashMap forbids null keys)");
        }

        try {
            System.out.print("  Attempting ConcurrentHashMap.put(\"key\", null): ");
            chm.put("key", null);
        } catch (NullPointerException e) {
            System.out.println("CAUGHT NullPointerException! (ConcurrentHashMap forbids null values to avoid ambiguous get() vs containsKey())");
        }
    }

    // ThreadLocal Memory Leak Demo
    private static final ThreadLocal<byte[]> threadLocalContext = new ThreadLocal<>();

    private static void demonstrateThreadLocalLeak() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Task 1 sets ThreadLocal value without calling remove()
        executor.submit(() -> {
            System.out.println("  [Task 1 on Thread Pool Worker] Setting ThreadLocal 1MB Context payload...");
            threadLocalContext.set(new byte[1024 * 1024]);
        }).get();

        // Task 2 executes on the same reused thread pool worker
        executor.submit(() -> {
            byte[] leakedData = threadLocalContext.get();
            System.out.println("  [Task 2 on Reused Worker Thread] Read ThreadLocal Context: " +
                    (leakedData != null ? "LEAKED! Found leftover data from Task 1" : "Clean"));
            threadLocalContext.remove(); // Clean up thread local value!
        }).get();

        executor.shutdown();
    }
}
