/**
 * ============================================================
 *  GRAPHS — MEDIUM PROBLEMS
 *  These problems combine graph traversal with more advanced
 *  patterns: topological sort, Dijkstra, multi-source BFS,
 *  and state-space search.
 * ============================================================
 *
 *  Problems:
 *   M1. Course Schedule (Cycle Detection / Topological Sort)
 *   M2. Course Schedule II (Topological Ordering)
 *   M3. Rotting Oranges (Multi-Source BFS)
 *   M4. 01 Matrix (Multi-Source BFS — distance)
 *   M5. Pacific Atlantic Water Flow (Multi-Source DFS)
 *   M6. Network Delay Time (Dijkstra's Algorithm)
 *   M7. Redundant Connection (Union-Find Cycle Detection)
 *   M8. Word Ladder (BFS — implicit graph)
 *
 * ============================================================
 */
import java.util.*;

public class Medium {

    // =========================================================
    // M1. COURSE SCHEDULE
    // Pattern: Cycle Detection in Directed Graph (Topological Sort)
    // =========================================================
    /**
     * Problem: Given numCourses and prerequisites [a, b] (must take b before a),
     * determine if it's possible to finish all courses. (LeetCode 207)
     *
     * Reduced to: Does the directed graph have a CYCLE?
     *   Cycle → impossible to complete all courses (circular dependency).
     *   No cycle → valid topological ordering exists.
     *
     * Approach A — Kahn's Algorithm (BFS topological sort):
     *   1. Compute in-degree for all courses.
     *   2. Add all courses with in-degree 0 to queue.
     *   3. Process queue: for each course, reduce in-degree of dependents.
     *   4. If processed count < numCourses → cycle exists → return false.
     *
     * Why does this detect cycles?
     *   Vertices in a cycle NEVER reach in-degree 0 (each depends on another in the cycle).
     *   So they're never added to the queue, and processed count falls short.
     *
     * Time:  O(V + E) — build graph + process all vertices and edges
     * Space: O(V + E) — adjacency list + in-degree array + queue
     */
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        int[] inDegree = new int[numCourses];

        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        for (int[] p : prerequisites) {
            adj.get(p[1]).add(p[0]);  // b → a (b is prerequisite of a)
            inDegree[p[0]]++;
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int processed = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            processed++;
            for (int v : adj.get(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) queue.offer(v);
            }
        }
        return processed == numCourses;  // all courses processed → no cycle
    }

    /**
     * Follow-up 1: DFS-based cycle detection?
     *   → Track 3 states: UNVISITED, IN_PROGRESS, DONE.
     *   → Finding an IN_PROGRESS node during DFS = back edge = cycle.
     *
     * Follow-up 2: What if prerequisites are optional (soft vs hard)?
     *   → Model with edge weights or priority. Becomes scheduling optimization.
     *
     * Follow-up 3: Minimum semesters to finish all courses? (LeetCode 1136)
     *   → BFS level-count = longest path in DAG = critical path.
     */

    // =========================================================
    // M2. COURSE SCHEDULE II
    // Pattern: Topological Sort (return the ordering)
    // =========================================================
    /**
     * Problem: Return a valid ordering of courses to take.
     * Return empty array if impossible (cycle). (LeetCode 210)
     *
     * Same as M1 but collect the BFS processing order = topological order.
     *
     * Time:  O(V + E)
     * Space: O(V + E)
     */
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        int[] inDegree = new int[numCourses];

        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        for (int[] p : prerequisites) {
            adj.get(p[1]).add(p[0]);
            inDegree[p[0]]++;
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int[] order = new int[numCourses];
        int idx = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order[idx++] = u;
            for (int v : adj.get(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) queue.offer(v);
            }
        }
        return idx == numCourses ? order : new int[0];
    }

    /** DFS-based topological sort (reverse post-order) */
    public int[] findOrderDFS(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        for (int[] p : prerequisites) adj.get(p[1]).add(p[0]);

        int[] state = new int[numCourses]; // 0=unvisited, 1=in-progress, 2=done
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < numCourses; i++) {
            if (state[i] == 0 && hasCycle(adj, state, stack, i)) return new int[0];
        }

        int[] order = new int[numCourses];
        for (int i = 0; i < numCourses; i++) order[i] = stack.pop();
        return order;
    }

    private boolean hasCycle(List<List<Integer>> adj, int[] state,
                             Deque<Integer> stack, int u) {
        state[u] = 1; // in-progress
        for (int v : adj.get(u)) {
            if (state[v] == 1) return true;  // back edge = cycle
            if (state[v] == 0 && hasCycle(adj, state, stack, v)) return true;
        }
        state[u] = 2; // done
        stack.push(u); // post-order
        return false;
    }

    /**
     * Follow-up 1: All valid topological orderings?
     *   → Backtracking: at each step, try all nodes with in-degree 0.
     *   → Exponential in worst case.
     *
     * Follow-up 2: Parallel scheduling (which courses can be taken simultaneously)?
     *   → BFS levels represent parallel batches. Level i = courses that can be taken in semester i.
     *
     * Follow-up 3: Course Schedule with time constraints?
     *   → Critical path method on DAG. Longest path = minimum total time.
     */

    // =========================================================
    // M3. ROTTING ORANGES
    // Pattern: Multi-Source BFS (simultaneous spread)
    // =========================================================
    /**
     * Problem: In a grid, 2=rotten orange, 1=fresh, 0=empty.
     * Each minute, rotten oranges rot adjacent fresh ones.
     * Return minimum minutes until no fresh oranges, or -1 if impossible. (LeetCode 994)
     *
     * Key Insight: This is MULTI-SOURCE BFS.
     *   - All rotten oranges are sources — add ALL to queue at time 0.
     *   - BFS level = time elapsed.
     *   - After BFS, if any fresh orange remains → return -1.
     *
     * Why multi-source BFS?
     *   Rotting spreads simultaneously from ALL rotten oranges, not one-by-one.
     *   Adding all sources to the initial queue simulates parallel spread.
     *
     * Time:  O(m × n) — each cell processed at most once
     * Space: O(m × n) — queue in worst case
     */
    public int orangesRotting(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        Queue<int[]> queue = new LinkedList<>();
        int fresh = 0;

        // Step 1: Enqueue all rotten oranges, count fresh
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 2) queue.offer(new int[]{r, c});
                else if (grid[r][c] == 1) fresh++;
            }
        }
        if (fresh == 0) return 0;

        int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        int minutes = 0;

        // Step 2: BFS level-by-level
        while (!queue.isEmpty()) {
            int size = queue.size();
            boolean rotted = false;
            for (int i = 0; i < size; i++) {
                int[] cell = queue.poll();
                for (int[] d : dirs) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == 1) {
                        grid[nr][nc] = 2;
                        queue.offer(new int[]{nr, nc});
                        fresh--;
                        rotted = true;
                    }
                }
            }
            if (rotted) minutes++;
        }
        return fresh == 0 ? minutes : -1;
    }

    /**
     * Follow-up 1: What if some cells are walls (blocked)?
     *   → Skip walls in BFS (treat as grid[r][c] == -1 or similar).
     *
     * Follow-up 2: What if rotting takes different times for different directions?
     *   → Weighted graph → use Dijkstra instead of BFS.
     *
     * Follow-up 3: Walls and Gates (LeetCode 286)?
     *   → Same multi-source BFS from all gates. Fill INF cells with distance.
     */

    // =========================================================
    // M4. 01 MATRIX (Nearest 0 Distance)
    // Pattern: Multi-Source BFS (distance from set of sources)
    // =========================================================
    /**
     * Problem: For each cell, find distance to nearest 0. (LeetCode 542)
     *
     * Naive: BFS from each 1-cell → O((m×n)²). Too slow.
     *
     * Optimal: REVERSE the problem. BFS from all 0-cells simultaneously.
     *   - Initialize: all 0-cells have distance 0, all 1-cells have distance ∞.
     *   - Multi-source BFS from all 0-cells.
     *   - BFS guarantees shortest distance (unweighted graph).
     *
     * Time:  O(m × n) — each cell visited at most once
     * Space: O(m × n) — queue + distance matrix
     */
    public int[][] updateMatrix(int[][] mat) {
        int rows = mat.length, cols = mat[0].length;
        int[][] dist = new int[rows][cols];
        Queue<int[]> queue = new LinkedList<>();

        // Initialize: 0-cells are sources
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mat[r][c] == 0) {
                    queue.offer(new int[]{r, c});
                } else {
                    dist[r][c] = Integer.MAX_VALUE;
                }
            }
        }

        int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] d : dirs) {
                int nr = cell[0] + d[0], nc = cell[1] + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && dist[nr][nc] > dist[cell[0]][cell[1]] + 1) {
                    dist[nr][nc] = dist[cell[0]][cell[1]] + 1;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        return dist;
    }

    /**
     * Follow-up 1: DP approach (two passes: top-left → bottom-right, then reverse)?
     *   → dist[r][c] = min(dist[r-1][c], dist[r][c-1]) + 1 (first pass)
     *   → dist[r][c] = min(dist[r][c], min(dist[r+1][c], dist[r][c+1]) + 1) (second pass)
     *   → Same O(m×n) time, O(1) extra space (modify in place).
     *
     * Follow-up 2: Manhattan distance instead of grid distance?
     *   → For grid problems, Manhattan = grid distance. They're equivalent for 4-directional movement.
     */

    // =========================================================
    // M5. PACIFIC ATLANTIC WATER FLOW
    // Pattern: Multi-Source DFS from Borders
    // =========================================================
    /**
     * Problem: Grid of heights. Water flows to lower/equal cells.
     * Find cells where water can flow to BOTH Pacific (top/left border)
     * and Atlantic (bottom/right border). (LeetCode 417)
     *
     * Key insight — Reverse the flow:
     *   Instead of "from cell, can water reach ocean?", think:
     *   "From ocean border, which cells can water flow UPWARD to?"
     *
     *   DFS/BFS from Pacific borders → mark all reachable cells.
     *   DFS/BFS from Atlantic borders → mark all reachable cells.
     *   Intersection = cells reaching BOTH oceans.
     *
     * Time:  O(m × n) — each cell visited twice (once per ocean)
     * Space: O(m × n) — two boolean matrices
     */
    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        List<List<Integer>> result = new ArrayList<>();
        int rows = heights.length, cols = heights[0].length;

        boolean[][] pacific = new boolean[rows][cols];
        boolean[][] atlantic = new boolean[rows][cols];

        // DFS from Pacific borders (top row + left column)
        for (int c = 0; c < cols; c++) dfsOcean(heights, pacific, 0, c, rows, cols);
        for (int r = 0; r < rows; r++) dfsOcean(heights, pacific, r, 0, rows, cols);

        // DFS from Atlantic borders (bottom row + right column)
        for (int c = 0; c < cols; c++) dfsOcean(heights, atlantic, rows - 1, c, rows, cols);
        for (int r = 0; r < rows; r++) dfsOcean(heights, atlantic, r, cols - 1, rows, cols);

        // Intersection
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (pacific[r][c] && atlantic[r][c]) {
                    result.add(Arrays.asList(r, c));
                }
            }
        }
        return result;
    }

    private void dfsOcean(int[][] heights, boolean[][] reachable, int r, int c,
                          int rows, int cols) {
        reachable[r][c] = true;
        int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                    && !reachable[nr][nc] && heights[nr][nc] >= heights[r][c]) {
                dfsOcean(heights, reachable, nr, nc, rows, cols);
            }
        }
    }

    /**
     * Follow-up 1: BFS variant?
     *   → Add all border cells to queue. Same reverse-flow logic.
     *
     * Follow-up 2: What if water flows only strictly downhill (not equal)?
     *   → Change condition to heights[nr][nc] > heights[r][c].
     *
     * Follow-up 3: 3 oceans instead of 2?
     *   → Three reachability matrices. Intersection of all three.
     */

    // =========================================================
    // M6. NETWORK DELAY TIME (Dijkstra's Algorithm)
    // Pattern: Shortest Path — Weighted Graph
    // =========================================================
    /**
     * Problem: Given n nodes, edges [u, v, w], and source k, find the
     * time it takes for all nodes to receive signal (max shortest path).
     * Return -1 if not all reachable. (LeetCode 743)
     *
     * This is classic single-source shortest path → Dijkstra.
     *
     * Algorithm:
     *   1. Build adjacency list (weighted).
     *   2. Min-heap: (distance, node). Start with (0, k).
     *   3. Relax edges: if dist[u] + w < dist[v], update and push.
     *   4. Answer = max(dist[all nodes]). If any ∞ → return -1.
     *
     * Time:  O((V + E) log V) — each edge relaxed once, log V per heap op
     * Space: O(V + E) — adjacency list + distance array + heap
     */
    public int networkDelayTime(int[][] times, int n, int k) {
        // Build adjacency list
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());
        for (int[] t : times) adj.get(t[0]).add(new int[]{t[1], t[2]});

        // Dijkstra
        int[] dist = new int[n + 1]; // 1-indexed
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[k] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        pq.offer(new int[]{k, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0], d = curr[1];
            if (d > dist[u]) continue;  // stale entry — skip

            for (int[] edge : adj.get(u)) {
                int v = edge[0], w = edge[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }

        int maxDist = 0;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;
            maxDist = Math.max(maxDist, dist[i]);
        }
        return maxDist;
    }

    /**
     * Follow-up 1: What if edges can have negative weights?
     *   → Dijkstra fails. Use Bellman-Ford: O(V × E).
     *   → Bellman-Ford also detects negative cycles.
     *
     * Follow-up 2: What if we want the actual shortest path (not just distance)?
     *   → Track parent[] array during relaxation. Reconstruct path by backtracking.
     *
     * Follow-up 3: All-pairs shortest path?
     *   → Run Dijkstra from each vertex: O(V(V+E) log V).
     *   → Or Floyd-Warshall: O(V³). Better for dense graphs.
     */

    // =========================================================
    // M7. REDUNDANT CONNECTION
    // Pattern: Union-Find — Cycle Detection in Undirected Graph
    // =========================================================
    /**
     * Problem: Given a graph that started as a tree and had ONE extra edge
     * added (creating exactly one cycle). Find and return that edge. (LeetCode 684)
     *
     * Key Insight: Process edges in order. Use Union-Find.
     *   The FIRST edge that connects two already-connected components = the
     *   edge creating the cycle.
     *
     * Why Union-Find?
     *   Before adding edge (u, v): if find(u) == find(v), they're already
     *   connected → this edge creates a cycle → return it.
     *
     * Time:  O(E × α(V)) ≈ O(E)
     * Space: O(V)
     */
    public int[] findRedundantConnection(int[][] edges) {
        int n = edges.length;
        int[] parent = new int[n + 1], rank = new int[n + 1];
        for (int i = 1; i <= n; i++) parent[i] = i;

        for (int[] e : edges) {
            int pu = find(parent, e[0]), pv = find(parent, e[1]);
            if (pu == pv) return e;  // cycle detected!
            // Union by rank
            if (rank[pu] < rank[pv]) { int t = pu; pu = pv; pv = t; }
            parent[pv] = pu;
            if (rank[pu] == rank[pv]) rank[pu]++;
        }
        return new int[0]; // should never reach here
    }

    /**
     * Follow-up 1: Redundant connection in DIRECTED graph? (LeetCode 685)
     *   → Much harder. Need to handle: in-degree 2 case AND cycle case.
     *   → Try removing candidate edges and checking if result is a valid tree.
     *
     * Follow-up 2: Remove minimum edges to make graph acyclic?
     *   → Total edges - (vertices - 1) edges must be removed.
     *   → Use MST (keep V-1 edges that form spanning tree).
     *
     * Follow-up 3: What if there are multiple valid answers?
     *   → Problem guarantees unique answer (return LAST such edge in input order).
     */

    // =========================================================
    // M8. WORD LADDER
    // Pattern: BFS on Implicit Graph (State Space Search)
    // =========================================================
    /**
     * Problem: Transform beginWord to endWord, changing ONE letter at a time.
     * Each intermediate word must be in wordList. Return shortest transformation
     * length, or 0 if impossible. (LeetCode 127)
     *
     * Key insight: This is BFS on an IMPLICIT GRAPH.
     *   - Each word = vertex
     *   - Edge between two words if they differ by exactly 1 character
     *   - Shortest path from beginWord to endWord
     *
     * Optimization — Generic state pattern:
     *   Instead of comparing all word pairs O(n² × L), use wildcard patterns.
     *   "hot" → "*ot", "h*t", "ho*"
     *   Build map: pattern → list of matching words. O(n × L).
     *
     * Time:  O(n × L²) — n words, L = word length, L patterns per word, L for substring
     * Space: O(n × L)  — pattern map + visited set
     */
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (!wordSet.contains(endWord)) return 0;

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.offer(beginWord);
        visited.add(beginWord);
        int level = 1;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String word = queue.poll();
                char[] chars = word.toCharArray();

                for (int j = 0; j < chars.length; j++) {
                    char orig = chars[j];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == orig) continue;
                        chars[j] = c;
                        String next = new String(chars);
                        if (next.equals(endWord)) return level + 1;
                        if (wordSet.contains(next) && !visited.contains(next)) {
                            visited.add(next);
                            queue.offer(next);
                        }
                    }
                    chars[j] = orig;  // restore
                }
            }
            level++;
        }
        return 0;
    }

    /**
     * Follow-up 1: Bidirectional BFS for speed?
     *   → BFS from both beginWord and endWord. Meet in the middle.
     *   → Reduces time from O(26^(L/2)) to O(2 × 26^(L/4)).
     *
     * Follow-up 2: Return ALL shortest transformation sequences? (LeetCode 126, Hard)
     *   → BFS for distances + DFS backtracking for path reconstruction.
     *   → Very hard. Requires careful graph construction.
     *
     * Follow-up 3: What if we can change at most 2 letters per step?
     *   → Different adjacency definition. Generate all 2-edit neighbors.
     *   → Each word has O(26² × L²) neighbors — potentially expensive.
     */
}
