/**
 * ============================================================
 *  PATTERN 2 — 0/1 KNAPSACK DP
 *  Problem 1 (Basic): Classic 0/1 Knapsack
 * ============================================================
 *
 *  Difficulty  : Basic / Medium
 *  Pattern     : 0/1 Knapsack — dp[i][w] = best value, take/skip each item
 *  Classic CLRS Chapter 16 + GFG / LeetCode
 *
 *  PROBLEM STATEMENT:
 *    Given n items, each with weight wt[i] and value val[i], and a knapsack
 *    with capacity W. Pick items (each at most once) to maximize total value
 *    without exceeding weight capacity W.
 *
 *  EXAMPLES:
 *    wt=[2,3,4,5], val=[3,4,5,6], W=8 → 10 (items 0+1+? or 0+3: 3+6=9? Let's trace)
 *    wt=[1,3,4,5], val=[1,4,5,7], W=7 → 9 (items 1+2: wt=3+4=7, val=4+5=9)
 *
 * ============================================================
 */
import java.util.*;

class Problem1_Knapsack01 {

    // =========================================================
    // APPROACH 1 — FULL 2D DP (most instructive)
    // =========================================================
    /**
     * STATE: dp[i][w] = max value using first i items with capacity w
     *
     * TRANSITION:
     *   Skip item i: dp[i][w] = dp[i-1][w]
     *   Take item i: dp[i][w] = dp[i-1][w - wt[i-1]] + val[i-1]  (if wt[i-1] <= w)
     *   dp[i][w] = max(skip, take)
     *
     * BASE: dp[0][w] = 0 for all w (no items → zero value)
     *
     * Time: O(n × W), Space: O(n × W)
     */
    public int knapsack2D(int W, int[] wt, int[] val) {
        int n = wt.length;
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i-1][w];  // skip
                if (wt[i-1] <= w)
                    dp[i][w] = Math.max(dp[i][w], dp[i-1][w - wt[i-1]] + val[i-1]); // take
            }
        }

        return dp[n][W];
    }

    // =========================================================
    // APPROACH 2 — 1D SPACE-OPTIMIZED (BACKWARDS ITERATION)
    // =========================================================
    /**
     * Space: O(W) by collapsing the 2D table to 1D.
     * CRUCIAL: iterate w from W DOWN to wt[i] (backwards) to prevent item reuse.
     *
     * Time: O(n × W), Space: O(W)
     */
    public int knapsack1D(int W, int[] wt, int[] val) {
        int n = wt.length;
        int[] dp = new int[W + 1];

        for (int i = 0; i < n; i++) {
            for (int w = W; w >= wt[i]; w--) {      // ← BACKWARDS
                dp[w] = Math.max(dp[w], dp[w - wt[i]] + val[i]);
            }
        }

        return dp[W];
    }

    // =========================================================
    // APPROACH 3 — RECONSTRUCT ITEMS SELECTED
    // =========================================================
    /**
     * Use the 2D table to backtrack which items were selected.
     * From dp[n][W], if dp[n][W] != dp[n-1][W], item n was taken.
     */
    public int[] knapsackItems(int W, int[] wt, int[] val) {
        int n = wt.length;
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i-1][w];
                if (wt[i-1] <= w)
                    dp[i][w] = Math.max(dp[i][w], dp[i-1][w - wt[i-1]] + val[i-1]);
            }
        }

        // Backtrack
        List<Integer> selected = new ArrayList<>();
        int w = W;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i-1][w]) {
                selected.add(i - 1);       // item i-1 (0-indexed) was taken
                w -= wt[i-1];
            }
        }

        return selected.stream().mapToInt(Integer::intValue).toArray();
    }

    // =========================================================
    // APPROACH 4 — TOP-DOWN MEMOIZATION
    // =========================================================
    private int[][] memo4;

    public int knapsackMemo(int W, int[] wt, int[] val) {
        memo4 = new int[wt.length + 1][W + 1];
        for (int[] row : memo4) Arrays.fill(row, -1);
        return solve(wt.length, W, wt, val);
    }

    private int solve(int i, int w, int[] wt, int[] val) {
        if (i == 0 || w == 0) return 0;
        if (memo4[i][w] != -1) return memo4[i][w];

        int skip = solve(i - 1, w, wt, val);
        int take = (wt[i-1] <= w) ? solve(i-1, w - wt[i-1], wt, val) + val[i-1] : 0;
        return memo4[i][w] = Math.max(skip, take);
    }

    // =========================================================
    // BONUS: Print the full DP table
    // =========================================================
    public void printTable(int W, int[] wt, int[] val) {
        int n = wt.length;
        int[][] dp = new int[n + 1][W + 1];
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i-1][w];
                if (wt[i-1] <= w)
                    dp[i][w] = Math.max(dp[i][w], dp[i-1][w - wt[i-1]] + val[i-1]);
            }
        }

        System.out.printf("%-6s", "");
        for (int w = 0; w <= W; w++) System.out.printf("%-4d", w);
        System.out.println();
        for (int i = 0; i <= n; i++) {
            String label = (i == 0) ? "none" : "item" + i;
            System.out.printf("%-6s", label);
            for (int w = 0; w <= W; w++) System.out.printf("%-4d", dp[i][w]);
            System.out.println();
        }
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_Knapsack01 sol = new Problem1_Knapsack01();

        System.out.println("=== Classic 0/1 Knapsack ===");

        int W1 = 8;
        int[] wt1 = {2, 3, 4, 5};
        int[] val1 = {3, 4, 5, 6};
        System.out.println("W=8, wt=[2,3,4,5], val=[3,4,5,6]");
        System.out.println("  2D DP   : " + sol.knapsack2D(W1, wt1, val1));   // 10
        System.out.println("  1D DP   : " + sol.knapsack1D(W1, wt1, val1));   // 10
        System.out.println("  Memo    : " + sol.knapsackMemo(W1, wt1, val1));  // 10
        System.out.println("  Items   : " + Arrays.toString(sol.knapsackItems(W1, wt1, val1)));
        System.out.println("\n  DP Table:");
        sol.printTable(W1, wt1, val1);

        System.out.println("\n---");
        int W2 = 7;
        int[] wt2 = {1, 3, 4, 5};
        int[] val2 = {1, 4, 5, 7};
        System.out.println("W=7, wt=[1,3,4,5], val=[1,4,5,7]");
        System.out.println("  2D DP   : " + sol.knapsack2D(W2, wt2, val2));   // 9
        System.out.println("  1D DP   : " + sol.knapsack1D(W2, wt2, val2));   // 9
        System.out.println("  Items   : " + Arrays.toString(sol.knapsackItems(W2, wt2, val2)));
    }
}
