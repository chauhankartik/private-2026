# Graphs — Theory, Internals & Complexity Proofs
> **Study goal:** Understand graph representations, traversals, and the WHY behind every algorithm.  
> Google interviewers expect you to reason about BFS vs DFS trade-offs, detect cycles, and pick the right algorithm instantly.

---

## 1. What Is a Graph?

A **graph** G = (V, E) consists of:
- **V** = set of vertices (nodes)
- **E** = set of edges (connections between vertices)

```
G = (V, E)
V = {A, B, C, D}
E = {(A,B), (A,C), (B,D), (C,D)}

    A --- B
    |     |
    C --- D
```

### Types of Graphs
| Type | Description | Example |
|---|---|---|
| **Undirected** | Edges have no direction: (u,v) = (v,u) | Social network (friendships) |
| **Directed (Digraph)** | Edges have direction: (u→v) ≠ (v→u) | Web links, dependencies |
| **Weighted** | Edges have costs/weights | Road network with distances |
| **Unweighted** | All edges have equal weight (or weight = 1) | Maze, grid problems |
| **DAG** | Directed Acyclic Graph — no cycles | Build systems, course prerequisites |
| **Tree** | Connected acyclic undirected graph (|E| = |V|-1) | File system, org chart |
| **Bipartite** | Vertices split into 2 sets, edges only between sets | Matching problems |

---

## 2. Graph Representations

### 2a. Adjacency List (preferred for interviews)

Each vertex stores a list of its neighbors.

```java
// Unweighted
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
adj.get(u).add(v);  // add edge u → v
adj.get(v).add(u);  // add edge v → u (if undirected)

// Weighted
List<List<int[]>> adj = new ArrayList<>();
adj.get(u).add(new int[]{v, weight});
```

| Operation | Time |
|---|---|
| Add edge | O(1) |
| Check if edge (u,v) exists | O(degree(u)) |
| Iterate neighbors of u | O(degree(u)) |
| Space | O(V + E) |

### 2b. Adjacency Matrix

```java
boolean[][] adj = new boolean[n][n];
adj[u][v] = true;  // edge u → v
adj[v][u] = true;  // undirected
```

| Operation | Time |
|---|---|
| Add edge | O(1) |
| Check if edge (u,v) exists | O(1) |
| Iterate neighbors of u | O(V) — must scan entire row |
| Space | O(V²) |

### 2c. Edge List

```java
int[][] edges = {{u1,v1,w1}, {u2,v2,w2}, ...};
```

Useful for: Union-Find algorithms, Kruskal's MST.

### When to use which?

| Representation | Best for |
|---|---|
| **Adjacency List** | Sparse graphs (E << V²), most interview problems |
| **Adjacency Matrix** | Dense graphs, or need O(1) edge lookup |
| **Edge List** | Kruskal's MST, Union-Find problems |

**Interview default:** Always use adjacency list unless told otherwise.

---

## 3. BFS — Breadth-First Search

### Algorithm
```
BFS(source):
  queue ← [source]
  visited[source] = true
  while queue not empty:
    u = queue.poll()
    for each neighbor v of u:
      if not visited[v]:
        visited[v] = true
        queue.offer(v)
```

### Properties
| Property | Value |
|---|---|
| Traversal order | Level by level (distance 0, 1, 2, ...) |
| Finds shortest path? | YES (unweighted graphs) |
| Data structure | Queue (FIFO) |
| Time | O(V + E) |
| Space | O(V) — visited array + queue |

### Proof of shortest path (unweighted):
> **Lemma:** BFS visits nodes in non-decreasing order of distance from source.  
> **Proof by induction:**
> - Base: source has distance 0, visited first. ✓
> - Inductive step: Assume all nodes at distance d are visited before any node at distance d+1.
>   When we process a node u at distance d, we discover its unvisited neighbors at distance d+1
>   and add them to the BACK of the queue. Since the queue is FIFO, all distance-d nodes are
>   processed before any distance-(d+1) node.

### When to use BFS:
- Shortest path in **unweighted** graphs
- Level-order traversal
- "Minimum steps" problems
- Multi-source BFS (multiple starting points — e.g., rotten oranges)

---

## 4. DFS — Depth-First Search

### Algorithm
```
DFS(u):
  visited[u] = true
  for each neighbor v of u:
    if not visited[v]:
      DFS(v)
```

### Properties
| Property | Value |
|---|---|
| Traversal order | Deep before wide (follows one path to its end) |
| Finds shortest path? | NO (in general) |
| Data structure | Call stack (recursion) or explicit Stack (LIFO) |
| Time | O(V + E) |
| Space | O(V) — visited + recursion stack (O(V) worst case) |

### DFS Edge Classification (Directed Graphs)
| Edge Type | Meaning | Detection |
|---|---|---|
| **Tree edge** | Part of DFS tree | First visit to v from u |
| **Back edge** | To an ancestor (→ CYCLE!) | v is on the current recursion stack |
| **Forward edge** | To a descendant (not tree) | v is visited, `disc[u] < disc[v]` |
| **Cross edge** | To a different subtree | v is visited, `disc[u] > disc[v]` |

### When to use DFS:
- Cycle detection
- Topological sorting
- Connected components
- Path finding (all paths, not shortest)
- Strongly connected components (Tarjan's, Kosaraju's)

---

## 5. BFS vs DFS — The Full Picture

| Criterion | BFS | DFS |
|---|---|---|
| Shortest path (unweighted) | ✓ Guaranteed | ✗ Not guaranteed |
| Space (worst case) | O(V) — wide queue | O(V) — deep stack |
| Space (binary tree) | O(V) — last level | O(log V) — height |
| Cycle detection | Works but DFS is simpler | Natural (back edge = cycle) |
| Topological sort | Kahn's algorithm (BFS) | DFS-based (reverse post-order) |
| Connected components | Works | Works (typically simpler) |
| Parallelism | Natural (level-by-level) | Harder to parallelize |

**Interview insight:** BFS = shortest path / minimum steps. DFS = cycles / topology / exhaustive exploration.

---

## 6. Shortest Path Algorithms

| Algorithm | Graph Type | Time | Space | Key Insight |
|---|---|---|---|---|
| **BFS** | Unweighted | O(V + E) | O(V) | Level-order = distance order |
| **Dijkstra** | Non-negative weights | O((V+E) log V) | O(V) | Greedy: always process closest node |
| **Bellman-Ford** | Any weights (neg allowed) | O(V × E) | O(V) | Relax ALL edges V-1 times |
| **Floyd-Warshall** | All-pairs shortest | O(V³) | O(V²) | DP: try each vertex as intermediate |
| **0-1 BFS** | Weights 0 or 1 only | O(V + E) | O(V) | Deque: weight-0 front, weight-1 back |
| **Topological + relax** | DAG | O(V + E) | O(V) | Process in topological order |

### Dijkstra's Algorithm (most important)
```
Dijkstra(source):
  dist[source] = 0, dist[all others] = ∞
  pq = min-heap with (0, source)
  while pq not empty:
    (d, u) = pq.poll()
    if d > dist[u]: continue  // stale entry
    for each (v, w) in adj[u]:
      if dist[u] + w < dist[v]:
        dist[v] = dist[u] + w
        pq.offer((dist[v], v))
```

**Why non-negative weights?** When we extract u from the priority queue, we're
guaranteed dist[u] is optimal (no future edge can reduce it further — because
all remaining edges have non-negative weight). With negative edges, this
guarantee breaks.

---

## 7. Union-Find (Disjoint Set Union)

```java
class UnionFind {
    int[] parent, rank;
    
    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }
    
    boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;  // already connected
        if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
        parent[py] = px;
        if (rank[px] == rank[py]) rank[px]++;
        return true;
    }
}
```

| Operation | Time |
|---|---|
| find() with path compression + union by rank | O(α(n)) ≈ O(1) amortized |
| α(n) = inverse Ackermann | Practically constant (≤ 4 for n < 10^80) |

### When to use Union-Find:
- Counting connected components dynamically
- Kruskal's MST
- Detecting cycles in undirected graphs
- "Group" / "union" / "connected" problems

---

## 8. Topological Sort

Ordering of vertices in a DAG such that for every edge u → v, u comes before v.

### Two approaches:

**A. Kahn's Algorithm (BFS-based):**
```
1. Compute in-degree for all vertices.
2. Add all vertices with in-degree 0 to queue.
3. While queue not empty:
   - Poll u, add to result.
   - For each neighbor v: indegree[v]--. If 0, add to queue.
4. If result.size() < V → cycle exists!
```

**B. DFS-based:**
```
1. DFS from each unvisited node.
2. After all neighbors processed, push u to stack.
3. Pop stack for topological order.
```

| Property | Kahn's (BFS) | DFS-based |
|---|---|---|
| Detects cycles | Yes (result.size() < V) | Yes (back edge in DFS) |
| Time | O(V + E) | O(V + E) |
| Useful for | Course schedule, build order | Same + SCC detection |

---

## 9. Key Java APIs for Graphs

```java
// Adjacency list (unweighted)
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

// Adjacency list (weighted)
List<List<int[]>> adj = new ArrayList<>();
adj.get(u).add(new int[]{v, weight});

// BFS queue
Queue<Integer> queue = new LinkedList<>();
// or: Deque<Integer> queue = new ArrayDeque<>();  // faster

// DFS stack (iterative)
Deque<Integer> stack = new ArrayDeque<>();

// Priority queue for Dijkstra
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
// pq stores {node, distance}

// Visited array
boolean[] visited = new boolean[n];

// Distance array
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[source] = 0;
```

---

## 10. Core Patterns in Graph Problems

| Pattern | Description | Key Insight |
|---|---|---|
| **BFS Shortest Path** | Minimum steps/distance in unweighted graph | Level-order = distance order |
| **Multi-Source BFS** | BFS from multiple start nodes simultaneously | Add ALL sources to queue initially |
| **DFS + Backtracking** | Explore all paths, undo choices | visited → recurse → unvisit |
| **Cycle Detection** | Find cycles in directed/undirected | DFS: back edge (directed), Union-Find (undirected) |
| **Topological Sort** | Order DAG vertices by dependency | Kahn's (BFS) or DFS post-order |
| **Union-Find** | Dynamic connectivity, component counting | Path compression + union by rank |
| **Dijkstra** | Shortest path with non-negative weights | Greedy: process closest first |
| **Grid as Graph** | 2D grid = implicit graph | 4-directional neighbors, bounds checking |
| **Bipartite Check** | 2-color the graph | BFS/DFS coloring; odd cycle = not bipartite |
| **Clone / Copy Graph** | Deep copy graph structure | HashMap: old node → new node |

---

## 11. Grid Problems = Graph Problems

Many interview problems use a 2D grid as an implicit graph:
- Each cell = vertex
- Adjacent cells (up/down/left/right) = edges

```java
// Standard 4-directional movement
int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};

for (int[] d : dirs) {
    int nr = r + d[0], nc = c + d[1];
    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
        // process (nr, nc)
    }
}
```

---

## 12. When NOT to Use Graph Algorithms

| Situation | Better Alternative |
|---|---|
| Tree structure (no cycles, connected) | Tree-specific DFS/BFS (simpler) |
| Dense graph + all-pairs shortest path | Floyd-Warshall (matrix DP) |
| Finding minimum spanning tree | Kruskal (edge list + UF) or Prim (adj list + PQ) |
| String transformation shortest path | BFS with word ladder pattern |
| Matching / flow problems | Specialized algorithms (Hopcroft-Karp, Ford-Fulkerson) |

---

*Next: → [01_easy.java](01_easy.java) — Easy problems applying these patterns*
