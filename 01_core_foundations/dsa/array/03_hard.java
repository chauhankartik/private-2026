/**
 * ============================================================
 *  ARRAYS — HARD PROBLEMS
 *  Each problem requires combining arrays with advanced
 *  algorithmic thinking and multi-pattern reasoning.
 * ============================================================
 *
 *  Problems:
 *   H1. Trapping Rain Water
 *   H2. First Missing Positive
 *   H3. Sliding Window Maximum
 *   H4. Median of Two Sorted Arrays
 *   H5. Maximum Gap
 *   H6. Longest Increasing Subsequence
 *
 * ============================================================
 */
import java.util.*;

public class Hard {

    // =========================================================
    // H1. TRAPPING RAIN WATER
    // Pattern: Two Pointers / Prefix Max (multiple approaches)
    // =========================================================
    /**
     * Problem: Given elevation map height[0..n-1], compute how much
     * water it can trap after raining.
     *
     * Key insight: Water at position i =
     *   min(maxLeft[i], maxRight[i]) - height[i]
     *   where maxLeft[i] = max(height[0..i])
     *         maxRight[i] = max(height[i..n-1])
     *
     * ── Approach A: Prefix/Suffix arrays ──
     *   Build maxLeft[] and maxRight[] arrays. O(n) time, O(n) space.
     *
     * ── Approach B: Two Pointers ── (optimal)
     *   Track leftMax and rightMax while converging from both sides.
     *   - If leftMax < rightMax, water at `left` is determined by leftMax
     *     (the right side can't be the bottleneck since rightMax is higher).
     *   - Vice versa.
     *
     * Proof of two-pointer correctness:
     *   At any step, without loss of generality assume leftMax ≤ rightMax.
     *   For index `left`:
     *     - All bars to the left have max = leftMax
     *     - All bars to the right have max ≥ rightMax ≥ leftMax
     *     - So water[left] = leftMax - height[left] (clamped to 0)
     *   The right side is guaranteed to have sufficient height, so leftMax
     *   is the binding constraint. We don't need to know the actual maxRight
     *   for this index — just that rightMax ≥ leftMax suffices.
     *
     * Time:  O(n) — single pass with two pointers
     * Space: O(1) — only tracking two max values
     */
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                leftMax = Math.max(leftMax, height[left]);
                water += leftMax - height[left];  // leftMax ≥ height[left], so non-negative
                left++;
            } else {
                rightMax = Math.max(rightMax, height[right]);
                water += rightMax - height[right];
                right--;
            }
        }
        return water;
    }

    /** Approach A: Prefix/suffix max arrays — clearer logic, O(n) space */
    public int trapPrefixSuffix(int[] height) {
        int n = height.length;
        if (n == 0) return 0;

        int[] maxLeft = new int[n], maxRight = new int[n];
        maxLeft[0] = height[0];
        for (int i = 1; i < n; i++) maxLeft[i] = Math.max(maxLeft[i - 1], height[i]);
        maxRight[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) maxRight[i] = Math.max(maxRight[i + 1], height[i]);

        int water = 0;
        for (int i = 0; i < n; i++) {
            water += Math.min(maxLeft[i], maxRight[i]) - height[i];
        }
        return water;
    }

    /**
     * Follow-up 1: Monotonic stack approach?
     *   → Push indices. When height[i] > stack.peek(), pop and compute
     *     water between current, popped, and new peek (bounded rectangle).
     *   → Computes water "layer by layer" horizontally, not column by column.
     *   → O(n) time, O(n) space.
     *
     * Follow-up 2: 2D Trapping Rain Water? (LeetCode 407)
     *   → BFS with priority queue from borders inward.
     *   → Process lowest boundary cell first — if neighbor is lower, trap water.
     *   → O(mn log(mn)) time.
     *
     * Follow-up 3: What if the elevation map is circular?
     *   → Find the global maximum, split into two halves, solve each.
     */

    // =========================================================
    // H2. FIRST MISSING POSITIVE
    // Pattern: Cyclic Sort / In-Place Hashing
    // =========================================================
    /**
     * Problem: Find the smallest positive integer NOT in the array.
     * Must run in O(n) time and O(1) space.
     *
     * Why is this hard?
     *   - Can't sort (O(n log n))
     *   - Can't use HashSet (O(n) space)
     *   - Answer is in range [1, n+1] (pigeonhole principle)
     *
     * In-Place Hashing / Cyclic Sort:
     *   Put each number in its "correct" position: nums[i] should be i+1.
     *   - nums[0] = 1, nums[1] = 2, ..., nums[n-1] = n
     *   - After placement, scan for first mismatch: nums[i] ≠ i+1 → answer is i+1.
     *
     * Why O(n)?
     *   Each swap places at least one element in its correct position.
     *   At most n swaps total (each element moved at most once to correct spot).
     *   Combined with the n-iteration loop: O(2n) = O(n).
     *
     * Time:  O(n) — amortized O(1) per element
     * Space: O(1) — in-place rearrangement
     */
    public int firstMissingPositive(int[] nums) {
        int n = nums.length;

        // Phase 1: Place each nums[i] at index nums[i]-1
        for (int i = 0; i < n; i++) {
            while (nums[i] > 0 && nums[i] <= n && nums[nums[i] - 1] != nums[i]) {
                // Swap nums[i] with nums[nums[i]-1]
                int target = nums[i] - 1;
                int temp = nums[i];
                nums[i] = nums[target];
                nums[target] = temp;
            }
        }

        // Phase 2: First index where nums[i] != i+1 is the answer
        for (int i = 0; i < n; i++) {
            if (nums[i] != i + 1) return i + 1;
        }
        return n + 1; // array is [1, 2, ..., n] → answer is n+1
    }

    /**
     * Follow-up 1: Find all numbers disappeared in [1, n]? (LeetCode 448)
     *   → Same cyclic sort. After placement, indices where nums[i] ≠ i+1 are missing.
     *   → OR: negate nums[|nums[i]|-1] as a presence marker.
     *
     * Follow-up 2: Find all duplicates in [1, n]? (LeetCode 442)
     *   → Mark visited by negating. If already negative → duplicate found.
     *   → O(n) time, O(1) space — same in-place marking technique.
     *
     * Follow-up 3: Kth missing positive? (LeetCode 1539)
     *   → Binary search: at index i, number of missing = arr[i] - (i+1).
     *   → Find first index where missing count ≥ k. O(log n).
     */

    // =========================================================
    // H3. SLIDING WINDOW MAXIMUM
    // Pattern: Monotonic Deque
    // =========================================================
    /**
     * Problem: Given array and window size k, return max element in
     * each sliding window position.
     *
     * Brute Force O(nk): For each window, scan k elements for max.
     *
     * Optimal O(n): Use a MONOTONIC DEQUE (decreasing order).
     *   Deque stores INDICES. Front of deque = index of current max.
     *
     * Invariant: deque elements are in DECREASING order of their values.
     *   - Before adding nums[i], remove all deque elements smaller than nums[i]
     *     from the BACK (they can never be the max for any future window).
     *   - Remove front if it's outside the window (index ≤ i - k).
     *   - Window max = nums[deque.peekFirst()]
     *
     * Why O(n)?
     *   Each element is added to deque ONCE and removed at most ONCE.
     *   Total deque operations across all iterations = 2n. → O(n).
     *
     * Time:  O(n) — amortized O(1) per element
     * Space: O(k) — deque holds at most k elements
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        Deque<Integer> deque = new ArrayDeque<>();  // stores indices
        int[] result = new int[nums.length - k + 1];
        int idx = 0;

        for (int i = 0; i < nums.length; i++) {
            // Remove elements outside window from front
            while (!deque.isEmpty() && deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }
            // Remove smaller elements from back (they're useless)
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }
            deque.offerLast(i);

            // Window is complete once i >= k-1
            if (i >= k - 1) {
                result[idx++] = nums[deque.peekFirst()];
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Sliding window MINIMUM?
     *   → Same algorithm, change condition: remove elements LARGER than nums[i].
     *
     * Follow-up 2: Min of all sliding window maximums across all k?
     *   → Compute maxSlidingWindow for each k=1..n. Not practical — O(n²).
     *   → For specific k: one pass with deque is O(n).
     *
     * Follow-up 3: Sliding window median? (LeetCode 480)
     *   → Two heaps (max-heap for lower half, min-heap for upper half).
     *   → Lazy deletion with HashMap. O(n log k) time.
     */

    // =========================================================
    // H4. MEDIAN OF TWO SORTED ARRAYS
    // Pattern: Binary Search on Partition
    // =========================================================
    /**
     * Problem: Find median of two sorted arrays in O(log(m+n)) time.
     *
     * Key insight — Binary Search on partition:
     *   We need to partition both arrays such that:
     *     - Left half contains exactly (m+n+1)/2 elements total
     *     - max(leftHalf) ≤ min(rightHalf)
     *
     *   Binary search on the SHORTER array's partition point (say i).
     *   The other partition j = (m+n+1)/2 - i is determined.
     *
     * Partition visualization:
     *   nums1: [... a1 | b1 ...]    partition at i
     *   nums2: [... a2 | b2 ...]    partition at j = half - i
     *
     *   Valid partition: a1 ≤ b2 AND a2 ≤ b1
     *     - If a1 > b2: i is too large → shrink (move left)
     *     - If a2 > b1: i is too small → grow (move right)
     *
     *   Median:
     *     Odd total:  max(a1, a2)
     *     Even total: (max(a1, a2) + min(b1, b2)) / 2.0
     *
     * Time:  O(log(min(m, n))) — binary search on shorter array
     * Space: O(1)
     */
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Ensure nums1 is the shorter array
        if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);

        int m = nums1.length, n = nums2.length;
        int lo = 0, hi = m;
        int half = (m + n + 1) / 2;

        while (lo <= hi) {
            int i = lo + (hi - lo) / 2;  // partition in nums1
            int j = half - i;             // partition in nums2

            int left1  = (i == 0) ? Integer.MIN_VALUE : nums1[i - 1];
            int right1 = (i == m) ? Integer.MAX_VALUE : nums1[i];
            int left2  = (j == 0) ? Integer.MIN_VALUE : nums2[j - 1];
            int right2 = (j == n) ? Integer.MAX_VALUE : nums2[j];

            if (left1 <= right2 && left2 <= right1) {
                // Valid partition found
                if ((m + n) % 2 == 1) {
                    return Math.max(left1, left2);
                } else {
                    return (Math.max(left1, left2) + Math.min(right1, right2)) / 2.0;
                }
            } else if (left1 > right2) {
                hi = i - 1;  // too many from nums1 on left
            } else {
                lo = i + 1;  // too few from nums1 on left
            }
        }
        throw new IllegalArgumentException("Input arrays not sorted");
    }

    /**
     * Follow-up 1: Kth smallest element in two sorted arrays?
     *   → Same binary search approach with partition at k instead of half.
     *   → Or: recursive elimination of k/2 elements each step → O(log k).
     *
     * Follow-up 2: Median of k sorted arrays?
     *   → Binary search on the median value. For each candidate, count elements
     *     ≤ candidate across all arrays using binary search.
     *   → O(k log(max-min) log n) — depends on value range.
     *
     * Follow-up 3: Online median (stream)?
     *   → Two heaps: max-heap for lower half, min-heap for upper half.
     *   → O(log n) per insertion. (LeetCode 295)
     */

    // =========================================================
    // H5. MAXIMUM GAP
    // Pattern: Bucket / Pigeonhole Principle
    // =========================================================
    /**
     * Problem: Given an unsorted array, find the max difference between
     * successive elements in the sorted form. Must be O(n) time.
     *
     * Why not just sort? O(n log n) — but problem demands O(n).
     *
     * Key insight — Pigeonhole Principle:
     *   If n elements span range [min, max], then in sorted order,
     *   the average gap = (max - min) / (n - 1).
     *   The maximum gap ≥ average gap (pigeonhole principle).
     *
     *   Create (n-1) buckets of width = ceil((max - min) / (n - 1)).
     *   By pigeonhole, the max gap CANNOT occur between elements in the SAME bucket.
     *   → Max gap = max gap between consecutive buckets (max of bucket i, min of bucket i+1).
     *
     * Why only track min and max per bucket?
     *   Elements within the same bucket differ by at most `bucketWidth - 1 < average gap`.
     *   So the max gap must be between two different buckets: max[i] and min[next non-empty bucket].
     *
     * Time:  O(n) — one pass to fill buckets, one pass to compute gap
     * Space: O(n) — n-1 buckets
     */
    public int maximumGap(int[] nums) {
        int n = nums.length;
        if (n < 2) return 0;

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int num : nums) {
            min = Math.min(min, num);
            max = Math.max(max, num);
        }
        if (min == max) return 0;

        // Bucket setup
        int bucketSize = Math.max(1, (max - min) / (n - 1));
        int bucketCount = (max - min) / bucketSize + 1;

        int[] bucketMin = new int[bucketCount];
        int[] bucketMax = new int[bucketCount];
        Arrays.fill(bucketMin, Integer.MAX_VALUE);
        Arrays.fill(bucketMax, Integer.MIN_VALUE);

        for (int num : nums) {
            int idx = (num - min) / bucketSize;
            bucketMin[idx] = Math.min(bucketMin[idx], num);
            bucketMax[idx] = Math.max(bucketMax[idx], num);
        }

        // Max gap = max over gaps between consecutive non-empty buckets
        int maxGap = 0, prevMax = min;
        for (int i = 0; i < bucketCount; i++) {
            if (bucketMin[i] == Integer.MAX_VALUE) continue; // empty bucket
            maxGap = Math.max(maxGap, bucketMin[i] - prevMax);
            prevMax = bucketMax[i];
        }
        return maxGap;
    }

    /**
     * Follow-up 1: Minimum gap instead of maximum?
     *   → Within-bucket gaps matter now. Need to sort within each bucket.
     *   → O(n + k log k) if bucket sizes are small (nearly O(n) in practice).
     *
     * Follow-up 2: What about radix sort for O(n)?
     *   → Radix sort is O(n × w) where w = word size. Valid for bounded integers.
     *   → Bucket approach is more general and conceptually cleaner.
     *
     * Follow-up 3: Can we solve this with counting sort?
     *   → Only if range is small. For large range, counting sort = O(max-min) space.
     */

    // =========================================================
    // H6. LONGEST INCREASING SUBSEQUENCE
    // Pattern: DP + Binary Search (Patience Sorting)
    // =========================================================
    /**
     * Problem: Find the length of the longest strictly increasing subsequence.
     *
     * ── Approach A: DP O(n²) ──
     *   dp[i] = length of LIS ending at index i.
     *   dp[i] = max(dp[j] + 1) for all j < i where nums[j] < nums[i]
     *
     * ── Approach B: Binary Search O(n log n) — Patience Sorting ──
     *   Maintain array `tails` where tails[i] = smallest tail element of
     *   all increasing subsequences of length i+1.
     *
     *   Key property: `tails` is always SORTED.
     *
     *   For each nums[i]:
     *     - If nums[i] > all tails → extend LIS (append)
     *     - Otherwise → replace the first tail ≥ nums[i] (binary search)
     *       This gives a better (smaller) tail, potentially allowing longer sequences later.
     *
     * Why does this work?
     *   `tails[i]` represents the best possible ending value for a subsequence
     *   of length i+1. By keeping tails as small as possible, we maximize
     *   the chance that future elements can extend the subsequence.
     *
     * Time:  O(n log n) — n elements, O(log n) binary search each
     * Space: O(n) — tails array
     */
    public int lengthOfLIS(int[] nums) {
        List<Integer> tails = new ArrayList<>();

        for (int num : nums) {
            int pos = Collections.binarySearch(tails, num);
            if (pos < 0) pos = -(pos + 1);  // insertion point
            if (pos == tails.size()) {
                tails.add(num);   // extend: new longest subsequence
            } else {
                tails.set(pos, num); // replace: better (smaller) tail
            }
        }
        return tails.size();
    }

    /** DP approach O(n²) — simpler, useful when n ≤ 2500 */
    public int lengthOfLIS_DP(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int maxLen = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }
        return maxLen;
    }

    /**
     * Follow-up 1: Print the actual LIS (not just length)?
     *   → Track parent pointers during DP, or reconstruct from tails + index tracking.
     *
     * Follow-up 2: Number of longest increasing subsequences? (LeetCode 673)
     *   → DP with two arrays: dp[i] = LIS length, count[i] = number of LIS ending at i.
     *   → If dp[j]+1 == dp[i], add count[j] to count[i].
     *
     * Follow-up 3: Longest Increasing Subsequence in 2D (Russian Doll Envelopes)? (LeetCode 354)
     *   → Sort by width ASC, height DESC. Then LIS on heights.
     *   → The descending height sort prevents same-width envelopes from nesting.
     *   → O(n log n).
     */
}
