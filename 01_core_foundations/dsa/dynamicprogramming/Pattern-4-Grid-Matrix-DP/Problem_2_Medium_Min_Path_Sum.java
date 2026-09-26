/**
 * ============================================================
 *  PATTERN 4 — GRID / MATRIX DP
 *  Problem 2 (Medium): Minimum Path Sum   LC 64
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Grid DP — dp[i][j] = min cost to reach (i,j)
 *  LeetCode    : https://leetcode.com/problems/minimum-path-sum/
 *
 *  PROBLEM STATEMENT:
 *    Given an m × n grid of non-negative integers, find a path from
 *    top-left to bottom-right that minimizes the sum of numbers along the path.
 *    You can only move right or down.
 *
 *  EXAMPLES:
 *    [[1,3,1],[1,5,1],[4,2,1]]  → 7  (1→3→1→1→1)
 *    [[1,2,3],[4,5,6]]          → 12 (1→2→3→6)
 *
 * ============================================================
 */
import java.util.*;

class Problem2_MinPathSum {

    // =========================================================
    // APPROACH 1 — 1D SPACE-OPTIMIZED DP (optimal)
    // =========================================================
    /**
     * dp[j] = min cost to reach current row, column j.
     *
     * Initialization:
     *   dp[0] = grid[0][0] (top-left)
     *   dp[j] = dp[j-1] + grid[0][j]  (first row: can only come from left)
     *
     * Row updates (i from 1 to m-1):
     *   dp[0] += grid[i][0]   (left column: can only come from above)
     *   dp[j] = min(dp[j], dp[j-1]) + grid[i][j]
     *           ↑ from above  ↑ from left
     *
     * VISUAL for [[1,3,1],[1,5,1],[4,2,1]]:
     *   Init: dp = [1, 4, 5]  (first row cumulative sum)
     *   Row 1: dp[0]=1+1=2, dp[1]=min(4,2)+5=7, dp[2]=min(5,7)+1=6
     *          dp = [2, 7, 6]
     *   Row 2: dp[0]=2+4=6, dp[1]=min(7,6)+2=8, dp[2]=min(6,8)+1=7
     *          dp = [6, 8, 7]   ← answer: dp[2] = 7 ✓
     *
     * Time: O(m × n), Space: O(n)
     *
     * Follow-up: What if you can move in all 4 directions?
     *   This is no longer a simple DP (can revisit cells) → use Dijkstra's algorithm.
     * Follow-up: Triangle (LC 120) — min path from top of triangle to bottom?
     *   Same pattern: process row by row bottom-up. dp[j] = min(dp[j], dp[j+1]) + tri[i][j].
     */
    public int minPathSum(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dp = new int[n];

        // First row initialization
        dp[0] = grid[0][0];
        for (int j = 1; j < n; j++) dp[j] = dp[j-1] + grid[0][j];

        for (int i = 1; i < m; i++) {
            dp[0] += grid[i][0];                            // left column: from above only
            for (int j = 1; j < n; j++) {
                dp[j] = Math.min(dp[j], dp[j-1]) + grid[i][j];  // min(above, left) + cell
            }
        }

        return dp[n-1];
    }

    // =========================================================
    // APPROACH 2 — IN-PLACE DP (modify grid directly)
    // =========================================================
    /**
     * Modify grid in-place to store dp values. No extra space needed.
     * (Only do this in interviews if modifying input is allowed.)
     */
    public int minPathSumInPlace(int[][] grid) {
        int m = grid.length, n = grid[0].length;

        // First row
        for (int j = 1; j < n; j++) grid[0][j] += grid[0][j-1];
        // First column
        for (int i = 1; i < m; i++) grid[i][0] += grid[i-1][0];

        for (int i = 1; i < m; i++)
            for (int j = 1; j < n; j++)
                grid[i][j] += Math.min(grid[i-1][j], grid[i][j-1]);

        return grid[m-1][n-1];
    }

    // =========================================================
    // APPROACH 3 — RECONSTRUCT MINIMUM PATH
    // =========================================================
    /**
     * Use 2D DP, then backtrack from (m-1, n-1) to (0,0).
     * At each cell, go toward the neighbor with the smaller dp value.
     */
    public List<int[]> minPath(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dp = new int[m][n];
        dp[0][0] = grid[0][0];
        for (int j = 1; j < n; j++) dp[0][j] = dp[0][j-1] + grid[0][j];
        for (int i = 1; i < m; i++) dp[i][0] = dp[i-1][0] + grid[i][0];
        for (int i = 1; i < m; i++)
            for (int j = 1; j < n; j++)
                dp[i][j] = Math.min(dp[i-1][j], dp[i][j-1]) + grid[i][j];

        // Backtrack from (m-1, n-1)
        List<int[]> path = new ArrayList<>();
        int i = m-1, j = n-1;
        while (i > 0 || j > 0) {
            path.add(new int[]{i, j});
            if (i == 0) j--;
            else if (j == 0) i--;
            else if (dp[i-1][j] < dp[i][j-1]) i--;
            else j--;
        }
        path.add(new int[]{0, 0});
        Collections.reverse(path);
        return path;
    }

    // =========================================================
    // BONUS: Triangle (LC 120) — same pattern, different structure
    // =========================================================
    /**
     * triangle[i][j]: access valid for j in [0, i].
     * Process bottom-up: for each row from second-to-last to top,
     *   dp[j] = min(dp[j], dp[j+1]) + triangle[i][j]
     * Answer: dp[0] after processing all rows.
     *
     * Time: O(n²), Space: O(n)
     */
    public int minimumTotal(List<List<Integer>> triangle) {
        int n = triangle.size();
        int[] dp = new int[n];
        // Initialize with bottom row
        for (int j = 0; j < n; j++) dp[j] = triangle.get(n-1).get(j);

        // Process bottom-up
        for (int i = n-2; i >= 0; i--) {
            for (int j = 0; j <= i; j++) {
                dp[j] = Math.min(dp[j], dp[j+1]) + triangle.get(i).get(j);
            }
        }
        return dp[0];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_MinPathSum sol = new Problem2_MinPathSum();

        System.out.println("=== LC 64: Minimum Path Sum ===");

        int[][][] grids = {
            {{1,3,1},{1,5,1},{4,2,1}},    // 7
            {{1,2,3},{4,5,6}},             // 12
            {{1}},                          // 1
            {{1,2},{1,1}},                 // 3
        };

        for (int[][] g : grids) {
            System.out.println("Grid: " + Arrays.deepToString(g));
            System.out.println("  1D DP      : " + sol.minPathSum(g));

            // Make a copy for in-place (to avoid mutating)
            int[][] copy = Arrays.stream(g).map(int[]::clone).toArray(int[][]::new);
            System.out.println("  In-place   : " + sol.minPathSumInPlace(copy));

            System.out.print("  Min Path   : ");
            sol.minPath(g).forEach(cell -> System.out.print(Arrays.toString(cell) + " "));
            System.out.println();
        }

        System.out.println("\n=== LC 120: Triangle ===");
        List<List<Integer>> triangle = List.of(
            List.of(2),
            List.of(3,4),
            List.of(6,5,7),
            List.of(4,1,8,3)
        );
        System.out.println("Triangle min path: " + sol.minimumTotal(triangle)); // 11 (2+3+5+1)
    }
}
