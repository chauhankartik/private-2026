/**
 * ============================================================
 *  PATTERN 7 — STRONGLY CONNECTED COMPONENTS (SCC) AND BRIDGES
 *  Problem 1 (Basic): Kosaraju's Two-Pass DFS   Classic
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a directed graph with V vertices and E edges, find all Strongly
 *    Connected Components (SCCs). An SCC is a maximal set of vertices such
 *    that there is a path from each vertex to every other vertex in the set.
 *
 *  EXAMPLE:
 *    Vertices: 0,1,2,3,4
 *    Edges: 0→1, 1→2, 2→0, 1→3, 3→4
 *    SCCs: {0,1,2}, {3}, {4}
 *
 *  CONSTRAINTS:
 *    1 <= V <= 10^4
 *    0 <= E <= 10^5
 *
 *  APPROACH 1: Kosaraju's Two-Pass DFS
 *    Time:  O(V + E)  — two full DFS traversals
 *    Space: O(V + E)  — reversed graph + finish-order stack
 *
 *  APPROACH 2: Condensation DAG (SCC compression) for downstream problems
 *    Time:  O(V + E)
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Kosaraju_Two_Pass_DFS {

    // =========================================================
    // APPROACH 1 — KOSARAJU'S ALGORITHM
    // =========================================================

    /**
     * Finds all SCCs using Kosaraju's two-pass DFS.
     *
     * PASS 1 — FINISH ORDER on ORIGINAL GRAPH:
     *   Run DFS on original graph. When DFS finishes a vertex, push it to a stack.
     *   Stack top = vertex with highest finish time = "can reach most things."
     *
     * REVERSE THE GRAPH:
     *   Flip all edge directions: edge u→v becomes v→u.
     *   In reversed graph: if u→v existed, now v can reach u.
     *
     * PASS 2 — DFS on REVERSED GRAPH in REVERSE FINISH ORDER:
     *   Pop vertices from stack. For each unvisited popped vertex:
     *   Run DFS on reversed graph. All vertices reachable = one SCC.
     *
     * WHY DOES IT WORK?
     *   The vertex with highest finish time can reach all others (in original).
     *   In the reversed graph, "all others reachable from u" = "all others that
     *   can reach u in the original" = u's SCC.
     *   The reversed graph's structure ensures we only explore u's own SCC
     *   (not SCCs that u can reach but are separate).
     *
     * @param totalVertices number of vertices (0-indexed)
     * @param adjacencyList original directed graph
     * @return list of SCCs, each SCC is a list of vertex indices
     *
     * Time:  O(V + E)
     * Space: O(V + E) — reversed graph + O(V) stack
     */
    public static List<List<Integer>> kosarajuSCC(
            int totalVertices, List<List<Integer>> adjacencyList) {

        if (totalVertices == 0) return new ArrayList<>();

        // ----- PASS 1: DFS on original graph, collect finish order -----
        boolean[] visitedPass1 = new boolean[totalVertices];
        Deque<Integer> finishOrderStack = new ArrayDeque<>();

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (!visitedPass1[startVertex]) {
                dfsPass1(adjacencyList, startVertex, visitedPass1, finishOrderStack);
            }
        }

        // ----- BUILD REVERSED GRAPH -----
        List<List<Integer>> reversedAdjacencyList = new ArrayList<>();
        for (int i = 0; i < totalVertices; i++) reversedAdjacencyList.add(new ArrayList<>());
        for (int fromVertex = 0; fromVertex < totalVertices; fromVertex++) {
            for (int toVertex : adjacencyList.get(fromVertex)) {
                reversedAdjacencyList.get(toVertex).add(fromVertex);  // reverse edge direction
            }
        }

        // ----- PASS 2: DFS on reversed graph in reverse finish order -----
        boolean[] visitedPass2 = new boolean[totalVertices];
        List<List<Integer>> allSCCs = new ArrayList<>();

        while (!finishOrderStack.isEmpty()) {
            int topVertex = finishOrderStack.pop();

            if (!visitedPass2[topVertex]) {
                List<Integer> currentSCC = new ArrayList<>();
                dfsPass2(reversedAdjacencyList, topVertex, visitedPass2, currentSCC);
                allSCCs.add(currentSCC);
            }
        }

        return allSCCs;
    }

    /**
     * Pass 1 DFS: pushes vertices to finishOrderStack when fully processed.
     * Iterative to avoid StackOverflowError on large graphs.
     */
    private static void dfsPass1(
            List<List<Integer>> adjacencyList,
            int startVertex,
            boolean[] visited,
            Deque<Integer> finishOrderStack) {

        Deque<int[]> dfsStack = new ArrayDeque<>(); // int[] = {vertex, neighborIndex}
        dfsStack.push(new int[]{startVertex, 0});
        visited[startVertex] = true;

        while (!dfsStack.isEmpty()) {
            int[] frame = dfsStack.peek();
            int currentVertex = frame[0];
            int neighborIndex = frame[1];

            List<Integer> neighbors = adjacencyList.get(currentVertex);
            if (neighborIndex < neighbors.size()) {
                frame[1]++;  // advance neighbor index
                int nextVertex = neighbors.get(neighborIndex);
                if (!visited[nextVertex]) {
                    visited[nextVertex] = true;
                    dfsStack.push(new int[]{nextVertex, 0});
                }
            } else {
                // All neighbors processed: vertex is finished
                dfsStack.pop();
                finishOrderStack.push(currentVertex);
            }
        }
    }

    /**
     * Pass 2 DFS: collects all vertices reachable from startVertex in reversed graph.
     * These form one SCC.
     */
    private static void dfsPass2(
            List<List<Integer>> reversedAdjList,
            int startVertex,
            boolean[] visited,
            List<Integer> currentSCC) {

        Deque<Integer> dfsStack = new ArrayDeque<>();
        dfsStack.push(startVertex);
        visited[startVertex] = true;

        while (!dfsStack.isEmpty()) {
            int currentVertex = dfsStack.pop();
            currentSCC.add(currentVertex);

            for (int neighborVertex : reversedAdjList.get(currentVertex)) {
                if (!visited[neighborVertex]) {
                    visited[neighborVertex] = true;
                    dfsStack.push(neighborVertex);
                }
            }
        }
    }

    // =========================================================
    // APPROACH 2 — CONDENSATION DAG BUILDER
    // =========================================================

    /**
     * Builds the Condensation DAG: each SCC is condensed into a single "super-node."
     * Edges between super-nodes preserve inter-SCC connections.
     *
     * USE CASES FOR CONDENSATION DAG:
     *   - Count the number of SCCs that have in-degree 0 in the condensation.
     *   - Determine if there's a single SCC that all others are reachable from.
     *   - Find the minimum number of edges to make a DAG strongly connected.
     *
     * @return condensation DAG as adjacency list of super-node indices
     */
    public static List<List<Integer>> buildCondensationDAG(
            int totalVertices,
            List<List<Integer>> adjacencyList,
            List<List<Integer>> allSCCs) {

        // Map each vertex to its SCC index
        int[] sccId = new int[totalVertices];
        for (int sccIndex = 0; sccIndex < allSCCs.size(); sccIndex++) {
            for (int vertex : allSCCs.get(sccIndex)) {
                sccId[vertex] = sccIndex;
            }
        }

        int totalSCCs = allSCCs.size();
        Set<Long> addedEdges = new HashSet<>();  // prevent duplicate edges in condensation
        List<List<Integer>> condensationDAG = new ArrayList<>();
        for (int i = 0; i < totalSCCs; i++) condensationDAG.add(new ArrayList<>());

        for (int fromVertex = 0; fromVertex < totalVertices; fromVertex++) {
            for (int toVertex : adjacencyList.get(fromVertex)) {
                int fromSCC = sccId[fromVertex];
                int toSCC   = sccId[toVertex];
                if (fromSCC != toSCC) {
                    long edgeKey = (long) fromSCC * totalSCCs + toSCC;
                    if (addedEdges.add(edgeKey)) {
                        condensationDAG.get(fromSCC).add(toSCC);
                    }
                }
            }
        }
        return condensationDAG;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Kosaraju's Two-Pass DFS — Test Suite");
        System.out.println("==============================================");

        // Helper to build directed adjacency list
        List<List<Integer>> buildGraph(int v, int[][] edges) {
            // (Java doesn't support local methods — using inline lambda concept)
            List<List<Integer>> adj = new ArrayList<>();
            for (int i = 0; i < v; i++) adj.add(new ArrayList<>());
            for (int[] e : edges) adj.get(e[0]).add(e[1]);
            return adj;
        }
        // We'll use a static helper instead:

        // --- Test 1: Classic SCC example ---
        System.out.println("\n--- Test 1: Classic graph with 3 SCCs ---");
        // SCCs: {0,1,2}, {3}, {4}
        int v1 = 5;
        int[][] e1 = {{0,1},{1,2},{2,0},{1,3},{3,4}};
        List<List<Integer>> g1 = buildDirected(v1, e1);
        List<List<Integer>> sccs1 = kosarajuSCC(v1, g1);
        System.out.println("Number of SCCs: " + sccs1.size() + " (expected 3)");
        sccs1.forEach(scc -> System.out.println("  SCC: " + scc));

        // --- Test 2: Fully connected (one big SCC) ---
        System.out.println("\n--- Test 2: All in one SCC (cycle through all) ---");
        int v2 = 4;
        int[][] e2 = {{0,1},{1,2},{2,3},{3,0}};
        List<List<Integer>> g2 = buildDirected(v2, e2);
        List<List<Integer>> sccs2 = kosarajuSCC(v2, g2);
        System.out.println("SCCs: " + sccs2.size() + " (expected 1)");
        sccs2.forEach(scc -> System.out.println("  SCC: " + scc));

        // --- Test 3: All isolated (no edges) ---
        System.out.println("\n--- Test 3: 3 isolated vertices ---");
        List<List<Integer>> g3 = buildDirected(3, new int[0][]);
        List<List<Integer>> sccs3 = kosarajuSCC(3, g3);
        System.out.println("SCCs: " + sccs3.size() + " (expected 3)");

        // --- Test 4: DAG (no SCCs larger than 1) ---
        System.out.println("\n--- Test 4: DAG (no backward edges) ---");
        int v4 = 4;
        int[][] e4 = {{0,1},{0,2},{1,3},{2,3}};
        List<List<Integer>> g4 = buildDirected(v4, e4);
        List<List<Integer>> sccs4 = kosarajuSCC(v4, g4);
        System.out.println("SCCs: " + sccs4.size() + " (expected 4: each vertex alone)");

        // --- Test 5: Condensation DAG ---
        System.out.println("\n--- Test 5: Condensation DAG from Test 1 ---");
        List<List<Integer>> condensation = buildCondensationDAG(v1, g1, sccs1);
        System.out.println("Condensation edges:");
        for (int i = 0; i < condensation.size(); i++) {
            if (!condensation.get(i).isEmpty())
                System.out.println("  SCC " + i + " → " + condensation.get(i));
        }

        System.out.println("\n==============================================");
        System.out.println("  All Kosaraju tests completed.");
        System.out.println("==============================================");
    }

    // Static helper since Java doesn't allow local methods
    private static List<List<Integer>> buildDirected(int v, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < v; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);
        return adj;
    }
}
