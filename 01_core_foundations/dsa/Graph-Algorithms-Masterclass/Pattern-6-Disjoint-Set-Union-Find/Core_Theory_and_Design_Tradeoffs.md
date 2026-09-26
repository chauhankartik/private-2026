# Pattern 6 — Disjoint Set Union-Find

## The Concept: Real-World Analogy
**Social network friend circles**: Initially everyone is their own group. When Alice and Bob become friends, merge their groups. When queried "are Alice and Charlie in the same friend circle?", answer in near-O(1) with `find(alice) == find(charlie)`.

**Percolation in physics**: Grid of open/blocked sites — does any path of open sites connect top to bottom? Dynamic connectivity with Union-Find.

**Network connectivity**: As edges are added to a network dynamically ("Number of Islands II"), Union-Find tracks connected components without rebuilding from scratch.

---

## The Graph Representation Choice

Union-Find is a **self-contained data structure**, not a traditional graph traversal. It uses two arrays:

```java
int[] parent;   // parent[i] = representative/root of component containing i
int[] rank;     // rank[i] = approximate tree height (for union by rank)
```

For **grid problems** (Number of Islands II), internally map 2D coordinates to 1D:
```java
int index = row * numColumns + column;
```

For **dynamic edge problems** (Redundant Connection), process edges one by one and union their endpoints.

**No adjacency list needed** — Union-Find replaces graph traversal entirely for connectivity queries.

---

## State Tracking Objects

| Field | Role |
|---|---|
| `parent[v]` | Canonical root of the component containing v |
| `rank[v]` | Upper bound on tree height — used to keep trees flat |
| `componentCount` | Total number of distinct connected components |
| `size[v]` | Component size at root v (for size-weighted union) |

---

## Operations Deep Dive

### `find(x)` with Path Compression
```java
int find(int x) {
    if (parent[x] != x) {
        parent[x] = find(parent[x]);   // PATH COMPRESSION: point directly to root
    }
    return parent[x];
}
```
**Path compression** flattens the tree — future `find` calls on the same subtree become O(1).

### `union(x, y)` with Union by Rank
```java
boolean union(int x, int y) {
    int rootX = find(x), rootY = find(y);
    if (rootX == rootY) return false;   // already connected — no-op (or: cycle detected)
    if (rank[rootX] < rank[rootY]) { int tmp = rootX; rootX = rootY; rootY = tmp; }
    parent[rootY] = rootX;              // attach smaller-rank tree under larger-rank tree
    if (rank[rootX] == rank[rootY]) rank[rootX]++;
    componentCount--;
    return true;
}
```
**Union by rank** prevents degenerate chains — keeps tree height O(log V).

### Combined amortized complexity with both optimizations:
```
find() and union(): O(α(V)) ≈ O(1)   (α = inverse Ackermann function)
α(10^80) = 4 — effectively constant for any real input.
```

---

## Corner Cases

```
1. REDUNDANT CONNECTION:
   Process edges in order. The FIRST edge where find(u) == find(v) is redundant.
   Return that edge — it's the one creating a cycle.

2. NUMBER OF ISLANDS II:
   Only add a cell to Union-Find when it becomes land (not pre-initialized).
   Check all 4 neighbors; union if neighbor is already land.
   Track component count: count++ on land addition, count-- on each successful union.

3. SELF-LOOP EDGE:
   Edge (u, u): find(u) == find(u) always → correctly detected as redundant/cycle.

4. FIND ON UNINITIALIZED VERTEX:
   If grid cells can be 'water' (inactive), use a boolean `isActive[]` flag.
   Only call find() on active (land) cells.

5. PATH COMPRESSION CHANGES PARENT:
   After compression, parent[x] points directly to root — not to original parent.
   This is safe because the ROOT is the canonical identifier, not intermediate nodes.
```

---

## Complexity Summary

| Operation | Without Optimization | With Path Compression | With Both |
|---|---|---|---|
| `find(x)` | O(V) worst | O(log V) amortized | O(α(V)) ≈ O(1) |
| `union(x, y)` | O(V) worst | O(log V) amortized | O(α(V)) ≈ O(1) |
| Build (V vertices) | O(V) | O(V) | O(V) |
| M operations | O(M × V) | O(M log V) | O(M × α(V)) |
| Number of Islands II | O(L × α(M × N)) | — | — |

where L = number of land additions, M × N = grid size.
