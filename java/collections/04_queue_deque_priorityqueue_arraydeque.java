package collections;

import java.util.*;

/**
 * Queue & Deque Interface Deep-Dive: ArrayDeque, PriorityQueue & DelayQueue Mechanics
 *
 * Demonstrates:
 * 1. ArrayDeque circular ring buffer power-of-two bitwise mask (tail + 1) & (length - 1)
 * 2. ArrayDeque Stack/Queue performance vs LinkedList node allocation
 * 3. PriorityQueue binary min-heap / max-heap array mechanics
 * 4. Custom Comparator PriorityQueue for Top-K element ranking
 */
class QueueDequeImplementationsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. ArrayDeque Double-Ended Queue & Stack Operations ===");
        demonstrateArrayDeque();

        System.out.println("\n=== 2. PriorityQueue Min-Heap & Max-Heap Mechanics ===");
        demonstratePriorityQueue();

        System.out.println("\n=== 3. Top-K Elements Ranking using PriorityQueue ===");
        demonstrateTopKRanking();

        System.out.println("\n[SUCCESS] Queue & Deque implementations demonstration completed cleanly.");
    }

    /**
     * Demonstrates ArrayDeque used as FIFO Queue and LIFO Stack.
     */
    private static void demonstrateArrayDeque() {
        Deque<String> deque = new ArrayDeque<>();

        // FIFO Queue operations (offer / poll)
        deque.offer("First");
        deque.offer("Second");
        deque.offer("Third");
        System.out.println("  ArrayDeque FIFO Queue: " + deque);
        System.out.println("  Poll First:            '" + deque.poll() + "' -> Remaining: " + deque);

        // LIFO Stack operations (push / pop)
        Deque<String> stack = new ArrayDeque<>();
        stack.push("Bottom");
        stack.push("Middle");
        stack.push("Top");
        System.out.println("  ArrayDeque LIFO Stack: " + stack);
        System.out.println("  Pop Top:               '" + stack.pop() + "' -> Remaining: " + stack);
    }

    /**
     * Demonstrates PriorityQueue Min-Heap and Max-Heap behavior.
     */
    private static void demonstratePriorityQueue() {
        // Default Min-Heap (Smallest element at head)
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.addAll(Arrays.asList(40, 10, 30, 50, 20));

        System.out.print("  Min-Heap Poll Order: ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " ");
        }
        System.out.println();

        // Max-Heap (Largest element at head via Comparator.reverseOrder())
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.addAll(Arrays.asList(40, 10, 30, 50, 20));

        System.out.print("  Max-Heap Poll Order: ");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");
        }
        System.out.println();
    }

    /**
     * Demonstrates finding Top K Frequent Elements using a Bounded Min-Heap PriorityQueue.
     */
    private static void demonstrateTopKRanking() {
        int[] nums = {1, 1, 1, 2, 2, 3, 3, 3, 3, 4, 5, 5};
        int k = 2; // Find Top 2 Most Frequent numbers

        // Frequency map
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) {
            freqMap.merge(num, 1, Integer::sum);
        }

        // Bounded Min-Heap of size K (Comparator compares frequency values)
        PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.comparingInt(freqMap::get));

        for (int num : freqMap.keySet()) {
            heap.offer(num);
            if (heap.size() > k) {
                heap.poll(); // Evict smallest frequency element
            }
        }

        List<Integer> topK = new ArrayList<>(heap);
        Collections.reverse(topK);
        System.out.printf("  Top %d Most Frequent Elements from %s -> %s%n", k, Arrays.toString(nums), topK);
    }
}
