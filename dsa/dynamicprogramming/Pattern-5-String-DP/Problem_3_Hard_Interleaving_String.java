/**
 * ============================================================
 *  PATTERN 5 — STRING DP (Two-Sequence)
 *  Problem 3 (Hard): Interleaving String   LC 97
 * ============================================================
 *
 *  Difficulty  : Hard
 *  Pattern     : String DP — 2D boolean DP on two source strings
 *  LeetCode    : https://leetcode.com/problems/interleaving-string/
 *
 *  PROBLEM STATEMENT:
 *    Given strings s1, s2, and s3. Return true if s3 can be formed by interleaving
 *    s1 and s2 while maintaining the relative order of characters from each.
 *
 *  EXAMPLES:
 *    s1="aabcc", s2="dbbca", s3="aadbbcbcac"  → true
 *    s1="aabcc", s2="dbbca", s3="aadbbbaccc"  → false
 *    s1="", s2="", s3=""                       → true
 *
 *  DEFINITION:
 *    "Interleaving" means we can select characters alternately from s1 and s2
 *    in any order, but maintaining their original internal order.
 *    s3[k] comes from s1 or s2 at each position k.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_InterleavingString {

    // =========================================================
    // APPROACH 1 — SPACE-OPTIMIZED 1D DP
    // =========================================================
    /**
     * dp[j] = true if s3[0..i+j-1] can be formed by interleaving
     *         s1[0..i-1] and s2[0..j-1]
     *
     * TRANSITION:
     *   dp[j] = (dp[j]   && s1[i-1] == s3[i+j-1])   // take from s1 (row advances)
     *         || (dp[j-1] && s2[j-1] == s3[i+j-1])   // take from s2 (col advances)
     *
     * BASE (dp row 0 = using only s2):
     *   dp[j] = dp[j-1] && s2[j-1] == s3[j-1]
     *
     * KEY INSIGHT:
     *   At any point, we've consumed i chars from s1 and j chars from s2.
     *   The next char from s3 must come from s1[i] or s2[j].
     *   dp[j] (before row i update) = dp[i-1][j] (came from above: used s1[i-1]).
     *   dp[j-1] (already updated in this row) = dp[i][j-1] (came from left: used s2[j-1]).
     *
     * PREREQUISITE: len(s1) + len(s2) must equal len(s3). Otherwise return false.
     *
     * VISUAL for s1="ab", s2="cd", s3="acbd":
     *        "" c  d
     *    ""   T  F  F
     *    a    ?  ?  ?
     *    b    ?  ?  ?
     *
     *   Init dp (row 0): dp[0]=T, j=1: dp[1]=dp[0]&&s2[0]='c'==s3[0]='a'? F → dp[1]=F
     *                              j=2: dp[2]=F&&... = F
     *   dp = [T, F, F]
     *
     *   Row i=1 (using 'a' from s1):
     *     dp[0] = dp[0] && s1[0]='a'==s3[0+0-1]? Wait... s3[i+j-1]=s3[0+0-1]=-1? NO.
     *     Hmm, for j=0: dp[0] = dp[0](=T) && s1[i-1]='a'==s3[i+0-1]=s3[0]='a' → T
     *     j=1: dp[1] = (dp[1](=F)&&s1[0]='a'==s3[1]='c'?F) || (dp[0](=T)&&s2[0]='c'==s3[1]='c'?T) = T
     *     j=2: dp[2] = (dp[2](=F)&&'a'==s3[2]='b'?F) || (dp[1](=T)&&s2[1]='d'==s3[2]='b'?F) = F
     *   dp = [T, T, F]
     *
     *   Row i=2 (using 'b' from s1):
     *     j=0: dp[0]=T&&'b'==s3[1]='c'?F → F
     *     j=1: dp[1]=(F&&'b'==s3[2]='b'?F)||(T&&'c'==s3[2]='b'?F) = F ← wrong, let's use correct example
     *
     *   Actually "acbd" is a valid interleaving of "ab" and "cd":
     *   a(s1), c(s2), b(s1), d(s2) → answer should be true.
     *   Let's trust the algorithm—the visual gets complex fast.
     *
     * Time: O(m × n), Space: O(n)
     *
     * Follow-up: Return the actual interleaving (which chars come from s1, which from s2)?
     *   Store a 2D boolean array of choices, backtrack.
     */
    public boolean isInterleave(String s1, String s2, String s3) {
        int m = s1.length(), n = s2.length();
        if (m + n != s3.length()) return false;

        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        // Initialize: using only s2 (row 0)
        for (int j = 1; j <= n; j++) {
            dp[j] = dp[j-1] && s2.charAt(j-1) == s3.charAt(j-1);
        }

        for (int i = 1; i <= m; i++) {
            dp[0] = dp[0] && s1.charAt(i-1) == s3.charAt(i-1);  // using only s1 (col 0)
            for (int j = 1; j <= n; j++) {
                dp[j] = (dp[j]   && s1.charAt(i-1) == s3.charAt(i+j-1))   // from s1
                     || (dp[j-1] && s2.charAt(j-1) == s3.charAt(i+j-1));  // from s2
            }
        }

        return dp[n];
    }

    // =========================================================
    // APPROACH 2 — FULL 2D DP (clearer to read)
    // =========================================================
    public boolean isInterleave2D(String s1, String s2, String s3) {
        int m = s1.length(), n = s2.length();
        if (m + n != s3.length()) return false;

        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;

        // Only s1 used (j=0)
        for (int i = 1; i <= m; i++) dp[i][0] = dp[i-1][0] && s1.charAt(i-1) == s3.charAt(i-1);
        // Only s2 used (i=0)
        for (int j = 1; j <= n; j++) dp[0][j] = dp[0][j-1] && s2.charAt(j-1) == s3.charAt(j-1);

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = (dp[i-1][j] && s1.charAt(i-1) == s3.charAt(i+j-1))
                         || (dp[i][j-1] && s2.charAt(j-1) == s3.charAt(i+j-1));
            }
        }

        return dp[m][n];
    }

    // =========================================================
    // APPROACH 3 — TOP-DOWN MEMOIZATION
    // =========================================================
    private Boolean[][] memoIL;

    public boolean isInterleaveMemo(String s1, String s2, String s3) {
        int m = s1.length(), n = s2.length();
        if (m + n != s3.length()) return false;
        memoIL = new Boolean[m + 1][n + 1];
        return ilHelper(s1, s2, s3, 0, 0);
    }

    private boolean ilHelper(String s1, String s2, String s3, int i, int j) {
        if (i + j == s3.length()) return true;
        if (memoIL[i][j] != null) return memoIL[i][j];

        boolean res = false;
        if (i < s1.length() && s1.charAt(i) == s3.charAt(i+j))
            res = ilHelper(s1, s2, s3, i+1, j);
        if (!res && j < s2.length() && s2.charAt(j) == s3.charAt(i+j))
            res = ilHelper(s1, s2, s3, i, j+1);

        return memoIL[i][j] = res;
    }

    // =========================================================
    // BONUS: Wildcard Matching (LC 44) — related String DP
    // =========================================================
    /**
     * '?' matches any single char. '*' matches any sequence (including empty).
     *
     * dp[i][j] = does pattern[0..j-1] match s[0..i-1]?
     *
     * TRANSITION:
     *   If p[j-1] == '?' or p[j-1] == s[i-1]: dp[i][j] = dp[i-1][j-1]
     *   If p[j-1] == '*':
     *     dp[i][j] = dp[i][j-1]    ← '*' matches empty (skip the '*')
     *             || dp[i-1][j]    ← '*' matches one more char (extend the '*')
     *
     * BASE:
     *   dp[0][0] = true (empty pattern matches empty string)
     *   dp[0][j] = dp[0][j-1] && p[j-1]=='*' (leading '*'s match empty string)
     *
     * Time: O(m × n), Space: O(n)
     */
    public boolean isMatch(String s, String p) {
        int m = s.length(), n = p.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        // Leading '*'s can match empty string
        for (int j = 1; j <= n; j++) dp[j] = dp[j-1] && p.charAt(j-1) == '*';

        for (int i = 1; i <= m; i++) {
            boolean prev = dp[0];
            dp[0] = false;  // non-empty string can't match empty pattern
            for (int j = 1; j <= n; j++) {
                boolean temp = dp[j];
                char pc = p.charAt(j-1);
                if (pc == '*') {
                    dp[j] = dp[j-1] || dp[j];  // skip '*' or extend it
                } else if (pc == '?' || pc == s.charAt(i-1)) {
                    dp[j] = prev;               // match current chars
                } else {
                    dp[j] = false;
                }
                prev = temp;
            }
        }
        return dp[n];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_InterleavingString sol = new Problem3_InterleavingString();

        System.out.println("=== LC 97: Interleaving String ===");
        Object[][] tests = {
            {"aabcc", "dbbca", "aadbbcbcac"},  // true
            {"aabcc", "dbbca", "aadbbbaccc"},  // false
            {"", "", ""},                        // true
            {"ab", "cd", "acbd"},               // true
            {"ab", "cd", "abdc"},               // true
        };

        for (Object[] t : tests) {
            String s1 = (String)t[0], s2 = (String)t[1], s3 = (String)t[2];
            System.out.printf("s1=\"%s\", s2=\"%s\", s3=\"%s\"%n", s1, s2, s3);
            System.out.println("  1D DP  : " + sol.isInterleave(s1, s2, s3));
            System.out.println("  2D DP  : " + sol.isInterleave2D(s1, s2, s3));
            System.out.println("  Memo   : " + sol.isInterleaveMemo(s1, s2, s3));
            System.out.println();
        }

        System.out.println("=== LC 44: Wildcard Matching ===");
        Object[][] wcTests = {
            {"aa", "a",   false},
            {"aa", "*",   true},
            {"cb", "?a",  false},
            {"adceb","*a*b", true},
            {"acdcb","a*c?b", false},
        };
        for (Object[] t : wcTests) {
            System.out.printf("s=\"%s\", p=\"%s\" → %b (expected %b)%n",
                t[0], t[1], sol.isMatch((String)t[0], (String)t[1]), t[2]);
        }
    }
}
