/**
 * ============================================================
 *  QUEUE — HARD PROBLEMS
 * ============================================================
 *
 *  H1.  Sliding Window Maximum                    LC 239
 *  H2.  Shortest Subarray with Sum at Least K     LC 862
 *  H3.  Jump Game VI                              LC 1696
 *  H4.  Word Ladder                               LC 127
 *  H5.  Constrained Subsequence Sum               LC 1425
 *  H6.  Shortest Path with Obstacles Elimination  LC 1293
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class QueueHard {

    // =========================================================
    // H1. Sliding Window Maximum  LC 239
    // Pattern: MONOTONIC DEQUE (decreasing)
    // =========================================================
    /**
     * Find the maximum in every contiguous window of size k.
     *
     * Brute: for each window, scan k elements for max — O(nk).
     *
     * Optimal: MONOTONIC DECREASING DEQUE.
     * Deque stores INDICES. Elements at those indices are in decreasing order.
     *
     * For each element:
     *   1. Remove expired indices from FRONT (index < i - k + 1).
     *   2. Remove smaller elements from BACK (they'll never be the max).
     *   3. Add current index to BACK.
     *   4. FRONT = index of current window's maximum.
     *
     * WHY REMOVE SMALLER FROM BACK:
     *   If nums[j] <= nums[i] and j < i, then nums[j] will NEVER be the
     *   window maximum while nums[i] is in the window. So discard j.
     *
     * WHY DEQUE AND NOT STACK:
     *   We remove from BOTH ends:
     *     - Front: expired elements (too old).
     *     - Back: dominated elements (too small).
     *
     * Time:  O(n) — each element pushed/popped at most once
     * Space: O(k) — deque holds at most k indices
     *
     * Follow-up: Sliding window MINIMUM?
     *   Same approach, monotonic INCREASING deque (remove larger from back).
     * Follow-up: Sliding window with dynamic k?
     *   Use a balanced BST (TreeMap) or two heaps for O(log n) per operation.
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] result = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();  // stores indices

        for (int i = 0; i < n; i++) {
            // 1. Remove expired indices from front
            while (!dq.isEmpty() && dq.peekFirst() < i - k + 1)
                dq.pollFirst();

            // 2. Remove dominated (smaller) elements from back
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i])
                dq.pollLast();

            // 3. Add current index
            dq.offerLast(i);

            // 4. Record result (once we have a full window)
            if (i >= k - 1)
                result[i - k + 1] = nums[dq.peekFirst()];
        }

        return result;
    }

    // =========================================================
    // H2. Shortest Subarray with Sum at Least K  LC 862
    // Pattern: MONOTONIC DEQUE + PREFIX SUMS
    // =========================================================
    /**
     * Find the shortest subarray with sum ≥ k. Array can have NEGATIVES.
     *
     * WHY NOT REGULAR SLIDING WINDOW:
     *   Sliding window assumes monotonic sum (all positives).
     *   Negatives break the monotonicity — shrinking may increase sum.
     *
     * Approach: prefix sums + monotonic increasing deque.
     *
     * prefix[i] = sum of nums[0..i-1]. prefix[0] = 0.
     * Subarray sum [j..i-1] = prefix[i] - prefix[j].
     * We want prefix[i] - prefix[j] ≥ k, i.e., prefix[j] ≤ prefix[i] - k.
     *
     * Monotonic INCREASING deque on prefix sums:
     *   For each i:
     *     1. While front of deque satisfies prefix[i] - prefix[front] ≥ k:
     *        Record length, remove front (any later i will give longer subarray).
     *     2. While back of deque has prefix[back] ≥ prefix[i]:
     *        Remove back (prefix[i] is smaller and later — strictly better).
     *     3. Add i to back.
     *
     * WHY REMOVE FROM FRONT WHEN SATISFIED:
     *   If prefix[i] - prefix[j] ≥ k, then for any i' > i,
     *   prefix[i'] - prefix[j] might also ≥ k but gives a LONGER subarray.
     *   So j is "used up" — remove it.
     *
     * WHY REMOVE LARGER FROM BACK:
     *   If prefix[j] ≥ prefix[i] and j < i, then:
     *   For any future i', prefix[i'] - prefix[j] ≤ prefix[i'] - prefix[i].
     *   And subarray [j+1..i'] is longer than [i+1..i'].
     *   So j is dominated by i — remove j.
     *
     * Time:  O(n) — each index pushed/popped at most once
     * Space: O(n) — prefix array + deque
     *
     * Follow-up: Shortest subarray with sum exactly k?
     *   Use a HashMap of prefix sums (like two-sum with indices).
     */
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + nums[i];

        Deque<Integer> dq = new ArrayDeque<>();
        int minLen = Integer.MAX_VALUE;

        for (int i = 0; i <= n; i++) {
            // 1. Check if current prefix satisfies condition with front
            while (!dq.isEmpty() && prefix[i] - prefix[dq.peekFirst()] >= k) {
                minLen = Math.min(minLen, i - dq.pollFirst());
            }

            // 2. Maintain increasing order — remove larger prefix sums from back
            while (!dq.isEmpty() && prefix[dq.peekLast()] >= prefix[i]) {
                dq.pollLast();
            }

            // 3. Add current index
            dq.offerLast(i);
        }

        return minLen == Integer.MAX_VALUE ? -1 : minLen;
    }

    // =========================================================
    // H3. Jump Game VI  LC 1696
    // Pattern: DP + MONOTONIC DEQUE
    // =========================================================
    /**
     * From index 0, jump at most k steps forward. Maximize score sum.
     *
     * dp[i] = max score to reach index i
     * dp[i] = nums[i] + max(dp[i-k], dp[i-k+1], ..., dp[i-1])
     *
     * Brute: O(nk) — check all k previous positions.
     * Optimal: monotonic deque to find max in sliding window of size k → O(n).
     *
     * This is exactly "sliding window maximum" applied to DP values.
     *
     * Deque maintains indices in DECREASING order of dp values.
     * For each i: dp[i] = nums[i] + dp[deque.front] (the maximum in window).
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Jump Game VII (LC 1871) — can/can't jump?
     *   BFS or sliding window with boolean DP.
     */
    public int maxResult(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        dp[0] = nums[0];
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerLast(0);

        for (int i = 1; i < n; i++) {
            // Remove expired indices
            while (!dq.isEmpty() && dq.peekFirst() < i - k)
                dq.pollFirst();

            // dp[i] = nums[i] + best reachable dp value
            dp[i] = nums[i] + dp[dq.peekFirst()];

            // Maintain decreasing order of dp values
            while (!dq.isEmpty() && dp[dq.peekLast()] <= dp[i])
                dq.pollLast();

            dq.offerLast(i);
        }

        return dp[n - 1];
    }

    // =========================================================
    // H4. Word Ladder  LC 127
    // Pattern: BFS — State-Space Search with HashSet
    // =========================================================
    /**
     * Transform beginWord → endWord, changing one letter at a time.
     * Each intermediate word must be in wordList.
     * Return minimum transformation steps.
     *
     * Model as a GRAPH:
     *   Nodes = words in wordList.
     *   Edges = words differing by exactly one character.
     *   BFS from beginWord to endWord.
     *
     * Neighbor generation:
     *   For each position, try all 26 characters.
     *   Check if the resulting word is in the wordSet.
     *   This is O(26 × L) per word = O(L) per word.
     *   Alternative: precompute adjacency with wildcards (h*t → hot, hat).
     *
     * Time:  O(M² × N) where M = word length, N = wordList size
     *        (M positions × 26 chars × M for string creation, N words max)
     * Space: O(M × N)
     *
     * Follow-up: Word Ladder II (LC 126) — return ALL shortest paths?
     *   BFS to find distances + DFS backtracking to reconstruct paths.
     * Follow-up: Bidirectional BFS?
     *   Start from both ends. Expand the smaller frontier each step.
     *   Meets in the middle — reduces time significantly in practice.
     */
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (!wordSet.contains(endWord)) return 0;

        Queue<String> q = new LinkedList<>();
        q.offer(beginWord);
        wordSet.remove(beginWord);
        int steps = 1;

        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                String word = q.poll();
                char[] chars = word.toCharArray();

                for (int j = 0; j < chars.length; j++) {
                    char original = chars[j];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == original) continue;
                        chars[j] = c;
                        String next = new String(chars);
                        if (next.equals(endWord)) return steps + 1;
                        if (wordSet.contains(next)) {
                            wordSet.remove(next);
                            q.offer(next);
                        }
                    }
                    chars[j] = original;
                }
            }
            steps++;
        }

        return 0;
    }

    // =========================================================
    // H5. Constrained Subsequence Sum  LC 1425
    // Pattern: DP + MONOTONIC DEQUE (same as Jump Game VI)
    // =========================================================
    /**
     * Maximum sum of a subsequence where consecutive chosen elements
     * are at most k indices apart.
     *
     * dp[i] = max sum of valid subsequence ending at index i
     * dp[i] = nums[i] + max(0, max(dp[i-k], ..., dp[i-1]))
     *
     * The max(0, ...) means we can START a new subsequence at i.
     *
     * Monotonic deque for sliding window maximum over dp values.
     * Answer: max(dp[0], dp[1], ..., dp[n-1]).
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: What if the constraint is "at LEAST k apart"?
     *   dp[i] = nums[i] + max(0, dp[0..i-k]).
     *   Maintain a running max of dp[0..i-k] → O(n).
     */
    public int constrainedSubsetSum(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        Deque<Integer> dq = new ArrayDeque<>();
        int maxSum = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            // Remove expired
            while (!dq.isEmpty() && dq.peekFirst() < i - k)
                dq.pollFirst();

            // dp[i] = nums[i] + max(0, best in window)
            dp[i] = nums[i];
            if (!dq.isEmpty()) dp[i] += Math.max(0, dp[dq.peekFirst()]);

            // Maintain decreasing deque
            while (!dq.isEmpty() && dp[dq.peekLast()] <= dp[i])
                dq.pollLast();

            dq.offerLast(i);
            maxSum = Math.max(maxSum, dp[i]);
        }

        return maxSum;
    }

    // =========================================================
    // H6. Shortest Path with Obstacles Elimination  LC 1293
    // Pattern: BFS with Extra State Dimension
    // =========================================================
    /**
     * Grid with obstacles. Can eliminate at most k obstacles.
     * Find shortest path from (0,0) to (m-1,n-1).
     *
     * Standard BFS but with state = (row, col, obstacles_remaining).
     * Visited array is 3D: visited[r][c][k].
     *
     * WHY 3D STATE:
     *   Reaching (r, c) with 3 eliminations left vs 1 elimination left
     *   are DIFFERENT states — different future possibilities.
     *   We might reach the same cell via a longer path but with more
     *   eliminations remaining, which could lead to a shorter overall path.
     *
     * Optimization: if k >= m + n - 3, answer is m + n - 2
     *   (Manhattan distance — we can eliminate any obstacle in our way).
     *
     * Time:  O(m × n × k)
     * Space: O(m × n × k)
     *
     * Follow-up: What if obstacles regenerate after t time steps?
     *   Add time to the state: (row, col, remaining, time).
     */
    public int shortestPath(int[][] grid, int k) {
        int m = grid.length, n = grid[0].length;

        // Optimization: if we can eliminate enough obstacles, straight path
        if (k >= m + n - 3) return m + n - 2;

        boolean[][][] visited = new boolean[m][n][k + 1];
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{0, 0, k});
        visited[0][0][k] = true;
        int steps = 0;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                int[] state = q.poll();
                int r = state[0], c = state[1], rem = state[2];

                if (r == m - 1 && c == n - 1) return steps;

                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;

                    int nrem = rem - grid[nr][nc]; // use one elimination if obstacle
                    if (nrem >= 0 && !visited[nr][nc][nrem]) {
                        visited[nr][nc][nrem] = true;
                        q.offer(new int[]{nr, nc, nrem});
                    }
                }
            }
            steps++;
        }

        return -1;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        QueueHard sol = new QueueHard();

        System.out.println("═══ H1: Sliding Window Maximum ═══");
        System.out.println(Arrays.toString(
            sol.maxSlidingWindow(new int[]{1,3,-1,-3,5,3,6,7}, 3)));
        // [3, 3, 5, 5, 6, 7]

        System.out.println("\n═══ H2: Shortest Subarray Sum ≥ K ═══");
        System.out.println(sol.shortestSubarray(new int[]{2,-1,2}, 3));     // 3
        System.out.println(sol.shortestSubarray(new int[]{1}, 1));          // 1
        System.out.println(sol.shortestSubarray(new int[]{84,-37,32,40,95}, 167)); // 3

        System.out.println("\n═══ H3: Jump Game VI ═══");
        System.out.println(sol.maxResult(new int[]{1,-1,-2,4,-7,3}, 2));   // 7
        System.out.println(sol.maxResult(new int[]{10,-5,-2,4,0,3}, 3));   // 17

        System.out.println("\n═══ H4: Word Ladder ═══");
        System.out.println(sol.ladderLength("hit", "cog",
            Arrays.asList("hot","dot","dog","lot","log","cog")));  // 5
        System.out.println(sol.ladderLength("hit", "cog",
            Arrays.asList("hot","dot","dog","lot","log")));        // 0

        System.out.println("\n═══ H5: Constrained Subsequence Sum ═══");
        System.out.println(sol.constrainedSubsetSum(new int[]{10,2,-10,5,20}, 2));  // 37
        System.out.println(sol.constrainedSubsetSum(new int[]{-1,-2,-3}, 1));       // -1

        System.out.println("\n═══ H6: Shortest Path with Obstacles ═══");
        System.out.println(sol.shortestPath(new int[][]{
            {0,0,0},{1,1,0},{0,0,0},{0,1,1},{0,0,0}}, 1));  // 6
    }
}
