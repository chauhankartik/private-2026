/**
 * ============================================================
 *  GOOGLE-LEVEL GRAPH PROBLEMS
 *
 *  These problems are representative of actual Google interview
 *  questions. Each problem tests MULTIPLE topics simultaneously
 *  and requires system-level thinking.
 *
 *  Problems:
 *   G1. Shortest Path to Get All Keys (BFS + Bitmask State)
 *   G2. Swim in Rising Water (Binary Search + BFS / Dijkstra)
 *   G3. Making A Large Island (DFS + Connected Component ID)
 *   G4. Bus Routes (BFS on Route Graph)
 *   G5. Longest Path in DAG (Topological Sort + DP)
 *   G6. Minimum Height Trees (Graph Pruning / Peeling)
 *
 *  Multi-topic matrix:
 *   G1: BFS + Bitmask DP + State Space Search
 *   G2: Dijkstra / Binary Search + BFS + Monotonic reasoning
 *   G3: DFS + Component Labeling + Brute Force Optimization
 *   G4: BFS + Graph Abstraction + HashMap
 *   G5: Topological Sort + DP + DAG reasoning
 *   G6: Graph theory + Leaf pruning + Tree centroid
 * ============================================================
 */
import java.util.*;

public class GoogleLevel {

    // =========================================================
    // G1. SHORTEST PATH TO GET ALL KEYS
    // Topics: BFS + Bitmask State Space + Grid
    // =========================================================
    /**
     * Problem: Grid with '@' (start), 'a'-'f' (keys), 'A'-'F' (locks),
     * '.' (empty), '#' (wall). Find shortest path to collect ALL keys.
     * (LeetCode 864)
     *
     * Key insight — State = (row, col, keys_collected):
     *   Standard BFS on (row, col) doesn't work because visiting the same
     *   cell with DIFFERENT sets of keys leads to different outcomes.
     *
     *   Encode keys as a BITMASK: key 'a' = bit 0, 'b' = bit 1, etc.
     *   State = (row, col, keyBitmask). Total states: m × n × 2^k (k ≤ 6).
     *
     *   BFS on this state graph finds shortest path to state where
     *   keyBitmask = (1 << totalKeys) - 1 (all keys collected).
     *
     * Why bitmask?
     *   At most 6 keys → 2^6 = 64 possible key sets.
     *   Total states: m × n × 64 — very manageable.
     *
     * Time:  O(m × n × 2^k) — each state visited once
     * Space: O(m × n × 2^k) — visited set + queue
     *
     * Topics tested: State space BFS, bitmask DP, grid traversal.
     */
    public int shortestPathAllKeys(String[] grid) {
        int rows = grid.length, cols = grid[0].length();
        int startR = -1, startC = -1, totalKeys = 0;

        // Find start and count keys
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = grid[r].charAt(c);
                if (ch == '@') { startR = r; startC = c; }
                if (ch >= 'a' && ch <= 'f') totalKeys = Math.max(totalKeys, ch - 'a' + 1);
            }
        }

        int allKeys = (1 << totalKeys) - 1;
        // BFS: state = {row, col, keyBitmask}
        boolean[][][] visited = new boolean[rows][cols][allKeys + 1];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startR, startC, 0});
        visited[startR][startC][0] = true;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int[] curr = queue.poll();
                int r = curr[0], c = curr[1], keys = curr[2];

                if (keys == allKeys) return steps;  // all keys collected!

                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1], nkeys = keys;
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                    char ch = grid[nr].charAt(nc);
                    if (ch == '#') continue;  // wall
                    if (ch >= 'A' && ch <= 'F' && (keys & (1 << (ch - 'A'))) == 0)
                        continue;  // lock without key

                    if (ch >= 'a' && ch <= 'f')
                        nkeys |= (1 << (ch - 'a'));  // pick up key

                    if (!visited[nr][nc][nkeys]) {
                        visited[nr][nc][nkeys] = true;
                        queue.offer(new int[]{nr, nc, nkeys});
                    }
                }
            }
            steps++;
        }
        return -1;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if there are more than 6 keys (say 26)?
     *    → 2^26 = 67M states per cell. May need A* with heuristic or meet-in-the-middle.
     *
     * 2. What if some keys are optional (only need k of n keys)?
     *    → Target check: popcount(keys) >= k instead of keys == allKeys.
     *
     * 3. What if locks require MULTIPLE keys (combination lock)?
     *    → Locks become AND-conditions on bits. Same bitmask BFS.
     */

    // =========================================================
    // G2. SWIM IN RISING WATER
    // Topics: Dijkstra / Binary Search + BFS
    // =========================================================
    /**
     * Problem: n×n grid where elevation[r][c] = water level at which cell
     * becomes swimmable. Find minimum time T to swim from (0,0) to (n-1,n-1).
     * At time T, you can visit any cell with elevation ≤ T.
     * (LeetCode 778)
     *
     * ── Approach A: Dijkstra ──
     *   This is a shortest path problem where the "cost" is the maximum
     *   elevation encountered along the path (not sum of costs).
     *
     *   Modify Dijkstra: dist[r][c] = min over all paths of (max elevation on path).
     *   Relax: dist[v] = min(dist[v], max(dist[u], elevation[v]))
     *
     * ── Approach B: Binary Search + BFS ──
     *   Binary search on T: "Can we reach (n-1,n-1) if water level is T?"
     *   Validation: BFS/DFS only through cells with elevation ≤ T.
     *   Monotonic: if possible at time T, then possible at T+1. ✓
     *
     * Time:  O(n² log n) — Dijkstra with heap
     *        O(n² log(max)) — Binary Search + BFS
     * Space: O(n²)
     *
     * Topics tested: Dijkstra with modified relaxation, binary search on answer.
     */
    public int swimInWater(int[][] grid) {
        int n = grid.length;
        // Dijkstra: min-heap of {maxElevation, row, col}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);

        dist[0][0] = grid[0][0];
        pq.offer(new int[]{grid[0][0], 0, 0});

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int maxElev = curr[0], r = curr[1], c = curr[2];

            if (r == n - 1 && c == n - 1) return maxElev;
            if (maxElev > dist[r][c]) continue; // stale

            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
                    int newMax = Math.max(maxElev, grid[nr][nc]);
                    if (newMax < dist[nr][nc]) {
                        dist[nr][nc] = newMax;
                        pq.offer(new int[]{newMax, nr, nc});
                    }
                }
            }
        }
        return -1; // should never reach here
    }

    /**
     * Google Follow-ups:
     *
     * 1. Union-Find approach?
     *    → Sort cells by elevation. Add cells in order, unioning neighbors.
     *    → When (0,0) and (n-1,n-1) become connected, answer = current elevation.
     *    → O(n² × α(n²)) ≈ O(n²).
     *
     * 2. What if we can swim diagonally (8 directions)?
     *    → Add 4 more directions to Dijkstra. Same algorithm.
     *
     * 3. What if water rises at different rates in different cells?
     *    → More complex model. May need time-dependent Dijkstra.
     */

    // =========================================================
    // G3. MAKING A LARGE ISLAND
    // Topics: DFS + Component Labeling + Enumeration
    // =========================================================
    /**
     * Problem: In a binary grid (0/1), flip at most one 0 to 1. Find the
     * size of the largest island. (LeetCode 827)
     *
     * Naive: For each 0, temporarily flip to 1, run DFS for island size.
     *   → O(n⁴) — way too slow.
     *
     * Optimal — Two-phase approach:
     *   Phase 1: DFS to label each island with a unique ID and record its size.
     *   Phase 2: For each 0, check its 4 neighbors. Sum sizes of DISTINCT
     *            adjacent islands + 1 (the flipped cell).
     *
     * Why label with IDs?
     *   If a 0-cell is adjacent to the SAME island on two sides, we shouldn't
     *   count it twice. Using unique IDs lets us deduplicate.
     *
     * Time:  O(n²) — DFS for labeling + scan for each 0
     * Space: O(n²) — island labels + sizes map
     *
     * Topics tested: Connected components, precomputation, deduplication.
     */
    public int largestIsland(int[][] grid) {
        int n = grid.length;
        Map<Integer, Integer> islandSize = new HashMap<>();
        int id = 2; // start IDs from 2 (0 and 1 already used)

        // Phase 1: Label islands with unique IDs
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 1) {
                    int size = dfsLabel(grid, r, c, n, id);
                    islandSize.put(id, size);
                    id++;
                }
            }
        }

        // Phase 2: Try flipping each 0
        int maxSize = islandSize.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 0) {
                    Set<Integer> seen = new HashSet<>();
                    int total = 1; // the flipped cell itself
                    for (int[] d : dirs) {
                        int nr = r + d[0], nc = c + d[1];
                        if (nr >= 0 && nr < n && nc >= 0 && nc < n
                                && grid[nr][nc] > 1 && seen.add(grid[nr][nc])) {
                            total += islandSize.get(grid[nr][nc]);
                        }
                    }
                    maxSize = Math.max(maxSize, total);
                }
            }
        }
        return maxSize;
    }

    private int dfsLabel(int[][] grid, int r, int c, int n, int id) {
        if (r < 0 || r >= n || c < 0 || c >= n || grid[r][c] != 1) return 0;
        grid[r][c] = id;
        return 1 + dfsLabel(grid, r+1, c, n, id) + dfsLabel(grid, r-1, c, n, id)
                 + dfsLabel(grid, r, c+1, n, id) + dfsLabel(grid, r, c-1, n, id);
    }

    /**
     * Google Follow-ups:
     *
     * 1. Flip at most K zeros?
     *    → Much harder. BFS/DP with bitmask or greedy heuristic.
     *    → No known polynomial-time exact solution for general K.
     *
     * 2. What if the grid changes over time (add/remove cells)?
     *    → Dynamic connectivity with Union-Find. Support addLand(r,c).
     *    → LeetCode 305 (Number of Islands II).
     *
     * 3. What if we need to maximize PERIMETER instead of area?
     *    → Different metric. Count border edges per island.
     */

    // =========================================================
    // G4. BUS ROUTES
    // Topics: BFS on Route Graph + HashMap + Abstraction
    // =========================================================
    /**
     * Problem: routes[i] = list of stops for bus i. Start at source stop,
     * reach target stop. Find minimum number of BUS RIDES (not stops).
     * (LeetCode 815)
     *
     * Key insight — Abstraction:
     *   Don't BFS on stops (too many). BFS on ROUTES (buses).
     *   - Build: stopToRoutes map (which routes serve each stop).
     *   - From current route, you can transfer to any route that shares a stop.
     *   - BFS level = number of buses taken.
     *
     * Why route-level BFS?
     *   We want minimum bus RIDES, not minimum stops visited.
     *   Each BFS level = taking one more bus.
     *   From all stops of current route, find all connecting routes.
     *
     * Time:  O(N × R) — N = total stops across all routes, R = number of routes
     * Space: O(N × R) — stop-to-route mapping
     *
     * Topics tested: Graph abstraction, problem modeling, BFS level counting.
     */
    public int numBusesToDestination(int[][] routes, int source, int target) {
        if (source == target) return 0;

        // Build: stop → list of route indices
        Map<Integer, List<Integer>> stopToRoutes = new HashMap<>();
        for (int i = 0; i < routes.length; i++) {
            for (int stop : routes[i]) {
                stopToRoutes.computeIfAbsent(stop, k -> new ArrayList<>()).add(i);
            }
        }

        // BFS on routes
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visitedRoute = new boolean[routes.length];
        Set<Integer> visitedStop = new HashSet<>();

        // Start: add all routes that serve the source stop
        for (int routeId : stopToRoutes.getOrDefault(source, Collections.emptyList())) {
            queue.offer(routeId);
            visitedRoute[routeId] = true;
        }

        int buses = 1;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int routeId = queue.poll();
                for (int stop : routes[routeId]) {
                    if (stop == target) return buses;
                    if (!visitedStop.add(stop)) continue;

                    for (int nextRoute : stopToRoutes.getOrDefault(stop, Collections.emptyList())) {
                        if (!visitedRoute[nextRoute]) {
                            visitedRoute[nextRoute] = true;
                            queue.offer(nextRoute);
                        }
                    }
                }
            }
            buses++;
        }
        return -1;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if we also need the actual route (which buses to take)?
     *    → Track parentRoute[] during BFS. Reconstruct path.
     *
     * 2. What if buses have different costs?
     *    → Dijkstra on the route graph with weighted edges.
     *
     * 3. What if buses run on schedules (timestamps)?
     *    → Time-expanded graph: each (stop, time) is a node.
     *    → Add waiting edges + riding edges. BFS/Dijkstra on this graph.
     *    → This is how Google Maps transit routing works!
     */

    // =========================================================
    // G5. LONGEST PATH IN A DAG
    // Topics: Topological Sort + DP
    // =========================================================
    /**
     * Problem: Find the longest path in a Directed Acyclic Graph.
     * Specifically: Longest Increasing Path in a Matrix. (LeetCode 329)
     *
     * Key insight: The matrix IS a DAG.
     *   Edge from cell (r,c) to (nr,nc) if matrix[nr][nc] > matrix[r][c].
     *   No cycles possible (strictly increasing).
     *
     * DFS + Memoization = Topological DP on the implicit DAG.
     *   dp[r][c] = longest increasing path starting from (r,c).
     *   dp[r][c] = 1 + max(dp[nr][nc]) for all valid (nr,nc).
     *
     * Why this is essentially topological sort:
     *   We process nodes in reverse topological order (via DFS).
     *   dp values are computed in post-order.
     *
     * Time:  O(m × n) — each cell computed once (memoization)
     * Space: O(m × n) — dp cache + recursion stack
     *
     * Topics tested: DAG DP, DFS memoization, matrix as graph.
     */
    public int longestIncreasingPath(int[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        int[][] dp = new int[rows][cols];
        int maxPath = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                maxPath = Math.max(maxPath, dfsLIP(matrix, dp, r, c, rows, cols));
            }
        }
        return maxPath;
    }

    private int dfsLIP(int[][] matrix, int[][] dp, int r, int c, int rows, int cols) {
        if (dp[r][c] != 0) return dp[r][c]; // memoized

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        int maxLen = 1;

        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                    && matrix[nr][nc] > matrix[r][c]) {
                maxLen = Math.max(maxLen, 1 + dfsLIP(matrix, dp, nr, nc, rows, cols));
            }
        }
        dp[r][c] = maxLen;
        return maxLen;
    }

    /**
     * Google Follow-ups:
     *
     * 1. What if values can be equal (non-strictly increasing)?
     *    → The graph may have cycles (equal values create cycles).
     *    → Need cycle detection + different approach (can't use memoization directly).
     *
     * 2. Longest path in a general graph (with cycles)?
     *    → NP-hard in general. No polynomial algorithm known.
     *    → For DAGs: topological sort + DP is O(V + E).
     *
     * 3. Count the number of longest increasing paths?
     *    → Track both dp[r][c] (length) and count[r][c] (number of paths).
     *    → When extending, if length matches longest, add count.
     */

    // =========================================================
    // G6. MINIMUM HEIGHT TREES
    // Topics: Graph Pruning + Tree Centroid
    // =========================================================
    /**
     * Problem: Given a tree, find all roots that minimize the tree height.
     * (LeetCode 310)
     *
     * Key insight — there are at most 2 such roots (centroids of the tree).
     *
     * Algorithm — Leaf Peeling (topological sort on trees):
     *   1. Find all leaves (degree 1).
     *   2. Remove all leaves simultaneously (like peeling an onion).
     *   3. New leaves form the next layer.
     *   4. Repeat until ≤ 2 nodes remain. These are the roots.
     *
     * Why does this work?
     *   The center of a tree (minimizing max distance to any node)
     *   is always in the "middle." Peeling leaves from outside inward
     *   converges to the center — the centroid.
     *
     * Theorem: A tree has 1 or 2 centroids (centers of the longest path).
     *
     * Time:  O(V) — each node processed once
     * Space: O(V) — adjacency list + degree tracking
     *
     * Topics tested: Tree theory, BFS-like pruning, centroid decomposition.
     */
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        if (n == 1) return Collections.singletonList(0);
        if (n == 2) return Arrays.asList(0, 1);

        // Build adjacency list and track degree
        List<Set<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new HashSet<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        // Find initial leaves
        Queue<Integer> leaves = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (adj.get(i).size() == 1) leaves.offer(i);
        }

        // Peel layers until ≤ 2 nodes remain
        int remaining = n;
        while (remaining > 2) {
            int size = leaves.size();
            remaining -= size;
            Queue<Integer> newLeaves = new LinkedList<>();

            for (int i = 0; i < size; i++) {
                int leaf = leaves.poll();
                for (int neighbor : adj.get(leaf)) {
                    adj.get(neighbor).remove(leaf);
                    if (adj.get(neighbor).size() == 1) {
                        newLeaves.offer(neighbor);
                    }
                }
            }
            leaves = newLeaves;
        }
        return new ArrayList<>(leaves);
    }

    /**
     * Google Follow-ups:
     *
     * 1. What is the tree centroid formally?
     *    → The vertex that minimizes the maximum subtree size when rooted.
     *    → Equivalently: the center of the tree's diameter (longest path).
     *
     * 2. Can this be solved with DFS?
     *    → Yes. Find diameter endpoints with 2 BFS/DFS calls.
     *    → Centroid = middle of the diameter path.
     *    → O(V) — same complexity, but leaf peeling is more elegant.
     *
     * 3. What about centroid decomposition for divide and conquer on trees?
     *    → Advanced technique. Decompose tree using centroids recursively.
     *    → Depth of decomposition tree = O(log n).
     *    → Used for: path queries, distance queries on trees.
     *
     * 4. What if the graph is NOT a tree (has extra edges)?
     *    → First find spanning tree, then apply MHT on it.
     *    → Or: BFS from all nodes, find node minimizing max BFS depth.
     */
}
