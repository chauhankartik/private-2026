/**
 * ============================================================
 *  PATTERN 4 — GRID / MATRIX DP
 *  Problem 3 (Hard): Maximal Square   LC 221
 * ============================================================
 *
 *  Difficulty  : Medium-Hard
 *  Pattern     : Grid DP — dp[i][j] = side of largest all-1 square at (i,j)
 *  LeetCode    : https://leetcode.com/problems/maximal-square/
 *
 *  PROBLEM STATEMENT:
 *    Given an m × n binary matrix of '0's and '1's, find the largest square
 *    containing only '1's and return its area.
 *
 *  EXAMPLES:
 *    [["1","0","1","0","0"],
 *     ["1","0","1","1","1"],
 *     ["1","1","1","1","1"],
 *     ["1","0","0","1","0"]]  → 4 (2×2 square)
 *
 *    [["0","1"],["1","0"]]  → 1
 *    [["0"]]  → 0
 *
 * ============================================================
 */
import java.util.*;

class Problem3_MaximalSquare {

    // =========================================================
    // APPROACH 1 — 1D SPACE-OPTIMIZED DP (optimal)
    // =========================================================
    /**
     * dp[j] = side length of largest all-1 square whose bottom-right is (currentRow, j)
     * prev  = dp[j] from the PREVIOUS row (= dp[i-1][j] in the 2D formulation)
     *
     * KEY INSIGHT:
     *   dp[i][j] = min(dp[i-1][j],   ← above
     *                  dp[i][j-1],   ← left
     *                  dp[i-1][j-1]) ← diagonal
     *              + 1
     *
     *   WHY MIN OF THREE NEIGHBORS?
     *     The square at (i,j) is constrained by all three directions.
     *     If the above square has side s1, left has side s2, diagonal has side s3,
     *     the new square can have side min(s1,s2,s3)+1.
     *     Example: if left=3, above=3, diagonal=2 → new side = 2+1=3, NOT 4.
     *     (The diagonal limits how far back both dimensions can extend simultaneously.)
     *
     * SPACE OPTIMIZATION:
     *   'prev' saves dp[i-1][j-1] (diagonal) BEFORE dp[j] is overwritten.
     *   dp[j] before update = dp[i-1][j] (above).
     *   dp[j-1] after update = dp[i][j-1] (left).
     *
     * VISUAL for row=[1,1,1], prev_dp=[1,1,1]:
     *   j=1: prev=prev_dp[1]=1, dp[1]=min(dp[1]=1, dp[0]=1, prev=1)+1=2
     *   j=2: prev=prev_dp[2]=1, dp[2]=min(dp[2]=1, dp[1]=2, prev=1)+1=2
     *   After: dp=[1,2,2], maxSide=2
     *
     * Time: O(m × n), Space: O(n)
     *
     * Follow-up: Maximal Rectangle (LC 85)?
     *   For each row, build a height histogram (height[j] = consecutive 1s above).
     *   Apply "Largest Rectangle in Histogram" (stack-based) on each row's histogram.
     *   Time: O(m × n), Space: O(n).
     * Follow-up: Count all squares (LC 1277)?
     *   Answer = sum of all dp[i][j] values. Each dp[i][j]=k means k squares end here.
     */
    public int maximalSquare(char[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[] dp = new int[n + 1];
        int maxSide = 0, prev = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (matrix[i-1][j-1] == '1') {
                    dp[j] = Math.min(prev, Math.min(dp[j], dp[j-1])) + 1;
                    maxSide = Math.max(maxSide, dp[j]);
                } else {
                    dp[j] = 0;
                }
                prev = temp;  // save dp[i-1][j] before it's overwritten → becomes diagonal for j+1
            }
            prev = 0;  // reset for new row (dp[i-1][0] = 0 for the column-0 case)
        }

        return maxSide * maxSide;
    }

    // =========================================================
    // APPROACH 2 — FULL 2D DP (cleaner to understand)
    // =========================================================
    /**
     * Classic 2D DP. No space optimization.
     * Time: O(m × n), Space: O(m × n)
     */
    public int maximalSquare2D(char[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[][] dp = new int[m + 1][n + 1];
        int maxSide = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (matrix[i-1][j-1] == '1') {
                    dp[i][j] = Math.min(dp[i-1][j],
                                Math.min(dp[i][j-1], dp[i-1][j-1])) + 1;
                    maxSide = Math.max(maxSide, dp[i][j]);
                }
            }
        }

        return maxSide * maxSide;
    }

    // =========================================================
    // APPROACH 3 — BRUTE FORCE (validate all squares)
    // =========================================================
    /**
     * For each cell (i,j) and each possible side length s,
     * check if the s×s square with top-left at (i,j) is all-1s.
     * Time: O(m²n²) or O(m × n × min(m,n)²) — too slow for large grids.
     */
    public int maximalSquareBrute(char[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int maxSide = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == '1') {
                    int s = 1;
                    while (isSquare(matrix, i, j, s + 1, m, n)) s++;
                    maxSide = Math.max(maxSide, s);
                }
            }
        }
        return maxSide * maxSide;
    }

    private boolean isSquare(char[][] m, int r, int c, int s, int rows, int cols) {
        if (r + s > rows || c + s > cols) return false;
        for (int i = r; i < r + s; i++)
            for (int j = c; j < c + s; j++)
                if (m[i][j] != '1') return false;
        return true;
    }

    // =========================================================
    // BONUS: Count Squares (LC 1277)
    // =========================================================
    /**
     * Count all squares of all sizes with all-1 entries.
     * dp[i][j] = side of largest square ending at (i,j).
     * Each dp[i][j]=k contributes k squares (of sizes 1×1, 2×2, ..., k×k).
     * Answer = sum of all dp values.
     */
    public int countSquares(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int count = 0;
        int[] dp = new int[n + 1];
        int prev = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (matrix[i-1][j-1] == 1) {
                    dp[j] = Math.min(prev, Math.min(dp[j], dp[j-1])) + 1;
                    count += dp[j];
                } else {
                    dp[j] = 0;
                }
                prev = temp;
            }
            prev = 0;
        }
        return count;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_MaximalSquare sol = new Problem3_MaximalSquare();

        System.out.println("=== LC 221: Maximal Square ===");

        char[][][] matrices = {
            {{'1','0','1','0','0'},{'1','0','1','1','1'},
             {'1','1','1','1','1'},{'1','0','0','1','0'}},  // 4
            {{'0','1'},{'1','0'}},                           // 1
            {{'0'}},                                          // 0
            {{'1','1','1','1'},{'1','1','1','1'},
             {'1','1','1','1'}},                             // 9 (3x3 square)
        };

        for (char[][] m : matrices) {
            System.out.println("Matrix: " + Arrays.deepToString(m));
            System.out.println("  1D DP  : " + sol.maximalSquare(m));
            System.out.println("  2D DP  : " + sol.maximalSquare2D(m));
            System.out.println("  Brute  : " + sol.maximalSquareBrute(m));
            System.out.println();
        }

        System.out.println("=== LC 1277: Count Squares ===");
        int[][] cs1 = {{0,1,1,1},{1,1,1,1},{0,1,1,1}};
        System.out.println(sol.countSquares(cs1)); // 15
        int[][] cs2 = {{1,0,1},{1,1,0},{1,1,0}};
        System.out.println(sol.countSquares(cs2)); // 7
    }
}
