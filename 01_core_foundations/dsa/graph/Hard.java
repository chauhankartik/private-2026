
/**
 * ============================================================
 *  GRAPHS — HARD PROBLEMS
 *  Each problem requires combining graph algorithms with advanced
 *  algorithmic thinking and multi-pattern reasoning.
 * ============================================================
 *
 *  Problems:
 *   H1. Shortest Path in Binary Matrix (BFS with 8 directions)
 *   H2. Minimum Cost to Connect All Points (MST — Prim/Kruskal)
 *   H3. Cheapest Flights Within K Stops (Modified Dijkstra / BFS)
 *   H4. Critical Connections (Tarjan's Bridge Finding)
 *   H5. Accounts Merge (Union-Find + HashMap)
 *   H6. Alien Dictionary (Topological Sort on Characters)
 *
 * ============================================================
 */
import java.util.*;

public class Hard {

    // =========================================================
    // H1. SHORTEST PATH IN BINARY MATRIX
    // Pattern: BFS — 8-Directional Shortest Path
    // =========================================================
    /**
     * Problem: Find shortest path from top-left (0,0) to bottom-right (n-1,n-1)
     * in an n×n grid where 0 = passable, 1 = blocked. Move in 8 directions.
     * (LeetCode 1091)
     *
     * This is standard BFS on a grid — but with 8 directions instead of 4.
     *
     * Key insight: BFS guarantees shortest path in an unweighted graph.
     * The grid is an implicit graph with equal-weight edges.
     * Include diagonal moves: 8 neighbors per cell.
     *
     * Why BFS, not DFS?
     * DFS might find A path but not the SHORTEST path.
     * BFS explores level-by-level → first time we reach target = shortest path.
     *
     * Time: O(n²) — each cell visited at most once
     * Space: O(n²) — queue + visited
     */
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;
        if (grid[0][0] == 1 || grid[n - 1][n - 1] == 1)
            return -1;

        int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[] { 0, 0 });
        grid[0][0] = 1; // mark visited by blocking
        int pathLen = 1;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int[] cell = queue.poll();
                int r = cell[0], c = cell[1];
                if (r == n - 1 && c == n - 1)
                    return pathLen;

                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 0) {
                        grid[nr][nc] = 1; // mark visited
                        queue.offer(new int[] { nr, nc });
                    }
                }
            }
            pathLen++;
        }
        return -1;
    }

    /**
     * Follow-up 1: A* search for faster performance?
     * → Heuristic: Chebyshev distance (max(|dr|, |dc|)) since 8-directional.
     * → A* expands fewer nodes than BFS in practice.
     *
     * Follow-up 2: What if some cells have different traversal costs?
     * → Weighted graph → Dijkstra or 0-1 BFS (if only 2 weights).
     *
     * Follow-up 3: Track the actual shortest path (not just length)?
     * → Store parent[r][c] = previous cell. Reconstruct path by backtracking from
     * target.
     */

    // =========================================================
    // H2. MINIMUM COST TO CONNECT ALL POINTS (MST)
    // Pattern: Prim's Algorithm / Kruskal's Algorithm
    // =========================================================
    /**
     * Problem: Given n points in 2D, find minimum cost to connect ALL points.
     * Cost of connecting (xi,yi) and (xj,yj) = |xi-xj| + |yi-yj| (Manhattan
     * distance).
     * (LeetCode 1584)
     *
     * This is the MINIMUM SPANNING TREE (MST) problem.
     *
     * ── Prim's Algorithm ──
     * Greedy: start from any node, always add the cheapest edge to an
     * unvisited node. Use a min-heap.
     *
     * ── Kruskal's Algorithm ──
     * Sort all edges by weight. Add cheapest edge that doesn't form a cycle
     * (checked via Union-Find). Stop when n-1 edges added.
     *
     * Prim's is better here: dense graph (all-pairs edges = O(n²)).
     * Prim's: O(n² log n) with heap (or O(n²) with simple array for dense).
     * Kruskal's: O(n² log n²) = O(n² log n) for sorting n² edges.
     *
     * Time: O(n² log n) — n² edges considered, log n per heap op
     * Space: O(n²) — edges in heap (worst case)
     */
    public int minCostConnectPoints(int[][] points) {
        int n = points.length;
        boolean[] inMST = new boolean[n];
        // min-heap: {cost, point_index}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.offer(new int[] { 0, 0 }); // start from point 0

        int totalCost = 0, edgesUsed = 0;

        while (edgesUsed < n) {
            int[] curr = pq.poll();
            int cost = curr[0], u = curr[1];
            if (inMST[u])
                continue;

            inMST[u] = true;
            totalCost += cost;
            edgesUsed++;

            // Add edges to all non-MST points
            for (int v = 0; v < n; v++) {
                if (!inMST[v]) {
                    int dist = Math.abs(points[u][0] - points[v][0])
                            + Math.abs(points[u][1] - points[v][1]);
                    pq.offer(new int[] { dist, v });
                }
            }
        }
        return totalCost;
    }

    /** Kruskal's approach — for comparison */
    public int minCostConnectPointsKruskal(int[][] points) {
        int n = points.length;
        // Generate all edges
        List<int[]> edges = new ArrayList<>(); // {cost, u, v}
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int cost = Math.abs(points[i][0] - points[j][0])
                        + Math.abs(points[i][1] - points[j][1]);
                edges.add(new int[] { cost, i, j });
            }
        }
        edges.sort((a, b) -> a[0] - b[0]);

        // Union-Find
        int[] parent = new int[n], rank = new int[n];
        for (int i = 0; i < n; i++)
            parent[i] = i;

        int totalCost = 0, edgesUsed = 0;
        for (int[] e : edges) {
            if (edgesUsed == n - 1)
                break;
            int pu = findUF(parent, e[1]), pv = findUF(parent, e[2]);
            if (pu != pv) {
                totalCost += e[0];
                edgesUsed++;
                if (rank[pu] < rank[pv]) {
                    int t = pu;
                    pu = pv;
                    pv = t;
                }
                parent[pv] = pu;
                if (rank[pu] == rank[pv])
                    rank[pu]++;
            }
        }
        return totalCost;
    }

    private int findUF(int[] parent, int x) {
        if (parent[x] != x)
            parent[x] = findUF(parent, parent[x]);
        return parent[x];
    }

    /**
     * Follow-up 1: Prim's vs Kruskal's — when to choose which?
     * → Prim's: dense graphs (adjacency matrix), simple implementation.
     * → Kruskal's: sparse graphs (edge list), naturally uses Union-Find.
     *
     * Follow-up 2: What if we want the SECOND minimum spanning tree?
     * → For each MST edge, remove it and find MST of remaining graph.
     * → The best among these is the second MST. O(VE log V).
     *
     * Follow-up 3: What about Euclidean MST (L2 distance)?
     * → Delaunay triangulation reduces to O(n) candidate edges.
     * → Then Kruskal's on O(n) edges: O(n log n) total.
     */

    // =========================================================
    // H3. CHEAPEST FLIGHTS WITHIN K STOPS
    // Pattern: Modified BFS / Bellman-Ford with relaxation limit
    // =========================================================
    /**
     * Problem: Find cheapest price from src to dst with at most k stops
     * (k+1 edges). Return -1 if no such route. (LeetCode 787)
     *
     * Why plain Dijkstra fails:
     * Dijkstra finds the shortest path overall, but doesn't respect the
     * constraint on number of edges/stops. A longer (more costly) path
     * might have fewer stops and lead to a valid solution.
     *
     * ── Approach A: Modified Bellman-Ford ──
     * Run Bellman-Ford for exactly k+1 iterations (relaxations).
     * In round i, we find shortest paths using at most i edges.
     *
     * ── Approach B: BFS with levels ──
     * BFS level = number of stops used. Process level by level (max k+1 levels).
     * Track minimum cost to reach each node.
     *
     * Time: O(k × E) — Bellman-Ford variant
     * Space: O(V) — distance array
     */
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        // Bellman-Ford with k+1 relaxation rounds
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (int i = 0; i <= k; i++) {
            int[] temp = Arrays.copyOf(dist, n); // snapshot to avoid cascading updates
            for (int[] f : flights) {
                int u = f[0], v = f[1], w = f[2];
                if (dist[u] != Integer.MAX_VALUE && dist[u] + w < temp[v]) {
                    temp[v] = dist[u] + w;
                }
            }
            dist = temp;
        }
        return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
    }

    /** BFS approach — clearer level-by-level logic */
    public int findCheapestPriceBFS(int n, int[][] flights, int src, int dst, int k) {
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++)
            adj.add(new ArrayList<>());
        for (int[] f : flights)
            adj.get(f[0]).add(new int[] { f[1], f[2] });

        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        Queue<int[]> queue = new LinkedList<>(); // {node, cost}
        queue.offer(new int[] { src, 0 });

        int stops = 0;
        while (!queue.isEmpty() && stops <= k) {
            int size = queue.size();
            int[] tempDist = Arrays.copyOf(dist, n);
            for (int i = 0; i < size; i++) {
                int[] curr = queue.poll();
                int u = curr[0], cost = curr[1];
                for (int[] edge : adj.get(u)) {
                    int v = edge[0], w = edge[1];
                    if (cost + w < tempDist[v]) {
                        tempDist[v] = cost + w;
                        queue.offer(new int[] { v, cost + w });
                    }
                }
            }
            dist = tempDist;
            stops++;
        }
        return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
    }

    /**
     * Follow-up 1: Dijkstra with state (node, stops)?
     * → State = (cost, node, stopsUsed). Explore state space.
     * → PQ ordered by cost. When reaching dst, return cost.
     * → O(V×K × log(V×K)) — can be faster in practice.
     *
     * Follow-up 2: What if there are negative prices (discounts)?
     * → Bellman-Ford handles negative weights naturally.
     * → But check for negative cycles (not applicable here).
     *
     * Follow-up 3: Multiple queries with same graph, different k?
     * → Precompute dist[k][v] for all k using DP.
     * → dist[i][v] = min over all edges (u,v) of dist[i-1][u] + w(u,v).
     */

    // =========================================================
    // H4. CRITICAL CONNECTIONS IN A NETWORK (Bridges)
    // Pattern: Tarjan's Algorithm — Bridge Finding
    // =========================================================
    /**
     * Problem: Find all edges whose removal disconnects the graph.
     * (LeetCode 1192)
     *
     * These edges are called BRIDGES (critical connections).
     *
     * Tarjan's Algorithm:
     * DFS the graph. For each node u, track:
     * - disc[u]: discovery time (when u was first visited)
     * - low[u]: lowest discovery time reachable from subtree rooted at u
     *
     * An edge (u, v) is a bridge if low[v] > disc[u]:
     * This means v's subtree has NO back edge to u or any ancestor of u.
     * Removing (u, v) disconnects v's subtree.
     *
     * Why low[v] > disc[u] detects bridges:
     * low[v] = min disc time reachable from v via tree + back edges.
     * If low[v] > disc[u], there's no "alternative path" from v to u.
     * So (u, v) is the ONLY connection → bridge.
     *
     * Time: O(V + E) — single DFS
     * Space: O(V + E) — adjacency list + disc/low arrays + recursion stack
     */
    private int timer = 0;

    public List<List<Integer>> criticalConnections(int n, List<List<Integer>> connections) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++)
            adj.add(new ArrayList<>());
        for (List<Integer> c : connections) {
            adj.get(c.get(0)).add(c.get(1));
            adj.get(c.get(1)).add(c.get(0));
        }

        int[] disc = new int[n], low = new int[n];
        Arrays.fill(disc, -1);
        List<List<Integer>> bridges = new ArrayList<>();
        timer = 0;

        dfsBridge(adj, disc, low, bridges, 0, -1);
        return bridges;
    }

    private void dfsBridge(List<List<Integer>> adj, int[] disc, int[] low,
            List<List<Integer>> bridges, int u, int parent) {
        disc[u] = low[u] = timer++;

        for (int v : adj.get(u)) {
            if (v == parent)
                continue; // skip the edge we came from
            if (disc[v] == -1) {
                // Tree edge
                dfsBridge(adj, disc, low, bridges, v, u);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > disc[u]) {
                    bridges.add(Arrays.asList(u, v)); // BRIDGE found!
                }
            } else {
                // Back edge — update low[u]
                low[u] = Math.min(low[u], disc[v]);
            }
        }
    }

    /**
     * Follow-up 1: Find ARTICULATION POINTS (critical nodes)?
     * → Same Tarjan's DFS. Node u is articulation point if:
     * - u is root with ≥ 2 children, OR
     * - u is not root and low[v] ≥ disc[u] for some child v.
     *
     * Follow-up 2: What about directed graphs?
     * → Bridges in directed graphs = different concept (strong bridges).
     * → Use Tarjan's SCC (strongly connected components) algorithm.
     *
     * Follow-up 3: Can we pre-process to answer bridge queries in O(1)?
     * → Build bridge tree. Edges between 2-edge-connected components = bridges.
     * → LCA queries on the bridge tree answer connectivity queries.
     */

    // =========================================================
    // H5. ACCOUNTS MERGE
    // Pattern: Union-Find + HashMap (Grouping by Identity)
    // =========================================================
    /**
     * Problem: Given accounts [[name, email1, email2, ...], ...],
     * merge accounts belonging to the same person (sharing any email).
     * (LeetCode 721)
     *
     * Key insight: Emails are the nodes. If two emails appear in the same
     * account, they belong to the same person → UNION them.
     *
     * Algorithm:
     * 1. Map each email to an ID (integer). Track email → owner name.
     * 2. For each account, union all emails in that account.
     * 3. Group emails by their root parent (after unions).
     * 4. Sort each group, prepend the name, add to result.
     *
     * Time: O(N × α(N) + N log N) — N = total emails, α for UF, log N for sort
     * Space: O(N)
     */
    public List<List<String>> accountsMerge(List<List<String>> accounts) {
        // Email → ID mapping
        Map<String, Integer> emailToId = new HashMap<>();
        Map<String, String> emailToName = new HashMap<>();
        int id = 0;

        for (List<String> account : accounts) {
            String name = account.get(0);
            for (int i = 1; i < account.size(); i++) {
                String email = account.get(i);
                emailToName.put(email, name);
                if (!emailToId.containsKey(email)) {
                    emailToId.put(email, id++);
                }
            }
        }

        // Union-Find
        int[] parent = new int[id], rank = new int[id];
        for (int i = 0; i < id; i++)
            parent[i] = i;

        for (List<String> account : accounts) {
            int firstId = emailToId.get(account.get(1));
            for (int i = 2; i < account.size(); i++) {
                int currId = emailToId.get(account.get(i));
                unionAccounts(parent, rank, firstId, currId);
            }
        }

        // Group emails by root
        Map<Integer, List<String>> groups = new HashMap<>();
        for (String email : emailToId.keySet()) {
            int root = findAccounts(parent, emailToId.get(email));
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(email);
        }

        // Build result
        List<List<String>> result = new ArrayList<>();
        for (List<String> emails : groups.values()) {
            Collections.sort(emails);
            List<String> merged = new ArrayList<>();
            merged.add(emailToName.get(emails.get(0)));
            merged.addAll(emails);
            result.add(merged);
        }
        return result;
    }

    private int findAccounts(int[] parent, int x) {
        if (parent[x] != x)
            parent[x] = findAccounts(parent, parent[x]);
        return parent[x];
    }

    private void unionAccounts(int[] parent, int[] rank, int x, int y) {
        int px = findAccounts(parent, x), py = findAccounts(parent, y);
        if (px == py)
            return;
        if (rank[px] < rank[py]) {
            int t = px;
            px = py;
            py = t;
        }
        parent[py] = px;
        if (rank[px] == rank[py])
            rank[px]++;
    }

    /**
     * Follow-up 1: DFS/BFS on email graph?
     * → Build adjacency list of emails. DFS connected components.
     * → Same complexity but Union-Find is more natural for "merge" problems.
     *
     * Follow-up 2: Streaming accounts (arrive one at a time)?
     * → Union-Find naturally supports incremental additions.
     * → New account: create/union emails. Query: find root.
     *
     * Follow-up 3: Scale to billions of accounts (distributed)?
     * → Consistent hashing + distributed Union-Find.
     * → Map-Reduce: map phase emits (email, accountId) pairs,
     * reduce phase unions connected accounts.
     */

    // =========================================================
    // H6. ALIEN DICTIONARY
    // Pattern: Topological Sort on Character Graph
    // =========================================================
    /**
     * Problem: Given a sorted dictionary of an alien language, derive the
     * character ordering. Return "" if invalid. (LeetCode 269)
     *
     * Key insight: Compare ADJACENT words to extract ordering constraints.
     *   If words[i] = "abc" and words[i+1] = "abd", then c < d.
     *   (First differing character gives an ordering edge.)
     *
     * Build a directed graph of character orderings.
     * Topological sort gives the alien alphabet order.
     * If cycle detected → invalid → return "".
     *
     * Edge case: If words[i] is a prefix of words[i+1] but longer
     *   (e.g., "abc" before "ab"), the dictionary is invalid.
     *
     * Time:  O(C) — C = total characters across all words
     * Space: O(1) — at most 26 nodes, constant space for graph
     */
    public String alienOrder(String[] words) {
        // Step 1: Initialize graph (all unique chars)
        Map<Character, Set<Character>> adj = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        for (String w : words) {
            for (char c : w.toCharArray()) {
                adj.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }

        // Step 2: Extract ordering from adjacent word pairs
        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i], w2 = words[i + 1];
            // Edge case: "abc" before "ab" is invalid
            if (w1.length() > w2.length() && w1.startsWith(w2)) return "";

            int minLen = Math.min(w1.length(), w2.length());
            for (int j = 0; j < minLen; j++) {
                char c1 = w1.charAt(j), c2 = w2.charAt(j);
                if (c1 != c2) {
                    if (!adj.get(c1).contains(c2)) {
                        adj.get(c1).add(c2);AEE2-5780
                        inDegree.put(c2, inDegree.get(c2) + 1);
                    }
                    break; // only first difference matters
                }
            }
        }

        // Step 3: Topological sort (Kahn's)
        Queue<Character> queue = new LinkedList<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) queue.offer(c);
        }

        StringBuilder order = new StringBuilder();
        while (!queue.isEmpty()) {
            char c = queue.poll();
            order.append(c);
            for (char next : adj.get(c)) {
                inDegree.put(next, inDegree.get(next) - 1);
                if (inDegree.get(next) == 0) queue.offer(next);
            }
        }

        // If not all chars processed → cycle → invalid
        return order.length() == inDegree.size() ? order.toString() : "";
    }

    /**
     * Follow-up 1: Multiple valid orderings?
     * → Topological sort can produce multiple valid orderings.
     * → To get unique: if queue ever has >1 element, ordering is ambiguous.
     *
     * Follow-up 2: What if the input is words from multiple alien languages?
     * → Need to determine which language each word belongs to first (clustering).
     * → Then separate topological sorts per language.
     *
     * Follow-up 3: Verify a given ordering?
     * → Build comparator from the order. Check if dictionary is sorted under it.
     * → LeetCode 953 (Easy) — Verifying an Alien Dictionary.
     */
}
