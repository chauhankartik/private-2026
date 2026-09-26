# Graphs — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase introduces a new graph traversal/algorithm pattern.

---

## Phase 1: BFS / DFS Fundamentals (Grid traversal)

*Goal: Master grid-based BFS/DFS — the most common graph problems in interviews.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Number of Islands | 200 | Medium | DFS/BFS flood fill, mark visited |
| 2 | Flood Fill | 733 | Easy | BFS/DFS from source, change color |
| 3 | Max Area of Island | 695 | Medium | DFS, count cells in each component |
| 4 | Surrounded Regions | 130 | Medium | BFS from borders first, then flip remaining |
| 5 | Number of Enclaves | 1020 | Medium | Same as #4, count instead of flip |
| 6 | Rotting Oranges | 994 | Medium | Multi-source BFS, track levels (time) |
| 7 | 01 Matrix | 542 | Medium | Multi-source BFS from all 0s simultaneously |
| 8 | Pacific Atlantic Water Flow | 417 | Medium | Two BFS/DFS from each ocean, intersect |

---

## Phase 2: BFS / DFS on Graphs (Adjacency list)

*Goal: Move from grids to explicit graph representations.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 9 | Clone Graph | 133 | Medium | BFS/DFS + HashMap (original→clone) |
| 10 | Number of Connected Components | 323 | Medium | DFS/BFS or Union-Find, count components |
| 11 | Graph Valid Tree | 261 | Medium | n-1 edges + connected = tree |
| 12 | All Paths From Source to Target | 797 | Medium | DFS/backtracking on DAG |
| 13 | Find if Path Exists in Graph | 1971 | Easy | BFS/DFS or Union-Find |
| 14 | Keys and Rooms | 841 | Medium | BFS/DFS, collect keys to unlock rooms |
| 15 | Is Graph Bipartite? | 785 | Medium | BFS/DFS 2-coloring |

---

## Phase 3: Topological Sort (DAGs, prerequisites)

*Goal: Order nodes respecting dependencies. Detect cycles in directed graphs.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 16 | Course Schedule | 207 | Medium | Cycle detection via topo sort (Kahn's BFS) |
| 17 | Course Schedule II | 210 | Medium | Return the topological order |
| 18 | Alien Dictionary | 269 | Hard | Build graph from word order, topo sort |
| 19 | Minimum Height Trees | 310 | Medium | Peel leaves layer by layer (reverse topo) |
| 20 | Parallel Courses | 1136 | Medium | Topo sort, track levels = parallel rounds |
| 21 | Find All Possible Recipes | 2115 | Medium | Topo sort with ingredient dependencies |

---

## Phase 4: Shortest Path (BFS for unweighted, Dijkstra for weighted)

*Goal: BFS = shortest in unweighted. Dijkstra = shortest in weighted (non-negative).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 22 | Shortest Path in Binary Matrix | 1091 | Medium | BFS, 8-directional, track distance |
| 23 | Word Ladder | 127 | Hard | BFS, each word is a node, edges = 1-char diff |
| 24 | Network Delay Time | 743 | Medium | Dijkstra's algorithm |
| 25 | Path With Minimum Effort | 1631 | Medium | Dijkstra on grid (minimize max edge) |
| 26 | Cheapest Flights Within K Stops | 787 | Medium | BFS/Bellman-Ford with stop limit |
| 27 | Swim in Rising Water | 778 | Hard | Dijkstra or binary search + BFS |
| 28 | Shortest Path to Get All Keys | 864 | Hard | BFS with bitmask state |

---

## Phase 5: Union-Find (Disjoint Set Union)

*Goal: Group elements dynamically. union() + find() with path compression and rank.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 29 | Number of Provinces | 547 | Medium | Union-Find or DFS, count components |
| 30 | Redundant Connection | 684 | Medium | Union-Find, edge that forms cycle |
| 31 | Accounts Merge | 721 | Medium | Union-Find on emails, group by person |
| 32 | Longest Consecutive Sequence | 128 | Medium | Union-Find or HashSet approach |
| 33 | Most Stones Removed | 947 | Medium | Union-Find on rows/cols, answer = n - components |
| 34 | Smallest String With Swaps | 1202 | Medium | Union-Find groups, sort within groups |

---

## Phase 6: Minimum Spanning Tree

*Goal: Kruskal's (sort edges + Union-Find) or Prim's (min-heap).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 35 | Min Cost to Connect All Points | 1584 | Medium | Prim's or Kruskal's on complete graph |
| 36 | Connecting Cities With Minimum Cost | 1135 | Medium | Kruskal's: sort edges, union, stop at n-1 |
| 37 | Find Critical and Pseudo-Critical Edges | 1489 | Hard | Kruskal's + edge inclusion/exclusion |

---

## Phase 7: Advanced Graph Algorithms

*Goal: Tarjan's, Bellman-Ford, Floyd-Warshall, and special graph problems.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 38 | Critical Connections in a Network | 1192 | Hard | Tarjan's bridge-finding algorithm |
| 39 | Evaluate Division | 399 | Medium | Weighted graph, BFS/DFS for path product |
| 40 | Reconstruct Itinerary | 332 | Hard | Eulerian path (Hierholzer's algorithm) |
| 41 | Word Ladder II | 126 | Hard | BFS for shortest paths + DFS backtrack |
| 42 | Graph Coloring / m-Coloring | — | Medium | Backtracking with constraint checking |

---

## Phase 8: Google-Level / Multi-Pattern

*Goal: Complex state BFS, implicit graphs, advanced modeling.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 43 | Bus Routes | 815 | Hard | BFS on routes (not stops), route-level graph |
| 44 | Snakes and Ladders | 909 | Medium | BFS on board positions |
| 45 | Minimum Moves to Reach Target with Rotations | 1210 | Hard | BFS with state = (position, orientation) |
| 46 | Shortest Path Visiting All Nodes | 847 | Hard | BFS + bitmask for visited set |
| 47 | Making A Large Island | 827 | Hard | Label islands + check each 0's neighbors |
| 48 | Detect Cycles in 2D Grid | 1559 | Medium | DFS with parent tracking |
| 49 | Path With Maximum Probability | 1514 | Medium | Modified Dijkstra (maximize product) |
| 50 | Minimum Cost to Make at Least One Valid Path | 1368 | Hard | 0-1 BFS (deque-based Dijkstra) |

---

## Study Roadmap

```
Week 1 (Days 1-3):  Phase 1 — Grid BFS/DFS (#1-#8)
Week 1 (Days 4-5):  Phase 2 — Graph BFS/DFS (#9-#15)
Week 2 (Days 1-2):  Phase 3 — Topological sort (#16-#21)
Week 2 (Days 3-5):  Phase 4 — Shortest path (#22-#28)
Week 3 (Days 1-2):  Phase 5 — Union-Find (#29-#34)
Week 3 (Day 3):     Phase 6 — MST (#35-#37)
Week 3 (Days 4-5):  Phase 7-8 — Advanced (#38-#50)
```
