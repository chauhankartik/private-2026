/**
 * ============================================================
 *  PATTERN 9 — STATE MACHINE DP
 *  Problem 1 (Basic): Best Time to Buy and Sell Stock   LC 121
 * ============================================================
 *
 *  Difficulty  : Easy (classified "Basic" for pattern entry)
 *  Pattern     : State Machine DP — two states: HOLD / FREE
 *  LeetCode    : https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
 *
 *  PROBLEM STATEMENT:
 *    Given a prices array where prices[i] = stock price on day i.
 *    You may buy on ONE day and sell on a LATER day (at most 1 transaction).
 *    Maximize profit. If no profit is possible, return 0.
 *
 *  EXAMPLES:
 *    [7,1,5,3,6,4]  → 5 (buy at 1, sell at 6)
 *    [7,6,4,3,1]    → 0 (prices always fall, no profit)
 *
 * ============================================================
 */
import java.util.*;

class Problem1_StockBuySell1 {

    // =========================================================
    // APPROACH 1 — STATE MACHINE DP (canonical, general pattern)
    // =========================================================
    /**
     * STATES:
     *   HOLD: we currently hold a stock (bought it on some past day)
     *   FREE: we don't hold a stock (either never bought, or already sold)
     *
     * STATE MACHINE DIAGRAM:
     *   FREE ──(buy)──> HOLD ──(sell)──> FREE
     *   FREE ──(rest)─> FREE
     *   HOLD ──(rest)─> HOLD
     *   (After selling, we can't buy again since at most 1 transaction)
     *
     *   For 1-transaction constraint: once we sell, we enter a "SOLD" final state
     *   (not allowed to buy again). Modeling as HOLD/FREE where FREE is the terminal:
     *
     * TRANSITION:
     *   hold[i] = max(hold[i-1],          -prices[i])
     *             ← keep holding OR buy today (pay prices[i], so profit = -prices[i])
     *
     *   free[i] = max(free[i-1], hold[i-1] + prices[i])
     *             ← stay free (don't sell) OR sell today (profit = prices[i] - buy_price)
     *
     * BASE CASE:
     *   hold[0] = -prices[0]  (buy on day 0)
     *   free[0] = 0           (don't buy on day 0, profit = 0)
     *
     * ANSWER: free[n-1]  (always better to not hold stock at the end)
     *
     * VISUAL for [7,1,5,3,6,4]:
     *   Day 0: hold=-7, free=0
     *   Day 1: hold=max(-7, -1)=-1, free=max(0, -7+1)=0
     *   Day 2: hold=max(-1, -5)=-1, free=max(0, -1+5)=4
     *   Day 3: hold=max(-1, -3)=-1, free=max(4, -1+3)=4
     *   Day 4: hold=max(-1, -6)=-1, free=max(4, -1+6)=5 ← best!
     *   Day 5: hold=max(-1, -4)=-1, free=max(5, -1+4)=5
     *   Answer: free[5] = 5 ✓
     *
     * Time:  O(n)
     * Space: O(1) (only track previous day's values)
     *
     * Follow-up: Why is "hold" transition -prices[i] (not -prices[i] + free[i-1])?
     *   Because we can only buy ONCE. If we buy on day i, our total investment is
     *   just -prices[i] regardless of past days (no prior profit to add since only 1 tx).
     *   Contrast with unlimited transactions where: hold = max(hold, free - prices[i]).
     *
     * Follow-up: What if we allow buying multiple times (LC 122)?
     *   Change hold[i] = max(hold[i-1], free[i-1] - prices[i])
     *   This lets us "reinvest" previous profits.
     */
    public int maxProfit(int[] prices) {
        int hold = -prices[0];   // best profit while holding a stock
        int free = 0;            // best profit while not holding (or never bought)

        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold;
            int prevFree = free;
            // Buy: only allowed if we haven't bought before → cost = -prices[i]
            // (since 1 transaction: max purchase price doesn't use prevFree)
            hold = Math.max(prevHold, -prices[i]);
            // Sell: receive prices[i], add to our buy cost (hold = -buy_price)
            free = Math.max(prevFree, prevHold + prices[i]);
        }

        return free;
    }

    // =========================================================
    // APPROACH 2 — GREEDY (O(n), most intuitive for this variant)
    // =========================================================
    /**
     * Track the minimum price seen so far.
     * At each day, compute profit if we sell today (prices[i] - minSoFar).
     * Update max profit.
     *
     * Equivalent to Approach 1 mathematically.
     *
     * Time:  O(n), Space: O(1)
     */
    public int maxProfitGreedy(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;

        for (int price : prices) {
            if (price < minPrice) {
                minPrice = price;            // found a better buy day
            } else if (price - minPrice > maxProfit) {
                maxProfit = price - minPrice; // found a better sell day
            }
        }

        return maxProfit;
    }

    // =========================================================
    // APPROACH 3 — KADANE'S VARIANT (max subarray on differences)
    // =========================================================
    /**
     * Let diff[i] = prices[i] - prices[i-1].
     * The problem reduces to finding the maximum sum subarray of diff[].
     * This is exactly Kadane's algorithm.
     *
     * Why? Profit = prices[sell] - prices[buy] = sum of daily differences from buy+1 to sell.
     *
     * Time: O(n), Space: O(1)
     */
    public int maxProfitKadane(int[] prices) {
        int maxSum = 0, curSum = 0;
        for (int i = 1; i < prices.length; i++) {
            curSum = Math.max(0, curSum + prices[i] - prices[i - 1]);
            maxSum = Math.max(maxSum, curSum);
        }
        return maxSum;
    }

    // =========================================================
    // EXTENSION: Maximum Profit with Unlimited Transactions (LC 122)
    // =========================================================
    /**
     * STATES: HOLD / FREE (same as above)
     *
     * Difference: after selling, we CAN buy again.
     * So hold = max(hold, FREE - prices[i])  ← use free[i-1] to reinvest profit
     *
     * Greedy equivalent: sum all positive daily differences.
     *
     * Time: O(n), Space: O(1)
     */
    public int maxProfitUnlimited(int[] prices) {
        int hold = -prices[0], free = 0;
        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold, prevFree = free;
            hold = Math.max(prevHold, prevFree - prices[i]);  // can reinvest!
            free = Math.max(prevFree, prevHold + prices[i]);
        }
        return free;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_StockBuySell1 sol = new Problem1_StockBuySell1();

        System.out.println("=== LC 121: Best Time to Buy and Sell Stock (1 transaction) ===");

        int[][] testCases = {
            {7, 1, 5, 3, 6, 4},   // 5
            {7, 6, 4, 3, 1},       // 0
            {1, 2},                // 1
            {2, 4, 1},             // 2
            {3, 3, 5, 0, 0, 3, 1, 4},  // 4
        };

        for (int[] prices : testCases) {
            System.out.println("prices = " + Arrays.toString(prices));
            System.out.println("  State Machine : " + sol.maxProfit(prices));
            System.out.println("  Greedy        : " + sol.maxProfitGreedy(prices));
            System.out.println("  Kadane        : " + sol.maxProfitKadane(prices));
            System.out.println();
        }

        System.out.println("=== LC 122: Best Time to Buy/Sell (Unlimited transactions) ===");
        System.out.println(sol.maxProfitUnlimited(new int[]{7,1,5,3,6,4})); // 7 (buy@1,sell@5 + buy@3,sell@6)
        System.out.println(sol.maxProfitUnlimited(new int[]{1,2,3,4,5}));   // 4
        System.out.println(sol.maxProfitUnlimited(new int[]{7,6,4,3,1}));   // 0
    }
}
