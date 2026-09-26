# Dynamic Programming — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "minimum coins / fewest steps to reach target" | Unbounded Knapsack (Coin Change) |
| "how many ways to reach / decode / partition" | Linear DP (counting) |
| "can't pick two adjacent" | Linear DP (House Robber) |
| "longest increasing subsequence" | LIS (DP + Binary Search) |
| "can this string be segmented" | Linear DP (Word Break) |
| "unique paths in grid" | Grid DP |
| "edit distance / transform string A to B" | String DP (2D) |
| "longest common subsequence / substring" | String DP (2D) |
| "can array be split into equal halves" | 0/1 Knapsack (Subset Sum) |
| "maximum subarray sum" | Kadane's (Linear DP) |
| "palindrome partitioning / longest palindrome" | Interval DP or Expand Around Center |
| "burst balloons / merge stones / matrix chain" | Interval DP |
| "buy/sell stock with constraints" | State Machine DP |
| "game theory — who wins" | Interval DP / Game DP |
| "at most m zeros and n ones" | Multi-dimensional Knapsack |
| "knapsack with limited items" | 0/1 Knapsack (iterate backwards) |
| "knapsack with unlimited items" | Unbounded Knapsack (iterate forwards) |
| "minimum cost to paint / arrange" | Linear DP with states |
| "number of subsets / partitions with property" | Subset Sum / Counting DP |

---

## The Five Core Templates (Memorize These)

### Template 1: Linear DP (Fibonacci / House Robber)

```java
// dp[i] depends on dp[i-1], dp[i-2] → space-optimize to O(1)
int prev2 = BASE_0, prev1 = BASE_1;
for (int i = 2; i <= n; i++) {
    int curr = f(prev1, prev2, input[i]);
    prev2 = prev1;
    prev1 = curr;
}
return prev1;
```

### Template 2: 0/1 Knapsack (Subset Sum / Partition)

```java
// Each item used at most ONCE → iterate capacity BACKWARDS
boolean[] dp = new boolean[target + 1];
dp[0] = true;
for (int num : nums) {
    for (int j = target; j >= num; j--) {  // ← BACKWARDS
        dp[j] = dp[j] || dp[j - num];
    }
}
return dp[target];
```

### Template 3: Unbounded Knapsack (Coin Change)

```java
// Each item used unlimited times → iterate capacity FORWARDS
int[] dp = new int[amount + 1];
Arrays.fill(dp, amount + 1);
dp[0] = 0;
for (int i = 1; i <= amount; i++) {
    for (int c : coins) {
        if (c <= i) dp[i] = Math.min(dp[i], dp[i - c] + 1);
    }
}
```

### Template 4: String DP (Edit Distance / LCS)

```java
int[][] dp = new int[m + 1][n + 1];
// Base cases: dp[0][j], dp[i][0]
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1))
            dp[i][j] = dp[i-1][j-1] + 1;           // chars match
        else
            dp[i][j] = 1 + min(dp[i-1][j-1],        // replace
                           dp[i-1][j],               // delete
                           dp[i][j-1]);              // insert
    }
}
```

### Template 5: Interval DP (Burst Balloons / Stone Game)

```java
int[][] dp = new int[n][n];
// Base: dp[i][i] = single element case
for (int len = 2; len <= n; len++) {            // range length
    for (int i = 0; i + len - 1 < n; i++) {     // start index
        int j = i + len - 1;                     // end index
        for (int k = i; k <= j; k++) {           // split point
            dp[i][j] = max(dp[i][j],
                dp[i][k-1] + dp[k+1][j] + cost(i, k, j));
        }
    }
}
```

---

## State Machine Template (Stock Buy/Sell)

```java
// States: hold, sold, rest
int hold = -prices[0], sold = 0, rest = 0;
for (int i = 1; i < n; i++) {
    int prevHold = hold, prevSold = sold, prevRest = rest;
    hold = max(prevHold, prevRest - prices[i]);    // buy or keep holding
    sold = prevHold + prices[i];                    // sell
    rest = max(prevRest, prevSold);                 // cooldown or stay
}
return max(sold, rest);
```

**State Machine Diagram:**
```
rest ──(buy)──→ hold ──(sell)──→ sold ──(cooldown)──→ rest
 ↻ (rest)         ↻ (hold)
```

---

## Space Optimization Rules

| Pattern | Full Space | Optimized | How |
|---|---|---|---|
| Linear (dp[i-1], dp[i-2]) | O(n) | O(1) | Two variables: prev1, prev2 |
| Grid DP | O(m×n) | O(n) | One row + left-to-right |
| String DP (two strings) | O(m×n) | O(min(m,n)) | Two rows: prev[], curr[] |
| 0/1 Knapsack | O(n×W) | O(W) | 1D array, iterate backwards |
| Unbounded Knapsack | O(n×W) | O(W) | 1D array, iterate forwards |
| Interval DP | O(n²) | — | Not typically reducible |

---

## Iteration Direction Rules

```
0/1 KNAPSACK:       j from TARGET down to num    (backwards — prevent reuse)
UNBOUNDED KNAPSACK: j from num up to TARGET      (forwards — allow reuse)
GRID DP:            row by row, left to right     (dp[j] = above, dp[j-1] = left)
STRING DP:          i and j both forward          (smaller substrings first)
INTERVAL DP:        by length, short to long      (smaller ranges first)
```

---

## Complexity Quick Reference

| Problem Type | Time | Space |
|---|---|---|
| Linear DP (Fibonacci, Robber, Stairs) | O(n) | O(1) |
| Kadane's (max subarray) | O(n) | O(1) |
| LIS (binary search) | O(n log n) | O(n) |
| LIS (DP) | O(n²) | O(n) |
| Coin Change / Knapsack | O(n × W) | O(W) |
| Grid DP (unique paths) | O(m × n) | O(n) |
| String DP (LCS, edit dist) | O(m × n) | O(min(m,n)) |
| Interval DP (balloons) | O(n³) | O(n²) |
| Stock (state machine) | O(n) | O(1) |
| Word Break | O(n² × L) | O(n) |

---

## Problem → Pattern Map (for Google interviews)

```
Climbing Stairs / Fibonacci     → Linear DP (1D)
House Robber I/II               → Linear DP (skip/take, circular)
Max Subarray                    → Kadane's (extend or restart)
Coin Change                     → Unbounded Knapsack
Coin Change II (ways)           → Unbounded Knapsack (coins outer, amount inner)
Partition Equal Subset Sum      → 0/1 Knapsack (subset sum)
Target Sum                      → 0/1 Knapsack (algebraic transformation)
Ones and Zeroes                 → 2D Knapsack
Unique Paths I/II               → Grid DP
Min Path Sum                    → Grid DP
Maximal Square                  → Grid DP (min of 3 neighbors + 1)
Edit Distance                   → String DP (2D)
LCS / Shortest Common Superseq → String DP (2D)
Longest Palindromic Substring   → Expand Around Center / Interval DP
Word Break                      → Linear DP + HashSet
Decode Ways                     → Linear DP (1 or 2 digits)
LIS                             → DP + Binary Search (tails array)
Burst Balloons                  → Interval DP (last burst)
Stone Game                      → Interval DP (game theory)
Stock with Cooldown/Fee         → State Machine DP
Interleaving String             → String DP (2D boolean)
Palindrome Partitioning II      → Linear DP + Expand
```

---

## DP vs Greedy — The Decision

```
ASK YOURSELF: "Does the greedy choice ALWAYS lead to global optimum?"

YES → Greedy:
  Activity selection, Huffman, fractional knapsack, jump game I

NO → DP:
  0/1 knapsack, coin change (arbitrary denoms), edit distance, LCS

PROOF TECHNIQUE:
  Greedy: exchange argument (swap any non-greedy choice → not worse)
  DP: optimal substructure + overlapping subproblems
```

---

## Interview Communication Script

When you start solving any DP problem:

> "I recognize this as a DP problem because it has optimal substructure
> and overlapping subproblems. Let me define the state:
> - **State**: dp[i] represents [what it encodes].
> - **Transition**: dp[i] = f(dp[i-1], ...) because [reason].
> - **Base case**: dp[0] = [value] because [reason].
> - **Answer**: dp[n] / max(dp[...]).
> I'll first implement the O(n × W) bottom-up solution, then
> optimize space by noticing dp[i] only depends on [prev states]."

---

## Complexity Proof Keywords for Interviews

- **Linear DP O(n):** "Each state computed once, transition is O(1). n states × O(1) per state."
- **Knapsack O(n×W):** "n items × W capacity states. This is pseudo-polynomial — polynomial in value of W, not bit-length."
- **LIS O(n log n):** "n elements, each requiring O(log n) binary search on the tails array."
- **String DP O(m×n):** "m×n states, each computed in O(1). Space O(min(m,n)) using rolling rows."
- **Interval DP O(n³):** "O(n²) subproblems, each trying O(n) split points."
- **Space optimization:** "dp[i] only depends on dp[i-1] (and dp[i-2]), so we discard older rows."

---

*Files in this module:*
- `00_theory.md` — Core concepts, five patterns, state design, complexity
- `01_easy.java` — 8 easy problems (Fibonacci, stairs, robber, Kadane's)
- `02_medium.java` — 9 medium problems (coin change, LIS, word break, knapsack)
- `03_hard.java` — 6 hard problems (edit distance, burst balloons, LCS)
- `04_google_level.java` — 5 Google-level (state machine, game DP, 2D knapsack)
- `05_cheatsheet.md` — This file
- `06_top50_problems.md` — Top 50 problems in intuition-building order
