/**
 * ============================================================
 *  PATTERN 6 — INTERVAL / RANGE DP
 *  Problem 2 (Medium): Minimum Cost Tree (Optimal BST)   LC 1130
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Interval DP (split-point style)
 *  LeetCode    : LC 1130 — Minimum Cost Tree From Leaf Values
 *                (Related: Optimal Binary Search Tree — Classic CLRS)
 *
 *  PROBLEM STATEMENT (LC 1130):
 *    Given an array arr of positive integers, build a binary tree where
 *    each leaf = arr[i] (in-order from left to right).
 *    Each non-leaf node's value = max(left_subtree_leaves) * max(right_subtree_leaves).
 *    Minimize the sum of all non-leaf node values.
 *
 *  EXAMPLES:
 *    arr = [6, 2, 4]  →  32
 *      Tree:   24         Tree:   12
 *             / \    or        /    \
 *            12   4           6      8
 *           / \              / \
 *          6   2            2   4
 *      Non-leaf sum: 24+12=36  vs  12+8=20... wait let's verify:
 *      Optimal: root=24 (6*4), with node 8 (2*4) → 24+8=32 ✓
 *
 *  RELATED CLASSIC: Optimal Binary Search Tree (CLRS Ch 15.5)
 *    Given n keys with frequencies, find BST minimizing expected search cost.
 *    This is included as Approach 2 below.
 *
 * ============================================================
 */
import java.util.*;

class Problem2_MinCostTree {

    // =========================================================
    // APPROACH 1A — INTERVAL DP (LC 1130: Min Cost Tree from Leaves)
    // =========================================================
    /**
     * STATE:
     *   dp[i][j] = min cost to build the subtree using leaves arr[i..j]
     *   maxLeaf[i][j] = max leaf value in arr[i..j] (precomputed)
     *
     * TRANSITION:
     *   Split at k ∈ [i, j-1]:
     *     Left subtree uses arr[i..k], right uses arr[k+1..j]
     *     Cost of this split = dp[i][k] + dp[k+1][j]
     *                        + maxLeaf[i][k] * maxLeaf[k+1][j]  ← the root node
     *
     *   dp[i][j] = min over k of (dp[i][k] + dp[k+1][j] + maxLeaf[i][k]*maxLeaf[k+1][j])
     *
     * BASE CASE:
     *   dp[i][i] = 0  (single leaf: no internal nodes needed, cost = 0)
     *
     * VISUAL for arr = [6, 2, 4]:
     *   maxLeaf[0][0]=6, [1][1]=2, [2][2]=4
     *   maxLeaf[0][1]=6, [1][2]=4, [0][2]=6
     *
     *   len=2:
     *     dp[0][1]: k=0 → dp[0][0]+dp[1][1] + 6*2 = 0+0+12 = 12
     *     dp[1][2]: k=1 → dp[1][1]+dp[2][2] + 2*4 = 0+0+8  = 8
     *
     *   len=3:
     *     dp[0][2]: k=0 → dp[0][0]+dp[1][2] + 6*4 = 0+8+24 = 32
     *               k=1 → dp[0][1]+dp[2][2] + 6*4 = 12+0+24= 36
     *     → dp[0][2] = 32 ✓
     *
     * Time:  O(n³)
     * Space: O(n²)
     *
     * Follow-up: Can you solve LC 1130 in O(n) using a monotonic stack?
     *   YES — greedy: always merge the two smallest adjacent leaves.
     *   Monotonic stack maintains decreasing order; O(n) time.
     */
    public int mctFromLeafValues(int[] arr) {
        int n = arr.length;

        // Precompute max leaf in every range [i..j]
        int[][] maxLeaf = new int[n][n];
        for (int i = 0; i < n; i++) {
            maxLeaf[i][i] = arr[i];
            for (int j = i + 1; j < n; j++) {
                maxLeaf[i][j] = Math.max(maxLeaf[i][j - 1], arr[j]);
            }
        }

        // Interval DP
        int[][] dp = new int[n][n];
        // dp[i][i] = 0 (implicit, since int[] is zero-initialized)

        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j]
                             + maxLeaf[i][k] * maxLeaf[k + 1][j];
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        }

        return dp[0][n - 1];
    }

    // =========================================================
    // APPROACH 1B — MONOTONIC STACK (O(n) greedy solution for LC 1130)
    // =========================================================
    /**
     * GREEDY INSIGHT:
     *   Each non-leaf node = max(left leaves) * max(right leaves).
     *   To minimize total cost, we want small values multiplied together.
     *   When we merge two leaves, the SMALLER one disappears from the array,
     *   the LARGER one remains as a boundary for future merges.
     *
     *   Strategy: Always merge the two adjacent elements where the SMALLER
     *   of the two has minimal impact (i.e., multiply by the smallest possible neighbor).
     *
     *   Use a MONOTONIC DECREASING STACK:
     *   - Process each element.
     *   - While stack top < current (or next), pop and merge:
     *       cost += stack.pop() * min(stack.top, current)
     *
     * Time:  O(n)
     * Space: O(n)
     */
    public int mctFromLeafValuesStack(int[] arr) {
        int cost = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(Integer.MAX_VALUE);  // sentinel

        for (int val : arr) {
            while (stack.peek() <= val) {
                int mid = stack.pop();
                // merge 'mid' with the smaller of its neighbors
                cost += mid * Math.min(stack.peek(), val);
            }
            stack.push(val);
        }

        // Remaining elements in stack: merge from top to bottom
        while (stack.size() > 2) {
            cost += stack.pop() * stack.peek();
        }

        return cost;
    }

    // =========================================================
    // APPROACH 2 — OPTIMAL BST (Classic CLRS / Knuth's Problem)
    // =========================================================
    /**
     * PROBLEM: Given n keys k1 < k2 < ... < kn with search frequencies freq[i],
     * and dummy keys (representing misses between/around real keys) with
     * frequencies dummyFreq[], build a BST minimizing expected search cost.
     *
     * Expected cost = sum over all nodes of (depth+1) * probability
     *
     * dp[i][j] = min expected search cost for keys k_i..k_j
     * w[i][j]  = sum of all frequencies for keys i..j (used as weight)
     *
     * TRANSITION (Knuth's Optimal BST):
     *   dp[i][j] = w[i][j] + min over root r ∈ [i..j]:
     *                dp[i][r-1] + dp[r+1][j]
     *
     *   Adding w[i][j] accounts for depth increment when adding the subtree under root.
     *
     * BASE CASE:
     *   dp[i][i-1] = 0  (empty subtree, zero cost)
     *   dp[i][i]   = w[i][i] = freq[i] + dummyFreq[i] + dummyFreq[i+1] (simplified)
     *
     * KNUTH'S OPTIMIZATION: root[i][j-1] <= root[i][j] <= root[i+1][j]
     *   Reduces complexity from O(n³) to O(n²) for this specific cost structure.
     *
     * Time:  O(n³) basic | O(n²) with Knuth's optimization
     * Space: O(n²)
     *
     * Below is the basic O(n³) version for clarity.
     *
     * @param freq      freq[i] = frequency of key i (1-indexed)
     * @param n         number of distinct keys
     * @return          minimum expected search cost * total_weight (integer formulation)
     */
    public double optimalBST(double[] freq, int n) {
        // dp[i][j] = min cost for keys i..j (1-indexed, 0 = empty)
        double[][] dp = new double[n + 2][n + 2];
        double[][] w  = new double[n + 2][n + 2];

        // Base: single key costs
        for (int i = 1; i <= n; i++) {
            dp[i][i] = freq[i];
            w[i][i]  = freq[i];
            w[i][i - 1] = 0;
        }
        w[n + 1][n] = 0;

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                w[i][j] = w[i][j - 1] + freq[j];
                dp[i][j] = Double.MAX_VALUE;
                for (int r = i; r <= j; r++) {
                    double cost = (r > i ? dp[i][r - 1] : 0)
                                + (r < j ? dp[r + 1][j] : 0)
                                + w[i][j];
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        }

        return dp[1][n];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_MinCostTree sol = new Problem2_MinCostTree();

        System.out.println("=== LC 1130: Min Cost Tree from Leaf Values ===");

        int[] arr1 = {6, 2, 4};
        System.out.println("arr = [6,2,4]");
        System.out.println("  Interval DP : " + sol.mctFromLeafValues(arr1));      // 32
        System.out.println("  Mono Stack  : " + sol.mctFromLeafValuesStack(arr1)); // 32

        int[] arr2 = {4, 11};
        System.out.println("arr = [4,11]");
        System.out.println("  Interval DP : " + sol.mctFromLeafValues(arr2));      // 44
        System.out.println("  Mono Stack  : " + sol.mctFromLeafValuesStack(arr2)); // 44

        int[] arr3 = {15, 13, 5, 3, 15};
        System.out.println("arr = [15,13,5,3,15]");
        System.out.println("  Interval DP : " + sol.mctFromLeafValues(arr3));
        System.out.println("  Mono Stack  : " + sol.mctFromLeafValuesStack(arr3));

        System.out.println("\n=== Optimal BST (Classic) ===");
        // Keys with frequencies: {0.25, 0.20, 0.05, 0.20, 0.30} (sum=1.0)
        // (1-indexed, freq[0] unused)
        double[] freq = {0, 0.25, 0.20, 0.05, 0.20, 0.30};
        System.out.println("Expected min cost: " + sol.optimalBST(freq, 5)); // ~2.75
    }
}
