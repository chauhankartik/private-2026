/**
 * ============================================================
 *  PATTERN 3 — SHORTEST PATHS: SINGLE SOURCE
 *  Problem 2 (Medium): Bellman-Ford — Negative Weights   LC 787 / Classic
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a directed weighted graph (V vertices, E edges), with potentially
 *    negative edge weights, find the shortest path from sourceVertex to all
 *    other vertices. Detect and report negative weight cycles.
 *
 *  CONSTRAINTS:
 *    1 <= V <= 500
 *    1 <= E <= 10^4
 *    -10^4 <= weight <= 10^4
 *
 *  APPROACH 1: Standard Bellman-Ford — V-1 relaxation passes over all edges
 *    Time:  O(V × E)
 *    Space: O(V)  — distance[] only
 *
 *  APPROACH 2: SPFA (Shortest Path Faster Algorithm) — BFS-based Bellman-Ford
 *    Time:  O(E) average, O(V × E) worst case
 *    Space: O(V)  — distance[] + queue + inQueue[]
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Bellman_Ford_Negative_Weights {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — STANDARD BELLMAN-FORD
    // =========================================================

    /**
     * Result record from Bellman-Ford computation.
     */
    public static class BellmanFordResult {
        public final int[] distance;           // shortest distances from source
        public final boolean hasNegativeCycle; // true if any negative cycle reachable from source

        public BellmanFordResult(int[] distance, boolean hasNegativeCycle) {
            this.distance = distance;
            this.hasNegativeCycle = hasNegativeCycle;
        }
    }

    /**
     * Computes shortest distances using Bellman-Ford algorithm.
     *
     * ALGORITHM:
     *   1. Initialize distance[source] = 0; all others = ∞.
     *   2. Repeat V-1 times (enough for any acyclic shortest path):
     *      For each edge (u → v, weight w):
     *        If distance[u] + w < distance[v]: relax (update distance[v]).
     *   3. Run one MORE pass:
     *      If any edge can still be relaxed → that edge is on or reachable from
     *      a negative weight cycle → hasNegativeCycle = true.
     *
     * WHY V-1 PASSES?
     *   A shortest path in a graph with V vertices uses at most V-1 edges.
     *   After pass k, we have optimal distances for all paths using ≤ k edges.
     *
     * @param totalVertices  number of vertices (0-indexed)
     * @param edges          int[][] each row = {source, destination, weight}
     * @param sourceVertex   starting vertex
     * @return BellmanFordResult containing distances and negative cycle flag
     *
     * Time:  O(V × E)
     * Space: O(V)  — distance array only (edge list provided externally)
     */
    public static BellmanFordResult bellmanFord(int totalVertices, int[][] edges, int sourceVertex) {
        if (totalVertices <= 0) return new BellmanFordResult(new int[0], false);

        int[] distance = new int[totalVertices];
        Arrays.fill(distance, INFINITY);
        distance[sourceVertex] = 0;

        // Run V-1 relaxation passes
        for (int pass = 0; pass < totalVertices - 1; pass++) {
            boolean anyRelaxation = false;

            for (int[] edge : edges) {
                int fromVertex = edge[0];
                int toVertex   = edge[1];
                int weight     = edge[2];

                // Only relax if fromVertex is reachable (not ∞)
                if (distance[fromVertex] != INFINITY) {
                    int candidateDistance = distance[fromVertex] + weight;
                    if (candidateDistance < distance[toVertex]) {
                        distance[toVertex] = candidateDistance;
                        anyRelaxation = true;
                    }
                }
            }

            // Early termination: if no relaxation occurred, we're done
            if (!anyRelaxation) break;
        }

        // V-th pass: detect negative cycles
        boolean hasNegativeCycle = false;
        for (int[] edge : edges) {
            int fromVertex = edge[0];
            int toVertex   = edge[1];
            int weight     = edge[2];

            if (distance[fromVertex] != INFINITY &&
                distance[fromVertex] + weight < distance[toVertex]) {
                hasNegativeCycle = true;
                break;
            }
        }

        return new BellmanFordResult(distance, hasNegativeCycle);
    }

    /**
     * Marks all vertices reachable through a negative cycle as -∞ (Integer.MIN_VALUE/2).
     * This is important when you want to correctly report distances in graphs with
     * negative cycles (those vertices have undefined/−∞ shortest distances).
     */
    public static void propagateNegativeCycleEffect(int totalVertices, int[][] edges, int[] distance) {
        // Run V more passes: anything that can still be relaxed is -∞
        for (int pass = 0; pass < totalVertices; pass++) {
            for (int[] edge : edges) {
                int fromVertex = edge[0];
                int toVertex   = edge[1];
                int weight     = edge[2];
                if (distance[fromVertex] == -INFINITY ||
                    (distance[fromVertex] != INFINITY && distance[fromVertex] + weight < distance[toVertex])) {
                    distance[toVertex] = -INFINITY;  // mark as affected by negative cycle
                }
            }
        }
    }

    // =========================================================
    // APPROACH 2 — SPFA (SHORTEST PATH FASTER ALGORITHM)
    // =========================================================

    /**
     * SPFA: BFS-based Bellman-Ford that only relaxes edges from vertices
     * whose distance was recently updated. Typically much faster in practice.
     *
     * CYCLE DETECTION IN SPFA:
     *   Count how many times each vertex is enqueued.
     *   If any vertex is enqueued V or more times → negative cycle.
     *   (In an acyclic graph, each vertex is enqueued at most V-1 times.)
     *
     * @return BellmanFordResult with shortest distances and negative cycle flag
     *
     * Time:  O(E) average, O(V × E) worst
     * Space: O(V)
     */
    public static BellmanFordResult spfa(
            List<List<int[]>> adjacencyList,
            int totalVertices,
            int sourceVertex) {

        if (totalVertices == 0) return new BellmanFordResult(new int[0], false);

        int[] distance = new int[totalVertices];
        boolean[] inQueue = new boolean[totalVertices];
        int[] enqueueCount = new int[totalVertices];   // for cycle detection
        Arrays.fill(distance, INFINITY);
        distance[sourceVertex] = 0;

        Queue<Integer> bfsQueue = new LinkedList<>();
        bfsQueue.offer(sourceVertex);
        inQueue[sourceVertex] = true;
        enqueueCount[sourceVertex]++;

        while (!bfsQueue.isEmpty()) {
            int currentVertex = bfsQueue.poll();
            inQueue[currentVertex] = false;

            for (int[] neighborEdge : adjacencyList.get(currentVertex)) {
                int neighborVertex = neighborEdge[0];
                int edgeWeight     = neighborEdge[1];

                if (distance[currentVertex] + edgeWeight < distance[neighborVertex]) {
                    distance[neighborVertex] = distance[currentVertex] + edgeWeight;

                    if (!inQueue[neighborVertex]) {
                        bfsQueue.offer(neighborVertex);
                        inQueue[neighborVertex] = true;
                        enqueueCount[neighborVertex]++;

                        // Negative cycle: enqueued V or more times
                        if (enqueueCount[neighborVertex] >= totalVertices) {
                            return new BellmanFordResult(distance, true);
                        }
                    }
                }
            }
        }
        return new BellmanFordResult(distance, false);
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Bellman-Ford Negative Weights — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Standard positive weights ---
        System.out.println("\n--- Test 1: Standard graph (no negative weights) ---");
        // 0→1(4), 0→2(5), 1→3(3), 2→3(1)
        int[][] edges1 = {{0,1,4},{0,2,5},{1,3,3},{2,3,1}};
        BellmanFordResult result1 = bellmanFord(4, edges1, 0);
        System.out.println("Distances from 0: " + Arrays.toString(result1.distance));
        System.out.println("Has negative cycle: " + result1.hasNegativeCycle + " (expected false)");

        // --- Test 2: Negative weights, no cycle ---
        System.out.println("\n--- Test 2: Negative edge (1→2, weight -3) ---");
        int[][] edges2 = {{0,1,4},{1,2,-3},{0,2,8}};
        BellmanFordResult result2 = bellmanFord(3, edges2, 0);
        System.out.println("Distances from 0: " + Arrays.toString(result2.distance));
        // Expected: [0, 4, 1]  (0→1=4, then 1→2: 4+(-3)=1, better than 0→2=8)
        System.out.println("Has negative cycle: " + result2.hasNegativeCycle + " (expected false)");

        // --- Test 3: Negative cycle ---
        System.out.println("\n--- Test 3: Negative cycle (0→1→2→0 with total weight -1) ---");
        int[][] edges3 = {{0,1,1},{1,2,1},{2,0,-3}};  // cycle weight: 1+1-3 = -1
        BellmanFordResult result3 = bellmanFord(3, edges3, 0);
        System.out.println("Has negative cycle: " + result3.hasNegativeCycle + " (expected true)");

        // --- Test 4: SPFA on graph with negative edges ---
        System.out.println("\n--- Test 4: SPFA on graph with negative edge ---");
        List<List<int[]>> adj4 = new ArrayList<>();
        for (int i = 0; i < 3; i++) adj4.add(new ArrayList<>());
        adj4.get(0).add(new int[]{1, 4});
        adj4.get(1).add(new int[]{2, -3});
        adj4.get(0).add(new int[]{2, 8});
        BellmanFordResult spfaResult = spfa(adj4, 3, 0);
        System.out.println("SPFA distances: " + Arrays.toString(spfaResult.distance));
        System.out.println("SPFA negative cycle: " + spfaResult.hasNegativeCycle);

        // --- Test 5: Disconnected vertex ---
        System.out.println("\n--- Test 5: Disconnected vertex 3 ---");
        int[][] edges5 = {{0,1,2},{1,2,3}};
        BellmanFordResult result5 = bellmanFord(4, edges5, 0);
        System.out.println("Dist to vertex 3: " + result5.distance[3] + " (expected INFINITY-ish)");

        // --- Test 6: Single vertex ---
        System.out.println("\n--- Test 6: Single vertex, no edges ---");
        BellmanFordResult result6 = bellmanFord(1, new int[0][], 0);
        System.out.println("Dist: " + Arrays.toString(result6.distance) + " (expected [0])");

        System.out.println("\n==============================================");
        System.out.println("  All Bellman-Ford tests completed.");
        System.out.println("==============================================");
    }
}
