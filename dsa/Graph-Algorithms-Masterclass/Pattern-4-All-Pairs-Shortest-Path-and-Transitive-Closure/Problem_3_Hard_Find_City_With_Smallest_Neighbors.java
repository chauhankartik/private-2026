/**
 * ============================================================
 *  PATTERN 4 — ALL-PAIRS SHORTEST PATH
 *  Problem 3 (Hard): Find City With Smallest Neighbors   LC 1334
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    There are n cities and edges[i] = [from, to, weight]. Given a distance threshold,
 *    find the city with the SMALLEST NUMBER of cities reachable within the threshold.
 *    If multiple cities qualify, return the one with the GREATEST index.
 *
 *  EXAMPLE:
 *    n=4, edges=[[0,1,3],[1,2,1],[1,3,4],[2,3,1]], threshold=4
 *    Output: 3  (city 3 can reach: {1,2} within 4, city 0 can reach: {1,2,3} → city 3 wins)
 *
 *  CONSTRAINTS:
 *    2 <= n <= 100
 *    1 <= distanceThreshold <= 10^4
 *    edges form an undirected weighted graph.
 *
 *  APPROACH 1: Floyd-Warshall all-pairs + count neighbors per city
 *    Time:  O(V³ + V²)
 *    Space: O(V²)
 *
 *  APPROACH 2: Dijkstra from each city + count reachable
 *    Time:  O(V × (V + E) log V)
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Find_City_With_Smallest_Neighbors {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — FLOYD-WARSHALL + THRESHOLD COUNTING
    // =========================================================

    /**
     * Finds the city with fewest reachable neighbors within distanceThreshold.
     *
     * ALGORITHM:
     *   1. Build all-pairs shortest path matrix using Floyd-Warshall.
     *   2. For each city, count how many other cities are reachable within threshold.
     *   3. Return the city with the MINIMUM reachable count.
     *      Tie-break: return the city with the LARGEST index.
     *      (Iterate in reverse order and update "best" when strictly fewer.)
     *
     * WHY FLOYD-WARSHALL?
     *   n ≤ 100 → V³ = 10^6 operations — trivially fast.
     *   We need all-pairs, not just single-source.
     *   Floyd-Warshall is simple and optimal for this size.
     *
     * @param n                number of cities (0-indexed)
     * @param edges            undirected edges: [from, to, weight]
     * @param distanceThreshold reachability threshold
     * @return index of the best city
     *
     * Time:  O(V³ + V²)  = O(V³)
     * Space: O(V²)
     */
    public int findTheCity(int n, int[][] edges, int distanceThreshold) {
        // Initialize distance matrix
        int[][] distanceMatrix = new int[n][n];
        for (int[] row : distanceMatrix) Arrays.fill(row, INFINITY);
        for (int city = 0; city < n; city++) distanceMatrix[city][city] = 0;

        // Fill direct edges (undirected: add both directions)
        for (int[] edge : edges) {
            int cityA = edge[0], cityB = edge[1], roadWeight = edge[2];
            distanceMatrix[cityA][cityB] = Math.min(distanceMatrix[cityA][cityB], roadWeight);
            distanceMatrix[cityB][cityA] = Math.min(distanceMatrix[cityB][cityA], roadWeight);
        }

        // Floyd-Warshall: compute all-pairs shortest paths
        for (int intermediateCity = 0; intermediateCity < n; intermediateCity++) {
            for (int fromCity = 0; fromCity < n; fromCity++) {
                for (int toCity = 0; toCity < n; toCity++) {
                    if (distanceMatrix[fromCity][intermediateCity] != INFINITY &&
                        distanceMatrix[intermediateCity][toCity]   != INFINITY) {

                        int distanceThroughIntermediate =
                            distanceMatrix[fromCity][intermediateCity] +
                            distanceMatrix[intermediateCity][toCity];

                        if (distanceThroughIntermediate < distanceMatrix[fromCity][toCity]) {
                            distanceMatrix[fromCity][toCity] = distanceThroughIntermediate;
                        }
                    }
                }
            }
        }

        // Count reachable cities for each city and find the best
        int bestCity = -1;
        int minimumReachableCount = n;  // upper bound: at most n-1 neighbors reachable

        // Iterate in REVERSE order: when counts are equal, the larger index wins automatically
        for (int candidateCity = n - 1; candidateCity >= 0; candidateCity--) {
            int reachableCount = 0;

            for (int otherCity = 0; otherCity < n; otherCity++) {
                if (otherCity != candidateCity &&
                    distanceMatrix[candidateCity][otherCity] <= distanceThreshold) {
                    reachableCount++;
                }
            }

            if (reachableCount < minimumReachableCount) {
                minimumReachableCount = reachableCount;
                bestCity = candidateCity;
            }
        }
        return bestCity;
    }

    // =========================================================
    // APPROACH 2 — DIJKSTRA FROM EACH CITY
    // =========================================================

    /**
     * Alternative: run Dijkstra from each city and count reachable neighbors.
     * Preferred for sparse graphs where E << V² and V is moderately large.
     *
     * Time:  O(V × (V + E) log V)
     * Space: O(V + E)
     */
    public int findTheCityDijkstra(int n, int[][] edges, int distanceThreshold) {
        // Build adjacency list
        List<List<int[]>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) adjacencyList.add(new ArrayList<>());
        for (int[] edge : edges) {
            adjacencyList.get(edge[0]).add(new int[]{edge[1], edge[2]});
            adjacencyList.get(edge[1]).add(new int[]{edge[0], edge[2]});
        }

        int bestCity = -1;
        int minimumReachableCount = n;

        for (int sourceCity = n - 1; sourceCity >= 0; sourceCity--) {
            int[] distances = dijkstraSingleSource(adjacencyList, n, sourceCity);
            int reachableCount = 0;

            for (int otherCity = 0; otherCity < n; otherCity++) {
                if (otherCity != sourceCity && distances[otherCity] <= distanceThreshold) {
                    reachableCount++;
                }
            }

            if (reachableCount < minimumReachableCount) {
                minimumReachableCount = reachableCount;
                bestCity = sourceCity;
            }
        }
        return bestCity;
    }

    private int[] dijkstraSingleSource(List<List<int[]>> adj, int n, int source) {
        int[] dist = new int[n];
        Arrays.fill(dist, INFINITY);
        dist[source] = 0;
        PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        heap.offer(new int[]{0, source});
        while (!heap.isEmpty()) {
            int[] e = heap.poll();
            int d = e[0], v = e[1];
            if (d > dist[v]) continue;
            for (int[] neighbor : adj.get(v)) {
                int cand = dist[v] + neighbor[1];
                if (cand < dist[neighbor[0]]) {
                    dist[neighbor[0]] = cand;
                    heap.offer(new int[]{cand, neighbor[0]});
                }
            }
        }
        return dist;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Find_City_With_Smallest_Neighbors solver =
            new Problem_3_Hard_Find_City_With_Smallest_Neighbors();

        System.out.println("==============================================");
        System.out.println("  Find City Smallest Neighbors — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: LC 1334 Example 1 ---
        System.out.println("\n--- Test 1: n=4, threshold=4 ---");
        int[][] edges1 = {{0,1,3},{1,2,1},{1,3,4},{2,3,1}};
        System.out.println("Floyd-Warshall: " + solver.findTheCity(4, edges1, 4) + " (expected 3)");
        System.out.println("Dijkstra:       " + solver.findTheCityDijkstra(4, edges1, 4));

        // --- Test 2: LC 1334 Example 2 ---
        System.out.println("\n--- Test 2: n=5, threshold=29 ---");
        int[][] edges2 = {{0,1,2},{0,4,8},{1,2,3},{1,4,2},{2,3,1},{3,4,1}};
        System.out.println("Floyd-Warshall: " + solver.findTheCity(5, edges2, 29) + " (expected 0)");
        System.out.println("Dijkstra:       " + solver.findTheCityDijkstra(5, edges2, 29));

        // --- Test 3: Very small threshold (almost nothing reachable) ---
        System.out.println("\n--- Test 3: Threshold=1, few edges qualify ---");
        int[][] edges3 = {{0,1,10},{1,2,10},{2,3,10}};
        System.out.println("Floyd-Warshall (t=1): " + solver.findTheCity(4, edges3, 1) + " (expected 3, no neighbors)");

        // --- Test 4: All cities equally reachable ---
        System.out.println("\n--- Test 4: Complete graph, tie-break by largest index ---");
        int[][] edges4 = {{0,1,1},{0,2,1},{0,3,1},{1,2,1},{1,3,1},{2,3,1}};
        System.out.println("Floyd-Warshall (t=1): " + solver.findTheCity(4, edges4, 1));
        System.out.println("Dijkstra (t=1):       " + solver.findTheCityDijkstra(4, edges4, 1));

        System.out.println("\n==============================================");
        System.out.println("  All Find City tests completed.");
        System.out.println("==============================================");
    }
}
