/**
 * ============================================================
 *  PATTERN 4 — MONOTONIC DEQUES AND SLIDING WINDOWS
 *  Problem 1 (Basic): Sliding Window Maximum   LC 239
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an array of integers nums and a sliding window of size k, return an array
 *    of the maximum value in each window position.
 *
 *  EXAMPLE:
 *    nums=[1,3,-1,-3,5,3,6,7], k=3
 *    Output: [3,3,5,5,6,7]
 *
 *  CONSTRAINTS:
 *    1 <= nums.length (= N) <= 10^5
 *    -10^4 <= nums[i] <= 10^4
 *    1 <= k <= N
 *
 *  APPROACH 1: Monotonic DECREASING deque storing INDICES (ArrayDeque)
 *    Time:  O(N)  — each index enters and leaves deque exactly once
 *    Space: O(k)  — deque holds at most k indices
 *
 *  APPROACH 2: Array-backed int[] deque (primitive, zero GC overhead)
 *    Time:  O(N)
 *    Space: O(N)  — int[] of size N (deque never exceeds k but we allocate N)
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Sliding_Window_Maximum {

    // =========================================================
    // APPROACH 1 — ARRAYDEQUE MONOTONIC DECREASING DEQUE
    // =========================================================

    /**
     * Finds maximum in each sliding window using a monotonic decreasing deque.
     *
     * DEQUE INVARIANT (stores INDICES, values are DECREASING from front to back):
     *   deque.peekFirst() = INDEX of the current window maximum.
     *   deque.peekLast()  = INDEX of the most recently added element.
     *   All indices in deque are within the current window [currentIndex-k+1, currentIndex].
     *
     * TWO MAINTENANCE RULES per new element at index i:
     *   1. EVICT EXPIRED from FRONT:
     *      While deque.front <= i - k: deque.pollFirst()   // outside window
     *
     *   2. MAINTAIN MONOTONE from BACK:
     *      While deque not empty AND nums[deque.back] <= nums[i]:
     *        deque.pollLast()   // dominated — never be max while i is in window
     *
     *   3. ADD: deque.offerLast(i)
     *
     *   4. RECORD: if i >= k-1: result[i-k+1] = nums[deque.peekFirst()]
     *
     * WHY STORE INDICES NOT VALUES?
     *   Step 1 (expiry check) requires knowing the POSITION of the front element.
     *   Without position, we can't tell if it's still in the window.
     *
     * @param inputNums    array of integers
     * @param windowSize   size of the sliding window
     * @return array of window maximums
     *
     * Time:  O(N)  — each index offered/polled at most once
     * Space: O(k)  — deque holds at most k indices at any time
     */
    public int[] maxSlidingWindowDeque(int[] inputNums, int windowSize) {
        if (inputNums == null || inputNums.length == 0 || windowSize <= 0) return new int[0];

        int totalElements   = inputNums.length;
        int resultCount     = totalElements - windowSize + 1;
        int[] windowMaxResult = new int[resultCount];

        // Monotonic decreasing deque stores INDICES (front = max index)
        Deque<Integer> monotonicDeque = new ArrayDeque<>();

        for (int currentIndex = 0; currentIndex < totalElements; currentIndex++) {

            // RULE 1: Evict expired front elements (outside the current window)
            while (!monotonicDeque.isEmpty()
                   && monotonicDeque.peekFirst() <= currentIndex - windowSize) {
                monotonicDeque.pollFirst();
            }

            // RULE 2: Evict back elements that are DOMINATED by current element
            // (They are smaller AND older — can never be the max while current is in window)
            while (!monotonicDeque.isEmpty()
                   && inputNums[monotonicDeque.peekLast()] <= inputNums[currentIndex]) {
                monotonicDeque.pollLast();
            }

            // Add current index to back of deque
            monotonicDeque.offerLast(currentIndex);

            // Record result once we have a full window
            if (currentIndex >= windowSize - 1) {
                int resultIndex = currentIndex - windowSize + 1;
                windowMaxResult[resultIndex] = inputNums[monotonicDeque.peekFirst()];
            }
        }

        return windowMaxResult;
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED INT[] DEQUE (ZERO GC)
    // =========================================================

    /**
     * Same algorithm using raw int[] as the deque.
     * Linear (non-circular) deque — valid for single-pass problems where
     * deque content monotonically advances (head only moves right, tail also moves right).
     *
     * ARRAY DEQUE INVARIANT:
     *   dequeArray[dequeHead .. dequeTail] holds the current valid indices.
     *   dequeHead: points to front (current window maximum).
     *   dequeTail: points to last added element.
     *   dequeHead > dequeTail → deque is EMPTY.
     *
     * OPERATIONS:
     *   Front read:  dequeArray[dequeHead]
     *   Front evict: dequeHead++
     *   Back read:   dequeArray[dequeTail]
     *   Back evict:  dequeTail--
     *   Back add:    dequeArray[++dequeTail] = value
     *
     * @param inputNums   array of integers
     * @param windowSize  sliding window size
     * @return window maximums
     *
     * Time:  O(N)
     * Space: O(N)  — int[] of N (head/tail advance monotonically, never wrap)
     */
    public int[] maxSlidingWindowArray(int[] inputNums, int windowSize) {
        if (inputNums == null || inputNums.length == 0 || windowSize <= 0) return new int[0];

        int totalElements     = inputNums.length;
        int[] dequeArray      = new int[totalElements];   // stores indices
        int dequeHead         = 0;     // points to front element
        int dequeTail         = -1;    // points to last element (-1 = empty)
        int[] windowMaxResult = new int[totalElements - windowSize + 1];

        for (int currentIndex = 0; currentIndex < totalElements; currentIndex++) {

            // RULE 1: Evict expired front
            if (dequeHead <= dequeTail && dequeArray[dequeHead] <= currentIndex - windowSize) {
                dequeHead++;
            }

            // RULE 2: Evict dominated back elements
            while (dequeHead <= dequeTail
                   && inputNums[dequeArray[dequeTail]] <= inputNums[currentIndex]) {
                dequeTail--;
            }

            // Add current index to back
            dequeArray[++dequeTail] = currentIndex;

            // Record window maximum
            if (currentIndex >= windowSize - 1) {
                windowMaxResult[currentIndex - windowSize + 1] = inputNums[dequeArray[dequeHead]];
            }
        }

        return windowMaxResult;
    }

    /**
     * BONUS: Brute force O(N×K) for validation in tests.
     */
    private int[] maxSlidingWindowBruteForce(int[] nums, int k) {
        int n = nums.length;
        int[] result = new int[n - k + 1];
        for (int i = 0; i <= n - k; i++) {
            int max = nums[i];
            for (int j = i + 1; j < i + k; j++) max = Math.max(max, nums[j]);
            result[i] = max;
        }
        return result;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Sliding_Window_Maximum solver = new Problem_1_Basic_Sliding_Window_Maximum();

        System.out.println("========================================");
        System.out.println("  Sliding Window Maximum — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<int[], Integer> testAll = (nums, k) -> {
            int[] deque  = solver.maxSlidingWindowDeque(nums, k);
            int[] arr    = solver.maxSlidingWindowArray(nums, k);
            int[] brute  = solver.maxSlidingWindowBruteForce(nums, k);
            boolean ok   = Arrays.equals(deque, arr) && Arrays.equals(arr, brute);
            System.out.println("Input: " + Arrays.toString(nums) + "  k=" + k);
            System.out.println("Deque: " + Arrays.toString(deque));
            System.out.println("Array: " + Arrays.toString(arr));
            System.out.println("Brute: " + Arrays.toString(brute));
            System.out.println("All match: " + ok);
            System.out.println();
        };

        System.out.println("\n--- LC 239 Example ---");
        testAll.accept(new int[]{1,3,-1,-3,5,3,6,7}, 3);
        // Expected: [3,3,5,5,6,7]

        System.out.println("--- Window size = 1 ---");
        testAll.accept(new int[]{4,2,7,1}, 1);
        // Expected: [4,2,7,1]

        System.out.println("--- Window size = N ---");
        testAll.accept(new int[]{4,2,7,1,5}, 5);
        // Expected: [7]

        System.out.println("--- All same ---");
        testAll.accept(new int[]{3,3,3,3,3}, 3);
        // Expected: [3,3,3]

        System.out.println("--- Strictly decreasing ---");
        testAll.accept(new int[]{9,7,5,3,1}, 2);
        // Expected: [9,7,5,3]

        System.out.println("--- Strictly increasing ---");
        testAll.accept(new int[]{1,3,5,7,9}, 3);
        // Expected: [5,7,9]

        System.out.println("--- Single element ---");
        testAll.accept(new int[]{42}, 1);

        System.out.println("========================================");
        System.out.println("  All Sliding Window Maximum tests done.");
        System.out.println("========================================");
    }
}
