/**
 * ============================================================
 *  PATTERN 4 — MONOTONIC DEQUES AND SLIDING WINDOWS
 *  Problem 2 (Medium): Longest Continuous Subarray   LC 1438
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an array of integers nums and a limit value, return the size of the longest
 *    non-empty contiguous subarray such that the absolute difference between any two
 *    elements is <= limit.
 *
 *  EXAMPLE:
 *    nums=[8,2,4,7], limit=4  → 2  (subarray [2,4] or [4,7])
 *    nums=[10,1,2,4,7,2], limit=5  → 4  (subarray [2,4,7,2])
 *
 *  CONSTRAINTS:
 *    1 <= nums.length (= N) <= 10^5
 *    1 <= nums[i] <= 10^9
 *    0 <= limit <= 10^9
 *
 *  KEY INSIGHT:
 *    For any subarray [left, right] to be valid:
 *      max(nums[left..right]) - min(nums[left..right]) <= limit
 *    Use TWO monotonic deques simultaneously:
 *      maxDeque: monotonic DECREASING (front = current window max)
 *      minDeque: monotonic INCREASING (front = current window min)
 *    Use sliding window (right expands; left shrinks when constraint violated).
 *
 *  APPROACH 1: Two ArrayDeque monotonic deques + two-pointer window
 *    Time:  O(N)
 *    Space: O(N)  — both deques hold at most N indices
 *
 *  APPROACH 2: Two array-backed int[] deques (primitive, zero GC)
 *    Time:  O(N)
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Longest_Continuous_Subarray {

    // =========================================================
    // APPROACH 1 — DUAL ARRAYDEQUE MONOTONIC DEQUES
    // =========================================================

    /**
     * Finds the longest subarray where max-min <= limit.
     *
     * WINDOW MANAGEMENT:
     *   rightPointer expands: always include nums[right] in the window.
     *   leftPointer shrinks: when max-min > limit, advance left until constraint satisfied.
     *
     *   When left advances:
     *     Evict leftPointer from BOTH deques if it's their front
     *     (it may or may not be the current max/min front).
     *
     * DUAL DEQUE INVARIANT:
     *   maxDeque.front: index of current window MAXIMUM (decreasing deque by value).
     *   minDeque.front: index of current window MINIMUM (increasing deque by value).
     *   Both deques only hold indices within [leftPointer, rightPointer].
     *
     * @param nums  integer array
     * @param limit maximum allowed absolute difference within the window
     * @return length of the longest valid subarray
     *
     * Time:  O(N) — each index enters/leaves each deque at most once
     * Space: O(N) — each deque holds at most N indices
     */
    public int longestSubarrayDeque(int[] nums, int limit) {
        if (nums == null || nums.length == 0) return 0;

        int totalElements = nums.length;
        int longestLength = 0;
        int leftPointer   = 0;

        // maxDeque: monotonic DECREASING (front = max of current window)
        Deque<Integer> maxDeque = new ArrayDeque<>();
        // minDeque: monotonic INCREASING (front = min of current window)
        Deque<Integer> minDeque = new ArrayDeque<>();

        for (int rightPointer = 0; rightPointer < totalElements; rightPointer++) {
            int currentValue = nums[rightPointer];

            // Maintain max deque: remove back elements smaller than current
            while (!maxDeque.isEmpty() && nums[maxDeque.peekLast()] <= currentValue) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(rightPointer);

            // Maintain min deque: remove back elements larger than current
            while (!minDeque.isEmpty() && nums[minDeque.peekLast()] >= currentValue) {
                minDeque.pollLast();
            }
            minDeque.offerLast(rightPointer);

            // Shrink window from left while constraint violated
            while (nums[maxDeque.peekFirst()] - nums[minDeque.peekFirst()] > limit) {
                leftPointer++;
                // Evict expired fronts
                if (maxDeque.peekFirst() < leftPointer) maxDeque.pollFirst();
                if (minDeque.peekFirst() < leftPointer) minDeque.pollFirst();
            }

            longestLength = Math.max(longestLength, rightPointer - leftPointer + 1);
        }

        return longestLength;
    }

    // =========================================================
    // APPROACH 2 — DUAL ARRAY-BACKED INT[] DEQUES
    // =========================================================

    /**
     * Same algorithm with raw int[] arrays for both deques.
     * Zero GC overhead — suitable for high-throughput scenarios.
     *
     * ARRAY DEQUE STRUCTURE:
     *   maxDequeArr[maxHead .. maxTail]: indices, values in decreasing order
     *   minDequeArr[minHead .. minTail]: indices, values in increasing order
     *   head > tail → deque is empty.
     *
     * Time:  O(N)
     * Space: O(N) — two int[] of size N (primitives, no boxing)
     */
    public int longestSubarrayArray(int[] nums, int limit) {
        if (nums == null || nums.length == 0) return 0;

        int totalElements = nums.length;
        int longestLength = 0;
        int leftPointer   = 0;

        int[] maxDequeArr = new int[totalElements];
        int[] minDequeArr = new int[totalElements];
        int maxHead = 0, maxTail = -1;   // max deque (decreasing)
        int minHead = 0, minTail = -1;   // min deque (increasing)

        for (int rightPointer = 0; rightPointer < totalElements; rightPointer++) {
            int currentValue = nums[rightPointer];

            // Maintain max deque (decreasing by value)
            while (maxHead <= maxTail && nums[maxDequeArr[maxTail]] <= currentValue) {
                maxTail--;   // remove dominated back element
            }
            maxDequeArr[++maxTail] = rightPointer;

            // Maintain min deque (increasing by value)
            while (minHead <= minTail && nums[minDequeArr[minTail]] >= currentValue) {
                minTail--;
            }
            minDequeArr[++minTail] = rightPointer;

            // Shrink window from left
            while (nums[maxDequeArr[maxHead]] - nums[minDequeArr[minHead]] > limit) {
                leftPointer++;
                if (maxDequeArr[maxHead] < leftPointer) maxHead++;
                if (minDequeArr[minHead] < leftPointer) minHead++;
            }

            longestLength = Math.max(longestLength, rightPointer - leftPointer + 1);
        }

        return longestLength;
    }

    /** Brute force O(N²) for test validation */
    private int longestSubarrayBrute(int[] nums, int limit) {
        int best = 0;
        for (int i = 0; i < nums.length; i++) {
            int max = nums[i], min = nums[i];
            for (int j = i; j < nums.length; j++) {
                max = Math.max(max, nums[j]);
                min = Math.min(min, nums[j]);
                if (max - min <= limit) best = Math.max(best, j - i + 1);
                else break;
            }
        }
        return best;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Longest_Continuous_Subarray solver =
            new Problem_2_Medium_Longest_Continuous_Subarray();

        System.out.println("========================================");
        System.out.println("  Longest Continuous Subarray — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<int[], Integer> testAll = (nums, limit) -> {
            int d = solver.longestSubarrayDeque(nums, limit);
            int a = solver.longestSubarrayArray(nums, limit);
            int b = solver.longestSubarrayBrute(nums, limit);
            boolean ok = (d == a && a == b);
            System.out.printf("nums=%-30s limit=%-4d | Deque=%d Array=%d Brute=%d %s%n",
                Arrays.toString(nums), limit, d, a, b, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 1438 examples ---");
        testAll.accept(new int[]{8,2,4,7}, 4);          // expected 2
        testAll.accept(new int[]{10,1,2,4,7,2}, 5);      // expected 4
        testAll.accept(new int[]{4,2,2,2,4,4,2,2}, 0);   // expected 3

        System.out.println("\n--- Edge cases ---");
        testAll.accept(new int[]{1}, 0);                   // single element
        testAll.accept(new int[]{1,1,1,1}, 0);             // all same
        testAll.accept(new int[]{1,100}, 200);             // entire array valid

        System.out.println("\n--- Large limit (all valid) ---");
        testAll.accept(new int[]{3,1,4,1,5,9,2,6}, 1000); // entire array

        System.out.println("\n--- Zero limit ---");
        testAll.accept(new int[]{1,2,3}, 0);               // only single elements

        System.out.println("\n--- Alternating ---");
        testAll.accept(new int[]{1,5,1,5,1}, 4);           // expected 2

        System.out.println("\n========================================");
        System.out.println("  All Longest Subarray tests done.");
        System.out.println("========================================");
    }
}
