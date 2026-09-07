/**
 * ============================================================
 *  GOOGLE-LEVEL ARRAY PROBLEMS
 *
 *  These problems are representative of actual Google interview
 *  questions. Each problem is designed to test MULTIPLE topics
 *  simultaneously and require system-level thinking.
 *
 *  Problems:
 *   G1. Count of Range Sum (Merge Sort + Counting)
 *   G2. Shortest Subarray with Sum at Least K (Monotonic Deque + Prefix Sum)
 *   G3. Maximum Sum of 3 Non-Overlapping Subarrays (DP + Prefix Sum)
 *   G4. Split Array Largest Sum (Binary Search on Answer)
 *   G5. Candy (Two-Pass Greedy)
 *   G6. Minimum Number of Increments on Subarrays to Form a Target Array
 *
 *  Multi-topic matrix:
 *   G1: Merge Sort + Counting + Divide and Conquer
 *   G2: Monotonic Deque + Prefix Sum + Negative numbers handling
 *   G3: DP + Prefix Sum + Interval optimization
 *   G4: Binary Search on Answer + Greedy validation
 *   G5: Greedy + Multi-pass + Constraint propagation
 *   G6: Greedy + Difference Array reasoning + Math
 * ============================================================
 */
import java.util.*;

public class GoogleLevel {

    // =========================================================
    // G1. COUNT OF RANGE SUM
    // Topics: Merge Sort + Counting + Divide & Conquer
    // =========================================================
    /**
     * Problem: Given integer array nums, count range sums S(i,j) = sum(nums[i..j])
     * that lie in [lower, upper] inclusive.
     *
     * Brute Force O(n²): Compute all prefix sum differences.
     *
     * Key insight: Let prefix[i] = sum(nums[0..i-1]).
     *   Range sum S(i,j) = prefix[j+1] - prefix[i].
     *   We need: lower ≤ prefix[j+1] - prefix[i] ≤ upper, for i ≤ j.
     *   Equivalently: prefix[j+1] - upper ≤ prefix[i] ≤ prefix[j+1] - lower.
     *
     *   This is a "count inversions" variant → solvable with MERGE SORT.
     *   During merge, for each element in the right half, count valid
     *   elements in the left half using two pointers (sorted order).
     *
     * Why merge sort?
     *   After sorting, the left half contains earlier prefix sums.
     *   For each element in the right half, we can binary-search/two-pointer
     *   to count valid matches in the left half.
     *   Merge sort guarantees we compare all (left, right) valid pairs.
     *
     * Time:  O(n log n) — merge sort with O(n) counting per level
     * Space: O(n)       — auxiliary array for merging
     *
     * Topics tested: Divide and conquer, prefix sums, counting during merge.
     */
    public int countRangeSum(int[] nums, int lower, int upper) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        return mergeCount(prefix, 0, prefix.length, lower, upper);
    }

    private int mergeCount(long[] prefix, int lo, int hi, int lower, int upper) {
        if (hi - lo <= 1) return 0;
        int mid = lo + (hi - lo) / 2;
        int count = mergeCount(prefix, lo, mid, lower, upper)
                  + mergeCount(prefix, mid, hi, lower, upper);

        // Count valid pairs: for each j in [mid, hi), count i in [lo, mid) where
        //   lower ≤ prefix[j] - prefix[i] ≤ upper
        //   ↔ prefix[j] - upper ≤ prefix[i] ≤ prefix[j] - lower
        int t = lo, u = lo;
        for (int j = mid; j < hi; j++) {
            while (t < mid && prefix[t] < prefix[j] - upper) t++;
            while (u < mid && prefix[u] <= prefix[j] - lower) u++;
            count += u - t;
        }

        // Standard merge
        long[] sorted = new long[hi - lo];
        int p1 = lo, p2 = mid, idx = 0;
        while (p1 < mid && p2 < hi) {
            sorted[idx++] = prefix[p1] <= prefix[p2] ? prefix[p1++] : prefix[p2++];
        }
        while (p1 < mid) sorted[idx++] = prefix[p1++];
        while (p2 < hi) sorted[idx++] = prefix[p2++];
        System.arraycopy(sorted, 0, prefix, lo, sorted.length);

        return count;
    }

    /**
     * Google Follow-ups:
     *
     * 1. How would you solve this with a balanced BST / BIT?
     *    → Insert prefix sums into a BIT (Binary Indexed Tree) / segment tree.
     *    → For each prefix[j], query count in range [prefix[j]-upper, prefix[j]-lower].
     *    → Coordinate compress prefix values first. O(n log n).
     *
     * 2. What if we just need to check if ANY range sum is in [lower, upper]?
     *    → Early termination in merge sort. Or prefix sum + sorted set query.
     *
     * 3. What about LONGEST subarray with sum in [lower, upper]?
     *    → Harder — requires monotonic deque or balanced BST with augmented queries.
     */

    // =========================================================
    // G2. SHORTEST SUBARRAY WITH SUM AT LEAST K
    // Topics: Monotonic Deque + Prefix Sum + Negative Numbers
    // =========================================================
    /**
     * Problem: Find the shortest subarray with sum ≥ k. (LeetCode 862)
     * Array can have NEGATIVE numbers.
     *
     * Why can't we use simple sliding window?
     *   Standard sliding window assumes all positive numbers (monotonic prefix sum).
     *   With negatives, shrinking from left might skip a valid shorter window.
     *
     * Key insight — Monotonic deque on prefix sums:
     *   Compute prefix[i] = sum(nums[0..i-1]).
     *   We need: prefix[j] - prefix[i] ≥ k with j > i, minimizing j - i.
     *
     *   Maintain a DEQUE of indices into prefix[], in INCREASING order of prefix values.
     *
     *   For each j:
     *     1. While prefix[j] - prefix[deque.front()] ≥ k:
     *        → We found a valid window! Record length. Pop front.
     *        → Why pop? Any future j' > j would give a LONGER window with same i.
     *     2. While prefix[deque.back()] ≥ prefix[j]:
     *        → Remove from back. If prefix[j] is smaller, it's a better starting point.
     *     3. Push j to deque.
     *
     * Time:  O(n) — each index entered/removed from deque at most once
     * Space: O(n) — prefix array + deque
     *
     * Topics tested: Why sliding window fails with negatives, monotonic deque reasoning.
     */
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        Deque<Integer> deque = new ArrayDeque<>();
        int minLen = n + 1;

        for (int j = 0; j <= n; j++) {
            // Try to find valid windows (shrink from left)
            while (!deque.isEmpty() && prefix[j] - prefix[deque.peekFirst()] >= k) {
                minLen = Math.min(minLen, j - deque.pollFirst());
            }
            // Maintain monotonicity (remove larger prefix sums from back)
            while (!deque.isEmpty() && prefix[deque.peekLast()] >= prefix[j]) {
                deque.pollLast();
            }
            deque.offerLast(j);
        }
        return minLen <= n ? minLen : -1;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if all numbers are positive?
     *    → Simple sliding window works. O(n) time, O(1) space.
     *    → The deque approach is strictly more general.
     *
     * 2. LONGEST subarray with sum ≥ k?
     *    → Cannot pop from front (we want longest). Use prefix + binary search.
     *    → Or: since we want max length, use prefix sum + monotonic approach
     *      but track differently.
     *
     * 3. Prove that the deque approach doesn't miss any valid pairs.
     *    → When we pop index i from front (found valid pair with j):
     *      For any future j' > j, the pair (i, j') gives j'-i > j-i (longer window).
     *      Since we want SHORTEST, i is no longer useful → safe to pop.
     *    → When we pop index i from back (prefix[i] ≥ prefix[j]):
     *      For any future query, j is a strictly better starting point than i
     *      (earlier or equal prefix sum, later index). i is dominated → safe to pop.
     */

    // =========================================================
    // G3. MAXIMUM SUM OF 3 NON-OVERLAPPING SUBARRAYS
    // Topics: DP + Prefix Sum + Interval Optimization
    // =========================================================
    /**
     * Problem: Find three non-overlapping subarrays of length k with
     * maximum total sum. Return starting indices (lexicographically smallest).
     * (LeetCode 689)
     *
     * Algorithm — Three-pass DP:
     *   1. Compute prefix sums → O(1) range sum queries.
     *   2. Build left[i]: best start index for one k-subarray in [0, i].
     *   3. Build right[i]: best start index for one k-subarray in [i, n-k].
     *   4. For each middle subarray starting at j (k ≤ j ≤ n-2k):
     *      Total = sum(best_left) + sum(middle at j) + sum(best_right)
     *      Track the maximum.
     *
     * Why three passes?
     *   This avoids O(n³) brute force (trying all triples).
     *   Precomputing best-left and best-right reduces the problem to:
     *   "For each middle position, what's the best companion on each side?"
     *
     * Time:  O(n) — three linear passes
     * Space: O(n) — prefix sums + left/right arrays
     *
     * Topics tested: DP optimization, prefix sums, lexicographic tie-breaking.
     */
    public int[] maxSumOfThreeSubarrays(int[] nums, int k) {
        int n = nums.length;

        // Step 1: Compute window sums (sliding window of size k)
        int[] windowSum = new int[n - k + 1];
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += nums[i];
            if (i >= k) sum -= nums[i - k];
            if (i >= k - 1) windowSum[i - k + 1] = sum;
        }

        // Step 2: left[i] = index of best window starting in [0, i]
        int[] left = new int[windowSum.length];
        int bestLeft = 0;
        for (int i = 0; i < windowSum.length; i++) {
            if (windowSum[i] > windowSum[bestLeft]) bestLeft = i;
            left[i] = bestLeft;
        }

        // Step 3: right[i] = index of best window starting in [i, end]
        int[] right = new int[windowSum.length];
        int bestRight = windowSum.length - 1;
        for (int i = windowSum.length - 1; i >= 0; i--) {
            if (windowSum[i] >= windowSum[bestRight]) bestRight = i; // ≥ for lex smallest
            right[i] = bestRight;
        }

        // Step 4: Try each middle position
        int[] result = new int[3];
        int maxTotal = 0;
        for (int mid = k; mid <= windowSum.length - k - 1; mid++) {
            int l = left[mid - k], r = right[mid + k];
            int total = windowSum[l] + windowSum[mid] + windowSum[r];
            if (total > maxTotal) {
                maxTotal = total;
                result = new int[]{l, mid, r};
            }
        }
        return result;
    }

    /**
     * Google Follow-ups:
     *
     * 1. Generalize to m non-overlapping subarrays? (m arbitrary)
     *    → DP: dp[t][i] = max sum using t subarrays in nums[0..i].
     *    → O(n × m) time. Reconstruct indices with backtracking.
     *
     * 2. Non-overlapping subarrays of DIFFERENT sizes?
     *    → DP with variable window sizes. Much harder — O(n² × m).
     *
     * 3. What if subarrays can overlap?
     *    → Top-k windows by sum, greedily select. May need priority queue.
     */

    // =========================================================
    // G4. SPLIT ARRAY LARGEST SUM
    // Topics: Binary Search on Answer + Greedy Validation
    // =========================================================
    /**
     * Problem: Split array into m subarrays to minimize the largest
     * subarray sum. (LeetCode 410)
     *
     * Key insight — Binary Search on the answer:
     *   The answer (largest subarray sum) lies in:
     *     - Lower bound: max(nums) (each element must be in some subarray)
     *     - Upper bound: sum(nums) (one subarray containing everything)
     *
     *   Binary search: "Can we split into ≤ m subarrays where each sum ≤ mid?"
     *   This is a MONOTONIC predicate — if we can do it for mid, we can for mid+1.
     *
     * Greedy validation (canSplit):
     *   Greedily pack elements left-to-right until adding next would exceed `mid`.
     *   Start a new subarray. Count subarrays needed. Return count ≤ m.
     *
     * Why greedy validation is optimal:
     *   If we take fewer elements in an earlier subarray than possible,
     *   the remaining subarrays get MORE elements → larger sums.
     *   Greedy (take as many as possible) minimizes the number of splits.
     *
     * Time:  O(n × log(sum - max)) — binary search × validation
     * Space: O(1)
     *
     * Topics tested: Binary search on answer, greedy proof, monotonic predicates.
     */
    public int splitArray(int[] nums, int m) {
        long lo = 0, hi = 0;
        for (int num : nums) {
            lo = Math.max(lo, num);   // can't have subarray sum < max element
            hi += num;                 // one subarray = entire array
        }

        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (canSplit(nums, m, mid)) {
                hi = mid;       // feasible — try smaller
            } else {
                lo = mid + 1;   // not feasible — need larger limit
            }
        }
        return (int) lo;
    }

    private boolean canSplit(int[] nums, int m, long maxSum) {
        int count = 1;
        long currSum = 0;
        for (int num : nums) {
            currSum += num;
            if (currSum > maxSum) {
                count++;
                currSum = num;
                if (count > m) return false;
            }
        }
        return true;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if we want to minimize the SUM of all subarray sums?
     *    → That's always total sum, regardless of splitting. Trick question!
     *
     * 2. DP approach?
     *    → dp[i][j] = min largest sum splitting first i elements into j parts.
     *    → Transition: dp[i][j] = min over k of max(dp[k][j-1], sum(k+1..i)).
     *    → O(n² × m). Slower than binary search but conceptually clearer.
     *
     * 3. Painter's partition / book allocation?
     *    → Same problem with different skin. Always recognize the pattern:
     *      "Minimize the maximum" → binary search on answer.
     *
     * 4. What if the array has negative numbers?
     *    → Binary search still works, but canSplit logic needs adjustment.
     *    → The predicate may lose monotonicity in certain edge cases.
     */

    // =========================================================
    // G5. CANDY
    // Topics: Two-Pass Greedy + Constraint Propagation
    // =========================================================
    /**
     * Problem: n children in a line, each with a rating. Assign candy:
     *   - Every child gets at least 1 candy.
     *   - Higher-rated child gets more candy than each neighbor.
     * Minimize total candy.
     * (LeetCode 135)
     *
     * Key insight — Two-pass greedy:
     *   Pass 1 (left → right): Enforce left neighbor constraint.
     *     If ratings[i] > ratings[i-1] → candy[i] = candy[i-1] + 1
     *   Pass 2 (right → left): Enforce right neighbor constraint.
     *     If ratings[i] > ratings[i+1] → candy[i] = max(candy[i], candy[i+1] + 1)
     *
     * Why two passes?
     *   A single left→right pass handles the increasing sequences.
     *   But it misses the constraint from the right neighbor.
     *   The second pass (right→left) fixes this, and max() ensures
     *   we don't violate the constraint already set by the first pass.
     *
     * Proof of minimality:
     *   Each child gets the minimum candy satisfying BOTH constraints.
     *   candy[i] = max(left_constraint, right_constraint) is the tightest possible.
     *
     * Time:  O(n) — two passes
     * Space: O(n) — candy array
     *
     * Topics tested: Greedy reasoning, bidirectional constraint propagation.
     */
    public int candy(int[] ratings) {
        int n = ratings.length;
        int[] candy = new int[n];
        Arrays.fill(candy, 1);

        // Pass 1: Left → Right (enforce left neighbor)
        for (int i = 1; i < n; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candy[i] = candy[i - 1] + 1;
            }
        }

        // Pass 2: Right → Left (enforce right neighbor)
        for (int i = n - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candy[i] = Math.max(candy[i], candy[i + 1] + 1);
            }
        }

        int total = 0;
        for (int c : candy) total += c;
        return total;
    }

    /**
     * Google Follow-ups:
     *
     * 1. O(1) space solution?
     *    → Track ascending and descending slopes. Complex but possible.
     *    → Count ups, downs, and peaks to calculate candy without array.
     *
     * 2. What if the constraint is ≥ (not strictly >)?
     *    → Change condition to ratings[i] >= ratings[i-1].
     *    → Equal ratings don't require more candy.
     *
     * 3. Circular arrangement (first and last are neighbors)?
     *    → Run the algorithm, then check and fix the wrap-around constraint.
     *    → May require an additional pass.
     *
     * 4. Real-world application?
     *    → Performance review bonus allocation, seating optimization.
     *    → Same constraint propagation pattern appears in scheduling.
     */

    // =========================================================
    // G6. MINIMUM INCREMENTS ON SUBARRAYS TO FORM TARGET
    // Topics: Greedy + Difference Reasoning + Math
    // =========================================================
    /**
     * Problem: Starting from an all-zero array, each operation picks a subarray
     * and increments all elements by 1. Find the min operations to reach
     * target array. (LeetCode 1526)
     *
     * Key insight — Think about DIFFERENCES:
     *   Each "step up" from target[i-1] to target[i] (when target[i] > target[i-1])
     *   requires a NEW operation to begin covering position i.
     *
     *   When target[i] ≤ target[i-1], the operations covering i-1 can be
     *   "extended" to cover i (since those operations were already ≥ target[i]).
     *
     *   So: answer = target[0] + sum of max(0, target[i] - target[i-1]) for i ≥ 1
     *
     * Alternative view: Think of the target array as a histogram.
     *   Each horizontal "layer" is one operation (a subarray).
     *   The number of layers at position i is target[i].
     *   New layers start whenever target[i] > target[i-1].
     *
     * Proof:
     *   - Lower bound: We need at least target[i] operations at each position i.
     *     But operations are subarrays — they can cover multiple positions.
     *   - The "staircase" argument: Each increase is a new staircase step that
     *     requires a new operation. Decreases are free (stop existing operations).
     *   - Sum of increases = sum of new operations started = minimum operations.
     *
     * Time:  O(n) — single pass
     * Space: O(1)
     *
     * Topics tested: Difference reasoning, greedy proof, thinking about operations
     *                as geometric layers.
     */
    public int minNumberOperations(int[] target) {
        int ops = target[0];  // first element always requires target[0] operations
        for (int i = 1; i < target.length; i++) {
            if (target[i] > target[i - 1]) {
                ops += target[i] - target[i - 1];  // new "steps up"
            }
        }
        return ops;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if operations can also DECREMENT subarrays?
     *    → Then minimum operations = sum of |target[i] - target[i-1]| for all i.
     *    → (Plus target[0] for the initial step from 0.)
     *
     * 2. What if operations have a COST proportional to subarray length?
     *    → Different problem entirely. Greedy doesn't apply.
     *    → DP: dp[i] = min cost to build target[0..i].
     *
     * 3. Generalize: starting from arbitrary array (not all zeros)?
     *    → Compute diff = target[i] - start[i]. Problem becomes:
     *      Minimum operations to achieve the diff array.
     *    → Handle negative diffs as decrements (separate set of ops).
     *
     * 4. Can you relate this to "painting a fence" problems?
     *    → Yes — each layer is a "paint stroke." Same greedy principle:
     *      count new strokes started (increases from left to right).
     */
}
