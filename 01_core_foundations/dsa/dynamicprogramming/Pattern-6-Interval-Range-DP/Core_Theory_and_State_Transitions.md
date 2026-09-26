# Pattern 6 — Interval / Range DP
> **Core Idea:** Solve a problem over every contiguous subarray/substring `[i..j]`,
> building answers for larger intervals out of answers for smaller ones.

---

## 1. The Mental Model

```
Interval DP = "What is the best way to handle the segment [i..j]?"

The answer for [i..j] depends only on strictly smaller intervals:
  [i..k] and [k+1..j]   for some split point k ∈ [i, j-1]

Iteration order: short intervals FIRST, long intervals LAST.
  (length = 1 → length = 2 → ... → length = n)
```

**Key signal words:** merge, split, palindrome, burst, remove, matrix chain,
optimal order of operations on a sequence.

---

## 2. Two Flavours of Interval DP

### Flavour A — "Split Point" (Classic)
```
dp[i][j] = best result for subarray [i..j]
Transition: dp[i][j] = optimal over all k ∈ [i, j-1]:
              f(dp[i][k], dp[k+1][j], cost(i, k, j))
```
Examples: Matrix Chain Multiplication, Minimum Cost to Merge Stones,
          Optimal BST, Stone Game.

### Flavour B — "Last Action" (Reverse Thinking)
```
dp[i][j] = best result if the element chosen LAST in [i..j] is k
Transition: dp[i][j] = max/min over k ∈ [i, j]:
              dp[i][k-1] + cost(k, boundary_left, boundary_right) + dp[k+1][j]
```
Used when forward thinking creates dependency problems (e.g., Burst Balloons).
Choosing the "last action" makes left and right subproblems **independent**.

---

## 3. Canonical Template

```java
// n = array length
int[][] dp = new int[n][n];

// Base case: single elements (length = 1)
for (int i = 0; i < n; i++) {
    dp[i][i] = baseCaseValue(i);
}

// Fill by increasing interval length
for (int len = 2; len <= n; len++) {           // length of interval
    for (int i = 0; i + len - 1 < n; i++) {   // start of interval
        int j = i + len - 1;                   // end of interval
        dp[i][j] = WORST_VALUE;               // Integer.MAX_VALUE or 0

        for (int k = i; k < j; k++) {          // split / last-action point
            int candidate = combine(dp[i][k], dp[k+1][j], cost(i, k, j));
            dp[i][j] = bestOf(dp[i][j], candidate);
        }
    }
}

return dp[0][n-1];
```

---

## 4. State Transition Diagrams

### Matrix Chain Multiplication
```
dp[i][j] = minimum multiplications for matrices A[i] × A[i+1] × ... × A[j]

dp[i][j] = min over k ∈ [i, j-1]:
              dp[i][k] + dp[k+1][j] + dims[i] × dims[k+1] × dims[j+1]

Base: dp[i][i] = 0

Example (4 matrices, dims = [10, 30, 5, 60, 15]):
  len=2: dp[0][1]=1500, dp[1][2]=9000, dp[2][3]=45000
  len=3: dp[0][2]=min(dp[0][0]+dp[1][2]+10×30×5, dp[0][1]+dp[2][2]+10×5×60) = 4500
  len=4: dp[0][3] = min over k=0,1,2 → 15750
```

### Burst Balloons (Reverse: Last Burst)
```
Pad: nums = [1, a₀, a₁, ..., aₙ₋₁, 1]  (1-indexed in padded array)

dp[i][j] = max coins when balloon k is the LAST to burst in [i..j]

dp[i][j] = max over k ∈ [i, j]:
              padded[i-1] × padded[k] × padded[j+1]   ← k burst last
            + dp[i][k-1]                               ← left subproblem
            + dp[k+1][j]                               ← right subproblem

Base: dp[i][i-1] = 0 (empty range)

Why "last burst"? → left and right subproblems become INDEPENDENT:
  the boundaries of [i..k-1] are padded[i-1] and padded[k] (not yet burst)
  the boundaries of [k+1..j] are padded[k] and padded[j+1] (not yet burst)
```

### Palindrome Check Precomputation
```
isPalin[i][j] = true if s[i..j] is a palindrome

isPalin[i][i] = true
isPalin[i][i+1] = (s[i] == s[i+1])

For len >= 3:
isPalin[i][j] = (s[i] == s[j]) && isPalin[i+1][j-1]

Use this table to speed up:
  - Palindrome Partitioning II (LC 132)
  - Count Palindromic Substrings (LC 647)
  - Longest Palindromic Subsequence (LC 516)
```

---

## 5. Key State Designs

| Problem | State `dp[i][j]` | Transition |
|---|---|---|
| Matrix Chain Mult. | Min ops for matrices [i..j] | `min(dp[i][k] + dp[k+1][j] + cost)` |
| Burst Balloons | Max coins, all in [i..j] burst | `max(dp[i][k-1] + burst_k_last + dp[k+1][j])` |
| Min Cost Tree | Min cost to build BST from keys [i..j] | `min(dp[i][k-1] + dp[k+1][j] + sum(freq[i..j]))` |
| Longest Palindromic Subseq. | LPS length for s[i..j] | `if s[i]==s[j]: dp[i+1][j-1]+2, else max(dp[i+1][j], dp[i][j-1])` |
| Palindrome Partitioning II | Min cuts for s[0..i] | `min over j where s[j..i] isPalin: dp[j-1]+1` |
| Stone Merge (Piles) | Min cost to merge piles [i..j] | `min(dp[i][k] + dp[k+1][j] + sum[i..j])` |

---

## 6. Iteration Order: WHY length-first?

```
To compute dp[i][j] (length L), we need:
  dp[i][k]   → length k-i+1 < L   ← ALREADY COMPUTED if we go short-first
  dp[k+1][j] → length j-k   < L   ← ALREADY COMPUTED if we go short-first

If instead we iterate (i from 0 to n, j from i+1 to n):
  dp[i][j] might need dp[i'][j'] where i' > i and j' > j → NOT yet computed!

CORRECT ORDER:
  for (len = 2 to n)               ← outer: interval length
    for (i = 0 to n-len)           ← inner: start index
      j = i + len - 1
      for (k = i to j-1)           ← split point
```

---

## 7. Space and Time Complexity

| Property | Value |
|---|---|
| States | O(n²) — all pairs (i, j) |
| Transitions per state | O(n) — split points |
| **Total Time** | **O(n³)** |
| **Space** | **O(n²)** |
| Space reducible? | Rarely (unlike linear DP) |

**Optimization exceptions:**
- Knuth's optimization (for certain monotone cost functions): O(n²) time
- Palindrome DP with expand-around-center: O(n²) time, O(1) extra space

---

## 8. Common Pitfalls

```java
// PITFALL 1: Wrong base case
// dp[i][i] ≠ 0 always. It depends on the problem:
//   Matrix Chain: dp[i][i] = 0 (one matrix, no multiplication)
//   Burst Balloons: dp[i][i] = nums[i-1] × nums[i] × nums[i+1] (burst single)
//   Stone Merge: dp[i][i] = 0 (one pile, no merging needed)

// PITFALL 2: Wrong split range
// For "last k in [i..j]" (Burst Balloons flavour):
//   k ranges from i to j INCLUSIVE
// For "split between k and k+1":
//   k ranges from i to j-1 EXCLUSIVE of j

// PITFALL 3: Forgetting to pad / add sentinel boundaries
// Burst Balloons: pad with 1s at both ends for clean boundary handling

// PITFALL 4: Off-by-one in prefix sums
// Stone Merge uses sum[i..j] = prefixSum[j+1] - prefixSum[i]
// Build prefix BEFORE the DP loops

// PITFALL 5: Iterating j from i to n (WRONG)
// Always iterate by LENGTH, not by endpoints:
//   WRONG: for i, for j > i, for k
//   RIGHT: for len, for i, j = i + len - 1, for k
```

---

## 9. Interview Communication Script

```
"This is an Interval DP problem because the answer for a range [i..j]
 is built from answers for smaller ranges — it has optimal substructure
 over intervals.

 State: dp[i][j] = [what it means]
 Transition: dp[i][j] = [formula, iterating over split/last-action k]
 Base case: dp[i][i] = [single-element answer]
 Iteration order: by increasing length (short ranges first)

 Time: O(n³) — O(n²) intervals × O(n) split points.
 Space: O(n²) for the DP table.

 Key insight: [for Burst Balloons] I think about which balloon to burst
 LAST in each range. This makes the left and right subproblems independent
 because the boundaries are fixed by the un-burst balloon k."
```

---

## 10. Quick Pattern Recognition

```
Signal                                       → Interval DP
─────────────────────────────────────────────────────────
"merge/combine adjacent elements optimally"  → Stone Merge / Matrix Chain
"palindrome count / partition / subsequence" → Palindrome DP
"burst / remove elements, gain coins"        → Burst Balloons (reverse DP)
"game: two players pick from ends of array"  → Game DP (Interval)
"build optimal BST from keys"                → Optimal BST DP
"remove boxes in groups for multiplied cost" → Remove Boxes (hard)
```

*Next: See the three problems — Basic (Longest Palindromic Substring),
Medium (Min Cost Tree / Optimal BST), Hard (Burst Balloons).*
