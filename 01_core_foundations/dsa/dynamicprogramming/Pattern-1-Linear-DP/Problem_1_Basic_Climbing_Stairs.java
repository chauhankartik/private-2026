/**
 * ============================================================
 *  PATTERN 1 — LINEAR DP (1D)
 *  Problem 1 (Basic): Climbing Stairs   LC 70
 * ============================================================
 *
 *  Difficulty  : Easy
 *  Pattern     : Linear DP — dp[i] = dp[i-1] + dp[i-2]
 *  LeetCode    : https://leetcode.com/problems/climbing-stairs/
 *
 *  PROBLEM STATEMENT:
 *    You are climbing a staircase with n steps. Each time you can climb
 *    1 or 2 steps. In how many distinct ways can you reach the top?
 *
 *  EXAMPLES:
 *    n=2 → 2  ([1,1] or [2])
 *    n=3 → 3  ([1,1,1], [1,2], [2,1])
 *    n=5 → 8
 *
 *  WHY THIS PROBLEM MATTERS:
 *    It's a DISGUISED Fibonacci problem. Recognizing the Fibonacci pattern
 *    in novel problem descriptions is a core interview skill.
 *    Also teaches: top-down memo, bottom-up tabulation, space optimization.
 * ============================================================
 */
import java.util.*;

class Problem1_ClimbingStairs {

    // =========================================================
    // APPROACH 1 — BOTTOM-UP DP (optimal)
    // =========================================================
    /**
     * dp[i] = number of distinct ways to reach step i
     *
     * RECURRENCE:
     *   dp[i] = dp[i-1] + dp[i-2]
     *   Reason: to reach step i, you came either from step i-1 (1-step) or
     *           step i-2 (2-step). Sum of those two ways.
     *
     * BASE CASES:
     *   dp[0] = 1  (one way to be at ground: don't move)
     *   dp[1] = 1  (one way to reach step 1: one 1-step)
     *
     * VISUAL (n=5):
     *   dp: [1, 1, 2, 3, 5, 8]
     *        0  1  2  3  4  5  ← index
     *   This is Fibonacci(n+1): fib(1)=1, fib(2)=1, fib(3)=2, ...
     *
     * SPACE OPTIMIZATION:
     *   dp[i] only depends on dp[i-1] and dp[i-2] → keep 2 variables only.
     *
     * Time: O(n), Space: O(1)
     *
     * Follow-up: What if you can climb 1, 2, or 3 steps? (Tribonacci)
     *   dp[i] = dp[i-1] + dp[i-2] + dp[i-3]
     * Follow-up: What if each step has a cost? (Min Cost Climbing Stairs LC 746)
     *   dp[i] = min(dp[i-1], dp[i-2]) + cost[i]
     */
    public int climbStairs(int n) {
        if (n <= 1) return 1;
        int prev2 = 1, prev1 = 1;  // dp[0]=1, dp[1]=1
        for (int i = 2; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // APPROACH 2 — TOP-DOWN MEMOIZATION
    // =========================================================
    /**
     * Start from n, recurse down. Cache results to avoid recomputation.
     * Time: O(n), Space: O(n) — memo array + recursion stack.
     */
    public int climbStairsMemo(int n) {
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);
        return memo(n, memo);
    }

    private int memo(int n, int[] memo) {
        if (n <= 1) return 1;
        if (memo[n] != -1) return memo[n];
        memo[n] = memo(n - 1, memo) + memo(n - 2, memo);
        return memo[n];
    }

    // =========================================================
    // APPROACH 3 — GENERALIZATION: k-step climbing
    // =========================================================
    /**
     * At each step, you can take 1, 2, ..., k steps.
     * dp[i] = sum of dp[i-1] + dp[i-2] + ... + dp[i-k]
     *
     * Use a sliding window sum for O(n) time (instead of O(n*k)).
     */
    public int climbStairsKSteps(int n, int k) {
        int[] dp = new int[n + 1];
        dp[0] = 1;
        int windowSum = 1;  // sliding sum of last k dp values

        for (int i = 1; i <= n; i++) {
            dp[i] = windowSum;
            windowSum += dp[i];
            if (i >= k) windowSum -= dp[i - k];
        }

        return dp[n];
    }

    // =========================================================
    // APPROACH 4 — MIN COST CLIMBING STAIRS (LC 746)
    // =========================================================
    /**
     * cost[i] = cost to step on stair i. From stair i you can go to i+1 or i+2.
     * You can start at index 0 or 1. Find minimum cost to reach the top (beyond last stair).
     *
     * dp[i] = min cost to reach stair i
     * dp[i] = min(dp[i-1], dp[i-2]) + cost[i]
     * Answer = min(dp[n-1], dp[n-2])
     *
     * Time: O(n), Space: O(1)
     */
    public int minCostClimbingStairs(int[] cost) {
        int n = cost.length;
        int prev2 = cost[0], prev1 = cost[1];
        for (int i = 2; i < n; i++) {
            int curr = Math.min(prev1, prev2) + cost[i];
            prev2 = prev1;
            prev1 = curr;
        }
        return Math.min(prev1, prev2);
    }

    // =========================================================
    // APPROACH 5 — TRIBONACCI (LC 1137)
    // =========================================================
    /**
     * dp[i] = dp[i-1] + dp[i-2] + dp[i-3]
     * Base: dp[0]=0, dp[1]=1, dp[2]=1
     */
    public int tribonacci(int n) {
        if (n == 0) return 0;
        if (n <= 2) return 1;
        int a = 0, b = 1, c = 1;
        for (int i = 3; i <= n; i++) {
            int d = a + b + c;
            a = b; b = c; c = d;
        }
        return c;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_ClimbingStairs sol = new Problem1_ClimbingStairs();

        System.out.println("=== LC 70: Climbing Stairs ===");
        for (int n = 1; n <= 10; n++) {
            System.out.printf("n=%2d: Bottom-Up=%d, Memo=%d%n",
                n, sol.climbStairs(n), sol.climbStairsMemo(n));
        }

        System.out.println("\n=== K-Step Climbing (k=3) ===");
        for (int n = 1; n <= 8; n++) {
            System.out.printf("n=%d, k=3: %d%n", n, sol.climbStairsKSteps(n, 3));
        }
        // Expected: n=1→1, n=2→2, n=3→4, n=4→7, n=5→13...

        System.out.println("\n=== LC 746: Min Cost Climbing Stairs ===");
        System.out.println(sol.minCostClimbingStairs(new int[]{10,15,20}));          // 15
        System.out.println(sol.minCostClimbingStairs(new int[]{1,100,1,1,1,100,1,1,100,1})); // 6

        System.out.println("\n=== LC 1137: Tribonacci ===");
        for (int n = 0; n <= 10; n++) {
            System.out.printf("T(%d)=%d  ", n, sol.tribonacci(n));
        }
        System.out.println();
    }
}
