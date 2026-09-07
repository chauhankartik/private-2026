/**
 * ============================================================
 *  DYNAMIC PROGRAMMING — EASY PROBLEMS
 * ============================================================
 *
 *  E1.  Fibonacci Number                         LC 509
 *  E2.  Climbing Stairs                          LC 70
 *  E3.  Min Cost Climbing Stairs                 LC 746
 *  E4.  House Robber                             LC 198
 *  E5.  Maximum Subarray (Kadane's)              LC 53
 *  E6.  Counting Bits                            LC 338
 *  E7.  N-th Tribonacci Number                   LC 1137
 *  E8.  Divisor Game                             LC 1025
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class DPEasy {

    // =========================================================
    // E1. Fibonacci Number  LC 509
    // Pattern: LINEAR DP (1D, two previous states)
    // =========================================================
    /**
     * F(0) = 0, F(1) = 1, F(n) = F(n-1) + F(n-2)
     *
     * Brute force: plain recursion — O(2^n) time, O(n) stack space.
     * The recursion tree has exponential nodes because we recompute:
     *   fib(5) → fib(4) + fib(3)
     *   fib(4) → fib(3) + fib(2)    ← fib(3) is computed TWICE
     *
     * Top-down memo: O(n) time, O(n) space (memo array + recursion stack).
     * Bottom-up:     O(n) time, O(n) space.
     * Space-opt:     O(n) time, O(1) space — only need prev two values.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Compute fib(n) in O(log n) using matrix exponentiation.
     *   [[F(n+1), F(n)], [F(n), F(n-1)]] = [[1,1],[1,0]]^n
     */

    // Top-down (memoization)
    public int fibMemo(int n) {
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);
        return fibHelper(n, memo);
    }

    private int fibHelper(int n, int[] memo) {
        if (n <= 1) return n;
        if (memo[n] != -1) return memo[n];
        memo[n] = fibHelper(n - 1, memo) + fibHelper(n - 2, memo);
        return memo[n];
    }

    // Bottom-up (tabulation)
    public int fibTable(int n) {
        if (n <= 1) return n;
        int[] dp = new int[n + 1];
        dp[0] = 0; dp[1] = 1;
        for (int i = 2; i <= n; i++)
            dp[i] = dp[i - 1] + dp[i - 2];
        return dp[n];
    }

    // Space-optimized (O(1) space)
    public int fib(int n) {
        if (n <= 1) return n;
        int prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // E2. Climbing Stairs  LC 70
    // Pattern: LINEAR DP (identical to Fibonacci)
    // =========================================================
    /**
     * You can climb 1 or 2 steps. How many distinct ways to reach step n?
     *
     * dp[i] = number of ways to reach step i
     * dp[i] = dp[i-1] + dp[i-2]
     *   (arrive from step i-1 with 1 step, or from step i-2 with 2 steps)
     *
     * Base: dp[0] = 1 (one way to stay at ground), dp[1] = 1 (one step)
     *
     * THIS IS FIBONACCI with dp[0]=1, dp[1]=1.
     * The number of ways to reach step n = fib(n+1).
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: What if you can climb 1, 2, or 3 steps?
     *   dp[i] = dp[i-1] + dp[i-2] + dp[i-3] — Tribonacci variant.
     * Follow-up: What if the steps are given as an array [1, 3, 5]?
     *   dp[i] = sum(dp[i - step]) for each step in steps — general case.
     */
    public int climbStairs(int n) {
        if (n <= 2) return n;
        int prev2 = 1, prev1 = 2;
        for (int i = 3; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // E3. Min Cost Climbing Stairs  LC 746
    // Pattern: LINEAR DP (min cost instead of count)
    // =========================================================
    /**
     * Each step has a cost. Pay cost[i] to step on stair i.
     * From stair i, you can go to i+1 or i+2.
     * Start from step 0 or step 1. Reach past the last step.
     *
     * dp[i] = minimum cost to reach step i
     * dp[i] = min(dp[i-1] + cost[i-1], dp[i-2] + cost[i-2])
     *
     * Base: dp[0] = 0, dp[1] = 0 (can start at either step for free)
     * Answer: dp[n] (top of stairs, one past the last index)
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Return the actual path taken?
     *   Track choices in a parent array, then backtrack from dp[n].
     */
    public int minCostClimbingStairs(int[] cost) {
        int n = cost.length;
        int prev2 = 0, prev1 = 0;  // cost to reach step 0 and step 1
        for (int i = 2; i <= n; i++) {
            int curr = Math.min(prev1 + cost[i - 1], prev2 + cost[i - 2]);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // E4. House Robber  LC 198
    // Pattern: LINEAR DP (take or skip, no adjacents)
    // =========================================================
    /**
     * Rob houses along a street. Can't rob two adjacent houses.
     * Maximize total money.
     *
     * dp[i] = max money from first i houses
     * At house i, two choices:
     *   - Skip house i: dp[i] = dp[i-1]
     *   - Rob house i:  dp[i] = dp[i-2] + nums[i]
     * dp[i] = max(dp[i-1], dp[i-2] + nums[i])
     *
     * Base: dp[0] = nums[0], dp[1] = max(nums[0], nums[1])
     *
     * WHY THIS WORKS:
     *   dp[i-1] already encodes the best we can do without house i.
     *   dp[i-2] + nums[i] is the best including house i (skipping i-1).
     *   No need to consider dp[i-3] + nums[i] because dp[i-2] ≥ dp[i-3].
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Houses in a circle (House Robber II, LC 213)?
     *   Two passes: rob [0..n-2] and [1..n-1], take max.
     *   This avoids robbing both first and last (which are adjacent in circle).
     */
    public int rob(int[] nums) {
        if (nums.length == 1) return nums[0];
        int prev2 = nums[0];
        int prev1 = Math.max(nums[0], nums[1]);
        for (int i = 2; i < nums.length; i++) {
            int curr = Math.max(prev1, prev2 + nums[i]);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // E5. Maximum Subarray (Kadane's Algorithm)  LC 53
    // Pattern: LINEAR DP (extend or restart)
    // =========================================================
    /**
     * Find the contiguous subarray with the largest sum.
     *
     * dp[i] = maximum sum of subarray ENDING at index i
     * dp[i] = max(nums[i], dp[i-1] + nums[i])
     *   Either: start a new subarray at i (nums[i])
     *   Or:     extend the previous subarray (dp[i-1] + nums[i])
     *
     * Extend if dp[i-1] > 0 (previous sum adds value).
     * Restart if dp[i-1] ≤ 0 (previous sum is a burden).
     *
     * Answer: max(dp[0], dp[1], ..., dp[n-1])
     *
     * KADANE'S ALGORITHM is exactly this DP with O(1) space.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Return the subarray itself?
     *   Track start and end indices. Reset start when restarting.
     * Follow-up: Maximum circular subarray (LC 918)?
     *   max(Kadane's max, total_sum - Kadane's min).
     *   Edge case: if all negative, answer = max element.
     */
    public int maxSubArray(int[] nums) {
        int maxSum = nums[0];
        int currSum = nums[0];
        for (int i = 1; i < nums.length; i++) {
            currSum = Math.max(nums[i], currSum + nums[i]);
            maxSum = Math.max(maxSum, currSum);
        }
        return maxSum;
    }

    // Version that tracks the subarray indices
    public int[] maxSubArrayWithIndices(int[] nums) {
        int maxSum = nums[0], currSum = nums[0];
        int start = 0, end = 0, tempStart = 0;
        for (int i = 1; i < nums.length; i++) {
            if (currSum + nums[i] < nums[i]) {
                currSum = nums[i];
                tempStart = i;     // restart subarray here
            } else {
                currSum += nums[i];
            }
            if (currSum > maxSum) {
                maxSum = currSum;
                start = tempStart;
                end = i;
            }
        }
        return new int[]{maxSum, start, end};
    }

    // =========================================================
    // E6. Counting Bits  LC 338
    // Pattern: LINEAR DP (bit manipulation + DP)
    // =========================================================
    /**
     * For every number i in [0, n], count the number of 1's in its binary.
     *
     * Brute: for each number, count bits with Integer.bitCount — O(n log n).
     *
     * DP observation:
     *   i >> 1 removes the last bit. We already know the answer for i >> 1.
     *   The last bit contributes (i & 1).
     *   dp[i] = dp[i >> 1] + (i & 1)
     *
     * Example: 5 = 101 → dp[5] = dp[2] + 1 = 1 + 1 = 2
     *          2 = 010 → dp[2] = dp[1] + 0 = 1 + 0 = 1
     *
     * Alternative recurrence: dp[i] = dp[i & (i-1)] + 1
     *   i & (i-1) clears the lowest set bit.
     *
     * Time:  O(n)
     * Space: O(n) — output array
     *
     * Follow-up: Can you do it without the & and >> operators?
     *   Use dp[i] = dp[i - highest_power_of_2_leq_i] + 1.
     */
    public int[] countBits(int n) {
        int[] dp = new int[n + 1];
        for (int i = 1; i <= n; i++)
            dp[i] = dp[i >> 1] + (i & 1);
        return dp;
    }

    // =========================================================
    // E7. N-th Tribonacci Number  LC 1137
    // Pattern: LINEAR DP (three previous states)
    // =========================================================
    /**
     * T(0) = 0, T(1) = 1, T(2) = 1
     * T(n) = T(n-1) + T(n-2) + T(n-3)
     *
     * Same as Fibonacci but with three previous values.
     * Space-optimize to O(1) by keeping three variables.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: What if each step costs differently and you can jump 1, 2, or 3?
     *   Combine with min-cost pattern: dp[i] = min(dp[i-1]+c1, dp[i-2]+c2, dp[i-3]+c3).
     */
    public int tribonacci(int n) {
        if (n == 0) return 0;
        if (n <= 2) return 1;
        int a = 0, b = 1, c = 1;
        for (int i = 3; i <= n; i++) {
            int next = a + b + c;
            a = b;
            b = c;
            c = next;
        }
        return c;
    }

    // =========================================================
    // E8. Divisor Game  LC 1025
    // Pattern: GAME DP (boolean DP / mathematical insight)
    // =========================================================
    /**
     * Alice and Bob take turns. On a turn with number n:
     *   Choose x where 1 <= x < n and n % x == 0.
     *   Replace n with n - x.
     * The player who cannot make a move (n == 1) loses.
     *
     * DP approach:
     *   dp[i] = true if the current player wins starting with number i.
     *   dp[1] = false (can't move → lose).
     *   dp[i] = true if ANY divisor x of i makes dp[i - x] = false.
     *   (I win if I can leave my opponent in a losing position.)
     *
     * Mathematical insight: Alice wins if and only if n is EVEN.
     *   Proof: If n is even, Alice picks x=1, leaving Bob with n-1 (odd).
     *          If n is odd, all divisors are odd, so n-x is even → Bob gets even.
     *          By induction, the player with even n always wins.
     *
     * Time:  O(1) with math insight, O(n√n) with DP
     * Space: O(1) with math, O(n) with DP
     *
     * Follow-up: What if you can only subtract PRIME divisors?
     *   DP is necessary — the math shortcut doesn't apply.
     */
    public boolean divisorGame(int n) {
        return n % 2 == 0;  // mathematical solution
    }

    // DP version for understanding
    public boolean divisorGameDP(int n) {
        boolean[] dp = new boolean[n + 1];
        dp[1] = false;
        for (int i = 2; i <= n; i++) {
            for (int x = 1; x < i; x++) {
                if (i % x == 0 && !dp[i - x]) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[n];
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        DPEasy sol = new DPEasy();

        System.out.println("═══ E1: Fibonacci ═══");
        System.out.println("fib(10) = " + sol.fib(10));          // 55
        System.out.println("memo:     " + sol.fibMemo(10));       // 55
        System.out.println("table:    " + sol.fibTable(10));      // 55

        System.out.println("\n═══ E2: Climbing Stairs ═══");
        System.out.println("n=2: " + sol.climbStairs(2));   // 2
        System.out.println("n=3: " + sol.climbStairs(3));   // 3
        System.out.println("n=5: " + sol.climbStairs(5));   // 8

        System.out.println("\n═══ E3: Min Cost Climbing Stairs ═══");
        System.out.println(sol.minCostClimbingStairs(new int[]{10, 15, 20}));       // 15
        System.out.println(sol.minCostClimbingStairs(new int[]{1, 100, 1, 1, 1, 100, 1, 1, 100, 1})); // 6

        System.out.println("\n═══ E4: House Robber ═══");
        System.out.println(sol.rob(new int[]{1, 2, 3, 1}));   // 4
        System.out.println(sol.rob(new int[]{2, 7, 9, 3, 1})); // 12

        System.out.println("\n═══ E5: Maximum Subarray (Kadane) ═══");
        System.out.println(sol.maxSubArray(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4})); // 6
        int[] result = sol.maxSubArrayWithIndices(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4});
        System.out.println("Sum=" + result[0] + " [" + result[1] + ".." + result[2] + "]");
        // Sum=6 [3..6]

        System.out.println("\n═══ E6: Counting Bits ═══");
        System.out.println(Arrays.toString(sol.countBits(5)));
        // [0, 1, 1, 2, 1, 2]

        System.out.println("\n═══ E7: Tribonacci ═══");
        System.out.println("T(4)  = " + sol.tribonacci(4));    // 4
        System.out.println("T(25) = " + sol.tribonacci(25));   // 1389537

        System.out.println("\n═══ E8: Divisor Game ═══");
        System.out.println("n=2: " + sol.divisorGame(2));   // true
        System.out.println("n=3: " + sol.divisorGame(3));   // false
        System.out.println("n=4: " + sol.divisorGameDP(4)); // true (DP version)
    }
}
