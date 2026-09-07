/**
 * ============================================================
 *  PATTERN 9 — STATE MACHINE DP
 *  Problem 2 (Medium): Best Time to Buy and Sell Stock with Cooldown   LC 309
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : State Machine DP — THREE states: HOLD / SOLD / REST
 *  LeetCode    : https://leetcode.com/problems/best-time-to-buy-and-sell-stock-with-cooldown/
 *
 *  PROBLEM STATEMENT:
 *    Unlimited transactions allowed, BUT after selling, you must rest for 1 day
 *    (cannot buy on the day immediately after selling).
 *    Find the maximum profit.
 *
 *  EXAMPLES:
 *    [1,2,3,0,2]  → 3 (buy@1, sell@2, rest, buy@0, sell@2)
 *    [1]          → 0
 *    [1,2]        → 1
 *
 *  STATE MACHINE DIAGRAM:
 *
 *     REST ──(buy)──> HOLD ──(sell)──> SOLD
 *      ↑                ↑              |
 *      └──(rest)────────┘              └──(cooldown = 1 day)──> REST
 *      └──(from SOLD after cooldown)────────────────────────────┘
 *
 *    Valid transitions:
 *      REST → HOLD (buy)
 *      REST → REST (do nothing)
 *      HOLD → HOLD (do nothing = keep holding)
 *      HOLD → SOLD (sell)
 *      SOLD → REST (mandatory cooldown day)
 *
 *    INVALID:
 *      SOLD → HOLD (can't buy immediately after selling)
 *
 * ============================================================
 */
import java.util.*;

class Problem2_StockWithCooldown {

    // =========================================================
    // APPROACH 1 — STATE MACHINE DP (O(1) space)
    // =========================================================
    /**
     * THREE STATES:
     *   HOLD = currently holding a stock
     *   SOLD = just sold (entered cooldown for NEXT day)
     *   REST = not holding, not in cooldown (free to buy)
     *
     * TRANSITION:
     *   hold[i] = max(hold[i-1],           rest[i-1] - prices[i])
     *             ← keep holding OR buy (only from REST state, not from SOLD)
     *
     *   sold[i] = hold[i-1] + prices[i]
     *             ← must come from HOLD: sell today (no choice structure here)
     *             (Note: can't sell from REST, so this is forced if we decide to sell)
     *
     *   rest[i] = max(rest[i-1],           sold[i-1])
     *             ← stay resting OR come from SOLD (cooldown day has passed)
     *
     * BASE CASE (day 0):
     *   hold = -prices[0]  (buy on day 0)
     *   sold = 0           (can't have sold on day 0 with no prior holding... or 0 profit)
     *                      (actually: we can't sell on day 0 without buying; sold = -inf?)
     *                      Set to 0 for simplicity — the DP handles it correctly.
     *   rest = 0           (haven't started: 0 profit)
     *
     * ANSWER: max(sold[n-1], rest[n-1])
     *   (Never hold at the end — selling always gives more profit.)
     *
     * VISUAL for [1,2,3,0,2]:
     *   Day 0: hold=-1, sold=0, rest=0
     *   Day 1: hold=max(-1, 0-2)=-1, sold=-1+2=1, rest=max(0,0)=0
     *   Day 2: hold=max(-1, 0-3)=-1, sold=-1+3=2, rest=max(0,1)=1
     *   Day 3: hold=max(-1, 1-0)=1,  sold=-1+0=-1,rest=max(1,2)=2
     *   Day 4: hold=max(1, 2-2)=1,   sold=1+2=3,  rest=max(2,-1)=2
     *   Answer: max(sold=3, rest=2) = 3 ✓
     *
     * Time:  O(n)
     * Space: O(1) — only three variables
     *
     * Follow-up: What if cooldown is k days (not just 1)?
     *   Need to track sold[i-1], sold[i-2], ..., sold[i-k] or use a sliding window.
     *   Or: rest[i] = max(rest[i-1], sold[i-k]).
     * Follow-up: Best Time to Buy/Sell with Transaction Fee (LC 714)?
     *   Two states (HOLD/FREE), fee subtracted on sell:
     *   free[i] = max(free[i-1], hold[i-1] + prices[i] - fee)
     */
    public int maxProfit(int[] prices) {
        int hold = -prices[0];
        int sold = 0;
        int rest = 0;

        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold, prevSold = sold, prevRest = rest;

            hold = Math.max(prevHold, prevRest - prices[i]);  // buy only from REST
            sold = prevHold + prices[i];                       // sell from HOLD
            rest = Math.max(prevRest, prevSold);              // from REST or SOLD cooldown
        }

        return Math.max(sold, rest);
    }

    // =========================================================
    // APPROACH 2 — FULL DP TABLE (for visualization)
    // =========================================================
    /**
     * Same logic but stores all day values in 2D array for debugging.
     *
     * Time: O(n), Space: O(n)
     */
    public int maxProfitTable(int[] prices) {
        int n = prices.length;
        if (n == 1) return 0;

        // dp[i][0] = HOLD, dp[i][1] = SOLD, dp[i][2] = REST
        int[][] dp = new int[n][3];
        dp[0][0] = -prices[0];  // hold
        dp[0][1] = 0;           // sold (can't sell on day 0, set to 0)
        dp[0][2] = 0;           // rest

        for (int i = 1; i < n; i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][2] - prices[i]);  // HOLD
            dp[i][1] = dp[i-1][0] + prices[i];                          // SOLD
            dp[i][2] = Math.max(dp[i-1][2], dp[i-1][1]);               // REST
        }

        // Debug: print the DP table
        System.out.println("Day | Price | HOLD | SOLD | REST");
        System.out.println("----+-------+------+------+-----");
        for (int i = 0; i < n; i++) {
            System.out.printf(" %2d |   %3d |  %3d |  %3d | %3d%n",
                i, prices[i], dp[i][0], dp[i][1], dp[i][2]);
        }

        return Math.max(dp[n-1][1], dp[n-1][2]);
    }

    // =========================================================
    // APPROACH 3 — STOCK WITH TRANSACTION FEE (LC 714)
    // =========================================================
    /**
     * Unlimited transactions, but each sale incurs a fee.
     *
     * STATES: HOLD / FREE (no cooldown needed)
     * TRANSITION:
     *   hold[i] = max(hold[i-1], free[i-1] - prices[i])   // buy from FREE
     *   free[i] = max(free[i-1], hold[i-1] + prices[i] - fee)  // sell, pay fee
     *
     * ANSWER: free[n-1]
     *
     * Time: O(n), Space: O(1)
     */
    public int maxProfitWithFee(int[] prices, int fee) {
        int hold = -prices[0], free = 0;
        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold, prevFree = free;
            hold = Math.max(prevHold, prevFree - prices[i]);
            free = Math.max(prevFree, prevHold + prices[i] - fee);
        }
        return free;
    }

    // =========================================================
    // APPROACH 4 — K-DAY COOLDOWN GENERALIZATION
    // =========================================================
    /**
     * After selling, must rest for EXACTLY k days before buying again.
     * rest[i] can buy only if sold[i-k] (or earlier) has been processed.
     *
     * Use a circular buffer or a deque to track the last k sold values.
     *
     * For k=1 (cooldown=1): rest[i] = max(rest[i-1], sold[i-1]) — standard.
     * For k=2 (cooldown=2): rest[i] = max(rest[i-1], sold[i-2]).
     *
     * Simple implementation with O(n) space for sold array:
     */
    public int maxProfitCooldownK(int[] prices, int k) {
        int n = prices.length;
        int[] hold = new int[n], sold = new int[n], rest = new int[n];

        hold[0] = -prices[0];
        // sold[0] = 0, rest[0] = 0 (can't sell on day 0)

        for (int i = 1; i < n; i++) {
            // Can buy only if we're in REST state (sold k+ days ago)
            int bestRest = (i - k - 1 >= 0) ? sold[i - k - 1] : 0;
            // If i-k-1 < 0 we haven't had enough days of cooldown from day 0 sell
            // so rest comes from 0 (never sold) or rest[i-1]
            bestRest = Math.max(bestRest, rest[i-1]);

            hold[i] = Math.max(hold[i-1], bestRest - prices[i]);
            sold[i] = hold[i-1] + prices[i];
            rest[i] = Math.max(rest[i-1], i >= k + 1 ? sold[i-k-1] : 0);
        }

        return Math.max(sold[n-1], rest[n-1]);
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_StockWithCooldown sol = new Problem2_StockWithCooldown();

        System.out.println("=== LC 309: Stock with Cooldown (k=1 day rest) ===");

        int[][] cases = {
            {1, 2, 3, 0, 2},   // 3
            {1},                // 0
            {1, 2},             // 1
            {2, 1, 4},          // 3
        };

        for (int[] prices : cases) {
            System.out.println("\nprices = " + Arrays.toString(prices));
            System.out.println("  State Machine : " + sol.maxProfit(prices));
            System.out.println("  DP Table      :");
            sol.maxProfitTable(prices);
        }

        System.out.println("\n=== LC 714: Stock with Transaction Fee ===");
        System.out.println(sol.maxProfitWithFee(new int[]{1,3,2,8,4,9}, 2)); // 8
        System.out.println(sol.maxProfitWithFee(new int[]{1,3,7,5,10,3}, 3)); // 6

        System.out.println("\n=== Generalized Cooldown (k days) ===");
        System.out.println("k=1: " + sol.maxProfitCooldownK(new int[]{1,2,3,0,2}, 1)); // 3
        System.out.println("k=2: " + sol.maxProfitCooldownK(new int[]{1,2,3,0,2}, 2)); // 2?
    }
}
