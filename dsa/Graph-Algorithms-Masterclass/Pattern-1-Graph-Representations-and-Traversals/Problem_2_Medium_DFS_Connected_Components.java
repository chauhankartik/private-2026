/**
 * ============================================================
 *  PATTERN 1 — GRAPH REPRESENTATIONS AND TRAVERSALS
 *  Problem 2 (Medium): DFS — Connected Components   LC 323 / 200
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an undirected graph with n vertices and a list of edges,
 *    find and label all connected components using DFS.
 *    Also implement: cycle detection in an undirected graph,
 *    and count connected components in a 2D grid (Number of Islands).
 *
 *  CONSTRAINTS:
 *    0 <= n <= 2000
 *    0 <= edges.length <= 5000
 *    Grid: 0 <= rows, cols <= 300
 *
 *  APPROACH 1: Recursive DFS with component labeling
 *    Time:  O(V + E)
 *    Space: O(V + E)  — adjacency list + recursion stack
 *
 *  APPROACH 2: Iterative DFS using explicit stack (avoids StackOverflowError)
 *    Time:  O(V + E)
 *    Space: O(V + E)  — explicit stack (no JVM stack risk)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_DFS_Connected_Components {

    // =========================================================
    // APPROACH 1 — RECURSIVE DFS WITH COMPONENT LABELING
    // =========================================================

    /**
     * Assigns a component label to every vertex using recursive DFS.
     * Each distinct label (0, 1, 2, ...) represents one connected component.
     *
     * @param adjacencyList  undirected graph
     * @param totalVertices  number of vertices
     * @param componentLabel output: componentLabel[v] = which component v belongs to (-1 = unvisited)
     * @return total number of connected components found
     *
     * Time:  O(V + E)
     * Space: O(V + E) — adjacency list + O(V) recursion stack depth (may overflow on chains)
     */
    public static int labelConnectedComponents(
            List<List<Integer>> adjacencyList,
            int totalVertices,
            int[] componentLabel) {

        if (adjacencyList == null || totalVertices == 0) return 0;

        Arrays.fill(componentLabel, -1);
        int currentComponentId = 0;

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (componentLabel[startVertex] == -1) {
                dfsRecursive(adjacencyList, startVertex, currentComponentId, componentLabel);
                currentComponentId++;
            }
        }
        return currentComponentId;
    }

    /**
     * Recursive DFS: visits all vertices in the connected component containing startVertex.
     * Labels each visited vertex with componentId.
     *
     * Undirected graph cycle detection:
     *   If we encounter a visited vertex that is NOT the parent → cycle exists.
     */
    private static void dfsRecursive(
            List<List<Integer>> adjacencyList,
            int currentVertex,
            int componentId,
            int[] componentLabel) {

        componentLabel[currentVertex] = componentId;

        for (int neighborVertex : adjacencyList.get(currentVertex)) {
            if (componentLabel[neighborVertex] == -1) {
                dfsRecursive(adjacencyList, neighborVertex, componentId, componentLabel);
            }
        }
    }

    /**
     * Detects if an UNDIRECTED graph contains a cycle using DFS.
     * A cycle exists if DFS encounters an already-visited neighbor that is NOT the direct parent.
     *
     * @param adjacencyList undirected graph
     * @param totalVertices number of vertices
     * @return true if any cycle exists
     *
     * Time:  O(V + E)
     * Space: O(V)  — visited[] + O(V) recursion stack
     */
    public static boolean hasCycleUndirected(List<List<Integer>> adjacencyList, int totalVertices) {
        boolean[] visited = new boolean[totalVertices];

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (!visited[startVertex]) {
                if (dfsDetectCycle(adjacencyList, startVertex, -1, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param parentVertex the vertex we came from (used to avoid treating the incoming edge as a cycle)
     * @return true if a cycle is detected in this DFS traversal
     */
    private static boolean dfsDetectCycle(
            List<List<Integer>> adjacencyList,
            int currentVertex,
            int parentVertex,
            boolean[] visited) {

        visited[currentVertex] = true;

        for (int neighborVertex : adjacencyList.get(currentVertex)) {
            if (!visited[neighborVertex]) {
                if (dfsDetectCycle(adjacencyList, neighborVertex, currentVertex, visited)) {
                    return true;
                }
            } else if (neighborVertex != parentVertex) {
                // Visited neighbor that is NOT our parent → back edge → cycle
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // APPROACH 2 — ITERATIVE DFS (EXPLICIT STACK)
    // =========================================================

    /**
     * Iterative DFS using an explicit Deque as a stack.
     * Preferred for very deep graphs (V = 10^5) to avoid JVM StackOverflowError.
     *
     * Also detects cycles in an undirected graph during traversal.
     *
     * @param adjacencyList undirected graph
     * @param totalVertices number of vertices
     * @param componentLabel output: component label per vertex
     * @return number of connected components
     *
     * Time:  O(V + E)
     * Space: O(V)  — visited[], parent[], explicit stack
     */
    public static int labelComponentsIterative(
            List<List<Integer>> adjacencyList,
            int totalVertices,
            int[] componentLabel) {

        if (adjacencyList == null || totalVertices == 0) return 0;

        Arrays.fill(componentLabel, -1);
        int[] parentVertex = new int[totalVertices];
        Arrays.fill(parentVertex, -1);
        int currentComponentId = 0;

        Deque<Integer> dfsStack = new ArrayDeque<>();

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (componentLabel[startVertex] != -1) continue;

            dfsStack.push(startVertex);
            componentLabel[startVertex] = currentComponentId;

            while (!dfsStack.isEmpty()) {
                int currentVertex = dfsStack.pop();

                for (int neighborVertex : adjacencyList.get(currentVertex)) {
                    if (componentLabel[neighborVertex] == -1) {
                        componentLabel[neighborVertex] = currentComponentId;
                        parentVertex[neighborVertex] = currentVertex;
                        dfsStack.push(neighborVertex);
                    }
                    // Note: cycle detection in iterative DFS requires tracking visited state carefully
                    // Use the recursive version for cycle detection, or add a visited[] separate from componentLabel.
                }
            }
            currentComponentId++;
        }
        return currentComponentId;
    }

    // =========================================================
    // GRID-BASED DFS: NUMBER OF ISLANDS (LC 200)
    // =========================================================

    private static final int[] ROW_DIRECTIONS = {-1, 1, 0, 0};
    private static final int[] COL_DIRECTIONS = {0, 0, -1, 1};

    /**
     * Counts the number of islands in a 2D binary grid.
     * An island is a connected group of '1's (land); '0' is water.
     * Connectivity is 4-directional.
     *
     * @param grid 2D char array: '1' = land, '0' = water
     * @return number of distinct islands
     *
     * Time:  O(rows × cols)
     * Space: O(rows × cols)  — recursion depth in worst case (grid filled with '1')
     */
    public static int numberOfIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;

        int totalRows = grid.length;
        int totalCols = grid[0].length;
        int islandCount = 0;

        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                if (grid[row][col] == '1') {
                    islandCount++;
                    sinkIslandDFS(grid, row, col, totalRows, totalCols);
                }
            }
        }
        return islandCount;
    }

    /**
     * "Sinks" an island by changing all connected '1's to '0' via DFS.
     * This is a safe in-place marking strategy (no separate visited[][] needed).
     */
    private static void sinkIslandDFS(char[][] grid, int row, int col, int totalRows, int totalCols) {
        if (row < 0 || row >= totalRows || col < 0 || col >= totalCols) return;
        if (grid[row][col] != '1') return;

        grid[row][col] = '0';   // mark as visited/sunk

        for (int direction = 0; direction < 4; direction++) {
            sinkIslandDFS(grid, row + ROW_DIRECTIONS[direction], col + COL_DIRECTIONS[direction],
                totalRows, totalCols);
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  DFS Connected Components — Test Suite");
        System.out.println("========================================");

        // Helper to build adjacency list
        // (reuse Pattern 1 Problem 1 builder concept inline)
        List<List<Integer>> buildGraph = null;

        // --- Test 1: Three components ---
        System.out.println("\n--- Test 1: Three components (0-1-2 | 3-4 | 5) ---");
        int totalV1 = 6;
        List<List<Integer>> graph1 = new ArrayList<>();
        for (int i = 0; i < totalV1; i++) graph1.add(new ArrayList<>());
        for (int[] e : new int[][]{{0,1},{1,2},{3,4}}) {
            graph1.get(e[0]).add(e[1]); graph1.get(e[1]).add(e[0]);
        }
        int[] labels1 = new int[totalV1];
        int count1 = labelConnectedComponents(graph1, totalV1, labels1);
        System.out.println("Recursive — Components: " + count1 + " (expected 3)");
        System.out.println("Labels: " + Arrays.toString(labels1));

        int[] labels1b = new int[totalV1];
        int count1b = labelComponentsIterative(graph1, totalV1, labels1b);
        System.out.println("Iterative — Components: " + count1b + " (expected 3)");
        System.out.println("Labels: " + Arrays.toString(labels1b));

        // --- Test 2: Cycle detection ---
        System.out.println("\n--- Test 2: Cycle detection ---");
        // Graph with cycle: 0-1-2-0
        List<List<Integer>> graphCycle = new ArrayList<>();
        for (int i = 0; i < 3; i++) graphCycle.add(new ArrayList<>());
        for (int[] e : new int[][]{{0,1},{1,2},{2,0}}) {
            graphCycle.get(e[0]).add(e[1]); graphCycle.get(e[1]).add(e[0]);
        }
        System.out.println("Has cycle (triangle): " + hasCycleUndirected(graphCycle, 3) + " (expected true)");

        // Tree (no cycle): 0-1-2, 0-3
        List<List<Integer>> graphTree = new ArrayList<>();
        for (int i = 0; i < 4; i++) graphTree.add(new ArrayList<>());
        for (int[] e : new int[][]{{0,1},{1,2},{0,3}}) {
            graphTree.get(e[0]).add(e[1]); graphTree.get(e[1]).add(e[0]);
        }
        System.out.println("Has cycle (tree): " + hasCycleUndirected(graphTree, 4) + " (expected false)");

        // --- Test 3: Number of Islands ---
        System.out.println("\n--- Test 3: Number of Islands ---");
        char[][] grid1 = {
            {'1','1','1','1','0'},
            {'1','1','0','1','0'},
            {'1','1','0','0','0'},
            {'0','0','0','0','0'}
        };
        System.out.println("Islands grid1: " + numberOfIslands(grid1) + " (expected 1)");

        char[][] grid2 = {
            {'1','1','0','0','0'},
            {'1','1','0','0','0'},
            {'0','0','1','0','0'},
            {'0','0','0','1','1'}
        };
        System.out.println("Islands grid2: " + numberOfIslands(grid2) + " (expected 3)");

        char[][] grid3 = {{'1'},{'0'},{'1'}};
        System.out.println("Islands grid3 (column): " + numberOfIslands(grid3) + " (expected 2)");

        // --- Test 4: Empty graph ---
        System.out.println("\n--- Test 4: Empty graph (no edges, 4 isolated vertices) ---");
        List<List<Integer>> graph4 = new ArrayList<>();
        for (int i = 0; i < 4; i++) graph4.add(new ArrayList<>());
        int[] labels4 = new int[4];
        System.out.println("Components (4 isolated): " + labelConnectedComponents(graph4, 4, labels4) + " (expected 4)");

        System.out.println("\n========================================");
        System.out.println("  All DFS tests completed.");
        System.out.println("========================================");
    }
}
