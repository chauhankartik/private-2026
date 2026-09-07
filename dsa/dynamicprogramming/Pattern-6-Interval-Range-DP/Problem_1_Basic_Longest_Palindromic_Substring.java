/**
 * ============================================================
 *  PATTERN 6 — INTERVAL / RANGE DP
 *  Problem 1 (Basic): Longest Palindromic Substring   LC 5
 * ============================================================
 *
 *  Difficulty  : Medium-Easy (classified "Basic" for pattern entry)
 *  Pattern     : Interval DP  (and Expand-Around-Center variant)
 *  LeetCode    : https://leetcode.com/problems/longest-palindromic-substring/
 *
 *  PROBLEM STATEMENT:
 *    Given a string s, return the longest palindromic substring in s.
 *
 *  EXAMPLES:
 *    "babad"  → "bab" (or "aba")
 *    "cbbd"   → "bb"
 *    "a"      → "a"
 *    "racecar"→ "racecar"
 *
 *  KEY LEARNING:
 *    This problem can be solved two ways, teaching both Interval DP
 *    and the superior Expand-Around-Center O(n²) approach.
 *    Knowing BOTH is expected at FAANG level.
 * ============================================================
 */
import java.util.*;

class Problem1_LongestPalindromicSubstring {

    // =========================================================
    // APPROACH 1 — INTERVAL DP
    // =========================================================
    /**
     * BRUTE FORCE: Check all O(n²) substrings for palindrome → O(n³).
     *
     * INTERVAL DP APPROACH:
     *   State: isPalin[i][j] = true if s[i..j] is a palindrome
     *
     *   Transition:
     *     isPalin[i][j] = (s[i] == s[j]) && isPalin[i+1][j-1]
     *
     *   Base cases:
     *     isPalin[i][i]   = true   (single char is always palindrome)
     *     isPalin[i][i+1] = (s[i] == s[i+1])   (two chars)
     *
     *   Iteration order: by increasing length (short to long)
     *     Lengths 1 → 2 → 3 → ... → n
     *
     *   VISUAL for "babad":
     *     len=1: all [i][i] = true
     *     len=2: [0][1]='b'=='a'? false, [1][2]='a'=='b'? false,
     *            [2][3]='b'=='a'? false, [3][4]='a'=='d'? false
     *     len=3: [0][2]='b'=='b'? true & isPalin[1][1]=true → TRUE → "bab"
     *            [1][3]='a'=='a'? true & isPalin[2][2]=true → TRUE → "aba"
     *            [2][4]='b'=='d'? false
     *     → Longest = "bab" (or "aba")
     *
     * Time:  O(n²) — fill n² cells
     * Space: O(n²) — the isPalin table
     *
     * Follow-up: Palindrome Partitioning II (LC 132) reuses this table.
     * Follow-up: Count all palindromic substrings (LC 647)?
     *   Count the number of true cells in isPalin table.
     */
    public String longestPalindromeDP(String s) {
        int n = s.length();
        boolean[][] isPalin = new boolean[n][n];

        int start = 0, maxLen = 1;

        // Base case: single characters
        for (int i = 0; i < n; i++) isPalin[i][i] = true;

        // Base case: two characters
        for (int i = 0; i < n - 1; i++) {
            if (s.charAt(i) == s.charAt(i + 1)) {
                isPalin[i][i + 1] = true;
                start = i;
                maxLen = 2;
            }
        }

        // Fill by increasing length (3 to n)
        for (int len = 3; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                // s[i..j] is palindrome if ends match AND inner is palindrome
                if (s.charAt(i) == s.charAt(j) && isPalin[i + 1][j - 1]) {
                    isPalin[i][j] = true;
                    if (len > maxLen) {
                        start = i;
                        maxLen = len;
                    }
                }
            }
        }

        return s.substring(start, start + maxLen);
    }

    // =========================================================
    // APPROACH 2 — EXPAND AROUND CENTER (Preferred / Optimal)
    // =========================================================
    /**
     * KEY INSIGHT: Every palindrome has a center.
     *   - Odd-length palindromes: center is a single character.
     *   - Even-length palindromes: center is between two characters.
     *   → There are 2n - 1 possible centers (n singles + n-1 between-pairs).
     *
     * Algorithm:
     *   For each center, expand outward while s[lo] == s[hi].
     *   Track the expansion that yields the longest palindrome.
     *
     * VISUAL for "racecar":
     *   Center at index 3 ('e'):
     *     expand: r(3-1),'e','e' → 'c','e','c' match!
     *             → 'a','c','e','c','a' match!
     *             → 'r','a','c','e','c','a','r' match!
     *     → Length 7, the full string!
     *
     * Time:  O(n²) — each center can expand O(n) times, n centers
     * Space: O(1) — only a few integer variables
     *
     * Follow-up: Manacher's Algorithm?
     *   O(n) palindrome detection using the "mirror" property of palindromes.
     *   Rarely asked in interviews but shows mastery.
     */
    public String longestPalindromeExpand(String s) {
        if (s == null || s.length() < 2) return s;

        int start = 0, end = 0;

        for (int i = 0; i < s.length(); i++) {
            int odd  = expandAroundCenter(s, i, i);     // odd-length palindromes
            int even = expandAroundCenter(s, i, i + 1); // even-length palindromes
            int len  = Math.max(odd, even);

            if (len > end - start + 1) {
                // Compute the new start/end from center i and length len
                start = i - (len - 1) / 2;
                end   = i + len / 2;
            }
        }

        return s.substring(start, end + 1);
    }

    /**
     * Expand outward from center (lo, hi) while characters match.
     * Returns the length of the palindrome found.
     */
    private int expandAroundCenter(String s, int lo, int hi) {
        while (lo >= 0 && hi < s.length() && s.charAt(lo) == s.charAt(hi)) {
            lo--;
            hi++;
        }
        // After the loop: s[lo+1..hi-1] is the palindrome
        return hi - lo - 1;  // length
    }

    // =========================================================
    // APPROACH 3 — BRUTE FORCE (for comparison)
    // =========================================================
    /**
     * Check every substring O(n²) and verify palindrome O(n) each → O(n³).
     * Useful as a brute-force baseline to validate other approaches.
     */
    public String longestPalindromeBrute(String s) {
        int n = s.length();
        String result = s.substring(0, 1);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (isPalindrome(s, i, j) && j - i + 1 > result.length()) {
                    result = s.substring(i, j + 1);
                }
            }
        }
        return result;
    }

    private boolean isPalindrome(String s, int lo, int hi) {
        while (lo < hi) {
            if (s.charAt(lo++) != s.charAt(hi--)) return false;
        }
        return true;
    }

    // =========================================================
    // BONUS: Count Palindromic Substrings (LC 647)
    // =========================================================
    /**
     * Count all distinct palindromic substrings (by position, not value).
     * Reuses expand-around-center; each expansion gives one palindrome.
     *
     * Time: O(n²), Space: O(1)
     */
    public int countSubstrings(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            count += countExpand(s, i, i);       // odd-length
            count += countExpand(s, i, i + 1);   // even-length
        }
        return count;
    }

    private int countExpand(String s, int lo, int hi) {
        int count = 0;
        while (lo >= 0 && hi < s.length() && s.charAt(lo--) == s.charAt(hi++))
            count++;
        return count;
    }

    // =========================================================
    // Main: run all approaches
    // =========================================================
    public static void main(String[] args) {
        Problem1_LongestPalindromicSubstring sol = new Problem1_LongestPalindromicSubstring();

        String[] tests = {"babad", "cbbd", "a", "ac", "racecar", "abcba"};

        for (String test : tests) {
            System.out.println("Input: \"" + test + "\"");
            System.out.println("  DP expand   : \"" + sol.longestPalindromeDP(test) + "\"");
            System.out.println("  Expand ctr  : \"" + sol.longestPalindromeExpand(test) + "\"");
            System.out.println("  Brute force : \"" + sol.longestPalindromeBrute(test) + "\"");
            System.out.println("  Count substrings: " + sol.countSubstrings(test));
            System.out.println();
        }
    }
}
