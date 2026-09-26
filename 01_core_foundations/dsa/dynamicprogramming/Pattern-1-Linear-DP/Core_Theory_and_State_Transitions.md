# Pattern 1 — Linear DP (1D)
> **Core Idea:** `dp[i]` = the optimal answer for the first `i` elements (or up to index `i`).
> Each state depends only on a fixed number of prior states — no 2D table needed.

---

## 1. The Mental Model

```
"The answer for position i can be derived from answers at i-1, i-2, etc."

Step 1: Define dp[i] clearly (what does it represent?)
Step 2: Write the recurrence: dp[i] = f(dp[i-1], dp[i-2], ..., input[i])
Step 3: Identify base cases (dp[0], dp[1])
Step 4: Determine iteration direction (left-to-right unless otherwise needed)
Step 5: Space-optimize if dp[i] only depends on a constant number of prior states
```

**Key signal words:** Fibonacci-like growth, "climbing stairs", "can't pick adjacent",
"decode ways", "max subarray", "number of ways to reach position i".

---

## 2. Canonical Templates

### Template A: dp[i] from dp[i-1] only (Fibonacci style)
```java
int prev2 = BASE_0, prev1 = BASE_1;
for (int i = 2; i <= n; i++) {
    int curr = prev1 + prev2;          // or any f(prev1, prev2, input[i])
    prev2 = prev1;
    prev1 = curr;
}
return prev1;
```

### Template B: dp[i] from dp[i-1], choice of take/skip
```java
// House Robber style: can't take two adjacent elements
int take = nums[0];       // dp[i][robbed]
int skip = 0;             // dp[i][not robbed]
for (int i = 1; i < n; i++) {
    int newTake = skip + nums[i];      // rob i: came from skip[i-1]
    int newSkip = Math.max(skip, take);// skip i: best of previous two
    take = newTake;
    skip = newSkip;
}
return Math.max(take, skip);
```

### Template C: dp[i] = min/max over all j < i (O(n²) transitions)
```java
// LIS style: dp[i] = length of LIS ending at index i
int[] dp = new int[n];
Arrays.fill(dp, 1);
for (int i = 1; i < n; i++) {
    for (int j = 0; j < i; j++) {
        if (arr[j] < arr[i])
            dp[i] = Math.max(dp[i], dp[j] + 1);
    }
}
return Arrays.stream(dp).max().getAsInt();
```

### Template D: Kadane's Algorithm (max subarray)
```java
// dp[i] = max subarray sum ending at index i
int curMax = nums[0], globalMax = nums[0];
for (int i = 1; i < n; i++) {
    curMax = Math.max(nums[i], curMax + nums[i]);  // extend or restart
    globalMax = Math.max(globalMax, curMax);
}
return globalMax;
```

---

## 3. State Transition Diagrams

### Climbing Stairs (LC 70)
```
dp[i] = number of ways to reach step i

dp[0] = 1  (one way to be at ground: do nothing)
dp[1] = 1  (one way: one 1-step)
dp[i] = dp[i-1] + dp[i-2]  (come from i-1 via 1-step, or from i-2 via 2-step)

Visual (n=4):
  dp[0]=1, dp[1]=1, dp[2]=2, dp[3]=3, dp[4]=5
  (Fibonacci sequence shifted by 1)
```

### House Robber (LC 198)
```
dp[i][0] = max loot NOT robbing house i
dp[i][1] = max loot robbing house i

dp[i][0] = max(dp[i-1][0], dp[i-1][1])   // best of previous two states
dp[i][1] = dp[i-1][0] + nums[i]           // must come from not-robbing i-1

Base: dp[0][0]=0, dp[0][1]=nums[0]
Answer: max(dp[n-1][0], dp[n-1][1])

Space-optimized:
  skip = max(skip, take)
  take = old_skip + nums[i]
```

### Longest Increasing Subsequence (LC 300)
```
dp[i] = length of LIS ending exactly at index i

dp[i] = 1 + max(dp[j]) for all j < i where nums[j] < nums[i]
Base: dp[i] = 1 (LIS of just element i)
Answer: max(dp[i]) over all i

Visual for [10,9,2,5,3,7,101,18]:
  i=0: dp[0]=1 (LIS: [10])
  i=1: dp[1]=1 (LIS: [9]; 9<10 so can't extend dp[0])
  i=2: dp[2]=1 (LIS: [2])
  i=3: dp[3]=2 (LIS: [2,5])
  i=4: dp[4]=2 (LIS: [2,3])
  i=5: dp[5]=3 (LIS: [2,3,7] or [2,5,7])
  i=6: dp[6]=4 (LIS: [2,3,7,101])
  i=7: dp[7]=4 (LIS: [2,3,7,18])
  Answer: 4
```

### Decode Ways (LC 91)
```
dp[i] = number of ways to decode s[0..i-1]

Single digit decode: if s[i-1] != '0': dp[i] += dp[i-1]
Two digit decode:    if s[i-2..i-1] in [10,26]: dp[i] += dp[i-2]

Base: dp[0]=1 (empty string: 1 way), dp[1]= (s[0]!='0' ? 1 : 0)
```

---

## 4. Space Optimization Guide

| `dp[i]` depends on | Keep | Space |
|---|---|---|
| `dp[i-1]` only | 1 variable | O(1) |
| `dp[i-1]`, `dp[i-2]` | 2 variables (prev1, prev2) | O(1) |
| `dp[i-1]`, `dp[i-2]`, `dp[i-3]` | 3 variables | O(1) |
| All `dp[j]` for j < i | Full array | O(n) |

---

## 5. Complexity Analysis

| Problem | Time | Space | Optimized Space |
|---|---|---|---|
| Fibonacci / Climbing Stairs | O(n) | O(n) | O(1) |
| House Robber | O(n) | O(n) | O(1) |
| Max Subarray (Kadane's) | O(n) | O(1) | O(1) |
| Decode Ways | O(n) | O(n) | O(1) |
| LIS (DP only) | O(n²) | O(n) | O(n) |
| LIS (DP + binary search) | O(n log n) | O(n) | O(n) |

---

## 6. Common Pitfalls

```java
// PITFALL 1: Wrong base case — dp[0] vs dp[1]
// Climbing Stairs: dp[0]=1 (not dp[0]=0)
// House Robber: take=nums[0], skip=0 (not both 0)

// PITFALL 2: Off-by-one in Decode Ways
// s is 1-indexed in DP: s.charAt(i-1), s.charAt(i-2)
// Array size: dp[n+1] (one extra for empty string)

// PITFALL 3: Mutation of rolling variables in wrong order
// WRONG: skip = max(skip,take); take = skip + nums[i];  ← uses NEW skip!
// RIGHT: int oldSkip = skip; skip = max(skip,take); take = oldSkip + nums[i];

// PITFALL 4: LIS — forgetting to initialize dp[i]=1
// Every element alone is an LIS of length 1.

// PITFALL 5: Kadane's with all-negative array
// Return the max single element (not 0).
// Initialize: curMax = globalMax = nums[0] (not 0).
```

---

## 7. Interview Communication Script

```
"This is a Linear DP problem. dp[i] represents [what it means].
 The recurrence is: dp[i] = [formula from prior states].
 Base cases: dp[0]=[value] because [reason].
 I'll iterate from left to right, and since dp[i] only depends on
 dp[i-1] (and dp[i-2]), I can optimize space to O(1) using variables."
```

*Next: Basic (Climbing Stairs), Medium (House Robber), Hard (LIS).*
