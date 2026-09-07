# Pattern 2 — Cycle Detection and Topological Sort

## The Concept: Real-World Analogy
**Task scheduling** in a build system (Make, Bazel, Gradle): each task is a vertex; an edge A→B means "A must finish before B starts." If there's a cycle (A depends on B, B depends on A), the build is impossible. **Topological sort** produces a valid build order. **Cycle detection** reports the conflict.

In **university course registration**: CS401 requires CS201, CS201 requires CS101. A topological order gives a valid semester sequence.

---

## The Graph Representation Choice

**Directed Acyclic Graphs (DAGs)** are the natural structure here.
Use an **Adjacency List + in-degree array**:

```java
List<List<Integer>> adjacencyList;   // prerequisite → dependent edges
int[] inDegree;                       // number of prerequisites for each course
```

- **Adjacency List** for Kahn's BFS (process vertices with inDegree=0 first).
- **Adjacency List + recursion color** for DFS-based cycle detection.
- **Edge List** is insufficient — we need per-vertex neighbor iteration.

---

## State Tracking Objects

| Array | Role | Values |
|---|---|---|
| `inDegree[v]` | Count of incoming edges for vertex v (Kahn's BFS) | 0 to V |
| `color[v]` | DFS 3-color marking for directed cycle detection | 0=white, 1=gray, 2=black |
| `topologicalOrder[]` | Result list — valid ordering of vertices | filled during processing |
| `visited[v]` | Whether v has been fully processed (DFS) | false/true |
| `onStack[v]` | Whether v is in the current DFS recursion stack | false/true |

### The 3-Color DFS Invariant
```
WHITE (0): Not yet visited.
GRAY  (1): Currently in the DFS recursion stack (being explored).
BLACK (2): Fully processed (all descendants visited).

A BACK EDGE occurs when DFS encounters a GRAY vertex.
Back edge = cycle in a directed graph.
```

---

## Kahn's BFS Algorithm (Topological Sort)
```
1. Compute inDegree[] for all vertices.
2. Enqueue all vertices with inDegree == 0 (no prerequisites).
3. While queue is not empty:
   a. Dequeue vertex u → add to topologicalOrder.
   b. For each neighbor v of u: decrement inDegree[v].
   c. If inDegree[v] == 0: enqueue v.
4. If topologicalOrder.size() < V: cycle detected (some vertices never reached inDegree=0).
```

---

## DFS Topological Sort Algorithm
```
1. Run DFS on each unvisited vertex.
2. When DFS on vertex u FINISHES (all descendants explored): push u onto a stack.
3. Pop from stack → topological order (reverse of finish times).
4. Cycle: if we visit a GRAY (onStack) vertex → back edge → cycle.
```

---

## Corner Cases

```
1. DISCONNECTED GRAPH:
   Must iterate all vertices: for (v = 0 to V-1) if unvisited → startDFS(v).

2. SELF-LOOP (trivial cycle):
   Edge (u, u) → inDegree[u] is never reduced to 0 → detected by Kahn's.
   DFS: immediately a back edge (gray vertex visited again).

3. PARALLEL EDGES:
   u→v appearing twice → inDegree[v] incremented twice.
   Must be decremented twice. Usually fine with the standard algorithm.

4. VALID TOPOLOGICAL ORDER MAY NOT BE UNIQUE:
   Multiple vertices with inDegree=0 simultaneously → different valid orders.
   For lex-smallest order: use a MinHeap (PriorityQueue) instead of a plain queue.

5. ALIEN DICTIONARY:
   Characters are vertices. Build edges by comparing adjacent words.
   If word1 is a PREFIX of word2 but appears AFTER word2 → impossible (return "").
   This is the most subtle edge case.
```

---

## Complexity Analysis

| Algorithm | Time | Space |
|---|---|---|
| Kahn's BFS Topological Sort | O(V + E) | O(V + E) |
| DFS Topological Sort | O(V + E) | O(V) stack depth |
| Cycle Detection (Kahn's) | O(V + E) | O(V + E) |
| Cycle Detection (DFS) | O(V + E) | O(V) |
| Alien Dictionary | O(N × L + V + E) | O(V + E) |

where N = number of words, L = average word length.

---

## Interview Script

```
"This is a topological sort problem. I'll use Kahn's BFS because it naturally
 detects cycles: if the final order doesn't include all V vertices,
 a cycle exists. I'll build an adjacency list and inDegree array in O(E),
 then process in O(V+E). Space is O(V+E) for the graph."
```
