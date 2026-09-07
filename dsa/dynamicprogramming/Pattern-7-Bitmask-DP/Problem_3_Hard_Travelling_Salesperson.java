/**
 * ============================================================
 *  PATTERN 7 — BITMASK DP
 *  Problem 3 (Hard): Travelling Salesperson Problem (TSP)
 *                    LC 847 — Shortest Path Visiting All Nodes
 * ============================================================
 *
 *  Difficulty  : Hard
 *  Pattern     : Bitmask DP — dp[mask][node]
 *  LeetCode    : https://leetcode.com/problems/shortest-path-visiting-all-nodes/
 *                (Classic TSP via bitmask DP also shown below)
 *
 *  PROBLEM STATEMENT (LC 847):
 *    Given an undirected connected graph with n nodes (0..n-1),
 *    find the shortest path that visits every node.
 *    You may start and end at any node, and revisit nodes/edges.
 *
 *  EXAMPLES:
 *    graph = [[1,2,3],[0],[0],[0]]    → 4  (star graph: 0-1, 0-2, 0-3)
 *    graph = [[1],[0,2,4],[1,3,4],[2],[1,2]]  → 4
 *
 *  RELATED: Classic TSP (weighted, no revisit, must return to start)
 *    Given n cities and a distance matrix, find the minimum-cost Hamiltonian cycle.
 *    Both problems use dp[mask][last] bitmask DP.
 *
 * ============================================================
 */
import java.util.*;

class Problem3_TravellingSalesperson {

    // =========================================================
    // PART A — LC 847: Shortest Path Visiting All Nodes (BFS + Bitmask)
    // =========================================================
    /**
     * APPROACH: BFS + Bitmask State
     *
     * STATE: (currentNode, visitedMask)
     *   currentNode   = which node we're currently at
     *   visitedMask   = bitmask of which nodes have been visited
     *
     * Why BFS? We want the SHORTEST path (unweighted graph), and BFS guarantees
     * we find the minimum number of steps first.
     *
     * TRANSITION:
     *   From (node, mask), for each neighbor nb of node:
     *     Move to (nb, mask | (1 << nb)) with cost + 1
     *
     * BASE:
     *   Start at EVERY node simultaneously (BFS from all start nodes at step 0).
     *   Initial states: (i, 1 << i) for each node i.
     *
     * TERMINATION:
     *   When visitedMask == (1 << n) - 1  (all nodes visited)
     *
     * VISITED SET:
     *   A boolean visited[mask][node] to avoid re-processing the same state.
     *
     * VISUAL for [[1,2,3],[0],[0],[0]] (star, 4 nodes):
     *   Initial queue: (0, 0001), (1, 0010), (2, 0100), (3, 1000)
     *   Step 1 from (0, 0001): visit 1→(1, 0011), 2→(2, 0101), 3→(3, 1001)
     *   Step 2: (1, 0011)→(0, 0111)...
     *   Step 4: (0, 1111) → DONE, answer = 4
     *
     * Time:  O(2^n * n) — n*2^n states, each processed once
     * Space: O(2^n * n) — visited array
     *
     * Follow-up: If edges are weighted (actual TSP)?
     *   BFS won't work. Use DP with Dijkstra or the classic TSP bitmask DP (Part B).
     */
    public int shortestPathLength(int[][] graph) {
        int n = graph.length;
        int fullMask = (1 << n) - 1;

        if (n == 1) return 0;

        // BFS queue: [node, visitedMask]
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[1 << n][n];

        // Start from every node
        for (int i = 0; i < n; i++) {
            int mask = 1 << i;
            queue.offer(new int[]{i, mask});
            visited[mask][i] = true;
        }

        int steps = 0;
        while (!queue.isEmpty()) {
            steps++;
            int size = queue.size();

            for (int s = 0; s < size; s++) {
                int[] curr = queue.poll();
                int node = curr[0], mask = curr[1];

                for (int nb : graph[node]) {
                    int newMask = mask | (1 << nb);

                    if (newMask == fullMask) return steps;  // all visited!

                    if (!visited[newMask][nb]) {
                        visited[newMask][nb] = true;
                        queue.offer(new int[]{nb, newMask});
                    }
                }
            }
        }

        return -1;  // should never reach here for connected graphs
    }

    // =========================================================
    // PART B — Classic TSP: Minimum Cost Hamiltonian Cycle (Bitmask DP)
    // =========================================================
    /**
     * PROBLEM:
     *   n cities. dist[i][j] = cost to travel from city i to city j.
     *   Start at city 0. Visit ALL cities exactly once. Return to city 0.
     *   Minimize total travel cost.
     *
     * STATE:
     *   dp[mask][i] = minimum cost to travel from city 0, visiting exactly the
     *                 cities in 'mask', and currently being at city i.
     *   (Bit j in mask = city j has been visited)
     *
     * BASE CASE:
     *   dp[1 << 0][0] = 0  (start at city 0, only city 0 visited, zero cost)
     *   Everything else = infinity
     *
     * TRANSITION:
     *   For each (mask, last) where city 'last' is in mask:
     *     For each city 'next' NOT in mask:
     *       dp[mask | (1<<next)][next] = min(
     *         dp[mask | (1<<next)][next],
     *         dp[mask][last] + dist[last][next]
     *       )
     *
     * ANSWER:
     *   min over all last cities (last != 0):
     *     dp[fullMask][last] + dist[last][0]   ← return to city 0
     *
     * VISUAL (n=4, cities 0,1,2,3):
     *   mask=0001 (city 0): dp[0001][0]=0
     *   mask=0011 (cities 0,1): dp[0011][1]=dp[0001][0]+dist[0][1]
     *   mask=0101 (cities 0,2): dp[0101][2]=dp[0001][0]+dist[0][2]
     *   ...
     *   mask=1111 (all): dp[1111][1]=min of all ways to reach city 1 last
     *   Answer = min(dp[1111][1]+dist[1][0], dp[1111][2]+dist[2][0], dp[1111][3]+dist[3][0])
     *
     * Time:  O(n^2 * 2^n) — n*2^n states, each with n transitions
     * Space: O(n * 2^n)
     *
     * Note: This is an NP-hard problem. Bitmask DP is optimal for n <= 20.
     * For n > 20, heuristics (nearest neighbor, 2-opt, Christofides) are used.
     *
     * Follow-up: What is the best APPROXIMATE algorithm for TSP?
     *   Christofides algorithm: 3/2-approximation ratio guarantee.
     *   Simple greedy nearest-neighbor: no guarantee but fast.
     */
    public int tspMinCost(int[][] dist) {
        int n = dist.length;
        int fullMask = (1 << n) - 1;

        // dp[mask][last] = min cost to visit all cities in mask, ending at 'last'
        int[][] dp = new int[1 << n][n];
        for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE / 2);

        // Base case: start at city 0
        dp[1][0] = 0;

        // Fill DP
        for (int mask = 1; mask < (1 << n); mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0) continue;  // 'last' must be in mask
                if (dp[mask][last] >= Integer.MAX_VALUE / 2) continue;

                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) continue;  // 'next' not visited yet
                    if (dist[last][next] == Integer.MAX_VALUE / 2) continue;  // no edge

                    int newMask = mask | (1 << next);
                    dp[newMask][next] = Math.min(dp[newMask][next],
                                                  dp[mask][last] + dist[last][next]);
                }
            }
        }

        // Find minimum cost to return to city 0
        int ans = Integer.MAX_VALUE;
        for (int last = 1; last < n; last++) {
            if (dist[last][0] < Integer.MAX_VALUE / 2) {
                ans = Math.min(ans, dp[fullMask][last] + dist[last][0]);
            }
        }

        return ans;
    }

    // =========================================================
    // PART C — TSP Path Reconstruction
    // =========================================================
    /**
     * Return the actual optimal tour (city visit order), not just the cost.
     * Store the 'next' city chosen at each (mask, last) in a parent table.
     */
    public int[] tspTour(int[][] dist) {
        int n = dist.length;
        int fullMask = (1 << n) - 1;

        int[][] dp     = new int[1 << n][n];
        int[][] parent = new int[1 << n][n];
        for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE / 2);
        for (int[] row : parent) Arrays.fill(row, -1);

        dp[1][0] = 0;

        for (int mask = 1; mask < (1 << n); mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0) continue;
                if (dp[mask][last] >= Integer.MAX_VALUE / 2) continue;
                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) continue;
                    int newCost = dp[mask][last] + dist[last][next];
                    int newMask = mask | (1 << next);
                    if (newCost < dp[newMask][next]) {
                        dp[newMask][next] = newCost;
                        parent[newMask][next] = last;
                    }
                }
            }
        }

        // Find last city in optimal tour
        int lastCity = 1, minCost = Integer.MAX_VALUE;
        for (int last = 1; last < n; last++) {
            int cost = dp[fullMask][last] + dist[last][0];
            if (cost < minCost) {
                minCost = cost;
                lastCity = last;
            }
        }

        // Reconstruct path backwards
        int[] tour = new int[n + 1];
        tour[n] = 0;  // return to start
        int mask = fullMask, city = lastCity;
        for (int i = n - 1; i >= 0; i--) {
            tour[i] = city;
            int prev = parent[mask][city];
            mask ^= (1 << city);
            city = prev;
        }

        return tour;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_TravellingSalesperson sol = new Problem3_TravellingSalesperson();

        // Part A: Shortest Path Visiting All Nodes (LC 847)
        System.out.println("=== LC 847: Shortest Path Visiting All Nodes ===");

        int[][] g1 = {{1, 2, 3}, {0}, {0}, {0}};
        System.out.println("Star graph: " + sol.shortestPathLength(g1));  // 4

        int[][] g2 = {{1}, {0, 2, 4}, {1, 3, 4}, {2}, {1, 2}};
        System.out.println("Graph 2   : " + sol.shortestPathLength(g2));  // 4

        int[][] g3 = {{1}, {0, 2}, {1}};
        System.out.println("Path 0-1-2: " + sol.shortestPathLength(g3));  // 2

        // Part B: Classic TSP
        System.out.println("\n=== Classic TSP (Minimum Hamiltonian Cycle) ===");

        // 4-city example (fully connected)
        final int INF = Integer.MAX_VALUE / 2;
        int[][] dist = {
            {0,  10, 15, 20},
            {10, 0,  35, 25},
            {15, 35, 0,  30},
            {20, 25, 30, 0 }
        };
        System.out.println("4-city TSP min cost: " + sol.tspMinCost(dist));  // 80

        // Part C: Tour reconstruction
        int[] tour = sol.tspTour(dist);
        System.out.print("Optimal tour: ");
        for (int c : tour) System.out.print(c + " ");
        System.out.println();

        // 5-city example
        int[][] dist5 = {
            {0, 2, 9, 10, INF},
            {1, 0, 6, 4,  INF},
            {INF, 7, 0, 8, INF},
            {6, 3, INF, 0, 3},
            {INF, INF, INF, 5, 0}
        };
        // Note: INF means no direct edge
        System.out.println("5-city TSP min cost: " + sol.tspMinCost(dist5));
    }
}
