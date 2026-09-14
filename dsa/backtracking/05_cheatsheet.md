# Recursion & Backtracking — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet for recursion and backtracking.

---

## Pattern Recognition Table

| If the problem mentions... | Think... | Key Technique |
|---|---|---|
| "All subsets / power set" | Inclusion / Exclusion | `backtrack(start + 1)` |
| "All permutations" | Visited Array | `boolean[] used`, loop `i = 0` to $N-1$ |
| "Subsets/Permutations with duplicates" | Sort + Sister Skip | `Arrays.sort(nums)` + `if (i > start && nums[i] == nums[i-1]) continue` |
| "Combinations of size K" | Fixed Length Bounds | Loop `i` up to $N - (K - \text{curr.size()}) + 1$ |
| "Generate valid parentheses" | Open/Close Counter | Add '(' if `open < n`, add ')' if `close < open` |
| "Grid word search / path" | Cell Marking DFS | `board[r][c] = '#'` in-place, restore before return |
| "N-Queens / Sudoku collision" | Bitmasking | Bitmask cols, diag1 (`r-c+n`), diag2 (`r+c`) |
| "Partition K equal sum subsets" | Bucket Filling + Sort | Sort descending, fill $K$ buckets, prune empty bucket |
| "String expression operators" | Evaluated State Tracking | Track `eval` and `prevOperand` to handle multiplication |

---

## The Four Templates (Memorize These)

### Template 1: Subsets & Combinations (Distinct Elements)

```java
private void backtrack(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
    result.add(new ArrayList<>(current)); // Deep Copy Snapshot!

    for (int i = start; i < nums.length; i++) {
        current.add(nums[i]);            // Choose
        backtrack(i + 1, nums, current, result); // Explore
        current.remove(current.size() - 1); // Un-choose
    }
}
```

### Template 2: Duplicate Handling (Subsets II / Permutations II)

```java
Arrays.sort(nums); // Mandatory step!

private void backtrack(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
    result.add(new ArrayList<>(current));

    for (int i = start; i < nums.length; i++) {
        // Skip duplicate choices at the exact same depth level
        if (i > start && nums[i] == nums[i - 1]) continue;

        current.add(nums[i]);
        backtrack(i + 1, nums, current, result);
        current.remove(current.size() - 1);
    }
}
```

### Template 3: Grid Cell In-Place Marking Backtracking

```java
private boolean dfs(char[][] board, int r, int c, String word, int index) {
    if (index == word.length()) return true;
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] != word.charAt(index)) {
        return false;
    }

    char temp = board[r][c];
    board[r][c] = '#'; // Mark cell visited in-place

    for (int[] d : DIRS) {
        if (dfs(board, r + d[0], c + d[1], word, index + 1)) {
            board[r][c] = temp; // Restore before returning true
            return true;
        }
    }

    board[r][c] = temp; // Un-choose / Backtrack
    return false;
}
```

### Template 4: Bitmask N-Queens Diagonal Tracking

```java
private void backtrackQueens(int row, int n, boolean[] cols, boolean[] d1, boolean[] d2) {
    if (row == n) { count++; return; }

    for (int col = 0; col < n; col++) {
        int id1 = row - col + n;
        int id2 = row + col;

        if (cols[col] || d1[id1] || d2[id2]) continue; // O(1) Collision Check

        cols[col] = d1[id1] = d2[id2] = true;
        backtrackQueens(row + 1, n, cols, d1, d2);
        cols[col] = d1[id1] = d2[id2] = false;
    }
}
```

---

## 30-Second Interview Deep-Copy Alert

> *"When collecting paths in Java backtracking, `result.add(path)` adds a reference to the mutable list. As the recursion unwinds and `path.remove()` is called, all stored references become empty! Always pass `result.add(new ArrayList<>(path))`."*
