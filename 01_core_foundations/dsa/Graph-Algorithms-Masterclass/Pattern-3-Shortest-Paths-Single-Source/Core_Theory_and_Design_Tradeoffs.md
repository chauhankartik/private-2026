# Pattern 3 — Shortest Paths: Single Source

## The Concept: Real-World Analogy
**GPS Navigation (Dijkstra):** Each intersection is a vertex, each road is a weighted edge (travel time). Dijkstra finds the fastest route from your current location to all destinations simultaneously.

**Financial Arbitrage (Bellman-Ford):** Currency exchange rates form a weighted directed graph. A negative-weight cycle means you can exchange currencies in a loop and gain money infinitely — Bellman-Ford detects this.

**Flight booking with layover constraints (SPFA/Bellman-Ford variant):** Cheapest flight within K stops — a modification of Bellman-Ford that processes at most K+1 layers.

---

## The Graph Representation Choice

**Dijkstra**: Use **Adjacency List of weighted edges** + a **PriorityQueue** (min-heap):
```java
List<List<int[]>> adjacencyList;   // int[] = {neighborVertex, edgeWeight}
PriorityQueue<int[]> minHeap;      // int[] = {currentDistance, currentVertex}
```
Adjacency list gives O(V + E) graph construction and O(degree) neighbor expansion.

**Bellman-Ford**: Use an **Edge List** since we iterate over ALL edges V-1 times:
```java
int[][] edges;   // {sourceVertex, destinationVertex, weight}
```
No adjacency list needed — edge list is ideal for "process all edges" patterns.

**Cheapest Flights (K stops)**: Edge list with DP table `dp[stop][vertex]` = min cost.

---

## State Tracking Objects

| Array | Role | Initial Value |
|---|---|---|
| `distance[]` | Shortest distance from source to each vertex | `Integer.MAX_VALUE` (∞) |
| `visited[]` | Settled vertices in Dijkstra (finalized distances) | `false` |
| `parent[]` | Path reconstruction — predecessor on shortest path | `-1` |
| `hasNegativeCycle` | Flag set after V-th Bellman-Ford relaxation if any update | `false` |
| `dp[k][v]` | Min cost to reach vertex v using at most k edges | `Integer.MAX_VALUE` |

---

## Algorithm Decision Tree

```
Has negative weights?
  NO  → Use Dijkstra (O((V+E) log V))
  YES →
    Need to detect negative cycles?
      YES → Use Bellman-Ford (O(V × E))
    Need shortest path with at most K edges?
      YES → Use BFS-Bellman-Ford layered DP (O(K × E))
    Unweighted (all weights = 1)?
      YES → Use BFS (O(V + E))
```

---

## Dijkstra: Why Greedy Works
```
Invariant: When a vertex is popped from the min-heap, its distance is FINAL.
Proof: All remaining heap entries have distance ≥ current popped distance.
       Since edge weights are non-negative, no future path can improve it.

VIOLATION: If any edge weight is negative, a later path could be shorter.
           Dijkstra gives WRONG answers with negative weights.
```

## Bellman-Ford: Why V-1 Iterations Suffice
```
In a graph with V vertices, the longest acyclic shortest path has at most V-1 edges.
After k iterations, we have correct distances for all paths using ≤ k edges.
After V-1 iterations, all paths are found.
Iteration V: if any distance STILL improves → that improvement uses a cycle → negative cycle.
```

---

## Corner Cases

```
1. SOURCE NOT REACHABLE: distance[source] = 0, all others = MAX_VALUE.
   If no path exists, distance[target] remains MAX_VALUE → return -1.

2. OVERFLOW IN DISTANCE ADDITION:
   int distance + int weight → can overflow if distance = MAX_VALUE.
   Guard: if (distance[u] == MAX_VALUE) continue; // skip unreachable

3. GRAPH WITH SELF-LOOPS:
   Edge (u, u, w) with w > 0: Dijkstra ignores (no improvement).
   Edge (u, u, w) with w < 0: Bellman-Ford detects as negative cycle.

4. DISCONNECTED GRAPH:
   Dijkstra naturally handles this — unreached vertices keep distance = MAX_VALUE.

5. K-STOPS CONSTRAINT:
   Use a COPY of the previous layer's dp to avoid using edges added in the current layer.
   (Common bug: using in-place dp allows multi-hop within one "layer".)
```

---

## Complexity Summary

| Algorithm | Time | Space | Negative Weights | Negative Cycles |
|---|---|---|---|---|
| BFS (unweighted) | O(V+E) | O(V) | ❌ | ❌ |
| Dijkstra (binary heap) | O((V+E) log V) | O(V+E) | ❌ | ❌ |
| Bellman-Ford | O(V × E) | O(V) | ✅ | ✅ detects |
| SPFA (avg) | O(E) avg, O(VE) worst | O(V) | ✅ | ✅ detects |
| K-stops DP | O(K × E) | O(V) | ✅ | N/A |
