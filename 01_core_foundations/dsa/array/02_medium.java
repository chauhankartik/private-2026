/**
 * ============================================================
 *  ARRAYS — MEDIUM PROBLEMS
 *  These problems combine arrays with sorting, two pointers,
 *  or require non-obvious transformations.
 * ============================================================
 *
 *  Problems:
 *   M1. 3Sum
 *   M2. Product of Array Except Self
 *   M3. Container With Most Water
 *   M4. Rotate Array
 *   M5. Next Permutation
 *   M6. Sort Colors (Dutch National Flag)
 *   M7. Merge Intervals
 *   M8. Spiral Matrix
 *
 * ============================================================
 */
import java.util.*;

public class Medium {

    // =========================================================
    // M1. 3SUM
    // Pattern: Sort + Two Pointers (reduce to Two Sum)
    // =========================================================
    /**
     * Problem: Find all unique triplets [a, b, c] where a + b + c = 0.
     *
     * Brute Force O(n³): Three nested loops. Check all triplets.
     *
     * Optimal O(n²): Sort the array. For each nums[i], find pairs
     * (j, k) in the remaining sorted subarray such that
     * nums[j] + nums[k] == -nums[i] using two pointers.
     *
     * Key insight: Sorting enables:
     *   1. Two-pointer technique (O(n) per fixed element)
     *   2. Easy duplicate skipping (adjacent equal elements)
     *
     * Deduplication logic:
     *   - Skip nums[i] if nums[i] == nums[i-1] (same fixed element)
     *   - After finding a triplet, skip left++ while nums[left] == nums[left-1]
     *   - Same for right--
     *
     * Time:  O(n²) — O(n log n) sort + O(n) per element × n elements
     * Space: O(1)  — excluding output (sort is in-place)
     */
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);

        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;  // skip duplicate i
            if (nums[i] > 0) break;  // optimization: smallest > 0 → no triplet possible

            int left = i + 1, right = nums.length - 1;
            int target = -nums[i];

            while (left < right) {
                int sum = nums[left] + nums[right];
                if (sum == target) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    while (left < right && nums[left] == nums[left + 1]) left++;   // skip dup
                    while (left < right && nums[right] == nums[right - 1]) right--; // skip dup
                    left++; right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }

    /**
     * Follow-up 1: 4Sum? (LeetCode 18)
     *   → Fix two elements, two-pointer on remaining. O(n³).
     *   → Generalized: kSum via recursion reducing to 2Sum base case.
     *
     * Follow-up 2: 3Sum closest to target? (LeetCode 16)
     *   → Same structure, track closest = min |sum - target|.
     *
     * Follow-up 3: Count triplets instead of returning them?
     *   → Same approach, but add (right - left) on match instead of individual pairs.
     */

    // =========================================================
    // M2. PRODUCT OF ARRAY EXCEPT SELF
    // Pattern: Prefix Product + Suffix Product
    // =========================================================
    /**
     * Problem: Return array where output[i] = product of all elements
     * except nums[i]. WITHOUT using division. O(n) time.
     *
     * Why not use total product / nums[i]?
     *   - Division is explicitly forbidden by the problem.
     *   - Also breaks when nums[i] == 0.
     *
     * Key Insight: output[i] = (product of all LEFT of i) × (product of all RIGHT of i)
     *   = prefix_product[0..i-1] × suffix_product[i+1..n-1]
     *
     * Two-pass approach:
     *   Pass 1 (left → right): Build prefix products into output.
     *   Pass 2 (right → left): Multiply suffix products into output.
     *
     * Time:  O(n) — two passes
     * Space: O(1) — output array doesn't count; we use a running variable for suffix
     */
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] output = new int[n];

        // Pass 1: output[i] = product of all elements to the LEFT of i
        output[0] = 1;
        for (int i = 1; i < n; i++) {
            output[i] = output[i - 1] * nums[i - 1];
        }

        // Pass 2: multiply by product of all elements to the RIGHT of i
        int rightProduct = 1;
        for (int i = n - 2; i >= 0; i--) {
            rightProduct *= nums[i + 1];
            output[i] *= rightProduct;
        }
        return output;
    }

    /**
     * Follow-up 1: What if zeros exist?
     *   → Algorithm handles it naturally! No division involved.
     *   → With division approach: count zeros. If >1 zero, all outputs are 0.
     *     If exactly 1 zero at index k, only output[k] = product of non-zeros.
     *
     * Follow-up 2: Product of array except self, but return indices of max element?
     *   → Build product array, then linear scan for max.
     *
     * Follow-up 3: Can you do this with prefix XOR instead?
     *   → XOR is its own inverse: xor_except_self[i] = total_xor ^ nums[i]
     *   → That's O(n) with O(1) space — even simpler than product version.
     */

    // =========================================================
    // M3. CONTAINER WITH MOST WATER
    // Pattern: Two Pointers (greedy shrink from outside)
    // =========================================================
    /**
     * Problem: Given heights[0..n-1], find two lines that form a container
     * holding the most water. Area = min(h[i], h[j]) × (j - i).
     *
     * Brute Force O(n²): Try all pairs (i, j).
     *
     * Optimal O(n): Start with widest container (left=0, right=n-1).
     *   Move the pointer with the SHORTER height inward.
     *
     * Proof of correctness (why this works):
     *   Suppose h[left] < h[right]. Any container with left as one side
     *   and any r < right as the other side has:
     *     - width = r - left < right - left (smaller)
     *     - height ≤ h[left] (bounded by shorter side)
     *   → Area ≤ h[left] × (right - left) = current area
     *   → We will NEVER find a better container using left. Safe to skip.
     *   → Move left++.
     *
     * Time:  O(n) — each pointer moves at most n times total
     * Space: O(1)
     */
    public int maxArea(int[] height) {
        int left = 0, right = height.length - 1;
        int maxWater = 0;
        while (left < right) {
            int h = Math.min(height[left], height[right]);
            maxWater = Math.max(maxWater, h * (right - left));
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxWater;
    }

    /**
     * Follow-up 1: What if heights can be negative? (Water can't have negative height)
     *   → Take max(0, h) for each height. Same algorithm.
     *
     * Follow-up 2: This vs Trapping Rain Water?
     *   → Container: choose TWO lines, ignore lines between them.
     *   → Trapping: ALL bars matter (water fills between them). Different problem!
     *   → See Hard H1 for trapping rain water.
     *
     * Follow-up 3: Volume of water in 3D? (LeetCode 407)
     *   → BFS/priority queue from borders inward. Much harder.
     */

    // =========================================================
    // M4. ROTATE ARRAY
    // Pattern: Reverse Trick (three reverses)
    // =========================================================
    /**
     * Problem: Rotate array right by k positions.
     * [1,2,3,4,5,6,7] with k=3 → [5,6,7,1,2,3,4]
     *
     * Approach A — Extra array: Copy to temp. O(n) time, O(n) space.
     *
     * Approach B — Reverse trick: O(n) time, O(1) space!
     *   Step 1: Reverse entire array:     [7,6,5,4,3,2,1]
     *   Step 2: Reverse first k elements: [5,6,7,4,3,2,1]
     *   Step 3: Reverse remaining n-k:    [5,6,7,1,2,3,4] ✓
     *
     * Why does this work?
     *   After full reverse, the last k elements are now first (but reversed).
     *   Reversing each half puts them in correct order.
     *   This is a well-known technique used in string rotation too.
     *
     * Time:  O(n) — three passes, each ≤ n
     * Space: O(1) — in-place swaps
     */
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        k %= n;  // handle k > n
        reverse(nums, 0, n - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, n - 1);
    }

    private void reverse(int[] arr, int left, int right) {
        while (left < right) {
            int temp = arr[left];
            arr[left++] = arr[right];
            arr[right--] = temp;
        }
    }

    /**
     * Follow-up 1: Rotate LEFT by k?
     *   → rotate(nums, n - k) — or reverse the three segments differently.
     *
     * Follow-up 2: Cyclic replacement approach?
     *   → Place each element at position (i + k) % n. Track start, count.
     *   → GCD(n, k) cycles, each of length n/gcd(n,k). O(n) time, O(1) space.
     *
     * Follow-up 3: Rotate a 2D matrix by 90 degrees? (LeetCode 48)
     *   → Transpose + reverse each row. O(n²) time, O(1) space.
     */

    // =========================================================
    // M5. NEXT PERMUTATION
    // Pattern: Lexicographic Algorithm (find, swap, reverse)
    // =========================================================
    /**
     * Problem: Rearrange numbers into the next lexicographically
     * greater permutation. If already largest, wrap to smallest.
     *
     * Algorithm (Narayana Pandita's formula, 14th century):
     *   1. FIND: Scan right-to-left for first index i where nums[i] < nums[i+1].
     *      (This is the "pivot" — the rightmost element that can be increased.)
     *   2. SWAP: Find rightmost j > i where nums[j] > nums[i]. Swap them.
     *   3. REVERSE: Reverse the suffix after position i.
     *
     * Why does this produce the NEXT permutation?
     *   - Step 1 finds the rightmost "ascent" — everything to the right is descending
     *     (already the largest permutation of that suffix).
     *   - Step 2 replaces nums[i] with the smallest element that's still larger.
     *   - Step 3 makes the suffix the smallest possible (ascending order).
     *
     * Example: [1, 3, 5, 4, 2]
     *   Step 1: i=1 (nums[1]=3 < nums[2]=5)
     *   Step 2: j=3 (nums[3]=4 > nums[1]=3, rightmost). Swap → [1, 4, 5, 3, 2]
     *   Step 3: Reverse [5,3,2] → [2,3,5]. Result: [1, 4, 2, 3, 5] ✓
     *
     * Time:  O(n) — three linear scans + reverse
     * Space: O(1) — in-place
     */
    public void nextPermutation(int[] nums) {
        int n = nums.length;

        // Step 1: Find pivot
        int i = n - 2;
        while (i >= 0 && nums[i] >= nums[i + 1]) i--;

        if (i >= 0) {
            // Step 2: Find swap partner
            int j = n - 1;
            while (nums[j] <= nums[i]) j--;
            // Swap
            int temp = nums[i]; nums[i] = nums[j]; nums[j] = temp;
        }

        // Step 3: Reverse suffix
        reverse(nums, i + 1, n - 1);
    }

    /**
     * Follow-up 1: Previous permutation?
     *   → Mirror: Find rightmost descent (nums[i] > nums[i+1]).
     *   → Swap with rightmost smaller element. Reverse suffix.
     *
     * Follow-up 2: k-th permutation? (LeetCode 60)
     *   → Factorial number system. Build digit by digit.
     *   → O(n²) or O(n log n) with Fenwick tree.
     *
     * Follow-up 3: Generate ALL permutations? (LeetCode 46)
     *   → Backtracking with swap. Or repeatedly call nextPermutation.
     */

    // =========================================================
    // M6. SORT COLORS (DUTCH NATIONAL FLAG)
    // Pattern: Three-Way Partition (Dijkstra)
    // =========================================================
    /**
     * Problem: Sort array containing only 0, 1, 2 in ONE pass.
     * No counting sort (that's two passes).
     *
     * Algorithm — Dutch National Flag (Edsger Dijkstra):
     *   Three pointers: lo, mid, hi
     *   - [0, lo-1] = all 0s
     *   - [lo, mid-1] = all 1s
     *   - [mid, hi] = unexamined
     *   - [hi+1, n-1] = all 2s
     *
     *   Initialize: lo = 0, mid = 0, hi = n-1
     *   While mid <= hi:
     *     - nums[mid] == 0: swap(lo, mid), lo++, mid++
     *     - nums[mid] == 1: mid++
     *     - nums[mid] == 2: swap(mid, hi), hi-- (don't advance mid — swapped value is unknown)
     *
     * Time:  O(n) — single pass (each element examined at most twice)
     * Space: O(1) — in-place, three pointers
     *
     * Proof of O(n): Each iteration either advances mid or decrements hi.
     *   Total iterations ≤ 2n (mid goes right, hi goes left, they meet).
     */
    public void sortColors(int[] nums) {
        int lo = 0, mid = 0, hi = nums.length - 1;
        while (mid <= hi) {
            if (nums[mid] == 0) {
                swap(nums, lo++, mid++);
            } else if (nums[mid] == 1) {
                mid++;
            } else {  // nums[mid] == 2
                swap(nums, mid, hi--);
                // don't advance mid — need to examine swapped element
            }
        }
    }

    private void swap(int[] arr, int i, int j) {
        int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
    }

    /**
     * Follow-up 1: Sort with 4 colors? (k-way partition)
     *   → Generalize to k pointers. Or use counting sort (O(n + k)).
     *
     * Follow-up 2: Partition around a pivot (like quicksort)?
     *   → Same 3-way partition: elements < pivot, == pivot, > pivot.
     *   → This is exactly what Java's dual-pivot quicksort does internally.
     *
     * Follow-up 3: Wiggle sort — nums[0] < nums[1] > nums[2] < nums[3]...?
     *   → Find median (O(n) with quickselect), then 3-way partition and
     *     interleave. (LeetCode 324 — Hard)
     */

    // =========================================================
    // M7. MERGE INTERVALS
    // Pattern: Sort + Linear Sweep
    // =========================================================
    /**
     * Problem: Given intervals [[1,3],[2,6],[8,10],[15,18]],
     * merge overlapping intervals → [[1,6],[8,10],[15,18]].
     *
     * Algorithm:
     *   1. Sort intervals by start time.
     *   2. Sweep left-to-right: if current overlaps with last merged,
     *      extend the end. Otherwise, start a new interval.
     *
     * Overlap condition: intervals[i].start <= merged.last.end
     *
     * Why sort by start?
     *   If sorted by start, we only need to check overlap with the
     *   LAST merged interval (not all previous intervals).
     *   Without sorting, we'd need O(n²) pairwise comparisons.
     *
     * Time:  O(n log n) — dominated by sort
     * Space: O(n) — output (could also be O(log n) for sort stack)
     */
    public int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        List<int[]> merged = new ArrayList<>();

        for (int[] curr : intervals) {
            if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < curr[0]) {
                merged.add(curr);  // no overlap → new interval
            } else {
                // Overlap → extend end of last merged
                merged.get(merged.size() - 1)[1] =
                    Math.max(merged.get(merged.size() - 1)[1], curr[1]);
            }
        }
        return merged.toArray(new int[0][]);
    }

    /**
     * Follow-up 1: Insert a new interval into sorted non-overlapping intervals? (LeetCode 57)
     *   → Find insertion point, merge affected intervals, collect non-overlapping.
     *   → O(n) — no need to sort again.
     *
     * Follow-up 2: Count intervals that overlap with a query interval?
     *   → Sort by start, binary search for candidates.
     *   → Or use a sweep line algorithm.
     *
     * Follow-up 3: Meeting Rooms — min rooms needed? (LeetCode 253)
     *   → Sweep line: sort start/end events. Track active meetings.
     *   → Or: sort starts and ends separately, two-pointer sweep.
     */
    public int minMeetingRooms(int[][] intervals) {
        int n = intervals.length;
        int[] starts = new int[n], ends = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i] = intervals[i][1];
        }
        Arrays.sort(starts);
        Arrays.sort(ends);

        int rooms = 0, endPtr = 0;
        for (int i = 0; i < n; i++) {
            if (starts[i] < ends[endPtr]) {
                rooms++;
            } else {
                endPtr++;
            }
        }
        return rooms;
    }

    // =========================================================
    // M8. SPIRAL MATRIX
    // Pattern: Boundary Simulation (4 pointers)
    // =========================================================
    /**
     * Problem: Return all elements of an m×n matrix in spiral order.
     *
     * Algorithm — Layer-by-layer peeling:
     *   Maintain 4 boundaries: top, bottom, left, right.
     *   For each layer:
     *     1. Traverse right (top row)      → top++
     *     2. Traverse down (right column)  → right--
     *     3. Traverse left (bottom row)    → bottom--
     *     4. Traverse up (left column)     → left++
     *   Stop when boundaries cross.
     *
     * Time:  O(m × n) — visit each element exactly once
     * Space: O(1) — excluding output
     */
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix.length == 0) return result;

        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // → Traverse right along top row
            for (int j = left; j <= right; j++) result.add(matrix[top][j]);
            top++;

            // ↓ Traverse down along right column
            for (int i = top; i <= bottom; i++) result.add(matrix[i][right]);
            right--;

            // ← Traverse left along bottom row
            if (top <= bottom) {  // check if rows remain
                for (int j = right; j >= left; j--) result.add(matrix[bottom][j]);
                bottom--;
            }

            // ↑ Traverse up along left column
            if (left <= right) {  // check if columns remain
                for (int i = bottom; i >= top; i--) result.add(matrix[i][left]);
                left++;
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Generate spiral matrix (fill 1 to n²)? (LeetCode 59)
     *   → Same 4-pointer approach, but WRITE into the matrix instead of reading.
     *
     * Follow-up 2: Diagonal traversal? (LeetCode 498)
     *   → Diagonals have constant (i + j). Alternate traversal direction.
     *
     * Follow-up 3: Rotate matrix 90° clockwise in-place? (LeetCode 48)
     *   → Transpose + reverse each row. O(n²) time, O(1) space.
     */
    public void rotateMatrix(int[][] matrix) {
        int n = matrix.length;
        // Step 1: Transpose (swap matrix[i][j] with matrix[j][i])
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }
        // Step 2: Reverse each row
        for (int[] row : matrix) {
            int l = 0, r = n - 1;
            while (l < r) {
                int temp = row[l]; row[l++] = row[r]; row[r--] = temp;
            }
        }
    }
}
