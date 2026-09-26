/**
 * ============================================================
 *  SLIDING WINDOW — HARD PROBLEMS
 *  Advanced window mechanics, monotonic deques, and "exactly K".
 * ============================================================
 *
 *  Problems:
 *   H1. Minimum Window Substring (LC 76)
 *   H2. Sliding Window Maximum (LC 239)
 *   H3. Longest Substring with At Most K Distinct Characters (LC 340)
 *   H4. Subarrays with K Different Integers (LC 992)
 *   H5. Minimum Window Subsequence (LC 727)
 *   H6. Count Number of Nice Subarrays (LC 1248)
 *
 * ============================================================
 */
import java.util.*;

class Hard {

    // =========================================================
    // H1. MINIMUM WINDOW SUBSTRING
    // Pattern: Variable Window — Shortest + Frequency Match
    // LeetCode 76
    // =========================================================
    /**
     * Problem: Given strings s and t, find the minimum window in s
     * that contains ALL characters of t (including duplicates).
     *
     * Approach: Variable window — shrink when valid.
     * 1. Expand right: add s[right] to window frequency.
     * 2. Track "formed" = how many UNIQUE chars in t have their
     *    required frequency met in the window.
     * 3. When formed == required (all chars satisfied), shrink left
     *    to minimize the window.
     *
     * Time:  O(|s| + |t|) — each char enters/leaves once
     * Space: O(|s| + |t|) — frequency maps
     */
    public String minWindow(String s, String t) {
        if (s.length() < t.length()) return "";
        
        Map<Character, Integer> tFreq = new HashMap<>();
        for (char c : t.toCharArray()) tFreq.merge(c, 1, Integer::sum);
        
        int required = tFreq.size();  // unique chars in t
        int formed = 0;               // unique chars with freq met
        Map<Character, Integer> windowFreq = new HashMap<>();
        
        int left = 0;
        int bestLen = Integer.MAX_VALUE, bestLeft = 0;
        
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowFreq.merge(c, 1, Integer::sum);
            
            if (tFreq.containsKey(c) && windowFreq.get(c).intValue() == tFreq.get(c).intValue()) {
                formed++;
            }
            
            // Shrink while valid
            while (formed == required) {
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestLeft = left;
                }
                
                char leftChar = s.charAt(left);
                windowFreq.merge(leftChar, -1, Integer::sum);
                if (tFreq.containsKey(leftChar) && windowFreq.get(leftChar) < tFreq.get(leftChar)) {
                    formed--;
                }
                left++;
            }
        }
        
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestLeft, bestLeft + bestLen);
    }

    /**
     * Follow-up 1: Optimized with filtered indices?
     *   → Pre-filter s to only indices containing chars in t.
     *   → Reduces work when |s| >> |t| and s has many irrelevant chars.
     *
     * Follow-up 2: What if t has no duplicates?
     *   → Simpler: just track a set of "remaining chars needed".
     */

    // =========================================================
    // H2. SLIDING WINDOW MAXIMUM
    // Pattern: Fixed Window + Monotonic Deque
    // LeetCode 239
    // =========================================================
    /**
     * Problem: Given array nums and window size k, return the max
     * of each sliding window.
     *
     * Brute Force O(n·k): For each window, scan for max.
     *
     * Optimal O(n): Monotonic DECREASING deque.
     *   - Deque stores INDICES (not values).
     *   - Front of deque = index of max in current window.
     *   - Before adding i: remove all indices with smaller values
     *     from the back (they can never be the max while i is in window).
     *   - Remove front if it's outside the window.
     *
     * Why O(n)?
     *   Each element is pushed once and popped at most once → 2n deque ops.
     *
     * Time:  O(n)
     * Space: O(k) — deque holds at most k indices
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        Deque<Integer> deque = new ArrayDeque<>();  // monotonic decreasing
        int[] result = new int[nums.length - k + 1];
        
        for (int i = 0; i < nums.length; i++) {
            // Remove indices outside the window
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
                deque.pollFirst();
            }
            
            // Remove indices of elements smaller than current
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.pollLast();
            }
            
            deque.offerLast(i);
            
            if (i >= k - 1) {
                result[i - k + 1] = nums[deque.peekFirst()];
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Sliding window MINIMUM?
     *   → Change deque to monotonic INCREASING (remove larger elements).
     *
     * Follow-up 2: Sliding window median?
     *   → Two heaps (maxHeap + minHeap) with lazy deletion, or balanced BST (TreeMap).
     *   → O(n log k) time.
     */

    // =========================================================
    // H3. LONGEST SUBSTRING WITH AT MOST K DISTINCT CHARACTERS
    // Pattern: Variable Window — Longest + HashMap
    // LeetCode 340
    // =========================================================
    /**
     * Problem: Given string s and int k, find the length of the
     * longest substring with at most k distinct characters.
     *
     * Classic variable window: expand right, shrink left when
     * distinct count exceeds k.
     *
     * Time:  O(n)
     * Space: O(k) — map has at most k+1 entries
     */
    public int lengthOfLongestSubstringKDistinct(String s, int k) {
        Map<Character, Integer> freq = new HashMap<>();
        int left = 0, maxLen = 0;
        
        for (int right = 0; right < s.length(); right++) {
            freq.merge(s.charAt(right), 1, Integer::sum);
            
            while (freq.size() > k) {
                char leftChar = s.charAt(left++);
                freq.merge(leftChar, -1, Integer::sum);
                if (freq.get(leftChar) == 0) freq.remove(leftChar);
            }
            
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    // =========================================================
    // H4. SUBARRAYS WITH K DIFFERENT INTEGERS
    // Pattern: Exactly K = atMost(K) - atMost(K-1)
    // LeetCode 992
    // =========================================================
    /**
     * Problem: Return the number of subarrays with EXACTLY k
     * different integers.
     *
     * Direct sliding window can't handle "exactly K" because both
     * growing and shrinking can toggle between valid/invalid.
     *
     * Key insight: exactly(K) = atMost(K) - atMost(K - 1)
     *
     * atMost(K) counts all subarrays with ≤ K distinct elements.
     * For each valid window [left..right], there are (right - left + 1)
     * valid subarrays ending at right.
     *
     * Time:  O(n) — two passes of O(n) each
     * Space: O(n) — frequency map
     */
    public int subarraysWithKDistinct(int[] nums, int k) {
        return atMostK(nums, k) - atMostK(nums, k - 1);
    }

    private int atMostK(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        int left = 0, count = 0;
        
        for (int right = 0; right < nums.length; right++) {
            freq.merge(nums[right], 1, Integer::sum);
            
            while (freq.size() > k) {
                int leftVal = nums[left++];
                freq.merge(leftVal, -1, Integer::sum);
                if (freq.get(leftVal) == 0) freq.remove(leftVal);
            }
            
            count += right - left + 1;
        }
        return count;
    }

    /**
     * Follow-up: Can we solve this in a single pass?
     *   → Yes, but much more complex. Maintain two left pointers
     *     (leftFar and leftClose) such that windows [leftFar..right]
     *     has ≤ K distinct and [leftClose..right] has exactly K distinct.
     *   → Count = leftClose - leftFar for each right.
     */

    // =========================================================
    // H5. MINIMUM WINDOW SUBSEQUENCE
    // Pattern: Variable Window + Forward/Backward Scan
    // LeetCode 727
    // =========================================================
    /**
     * Problem: Given strings s and t, find the minimum length substring
     * of s that contains t as a SUBSEQUENCE (not anagram!).
     *
     * Approach:
     * 1. Forward scan: find a window containing t as subsequence.
     * 2. Backward scan: minimize the window by shrinking from the right
     *    end of the found subsequence back to tighten the start.
     * 3. Record minimum and continue from start + 1.
     *
     * Time:  O(|s| · |t|) — for each starting position, scan t
     * Space: O(1)
     */
    public String minWindowSubsequence(String s, String t) {
        int bestStart = -1, bestLen = Integer.MAX_VALUE;
        int sIdx = 0;
        
        while (sIdx < s.length()) {
            // Forward: find t as subsequence starting from sIdx
            int tIdx = 0;
            int start = sIdx;
            while (start < s.length() && tIdx < t.length()) {
                if (s.charAt(start) == t.charAt(tIdx)) tIdx++;
                start++;
            }
            if (tIdx < t.length()) break;  // t not found
            
            // start is now one past the end of the found subsequence
            int end = start - 1;
            
            // Backward: tighten the window
            tIdx = t.length() - 1;
            while (tIdx >= 0) {
                if (s.charAt(end) == t.charAt(tIdx)) tIdx--;
                end--;
            }
            end++;  // end is now the start of the tightened window
            
            if (start - end < bestLen) {
                bestLen = start - end;
                bestStart = end;
            }
            
            sIdx = end + 1;  // move past this window
        }
        
        return bestStart == -1 ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    // =========================================================
    // H6. COUNT NUMBER OF NICE SUBARRAYS
    // Pattern: Exactly K = atMost(K) - atMost(K-1)
    // LeetCode 1248
    // =========================================================
    /**
     * Problem: Given array of integers and int k, return the number
     * of subarrays with exactly k ODD numbers.
     *
     * Transform: Treat odd = 1, even = 0.
     * Problem becomes: count subarrays with exactly k ones.
     * Use: exactly(k) = atMost(k) - atMost(k - 1)
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public int numberOfSubarrays(int[] nums, int k) {
        return atMostOdds(nums, k) - atMostOdds(nums, k - 1);
    }

    private int atMostOdds(int[] nums, int k) {
        int left = 0, count = 0, oddCount = 0;
        for (int right = 0; right < nums.length; right++) {
            if (nums[right] % 2 != 0) oddCount++;
            
            while (oddCount > k) {
                if (nums[left] % 2 != 0) oddCount--;
                left++;
            }
            
            count += right - left + 1;
        }
        return count;
    }

    /**
     * Follow-up: Alternative approach using prefix sum?
     *   → prefixOdd[i] = count of odds in nums[0..i].
     *   → For each right, count how many left positions have
     *     prefixOdd[right] - prefixOdd[left-1] == k.
     *   → Use a frequency map of prefix odd counts.
     */
}
