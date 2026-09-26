# Pattern 5 — Minimum Spanning Trees (MST)

## The Concept: Real-World Analogy
**Telecommunications cable layout**: Given N cities, connect all of them with fiber cable at minimum total cost. Each potential cable segment (edge) has a cost. The MST selects the cheapest subset of cables that keeps all cities connected with no redundant loops.

**Cluster analysis in ML**: MST helps identify natural clusters — remove the longest edges of the MST to split the graph into clusters.

**Network design**: Design a computer network connecting N servers where each link has a cost. MST minimizes total infrastructure spend while ensuring full connectivity.

---

## The Graph Representation Choice

### Kruskal's Algorithm → Edge List
```java
int[][] edges = {{weight, src, dst}, ...};   // sorted by weight
```
Kruskal processes edges in weight order globally. An edge list sorted once with `Arrays.sort()` is perfect. No adjacency structure needed.

### Prim's Algorithm → Adjacency List (sparse) or Matrix (dense)
```java
List<List<int[]>> adjacencyList;   // int[] = {neighbor, weight}
// OR for dense graphs:
int[][] costMatrix;
```
Prim grows the MST from a starting vertex, always picking the cheapest edge to an unvisited vertex. For dense graphs (V² ≈ E), a simple O(V²) matrix + min-scanning loop beats the heap-based O(E log V) Prim.

**Rule of thumb:**
- Sparse graph (E ≈ V log V): Kruskal or heap-Prim.
- Dense graph (E ≈ V²): matrix-Prim is simpler and equally efficient.
- "Connect all points" (geometric graph): Prim with O(V²) avoids building E=V² edges explicitly.

---

## State Tracking Objects

| Structure | Role |
|---|---|
| `UnionFind` | Kruskal's: tracks connected components; detects if adding edge creates cycle |
| `inMST[v]` | Prim's: marks vertices already in the MST |
| `cheapestEdge[v]` | Prim's: minimum edge weight connecting v to the current MST |
| `mstEdges[]` | Collected MST edges for output |
| `totalMSTWeight` | Running sum of MST edge weights |

---

## Union-Find for Kruskal's

```
find(x): returns the root representative of x's component (with path compression).
union(x, y): merges two components (by rank to keep tree flat).

CYCLE DETECTION:
  An edge (u, v) creates a cycle if and only if find(u) == find(v).
  → Skip edges where find(u) == find(v).
  → Accept edges where find(u) != find(v), then union(u, v).

MST SIZE PROPERTY:
  A spanning tree of V vertices has exactly V-1 edges.
  If we accept fewer than V-1 edges → graph is disconnected (no MST exists).
```

---

## Cut Property (Why MST Algorithms Work)

```
CUT: A partition of V into two sets (S, V−S).
CROSSING EDGE: An edge with one endpoint in S and one in V−S.

CUT PROPERTY: The minimum-weight crossing edge of ANY cut is always part of some MST.

Kruskal's: processes edges in increasing weight order → always picks the min edge for some cut.
Prim's: the cheapest edge from the current MST to unvisited vertices → min crossing edge.
Both algorithms are correct by the Cut Property.
```

---

## Corner Cases

```
1. DISCONNECTED GRAPH:
   No MST exists. Kruskal: fewer than V-1 edges accepted.
   Prim: some vertices remain with cheapestEdge = MAX_VALUE.
   Detection: check if MST has exactly V-1 edges.

2. PARALLEL EDGES (multiple edges between same vertices):
   Both algorithms handle naturally — the min-weight one will be selected first.

3. ALL SAME WEIGHTS:
   Any spanning tree is an MST. Both algorithms produce one valid answer.

4. GEOMETRIC "CONNECT ALL POINTS" (LC 1584):
   Manhattan distance = |x1-x2| + |y1-y2|.
   Building all O(V²) edges and running Kruskal/Prim works.
   Optimization: Prim's with O(V²) matrix avoids edge sorting.

5. NEGATIVE WEIGHTS:
   MST algorithms handle negative weights correctly.
   (Unlike Dijkstra — MST is not a shortest-path algorithm.)
```

---

## Complexity Summary

| Algorithm | Time | Space | Best For |
|---|---|---|---|
| Kruskal's (Union-Find) | O(E log E) | O(V + E) | Sparse graphs |
| Prim's (binary heap) | O(E log V) | O(V + E) | Sparse graphs |
| Prim's (adjacency matrix) | O(V²) | O(V²) | Dense graphs |
| Connect All Points | O(V² log V) or O(V²) | O(V²) or O(V) | Geometric |
