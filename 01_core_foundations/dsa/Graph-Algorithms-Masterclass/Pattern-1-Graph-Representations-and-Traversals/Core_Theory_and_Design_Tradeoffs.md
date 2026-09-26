# Pattern 1 — Graph Representations and Traversals

## The Concept: Real-World Analogy
Think of a **social network** (Facebook/LinkedIn). Each person is a **vertex**. A friendship is an **edge**. Traversal answers: "Starting from Alice, which people can Alice reach within 2 hops?" BFS finds the shortest hop-count path; DFS explores deeply first, useful for mapping all reachable clusters.

In **GPS routing**, intersections are vertices and roads are edges. BFS on an unweighted graph finds the fewest-turn route. DFS is used to identify isolated road networks.

---

## The Graph Representation Choice

### Adjacency Matrix
```
matrix[u][v] = weight  (or 1/0 for unweighted)
```
- **Pros:** O(1) edge lookup; simple to code for dense graphs.
- **Cons:** O(V²) space; iterating over neighbors is O(V) even if a vertex has 1 neighbor.
- **When to use:** V ≤ 1000 and the graph is dense (E ≈ V²). Floyd-Warshall (Pattern 4).

### Adjacency List
```java
List<List<Integer>> adjacencyList = new ArrayList<>();
// or with weights:
List<List<int[]>> adjacencyList = new ArrayList<>();  // int[] = {neighbor, weight}
```
- **Pros:** O(V + E) space; iterating over neighbors is O(degree).
- **Cons:** O(degree) edge lookup.
- **When to use:** Sparse graphs (E << V²). BFS/DFS, Dijkstra, Topological Sort.
- **→ This is the standard choice for Patterns 1, 2, 3, 5, 6, 7.**

### Edge List
```java
int[][] edges = {{src, dst, weight}, ...};
```
- **Pros:** Simple; O(E) space.
- **Cons:** O(E) neighbor lookup; no per-vertex iteration structure.
- **When to use:** Algorithms that process all edges uniformly — Kruskal's MST, Bellman-Ford.

---

## State Tracking Objects

| Array | Purpose | Initial Value |
|---|---|---|
| `visited[]` | Marks vertices already processed — prevents revisiting in BFS/DFS | `false` |
| `distance[]` | Shortest hop count from source (BFS) | `Integer.MAX_VALUE` (`-1` for "unvisited") |
| `parent[]` | Stores the predecessor vertex for path reconstruction | `-1` |
| `componentId[]` | Labels each vertex with its connected component index | `-1` |
| `color[]` | Bipartite checking: 0 = uncolored, 1 = red, 2 = blue | `0` |

---

## BFS vs DFS: When to Use Which

| Property | BFS | DFS |
|---|---|---|
| Data structure | Queue (FIFO) | Stack / Recursion |
| Shortest path (unweighted) | ✅ Guaranteed | ❌ Not guaranteed |
| Connected components | ✅ | ✅ |
| Cycle detection | ✅ (undirected) | ✅ (directed + undirected) |
| Topological sort | ✅ (Kahn's) | ✅ (reverse finish order) |
| Memory on deep trees | O(V) worst | O(V) worst (stack depth) |
| Code complexity | Medium | Low (recursive) |

---

## Corner Cases to Handle

```
1. DISCONNECTED GRAPHS:
   Never run BFS/DFS from only one source.
   Loop: for (int v = 0; v < totalVertices; v++) { if (!visited[v]) runBFS(v); }

2. SELF-LOOPS:
   An edge (u, u) — BFS/DFS: visited[u] is true before we process u's neighbors,
   so the self-loop is harmlessly skipped if we check visited before enqueueing.

3. PARALLEL EDGES (multi-graphs):
   Multiple edges between same pair. Use a Set<Integer> for visited neighbors
   inside BFS if you must process edges uniquely. For most traversal problems, ignored.

4. EMPTY GRAPH (V=0 or E=0):
   Guard: if (totalVertices == 0) return;
   An E=0 graph has V isolated components.

5. WORD LADDER (implicit graph):
   Vertices are strings; edges are implicit (differ by 1 char).
   Never build the explicit graph — generate neighbors on the fly in O(L * 26).
```

---

## Complexity Quick Reference

| Algorithm | Time | Space |
|---|---|---|
| BFS (adjacency list) | O(V + E) | O(V) |
| DFS (adjacency list) | O(V + E) | O(V) |
| BFS (adjacency matrix) | O(V²) | O(V) |
| Connected components | O(V + E) | O(V) |
| Word Ladder (BFS) | O(N × L² × 26) | O(N × L) |

where N = number of words, L = word length.

---

## Interview Communication Script

```
"I'll represent this graph as an adjacency list using List<List<Integer>>
 for O(V+E) space. I'll use BFS [or DFS] because [reason].
 I need a visited[] array to prevent revisiting. For disconnected graphs,
 I'll loop over all vertices and trigger BFS/DFS on unvisited ones.
 Time: O(V+E), Space: O(V+E) including the adjacency list."
```
