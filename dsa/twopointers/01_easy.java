
/**
 * ============================================================
 *  TWO POINTERS — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Two Sum II — Input Array Is Sorted (LC 167)
 *   E2. Valid Palindrome (LC 125)
 *   E3. Remove Duplicates from Sorted Array (LC 26)
 *   E4. Move Zeroes (LC 283)
 *   E5. Squares of a Sorted Array (LC 977)
 *   E6. Reverse String (LC 344)
 *   E7. Remove Element (LC 27)
 *   E8. Merge Sorted Array (LC 88)
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
    // E1. TWO SUM II — INPUT ARRAY IS SORTED
    // Pattern: Opposite-End Two Pointers — Pair Sum
    // LeetCode 167
    // =========================================================
    /**
     * Problem: Given a 1-indexed sorted array numbers, find two numbers
     * that add up to target. Return their 1-based indices.
     *
     * Brute Force O(n²): Try every pair (i, j). Too slow.
     *
     * Optimal O(n): Opposite-end squeeze.
     * 1. Place left at 0, right at n-1.
     * 2. Compute sum = numbers[left] + numbers[right].
     * 3. If sum == target → return {left+1, right+1}.
     *    If sum < target  → left++ (need larger sum).
     *    If sum > target  → right-- (need smaller sum).
     *
     * Why is this correct?
     *   When sum < target, can any pair with numbers[left] work?
     *   Only if the right element is larger. But right is already at max.
     *   So numbers[left] CAN'T form a valid pair → advance left.
     *   Symmetric argument for sum > target → retreat right.
     *
     * Time: O(n) — each pointer moves at most n times
     * Space: O(1)
     */
    public int[] twoSum(int[] numbers, int target) {
        int left = 0, right = numbers.length - 1;

        while (left < right) {
            int sum = numbers[left] + numbers[right];
            if (sum == target) {
                return new int[]{left + 1, right + 1}; // 1-indexed
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
        return new int[]{-1, -1}; // guaranteed to find by problem constraints
    }

    /**
     * Follow-up 1: What if the array is NOT sorted?
     *   → Can't use two pointers directly. Use HashMap in O(n).
     *   → Or sort first O(n log n), then two pointers. But indices change after sort.
     *
     * Follow-up 2: What if there are MULTIPLE pairs with target sum?
     *   → Collect all: when found, record pair, advance both left++ and right--.
     *   → Still O(n).
     *
     * Follow-up 3: What if numbers can overflow int?
     *   → Cast to long: (long) numbers[left] + numbers[right].
     */

    // =========================================================
    // E2. VALID PALINDROME
    // Pattern: Opposite-End Two Pointers — Palindrome Check
    // LeetCode 125
    // =========================================================
    /**
     * Problem: Given string s, return true if it is a palindrome
     * considering only alphanumeric characters and ignoring case.
     *
     * Brute Force O(n): Clean the string → reverse → compare.
     * This is fine, but uses O(n) extra space.
     *
     * Optimal O(n), O(1) extra space: In-place two pointers.
     * - left starts at 0, right at end.
     * - Skip non-alphanumeric characters on both sides.
     * - Compare lowercase versions. If mismatch → not palindrome.
     *
     * Time: O(n) — each character visited at most once
     * Space: O(1) — no string cleaning
     */
    public boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;

        while (left < right) {
            // Skip non-alphanumeric from left
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            // Skip non-alphanumeric from right
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }
            // Compare (case-insensitive)
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /**
     * Follow-up 1: Valid Palindrome II (LC 680) — at most ONE deletion allowed?
     *   → When mismatch at (left, right), try skipping left OR right.
     *   → If either resulting substring is a palindrome → valid.
     *
     * Follow-up 2: How to handle Unicode (not just ASCII)?
     *   → Character.isLetterOrDigit() handles Unicode correctly.
     *   → Character.toLowerCase() is locale-sensitive; prefer
     *     Character.toLowerCase(int codePoint) for Unicode correctness.
     */

    // =========================================================
    // E3. REMOVE DUPLICATES FROM SORTED ARRAY
    // Pattern: Same-Direction Two Pointers — Read/Write
    // LeetCode 26
    // =========================================================
    /**
     * Problem: Given sorted array nums, remove duplicates IN-PLACE.
     * Return the count k of unique elements (first k elements are unique).
     *
     * Key insight: Use a write pointer (slow) and a read pointer (fast).
     * - slow: the position of the last written unique element.
     * - fast: scans ahead looking for the next new unique element.
     * - When nums[fast] != nums[slow]: a new unique element found → write it.
     *
     * Why does this work?
     *   Array is SORTED → all duplicates of a value are adjacent.
     *   So nums[fast] != nums[slow] guarantees a truly new value.
     *
     * Time: O(n) — fast pointer makes a single pass
     * Space: O(1) — in-place modification
     */
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;

        int slow = 0; // write head: points to last written unique element

        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                slow++;
                nums[slow] = nums[fast]; // write next unique element
            }
        }
        return slow + 1; // number of unique elements
    }

    /**
     * Follow-up 1: Remove Duplicates II (LC 80) — allow at most k=2 occurrences?
     *   → Compare nums[fast] with nums[slow - 1] (two slots back).
     *   → If different, it hasn't appeared twice yet → write it.
     *
     * public int removeDuplicatesII(int[] nums) {
     *     int slow = 0;
     *     for (int fast = 0; fast < nums.length; fast++) {
     *         if (slow < 2 || nums[fast] != nums[slow - 2]) {
     *             nums[slow++] = nums[fast];
     *         }
     *     }
     *     return slow;
     * }
     *
     * Follow-up 2: What if the array is NOT sorted?
     *   → This approach fails. Need a HashSet to track seen elements.
     */

    // =========================================================
    // E4. MOVE ZEROES
    // Pattern: Same-Direction Two Pointers — Partition
    // LeetCode 283
    // =========================================================
    /**
     * Problem: Move all zeros in nums to the end IN-PLACE,
     * maintaining the relative order of non-zero elements.
     *
     * Approach: Write pointer places non-zero elements at the front.
     * After pass, fill the rest with zeros.
     *
     * Alternative (fewer writes): swap-based approach.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public void moveZeroes(int[] nums) {
        int slow = 0; // write pointer for non-zero elements

        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) {
                nums[slow++] = nums[fast]; // write non-zero
            }
        }
        // Fill remaining positions with zero
        while (slow < nums.length) {
            nums[slow++] = 0;
        }
    }

    /**
     * Follow-up: Minimize write operations (swap instead of overwrite)?
     *   → Swap nums[fast] with nums[slow] when nums[fast] != 0.
     *   → Each non-zero element is swapped at most once.
     *
     * public void moveZeroesSwap(int[] nums) {
     *     int slow = 0;
     *     for (int fast = 0; fast < nums.length; fast++) {
     *         if (nums[fast] != 0) {
     *             int tmp = nums[slow]; nums[slow] = nums[fast]; nums[fast] = tmp;
     *             slow++;
     *         }
     *     }
     * }
     *
     * Follow-up 2: What if we want zeros at the FRONT?
     *   → Run the scan from right to left, writing from the right end.
     */

    // =========================================================
    // E5. SQUARES OF A SORTED ARRAY
    // Pattern: Opposite-End Two Pointers — Merge-Like Fill
    // LeetCode 977
    // =========================================================
    /**
     * Problem: Given integer array nums sorted in non-decreasing order,
     * return an array of the squares of each number, also sorted.
     *
     * Brute Force O(n log n): Square all, then sort.
     *
     * Optimal O(n): Opposite-end two pointers, fill result from right.
     * - The largest square is always at the leftmost or rightmost end
     *   (because large negatives and large positives have large squares).
     * - Compare |nums[left]| vs |nums[right]|.
     * - Place the larger square at result[k--] and advance that pointer inward.
     *
     * Why fill from right?
     *   We're picking maximums → it's natural to place them at the end.
     *   Filling from left would require knowing the minimum first.
     *
     * Time: O(n) — single pass filling result
     * Space: O(n) — output array (not counted as extra space in LC)
     */
    public int[] sortedSquares(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        int left = 0, right = n - 1;
        int k = n - 1; // fill from right

        while (left <= right) {
            int leftSq = nums[left] * nums[left];
            int rightSq = nums[right] * nums[right];

            if (leftSq > rightSq) {
                result[k--] = leftSq;
                left++;
            } else {
                result[k--] = rightSq;
                right--;
            }
        }
        return result;
    }

    /**
     * Follow-up 1: What if we need to return the MINIMUM squared value?
     *   → After squaring, the minimum is somewhere in the middle.
     *   → Use binary search to find the element closest to 0, then expand outward.
     *
     * Follow-up 2: Can you do it in one line?
     *   → IntStream.of(nums).map(x -> x*x).sorted().toArray() — O(n log n) though.
     */

    // =========================================================
    // E6. REVERSE STRING
    // Pattern: Opposite-End Two Pointers — Symmetric Swap
    // LeetCode 344
    // =========================================================
    /**
     * Problem: Reverse a char array s in-place with O(1) extra memory.
     *
     * Pattern: Classic symmetric swap with opposite-end pointers.
     * Swap s[left] and s[right] while left < right.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public void reverseString(char[] s) {
        int left = 0, right = s.length - 1;

        while (left < right) {
            char temp = s[left];
            s[left++] = s[right];
            s[right--] = temp;
        }
    }

    /**
     * Follow-up 1: Reverse Words in a String (LC 151)?
     *   → Reverse the entire string, then reverse each word individually.
     *   → Two-pass O(n) approach.
     *
     * Follow-up 2: Reverse only the vowels (LC 345)?
     *   → Same two-pointer swap, but advance pointers past consonants.
     */

    // =========================================================
    // E7. REMOVE ELEMENT
    // Pattern: Same-Direction Two Pointers — Filter
    // LeetCode 27
    // =========================================================
    /**
     * Problem: Remove all occurrences of val from nums in-place.
     * Return k = count of elements not equal to val.
     *
     * Same read/write pointer pattern as E3, but simpler:
     * the array doesn't need to be sorted.
     * Just copy every non-val element to the write head.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int removeElement(int[] nums, int val) {
        int slow = 0; // write pointer

        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != val) {
                nums[slow++] = nums[fast];
            }
        }
        return slow;
    }

    /**
     * Follow-up: What if val is very RARE (most elements are not val)?
     *   → Optimization: swap the found val with the last element, shrink the array.
     *   → Minimizes writes when few elements are removed.
     *
     * public int removeElementRare(int[] nums, int val) {
     *     int k = nums.length;
     *     for (int i = 0; i < k; ) {
     *         if (nums[i] == val) {
     *             nums[i] = nums[--k]; // replace with last element
     *         } else {
     *             i++;
     *         }
     *     }
     *     return k;
     * }
     */

    // =========================================================
    // E8. MERGE SORTED ARRAY
    // Pattern: Merge Two Arrays — Three Pointers (Fill from Right)
    // LeetCode 88
    // =========================================================
    /**
     * Problem: nums1 has m valid elements + n spaces. nums2 has n elements.
     * Merge nums2 into nums1 IN-PLACE, keeping sorted order.
     *
     * Brute Force: Copy nums2 into nums1, then sort. O((m+n) log(m+n)).
     *
     * Optimal O(m+n): Fill nums1 from RIGHT to LEFT.
     * - p1 = m-1 (last valid in nums1), p2 = n-1 (last in nums2), k = m+n-1.
     * - Compare nums1[p1] and nums2[p2]: place the larger at nums1[k--].
     * - After one array is exhausted, copy remaining from nums2.
     *
     * Why fill from right?
     *   Writing from right-to-left never overwrites unprocessed elements of nums1
     *   (we're filling into the empty space first, then merging backwards).
     *
     * Time: O(m+n)
     * Space: O(1) — in-place
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;     // last valid in nums1
        int p2 = n - 1;     // last in nums2
        int k = m + n - 1;  // fill from right

        while (p1 >= 0 && p2 >= 0) {
            if (nums1[p1] >= nums2[p2]) {
                nums1[k--] = nums1[p1--];
            } else {
                nums1[k--] = nums2[p2--];
            }
        }
        // If nums2 still has elements, copy them
        // (If nums1 still has elements, they're already in place)
        while (p2 >= 0) {
            nums1[k--] = nums2[p2--];
        }
    }

    /**
     * Follow-up 1: What if we had two separate arrays and needed to merge into a new array?
     *   → Simple two-pointer merge: no need for right-to-left trick.
     *
     * Follow-up 2: Merge k sorted arrays?
     *   → Use a min-heap (PriorityQueue) of size k. Pop minimum, push next from same array.
     *   → O(N log k) where N = total elements.
     */
}
