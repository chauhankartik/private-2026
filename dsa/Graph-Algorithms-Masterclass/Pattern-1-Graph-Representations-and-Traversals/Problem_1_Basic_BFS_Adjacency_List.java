/**
 * ============================================================
 *  PATTERN 1 — GRAPH REPRESENTATIONS AND TRAVERSALS
 *  Problem 1 (Basic): BFS on an Adjacency List   LC 1971 / 847
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a directed/undirected graph with V vertices (0-indexed) and
 *    a list of edges, determine whether a path exists between a source
 *    and a target vertex using BFS. Also demonstrate:
 *      - Level-by-level BFS with distance tracking.
 *      - Multi-source BFS for finding distances from multiple starting points.
 *      - Path reconstruction using a parent[] array.
 *
 *  CONSTRAINTS:
 *    0 <= V <= 10^5
 *    0 <= E <= 2 * 10^5
 *    0 <= source, target < V
 *
 *  APPROACH 1: Standard BFS with adjacency list
 *    Time:  O(V + E)
 *    Space: O(V + E)  — adjacency list + visited[] + queue
 *
 *  APPROACH 2: BFS on adjacency matrix (for dense graphs or small V)
 *    Time:  O(V²)     — iterating all columns to find neighbors
 *    Space: O(V²)     — matrix storage
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_BFS_Adjacency_List {

    // =========================================================
    // GRAPH BUILDER UTILITY
    // =========================================================
    /**
     * Builds an undirected adjacency list from an edge array.
     *
     * @param totalVertices number of vertices (0 to totalVertices-1)
     * @param edges         each int[] = {vertexA, vertexB}
     * @return adjacency list: List[i] = neighbors of vertex i
     */
    public static List<List<Integer>> buildAdjacencyList(int totalVertices, int[][] edges) {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int vertex = 0; vertex < totalVertices; vertex++) {
            adjacencyList.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            int vertexA = edge[0], vertexB = edge[1];
            adjacencyList.get(vertexA).add(vertexB);
            adjacencyList.get(vertexB).add(vertexA);   // undirected: add both directions
        }
        return adjacencyList;
    }

    // =========================================================
    // APPROACH 1 — BFS WITH ADJACENCY LIST
    // =========================================================

    /**
     * BFS: checks if a path exists from sourceVertex to targetVertex.
     * Simultaneously records the shortest hop-distance from source to all vertices.
     *
     * @param adjacencyList graph as adjacency list
     * @param totalVertices number of vertices
     * @param sourceVertex  BFS starting vertex
     * @param targetVertex  vertex to find
     * @param distance      output array: distance[v] = hops from source to v (-1 if unreachable)
     * @param parent        output array: parent[v] = predecessor on BFS shortest path (-1 = none)
     * @return true if targetVertex is reachable from sourceVertex
     *
     * Time:  O(V + E)
     * Space: O(V)   — visited[] + queue + distance[] + parent[]
     */
    public static boolean bfsPathExists(
            List<List<Integer>> adjacencyList,
            int totalVertices,
            int sourceVertex,
            int targetVertex,
            int[] distance,
            int[] parent) {

        if (adjacencyList == null || totalVertices == 0) return false;
        if (sourceVertex < 0 || sourceVertex >= totalVertices) return false;
        if (targetVertex < 0 || targetVertex >= totalVertices) return false;
        if (sourceVertex == targetVertex) return true;

        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);

        boolean[] visited = new boolean[totalVertices];
        Queue<Integer> bfsQueue = new LinkedList<>();

        visited[sourceVertex] = true;
        distance[sourceVertex] = 0;
        bfsQueue.offer(sourceVertex);

        while (!bfsQueue.isEmpty()) {
            int currentVertex = bfsQueue.poll();

            for (int neighborVertex : adjacencyList.get(currentVertex)) {
                if (!visited[neighborVertex]) {
                    visited[neighborVertex] = true;
                    distance[neighborVertex] = distance[currentVertex] + 1;
                    parent[neighborVertex] = currentVertex;
                    bfsQueue.offer(neighborVertex);

                    if (neighborVertex == targetVertex) return true;  // early exit
                }
            }
        }
        return false;
    }

    /**
     * Reconstructs the shortest path from sourceVertex to targetVertex
     * using the parent[] array filled by bfsPathExists().
     *
     * @return ordered list of vertices on the path, or empty if no path
     */
    public static List<Integer> reconstructPath(int[] parent, int sourceVertex, int targetVertex) {
        List<Integer> path = new ArrayList<>();
        if (parent[targetVertex] == -1 && sourceVertex != targetVertex) return path;

        for (int currentVertex = targetVertex; currentVertex != -1; currentVertex = parent[currentVertex]) {
            path.add(currentVertex);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Counts connected components in an undirected graph using BFS.
     * Iterates ALL vertices to handle disconnected graphs.
     *
     * @return number of connected components
     *
     * Time:  O(V + E)
     * Space: O(V)
     */
    public static int countConnectedComponents(List<List<Integer>> adjacencyList, int totalVertices) {
        if (totalVertices == 0) return 0;

        boolean[] visited = new boolean[totalVertices];
        int componentCount = 0;

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (!visited[startVertex]) {
                componentCount++;
                // BFS from this unvisited vertex — explores its entire component
                Queue<Integer> bfsQueue = new LinkedList<>();
                bfsQueue.offer(startVertex);
                visited[startVertex] = true;

                while (!bfsQueue.isEmpty()) {
                    int currentVertex = bfsQueue.poll();
                    for (int neighborVertex : adjacencyList.get(currentVertex)) {
                        if (!visited[neighborVertex]) {
                            visited[neighborVertex] = true;
                            bfsQueue.offer(neighborVertex);
                        }
                    }
                }
            }
        }
        return componentCount;
    }

    // =========================================================
    // APPROACH 2 — BFS ON ADJACENCY MATRIX
    // =========================================================

    /**
     * BFS using an adjacency matrix representation.
     * Suitable when V is small (≤ 1000) and the graph is dense.
     *
     * @param adjacencyMatrix adjacencyMatrix[u][v] = 1 if edge exists, 0 otherwise
     * @param sourceVertex    starting vertex
     * @param targetVertex    target vertex to reach
     * @return shortest hop-distance from source to target, or -1 if unreachable
     *
     * Time:  O(V²)  — for each dequeued vertex, scan all V columns for neighbors
     * Space: O(V)   — visited array + queue
     */
    public static int bfsMatrixDistance(int[][] adjacencyMatrix, int sourceVertex, int targetVertex) {
        if (adjacencyMatrix == null || adjacencyMatrix.length == 0) return -1;
        int totalVertices = adjacencyMatrix.length;

        if (sourceVertex == targetVertex) return 0;

        boolean[] visited = new boolean[totalVertices];
        Queue<Integer> bfsQueue = new LinkedList<>();
        int[] distance = new int[totalVertices];
        Arrays.fill(distance, -1);

        visited[sourceVertex] = true;
        distance[sourceVertex] = 0;
        bfsQueue.offer(sourceVertex);

        while (!bfsQueue.isEmpty()) {
            int currentVertex = bfsQueue.poll();

            for (int neighborVertex = 0; neighborVertex < totalVertices; neighborVertex++) {
                // Check adjacency matrix for an edge
                if (adjacencyMatrix[currentVertex][neighborVertex] == 1 && !visited[neighborVertex]) {
                    visited[neighborVertex] = true;
                    distance[neighborVertex] = distance[currentVertex] + 1;
                    bfsQueue.offer(neighborVertex);
                    if (neighborVertex == targetVertex) return distance[neighborVertex];
                }
            }
        }
        return -1;
    }

    /**
     * Converts an adjacency list to an adjacency matrix.
     * Useful for testing Approach 2 with Approach 1's input.
     */
    public static int[][] toAdjacencyMatrix(List<List<Integer>> adjacencyList, int totalVertices) {
        int[][] matrix = new int[totalVertices][totalVertices];
        for (int vertex = 0; vertex < totalVertices; vertex++) {
            for (int neighbor : adjacencyList.get(vertex)) {
                matrix[vertex][neighbor] = 1;
            }
        }
        return matrix;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  BFS on Adjacency List — Test Suite");
        System.out.println("========================================");

        // --- Test 1: Simple connected graph ---
        System.out.println("\n--- Test 1: Simple connected graph (0-1-2-3) ---");
        int[][] edges1 = {{0,1},{1,2},{2,3}};
        List<List<Integer>> graph1 = buildAdjacencyList(4, edges1);
        int[] dist1 = new int[4], parent1 = new int[4];
        boolean found1 = bfsPathExists(graph1, 4, 0, 3, dist1, parent1);
        System.out.println("Path 0→3 exists: " + found1);
        System.out.println("Shortest distance: " + dist1[3]);
        System.out.println("Path: " + reconstructPath(parent1, 0, 3));
        System.out.println("Components: " + countConnectedComponents(graph1, 4));

        // --- Test 2: Disconnected graph ---
        System.out.println("\n--- Test 2: Disconnected graph (0-1 | 2-3 | 4) ---");
        int[][] edges2 = {{0,1},{2,3}};
        List<List<Integer>> graph2 = buildAdjacencyList(5, edges2);
        int[] dist2 = new int[5], parent2 = new int[5];
        boolean found2 = bfsPathExists(graph2, 5, 0, 4, dist2, parent2);
        System.out.println("Path 0→4 exists: " + found2 + " (expected false)");
        System.out.println("Path 0→1 exists: " + bfsPathExists(graph2, 5, 0, 1, dist2, parent2));
        System.out.println("Components: " + countConnectedComponents(graph2, 5) + " (expected 3)");

        // --- Test 3: Dense graph — adjacency matrix BFS ---
        System.out.println("\n--- Test 3: BFS on adjacency matrix ---");
        //   0 - 1 - 2
        //       |
        //       3 - 4
        int[][] edges3 = {{0,1},{1,2},{1,3},{3,4}};
        List<List<Integer>> graph3 = buildAdjacencyList(5, edges3);
        int[][] matrix3 = toAdjacencyMatrix(graph3, 5);
        System.out.println("Matrix dist 0→4: " + bfsMatrixDistance(matrix3, 0, 4) + " (expected 3)");
        System.out.println("Matrix dist 0→2: " + bfsMatrixDistance(matrix3, 0, 2) + " (expected 2)");
        System.out.println("Matrix dist 2→4: " + bfsMatrixDistance(matrix3, 2, 4) + " (expected 3)");

        // --- Test 4: Single vertex ---
        System.out.println("\n--- Test 4: Single vertex ---");
        List<List<Integer>> graph4 = buildAdjacencyList(1, new int[0][]);
        int[] dist4 = new int[1], parent4 = new int[1];
        System.out.println("Path 0→0 (single vertex): " + bfsPathExists(graph4, 1, 0, 0, dist4, parent4));

        // --- Test 5: Star graph ---
        System.out.println("\n--- Test 5: Star graph (center=0, leaves=1,2,3,4) ---");
        int[][] edges5 = {{0,1},{0,2},{0,3},{0,4}};
        List<List<Integer>> graph5 = buildAdjacencyList(5, edges5);
        int[] dist5 = new int[5], parent5 = new int[5];
        bfsPathExists(graph5, 5, 1, 4, dist5, parent5);
        System.out.println("Path 1→4 (via center): " + reconstructPath(parent5, 1, 4));
        System.out.println("Distance 1→4: " + dist5[4] + " (expected 2)");

        // --- Test 6: Cycle graph ---
        System.out.println("\n--- Test 6: Cycle graph (0-1-2-3-0) ---");
        int[][] edges6 = {{0,1},{1,2},{2,3},{3,0}};
        List<List<Integer>> graph6 = buildAdjacencyList(4, edges6);
        int[] dist6 = new int[4], parent6 = new int[4];
        bfsPathExists(graph6, 4, 0, 2, dist6, parent6);
        System.out.println("Distance 0→2: " + dist6[2] + " (expected 2, not 4 via long way)");
        System.out.println("Components: " + countConnectedComponents(graph6, 4) + " (expected 1)");

        System.out.println("\n========================================");
        System.out.println("  All BFS tests completed.");
        System.out.println("========================================");
    }
}
