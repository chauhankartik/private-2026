/**
 * ============================================================
 *  PATTERN 2 — 0/1 KNAPSACK DP
 *  Problem 2 (Medium): Partition Equal Subset Sum   LC 416
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : 0/1 Knapsack — subset sum (boolean variant)
 *  LeetCode    : https://leetcode.com/problems/partition-equal-subset-sum/
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array nums, return true if you can partition it into
 *    two subsets with equal sums.
 *
 *  EXAMPLES:
 *    [1,5,11,5]  → true  ([1,5,5] and [11])
 *    [1,2,3,5]   → false
 *    [1,2,5]     → false
 *
 *  KEY REDUCTION:
 *    Let total = sum(nums).
 *    If total is ODD → impossible (can't split evenly).
 *    If total is EVEN → find a subset summing to target = total / 2.
 *    This is exactly 0/1 Knapsack (Subset Sum variant).
 *
 * ============================================================
 */
import java.util.*;

class Problem2_PartitionEqualSubset {

    // =========================================================
    // APPROACH 1 — 1D DP (optimal, O(n × target))
    // =========================================================
    /**
     * dp[j] = true if we can form sum j using a subset of nums seen so far.
     *
     * BASE: dp[0] = true (empty subset sums to 0)
     *
     * TRANSITION: for each num, for j from target DOWN to num:
     *   dp[j] = dp[j] || dp[j - num]
     *   (either don't take num: dp[j] stays, or take num: check if j-num was reachable)
     *
     * BACKWARDS because this is 0/1 (no reuse). Forward would give unbounded.
     *
     * EARLY TERMINATION: if dp[target] becomes true, return immediately.
     *
     * VISUAL for [1,5,11,5], target=11:
     *   dp = [T,F,F,F,F,F,F,F,F,F,F,F] (size 12: 0..11)
     *
     *   Process 1 (j from 11 down to 1):
     *     j=1: dp[1] |= dp[0]=T → dp[1]=T
     *   dp = [T,T,F,F,F,F,F,F,F,F,F,F]
     *
     *   Process 5 (j from 11 down to 5):
     *     j=11: dp[11]|=dp[6]=F → F
     *     j=6:  dp[6] |=dp[1]=T → T
     *     j=5:  dp[5] |=dp[0]=T → T
     *   dp = [T,T,F,F,F,T,T,F,F,F,F,F]
     *
     *   Process 11 (j from 11 down to 11):
     *     j=11: dp[11]|=dp[0]=T → T! ← FOUND
     *   Return true ✓
     *
     * Time: O(n × target), Space: O(target)
     *
     * Follow-up: Minimum subset sum difference?
     *   Run subset sum for all sums up to total/2.
     *   Answer = min(total - 2*j) for all j in [0, total/2] where dp[j]=true.
     */
    public boolean canPartition(int[] nums) {
        int total = 0;
        for (int n : nums) total += n;

        if (total % 2 != 0) return false;
        int target = total / 2;

        boolean[] dp = new boolean[target + 1];
        dp[0] = true;

        for (int num : nums) {
            for (int j = target; j >= num; j--) {  // ← BACKWARDS
                dp[j] = dp[j] || dp[j - num];
                if (dp[target]) return true;  // early exit
            }
        }

        return dp[target];
    }

    // =========================================================
    // APPROACH 2 — 2D DP (full table, more instructive)
    // =========================================================
    /**
     * dp[i][j] = can we form sum j using nums[0..i-1]?
     * Time: O(n × target), Space: O(n × target)
     */
    public boolean canPartition2D(int[] nums) {
        int total = 0;
        for (int n : nums) total += n;
        if (total % 2 != 0) return false;
        int target = total / 2;

        int n = nums.length;
        boolean[][] dp = new boolean[n + 1][target + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = true;  // sum=0 always reachable

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= target; j++) {
                dp[i][j] = dp[i-1][j];  // skip nums[i-1]
                if (nums[i-1] <= j)
                    dp[i][j] = dp[i][j] || dp[i-1][j - nums[i-1]];  // take nums[i-1]
            }
        }

        return dp[n][target];
    }

    // =========================================================
    // APPROACH 3 — BITSET TRICK (ultra-fast in practice)
    // =========================================================
    /**
     * Encode the reachable sums as bits in a long integer.
     * For each num, shift the bitset left by 'num' positions and OR with itself.
     *
     * bits |= (bits << num)
     * means: for every sum j reachable before, j+num is also now reachable.
     *
     * Answer: check if bit at position 'target' is set.
     *
     * Works when target <= 63 (Java long = 64 bits). For larger targets, use BigInteger.
     *
     * Time: O(n) amortized (bitwise ops on longs are O(1)), Space: O(1)
     */
    public boolean canPartitionBitset(int[] nums) {
        int total = 0;
        for (int n : nums) total += n;
        if (total % 2 != 0) return false;
        int target = total / 2;

        if (target > 63) {
            // Fall back to standard DP for large targets
            return canPartition(nums);
        }

        long bits = 1L;  // bit 0 is set: sum 0 is reachable
        for (int num : nums) {
            bits |= (bits << num);
        }
        return ((bits >> target) & 1L) == 1L;
    }

    // =========================================================
    // BONUS: Minimum Subset Sum Difference
    // =========================================================
    /**
     * Split array into two subsets to MINIMIZE |sum1 - sum2|.
     *
     * Run subset sum DP to find all reachable sums in [0, total/2].
     * Answer = min(total - 2*j) for reachable j closest to total/2.
     */
    public int minimumSubsetSumDiff(int[] nums) {
        int total = 0;
        for (int n : nums) total += n;
        int half = total / 2;

        boolean[] dp = new boolean[half + 1];
        dp[0] = true;

        for (int num : nums) {
            for (int j = half; j >= num; j--) {
                dp[j] = dp[j] || dp[j - num];
            }
        }

        // Find largest reachable sum <= total/2
        for (int j = half; j >= 0; j--) {
            if (dp[j]) return total - 2 * j;  // minimize |S1 - S2| = total - 2*S1
        }
        return total;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_PartitionEqualSubset sol = new Problem2_PartitionEqualSubset();

        System.out.println("=== LC 416: Partition Equal Subset Sum ===");

        int[][] tests = {
            {1, 5, 11, 5},   // true
            {1, 2, 3, 5},    // false
            {1, 2, 5},       // false
            {2, 2, 1, 1},    // true
            {3, 3, 3, 4, 5}, // true (target=9: 3+3+3=9)
        };

        for (int[] nums : tests) {
            System.out.println("nums = " + Arrays.toString(nums));
            System.out.println("  1D DP   : " + sol.canPartition(nums));
            System.out.println("  2D DP   : " + sol.canPartition2D(nums));
            System.out.println("  Bitset  : " + sol.canPartitionBitset(nums));
            System.out.println("  Min Diff: " + sol.minimumSubsetSumDiff(nums));
            System.out.println();
        }
    }
}
