/**
 * ============================================================
 *  DYNAMIC PROGRAMMING — HARD PROBLEMS
 * ============================================================
 *
 *  H1.  Edit Distance                             LC 72
 *  H2.  Burst Balloons                            LC 312
 *  H3.  Palindrome Partitioning II                LC 132
 *  H4.  Longest Common Subsequence                LC 1143
 *  H5.  Maximal Square                            LC 221
 *  H6.  Interleaving String                       LC 97
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class DPHard {

    // =========================================================
    // H1. Edit Distance (Levenshtein Distance)  LC 72
    // Pattern: STRING DP (2D: two strings)
    // =========================================================
    /**
     * Minimum operations to convert word1 → word2.
     * Operations: insert, delete, replace (each costs 1).
     *
     * dp[i][j] = edit distance between word1[0..i-1] and word2[0..j-1]
     *
     * Base cases:
     *   dp[0][j] = j  (insert j characters to match word2[0..j-1])
     *   dp[i][0] = i  (delete i characters from word1[0..i-1])
     *
     * Transition:
     *   If word1[i-1] == word2[j-1]: dp[i][j] = dp[i-1][j-1]  (no operation)
     *   Else: dp[i][j] = 1 + min(
     *     dp[i-1][j-1],  ← REPLACE word1[i-1] with word2[j-1]
     *     dp[i-1][j],    ← DELETE  word1[i-1]
     *     dp[i][j-1]     ← INSERT  word2[j-1] after word1[i-1]
     *   )
     *
     * VISUAL:
     *     ""  r  o  s
     *  ""  0  1  2  3
     *  h   1  1  2  3
     *  o   2  2  1  2
     *  r   3  2  2  2
     *  s   4  3  3  2
     *  e   5  4  4  3    ← answer: 3
     *
     * Space-optimized: two 1D arrays (previous row + current row).
     *
     * Time:  O(m × n)
     * Space: O(min(m, n)) with optimization
     *
     * Follow-up: If operations have different costs (insert=a, delete=b, replace=c)?
     *   Replace constants in the min expression.
     * Follow-up: One Edit Distance (LC 161)?
     *   Check if edit distance is exactly 1 — O(n) without full DP.
     */
    public int minDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();

        // Space-optimized: use two rows
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        // Base case: dp[0][j] = j
        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;  // dp[i][0] = i
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(prev[j - 1],       // replace
                                  Math.min(prev[j],            // delete
                                           curr[j - 1]));      // insert
                }
            }
            // Swap rows
            int[] tmp = prev; prev = curr; curr = tmp;
        }

        return prev[n];
    }

    // Full 2D version for clarity
    public int minDistanceFull(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                  Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[m][n];
    }

    // =========================================================
    // H2. Burst Balloons  LC 312
    // Pattern: INTERVAL DP (reverse thinking: last balloon burst)
    // =========================================================
    /**
     * n balloons with values nums[i]. Burst balloon i → get
     * nums[left] × nums[i] × nums[right] coins.
     * After burst, left and right become adjacent.
     * Maximize total coins.
     *
     * KEY INSIGHT — REVERSE THINKING:
     *   Instead of which balloon to burst FIRST, think about which to burst LAST.
     *   If balloon k is the LAST to burst in range [i..j]:
     *     - All others in [i..j] are already burst.
     *     - Neighbors of k are now nums[i-1] and nums[j+1] (the boundaries).
     *     - Coins from bursting k last: nums[i-1] × nums[k] × nums[j+1]
     *     - Plus: best coins from [i..k-1] and [k+1..j]
     *
     * dp[i][j] = max coins from bursting all balloons in range [i..j]
     * dp[i][j] = max over k ∈ [i,j] of:
     *   dp[i][k-1] + nums[i-1] × nums[k] × nums[j+1] + dp[k+1][j]
     *
     * Pad with 1s: nums = [1, ...original..., 1]
     * Iterate by range length (short to long).
     *
     * WHY "LAST BURST" WORKS:
     *   When k is the last balloon, the subproblems [i..k-1] and [k+1..j]
     *   are INDEPENDENT — their boundary conditions don't change.
     *   If we think "first burst", the subproblems overlap (boundaries shift).
     *
     * Time:  O(n³)
     * Space: O(n²)
     *
     * Follow-up: Print the optimal burst order?
     *   Store the choice of k at each (i,j), reconstruct recursively.
     */
    public int maxCoins(int[] nums) {
        int n = nums.length;
        // Pad: [1, nums[0], nums[1], ..., nums[n-1], 1]
        int[] padded = new int[n + 2];
        padded[0] = padded[n + 1] = 1;
        for (int i = 0; i < n; i++) padded[i + 1] = nums[i];

        int[][] dp = new int[n + 2][n + 2];

        // Iterate by range length
        for (int len = 1; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    int coins = padded[i - 1] * padded[k] * padded[j + 1]
                              + dp[i][k - 1] + dp[k + 1][j];
                    dp[i][j] = Math.max(dp[i][j], coins);
                }
            }
        }

        return dp[1][n];
    }

    // =========================================================
    // H3. Palindrome Partitioning II  LC 132
    // Pattern: LINEAR DP + PALINDROME PRECOMPUTATION
    // =========================================================
    /**
     * Minimum cuts to partition a string into palindromic substrings.
     *
     * Brute: try all partitions — O(2^n).
     *
     * dp[i] = minimum cuts for s[0..i]
     * dp[i] = min over all j ≤ i where s[j..i] is palindrome:
     *   dp[j - 1] + 1  (cut before j, then s[j..i] is one palindrome)
     *
     * Base: dp[-1] = -1 (so that a full palindrome gives 0 cuts)
     *
     * Palindrome check: expand-around-center while updating dp.
     *   For each center, expand outward. If s[j..i] is palindrome:
     *     dp[i] = min(dp[i], dp[j - 1] + 1)
     *
     * This is O(n²) total because each (j, i) pair is visited once.
     *
     * Time:  O(n²)
     * Space: O(n)
     *
     * Follow-up: Palindrome Partitioning I (LC 131) — return all partitions?
     *   Backtracking: try each prefix palindrome, recurse on suffix.
     */
    public int minCut(String s) {
        int n = s.length();
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);

        for (int center = 0; center < n; center++) {
            // Odd-length palindromes
            expandAndUpdate(s, center, center, dp);
            // Even-length palindromes
            expandAndUpdate(s, center, center + 1, dp);
        }

        return dp[n - 1];
    }

    private void expandAndUpdate(String s, int lo, int hi, int[] dp) {
        while (lo >= 0 && hi < s.length() && s.charAt(lo) == s.charAt(hi)) {
            // s[lo..hi] is a palindrome
            dp[hi] = Math.min(dp[hi], (lo == 0 ? 0 : dp[lo - 1] + 1));
            lo--;
            hi++;
        }
    }

    // =========================================================
    // H4. Longest Common Subsequence  LC 1143
    // Pattern: STRING DP (classic 2D)
    // =========================================================
    /**
     * Find the length of the longest subsequence present in both strings.
     *
     * dp[i][j] = LCS length of text1[0..i-1] and text2[0..j-1]
     *
     * Transition:
     *   If text1[i-1] == text2[j-1]: dp[i][j] = dp[i-1][j-1] + 1
     *     (both chars match → extend LCS by 1)
     *   Else: dp[i][j] = max(dp[i-1][j], dp[i][j-1])
     *     (skip one char from either string, take the better option)
     *
     * Base: dp[0][j] = 0, dp[i][0] = 0 (empty string has LCS 0)
     *
     * VISUAL (text1="abcde", text2="ace"):
     *     ""  a  c  e
     *  ""  0  0  0  0
     *  a   0  1  1  1
     *  b   0  1  1  1
     *  c   0  1  2  2
     *  d   0  1  2  2
     *  e   0  1  2  3    ← answer: 3 (subsequence "ace")
     *
     * Time:  O(m × n)
     * Space: O(min(m, n)) with rolling array
     *
     * Follow-up: Print the actual LCS?
     *   Backtrack from dp[m][n]: if chars match, include; else follow larger dp value.
     * Follow-up: Shortest Common Supersequence (LC 1092)?
     *   Length = m + n - LCS. Build by merging with LCS alignment.
     */
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();

        // Space optimization: two rows
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = prev[j - 1] + 1;
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            Arrays.fill(curr, 0);
        }

        return prev[n];
    }

    // Full 2D version + print actual LCS
    public String longestCommonSubsequencePrint(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Backtrack to find the actual LCS
        StringBuilder lcs = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                lcs.append(text1.charAt(i - 1));
                i--; j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        return lcs.reverse().toString();
    }

    // =========================================================
    // H5. Maximal Square  LC 221
    // Pattern: GRID DP (2D, square substructure)
    // =========================================================
    /**
     * Find the largest square containing only 1's in a binary matrix.
     * Return its area.
     *
     * dp[i][j] = side length of the largest square whose BOTTOM-RIGHT corner is (i, j)
     *
     * Transition:
     *   If matrix[i][j] == '0': dp[i][j] = 0
     *   If matrix[i][j] == '1':
     *     dp[i][j] = min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]) + 1
     *
     * WHY MIN OF THREE NEIGHBORS:
     *   The square at (i,j) is limited by the smallest of:
     *     - square ending at (i-1, j)     ← above
     *     - square ending at (i, j-1)     ← left
     *     - square ending at (i-1, j-1)   ← diagonal
     *   If any of them is smaller, the square at (i,j) can't be bigger.
     *
     *   Example:   1 1 1       dp:  1 1 1
     *              1 1 1            1 2 2
     *              1 1 1            1 2 3  ← side=3, area=9
     *
     * Answer: max(dp[i][j])² — side length squared = area.
     *
     * Time:  O(m × n)
     * Space: O(n) with rolling array
     *
     * Follow-up: Maximal Rectangle (LC 85)?
     *   Use histogram approach: for each row, build height histogram,
     *   then apply largest rectangle in histogram (stack-based).
     */
    public int maximalSquare(char[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        int maxSide = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (matrix[i - 1][j - 1] == '1') {
                    curr[j] = Math.min(prev[j - 1],
                              Math.min(prev[j], curr[j - 1])) + 1;
                    maxSide = Math.max(maxSide, curr[j]);
                } else {
                    curr[j] = 0;
                }
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            Arrays.fill(curr, 0);
        }

        return maxSide * maxSide;
    }

    // =========================================================
    // H6. Interleaving String  LC 97
    // Pattern: STRING DP (2D: two pointers advancing)
    // =========================================================
    /**
     * Given s1, s2, s3: can s3 be formed by interleaving s1 and s2?
     * Interleaving = s1 and s2 maintain their relative character order.
     *
     * dp[i][j] = true if s3[0..i+j-1] can be formed by interleaving
     *            s1[0..i-1] and s2[0..j-1]
     *
     * Transition:
     *   dp[i][j] = (dp[i-1][j] && s1[i-1] == s3[i+j-1])   ← take from s1
     *           || (dp[i][j-1] && s2[j-1] == s3[i+j-1])    ← take from s2
     *
     * Base:
     *   dp[0][0] = true
     *   dp[i][0] = dp[i-1][0] && s1[i-1] == s3[i-1] (only using s1)
     *   dp[0][j] = dp[0][j-1] && s2[j-1] == s3[j-1] (only using s2)
     *
     * Prerequisite check: len(s1) + len(s2) must equal len(s3).
     *
     * Time:  O(m × n)
     * Space: O(n) with 1D optimization
     *
     * Follow-up: Return the actual interleaved path?
     *   Backtrack from dp[m][n], recording whether each char came from s1 or s2.
     */
    public boolean isInterleave(String s1, String s2, String s3) {
        int m = s1.length(), n = s2.length();
        if (m + n != s3.length()) return false;

        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        // Base: using only s2
        for (int j = 1; j <= n; j++)
            dp[j] = dp[j - 1] && s2.charAt(j - 1) == s3.charAt(j - 1);

        for (int i = 1; i <= m; i++) {
            dp[0] = dp[0] && s1.charAt(i - 1) == s3.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                dp[j] = (dp[j] && s1.charAt(i - 1) == s3.charAt(i + j - 1))
                      || (dp[j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1));
            }
        }

        return dp[n];
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        DPHard sol = new DPHard();

        System.out.println("═══ H1: Edit Distance ═══");
        System.out.println(sol.minDistance("horse", "ros"));          // 3
        System.out.println(sol.minDistanceFull("intention", "execution")); // 5

        System.out.println("\n═══ H2: Burst Balloons ═══");
        System.out.println(sol.maxCoins(new int[]{3, 1, 5, 8}));  // 167

        System.out.println("\n═══ H3: Palindrome Partitioning II ═══");
        System.out.println(sol.minCut("aab"));    // 1 (["aa","b"])
        System.out.println(sol.minCut("a"));      // 0
        System.out.println(sol.minCut("ab"));     // 1

        System.out.println("\n═══ H4: Longest Common Subsequence ═══");
        System.out.println(sol.longestCommonSubsequence("abcde", "ace"));  // 3
        System.out.println(sol.longestCommonSubsequencePrint("abcde", "ace"));  // "ace"
        System.out.println(sol.longestCommonSubsequence("abc", "def"));    // 0

        System.out.println("\n═══ H5: Maximal Square ═══");
        char[][] matrix = {
            {'1','0','1','0','0'},
            {'1','0','1','1','1'},
            {'1','1','1','1','1'},
            {'1','0','0','1','0'}
        };
        System.out.println(sol.maximalSquare(matrix));  // 4 (2x2 square)

        System.out.println("\n═══ H6: Interleaving String ═══");
        System.out.println(sol.isInterleave("aabcc", "dbbca", "aadbbcbcac")); // true
        System.out.println(sol.isInterleave("aabcc", "dbbca", "aadbbbaccc")); // false
    }
}
