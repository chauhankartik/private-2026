# Trie (Prefix Tree) — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet for Trie problems.

---

## Pattern Recognition Table

| If the problem mentions... | Think... | Key Technique |
|---|---|---|
| "Prefix matching", "starts-with" | Standard Array Trie | `TrieNode[26]` with `isEnd` flag |
| "Maximum XOR pair / subarray" | Bitwise (Binary) Trie | 32-bit `BinaryNode[2]` with opposite bit query (`bit ^ 1`) |
| "Suffix matching", "Ends with" | Reverse Suffix Trie | Insert reversed string `w.reverse()` into Trie |
| "Search with '.' wildcard" | Trie + DFS Backtracking | Branch over all non-null children when `ch == '.'` |
| "Word Search II / Grid Boggle" | Trie + Grid Backtracking | DFS on grid, store word at terminal node, prune refs |
| "Top 3 autocompletion suggestions" | Trie Node Candidates List | Store top 3 items or PriorityQueue directly in `TrieNode` |
| "Suffix & Prefix wrapped query" | Wrapped Trie | Insert `suffix + '{' + prefix` into Trie |
| "Sum of prefix scores / Map sum" | Pass-through Counter Trie | Increment `node.passCount` on insert |
| "Delete duplicate folder subtrees" | Trie Subtree Hashing | Bottom-up serialization `folder + "(" + children + ")"` |

---

## The Four Templates (Memorize These)

### Template 1: Standard Array Trie (`TrieNode[26]`)

```java
class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEnd = false;
}

public void insert(String word) {
    TrieNode curr = root;
    for (char ch : word.toCharArray()) {
        int idx = ch - 'a';
        if (curr.children[idx] == null) curr.children[idx] = new TrieNode();
        curr = curr.children[idx];
    }
    curr.isEnd = true;
}
```

### Template 2: Bitwise (Binary 32-bit) Trie for Maximum XOR

```java
class BinaryNode {
    BinaryNode[] children = new BinaryNode[2];
}

public void insert(int num) {
    BinaryNode curr = root;
    for (int i = 31; i >= 0; i--) {
        int bit = (num >> i) & 1;
        if (curr.children[bit] == null) curr.children[bit] = new BinaryNode();
        curr = curr.children[bit];
    }
}

public int getMaxXor(int num) {
    BinaryNode curr = root;
    int maxXor = 0;
    for (int i = 31; i >= 0; i--) {
        int bit = (num >> i) & 1;
        int opp = bit ^ 1;
        if (curr.children[opp] != null) {
            maxXor |= (1 << i);
            curr = curr.children[opp];
        } else {
            curr = curr.children[bit];
        }
    }
    return maxXor;
}
```

### Template 3: Reverse Suffix Trie Streaming (Stream of Characters)

```java
public void insertReversed(String word) {
    Node curr = root;
    for (int i = word.length() - 1; i >= 0; i--) {
        int idx = word.charAt(i) - 'a';
        if (curr.children[idx] == null) curr.children[idx] = new Node();
        curr = curr.children[idx];
    }
    curr.isEnd = true;
}
```

### Template 4: Trie Grid Backtracking with Node Pruning (Word Search II)

```java
private void dfs(char[][] board, int r, int c, TrieNode parent) {
    char ch = board[r][c];
    TrieNode curr = parent.children[ch - 'a'];
    if (curr == null || curr.refs == 0) return;

    if (curr.word != null) {
        result.add(curr.word);
        curr.word = null; // Avoid duplicate match
    }

    board[r][c] = '#'; // Mark visited
    for (int[] d : DIRS) {
        int nr = r + d[0], nc = c + d[1];
        if (inBounds(nr, nc) && board[nr][nc] != '#') {
            dfs(board, nr, nc, curr);
        }
    }
    board[r][c] = ch; // Backtrack
}
```

---

## 30-Second Interview Trade-off Argument

1. **Why Trie over HashMap?** "Prefix queries on HashMap require scanning all $N$ keys ($O(N \cdot M)$). Trie gives guaranteed $O(M)$ lookup for both prefixes and exact matches."
2. **Why Array vs Map Nodes?** "`TrieNode[26]` gives $O(1)$ child indexing with zero hashing overhead and optimal CPU cache line prefetching. `Map<Character, TrieNode>` is preferred when alphabet $\Sigma$ is large or sparse."
3. **Memory Overhead Awareness:** "A 64-bit JVM node with 26 pointers costs $\approx 248$ bytes. For huge datasets, we can compress single-child chains into a Radix Tree."
