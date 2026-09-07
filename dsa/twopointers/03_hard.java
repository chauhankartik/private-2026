/**
 * ============================================================
 *  TWO POINTERS — HARD PROBLEMS
 *  Trapping water, k-diff pairs, interval merging, and more.
 * ============================================================
 *
 *  Problems:
 *   H1. Trapping Rain Water (LC 42)
 *   H2. 4Sum (LC 18)
 *   H3. Minimum Window Substring using Two Pointer Variant (LC 76)
 *   H4. Find the Closest Pair from Two Sorted Arrays
 *   H5. Count Pairs with Given Sum (Two Pointer on Sorted Array)
 *   H6. Palindrome Pairs (LC 336) — Two Pointer & Trie Hybrid
 *
 * ============================================================
 */
import java.util.*;

class Hard {

    // =========================================================
    // H1. TRAPPING RAIN WATER
    // Pattern: Opposite-End Two Pointers — Max Boundary Tracking
    // LeetCode 42
    // =========================================================
    /**
     * Problem: Given elevation map height[], compute how much
     * rainwater can be trapped after raining.
     *
     * Key formula: water at position i = min(maxLeft, maxRight) - height[i]
     *
     * Brute Force O(n²): For each index, scan left and right for max heights.
     *
     * Better O(n) with O(n) space: Precompute maxLeft[] and maxRight[] arrays.
     *
     * Optimal O(n), O(1) space: Two-pointer approach.
     * - The formula depends on min(maxLeft, maxRight).
     * - We only need to process the MINIMUM SIDE to guarantee correctness.
     *
     * Proof why moving the pointer with smaller max-boundary is correct:
     *   Suppose maxLeft < maxRight at some step.
     *   Water at left = min(maxLeft, maxRight) - height[left] = maxLeft - height[left].
     *   This is EXACT because we know maxLeft is the true left boundary,
     *   and maxRight >= maxLeft, so min is maxLeft regardless of exact maxRight value.
     *   → We can safely compute water[left] and advance left.
     *   Symmetric for maxRight < maxLeft.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int maxLeft = 0, maxRight = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] <= height[right]) {
                // Process left side (maxLeft is the binding constraint)
                if (height[left] >= maxLeft) {
                    maxLeft = height[left]; // update left boundary
                } else {
                    totalWater += maxLeft - height[left]; // trap water
                }
                left++;
            } else {
                // Process right side (maxRight is the binding constraint)
                if (height[right] >= maxRight) {
                    maxRight = height[right]; // update right boundary
                } else {
                    totalWater += maxRight - height[right]; // trap water
                }
                right--;
            }
        }
        return totalWater;
    }

    /**
     * Follow-up 1: Trapping Rain Water II (LC 407) — 3D grid?
     *   → Min-heap (priority queue) approach. Always process the cell with
     *     the smallest boundary. O(m*n log(m*n)).
     *
     * Follow-up 2: How to reconstruct WHERE the water pools are?
     *   → Use the precomputed maxLeft[], maxRight[] approach.
     *   → Water at i = max(0, min(maxLeft[i], maxRight[i]) - height[i]).
     *
     * Follow-up 3: What if heights are floating point?
     *   → Same algorithm works exactly — no integer assumption.
     */

    // =========================================================
    // H2. 4SUM
    // Pattern: Fix Two Elements + Two-Pointer Inner Pass
    // LeetCode 18
    // =========================================================
    /**
     * Problem: Given integer array nums and int target,
     * return all UNIQUE quadruplets [a,b,c,d] that sum to target.
     *
     * Generalization of 3Sum: two outer loops + two-pointer inner.
     *
     * Duplicate skipping at THREE levels:
     *   - Outer i: if i > 0 && nums[i] == nums[i-1], skip.
     *   - Inner j: if j > i+1 && nums[j] == nums[j-1], skip.
     *   - After match: advance left/right past same values.
     *
     * Integer overflow warning:
     *   nums[i] + nums[j] + nums[left] + nums[right] can overflow int
     *   when values are large (up to 10^9 each).
     *   → Cast to long.
     *
     * Time: O(n³) — two outer loops × two-pointer O(n)
     * Space: O(1) extra (O(n) for sort)
     */
    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        int n = nums.length;

        for (int i = 0; i < n - 3; i++) {
            // Early termination (for positive arrays): min possible sum
            if ((long) nums[i] + nums[i + 1] + nums[i + 2] + nums[i + 3] > target) break;
            // Pruning: max possible sum with nums[i]
            if ((long) nums[i] + nums[n - 3] + nums[n - 2] + nums[n - 1] < target) continue;

            if (i > 0 && nums[i] == nums[i - 1]) continue; // skip outer dup

            for (int j = i + 1; j < n - 2; j++) {
                if (j > i + 1 && nums[j] == nums[j - 1]) continue; // skip inner dup

                // Early termination for j level
                if ((long) nums[i] + nums[j] + nums[j + 1] + nums[j + 2] > target) break;
                if ((long) nums[i] + nums[j] + nums[n - 2] + nums[n - 1] < target) continue;

                int left = j + 1, right = n - 1;

                while (left < right) {
                    long sum = (long) nums[i] + nums[j] + nums[left] + nums[right];

                    if (sum == target) {
                        result.add(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                        while (left < right && nums[left] == nums[left + 1]) left++;
                        while (left < right && nums[right] == nums[right - 1]) right--;
                        left++;
                        right--;
                    } else if (sum < target) {
                        left++;
                    } else {
                        right--;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Follow-up 1: kSum (generalized)?
     *   → Recursive: for k > 2, fix one element, recurse on kSum-1.
     *   → For k == 2, use two-pointer (base case).
     *   → Time: O(n^(k-1)).
     *
     * Follow-up 2: How does two early termination pruning lines work?
     *   → "min sum > target": even the smallest possible k elements still exceed target.
     *   → "max sum < target": even the largest possible k elements can't reach target.
     *   → These can reduce practical runtime dramatically on real inputs.
     */

    // =========================================================
    // H3. MINIMUM WINDOW SUBSTRING (Two-Pointer / Shrink Variant)
    // Pattern: Variable Two-Pointer — Shrink When Valid
    // LeetCode 76
    // =========================================================
    /**
     * Problem: Given strings s and t, find the minimum window substring
     * of s that contains all characters of t.
     *
     * (This is also a Sliding Window problem; shown here as it perfectly
     *  illustrates the "shrink while valid" two-pointer variant.)
     *
     * Approach:
     * - Use a freq map for t. Track how many distinct chars are "satisfied".
     * - Expand right until all chars are satisfied (valid window).
     * - Shrink left as much as possible while still valid.
     * - Record minimum valid window.
     *
     * satisfied = count of distinct chars in t where window freq >= t freq.
     * Window becomes valid when satisfied == t's distinct char count.
     *
     * Time: O(|s| + |t|)
     * Space: O(|t|) for frequency map
     */
    public String minWindow(String s, String t) {
        if (s.isEmpty() || t.isEmpty()) return "";

        Map<Character, Integer> tFreq = new HashMap<>();
        for (char c : t.toCharArray()) {
            tFreq.merge(c, 1, Integer::sum);
        }

        int required = tFreq.size(); // distinct chars in t to satisfy
        int satisfied = 0;           // distinct chars currently satisfied in window
        Map<Character, Integer> windowFreq = new HashMap<>();

        int left = 0;
        int minLen = Integer.MAX_VALUE;
        int minStart = 0;

        for (int right = 0; right < s.length(); right++) {
            // EXPAND: add s[right] to window
            char rightChar = s.charAt(right);
            windowFreq.merge(rightChar, 1, Integer::sum);

            // Check if this char's requirement is now satisfied
            if (tFreq.containsKey(rightChar) &&
                windowFreq.get(rightChar).equals(tFreq.get(rightChar))) {
                satisfied++;
            }

            // SHRINK: while window is valid, try to contract from left
            while (satisfied == required) {
                // Record current window if smaller
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    minStart = left;
                }

                // Remove s[left] from window
                char leftChar = s.charAt(left);
                windowFreq.merge(leftChar, -1, Integer::sum);
                if (tFreq.containsKey(leftChar) &&
                    windowFreq.get(leftChar) < tFreq.get(leftChar)) {
                    satisfied--;
                }
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(minStart, minStart + minLen);
    }

    /**
     * Follow-up 1: What if t has duplicate characters?
     *   → The freq comparison (windowFreq >= tFreq) handles this correctly.
     *   → satisfied only increments when the window matches the exact required count.
     *
     * Follow-up 2: Find ALL minimum windows?
     *   → Collect all windows of length minLen when satisfied == required.
     */

    // =========================================================
    // H4. CLOSEST PAIR FROM TWO SORTED ARRAYS
    // Pattern: Merge-Style Two Pointers — Cross-Array
    // Classic interview problem
    // =========================================================
    /**
     * Problem: Given two sorted arrays a[] and b[], find a pair (a[i], b[j])
     * with minimum |a[i] + b[j] - target|.
     * Equivalently: one element from each array, closest sum to target.
     *
     * Approach: Start with left = 0 (from a) and right = b.length-1.
     * - If a[left] + b[right] < target → need to increase → left++
     * - If sum > target → right--
     * - Track closest pair throughout.
     *
     * This works because arrays are sorted:
     *   a is non-decreasing (left → right)
     *   b is non-decreasing but we traverse right → left
     * → The combined pointer movement eliminates O(n) per step.
     *
     * Time: O(m+n) after O(m log m + n log n) sort
     * Space: O(1)
     */
    public int[] closestPairFromTwoSortedArrays(int[] a, int[] b, int target) {
        int left = 0, right = b.length - 1;
        int bestDiff = Integer.MAX_VALUE;
        int[] result = new int[2];

        while (left < a.length && right >= 0) {
            int sum = a[left] + b[right];
            int diff = Math.abs(sum - target);

            if (diff < bestDiff) {
                bestDiff = diff;
                result[0] = a[left];
                result[1] = b[right];
            }

            if (sum < target) {
                left++;  // increase sum
            } else if (sum > target) {
                right--; // decrease sum
            } else {
                return result; // exact match
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Return ALL pairs with minimum difference?
     *   → First pass to find minDiff. Second pass to collect pairs.
     *
     * Follow-up 2: What if we want 3 arrays (one element from each)?
     *   → Fix element from first array, run two-pointer cross-array on the other two.
     *   → O(m * (n+p)).
     */

    // =========================================================
    // H5. COUNT TRIPLETS WITH SUM LESS THAN TARGET
    // Pattern: Sort + Fix + Two-Pointer Counting
    // Classic problem (variant of 3Sum)
    // =========================================================
    /**
     * Problem: Given array nums and int target, count triplets
     * (i, j, k) with i < j < k and nums[i] + nums[j] + nums[k] < target.
     *
     * Key counting insight:
     * When nums[i] + nums[left] + nums[right] < target:
     *   ALL pairs (left, x) where x in [left+1 .. right] also form valid triplets
     *   with nums[i] (because nums[x] <= nums[right] for x < right in sorted array).
     *   → Count += (right - left) triplets in one step!
     *   Then advance left to check more.
     *
     * When sum >= target: right-- to decrease sum.
     *
     * Time: O(n²)
     * Space: O(1) extra
     */
    public int countTripletsSumLessThanTarget(int[] nums, int target) {
        Arrays.sort(nums);
        int count = 0;

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1, right = nums.length - 1;

            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (sum < target) {
                    // All triplets (i, left, x) for x in [left+1..right] are valid
                    count += (right - left);
                    left++; // move left to find more valid triplets
                } else {
                    right--; // decrease sum
                }
            }
        }
        return count;
    }

    /**
     * Follow-up 1: Count triplets with sum GREATER THAN target?
     *   → When sum > target: count += (right - left), right--.
     *   → When sum <= target: left++.
     *
     * Follow-up 2: Find triplets with sum EXACTLY equal to target?
     *   → This is exactly 3Sum (M1 in medium problems).
     */

    // =========================================================
    // H6. PALINDROME PAIRS
    // Pattern: Trie + Two-Pointer String Check
    // LeetCode 336
    // =========================================================
    /**
     * Problem: Given list of unique strings words, find all pairs
     * (i, j) where words[i] + words[j] is a palindrome.
     *
     * Brute Force O(n² * k): Check all pairs, verify palindrome in O(k).
     *
     * Optimal O(n * k²): For each word, find its reverse in the map.
     * For each word w of length k, split into ALL prefixes and suffixes.
     * Two cases:
     *   Case 1: prefix is palindrome AND suffix-reversed exists in map.
     *           → (map[suffix-reversed], i) is a palindrome pair.
     *   Case 2: suffix is palindrome AND prefix-reversed exists in map.
     *           → (i, map[prefix-reversed]) is a palindrome pair.
     *
     * This problem is included here for the "isPalindrome" two-pointer check used
     * at its core. The overall algorithm is HashMap-based.
     *
     * Time: O(n * k²) — n words, k splits, k for palindrome check
     * Space: O(n * k) for HashMap
     */
    public List<List<Integer>> palindromePairs(String[] words) {
        Map<String, Integer> wordIndex = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            wordIndex.put(words[i], i);
        }

        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int n = word.length();

            for (int k = 0; k <= n; k++) {
                // Case 1: prefix = word[0..k-1] is palindrome
                //         suffix = word[k..n-1], reversed = word.reverse(k..n-1)
                //         If reversed suffix exists in map → pair (map[reversed], i)
                String prefix = word.substring(0, k);
                String suffix = word.substring(k);

                if (isPalindromeTP(prefix)) {
                    String reversedSuffix = new StringBuilder(suffix).reverse().toString();
                    if (wordIndex.containsKey(reversedSuffix) && wordIndex.get(reversedSuffix) != i) {
                        result.add(Arrays.asList(wordIndex.get(reversedSuffix), i));
                    }
                }

                // Case 2: suffix is palindrome (avoid duplicates when k == n)
                if (k != n && isPalindromeTP(suffix)) {
                    String reversedPrefix = new StringBuilder(prefix).reverse().toString();
                    if (wordIndex.containsKey(reversedPrefix) && wordIndex.get(reversedPrefix) != i) {
                        result.add(Arrays.asList(i, wordIndex.get(reversedPrefix)));
                    }
                }
            }
        }
        return result;
    }

    /** Two-pointer palindrome check used in H6. */
    private boolean isPalindromeTP(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    /**
     * Follow-up 1: Optimize further with Trie?
     *   → Insert all reversed words into a Trie.
     *   → For each word, walk the Trie simultaneously. When Trie path ends,
     *     check if remaining word is a palindrome. O(n * k) time.
     *
     * Follow-up 2: What if words can be empty string ""?
     *   → A pair ("", w) is a palindrome pair if w is a palindrome.
     *   → The split k=0 case handles this correctly.
     */
}
