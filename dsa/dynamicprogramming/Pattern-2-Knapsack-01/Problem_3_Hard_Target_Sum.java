/**
 * ============================================================
 *  PATTERN 2 — 0/1 KNAPSACK DP
 *  Problem 3 (Hard): Target Sum   LC 494
 * ============================================================
 *
 *  Difficulty  : Medium-Hard
 *  Pattern     : 0/1 Knapsack — count subsets (algebraic transformation)
 *  LeetCode    : https://leetcode.com/problems/target-sum/
 *
 *  PROBLEM STATEMENT:
 *    Given integer array nums and integer target. Assign '+' or '-' to each number.
 *    Count the number of ways to achieve expression sum == target.
 *
 *  EXAMPLES:
 *    nums=[1,1,1,1,1], target=3  → 5
 *    nums=[1], target=1          → 1
 *    nums=[1,0], target=1        → 2 (+1+0 and +1-0)
 *
 *  ALGEBRAIC TRANSFORMATION (key insight):
 *    Let P = set of elements assigned '+', N = set assigned '-'.
 *    P ∪ N = all elements, P - N = target.
 *    P + N = total (sum of all elements).
 *    Adding: 2P = total + target → P = (total + target) / 2
 *    So: COUNT SUBSETS SUMMING TO P.
 *    Conditions: (total + target) must be even AND non-negative.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_TargetSum {

    // =========================================================
    // APPROACH 1 — ALGEBRAIC REDUCTION + COUNT SUBSETS DP (optimal)
    // =========================================================
    /**
     * Reduce to: count subsets of nums summing to S = (total + target) / 2.
     *
     * dp[j] = number of subsets summing to j
     * BASE: dp[0] = 1 (empty subset sums to 0 in exactly 1 way)
     * TRANSITION (backwards): dp[j] += dp[j - num]
     *
     * CAREFUL: This is counting, not boolean. Multiple paths to the same sum are counted.
     *
     * EDGE CASES:
     *   If |target| > total → 0 ways (impossible).
     *   If (total + target) % 2 != 0 → 0 ways (P is not an integer).
     *
     * VISUAL for nums=[1,1,1,1,1], target=3:
     *   total=5, P=(5+3)/2=4, count subsets summing to 4.
     *   dp = [1,0,0,0,0] (size 5: indices 0..4)
     *
     *   Process 1 (j from 4 to 1):
     *     j=1: dp[1]+=dp[0]=1
     *   dp = [1,1,0,0,0]
     *
     *   Process 1 again:
     *     j=2: dp[2]+=dp[1]=1, j=1: dp[1]+=dp[0]=1
     *   dp = [1,2,1,0,0]
     *
     *   Process 1 again: dp = [1,3,3,1,0]
     *   Process 1 again: dp = [1,4,6,4,1]
     *   Process 1 again: dp = [1,5,10,10,5]
     *   dp[4] = 5 ✓
     *
     * Time: O(n × S), Space: O(S)
     *
     * Follow-up: What if numbers can be negative?
     *   Shift all numbers by +min to make them non-negative, adjust target.
     * Follow-up: What if values are large (S could be huge)?
     *   Use a HashMap<Integer, Long> instead of an array.
     */
    public int findTargetSumWays(int[] nums, int target) {
        int total = 0;
        for (int n : nums) total += n;

        if (Math.abs(target) > total) return 0;
        if ((total + target) % 2 != 0) return 0;

        int S = (total + target) / 2;  // sum of the '+' subset
        return countSubsets(nums, S);
    }

    private int countSubsets(int[] nums, int S) {
        if (S < 0) return 0;
        int[] dp = new int[S + 1];
        dp[0] = 1;
        for (int num : nums) {
            for (int j = S; j >= num; j--) {  // ← BACKWARDS (0/1: no reuse)
                dp[j] += dp[j - num];
            }
        }
        return dp[S];
    }

    // =========================================================
    // APPROACH 2 — RECURSIVE BACKTRACKING WITH MEMOIZATION
    // =========================================================
    /**
     * At each index, try assigning '+' or '-'. Memoize on (index, currentSum).
     *
     * State: (idx, remainingTarget) → number of ways to assign signs to nums[idx..n-1]
     *        such that their contribution makes remainingTarget.
     *
     * Base: if idx == n, return 1 if remainingTarget == 0, else 0.
     *
     * Memo key: idx * (2*totalSum+1) + remainingTarget + totalSum
     *   (shift to handle negative remaining targets)
     *
     * Time: O(n × 2*total) — unique states
     * Space: O(n × 2*total) — memo
     */
    private Map<String, Integer> memo2;

    public int findTargetSumWaysMemo(int[] nums, int target) {
        memo2 = new HashMap<>();
        return memoHelper(nums, 0, target);
    }

    private int memoHelper(int[] nums, int idx, int remaining) {
        if (idx == nums.length) return remaining == 0 ? 1 : 0;

        String key = idx + "," + remaining;
        if (memo2.containsKey(key)) return memo2.get(key);

        int plus  = memoHelper(nums, idx + 1, remaining - nums[idx]);
        int minus = memoHelper(nums, idx + 1, remaining + nums[idx]);
        int total = plus + minus;
        memo2.put(key, total);
        return total;
    }

    // =========================================================
    // APPROACH 3 — BRUTE FORCE BACKTRACKING (for small n)
    // =========================================================
    /**
     * Try all 2^n sign assignments. Time: O(2^n).
     */
    private int bruteCount;

    public int findTargetSumWaysBrute(int[] nums, int target) {
        bruteCount = 0;
        bruteHelper(nums, 0, 0, target);
        return bruteCount;
    }

    private void bruteHelper(int[] nums, int idx, int current, int target) {
        if (idx == nums.length) {
            if (current == target) bruteCount++;
            return;
        }
        bruteHelper(nums, idx + 1, current + nums[idx], target);
        bruteHelper(nums, idx + 1, current - nums[idx], target);
    }

    // =========================================================
    // BONUS: 2D DP Version (full table for visualization)
    // =========================================================
    /**
     * dp[i][j] = number of ways to assign signs to nums[0..i-1] achieving sum j.
     * Offset: shift sums by total to handle negative indices.
     *
     * Time: O(n × 2*total), Space: O(n × 2*total)
     */
    public int findTargetSumWays2D(int[] nums, int target) {
        int n = nums.length;
        int total = 0;
        for (int num : nums) total += num;

        if (Math.abs(target) > total) return 0;

        int offset = total;  // shift index: sum s stored at index s + offset
        int size = 2 * total + 1;
        int[][] dp = new int[n + 1][size];
        dp[0][offset] = 1;  // before processing any element, sum=0 has 1 way

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < size; j++) {
                if (dp[i-1][j] == 0) continue;
                // Assign '+'
                if (j + nums[i-1] < size) dp[i][j + nums[i-1]] += dp[i-1][j];
                // Assign '-'
                if (j - nums[i-1] >= 0)  dp[i][j - nums[i-1]] += dp[i-1][j];
            }
        }

        return dp[n][target + offset];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_TargetSum sol = new Problem3_TargetSum();

        System.out.println("=== LC 494: Target Sum ===");

        Object[][] tests = {
            {new int[]{1,1,1,1,1}, 3},   // 5
            {new int[]{1}, 1},             // 1
            {new int[]{1, 0}, 1},          // 2
            {new int[]{0,0,0,0,0,0,0,0,1}, 1}, // 256
        };

        for (Object[] t : tests) {
            int[] nums = (int[]) t[0];
            int target  = (int) t[1];
            System.out.println("nums=" + Arrays.toString(nums) + ", target=" + target);
            System.out.println("  Algebraic DP  : " + sol.findTargetSumWays(nums, target));
            System.out.println("  Memoization   : " + sol.findTargetSumWaysMemo(nums, target));
            System.out.println("  Brute Force   : " + sol.findTargetSumWaysBrute(nums, target));
            System.out.println("  2D DP         : " + sol.findTargetSumWays2D(nums, target));
            System.out.println();
        }
    }
}
