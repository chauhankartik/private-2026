/**
 * ============================================================
 *  PATTERN 6 — DISJOINT SET UNION-FIND
 *  Problem 1 (Basic): Number of Provinces   LC 547
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    There are n cities. isConnected[i][j] = 1 means city i and j are directly connected.
 *    A province is a group of cities directly or indirectly connected. Return the total number.
 *
 *  EXAMPLE:
 *    isConnected=[[1,1,0],[1,1,0],[0,0,1]] → 2 (cities {0,1} and {2})
 *
 *  CONSTRAINTS:
 *    1 <= n <= 200
 *
 *  APPROACH 1: Union-Find — process matrix upper triangle, union connected cities
 *    Time:  O(n²)  — matrix traversal
 *    Space: O(n)   — Union-Find arrays
 *
 *  APPROACH 2: DFS/BFS — traverse each city's connections, count unvisited starts
 *    Time:  O(n²)  — matrix traversal (adjacency matrix BFS/DFS)
 *    Space: O(n)   — visited[] + queue
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Number_of_Provinces {

    // =========================================================
    // UNION-FIND INNER CLASS
    // =========================================================
    static class UnionFind {
        private final int[] parent;
        private final int[] rank;
        private int componentCount;

        public UnionFind(int n) {
            parent = new int[n]; rank = new int[n];
            componentCount = n;
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        public int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        public boolean union(int x, int y) {
            int rx = find(x), ry = find(y);
            if (rx == ry) return false;
            if (rank[rx] < rank[ry]) { int t = rx; rx = ry; ry = t; }
            parent[ry] = rx;
            if (rank[rx] == rank[ry]) rank[rx]++;
            componentCount--;
            return true;
        }

        public int getComponentCount() { return componentCount; }
    }

    // =========================================================
    // APPROACH 1 — UNION-FIND
    // =========================================================

    /**
     * Counts provinces using Union-Find.
     *
     * TRAVERSAL:
     *   Only process upper triangle (i < j) — symmetric matrix, no self-loops.
     *   For each isConnected[i][j] == 1 with i != j: union(i, j).
     *   Final componentCount = number of provinces.
     *
     * @param isConnected n×n adjacency matrix (0 or 1)
     * @return number of distinct provinces
     *
     * Time:  O(n²)  — matrix traversal (Union-Find ops are ≈ O(1))
     * Space: O(n)
     */
    public int findCircleNum(int[][] isConnected) {
        if (isConnected == null || isConnected.length == 0) return 0;
        int totalCities = isConnected.length;

        UnionFind unionFind = new UnionFind(totalCities);

        for (int cityA = 0; cityA < totalCities; cityA++) {
            for (int cityB = cityA + 1; cityB < totalCities; cityB++) {
                if (isConnected[cityA][cityB] == 1) {
                    unionFind.union(cityA, cityB);
                }
            }
        }

        return unionFind.getComponentCount();
    }

    // =========================================================
    // APPROACH 2 — DFS (adjacency matrix)
    // =========================================================

    /**
     * Counts provinces using DFS on the adjacency matrix.
     * For each unvisited city, run DFS to mark its entire province as visited.
     *
     * Time:  O(n²)  — DFS processes all matrix entries
     * Space: O(n)   — visited[] + O(n) recursion depth
     */
    public int findCircleNumDFS(int[][] isConnected) {
        if (isConnected == null || isConnected.length == 0) return 0;
        int n = isConnected.length;
        boolean[] visited = new boolean[n];
        int provinceCount = 0;

        for (int city = 0; city < n; city++) {
            if (!visited[city]) {
                provinceCount++;
                dfsMarkProvince(isConnected, city, visited, n);
            }
        }
        return provinceCount;
    }

    private void dfsMarkProvince(int[][] isConnected, int currentCity, boolean[] visited, int n) {
        visited[currentCity] = true;
        for (int neighborCity = 0; neighborCity < n; neighborCity++) {
            if (isConnected[currentCity][neighborCity] == 1 && !visited[neighborCity]) {
                dfsMarkProvince(isConnected, neighborCity, visited, n);
            }
        }
    }

    /**
     * BFS variant — iterative alternative to DFS.
     * Time:  O(n²), Space: O(n)
     */
    public int findCircleNumBFS(int[][] isConnected) {
        int n = isConnected.length;
        boolean[] visited = new boolean[n];
        int provinceCount = 0;

        for (int startCity = 0; startCity < n; startCity++) {
            if (!visited[startCity]) {
                provinceCount++;
                Queue<Integer> bfsQueue = new LinkedList<>();
                bfsQueue.offer(startCity);
                visited[startCity] = true;

                while (!bfsQueue.isEmpty()) {
                    int currentCity = bfsQueue.poll();
                    for (int neighborCity = 0; neighborCity < n; neighborCity++) {
                        if (isConnected[currentCity][neighborCity] == 1 && !visited[neighborCity]) {
                            visited[neighborCity] = true;
                            bfsQueue.offer(neighborCity);
                        }
                    }
                }
            }
        }
        return provinceCount;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Number_of_Provinces solver = new Problem_1_Basic_Number_of_Provinces();

        System.out.println("==============================================");
        System.out.println("  Number of Provinces — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Two provinces ---
        System.out.println("\n--- Test 1: [[1,1,0],[1,1,0],[0,0,1]] ---");
        int[][] g1 = {{1,1,0},{1,1,0},{0,0,1}};
        System.out.println("Union-Find: " + solver.findCircleNum(g1)    + " (expected 2)");
        System.out.println("DFS:        " + solver.findCircleNumDFS(g1) + " (expected 2)");
        System.out.println("BFS:        " + solver.findCircleNumBFS(g1) + " (expected 2)");

        // --- Test 2: Three isolated cities ---
        System.out.println("\n--- Test 2: All isolated [[1,0,0],[0,1,0],[0,0,1]] ---");
        int[][] g2 = {{1,0,0},{0,1,0},{0,0,1}};
        System.out.println("Union-Find: " + solver.findCircleNum(g2) + " (expected 3)");
        System.out.println("DFS:        " + solver.findCircleNumDFS(g2));

        // --- Test 3: All connected ---
        System.out.println("\n--- Test 3: All connected [[1,1,1],[1,1,1],[1,1,1]] ---");
        int[][] g3 = {{1,1,1},{1,1,1},{1,1,1}};
        System.out.println("Union-Find: " + solver.findCircleNum(g3) + " (expected 1)");

        // --- Test 4: Single city ---
        System.out.println("\n--- Test 4: Single city ---");
        int[][] g4 = {{1}};
        System.out.println("Union-Find: " + solver.findCircleNum(g4) + " (expected 1)");

        // --- Test 5: Chain connection ---
        System.out.println("\n--- Test 5: Chain 0-1, 1-2, 2-3 ---");
        int[][] g5 = {
            {1,1,0,0},
            {1,1,1,0},
            {0,1,1,1},
            {0,0,1,1}
        };
        System.out.println("Union-Find: " + solver.findCircleNum(g5) + " (expected 1)");
        System.out.println("BFS:        " + solver.findCircleNumBFS(g5));

        System.out.println("\n==============================================");
        System.out.println("  All Number of Provinces tests completed.");
        System.out.println("==============================================");
    }
}
