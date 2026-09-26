# Graphs — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "shortest path" / "minimum steps" (unweighted) | BFS |
| "shortest path" (weighted, non-negative) | Dijkstra |
| "shortest path" (negative weights) | Bellman-Ford |
| "all-pairs shortest path" | Floyd-Warshall |
| "can you finish all courses?" / "dependencies" | Topological Sort (Kahn's BFS) |
| "ordering" / "schedule" | Topological Sort |
| "cycle detection" (directed) | DFS with 3-state coloring |
| "cycle detection" (undirected) | Union-Find (or DFS with parent tracking) |
| "connected components" / "groups" | Union-Find or DFS component counting |
| "merge accounts" / "union groups" | Union-Find + HashMap |
| "grid with 1s and 0s" / "island" | DFS/BFS on grid (implicit graph) |
| "rotten oranges" / "simultaneous spread" | Multi-Source BFS |
| "minimum spanning tree" | Prim's (dense) or Kruskal's (sparse) |
| "bipartite" / "2-colorable" | BFS/DFS 2-coloring |
| "bridges" / "critical connections" | Tarjan's algorithm (disc/low arrays) |
| "clone/copy graph" | HashMap (old→new) + DFS/BFS |
| "word transformation" / "state space" | BFS on implicit graph |
| "collect all items" / "multiple objectives" | BFS + Bitmask state |
| "minimum time/cost with constraints" | Modified Dijkstra / BFS with state |
| "minimize the maximum along path" | Dijkstra with max-relaxation / Binary search + BFS |
| "longest path" (DAG / matrix) | DFS + Memoization (topological DP) |
| "tree center" / "minimize height" | Leaf peeling (tree centroid) |

---

## Java Graph Idioms (Memorize These)

```java
// 1. Adjacency list (unweighted) — THE standard setup
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
for (int[] e : edges) {
    adj.get(e[0]).add(e[1]);
    adj.get(e[1]).add(e[0]); // omit for directed
}

// 2. Adjacency list (weighted)
List<List<int[]>> adj = new ArrayList<>();
adj.get(u).add(new int[]{v, weight});

// 3. BFS template
Queue<Integer> queue = new LinkedList<>();
boolean[] visited = new boolean[n];
queue.offer(source); visited[source] = true;
while (!queue.isEmpty()) {
    int u = queue.poll();
    for (int v : adj.get(u)) {
        if (!visited[v]) { visited[v] = true; queue.offer(v); }
    }
}

// 4. DFS template (recursive)
void dfs(int u, boolean[] visited, List<List<Integer>> adj) {
    visited[u] = true;
    for (int v : adj.get(u)) {
        if (!visited[v]) dfs(v, visited, adj);
    }
}

// 5. Grid 4-direction template
int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
for (int[] d : dirs) {
    int nr = r + d[0], nc = c + d[1];
    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
        // process neighbor
    }
}

// 6. Dijkstra setup
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]); // {node, dist}
int[] dist = new int[n]; Arrays.fill(dist, Integer.MAX_VALUE); dist[src] = 0;
pq.offer(new int[]{src, 0});

// 7. Union-Find (always have this ready to write in 30 seconds)
int find(int[] p, int x) { return p[x] == x ? x : (p[x] = find(p, p[x])); }
boolean union(int[] p, int[] r, int x, int y) {
    int px = find(p, x), py = find(p, y);
    if (px == py) return false;
    if (r[px] < r[py]) { int t = px; px = py; py = t; }
    p[py] = px; if (r[px] == r[py]) r[px]++;
    return true;
}

// 8. Topological sort (Kahn's BFS)
int[] inDegree = new int[n];
// ... compute in-degrees from adjacency list
Queue<Integer> q = new LinkedList<>();
for (int i = 0; i < n; i++) if (inDegree[i] == 0) q.offer(i);
while (!q.isEmpty()) {
    int u = q.poll(); // process u
    for (int v : adj.get(u)) { if (--inDegree[v] == 0) q.offer(v); }
}
```

---

## Complexity Quick Reference

| Algorithm | Time | Space | When to Use |
|---|---|---|---|
| BFS | O(V + E) | O(V) | Shortest path (unweighted), level-order |
| DFS | O(V + E) | O(V) | Cycles, topology, exhaustive search |
| Dijkstra | O((V+E) log V) | O(V) | Shortest path (non-negative weights) |
| Bellman-Ford | O(V × E) | O(V) | Negative weights, detect neg. cycles |
| Floyd-Warshall | O(V³) | O(V²) | All-pairs shortest paths |
| Kahn's Topo Sort | O(V + E) | O(V) | DAG ordering, cycle detection |
| Union-Find | O(E × α(V)) | O(V) | Connectivity, components, MST |
| Prim's MST | O((V+E) log V) | O(V) | Dense graph MST |
| Kruskal's MST | O(E log E) | O(V) | Sparse graph MST |
| Tarjan's (bridges) | O(V + E) | O(V) | Bridges, articulation points, SCC |

---

## BFS Shortest Path Template

```java
// Shortest path in unweighted graph
int bfsShortestPath(List<List<Integer>> adj, int src, int dst, int n) {
    boolean[] visited = new boolean[n];
    Queue<Integer> queue = new LinkedList<>();
    queue.offer(src); visited[src] = true;
    int dist = 0;
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int u = queue.poll();
            if (u == dst) return dist;
            for (int v : adj.get(u)) {
                if (!visited[v]) { visited[v] = true; queue.offer(v); }
            }
        }
        dist++;
    }
    return -1; // unreachable
}
```

---

## Dijkstra Template

```java
int[] dijkstra(List<List<int[]>> adj, int src, int n) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    pq.offer(new int[]{src, 0});
    
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int u = curr[0], d = curr[1];
        if (d > dist[u]) continue;  // *** STALE CHECK — never forget ***
        for (int[] edge : adj.get(u)) {
            int v = edge[0], w = edge[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    return dist;
}
```

---

## Union-Find Template

```java
class UF {
    int[] parent, rank;
    int components;
    
    UF(int n) {
        parent = new int[n]; rank = new int[n]; components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }
    
    boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;
        if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
        parent[py] = px;
        if (rank[px] == rank[py]) rank[px]++;
        components--;
        return true;
    }
}
```

---

## Problem → Pattern Map (for Google interviews)

```
Number of Islands       → DFS/BFS grid flood fill
Course Schedule         → Topological Sort (Kahn's)
Network Delay Time      → Dijkstra
Rotten Oranges          → Multi-Source BFS
Word Ladder             → BFS on implicit graph
Redundant Connection    → Union-Find cycle detection
Clone Graph             → HashMap + DFS/BFS
Accounts Merge          → Union-Find + HashMap
Alien Dictionary        → Topological Sort on chars
Critical Connections    → Tarjan's bridge finding
Min Cost Connect Points → Prim's MST
Cheapest Flights (K)    → Modified Bellman-Ford
Swim in Rising Water    → Dijkstra (max-relaxation)
Shortest Path All Keys  → BFS + Bitmask
Longest Increasing Path → DFS + Memoization (DAG DP)
Minimum Height Trees    → Leaf peeling (centroid)
```

---

## Interview Communication Script

When you start solving any graph problem:

> "Let me model this as a graph. The vertices are [nodes/cells/states],
> and edges connect [adjacent cells / dependent items / transformations].
> This is [directed/undirected], [weighted/unweighted], [may/may not have cycles].
>
> Since we need [shortest path / cycle detection / ordering / connectivity]:
> - Shortest path + unweighted → BFS
> - Shortest path + weighted → Dijkstra
> - Dependencies / ordering → Topological Sort
> - Connectivity / grouping → Union-Find
> - Cycle detection → DFS with state coloring
>
> Let me code the [algorithm] now..."

---

## Complexity Proof Keywords for Interviews

When asked "why is this O(V + E)?", use these arguments:

- **BFS/DFS:** "Each vertex is visited once (O(V)). Each edge is examined once 
  in directed (twice in undirected) (O(E)). Total: O(V + E)."
- **Dijkstra:** "Each vertex extracted from heap once (O(V log V)). Each edge relaxed 
  once (O(E log V)). Total: O((V+E) log V)."
- **Union-Find:** "Each find with path compression is amortized O(α(n)). Over E 
  operations, total is O(E × α(n)) which is practically O(E)."
- **Multi-source BFS:** "Same as BFS — adding multiple sources doesn't change 
  asymptotic complexity. Each cell still visited at most once."
- **Topological sort:** "Each vertex processed once, each edge examined once for 
  in-degree updates. O(V + E)."
- **Tarjan's:** "Single DFS with O(1) work per node/edge. O(V + E)."

---

*Files in this module:*
- `00_theory.md` — Representations, BFS/DFS proofs, algorithms, Java APIs
- `01_easy.java` — 8 easy problems with follow-ups
- `02_medium.java` — 8 medium problems (topo sort, Dijkstra, multi-source BFS)
- `03_hard.java` — 6 hard problems (MST, Tarjan's, modified shortest path)
- `04_google_level.java` — 6 Google-level multi-topic problems
- `05_cheatsheet.md` — This file
