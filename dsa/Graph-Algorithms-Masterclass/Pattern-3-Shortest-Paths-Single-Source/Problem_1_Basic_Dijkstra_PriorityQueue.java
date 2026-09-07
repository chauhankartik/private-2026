/**
 * ============================================================
 *  PATTERN 3 — SHORTEST PATHS: SINGLE SOURCE
 *  Problem 1 (Basic): Dijkstra — Single Source Shortest Path   LC 743 / 1514
 * ============================================================
 *
 *  PROBLEM STATEMENT (LC 743 — Network Delay Time):
 *    Given a directed weighted graph with n nodes and an array of travel times
 *    (times[i] = [source, target, weight]), determine the minimum time for all
 *    nodes to receive a signal sent from node k. Return -1 if impossible.
 *
 *  CONSTRAINTS:
 *    1 <= n <= 100 (1-indexed nodes)
 *    1 <= times.length <= 6000
 *    1 <= weight <= 100 (non-negative weights → Dijkstra applicable)
 *
 *  APPROACH 1: Dijkstra with adjacency list + PriorityQueue (min-heap)
 *    Time:  O((V + E) log V)  — each vertex settled once, each edge relaxed once
 *    Space: O(V + E)           — adjacency list + distance[] + heap
 *
 *  APPROACH 2: Dijkstra with adjacency matrix (dense graph variant)
 *    Time:  O(V²)  — for each of V extractions, scan all V vertices for minimum
 *    Space: O(V²)  — matrix storage
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Dijkstra_PriorityQueue {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — DIJKSTRA WITH MIN-HEAP (PRIORITY QUEUE)
    // =========================================================

    /**
     * Computes shortest distances from sourceVertex to all other vertices.
     *
     * ALGORITHM:
     *   1. Initialize distance[source] = 0; all others = ∞.
     *   2. Push {0, source} into min-heap (ordered by distance).
     *   3. While heap not empty:
     *      a. Pop {currentDistance, currentVertex} (minimum distance entry).
     *      b. If currentDistance > distance[currentVertex]: STALE ENTRY → skip.
     *      c. For each neighbor: if distance[current] + weight < distance[neighbor]:
     *           update distance[neighbor], push {new distance, neighbor} to heap.
     *   4. Return distance array.
     *
     * STALE ENTRY HANDLING:
     *   We use "lazy deletion" — we don't remove old heap entries when a shorter
     *   path is found. Instead, we skip them when popped if their recorded distance
     *   is outdated (currentDistance > distance[currentVertex]).
     *
     * @param adjacencyList  adjacencyList.get(u) = list of int[]{v, weight}
     * @param totalVertices  total number of vertices (0-indexed)
     * @param sourceVertex   starting vertex
     * @return distance[] where distance[v] = shortest distance from source to v
     *         (distance[v] = INFINITY if unreachable)
     *
     * Time:  O((V + E) log V)
     * Space: O(V + E)
     */
    public static int[] dijkstra(
            List<List<int[]>> adjacencyList,
            int totalVertices,
            int sourceVertex) {

        if (adjacencyList == null || totalVertices == 0) return new int[0];

        int[] distance = new int[totalVertices];
        Arrays.fill(distance, INFINITY);
        distance[sourceVertex] = 0;

        // Min-heap: int[] = {currentDistance, currentVertex}
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(entry -> entry[0])
        );
        minHeap.offer(new int[]{0, sourceVertex});

        while (!minHeap.isEmpty()) {
            int[] entry             = minHeap.poll();
            int currentDistance     = entry[0];
            int currentVertex       = entry[1];

            // Stale entry: a shorter path to currentVertex was already found and settled
            if (currentDistance > distance[currentVertex]) continue;

            for (int[] neighborEdge : adjacencyList.get(currentVertex)) {
                int neighborVertex = neighborEdge[0];
                int edgeWeight     = neighborEdge[1];

                int candidateDistance = distance[currentVertex] + edgeWeight;

                if (candidateDistance < distance[neighborVertex]) {
                    distance[neighborVertex] = candidateDistance;
                    minHeap.offer(new int[]{candidateDistance, neighborVertex});
                }
            }
        }
        return distance;
    }

    /**
     * LC 743: Network Delay Time.
     * Nodes are 1-indexed. Returns max(distance) if all reachable, else -1.
     */
    public int networkDelayTime(int[][] times, int n, int sourceNode) {
        // Build adjacency list (0-indexed internally)
        List<List<int[]>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) adjacencyList.add(new ArrayList<>());

        for (int[] time : times) {
            int fromNode  = time[0] - 1;   // convert to 0-indexed
            int toNode    = time[1] - 1;
            int travelTime = time[2];
            adjacencyList.get(fromNode).add(new int[]{toNode, travelTime});
        }

        int[] distance = dijkstra(adjacencyList, n, sourceNode - 1);

        int maxDelay = 0;
        for (int dist : distance) {
            if (dist == INFINITY) return -1;   // some node unreachable
            maxDelay = Math.max(maxDelay, dist);
        }
        return maxDelay;
    }

    /**
     * Reconstructs the shortest path from sourceVertex to targetVertex.
     * Requires a separate parent[] array tracked during Dijkstra.
     */
    public static List<Integer> dijkstraWithPath(
            List<List<int[]>> adjacencyList,
            int totalVertices,
            int sourceVertex,
            int targetVertex) {

        int[] distance = new int[totalVertices];
        int[] parent   = new int[totalVertices];
        Arrays.fill(distance, INFINITY);
        Arrays.fill(parent, -1);
        distance[sourceVertex] = 0;

        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        minHeap.offer(new int[]{0, sourceVertex});

        while (!minHeap.isEmpty()) {
            int[] entry = minHeap.poll();
            int currDist = entry[0], currVert = entry[1];
            if (currDist > distance[currVert]) continue;

            for (int[] edge : adjacencyList.get(currVert)) {
                int neighbor = edge[0], weight = edge[1];
                int candidate = distance[currVert] + weight;
                if (candidate < distance[neighbor]) {
                    distance[neighbor] = candidate;
                    parent[neighbor] = currVert;
                    minHeap.offer(new int[]{candidate, neighbor});
                }
            }
        }

        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        if (distance[targetVertex] == INFINITY) return path;  // unreachable
        for (int v = targetVertex; v != -1; v = parent[v]) path.add(v);
        Collections.reverse(path);
        return path;
    }

    // =========================================================
    // APPROACH 2 — DIJKSTRA WITH ADJACENCY MATRIX (DENSE GRAPHS)
    // =========================================================

    /**
     * O(V²) Dijkstra using an adjacency matrix.
     * Simpler to implement; optimal for dense graphs (E ≈ V²).
     *
     * @param costMatrix costMatrix[u][v] = edge weight, or INFINITY if no edge
     * @param sourceVertex starting vertex
     * @return distance[] array
     *
     * Time:  O(V²)
     * Space: O(V)  — distance[], settled[] (matrix already provided)
     */
    public static int[] dijkstraMatrix(int[][] costMatrix, int sourceVertex) {
        if (costMatrix == null || costMatrix.length == 0) return new int[0];
        int totalVertices = costMatrix.length;

        int[] distance = new int[totalVertices];
        boolean[] settled = new boolean[totalVertices];
        Arrays.fill(distance, INFINITY);
        distance[sourceVertex] = 0;

        for (int iteration = 0; iteration < totalVertices; iteration++) {
            // Find the unsettled vertex with minimum distance
            int currentVertex = -1;
            for (int vertex = 0; vertex < totalVertices; vertex++) {
                if (!settled[vertex] && (currentVertex == -1 || distance[vertex] < distance[currentVertex])) {
                    currentVertex = vertex;
                }
            }

            if (currentVertex == -1 || distance[currentVertex] == INFINITY) break;  // all remaining unreachable
            settled[currentVertex] = true;

            // Relax all edges from currentVertex
            for (int neighborVertex = 0; neighborVertex < totalVertices; neighborVertex++) {
                if (!settled[neighborVertex] && costMatrix[currentVertex][neighborVertex] < INFINITY) {
                    int candidateDistance = distance[currentVertex] + costMatrix[currentVertex][neighborVertex];
                    if (candidateDistance < distance[neighborVertex]) {
                        distance[neighborVertex] = candidateDistance;
                    }
                }
            }
        }
        return distance;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Dijkstra PriorityQueue — Test Suite");
        System.out.println("==============================================");

        Problem_1_Basic_Dijkstra_PriorityQueue solver = new Problem_1_Basic_Dijkstra_PriorityQueue();

        // --- Test 1: LC 743 Network Delay Time ---
        System.out.println("\n--- Test 1: LC 743 Network Delay Time ---");
        // Graph: 2→1 weight 1, 2→3 weight 1, 3→4 weight 1
        int[][] times1 = {{2,1,1},{2,3,1},{3,4,1}};
        System.out.println("Delay (n=4, k=2): " + solver.networkDelayTime(times1, 4, 2) + " (expected 2)");

        int[][] times2 = {{1,2,1}};
        System.out.println("Delay (n=2, k=1): " + solver.networkDelayTime(times2, 2, 1) + " (expected 1)");

        int[][] times3 = {{1,2,1}};
        System.out.println("Delay (n=2, k=2): " + solver.networkDelayTime(times3, 2, 2) + " (expected -1)");

        // --- Test 2: Shortest path with reconstruction ---
        System.out.println("\n--- Test 2: Path reconstruction ---");
        // Graph: 0→1(4), 0→2(1), 2→1(2), 1→3(1)
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < 4; i++) adj.add(new ArrayList<>());
        adj.get(0).add(new int[]{1, 4});
        adj.get(0).add(new int[]{2, 1});
        adj.get(2).add(new int[]{1, 2});
        adj.get(1).add(new int[]{3, 1});

        int[] dist = dijkstra(adj, 4, 0);
        System.out.println("Distances from 0: " + Arrays.toString(dist));
        System.out.println("Path 0→3: " + dijkstraWithPath(adj, 4, 0, 3) + " (expected [0,2,1,3])");

        // --- Test 3: Adjacency matrix Dijkstra ---
        System.out.println("\n--- Test 3: Matrix Dijkstra ---");
        int inf = INFINITY;
        int[][] matrix = {
            {0,   4,   1,   inf},
            {inf, 0,   inf, 1  },
            {inf, 2,   0,   inf},
            {inf, inf, inf, 0  }
        };
        System.out.println("Matrix dist from 0: " + Arrays.toString(dijkstraMatrix(matrix, 0)));
        // Expected: [0, 3, 1, 4]

        // --- Test 4: Disconnected graph ---
        System.out.println("\n--- Test 4: Disconnected (vertex 3 unreachable) ---");
        List<List<int[]>> adj4 = new ArrayList<>();
        for (int i = 0; i < 4; i++) adj4.add(new ArrayList<>());
        adj4.get(0).add(new int[]{1, 5});
        adj4.get(1).add(new int[]{2, 3});
        int[] dist4 = dijkstra(adj4, 4, 0);
        System.out.println("Dist from 0: " + Arrays.toString(dist4));
        System.out.println("Vertex 3 reachable: " + (dist4[3] < INFINITY) + " (expected false)");

        System.out.println("\n==============================================");
        System.out.println("  All Dijkstra tests completed.");
        System.out.println("==============================================");
    }
}
