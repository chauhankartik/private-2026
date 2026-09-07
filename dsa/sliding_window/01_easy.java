
/**
 * ============================================================
 *  SLIDING WINDOW — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Maximum Sum Subarray of Size K
 *   E2. Max Consecutive Ones
 *   E3. Contains Duplicate II
 *   E4. Maximum Number of Vowels in a Substring of Given Length
 *   E5. Minimum Difference Between Highest and Lowest of K Scores
 *   E6. Defuse the Bomb (Circular Fixed Window)
 *   E7. Diet Plan Performance
 *   E8. Number of Sub-arrays of Size K with Average ≥ Threshold
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Easy {

    // =========================================================
    // E1. MAXIMUM SUM SUBARRAY OF SIZE K
    // Pattern: Fixed-Size Sliding Window
    // =========================================================
    /**
     * Problem: Given an array of integers and an integer k, find the
     * maximum sum of any contiguous subarray of size k.
     *
     * Brute Force O(n·k): For each starting index i, compute sum of k elements.
     *
     * Optimal O(n): Fixed sliding window.
     * 1. Compute sum of first k elements.
     * 2. Slide: add arr[i], remove arr[i - k].
     * 3. Track max.
     *
     * Why does this work?
     * Consecutive windows of size k differ by exactly TWO elements:
     * one entering (right) and one leaving (left). We update in O(1).
     *
     * Time: O(n) — single pass after initial window
     * Space: O(1) — just sum and max
     */
    public int maxSumSubarrayOfSizeK(int[] arr, int k) {
        int windowSum = 0;
        for (int i = 0; i < k; i++)
            windowSum += arr[i];

        int maxSum = windowSum;
        for (int i = k; i < arr.length; i++) {
            windowSum += arr[i] - arr[i - k];
            maxSum = Math.max(maxSum, windowSum);
        }
        return maxSum;
    }

    /**
     * Follow-up 1: Find the starting index of the maximum sum subarray?
     * → Track the index when maxSum is updated: bestStart = i - k + 1.
     *
     * Follow-up 2: What if k > arr.length?
     * → Return -1 or handle edge case. Always check constraints.
     *
     * Follow-up 3: Minimum sum subarray of size k?
     * → Same template, change max to min.
     */

    // =========================================================
    // E2. MAX CONSECUTIVE ONES
    // Pattern: Variable Window (simplest form)
    // LeetCode 485
    // =========================================================
    /**
     * Problem: Given a binary array nums, return the maximum number
     * of consecutive 1's in the array.
     *
     * This is the simplest sliding window: no shrinking needed.
     * Just track the current streak length.
     *
     * Time: O(n) — single pass
     * Space: O(1)
     */
    public int findMaxConsecutiveOnes(int[] nums) {
        int maxLen = 0, count = 0;
        for (int num : nums) {
            if (num == 1) {
                count++;
                maxLen = Math.max(maxLen, count);
            } else {
                count = 0;
            }
        }
        return maxLen;
    }

    /**
     * Follow-up 1: Flip at most ONE zero? (LC 487)
     * → Sliding window: track count of zeros in window.
     * → Shrink when zeros > 1.
     *
     * public int findMaxConsecutiveOnesII(int[] nums) {
     * int left = 0, res = 0, zeroCount = 0;
     * for(int right = 0; right < nums.length; right++) {
     * if(nums[right] == 0) zeroCount++;
     * while(zeroCount > 1) {
     * if(nums[left] == 0) zeroCount--;
     * left++;
     * }
     * res = Math.max(res, right - left + 1);
     * }
     * return res;
     * }
     *
     * Follow-up 2: Flip at most K zeros? (LC 1004)
     *
     * public int longestOnes(int[] nums, int k) {
     * int left = 0, res = 0, zeroCount = 0;
     * for(int right = 0; right < nums.length; right++) {
     * if(nums[right] == 0) zeroCount++;
     * while(zeroCount > k) {
     * if(nums[left] == 0) zeroCount--;
     * left++;
     * }
     * res = Math.max(res, right - left + 1);
     * }
     * return res;
     * }
     */

    // =========================================================
    // E3. CONTAINS DUPLICATE II
    // Pattern: Fixed-Size Sliding Window + HashSet
    // LeetCode 219
    // =========================================================
    /**
     * Problem: Given nums and int k, return true if there are two
     * distinct indices i and j such that nums[i] == nums[j] and |i - j| <= k.
     *
     * Approach: Maintain a HashSet as a sliding window of size k.
     * - Add each element. If add() returns false → duplicate within range.
     * - When window exceeds k, remove the oldest element.
     *
     * Time: O(n) — each element added/removed from set once
     * Space: O(k) — set holds at most k+1 elements
     */
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Set<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if (!window.add(nums[i]))
                return true; // duplicate found
            if (window.size() > k) {
                window.remove(nums[i - k]); // shrink window
            }
        }
        return false;
    }

    /**
     * Follow-up: Contains Duplicate III (LC 220) — values differ by at most t,
     * indices by at most k?
     * → Use TreeSet with floor/ceiling queries. See Hard problems.
     */

    // =========================================================
    // E4. MAXIMUM NUMBER OF VOWELS IN A SUBSTRING OF GIVEN LENGTH
    // Pattern: Fixed-Size Sliding Window + Counter
    // LeetCode 1456
    // =========================================================
    /**
     * Problem: Given a string s and integer k, return the maximum number
     * of vowels in any substring of length k.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int maxVowels(String s, int k) {
        Set<Character> vowels = Set.of('a', 'e', 'i', 'o', 'u');

        int count = 0;
        for (int i = 0; i < k; i++) {
            if (vowels.contains(s.charAt(i)))
                count++;
        }

        int maxCount = count;
        for (int i = k; i < s.length(); i++) {
            if (vowels.contains(s.charAt(i)))
                count++; // add right
            if (vowels.contains(s.charAt(i - k)))
                count--; // remove left
            maxCount = Math.max(maxCount, count);
        }
        return maxCount;
    }

    /**
     * Follow-up: What if we want consonants instead?
     * → Flip the condition: !vowels.contains(...)
     *
     * Follow-up: What if the window size is variable — longest substring with at
     * most k vowels?
     * → Variable sliding window template.
     */

    // =========================================================
    // E5. MINIMUM DIFFERENCE BETWEEN HIGHEST AND LOWEST OF K SCORES
    // Pattern: Sort + Fixed Window
    // LeetCode 1984
    // =========================================================
    /**
     * Problem: Given array nums and integer k, pick k scores such that
     * the difference between max and min is minimized.
     *
     * Key insight: Sort the array. Then the k closest scores are always
     * a contiguous subarray. Slide a window of size k and track min difference.
     *
     * Time: O(n log n) for sort + O(n) for sliding window
     * Space: O(1) extra (or O(n) for sort)
     */
    public int minimumDifference(int[] nums, int k) {
        Arrays.sort(nums);
        int minDiff = Integer.MAX_VALUE;
        for (int i = k - 1; i < nums.length; i++) {
            minDiff = Math.min(minDiff, nums[i] - nums[i - k + 1]);
        }
        return minDiff;
    }

    // =========================================================
    // E6. DEFUSE THE BOMB
    // Pattern: Fixed-Size Circular Sliding Window
    // LeetCode 1652
    // =========================================================
    /**
     * Problem: Circular array code and int k.
     * Replace each element with sum of next k (if k>0) or previous |k| (if k<0).
     *
     * Key insight: Use modular arithmetic for circular indexing.
     *
     * Time: O(n)
     * Space: O(n) for result
     */
    public int[] decrypt(int[] code, int k) {
        int n = code.length;
        int[] result = new int[n];
        if (k == 0)
            return result;

        int start = k > 0 ? 1 : n + k;
        int end = k > 0 ? k : n - 1;

        int windowSum = 0;
        for (int i = start; i <= end; i++)
            windowSum += code[i];

        for (int i = 0; i < n; i++) {
            result[i] = windowSum;
            windowSum -= code[start % n];
            start++;
            end++;
            windowSum += code[end % n];
        }
        return result;
    }

    // =========================================================
    // E7. DIET PLAN PERFORMANCE
    // Pattern: Fixed-Size Sliding Window + Conditional Counting
    // LeetCode 1176
    // =========================================================
    /**
     * Problem: Calories per day array, window size k.
     * If window sum < lower → lose 1 point. If > upper → gain 1 point.
     * Return total points.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int dietPlanPerformance(int[] calories, int k, int lower, int upper) {
        int windowSum = 0, points = 0;
        for (int i = 0; i < k; i++)
            windowSum += calories[i];

        if (windowSum < lower)
            points--;
        if (windowSum > upper)
            points++;

        for (int i = k; i < calories.length; i++) {
            windowSum += calories[i] - calories[i - k];
            if (windowSum < lower)
                points--;
            if (windowSum > upper)
                points++;
        }
        return points;
    }

    // =========================================================
    // E8. NUMBER OF SUB-ARRAYS OF SIZE K WITH AVERAGE ≥ THRESHOLD
    // Pattern: Fixed-Size Sliding Window + Counting
    // LeetCode 1343
    // =========================================================
    /**
     * Problem: Count subarrays of size k whose average is >= threshold.
     *
     * Trick: Instead of computing average (float division), compare
     * sum >= threshold * k (integer comparison — faster, no precision issues).
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int numOfSubarrays(int[] arr, int k, int threshold) {
        int windowSum = 0, count = 0;
        int target = threshold * k; // compare sum directly

        for (int i = 0; i < k; i++)
            windowSum += arr[i];
        if (windowSum >= target)
            count++;

        for (int i = k; i < arr.length; i++) {
            windowSum += arr[i] - arr[i - k];
            if (windowSum >= target)
                count++;
        }
        return count;
    }

    /**
     * Follow-up: What if k is not fixed — find shortest subarray with average ≥
     * threshold?
     * → Binary search on answer + prefix sums, or deque-based approach. Much
     * harder.
     */
}
