/**
 * ============================================================
 *  PATTERN 1 — LINEAR DP (1D)
 *  Problem 3 (Hard): Longest Increasing Subsequence   LC 300
 * ============================================================
 *
 *  Difficulty  : Medium-Hard (marked Hard as the final Linear DP problem)
 *  Pattern     : Linear DP (O(n²)) + Binary Search Optimization (O(n log n))
 *  LeetCode    : https://leetcode.com/problems/longest-increasing-subsequence/
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array nums, return the length of the longest strictly
 *    increasing subsequence.
 *
 *  EXAMPLES:
 *    [10,9,2,5,3,7,101,18]  → 4  ([2,3,7,101] or [2,3,7,18])
 *    [0,1,0,3,2,3]          → 4
 *    [7,7,7,7,7,7,7]        → 1
 *
 *  WHY THIS PROBLEM MATTERS:
 *    Demonstrates two fundamentally different DP approaches:
 *    1. O(n²) DP — classic, easy to understand
 *    2. O(n log n) Patience Sorting — binary search on a "tails" array
 *    The latter requires understanding the invariant maintenance deeply.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_LongestIncreasingSubsequence {

    // =========================================================
    // APPROACH 1 — CLASSIC DP: O(n²)
    // =========================================================
    /**
     * STATE:
     *   dp[i] = length of LIS ending EXACTLY at index i
     *
     * TRANSITION:
     *   dp[i] = 1 + max(dp[j]) for all j < i where nums[j] < nums[i]
     *   (extend any previous LIS that ends with a smaller element)
     *
     * BASE: dp[i] = 1 (every element alone is an LIS of length 1)
     *
     * ANSWER: max(dp[i]) over all i — the LIS might end at any index
     *
     * VISUAL for [10,9,2,5,3,7,101,18]:
     *   dp[0]=1 (LIS ending at 10: [10])
     *   dp[1]=1 (9<10, can't extend dp[0]; LIS: [9])
     *   dp[2]=1 (2<9,2<10; can't extend either; LIS: [2])
     *   dp[3]=2 (5>2 → extend dp[2]: [2,5])
     *   dp[4]=2 (3>2 → extend dp[2]: [2,3])
     *   dp[5]=3 (7>5, extend dp[3]→3; 7>3, extend dp[4]→3; LIS: [2,3,7])
     *   dp[6]=4 (101>everything, extend dp[5]→4; LIS: [2,3,7,101])
     *   dp[7]=4 (18>7, extend dp[5]→4; LIS: [2,3,7,18])
     *   Answer: max(dp) = 4 ✓
     *
     * Time: O(n²), Space: O(n)
     *
     * Follow-up: Print the actual LIS?
     *   Store parent[i] = j (which j maximized dp[i]).
     *   Backtrack from the index of max(dp[i]).
     */
    public int lengthOfLIS(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int maxLen = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }

        return maxLen;
    }

    // =========================================================
    // APPROACH 2 — BINARY SEARCH (Patience Sorting): O(n log n)
    // =========================================================
    /**
     * KEY IDEA — THE "TAILS" ARRAY:
     *   Maintain an array 'tails' where tails[len-1] = the SMALLEST possible tail
     *   element of any IS of length 'len' seen so far.
     *
     * INVARIANT: tails[] is always SORTED (strictly increasing).
     *
     * PROCESS each nums[i]:
     *   Binary search for the LEFTMOST index in tails[] where tails[pos] >= nums[i].
     *   (= first position where we can replace tails[pos] with nums[i])
     *
     *   Case 1: nums[i] > all of tails[] → EXTEND: tails[size] = nums[i]; size++
     *   Case 2: nums[i] <= tails[pos]   → REPLACE: tails[pos] = nums[i]
     *     (maintains the invariant with a smaller tail, enabling more future extensions)
     *
     * ANSWER: final 'size' of the valid portion of tails[] = LIS length
     *
     * NOTE: tails[] does NOT store the actual LIS. It's a "patience sort" pile structure.
     *   To reconstruct the actual LIS, you need the O(n²) DP with parent tracking.
     *
     * VISUAL for [10,9,2,5,3,7,101,18]:
     *   Process 10: tails=[], 10>all → [10], size=1
     *   Process  9: tails=[10], 9<10, pos=0 → replace: [9], size=1
     *   Process  2: tails=[9],  2<9,  pos=0 → replace: [2], size=1
     *   Process  5: tails=[2],  5>2,  pos=1 → extend:  [2,5], size=2
     *   Process  3: tails=[2,5], 3 between 2 and 5, pos=1 → replace: [2,3], size=2
     *   Process  7: tails=[2,3], 7>3, pos=2 → extend: [2,3,7], size=3
     *   Process 101: tails=[2,3,7], 101>7, pos=3 → extend: [2,3,7,101], size=4
     *   Process 18: tails=[2,3,7,101], 18<101, pos=3 → replace: [2,3,7,18], size=4
     *   Answer: size=4 ✓
     *
     *   Note: final tails=[2,3,7,18] is NOT the LIS itself, but its LENGTH is correct.
     *   (The actual LIS [2,3,7,18] happens to match here, but this is coincidental.)
     *
     * Time: O(n log n), Space: O(n)
     *
     * Follow-up: LIS variant — Non-decreasing (allow equal)?
     *   Use upper_bound instead of lower_bound (replace at tails[pos] > nums[i]).
     */
    public int lengthOfLISBinarySearch(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;

        for (int num : nums) {
            int lo = 0, hi = size;
            // Binary search: find leftmost tails[pos] >= num (lower_bound)
            while (lo < hi) {
                int mid = (lo + hi) / 2;
                if (tails[mid] < num) lo = mid + 1;
                else hi = mid;
            }
            tails[lo] = num;        // replace or extend
            if (lo == size) size++; // extended the tails array
        }

        return size;
    }

    // =========================================================
    // APPROACH 3 — RECONSTRUCT ACTUAL LIS
    // =========================================================
    /**
     * Use the O(n²) DP with a parent array to reconstruct the actual LIS.
     */
    public List<Integer> findActualLIS(int[] nums) {
        int n = nums.length;
        int[] dp     = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dp, 1);
        Arrays.fill(parent, -1);
        int maxLen = 1, endIdx = 0;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i] && dp[j] + 1 > dp[i]) {
                    dp[i] = dp[j] + 1;
                    parent[i] = j;
                }
            }
            if (dp[i] > maxLen) {
                maxLen = dp[i];
                endIdx = i;
            }
        }

        // Backtrack to find LIS
        List<Integer> lis = new ArrayList<>();
        for (int idx = endIdx; idx != -1; idx = parent[idx]) {
            lis.add(nums[idx]);
        }
        Collections.reverse(lis);
        return lis;
    }

    // =========================================================
    // BONUS: Number of Longest Increasing Subsequences (LC 673)
    // =========================================================
    /**
     * count[i] = number of LIS of length dp[i] ending at index i.
     *
     * If dp[j] + 1 == dp[i]: this is a new optimal → count[i] += count[j]
     * If dp[j] + 1 == dp[i] and already optimal: accumulated count[i] += count[j]
     */
    public int findNumberOfLIS(int[] nums) {
        int n = nums.length;
        int[] dp    = new int[n];
        int[] count = new int[n];
        Arrays.fill(dp, 1);
        Arrays.fill(count, 1);
        int maxLen = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    if (dp[j] + 1 > dp[i]) {
                        dp[i] = dp[j] + 1;
                        count[i] = count[j];      // new best: take j's count
                    } else if (dp[j] + 1 == dp[i]) {
                        count[i] += count[j];     // tie: accumulate
                    }
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }

        int totalCount = 0;
        for (int i = 0; i < n; i++) {
            if (dp[i] == maxLen) totalCount += count[i];
        }
        return totalCount;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_LongestIncreasingSubsequence sol = new Problem3_LongestIncreasingSubsequence();

        int[][] tests = {
            {10,9,2,5,3,7,101,18},   // 4
            {0,1,0,3,2,3},            // 4
            {7,7,7,7,7,7,7},          // 1
            {4,10,4,3,8,9},           // 3
        };

        System.out.println("=== LC 300: Longest Increasing Subsequence ===");
        for (int[] nums : tests) {
            System.out.println("nums = " + Arrays.toString(nums));
            System.out.println("  O(n²) DP    : " + sol.lengthOfLIS(nums));
            System.out.println("  O(n logn) BS: " + sol.lengthOfLISBinarySearch(nums));
            System.out.println("  Actual LIS  : " + sol.findActualLIS(nums));
            System.out.println();
        }

        System.out.println("=== LC 673: Number of LIS ===");
        System.out.println(sol.findNumberOfLIS(new int[]{1,3,5,4,7}));         // 2
        System.out.println(sol.findNumberOfLIS(new int[]{2,2,2,2,2}));         // 5
        System.out.println(sol.findNumberOfLIS(new int[]{1,2,4,3,5,4,7,2}));   // 3
    }
}
