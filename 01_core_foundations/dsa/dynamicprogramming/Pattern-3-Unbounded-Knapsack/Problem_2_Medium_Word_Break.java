/**
 * ============================================================
 *  PATTERN 3 — UNBOUNDED KNAPSACK DP
 *  Problem 2 (Medium): Word Break   LC 139
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Unbounded Knapsack — words as "coins" of varying length
 *  LeetCode    : https://leetcode.com/problems/word-break/
 *
 *  PROBLEM STATEMENT:
 *    Given string s and dictionary wordDict, return true if s can be segmented
 *    into a space-separated sequence of dictionary words.
 *    The same word may be used multiple times.
 *
 *  EXAMPLES:
 *    s="leetcode", wordDict=["leet","code"]  → true
 *    s="applepenapple", wordDict=["apple","pen"]  → true  ("apple pen apple")
 *    s="catsandog", wordDict=["cats","dog","sand","and","cat"]  → false
 *
 *  KEY INSIGHT:
 *    Treat 's' as the "amount" and words as "coins" of varying length.
 *    dp[i] = can we form s[0..i-1] using dictionary words?
 *    For each end position i, try all start positions j: if dp[j] && s[j..i-1] in dict.
 *
 * ============================================================
 */
import java.util.*;

class Problem2_WordBreak {

    // =========================================================
    // APPROACH 1 — BOTTOM-UP DP (optimal)
    // =========================================================
    /**
     * dp[i] = true if s[0..i-1] can be segmented using wordDict.
     *
     * BASE: dp[0] = true (empty string is always valid)
     *
     * TRANSITION:
     *   For each i from 1 to n:
     *     For each j from 0 to i-1:
     *       If dp[j] == true AND s.substring(j, i) ∈ wordDict:
     *         dp[i] = true; break
     *
     * VISUAL for s="leetcode", dict={leet,code}:
     *   dp[0]=T
     *   dp[4]: j=0, dp[0]=T and s[0..3]="leet" ∈ dict → dp[4]=T
     *   dp[8]: j=4, dp[4]=T and s[4..7]="code" ∈ dict → dp[8]=T ✓
     *
     * OPTIMIZATION: only try word lengths that exist in the dictionary.
     *   Instead of j from 0 to i-1, try j = i - word.length() for each word.
     *
     * Time: O(n² × L) where L = max word length for substring check
     *       O(n × total_chars) with HashSet and pre-known lengths
     * Space: O(n + D) — dp array + dictionary set
     *
     * Follow-up: Word Break II (LC 140) — return all valid sentences?
     *   Same DP, but store parent[i] = list of start positions j such that
     *   dp[j]=true and s[j..i-1] ∈ dict. Backtrack to collect all sentences.
     */
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && dict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }

        return dp[n];
    }

    // =========================================================
    // APPROACH 2 — OPTIMIZED: only try valid word lengths
    // =========================================================
    /**
     * Pre-compute the set of word lengths. At each position i,
     * only try endings j = i - wordLen for each valid wordLen.
     * Reduces inner loop from O(n) to O(|wordLengths| × avg comparisons).
     */
    public boolean wordBreakOpt(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        Set<Integer> lengths = new HashSet<>();
        for (String w : wordDict) lengths.add(w.length());

        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        for (int i = 1; i <= n; i++) {
            for (int len : lengths) {
                if (len <= i && dp[i - len] && dict.contains(s.substring(i - len, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }

        return dp[n];
    }

    // =========================================================
    // APPROACH 3 — TOP-DOWN MEMOIZATION
    // =========================================================
    private Boolean[] memoWB;
    private Set<String> dictSet;

    public boolean wordBreakMemo(String s, List<String> wordDict) {
        dictSet = new HashSet<>(wordDict);
        memoWB = new Boolean[s.length()];
        return wbHelper(s, 0);
    }

    /**
     * @return true if s[start..n-1] can be segmented
     */
    private boolean wbHelper(String s, int start) {
        if (start == s.length()) return true;
        if (memoWB[start] != null) return memoWB[start];

        for (int end = start + 1; end <= s.length(); end++) {
            if (dictSet.contains(s.substring(start, end)) && wbHelper(s, end)) {
                return memoWB[start] = true;
            }
        }
        return memoWB[start] = false;
    }

    // =========================================================
    // APPROACH 4 — WORD BREAK II: Return All Sentences (LC 140)
    // =========================================================
    /**
     * Backtracking with memoization.
     * memo2[i] = list of all valid sentences from s[i..n-1]
     *
     * Time: O(n × 2^n) worst case — exponentially many valid sentences.
     * Space: O(n × 2^n) for all sentences stored.
     */
    private Map<Integer, List<String>> memo2;

    public List<String> wordBreakII(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        memo2 = new HashMap<>();
        return breakII(s, 0, dict);
    }

    private List<String> breakII(String s, int start, Set<String> dict) {
        if (memo2.containsKey(start)) return memo2.get(start);

        List<String> result = new ArrayList<>();
        if (start == s.length()) {
            result.add("");
            return result;
        }

        for (int end = start + 1; end <= s.length(); end++) {
            String word = s.substring(start, end);
            if (dict.contains(word)) {
                List<String> rest = breakII(s, end, dict);
                for (String sentence : rest) {
                    result.add(word + (sentence.isEmpty() ? "" : " " + sentence));
                }
            }
        }

        memo2.put(start, result);
        return result;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_WordBreak sol = new Problem2_WordBreak();

        System.out.println("=== LC 139: Word Break ===");
        Object[][] tests = {
            {"leetcode",     List.of("leet","code")},           // true
            {"applepenapple",List.of("apple","pen")},           // true
            {"catsandog",    List.of("cats","dog","sand","and","cat")}, // false
            {"cars",         List.of("car","ca","rs")},          // true
        };

        for (Object[] t : tests) {
            String s = (String) t[0];
            @SuppressWarnings("unchecked")
            List<String> dict = (List<String>) t[1];
            System.out.println("s=\"" + s + "\", dict=" + dict);
            System.out.println("  Bottom-Up  : " + sol.wordBreak(s, dict));
            System.out.println("  Optimized  : " + sol.wordBreakOpt(s, dict));
            System.out.println("  Memo       : " + sol.wordBreakMemo(s, dict));
            System.out.println();
        }

        System.out.println("=== LC 140: Word Break II ===");
        System.out.println(sol.wordBreakII("catsanddog", List.of("cat","cats","and","sand","dog")));
        // ["cat sand dog", "cats and dog"]
        System.out.println(sol.wordBreakII("pineapplepenapple",
            List.of("apple","pen","applepen","pine","pineapple")));
    }
}
