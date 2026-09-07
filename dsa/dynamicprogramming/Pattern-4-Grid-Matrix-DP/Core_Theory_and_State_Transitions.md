# Pattern 4 — Grid / Matrix DP (2D)
> **Core Idea:** `dp[i][j]` = the optimal answer for cell `(i, j)` in a 2D grid.
> Each cell depends on its top `dp[i-1][j]`, left `dp[i][j-1]`,
> and/or diagonal `dp[i-1][j-1]` neighbors.

---

## 1. The Mental Model

```
"The problem lives on a 2D grid. We process row by row, left to right.
 The answer at cell (i, j) depends only on nearby already-computed cells."

dp[i][j] = answer for cell (i, j) or for subgrid ending at (i, j)

Iteration: for i = 0 to m-1, for j = 0 to n-1 (top-left to bottom-right)
Space opt: process row by row — only need the previous row.
```

**Key signal words:** "grid", "matrix", "top-left to bottom-right",
"minimum path", "unique paths", "count paths", "largest square of 1s".

---

## 2. Canonical Templates

### Template A: Counting paths (Unique Paths)
```java
int[] dp = new int[n];
Arrays.fill(dp, 1);               // top row: all 1s
for (int i = 1; i < m; i++) {
    for (int j = 1; j < n; j++) {
        dp[j] += dp[j - 1];       // from above + from left
    }
}
return dp[n - 1];
```

### Template B: Minimum path sum
```java
int[][] dp = new int[m][n];
dp[0][0] = grid[0][0];
for (int j = 1; j < n; j++) dp[0][j] = dp[0][j-1] + grid[0][j];  // top row
for (int i = 1; i < m; i++) dp[i][0] = dp[i-1][0] + grid[i][0];  // left col
for (int i = 1; i < m; i++)
    for (int j = 1; j < n; j++)
        dp[i][j] = Math.min(dp[i-1][j], dp[i][j-1]) + grid[i][j];
return dp[m-1][n-1];
```

### Template C: Maximal Square
```java
// dp[i][j] = side length of largest square with bottom-right corner at (i,j)
int[] dp = new int[n + 1];
int prev = 0, ans = 0;
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        int temp = dp[j];
        if (matrix[i-1][j-1] == '1') {
            dp[j] = Math.min(prev, Math.min(dp[j], dp[j-1])) + 1;
            ans = Math.max(ans, dp[j]);
        } else dp[j] = 0;
        prev = temp;
    }
}
return ans * ans;
```

---

## 3. State Transition Diagrams

### Unique Paths (LC 62)
```
dp[i][j] = number of paths to reach (i,j) from (0,0), moving only right/down

Base: dp[0][j]=1 (top row), dp[i][0]=1 (left col) — only one path along edges
Transition: dp[i][j] = dp[i-1][j] + dp[i][j-1]

Grid (m=3, n=3):
  1  1  1
  1  2  3
  1  3  6  ← answer: 6
```

### Minimum Path Sum (LC 64)
```
dp[i][j] = min cost to reach (i,j) from (0,0)

Grid:    dp table:
1 3 1    1  4  5
1 5 1    2  7  6
4 2 1    6  8  7  ← answer: 7

Path: (0,0)→(0,1)→(0,2)→(1,2)→(2,2): 1+3+1+1+1=7
```

### Maximal Square (LC 221)
```
dp[i][j] = side length of largest all-1 square with bottom-right at (i,j)

Key insight: dp[i][j] = min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]) + 1

WHY MIN OF THREE?
  The square at (i,j) is limited by the smallest of:
  - Square above:    dp[i-1][j]    (limits the height)
  - Square to left:  dp[i][j-1]   (limits the width)
  - Square diagonal: dp[i-1][j-1] (limits both)

Matrix:       dp:
1 0 1 0 0     1 0 1 0 0
1 0 1 1 1     1 0 1 1 1
1 1 1 1 1     1 1 1 2 2
1 0 0 1 0     1 0 0 1 0
← max dp value = 2, area = 4
```

### Unique Paths II (LC 63) — with obstacles
```
If grid[i][j] == 1 (obstacle): dp[i][j] = 0
Else: dp[i][j] = dp[i-1][j] + dp[i][j-1]

Handle blocked cells in top row / left col:
  If any cell in top row is blocked, all cells to its right = 0
  Same for left column.
```

---

## 4. Space Optimization: 2D → 1D

```
FULL 2D:  dp[i][j] depends on dp[i-1][j] (above) and dp[i][j-1] (left)
  → Keep full m×n array: O(m×n) space

1D OPTIMIZATION:
  dp[j] = current row's value at column j
  Before updating: dp[j] still holds PREVIOUS ROW's value at column j (= "above")
  After updating left neighbors: dp[j-1] holds CURRENT ROW's value (= "left")

  for (int i = 1; i < m; i++) {
      for (int j = 1; j < n; j++) {
          dp[j] = f(dp[j],     ← above (old value, from previous row)
                    dp[j-1]);  ← left  (new value, from current row pass)
      }
  }
  → O(n) space

For DIAGONAL dependency (dp[i-1][j-1]):
  Need an extra variable 'prev' to save dp[j] BEFORE it's updated:
  int prev = dp[j];  // save dp[i-1][j-1] before overwriting
  dp[j] = f(dp[j], dp[j-1], prev);
```

---

## 5. Complexity Analysis

| Problem | Time | Space | Optimized Space |
|---|---|---|---|
| Unique Paths | O(m × n) | O(m × n) | O(n) |
| Min Path Sum | O(m × n) | O(m × n) | O(n) |
| Maximal Square | O(m × n) | O(m × n) | O(n) |
| Unique Paths II | O(m × n) | O(m × n) | O(n) |
| Cherry Pickup (hard) | O(n³) | O(n³) | O(n²) |

---

## 6. Common Pitfalls

```java
// PITFALL 1: Forgetting base cases for top row and left column
// dp[0][j] = dp[0][j-1] + grid[0][j]  (only can come from left)
// dp[i][0] = dp[i-1][0] + grid[i][0]  (only can come from above)
// DON'T apply the min/max formula to the first row/column!

// PITFALL 2: Obstacle blocking in Unique Paths II
// If grid[0][j] == 1: all dp[0][k] for k >= j must be 0 (can't pass through).
// Check in a single pass left-to-right, tracking if blocked.

// PITFALL 3: Maximal Square — not saving diagonal predecessor
// Need 'prev' variable when space-optimizing to get dp[i-1][j-1].

// PITFALL 4: 1-indexed vs 0-indexed for matrix DP
// Grid is 0-indexed (m rows, n cols), but dp table might be (m+1)×(n+1) for cleaner base.
// Be consistent!

// PITFALL 5: Off-by-one when reading grid vs dp cell
// If dp[i][j] refers to grid[i-1][j-1] (1-indexed DP), use grid[i-1][j-1].
```

---

## 7. Interview Communication Script

```
"This is a Grid DP problem. The answer at each cell depends on adjacent
 previously-computed cells — top and left (and sometimes diagonal).

 State: dp[i][j] = [what it means for cell (i,j)]
 Transition: dp[i][j] = f(dp[i-1][j], dp[i][j-1], ...)
 Base cases: first row (all from left only) and first column (all from above only).
 Iteration: row by row, left to right.
 Space: O(n) by keeping only the current and previous rows."
```

*Next: Basic (Unique Paths), Medium (Min Path Sum), Hard (Maximal Square).*
