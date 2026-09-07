# Pattern 2 — 0/1 Knapsack DP
> **Core Idea:** Choose a subset of items where each item can be used AT MOST ONCE.
> `dp[i][w]` = best value using the first `i` items with weight capacity `w`.
> **Key rule:** iterate capacity BACKWARDS in space-optimized 1D form to prevent reuse.

---

## 1. The Mental Model

```
"For each item, I have exactly two choices: TAKE IT or SKIP IT."
"If I take item i, I lose weight wt[i] from remaining capacity."
"If I skip item i, nothing changes."

dp[i][w] = max value using first i items, capacity w

For item i (1-indexed):
  Skip: dp[i][w] = dp[i-1][w]
  Take: dp[i][w] = dp[i-1][w - wt[i]] + val[i]   (only if wt[i] <= w)

  dp[i][w] = max(skip, take)
```

**Key signal words:** "each item used at most once", "subset with property",
"partition array into two equal groups", "target sum with +/- signs".

---

## 2. Canonical Templates

### Template A: Full 2D (clearest for interviews)
```java
// n items, capacity W
int[][] dp = new int[n + 1][W + 1];
for (int i = 1; i <= n; i++) {
    for (int w = 0; w <= W; w++) {
        dp[i][w] = dp[i-1][w];                               // skip item i
        if (wt[i-1] <= w)
            dp[i][w] = Math.max(dp[i][w],
                dp[i-1][w - wt[i-1]] + val[i-1]);            // take item i
    }
}
return dp[n][W];
```

### Template B: Space-optimized 1D (BACKWARDS iteration!)
```java
// CRITICAL: iterate w BACKWARDS to prevent using the same item twice
int[] dp = new int[W + 1];
for (int i = 0; i < n; i++) {
    for (int w = W; w >= wt[i]; w--) {      // ← BACKWARDS
        dp[w] = Math.max(dp[w], dp[w - wt[i]] + val[i]);
    }
}
return dp[W];
```

### Template C: Subset Sum (boolean version)
```java
// Can we form exactly sum 'target' using a subset?
boolean[] dp = new boolean[target + 1];
dp[0] = true;
for (int num : nums) {
    for (int j = target; j >= num; j--) {   // ← BACKWARDS
        dp[j] = dp[j] || dp[j - num];
    }
}
return dp[target];
```

### Template D: Count subsets (counting version)
```java
int[] dp = new int[target + 1];
dp[0] = 1;                                   // one way to form 0: empty subset
for (int num : nums) {
    for (int j = target; j >= num; j--) {   // ← BACKWARDS
        dp[j] += dp[j - num];
    }
}
return dp[target];
```

---

## 3. State Transition Diagrams

### Classic 0/1 Knapsack
```
Items:    wt=[2,3,4,5], val=[3,4,5,6], W=8

dp table (rows=items, cols=capacity):

     cap: 0  1  2  3  4  5  6  7  8
item 0:   0  0  0  0  0  0  0  0  0   (no items)
item 1:   0  0  3  3  3  3  3  3  3   (wt=2,val=3)
item 2:   0  0  3  4  4  7  7  7  7   (wt=3,val=4)
item 3:   0  0  3  4  5  7  8  9  9   (wt=4,val=5)
item 4:   0  0  3  4  5  7  8  9 10   (wt=5,val=6) ← answer: 10

Trace dp[4][8]=10: took item 4 (wt=5,val=6) + dp[3][3]=4 (item 2 only)
```

### Partition Equal Subset Sum (LC 416)
```
Can we split nums into two equal-sum halves?
Equivalent: can we find a subset summing to total/2?

total = sum(nums). If total is odd → impossible.
target = total / 2.

dp[j] = true if we can form sum j using a subset.

Example: nums=[1,5,11,5], total=22, target=11
  dp[0]=true
  After 1: dp[1]=true
  After 5: dp[1]=dp[6]=true
  After 11: dp[1]=dp[6]=dp[11]=true → FOUND! return true
```

### Target Sum (LC 494) — Algebraic Transformation
```
Assign +/- to each element to reach target.
Let P = subset with +, N = subset with -.
P + N = total (sum of all nums)
P - N = target
→ 2P = total + target
→ P = (total + target) / 2

If (total + target) is odd or P < 0 → impossible.
Count subsets summing to P.

This reduces TARGET SUM to COUNT SUBSETS (Template D above).
```

---

## 4. Why BACKWARDS Iteration?

```
Suppose dp = [T, F, F, F, F, F] and we process num=2:

WRONG (forwards):
  j=2: dp[2] |= dp[0] = T → dp[2]=T   ← using element 2 once ✓
  j=4: dp[4] |= dp[2] = T → dp[4]=T   ← dp[2] was UPDATED THIS PASS!
                                         This means we used element 2 TWICE! ✗

CORRECT (backwards):
  j=6: dp[6] |= dp[4] = F → dp[6]=F   (dp[4] not yet updated this pass)
  j=4: dp[4] |= dp[2] = F → dp[4]=F   (dp[2] not yet updated this pass)
  j=2: dp[2] |= dp[0] = T → dp[2]=T   ← only uses element 2 once ✓

By going BACKWARDS, when we compute dp[j], dp[j-num] still holds the
value from the PREVIOUS item's pass → prevents double-counting.
```

---

## 5. Problem Variants Map

| Problem | Key Transformation | Template |
|---|---|---|
| Classic 0/1 Knapsack | Direct | 2D or 1D backwards |
| Partition Equal Subset (LC 416) | target = sum/2 | Boolean 1D backwards |
| Target Sum (LC 494) | P = (sum+target)/2, count subsets | Count 1D backwards |
| Last Stone Weight II (LC 1049) | Minimize |P-N|, same as knapsack | Max 1D backwards |
| Count of Subsets (GFG) | Count how many subsets sum to S | Count 1D backwards |
| Ones and Zeroes (LC 474) | 2D knapsack (zeros, ones) | 2D backwards |

---

## 6. Complexity Analysis

| Version | Time | Space |
|---|---|---|
| Full 2D DP | O(n × W) | O(n × W) |
| 1D space-optimized | O(n × W) | O(W) |
| Boolean subset sum | O(n × target) | O(target) |

**Pseudo-polynomial time:** O(n × W) is NOT polynomial in input size because W can be
exponentially large in the number of bits. This is why 0/1 Knapsack is NP-complete.

---

## 7. Common Pitfalls

```java
// PITFALL 1: Iterating FORWARDS in 0/1 Knapsack
// This is the UNBOUNDED Knapsack (item reuse allowed). Never do this for 0/1.

// PITFALL 2: Wrong target for partition problems
// Check: if (sum % 2 != 0) return false;  BEFORE computing target = sum/2.

// PITFALL 3: Forgetting to check wt[i] <= w before taking
// Taking an item that's heavier than remaining capacity → invalid.

// PITFALL 4: 1-indexing vs 0-indexing mismatch
// With items 1-indexed: val[i-1], wt[i-1] inside the loop.
// With items 0-indexed: val[i], wt[i] but outer loop goes 0 to n-1.

// PITFALL 5: Target Sum — not checking (sum+target) parity
// If (sum + target) is odd → can't split evenly → return 0 (no valid assignment).
```

---

## 8. Interview Communication Script

```
"This is a 0/1 Knapsack problem. Each item can be used at most once.

 State: dp[w] = best value (or: can we form sum w?) with capacity w.
 Transition: for each item i, iterate w from W DOWN to wt[i]:
   dp[w] = max(dp[w], dp[w - wt[i]] + val[i])

 The BACKWARD iteration ensures each item is counted at most once.
 Forward iteration would allow reuse (that's Unbounded Knapsack).

 Time: O(n × W). Space: O(W) with 1D optimization."
```

*Next: Basic (0/1 Knapsack Classic), Medium (Partition Equal Subset), Hard (Target Sum).*
