/**
 * ============================================================
 *  PATTERN 4 — MONOTONIC DEQUES AND SLIDING WINDOWS
 *  Problem 3 (Hard): Constrained Subsequence Sum   LC 1425
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array nums and an integer k, return the maximum sum of a
 *    NON-EMPTY subsequence of nums such that for every two consecutive integers
 *    in the subsequence, nums[i] and nums[j] (i < j), j - i <= k.
 *    A subsequence is derived by deleting some elements without changing order.
 *
 *  EXAMPLE:
 *    nums=[10,2,-10,5,20], k=2  → 37  (10+2+5+20)
 *    nums=[-1,-2,-3], k=1       → -1  (just the largest element)
 *
 *  CONSTRAINTS:
 *    1 <= nums.length (= N) <= 10^5
 *    -10^4 <= nums[i] <= 10^4
 *    1 <= k <= N
 *
 *  DP RECURRENCE:
 *    dp[i] = maximum sum of any valid subsequence ENDING at index i.
 *    dp[i] = nums[i] + max(0, max(dp[i-k], dp[i-k+1], ..., dp[i-1]))
 *          = nums[i] + max(0, maxDpInWindow(i-k to i-1))
 *    Answer = max(dp[0], dp[1], ..., dp[N-1])
 *
 *    The "max dp in a window of size k" query is solved by a MONOTONIC DECREASING DEQUE.
 *
 *  APPROACH 1: DP + monotonic decreasing deque (ArrayDeque)
 *    Time:  O(N)
 *    Space: O(N)  — dp[] + deque
 *
 *  APPROACH 2: DP + array-backed deque (primitive, zero GC)
 *    Time:  O(N)
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Constrained_Subsequence_Sum {

    // =========================================================
    // APPROACH 1 — DP + ARRAYDEQUE MONOTONIC DEQUE
    // =========================================================

    /**
     * Finds the maximum constrained subsequence sum.
     *
     * DP STATE: dp[i] = best subsequence sum ending exactly at index i.
     *
     * RECURRENCE DERIVATION:
     *   We can jump to index i from any index j in [max(0, i-k), i-1].
     *   If the best sum ending at j is positive, include j in our path → extend.
     *   If all past values are negative, start fresh at i.
     *   dp[i] = nums[i] + max(0, max(dp[j] for j in [i-k, i-1]))
     *
     * DEQUE USAGE:
     *   The deque maintains a monotonic DECREASING window of dp values.
     *   After computing dp[i], add dp[i]'s index to the deque.
     *   Before using the deque max for dp[i], evict indices outside window [i-k, i-1].
     *
     * @param nums input array
     * @param k    maximum allowed gap between consecutive elements
     * @return maximum valid subsequence sum
     *
     * Time:  O(N) — each index enters/leaves deque at most once
     * Space: O(N) — dp array + deque
     */
    public int constrainedSubsetSumDeque(int[] nums, int k) {
        if (nums == null || nums.length == 0) return 0;

        int totalElements = nums.length;
        int[] dp = new int[totalElements];
        int globalMaxSum = Integer.MIN_VALUE;

        // Monotonic decreasing deque of INDICES — front holds index of max dp in window
        Deque<Integer> maxDpDeque = new ArrayDeque<>();

        for (int currentIndex = 0; currentIndex < totalElements; currentIndex++) {

            // RULE 1: Evict expired front indices (outside window [currentIndex-k, currentIndex-1])
            while (!maxDpDeque.isEmpty()
                   && maxDpDeque.peekFirst() < currentIndex - k) {
                maxDpDeque.pollFirst();
            }

            // dp[currentIndex] = nums[currentIndex] + max(0, best dp in window)
            int bestWindowDp = maxDpDeque.isEmpty() ? 0 : Math.max(0, dp[maxDpDeque.peekFirst()]);
            dp[currentIndex] = nums[currentIndex] + bestWindowDp;

            globalMaxSum = Math.max(globalMaxSum, dp[currentIndex]);

            // RULE 2: Maintain monotonic deque — remove back indices with dp <= dp[currentIndex]
            while (!maxDpDeque.isEmpty() && dp[maxDpDeque.peekLast()] <= dp[currentIndex]) {
                maxDpDeque.pollLast();
            }

            // Add current index to deque
            maxDpDeque.offerLast(currentIndex);
        }

        return globalMaxSum;
    }

    // =========================================================
    // APPROACH 2 — DP + ARRAY-BACKED INT[] DEQUE
    // =========================================================

    /**
     * Same DP recurrence with a raw int[] deque.
     *
     * NOTE ON DEQUE INDEX ORDER:
     *   We add dp[i] to the deque AFTER computing dp[i], so when processing dp[i+1],
     *   the window correctly contains dp[i-k+1 .. i] (the last k values of dp before i+1).
     *   The expiry condition checks: dequeArr[dequeHead] < currentIndex - k
     *   (i.e., the front was computed more than k positions ago).
     *
     * Time:  O(N)
     * Space: O(N) — dp[] + int[] deque (all primitives)
     */
    public int constrainedSubsetSumArray(int[] nums, int k) {
        if (nums == null || nums.length == 0) return 0;

        int totalElements = nums.length;
        int[] dp          = new int[totalElements];
        int[] dequeArr    = new int[totalElements];
        int dequeHead     = 0;
        int dequeTail     = -1;
        int globalMaxSum  = Integer.MIN_VALUE;

        for (int currentIndex = 0; currentIndex < totalElements; currentIndex++) {

            // Evict expired front
            if (dequeHead <= dequeTail && dequeArr[dequeHead] < currentIndex - k) {
                dequeHead++;
            }

            // Compute dp[currentIndex]
            int bestWindowDp = (dequeHead <= dequeTail)
                ? Math.max(0, dp[dequeArr[dequeHead]])
                : 0;
            dp[currentIndex] = nums[currentIndex] + bestWindowDp;

            globalMaxSum = Math.max(globalMaxSum, dp[currentIndex]);

            // Maintain monotonic deque (decreasing dp values)
            while (dequeHead <= dequeTail && dp[dequeArr[dequeTail]] <= dp[currentIndex]) {
                dequeTail--;
            }
            dequeArr[++dequeTail] = currentIndex;
        }

        return globalMaxSum;
    }

    /**
     * BONUS: O(N × K) DP without deque — for clarity and brute-force validation.
     */
    private int constrainedSubsetSumBrute(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        int globalMax = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            dp[i] = nums[i];
            for (int j = Math.max(0, i - k); j < i; j++) {
                if (dp[j] > 0) dp[i] = Math.max(dp[i], dp[j] + nums[i]);
            }
            globalMax = Math.max(globalMax, dp[i]);
        }
        return globalMax;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Constrained_Subsequence_Sum solver =
            new Problem_3_Hard_Constrained_Subsequence_Sum();

        System.out.println("========================================");
        System.out.println("  Constrained Subsequence Sum — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<int[], Integer> testAll = (nums, k) -> {
            int d = solver.constrainedSubsetSumDeque(nums, k);
            int a = solver.constrainedSubsetSumArray(nums, k);
            int b = solver.constrainedSubsetSumBrute(nums, k);
            boolean ok = (d == a && a == b);
            System.out.printf("nums=%-30s k=%-2d | Deque=%d Array=%d Brute=%d %s%n",
                Arrays.toString(nums), k, d, a, b, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 1425 examples ---");
        testAll.accept(new int[]{10,2,-10,5,20}, 2);     // expected 37
        testAll.accept(new int[]{-1,-2,-3}, 1);           // expected -1
        testAll.accept(new int[]{10,-2,-10,-5,20}, 2);    // expected 23

        System.out.println("\n--- Edge cases ---");
        testAll.accept(new int[]{5}, 1);                  // single element
        testAll.accept(new int[]{1,1,1,1,1}, 1);          // consecutive k=1
        testAll.accept(new int[]{-5,-3,-1}, 2);           // all negative

        System.out.println("\n--- k = N (can take any pair) ---");
        testAll.accept(new int[]{3,-2,4,-1,5}, 5);        // take 3+4+5=12

        System.out.println("\n--- Alternating positive/negative ---");
        testAll.accept(new int[]{5,-3,5,-3,5}, 2);        // 5+5+5=15

        System.out.println("\n--- Large positive values ---");
        testAll.accept(new int[]{10000,10000,10000}, 1);  // 30000

        System.out.println("\n========================================");
        System.out.println("  All Constrained Subsequence Sum tests done.");
        System.out.println("========================================");
    }
}
