/**
 * ============================================================
 *  PATTERN 5 — PRIORITY QUEUES AND HEAP MECHANICS
 *  Problem 1 (Basic): Kth Largest Element in an Array   LC 215
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array nums and an integer k, return the k-th largest element.
 *    Note: k-th largest means k-th largest in sorted order (not k-th distinct).
 *
 *  EXAMPLE:
 *    nums=[3,2,1,5,6,4], k=2  → 5
 *    nums=[3,2,3,1,2,4,5,5,6], k=4  → 4
 *
 *  CONSTRAINTS:
 *    1 <= k <= nums.length (= N) <= 10^4
 *    -10^4 <= nums[i] <= 10^4
 *
 *  APPROACH 1: Min-heap of size K (PriorityQueue — standard)
 *    Maintain a min-heap with at most K elements.
 *    The heap root = the K-th largest seen so far.
 *    After processing all N elements: heap root = k-th largest overall.
 *    Time:  O(N log K)   — N insertions, each O(log K)
 *    Space: O(K)         — heap holds at most K elements
 *
 *  APPROACH 2: Quickselect (array-backed, average O(N))
 *    Partition-based selection — same logic as QuickSort pivot but only recurse
 *    into one half. Average O(N), worst case O(N²) (avoided with random pivot).
 *    Time:  O(N) average, O(N²) worst
 *    Space: O(1) extra (in-place partitioning)
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Kth_Largest_Element {

    // =========================================================
    // APPROACH 1 — MIN-HEAP OF SIZE K
    // =========================================================

    /**
     * Finds the k-th largest element using a min-heap of bounded size K.
     *
     * HEAP INVARIANT:
     *   The heap always contains at most K elements.
     *   Heap root = minimum of the K largest elements seen so far
     *             = the K-th largest element seen so far.
     *
     * ALGORITHM:
     *   For each element x in nums:
     *     If heap.size() < k: always insert (still building the first k elements).
     *     Else if x > heap.peek() (= current k-th largest):
     *       This x belongs in the top-K. Remove the smallest top-K candidate (root)
     *       and insert x. The heap root now reflects the new k-th largest.
     *     Else: x does not belong in top-K — skip.
     *
     * WHY MIN-HEAP (not max-heap)?
     *   We want to efficiently DISCARD the smallest element when a larger one arrives.
     *   A min-heap lets us read and remove the smallest in O(log K).
     *
     * @param nums input array
     * @param k    rank target (1 = largest, 2 = second largest, ...)
     * @return the k-th largest element
     *
     * Time:  O(N log K)  — N elements, each at most O(log K) heap operations
     * Space: O(K)        — heap holds at most K Integer objects
     */
    public int findKthLargestMinHeap(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k < 1 || k > nums.length) {
            throw new IllegalArgumentException("Invalid input: nums=" +
                Arrays.toString(nums) + " k=" + k);
        }

        // Min-heap: root is always the SMALLEST of the K largest elements
        PriorityQueue<Integer> kLargestMinHeap = new PriorityQueue<>(k);

        for (int currentElement : nums) {
            if (kLargestMinHeap.size() < k) {
                // Heap not yet full — always add
                kLargestMinHeap.offer(currentElement);
            } else if (currentElement > kLargestMinHeap.peek()) {
                // Current element is larger than the current k-th largest → swap it in
                kLargestMinHeap.poll();               // remove old k-th largest
                kLargestMinHeap.offer(currentElement); // insert new candidate
            }
            // else: currentElement <= heap root → not in top-K, ignore
        }

        // Heap root = minimum of the K largest = the K-th largest overall
        return kLargestMinHeap.peek();
    }

    // =========================================================
    // APPROACH 2 — QUICKSELECT (IN-PLACE, ARRAY-BACKED)
    // =========================================================

    /**
     * Finds the k-th largest element using Quickselect.
     *
     * REFRAME: k-th largest = (N - k)-th smallest (0-indexed in sorted order).
     * Let targetIndex = N - k.
     *
     * QUICKSELECT ALGORITHM:
     *   1. Pick a pivot (we use random pivot to avoid worst-case O(N²)).
     *   2. Partition: elements < pivot go left, elements >= pivot go right.
     *   3. After partition, pivot is at its final sorted position (pivotPos).
     *      - If pivotPos == targetIndex: found! Return nums[pivotPos].
     *      - If pivotPos < targetIndex: recurse right (targetIndex is to the right).
     *      - If pivotPos > targetIndex: recurse left (targetIndex is to the left).
     *
     * PARTITION (Lomuto scheme):
     *   Swap pivot to the end. Walk i pointer from left.
     *   All elements < pivot go to left of i. Pivot is placed at i+1.
     *
     * @param nums input array (MODIFIED IN-PLACE — pass a copy if original must be preserved)
     * @param k    k-th largest
     * @return the k-th largest element
     *
     * Time:  O(N) average  — random pivot halves expected work each step
     *        O(N²) worst   — degenerate partitions (avoided by random pivot)
     * Space: O(log N) average  — recursion depth
     *        O(N) worst        — degenerate recursion (avoided by random pivot)
     */
    public int findKthLargestQuickSelect(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k < 1 || k > nums.length) {
            throw new IllegalArgumentException("Invalid input");
        }
        int[] workingArray = nums.clone();   // clone to avoid mutating original
        int targetIndex = workingArray.length - k;  // k-th largest = (N-k)-th smallest
        return quickSelect(workingArray, 0, workingArray.length - 1, targetIndex);
    }

    /**
     * Recursive quickselect on workingArray[leftBound..rightBound].
     */
    private int quickSelect(int[] workingArray, int leftBound, int rightBound, int targetIndex) {
        if (leftBound == rightBound) return workingArray[leftBound];

        // Random pivot to avoid O(N²) worst case
        int pivotIndex = leftBound + new Random().nextInt(rightBound - leftBound + 1);
        int finalPivotPosition = partition(workingArray, leftBound, rightBound, pivotIndex);

        if (finalPivotPosition == targetIndex) {
            return workingArray[finalPivotPosition];
        } else if (finalPivotPosition < targetIndex) {
            return quickSelect(workingArray, finalPivotPosition + 1, rightBound, targetIndex);
        } else {
            return quickSelect(workingArray, leftBound, finalPivotPosition - 1, targetIndex);
        }
    }

    /**
     * Lomuto partition: places pivot at its correct sorted position.
     * @return final sorted position of the pivot
     */
    private int partition(int[] arr, int leftBound, int rightBound, int pivotIndex) {
        int pivotValue = arr[pivotIndex];
        // Move pivot to end
        swap(arr, pivotIndex, rightBound);

        int insertionPoint = leftBound;   // next position for elements < pivotValue

        for (int scanner = leftBound; scanner < rightBound; scanner++) {
            if (arr[scanner] < pivotValue) {
                swap(arr, scanner, insertionPoint);
                insertionPoint++;
            }
        }

        // Place pivot at its correct position
        swap(arr, insertionPoint, rightBound);
        return insertionPoint;
    }

    private void swap(int[] arr, int indexA, int indexB) {
        int temp = arr[indexA];
        arr[indexA] = arr[indexB];
        arr[indexB] = temp;
    }

    /**
     * BONUS — Array-backed manual min-heap for extreme performance.
     * Demonstrates how Java's PriorityQueue works internally.
     *
     * Time: O(N log K), Space: O(K) primitives — no boxing overhead.
     */
    public int findKthLargestManualHeap(int[] nums, int k) {
        int[] heap = new int[k];
        int heapSize = 0;

        for (int element : nums) {
            if (heapSize < k) {
                heap[heapSize++] = element;
                bubbleUp(heap, heapSize - 1);
            } else if (element > heap[0]) {
                heap[0] = element;
                siftDown(heap, 0, heapSize);
            }
        }
        return heap[0];
    }

    private void bubbleUp(int[] heap, int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap[parent] <= heap[index]) break;
            int tmp = heap[parent]; heap[parent] = heap[index]; heap[index] = tmp;
            index = parent;
        }
    }

    private void siftDown(int[] heap, int index, int size) {
        while (true) {
            int smallest = index;
            int left = 2 * index + 1, right = 2 * index + 2;
            if (left < size && heap[left] < heap[smallest])   smallest = left;
            if (right < size && heap[right] < heap[smallest]) smallest = right;
            if (smallest == index) break;
            int tmp = heap[smallest]; heap[smallest] = heap[index]; heap[index] = tmp;
            index = smallest;
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Kth_Largest_Element solver = new Problem_1_Basic_Kth_Largest_Element();

        System.out.println("========================================");
        System.out.println("  Kth Largest Element — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<int[], Integer> testAll = (nums, k) -> {
            int heapResult   = solver.findKthLargestMinHeap(nums, k);
            int qsResult     = solver.findKthLargestQuickSelect(nums, k);
            int manualResult = solver.findKthLargestManualHeap(nums, k);
            boolean ok = (heapResult == qsResult && qsResult == manualResult);
            System.out.printf("nums=%-30s k=%d | Heap=%d QS=%d Manual=%d %s%n",
                Arrays.toString(nums), k, heapResult, qsResult, manualResult, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 215 examples ---");
        testAll.accept(new int[]{3,2,1,5,6,4}, 2);         // expected 5
        testAll.accept(new int[]{3,2,3,1,2,4,5,5,6}, 4);   // expected 4

        System.out.println("\n--- k = 1 (find maximum) ---");
        testAll.accept(new int[]{7,3,9,2,5}, 1);            // expected 9

        System.out.println("\n--- k = N (find minimum) ---");
        testAll.accept(new int[]{7,3,9,2,5}, 5);            // expected 2

        System.out.println("\n--- Single element ---");
        testAll.accept(new int[]{42}, 1);                   // expected 42

        System.out.println("\n--- Duplicates ---");
        testAll.accept(new int[]{5,5,5,5,5}, 3);            // expected 5

        System.out.println("\n--- Negative values ---");
        testAll.accept(new int[]{-3,-1,-2,-5,-4}, 2);       // expected -2

        System.out.println("\n--- Mixed negative/positive ---");
        testAll.accept(new int[]{-1,2,0,-3,5}, 3);          // expected 0

        System.out.println("\n--- Invalid input defensive check ---");
        try {
            solver.findKthLargestMinHeap(new int[]{1,2,3}, 5);
            System.out.println("FAIL: should have thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: IllegalArgumentException on k > N");
        }

        System.out.println("\n========================================");
        System.out.println("  All Kth Largest tests done.");
        System.out.println("========================================");
    }
}
