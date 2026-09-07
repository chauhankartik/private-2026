# Pattern 4 — All-Pairs Shortest Path and Transitive Closure

## The Concept: Real-World Analogy
**Network latency matrix**: Given a data center with N routers, find the minimum latency between EVERY pair of routers. Floyd-Warshall fills this matrix in O(V³) — feasible for V ≤ 500.

**Currency exchange / evaluate division**: If A/B = 2 and B/C = 3, what is A/C? Model as a directed weighted graph (weights are ratios). Floyd-Warshall propagates products to find all ratios.

**"Find the City" problem**: With V cities and roads of varying distances, and a distance threshold, find the city with the fewest reachable neighbors — solved by running Floyd-Warshall then counting reachable cities per vertex.

---

## The Graph Representation Choice

**Adjacency Matrix** is the natural choice for All-Pairs SP:
```java
double[][] distanceMatrix = new double[V][V];
// distanceMatrix[i][j] = direct edge weight, or ∞ if no direct edge, or 0 if i==j
```
- Floyd-Warshall fills this matrix in-place: `dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])`.
- The triple loop is O(V³) regardless of edge density.
- For `V ≤ 500`: 500³ = 125M operations — acceptable.
- For `V > 1000`: prefer running Dijkstra from each vertex → O(V × (V+E) log V).

**When to use Adjacency List instead:**
- Graph is sparse (E << V²) and V > 300 → run Dijkstra V times instead.
- Floyd-Warshall on sparse graphs wastes time on non-existent edges.

---

## State Tracking Objects

| Structure | Role |
|---|---|
| `distanceMatrix[V][V]` | Stores shortest distance between every pair; updated in-place |
| `nextHop[V][V]` | Path reconstruction: `nextHop[i][j]` = first step from i toward j |
| `hasNegativeCycle` | `distanceMatrix[v][v] < 0` after Floyd-Warshall → negative cycle through v |

---

## Floyd-Warshall Core Invariant

```
After processing intermediate vertex k:
  distanceMatrix[i][j] = shortest path from i to j using ONLY vertices {0, 1, ..., k} as intermediates.

The KEY INSIGHT: the order of the outer loop (k) doesn't matter for correctness
because distanceMatrix[i][k] and distanceMatrix[k][j] are already optimal
(they use only vertices {0..k-1} as intermediates, which is correct).

Recurrence:
  dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])

Base case (k = -1, no intermediates):
  dist[i][j] = direct edge weight, or ∞ if no edge, or 0 if i == j.
```

---

## Evaluate Division: Graph Modeling

```
Equations like A/B = 2.0, B/C = 3.0 become:
  Edge A→B with weight 2.0
  Edge B→A with weight 0.5   (reciprocal)
  Edge B→C with weight 3.0
  Edge C→B with weight 0.333

Query A/C: find path A→B→C, multiply weights: 2.0 × 3.0 = 6.0.

Floyd-Warshall for division:
  dist[i][j] = max(dist[i][j], dist[i][k] × dist[k][j])  ← use max to find product paths
  (or use BFS/DFS for each query — O(Q × (V+E)))
```

---

## Corner Cases

```
1. NEGATIVE CYCLE DETECTION:
   After Floyd-Warshall, check: if dist[v][v] < 0 for any v → negative cycle.
   Paths through this cycle are undefined (-∞).

2. UNREACHABLE PAIRS:
   Initialize dist[i][j] = ∞ (Double.MAX_VALUE or Integer.MAX_VALUE/2).
   USE MAX_VALUE/2 to prevent overflow in dist[i][k] + dist[k][j].

3. EVALUATE DIVISION — UNKNOWN VARIABLES:
   If a queried variable doesn't exist in the graph → return -1.0.
   If source == destination AND variable exists → return 1.0.

4. SELF-LOOPS:
   dist[i][i] = 0 always (no cost to stay). Floyd-Warshall preserves this.

5. FIND CITY — THRESHOLD FILTERING:
   Count neighbors j where dist[i][j] <= threshold.
   Tie-break: return the city with the LARGEST index (standard problem constraint).
```

---

## Complexity Summary

| Algorithm | Time | Space |
|---|---|---|
| Floyd-Warshall | O(V³) | O(V²) |
| Evaluate Division (Floyd-Warshall) | O(N³ + Q) | O(N²) |
| Evaluate Division (BFS per query) | O(Q × (V+E)) | O(V+E) |
| Find City (Floyd-Warshall) | O(V³ + V²) | O(V²) |
| Path Reconstruction | O(V) per path | O(V²) for nextHop table |
