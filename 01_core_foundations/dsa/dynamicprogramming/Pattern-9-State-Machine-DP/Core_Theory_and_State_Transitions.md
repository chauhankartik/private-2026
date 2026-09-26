# Pattern 9 — State Machine DP
> **Core Idea:** Model a problem as a finite state machine.
> Each state captures a meaningful "mode" (e.g., holding a stock, in cooldown).
> At each time step, transition between states based on allowed actions.

---

## 1. The Mental Model

```
State Machine DP = DP where the "state" is a discrete operational mode.

At each time step t, you are in one of k states.
You choose an action (or it's forced), which transitions you to a new state.
Each action has a cost/reward.

Goal: maximize total reward (or minimize cost) over all time steps.

dp[t][s] = best total reward up to time t when ending in state s
Transition: dp[t][s'] = max over all (s, action) that lead to s' of:
              dp[t-1][s] + reward(t, action)
```

**Key signal words:** "buy/sell with constraints", "cooldown period", "at most k transactions",
"hold/not-hold", "locked states", "finite number of modes with limited transitions".

---

## 2. The Stock Problem Family — A Complete Case Study

This one family covers all State Machine DP patterns:

### Variant 1: One Transaction (LC 121)
```
States: {HOLD, SOLD}

HOLD:  best profit if holding a stock at day i
SOLD:  best profit if not holding at day i

Transitions:
  HOLD[i] = max(HOLD[i-1],   -prices[i])   // keep holding OR buy today
  SOLD[i] = max(SOLD[i-1],  HOLD[i-1] + prices[i]) // keep sold OR sell today

Base: HOLD[0] = -prices[0], SOLD[0] = 0
Answer: SOLD[n-1]
```

### Variant 2: Unlimited Transactions (LC 122)
```
Same as Variant 1 — unlimited means we can buy again immediately after selling.
(No difference in state machine; SOLD[i] can feed into HOLD[i] same day conceptually,
 but the DP handles it correctly because we look at SOLD[i-1])

HOLD[i] = max(HOLD[i-1], SOLD[i-1] - prices[i])
SOLD[i] = max(SOLD[i-1], HOLD[i-1] + prices[i])
```

### Variant 3: With Cooldown (LC 309)
```
States: {HOLD, SOLD, REST}
  HOLD  = holding a stock
  SOLD  = just sold (must rest next day, entering cooldown)
  REST  = in cooldown / resting (can buy tomorrow)

Transitions:
  HOLD[i] = max(HOLD[i-1],   REST[i-1] - prices[i])   // keep holding OR buy (only from REST)
  SOLD[i] = HOLD[i-1] + prices[i]                      // must sell from HOLD
  REST[i] = max(REST[i-1],   SOLD[i-1])               // carry over rest OR cooldown from SOLD

Base: HOLD[0]=-prices[0], SOLD[0]=0, REST[0]=0
Answer: max(SOLD[n-1], REST[n-1])
```

### Variant 4: With Transaction Fee (LC 714)
```
States: {HOLD, FREE}
  HOLD = holding a stock
  FREE = not holding (free to buy)

Fee is charged on each sell:
  HOLD[i] = max(HOLD[i-1], FREE[i-1] - prices[i])
  FREE[i] = max(FREE[i-1], HOLD[i-1] + prices[i] - fee)  // pay fee on sell

Answer: FREE[n-1]
```

### Variant 5: At Most K Transactions (LC 188)
```
States: dp[i][k][0/1]
  dp[i][k][0] = max profit on day i, with at most k transactions remaining, NOT holding
  dp[i][k][1] = max profit on day i, with at most k transactions remaining, HOLDING

Transitions:
  dp[i][k][0] = max(dp[i-1][k][0], dp[i-1][k][1] + prices[i])   // rest or sell
  dp[i][k][1] = max(dp[i-1][k][1], dp[i-1][k-1][0] - prices[i]) // rest or buy (uses 1 transaction)

Base: dp[0][k][0]=0, dp[0][k][1]=-prices[0] for all k
Answer: dp[n-1][K][0]

Space optimization: iterate k from K down to 1.
Special case: if 2K >= n, unlimited transactions suffice (greedy).
```

---

## 3. General State Machine Template

```java
// k states, n time steps
int[][] dp = new int[n][k];

// Initialize base case (day 0)
for (int s = 0; s < k; s++) dp[0][s] = baseCase(s);

// Fill for days 1..n-1
for (int i = 1; i < n; i++) {
    for (int s = 0; s < k; s++) {
        dp[i][s] = Integer.MIN_VALUE;
        // Try all states that can transition to state s
        for (int prev : statesThatLeadTo(s)) {
            dp[i][s] = Math.max(dp[i][s],
                dp[i-1][prev] + reward(i, prev, s));
        }
    }
}

return bestOf(dp[n-1]);
```

### Space-Optimized (O(1) when only prev day matters)
```java
// Only keep current and previous day's values
int hold = -prices[0], rest = 0, sold = 0;

for (int i = 1; i < n; i++) {
    int prevHold = hold, prevRest = rest, prevSold = sold;
    hold = Math.max(prevHold, prevRest - prices[i]);
    sold = prevHold + prices[i];
    rest = Math.max(prevRest, prevSold);
}
return Math.max(sold, rest);
```

---

## 4. State Transition Diagrams

### Stock with Cooldown
```
     buy              sell
REST ───────> HOLD ──────────> SOLD
  ^                               |
  └───────── (cooldown day) ──────┘
  (REST -> REST also allowed)
  (HOLD -> HOLD allowed: keep holding)

Arrows show valid transitions. Invalid: REST -> SOLD directly, SOLD -> HOLD directly.
```

### At Most 2 Transactions (LC 123)
```
States per day: {noHold0, hold1, sold1, hold2, sold2}
  noHold0 = never transacted yet
  hold1   = in first buy (holding)
  sold1   = completed first sell (between transactions)
  hold2   = in second buy
  sold2   = completed second sell

Transitions (day i):
  noHold0 = noHold0          (do nothing)
  hold1   = max(hold1, noHold0 - prices[i])   (buy for 1st time)
  sold1   = max(sold1, hold1 + prices[i])     (sell 1st stock)
  hold2   = max(hold2, sold1 - prices[i])     (buy 2nd time)
  sold2   = max(sold2, hold2 + prices[i])     (sell 2nd stock)

Answer: sold2
```

---

## 5. Complexity Analysis

| Variant | Time | Space | Optimized Space |
|---|---|---|---|
| One transaction | O(n) | O(n) | O(1) |
| Unlimited | O(n) | O(n) | O(1) |
| With cooldown | O(n) | O(n) | O(1) |
| With fee | O(n) | O(n) | O(1) |
| At most 2 | O(n) | O(n) | O(1) |
| At most k | O(n × k) | O(n × k) | O(k) |

---

## 6. Key Patterns Across All Variants

```
RULE 1: Identify states.
  "Hold" means you bought but haven't sold.
  "Free/Rest" means not holding and can buy.
  "Cooldown/Sold" means just sold, must wait.

RULE 2: A "transaction" = one buy + one sell.
  When counting k transactions, decrement k on BUY (not sell).

RULE 3: If 2*k >= n, unlimited transactions = same result as k transactions.
  Use greedy (sum all positive daily differences).

RULE 4: Space optimization: only previous day matters.
  Use variables (prevHold, prevFree) instead of 2D array.

RULE 5: Answer is always the "not-holding" state at the end.
  You never end optimal by holding stock.
```

---

## 7. Other State Machine DP Problems

| Problem | States | Key Transition |
|---|---|---|
| Paint Fence (LC 276) | {sameColor, diffColor} | diffColor = (k-1)(same+diff), sameColor = diffColor |
| House Robber (linear) | {rob, skip} | rob[i]=skip[i-1]+val[i], skip[i]=max(rob[i-1],skip[i-1]) |
| Wiggle Subsequence | {up, down} | up=down+1 if arr[i]>arr[i-1]; down=up+1 if arr[i]<arr[i-1] |
| Best Sightseeing Pair | {maxLeft, answer} | Track max(arr[i]+i) rolling while computing answer |
| Jump Game VI | {state} | Sliding window max DP |

---

## 8. Common Pitfalls

```java
// PITFALL 1: Using same-day values in the same loop pass
// WRONG: hold = Math.max(hold, rest - prices[i]); rest = Math.max(rest, sold); ...
//   This uses the NEW 'hold' when computing 'rest' — wrong!
// CORRECT: save all previous values BEFORE updating:
//   int ph = hold, pr = rest, ps = sold;
//   hold = Math.max(ph, pr - prices[i]);
//   sold = ph + prices[i];
//   rest = Math.max(pr, ps);

// PITFALL 2: Counting transactions incorrectly
// A transaction = 1 buy + 1 sell. Decrement k on BUY:
//   dp[i][k][1] uses dp[i-1][k-1][0] (k-1 remaining after this buy)

// PITFALL 3: Not handling k >= n/2 edge case for LC 188
// When k >= n/2, treat as unlimited transactions (greedy).

// PITFALL 4: Base case for "hold" on day 0
// dp[0][holding] = -prices[0]  (bought on day 0)
// dp[0][not_holding] = 0

// PITFALL 5: Returning wrong terminal state
// Always return the "not-holding" state at the end.
// Holding at the end = no sell = no final profit from last buy.
```

---

## 9. Interview Communication Script

```
"This is a State Machine DP problem. The key is identifying the discrete
 operational states — in the stock problem these are HOLD, SOLD, and REST.

 State: dp[i][s] = max profit up to day i ending in state s
 Transition: (draw the state machine diagram)
   HOLD[i] = max(HOLD[i-1], REST[i-1] - prices[i])   // keep holding or buy
   SOLD[i] = HOLD[i-1] + prices[i]                    // sell
   REST[i] = max(REST[i-1], SOLD[i-1])               // cooldown or stay

 Space optimization: only yesterday's values matter -> O(1) space.
 Answer: max(SOLD[n-1], REST[n-1]) — never holding at the end.

 Time: O(n). Space: O(1)."
```

*Next: Basic (Stock Buy/Sell 1), Medium (Stock with Cooldown), Hard (Stock K Transactions).*
