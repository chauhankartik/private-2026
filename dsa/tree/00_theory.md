# Binary Tree / BST — Theory & Pattern Guide
> **Study goal:** Master every tree traversal, recursion pattern, and BST property
> needed for Google/FAANG interviews.

---

## 1. Core Data Structure

```
        1            ← root (depth 0)
       / \
      2   3          ← depth 1
     / \   \
    4   5   6        ← depth 2  (leaves: 4, 5, 6)
```

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int v) { val = v; }
}
```

**Vocabulary:**
- **Height** of node = longest path to a leaf below it. Leaf height = 0.
- **Depth** of node = distance from root. Root depth = 0.
- **Level** = set of nodes at the same depth.
- **Full binary tree** = every node has 0 or 2 children.
- **Complete binary tree** = all levels filled except possibly last, filled left to right.
- **Perfect binary tree** = all internal nodes have 2 children, all leaves at same depth.
- **Balanced** = height difference of left/right subtrees ≤ 1 for every node.

---

## 2. Traversals — The Foundation

### DFS Traversals (recursive & iterative)

```
Tree:   1
       / \
      2   3
     / \
    4   5

Preorder   (Root → Left → Right):  1  2  4  5  3
Inorder    (Left → Root → Right):  4  2  5  1  3  ← sorted for BST!
Postorder  (Left → Right → Root):  4  5  2  3  1
```

```java
// RECURSIVE (clean, O(n) time, O(h) stack space)
void preorder(TreeNode node)  { visit(node); preorder(node.left);  preorder(node.right); }
void inorder(TreeNode node)   { inorder(node.left); visit(node); inorder(node.right); }
void postorder(TreeNode node) { postorder(node.left); postorder(node.right); visit(node); }

// ITERATIVE preorder (explicit stack)
Stack<TreeNode> stack = new Stack<>();
stack.push(root);
while (!stack.isEmpty()) {
    TreeNode node = stack.pop();
    visit(node);
    if (node.right != null) stack.push(node.right);  // right first (LIFO order)
    if (node.left  != null) stack.push(node.left);
}

// ITERATIVE inorder (Morris Traversal for O(1) space — see advanced file)
```

### BFS / Level-Order Traversal

```java
Queue<TreeNode> q = new LinkedList<>();
q.offer(root);
while (!q.isEmpty()) {
    int size = q.size();                // snapshot of THIS level's size
    for (int i = 0; i < size; i++) {
        TreeNode node = q.poll();
        visit(node);                    // process node at this level
        if (node.left  != null) q.offer(node.left);
        if (node.right != null) q.offer(node.right);
    }
    // end of one level
}
```

---

## 3. The Five Core Patterns

### Pattern 1: Recursive DFS (bottom-up)
```
The function returns a value computed from both subtrees.
f(node) = combine(f(node.left), f(node.right), node.val)
```
Used for: height, diameter, path sum, LCA, balance check.

### Pattern 2: BFS / Level-Order
```
Process level by level using a Queue.
```
Used for: level averages, right side view, zigzag traversal, min depth.

### Pattern 3: BST Property
```
Left subtree values < node.val < Right subtree values
```
Used for: search, insert, delete, kth smallest, validate BST, range sum.

### Pattern 4: Path Problems
```
Two types of paths:
  (a) Root-to-leaf path: must start at root, end at leaf.
  (b) Any node-to-node path: can start and end anywhere (↔ tree diameter pattern).
```
For (b): at each node, consider path going THROUGH it = left_gain + right_gain + node.val.
          Update global max, but return max(left_gain, right_gain) + node.val to parent.

### Pattern 5: Serialize / Reconstruct
```
Preorder + Inorder → Unique tree  (two-sequence reconstruction)
Preorder alone → Unique tree (if null markers included)
```

---

## 4. BST vs Binary Tree

| Property | Binary Tree | BST |
|---|---|---|
| Structure | No ordering constraint | Left < Root < Right |
| Search | O(n) worst | O(h) = O(log n) balanced, O(n) skewed |
| Insert | Arbitrary | O(h) |
| Delete | Arbitrary | O(h) |
| Min/Max | O(n) | O(h) — go all left / all right |
| Successor | O(n) | O(h) — go right then leftmost |
| Inorder | arbitrary order | SORTED sequence ✓ |

---

## 5. Height vs Depth

```
Tree:
     A      depth=0, height=2
    / \
   B   C    depth=1, height=1 (B), 0 (C)
  / \
 D   E      depth=2, height=0 (leaves)
```

**Height of tree** = height of root = max depth of any leaf.

---

## 6. Recursion Template

```java
// Template for bottom-up recursive problems
ReturnType dfs(TreeNode node) {
    if (node == null) return BASE_CASE;        // base case

    ReturnType left  = dfs(node.left);          // solve left subtree
    ReturnType right = dfs(node.right);         // solve right subtree

    // ← at this point, both subtrees are solved
    // Update a global answer if needed:
    //   ans = max(ans, combine(left, right, node.val))
    // Return something useful to the parent:
    return combine(left, right, node.val);       // could be different from ans update
}
```

The KEY distinction: what you **update globally** vs what you **return to parent** can be different.
- Diameter: globally update `left + right`, but return `max(left, right) + 1`.
- Path Sum: globally update `left + right + node`, but return `max(left, right) + node`.

---

## 7. Common Bugs

```java
// Bug 1: Forgetting to handle null (always check null first)
int height(TreeNode node) {
    if (node == null) return -1;  // or 0 depending on convention
    ...
}

// Bug 2: Confusing height convention
// Some use: leaf height = 0, null height = -1
// Some use: leaf height = 1, null height = 0
// Pick one and be consistent. LC usually uses: leaf = 1, null = 0.

// Bug 3: Off-by-one in level order
// ALWAYS snapshot q.size() before the inner loop — don't call q.size() inside the loop

// Bug 4: Modifying the BST while traversing — use a copy or careful pointer management

// Bug 5: Using global variable across test cases — reset before each call
```

---

## 8. Interview Frequency

| # | Problem | Pattern | Freq |
|---|---|---|---|
| 1 | Max Depth / Height | Recursive DFS | ⭐⭐⭐⭐⭐ |
| 2 | Invert Binary Tree | Recursive | ⭐⭐⭐⭐⭐ |
| 3 | Validate BST | DFS with range | ⭐⭐⭐⭐⭐ |
| 4 | Level Order Traversal | BFS | ⭐⭐⭐⭐⭐ |
| 5 | Lowest Common Ancestor | Recursive DFS | ⭐⭐⭐⭐⭐ |
| 6 | Binary Tree Diameter | Bottom-up DFS | ⭐⭐⭐⭐ |
| 7 | Path Sum I/II/III | DFS | ⭐⭐⭐⭐ |
| 8 | Symmetric Tree | Recursive/BFS | ⭐⭐⭐⭐ |
| 9 | Serialize/Deserialize | DFS/BFS | ⭐⭐⭐⭐ |
| 10 | Right Side View | BFS | ⭐⭐⭐⭐ |
| 11 | Kth Smallest in BST | Inorder | ⭐⭐⭐⭐ |
| 12 | Construct from Preorder+Inorder | Divide | ⭐⭐⭐ |
| 13 | Flatten to Linked List | Postorder | ⭐⭐⭐ |
| 14 | Max Path Sum | Bottom-up DFS | ⭐⭐⭐ |

---

*Next:*
- `01_easy.java` — Depth, invert, symmetric, level order, path sum I, same tree
- `02_medium.java` — BFS views, diameter, LCA, validate BST, kth smallest, construct
- `03_hard.java` — Serialize, max path sum, Morris traversal, flatten to LL
- `04_google_level.java` — BST iterator, count complete nodes, vertical order, good nodes
