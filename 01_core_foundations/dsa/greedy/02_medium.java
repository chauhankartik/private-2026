/**
 * ============================================================
 *  GREEDY ALGORITHMS — MEDIUM PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   M1. Gas Station (LeetCode 134 / InterviewBit)
 *   M2. Jump Game I (LeetCode 55)
 *   M3. Jump Game II (LeetCode 45)
 *   M4. Non-overlapping Intervals (LeetCode 435)
 *   M5. Task Scheduler (LeetCode 621)
 *   M6. Partition Labels (LeetCode 763)
 *   M7. Bag of Tokens (LeetCode 948)
 *   M8. Minimum Number of Arrows to Burst Balloons (LeetCode 452)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Medium {

    // =========================================================
    // M1. GAS STATION
    // Pattern: Single Pass Greedy Range Elimination
    // LeetCode: 134 / InterviewBit
    // =========================================================
    /**
     * Problem: Given gas array A and cost array B, find minimum starting station
     * index to complete circular circuit once, else return -1.
     *
     * Brute Force O(n²): Try starting from every station i and simulate loop.
     *
     * Optimal O(n): Single pass.
     * 1. If sum(gas) < sum(cost), impossible (return -1).
     * 2. Track currentTank = gas[i] - cost[i]. If currentTank < 0, candidate start = i + 1.
     *
     * Proof: If start S cannot reach K, no station between S and K can reach K either.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int totalGas = 0, totalCost = 0;
        int currentTank = 0, startIndex = 0;

        for (int i = 0; i < gas.length; i++) {
            totalGas += gas[i];
            totalCost += cost[i];
            currentTank += gas[i] - cost[i];

            if (currentTank < 0) {
                startIndex = i + 1;
                currentTank = 0;
            }
        }
        return (totalGas >= totalCost) ? startIndex : -1;
    }

    // =========================================================
    // M2. JUMP GAME I
    // Pattern: Max Reachable Boundary Tracking
    // LeetCode: 55
    // =========================================================
    /**
     * Problem: Given non-negative array nums where nums[i] is max jump length,
     * determine if you can reach the last index.
     *
     * Optimal O(n): Maintain maxReach. Iterate i from 0 to n-1:
     * If i > maxReach, return false.
     * maxReach = max(maxReach, i + nums[i]).
     *
     * Time: O(n)
     * Space: O(1)
     */
    public boolean canJump(int[] nums) {
        int maxReach = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > maxReach) return false;
            maxReach = Math.max(maxReach, i + nums[i]);
            if (maxReach >= nums.length - 1) return true;
        }
        return true;
    }

    // =========================================================
    // M3. JUMP GAME II
    // Pattern: BFS / Greedy Level Extension
    // LeetCode: 45
    // =========================================================
    /**
     * Problem: Return minimum number of jumps to reach last index.
     *
     * Optimal O(n): Maintain curEnd and maxReach. When i reaches curEnd, jump++
     * and set curEnd = maxReach.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int jump(int[] nums) {
        if (nums.length <= 1) return 0;
        int jumps = 0, curEnd = 0, maxReach = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            maxReach = Math.max(maxReach, i + nums[i]);
            if (i == curEnd) {
                jumps++;
                curEnd = maxReach;
                if (curEnd >= nums.length - 1) break;
            }
        }
        return jumps;
    }

    // =========================================================
    // M4. NON-OVERLAPPING INTERVALS
    // Pattern: Interval Scheduling (Sort by End Time)
    // LeetCode: 435
    // =========================================================
    /**
     * Problem: Find min number of intervals to remove to make rest non-overlapping.
     *
     * Optimal O(n log n): Sort intervals by end time. Pick earliest ending interval.
     * Count removed intervals.
     *
     * Time: O(n log n)
     * Space: O(1) or O(log n) sorting space
     */
    public int eraseOverlapIntervals(int[][] intervals) {
        if (intervals.length == 0) return 0;
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));

        int count = 0;
        int prevEnd = intervals[0][1];

        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] < prevEnd) {
                count++; // Overlap -> remove current interval (greedily keep interval with smaller end time)
            } else {
                prevEnd = intervals[i][1];
            }
        }
        return count;
    }

    // =========================================================
    // M5. TASK SCHEDULER
    // Pattern: Frequency Counting & Formula / Heap Greedy
    // LeetCode: 621
    // =========================================================
    /**
     * Problem: Minimum CPU intervals to finish tasks with cooling period n.
     *
     * Optimal O(N): Count task frequencies. Identify max Frequency maxFreq.
     * Max slots = (maxFreq - 1) * (n + 1) + count(tasks with maxFreq).
     * Result = max(tasks.length, Max slots).
     *
     * Time: O(N) where N is total tasks
     * Space: O(1) — fixed array of size 26
     */
    public int leastInterval(char[] tasks, int n) {
        int[] freq = new int[26];
        int maxFreq = 0;
        for (char c : tasks) {
            freq[c - 'A']++;
            maxFreq = Math.max(maxFreq, freq[c - 'A']);
        }
        int maxFreqCount = 0;
        for (int f : freq) {
            if (f == maxFreq) maxFreqCount++;
        }
        int partCount = maxFreq - 1;
        int emptySlots = partCount * (n - (maxFreqCount - 1));
        int availableTasks = tasks.length - maxFreq * maxFreqCount;
        int idles = Math.max(0, emptySlots - availableTasks);

        return tasks.length + idles;
    }

    // =========================================================
    // M6. PARTITION LABELS
    // Pattern: Last Occurrence Index Boundary Tracking
    // LeetCode: 763
    // =========================================================
    /**
     * Problem: Partition string into max parts such that each letter appears in at most one part.
     *
     * Optimal O(n):
     * 1. Record last index of each character.
     * 2. Expand current partition end to max(last[s[i]]).
     * 3. When i == end, cut partition.
     *
     * Time: O(n)
     * Space: O(1) — 26 character last index array
     */
    public List<Integer> partitionLabels(String s) {
        int[] last = new int[26];
        for (int i = 0; i < s.length(); i++) {
            last[s.charAt(i) - 'a'] = i;
        }

        List<Integer> result = new ArrayList<>();
        int start = 0, end = 0;
        for (int i = 0; i < s.length(); i++) {
            end = Math.max(end, last[s.charAt(i) - 'a']);
            if (i == end) {
                result.add(end - start + 1);
                start = i + 1;
            }
        }
        return result;
    }

    // =========================================================
    // M7. BAG OF TOKENS
    // Pattern: Two-Pointer Greedy (Buy Low, Sell High)
    // LeetCode: 948
    // =========================================================
    /**
     * Problem: Maximize score using tokens.
     * - Face up: Lose tokens[i] power, gain 1 score. (Use smallest token)
     * - Face down: Gain tokens[i] power, lose 1 score. (Use largest token)
     *
     * Optimal O(n log n): Sort tokens. Two pointers (left for face up, right for face down).
     *
     * Time: O(n log n)
     * Space: O(1)
     */
    public int bagOfTokensScore(int[] tokens, int power) {
        Arrays.sort(tokens);
        int left = 0, right = tokens.length - 1;
        int score = 0, maxScore = 0;

        while (left <= right) {
            if (power >= tokens[left]) {
                power -= tokens[left++];
                score++;
                maxScore = Math.max(maxScore, score);
            } else if (score > 0) {
                power += tokens[right--];
                score--;
            } else {
                break;
            }
        }
        return maxScore;
    }

    // =========================================================
    // M8. MINIMUM ARROWS TO BURST BALLOONS
    // Pattern: Interval Overlap Count
    // LeetCode: 452
    // =========================================================
    /**
     * Problem: Burst all balloons using min number of vertical arrows.
     *
     * Optimal O(n log n): Sort by end coordinate. Shoot arrow at end of first balloon.
     *
     * Time: O(n log n)
     * Space: O(1)
     */
    public int findMinArrowShots(int[][] points) {
        if (points.length == 0) return 0;
        // Use Integer.compare to avoid integer overflow underflows
        Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));

        int arrows = 1;
        int prevEnd = points[0][1];

        for (int i = 1; i < points.length; i++) {
            if (points[i][0] > prevEnd) {
                arrows++;
                prevEnd = points[i][1];
            }
        }
        return arrows;
    }
}
