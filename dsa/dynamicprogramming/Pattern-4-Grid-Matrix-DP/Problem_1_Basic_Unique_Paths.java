/**
 * ============================================================
 *  PATTERN 4 — GRID / MATRIX DP
 *  Problem 1 (Basic): Unique Paths   LC 62 + LC 63
 * ============================================================
 *
 *  Difficulty  : Medium (Basic for Grid DP pattern)
 *  Pattern     : Grid DP — dp[i][j] from above + left
 *  LeetCode    : LC 62 / LC 63
 *
 *  PROBLEM STATEMENT (LC 62):
 *    A robot is at top-left of an m × n grid. It can move only right or down.
 *    Count the number of distinct paths to reach bottom-right.
 *
 *  EXAMPLES:
 *    m=3, n=7 → 28
 *    m=3, n=2 → 3
 *
 * ============================================================
 */
import java.util.*;

class Problem1_UniquePaths {

    // =========================================================
    // APPROACH 1 — 1D SPACE-OPTIMIZED DP (optimal)
    // =========================================================
    /**
     * dp[j] = number of ways to reach current row, column j
     *
     * Before processing row i:
     *   dp[j] = ways to reach row i-1, col j (from above)
     * After updating left-to-right:
     *   dp[j] += dp[j-1]  ← add ways from left (current row)
     *
     * VISUAL (m=3, n=3):
     *   Initial: dp = [1,1,1] (top row — only 1 way: go all right)
     *   Row 1:   dp[0]=1, dp[1]=1+1=2, dp[2]=2+1=3
     *   Row 2:   dp[0]=1, dp[1]=1+2=3, dp[2]=3+3=6  ← answer: 6
     *
     * Time: O(m × n), Space: O(n)
     *
     * Follow-up: Math (combinatorics) solution in O(min(m,n))?
     *   C(m+n-2, m-1) = (m+n-2)! / ((m-1)! × (n-1)!)
     *   Total steps = m+n-2. Choose m-1 of them to go down.
     */
    public int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1);  // first row: all 1s (only move right)

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1];  // from above (dp[j]) + from left (dp[j-1])
            }
        }

        return dp[n - 1];
    }

    // =========================================================
    // APPROACH 2 — MATH (Combinatorics)
    // =========================================================
    /**
     * The answer is C(m+n-2, m-1).
     * (Choose exactly m-1 downward steps from total m+n-2 steps)
     *
     * Use long to avoid overflow during computation.
     */
    public int uniquePathsMath(int m, int n) {
        long result = 1;
        int total = m + n - 2;
        int choose = Math.min(m - 1, n - 1);  // smaller for fewer multiplications
        for (int i = 0; i < choose; i++) {
            result = result * (total - i) / (i + 1);
        }
        return (int) result;
    }

    // =========================================================
    // APPROACH 3 — UNIQUE PATHS II (with obstacles, LC 63)
    // =========================================================
    /**
     * obstacleGrid[i][j] = 1 means blocked; 0 means open.
     *
     * dp[j]: if cell is blocked → dp[j] = 0; else → dp[j] += dp[j-1] (same as LC 62)
     *
     * Handle obstacles in top row / left column carefully:
     *   Once blocked, all subsequent cells in that row/column = 0.
     *
     * Time: O(m × n), Space: O(n)
     */
    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        int m = obstacleGrid.length, n = obstacleGrid[0].length;

        if (obstacleGrid[0][0] == 1 || obstacleGrid[m-1][n-1] == 1) return 0;

        int[] dp = new int[n];
        dp[0] = 1;

        // Initialize top row
        for (int j = 1; j < n; j++) {
            dp[j] = obstacleGrid[0][j] == 1 ? 0 : dp[j-1];
        }

        for (int i = 1; i < m; i++) {
            dp[0] = obstacleGrid[i][0] == 1 ? 0 : dp[0];  // left column
            for (int j = 1; j < n; j++) {
                dp[j] = obstacleGrid[i][j] == 1 ? 0 : dp[j] + dp[j-1];
            }
        }

        return dp[n-1];
    }

    // =========================================================
    // APPROACH 4 — COUNT PATHS AND PRINT ONE PATH
    // =========================================================
    /**
     * Use the 2D DP table to reconstruct one valid path.
     * From (0,0), at each step go in the direction with more paths.
     */
    public List<int[]> findOnePath(int m, int n) {
        int[][] dp = new int[m][n];
        for (int[] row : dp) Arrays.fill(row, 1);
        for (int i = 1; i < m; i++)
            for (int j = 1; j < n; j++)
                dp[i][j] = dp[i-1][j] + dp[i][j-1];

        // Reconstruct path (greedy: always go in direction with more options)
        List<int[]> path = new ArrayList<>();
        int i = 0, j = 0;
        path.add(new int[]{0, 0});
        while (i < m - 1 || j < n - 1) {
            if (i == m - 1) j++;       // can only go right
            else if (j == n - 1) i++;  // can only go down
            else if (dp[i+1][j] > dp[i][j+1]) i++;  // more paths going down
            else j++;
            path.add(new int[]{i, j});
        }
        return path;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_UniquePaths sol = new Problem1_UniquePaths();

        System.out.println("=== LC 62: Unique Paths ===");
        int[][] dims = {{3,7},{3,2},{7,3},{1,1},{10,10}};
        for (int[] d : dims) {
            System.out.printf("m=%d, n=%d: DP=%d, Math=%d%n",
                d[0], d[1], sol.uniquePaths(d[0], d[1]), sol.uniquePathsMath(d[0], d[1]));
        }

        System.out.println("\n=== LC 63: Unique Paths II (with obstacles) ===");
        int[][][] grids = {
            {{0,0,0},{0,1,0},{0,0,0}},  // 2
            {{0,1},{0,0}},              // 1
            {{1,0}},                    // 0
        };
        for (int[][] g : grids) {
            System.out.println("Grid: " + Arrays.deepToString(g) +
                " → " + sol.uniquePathsWithObstacles(g));
        }

        System.out.println("\n=== One Path Reconstruction (3x4) ===");
        List<int[]> path = sol.findOnePath(3, 4);
        for (int[] step : path) {
            System.out.print(Arrays.toString(step) + " ");
        }
        System.out.println();
    }
}
