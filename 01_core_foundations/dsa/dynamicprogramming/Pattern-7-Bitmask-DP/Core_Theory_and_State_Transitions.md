# Pattern 7 — Bitmask DP
> **Core Idea:** Use an integer bitmask to encode which elements from a set
> have been "used/visited/assigned". Each bit = one element's status.
> Iterate over all 2ⁿ subsets and build answers from smaller subsets.

---

## 1. The Mental Model

```
"We have n items (n <= 20). For each SUBSET of these items,
 we want to know the optimal answer."

State: dp[mask] = best answer when exactly the items in 'mask' have been used.

Transition: dp[mask | (1 << j)] = combine(dp[mask], cost(mask, j))
  where j is a NOT-yet-used item (bit j is 0 in mask).

Iteration: from mask = 0 to mask = (1 << n) - 1  (all subsets)
```

**Key signal words:** "assign n people to n tasks", "visit all n cities exactly once",
"partition set into groups", n <= 20.

---

## 2. Bit Manipulation Fundamentals

```java
// Check if bit j is set in mask:
boolean isSet = (mask & (1 << j)) != 0;

// Set bit j in mask (mark j as used):
int newMask = mask | (1 << j);

// Clear bit j in mask (unmark j):
int cleared = mask & ~(1 << j);

// Number of set bits:
int count = Integer.bitCount(mask);

// Iterate over all non-empty subsets of mask:
for (int sub = mask; sub > 0; sub = (sub - 1) & mask) { /* sub is a subset */ }

// Full mask (all n bits set):
int fullMask = (1 << n) - 1;
```

---

## 3. Canonical Templates

### Template A: dp[mask] — subset of items processed
```java
int[] dp = new int[1 << n];
Arrays.fill(dp, Integer.MAX_VALUE / 2);
dp[0] = 0;

for (int mask = 0; mask < (1 << n); mask++) {
    if (dp[mask] >= Integer.MAX_VALUE / 2) continue;
    for (int j = 0; j < n; j++) {
        if ((mask & (1 << j)) == 0) {           // j not yet used
            int newMask = mask | (1 << j);
            dp[newMask] = Math.min(dp[newMask], dp[mask] + cost[mask][j]);
        }
    }
}
return dp[(1 << n) - 1];
```

### Template B: dp[mask][last] — subset + last item chosen (TSP style)
```java
int[][] dp = new int[1 << n][n];
for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE / 2);
dp[1][0] = 0;  // start at city 0

for (int mask = 1; mask < (1 << n); mask++) {
    for (int last = 0; last < n; last++) {
        if ((mask & (1 << last)) == 0) continue;
        if (dp[mask][last] >= Integer.MAX_VALUE / 2) continue;
        for (int next = 0; next < n; next++) {
            if ((mask & (1 << next)) != 0) continue;
            int newMask = mask | (1 << next);
            dp[newMask][next] = Math.min(dp[newMask][next],
                                         dp[mask][last] + dist[last][next]);
        }
    }
}
int fullMask = (1 << n) - 1;
int ans = Integer.MAX_VALUE;
for (int last = 1; last < n; last++)
    ans = Math.min(ans, dp[fullMask][last] + dist[last][0]);
```

---

## 4. State Transition Diagrams

### Travelling Salesperson (TSP)
```
dp[mask][i] = min cost to reach city i, having visited exactly cities in 'mask'

Base:   dp[1][0] = 0   (at city 0, only city 0 visited)

Transition (n=3):
  mask=001: dp[001][0]=0
  mask=011: dp[011][1] = dp[001][0] + dist[0][1]
  mask=101: dp[101][2] = dp[001][0] + dist[0][2]
  mask=111: dp[111][1] = min(dp[011][0]+dist[0][1], dp[101][2]+dist[2][1])
            dp[111][2] = min(dp[011][1]+dist[1][2], dp[101][0]+dist[0][2])
  Answer  = min(dp[111][1]+dist[1][0], dp[111][2]+dist[2][0])
```

### Can I Win (Game DP)
```
dp[mask] = can CURRENT player win given 'mask' of remaining numbers?

Transition:
  For each i in [1..maxChoosable] where bit i is NOT set in mask:
    If (running sum + i) >= desiredTotal -> WIN (pick i)
    Else if dp[mask | (1<<i)] == false   -> WIN (opponent loses)
  If no winning move found -> LOSE

Memoize: HashMap<Integer, Boolean> memo
```

### Assignment Problem (n workers to n tasks)
```
dp[mask] = min cost to assign tasks to the first popcount(mask) workers
           where 'mask' represents which tasks have been assigned

For worker = popcount(mask):
  For each task j not in mask:
    dp[mask | (1<<j)] = min(dp[mask | (1<<j)], dp[mask] + cost[worker][j])

Answer: dp[fullMask]
```

---

## 5. Complexity Analysis

| Property | Value |
|---|---|
| States `dp[mask]` | O(2ⁿ) |
| States `dp[mask][i]` | O(n · 2ⁿ) |
| Transitions per state | O(n) |
| **Total Time (A)** | **O(n · 2ⁿ)** |
| **Total Time (B)** | **O(n² · 2ⁿ)** |
| Subset enumeration total | O(3ⁿ) |
| **Practical limit** | n ≤ 20 |

**Why 3ⁿ for subset enumeration?**
```
Each element has 3 choices: in mask but not sub, in sub, or not in mask.
Iterating all (mask, sub) pairs where sub is a subset of mask = O(3ⁿ).
```

---

## 6. Key Problem Patterns

| Problem | Template | Key Insight |
|---|---|---|
| TSP | dp[mask][last] | Need last city for return-edge cost |
| Assignment | dp[mask] | mask = assigned tasks; derive worker from bitcount |
| Matchsticks to Square | Backtracking + bitmask memo | 4 equal-sum buckets |
| Can I Win | dp[mask] game | Current player wins if ANY move loses for opponent |
| Shortest Path visiting All Nodes | dp[mask][node] | BFS + bitmask state |
| Beautiful Arrangement | dp[mask] | Count perms where position i divides arr[i] |

---

## 7. Common Pitfalls

```java
// PITFALL 1: n must be small — O(2^n) blows up for n > 25
// n <= 20 -> ~1M; n == 25 -> ~33M (borderline)

// PITFALL 2: Wrong base for TSP
// dp[1 << startCity][startCity] = 0, NOT dp[0][startCity] = 0

// PITFALL 3: Not guarding against unset bits in transition
// MUST CHECK: (mask & (1 << j)) == 0 before using j as "next"

// PITFALL 4: Integer overflow with 1 << n
// For n >= 31, use 1L << n (long mask). Java int is 32-bit signed.

// PITFALL 5: Iterating mask in wrong direction
// Forward iteration (0 to fullMask) is CORRECT because
// dp[mask] only depends on dp[masks with fewer bits].
```

---

## 8. Interview Communication Script

```
"This is Bitmask DP. n is small (<= 20), so we can represent
 any subset of items as a bitmask integer.

 State: dp[mask] = [best answer] when items in 'mask' are used.
 Transition: for each unused item j (bit j is 0):
   dp[mask | (1<<j)] = combine(dp[mask], cost(mask, j))
 Base: dp[0] = 0
 Answer: dp[(1<<n) - 1]

 Time: O(n * 2^n). Space: O(2^n).
 This is optimal for subset enumeration problems."
```

*Next: Basic (Matchsticks to Square), Medium (Can I Win), Hard (TSP).*
