/**
 * ============================================================
 *  PATTERN 3 — UNBOUNDED KNAPSACK DP
 *  Problem 3 (Hard): Combination Sum IV / Rod Cutting   LC 377
 * ============================================================
 *
 *  Difficulty  : Medium-Hard
 *  Pattern     : Unbounded Knapsack — count permutations + rod cutting
 *  LeetCode    : LC 377 — Combination Sum IV
 *
 *  PROBLEM STATEMENT (LC 377):
 *    Given an integer array nums (distinct) and a target, return the number of
 *    possible combinations that add up to target. Different orderings count separately.
 *
 *  EXAMPLES:
 *    nums=[1,2,3], target=4  → 7
 *      (1+1+1+1, 1+1+2, 1+2+1, 1+3, 2+1+1, 2+2, 3+1)
 *    nums=[9], target=3  → 0
 *
 *  KEY DISTINCTION:
 *    This is PERMUTATIONS (order matters): [1,2] ≠ [2,1].
 *    → Target as OUTER loop, nums as INNER loop.
 *    Contrast with Coin Change II (combinations): [1,2] == [2,1].
 *    → Coins as OUTER loop, amounts as INNER loop.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_CombinationSumIV {

    // =========================================================
    // APPROACH 1 — PERMUTATION COUNT DP (target outer, nums inner)
    // =========================================================
    /**
     * dp[i] = number of ordered sequences summing to i.
     *
     * BASE: dp[0] = 1 (one way to form 0: empty sequence)
     *
     * TRANSITION (AMOUNTS as outer, nums as inner):
     *   dp[i] += dp[i - num] for each num <= i
     *
     * WHY OUTER=AMOUNTS for permutations?
     *   When computing dp[i], we add dp[i-num] for each num.
     *   This says: "start with any sequence summing to i-num, then append num."
     *   Different orderings of the SAME numbers → appended in different positions → counted separately.
     *   The outer loop iterates amounts, so each dp[i-num] was computed with all possible orderings.
     *
     * CONTRAST with combinations (Coin Change II):
     *   Outer=coins: each coin is "committed" before trying larger amounts.
     *   This forces a canonical order (smaller coins first), preventing permutation counting.
     *
     * VISUAL for nums=[1,2,3], target=4:
     *   dp[0]=1
     *   dp[1]: num 1: dp[0]=1 → dp[1]=1
     *   dp[2]: num 1: dp[1]=1, num 2: dp[0]=1 → dp[2]=2  ([1,1],[2])
     *   dp[3]: num 1: dp[2]=2, num 2: dp[1]=1, num 3: dp[0]=1 → dp[3]=4 ([1,1,1],[1,2],[2,1],[3])
     *   dp[4]: num 1: dp[3]=4, num 2: dp[2]=2, num 3: dp[1]=1 → dp[4]=7 ✓
     *
     * Time: O(target × n), Space: O(target)
     *
     * Follow-up: What if negative numbers were allowed?
     *   Infinite loops possible (e.g., [1,-1] for target=1: 1-1+1=1, 1-1+1-1+1=1...).
     *   Need to bound the sequence length: dp[i][k] = ways using exactly k terms.
     */
    public int combinationSum4(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int i = 1; i <= target; i++) {       // ← AMOUNTS as outer
            for (int num : nums) {                  // ← NUMS as inner
                if (num <= i) dp[i] += dp[i - num];
            }
        }
        return dp[target];
    }

    // =========================================================
    // APPROACH 2 — TOP-DOWN MEMOIZATION
    // =========================================================
    private int[] memoCS;

    public int combinationSum4Memo(int[] nums, int target) {
        memoCS = new int[target + 1];
        Arrays.fill(memoCS, -1);
        memoCS[0] = 1;
        return memoCS(nums, target);
    }

    private int memoCS(int[] nums, int rem) {
        if (rem < 0) return 0;
        if (memoCS[rem] != -1) return memoCS[rem];
        int count = 0;
        for (int num : nums) count += memoCS(nums, rem - num);
        return memoCS[rem] = count;
    }

    // =========================================================
    // PART B — ROD CUTTING PROBLEM (Classic Unbounded Knapsack)
    // =========================================================
    /**
     * PROBLEM:
     *   A rod of length n. price[i] = revenue from selling a piece of length i (1-indexed).
     *   Cut the rod into pieces to maximize total revenue. Pieces can repeat (unbounded).
     *
     * STATE: dp[len] = max revenue from a rod of length len
     *
     * TRANSITION:
     *   For each cut size k (1 to len):
     *     dp[len] = max(dp[len], price[k] + dp[len - k])
     *   (cut k from the front, recursively solve the remaining len-k)
     *
     * BASE: dp[0] = 0 (no rod → no revenue)
     *
     * Time: O(n²), Space: O(n)
     *
     * Follow-up: Minimum cuts to divide into pieces of at most size k?
     *   Different DP: dp[len] = ceil(len / k) (greedy).
     * Follow-up: Reconstruct the cut sequence?
     *   Store parent[len] = k (the cut size that gave max revenue).
     */
    public int rodCutting(int[] price, int n) {
        int[] dp = new int[n + 1];
        for (int len = 1; len <= n; len++) {
            for (int cut = 1; cut <= len; cut++) {
                dp[len] = Math.max(dp[len], price[cut - 1] + dp[len - cut]);
            }
        }
        return dp[n];
    }

    /**
     * Reconstruct which cuts maximize revenue.
     */
    public List<Integer> rodCuttingCuts(int[] price, int n) {
        int[] dp     = new int[n + 1];
        int[] parent = new int[n + 1];
        for (int len = 1; len <= n; len++) {
            for (int cut = 1; cut <= len; cut++) {
                if (price[cut - 1] + dp[len - cut] > dp[len]) {
                    dp[len] = price[cut - 1] + dp[len - cut];
                    parent[len] = cut;
                }
            }
        }
        List<Integer> cuts = new ArrayList<>();
        for (int rem = n; rem > 0; rem -= parent[rem]) {
            cuts.add(parent[rem]);
        }
        return cuts;
    }

    // =========================================================
    // COMBINATION vs PERMUTATION SUMMARY
    // =========================================================
    /**
     * Same DP, DIFFERENT LOOP ORDER:
     *
     *   COMBINATIONS (LC 518): coins outer, amounts inner (FORWARD)
     *     → Each coin is processed completely before moving to larger amounts
     *     → [1,2] and [2,1] are counted as ONE combination
     *
     *   PERMUTATIONS (LC 377): amounts outer, coins inner (FORWARD)
     *     → At each amount, ALL coins are tried as the LAST piece
     *     → [1,2] and [2,1] are counted as TWO permutations
     *
     * This is the same distinction as in counting paths in a graph vs DAG.
     */
    public int demonstrateDifference(int[] nums, int target) {
        int[] combinations  = new int[target + 1];
        int[] permutations  = new int[target + 1];
        combinations[0] = permutations[0] = 1;

        // Combinations: coins outer
        for (int num : nums)
            for (int j = num; j <= target; j++)
                combinations[j] += combinations[j - num];

        // Permutations: amounts outer
        for (int i = 1; i <= target; i++)
            for (int num : nums)
                if (num <= i) permutations[i] += permutations[i - num];

        System.out.printf("nums=%s, target=%d%n", Arrays.toString(nums), target);
        System.out.println("  Combinations: " + combinations[target]);
        System.out.println("  Permutations: " + permutations[target]);
        return permutations[target];
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_CombinationSumIV sol = new Problem3_CombinationSumIV();

        System.out.println("=== LC 377: Combination Sum IV (Permutations) ===");
        System.out.println("nums=[1,2,3], target=4: " + sol.combinationSum4(new int[]{1,2,3}, 4)); // 7
        System.out.println("nums=[9], target=3:      " + sol.combinationSum4(new int[]{9}, 3));    // 0
        System.out.println("nums=[1,2,3], target=4 memo: " + sol.combinationSum4Memo(new int[]{1,2,3}, 4)); // 7

        System.out.println("\n=== Combinations vs Permutations Demo ===");
        sol.demonstrateDifference(new int[]{1,2,3}, 4);  // comb=4, perm=7

        System.out.println("\n=== Rod Cutting (Classic) ===");
        // price = [1,5,8,9,10,17,17,20] (lengths 1..8)
        int[] price1 = {1,5,8,9,10,17,17,20};
        System.out.println("price=" + Arrays.toString(price1) + ", n=8");
        System.out.println("  Max revenue : " + sol.rodCutting(price1, 8));  // 22 (cuts: 2+6=17+5, or 2+2+2+2=20)
        System.out.println("  Cuts        : " + sol.rodCuttingCuts(price1, 8));

        int[] price2 = {3,5,8,9,10,17,17,20};
        System.out.println("price=" + Arrays.toString(price2) + ", n=8");
        System.out.println("  Max revenue : " + sol.rodCutting(price2, 8));
        System.out.println("  Cuts        : " + sol.rodCuttingCuts(price2, 8));
    }
}
