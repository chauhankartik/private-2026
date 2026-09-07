/**
 * ============================================================
 *  PATTERN 6 — DISJOINT SET UNION-FIND
 *  Problem 2 (Medium): Redundant Connection   LC 684 / 685
 * ============================================================
 *
 *  PROBLEM STATEMENT (LC 684 — Undirected):
 *    Given a tree with N nodes (1-indexed, originally N-1 edges), one extra
 *    edge has been added. Find and return this redundant edge. If multiple
 *    answers, return the one appearing last in the input.
 *
 *  CONSTRAINTS:
 *    3 <= nodes.length <= 1000
 *    edges[i] = [ai, bi]: ai != bi (no self-loops), all values in [1, n]
 *
 *  APPROACH 1: Union-Find — process edges in order; the first edge that creates
 *              a cycle (find(a) == find(b)) is the redundant one.
 *    Time:  O(N × α(N)) ≈ O(N)
 *    Space: O(N)
 *
 *  APPROACH 2: DFS path detection — for each edge, check if a path already
 *              exists between its endpoints before adding it.
 *    Time:  O(N²)  — O(N) DFS per edge, N edges
 *    Space: O(N)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Redundant_Connection {

    // =========================================================
    // APPROACH 1 — UNION-FIND (OPTIMAL)
    // =========================================================

    /**
     * Finds the redundant connection using Union-Find.
     *
     * KEY INSIGHT:
     *   A spanning tree with N nodes has exactly N-1 edges.
     *   The input has N edges → exactly one edge is redundant.
     *   Process edges sequentially. The FIRST edge where both endpoints
     *   are already in the same component creates a cycle → it's redundant.
     *   Since we want the LAST such edge (if multiple), process in order and
     *   keep updating — but by definition there's exactly one such edge.
     *
     * @param edges array of edges [a, b] (1-indexed nodes)
     * @return the redundant edge [a, b]
     *
     * Time:  O(N × α(N)) ≈ O(N)
     * Space: O(N)
     */
    public int[] findRedundantConnection(int[][] edges) {
        int n = edges.length;   // also = number of nodes (1 to n)
        int[] parent = new int[n + 1];
        int[] rank   = new int[n + 1];

        for (int node = 1; node <= n; node++) parent[node] = node;

        for (int[] edge : edges) {
            int nodeA = edge[0];
            int nodeB = edge[1];

            int rootA = find(parent, nodeA);
            int rootB = find(parent, nodeB);

            if (rootA == rootB) {
                // Both nodes already connected → this edge creates a cycle → redundant
                return edge;
            }

            // Union by rank
            if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
            parent[rootB] = rootA;
            if (rank[rootA] == rank[rootB]) rank[rootA]++;
        }

        return new int[0];   // should never reach here per problem constraints
    }

    private int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    // =========================================================
    // APPROACH 2 — DFS REACHABILITY CHECK
    // =========================================================

    /**
     * For each edge, use DFS to check if there's already a path between its endpoints.
     * If yes → this edge is redundant. Otherwise, add the edge to the graph.
     * Returns the LAST redundant edge found (per problem requirement: "appears last").
     *
     * Time:  O(N²)  — O(N) DFS per edge, N edges
     * Space: O(N)   — adjacency list + visited[]
     */
    public int[] findRedundantConnectionDFS(int[][] edges) {
        int n = edges.length;
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i <= n; i++) adjacencyList.add(new ArrayList<>());

        int[] redundantEdge = null;

        for (int[] edge : edges) {
            int nodeA = edge[0];
            int nodeB = edge[1];

            // Check if nodeA and nodeB are already connected via DFS
            if (dfsIsReachable(adjacencyList, nodeA, nodeB, new boolean[n + 1])) {
                redundantEdge = edge;   // update (last one wins)
            } else {
                // Add edge to graph (it's not redundant yet)
                adjacencyList.get(nodeA).add(nodeB);
                adjacencyList.get(nodeB).add(nodeA);
            }
        }

        return redundantEdge != null ? redundantEdge : new int[0];
    }

    private boolean dfsIsReachable(
            List<List<Integer>> adj, int currentNode, int targetNode, boolean[] visited) {
        if (currentNode == targetNode) return true;
        visited[currentNode] = true;
        for (int neighbor : adj.get(currentNode)) {
            if (!visited[neighbor] && dfsIsReachable(adj, neighbor, targetNode, visited)) {
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // BONUS: Redundant Connection II (Directed Graph — LC 685)
    // =========================================================

    /**
     * LC 685: Directed graph variant.
     * In a rooted tree, each node has exactly one parent. One extra edge added.
     * Either:
     *   Case 1: Creates a node with two parents (in-degree = 2). No cycle.
     *   Case 2: Creates a cycle (all nodes still have one parent). No in-degree=2 node.
     *   Case 3: Both — a node has two parents AND there's a cycle.
     *
     * ALGORITHM:
     *   1. Find node with in-degree 2 (candidate1 = last edge to it, candidate2 = first).
     *   2. Try removing candidate1. If remaining edges form a valid tree → return candidate1.
     *   3. Else return candidate2 (or the cycle edge if no in-degree-2 node).
     *
     * Time:  O(N × α(N)) ≈ O(N)
     * Space: O(N)
     */
    public int[] findRedundantDirectedConnection(int[][] edges) {
        int n = edges.length;
        int[] parent = new int[n + 1];   // parent[v] = direct parent in the input tree
        for (int i = 1; i <= n; i++) parent[i] = i;

        int[] candidate1 = null;  // second edge pointing to node with in-degree 2
        int[] candidate2 = null;  // first edge pointing to node with in-degree 2
        int[] nodeParent = new int[n + 1];  // nodeParent[v] = who points to v

        // Step 1: Find node with two parents
        for (int[] edge : edges) {
            int from = edge[0], to = edge[1];
            if (nodeParent[to] == 0) {
                nodeParent[to] = from;
            } else {
                // Node 'to' already has a parent → in-degree 2
                candidate2 = new int[]{nodeParent[to], to};   // first edge to 'to'
                candidate1 = new int[]{from, to};             // second (current) edge to 'to'
            }
        }

        // Step 2: Try excluding candidate1 — use Union-Find on remaining edges
        for (int[] uf = new int[n + 1], rank = new int[n + 1], i = 1; i <= n; i++) uf[i] = i;
        // Re-initialize and apply Kruskal's logic with candidate1 excluded
        int[] unionFindArr = new int[n + 1];
        for (int i = 1; i <= n; i++) unionFindArr[i] = i;

        for (int[] edge : edges) {
            if (candidate1 != null && Arrays.equals(edge, candidate1)) continue;  // skip candidate1

            int from = edge[0], to = edge[1];
            int rootFrom = findDirected(unionFindArr, from);
            int rootTo   = findDirected(unionFindArr, to);

            if (rootFrom == rootTo) {
                // Cycle found even after removing candidate1 → candidate2 is not the answer
                // The cycle edge itself is the answer (no in-degree-2 case)
                return candidate2 == null ? edge : candidate2;
            }
            unionFindArr[rootTo] = rootFrom;
        }

        // No cycle after removing candidate1 → candidate1 is the redundant edge
        return candidate1 != null ? candidate1 : new int[0];
    }

    private int findDirected(int[] parent, int x) {
        if (parent[x] != x) parent[x] = findDirected(parent, parent[x]);
        return parent[x];
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Redundant_Connection solver = new Problem_2_Medium_Redundant_Connection();

        System.out.println("==============================================");
        System.out.println("  Redundant Connection — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Simple triangle ---
        System.out.println("\n--- Test 1: Triangle [[1,2],[1,3],[2,3]] ---");
        int[][] edges1 = {{1,2},{1,3},{2,3}};
        System.out.println("Union-Find: " + Arrays.toString(solver.findRedundantConnection(edges1)) + " (expected [2,3])");
        System.out.println("DFS:        " + Arrays.toString(solver.findRedundantConnectionDFS(edges1)));

        // --- Test 2: Longer chain with redundancy ---
        System.out.println("\n--- Test 2: [[1,2],[2,3],[3,4],[1,4],[1,5]] ---");
        int[][] edges2 = {{1,2},{2,3},{3,4},{1,4},{1,5}};
        System.out.println("Union-Find: " + Arrays.toString(solver.findRedundantConnection(edges2)) + " (expected [1,4])");
        System.out.println("DFS:        " + Arrays.toString(solver.findRedundantConnectionDFS(edges2)));

        // --- Test 3: Minimal case ---
        System.out.println("\n--- Test 3: 3 nodes, cycle 1-2-3-1 ---");
        int[][] edges3 = {{1,2},{2,3},{3,1}};
        System.out.println("Union-Find: " + Arrays.toString(solver.findRedundantConnection(edges3)) + " (expected [3,1])");

        // --- Test 4: LC 685 Directed Graph ---
        System.out.println("\n--- Test 4: Directed [[1,2],[1,3],[2,3]] ---");
        int[][] edges4 = {{1,2},{1,3},{2,3}};
        System.out.println("Directed: " + Arrays.toString(solver.findRedundantDirectedConnection(edges4)) + " (expected [2,3])");

        System.out.println("\n--- Test 5: Directed cycle only [[2,1],[3,1],[4,2],[1,4]] ---");
        int[][] edges5 = {{2,1},{3,1},{4,2},{1,4}};
        System.out.println("Directed: " + Arrays.toString(solver.findRedundantDirectedConnection(edges5)) + " (expected [2,1])");

        System.out.println("\n==============================================");
        System.out.println("  All Redundant Connection tests completed.");
        System.out.println("==============================================");
    }
}
