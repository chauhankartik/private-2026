/**
 * ============================================================
 *  PATTERN 3 — UNBOUNDED KNAPSACK DP
 *  Problem 1 (Basic): Coin Change   LC 322
 * ============================================================
 *
 *  Difficulty  : Medium (Basic for Unbounded Knapsack pattern)
 *  Pattern     : Unbounded Knapsack — minimize (fewest coins)
 *  LeetCode    : https://leetcode.com/problems/coin-change/
 *
 *  PROBLEM STATEMENT:
 *    Given coins[] and amount. Find the fewest coins needed to make up amount.
 *    Return -1 if impossible. You may use each coin unlimited times.
 *
 *  EXAMPLES:
 *    coins=[1,2,5], amount=11  → 3 (5+5+1)
 *    coins=[2], amount=3       → -1
 *    coins=[1], amount=0       → 0
 *
 * ============================================================
 */
import java.util.*;

class Problem1_CoinChange {

    // =========================================================
    // APPROACH 1 — BOTTOM-UP DP (optimal)
    // =========================================================
    /**
     * dp[w] = minimum coins to make amount w
     * BASE: dp[0] = 0 (0 coins for amount 0)
     * INIT: dp[1..amount] = amount+1 (acts as infinity — more than possible)
     *
     * TRANSITION: for each amount w, try all coins:
     *   dp[w] = min(dp[w], dp[w - coin] + 1)
     *
     * ITERATION ORDER: amounts as OUTER, coins as INNER.
     *   (This is equivalent to coins outer, amounts inner for minimization
     *    because we're taking the minimum — order doesn't matter for min.)
     *
     * VISUAL for coins=[1,2,5], amount=11:
     *   dp=[0,∞,∞,∞,∞,∞,∞,∞,∞,∞,∞,∞]
     *   w=1: coin 1: dp[1]=min(∞, dp[0]+1)=1
     *   w=2: coin 1: dp[2]=min(∞,dp[1]+1)=2; coin 2: dp[2]=min(2,dp[0]+1)=1
     *   w=5: coin 5: dp[5]=min(3, dp[0]+1)=1
     *   w=11:coin 5: dp[11]=min(∞,dp[6]+1)=min(∞,2+1)=3 (5+5+1) ✓
     *
     * Time: O(n × amount), Space: O(amount)
     *
     * Follow-up: Reconstruct which coins were used?
     *   Store parent[w] = coin used. Backtrack from amount to 0.
     * Follow-up: Coin Change II (count number of combinations, LC 518)?
     *   Use dp[j] += dp[j-coin] with COINS as outer loop (combinations).
     */
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);  // infinity
        dp[0] = 0;

        for (int w = 1; w <= amount; w++) {
            for (int coin : coins) {
                if (coin <= w) {
                    dp[w] = Math.min(dp[w], dp[w - coin] + 1);
                }
            }
        }

        return dp[amount] > amount ? -1 : dp[amount];
    }

    // =========================================================
    // APPROACH 2 — TOP-DOWN MEMOIZATION
    // =========================================================
    /**
     * Recursively: coinChange(amount) = min over all coins of (1 + coinChange(amount - coin))
     * Memoize with a 1D array.
     */
    private int[] memoCoin;

    public int coinChangeMemo(int[] coins, int amount) {
        memoCoin = new int[amount + 1];
        Arrays.fill(memoCoin, -1);
        int result = memoHelper(coins, amount);
        return result == Integer.MAX_VALUE ? -1 : result;
    }

    private int memoHelper(int[] coins, int rem) {
        if (rem < 0) return Integer.MAX_VALUE;
        if (rem == 0) return 0;
        if (memoCoin[rem] != -1) return memoCoin[rem];

        int best = Integer.MAX_VALUE;
        for (int coin : coins) {
            int sub = memoHelper(coins, rem - coin);
            if (sub != Integer.MAX_VALUE)
                best = Math.min(best, sub + 1);
        }

        return memoCoin[rem] = best;
    }

    // =========================================================
    // APPROACH 3 — BFS (Shortest Path / Level-by-Level)
    // =========================================================
    /**
     * Model as unweighted shortest path:
     *   Nodes: 0 to amount. Edge from w to w+coin for each coin.
     *   Find shortest path from 0 to 'amount'.
     *
     * BFS guarantees minimum steps (= minimum coins).
     *
     * Time: O(n × amount), Space: O(amount) for visited array
     */
    public int coinChangeBFS(int[] coins, int amount) {
        if (amount == 0) return 0;

        boolean[] visited = new boolean[amount + 1];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(0);
        visited[0] = true;
        int steps = 0;

        while (!queue.isEmpty()) {
            steps++;
            int size = queue.size();
            for (int s = 0; s < size; s++) {
                int curr = queue.poll();
                for (int coin : coins) {
                    int next = curr + coin;
                    if (next == amount) return steps;
                    if (next < amount && !visited[next]) {
                        visited[next] = true;
                        queue.offer(next);
                    }
                }
            }
        }

        return -1;
    }

    // =========================================================
    // APPROACH 4 — RECONSTRUCT COINS USED
    // =========================================================
    /**
     * Store which coin was used to achieve each amount.
     * Backtrack from amount to 0 using parent[] array.
     */
    public List<Integer> coinChangeReconstruct(int[] coins, int amount) {
        int[] dp     = new int[amount + 1];
        int[] parent = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        Arrays.fill(parent, -1);
        dp[0] = 0;

        for (int w = 1; w <= amount; w++) {
            for (int coin : coins) {
                if (coin <= w && dp[w - coin] + 1 < dp[w]) {
                    dp[w] = dp[w - coin] + 1;
                    parent[w] = coin;
                }
            }
        }

        if (dp[amount] > amount) return new ArrayList<>();

        List<Integer> result = new ArrayList<>();
        for (int w = amount; w > 0; w -= parent[w]) {
            result.add(parent[w]);
        }
        return result;
    }

    // =========================================================
    // BONUS: Coin Change II — Count Combinations (LC 518)
    // =========================================================
    /**
     * Count the NUMBER OF WAYS to make 'amount' using coins (unlimited each).
     * ORDER DOESN'T MATTER: [1,2] and [2,1] = same combination.
     * → Coins as OUTER loop, amounts as INNER loop.
     *
     * dp[j] = number of combinations to make sum j.
     * BASE: dp[0] = 1.
     * TRANSITION: dp[j] += dp[j - coin] (FORWARDS — unbounded)
     */
    public int changeCount(int amount, int[] coins) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;
        for (int coin : coins) {                    // coins OUTER
            for (int j = coin; j <= amount; j++) {  // amounts INNER, FORWARDS
                dp[j] += dp[j - coin];
            }
        }
        return dp[amount];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_CoinChange sol = new Problem1_CoinChange();

        System.out.println("=== LC 322: Coin Change (Minimum Coins) ===");
        int[][] coinsArr = {{1,2,5}, {2}, {1}, {186,419,83,408}};
        int[]   amounts  = {11, 3, 0, 6249};
        int[]   expected = {3, -1, 0, 20};

        for (int i = 0; i < coinsArr.length; i++) {
            int[] c = coinsArr[i];
            int a = amounts[i];
            System.out.println("coins=" + Arrays.toString(c) + ", amount=" + a);
            System.out.println("  Bottom-Up  : " + sol.coinChange(c, a)    + "  (expected " + expected[i] + ")");
            System.out.println("  Memo       : " + sol.coinChangeMemo(c, a));
            if (a <= 1000) {
                System.out.println("  BFS        : " + sol.coinChangeBFS(c, a));
                System.out.println("  Coins used : " + sol.coinChangeReconstruct(c, a));
            }
            System.out.println();
        }

        System.out.println("=== LC 518: Coin Change II (Count Combinations) ===");
        System.out.println(sol.changeCount(5,  new int[]{1,2,5}));   // 4
        System.out.println(sol.changeCount(3,  new int[]{2}));       // 0
        System.out.println(sol.changeCount(10, new int[]{10}));      // 1
    }
}
