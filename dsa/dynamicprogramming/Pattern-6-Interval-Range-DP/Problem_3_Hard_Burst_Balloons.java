/**
 * ============================================================
 *  PATTERN 6 — INTERVAL / RANGE DP
 *  Problem 3 (Hard): Burst Balloons   LC 312
 * ============================================================
 *
 *  Difficulty  : Hard
 *  Pattern     : Interval DP — "Last Action / Reverse Thinking"
 *  LeetCode    : https://leetcode.com/problems/burst-balloons/
 *
 *  PROBLEM STATEMENT:
 *    n balloons indexed 0..n-1, each with a number nums[i].
 *    Bursting balloon i gives nums[left] * nums[i] * nums[right] coins,
 *    where left and right are i's remaining neighbors.
 *    After bursting, left and right become adjacent.
 *    Return the maximum coins you can collect by bursting all balloons.
 *
 *  EXAMPLES:
 *    nums = [3, 1, 5, 8]  →  167
 *      Burst order: 1 → 5 → 3 → 8
 *      Coins: 3*1*5 + 3*5*8 + 1*3*8 + 1*8*1 = 15+120+24+8 = 167
 *
 *    nums = [1, 5]  →  10
 *
 *  THE KEY INSIGHT — WHY "LAST BURST":
 *    If we think "first burst", the boundaries of subproblems CHANGE
 *    after each burst (neighbors shift). Subproblems are NOT independent.
 *
 *    If we think "LAST burst": when balloon k is the LAST to burst in [i..j],
 *    ALL other balloons in [i..j] are already gone.
 *    Balloon k's only living neighbors are the BOUNDARIES: padded[i-1] and padded[j+1].
 *    So left subproblem [i..k-1] and right subproblem [k+1..j] are INDEPENDENT.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_BurstBalloons {

    // =========================================================
    // APPROACH 1 — TOP-DOWN DP WITH MEMOIZATION
    // =========================================================
    /**
     * STATE:
     *   dp[i][j] = max coins from bursting all balloons in range [i..j]
     *              (using the padded array where padded[0]=padded[n+1]=1)
     *
     * TRANSITION (k = last balloon to burst in [i..j]):
     *   dp[i][j] = max over k ∈ [i..j]:
     *     padded[i-1] * padded[k] * padded[j+1]   ← coins from bursting k last
     *   + dp[i][k-1]                               ← best from left of k
     *   + dp[k+1][j]                               ← best from right of k
     *
     * BASE CASE:
     *   dp[i][j] = 0 when i > j (empty range)
     *
     * VISUAL for nums = [3, 1, 5, 8]:
     *   padded = [1, 3, 1, 5, 8, 1]  (indices 0..5, balloons at 1..4)
     *
     *   len=1:
     *     dp[1][1]: k=1 → padded[0]*padded[1]*padded[2] = 1*3*1 = 3
     *     dp[2][2]: k=2 → padded[1]*padded[2]*padded[3] = 3*1*5 = 15
     *     dp[3][3]: k=3 → padded[2]*padded[3]*padded[4] = 1*5*8 = 40
     *     dp[4][4]: k=4 → padded[3]*padded[4]*padded[5] = 5*8*1 = 40
     *
     *   len=2:
     *     dp[1][2]: k=1 → 1*3*5 + dp[2][2] = 15+15 = 30
     *               k=2 → 1*1*5 + dp[1][1] = 5+3   = 8   → dp[1][2]=30
     *     dp[2][3]: k=2 → 3*1*8 + dp[3][3] = 24+40 = 64
     *               k=3 → 3*5*8 + dp[2][2] = 120+15=135  → dp[2][3]=135
     *     dp[3][4]: k=3 → 1*5*1 + dp[4][4] = 5+40  = 45
     *               k=4 → 1*8*1 + dp[3][3] = 8+40  = 48  → dp[3][4]=48
     *
     *   len=3:
     *     dp[1][3]: k=1 → 1*3*8+dp[2][3]=24+135=159
     *               k=2 → 1*1*8+dp[1][1]+dp[3][3]=8+3+40=51
     *               k=3 → 1*5*8+dp[1][2]=40+30=70 → dp[1][3]=159
     *     dp[2][4]: k=2 → 3*1*1+dp[3][4]=3+48=51
     *               k=3 → 3*5*1+dp[2][2]+dp[4][4]=15+15+40=70
     *               k=4 → 3*8*1+dp[2][3]=24+135=159 → dp[2][4]=159
     *
     *   len=4:
     *     dp[1][4]: k=1 → 1*3*1+dp[2][4]=3+159=162
     *               k=2 → 1*1*1+dp[1][1]+dp[3][4]=1+3+48=52
     *               k=3 → 1*5*1+dp[1][2]+dp[4][4]=5+30+40=75
     *               k=4 → 1*8*1+dp[1][3]=8+159=167 ← BEST!
     *     → dp[1][4] = 167 ✓
     *
     * Time:  O(n³) — O(n²) states × O(n) transitions each
     * Space: O(n²) — memoization table
     *
     * Follow-up: Return the optimal burst ORDER?
     *   Store the chosen k at each (i,j) in a 'choice' table.
     *   Reconstruct: burst choice[1][n] last, recurse into left/right subranges.
     */
    private int[][] memo;
    private int[] padded;

    public int maxCoinsTopDown(int[] nums) {
        int n = nums.length;
        padded = new int[n + 2];
        padded[0] = padded[n + 1] = 1;
        for (int i = 0; i < n; i++) padded[i + 1] = nums[i];

        memo = new int[n + 2][n + 2];
        for (int[] row : memo) Arrays.fill(row, -1);

        return solve(1, n);
    }

    private int solve(int i, int j) {
        if (i > j) return 0;
        if (memo[i][j] != -1) return memo[i][j];

        int best = 0;
        for (int k = i; k <= j; k++) {
            int coins = padded[i - 1] * padded[k] * padded[j + 1]
                      + solve(i, k - 1)
                      + solve(k + 1, j);
            best = Math.max(best, coins);
        }

        return memo[i][j] = best;
    }

    // =========================================================
    // APPROACH 2 — BOTTOM-UP INTERVAL DP (Preferred in interviews)
    // =========================================================
    /**
     * Same recurrence, but filled iteratively by increasing interval length.
     * Cleaner to present in interviews (no recursion overhead).
     *
     * Time:  O(n³)
     * Space: O(n²)
     */
    public int maxCoinsBottomUp(int[] nums) {
        int n = nums.length;

        // Pad the array with 1 on both ends
        int[] p = new int[n + 2];
        p[0] = p[n + 1] = 1;
        for (int i = 0; i < n; i++) p[i + 1] = nums[i];

        int[][] dp = new int[n + 2][n + 2];

        // Iterate by increasing interval length (within the original balloon indices)
        for (int len = 1; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    // k is the LAST balloon to burst in [i..j]
                    int coins = p[i - 1] * p[k] * p[j + 1]
                              + dp[i][k - 1]
                              + dp[k + 1][j];
                    dp[i][j] = Math.max(dp[i][j], coins);
                }
            }
        }

        return dp[1][n];
    }

    // =========================================================
    // APPROACH 3 — BRUTE FORCE (Naive recursion — exponential)
    // =========================================================
    /**
     * Try all possible burst orders via backtracking.
     * Time: O(n!) — factorial, unusable for n > 10.
     * Only for small-n validation.
     */
    public int maxCoinsBrute(int[] nums) {
        return bruteHelper(new ArrayList<>(toList(nums)));
    }

    private int bruteHelper(List<Integer> list) {
        if (list.isEmpty()) return 0;
        int best = 0;
        for (int i = 0; i < list.size(); i++) {
            int left  = (i > 0) ? list.get(i - 1) : 1;
            int right = (i < list.size() - 1) ? list.get(i + 1) : 1;
            int coins = left * list.get(i) * right;
            int removed = list.remove(i);
            best = Math.max(best, coins + bruteHelper(list));
            list.add(i, removed);
        }
        return best;
    }

    private List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int x : arr) list.add(x);
        return list;
    }

    // =========================================================
    // BONUS: Reconstruct Optimal Burst Order
    // =========================================================
    /**
     * Track which balloon k was chosen as "last" for each interval.
     * Then reconstruct the burst order via reverse recursion.
     *
     * The balloon chosen LAST in [i..j] is burst first when we read
     * the order forward (since "last to burst" = "put off as long as possible").
     */
    public void printBurstOrder(int[] nums) {
        int n = nums.length;
        int[] p = new int[n + 2];
        p[0] = p[n + 1] = 1;
        for (int i = 0; i < n; i++) p[i + 1] = nums[i];

        int[][] dp  = new int[n + 2][n + 2];
        int[][] last = new int[n + 2][n + 2];  // which k was last in [i][j]

        for (int len = 1; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    int coins = p[i - 1] * p[k] * p[j + 1]
                              + dp[i][k - 1] + dp[k + 1][j];
                    if (coins > dp[i][j]) {
                        dp[i][j] = coins;
                        last[i][j] = k;
                    }
                }
            }
        }

        System.out.print("Burst order (last-to-first in optimal): ");
        printOrder(last, 1, n);
        System.out.println();
        System.out.println("Max coins: " + dp[1][n]);
    }

    private void printOrder(int[][] last, int i, int j) {
        if (i > j) return;
        int k = last[i][j];
        printOrder(last, i, k - 1);
        printOrder(last, k + 1, j);
        System.out.print("balloon[" + (k - 1) + "] ");  // 0-indexed original
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_BurstBalloons sol = new Problem3_BurstBalloons();

        int[][] testCases = {
            {3, 1, 5, 8},   // Expected: 167
            {1, 5},          // Expected: 10
            {1},             // Expected: 1
            {8, 3, 4, 6}    // Expected: 576? Let's see
        };

        for (int[] nums : testCases) {
            System.out.println("nums = " + Arrays.toString(nums));
            System.out.println("  Top-Down  : " + sol.maxCoinsTopDown(nums));
            System.out.println("  Bottom-Up : " + sol.maxCoinsBottomUp(nums));
            if (nums.length <= 8) {
                System.out.println("  Brute     : " + sol.maxCoinsBrute(nums));
            }
            sol.printBurstOrder(nums);
            System.out.println();
        }
    }
}
