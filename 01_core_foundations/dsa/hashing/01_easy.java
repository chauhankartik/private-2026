/**
 * ============================================================
 *  HASHING — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Two Sum
 *   E2. Contains Duplicate
 *   E3. Single Number (XOR bonus + HashMap approach)
 *   E4. Intersection of Two Arrays
 *   E5. First Unique Character in a String
 *   E6. Isomorphic Strings
 *   E7. Happy Number
 *   E8. Valid Anagram
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
public class Easy {

    // =========================================================
    // E1. TWO SUM
    // Pattern: Two-Sum / Complement + Index Tracking
    // =========================================================
    /**
     * Problem: Given int[] nums and int target, return indices [i, j]
     * such that nums[i] + nums[j] == target. Exactly one solution exists.
     *
     * Brute Force O(n²): For every pair (i, j), check nums[i]+nums[j]==target.
     *
     * Optimal O(n): As we iterate, for each nums[i], the complement is
     *   `target - nums[i]`. If complement was already seen (stored in map),
     *   we found our answer. Otherwise, store nums[i] → i in the map.
     *
     * Why does this work?
     *   - The map acts as a "seen set" enriched with indices.
     *   - We need exactly ONE pass because: if nums[j] is the complement of
     *     nums[i] (j > i), then when we reach j, nums[i] is already in the map.
     *
     * Time Complexity:  O(n)   — one pass, O(1) avg per HashMap op
     * Space Complexity: O(n)   — in the worst case we store all n elements
     */
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();   // value → index
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (seen.containsKey(complement)) {
                return new int[]{seen.get(complement), i};
            }
            seen.put(nums[i], i);
        }
        return new int[]{-1, -1}; // guaranteed by problem to not reach here
    }

    /**
     * Follow-up 1: What if the array is sorted?
     *   → Use two pointers (left, right). Move left++ if sum < target, right-- if sum > target.
     *   → O(n) time, O(1) space — better space complexity.
     *
     * Follow-up 2: What if we need ALL pairs, not just one?
     *   → Instead of returning on first match, collect all pairs.
     *   → Handle duplicates: use multiset (frequency map).
     *
     * Follow-up 3: What if numbers can repeat and we return all unique pairs?
     *   See: E1_TwoSumAllPairs below.
     */
    public List<int[]> twoSumAllPairs(int[] nums, int target) {
        // Pattern: Frequency Map + Two-pointer on sorted array
        Arrays.sort(nums);  // O(n log n)
        List<int[]> result = new ArrayList<>();
        int left = 0, right = nums.length - 1;
        while (left < right) {
            int sum = nums[left] + nums[right];
            if (sum == target) {
                result.add(new int[]{nums[left], nums[right]});
                // Skip duplicates
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++; right--;
            } else if (sum < target) left++;
            else right--;
        }
        return result;
    }

    // =========================================================
    // E2. CONTAINS DUPLICATE
    // Pattern: Deduplication via HashSet
    // =========================================================
    /**
     * Problem: Return true if any value appears at least twice.
     *
     * Key insight: A HashSet only stores unique elements. If add() returns false,
     * the element was already present → duplicate found.
     *
     * Time:  O(n) — one pass
     * Space: O(n) — worst case all unique, set holds n elements
     */
    public boolean containsDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int n : nums) {
            if (!seen.add(n)) return true;  // add() returns false if already present
        }
        return false;
    }

    /**
     * Follow-up: Contains Duplicate within distance k (LeetCode 219)
     *   → Maintain a SLIDING WINDOW of size k using a HashSet.
     *   → If set size exceeds k, remove the oldest element.
     *
     * Time: O(n), Space: O(k)
     */
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Set<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if (window.contains(nums[i])) return true;
            window.add(nums[i]);
            if (window.size() > k) {
                window.remove(nums[i - k]);  // shrink window from left
            }
        }
        return false;
    }

    // =========================================================
    // E3. FIRST UNIQUE CHARACTER IN A STRING
    // Pattern: Frequency Count (fixed alphabet → O(1) space)
    // =========================================================
    /**
     * Problem: Return index of first non-repeating character in string s.
     *
     * Two-pass approach:
     *  Pass 1: Build frequency array for all 26 chars. O(n) time, O(1) space.
     *  Pass 2: Return index of first char with frequency == 1.
     *
     * Why O(1) space?
     *   The alphabet is fixed (26 lowercase letters). int[26] is a constant.
     *   This is the interviewer's expected answer — NOT HashMap<Character, Integer>.
     *
     * Time:  O(n)
     * Space: O(1) — 26-element array, constant regardless of n
     */
    public int firstUniqChar(String s) {
        int[] freq = new int[26];
        for (char c : s.toCharArray()) freq[c - 'a']++;
        for (int i = 0; i < s.length(); i++) {
            if (freq[s.charAt(i) - 'a'] == 1) return i;
        }
        return -1;
    }

    /**
     * Follow-up: Unicode characters (not just lowercase ASCII)?
     *   → Use HashMap<Character, Integer> — O(n) space.
     *
     * Follow-up: Stream of characters (can't do two passes)?
     *   → Use a LinkedHashMap to maintain insertion order.
     *   → On each new char: if already in map with count > 1 → remove.
     *   → Return the first key of the LinkedHashMap.
     */
    public char firstUniqCharStream(String stream) {
        Map<Character, Integer> count = new LinkedHashMap<>();
        for (char c : stream.toCharArray()) {
            count.merge(c, 1, Integer::sum);  // elegant: put or increment
        }
        for (Map.Entry<Character, Integer> e : count.entrySet()) {
            if (e.getValue() == 1) return e.getKey();
        }
        return '\0';
    }

    // =========================================================
    // E4. VALID ANAGRAM
    // Pattern: Frequency Count + Difference Array
    // =========================================================
    /**
     * Problem: Return true if t is an anagram of s (same chars, same frequency).
     *
     * Approach: Increment for s, decrement for t. If all zeros → anagram.
     *
     * Time:  O(n)   where n = length of s (assume |s| == |t|)
     * Space: O(1)   — fixed 26-char alphabet
     */
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] diff = new int[26];
        for (int i = 0; i < s.length(); i++) {
            diff[s.charAt(i) - 'a']++;
            diff[t.charAt(i) - 'a']--;
        }
        for (int d : diff) if (d != 0) return false;
        return true;
    }

    /**
     * Follow-up: What if input contains Unicode characters?
     *   → Use HashMap<Character, Integer>. O(n) space.
     *
     * Follow-up: Check if two strings are anagrams of each other after sorting?
     *   → Sort both → compare. O(n log n) time, O(1) extra space (in-place sort).
     *   → HashMap approach is O(n) — strictly better.
     */

    // =========================================================
    // E5. INTERSECTION OF TWO ARRAYS
    // Pattern: HashSet Deduplication
    // =========================================================
    /**
     * Problem: Return array of unique elements present in both arrays.
     *
     * Time:  O(n + m)  — build set from nums1, iterate nums2
     * Space: O(min(n, m)) — store smaller array in set
     */
    public int[] intersection(int[] nums1, int[] nums2) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums1) set.add(n);

        Set<Integer> result = new HashSet<>();
        for (int n : nums2) {
            if (set.contains(n)) result.add(n);
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Follow-up: Intersection with duplicates (return each element as many times as it appears)?
     *   → Use frequency maps instead of sets.
     *   → Result count = min(freq1[x], freq2[x])
     *
     * Follow-up: Arrays are sorted?
     *   → Two pointers. O(n + m) time, O(1) extra space.
     *
     * Follow-up: nums1 is small but nums2 is HUGE (doesn't fit in memory)?
     *   → Build set from nums1 (small), stream nums2 line by line.
     */

    // =========================================================
    // E6. ISOMORPHIC STRINGS
    // Pattern: Two-way Index Tracking (Bidirectional Map)
    // =========================================================
    /**
     * Problem: Two strings s and t are isomorphic if chars can be mapped
     * such that all occurrences of a char in s map to the same char in t,
     * preserving order. No two chars map to the same char.
     *
     * Key insight: We need a BIJECTION (one-to-one mapping both ways).
     *   "egg" → "add"  ✓  (e→a, g→d)
     *   "foo" → "bar"  ✗  (f→b, o→a, o→r — o maps to two chars)
     *   "ab"  → "aa"   ✗  (a→a, b→a — two chars map to same)
     *
     * Solution: Maintain two maps. If either mapping is inconsistent → false.
     *
     * Time:  O(n)
     * Space: O(1) — at most 256 unique ASCII chars, constant bound
     */
    public boolean isIsomorphic(String s, String t) {
        int[] sToT = new int[256];
        int[] tToS = new int[256];
        Arrays.fill(sToT, -1);
        Arrays.fill(tToS, -1);

        for (int i = 0; i < s.length(); i++) {
            char sc = s.charAt(i), tc = t.charAt(i);
            if (sToT[sc] == -1 && tToS[tc] == -1) {
                sToT[sc] = tc;
                tToS[tc] = sc;
            } else if (sToT[sc] != tc || tToS[tc] != sc) {
                return false;
            }
        }
        return true;
    }

    /**
     * Follow-up: Generalize — given N strings, check if they are all mutually isomorphic?
     *   → Normalize each string to a "pattern" (first occurrence index encoding).
     *   → "egg" → "0 1 1", "add" → "0 1 1" — equal patterns = isomorphic.
     *   → Compare all normalized forms.
     */

    // =========================================================
    // E7. HAPPY NUMBER
    // Pattern: Cycle Detection via HashSet (Floyd's alternative)
    // =========================================================
    /**
     * Problem: A "happy number" eventually reaches 1 when repeatedly replaced
     * by the sum of squares of its digits. Detect if n is happy.
     *
     * Key insight: If not happy, the sequence enters a CYCLE.
     * We detect cycles using a HashSet of seen numbers.
     *
     * Alternatively: Floyd's cycle detection (slow/fast pointers) — O(1) space.
     *
     * Time:  O(log n) — digit sum reduces the number quickly
     * Space: O(log n) — number of steps before cycle
     */
    public boolean isHappy(int n) {
        Set<Integer> seen = new HashSet<>();
        while (n != 1) {
            if (!seen.add(n)) return false;  // cycle detected
            n = digitSquareSum(n);
        }
        return true;
    }

    private int digitSquareSum(int n) {
        int sum = 0;
        while (n > 0) {
            int d = n % 10;
            sum += d * d;
            n /= 10;
        }
        return sum;
    }

    /**
     * Follow-up: O(1) space solution?
     *   → Floyd's cycle detection: slow moves one step, fast moves two steps.
     *   → If they meet and the meeting point != 1 → not happy.
     */
    public boolean isHappyO1Space(int n) {
        int slow = n, fast = digitSquareSum(n);
        while (fast != 1 && slow != fast) {
            slow = digitSquareSum(slow);
            fast = digitSquareSum(digitSquareSum(fast));
        }
        return fast == 1;
    }

    // =========================================================
    // IMPORTS (add at file top in actual Java class)
    // =========================================================
    /*
     import java.util.*;
     import java.util.stream.*;
    */
}
