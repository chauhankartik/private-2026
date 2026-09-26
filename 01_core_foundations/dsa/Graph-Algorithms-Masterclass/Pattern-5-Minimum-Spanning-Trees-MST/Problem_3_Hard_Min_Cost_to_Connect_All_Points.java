/**
 * ============================================================
 *  PATTERN 5 — MINIMUM SPANNING TREES (MST)
 *  Problem 3 (Hard): Min Cost to Connect All Points   LC 1584
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an array of points[i] = [xi, yi] on a 2D plane, return the minimum cost
 *    to connect all points. The cost to connect two points is the Manhattan distance:
 *    |xi - xj| + |yi - yj|. All points must be connected (directly or indirectly).
 *
 *  EXAMPLE:
 *    points=[[0,0],[2,2],[3,10],[5,2],[7,0]]
 *    Output: 20
 *
 *  CONSTRAINTS:
 *    1 <= points.length (= N) <= 1000
 *    -10^6 <= xi, yi <= 10^6
 *    All points are distinct.
 *
 *  APPROACH 1: Prim's without building explicit edges — O(N²)
 *    Compute distances on the fly from current MST to all unincluded points.
 *    Time:  O(N²)
 *    Space: O(N)  — no edge list needed
 *
 *  APPROACH 2: Kruskal's — build all O(N²) edges, sort, apply Union-Find
 *    Time:  O(N² log N)
 *    Space: O(N²)  — explicit edge list
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Min_Cost_to_Connect_All_Points {

    // =========================================================
    // APPROACH 1 — PRIM'S O(N²) WITHOUT EXPLICIT EDGE LIST
    // =========================================================

    /**
     * Connects all points with minimum total Manhattan distance.
     *
     * KEY INSIGHT — SKIP EXPLICIT EDGE BUILDING:
     *   With N=1000, there are N*(N-1)/2 ≈ 500,000 edges.
     *   Building and sorting them = O(N² log N).
     *   Prim's approach: at each step, scan all unincluded points and pick
     *   the one with the smallest Manhattan distance to any already-included point.
     *   This gives O(N²) time with O(N) space — better than Kruskal's for this problem.
     *
     * ALGORITHM (same as matrix Prim's, but distance computed on the fly):
     *   1. cheapestDist[p] = min Manhattan distance from p to the current MST.
     *   2. Initially: cheapestDist[0] = 0 (start from point 0), all others = MAX.
     *   3. Repeat N times:
     *      a. Pick the unincluded point p with minimum cheapestDist[p].
     *      b. Include p in MST. Add cheapestDist[p] to total cost.
     *      c. For all unincluded points q: update cheapestDist[q] if dist(p,q) is smaller.
     *
     * @param points 2D array of coordinates [xi, yi]
     * @return minimum total Manhattan distance to connect all points
     *
     * Time:  O(N²)
     * Space: O(N)  — cheapestDist[] + inMST[]
     */
    public int minCostConnectPoints(int[][] points) {
        if (points == null || points.length == 0) return 0;
        int totalPoints = points.length;
        if (totalPoints == 1) return 0;

        int[] cheapestDistToMST = new int[totalPoints];  // min dist from point to current MST
        boolean[] inMST         = new boolean[totalPoints];

        Arrays.fill(cheapestDistToMST, Integer.MAX_VALUE);
        cheapestDistToMST[0] = 0;   // start from point 0

        int totalMSTCost = 0;

        for (int iteration = 0; iteration < totalPoints; iteration++) {
            // Find the unincluded point with the smallest distance to MST (O(N) scan)
            int selectedPoint = -1;
            for (int point = 0; point < totalPoints; point++) {
                if (!inMST[point] && (selectedPoint == -1 ||
                    cheapestDistToMST[point] < cheapestDistToMST[selectedPoint])) {
                    selectedPoint = point;
                }
            }

            inMST[selectedPoint] = true;
            totalMSTCost += cheapestDistToMST[selectedPoint];

            // Update cheapestDistToMST for all unincluded points
            int selectedX = points[selectedPoint][0];
            int selectedY = points[selectedPoint][1];

            for (int otherPoint = 0; otherPoint < totalPoints; otherPoint++) {
                if (!inMST[otherPoint]) {
                    int manhattanDistance = Math.abs(selectedX - points[otherPoint][0])
                                         + Math.abs(selectedY - points[otherPoint][1]);
                    if (manhattanDistance < cheapestDistToMST[otherPoint]) {
                        cheapestDistToMST[otherPoint] = manhattanDistance;
                    }
                }
            }
        }

        return totalMSTCost;
    }

    // =========================================================
    // APPROACH 2 — KRUSKAL'S WITH EXPLICIT EDGE LIST
    // =========================================================

    /**
     * Builds all O(N²) edges explicitly, sorts by Manhattan distance, and applies Kruskal's.
     *
     * MORE MEMORY INTENSIVE but demonstrates the Kruskal's approach clearly.
     * Useful when the edge set needs to be inspected or stored.
     *
     * Time:  O(N² log N)  — sorting N² edges
     * Space: O(N²)        — explicit edge list
     */
    public int minCostConnectPointsKruskal(int[][] points) {
        if (points == null || points.length == 0) return 0;
        int n = points.length;
        if (n == 1) return 0;

        // Build all edges as {manhattanDistance, pointA, pointB}
        int totalEdges = n * (n - 1) / 2;
        int[][] allEdges = new int[totalEdges][3];
        int edgeIndex = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int distance = Math.abs(points[i][0] - points[j][0])
                             + Math.abs(points[i][1] - points[j][1]);
                allEdges[edgeIndex++] = new int[]{distance, i, j};
            }
        }

        // Sort by Manhattan distance (ascending)
        Arrays.sort(allEdges, Comparator.comparingInt(edge -> edge[0]));

        // Kruskal's with Union-Find
        int[] parent = new int[n];
        int[] rank   = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        int totalCost = 0;
        int edgesAccepted = 0;

        for (int[] edge : allEdges) {
            int distance = edge[0];
            int pointA   = edge[1];
            int pointB   = edge[2];

            int rootA = find(parent, pointA);
            int rootB = find(parent, pointB);

            if (rootA != rootB) {
                // Union by rank
                if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
                parent[rootB] = rootA;
                if (rank[rootA] == rank[rootB]) rank[rootA]++;

                totalCost += distance;
                edgesAccepted++;
                if (edgesAccepted == n - 1) break;   // MST complete
            }
        }

        return totalCost;
    }

    private int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Min_Cost_to_Connect_All_Points solver =
            new Problem_3_Hard_Min_Cost_to_Connect_All_Points();

        System.out.println("==============================================");
        System.out.println("  Min Cost Connect All Points — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: LC 1584 Example 1 ---
        System.out.println("\n--- Test 1: 5 points ---");
        int[][] pts1 = {{0,0},{2,2},{3,10},{5,2},{7,0}};
        System.out.println("Prim's O(N²): " + solver.minCostConnectPoints(pts1) + " (expected 20)");
        System.out.println("Kruskal's:    " + solver.minCostConnectPointsKruskal(pts1));

        // --- Test 2: LC 1584 Example 2 ---
        System.out.println("\n--- Test 2: 3 points in line ---");
        int[][] pts2 = {{3,12},{-2,5},{-4,1}};
        System.out.println("Prim's: " + solver.minCostConnectPoints(pts2) + " (expected 18)");
        System.out.println("Kruskal's: " + solver.minCostConnectPointsKruskal(pts2));

        // --- Test 3: Single point ---
        System.out.println("\n--- Test 3: Single point ---");
        int[][] pts3 = {{0,0}};
        System.out.println("Prim's: " + solver.minCostConnectPoints(pts3) + " (expected 0)");

        // --- Test 4: Two points ---
        System.out.println("\n--- Test 4: Two points ---");
        int[][] pts4 = {{0,0},{1,1}};
        System.out.println("Prim's: " + solver.minCostConnectPoints(pts4) + " (expected 2)");

        // --- Test 5: Points on a line ---
        System.out.println("\n--- Test 5: Points on x-axis ---");
        int[][] pts5 = {{0,0},{3,0},{6,0},{9,0}};
        System.out.println("Prim's:    " + solver.minCostConnectPoints(pts5) + " (expected 9)");
        System.out.println("Kruskal's: " + solver.minCostConnectPointsKruskal(pts5));

        // --- Test 6: Large values ---
        System.out.println("\n--- Test 6: Large coordinate values ---");
        int[][] pts6 = {{-1000000,-1000000},{1000000,1000000}};
        System.out.println("Prim's: " + solver.minCostConnectPoints(pts6) + " (expected 4000000)");

        System.out.println("\n==============================================");
        System.out.println("  All Min Cost Connect Points tests completed.");
        System.out.println("==============================================");
    }
}
