# Pattern 8 — Tree DP
> **Core Idea:** Run DP on a rooted tree. Each node's answer depends on
> its children's answers. Post-order DFS (children before parent) is
> the natural iteration order.

---

## 1. The Mental Model

```
Tree DP = DFS + merging child results at each node

For every node u:
  1. Recurse into all children c of u → get dp[c]
  2. Combine dp[c] values to compute dp[u]
  3. Return dp[u] to u's parent

Key property: subtrees are INDEPENDENT subproblems.
  The subtree rooted at child c does not interact with the subtree of another child c'.
  This is the "optimal substructure" on trees.
```

**Key signal words:** "tree", "rooted tree", "path in tree", "subtree",
"select nodes with constraints (no adjacent)", "place cameras", "longest path".

---

## 2. Two Styles of Tree DP

### Style A: Single value per node — dp[node]
```java
// dp[node] = some scalar value for the subtree rooted at node
int dfs(int node, int parent, int[] vals, List<List<Integer>> adj) {
    int result = vals[node];      // start with node's own contribution
    for (int child : adj.get(node)) {
        if (child == parent) continue;
        result = combine(result, dfs(child, node, vals, adj));
    }
    dp[node] = result;
    return result;
}
```

### Style B: Multiple states per node — dp[node][state]
```java
// dp[node][0] = best answer when node is NOT selected
// dp[node][1] = best answer when node IS selected
// (generalizable to more states)
int[] dfs(int node, int parent, List<List<Integer>> adj, int[] vals) {
    int[] res = {0, vals[node]};  // {not-taken, taken}
    for (int child : adj.get(node)) {
        if (child == parent) continue;
        int[] childRes = dfs(child, node, adj, vals);
        res[0] += Math.max(childRes[0], childRes[1]);  // child can be taken or not
        res[1] += childRes[0];                         // parent taken -> child not taken
    }
    return res;
}
```

### Style C: Rerooting / All-roots DP
```java
// Phase 1: root at node 0, compute dp_down[u] = answer for subtree of u
// Phase 2: re-root to compute dp_up[u] = answer from u's "parent subtree"
// Combine: dp_all[u] = combine(dp_down[u], dp_up[u])
// Used when answer must be computed for EVERY possible root.
```

---

## 3. Canonical State Transitions

### Tree Diameter
```
Problem: Length of the longest path between any two nodes.

At each node u:
  longest1 = longest path going DOWN through one child subtree
  longest2 = longest path going DOWN through a different child subtree

  diameter candidate at u = longest1 + longest2 (path goes through u)
  return to parent       = 1 + longest1         (only one branch can extend upward)

  globalMax = max(globalMax, longest1 + longest2)

State: dfs(u) returns the longest path from u going into its subtree.
```

### House Robber on Tree
```
Problem: Select nodes to maximize sum; no two adjacent (parent-child) nodes selected.

dp[u][0] = max sum for subtree of u when u is NOT robbed
dp[u][1] = max sum for subtree of u when u IS robbed

For each child c:
  dp[u][0] += max(dp[c][0], dp[c][1])   // child can be robbed or not
  dp[u][1] += dp[c][0]                  // u is robbed -> child must not be

Answer: max(dp[root][0], dp[root][1])
```

### Tree Cameras (Hard — Greedy + State Machine on Tree)
```
Problem: Place minimum cameras to monitor all nodes.
         A camera monitors: itself, parent, all children.

Three states per node:
  0 = node is COVERED but has no camera
  1 = node has a CAMERA
  2 = node is NOT COVERED (needs parent to cover it)

Post-order DFS at node u:
  If any child is in state 2 (uncovered) -> u MUST place a camera -> state 1
  If any child is in state 1 (has camera) -> u is covered -> state 0
  Else (all children in state 0) -> u cannot be covered by children -> state 2
                                     (u asks parent to cover it)

After DFS, if root is in state 2 -> place one more camera at root.
```

### Longest Path Between Any Two Nodes (Weighted)
```
dp[u] = longest weighted path from u to any leaf in its subtree

At each node u with children c1, c2, ...:
  childPaths = sorted list of [dp[ci] + weight(u, ci)]
  globalMax = max(globalMax, childPaths[0] + childPaths[1])  // through u
  dp[u] = childPaths[0]                                      // return to parent

Answer: globalMax
```

---

## 4. Template: Post-Order DFS

```java
// General post-order tree DP template (adjacency list, rooted at 0)
class TreeDP {
    int[] dp;          // one value per node
    int[] parent;
    int globalAns;

    void dfs(int u, int par, List<List<Integer>> adj) {
        dp[u] = BASE_VALUE;             // initialize with node's own value
        for (int v : adj.get(u)) {
            if (v == par) continue;     // avoid going back to parent
            dfs(v, u, adj);             // process child first (post-order)
            dp[u] = merge(dp[u], dp[v]); // combine child result into u
        }
        globalAns = update(globalAns, dp[u]);  // update global answer at u
    }
}
```

---

## 5. Complexity Analysis

| Property | Value |
|---|---|
| Nodes | n |
| DFS visits | Each node once |
| Work per node | O(degree) — iterate children |
| **Total Time** | **O(n)** — linear (each edge visited twice) |
| **Space** | **O(n)** — dp array + recursion stack (depth = O(n) worst, O(log n) balanced) |
| Rerooting (2-pass) | O(n) time, O(n) space |

**Stack overflow risk:** For n up to 10⁵ on a path graph (depth = n), recursive DFS
can overflow the stack. Convert to iterative using explicit stack + topological order.

---

## 6. Key Problem Patterns

| Problem | Style | Key Insight |
|---|---|---|
| Tree Diameter | Single-value dp[u] | Track top-2 longest child paths |
| House Robber III | dp[u][0/1] | Selected/not-selected state |
| Tree Cameras | dp[u][0/1/2] | 3-state: covered, camera, uncovered |
| Max Path Sum in Tree | Single-value + global max | Path can go through any node |
| Minimum Vertex Cover | dp[u][0/1] | Cover all edges: take u or all children |
| Subtree Sum Queries | dp[u] = subtree sum | Standard DFS aggregation |
| Centroid Decomposition | Divide + dp | Advanced: O(n log n) for path queries |

---

## 7. Common Pitfalls

```java
// PITFALL 1: Forgetting to skip the parent
// In an adjacency list with undirected edges, always pass 'parent' and skip it:
//   for (int child : adj.get(u)) { if (child == parent) continue; ... }

// PITFALL 2: Path diameter bug — not tracking top-2 children
// For diameter: you need the TWO longest child paths at each node.
// Returning only the longest child path to the parent is CORRECT,
// but the diameter candidate = sum of top 2 (or just 1 if leaf).

// PITFALL 3: Stack overflow on linear chains
// Depth-first on a "bamboo" tree (n=100000) exceeds Java's default stack.
// Use: java -Xss64m, or convert to iterative BFS/DFS with explicit stack.

// PITFALL 4: dp[u][1] > dp[u][0] is NOT always true
// A node being selected might not always be better. Let the DP decide.

// PITFALL 5: Global variable vs return value confusion
// Some problems need a global max (diameter), others just need the return value.
// Keep them separate:
//   globalDiameter updated during DFS
//   return value = longest single path from u downward
```

---

## 8. Iterative Tree DP (Anti Stack Overflow)

```java
// Compute DFS order iteratively, then process in reverse (leaves first)
int[] order = new int[n];
int[] par   = new int[n];
int idx = 0;
Arrays.fill(par, -1);

Deque<Integer> stack = new ArrayDeque<>();
stack.push(0);
while (!stack.isEmpty()) {
    int u = stack.pop();
    order[idx++] = u;
    for (int v : adj.get(u)) {
        if (v == par[u]) continue;
        par[v] = u;
        stack.push(v);
    }
}

// Process in reverse DFS order (post-order = leaves before parents)
for (int i = idx - 1; i >= 0; i--) {
    int u = order[i];
    // dp[u] = ... using already-computed dp[children of u]
}
```

---

## 9. Interview Communication Script

```
"This is Tree DP. Trees have optimal substructure: the answer for a subtree
 depends only on its children's answers, and subtrees are independent.

 I'll do a post-order DFS (process children before parent).
 State: dp[u] = [what it means for the subtree rooted at u]
 Transition: dp[u] = combine(dp[child1], dp[child2], ..., val[u])
 Base case: leaf nodes (no children)
 Answer: dp[root] or a global variable updated during DFS

 Time: O(n) — each node and edge visited once.
 Space: O(n) — dp array + recursion stack."
```

*Next: Basic (Tree Diameter), Medium (House Robber Tree), Hard (Tree Cameras).*
