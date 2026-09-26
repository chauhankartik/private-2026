/**
 * ============================================================
 *  DYNAMIC PROGRAMMING — MEDIUM PROBLEMS
 * ============================================================
 *
 *  M1.  Coin Change                               LC 322
 *  M2.  Longest Increasing Subsequence            LC 300
 *  M3.  Word Break                                LC 139
 *  M4.  Decode Ways                               LC 91
 *  M5.  Unique Paths                              LC 62
 *  M6.  Partition Equal Subset Sum                 LC 416
 *  M7.  House Robber II                           LC 213
 *  M8.  Longest Palindromic Substring             LC 5
 *  M9.  Jump Game II                              LC 45
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class DPMedium {

    // =========================================================
    // M1. Coin Change  LC 322
    // Pattern: UNBOUNDED KNAPSACK (min count to reach target)
    // =========================================================
    /**
     * Given coins[] and amount, find MINIMUM number of coins to make that amount.
     * Each coin can be used unlimited times (unbounded knapsack).
     *
     * dp[i] = minimum coins needed to make amount i
     * dp[0] = 0 (0 coins for amount 0)
     * dp[i] = min over all coins c: dp[i - c] + 1
     *
     * WHY GREEDY FAILS:
     *   coins = [1, 3, 4], amount = 6
     *   Greedy picks 4 → 1 → 1 (3 coins). Optimal: 3 + 3 (2 coins).
     *   Greedy chooses locally largest coin but misses better combinations.
     *
     * WHY THIS TRANSITION WORKS:
     *   For each coin c, if we use one c, we need dp[i-c] more coins for the rest.
     *   Try all coins, take the minimum.
     *
     * Time:  O(amount × coins.length)
     * Space: O(amount)
     *
     * Follow-up: Return the actual coins used?
     *   Track which coin was chosen at each amount, then backtrack.
     * Follow-up: Count total NUMBER of ways (Coin Change II, LC 518)?
     *   dp[i] += dp[i - c] (sum instead of min).
     *   Iterate coins OUTER, amount INNER to avoid counting permutations.
     */
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);  // "infinity" (any valid answer ≤ amount)
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int c : coins) {
                if (c <= i) {
                    dp[i] = Math.min(dp[i], dp[i - c] + 1);
                }
            }
        }

        return dp[amount] > amount ? -1 : dp[amount];
    }

    // =========================================================
    // M2. Longest Increasing Subsequence  LC 300
    // Pattern: LINEAR DP + BINARY SEARCH optimization
    // =========================================================
    /**
     * Find the length of the longest strictly increasing subsequence.
     *
     * --- DP approach: O(n²) ---
     * dp[i] = length of LIS ending at index i
     * dp[i] = max(dp[j] + 1) for all j < i where nums[j] < nums[i]
     * Answer: max(dp[0..n-1])
     *
     * --- Binary Search approach: O(n log n) ---
     * Maintain a "tails" array where tails[k] = smallest tail element
     * for an increasing subsequence of length k+1.
     *
     * For each num:
     *   Binary search for the leftmost position in tails ≥ num.
     *   If found: replace tails[pos] = num (we found a smaller tail).
     *   If not:   append num (extends the longest subsequence).
     *
     * WHY TAILS STAYS SORTED:
     *   We always replace with equal-or-smaller values, never break monotonicity.
     *
     * The length of tails is the answer (but tails itself is NOT the LIS!).
     *
     * Time:  O(n log n)
     * Space: O(n)
     *
     * Follow-up: Print the actual LIS?
     *   Store (value, position_in_tails) and reconstruct backwards.
     * Follow-up: Number of longest increasing subsequences (LC 673)?
     *   Track both dp[i] (length) and count[i] (number of LIS ending at i).
     */

    // O(n²) DP — clear and readable
    public int lengthOfLISDP(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);  // each element is an LIS of length 1
        int maxLen = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }

        return maxLen;
    }

    // O(n log n) Binary Search — optimal
    public int lengthOfLIS(int[] nums) {
        List<Integer> tails = new ArrayList<>();

        for (int num : nums) {
            int pos = Collections.binarySearch(tails, num);
            if (pos < 0) pos = -(pos + 1);  // insertion point

            if (pos == tails.size()) {
                tails.add(num);    // extends longest subsequence
            } else {
                tails.set(pos, num); // replace with smaller tail
            }
        }

        return tails.size();
    }

    // =========================================================
    // M3. Word Break  LC 139
    // Pattern: LINEAR DP with substring matching
    // =========================================================
    /**
     * Given a string s and a dictionary wordDict, return true if s can be
     * segmented into a space-separated sequence of dictionary words.
     *
     * dp[i] = true if s[0..i-1] can be segmented
     * dp[0] = true (empty string is valid)
     * dp[i] = true if any j < i where dp[j] == true AND s[j..i-1] ∈ dict
     *
     * WHY START FROM j, NOT FROM i:
     *   We check all possible LAST words. If we can reach position j (dp[j]=true)
     *   and the substring from j to i is a valid word, then dp[i] = true.
     *
     * Optimization: instead of checking all j, only check j where i-j ≤ maxWordLen.
     *
     * Time:  O(n² × k)  where k = max word length (for substring + hash lookup)
     * Space: O(n)
     *
     * Follow-up: Return ALL possible segmentations (Word Break II, LC 140)?
     *   Use backtracking + memoization.
     * Follow-up: What if words can be used at most once?
     *   Track which words have been used (much harder, different problem).
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
                    break;  // no need to check further j values
                }
            }
        }

        return dp[n];
    }

    // =========================================================
    // M4. Decode Ways  LC 91
    // Pattern: LINEAR DP (single digit or two digits)
    // =========================================================
    /**
     * 'A' → "1", 'B' → "2", ..., 'Z' → "26".
     * Given a digit string, count the number of ways to decode it.
     *
     * dp[i] = number of ways to decode s[0..i-1]
     *
     * Transition:
     *   If s[i-1] != '0': dp[i] += dp[i-1]  (single digit decode: s[i-1] → letter)
     *   If s[i-2..i-1] forms a valid number in [10,26]: dp[i] += dp[i-2]
     *
     * Base: dp[0] = 1 (empty string: one way to decode nothing)
     *
     * TRICKY PART: '0' cannot be decoded alone.
     *   "06" is invalid (leading zero). "10" is valid (J).
     *   If s[i-1] == '0' AND s[i-2..i-1] is NOT in [10,20], there's no way.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Decode Ways II (LC 639) with '*' wildcard?
     *   '*' represents 1-9. Handle all cases with modular arithmetic.
     */
    public int numDecodings(String s) {
        if (s.charAt(0) == '0') return 0;
        int n = s.length();
        int prev2 = 1, prev1 = 1;  // dp[0] = 1, dp[1] = 1 (if s[0]!='0')

        for (int i = 2; i <= n; i++) {
            int curr = 0;
            int oneDigit = s.charAt(i - 1) - '0';
            int twoDigits = Integer.parseInt(s.substring(i - 2, i));

            if (oneDigit >= 1) curr += prev1;       // single digit valid
            if (twoDigits >= 10 && twoDigits <= 26) curr += prev2;  // two digits valid

            prev2 = prev1;
            prev1 = curr;
        }

        return prev1;
    }

    // =========================================================
    // M5. Unique Paths  LC 62
    // Pattern: GRID DP (2D)
    // =========================================================
    /**
     * Robot at top-left of m×n grid. Can only move right or down.
     * Count paths to bottom-right.
     *
     * dp[i][j] = number of paths to reach cell (i, j)
     * dp[i][j] = dp[i-1][j] + dp[i][j-1]  (come from above or left)
     * dp[0][j] = 1 for all j (only one way along top row)
     * dp[i][0] = 1 for all i (only one way along left column)
     *
     * Space optimization: use 1D array, process row by row.
     *   dp[j] += dp[j-1]  (dp[j] is "from above", dp[j-1] is "from left")
     *
     * Mathematical solution: C(m+n-2, m-1)
     *   We need exactly (m-1) down moves and (n-1) right moves.
     *   Choose positions for (m-1) down moves out of (m+n-2) total moves.
     *
     * Time:  O(m × n) DP, O(min(m,n)) math
     * Space: O(n) DP, O(1) math
     *
     * Follow-up: Unique Paths II (LC 63) with obstacles?
     *   If grid[i][j] is obstacle: dp[i][j] = 0.
     */
    public int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1);  // first row: all 1s

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1];  // dp[j] = from_above + from_left
            }
        }

        return dp[n - 1];
    }

    // Math solution — C(m+n-2, m-1) with overflow-safe computation
    public int uniquePathsMath(int m, int n) {
        long result = 1;
        for (int i = 1; i < m; i++) {
            result = result * (n - 1 + i) / i;
        }
        return (int) result;
    }

    // =========================================================
    // M6. Partition Equal Subset Sum  LC 416
    // Pattern: 0/1 KNAPSACK (subset sum variant)
    // =========================================================
    /**
     * Can the array be partitioned into two subsets with equal sum?
     *
     * Equivalent to: does a subset exist with sum = totalSum / 2?
     * If totalSum is odd → impossible.
     *
     * dp[j] = true if we can form sum j using some subset of elements
     * dp[0] = true (empty subset has sum 0)
     *
     * For each num, iterate j from target DOWN to num:
     *   dp[j] = dp[j] || dp[j - num]
     *
     * WHY ITERATE BACKWARDS (0/1 Knapsack):
     *   Forward iteration would use the same element twice.
     *   Backward ensures each element is used at most once.
     *
     * Time:  O(n × sum)
     * Space: O(sum)
     *
     * Follow-up: Partition into two subsets minimizing abs difference?
     *   Find the largest achievable sum ≤ totalSum/2 using the same DP.
     *   Answer = totalSum - 2 * bestSum.
     */
    public boolean canPartition(int[] nums) {
        int total = 0;
        for (int num : nums) total += num;
        if (total % 2 != 0) return false;

        int target = total / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;

        for (int num : nums) {
            for (int j = target; j >= num; j--) {  // BACKWARDS for 0/1 knapsack
                dp[j] = dp[j] || dp[j - num];
            }
        }

        return dp[target];
    }

    // =========================================================
    // M7. House Robber II (Circular)  LC 213
    // Pattern: LINEAR DP + CIRCULAR DECOMPOSITION
    // =========================================================
    /**
     * Same as House Robber I, but houses are in a circle.
     * First and last houses are adjacent.
     *
     * KEY INSIGHT: We can't rob BOTH house 0 and house n-1.
     * So the answer is max of:
     *   - Rob houses [0..n-2] (exclude last)
     *   - Rob houses [1..n-1] (exclude first)
     *
     * Each subproblem is standard House Robber I.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Houses in a binary tree (House Robber III, LC 337)?
     *   DFS returning (rob_this, skip_this) at each node.
     */
    public int robII(int[] nums) {
        if (nums.length == 1) return nums[0];
        return Math.max(
            robRange(nums, 0, nums.length - 2),
            robRange(nums, 1, nums.length - 1)
        );
    }

    private int robRange(int[] nums, int lo, int hi) {
        int prev2 = 0, prev1 = 0;
        for (int i = lo; i <= hi; i++) {
            int curr = Math.max(prev1, prev2 + nums[i]);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // =========================================================
    // M8. Longest Palindromic Substring  LC 5
    // Pattern: INTERVAL DP / EXPAND AROUND CENTER
    // =========================================================
    /**
     * Find the longest substring that is a palindrome.
     *
     * --- DP approach: O(n²) time, O(n²) space ---
     * dp[i][j] = true if s[i..j] is a palindrome
     * dp[i][i] = true (single char)
     * dp[i][i+1] = (s[i] == s[i+1]) (two chars)
     * dp[i][j] = dp[i+1][j-1] && s[i] == s[j] (general case)
     * Iterate by length: len = 3, 4, ..., n
     *
     * --- Expand Around Center: O(n²) time, O(1) space ---
     * For each center (2n-1 centers: n single, n-1 between pairs):
     *   Expand outward while s[left] == s[right].
     *
     * WHY 2n-1 CENTERS:
     *   Odd-length palindrome: center is a single character (n centers).
     *   Even-length palindrome: center is between two characters (n-1 centers).
     *
     * Time:  O(n²)
     * Space: O(1) with expand-around-center
     *
     * Follow-up: Longest palindromic subsequence (LC 516)?
     *   2D DP: dp[i][j] = length of longest palindromic subseq in s[i..j].
     * Follow-up: Manacher's algorithm for O(n)?
     *   Exploits palindrome symmetry to avoid redundant expansions.
     */
    public String longestPalindrome(String s) {
        int n = s.length();
        int start = 0, maxLen = 1;

        for (int center = 0; center < n; center++) {
            // Odd-length palindromes (center = single char)
            int len1 = expandAroundCenter(s, center, center);
            // Even-length palindromes (center = between two chars)
            int len2 = expandAroundCenter(s, center, center + 1);

            int len = Math.max(len1, len2);
            if (len > maxLen) {
                maxLen = len;
                start = center - (len - 1) / 2;
            }
        }

        return s.substring(start, start + maxLen);
    }

    private int expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return right - left - 1;  // length of palindrome
    }

    // =========================================================
    // M9. Jump Game II  LC 45
    // Pattern: GREEDY (optimal) / DP (cleaner to understand)
    // =========================================================
    /**
     * Each element is the max jump length from that position.
     * Find minimum jumps to reach the last index (guaranteed reachable).
     *
     * --- DP approach: O(n²) ---
     * dp[i] = minimum jumps to reach index i
     * dp[0] = 0
     * For each i, check all j < i where j + nums[j] >= i:
     *   dp[i] = min(dp[i], dp[j] + 1)
     *
     * --- Greedy approach: O(n) ---
     * Think of it as BFS on a 1D graph.
     * Each "level" = all positions reachable in k jumps.
     * Track current level's farthest reach and the overall farthest.
     *
     * Variables:
     *   jumps = 0 (number of jumps used)
     *   currEnd = 0 (farthest reachable with 'jumps' jumps)
     *   farthest = 0 (farthest reachable with 'jumps + 1' jumps)
     *
     * For each i from 0 to n-2:
     *   farthest = max(farthest, i + nums[i])
     *   if i == currEnd:   // reached the end of current level
     *     jumps++
     *     currEnd = farthest
     *
     * WHY WE DON'T CHECK i == n-1:
     *   We only need to jump BEFORE reaching the end. If currEnd ≥ n-1 at
     *   some point, the answer is jumps (we've already counted that jump).
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: Jump Game I (LC 55) — can you reach the end?
     *   Same greedy: track farthest. If farthest >= n-1 → true.
     * Follow-up: Jump Game III (LC 1306) — can jump to i+arr[i] or i-arr[i]?
     *   BFS/DFS from starting index.
     */
    public int jump(int[] nums) {
        int jumps = 0, currEnd = 0, farthest = 0;

        for (int i = 0; i < nums.length - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);
            if (i == currEnd) {
                jumps++;
                currEnd = farthest;
            }
        }

        return jumps;
    }

    // DP version for clarity
    public int jumpDP(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (j + nums[j] >= i) {
                    dp[i] = Math.min(dp[i], dp[j] + 1);
                }
            }
        }

        return dp[n - 1];
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        DPMedium sol = new DPMedium();

        System.out.println("═══ M1: Coin Change ═══");
        System.out.println(sol.coinChange(new int[]{1, 5, 11}, 15));  // 3 (5+5+5)
        System.out.println(sol.coinChange(new int[]{2}, 3));          // -1

        System.out.println("\n═══ M2: Longest Increasing Subsequence ═══");
        System.out.println(sol.lengthOfLIS(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));  // 4
        System.out.println(sol.lengthOfLISDP(new int[]{0, 1, 0, 3, 2, 3}));          // 4

        System.out.println("\n═══ M3: Word Break ═══");
        System.out.println(sol.wordBreak("leetcode", Arrays.asList("leet", "code")));         // true
        System.out.println(sol.wordBreak("applepenapple", Arrays.asList("apple", "pen")));   // true
        System.out.println(sol.wordBreak("catsandog", Arrays.asList("cats","dog","sand","and","cat"))); // false

        System.out.println("\n═══ M4: Decode Ways ═══");
        System.out.println(sol.numDecodings("12"));    // 2 ("AB" or "L")
        System.out.println(sol.numDecodings("226"));   // 3 ("BZ","VF","BBF")
        System.out.println(sol.numDecodings("06"));    // 0

        System.out.println("\n═══ M5: Unique Paths ═══");
        System.out.println(sol.uniquePaths(3, 7));     // 28
        System.out.println(sol.uniquePathsMath(3, 7)); // 28

        System.out.println("\n═══ M6: Partition Equal Subset Sum ═══");
        System.out.println(sol.canPartition(new int[]{1, 5, 11, 5}));  // true (1+5+5=11)
        System.out.println(sol.canPartition(new int[]{1, 2, 3, 5}));   // false

        System.out.println("\n═══ M7: House Robber II ═══");
        System.out.println(sol.robII(new int[]{2, 3, 2}));     // 3
        System.out.println(sol.robII(new int[]{1, 2, 3, 1}));  // 4

        System.out.println("\n═══ M8: Longest Palindromic Substring ═══");
        System.out.println(sol.longestPalindrome("babad"));   // "bab" or "aba"
        System.out.println(sol.longestPalindrome("cbbd"));    // "bb"

        System.out.println("\n═══ M9: Jump Game II ═══");
        System.out.println(sol.jump(new int[]{2, 3, 1, 1, 4}));    // 2
        System.out.println(sol.jumpDP(new int[]{2, 3, 0, 1, 4}));  // 2
    }
}
