/**
 * ============================================================
 *  DYNAMIC PROGRAMMING — GOOGLE-LEVEL PROBLEMS
 * ============================================================
 *
 *  G1.  Best Time to Buy/Sell Stock with Cooldown  LC 309  ★ Google
 *  G2.  Longest String Chain                       LC 1048 ★ Google
 *  G3.  Target Sum                                 LC 494  ★ Google
 *  G4.  Ones and Zeroes                            LC 474  ★ Google
 *  G5.  Stone Game                                 LC 877  ★ Google
 *
 *  Each problem includes:
 *   - Why Google asks it
 *   - Brute → optimal trace
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class DPGoogleLevel {

    // =========================================================
    // G1. Best Time to Buy/Sell Stock with Cooldown  LC 309
    //     ★ Google — STATE MACHINE DP
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests state machine design — the most elegant DP pattern.
     *   The stock series (I, II, III, IV, cooldown, fee) all use this framework.
     *   Interviewers love seeing candidates derive the state transitions cleanly.
     *
     * Rules: buy/sell any number of times. After selling, must wait 1 day (cooldown).
     *
     * THREE STATES at each day:
     *   hold[i]    = max profit on day i if HOLDING a stock
     *   sold[i]    = max profit on day i if just SOLD (in cooldown tomorrow)
     *   rest[i]    = max profit on day i if RESTING (not holding, not in cooldown)
     *
     * Transitions:
     *   hold[i] = max(hold[i-1],          ← keep holding
     *                 rest[i-1] - price)   ← buy today (can only buy from rest state)
     *
     *   sold[i] = hold[i-1] + price        ← sell today
     *
     *   rest[i] = max(rest[i-1],           ← keep resting
     *                 sold[i-1])            ← was in cooldown, now free
     *
     * Answer: max(sold[n-1], rest[n-1])  — never optimal to end holding
     *
     * STATE MACHINE DIAGRAM:
     *   rest ──(buy)──→ hold ──(sell)──→ sold ──(cooldown)──→ rest
     *    ↻ (rest)         ↻ (hold)
     *
     * Space-optimized: only need previous day's values.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: With transaction fee instead of cooldown (LC 714)?
     *   Two states: hold, cash. hold[i] = max(hold[i-1], cash[i-1] - price).
     *   cash[i] = max(cash[i-1], hold[i-1] + price - fee).
     * Follow-up: At most k transactions (LC 188)?
     *   dp[k][2]: for each of k transactions, track holding/not holding.
     */
    public int maxProfit(int[] prices) {
        if (prices.length <= 1) return 0;

        int hold = -prices[0];  // bought on day 0
        int sold = 0;           // impossible to sell on day 0, but 0 works
        int rest = 0;           // doing nothing

        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold, prevSold = sold, prevRest = rest;
            hold = Math.max(prevHold, prevRest - prices[i]);
            sold = prevHold + prices[i];
            rest = Math.max(prevRest, prevSold);
        }

        return Math.max(sold, rest);
    }

    // =========================================================
    // G2. Longest String Chain  LC 1048  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests combining sorting with DP. The key insight is transforming the
     *   problem from "longest chain" into a variant of LIS using a HashMap.
     *   Google loves problems where the state space is reduced via sorting.
     *
     * A word a is a predecessor of b if we can insert ONE letter into a to get b.
     * Find the longest chain: w1 → w2 → ... → wk where each wi is a predecessor of wi+1.
     *
     * Approach:
     *   1. Sort words by LENGTH (shorter words first).
     *   2. For each word, try removing each character to get a predecessor.
     *   3. dp[word] = 1 + max(dp[predecessor]) if predecessor exists.
     *
     * HashMap stores dp values keyed by word string → O(1) lookup.
     *
     * WHY SORT BY LENGTH:
     *   Ensures we process shorter words before longer ones.
     *   When processing word w, all possible predecessors are already computed.
     *
     * Time:  O(n × L²) where L = max word length (for each word, try L removals,
     *        each creating a string of length L)
     * Space: O(n × L)
     *
     * Follow-up: What if you can insert OR delete one character?
     *   Same idea but check both shorter and longer words as neighbors.
     */
    public int longestStrChain(String[] words) {
        Arrays.sort(words, (a, b) -> a.length() - b.length());
        Map<String, Integer> dp = new HashMap<>();
        int maxChain = 1;

        for (String word : words) {
            int best = 1;
            // Try removing each character
            for (int i = 0; i < word.length(); i++) {
                String pred = word.substring(0, i) + word.substring(i + 1);
                if (dp.containsKey(pred)) {
                    best = Math.max(best, dp.get(pred) + 1);
                }
            }
            dp.put(word, best);
            maxChain = Math.max(maxChain, best);
        }

        return maxChain;
    }

    // =========================================================
    // G3. Target Sum  LC 494  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests the ability to transform a problem into a known pattern.
     *   Most candidates try backtracking (2^n). Google expects the DP insight:
     *   this is a SUBSET SUM problem in disguise.
     *
     * Assign + or - to each number. Count ways to achieve target sum.
     *
     * --- Brute force: O(2^n) backtracking ---
     *   Try + and - for each number recursively.
     *
     * --- Transformation to Subset Sum ---
     *   Let P = set of numbers with +, N = set with -.
     *   P + N = total (all numbers)
     *   P - N = target
     *   → P = (total + target) / 2
     *
     *   Problem becomes: count subsets of nums that sum to P.
     *   This is a 0/1 knapsack counting problem.
     *
     * dp[j] = number of subsets that sum to j
     * dp[0] = 1 (empty subset)
     * For each num: dp[j] += dp[j - num]  (iterate j backwards for 0/1)
     *
     * Edge cases:
     *   - (total + target) must be even and non-negative.
     *   - If not, answer is 0.
     *
     * Time:  O(n × P) where P = (total + target) / 2
     * Space: O(P)
     *
     * Follow-up: What if numbers can be 0?
     *   Each 0 doubles the count (can be + or -, both give 0).
     *   Count zeros separately: answer *= 2^(count of zeros).
     */
    public int findTargetSumWays(int[] nums, int target) {
        int total = 0;
        for (int num : nums) total += num;

        // P = (total + target) / 2 must be integer and non-negative
        if ((total + target) % 2 != 0 || total + target < 0) return 0;
        int subsetSum = (total + target) / 2;

        int[] dp = new int[subsetSum + 1];
        dp[0] = 1;

        for (int num : nums) {
            for (int j = subsetSum; j >= num; j--) {  // 0/1 knapsack: backwards
                dp[j] += dp[j - num];
            }
        }

        return dp[subsetSum];
    }

    // Backtracking version for comparison
    public int findTargetSumWaysBT(int[] nums, int target) {
        return backtrack(nums, 0, target);
    }

    private int backtrack(int[] nums, int idx, int remaining) {
        if (idx == nums.length) return remaining == 0 ? 1 : 0;
        return backtrack(nums, idx + 1, remaining - nums[idx])
             + backtrack(nums, idx + 1, remaining + nums[idx]);
    }

    // =========================================================
    // G4. Ones and Zeroes  LC 474  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   A 2D knapsack problem — two constraints (0s and 1s) instead of one.
     *   Tests ability to extend the classic knapsack to multiple dimensions.
     *
     * Given binary strings, find the max subset size such that the subset
     * contains at most m zeros and n ones total.
     *
     * This is a 0/1 knapsack with TWO capacities: m (zeros) and n (ones).
     *
     * dp[i][j] = max subset size using at most i zeros and j ones
     *
     * For each string:
     *   Count its zeros (z) and ones (o).
     *   For i from m down to z, for j from n down to o:
     *     dp[i][j] = max(dp[i][j], dp[i-z][j-o] + 1)
     *
     * WHY ITERATE BACKWARDS:
     *   0/1 knapsack: each string used at most once.
     *   Forward iteration would count the same string multiple times.
     *
     * Time:  O(len × m × n) where len = number of strings
     * Space: O(m × n)
     *
     * Follow-up: What if each string has a "value" and you maximize total value?
     *   Replace +1 with +value[k] in the transition.
     */
    public int findMaxForm(String[] strs, int m, int n) {
        int[][] dp = new int[m + 1][n + 1];

        for (String s : strs) {
            int zeros = 0, ones = 0;
            for (char c : s.toCharArray()) {
                if (c == '0') zeros++;
                else ones++;
            }

            // 0/1 knapsack: iterate backwards
            for (int i = m; i >= zeros; i--) {
                for (int j = n; j >= ones; j--) {
                    dp[i][j] = Math.max(dp[i][j], dp[i - zeros][j - ones] + 1);
                }
            }
        }

        return dp[m][n];
    }

    // =========================================================
    // G5. Stone Game  LC 877  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests GAME THEORY + INTERVAL DP. Also has a clever mathematical proof
     *   that the first player always wins — but Google wants both solutions.
     *
     * Alice and Bob take turns (Alice first). Each turn, take the leftmost
     * or rightmost stone pile. Both play optimally. Does Alice win?
     *
     * --- Mathematical insight: Alice ALWAYS wins ---
     *   Piles have even length. Color them alternating: BWBW...BW.
     *   Alice can always take all black piles or all white piles.
     *   Since sum(piles) is odd (given), one color-group has more.
     *   Alice picks the better group → always wins.
     *
     * --- Interval DP (general solution for any game) ---
     *   dp[i][j] = max score difference (current player - opponent) for piles[i..j]
     *
     *   Transition:
     *     dp[i][j] = max(
     *       piles[i] - dp[i+1][j],    ← take left pile
     *       piles[j] - dp[i][j-1]     ← take right pile
     *     )
     *   The subtraction handles turn alternation:
     *     my_gain - opponent's_best_score_difference
     *
     *   Base: dp[i][i] = piles[i] (only one pile left, take it)
     *   Answer: dp[0][n-1] > 0 means first player wins.
     *
     * Time:  O(n²) DP, O(1) math
     * Space: O(n²) DP, O(1) math
     *
     * Follow-up: Stone Game II (LC 1140) with variable take count?
     *   dp[i][M] where M limits how many piles you can take.
     * Follow-up: Stone Game III (LC 1406) with 1-3 piles?
     *   dp[i] = max score difference starting at pile i.
     */

    // Mathematical: Alice always wins (only for even-length, distinct piles)
    public boolean stoneGame(int[] piles) {
        return true;  // Alice always wins with optimal play
    }

    // Interval DP: general solution (works for any variant)
    public boolean stoneGameDP(int[] piles) {
        int n = piles.length;
        int[][] dp = new int[n][n];

        // Base: single pile
        for (int i = 0; i < n; i++) dp[i][i] = piles[i];

        // Fill by increasing range length
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                dp[i][j] = Math.max(
                    piles[i] - dp[i + 1][j],   // take left
                    piles[j] - dp[i][j - 1]    // take right
                );
            }
        }

        return dp[0][n - 1] > 0;
    }

    // Space-optimized 1D DP (since each dp[i][j] depends on dp[i+1][j] and dp[i][j-1])
    public boolean stoneGameOpt(int[] piles) {
        int n = piles.length;
        int[] dp = Arrays.copyOf(piles, n);

        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                dp[i] = Math.max(piles[i] - dp[i + 1], piles[j] - dp[i]);
            }
        }

        return dp[0] > 0;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        DPGoogleLevel sol = new DPGoogleLevel();

        System.out.println("═══ G1: Stock with Cooldown ═══");
        System.out.println(sol.maxProfit(new int[]{1, 2, 3, 0, 2})); // 3 (buy@1, sell@3, cd, buy@0, sell@2)
        System.out.println(sol.maxProfit(new int[]{1}));             // 0

        System.out.println("\n═══ G2: Longest String Chain ═══");
        System.out.println(sol.longestStrChain(
            new String[]{"a", "b", "ba", "bca", "bda", "bdca"})); // 4 (a→ba→bda→bdca)
        System.out.println(sol.longestStrChain(
            new String[]{"xbc", "pcxbcf", "xb", "cxbc", "pcxbc"})); // 5

        System.out.println("\n═══ G3: Target Sum ═══");
        System.out.println(sol.findTargetSumWays(new int[]{1, 1, 1, 1, 1}, 3)); // 5
        System.out.println(sol.findTargetSumWaysBT(new int[]{1, 1, 1, 1, 1}, 3)); // 5 (backtrack)
        System.out.println(sol.findTargetSumWays(new int[]{1}, 1));               // 1

        System.out.println("\n═══ G4: Ones and Zeroes ═══");
        System.out.println(sol.findMaxForm(
            new String[]{"10", "0001", "111001", "1", "0"}, 5, 3)); // 4 ("10","0001","1","0")
        System.out.println(sol.findMaxForm(
            new String[]{"10", "0", "1"}, 1, 1)); // 2 ("0","1")

        System.out.println("\n═══ G5: Stone Game ═══");
        System.out.println(sol.stoneGame(new int[]{5, 3, 4, 5}));     // true
        System.out.println(sol.stoneGameDP(new int[]{5, 3, 4, 5}));   // true
        System.out.println(sol.stoneGameDP(new int[]{3, 7, 2, 3}));   // true
    }
}
