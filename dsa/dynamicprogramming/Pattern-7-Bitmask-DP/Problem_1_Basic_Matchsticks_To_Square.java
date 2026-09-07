/**
 * ============================================================
 *  PATTERN 7 — BITMASK DP
 *  Problem 1 (Basic): Matchsticks to Square   LC 473
 * ============================================================
 *
 *  Difficulty  : Medium (classified "Basic" for pattern entry)
 *  Pattern     : Bitmask DP / Backtracking with Bitmask Pruning
 *  LeetCode    : https://leetcode.com/problems/matchsticks-to-square/
 *
 *  PROBLEM STATEMENT:
 *    You have an array of matchsticks. Can you use ALL of them to form a square?
 *    Every matchstick must be used. You may not break any matchstick.
 *
 *  EXAMPLES:
 *    [1,1,2,2,2]  → true  (sides: 1+2=3, 1+2=3, 2+1=3, 2+1=3 → wait, sum=8, 8/4=2...)
 *                          Actually: sides 2,2,2,2 → [1,1] [2] [2] [2]... sum=8, side=2
 *                          [2],[2],[1+1],[2] → true ✓
 *    [3,3,3,3,4]  → false (sum=16, side=4, but 4-element groups don't work with the 4)
 *
 *  APPROACH OVERVIEW:
 *    Bitmask DP: dp[mask] = true if matchsticks in 'mask' can be distributed
 *                           such that the sides filled so far are complete multiples of target.
 *
 * ============================================================
 */
import java.util.*;

class Problem1_MatchsticksToSquare {

    // =========================================================
    // APPROACH 1 — BACKTRACKING (Classic, interview-ready)
    // =========================================================
    /**
     * INTUITION:
     *   Try to fill 4 buckets (sides), each of target = sum/4.
     *   Assign each matchstick to one of the 4 buckets.
     *
     * PRUNING (critical for performance):
     *   1. If sum % 4 != 0 → impossible.
     *   2. If any matchstick > target → impossible.
     *   3. Sort matchsticks DESCENDING → prune large mismatches early.
     *   4. If two buckets have the same current sum, skip duplicates.
     *   5. If filling a bucket fails completely (even starting fresh), return false.
     *
     * Time:  O(4^n) worst case, but pruning makes it very fast in practice.
     * Space: O(n) — recursion stack.
     *
     * Follow-up: Partition to K Equal Sum Subsets (LC 698)?
     *   Generalize from 4 buckets to k buckets. Same approach.
     */
    public boolean makesquare(int[] matchsticks) {
        int sum = 0;
        for (int m : matchsticks) sum += m;
        if (sum % 4 != 0) return false;

        int target = sum / 4;

        // Sort descending for better pruning
        Integer[] ms = new Integer[matchsticks.length];
        for (int i = 0; i < matchsticks.length; i++) ms[i] = matchsticks[i];
        Arrays.sort(ms, Collections.reverseOrder());

        if (ms[0] > target) return false;  // single stick exceeds one side

        return backtrack(ms, new int[4], 0, target);
    }

    private boolean backtrack(Integer[] ms, int[] sides, int idx, int target) {
        if (idx == ms.length) {
            // All matchsticks placed; valid if all 4 sides equal target
            return sides[0] == target && sides[1] == target && sides[2] == target;
            // sides[3] == target is guaranteed if the above three are true (sum = 4*target)
        }

        Set<Integer> tried = new HashSet<>();  // skip duplicate bucket sums

        for (int i = 0; i < 4; i++) {
            if (tried.contains(sides[i])) continue;  // same sum bucket → same result
            if (sides[i] + ms[idx] <= target) {
                tried.add(sides[i]);
                sides[i] += ms[idx];
                if (backtrack(ms, sides, idx + 1, target)) return true;
                sides[i] -= ms[idx];
            }
        }

        return false;
    }

    // =========================================================
    // APPROACH 2 — BITMASK DP
    // =========================================================
    /**
     * STATE:
     *   dp[mask] = true if the matchsticks in 'mask' can fill complete sides exactly.
     *   "Complete sides" means the total length of sticks in mask is divisible by target.
     *
     * TRANSITION:
     *   To go from mask to mask | (1 << i):
     *     Add matchstick i to the current side being built.
     *     If the new side sum = target (i.e., mask bits' sum % target == 0 after addition),
     *     that side is complete and we start a new one.
     *     Always valid as long as the partial side sum <= target.
     *
     * KEY INVARIANT:
     *   dp[mask] = true means:
     *     (a) The sum of matchsticks in mask is a multiple of target (sides filled so far),
     *     (b) OR we're currently filling a partial side, and that partial is valid.
     *
     *   Simpler reformulation: dp[mask] = total length of sticks in mask
     *   Track: dp[mask] = sum of matchsticks in mask modulo target.
     *     - If dp[mask] == 0, all sticks in mask have been organized into complete sides.
     *     - Transition: dp[mask | (1<<i)] is valid if dp[mask] + ms[i] <= target.
     *       New value: (dp[mask] + ms[i]) % target.
     *
     * ANSWER: dp[fullMask] == 0
     *   (All sticks placed, forming complete sides, with 4 sides total guaranteed by sum)
     *
     * Time:  O(n * 2^n)
     * Space: O(2^n)
     *
     * Follow-up: Why use DP over backtracking here?
     *   DP avoids re-exploring the same subset of matchsticks in different orders.
     *   Backtracking explores 4^n orders; DP only evaluates 2^n subsets once.
     */
    public boolean makesquareDP(int[] matchsticks) {
        int n = matchsticks.length;
        int sum = 0;
        for (int m : matchsticks) sum += m;
        if (sum % 4 != 0) return false;

        int target = sum / 4;
        if (matchsticks[0] > target) return false;  // after sort descending check

        // dp[mask] = sum of sticks in mask modulo target
        // -1 = invalid (cannot form this mask)
        int[] dp = new int[1 << n];
        Arrays.fill(dp, -1);
        dp[0] = 0;  // empty set: 0 mod target = 0

        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] == -1) continue;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue;  // already used
                int newMask = mask | (1 << i);
                if (dp[newMask] != -1) continue;       // already computed
                // Adding matchstick i: new partial side sum
                int newSum = dp[mask] + matchsticks[i];
                if (newSum <= target) {
                    dp[newMask] = newSum % target;
                }
            }
        }

        return dp[(1 << n) - 1] == 0;
    }

    // =========================================================
    // APPROACH 3 — GENERALIZED: Partition into K Equal Subsets (LC 698)
    // =========================================================
    /**
     * Same problem but generalize from 4 sides to k sides.
     * Bitmask DP works for k up to ~20 (since we have 2^n states).
     *
     * dp[mask] = current partial sum of the side being built, given sticks 'mask' used.
     * Transition: if dp[mask] + nums[i] <= target:
     *   dp[mask | (1<<i)] = (dp[mask] + nums[i]) % target
     * Answer: dp[fullMask] == 0
     */
    public boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = 0;
        for (int x : nums) sum += x;
        if (sum % k != 0) return false;

        int target = sum / k;
        Arrays.sort(nums);
        int n = nums.length;

        if (nums[n - 1] > target) return false;

        int[] dp = new int[1 << n];
        Arrays.fill(dp, -1);
        dp[0] = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] == -1) continue;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue;
                int newSum = dp[mask] + nums[i];
                if (newSum <= target) {
                    dp[mask | (1 << i)] = newSum % target;
                }
            }
        }

        return dp[(1 << n) - 1] == 0;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_MatchsticksToSquare sol = new Problem1_MatchsticksToSquare();

        int[][] tests = {
            {1, 1, 2, 2, 2},       // true
            {3, 3, 3, 3, 4},       // false
            {1, 1, 1, 1},          // true (all sides = 1)
            {5, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 3},  // true
        };

        for (int[] t : tests) {
            System.out.println("Input: " + Arrays.toString(t));
            System.out.println("  Backtracking : " + sol.makesquare(t));
            System.out.println("  Bitmask DP   : " + sol.makesquareDP(t));
            System.out.println();
        }

        System.out.println("=== Partition K Equal Subsets (LC 698) ===");
        System.out.println(sol.canPartitionKSubsets(new int[]{4,3,2,3,5,2,1}, 4)); // true
        System.out.println(sol.canPartitionKSubsets(new int[]{1,2,3,4}, 3));        // false
    }
}
