# Pattern 8 — Network Flow and Bipartite Matching

## The Concept: Real-World Analogy
**Water pipe networks (Max Flow)**: Water flows from a source (spring) to a sink (city reservoir) through a network of pipes. Each pipe has a maximum capacity. Max Flow finds the maximum water rate achievable.

**Job assignment (Bipartite Matching)**: N workers and M jobs. Each worker can do certain jobs. Bipartite matching finds the maximum number of jobs that can be simultaneously assigned (one worker per job).

**Internet routing**: Data packets flowing from data center to users. Each link has bandwidth limits. Max flow = maximum throughput.

---

## The Graph Representation Choice

**Residual Graph** is the key data structure for Ford-Fulkerson / Edmonds-Karp:
```java
int[][] residualCapacity;   // residualCapacity[u][v] = remaining capacity on edge u→v
                            // (includes both forward and backward edges)
```
An **adjacency matrix** (not list) works best here because:
- We need O(1) lookup of `residualCapacity[u][v]` during BFS/DFS augmentation.
- Backward edges: `residualCapacity[v][u]` is automatically tracked.
- V is typically small in flow problems (V ≤ 500).

For **very large sparse networks**, use adjacency list with explicit backward edge pairs:
```java
// Each edge stored as: {to, capacity, reverseEdgeIndex}
List<int[]>[] graph;
```

---

## State Tracking Objects

| Structure | Role |
|---|---|
| `residualCapacity[V][V]` | Flow network state — capacity minus flow already sent |
| `parent[]` | BFS path from source to sink (Edmonds-Karp augmenting path) |
| `visited[]` | BFS visited flag per augmentation iteration |
| `color[]` | Bipartite: 2-coloring for graph validity check |
| `matchLeft[]` | Bipartite matching: which right-side node is matched to each left node |
| `matchRight[]` | Bipartite matching: which left-side node is matched to each right node |

---

## Max Flow Core Concepts

### Residual Graph
```
Forward edge (u→v): residualCapacity[u][v] = capacity - flowSent
Backward edge (v→u): residualCapacity[v][u] = flowSent (can "undo" flow)

After sending flow f along path u→v:
  residualCapacity[u][v] -= f
  residualCapacity[v][u] += f

The backward edge allows the algorithm to "reroute" flow that was sent earlier.
This is what makes Ford-Fulkerson complete (finds optimal, not just greedy).
```

### Ford-Fulkerson vs Edmonds-Karp
```
Ford-Fulkerson: Find ANY augmenting path (DFS) and push flow.
  Time: O(E × maxFlow) — can be non-terminating with irrational capacities.

Edmonds-Karp: Find SHORTEST augmenting path (BFS, fewest edges).
  Time: O(V × E²) — guaranteed polynomial regardless of capacity values.
  Why faster? BFS guarantees augmenting path length is non-decreasing.
              Each edge becomes "saturating critical" at most V/2 times.
```

### Max-Flow Min-Cut Theorem
```
Maximum flow from source to sink = Minimum cut capacity.
A CUT separates source-reachable vertices from sink-reachable vertices.
After Edmonds-Karp, the min-cut is: edges (u,v) where
  u is reachable from source in residual graph, v is not.
```

---

## Bipartite Matching

```
A bipartite graph has two disjoint sets L (left) and R (right).
All edges go from L to R (no L-L or R-R edges).

MAXIMUM BIPARTITE MATCHING (Hungarian/Augmenting Path):
  For each left node l:
    Try to find an augmenting path via DFS:
      If right node r is unmatched → match l-r, return true.
      If r is matched to l' → try to rematch l' to a different r'.
        If l' can be rematched → reassign: match l-r, return true.

This is equivalent to Max Flow with unit capacities.
```

---

## Corner Cases

```
1. NO AUGMENTING PATH EXISTS:
   BFS from source doesn't reach sink in residual graph → algorithm terminates.
   Max flow = sum of all flow sent so far.

2. BIPARTITE CHECK FAILS (ODD CYCLE):
   During 2-coloring, if a neighbor has the same color → not bipartite.
   Return false immediately.

3. SELF-LOOPS IN FLOW NETWORK:
   residualCapacity[v][v] contributes nothing to flow. Safe to ignore.

4. DISCONNECTED FLOW NETWORK:
   Source cannot reach sink → max flow = 0 (no augmenting path found in first BFS).

5. ZERO-CAPACITY EDGES:
   Should not be in the graph (they contribute nothing).
   Safe to include — BFS/DFS will never augment through them.
```

---

## Complexity Summary

| Algorithm | Time | Space | Notes |
|---|---|---|---|
| Ford-Fulkerson (DFS) | O(E × maxFlow) | O(V²) | Pseudo-polynomial |
| Edmonds-Karp (BFS) | O(V × E²) | O(V²) | Polynomial |
| Is Bipartite (BFS 2-color) | O(V + E) | O(V) | |
| Max Bipartite Matching | O(V × E) | O(V + E) | Via augmenting paths |
| Dinic's Algorithm | O(V² × E) | O(V + E) | Advanced: faster for unit-cap |
