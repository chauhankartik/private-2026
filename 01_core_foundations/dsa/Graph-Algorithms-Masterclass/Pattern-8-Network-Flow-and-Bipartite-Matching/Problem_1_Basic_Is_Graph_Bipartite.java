/**
 * ============================================================
 *  PATTERN 8 — NETWORK FLOW AND BIPARTITE MATCHING
 *  Problem 1 (Basic): Is Graph Bipartite?   LC 785
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an undirected graph represented as an adjacency list, return true if
 *    the graph is bipartite — i.e., its vertices can be split into two groups
 *    such that every edge connects vertices from different groups (2-colorable).
 *
 *  EXAMPLE:
 *    graph=[[1,3],[0,2],[1,3],[0,2]] → true  (0,2 in group A; 1,3 in group B)
 *    graph=[[1,2,3],[0,2],[0,1,3],[0,2]] → false (odd cycle: 0-1-2-0)
 *
 *  CONSTRAINTS:
 *    1 <= graph.length <= 100
 *    0 <= graph[i].length < graph.length
 *    No self-loops, no parallel edges.
 *
 *  APPROACH 1: BFS 2-coloring — level-by-level alternating colors
 *    Time:  O(V + E)
 *    Space: O(V)  — color[] + queue
 *
 *  APPROACH 2: DFS 2-coloring — recursive color assignment
 *    Time:  O(V + E)
 *    Space: O(V)  — color[] + O(V) recursion stack
 *
 *  BIPARTITE THEOREM:
 *    A graph is bipartite if and only if it contains NO ODD-LENGTH CYCLES.
 *    2-coloring fails precisely when we encounter an odd cycle.
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Is_Graph_Bipartite {

    private static final int UNCOLORED = 0;
    private static final int RED       = 1;
    private static final int BLUE      = 2;

    // =========================================================
    // APPROACH 1 — BFS 2-COLORING
    // =========================================================

    /**
     * Checks bipartiteness using BFS.
     * Alternately assigns RED and BLUE to each level of BFS.
     * A conflict (neighbor with same color as current vertex) → not bipartite.
     *
     * HANDLES DISCONNECTED GRAPHS:
     *   Outer loop ensures all components are checked.
     *
     * @param graph adjacency list (graph[u] = list of neighbors of u)
     * @return true if bipartite
     *
     * Time:  O(V + E)
     * Space: O(V)
     */
    public boolean isBipartiteBFS(int[][] graph) {
        if (graph == null || graph.length == 0) return true;
        int totalVertices = graph.length;
        int[] color = new int[totalVertices];   // UNCOLORED = 0, RED = 1, BLUE = 2

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (color[startVertex] != UNCOLORED) continue;   // already colored in previous BFS

            // Start BFS from this uncolored vertex
            Queue<Integer> bfsQueue = new LinkedList<>();
            bfsQueue.offer(startVertex);
            color[startVertex] = RED;

            while (!bfsQueue.isEmpty()) {
                int currentVertex = bfsQueue.poll();
                int currentColor  = color[currentVertex];
                int neighborColor = (currentColor == RED) ? BLUE : RED;   // alternate

                for (int neighborVertex : graph[currentVertex]) {
                    if (color[neighborVertex] == UNCOLORED) {
                        // Uncolored neighbor: assign the opposite color
                        color[neighborVertex] = neighborColor;
                        bfsQueue.offer(neighborVertex);
                    } else if (color[neighborVertex] == currentColor) {
                        // Same color as current vertex → ODD CYCLE → not bipartite
                        return false;
                    }
                    // Neighbor already has the correct alternate color: OK
                }
            }
        }
        return true;
    }

    // =========================================================
    // APPROACH 2 — DFS 2-COLORING
    // =========================================================

    /**
     * Checks bipartiteness using DFS.
     * Assigns color to each vertex during DFS and checks for conflicts.
     *
     * Time:  O(V + E)
     * Space: O(V)
     */
    public boolean isBipartiteDFS(int[][] graph) {
        if (graph == null || graph.length == 0) return true;
        int[] color = new int[graph.length];

        for (int startVertex = 0; startVertex < graph.length; startVertex++) {
            if (color[startVertex] == UNCOLORED) {
                if (!dfsColor(graph, startVertex, RED, color)) return false;
            }
        }
        return true;
    }

    /**
     * DFS coloring: assigns assignedColor to currentVertex, then alternates for neighbors.
     * @return false if a coloring conflict is detected
     */
    private boolean dfsColor(int[][] graph, int currentVertex, int assignedColor, int[] color) {
        color[currentVertex] = assignedColor;
        int neighborColor = (assignedColor == RED) ? BLUE : RED;

        for (int neighborVertex : graph[currentVertex]) {
            if (color[neighborVertex] == UNCOLORED) {
                if (!dfsColor(graph, neighborVertex, neighborColor, color)) return false;
            } else if (color[neighborVertex] == assignedColor) {
                return false;   // conflict: same color as current vertex
            }
        }
        return true;
    }

    // =========================================================
    // BONUS: GET THE TWO GROUPS IF BIPARTITE
    // =========================================================

    /**
     * Returns the two vertex groups if the graph is bipartite.
     * Returns null if not bipartite.
     *
     * @return int[][] of size 2: result[0] = RED group, result[1] = BLUE group
     */
    public int[][] getBipartiteGroups(int[][] graph) {
        if (graph == null || graph.length == 0) return new int[2][0];
        int n = graph.length;
        int[] color = new int[n];

        for (int start = 0; start < n; start++) {
            if (color[start] != UNCOLORED) continue;
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(start);
            color[start] = RED;
            while (!queue.isEmpty()) {
                int curr = queue.poll();
                int nextColor = (color[curr] == RED) ? BLUE : RED;
                for (int neighbor : graph[curr]) {
                    if (color[neighbor] == UNCOLORED) {
                        color[neighbor] = nextColor;
                        queue.offer(neighbor);
                    } else if (color[neighbor] == color[curr]) {
                        return null;   // not bipartite
                    }
                }
            }
        }

        List<Integer> redGroup = new ArrayList<>(), blueGroup = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (color[v] == RED) redGroup.add(v);
            else blueGroup.add(v);
        }
        return new int[][]{
            redGroup.stream().mapToInt(Integer::intValue).toArray(),
            blueGroup.stream().mapToInt(Integer::intValue).toArray()
        };
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Is_Graph_Bipartite solver = new Problem_1_Basic_Is_Graph_Bipartite();

        System.out.println("==============================================");
        System.out.println("  Is Graph Bipartite — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: LC 785 Example 1 (bipartite) ---
        System.out.println("\n--- Test 1: Bipartite even cycle ---");
        int[][] g1 = {{1,3},{0,2},{1,3},{0,2}};
        System.out.println("BFS: " + solver.isBipartiteBFS(g1) + " (expected true)");
        System.out.println("DFS: " + solver.isBipartiteDFS(g1) + " (expected true)");
        System.out.println("Groups: " + Arrays.deepToString(solver.getBipartiteGroups(g1)));

        // --- Test 2: LC 785 Example 2 (not bipartite — odd cycle) ---
        System.out.println("\n--- Test 2: Odd cycle (triangle inside) ---");
        int[][] g2 = {{1,2,3},{0,2},{0,1,3},{0,2}};
        System.out.println("BFS: " + solver.isBipartiteBFS(g2) + " (expected false)");
        System.out.println("DFS: " + solver.isBipartiteDFS(g2) + " (expected false)");

        // --- Test 3: Simple triangle (odd cycle) ---
        System.out.println("\n--- Test 3: Triangle (0-1-2-0) ---");
        int[][] g3 = {{1,2},{0,2},{0,1}};
        System.out.println("BFS: " + solver.isBipartiteBFS(g3) + " (expected false)");

        // --- Test 4: Disconnected bipartite ---
        System.out.println("\n--- Test 4: Disconnected (two separate edges) ---");
        int[][] g4 = {{1},{0},{3},{2}};
        System.out.println("BFS: " + solver.isBipartiteBFS(g4) + " (expected true)");
        System.out.println("Groups: " + Arrays.deepToString(solver.getBipartiteGroups(g4)));

        // --- Test 5: Single vertex ---
        System.out.println("\n--- Test 5: Single vertex ---");
        System.out.println("BFS: " + solver.isBipartiteBFS(new int[][]{{}}) + " (expected true)");

        // --- Test 6: Path graph ---
        System.out.println("\n--- Test 6: Path 0-1-2-3 ---");
        int[][] g6 = {{1},{0,2},{1,3},{2}};
        System.out.println("BFS: " + solver.isBipartiteBFS(g6) + " (expected true)");
        System.out.println("Groups: " + Arrays.deepToString(solver.getBipartiteGroups(g6)));

        System.out.println("\n==============================================");
        System.out.println("  All Bipartite tests completed.");
        System.out.println("==============================================");
    }
}
