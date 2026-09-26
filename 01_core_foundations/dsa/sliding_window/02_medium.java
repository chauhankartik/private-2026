/**
 * ============================================================
 *  SLIDING WINDOW — MEDIUM PROBLEMS
 *  Variable-size windows, frequency matching, and counting.
 * ============================================================
 *
 *  Problems:
 *   M1. Longest Substring Without Repeating Characters (LC 3)
 *   M2. Max Consecutive Ones III (LC 1004)
 *   M3. Fruit Into Baskets (LC 904)
 *   M4. Longest Repeating Character Replacement (LC 424)
 *   M5. Permutation in String (LC 567)
 *   M6. Find All Anagrams in a String (LC 438)
 *   M7. Subarray Product Less Than K (LC 713)
 *   M8. Grumpy Bookstore Owner (LC 1052)
 *
 * ============================================================
 */
import java.util.*;

class Medium {

    // =========================================================
    // M1. LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS
    // Pattern: Variable Window — Longest + HashMap/Set
    // LeetCode 3
    // =========================================================
    /**
     * Problem: Given string s, find the length of the longest substring
     * without repeating characters.
     *
     * Brute Force O(n³): Check all substrings, verify uniqueness with set.
     * Better O(n²): Sliding window + set, but reset on every duplicate.
     *
     * Optimal O(n): Variable sliding window with HashMap storing
     * each character's LAST SEEN INDEX.
     *   - When a duplicate is found at index i, jump left pointer to
     *     max(left, lastSeen[char] + 1).
     *   - This avoids shrinking one-by-one.
     *
     * Why max(left, lastSeen + 1)?
     *   The last seen index might be BEFORE the current left pointer
     *   (already outside the window). We must not move left backwards.
     *
     * Time:  O(n) — each character visited once by right pointer
     * Space: O(min(n, alphabet)) — at most 26/128/256 entries
     */
    public int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int left = 0, maxLen = 0;
        
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen.containsKey(c)) {
                left = Math.max(left, lastSeen.get(c) + 1);
            }
            lastSeen.put(c, right);
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    /**
     * Follow-up 1: Return the actual substring, not just length?
     *   → Track bestStart index when maxLen is updated.
     *
     * Follow-up 2: What if only lowercase letters?
     *   → Use int[26] instead of HashMap — faster, O(1) space.
     */
    public int lengthOfLongestSubstringArray(String s) {
        int[] lastSeen = new int[128];  // ASCII
        Arrays.fill(lastSeen, -1);
        int left = 0, maxLen = 0;
        
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen[c] >= left) {
                left = lastSeen[c] + 1;
            }
            lastSeen[c] = right;
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    // =========================================================
    // M2. MAX CONSECUTIVE ONES III
    // Pattern: Variable Window — Longest + Counter
    // LeetCode 1004
    // =========================================================
    /**
     * Problem: Given binary array nums, return the maximum number of
     * consecutive 1's if you can flip at most k zeros.
     *
     * Reframe: Find the LONGEST subarray with at most k zeros.
     *   → Classic variable window.
     *
     * Time:  O(n) — each element enters and leaves at most once
     * Space: O(1)
     */
    public int longestOnes(int[] nums, int k) {
        int left = 0, zeroCount = 0, maxLen = 0;
        
        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) zeroCount++;
            
            while (zeroCount > k) {
                if (nums[left] == 0) zeroCount--;
                left++;
            }
            
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    /**
     * Follow-up: Non-shrinking optimization (slightly faster):
     *   Instead of shrinking with while, use if.
     *   The window never shrinks — it only grows or stays.
     *   Result: right - left at the end = answer.
     */
    public int longestOnesNonShrink(int[] nums, int k) {
        int left = 0, zeroCount = 0;
        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) zeroCount++;
            if (zeroCount > k) {
                if (nums[left] == 0) zeroCount--;
                left++;
            }
        }
        return nums.length - left;
    }

    // =========================================================
    // M3. FRUIT INTO BASKETS
    // Pattern: Variable Window — Longest + At Most K Distinct
    // LeetCode 904
    // =========================================================
    /**
     * Problem: Array where fruits[i] is the type of fruit at tree i.
     * You have 2 baskets, each can hold ONE type. Find the maximum
     * number of fruits you can collect from a contiguous segment.
     *
     * Reframe: Longest subarray with at most 2 DISTINCT elements.
     *
     * Time:  O(n)
     * Space: O(1) — map has at most 3 entries
     */
    public int totalFruit(int[] fruits) {
        Map<Integer, Integer> freq = new HashMap<>();
        int left = 0, maxLen = 0;
        
        for (int right = 0; right < fruits.length; right++) {
            freq.merge(fruits[right], 1, Integer::sum);
            
            while (freq.size() > 2) {
                int leftFruit = fruits[left++];
                freq.merge(leftFruit, -1, Integer::sum);
                if (freq.get(leftFruit) == 0) freq.remove(leftFruit);
            }
            
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    // =========================================================
    // M4. LONGEST REPEATING CHARACTER REPLACEMENT
    // Pattern: Variable Window — Longest + Frequency Trick
    // LeetCode 424
    // =========================================================
    /**
     * Problem: String s and int k. You can replace at most k characters.
     * Return the length of the longest substring with all same characters.
     *
     * Key insight: For a window of size W, if the most frequent character
     * appears maxFreq times, we need (W - maxFreq) replacements.
     * The window is valid if: W - maxFreq <= k.
     *
     * Subtle point: maxFreq is NEVER decremented when shrinking.
     *   This is OK because we only care about windows LARGER than
     *   the current best. A smaller maxFreq can't produce a larger window.
     *
     * Time:  O(n)
     * Space: O(26) = O(1)
     */
    public int characterReplacement(String s, int k) {
        int[] freq = new int[26];
        int left = 0, maxFreq = 0, maxLen = 0;
        
        for (int right = 0; right < s.length(); right++) {
            freq[s.charAt(right) - 'A']++;
            maxFreq = Math.max(maxFreq, freq[s.charAt(right) - 'A']);
            
            // Window size - maxFreq = replacements needed
            while (right - left + 1 - maxFreq > k) {
                freq[s.charAt(left) - 'A']--;
                left++;
            }
            
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    /**
     * Follow-up: Why don't we need to update maxFreq when shrinking?
     *   → We only update the answer when the window is LARGER than any previous valid window.
     *   → A window can only be larger if maxFreq is at least as large as before.
     *   → So stale maxFreq values just prevent the window from growing — they don't create wrong answers.
     */

    // =========================================================
    // M5. PERMUTATION IN STRING
    // Pattern: Fixed Window + Frequency Match
    // LeetCode 567
    // =========================================================
    /**
     * Problem: Given strings s1 and s2, return true if s2 contains a
     * permutation of s1 (i.e., an anagram of s1 as a substring).
     *
     * Key insight: A permutation is an anagram — same character frequencies.
     * Slide a window of size len(s1) over s2, comparing frequency arrays.
     *
     * Optimization: Track a "matches" counter. When matches == 26,
     * all frequencies align → permutation found.
     *
     * Time:  O(n) where n = len(s2)
     * Space: O(1) — two arrays of size 26
     */
    public boolean checkInclusion(String s1, String s2) {
        if (s1.length() > s2.length()) return false;
        
        int[] s1Freq = new int[26], s2Freq = new int[26];
        for (int i = 0; i < s1.length(); i++) {
            s1Freq[s1.charAt(i) - 'a']++;
            s2Freq[s2.charAt(i) - 'a']++;
        }
        
        int matches = 0;
        for (int i = 0; i < 26; i++) {
            if (s1Freq[i] == s2Freq[i]) matches++;
        }
        
        for (int i = s1.length(); i < s2.length(); i++) {
            if (matches == 26) return true;
            
            // Add right character
            int rightIdx = s2.charAt(i) - 'a';
            s2Freq[rightIdx]++;
            if (s2Freq[rightIdx] == s1Freq[rightIdx]) matches++;
            else if (s2Freq[rightIdx] == s1Freq[rightIdx] + 1) matches--;
            
            // Remove left character
            int leftIdx = s2.charAt(i - s1.length()) - 'a';
            s2Freq[leftIdx]--;
            if (s2Freq[leftIdx] == s1Freq[leftIdx]) matches++;
            else if (s2Freq[leftIdx] == s1Freq[leftIdx] - 1) matches--;
        }
        
        return matches == 26;
    }

    // =========================================================
    // M6. FIND ALL ANAGRAMS IN A STRING
    // Pattern: Fixed Window + Frequency Match (collect all positions)
    // LeetCode 438
    // =========================================================
    /**
     * Problem: Given strings s and p, find all start indices of p's
     * anagrams in s. (Same as M5, but return ALL positions.)
     *
     * Time:  O(n)
     * Space: O(1) + O(result size)
     */
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> result = new ArrayList<>();
        if (s.length() < p.length()) return result;
        
        int[] pFreq = new int[26], sFreq = new int[26];
        for (int i = 0; i < p.length(); i++) {
            pFreq[p.charAt(i) - 'a']++;
            sFreq[s.charAt(i) - 'a']++;
        }
        
        int matches = 0;
        for (int i = 0; i < 26; i++) {
            if (pFreq[i] == sFreq[i]) matches++;
        }
        if (matches == 26) result.add(0);
        
        for (int i = p.length(); i < s.length(); i++) {
            int rightIdx = s.charAt(i) - 'a';
            sFreq[rightIdx]++;
            if (sFreq[rightIdx] == pFreq[rightIdx]) matches++;
            else if (sFreq[rightIdx] == pFreq[rightIdx] + 1) matches--;
            
            int leftIdx = s.charAt(i - p.length()) - 'a';
            sFreq[leftIdx]--;
            if (sFreq[leftIdx] == pFreq[leftIdx]) matches++;
            else if (sFreq[leftIdx] == pFreq[leftIdx] - 1) matches--;
            
            if (matches == 26) result.add(i - p.length() + 1);
        }
        return result;
    }

    // =========================================================
    // M7. SUBARRAY PRODUCT LESS THAN K
    // Pattern: Variable Window — Counting
    // LeetCode 713
    // =========================================================
    /**
     * Problem: Given array of positive integers nums and int k,
     * return the count of contiguous subarrays where product < k.
     *
     * Key insight: All values are positive → product is monotonically
     * increasing as window grows → sliding window works!
     *
     * Counting trick: For each right, subarrays ending at right with
     * product < k are: [left..right], [left+1..right], ..., [right..right]
     * = (right - left + 1) subarrays.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public int numSubarrayProductLessThanK(int[] nums, int k) {
        if (k <= 1) return 0;
        
        int product = 1, left = 0, count = 0;
        for (int right = 0; right < nums.length; right++) {
            product *= nums[right];
            
            while (product >= k) {
                product /= nums[left++];
            }
            
            count += right - left + 1;  // count ALL valid subarrays ending at right
        }
        return count;
    }

    /**
     * Follow-up: What if array contains zeros or negative numbers?
     *   → Sliding window on product breaks (non-monotonic).
     *   → Need different approach (e.g., log transformation or DP).
     */

    // =========================================================
    // M8. GRUMPY BOOKSTORE OWNER
    // Pattern: Fixed Window + Overlay
    // LeetCode 1052
    // =========================================================
    /**
     * Problem: customers[i] = customers at minute i.
     * grumpy[i] = 1 if owner is grumpy (customers unsatisfied).
     * Owner can use a secret technique for k consecutive minutes
     * to suppress grumpiness. Maximize satisfied customers.
     *
     * Key insight: 
     * 1. Base satisfaction = sum of customers[i] where grumpy[i] == 0.
     * 2. The technique "rescues" customers during grumpy minutes.
     * 3. Slide a window of size k to find the maximum rescue.
     * 4. Answer = base + max rescue.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public int maxSatisfied(int[] customers, int[] grumpy, int k) {
        int baseSatisfied = 0;
        for (int i = 0; i < customers.length; i++) {
            if (grumpy[i] == 0) baseSatisfied += customers[i];
        }
        
        // Find max "rescue" in a window of size k
        int rescue = 0;
        for (int i = 0; i < k; i++) {
            if (grumpy[i] == 1) rescue += customers[i];
        }
        
        int maxRescue = rescue;
        for (int i = k; i < customers.length; i++) {
            if (grumpy[i] == 1) rescue += customers[i];
            if (grumpy[i - k] == 1) rescue -= customers[i - k];
            maxRescue = Math.max(maxRescue, rescue);
        }
        
        return baseSatisfied + maxRescue;
    }
}
