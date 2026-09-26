/**
 * ============================================================
 *  PATTERN 4 — ALL-PAIRS SHORTEST PATH
 *  Problem 1 (Basic): Floyd-Warshall   Classic / LC 1334
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a directed weighted graph with V vertices and an adjacency matrix,
 *    compute the shortest path between ALL pairs of vertices.
 *    Also detect negative weight cycles.
 *    Optionally reconstruct any shortest path between two vertices.
 *
 *  CONSTRAINTS:
 *    1 <= V <= 500 (Floyd-Warshall is O(V³), feasible for V ≤ 500)
 *    Edge weights may be negative, but assume no negative cycles unless detected.
 *
 *  APPROACH 1: Floyd-Warshall with in-place matrix update + path reconstruction
 *    Time:  O(V³)
 *    Space: O(V²)  — distance matrix + nextHop matrix
 *
 *  APPROACH 2: Repeated Dijkstra from each vertex (for sparse graphs)
 *    Time:  O(V × (V + E) log V)  — faster than Floyd-Warshall when E << V²
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Floyd_Warshall_Matrix {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — FLOYD-WARSHALL
    // =========================================================

    /**
     * Computes all-pairs shortest paths using Floyd-Warshall algorithm.
     * Modifies the distance matrix IN-PLACE.
     *
     * ALGORITHM:
     *   For each intermediate vertex k (0 to V-1):
     *     For each pair (i, j):
     *       If routing through k gives a shorter path:
     *         dist[i][j] = dist[i][k] + dist[k][j]
     *
     * INITIALIZATION REQUIRED:
     *   dist[i][i] = 0  (no cost to reach yourself)
     *   dist[i][j] = edge weight if edge i→j exists
     *   dist[i][j] = INFINITY if no direct edge
 *
     * NEGATIVE CYCLE DETECTION:
     *   After algorithm completes: if dist[v][v] < 0 for any v →
     *   there is a negative cycle through v.
     *
     * @param distanceMatrix input/output: [i][j] = shortest distance i→j (modified in-place)
     * @param nextHop        output: [i][j] = next vertex on shortest path from i to j
     *                       (pass null if path reconstruction not needed)
     * @return true if a negative cycle is detected
     *
     * Time:  O(V³)
     * Space: O(V²) for the matrices (provided externally)
     */
    public static boolean floydWarshall(int[][] distanceMatrix, int[][] nextHop) {
        if (distanceMatrix == null || distanceMatrix.length == 0) return false;
        int totalVertices = distanceMatrix.length;

        // Initialize nextHop matrix for path reconstruction
        if (nextHop != null) {
            for (int fromVertex = 0; fromVertex < totalVertices; fromVertex++) {
                for (int toVertex = 0; toVertex < totalVertices; toVertex++) {
                    if (fromVertex != toVertex && distanceMatrix[fromVertex][toVertex] < INFINITY) {
                        nextHop[fromVertex][toVertex] = toVertex;   // direct edge: next hop is toVertex
                    } else {
                        nextHop[fromVertex][toVertex] = -1;         // no path
                    }
                }
            }
        }

        // Core Floyd-Warshall: try each vertex as an intermediate
        for (int intermediateVertex = 0; intermediateVertex < totalVertices; intermediateVertex++) {
            for (int fromVertex = 0; fromVertex < totalVertices; fromVertex++) {
                for (int toVertex = 0; toVertex < totalVertices; toVertex++) {

                    // Guard against INFINITY overflow in addition
                    if (distanceMatrix[fromVertex][intermediateVertex] == INFINITY ||
                        distanceMatrix[intermediateVertex][toVertex] == INFINITY) continue;

                    int distanceThroughIntermediate =
                        distanceMatrix[fromVertex][intermediateVertex] +
                        distanceMatrix[intermediateVertex][toVertex];

                    if (distanceThroughIntermediate < distanceMatrix[fromVertex][toVertex]) {
                        distanceMatrix[fromVertex][toVertex] = distanceThroughIntermediate;

                        // Update path: to go from fromVertex to toVertex, first go to intermediateVertex
                        if (nextHop != null) {
                            nextHop[fromVertex][toVertex] = nextHop[fromVertex][intermediateVertex];
                        }
                    }
                }
            }
        }

        // Check for negative cycles: dist[v][v] < 0
        for (int vertex = 0; vertex < totalVertices; vertex++) {
            if (distanceMatrix[vertex][vertex] < 0) return true;
        }
        return false;
    }

    /**
     * Reconstructs the shortest path from fromVertex to toVertex using nextHop matrix.
     * Requires floydWarshall() to have been run with a non-null nextHop matrix.
     *
     * @return ordered list of vertices on the path, or empty list if no path
     */
    public static List<Integer> reconstructPath(int[][] nextHop, int fromVertex, int toVertex) {
        List<Integer> path = new ArrayList<>();
        if (nextHop == null || nextHop[fromVertex][toVertex] == -1) return path;

        path.add(fromVertex);
        int currentVertex = fromVertex;

        while (currentVertex != toVertex) {
            currentVertex = nextHop[currentVertex][toVertex];
            if (currentVertex == -1) return new ArrayList<>();  // no path
            path.add(currentVertex);
        }
        return path;
    }

    /**
     * Builds a distance matrix from an edge list (for convenience).
     *
     * @param totalVertices number of vertices
     * @param edges         each int[] = {from, to, weight}
     * @return initialized V×V distance matrix
     */
    public static int[][] buildDistanceMatrix(int totalVertices, int[][] edges) {
        int[][] matrix = new int[totalVertices][totalVertices];
        for (int[] row : matrix) Arrays.fill(row, INFINITY);
        for (int vertex = 0; vertex < totalVertices; vertex++) matrix[vertex][vertex] = 0;
        for (int[] edge : edges) {
            int from = edge[0], to = edge[1], weight = edge[2];
            matrix[from][to] = Math.min(matrix[from][to], weight);  // handle parallel edges
        }
        return matrix;
    }

    // =========================================================
    // APPROACH 2 — REPEATED DIJKSTRA (SPARSE GRAPHS)
    // =========================================================

    /**
     * Computes all-pairs shortest paths by running Dijkstra from each vertex.
     * More efficient than Floyd-Warshall for sparse graphs (E << V²).
     *
     * NOTE: Dijkstra requires non-negative weights. If negative weights exist,
     *       use Johnson's algorithm (reweight edges first) or Floyd-Warshall.
     *
     * Time:  O(V × (V + E) log V)
     * Space: O(V + E)
     */
    public static int[][] allPairsDijkstra(List<List<int[]>> adjacencyList, int totalVertices) {
        int[][] allDistances = new int[totalVertices][totalVertices];

        for (int sourceVertex = 0; sourceVertex < totalVertices; sourceVertex++) {
            allDistances[sourceVertex] = dijkstraSingleSource(adjacencyList, totalVertices, sourceVertex);
        }
        return allDistances;
    }

    private static int[] dijkstraSingleSource(
            List<List<int[]>> adjacencyList, int totalVertices, int sourceVertex) {
        int[] distance = new int[totalVertices];
        Arrays.fill(distance, INFINITY);
        distance[sourceVertex] = 0;

        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        minHeap.offer(new int[]{0, sourceVertex});

        while (!minHeap.isEmpty()) {
            int[] entry = minHeap.poll();
            int currentDist = entry[0], currentVert = entry[1];
            if (currentDist > distance[currentVert]) continue;

            for (int[] edge : adjacencyList.get(currentVert)) {
                int neighbor = edge[0], weight = edge[1];
                int candidate = distance[currentVert] + weight;
                if (candidate < distance[neighbor]) {
                    distance[neighbor] = candidate;
                    minHeap.offer(new int[]{candidate, neighbor});
                }
            }
        }
        return distance;
    }

    private static void printMatrix(int[][] matrix, String label) {
        System.out.println(label + ":");
        for (int[] row : matrix) {
            StringBuilder sb = new StringBuilder("  [");
            for (int i = 0; i < row.length; i++) {
                sb.append(row[i] == INFINITY ? " ∞" : String.format("%3d", row[i]));
                if (i < row.length - 1) sb.append(",");
            }
            sb.append(" ]");
            System.out.println(sb);
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Floyd-Warshall Matrix — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Basic 4-vertex graph ---
        System.out.println("\n--- Test 1: 4-vertex directed graph ---");
        int[][] edges1 = {{0,1,3},{0,3,7},{1,0,8},{1,2,2},{2,0,5},{2,3,1},{3,0,2}};
        int[][] dist1  = buildDistanceMatrix(4, edges1);
        int[][] next1  = new int[4][4];
        boolean hasCycle1 = floydWarshall(dist1, next1);
        printMatrix(dist1, "Distance matrix");
        System.out.println("Negative cycle: " + hasCycle1 + " (expected false)");
        System.out.println("Path 1→3: " + reconstructPath(next1, 1, 3) + " (expected [1,2,3])");
        System.out.println("Path 0→2: " + reconstructPath(next1, 0, 2));

        // --- Test 2: Negative edge (no cycle) ---
        System.out.println("\n--- Test 2: Negative edge, no cycle ---");
        int[][] edges2 = {{0,1,1},{1,2,-2},{0,2,10}};
        int[][] dist2  = buildDistanceMatrix(3, edges2);
        floydWarshall(dist2, null);
        printMatrix(dist2, "Distance matrix (neg edge)");
        // dist[0][2] should be 1+(-2) = -1, not 10

        // --- Test 3: Negative cycle detection ---
        System.out.println("\n--- Test 3: Negative cycle (0→1→2→0 weight sum < 0) ---");
        int[][] edges3 = {{0,1,2},{1,2,3},{2,0,-6}};   // cycle weight: 2+3-6 = -1
        int[][] dist3  = buildDistanceMatrix(3, edges3);
        boolean hasCycle3 = floydWarshall(dist3, null);
        System.out.println("Negative cycle detected: " + hasCycle3 + " (expected true)");

        // --- Test 4: Disconnected graph ---
        System.out.println("\n--- Test 4: Disconnected (vertex 3 isolated) ---");
        int[][] edges4 = {{0,1,5},{1,2,3}};
        int[][] dist4  = buildDistanceMatrix(4, edges4);
        floydWarshall(dist4, null);
        System.out.println("dist[0][3] = INFINITY? " + (dist4[0][3] == INFINITY));

        // --- Test 5: Repeated Dijkstra comparison ---
        System.out.println("\n--- Test 5: Repeated Dijkstra on same graph ---");
        List<List<int[]>> adj5 = new ArrayList<>();
        for (int i = 0; i < 4; i++) adj5.add(new ArrayList<>());
        for (int[] e : edges1) adj5.get(e[0]).add(new int[]{e[1], e[2]});
        int[][] dijkstraAll = allPairsDijkstra(adj5, 4);
        printMatrix(dijkstraAll, "All-pairs Dijkstra");

        System.out.println("\n==============================================");
        System.out.println("  All Floyd-Warshall tests completed.");
        System.out.println("==============================================");
    }
}
