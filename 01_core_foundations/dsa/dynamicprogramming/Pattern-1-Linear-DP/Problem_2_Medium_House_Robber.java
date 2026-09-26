/**
 * ============================================================
 *  PATTERN 1 — LINEAR DP (1D)
 *  Problem 2 (Medium): House Robber   LC 198 + LC 213
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Linear DP — dp[i][0/1] take/skip with adjacency constraint
 *  LeetCode    : LC 198 (linear), LC 213 (circular), LC 337 (tree)
 *
 *  PROBLEM STATEMENT (LC 198):
 *    Rob houses in a line. You cannot rob two adjacent houses.
 *    Find the maximum amount you can rob.
 *
 *  EXAMPLES:
 *    [1,2,3,1]    → 4  (rob house 1 and 3: 1+3=4)
 *    [2,7,9,3,1]  → 12 (rob house 1,3,5: 2+9+1=12)
 *
 *  PROBLEM STATEMENT (LC 213 — Circular):
 *    Same but houses form a CIRCLE (first and last are adjacent).
 *
 * ============================================================
 */
import java.util.*;

class Problem2_HouseRobber {

    // =========================================================
    // APPROACH 1 — LINEAR HOUSE ROBBER (LC 198)
    // =========================================================
    /**
     * STATE:
     *   rob[i]  = max loot when house i IS robbed
     *   skip[i] = max loot when house i is NOT robbed
     *
     * TRANSITION:
     *   rob[i]  = skip[i-1] + nums[i]   // must come from skip (can't rob adjacent)
     *   skip[i] = max(rob[i-1], skip[i-1]) // best of robbing or skipping house i-1
     *
     * VISUAL for [2,7,9,3,1]:
     *   i=0: rob=2,  skip=0
     *   i=1: rob=7,  skip=max(2,0)=2      (rob 7 OR skip it, best previous was 2)
     *   i=2: rob=2+9=11, skip=max(7,2)=7
     *   i=3: rob=7+3=10, skip=max(11,7)=11
     *   i=4: rob=11+1=12, skip=max(10,11)=11
     *   Answer: max(12, 11) = 12 ✓
     *
     * Time: O(n), Space: O(1)
     *
     * Follow-up: What if there's no adjacency constraint but at most 1 house per k?
     *   dp[i] = max(dp[i-1], dp[i-k] + nums[i]) — skip a window of k-1.
     */
    public int rob(int[] nums) {
        if (nums.length == 1) return nums[0];
        int robCurr = nums[0], skipCurr = 0;
        for (int i = 1; i < nums.length; i++) {
            int newRob  = skipCurr + nums[i];
            int newSkip = Math.max(robCurr, skipCurr);
            robCurr  = newRob;
            skipCurr = newSkip;
        }
        return Math.max(robCurr, skipCurr);
    }

    // =========================================================
    // APPROACH 2 — CIRCULAR HOUSE ROBBER (LC 213)
    // =========================================================
    /**
     * KEY INSIGHT:
     *   House 0 and house n-1 are adjacent (circular).
     *   Either rob house 0 (cannot rob house n-1) or don't rob house 0 (can rob n-1).
     *
     *   Strategy: Run linear robber TWICE:
     *     Case 1: nums[0..n-2] (exclude last house)
     *     Case 2: nums[1..n-1] (exclude first house)
     *     Answer: max(Case 1, Case 2)
     *
     * Time: O(n), Space: O(1)
     *
     * Follow-up: What if it's a circle with 3+ adjacency constraint (no 3 in a row)?
     *   Add a third state: dp[i][k] where k = consecutive robbed so far.
     */
    public int robCircular(int[] nums) {
        if (nums.length == 1) return nums[0];
        if (nums.length == 2) return Math.max(nums[0], nums[1]);
        // Case 1: rob from index 0 to n-2 (last house excluded)
        int c1 = robLinear(nums, 0, nums.length - 2);
        // Case 2: rob from index 1 to n-1 (first house excluded)
        int c2 = robLinear(nums, 1, nums.length - 1);
        return Math.max(c1, c2);
    }

    private int robLinear(int[] nums, int lo, int hi) {
        int robCurr = nums[lo], skipCurr = 0;
        for (int i = lo + 1; i <= hi; i++) {
            int newRob  = skipCurr + nums[i];
            int newSkip = Math.max(robCurr, skipCurr);
            robCurr  = newRob;
            skipCurr = newSkip;
        }
        return Math.max(robCurr, skipCurr);
    }

    // =========================================================
    // APPROACH 3 — FULL DP TABLE (for visualization)
    // =========================================================
    /**
     * Builds the full dp table and prints it, useful for debugging.
     * dp[i][0] = max loot NOT robbing house i
     * dp[i][1] = max loot robbing house i
     */
    public int robWithTable(int[] nums) {
        int n = nums.length;
        int[][] dp = new int[n][2];
        dp[0][0] = 0;
        dp[0][1] = nums[0];

        System.out.printf("%-5s %-8s %-8s%n", "House", "Not-Rob", "Rob");
        System.out.printf("%-5d %-8d %-8d%n", 0, dp[0][0], dp[0][1]);

        for (int i = 1; i < n; i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1]);
            dp[i][1] = dp[i-1][0] + nums[i];
            System.out.printf("%-5d %-8d %-8d%n", i, dp[i][0], dp[i][1]);
        }

        return Math.max(dp[n-1][0], dp[n-1][1]);
    }

    // =========================================================
    // APPROACH 4 — DECODE WAYS (LC 91, related linear DP)
    // =========================================================
    /**
     * PROBLEM: String of digits. Decode '1'→'A', ..., '26'→'Z'. Count distinct decodings.
     *
     * dp[i] = number of ways to decode s[0..i-1]
     * Base: dp[0]=1, dp[1]=(s[0]!='0' ? 1 : 0)
     *
     * Single digit: if s[i-1] != '0': dp[i] += dp[i-1]
     * Two digits:   if s[i-2..i-1] in [10,26]: dp[i] += dp[i-2]
     *
     * Time: O(n), Space: O(1)
     *
     * Tricky cases: '0' alone is invalid. '00', '30', '27'+ two-digit are invalid.
     */
    public int numDecodings(String s) {
        int n = s.length();
        if (n == 0 || s.charAt(0) == '0') return 0;

        int prev2 = 1;  // dp[0]: one way to decode empty prefix
        int prev1 = 1;  // dp[1]: valid if s[0] != '0'

        for (int i = 2; i <= n; i++) {
            int curr = 0;
            // Single-digit decode: s[i-1]
            if (s.charAt(i-1) != '0') curr += prev1;
            // Two-digit decode: s[i-2..i-1]
            int twoDigit = Integer.parseInt(s.substring(i-2, i));
            if (twoDigit >= 10 && twoDigit <= 26) curr += prev2;
            prev2 = prev1;
            prev1 = curr;
        }

        return prev1;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_HouseRobber sol = new Problem2_HouseRobber();

        System.out.println("=== LC 198: House Robber (Linear) ===");
        System.out.println(sol.rob(new int[]{1,2,3,1}));      // 4
        System.out.println(sol.rob(new int[]{2,7,9,3,1}));    // 12
        System.out.println(sol.rob(new int[]{2,1,1,2}));      // 4

        System.out.println("\n=== DP Table Visualization ===");
        sol.robWithTable(new int[]{2,7,9,3,1});

        System.out.println("\n=== LC 213: House Robber (Circular) ===");
        System.out.println(sol.robCircular(new int[]{2,3,2}));       // 3
        System.out.println(sol.robCircular(new int[]{1,2,3,1}));     // 4
        System.out.println(sol.robCircular(new int[]{1,2,3}));       // 3

        System.out.println("\n=== LC 91: Decode Ways ===");
        System.out.println(sol.numDecodings("12"));     // 2 ("AB" or "L")
        System.out.println(sol.numDecodings("226"));    // 3
        System.out.println(sol.numDecodings("06"));     // 0
        System.out.println(sol.numDecodings("11106"));  // 2
    }
}
