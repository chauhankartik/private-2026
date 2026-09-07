/**
 * ============================================================
 *  PATTERN 3 — SHORTEST PATHS: SINGLE SOURCE
 *  Problem 3 (Hard): Cheapest Flights Within K Stops   LC 787
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    There are n cities and m flights. flights[i] = [from, to, price].
 *    Find the cheapest price to travel from src to dst with AT MOST k stops.
 *    (k stops = k+1 edges). Return -1 if impossible.
 *
 *  EXAMPLE:
 *    n=3, flights=[[0,1,100],[1,2,100],[0,2,500]], src=0, dst=2, k=1
 *    Output: 200  (0→1→2, with 1 stop)
 *
 *  CONSTRAINTS:
 *    1 <= n <= 100
 *    0 <= flights.length <= 500
 *    0 <= k < n
 *
 *  APPROACH 1: Bellman-Ford variant — K+1 edge relaxation passes
 *    Time:  O(K × E)
 *    Space: O(V)  — two distance arrays (current layer, previous layer)
 *
 *  APPROACH 2: Dijkstra with state = (distance, stops_used)
 *    Time:  O(E × K × log(V × K))  — state space is V × K
 *    Space: O(V × K)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Cheapest_Flights_Within_K_Stops {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — BELLMAN-FORD K-PASS (LAYERED DP)
    // =========================================================

    /**
     * Finds cheapest flight from src to dst with at most k stops.
     *
     * KEY INSIGHT — USE TWO ARRAYS (not in-place):
     *   dp[v] = cheapest cost to reach v using AT MOST (pass) edges.
     *   CRITICAL: when relaxing edges in pass (pass+1), we must read from
     *   the PREVIOUS pass's values — not from the current pass!
     *
     *   If we relax in-place (single array), an edge relaxed early in the
     *   current pass might enable another relaxation in the SAME pass,
     *   effectively using 2+ edges in one "stop" — WRONG.
     *
     *   Example bug with in-place update:
     *     0→1 relaxed first (pass 1), then 1→2 uses the JUST-updated 0→1 value
     *     → 0→1→2 counted as 1 edge, but should require 2 passes (pass=2).
     *
     * @param n       number of cities (0-indexed)
     * @param flights int[][] each = {from, to, price}
     * @param src     source city
     * @param dst     destination city
     * @param k       max number of stops (= at most k+1 edges)
     * @return cheapest price, or -1 if no path within k stops
     *
     * Time:  O(K × E)
     * Space: O(V)  — two arrays of size V
     */
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        if (n <= 0 || flights == null) return -1;

        int[] previousLayerCost = new int[n];   // cost using ≤ pass edges
        Arrays.fill(previousLayerCost, INFINITY);
        previousLayerCost[src] = 0;

        // Perform k+1 relaxation passes (allowing at most k+1 edges = k stops)
        for (int stopCount = 0; stopCount <= k; stopCount++) {
            int[] currentLayerCost = Arrays.copyOf(previousLayerCost, n);

            for (int[] flight : flights) {
                int fromCity   = flight[0];
                int toCity     = flight[1];
                int flightCost = flight[2];

                // ONLY use previousLayerCost (not currentLayerCost) for the source
                // → ensures we don't use more than stopCount+1 edges in this pass
                if (previousLayerCost[fromCity] != INFINITY) {
                    int candidateCost = previousLayerCost[fromCity] + flightCost;
                    if (candidateCost < currentLayerCost[toCity]) {
                        currentLayerCost[toCity] = candidateCost;
                    }
                }
            }
            previousLayerCost = currentLayerCost;
        }

        return previousLayerCost[dst] == INFINITY ? -1 : previousLayerCost[dst];
    }

    // =========================================================
    // APPROACH 2 — MODIFIED DIJKSTRA WITH STOP COUNT STATE
    // =========================================================

    /**
     * Dijkstra where each heap state is {cost, vertex, stopsUsed}.
     * We allow visiting the same vertex multiple times IF the stop count is different.
     *
     * IMPORTANT DIFFERENCE FROM STANDARD DIJKSTRA:
     *   Standard Dijkstra: once a vertex is settled, skip it.
     *   Here: a vertex can be re-explored if reached with fewer stops,
     *         because fewer stops = more flexibility for future edges.
     *
     * STATE SPACE: (vertex, stopsUsed) pairs — at most V × (K+1) states.
     * Visited condition: visited[vertex][stopsUsed] = true prevents re-exploring
     *                    the EXACT same (vertex, stopCount) state.
     *
     * @return cheapest price, or -1 if impossible
     *
     * Time:  O(E × K × log(V × K))
     * Space: O(V × K)
     */
    public int findCheapestPriceDijkstra(int n, int[][] flights, int src, int dst, int k) {
        // Build adjacency list
        List<List<int[]>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) adjacencyList.add(new ArrayList<>());
        for (int[] flight : flights) {
            adjacencyList.get(flight[0]).add(new int[]{flight[1], flight[2]});
        }

        // Min-heap: {totalCost, currentCity, stopsUsedSoFar}
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(state -> state[0]));
        minHeap.offer(new int[]{0, src, 0});

        // Track minimum stops to reach each (city, stops) state
        // visitedMinCost[city][stops] = min cost seen for this state
        int[][] visitedMinCost = new int[n][k + 2];
        for (int[] row : visitedMinCost) Arrays.fill(row, INFINITY);
        visitedMinCost[src][0] = 0;

        while (!minHeap.isEmpty()) {
            int[] state      = minHeap.poll();
            int totalCost    = state[0];
            int currentCity  = state[1];
            int stopsUsed    = state[2];

            if (currentCity == dst) return totalCost;
            if (stopsUsed > k) continue;  // exceeded stop limit

            for (int[] neighborFlight : adjacencyList.get(currentCity)) {
                int nextCity     = neighborFlight[0];
                int flightCost   = neighborFlight[1];
                int newTotalCost = totalCost + flightCost;
                int newStops     = stopsUsed + 1;

                if (newStops <= k + 1 && newTotalCost < visitedMinCost[nextCity][newStops]) {
                    visitedMinCost[nextCity][newStops] = newTotalCost;
                    minHeap.offer(new int[]{newTotalCost, nextCity, newStops});
                }
            }
        }
        return -1;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Cheapest_Flights_Within_K_Stops solver =
            new Problem_3_Hard_Cheapest_Flights_Within_K_Stops();

        System.out.println("==============================================");
        System.out.println("  Cheapest Flights K Stops — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic LC 787 example ---
        System.out.println("\n--- Test 1: Classic example ---");
        // 0→1:100, 1→2:100, 0→2:500
        int[][] fl1 = {{0,1,100},{1,2,100},{0,2,500}};
        System.out.println("k=1: " + solver.findCheapestPrice(3, fl1, 0, 2, 1) + " (expected 200)");
        System.out.println("k=0: " + solver.findCheapestPrice(3, fl1, 0, 2, 0) + " (expected 500)");
        System.out.println("Dijkstra k=1: " + solver.findCheapestPriceDijkstra(3, fl1, 0, 2, 1));

        // --- Test 2: No path within k stops ---
        System.out.println("\n--- Test 2: No path within k stops ---");
        // 0→1:1, 1→2:1 (need 2 edges), k=0
        int[][] fl2 = {{0,1,1},{1,2,1}};
        System.out.println("k=0: " + solver.findCheapestPrice(3, fl2, 0, 2, 0) + " (expected -1)");
        System.out.println("k=1: " + solver.findCheapestPrice(3, fl2, 0, 2, 1) + " (expected 2)");

        // --- Test 3: Multiple paths, pick cheapest within stops ---
        System.out.println("\n--- Test 3: Multiple paths, choose cheapest ---");
        int[][] fl3 = {
            {0,1,5},{1,2,5},{0,3,2},{3,1,2},{1,4,1},{4,2,1}
        };
        System.out.println("0→2, k=2: " + solver.findCheapestPrice(5, fl3, 0, 2, 2) + " (expected 7: 0→3→1→2... wait, 2+2+5=9 or 0→1→2=10, 0→3→1→4→2=2+2+1+1=6 but k=2 means max 2 stops)");
        System.out.println("Dijkstra: " + solver.findCheapestPriceDijkstra(5, fl3, 0, 2, 2));

        // --- Test 4: Src == dst ---
        System.out.println("\n--- Test 4: src == dst ---");
        System.out.println("k=0: " + solver.findCheapestPrice(3, fl1, 0, 0, 0) + " (expected 0)");

        // --- Test 5: Large k (effectively unlimited) ---
        System.out.println("\n--- Test 5: k >= n-1 (unlimited hops) ---");
        int[][] fl5 = {{0,1,10},{1,2,10},{2,3,10},{3,4,10}};
        System.out.println("k=3 (exact): " + solver.findCheapestPrice(5, fl5, 0, 4, 3) + " (expected 40)");
        System.out.println("k=5 (more than enough): " + solver.findCheapestPrice(5, fl5, 0, 4, 5) + " (expected 40)");

        System.out.println("\n==============================================");
        System.out.println("  All Cheapest Flights tests completed.");
        System.out.println("==============================================");
    }
}
