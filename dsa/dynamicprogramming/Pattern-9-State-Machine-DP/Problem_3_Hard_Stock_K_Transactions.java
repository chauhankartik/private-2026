/**
 * ============================================================
 *  PATTERN 9 — STATE MACHINE DP
 *  Problem 3 (Hard): Best Time to Buy/Sell Stock — K Transactions   LC 188
 * ============================================================
 *
 *  Difficulty  : Hard
 *  Pattern     : State Machine DP — 3D: dp[day][k][hold/free]
 *  LeetCode    : https://leetcode.com/problems/best-time-to-buy-and-sell-stock-iv/
 *                (LC 123 = at most 2, LC 188 = at most k)
 *
 *  PROBLEM STATEMENT:
 *    Given prices[] and integer k, find the maximum profit using at most k transactions.
 *    A transaction = 1 buy + 1 sell. Must sell before buying again.
 *
 *  EXAMPLES:
 *    k=2, prices=[3,2,6,5,0,3]  → 7 (buy@2,sell@6 + buy@0,sell@3)
 *    k=2, prices=[2,4,1]        → 2 (buy@2,sell@4)
 *    k=1, prices=[7,1,5,3,6,4]  → 5 (buy@1,sell@6)
 *
 *  TRANSITION COUNT CONVENTION:
 *    "Transaction" = 1 buy + 1 sell.
 *    We DECREMENT k on BUY (not sell), so after k buys, no more purchases allowed.
 *    Equivalently: dp[day][k][1] uses dp[day-1][k-1][0] (k-1 remaining after this buy).
 *
 * ============================================================
 */
import java.util.*;

class Problem3_StockKTransactions {

    // =========================================================
    // APPROACH 1 — FULL 3D DP: dp[i][k][0/1]
    // =========================================================
    /**
     * STATE:
     *   dp[i][t][0] = max profit on day i, with at most t transactions remaining,
     *                 NOT currently holding a stock
     *   dp[i][t][1] = max profit on day i, with at most t transactions remaining,
     *                 CURRENTLY HOLDING a stock
     *
     * TRANSITION:
     *   dp[i][t][0] = max(dp[i-1][t][0],              // rest (don't sell)
     *                     dp[i-1][t][1] + prices[i])   // sell: gain prices[i]
     *
     *   dp[i][t][1] = max(dp[i-1][t][1],              // rest (keep holding)
     *                     dp[i-1][t-1][0] - prices[i]) // buy: costs prices[i], use 1 tx
     *                                                   // (t-1 remaining after this buy)
     *
     * BASE CASE:
     *   dp[0][t][0] = 0          ∀ t  (day 0, not holding: 0 profit)
     *   dp[0][t][1] = -prices[0] ∀ t  (day 0, holding: bought at prices[0])
     *   dp[i][0][0] = 0          ∀ i  (0 transactions left, not holding: 0 profit)
     *   dp[i][0][1] = -infinity  ∀ i  (0 transactions left, can't hold)
     *
     * ANSWER: dp[n-1][K][0]
     *   (Maximum profit on last day with at most K transactions, not holding)
     *
     * SPECIAL CASE: If 2*K >= n (can trade every day):
     *   Reduce to unlimited transactions (sum all positive daily diffs).
     *   This avoids O(n*K) blowup when K is huge.
     *
     * VISUAL for k=2, [3,2,6,5,0,3]:
     *   (abridged — see full table in maxProfitTable method)
     *   Key states:
     *     Best first transaction: buy@2, sell@6 → +4
     *     Best second transaction: buy@0, sell@3 → +3
     *     Total = 7 ✓
     *
     * Time:  O(n × K)
     * Space: O(n × K) → reducible to O(K) with rolling array
     *
     * Follow-up: What is the time complexity when K >= n/2?
     *   Reduced to O(n) via greedy (unlimited transactions).
     * Follow-up: At most 2 transactions (LC 123)?
     *   Hardcode K=2. The 5-variable O(1) solution is cleaner (shown below).
     */
    public int maxProfit(int k, int[] prices) {
        int n = prices.length;
        if (n == 0 || k == 0) return 0;

        // Optimization: if k >= n/2, unlimited transactions
        if (k >= n / 2) return maxProfitUnlimited(prices);

        // dp[t][0] = max profit with t transactions left, not holding
        // dp[t][1] = max profit with t transactions left, holding
        int[][] dp = new int[k + 1][2];
        // Initialize holding states to -infinity
        for (int[] row : dp) row[1] = Integer.MIN_VALUE / 2;

        // Buy on day 0: dp[t][1] = -prices[0] for all t
        for (int t = 1; t <= k; t++) dp[t][1] = -prices[0];

        for (int i = 1; i < n; i++) {
            // Iterate transactions from k down to 1 to use previous day's values
            // (or use a copy — here we use the standard 2D approach with separate prev)
            int[][] prev = new int[k + 1][2];
            for (int t = 0; t <= k; t++) {
                prev[t][0] = dp[t][0];
                prev[t][1] = dp[t][1];
            }

            for (int t = 1; t <= k; t++) {
                // Not holding: rest or sell
                dp[t][0] = Math.max(prev[t][0], prev[t][1] + prices[i]);
                // Holding: rest or buy (uses one transaction)
                dp[t][1] = Math.max(prev[t][1], prev[t-1][0] - prices[i]);
            }
        }

        return dp[k][0];
    }

    // =========================================================
    // APPROACH 2 — SPACE-OPTIMIZED WITH CAREFUL ORDERING
    // =========================================================
    /**
     * Use two 1D arrays (hold[], free[]) indexed by transaction count.
     *
     * hold[t] = max profit with t transactions LEFT, currently HOLDING
     * free[t] = max profit with t transactions LEFT, NOT holding
     *
     * Key: iterate t from k down to 1 to ensure we don't use "current day" values
     * when we need "previous day" values.
     *
     * Actually, because free[t] depends on hold[t] (same t), and hold[t] depends on
     * free[t-1] (one fewer transaction), we can iterate t correctly:
     *   - update hold[t] first (uses free[t-1] from previous day's value)
     *   - update free[t] second (uses hold[t] from current day)
     *
     * If we iterate t from k DOWN to 1:
     *   hold[t] uses free[t-1] which hasn't been updated for day i yet → CORRECT
     *   free[t] uses hold[t] which WAS updated for day i → CORRECT (we sell today)
     *
     * Time:  O(n × K), Space: O(K)
     */
    public int maxProfitSpaceOpt(int k, int[] prices) {
        int n = prices.length;
        if (n == 0 || k == 0) return 0;
        if (k >= n / 2) return maxProfitUnlimited(prices);

        int[] hold = new int[k + 1];
        int[] free = new int[k + 1];
        Arrays.fill(hold, Integer.MIN_VALUE / 2);
        // On day 0, we can buy (if t >= 1): hold[t] = -prices[0]
        for (int t = 1; t <= k; t++) hold[t] = -prices[0];

        for (int i = 1; i < n; i++) {
            for (int t = k; t >= 1; t--) {  // iterate from k down to 1
                // sell: free[t] = max(stay free, sell from hold[t])
                free[t] = Math.max(free[t], hold[t] + prices[i]);
                // buy: hold[t] = max(stay holding, buy using one transaction from free[t-1])
                hold[t] = Math.max(hold[t], free[t-1] - prices[i]);
            }
        }

        return free[k];
    }

    // =========================================================
    // APPROACH 3 — AT MOST 2 TRANSACTIONS (LC 123) — O(n), O(1) space
    // =========================================================
    /**
     * For K=2, manually track 5 states:
     *   buy1  = best profit after the 1st buy  (= -min price so far)
     *   sell1 = best profit after the 1st sell
     *   buy2  = best profit after the 2nd buy  (= sell1 - current price)
     *   sell2 = best profit after the 2nd sell
     *
     * Think of it as: each state depends on the previous state + today's price.
     *
     * TRANSITION:
     *   buy1  = max(buy1,  -prices[i])         // buy for the 1st time
     *   sell1 = max(sell1, buy1 + prices[i])   // sell the 1st stock
     *   buy2  = max(buy2,  sell1 - prices[i])  // buy for the 2nd time (reinvest sell1)
     *   sell2 = max(sell2, buy2 + prices[i])   // sell the 2nd stock
     *
     * Note: buy1 update must use PREVIOUS day's buy1 (no overlap issue here since
     * each var is independent). We can update in-place because:
     *   - buy1 uses only prices[i] (no previous sell1)
     *   - sell1 uses the CURRENT day's buy1 (buy today + sell today = 0 → allowed
     *     since they're on the SAME day; the max would just not change)
     *
     * Time:  O(n), Space: O(1)
     *
     * Follow-up: Why does this extend to K=3 or K=4?
     *   Add more buy/sell variables: buy1, sell1, buy2, sell2, buy3, sell3, ...
     *   The pattern repeats. For arbitrary K: use the K-dimensional DP.
     */
    public int maxProfitTwoTransactions(int[] prices) {
        int buy1  = Integer.MIN_VALUE;  // best after 1st buy
        int sell1 = 0;                  // best after 1st sell
        int buy2  = Integer.MIN_VALUE;  // best after 2nd buy
        int sell2 = 0;                  // best after 2nd sell

        for (int price : prices) {
            buy1  = Math.max(buy1,  -price);           // buy 1st time
            sell1 = Math.max(sell1, buy1 + price);     // sell 1st time
            buy2  = Math.max(buy2,  sell1 - price);    // buy 2nd time
            sell2 = Math.max(sell2, buy2 + price);     // sell 2nd time
        }

        return sell2;
    }

    // =========================================================
    // HELPER: Unlimited transactions (greedy sum of positive diffs)
    // =========================================================
    private int maxProfitUnlimited(int[] prices) {
        int profit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i-1])
                profit += prices[i] - prices[i-1];
        }
        return profit;
    }

    // =========================================================
    // APPROACH 4 — VISUAL DP TABLE PRINTER
    // =========================================================
    /**
     * Prints a formatted dp[day][tx][hold] table for small inputs.
     */
    public void printDPTable(int k, int[] prices) {
        int n = prices.length;
        // dp[i][t][0/1]: full table
        int[][][] dp = new int[n][k + 1][2];
        for (int[][] row : dp) for (int[] cell : row) cell[1] = Integer.MIN_VALUE / 2;
        for (int t = 1; t <= k; t++) dp[0][t][1] = -prices[0];

        for (int i = 1; i < n; i++) {
            for (int t = 1; t <= k; t++) {
                dp[i][t][0] = Math.max(dp[i-1][t][0], dp[i-1][t][1] + prices[i]);
                dp[i][t][1] = Math.max(dp[i-1][t][1], dp[i-1][t-1][0] - prices[i]);
            }
        }

        System.out.printf("%-4s %-6s", "Day", "Price");
        for (int t = 1; t <= k; t++) System.out.printf("| tx=%d:free tx=%d:hold ", t, t);
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%-4d %-6d", i, prices[i]);
            for (int t = 1; t <= k; t++) {
                System.out.printf("|  %-8d  %-8d  ",
                    dp[i][t][0],
                    dp[i][t][1] < Integer.MIN_VALUE / 3 ? -999 : dp[i][t][1]);
            }
            System.out.println();
        }
        System.out.println("Answer: " + dp[n-1][k][0]);
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_StockKTransactions sol = new Problem3_StockKTransactions();

        System.out.println("=== LC 188: Best Time to Buy/Sell — At Most K Transactions ===");

        Object[][] tests = {
            {2, new int[]{3,2,6,5,0,3}},   // 7
            {2, new int[]{2,4,1}},           // 2
            {1, new int[]{7,1,5,3,6,4}},    // 5
            {3, new int[]{1,2,4,2,5,7,2,4,9,0}},  // answer depends
        };

        for (Object[] t : tests) {
            int k = (int) t[0];
            int[] prices = (int[]) t[1];
            System.out.printf("k=%d, prices=%s%n", k, Arrays.toString(prices));
            System.out.println("  Full 3D DP    : " + sol.maxProfit(k, prices));
            System.out.println("  Space Opt     : " + sol.maxProfitSpaceOpt(k, prices));
            System.out.println();
        }

        System.out.println("=== LC 123: At Most 2 Transactions ===");
        System.out.println(sol.maxProfitTwoTransactions(new int[]{3,2,6,5,0,3})); // 7
        System.out.println(sol.maxProfitTwoTransactions(new int[]{1,2,3,4,5}));   // 4
        System.out.println(sol.maxProfitTwoTransactions(new int[]{7,6,4,3,1}));   // 0

        System.out.println("\n=== DP Table Visualization (k=2, [3,2,6,5,0,3]) ===");
        sol.printDPTable(2, new int[]{3,2,6,5,0,3});
    }
}
