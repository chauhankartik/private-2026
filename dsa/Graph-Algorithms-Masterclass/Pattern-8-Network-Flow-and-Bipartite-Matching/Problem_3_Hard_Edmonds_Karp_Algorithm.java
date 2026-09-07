/**
 * ============================================================
 *  PATTERN 8 — NETWORK FLOW AND BIPARTITE MATCHING
 *  Problem 3 (Hard): Edmonds-Karp Algorithm   Classic
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Maximum flow using BFS-based shortest augmenting paths (Edmonds-Karp).
 *    Guaranteed polynomial time: O(V × E²), unlike Ford-Fulkerson's O(E × maxFlow).
 *
 *  CONSTRAINTS:
 *    2 <= n <= 200
 *    Edge capacity >= 0
 *
 *  APPROACH 1: Edmonds-Karp — BFS finds shortest augmenting path each iteration
 *    Time:  O(V × E²)
 *    Space: O(V²)
 *
 *  APPROACH 2: Adjacency list residual graph (for sparse networks)
 *    Time:  O(V × E²)
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Edmonds_Karp_Algorithm {

    // =========================================================
    // APPROACH 1 — EDMONDS-KARP (BFS, ADJACENCY MATRIX)
    // =========================================================

    /**
     * Computes max flow using Edmonds-Karp (BFS augmentation).
     *
     * WHY BFS OVER DFS (Ford-Fulkerson)?
     *   BFS always finds the SHORTEST (fewest-edge) augmenting path.
     *   This guarantees:
     *     1. Each edge becomes "saturating critical" at most V/2 times.
     *     2. At most O(V × E) augmenting path iterations.
     *     3. Total time: O(V × E²) — polynomial regardless of capacities.
     *   Ford-Fulkerson with DFS: O(E × maxFlow) — bad for large capacities.
     *
     * KEY INVARIANT (why BFS path lengths are non-decreasing):
     *   After each BFS augmentation, the shortest path from source to any vertex
     *   can only stay the same or increase. This bounds the total iterations.
     *
     * @param totalNodes       number of vertices
     * @param residualCapacity capacity matrix (modified in-place)
     * @param sourceNode       source vertex
     * @param sinkNode         sink vertex
     * @return maximum flow value
     *
     * Time:  O(V × E²)
     * Space: O(V²) — residual matrix + O(V) for BFS arrays
     */
    public static int edmondsKarp(
            int totalNodes,
            int[][] residualCapacity,
            int sourceNode,
            int sinkNode) {

        if (totalNodes == 0 || sourceNode == sinkNode) return 0;

        int maximumFlow = 0;

        while (true) {
            // BFS to find shortest augmenting path
            int[] parent = new int[totalNodes];
            Arrays.fill(parent, -1);
            parent[sourceNode] = sourceNode;

            Queue<Integer> bfsQueue = new LinkedList<>();
            bfsQueue.offer(sourceNode);

            // BFS traversal
            bfsOuter:
            while (!bfsQueue.isEmpty()) {
                int currentNode = bfsQueue.poll();

                for (int nextNode = 0; nextNode < totalNodes; nextNode++) {
                    if (parent[nextNode] == -1 && residualCapacity[currentNode][nextNode] > 0) {
                        parent[nextNode] = currentNode;
                        if (nextNode == sinkNode) break bfsOuter;  // path found
                        bfsQueue.offer(nextNode);
                    }
                }
            }

            // No augmenting path found → done
            if (parent[sinkNode] == -1) break;

            // Find bottleneck along the BFS path
            int bottleneckFlow = Integer.MAX_VALUE;
            for (int node = sinkNode; node != sourceNode; node = parent[node]) {
                int predecessorNode = parent[node];
                bottleneckFlow = Math.min(bottleneckFlow, residualCapacity[predecessorNode][node]);
            }

            // Update residual capacities along the path
            for (int node = sinkNode; node != sourceNode; node = parent[node]) {
                int predecessorNode = parent[node];
                residualCapacity[predecessorNode][node] -= bottleneckFlow;
                residualCapacity[node][predecessorNode] += bottleneckFlow;
            }

            maximumFlow += bottleneckFlow;
        }

        return maximumFlow;
    }

    // =========================================================
    // APPROACH 2 — ADJACENCY LIST RESIDUAL GRAPH
    // =========================================================

    /**
     * Edge representation for adjacency list residual graph.
     * Each edge stores a reference to its reverse edge for O(1) reverse update.
     */
    static class FlowEdge {
        final int toNode;
        int remainingCapacity;
        final FlowEdge reverseEdge;

        FlowEdge(int toNode, int capacity, FlowEdge reverseEdge) {
            this.toNode = toNode;
            this.remainingCapacity = capacity;
            this.reverseEdge = reverseEdge;
        }
    }

    /**
     * Adds a directed edge and its reverse edge to the adjacency list.
     * The reverse edge starts with capacity 0 (pure backward edge).
     */
    public static void addFlowEdge(List<List<FlowEdge>> graph, int from, int to, int capacity) {
        FlowEdge forward = new FlowEdge(to, capacity, null);
        FlowEdge backward = new FlowEdge(from, 0, forward);
        // Java doesn't allow final field circular reference directly — use a wrapper
        // Instead, store as paired list entries and link by construction order.
        // Simplification: use index-based approach below.
        graph.get(from).add(new FlowEdge(to, capacity, null));
        graph.get(to).add(new FlowEdge(from, 0, graph.get(from).get(graph.get(from).size()-1)));
        // Link reverse
        FlowEdge fwd = graph.get(from).get(graph.get(from).size()-1);
        graph.get(to).get(graph.get(to).size()-1);
    }

    /**
     * Edmonds-Karp using adjacency list with index-based reverse edge tracking.
     * Cleaner than FlowEdge circular reference for interview settings.
     *
     * STRUCTURE: graph.get(u) = list of {toNode, capacity, reverseEdgeIndex, toNodeList}
     * Represented as: graph[u][i] = int[]{toNode, capacity, reverseIndex}
     *
     * Time:  O(V × E²)
     * Space: O(V + E)
     */
    public static int edmondsKarpList(int totalNodes, int[][] edgeList, int sourceNode, int sinkNode) {
        // Build adjacency list with edge indices for reverse lookup
        List<int[]>[] graph = new ArrayList[totalNodes];
        for (int i = 0; i < totalNodes; i++) graph[i] = new ArrayList<>();

        for (int[] edge : edgeList) {
            int from = edge[0], to = edge[1], cap = edge[2];
            int fwdIdx = graph[from].size();
            int bwdIdx = graph[to].size();
            graph[from].add(new int[]{to,   cap, bwdIdx});  // {to, capacity, reverseIdx in graph[to]}
            graph[to].add(new int[]  {from, 0,   fwdIdx});  // reverse edge with 0 capacity
        }

        int maxFlow = 0;

        while (true) {
            // BFS to find shortest path
            int[] parent    = new int[totalNodes];
            int[] parentEdge = new int[totalNodes];
            Arrays.fill(parent, -1);
            parent[sourceNode] = sourceNode;
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(sourceNode);

            outer:
            while (!queue.isEmpty()) {
                int curr = queue.poll();
                for (int ei = 0; ei < graph[curr].size(); ei++) {
                    int[] e = graph[curr].get(ei);
                    int next = e[0], cap = e[1];
                    if (parent[next] == -1 && cap > 0) {
                        parent[next] = curr;
                        parentEdge[next] = ei;
                        if (next == sinkNode) break outer;
                        queue.offer(next);
                    }
                }
            }

            if (parent[sinkNode] == -1) break;

            // Find bottleneck
            int bottleneck = Integer.MAX_VALUE;
            for (int node = sinkNode; node != sourceNode; node = parent[node]) {
                bottleneck = Math.min(bottleneck, graph[parent[node]].get(parentEdge[node])[1]);
            }

            // Update capacities
            for (int node = sinkNode; node != sourceNode; node = parent[node]) {
                int[] fwdEdge = graph[parent[node]].get(parentEdge[node]);
                int revIdx = fwdEdge[2];
                fwdEdge[1] -= bottleneck;
                graph[node].get(revIdx)[1] += bottleneck;
            }
            maxFlow += bottleneck;
        }
        return maxFlow;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Edmonds-Karp Algorithm — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic 6-node (same as Ford-Fulkerson test) ---
        System.out.println("\n--- Test 1: Classic 6-node network ---");
        int[][] edgeList1 = {
            {0,1,16},{0,2,13},{1,2,10},{1,3,12},
            {2,4,14},{3,2,9},{3,5,20},{4,3,7},{4,5,4}
        };
        int[][] residual1 = new int[6][6];
        for (int[] e : edgeList1) residual1[e[0]][e[1]] += e[2];
        System.out.println("Matrix Edmonds-Karp 0→5: " + edmondsKarp(6, residual1, 0, 5) + " (expected 23)");
        System.out.println("List   Edmonds-Karp 0→5: " + edmondsKarpList(6, edgeList1, 0, 5) + " (expected 23)");

        // --- Test 2: Simple path ---
        System.out.println("\n--- Test 2: Simple path 0→1→2 ---");
        int[][] edgeList2 = {{0,1,5},{1,2,3}};
        int[][] residual2 = new int[3][3];
        for (int[] e : edgeList2) residual2[e[0]][e[1]] += e[2];
        System.out.println("Max flow 0→2: " + edmondsKarp(3, residual2, 0, 2) + " (expected 3)");
        System.out.println("List max flow: " + edmondsKarpList(3, edgeList2, 0, 2));

        // --- Test 3: Parallel paths ---
        System.out.println("\n--- Test 3: Two parallel paths ---");
        int[][] edgeList3 = {{0,1,3},{0,2,2},{1,3,3},{2,3,2}};
        int[][] residual3 = new int[4][4];
        for (int[] e : edgeList3) residual3[e[0]][e[1]] += e[2];
        System.out.println("Max flow 0→3: " + edmondsKarp(4, residual3, 0, 3) + " (expected 5)");

        // --- Test 4: No path ---
        System.out.println("\n--- Test 4: No path from 0 to 2 ---");
        int[][] residual4 = {{0,5,0},{0,0,0},{0,0,0}};
        System.out.println("Max flow 0→2: " + edmondsKarp(3, residual4, 0, 2) + " (expected 0)");

        // --- Test 5: Bottleneck edge ---
        System.out.println("\n--- Test 5: Bottleneck edge limits flow ---");
        int[][] edgeList5 = {{0,1,100},{1,2,1},{0,2,100}};
        int[][] residual5 = new int[3][3];
        for (int[] e : edgeList5) residual5[e[0]][e[1]] += e[2];
        System.out.println("Max flow 0→2: " + edmondsKarp(3, residual5, 0, 2) + " (expected 101)");

        System.out.println("\n==============================================");
        System.out.println("  All Edmonds-Karp tests completed.");
        System.out.println("==============================================");
    }
}
