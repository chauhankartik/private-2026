# Dynamic Programming — Theory & Pattern Guide
> **Study goal:** Master every DP pattern, state design, and optimization technique
> needed for Google/FAANG interviews.

---

## 1. What is Dynamic Programming?

```
DP = Recursion + Memoization (top-down)
   = Iterative table filling (bottom-up)
```

**When to use DP:**
- The problem has **optimal substructure** — optimal solution uses optimal solutions of subproblems.
- The problem has **overlapping subproblems** — same subproblem is solved multiple times.

```
Example: Fibonacci
  fib(5)
   ├── fib(4)
   │    ├── fib(3)        ← repeated
   │    └── fib(2)        ← repeated
   └── fib(3)             ← repeated
        ├── fib(2)        ← repeated
        └── fib(1)
```

Without memo: O(2^n) — exponential (recomputing same subproblems).
With memo:    O(n) — each subproblem solved once.

---

## 2. Top-Down vs Bottom-Up

### Top-Down (Memoization)
```java
// Natural extension of recursion — add a cache
int[] memo;

int solve(int n) {
    if (n <= 1) return n;
    if (memo[n] != -1) return memo[n];        // already computed
    memo[n] = solve(n - 1) + solve(n - 2);    // store result
    return memo[n];
}
```
**Pros:** Easier to think about (start from original problem), only solves needed subproblems.
**Cons:** Recursion stack overhead, risk of stack overflow for deep recursion.

### Bottom-Up (Tabulation)
```java
// Fill table from smallest subproblems to largest
int solve(int n) {
    int[] dp = new int[n + 1];
    dp[0] = 0; dp[1] = 1;
    for (int i = 2; i <= n; i++)
        dp[i] = dp[i - 1] + dp[i - 2];
    return dp[n];
}
```
**Pros:** No recursion stack, usually faster (cache-friendly), enables space optimization.
**Cons:** Must determine correct iteration order, may compute unnecessary subproblems.

### Space Optimization
```java
// If dp[i] only depends on dp[i-1] and dp[i-2] → keep only 2 variables
int solve(int n) {
    int prev2 = 0, prev1 = 1;
    for (int i = 2; i <= n; i++) {
        int curr = prev1 + prev2;
        prev2 = prev1;
        prev1 = curr;
    }
    return prev1;
}
```

---

## 3. The DP Problem-Solving Framework

```
Step 1: DEFINE THE STATE
    What does dp[i] (or dp[i][j]) represent?
    The state should capture everything needed to make future decisions.

Step 2: IDENTIFY THE TRANSITION (recurrence relation)
    How does dp[i] relate to smaller subproblems?
    dp[i] = f(dp[i-1], dp[i-2], ...)

Step 3: SET BASE CASES
    What are the trivially solvable subproblems?
    dp[0] = ?, dp[1] = ?

Step 4: DETERMINE ITERATION ORDER
    Bottom-up: ensure dp values are computed before they're needed.

Step 5: EXTRACT THE ANSWER
    Where in the dp table is the final answer?
    Sometimes dp[n], sometimes max(dp[...]), sometimes dp[n][target].
```

---

## 4. The Five Core DP Patterns

### Pattern 1: Linear DP (1D)
```
State: dp[i] = answer for the first i elements
Transition: dp[i] = f(dp[i-1], dp[i-2], ...)
```
Used for: Fibonacci, climbing stairs, house robber, decode ways, max subarray.

### Pattern 2: Knapsack (2D: items × capacity)
```
State: dp[i][w] = best value using first i items with capacity w
Transition:
  Skip item i:  dp[i][w] = dp[i-1][w]
  Take item i:  dp[i][w] = dp[i-1][w - wt[i]] + val[i]
  dp[i][w] = max(skip, take)
```
Variants:
- **0/1 Knapsack**: each item used at most once.
- **Unbounded Knapsack**: each item used unlimited times → `dp[i][w - wt[i]]` (same row).
- **Subset Sum**: values = weights, target = exact sum.
- **Coin Change**: unbounded knapsack variant.

### Pattern 3: Grid / Matrix DP (2D)
```
State: dp[i][j] = answer for cell (i, j)
Transition: dp[i][j] = f(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
```
Used for: unique paths, min path sum, maximal square, edit distance.

### Pattern 4: String DP (2D: two strings or one string with intervals)
```
State: dp[i][j] = answer using first i chars of s1 and first j chars of s2
Transition: depends on s1[i-1] vs s2[j-1] match/mismatch
```
Used for: LCS, edit distance, interleaving strings, wildcard matching, palindrome.

### Pattern 5: Interval DP
```
State: dp[i][j] = answer for subarray/substring from index i to j
Transition: try all split points k ∈ [i, j)
  dp[i][j] = min/max over k of: dp[i][k] + dp[k+1][j] + cost(i, k, j)
Iteration: by increasing length (j - i)
```
Used for: matrix chain multiplication, burst balloons, palindrome partitioning, stone merge.

---

## 5. State Design Tips

```
RULE 1: State must encode ALL information needed for future decisions.
   ✗ dp[i] = max profit up to day i        (missing: do I hold a stock?)
   ✓ dp[i][0/1] = max profit up to day i, holding/not holding stock

RULE 2: Minimize dimensions — more dimensions = more time/space.
   Can you combine two dimensions into one?
   Can you use a rolling array to reduce space?

RULE 3: The transition determines the iteration order.
   If dp[i] depends on dp[i-1], iterate i from 0 to n.
   If dp[i] depends on dp[i+1], iterate i from n to 0.
   If dp[i][j] depends on dp[i-1][j-1], both dimensions go forward.

RULE 4: For 0/1 Knapsack bottom-up space optimization:
   1D dp array, iterate CAPACITY BACKWARDS (right to left).
   This prevents using the same item twice.
   For unbounded knapsack: iterate FORWARDS (left to right).
```

---

## 6. Complexity Analysis

| Pattern | Time | Space | Space-Optimized |
|---|---|---|---|
| Linear (1D) | O(n) | O(n) | O(1) if only prev states needed |
| Knapsack | O(n × W) | O(n × W) | O(W) with rolling array |
| Grid | O(m × n) | O(m × n) | O(n) row-by-row |
| String (two) | O(m × n) | O(m × n) | O(min(m,n)) |
| Interval | O(n³) | O(n²) | Not typically reducible |

**Pseudo-polynomial time:** O(n × W) where W = sum/capacity value.
This is NOT polynomial in input SIZE (n = number of elements, W can be exponentially large in bits).
Important for understanding why knapsack is NP-complete but DP is efficient for small W.

---

## 7. Common Bugs

```java
// Bug 1: Wrong base case
// dp[0] = 0 or dp[0] = 1? Depends on the problem semantics.
// Coin change: dp[0] = 0 (0 coins for amount 0)
// Climbing stairs: dp[0] = 1, dp[1] = 1

// Bug 2: Off-by-one in string DP
// dp[i][j] uses 1-indexed strings: s1.charAt(i-1) not s1.charAt(i)
// Array size should be (m+1) × (n+1), not m × n

// Bug 3: Wrong iteration order for space-optimized knapsack
// 0/1 knapsack: iterate w from W to 0 (backwards)
// Unbounded:    iterate w from 0 to W (forwards)

// Bug 4: Not initializing dp array properly
// Arrays.fill(dp, Integer.MAX_VALUE) for min problems
// dp[0] = 0 as the base case AFTER filling

// Bug 5: Integer overflow in transition
// dp[i] = dp[i-1] + dp[i-2] can overflow int for large n
// Use long or modular arithmetic when required

// Bug 6: Forgetting that dp[i][j] may be unused
// Not all cells in the dp table are valid states
// Initialize carefully: 0 vs infinity vs -infinity
```

---

## 8. DP vs Greedy Decision Guide

```
Use GREEDY when:
  - Local optimal choice leads to global optimal (provably)
  - No future decision ever contradicts a past decision
  - Examples: activity selection, Huffman coding, fractional knapsack

Use DP when:
  - Greedy choice doesn't guarantee global optimum
  - You need to try all options and pick the best
  - The problem has overlapping subproblems
  - Examples: 0/1 knapsack, edit distance, coin change (arbitrary denominations)
```

---

## 9. Interview Frequency

| # | Problem | Pattern | Freq |
|---|---|---|---|
| 1 | Climbing Stairs | Linear DP | ⭐⭐⭐⭐⭐ |
| 2 | House Robber I/II | Linear DP | ⭐⭐⭐⭐⭐ |
| 3 | Coin Change | Unbounded Knapsack | ⭐⭐⭐⭐⭐ |
| 4 | Longest Common Subsequence | String DP | ⭐⭐⭐⭐⭐ |
| 5 | Longest Increasing Subsequence | Linear DP + BS | ⭐⭐⭐⭐⭐ |
| 6 | Word Break | Linear DP | ⭐⭐⭐⭐⭐ |
| 7 | Edit Distance | String DP | ⭐⭐⭐⭐ |
| 8 | Unique Paths | Grid DP | ⭐⭐⭐⭐ |
| 9 | 0/1 Knapsack | Knapsack | ⭐⭐⭐⭐ |
| 10 | Partition Equal Subset Sum | Subset Sum / Knapsack | ⭐⭐⭐⭐ |
| 11 | Decode Ways | Linear DP | ⭐⭐⭐⭐ |
| 12 | Maximal Square | Grid DP | ⭐⭐⭐⭐ |
| 13 | Buy/Sell Stock (all variants) | State Machine DP | ⭐⭐⭐⭐ |
| 14 | Palindrome Partitioning II | Interval DP | ⭐⭐⭐ |
| 15 | Burst Balloons | Interval DP | ⭐⭐⭐ |

---

*Next:*
- `01_easy.java` — Fibonacci, climbing stairs, min cost climbing, house robber, max subarray, counting bits, tribonacci, divisor game
- `02_medium.java` — Coin change, LIS, word break, decode ways, unique paths, partition subset sum, house robber II, longest palindromic substring, jump game II
- `03_hard.java` — Edit distance, burst balloons, palindrome partitioning II, LCS, maximal square, interleaving string
- `04_google_level.java` — Buy/sell stock with cooldown, longest string chain, target sum, ones and zeroes, stone game
