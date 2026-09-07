/**
 * ============================================================
 *  PATTERN 5 — MINIMUM SPANNING TREES (MST)
 *  Problem 2 (Medium): Prim's Algorithm — Dense Graph   Classic
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a connected undirected weighted graph, find the MST using Prim's algorithm.
 *    Implement both the O(V²) matrix version (optimal for dense graphs) and
 *    the O(E log V) heap version (optimal for sparse graphs).
 *
 *  CONSTRAINTS:
 *    2 <= V <= 1000  (matrix Prim's optimal here)
 *    V-1 <= E <= V*(V-1)/2 (dense graph)
 *
 *  APPROACH 1: Prim's with adjacency matrix — O(V²) "cheapest neighbor" scan
 *    Time:  O(V²)
 *    Space: O(V²)  — matrix storage + cheapestEdge[] + inMST[]
 *
 *  APPROACH 2: Prim's with min-heap (adjacency list) — O(E log V)
 *    Time:  O(E log V)
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Prims_Dense_Graph {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    // =========================================================
    // APPROACH 1 — PRIM'S WITH ADJACENCY MATRIX (O(V²))
    // =========================================================

    /**
     * Prim's MST using an adjacency matrix — optimal for dense graphs.
     *
     * ALGORITHM:
     *   1. Start from vertex 0. Mark it as inMST.
     *   2. cheapestEdge[v] = minimum edge weight from the MST to vertex v.
     *   3. Repeat V-1 times:
     *      a. Find the unincluded vertex u with the MINIMUM cheapestEdge[u].
     *         (This is the O(V) scan per iteration → O(V²) total.)
     *      b. Add u to MST. Add cheapestEdge[u] to total weight.
     *      c. Update cheapestEdge[] for all neighbors of u.
     *
     * COMPARISON WITH KRUSKAL'S:
     *   Kruskal's: global edge sort + union-find → O(E log E), better for sparse graphs.
     *   Prim's (matrix): O(V²) → better when E ≈ V² (dense graphs, E >> V log V).
     *   Prim's (heap): O(E log V) → matches Kruskal for sparse graphs but simpler code.
     *
     * @param costMatrix V×V adjacency matrix; costMatrix[u][v] = edge weight or INFINITY
     * @return MSTResult with total weight and list of MST edges selected
     *
     * Time:  O(V²)
     * Space: O(V)  — cheapestEdge[], inMST[], mstParent[]
     */
    public static int primsMSTMatrix(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0) return -1;
        int totalVertices = costMatrix.length;

        int[] cheapestEdgeToMST = new int[totalVertices];    // min edge weight to connect v to MST
        int[] mstParent          = new int[totalVertices];   // which MST vertex provides the cheapest edge
        boolean[] inMST          = new boolean[totalVertices];

        Arrays.fill(cheapestEdgeToMST, INFINITY);
        Arrays.fill(mstParent, -1);

        cheapestEdgeToMST[0] = 0;   // start from vertex 0: cost 0 to include it
        int totalMSTWeight = 0;

        for (int iteration = 0; iteration < totalVertices; iteration++) {
            // Find the cheapest non-MST vertex (O(V) scan)
            int selectedVertex = -1;
            for (int vertex = 0; vertex < totalVertices; vertex++) {
                if (!inMST[vertex] && (selectedVertex == -1 ||
                    cheapestEdgeToMST[vertex] < cheapestEdgeToMST[selectedVertex])) {
                    selectedVertex = vertex;
                }
            }

            if (cheapestEdgeToMST[selectedVertex] == INFINITY) {
                return -1;  // graph is disconnected
            }

            inMST[selectedVertex] = true;
            totalMSTWeight += cheapestEdgeToMST[selectedVertex];

            // Update cheapestEdge for all neighbors of selectedVertex
            for (int neighborVertex = 0; neighborVertex < totalVertices; neighborVertex++) {
                if (!inMST[neighborVertex] &&
                    costMatrix[selectedVertex][neighborVertex] < cheapestEdgeToMST[neighborVertex]) {
                    cheapestEdgeToMST[neighborVertex] = costMatrix[selectedVertex][neighborVertex];
                    mstParent[neighborVertex] = selectedVertex;
                }
            }
        }

        return totalMSTWeight;
    }

    /**
     * Returns the MST edges (not just total weight) for adjacency matrix Prim's.
     * Each returned int[] = {parentVertex, childVertex, edgeWeight}.
     */
    public static List<int[]> primsMSTMatrixEdges(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0) return new ArrayList<>();
        int totalVertices = costMatrix.length;

        int[] cheapestEdge = new int[totalVertices];
        int[] mstParent    = new int[totalVertices];
        boolean[] inMST    = new boolean[totalVertices];

        Arrays.fill(cheapestEdge, INFINITY);
        Arrays.fill(mstParent, -1);
        cheapestEdge[0] = 0;

        List<int[]> mstEdges = new ArrayList<>();

        for (int iteration = 0; iteration < totalVertices; iteration++) {
            int selected = -1;
            for (int v = 0; v < totalVertices; v++) {
                if (!inMST[v] && (selected == -1 || cheapestEdge[v] < cheapestEdge[selected])) {
                    selected = v;
                }
            }
            if (cheapestEdge[selected] == INFINITY) break;

            inMST[selected] = true;
            if (mstParent[selected] != -1) {
                mstEdges.add(new int[]{mstParent[selected], selected, cheapestEdge[selected]});
            }

            for (int neighbor = 0; neighbor < totalVertices; neighbor++) {
                if (!inMST[neighbor] && costMatrix[selected][neighbor] < cheapestEdge[neighbor]) {
                    cheapestEdge[neighbor] = costMatrix[selected][neighbor];
                    mstParent[neighbor] = selected;
                }
            }
        }
        return mstEdges;
    }

    // =========================================================
    // APPROACH 2 — PRIM'S WITH MIN-HEAP (O(E log V))
    // =========================================================

    /**
     * Prim's MST using a PriorityQueue (min-heap) on adjacency list.
     * Optimal for sparse graphs: O(E log V).
     *
     * ALGORITHM:
     *   1. Push {0, startVertex} to min-heap (cost, vertex).
     *   2. While heap not empty:
     *      a. Pop {cost, vertex}.
     *      b. If vertex already in MST: skip (stale heap entry).
     *      c. Add vertex to MST. Add cost to total.
     *      d. Push all {edgeWeight, neighbor} for unvisited neighbors.
     *
     * NOTE: We use "lazy deletion" (same as Dijkstra) to handle stale entries.
     *
     * Time:  O(E log V)
     * Space: O(V + E)
     */
    public static int primsMSTHeap(List<List<int[]>> adjacencyList, int totalVertices) {
        if (adjacencyList == null || totalVertices == 0) return -1;

        boolean[] inMST = new boolean[totalVertices];
        int totalMSTWeight = 0;
        int verticesAdded = 0;

        // Min-heap: int[] = {edgeCost, vertexToAdd}
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        minHeap.offer(new int[]{0, 0});   // start from vertex 0 with cost 0

        while (!minHeap.isEmpty()) {
            int[] heapEntry  = minHeap.poll();
            int edgeCost     = heapEntry[0];
            int currentVertex = heapEntry[1];

            if (inMST[currentVertex]) continue;   // stale entry: already in MST

            inMST[currentVertex] = true;
            totalMSTWeight += edgeCost;
            verticesAdded++;

            if (verticesAdded == totalVertices) break;   // MST complete

            for (int[] neighborEdge : adjacencyList.get(currentVertex)) {
                int neighborVertex = neighborEdge[0];
                int neighborCost   = neighborEdge[1];
                if (!inMST[neighborVertex]) {
                    minHeap.offer(new int[]{neighborCost, neighborVertex});
                }
            }
        }

        return verticesAdded == totalVertices ? totalMSTWeight : -1;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Prim's Dense Graph MST — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Standard 5-vertex graph ---
        System.out.println("\n--- Test 1: 5-vertex classic Prim's example ---");
        int INF = INFINITY;
        int[][] matrix1 = {
            {0,   2,   INF, 6,   INF},
            {2,   0,   3,   8,   5  },
            {INF, 3,   0,   INF, 7  },
            {6,   8,   INF, 0,   9  },
            {INF, 5,   7,   9,   0  }
        };
        System.out.println("Matrix Prim's weight: " + primsMSTMatrix(matrix1) + " (expected 16)");
        System.out.print("MST edges: ");
        primsMSTMatrixEdges(matrix1).forEach(e -> System.out.print(Arrays.toString(e) + " "));
        System.out.println();

        // Build adjacency list from matrix for heap version
        int v1 = 5;
        List<List<int[]>> adj1 = new ArrayList<>();
        for (int i = 0; i < v1; i++) adj1.add(new ArrayList<>());
        for (int i = 0; i < v1; i++) {
            for (int j = 0; j < v1; j++) {
                if (i != j && matrix1[i][j] < INF) adj1.get(i).add(new int[]{j, matrix1[i][j]});
            }
        }
        System.out.println("Heap Prim's weight:   " + primsMSTHeap(adj1, v1) + " (expected 16)");

        // --- Test 2: Simple triangle ---
        System.out.println("\n--- Test 2: Triangle (0-1-2-0) ---");
        int[][] matrix2 = {{0,1,2},{1,0,3},{2,3,0}};
        System.out.println("Weight: " + primsMSTMatrix(matrix2) + " (expected 3: edges 0-1=1, 0-2=2)");

        // --- Test 3: Disconnected ---
        System.out.println("\n--- Test 3: Disconnected graph ---");
        int[][] matrix3 = {{0,1,INF},{1,0,INF},{INF,INF,0}};
        System.out.println("Weight: " + primsMSTMatrix(matrix3) + " (expected -1, disconnected)");

        // --- Test 4: 2-vertex graph ---
        System.out.println("\n--- Test 4: 2-vertex minimal graph ---");
        int[][] matrix4 = {{0,5},{5,0}};
        System.out.println("Weight: " + primsMSTMatrix(matrix4) + " (expected 5)");

        System.out.println("\n==============================================");
        System.out.println("  All Prim's tests completed.");
        System.out.println("==============================================");
    }
}
