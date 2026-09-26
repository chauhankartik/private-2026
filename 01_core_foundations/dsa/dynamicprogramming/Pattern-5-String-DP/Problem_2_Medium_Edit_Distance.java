/**
 * ============================================================
 *  PATTERN 5 — STRING DP (Two-Sequence)
 *  Problem 2 (Medium): Edit Distance   LC 72
 * ============================================================
 *
 *  Difficulty  : Hard (often listed Medium, treated as Hard here)
 *  Pattern     : String DP — dp[i][j] with insert/delete/replace
 *  LeetCode    : https://leetcode.com/problems/edit-distance/
 *
 *  PROBLEM STATEMENT:
 *    Given two strings word1 and word2, return the minimum number of operations
 *    (insert, delete, replace — each costs 1) to convert word1 to word2.
 *
 *  EXAMPLES:
 *    "horse" → "ros"  : 3 (replace h→r, remove o, remove e)
 *    "intention" → "execution": 5
 *
 * ============================================================
 */
import java.util.*;

class Problem2_EditDistance {

    // =========================================================
    // APPROACH 1 — SPACE-OPTIMIZED DP (two rows)
    // =========================================================
    /**
     * dp[i][j] = edit distance between word1[0..i-1] and word2[0..j-1]
     *
     * TRANSITION:
     *   If word1[i-1] == word2[j-1]: dp[i][j] = dp[i-1][j-1]  (no op needed)
     *   Else: dp[i][j] = 1 + min(
     *     dp[i-1][j-1],  ← REPLACE: turn word1[i-1] into word2[j-1]
     *     dp[i-1][j],    ← DELETE:  remove word1[i-1]
     *     dp[i][j-1]     ← INSERT:  insert word2[j-1] after word1[0..i-1]
     *   )
     *
     * BASE CASES:
     *   dp[i][0] = i  (delete all i chars from word1 to reach empty)
     *   dp[0][j] = j  (insert all j chars of word2 into empty)
     *
     * VISUAL for "horse" → "ros":
     *        ""  r  o  s
     *    ""   0  1  2  3
     *    h    1  1  2  3
     *    o    2  2  1  2
     *    r    3  2  2  2
     *    s    4  3  3  2
     *    e    5  4  4  3  ← answer: 3
     *
     * SPACE: two 1D arrays (prev, curr). Need prev[j-1] = diagonal → save as 'diag'.
     *
     * Time: O(m × n), Space: O(min(m, n))
     *
     * Follow-up: One Edit Distance (LC 161)?
     *   Check if edit distance == 1 in O(n) without full DP.
     * Follow-up: Operations with different costs (ins=a, del=b, rep=c)?
     *   Replace 1 with the respective cost in the min expression.
     */
    public int minDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int j = 0; j <= n; j++) prev[j] = j;  // dp[0][j] = j

        for (int i = 1; i <= m; i++) {
            curr[0] = i;  // dp[i][0] = i
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i-1) == word2.charAt(j-1)) {
                    curr[j] = prev[j-1];              // match: diagonal (no op)
                } else {
                    curr[j] = 1 + Math.min(prev[j-1],       // replace
                                  Math.min(prev[j],          // delete
                                           curr[j-1]));      // insert
                }
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }

        return prev[n];
    }

    // =========================================================
    // APPROACH 2 — FULL 2D DP (most instructive, easy to read)
    // =========================================================
    public int minDistanceFull(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i-1) == word2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i-1][j-1],
                                   Math.min(dp[i-1][j], dp[i][j-1]));
                }
            }
        }

        return dp[m][n];
    }

    // =========================================================
    // APPROACH 3 — RECONSTRUCT OPERATIONS
    // =========================================================
    /**
     * Backtrack through the 2D DP table to find the actual edit operations.
     * Returns a list of operation descriptions.
     */
    public List<String> editOperations(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i-1) == word2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
                }
            }
        }

        // Backtrack
        List<String> ops = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && word1.charAt(i-1) == word2.charAt(j-1)) {
                ops.add("Match '" + word1.charAt(i-1) + "'");
                i--; j--;
            } else if (j > 0 && (i == 0 || dp[i][j] == dp[i][j-1] + 1)) {
                ops.add("Insert '" + word2.charAt(j-1) + "'");
                j--;
            } else if (i > 0 && (j == 0 || dp[i][j] == dp[i-1][j] + 1)) {
                ops.add("Delete '" + word1.charAt(i-1) + "'");
                i--;
            } else {
                ops.add("Replace '" + word1.charAt(i-1) + "' → '" + word2.charAt(j-1) + "'");
                i--; j--;
            }
        }
        Collections.reverse(ops);
        return ops;
    }

    // =========================================================
    // BONUS: Delete Operations for Two Strings (LC 583)
    // =========================================================
    /**
     * Minimum deletions from s1 and s2 to make them equal.
     * Answer = m + n - 2 * LCS(s1, s2).
     */
    public int minDeleteSteps(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int[] prev = new int[n + 1], curr = new int[n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                curr[j] = s1.charAt(i-1) == s2.charAt(j-1)
                    ? prev[j-1] + 1
                    : Math.max(prev[j], curr[j-1]);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            Arrays.fill(curr, 0);
        }
        int lcs = prev[n];
        return (m - lcs) + (n - lcs);
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_EditDistance sol = new Problem2_EditDistance();

        System.out.println("=== LC 72: Edit Distance ===");
        String[][] tests = {
            {"horse", "ros"},          // 3
            {"intention", "execution"}, // 5
            {"", "abc"},               // 3
            {"abc", ""},               // 3
            {"abc", "abc"},            // 0
        };

        for (String[] t : tests) {
            System.out.printf("%-12s → %-12s%n", "\"" + t[0] + "\"", "\"" + t[1] + "\"");
            System.out.println("  2-Row DP  : " + sol.minDistance(t[0], t[1]));
            System.out.println("  Full 2D   : " + sol.minDistanceFull(t[0], t[1]));
            System.out.println("  Operations: " + sol.editOperations(t[0], t[1]));
            System.out.println();
        }

        System.out.println("=== LC 583: Delete Operations ===");
        System.out.println(sol.minDeleteSteps("sea", "eat"));  // 2
        System.out.println(sol.minDeleteSteps("leetcode", "etco")); // 4
    }
}
