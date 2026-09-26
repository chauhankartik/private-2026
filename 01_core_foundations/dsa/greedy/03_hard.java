/**
 * ============================================================
 *  GREEDY ALGORITHMS — HARD PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   H1. Candy (LeetCode 135 / InterviewBit)
 *   H2. Create Maximum Number (LeetCode 321)
 *   H3. Remove K Digits (LeetCode 402)
 *   H4. Minimum Replacement to Sort Array (LeetCode 2366)
 *   H5. Course Schedule III (LeetCode 630)
 *   H6. Maximum Performance of a Team (LeetCode 1383)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Hard {

    // =========================================================
    // H1. CANDY
    // Pattern: Two-Pass / Bidirectional Greedy
    // LeetCode: 135 / InterviewBit
    // =========================================================
    /**
     * Problem: N children in line with ratings. Give candies such that:
     * 1. Each child gets at least 1 candy.
     * 2. Children with higher rating than neighbor get more candies than neighbor.
     * Return minimum total candies.
     *
     * Brute Force O(n²): Iteratively update candies until convergence.
     *
     * Optimal O(n): Two Passes.
     * - Pass 1 (Left to Right): If ratings[i] > ratings[i-1], candies[i] = candies[i-1] + 1.
     * - Pass 2 (Right to Left): If ratings[i] > ratings[i+1], candies[i] = max(candies[i], candies[i+1] + 1).
     *
     * Time: O(n)
     * Space: O(n)
     */
    public int candy(int[] ratings) {
        int n = ratings.length;
        int[] candies = new int[n];
        Arrays.fill(candies, 1);

        for (int i = 1; i < n; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candies[i] = candies[i - 1] + 1;
            }
        }

        for (int i = n - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candies[i] = Math.max(candies[i], candies[i + 1] + 1);
            }
        }

        int total = 0;
        for (int c : candies) total += c;
        return total;
    }

    // =========================================================
    // H2. CREATE MAXIMUM NUMBER
    // Pattern: Monotonic Stack + Merge Subproblems
    // LeetCode: 321
    // =========================================================
    /**
     * Problem: Create max number of length k from two arrays preserving relative order.
     *
     * Optimal O(k · (n + m)²):
     * 1. Try taking i digits from nums1 and (k - i) digits from nums2.
     * 2. Use monotonic stack to extract max subsequence of given length.
     * 3. Merge two max subsequences greedily.
     * 4. Return maximum merged array.
     *
     * Time: O(k · (n + m)²)
     * Space: O(k)
     */
    public int[] maxNumber(int[] nums1, int[] nums2, int k) {
        int n = nums1.length, m = nums2.length;
        int[] best = new int[k];

        for (int i = Math.max(0, k - m); i <= Math.min(k, n); i++) {
            int[] seq1 = maxSubsequence(nums1, i);
            int[] seq2 = maxSubsequence(nums2, k - i);
            int[] merged = merge(seq1, seq2);
            if (isGreater(merged, 0, best, 0)) {
                best = merged;
            }
        }
        return best;
    }

    private int[] maxSubsequence(int[] nums, int k) {
        int[] stack = new int[k];
        int top = -1;
        int drop = nums.length - k;
        for (int num : nums) {
            while (top >= 0 && stack[top] < num && drop > 0) {
                top--;
                drop--;
            }
            if (top + 1 < k) {
                stack[++top] = num;
            } else {
                drop--;
            }
        }
        return stack;
    }

    private int[] merge(int[] nums1, int[] nums2) {
        int len = nums1.length + nums2.length;
        int[] result = new int[len];
        int i = 0, j = 0;
        for (int r = 0; r < len; r++) {
            if (isGreater(nums1, i, nums2, j)) {
                result[r] = nums1[i++];
            } else {
                result[r] = nums2[j++];
            }
        }
        return result;
    }

    private boolean isGreater(int[] nums1, int i, int[] nums2, int j) {
        while (i < nums1.length && j < nums2.length) {
            if (nums1[i] != nums2[j]) {
                return nums1[i] > nums2[j];
            }
            i++;
            j++;
        }
        return i < nums1.length;
    }

    // =========================================================
    // H3. REMOVE K DIGITS
    // Pattern: Monotonic Increasing Stack Greedy
    // LeetCode: 402
    // =========================================================
    /**
     * Problem: Remove k digits from non-negative number num to get smallest number.
     *
     * Optimal O(n): Monotonic Stack. If stack top > current digit and k > 0, pop top.
     *
     * Time: O(n)
     * Space: O(n)
     */
    public String removeKdigits(String num, int k) {
        if (k >= num.length()) return "0";

        StringBuilder stack = new StringBuilder();
        for (char ch : num.toCharArray()) {
            while (stack.length() > 0 && stack.charAt(stack.length() - 1) > ch && k > 0) {
                stack.deleteCharAt(stack.length() - 1);
                k--;
            }
            stack.append(ch);
        }

        // Trim remaining k digits from end if any
        while (k > 0 && stack.length() > 0) {
            stack.deleteCharAt(stack.length() - 1);
            k--;
        }

        // Remove leading zeros
        int start = 0;
        while (start < stack.length() && stack.charAt(start) == '0') {
            start++;
        }

        String result = stack.substring(start);
        return result.isEmpty() ? "0" : result;
    }

    // =========================================================
    // H4. MINIMUM REPLACEMENT TO SORT ARRAY
    // Pattern: Reverse Pass Splitting Greedy
    // LeetCode: 2366
    // =========================================================
    /**
     * Problem: Replace elements with sums equal to element to make array non-decreasing.
     * Minimize total replacement operations.
     *
     * Optimal O(n): Traverse right to left. Maintain last (maximum allowed boundary).
     * If nums[i] > last, divide into numElements = (nums[i] + last - 1) / last parts.
     * Operations += numElements - 1. Set last = nums[i] / numElements.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public long minimumReplacement(int[] nums) {
        long ops = 0;
        int n = nums.length;
        long last = nums[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            if (nums[i] > last) {
                long numElements = (nums[i] + last - 1) / last;
                ops += numElements - 1;
                last = nums[i] / numElements;
            } else {
                last = nums[i];
            }
        }
        return ops;
    }

    // =========================================================
    // H5. COURSE SCHEDULE III
    // Pattern: PriorityQueue Max-Heap Replacement Greedy
    // LeetCode: 630
    // =========================================================
    /**
     * Problem: Given courses [duration, lastDay], return max number of courses you can take.
     *
     * Optimal O(n log n): Sort courses by lastDay. Iterate and add to Max-Heap of durations.
     * If total time > current course lastDay, evict max duration course from heap.
     *
     * Time: O(n log n)
     * Space: O(n)
     */
    public int scheduleCourse(int[][] courses) {
        Arrays.sort(courses, (a, b) -> Integer.compare(a[1], b[1]));
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));

        int totalTime = 0;
        for (int[] course : courses) {
            int duration = course[0];
            int lastDay = course[1];

            totalTime += duration;
            maxHeap.offer(duration);

            if (totalTime > lastDay) {
                totalTime -= maxHeap.poll(); // Evict longest duration course taken so far
            }
        }
        return maxHeap.size();
    }

    // =========================================================
    // H6. MAXIMUM PERFORMANCE OF A TEAM
    // Pattern: Fixed Efficiency + Max-Heap Speed Priority
    // LeetCode: 1383
    // =========================================================
    /**
     * Problem: Select at most k engineers to maximize performance = sum(speed) * min(efficiency).
     * Return result modulo 10^9 + 7.
     *
     * Optimal O(n log n): Sort engineers descending by efficiency.
     * Iterate and maintain Min-Heap of top k speeds.
     *
     * Time: O(n log n)
     * Space: O(n)
     */
    public int maxPerformance(int n, int[] speed, int[] efficiency, int k) {
        int[][] engineers = new int[n][2];
        for (int i = 0; i < n; i++) {
            engineers[i][0] = efficiency[i];
            engineers[i][1] = speed[i];
        }
        Arrays.sort(engineers, (a, b) -> Integer.compare(b[0], a[0]));

        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        long speedSum = 0;
        long maxPerf = 0;

        for (int[] eng : engineers) {
            int eff = eng[0];
            int spd = eng[1];

            minHeap.offer(spd);
            speedSum += spd;

            if (minHeap.size() > k) {
                speedSum -= minHeap.poll();
            }

            maxPerf = Math.max(maxPerf, speedSum * eff);
        }

        return (int) (maxPerf % 1_000_000_007);
    }
}
