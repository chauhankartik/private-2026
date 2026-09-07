/**
 * ============================================================
 *  TWO POINTERS — GOOGLE-LEVEL PROBLEMS
 *  Multi-technique: two pointers + DP, binary search, greedy,
 *  or non-obvious pointer schemes.
 * ============================================================
 *
 *  Problems:
 *   G1. Minimum Number of Swaps to Make Strings Balanced (LC 1963)
 *   G2. Maximum Score of a Good Subarray (LC 1793)
 *   G3. Minimum Operations to Make Array Continuous (LC 2009)
 *   G4. Find the Longest Substring Containing Vowels in Even Counts (LC 1371)
 *   G5. Minimize Maximum Pair Sum in Array (LC 1877)
 *   G6. Reorder List (LC 143) — Find Mid + Reverse + Merge
 *
 * ============================================================
 */
import java.util.*;

class GoogleLevel {

    // =========================================================
    // G1. MINIMUM NUMBER OF SWAPS TO MAKE STRINGS BALANCED
    // Pattern: Two Pointers — Greedy Bracket Balancing
    // LeetCode 1963
    // =========================================================
    /**
     * Problem: Given string s of ']' and '[' of even length n (equal counts).
     * In one swap, choose any two characters and swap them.
     * Return the minimum swaps to make s balanced.
     *
     * Key observation:
     * A string with k mismatched pairs of brackets (unmatched ']' + '[')
     * requires ⌈k/2⌉ swaps (each swap fixes 2 mismatched pairs).
     *
     * Algorithm:
     * 1. Use two pointers: left scans right, right scans left.
     * 2. left finds an unmatched ']' (too many closing brackets).
     * 3. right finds an unmatched '[' (opening bracket after closing).
     * 4. Swap → fixes one pair of mismatches at left, one at right.
     *    Each such swap reduces mismatches by 2.
     *
     * Simpler mathematical derivation:
     * - Scan left to right, count open brackets.
     * - When count goes negative → unmatched ']' found.
     * - Count mismatched = max negative depth reached.
     * - Swaps = ⌈mismatched / 2⌉.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int minSwaps(String s) {
        // Count unmatched ']' using a balance counter
        int balance = 0; // +1 for '[', -1 for ']'
        int unmatched = 0; // max deficit = number of unmatched ']'

        for (char c : s.toCharArray()) {
            balance += (c == '[') ? 1 : -1;
            unmatched = Math.max(unmatched, -balance);
        }

        // Each swap fixes 2 unmatched brackets
        return (unmatched + 1) / 2;
    }

    /**
     * Follow-up 1: What if strings can have other bracket types ({}, ())?
     *   → Harder. Each type must be balanced independently AND nesting must be correct.
     *   → Stack-based approach.
     *
     * Follow-up 2: What if the equal count guarantee is removed?
     *   → First check if balance == 0 (total '[' == total ']'). If not, impossible.
     *
     * Follow-up 3: Prove ⌈k/2⌉ swaps is optimal:
     *   → Each swap can fix AT MOST 2 mismatched brackets (one ']' and one '[').
     *   → If there are k mismatches, we need at least k/2 swaps → lower bound ⌈k/2⌉.
     */

    // =========================================================
    // G2. MAXIMUM SCORE OF A GOOD SUBARRAY
    // Pattern: Two Pointers — Expand from Fixed Middle
    // LeetCode 1793
    // =========================================================
    /**
     * Problem: Given array nums of n integers and int k.
     * A good subarray satisfies: 0 <= i <= k <= j <= n-1.
     * Score = min(nums[i..j]) * (j - i + 1).
     * Return the maximum score of any good subarray.
     *
     * Key insight: Start from the required index k (must be in subarray).
     * Expand outward (left, right) greedily, always moving the side with
     * the LARGER neighbor (to maximize min value as long as possible).
     *
     * Why greedily expand the larger side?
     *   We want to keep the current minimum as large as possible.
     *   Expanding toward the side with the higher next value maximizes
     *   the chance that the window min stays unchanged.
     *   If both sides are equal, it doesn't matter which we expand.
     *
     * This is the "expand from center" variant of two pointers —
     * not squeeze inward, but EXPAND outward.
     *
     * Time: O(n) — left+right move at most n times total
     * Space: O(1)
     */
    public int maximumScore(int[] nums, int k) {
        int n = nums.length;
        int left = k, right = k;
        int minVal = nums[k];
        int maxScore = minVal; // window [k..k]

        while (left > 0 || right < n - 1) {
            // Expand toward the side with the higher next value
            boolean expandLeft  = (left  > 0)     && (right == n - 1 || nums[left - 1] >= nums[right + 1]);
            boolean expandRight = !expandLeft && (right < n - 1);

            if (expandLeft) {
                left--;
                minVal = Math.min(minVal, nums[left]);
            } else {
                right++;
                minVal = Math.min(minVal, nums[right]);
            }

            maxScore = Math.max(maxScore, minVal * (right - left + 1));
        }

        return maxScore;
    }

    /**
     * Follow-up 1: What if we also need to find the actual subarray [i, j]?
     *   → Track bestLeft, bestRight alongside maxScore.
     *
     * Follow-up 2: What if k can be any index (not fixed)?
     *   → This becomes the classic "Largest Rectangle in Histogram" (LC 84)
     *     solved with a monotonic stack in O(n).
     *
     * Follow-up 3: Prove this greedy is optimal?
     *   → Any valid subarray must contain k.
     *   → For any fixed minimum value m, the widest window with min ≥ m
     *     is the optimal window for that minimum.
     *   → Our expansion maintains this property.
     */

    // =========================================================
    // G3. MINIMUM OPERATIONS TO MAKE ARRAY CONTINUOUS
    // Pattern: Sort + Sliding Window / Two Pointers on Sorted Array
    // LeetCode 2009
    // =========================================================
    /**
     * Problem: An array is continuous if: length = n, all elements distinct,
     * max - min = n - 1 (consecutive values). Find the minimum replacements
     * to make nums continuous.
     *
     * Key observations:
     * 1. To minimize replacements, maximize elements we KEEP.
     * 2. We keep elements that fall in some window [x, x+n-1].
     * 3. Sort and deduplicate nums. Use two pointers / sliding window
     *    to find the window [nums[i], nums[i]+n-1] with the most elements.
     * 4. Answer = n - (max elements kept).
     *
     * Two-Pointer on sorted unique array:
     * - left pointer = start of window (nums[left]).
     * - right pointer scans while nums[right] <= nums[left] + n - 1.
     * - Count of usable elements = right - left.
     *
     * Why unique elements only?
     *   Duplicates can never coexist in a continuous array (all distinct).
     *   So duplicates are always replaced → remove them before counting.
     *
     * Time: O(n log n) for sort, O(n) for two-pointer pass
     * Space: O(n) for deduplication
     */
    public int minOperations(int[] nums) {
        int n = nums.length;
        Arrays.sort(nums);

        // Deduplicate
        int[] unique = Arrays.stream(nums).distinct().toArray();
        int m = unique.length;

        int maxKeep = 0;
        int right = 0;

        for (int left = 0; left < m; left++) {
            // Advance right while unique[right] fits in window [unique[left], unique[left]+n-1]
            while (right < m && unique[right] <= unique[left] + n - 1) {
                right++;
            }
            maxKeep = Math.max(maxKeep, right - left); // elements we can keep
        }

        return n - maxKeep;
    }

    /**
     * Follow-up 1: What if we can also delete elements (not just replace)?
     *   → Different problem. Minimizing deletions to make a continuous subarray.
     *   → Answer would be n - maxKeep (same formula, different interpretation).
     *
     * Follow-up 2: What if elements can be repeated in the result (non-distinct)?
     *   → The "consecutive values" constraint still requires distinct elements for uniqueness.
     */

    // =========================================================
    // G4. FIND THE LONGEST SUBSTRING CONTAINING VOWELS IN EVEN COUNTS
    // Pattern: Bitmask + Prefix XOR (Two-Pointer Inspired)
    // LeetCode 1371
    // =========================================================
    /**
     * Problem: Find the length of the longest substring of s where
     * every vowel (a, e, i, o, u) appears an even number of times.
     *
     * Even count ≡ parity = 0. Encode parities as a 5-bit bitmask.
     * - Bit 0: parity of 'a', Bit 1: parity of 'e', ..., Bit 4: parity of 'u'
     * - XOR with 1<<bit when we encounter a vowel (toggles parity).
     *
     * Key insight (prefix XOR):
     * A substring s[i+1..j] has all-even vowel counts iff
     *   prefix_mask[j] XOR prefix_mask[i] == 0
     *   i.e., prefix_mask[j] == prefix_mask[i].
     *
     * → Store the FIRST index where each mask was seen.
     * → For each j, if mask[j] was seen before at index i, candidate length = j - i.
     *
     * This is analogous to "subarray with equal number of 0s and 1s"
     * using prefix XOR instead of prefix sum.
     *
     * Time: O(n)
     * Space: O(32) = O(1) — at most 2^5 = 32 distinct masks
     */
    public int findTheLongestSubstring(String s) {
        Map<Integer, Integer> firstSeen = new HashMap<>();
        firstSeen.put(0, -1); // mask 0 seen before index 0 (empty prefix)

        String vowels = "aeiou";
        int mask = 0;
        int maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            int idx = vowels.indexOf(s.charAt(i));
            if (idx != -1) {
                mask ^= (1 << idx); // toggle parity of this vowel
            }

            if (firstSeen.containsKey(mask)) {
                maxLen = Math.max(maxLen, i - firstSeen.get(mask));
            } else {
                firstSeen.put(mask, i); // record first occurrence
            }
        }

        return maxLen;
    }

    /**
     * Follow-up 1: What if we need ODD counts of vowels (instead of even)?
     *   → For a single vowel: target mask = 1 << bit.
     *   → For all odd: target mask = 11111 (binary) = 31.
     *   → Look for firstSeen[mask ^ 31] instead of firstSeen[mask].
     *
     * Follow-up 2: Generalize to any set of characters with parity constraint?
     *   → Same bitmask approach. One bit per character in the constraint set.
     *   → Up to 64 characters (fits in a long).
     */

    // =========================================================
    // G5. MINIMIZE MAXIMUM PAIR SUM IN ARRAY
    // Pattern: Sort + Opposite-End Two Pointers — Greedy Pairing
    // LeetCode 1877
    // =========================================================
    /**
     * Problem: Given even-length array nums, pair up all elements.
     * The pair sum is the sum of paired elements. The "max pair sum"
     * is the largest sum among all pairs. Minimize the max pair sum.
     *
     * Greedy insight: Sort the array. Pair the smallest with the largest,
     * second smallest with second largest, etc.
     *
     * Proof (exchange argument):
     * Suppose we have optimal pairing with a cross (a_i paired with a_j,
     * a_k paired with a_l, where a_i < a_k < a_j < a_l).
     * Uncrossing: pair (a_i, a_l) and (a_k, a_j).
     * max(a_i+a_l, a_k+a_j) ≤ max(a_i+a_j, a_k+a_l) = max(a_k+a_l) [largest pair].
     * Uncrossing doesn't increase the max → we can always uncross → sorted pairing optimal.
     *
     * Time: O(n log n)
     * Space: O(1) extra
     */
    public int minPairSum(int[] nums) {
        Arrays.sort(nums);
        int maxPairSum = 0;
        int left = 0, right = nums.length - 1;

        while (left < right) {
            maxPairSum = Math.max(maxPairSum, nums[left] + nums[right]);
            left++;
            right--;
        }

        return maxPairSum;
    }

    /**
     * Follow-up 1: What if we want to MAXIMIZE the minimum pair sum?
     *   → Pair smallest with smallest, largest with largest.
     *   → Sort, then pair (0,1), (2,3), etc.
     *   → max of min pairs is tracked.
     *
     * Follow-up 2: What if we have k groups of size m (not pairs)?
     *   → Sort, then group-k elements at equidistant positions.
     *   → Generalized k-pointer pairing.
     */

    // =========================================================
    // G6. REORDER LIST
    // Pattern: Find Mid + Reverse Second Half + Merge
    // LeetCode 143
    // =========================================================
    /**
     * Problem: Given linked list L0 → L1 → ... → Ln, reorder it to:
     * L0 → Ln → L1 → Ln-1 → L2 → Ln-2 → ...
     * Must be done IN-PLACE.
     *
     * Algorithm (3-phase, each using two pointers):
     * Phase 1: Find the midpoint using slow/fast pointers.
     * Phase 2: Reverse the second half of the list.
     * Phase 3: Merge first half and reversed second half alternately.
     *
     * Why this works:
     *   After reversal, second half = [Ln, Ln-1, ..., Lmid+1].
     *   Merging first [L0,L1,...,Lmid] with reversed [Ln,Ln-1,...,Lmid+1]
     *   alternately produces exactly the required order.
     *
     * Time: O(n) — each phase is O(n)
     * Space: O(1) — no extra data structures
     */
    public void reorderList(ListNode head) {
        if (head == null || head.next == null) return;

        // Phase 1: Find midpoint
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Phase 2: Reverse the second half (starting from slow.next)
        ListNode secondHalf = slow.next;
        slow.next = null; // disconnect first and second halves
        secondHalf = reverseList(secondHalf);

        // Phase 3: Merge alternately
        ListNode first = head, second = secondHalf;
        while (second != null) {
            ListNode tmp1 = first.next;
            ListNode tmp2 = second.next;

            first.next = second;
            second.next = tmp1;

            first = tmp1;
            second = tmp2;
        }
    }

    /** Reverse a linked list in-place, return the new head. */
    private ListNode reverseList(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    // Helper ListNode class
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /**
     * Follow-up 1: What if we need to reorder in reverse (Ln→L0→Ln-1→L1)?
     *   → Reverse the ENTIRE list first, then reorder as above.
     *
     * Follow-up 2: What if this is a doubly-linked list?
     *   → Simpler: use opposite-end pointers, swap values as we go inward.
     *   → No need to reverse a segment.
     *
     * Follow-up 3: Can we verify correctness quickly?
     *   → For [1,2,3,4,5]: mid=3, second=[4,5], reversed=[5,4].
     *   → Merge: 1→5→2→4→3. Correct!
     */
}
