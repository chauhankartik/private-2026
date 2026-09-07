/**
 * ============================================================
 *  GRAPHS — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Number of Islands (BFS/DFS on Grid)
 *   E2. Flood Fill (BFS/DFS on Grid)
 *   E3. Find if Path Exists in Graph (BFS/DFS/Union-Find)
 *   E4. Clone Graph (HashMap + BFS/DFS)
 *   E5. Find the Town Judge (In-degree / Out-degree)
 *   E6. Number of Connected Components (Union-Find / DFS)
 *   E7. Is Graph Bipartite? (BFS/DFS 2-Coloring)
 *   E8. Surrounded Regions (Border BFS/DFS)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

public class Easy {

    // =========================================================
    // E1. NUMBER OF ISLANDS
    // Pattern: Grid as Graph + DFS/BFS Flood Fill
    // =========================================================
    /**
     * Problem: Given a 2D grid of '1' (land) and '0' (water), count the
     * number of islands. An island is surrounded by water and formed by
     * connecting adjacent lands horizontally or vertically.
     *
     * Key Insight: Each island is a CONNECTED COMPONENT in the grid-graph.
     *   - For each unvisited '1', start DFS/BFS to mark the entire island.
     *   - Increment count by 1 per new island found.
     *
     * Why DFS is natural here:
     *   We want to explore an entire connected region.
     *   DFS recursively visits all reachable land cells, marking them as visited.
     *   Each DFS call covers one full island → one increment.
     *
     * Time:  O(m × n) — each cell visited at most once
     * Space: O(m × n) — recursion stack in worst case (all land)
     *        Can reduce to O(min(m,n)) with BFS.
     */
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int rows = grid.length, cols = grid[0].length;
        int count = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '1') {
                    count++;
                    dfsIsland(grid, r, c, rows, cols);
                }
            }
        }
        return count;
    }

    private void dfsIsland(char[][] grid, int r, int c, int rows, int cols) {
        if (r < 0 || r >= rows || c < 0 || c >= cols || grid[r][c] != '1') return;
        grid[r][c] = '0';  // mark visited by sinking the island
        dfsIsland(grid, r + 1, c, rows, cols);
        dfsIsland(grid, r - 1, c, rows, cols);
        dfsIsland(grid, r, c + 1, rows, cols);
        dfsIsland(grid, r, c - 1, rows, cols);
    }

    /**
     * Follow-up 1: Number of islands using Union-Find?
     *   → Create UF with m×n elements. Union adjacent '1' cells.
     *   → Count = initial '1' count - successful unions.
     *   → O(m×n × α(m×n)) time, practically O(m×n).
     *
     * Follow-up 2: Number of DISTINCT islands (by shape)? (LeetCode 694)
     *   → DFS recording relative path directions as a string.
     *   → Store canonical shapes in HashSet. Count unique shapes.
     *
     * Follow-up 3: Max area of island? (LeetCode 695)
     *   → Same DFS, return size of each DFS traversal. Track max.
     */

    // =========================================================
    // E2. FLOOD FILL
    // Pattern: BFS/DFS Traversal on Grid
    // =========================================================
    /**
     * Problem: Starting from pixel (sr, sc), change all connected pixels
     * of the same original color to newColor. (Microsoft Paint bucket fill)
     *
     * Same as "island marking" but with a specific start point.
     *
     * Edge case: If originalColor == newColor, do nothing (avoid infinite loop).
     *
     * Time:  O(m × n) — visit each cell at most once
     * Space: O(m × n) — recursion stack
     */
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        int origColor = image[sr][sc];
        if (origColor == color) return image;  // crucial edge case!
        dfsFill(image, sr, sc, origColor, color);
        return image;
    }

    private void dfsFill(int[][] image, int r, int c, int origColor, int newColor) {
        if (r < 0 || r >= image.length || c < 0 || c >= image[0].length) return;
        if (image[r][c] != origColor) return;
        image[r][c] = newColor;
        dfsFill(image, r + 1, c, origColor, newColor);
        dfsFill(image, r - 1, c, origColor, newColor);
        dfsFill(image, r, c + 1, origColor, newColor);
        dfsFill(image, r, c - 1, origColor, newColor);
    }

    /**
     * Follow-up 1: BFS approach?
     *   → Queue-based. Same time/space complexity but avoids stack overflow for large grids.
     *
     * Follow-up 2: 8-directional flood fill (include diagonals)?
     *   → Add 4 more directions: {-1,-1}, {-1,1}, {1,-1}, {1,1}
     *
     * Follow-up 3: Count cells changed?
     *   → Track count in DFS/BFS. Return along with the image.
     */

    // =========================================================
    // E3. FIND IF PATH EXISTS IN GRAPH
    // Pattern: BFS/DFS + Connectivity Check
    // =========================================================
    /**
     * Problem: Given undirected graph with n vertices and edges,
     * determine if there's a valid path from source to destination.
     * (LeetCode 1971)
     *
     * Three approaches:
     *   A. BFS from source: if we reach destination → true.
     *   B. DFS from source: same.
     *   C. Union-Find: union all edges, check find(source) == find(dest).
     *
     * Union-Find is optimal for this class of problem:
     *   - No need to traverse; just check connectivity.
     *   - O(E × α(V)) construction, O(α(V)) per query.
     *
     * Time:  O(V + E) for BFS/DFS, O(E × α(V)) for Union-Find
     * Space: O(V + E)
     */
    public boolean validPath(int n, int[][] edges, int source, int destination) {
        // BFS approach
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == destination) return true;
            for (int v : adj.get(u)) {
                if (!visited[v]) {
                    visited[v] = true;
                    queue.offer(v);
                }
            }
        }
        return false;
    }

    /** Union-Find approach — better when there are MANY queries */
    public boolean validPathUF(int n, int[][] edges, int source, int destination) {
        int[] parent = new int[n], rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int[] e : edges) union(parent, rank, e[0], e[1]);
        return find(parent, source) == find(parent, destination);
    }

    private int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    private void union(int[] parent, int[] rank, int x, int y) {
        int px = find(parent, x), py = find(parent, y);
        if (px == py) return;
        if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
        parent[py] = px;
        if (rank[px] == rank[py]) rank[px]++;
    }

    /**
     * Follow-up 1: Find SHORTEST path (not just existence)?
     *   → BFS gives shortest path in unweighted graph. Track parent pointers.
     *
     * Follow-up 2: Multiple queries on same graph?
     *   → Build Union-Find once. Each query is O(α(V)) ≈ O(1).
     *   → BFS/DFS would be O(V+E) per query — much slower.
     *
     * Follow-up 3: Dynamic graph (edges added/removed)?
     *   → Union-Find handles additions. Removal is much harder
     *     (requires "link-cut trees" or offline algorithms).
     */

    // =========================================================
    // E4. CLONE GRAPH
    // Pattern: HashMap (old → new) + BFS/DFS
    // =========================================================
    /**
     * Problem: Deep copy a connected undirected graph. Each node has
     * a value and list of neighbors.
     *
     * Key insight: Use a HashMap mapping OLD node → NEW node.
     *   - If node already cloned (in map), return the clone.
     *   - Otherwise, create clone, add to map, recursively clone neighbors.
     *
     * Why HashMap?
     *   Graph may have cycles. Without tracking cloned nodes, we'd
     *   create infinite copies. The map acts as "visited" + reference store.
     *
     * Time:  O(V + E) — visit each node and edge once
     * Space: O(V)     — map storing V cloned nodes
     */
    // Node class definition (as given by LeetCode)
    static class Node {
        public int val;
        public List<Node> neighbors;
        Node(int val) {
            this.val = val;
            this.neighbors = new ArrayList<>();
        }
    }

    private Map<Node, Node> cloneMap = new HashMap<>();

    public Node cloneGraph(Node node) {
        if (node == null) return null;
        if (cloneMap.containsKey(node)) return cloneMap.get(node);

        Node clone = new Node(node.val);
        cloneMap.put(node, clone);

        for (Node neighbor : node.neighbors) {
            clone.neighbors.add(cloneGraph(neighbor));
        }
        return clone;
    }

    /** BFS variant — iterative, avoids stack overflow */
    public Node cloneGraphBFS(Node node) {
        if (node == null) return null;
        Map<Node, Node> map = new HashMap<>();
        Queue<Node> queue = new LinkedList<>();

        map.put(node, new Node(node.val));
        queue.offer(node);

        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            for (Node neighbor : curr.neighbors) {
                if (!map.containsKey(neighbor)) {
                    map.put(neighbor, new Node(neighbor.val));
                    queue.offer(neighbor);
                }
                map.get(curr).neighbors.add(map.get(neighbor));
            }
        }
        return map.get(node);
    }

    /**
     * Follow-up 1: Graph with random pointers (like linked list deep copy)?
     *   → Same HashMap pattern. Map old → new, resolve references in second pass.
     *
     * Follow-up 2: How to clone a weighted graph?
     *   → Same approach, copy edge weights along with the neighbor list.
     *
     * Follow-up 3: What if the graph is disconnected?
     *   → Clone each connected component separately.
     *   → Need access to all nodes (e.g., given as a list).
     */

    // =========================================================
    // E5. FIND THE TOWN JUDGE
    // Pattern: In-Degree / Out-Degree Counting
    // =========================================================
    /**
     * Problem: In a town of n people, the town judge:
     *   1. Trusts nobody (out-degree = 0)
     *   2. Is trusted by everybody else (in-degree = n-1)
     * Given trust pairs [a, b] (a trusts b), find the judge or return -1.
     *
     * Key Insight: Track trust balance: trust[i] = in-degree - out-degree.
     *   Judge has trust[i] == n - 1 (trusted by all, trusts nobody).
     *
     * Why a single array works:
     *   - When a trusts b: trust[a]--, trust[b]++
     *   - Judge's score: +1 from each of (n-1) others, 0 outgoing = n-1
     *   - Anyone who trusts someone has score < n-1
     *
     * Time:  O(E + V) — scan trust edges + find max
     * Space: O(V)     — trust array
     */
    public int findJudge(int n, int[][] trust) {
        int[] trustScore = new int[n + 1]; // 1-indexed

        for (int[] t : trust) {
            trustScore[t[0]]--;  // a trusts someone → disqualified
            trustScore[t[1]]++;  // b is trusted
        }

        for (int i = 1; i <= n; i++) {
            if (trustScore[i] == n - 1) return i;
        }
        return -1;
    }

    /**
     * Follow-up 1: What if there could be MULTIPLE judges?
     *   → Return all i where trustScore[i] == n-1. (Not possible by definition, but good to discuss.)
     *
     * Follow-up 2: What if trust is not transitive?
     *   → Same algorithm. Trust pairs are direct, not transitive.
     *
     * Follow-up 3: Celebrity problem (2D matrix version)?
     *   → Same concept but given as adjacency matrix.
     *   → Can solve in O(n) by elimination: if A knows B, A is not celebrity.
     */

    // =========================================================
    // E6. NUMBER OF CONNECTED COMPONENTS (Undirected)
    // Pattern: Union-Find / DFS Component Counting
    // =========================================================
    /**
     * Problem: Given n vertices and edges, count connected components.
     * (LeetCode 323)
     *
     * Approach A — DFS: For each unvisited vertex, DFS marks its component. Count DFS calls.
     * Approach B — Union-Find: Start with n components. Each successful union decreases count by 1.
     *
     * Union-Find is typically more efficient for this class of problem.
     *
     * Time:  O(V + E) for DFS; O(E × α(V)) for Union-Find
     * Space: O(V)
     */
    public int countComponents(int n, int[][] edges) {
        int[] parent = new int[n], rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        int components = n;
        for (int[] e : edges) {
            if (union(parent, rank, e[0], e[1])) {
                components--;  // successful union = one fewer component
            }
        }
        return components;
    }

    /** DFS approach */
    public int countComponentsDFS(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        boolean[] visited = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                count++;
                dfsComponent(adj, visited, i);
            }
        }
        return count;
    }

    private void dfsComponent(List<List<Integer>> adj, boolean[] visited, int u) {
        visited[u] = true;
        for (int v : adj.get(u)) {
            if (!visited[v]) dfsComponent(adj, visited, v);
        }
    }

    /**
     * Follow-up 1: Graph Connectivity Queries (online)?
     *   → Build UF. Each query: find(a) == find(b). O(α(n)) per query.
     *
     * Follow-up 2: Largest component size?
     *   → Track size array in Union-Find. Update on union.
     *
     * Follow-up 3: Components in a DIRECTED graph (strongly connected)?
     *   → Kosaraju's or Tarjan's algorithm. O(V + E). Much harder.
     */

    // =========================================================
    // E7. IS GRAPH BIPARTITE?
    // Pattern: BFS/DFS 2-Coloring
    // =========================================================
    /**
     * Problem: Can we color the graph with 2 colors such that no two
     * adjacent vertices share the same color?
     *
     * Key theorem: A graph is bipartite ⟺ it contains NO ODD-LENGTH CYCLE.
     *
     * Algorithm — BFS/DFS coloring:
     *   - Start with color 0 for source.
     *   - Assign opposite color to each neighbor.
     *   - If a neighbor already has the SAME color → not bipartite.
     *
     * Time:  O(V + E) — standard BFS/DFS
     * Space: O(V)     — color array
     */
    public boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];
        Arrays.fill(color, -1);  // -1 = uncolored

        for (int i = 0; i < n; i++) {
            if (color[i] != -1) continue;  // already colored
            // BFS from i
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(i);
            color[i] = 0;

            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int v : graph[u]) {
                    if (color[v] == -1) {
                        color[v] = 1 - color[u];  // opposite color
                        queue.offer(v);
                    } else if (color[v] == color[u]) {
                        return false;  // same color = odd cycle = not bipartite
                    }
                }
            }
        }
        return true;
    }

    /**
     * Follow-up 1: Prove the bipartite ↔ no odd cycle equivalence?
     *   → If bipartite: any cycle alternates colors → must have even length.
     *   → If no odd cycle: BFS coloring never conflicts → 2-colorable.
     *
     * Follow-up 2: Possible Bipartition (friend/enemy)? (LeetCode 886)
     *   → Same 2-coloring on an "enemy" graph. If two people dislike each other,
     *     they must be in different groups.
     *
     * Follow-up 3: What if the graph can be k-colorable (k > 2)?
     *   → k-coloring for k ≥ 3 is NP-complete. No polynomial algorithm known.
     */

    // =========================================================
    // E8. SURROUNDED REGIONS
    // Pattern: Border BFS/DFS (Reverse Thinking)
    // =========================================================
    /**
     * Problem: Capture all 'O' regions surrounded by 'X'. A region is
     * NOT surrounded if it touches the border.
     *
     * Key insight — REVERSE the problem:
     *   Instead of finding surrounded regions (hard), find UNSURROUNDED ones (easy).
     *   - Any 'O' connected to the border is NOT captured.
     *   - DFS/BFS from all border 'O's, mark them as safe (e.g., 'S').
     *   - Remaining 'O's are surrounded → flip to 'X'.
     *   - 'S' cells → flip back to 'O'.
     *
     * Why reverse thinking?
     *   Directly checking "is this region enclosed?" requires complex boundary logic.
     *   Starting from borders and marking reachable 'O's is a simple flood fill.
     *
     * Time:  O(m × n) — visit each cell at most once
     * Space: O(m × n) — recursion stack (or queue for BFS)
     */
    public void solve(char[][] board) {
        if (board.length == 0) return;
        int rows = board.length, cols = board[0].length;

        // Step 1: Mark border-connected 'O's as safe ('S')
        for (int r = 0; r < rows; r++) {
            dfsBorder(board, r, 0, rows, cols);
            dfsBorder(board, r, cols - 1, rows, cols);
        }
        for (int c = 0; c < cols; c++) {
            dfsBorder(board, 0, c, rows, cols);
            dfsBorder(board, rows - 1, c, rows, cols);
        }

        // Step 2: Flip remaining 'O' → 'X', restore 'S' → 'O'
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == 'O') board[r][c] = 'X';      // captured
                else if (board[r][c] == 'S') board[r][c] = 'O';  // restored
            }
        }
    }

    private void dfsBorder(char[][] board, int r, int c, int rows, int cols) {
        if (r < 0 || r >= rows || c < 0 || c >= cols || board[r][c] != 'O') return;
        board[r][c] = 'S';
        dfsBorder(board, r + 1, c, rows, cols);
        dfsBorder(board, r - 1, c, rows, cols);
        dfsBorder(board, r, c + 1, rows, cols);
        dfsBorder(board, r, c - 1, rows, cols);
    }

    /**
     * Follow-up 1: Union-Find approach?
     *   → Union all border 'O's with a virtual node.
     *   → Any 'O' connected to virtual node is safe. Rest are captured.
     *
     * Follow-up 2: Count number of captured regions?
     *   → After marking, count separate groups of 'X' that were originally 'O'.
     *
     * Follow-up 3: What about 3D grids?
     *   → Same approach with 6 directions (+/- x, y, z).
     */
}
