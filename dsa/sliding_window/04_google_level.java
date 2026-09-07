/**
 * ============================================================
 *  SLIDING WINDOW — GOOGLE LEVEL
 *  Deque optimizations, DP + sliding window hybrids.
 * ============================================================
 *
 *  Problems:
 *   G1. Minimum Number of K Consecutive Bit Flips (LC 995)
 *   G2. Shortest Subarray with Sum at Least K (LC 862)
 *   G3. Max Value of Equation (LC 1499)
 *   G4. Longest Substring with At Least K Repeating Chars (LC 395)
 *   G5. Substring with Concatenation of All Words (LC 30)
 *   G6. Constrained Subsequence Sum (LC 1425)
 *
 * ============================================================
 */
import java.util.*;

class GoogleLevel {

    // =========================================================
    // G1. MINIMUM NUMBER OF K CONSECUTIVE BIT FLIPS
    // Pattern: Greedy Sliding Window + Flip Tracking
    // LeetCode 995
    // =========================================================
    /**
     * Problem: Binary array nums. A k-bit flip flips k consecutive bits.
     * Return the minimum flips to make every value 1, or -1 if impossible.
     *
     * Key insight: Process left-to-right greedily. If nums[i] is 0
     * (after accounting for previous flips), we MUST flip starting at i.
     * Track the cumulative flip effect using a variable + a queue.
     *
     * Time:  O(n)
     * Space: O(n) — queue to track flip boundaries
     */
    public int minKBitFlips(int[] nums, int k) {
        int n = nums.length;
        int flips = 0, currentFlips = 0;
        int[] isFlipped = new int[n];  // marks where a flip starts
        
        for (int i = 0; i < n; i++) {
            // Remove the effect of a flip that started k positions ago
            if (i >= k) currentFlips -= isFlipped[i - k];
            
            // Current effective value: nums[i] XOR (currentFlips % 2)
            if ((nums[i] + currentFlips) % 2 == 0) {
                // Need to flip starting at i
                if (i + k > n) return -1;  // can't flip beyond array
                isFlipped[i] = 1;
                currentFlips++;
                flips++;
            }
        }
        return flips;
    }

    /**
     * Follow-up: Space optimization using deque instead of array?
     *   → Use a Deque to track flip start indices. Poll when front < i - k + 1.
     *   → Same O(n) time, but O(k) space instead of O(n).
     */

    // =========================================================
    // G2. SHORTEST SUBARRAY WITH SUM AT LEAST K
    // Pattern: Prefix Sum + Monotonic Deque
    // LeetCode 862
    // =========================================================
    /**
     * Problem: Given integer array nums (can have NEGATIVES) and int k,
     * return the length of the shortest subarray with sum >= k.
     *
     * Why can't we use normal sliding window?
     *   Negative numbers break monotonicity — adding elements can
     *   DECREASE the sum, so we can't expand/shrink predictably.
     *
     * Approach: Prefix sums + monotonic INCREASING deque.
     *   1. Compute prefix[i] = sum(nums[0..i-1]).
     *   2. For each i, we want the LARGEST j < i such that
     *      prefix[i] - prefix[j] >= k → prefix[j] <= prefix[i] - k.
     *   3. Deque maintains indices in INCREASING order of prefix values.
     *   4. Pop from FRONT while prefix[i] - prefix[front] >= k (found valid, try shorter).
     *   5. Pop from BACK while prefix[back] >= prefix[i] (back is dominated by i).
     *
     * Time:  O(n) — each index pushed and popped at most once
     * Space: O(n)
     */
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        
        Deque<Integer> deque = new ArrayDeque<>();  // monotonic increasing on prefix values
        int minLen = Integer.MAX_VALUE;
        
        for (int i = 0; i <= n; i++) {
            // Front: if prefix[i] - prefix[front] >= k, record and pop
            while (!deque.isEmpty() && prefix[i] - prefix[deque.peekFirst()] >= k) {
                minLen = Math.min(minLen, i - deque.pollFirst());
            }
            
            // Back: remove dominated indices (larger prefix values)
            while (!deque.isEmpty() && prefix[deque.peekLast()] >= prefix[i]) {
                deque.pollLast();
            }
            
            deque.offerLast(i);
        }
        
        return minLen == Integer.MAX_VALUE ? -1 : minLen;
    }

    /**
     * Follow-up: Why pop from front (not just peek)?
     *   → Once prefix[front] satisfies prefix[i] - prefix[front] >= k,
     *     any future j > i will give a LONGER subarray (j - front > i - front).
     *     So front is consumed — no future use.
     *
     * Follow-up: Why pop dominated elements from back?
     *   → If prefix[back] >= prefix[i] and back < i, then for any future j:
     *     prefix[j] - prefix[i] >= prefix[j] - prefix[back] AND i > back.
     *     So using i gives a shorter subarray with equal or better sum. back is useless.
     */

    // =========================================================
    // G3. MAX VALUE OF EQUATION
    // Pattern: Monotonic Deque Optimization
    // LeetCode 1499
    // =========================================================
    /**
     * Problem: Given points sorted by x. Find max value of
     * yi + yj + |xi - xj| where |xi - xj| <= k and i < j.
     *
     * Since points are sorted: |xi - xj| = xj - xi (j > i).
     * So: yi + yj + xj - xi = (yj + xj) + (yi - xi).
     * For each j, maximize (yi - xi) over valid i's (xj - xi <= k).
     *
     * → Sliding window maximum of (yi - xi) with constraint xj - xi <= k.
     * → Monotonic decreasing deque on (yi - xi).
     *
     * Time:  O(n)
     * Space: O(n)
     */
    public int findMaxValueOfEquation(int[][] points, int k) {
        Deque<int[]> deque = new ArrayDeque<>();  // [xi, yi - xi], decreasing on yi-xi
        int maxVal = Integer.MIN_VALUE;
        
        for (int[] point : points) {
            int xj = point[0], yj = point[1];
            
            // Remove points too far away
            while (!deque.isEmpty() && xj - deque.peekFirst()[0] > k) {
                deque.pollFirst();
            }
            
            // Best answer with current j
            if (!deque.isEmpty()) {
                maxVal = Math.max(maxVal, yj + xj + deque.peekFirst()[1]);
            }
            
            // Maintain decreasing order of (yi - xi)
            int val = yj - xj;
            while (!deque.isEmpty() && deque.peekLast()[1] <= val) {
                deque.pollLast();
            }
            deque.offerLast(new int[]{xj, val});
        }
        return maxVal;
    }

    // =========================================================
    // G4. LONGEST SUBSTRING WITH AT LEAST K REPEATING CHARACTERS
    // Pattern: Sliding Window with Bounded Unique Chars
    // LeetCode 395
    // =========================================================
    /**
     * Problem: Given string s and int k, find the length of the longest
     * substring where EVERY character appears at least k times.
     *
     * Why can't we use direct sliding window?
     *   No clear monotonic condition. Adding a char might help or hurt.
     *
     * Trick: Fix the number of UNIQUE characters allowed (1 to 26).
     * For each target unique count u:
     *   → Sliding window: expand right, shrink left when unique > u.
     *   → A window is valid when unique == u AND all chars have freq >= k.
     *   → Take max over all u.
     *
     * Time:  O(26 · n) = O(n) — 26 passes, each O(n)
     * Space: O(1) — frequency array
     */
    public int longestSubstring(String s, int k) {
        int maxLen = 0;
        
        for (int uniqueTarget = 1; uniqueTarget <= 26; uniqueTarget++) {
            int[] freq = new int[26];
            int left = 0, uniqueCount = 0, atLeastK = 0;
            
            for (int right = 0; right < s.length(); right++) {
                int rightIdx = s.charAt(right) - 'a';
                if (freq[rightIdx] == 0) uniqueCount++;
                freq[rightIdx]++;
                if (freq[rightIdx] == k) atLeastK++;
                
                while (uniqueCount > uniqueTarget) {
                    int leftIdx = s.charAt(left) - 'a';
                    if (freq[leftIdx] == k) atLeastK--;
                    freq[leftIdx]--;
                    if (freq[leftIdx] == 0) uniqueCount--;
                    left++;
                }
                
                if (uniqueCount == uniqueTarget && atLeastK == uniqueTarget) {
                    maxLen = Math.max(maxLen, right - left + 1);
                }
            }
        }
        return maxLen;
    }

    // =========================================================
    // G5. SUBSTRING WITH CONCATENATION OF ALL WORDS
    // Pattern: Fixed Window + Sliding HashMap
    // LeetCode 30
    // =========================================================
    /**
     * Problem: String s and array of equal-length words.
     * Find all starting indices where s contains a concatenation of
     * ALL words (each used exactly once, in any order).
     *
     * Key insight: Since all words have the same length w,
     * we can slide a window of size (numWords * w) over s,
     * jumping w characters at a time.
     * We need wordLen different starting offsets (0 to wordLen-1).
     *
     * Time:  O(n · w) where n = |s|, w = word length
     * Space: O(numWords)
     */
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        if (s.isEmpty() || words.length == 0) return result;
        
        int wordLen = words[0].length();
        int numWords = words.length;
        int windowLen = wordLen * numWords;
        
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) wordCount.merge(word, 1, Integer::sum);
        
        // Try each starting offset (0 to wordLen-1)
        for (int offset = 0; offset < wordLen; offset++) {
            Map<String, Integer> windowWords = new HashMap<>();
            int matched = 0;
            
            for (int right = offset; right + wordLen <= s.length(); right += wordLen) {
                String word = s.substring(right, right + wordLen);
                
                if (wordCount.containsKey(word)) {
                    windowWords.merge(word, 1, Integer::sum);
                    matched++;
                    
                    // Shrink if this word is over-counted
                    while (windowWords.get(word) > wordCount.get(word)) {
                        int left = right - (matched - 1) * wordLen;
                        String leftWord = s.substring(left, left + wordLen);
                        windowWords.merge(leftWord, -1, Integer::sum);
                        matched--;
                    }
                    
                    if (matched == numWords) {
                        result.add(right - (numWords - 1) * wordLen);
                    }
                } else {
                    // Reset window
                    windowWords.clear();
                    matched = 0;
                }
            }
        }
        return result;
    }

    // =========================================================
    // G6. CONSTRAINED SUBSEQUENCE SUM
    // Pattern: DP + Monotonic Deque (Sliding Window Max DP)
    // LeetCode 1425
    // =========================================================
    /**
     * Problem: Given array nums and int k, find the maximum sum of a
     * non-empty subsequence such that for every two consecutive elements
     * in the subsequence, nums[i] and nums[j], j - i <= k.
     *
     * DP recurrence: dp[i] = nums[i] + max(0, max(dp[j]) for j in [i-k, i-1])
     *
     * Naive: O(n·k) — check all k previous dp values.
     * Optimal: O(n) — use monotonic deque to track max of dp in window of size k.
     *
     * This is the classic "DP + Sliding Window Max" pattern.
     *
     * Time:  O(n)
     * Space: O(n) for dp + O(k) for deque
     */
    public int constrainedSubsetSum(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        Deque<Integer> deque = new ArrayDeque<>();  // monotonic decreasing on dp values
        int maxSum = Integer.MIN_VALUE;
        
        for (int i = 0; i < n; i++) {
            // Remove elements outside the window
            while (!deque.isEmpty() && deque.peekFirst() < i - k) {
                deque.pollFirst();
            }
            
            // dp[i] = nums[i] + max(0, best from window)
            dp[i] = nums[i];
            if (!deque.isEmpty()) {
                dp[i] = Math.max(dp[i], nums[i] + dp[deque.peekFirst()]);
            }
            
            // Maintain monotonic decreasing deque
            while (!deque.isEmpty() && dp[deque.peekLast()] <= dp[i]) {
                deque.pollLast();
            }
            
            // Only add to deque if dp[i] > 0 (otherwise it can't help future elements)
            if (dp[i] > 0) {
                deque.offerLast(i);
            }
            
            maxSum = Math.max(maxSum, dp[i]);
        }
        return maxSum;
    }

    /**
     * Follow-up: How does this relate to "Jump Game" problems?
     *   → Same idea: from position i, you can reach positions [i+1, i+k].
     *   → dp[i] = best value reachable at i.
     *   → Deque optimizes the "look back k positions" from O(k) to O(1) amortized.
     */
}
