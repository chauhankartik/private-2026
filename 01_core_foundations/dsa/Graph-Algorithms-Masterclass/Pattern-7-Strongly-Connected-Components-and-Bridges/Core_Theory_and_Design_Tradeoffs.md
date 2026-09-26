# Pattern 7 — Strongly Connected Components (SCC) and Bridges

## The Concept: Real-World Analogy
**Web page ranking (SCC)**: Two web pages are in the same SCC if you can navigate from one to the other and back following hyperlinks. Google's PageRank groups pages by SCC for ranking propagation.

**Power grid resilience (Bridges)**: A bridge in the power grid is a transmission line whose removal disconnects part of the grid. Identifying bridges lets engineers prioritize cable redundancy.

**Software module dependency analysis (SCC)**: In a large codebase, SCCs represent mutually-dependent module clusters. Code within the same SCC must be compiled together. SCCs guide build order optimization.

---

## The Graph Representation Choice

Both Kosaraju's and Tarjan's use **Adjacency List** for O(V + E) DFS traversal:
```java
List<List<Integer>> adjacencyList;        // original graph
List<List<Integer>> reversedAdjacency;   // Kosaraju's only: reversed edge directions
```

For **Bridges** (Tarjan's bridge-finding):
```java
List<List<int[]>> adjacencyList;   // int[] = {neighbor, edgeId} — needed for multi-edge tracking
```
Edge IDs prevent incorrectly treating a multi-edge (parallel edges) as a bridge.

---

## State Tracking Objects

### Kosaraju's Algorithm
| Array | Role |
|---|---|
| `visited[]` | Pass 1: standard DFS visited flag |
| `finishOrder` | Stack of vertices ordered by DFS finish time (decreasing) |
| `componentId[]` | Pass 2: SCC label for each vertex |
| `sccCount` | Total number of SCCs found |

### Tarjan's SCC & Bridge Finding
| Array | Role |
|---|---|
| `discoveryTime[]` | DFS timestamp when vertex was first visited |
| `lowLink[]` | Minimum discovery time reachable from vertex's subtree |
| `onStack[]` | Whether vertex is currently on the Tarjan SCC stack |
| `tarjanStack` | Explicit stack used for SCC grouping |
| `timer` | Global timestamp counter, incremented per vertex visit |

---

## Kosaraju's Algorithm: Two-Pass DFS

```
PASS 1 (original graph):
  Run DFS on all vertices. Push each vertex onto a stack when its DFS FINISHES.
  Stack top = vertex with highest finish time = "most reachable" vertex.

REVERSE the graph (flip all edge directions).

PASS 2 (reversed graph):
  Pop vertices from the stack. For each unvisited popped vertex, run DFS.
  All vertices reachable from it in the REVERSED graph form ONE SCC.

INTUITION:
  The vertex that finishes last in Pass 1 can reach everyone in the original.
  In the reversed graph, everyone that can reach it (= its SCC) is explored first.
```

---

## Tarjan's Low-Link Values for Bridges

```
lowLink[u] = minimum of:
  1. discoveryTime[u]  (the vertex itself)
  2. discoveryTime[w] for all back edges (u → w) where w is an ancestor
  3. lowLink[child]    for all DFS tree children

BRIDGE CONDITION:
  Edge (u, v) is a bridge if: lowLink[v] > discoveryTime[u]
  (The subtree rooted at v cannot reach u or any ancestor of u → removing (u,v) disconnects)

ARTICULATION POINT CONDITION:
  Vertex u is an articulation point if:
    - u is root of DFS tree AND has ≥ 2 children, OR
    - u is not root AND has a child v where lowLink[v] >= discoveryTime[u]
```

---

## Corner Cases

```
1. DISCONNECTED GRAPH:
   Must loop all vertices: for (v = 0..V-1) if not visited → startDFS(v).
   Each disconnected component produces its own SCC(s).

2. SINGLE-VERTEX SCC:
   A vertex with no outgoing/incoming edges = singleton SCC.
   Kosaraju: DFS from it in Pass 2 visits only itself.

3. PARALLEL EDGES IN BRIDGE FINDING:
   Two edges between u and v: neither is a bridge (the other remains).
   Use edge IDs to skip the SPECIFIC parent edge (not all edges to parent).
   Bug: skipping by vertex ID incorrectly skips both parallel edges.

4. SELF-LOOPS:
   Don't affect SCC computation. Ignore in bridge finding.

5. UNDIRECTED GRAPH BRIDGE FINDING:
   Must track parentEdgeId (not parentVertex) to correctly handle multi-edges.
```

---

## Complexity Summary

| Algorithm | Time | Space |
|---|---|---|
| Kosaraju's SCC | O(V + E) | O(V + E) (for reversed graph) |
| Tarjan's SCC | O(V + E) | O(V) |
| Bridge Finding (Tarjan) | O(V + E) | O(V) |
| Articulation Points | O(V + E) | O(V) |

**Tarjan's vs Kosaraju's:**
- Tarjan's: single DFS pass, O(V) extra space, more complex code.
- Kosaraju's: two DFS passes + reversed graph, O(V+E) extra space, simpler code.
- Both are O(V+E) total. Tarjan's is cache-friendlier (single pass).
