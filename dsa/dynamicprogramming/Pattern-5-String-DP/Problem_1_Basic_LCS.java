/**
 * ============================================================
 *  PATTERN 5 — STRING DP (Two-Sequence)
 *  Problem 1 (Basic): Longest Common Subsequence   LC 1143
 * ============================================================
 *
 *  Difficulty  : Medium (Basic for String DP pattern)
 *  Pattern     : String DP — dp[i][j] on two strings
 *  LeetCode    : https://leetcode.com/problems/longest-common-subsequence/
 *
 *  PROBLEM STATEMENT:
 *    Given two strings text1 and text2, return the length of their longest
 *    common subsequence. A subsequence does NOT need to be contiguous.
 *    Return 0 if no common subsequence.
 *
 *  EXAMPLES:
 *    text1="abcde", text2="ace"  → 3  ("ace")
 *    text1="abc",   text2="abc"  → 3  ("abc")
 *    text1="abc",   text2="def"  → 0
 *
 * ============================================================
 */
import java.util.*;

class Problem1_LongestCommonSubsequence {

    // =========================================================
    // APPROACH 1 — SPACE-OPTIMIZED DP (two rows)
    // =========================================================
    /**
     * dp[i][j] = LCS length for text1[0..i-1] and text2[0..j-1]
     *
     * TRANSITION:
     *   If text1[i-1] == text2[j-1]: dp[i][j] = dp[i-1][j-1] + 1  (match: extend LCS)
     *   Else: dp[i][j] = max(dp[i-1][j], dp[i][j-1])               (skip one char)
     *
     * BASE: dp[0][j] = 0, dp[i][0] = 0 (empty string has LCS 0)
     *
     * SPACE OPTIMIZATION: only need previous row → two 1D arrays.
     *
     * VISUAL for text1="abcde", text2="ace":
     *        "" a  c  e
     *    ""   0  0  0  0
     *    a    0  1  1  1
     *    b    0  1  1  1
     *    c    0  1  2  2
     *    d    0  1  2  2
     *    e    0  1  2  3  ← answer: 3
     *
     * Time: O(m × n), Space: O(min(m, n)) — two rows
     *
     * Follow-up: Print the actual LCS string?
     *   Use 2D DP, then backtrack: if chars match → include, else follow larger dp.
     * Follow-up: Longest Common Substring (contiguous)?
     *   dp[i][j] = 0 on mismatch (restart), dp[i][j] = dp[i-1][j-1]+1 on match.
     *   Track global max.
     */
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        // Ensure n is the shorter string for minimal space
        if (m < n) { String tmp = text1; text1 = text2; text2 = tmp; int t = m; m = n; n = t; }

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i-1) == text2.charAt(j-1))
                    curr[j] = prev[j-1] + 1;
                else
                    curr[j] = Math.max(prev[j], curr[j-1]);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            Arrays.fill(curr, 0);
        }

        return prev[n];
    }

    // =========================================================
    // APPROACH 2 — FULL 2D DP + RECONSTRUCT LCS STRING
    // =========================================================
    /**
     * Use full 2D table to reconstruct the actual LCS string.
     * Backtrack from dp[m][n]:
     *   If text1[i-1] == text2[j-1]: include char, move i-- and j--
     *   Else: move toward the larger dp value (i-- or j--)
     */
    public String lcsActual(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i-1) == text2.charAt(j-1))
                    dp[i][j] = dp[i-1][j-1] + 1;
                else
                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
            }
        }

        // Backtrack
        StringBuilder sb = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (text1.charAt(i-1) == text2.charAt(j-1)) {
                sb.append(text1.charAt(i-1));
                i--; j--;
            } else if (dp[i-1][j] > dp[i][j-1]) {
                i--;
            } else {
                j--;
            }
        }
        return sb.reverse().toString();
    }

    // =========================================================
    // APPROACH 3 — SHORTEST COMMON SUPERSEQUENCE (LC 1092)
    // =========================================================
    /**
     * Merge s1 and s2 into the shortest string that has both as subsequences.
     * Length = m + n - LCS(s1, s2).
     *
     * Reconstruct by interleaving s1 and s2 using the LCS as the "shared" backbone.
     */
    public String shortestCommonSupersequence(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = s1.charAt(i-1) == s2.charAt(j-1)
                    ? dp[i-1][j-1] + 1
                    : Math.max(dp[i-1][j], dp[i][j-1]);

        // Reconstruct SCS
        StringBuilder sb = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (s1.charAt(i-1) == s2.charAt(j-1)) {
                sb.append(s1.charAt(i-1));  // shared char: include once
                i--; j--;
            } else if (dp[i-1][j] > dp[i][j-1]) {
                sb.append(s1.charAt(i-1));  // take from s1
                i--;
            } else {
                sb.append(s2.charAt(j-1));  // take from s2
                j--;
            }
        }
        while (i > 0) sb.append(s1.charAt(i-- - 1));  // remaining s1
        while (j > 0) sb.append(s2.charAt(j-- - 1));  // remaining s2
        return sb.reverse().toString();
    }

    // =========================================================
    // BONUS: Longest Common Substring (contiguous)
    // =========================================================
    public int longestCommonSubstring(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int maxLen = 0;
        int[] dp = new int[n + 1];
        int prev = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[j] = prev + 1;
                    maxLen = Math.max(maxLen, dp[j]);
                } else {
                    dp[j] = 0;
                }
                prev = temp;
            }
            prev = 0;
        }
        return maxLen;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_LongestCommonSubsequence sol = new Problem1_LongestCommonSubsequence();

        System.out.println("=== LC 1143: Longest Common Subsequence ===");
        String[][] tests = {
            {"abcde", "ace"},    // 3, "ace"
            {"abc",   "abc"},    // 3, "abc"
            {"abc",   "def"},    // 0, ""
            {"oxcpqrsvwf", "shmtulqrypy"},  // 2, "qr"
        };

        for (String[] t : tests) {
            System.out.println("s1=\"" + t[0] + "\", s2=\"" + t[1] + "\"");
            System.out.println("  LCS length : " + sol.longestCommonSubsequence(t[0], t[1]));
            System.out.println("  LCS string : \"" + sol.lcsActual(t[0], t[1]) + "\"");
            System.out.println("  SCS        : \"" + sol.shortestCommonSupersequence(t[0], t[1]) + "\"");
            System.out.println("  LCSubstring: " + sol.longestCommonSubstring(t[0], t[1]));
            System.out.println();
        }
    }
}
