/**
 * ============================================================
 *  PATTERN 7 — SCC AND BRIDGES
 *  Problem 3 (Hard): Critical Connections / Bridges   LC 1192
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a network of n servers and connections (undirected edges), find all
 *    "critical connections" — edges whose removal disconnects the network.
 *    These are also called "bridges" in graph theory.
 *
 *  EXAMPLE:
 *    n=4, connections=[[0,1],[1,2],[2,0],[1,3]]
 *    Output: [[1,3]]  (removing [1,3] disconnects server 3)
 *
 *  CONSTRAINTS:
 *    2 <= n <= 10^5
 *    n-1 <= connections.length <= 10^5
 *    Each connection is unique (no parallel edges here, but we handle them)
 *
 *  APPROACH 1: Tarjan's Bridge-Finding — track lowLink and discoveryTime
 *    Time:  O(V + E)
 *    Space: O(V + E)
 *
 *  APPROACH 2: Also find Articulation Points (vertices whose removal disconnects graph)
 *    Time:  O(V + E)
 *    Space: O(V)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Critical_Connections_Bridges {

    // =========================================================
    // APPROACH 1 — TARJAN'S BRIDGE FINDING
    // =========================================================

    /**
     * Finds all bridges in an undirected graph using Tarjan's algorithm.
     *
     * BRIDGE CONDITION:
     *   Edge (u, v) where v is a DFS child of u is a bridge if:
     *   lowLink[v] > discoveryTime[u]
     *
     *   This means: the subtree rooted at v has NO back edge reaching u or
     *   any ancestor of u. Removing (u, v) disconnects v's subtree.
     *
     * LOWLINK UPDATE RULE:
     *   lowLink[u] = min(discoveryTime[u],
     *                    discoveryTime[w] for all back edges u→w,
     *                    lowLink[child] for all tree-edge children)
     *
     * EDGE ID TRICK (handling parallel edges):
     *   Standard problems (LC 1192) have no parallel edges.
     *   If parallel edges exist: use edge IDs. When traversing from u,
     *   skip the edge with the SAME ID as the edge we came from (parentEdgeId).
     *   DON'T skip by parent vertex — that incorrectly skips both parallel edges.
     *
     * @param totalServers   total vertices
     * @param connections    undirected edge list
     * @return list of bridges (critical connections)
     *
     * Time:  O(V + E)
     * Space: O(V + E) — adjacency list (with edge IDs) + recursive DFS stack
     */
    public List<List<Integer>> criticalConnections(int totalServers, List<List<Integer>> connections) {
        // Build adjacency list with edge IDs: adj.get(u) = list of {v, edgeId}
        List<List<int[]>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < totalServers; i++) adjacencyList.add(new ArrayList<>());

        int edgeId = 0;
        for (List<Integer> connection : connections) {
            int serverA = connection.get(0);
            int serverB = connection.get(1);
            adjacencyList.get(serverA).add(new int[]{serverB, edgeId});
            adjacencyList.get(serverB).add(new int[]{serverA, edgeId});
            edgeId++;
        }

        int[] discoveryTime = new int[totalServers];
        int[] lowLink       = new int[totalServers];
        Arrays.fill(discoveryTime, -1);

        List<List<Integer>> bridges = new ArrayList<>();
        int[] timer = {0};

        for (int startServer = 0; startServer < totalServers; startServer++) {
            if (discoveryTime[startServer] == -1) {
                dfsBridges(adjacencyList, startServer, -1, discoveryTime, lowLink, bridges, timer);
            }
        }

        return bridges;
    }

    /**
     * Recursive DFS for bridge detection.
     * @param parentEdgeId  the edge ID we came from (to avoid going back on the same edge)
     */
    private void dfsBridges(
            List<List<int[]>> adjacencyList,
            int currentServer,
            int parentEdgeId,
            int[] discoveryTime, int[] lowLink,
            List<List<Integer>> bridges,
            int[] timer) {

        discoveryTime[currentServer] = lowLink[currentServer] = timer[0]++;

        for (int[] neighborEdge : adjacencyList.get(currentServer)) {
            int neighborServer = neighborEdge[0];
            int currentEdgeId  = neighborEdge[1];

            if (currentEdgeId == parentEdgeId) continue;  // skip the edge we came from

            if (discoveryTime[neighborServer] == -1) {
                // Tree edge: recurse
                dfsBridges(adjacencyList, neighborServer, currentEdgeId,
                    discoveryTime, lowLink, bridges, timer);
                // Update lowLink from child
                lowLink[currentServer] = Math.min(lowLink[currentServer], lowLink[neighborServer]);

                // BRIDGE CHECK: if child can't reach current or its ancestors
                if (lowLink[neighborServer] > discoveryTime[currentServer]) {
                    bridges.add(Arrays.asList(currentServer, neighborServer));
                }
            } else {
                // Back edge: update lowLink
                lowLink[currentServer] = Math.min(lowLink[currentServer], discoveryTime[neighborServer]);
            }
        }
    }

    // =========================================================
    // APPROACH 2 — ARTICULATION POINTS
    // =========================================================

    /**
     * Finds all articulation points (cut vertices) in an undirected graph.
     *
     * ARTICULATION POINT CONDITIONS:
     *   1. Root of DFS tree with >= 2 children in the DFS tree.
     *   2. Non-root vertex u where a child v satisfies: lowLink[v] >= discoveryTime[u]
     *      (v's subtree can't bypass u to reach u's ancestors)
     *
     * NOTE: >= (not >) — this is different from bridge condition (which uses >).
     *   Bridge: lowLink[child] > discoveryTime[parent] → edge is critical.
     *   AP:     lowLink[child] >= discoveryTime[parent] → vertex is critical.
     *   If = , the back edge reaches exactly u (not above) → removing u still disconnects.
     *
     * @return set of articulation point vertex indices
     *
     * Time:  O(V + E)
     * Space: O(V)
     */
    public Set<Integer> findArticulationPoints(int totalVertices, List<List<Integer>> adjacencyList) {
        int[] discoveryTime = new int[totalVertices];
        int[] lowLink       = new int[totalVertices];
        boolean[] isAP      = new boolean[totalVertices];
        Arrays.fill(discoveryTime, -1);
        int[] timer = {0};

        for (int v = 0; v < totalVertices; v++) {
            if (discoveryTime[v] == -1) {
                dfsAP(adjacencyList, v, -1, discoveryTime, lowLink, isAP, timer);
            }
        }

        Set<Integer> articulationPoints = new LinkedHashSet<>();
        for (int v = 0; v < totalVertices; v++) {
            if (isAP[v]) articulationPoints.add(v);
        }
        return articulationPoints;
    }

    private void dfsAP(
            List<List<Integer>> adj, int u, int parent,
            int[] disc, int[] low, boolean[] isAP, int[] timer) {
        disc[u] = low[u] = timer[0]++;
        int childCount = 0;

        for (int v : adj.get(u)) {
            if (v == parent) continue;
            if (disc[v] == -1) {
                childCount++;
                dfsAP(adj, v, u, disc, low, isAP, timer);
                low[u] = Math.min(low[u], low[v]);
                // AP condition 1: root with multiple DFS children
                if (parent == -1 && childCount > 1) isAP[u] = true;
                // AP condition 2: non-root, child can't bypass u
                if (parent != -1 && low[v] >= disc[u]) isAP[u] = true;
            } else {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Critical_Connections_Bridges solver =
            new Problem_3_Hard_Critical_Connections_Bridges();

        System.out.println("==============================================");
        System.out.println("  Critical Connections (Bridges) — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: LC 1192 Example ---
        System.out.println("\n--- Test 1: LC 1192 n=4, [[0,1],[1,2],[2,0],[1,3]] ---");
        List<List<Integer>> conn1 = new ArrayList<>();
        for (int[] e : new int[][]{{0,1},{1,2},{2,0},{1,3}})
            conn1.add(Arrays.asList(e[0], e[1]));
        List<List<Integer>> bridges1 = solver.criticalConnections(4, conn1);
        System.out.println("Bridges: " + bridges1 + " (expected [[1,3]])");

        // --- Test 2: Simple bridge ---
        System.out.println("\n--- Test 2: Two components connected by one bridge ---");
        // 0-1-2-0 (cycle), 2-3-4 (chain)
        List<List<Integer>> conn2 = new ArrayList<>();
        for (int[] e : new int[][]{{0,1},{1,2},{0,2},{2,3},{3,4}})
            conn2.add(Arrays.asList(e[0], e[1]));
        List<List<Integer>> bridges2 = solver.criticalConnections(5, conn2);
        System.out.println("Bridges: " + bridges2 + " (expected [2,3] and [3,4])");

        // --- Test 3: No bridges (all in one 2-edge-connected component) ---
        System.out.println("\n--- Test 3: 3-cycle, no bridges ---");
        List<List<Integer>> conn3 = new ArrayList<>();
        for (int[] e : new int[][]{{0,1},{1,2},{0,2}})
            conn3.add(Arrays.asList(e[0], e[1]));
        System.out.println("Bridges: " + solver.criticalConnections(3, conn3) + " (expected [])");

        // --- Test 4: Path graph (all edges are bridges) ---
        System.out.println("\n--- Test 4: Path 0-1-2-3 (all bridges) ---");
        List<List<Integer>> conn4 = new ArrayList<>();
        for (int[] e : new int[][]{{0,1},{1,2},{2,3}})
            conn4.add(Arrays.asList(e[0], e[1]));
        System.out.println("Bridges: " + solver.criticalConnections(4, conn4) + " (expected 3 bridges)");

        // --- Test 5: Articulation Points ---
        System.out.println("\n--- Test 5: Articulation points in bridge graph ---");
        List<List<Integer>> adj5 = new ArrayList<>();
        for (int i = 0; i < 5; i++) adj5.add(new ArrayList<>());
        for (int[] e : new int[][]{{0,1},{1,2},{0,2},{2,3},{3,4}}) {
            adj5.get(e[0]).add(e[1]); adj5.get(e[1]).add(e[0]);
        }
        System.out.println("Articulation points: " + solver.findArticulationPoints(5, adj5));
        System.out.println("(Expected: 2 and 3 — removing either disconnects)");

        System.out.println("\n==============================================");
        System.out.println("  All Critical Connections tests completed.");
        System.out.println("==============================================");
    }
}
