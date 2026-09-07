/**
 * ============================================================
 *  PATTERN 3 — CORE QUEUE MECHANICS AND BUFFERING
 *  Problem 3 (Hard): Maximal Network Rank   LC 1615
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    There are n cities and m roads between cities. The network rank of two cities
 *    a and b = total number of roads directly connected to a OR b.
 *    If a road connects a and b directly, it is counted only ONCE.
 *    Find the MAXIMAL network rank among all pairs of cities.
 *
 *  EXAMPLE:
 *    n=4, roads=[[0,1],[0,3],[1,2],[1,3]]
 *    Network rank of (0,1) = degree[0]+degree[1]-1 = 2+3-1 = 4 (shared road counted once)
 *    Output: 4
 *
 *  CONSTRAINTS:
 *    2 <= n <= 100
 *    0 <= roads.length (= M) <= n*(n-1)/2
 *    No self-loops, no repeated roads.
 *
 *  WHY IN QUEUE PATTERN?
 *    This problem demonstrates BFS-style degree computation and graph buffering —
 *    the road adjacency tracking uses a boolean[][] buffer (adjacency matrix) and
 *    an int[] degree counter (analogous to a frequency buffer). This pattern of
 *    "build a degree/adjacency buffer then answer queries" is a queue-adjacent skill.
 *
 *  APPROACH 1: Degree array + adjacency matrix — O(N²) pair scan
 *    Time:  O(M + N²)
 *    Space: O(N²)
 *
 *  APPROACH 2: Degree array + adjacency set — O(N² + M) with HashSet lookup
 *    Time:  O(M + N²)
 *    Space: O(M)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Maximal_Network_Rank {

    // =========================================================
    // APPROACH 1 — DEGREE ARRAY + BOOLEAN ADJACENCY MATRIX
    // =========================================================

    /**
     * Computes maximal network rank using degree array and adjacency matrix.
     *
     * ALGORITHM:
     *   1. Build degree[city] = number of roads directly connected.
     *   2. Build isDirectlyConnected[a][b] = true if road a-b exists.
     *   3. For each pair (a, b) with a < b:
     *      networkRank = degree[a] + degree[b]
     *                    - (isDirectlyConnected[a][b] ? 1 : 0)
     *      Track maximum.
     *
     * WHY SUBTRACT 1 FOR DIRECT CONNECTION?
     *   degree[a] counts the road a-b.
     *   degree[b] also counts the road a-b.
     *   Their sum double-counts the shared road → subtract 1.
     *
     * @param totalCities  number of cities (0-indexed)
     * @param roads        array of [cityA, cityB] road pairs
     * @return maximal network rank
     *
     * Time:  O(M + N²)  — M to build arrays, N² to scan all pairs
     * Space: O(N²)      — boolean adjacency matrix
     */
    public int maximalNetworkRankMatrix(int totalCities, int[][] roads) {
        if (totalCities == 0) return 0;

        int[] cityDegree = new int[totalCities];
        boolean[][] isDirectlyConnected = new boolean[totalCities][totalCities];

        // Build degree array and adjacency matrix from road list
        for (int[] road : roads) {
            int cityA = road[0];
            int cityB = road[1];
            cityDegree[cityA]++;
            cityDegree[cityB]++;
            isDirectlyConnected[cityA][cityB] = true;
            isDirectlyConnected[cityB][cityA] = true;   // undirected
        }

        int maximalRank = 0;

        // Scan all unique city pairs (a, b) with a < b
        for (int cityA = 0; cityA < totalCities; cityA++) {
            for (int cityB = cityA + 1; cityB < totalCities; cityB++) {
                int pairNetworkRank = cityDegree[cityA] + cityDegree[cityB];
                // If directly connected, the shared road is double-counted — subtract 1
                if (isDirectlyConnected[cityA][cityB]) {
                    pairNetworkRank--;
                }
                maximalRank = Math.max(maximalRank, pairNetworkRank);
            }
        }

        return maximalRank;
    }

    // =========================================================
    // APPROACH 2 — DEGREE ARRAY + HASHSET ADJACENCY (SPACE-EFFICIENT)
    // =========================================================

    /**
     * Same algorithm using a HashSet of road pairs instead of a boolean matrix.
     * More memory-efficient when M << N² (sparse graphs).
     *
     * EDGE KEY ENCODING:
     *   For cities a and b (a < b), encode as: a * (n+1) + b
     *   This maps each undirected edge to a unique integer key.
     *   Lookup in HashSet is O(1) average.
     *
     * Time:  O(M + N²)
     * Space: O(M)  — set stores M edge keys (vs O(N²) for matrix)
     */
    public int maximalNetworkRankHashSet(int totalCities, int[][] roads) {
        if (totalCities == 0) return 0;

        int[] cityDegree = new int[totalCities];
        Set<Integer> directConnectionSet = new HashSet<>();

        for (int[] road : roads) {
            int cityA = road[0];
            int cityB = road[1];
            cityDegree[cityA]++;
            cityDegree[cityB]++;

            // Encode edge as unique integer (always smaller city first for consistency)
            int smallerCity = Math.min(cityA, cityB);
            int largerCity  = Math.max(cityA, cityB);
            directConnectionSet.add(smallerCity * (totalCities + 1) + largerCity);
        }

        int maximalRank = 0;

        for (int cityA = 0; cityA < totalCities; cityA++) {
            for (int cityB = cityA + 1; cityB < totalCities; cityB++) {
                int pairRank = cityDegree[cityA] + cityDegree[cityB];

                // Check direct connection via HashSet
                int edgeKey = cityA * (totalCities + 1) + cityB;
                if (directConnectionSet.contains(edgeKey)) pairRank--;

                maximalRank = Math.max(maximalRank, pairRank);
            }
        }

        return maximalRank;
    }

    /**
     * OPTIMIZED: Skip pairs that cannot possibly be maximum.
     *
     * KEY OBSERVATION:
     *   The maximum rank pair must include at least one of the highest-degree cities.
     *   Find the top-2 degrees. Pairs between the highest-degree city and any other
     *   are the candidates. This doesn't change worst-case but prunes many pairs.
     *
     * For full optimization: sort by degree, only scan top-degree city's pairs.
     */
    public int maximalNetworkRankOptimized(int totalCities, int[][] roads) {
        if (totalCities <= 1) return 0;
        int[] cityDegree = new int[totalCities];
        boolean[][] adjacent = new boolean[totalCities][totalCities];

        for (int[] road : roads) {
            cityDegree[road[0]]++;
            cityDegree[road[1]]++;
            adjacent[road[0]][road[1]] = true;
            adjacent[road[1]][road[0]] = true;
        }

        // Find max degree
        int maxDegree = 0;
        for (int deg : cityDegree) maxDegree = Math.max(maxDegree, deg);

        int maximalRank = 0;
        for (int a = 0; a < totalCities; a++) {
            if (cityDegree[a] < maxDegree - 1) continue;  // pruning
            for (int b = a + 1; b < totalCities; b++) {
                int rank = cityDegree[a] + cityDegree[b] - (adjacent[a][b] ? 1 : 0);
                maximalRank = Math.max(maximalRank, rank);
            }
        }
        return maximalRank;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Maximal_Network_Rank solver = new Problem_3_Hard_Maximal_Network_Rank();

        System.out.println("========================================");
        System.out.println("  Maximal Network Rank — Test Suite");
        System.out.println("========================================");

        java.util.function.BiFunction<Integer, int[][], String> testAll = (n, roads) -> {
            int m  = solver.maximalNetworkRankMatrix(n, roads);
            int hs = solver.maximalNetworkRankHashSet(n, roads);
            int op = solver.maximalNetworkRankOptimized(n, roads);
            boolean ok = (m == hs && hs == op);
            return String.format("Matrix=%d HashSet=%d Opt=%d %s", m, hs, op, ok ? "✓" : "MISMATCH");
        };

        System.out.println("\n--- LC 1615 Example 1 (n=4) ---");
        System.out.println(testAll.apply(4, new int[][]{{0,1},{0,3},{1,2},{1,3}}) + " (expected 4)");

        System.out.println("--- LC 1615 Example 2 (n=5) ---");
        System.out.println(testAll.apply(5, new int[][]{{0,1},{0,3},{1,2},{1,3},{2,3},{2,4}}) + " (expected 5)");

        System.out.println("--- LC 1615 Example 3 (n=8) ---");
        System.out.println(testAll.apply(8, new int[][]{{0,1},{1,2},{2,3},{2,4},{5,6},{5,7}}) + " (expected 5)");

        System.out.println("--- No roads ---");
        System.out.println(testAll.apply(3, new int[0][]) + " (expected 0)");

        System.out.println("--- All cities connected (complete graph n=4) ---");
        System.out.println(testAll.apply(4, new int[][]{{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}}) + " (expected 5)");

        System.out.println("--- Two isolated cities ---");
        System.out.println(testAll.apply(2, new int[][]{{0,1}}) + " (expected 1)");

        System.out.println("--- Star graph (one hub) ---");
        System.out.println(testAll.apply(5, new int[][]{{0,1},{0,2},{0,3},{0,4}}) + " (expected 5)");

        System.out.println("\n========================================");
        System.out.println("  All Network Rank tests done.");
        System.out.println("========================================");
    }
}
