/**
 * ============================================================
 *  PATTERN 7 — STRONGLY CONNECTED COMPONENTS (SCC) AND BRIDGES
 *  Problem 2 (Medium): Tarjan's SCC Algorithm   Classic
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Find all Strongly Connected Components in a directed graph using
 *    Tarjan's single-pass DFS algorithm with low-link values.
 *
 *  CONSTRAINTS:
 *    1 <= V <= 10^4
 *    0 <= E <= 10^5
 *
 *  APPROACH 1: Tarjan's SCC — single DFS pass with explicit stack
 *    Time:  O(V + E)  — single DFS traversal
 *    Space: O(V)      — discoveryTime[], lowLink[], onStack[], tarjanStack
 *
 *  APPROACH 2: Recursive Tarjan's (cleaner but risks StackOverflow for large V)
 *    Time:  O(V + E)
 *    Space: O(V + E)  — recursion stack + data arrays
 *
 *  TARJAN'S vs KOSARAJU'S:
 *    Tarjan: single DFS pass, O(V) extra space, more complex code.
 *    Kosaraju: two DFS passes + reversed graph (O(V+E) extra), simpler code.
 *    Both: O(V + E) total. Tarjan's is cache-friendlier (single pass).
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Tarjan_SCC {

    // =========================================================
    // APPROACH 1 — TARJAN'S ITERATIVE (SAFE FOR LARGE GRAPHS)
    // =========================================================

    /**
     * Tarjan's SCC using iterative DFS.
     * Safe for large V without StackOverflowError.
     *
     * KEY DATA STRUCTURES:
     *   discoveryTime[v]: DFS timestamp when v was first visited.
     *   lowLink[v]: minimum discoveryTime reachable from v's subtree
     *               (through tree edges and back edges — but NOT cross edges to other SCCs).
     *   onStack[v]: whether v is currently on the Tarjan SCC stack.
     *   tarjanStack: separate stack storing vertices of the current potential SCC.
     *
     * SCC ROOT CONDITION:
     *   Vertex v is the root of an SCC if: lowLink[v] == discoveryTime[v]
     *   (nothing in v's subtree has a back edge to an ancestor of v).
     *   When this condition is met, pop all vertices from tarjanStack down to v
     *   → they form one SCC.
     *
     * @param totalVertices number of vertices
     * @param adjacencyList directed graph
     * @return list of SCCs
     *
     * Time:  O(V + E)
     * Space: O(V)
     */
    public List<List<Integer>> tarjanSCC(int totalVertices, List<List<Integer>> adjacencyList) {
        int[] discoveryTime = new int[totalVertices];
        int[] lowLink       = new int[totalVertices];
        boolean[] onStack   = new boolean[totalVertices];
        Arrays.fill(discoveryTime, -1);  // -1 = unvisited

        Deque<Integer> tarjanStack = new ArrayDeque<>();
        List<List<Integer>> allSCCs = new ArrayList<>();
        int[] timer = {0};

        for (int startVertex = 0; startVertex < totalVertices; startVertex++) {
            if (discoveryTime[startVertex] == -1) {
                tarjanDFSIterative(adjacencyList, startVertex,
                    discoveryTime, lowLink, onStack, tarjanStack, allSCCs, timer);
            }
        }

        return allSCCs;
    }

    /**
     * Iterative Tarjan's DFS using an explicit frame stack to simulate recursion.
     * Each frame stores: {currentVertex, neighborIndex}.
     * On frame entry: assign discoveryTime and lowLink, push to tarjanStack.
     * On neighbor processing: update lowLink from neighbor's lowLink (post-DFS).
     * On frame exit: check if SCC root → pop and collect SCC.
     */
    private void tarjanDFSIterative(
            List<List<Integer>> adjacencyList,
            int startVertex,
            int[] discoveryTime, int[] lowLink,
            boolean[] onStack,
            Deque<Integer> tarjanStack,
            List<List<Integer>> allSCCs,
            int[] timer) {

        // Frame: {vertex, neighborIteratorIndex}
        Deque<int[]> callStack = new ArrayDeque<>();

        // Initialize startVertex
        discoveryTime[startVertex] = lowLink[startVertex] = timer[0]++;
        onStack[startVertex] = true;
        tarjanStack.push(startVertex);
        callStack.push(new int[]{startVertex, 0});

        while (!callStack.isEmpty()) {
            int[] frame = callStack.peek();
            int currentVertex = frame[0];
            int neighborIdx   = frame[1];

            List<Integer> neighbors = adjacencyList.get(currentVertex);

            if (neighborIdx < neighbors.size()) {
                frame[1]++;  // advance to next neighbor
                int neighborVertex = neighbors.get(neighborIdx);

                if (discoveryTime[neighborVertex] == -1) {
                    // Tree edge: recurse into neighbor
                    discoveryTime[neighborVertex] = lowLink[neighborVertex] = timer[0]++;
                    onStack[neighborVertex] = true;
                    tarjanStack.push(neighborVertex);
                    callStack.push(new int[]{neighborVertex, 0});
                } else if (onStack[neighborVertex]) {
                    // Back edge to a vertex still on the Tarjan stack
                    // Update lowLink (use discoveryTime, NOT lowLink of neighbor)
                    lowLink[currentVertex] = Math.min(lowLink[currentVertex], discoveryTime[neighborVertex]);
                }
                // Cross edges (onStack[neighbor] == false) are ignored:
                // they point to already-completed SCCs — don't update lowLink
            } else {
                // All neighbors processed — finalize this vertex
                callStack.pop();

                // Propagate lowLink up to parent
                if (!callStack.isEmpty()) {
                    int parentVertex = callStack.peek()[0];
                    lowLink[parentVertex] = Math.min(lowLink[parentVertex], lowLink[currentVertex]);
                }

                // Check if currentVertex is the root of an SCC
                if (lowLink[currentVertex] == discoveryTime[currentVertex]) {
                    List<Integer> currentSCC = new ArrayList<>();
                    while (true) {
                        int poppedVertex = tarjanStack.pop();
                        onStack[poppedVertex] = false;
                        currentSCC.add(poppedVertex);
                        if (poppedVertex == currentVertex) break;
                    }
                    allSCCs.add(currentSCC);
                }
            }
        }
    }

    // =========================================================
    // APPROACH 2 — RECURSIVE TARJAN'S (CLEANER, EDUCATIONAL)
    // =========================================================

    private int[] discoveryTimeRec;
    private int[] lowLinkRec;
    private boolean[] onStackRec;
    private Deque<Integer> tarjanStackRec;
    private List<List<Integer>> allSCCsRec;
    private int timerRec;

    /**
     * Recursive Tarjan's SCC. More readable but risks StackOverflowError for V > 10^4.
     * Use for educational purposes or small graphs (V ≤ 5000).
     *
     * Time:  O(V + E)
     * Space: O(V + E)  — recursion stack depth up to V
     */
    public List<List<Integer>> tarjanSCCRecursive(int totalVertices, List<List<Integer>> adjacencyList) {
        discoveryTimeRec = new int[totalVertices];
        lowLinkRec       = new int[totalVertices];
        onStackRec       = new boolean[totalVertices];
        tarjanStackRec   = new ArrayDeque<>();
        allSCCsRec       = new ArrayList<>();
        timerRec         = 0;
        Arrays.fill(discoveryTimeRec, -1);

        for (int v = 0; v < totalVertices; v++) {
            if (discoveryTimeRec[v] == -1) {
                tarjanRecursive(adjacencyList, v);
            }
        }
        return allSCCsRec;
    }

    private void tarjanRecursive(List<List<Integer>> adj, int v) {
        discoveryTimeRec[v] = lowLinkRec[v] = timerRec++;
        tarjanStackRec.push(v);
        onStackRec[v] = true;

        for (int neighbor : adj.get(v)) {
            if (discoveryTimeRec[neighbor] == -1) {
                tarjanRecursive(adj, neighbor);
                lowLinkRec[v] = Math.min(lowLinkRec[v], lowLinkRec[neighbor]);
            } else if (onStackRec[neighbor]) {
                lowLinkRec[v] = Math.min(lowLinkRec[v], discoveryTimeRec[neighbor]);
            }
        }

        // If v is SCC root: pop the SCC
        if (lowLinkRec[v] == discoveryTimeRec[v]) {
            List<Integer> scc = new ArrayList<>();
            while (true) {
                int popped = tarjanStackRec.pop();
                onStackRec[popped] = false;
                scc.add(popped);
                if (popped == v) break;
            }
            allSCCsRec.add(scc);
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Tarjan_SCC solver = new Problem_2_Medium_Tarjan_SCC();
        System.out.println("==============================================");
        System.out.println("  Tarjan's SCC Algorithm — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic 3-SCC graph ---
        System.out.println("\n--- Test 1: 3 SCCs: {0,1,2}, {3}, {4} ---");
        List<List<Integer>> g1 = buildDirected(5, new int[][]{{0,1},{1,2},{2,0},{1,3},{3,4}});
        List<List<Integer>> sccs1 = solver.tarjanSCC(5, g1);
        System.out.println("Iterative SCCs (" + sccs1.size() + " SCCs):");
        sccs1.forEach(scc -> System.out.println("  " + scc));
        List<List<Integer>> sccs1r = solver.tarjanSCCRecursive(5, g1);
        System.out.println("Recursive SCCs (" + sccs1r.size() + " SCCs):");
        sccs1r.forEach(scc -> System.out.println("  " + scc));

        // --- Test 2: One big SCC (cycle) ---
        System.out.println("\n--- Test 2: One SCC (all in a cycle) ---");
        List<List<Integer>> g2 = buildDirected(4, new int[][]{{0,1},{1,2},{2,3},{3,0}});
        List<List<Integer>> sccs2 = solver.tarjanSCC(4, g2);
        System.out.println("SCCs: " + sccs2.size() + " (expected 1): " + sccs2);

        // --- Test 3: DAG only ---
        System.out.println("\n--- Test 3: DAG (4 singleton SCCs) ---");
        List<List<Integer>> g3 = buildDirected(4, new int[][]{{0,1},{1,2},{2,3}});
        System.out.println("SCCs: " + solver.tarjanSCC(4, g3).size() + " (expected 4)");

        // --- Test 4: Complex graph ---
        System.out.println("\n--- Test 4: 8-vertex complex graph ---");
        // Two SCCs: {0,1,2,3} and {4,5,6,7}
        int[][] e4 = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{1,4}};
        List<List<Integer>> g4 = buildDirected(8, e4);
        List<List<Integer>> sccs4 = solver.tarjanSCC(8, g4);
        System.out.println("SCCs: " + sccs4.size() + " (expected 2)");
        sccs4.forEach(scc -> System.out.println("  " + scc));

        // --- Test 5: Single vertex ---
        System.out.println("\n--- Test 5: Single vertex ---");
        List<List<Integer>> g5 = buildDirected(1, new int[0][]);
        System.out.println("SCCs: " + solver.tarjanSCC(1, g5).size() + " (expected 1)");

        System.out.println("\n==============================================");
        System.out.println("  All Tarjan's SCC tests completed.");
        System.out.println("==============================================");
    }

    private static List<List<Integer>> buildDirected(int v, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < v; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);
        return adj;
    }
}
