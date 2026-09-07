/**
 * ============================================================
 *  ARRAYS — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Remove Duplicates from Sorted Array
 *   E2. Best Time to Buy and Sell Stock
 *   E3. Merge Sorted Array
 *   E4. Move Zeroes
 *   E5. Plus One
 *   E6. Pascal's Triangle
 *   E7. Running Sum of 1D Array
 *   E8. Maximum Subarray (Kadane's Algorithm)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

public class Easy {

    // =========================================================
    // E1. REMOVE DUPLICATES FROM SORTED ARRAY
    // Pattern: Two Pointers (slow/fast — in-place write)
    // =========================================================
    /**
     * Problem: Given a sorted array nums, remove duplicates IN-PLACE
     * such that each element appears once. Return the new length k.
     *
     * Brute Force O(n²): For each duplicate, shift all elements left by one.
     *
     * Optimal O(n): Use two pointers:
     *   - `slow` = write position (index of last unique element)
     *   - `fast` = read position (scans all elements)
     *   - When nums[fast] != nums[slow], we found a new unique:
     *     increment slow, write nums[fast] there.
     *
     * Why does this work?
     *   Array is SORTED, so all duplicates are adjacent.
     *   The slow pointer only advances for unique elements,
     *   effectively "compacting" the array in-place.
     *
     * Time:  O(n) — single pass
     * Space: O(1) — in-place
     */
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;
        int slow = 0;
        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                nums[++slow] = nums[fast];
            }
        }
        return slow + 1;
    }

    /**
     * Follow-up 1: Allow at most 2 duplicates? (LeetCode 80)
     *   → Change condition: nums[fast] != nums[slow - 1]
     *   → Generalized: allow at most k → check nums[fast] != nums[slow - k + 1]
     */
    public int removeDuplicatesAtMostTwo(int[] nums) {
        if (nums.length <= 2) return nums.length;
        int slow = 2;
        for (int fast = 2; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow - 2]) {
                nums[slow++] = nums[fast];
            }
        }
        return slow;
    }

    /**
     * Follow-up 2: Unsorted array? Remove all instances of a given value?
     *   → Same two-pointer pattern, condition: nums[fast] != val
     *   → LeetCode 27 (Remove Element).
     *
     * Follow-up 3: What if we must maintain relative order for unsorted input?
     *   → Two-pointer still works; just skip matching elements.
     */

    // =========================================================
    // E2. BEST TIME TO BUY AND SELL STOCK
    // Pattern: Running Minimum (Kadane's variant)
    // =========================================================
    /**
     * Problem: Given prices[i] = price on day i, find max profit from
     * ONE buy and ONE sell (buy before sell).
     *
     * Brute Force O(n²): Try all (buy, sell) pairs.
     *
     * Optimal O(n): Track the MINIMUM price seen so far.
     *   For each day, profit = price[i] - minSoFar.
     *   Update answer = max(answer, profit).
     *
     * Why does this work?
     *   For any selling day i, the best buying day is the day with
     *   the lowest price in [0, i-1]. We track this running minimum
     *   incrementally, avoiding re-scanning.
     *
     * Connection to Kadane's: Define diff[i] = prices[i] - prices[i-1].
     *   Max profit = max subarray sum of diff[]. This is exactly Kadane's.
     *
     * Time:  O(n) — single pass
     * Space: O(1)
     */
    public int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;
        for (int price : prices) {
            minPrice = Math.min(minPrice, price);
            maxProfit = Math.max(maxProfit, price - minPrice);
        }
        return maxProfit;
    }

    /**
     * Follow-up 1: Unlimited transactions? (Buy/sell multiple times)
     *   → Greedy: add up ALL positive differences.
     *   → profit += max(0, prices[i] - prices[i-1])
     *   → O(n) time, O(1) space. (LeetCode 122)
     */
    public int maxProfitUnlimited(int[] prices) {
        int profit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                profit += prices[i] - prices[i - 1];
            }
        }
        return profit;
    }

    /**
     * Follow-up 2: At most 2 transactions? (LeetCode 123)
     *   → Track buy1, sell1, buy2, sell2 in one pass.
     *   → Generalized to k transactions: DP[k][2] states.
     *
     * Follow-up 3: With cooldown? (LeetCode 309)
     *   → State machine DP: hold, sold, rest states.
     */

    // =========================================================
    // E3. MERGE SORTED ARRAY
    // Pattern: Two Pointers from End (reverse merge)
    // =========================================================
    /**
     * Problem: nums1 has m elements + n extra zeros at end.
     * nums2 has n elements. Merge nums2 into nums1 sorted, in-place.
     *
     * Key Insight: Start from the END of both arrays.
     *   Compare largest unmerged from each, place at the back of nums1.
     *   This avoids overwriting elements we haven't processed.
     *
     * Why from the end?
     *   If we merge from the front, we'd overwrite nums1[i] before
     *   processing it — requires extra space. From the end, the
     *   destination slots (m to m+n-1) are guaranteed to be empty (zeros).
     *
     * Time:  O(m + n) — each element placed exactly once
     * Space: O(1)     — in-place
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1, p2 = n - 1, write = m + n - 1;
        while (p2 >= 0) {
            if (p1 >= 0 && nums1[p1] > nums2[p2]) {
                nums1[write--] = nums1[p1--];
            } else {
                nums1[write--] = nums2[p2--];
            }
        }
        // If p2 < 0, remaining nums1 elements are already in place
    }

    /**
     * Follow-up 1: Merge k sorted arrays?
     *   → Min-heap of size k. Poll smallest, push next from that array.
     *   → O(N log k) where N = total elements.
     *
     * Follow-up 2: What if nums1 doesn't have extra space?
     *   → Allocate result array of size m+n → standard merge. O(m+n) space.
     *
     * Follow-up 3: Merge two sorted linked lists instead?
     *   → LeetCode 21. Recursive or iterative merge with pointer manipulation.
     */

    // =========================================================
    // E4. MOVE ZEROES
    // Pattern: Two Pointers (partition / snowball technique)
    // =========================================================
    /**
     * Problem: Move all zeroes to end of array, maintaining order
     * of non-zero elements. Must be in-place.
     *
     * Approach: Two pointers — same as E1 pattern.
     *   `slow` = next write position for non-zero.
     *   `fast` = scans all elements.
     *   When nums[fast] != 0, swap nums[slow] and nums[fast], advance slow.
     *
     * Why swap instead of overwrite?
     *   Swap automatically places zeros at the end without a second pass.
     *
     * Time:  O(n) — single pass
     * Space: O(1) — in-place
     */
    public void moveZeroes(int[] nums) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) {
                // Swap nums[slow] and nums[fast]
                int temp = nums[slow];
                nums[slow] = nums[fast];
                nums[fast] = temp;
                slow++;
            }
        }
    }

    /**
     * Follow-up 1: Minimize total operations (writes)?
     *   → Overwrite version: copy non-zeros forward, fill rest with 0.
     *   → Fewer writes when many zeros exist.
     */
    public void moveZeroesMinWrites(int[] nums) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) nums[slow++] = nums[fast];
        }
        while (slow < nums.length) nums[slow++] = 0;
    }

    /**
     * Follow-up 2: Move all instances of a specific value to end?
     *   → Same pattern: change condition from != 0 to != target.
     *
     * Follow-up 3: Partition array into negatives (left) and positives (right)?
     *   → Two-pointer swap. Similar to Dutch National Flag (see Medium M6).
     */

    // =========================================================
    // E5. PLUS ONE
    // Pattern: Array Simulation (Carry Propagation)
    // =========================================================
    /**
     * Problem: Given a number as an array of digits, add one.
     * digits = [1, 2, 9] → [1, 3, 0]
     * digits = [9, 9, 9] → [1, 0, 0, 0]
     *
     * Key insight: Process RIGHT to LEFT (like manual addition).
     *   - If digit < 9, increment and return immediately (no carry).
     *   - If digit == 9, set to 0 and continue (carry propagates).
     *   - If we exit the loop, ALL digits were 9 → new array [1, 0, 0, ..., 0].
     *
     * Time:  O(n) — worst case (all 9s), but average O(1) (early return)
     * Space: O(1) — or O(n) only when a new array is needed (all 9s)
     */
    public int[] plusOne(int[] digits) {
        for (int i = digits.length - 1; i >= 0; i--) {
            if (digits[i] < 9) {
                digits[i]++;
                return digits;  // no carry → done!
            }
            digits[i] = 0;  // carry propagates
        }
        // All digits were 9 → need longer array
        int[] result = new int[digits.length + 1];
        result[0] = 1;  // rest are already 0
        return result;
    }

    /**
     * Follow-up 1: Add two numbers represented as arrays? (LeetCode 66/2)
     *   → Process both arrays from right to left with carry.
     *
     * Follow-up 2: Subtract one instead of add?
     *   → Handle borrow instead of carry. Edge case: [1, 0, 0] → [0, 9, 9] → strip leading zeros.
     *
     * Follow-up 3: Multiply number array by a single digit?
     *   → Each digit: product = digit * multiplier + carry. Store product % 10, carry = product / 10.
     */

    // =========================================================
    // E6. PASCAL'S TRIANGLE
    // Pattern: Dynamic Programming on Array (build row from previous)
    // =========================================================
    /**
     * Problem: Generate first numRows rows of Pascal's Triangle.
     *
     * Recurrence: triangle[i][j] = triangle[i-1][j-1] + triangle[i-1][j]
     * Base cases: triangle[i][0] = triangle[i][i] = 1
     *
     * Why is this DP?
     *   Each value depends on two values from the previous row.
     *   We build bottom-up, row by row.
     *
     * Time:  O(n²) — n rows, row i has i+1 elements → ∑i = n(n+1)/2
     * Space: O(n²) — storing all rows (O(n) if only computing one row)
     */
    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> triangle = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                if (j == 0 || j == i) {
                    row.add(1);
                } else {
                    row.add(triangle.get(i - 1).get(j - 1) + triangle.get(i - 1).get(j));
                }
            }
            triangle.add(row);
        }
        return triangle;
    }

    /**
     * Follow-up 1: Return only the k-th row in O(k) space? (LeetCode 119)
     *   → Build row in-place using a single array, updating RIGHT to LEFT.
     */
    public List<Integer> getRow(int rowIndex) {
        List<Integer> row = new ArrayList<>(Collections.nCopies(rowIndex + 1, 1));
        for (int i = 2; i <= rowIndex; i++) {
            for (int j = i - 1; j >= 1; j--) {  // right to left!
                row.set(j, row.get(j) + row.get(j - 1));
            }
        }
        return row;
    }

    /**
     * Follow-up 2: Compute a specific entry C(n, k) efficiently?
     *   → C(n, k) = C(n, k-1) * (n-k+1) / k  (iterative formula, avoids overflow better)
     *
     * Follow-up 3: Is triangle[i][j] always C(i, j)?
     *   → Yes. Pascal's triangle encodes binomial coefficients.
     *   → This connects to combinatorics in Google interviews.
     */

    // =========================================================
    // E7. RUNNING SUM OF 1D ARRAY
    // Pattern: Prefix Sum (in-place)
    // =========================================================
    /**
     * Problem: Return running sum where runningSum[i] = sum(nums[0]..nums[i]).
     *
     * This is the SIMPLEST form of prefix sum — the foundation for
     * many harder problems (subarray sum = k, range queries, etc.).
     *
     * In-place: nums[i] += nums[i-1]
     *
     * Why prefix sums matter:
     *   After building prefix array, sum(i..j) = prefix[j] - prefix[i-1]
     *   → O(1) range sum query after O(n) preprocessing.
     *
     * Time:  O(n) — single pass
     * Space: O(1) — in-place modification (or O(n) if new array)
     */
    public int[] runningSum(int[] nums) {
        for (int i = 1; i < nums.length; i++) {
            nums[i] += nums[i - 1];
        }
        return nums;
    }

    /**
     * Follow-up 1: 2D prefix sum (range sum on a matrix)?
     *   → prefix[i][j] = sum of submatrix (0,0) to (i,j)
     *   → Build: prefix[i][j] = matrix[i][j] + prefix[i-1][j] + prefix[i][j-1] - prefix[i-1][j-1]
     *   → Query: O(1) per submatrix query. (LeetCode 304)
     *
     * Follow-up 2: Prefix XOR?
     *   → Same pattern: xor[i] = xor[i-1] ^ nums[i]
     *   → xor(l..r) = xor[r] ^ xor[l-1] (XOR is its own inverse!)
     *
     * Follow-up 3: Prefix product?
     *   → Same pattern but beware of zeros (can't divide by zero).
     *   → See Medium M2 (Product Except Self) for the workaround.
     */

    // =========================================================
    // E8. MAXIMUM SUBARRAY (KADANE'S ALGORITHM)
    // Pattern: Kadane's Algorithm (DP / Greedy on subarrays)
    // =========================================================
    /**
     * Problem: Find the contiguous subarray with the largest sum.
     *
     * Brute Force O(n²): Try all (i, j) subarrays, compute sum.
     *   (O(n³) if computing sum from scratch each time.)
     *
     * Divide and Conquer O(n log n): Split array, solve left/right,
     *   then find max crossing subarray.
     *
     * Kadane's O(n): At each position, decide:
     *   "Is it better to EXTEND the current subarray or START a new one?"
     *
     *   maxEndingHere = max(nums[i], maxEndingHere + nums[i])
     *   maxSoFar = max(maxSoFar, maxEndingHere)
     *
     * Why does this work? (Proof by optimal substructure)
     *   Let OPT(i) = max subarray sum ENDING at index i.
     *   Either:
     *     a) The best subarray ending at i includes element i-1 → OPT(i) = OPT(i-1) + nums[i]
     *     b) The best subarray starts fresh at i → OPT(i) = nums[i]
     *   OPT(i) = max(a, b) = max(OPT(i-1) + nums[i], nums[i])
     *   Answer = max over all i of OPT(i)
     *
     * Time:  O(n) — single pass
     * Space: O(1) — two variables
     */
    public int maxSubArray(int[] nums) {
        int maxEndingHere = nums[0];
        int maxSoFar = nums[0];
        for (int i = 1; i < nums.length; i++) {
            maxEndingHere = Math.max(nums[i], maxEndingHere + nums[i]);
            maxSoFar = Math.max(maxSoFar, maxEndingHere);
        }
        return maxSoFar;
    }

    /**
     * Follow-up 1: Return the actual subarray (indices), not just the sum?
     *   → Track start/end indices. Reset start when maxEndingHere resets.
     */
    public int[] maxSubArrayWithIndices(int[] nums) {
        int maxEndingHere = nums[0], maxSoFar = nums[0];
        int start = 0, end = 0, tempStart = 0;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > maxEndingHere + nums[i]) {
                maxEndingHere = nums[i];
                tempStart = i;
            } else {
                maxEndingHere += nums[i];
            }
            if (maxEndingHere > maxSoFar) {
                maxSoFar = maxEndingHere;
                start = tempStart;
                end = i;
            }
        }
        return new int[]{start, end, maxSoFar};
    }

    /**
     * Follow-up 2: Maximum CIRCULAR subarray sum? (LeetCode 918)
     *   → max(Kadane's normal, total - Kadane's on NEGATIVE array)
     *   → Edge case: all negatives → return max single element.
     *
     * Follow-up 3: Maximum subarray product? (LeetCode 152)
     *   → Track both maxProduct and minProduct (negative × negative = positive).
     *   → maxEndHere = max(nums[i], maxEndHere * nums[i], minEndHere * nums[i])
     */
}
