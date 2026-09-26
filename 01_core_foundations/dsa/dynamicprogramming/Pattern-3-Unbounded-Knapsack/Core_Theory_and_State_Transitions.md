# Pattern 3 — Unbounded Knapsack DP
> **Core Idea:** Choose items where each item can be used UNLIMITED times.
> `dp[w]` = best value achievable with capacity exactly `w`.
> **Key rule:** iterate capacity FORWARDS to allow reuse of the same item.

---

## 1. The Mental Model

```
"I can pick the same item again and again, as long as capacity allows."

dp[w] = min coins / max value / number of ways for capacity w

For each capacity w (from 0 to W):
  For each item i with weight wt[i]:
    If wt[i] <= w:
      dp[w] = best_of(dp[w], dp[w - wt[i]] + contribution[i])

FORWARDS iteration: when we process dp[w] and look back at dp[w - wt[i]],
that earlier value has ALREADY been updated for item i this pass.
This means item i can be selected multiple times. THAT'S WHAT WE WANT.
```

**Key signal words:** "unlimited supply of coins", "minimum number of coins",
"number of ways to make change", "infinite items", "rod cutting".

---

## 2. Canonical Templates

### Template A: Minimize (Coin Change — fewest coins)
```java
int[] dp = new int[amount + 1];
Arrays.fill(dp, amount + 1);     // infinity = impossible
dp[0] = 0;                        // 0 coins needed for amount 0
for (int w = 1; w <= amount; w++) {
    for (int coin : coins) {
        if (coin <= w)
            dp[w] = Math.min(dp[w], dp[w - coin] + 1);  // +1 coin
    }
}
return dp[amount] > amount ? -1 : dp[amount];
```

### Template B: Count ways (Coin Change II — number of combinations)
```java
int[] dp = new int[amount + 1];
dp[0] = 1;                        // 1 way to make 0: use nothing
for (int coin : coins) {          // ← COINS outer loop prevents permutation counting
    for (int w = coin; w <= amount; w++) {
        dp[w] += dp[w - coin];
    }
}
return dp[amount];
```

### Template C: Rod Cutting (maximize value)
```java
int[] dp = new int[n + 1];
// dp[len] = max value from a rod of length len
for (int len = 1; len <= n; len++) {
    for (int cut = 1; cut <= len; cut++) {
        dp[len] = Math.max(dp[len], price[cut] + dp[len - cut]);
    }
}
return dp[n];
```

---

## 3. State Transition Diagrams

### Coin Change (LC 322) — Minimize
```
coins=[1,5,11], amount=15

dp[0]=0, dp[1..15]=∞

Process all amounts 1..15, all coins each time:

dp[1]:  coin 1: dp[1-1]+1=1    → dp[1]=1
dp[5]:  coin 1: dp[4]+1=5
        coin 5: dp[0]+1=1      → dp[5]=1
dp[10]: coin 5: dp[5]+1=2      → dp[10]=2
dp[11]: coin 1: dp[10]+1=3
        coin 11: dp[0]+1=1     → dp[11]=1
dp[15]: coin 1: dp[14]+1=4
        coin 5: dp[10]+1=3
        coin 11: dp[4]+1=5     → dp[15]=3  (coins: 11+1+1+1... wait: 11+4=15 via 11+1+1+1+1=5 coins?)
Actually: dp[15] = min(dp[14]+1, dp[10]+1, dp[4]+1)
  dp[14]: coin 5: dp[9]+1, coin 11: dp[3]+1=4 → dp[14]=4? Let me trace:
  Correct answer: 3 coins (5+5+5=15, or 11+3*1=14?... no, 11+4=15? 11+1+1+1+1=5 coins)
  Actually dp[15]: coin 5 gives dp[10]+1=3. Answer=3 (three 5-coins). ✓
```

### Coin Change II (LC 518) — Count combinations
```
coins=[1,2,5], amount=5

CRUCIAL: coins as outer loop prevents counting [1,2] and [2,1] separately.

dp[0]=1
Process coin=1:
  dp[1]+=dp[0]=1, dp[2]+=dp[1]=1, dp[3]+=1, dp[4]+=1, dp[5]+=1
  dp=[1,1,1,1,1,1]
Process coin=2:
  dp[2]+=dp[0]=1→2, dp[3]+=dp[1]=1→2, dp[4]+=dp[2]=2→3, dp[5]+=dp[3]=2→3
  dp=[1,1,2,2,3,3]
Process coin=5:
  dp[5]+=dp[0]=1→4
  dp=[1,1,2,2,3,4]
Answer=4 ✓ (combinations: [1,1,1,1,1],[1,1,1,2],[1,2,2],[5])
```

### Word Break (LC 139)
```
dp[i] = true if s[0..i-1] can be segmented using words in wordDict

dp[0] = true (empty string)
For i = 1 to n:
  For j = 0 to i-1:
    If dp[j] == true AND s[j..i-1] ∈ wordDict:
      dp[i] = true; break

This is Unbounded Knapsack where "words" are coins of varying length.
```

---

## 4. Combinations vs Permutations — Critical Distinction

```
COMBINATIONS (order doesn't matter): [1,2] == [2,1]
  → Coins as OUTER loop, amounts as INNER loop

  for (int coin : coins)
    for (int w = coin; w <= amount; w++)

PERMUTATIONS (order matters): [1,2] ≠ [2,1], count both
  → Amounts as OUTER loop, coins as INNER loop

  for (int w = 1; w <= amount; w++)
    for (int coin : coins)
      if (coin <= w) dp[w] += dp[w - coin];

EXAMPLE: coins=[1,2], amount=3
  Combinations: [1,1,1],[1,2],[3] → 2 ways (no 3... wait: {1,1,1},{1,2}) → 2
  Permutations: [1,1,1],[1,2],[2,1] → 3 ways

Coin Change II asks for COMBINATIONS (outer=coins).
LC 377 Combination Sum IV asks for PERMUTATIONS (outer=amounts).
```

---

## 5. Complexity Analysis

| Problem | Time | Space |
|---|---|---|
| Coin Change (min) | O(n × W) | O(W) |
| Coin Change II (count) | O(n × W) | O(W) |
| Rod Cutting | O(n²) | O(n) |
| Word Break | O(n² × L) | O(n) |
| Combination Sum IV | O(n × W) | O(W) |

---

## 6. Comparison: 0/1 vs Unbounded

| Property | 0/1 Knapsack | Unbounded Knapsack |
|---|---|---|
| Item reuse | At most once | Unlimited |
| Inner loop direction | BACKWARDS (W → wt[i]) | FORWARDS (wt[i] → W) |
| Why? | Prevent reusing current item | Allow reusing current item |
| Example | Partition Subset Sum | Coin Change |

---

## 7. Common Pitfalls

```java
// PITFALL 1: Using backwards iteration for unbounded
// If you go backwards, each item is used at most once → becomes 0/1 Knapsack!

// PITFALL 2: Coin Change — initializing dp[0] = 0 AFTER Arrays.fill
// WRONG: Arrays.fill(dp, 0); dp[0] = 0; (no-op since already 0)
// CORRECT: Arrays.fill(dp, amount+1); dp[0] = 0;  ← amount+1 acts as infinity

// PITFALL 3: Coin Change II — permutations vs combinations loop order
// Always ask: does order matter?

// PITFALL 4: Rod Cutting 0-indexed vs 1-indexed
// price[cut] is 1-indexed (cut from 1 to n). Be consistent.

// PITFALL 5: Word Break — substring creation is O(L)
// Total: O(n^2 * L) where L = average word length. Use a HashSet for O(1) lookup.
```

---

## 8. Interview Communication Script

```
"This is an Unbounded Knapsack problem. Each item can be reused unlimited times.

 State: dp[w] = [min coins / max value / # ways] for amount/capacity w.
 Transition: for each item, iterate w FORWARDS from item_weight to W:
   dp[w] = best_of(dp[w], dp[w - wt[i]] + contribution[i])

 The FORWARD iteration allows the same item to be used again in this pass.
 This is opposite to 0/1 Knapsack which iterates BACKWARDS.

 Time: O(n × W). Space: O(W)."
```

*Next: Basic (Coin Change), Medium (Coin Change II / Word Break), Hard (Combination Sum IV / Rod Cutting).*
