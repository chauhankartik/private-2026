/**
 * ============================================================
 *  PATTERN 7 — BITMASK DP
 *  Problem 2 (Medium): Can I Win   LC 464
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Bitmask DP — Game Theory on Subsets
 *  LeetCode    : https://leetcode.com/problems/can-i-win/
 *
 *  PROBLEM STATEMENT:
 *    Two players take turns picking from integers 1..maxChoosable (each only once).
 *    The player who causes the running total to reach or exceed desiredTotal wins.
 *    Assuming both players play optimally, can the first player guarantee a win?
 *
 *  EXAMPLES:
 *    maxChoosable=10, desiredTotal=11  → false
 *      (total of all numbers = 55, but neither player can guarantee a win)
 *    maxChoosable=10, desiredTotal=0   → true  (first player wins instantly)
 *    maxChoosable=10, desiredTotal=1   → true  (first player picks 1)
 *
 *  KEY INSIGHT:
 *    State = which numbers have been used (bitmask).
 *    The running total is DERIVABLE from the bitmask (sum of used numbers).
 *    So we only need to memoize on 'mask', not on (mask, currentSum).
 *
 * ============================================================
 */
import java.util.*;

class Problem2_CanIWin {

    // =========================================================
    // APPROACH 1 — BITMASK DP WITH MEMOIZATION (HashMap)
    // =========================================================
    /**
     * STATE:
     *   canWin(mask) = can the CURRENT player win given that
     *                  the numbers whose bits are SET in mask have been used?
     *
     * KEY DERIVATION:
     *   currentSum = sum of all numbers whose bits are set in mask
     *   We recompute this from the mask each time (O(n) per call),
     *   OR precompute it as we build up the mask.
     *
     * TRANSITION:
     *   For each number i ∈ [1..maxChoosable] where bit (i-1) is NOT set in mask:
     *     Pick number i:
     *       If currentSum + i >= desiredTotal → WIN (current player wins now)
     *       Else if canWin(mask | (1 << (i-1))) == false → WIN (opponent will lose)
     *   If NO such winning move → LOSE
     *
     * BASE CASES:
     *   desiredTotal <= 0 → first player wins trivially
     *   total sum of 1..maxChoosable < desiredTotal → impossible to reach → false
     *
     * MEMOIZATION:
     *   HashMap<Integer, Boolean> memo  (mask → result)
     *   Key = mask (which numbers are used so far)
     *
     * Time:  O(2^n * n) — 2^n unique masks, each computed once, O(n) transitions
     * Space: O(2^n) — memoization table
     *
     * Follow-up: Why is the sum derivable from the mask?
     *   Sum = sum of (i) for each i where bit (i-1) is set in mask.
     *   This is deterministic from the mask — no need to track it separately.
     *
     * Follow-up: What if players could use the same number again?
     *   Remove the bitmask. State = currentSum. dp[sum] = can current player win?
     *   This becomes a simpler linear DP (like a 1D game DP).
     */
    private Map<Integer, Boolean> memo;
    private int maxNum;
    private int target;

    public boolean canIWin(int maxChoosable, int desiredTotal) {
        // Edge cases
        if (desiredTotal <= 0) return true;
        int totalSum = maxChoosable * (maxChoosable + 1) / 2;
        if (totalSum < desiredTotal) return false;

        this.maxNum = maxChoosable;
        this.target = desiredTotal;
        this.memo = new HashMap<>();

        return canWin(0, 0);
    }

    /**
     * @param mask       bitmask of which numbers (1..maxNum) have been used
     * @param currentSum running sum so far
     * @return true if the CURRENT PLAYER can guarantee a win
     */
    private boolean canWin(int mask, int currentSum) {
        if (memo.containsKey(mask)) return memo.get(mask);

        for (int i = 1; i <= maxNum; i++) {
            int bit = 1 << (i - 1);
            if ((mask & bit) != 0) continue;  // number i already used

            // Current player picks i
            if (currentSum + i >= target) {
                memo.put(mask, true);
                return true;  // immediate win
            }

            // If opponent loses from the resulting state, current player wins
            if (!canWin(mask | bit, currentSum + i)) {
                memo.put(mask, true);
                return true;
            }
        }

        // No winning move found
        memo.put(mask, false);
        return false;
    }

    // =========================================================
    // APPROACH 2 — BITMASK DP BOTTOM-UP (Iterative)
    // =========================================================
    /**
     * Build the DP table for all 2^maxChoosable masks bottom-up.
     * Process masks in order of increasing popcount (fewer numbers used → smaller mask).
     *
     * However, iterating masks in order of mask value (0 to fullMask) is NOT
     * guaranteed to process smaller popcount first.
     *
     * Better: sort masks by popcount, or process all masks and skip if not reachable.
     * The simplest: iterate masks from fullMask down to 0 (or use memoization, which
     * is already optimal for this problem).
     *
     * Note: For this problem, the top-down memoized approach IS the standard solution.
     * Bottom-up is less natural here. We show it for completeness.
     *
     * Alternative bottom-up: use topological order by popcount.
     */
    public boolean canIWinBottomUp(int maxChoosable, int desiredTotal) {
        if (desiredTotal <= 0) return true;
        int totalSum = maxChoosable * (maxChoosable + 1) / 2;
        if (totalSum < desiredTotal) return false;

        int fullMask = (1 << maxChoosable) - 1;

        // Precompute sum of each mask
        int[] sumOf = new int[fullMask + 1];
        for (int mask = 1; mask <= fullMask; mask++) {
            // sum of mask = sum of (mask with lowest bit removed) + lowest bit number
            int lsb = Integer.numberOfTrailingZeros(mask) + 1;  // 1-indexed number
            sumOf[mask] = sumOf[mask & (mask - 1)] + lsb;
        }

        // Sort masks by popcount (process states with fewer used numbers first)
        // So that when we compute dp[mask], all dp[mask | bit] are NOT yet computed —
        // wait, we actually need to process HIGHER popcount first (leaves of the game tree).
        // Let's sort by popcount DESCENDING.
        Integer[] masks = new Integer[fullMask + 1];
        for (int i = 0; i <= fullMask; i++) masks[i] = i;
        Arrays.sort(masks, (a, b) -> Integer.bitCount(b) - Integer.bitCount(a));

        boolean[] dp = new boolean[fullMask + 1];

        for (int mask : masks) {
            int curSum = sumOf[mask];
            // Current player (faced with this mask) can try each unused number
            boolean canWin = false;
            for (int i = 1; i <= maxChoosable; i++) {
                int bit = 1 << (i - 1);
                if ((mask & bit) != 0) continue;
                if (curSum + i >= desiredTotal) {
                    canWin = true;
                    break;
                }
                if (!dp[mask | bit]) {
                    canWin = true;
                    break;
                }
            }
            dp[mask] = canWin;
        }

        return dp[0];  // first player faces empty mask (no numbers used yet)
    }

    // =========================================================
    // APPROACH 3 — NAIVE RECURSION (no memo, exponential)
    // =========================================================
    /**
     * For validation on small inputs only. Time: O(n!) in the worst case.
     */
    public boolean canIWinNaive(int maxChoosable, int desiredTotal) {
        if (desiredTotal <= 0) return true;
        return naiveGame(maxChoosable, desiredTotal, new boolean[maxChoosable + 1], 0);
    }

    private boolean naiveGame(int maxNum, int target, boolean[] used, int currentSum) {
        for (int i = 1; i <= maxNum; i++) {
            if (used[i]) continue;
            if (currentSum + i >= target) return true;
            used[i] = true;
            if (!naiveGame(maxNum, target, used, currentSum + i)) {
                used[i] = false;
                return true;
            }
            used[i] = false;
        }
        return false;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_CanIWin sol = new Problem2_CanIWin();

        int[][] cases = {
            {10, 11},  // false
            {10, 0},   // true
            {10, 1},   // true
            {4, 6},    // true (pick 3, then pick 3+next)
            {3, 32},   // false (max sum = 6 < 32)
            {5, 10},   // false
        };

        for (int[] c : cases) {
            int maxC = c[0], desired = c[1];
            System.out.printf("maxChoosable=%d, desiredTotal=%d%n", maxC, desired);
            System.out.println("  Top-Down memo : " + sol.canIWin(maxC, desired));
            System.out.println("  Bottom-Up     : " + sol.canIWinBottomUp(maxC, desired));
            if (maxC <= 8) {
                System.out.println("  Naive         : " + sol.canIWinNaive(maxC, desired));
            }
            System.out.println();
        }
    }
}
