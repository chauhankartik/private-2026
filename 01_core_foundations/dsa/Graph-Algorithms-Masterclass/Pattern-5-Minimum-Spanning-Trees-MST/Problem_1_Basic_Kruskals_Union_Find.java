/**
 * ============================================================
 *  PATTERN 5 — MINIMUM SPANNING TREES (MST)
 *  Problem 1 (Basic): Kruskal's Algorithm with Union-Find   LC 1584 variant
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a connected undirected weighted graph with V vertices and E edges,
 *    find the Minimum Spanning Tree (MST): a subgraph with V-1 edges that
 *    connects all vertices with minimum total edge weight.
 *    Also detect if the graph is disconnected (no MST exists).
 *
 *  CONSTRAINTS:
 *    2 <= V <= 10^5
 *    V-1 <= E <= 2 × 10^5
 *    Edge weights may be any integer (Kruskal handles negative weights correctly).
 *
 *  APPROACH 1: Kruskal's with Union-Find (path compression + union by rank)
 *    Time:  O(E log E)  — dominated by sorting edges
 *    Space: O(V + E)    — Union-Find arrays + edge list
 *
 *  APPROACH 2: Kruskal's with edge filtering (spanning forest for disconnected graphs)
 *    Time:  O(E log E)
 *    Space: O(V)
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Kruskals_Union_Find {

    // =========================================================
    // UNION-FIND DATA STRUCTURE
    // =========================================================

    /**
     * Union-Find (Disjoint Set Union) with:
     *   - Path compression in find()
     *   - Union by rank in union()
     * Provides near-O(1) per operation: O(α(V)) ≈ O(1).
     */
    static class UnionFind {
        private final int[] parent;
        private final int[] rank;
        private int componentCount;

        public UnionFind(int totalVertices) {
            parent = new int[totalVertices];
            rank   = new int[totalVertices];
            componentCount = totalVertices;
            for (int vertex = 0; vertex < totalVertices; vertex++) {
                parent[vertex] = vertex;   // each vertex is its own root initially
                rank[vertex]   = 0;
            }
        }

        /**
         * Finds the root representative of the component containing vertex x.
         * Applies PATH COMPRESSION: makes x and all ancestors point directly to root.
         * Time: O(α(V)) amortized ≈ O(1)
         */
        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);   // compress path
            }
            return parent[x];
        }

        /**
         * Merges the components containing x and y.
         * Uses UNION BY RANK: attaches smaller-rank tree under larger-rank tree.
         * @return true if x and y were in different components (successful merge),
         *         false if already connected (adding this edge would create a cycle)
         */
        public boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return false;   // same component → cycle → skip

            // Attach smaller rank tree under larger rank tree
            if (rank[rootX] < rank[rootY]) {
                int temp = rootX; rootX = rootY; rootY = temp;
            }
            parent[rootY] = rootX;
            if (rank[rootX] == rank[rootY]) rank[rootX]++;

            componentCount--;
            return true;
        }

        public boolean isConnected(int x, int y) { return find(x) == find(y); }
        public int getComponentCount() { return componentCount; }
    }

    // =========================================================
    // APPROACH 1 — KRUSKAL'S MST ALGORITHM
    // =========================================================

    /**
     * Kruskal's MST Algorithm:
     *   1. Sort all edges by weight (ascending).
     *   2. For each edge (u, v, w) in sorted order:
     *      - If u and v are in different components: add edge to MST, union them.
     *      - If same component: skip (adding would create a cycle).
     *   3. MST is complete when exactly V-1 edges are selected.
     *   4. If fewer than V-1 edges selected → graph is disconnected (no spanning tree).
     *
     * @param totalVertices number of vertices (0-indexed)
     * @param edges         edge list: each int[] = {vertexA, vertexB, weight}
     * @return MSTResult containing total weight and MST edges, or null if disconnected
     *
     * Time:  O(E log E)  — sorting dominates
     * Space: O(V)        — Union-Find arrays (edge list provided externally)
     */
    public static MSTResult kruskalsMST(int totalVertices, int[][] edges) {
        if (totalVertices <= 0) return null;
        if (edges == null || edges.length == 0) {
            return totalVertices == 1 ? new MSTResult(0, new ArrayList<>()) : null;
        }

        // Sort edges by weight (ascending)
        Arrays.sort(edges, Comparator.comparingInt(edge -> edge[2]));

        UnionFind unionFind = new UnionFind(totalVertices);
        List<int[]> mstEdges = new ArrayList<>();
        int totalMSTWeight = 0;

        for (int[] edge : edges) {
            int vertexA    = edge[0];
            int vertexB    = edge[1];
            int edgeWeight = edge[2];

            if (unionFind.union(vertexA, vertexB)) {
                // Edge accepted into MST (no cycle created)
                mstEdges.add(edge);
                totalMSTWeight += edgeWeight;

                // Early termination: MST complete when V-1 edges selected
                if (mstEdges.size() == totalVertices - 1) break;
            }
            // else: edge creates a cycle → skip (already same component)
        }

        // Check if MST spans all vertices
        if (mstEdges.size() < totalVertices - 1) {
            return null;   // graph is disconnected — no spanning tree
        }

        return new MSTResult(totalMSTWeight, mstEdges);
    }

    /** Result container for MST computation. */
    public static class MSTResult {
        public final int totalWeight;
        public final List<int[]> mstEdges;

        public MSTResult(int totalWeight, List<int[]> mstEdges) {
            this.totalWeight = totalWeight;
            this.mstEdges    = mstEdges;
        }
    }

    // =========================================================
    // APPROACH 2 — KRUSKAL'S SPANNING FOREST (DISCONNECTED GRAPHS)
    // =========================================================

    /**
     * Computes a MINIMUM SPANNING FOREST for potentially disconnected graphs.
     * A spanning forest = one spanning tree per connected component.
     * Process all edges; result has fewer than V-1 edges if disconnected.
     *
     * @return list of selected forest edges and total weight
     *
     * Time:  O(E log E)
     * Space: O(V)
     */
    public static MSTResult kruskalsSpanningForest(int totalVertices, int[][] edges) {
        if (totalVertices <= 0) return new MSTResult(0, new ArrayList<>());
        if (edges == null || edges.length == 0) return new MSTResult(0, new ArrayList<>());

        int[][] sortedEdges = edges.clone();
        Arrays.sort(sortedEdges, Comparator.comparingInt(e -> e[2]));

        UnionFind unionFind = new UnionFind(totalVertices);
        List<int[]> forestEdges = new ArrayList<>();
        int totalWeight = 0;

        for (int[] edge : sortedEdges) {
            if (unionFind.union(edge[0], edge[1])) {
                forestEdges.add(edge);
                totalWeight += edge[2];
            }
        }

        return new MSTResult(totalWeight, forestEdges);
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Kruskal's Union-Find MST — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic connected graph ---
        System.out.println("\n--- Test 1: 4-vertex graph ---");
        //   0---1 (w=4)
        //   |   | \
        //   2---3 (w=2, w=1, w=3)
        int[][] edges1 = {{0,1,4},{0,2,3},{1,2,1},{1,3,2},{2,3,5}};
        MSTResult result1 = kruskalsMST(4, edges1);
        System.out.println("MST weight: " + result1.totalWeight + " (expected 6: edges 1-2=1, 1-3=2, 0-2=3)");
        System.out.print("MST edges:  ");
        result1.mstEdges.forEach(e -> System.out.print(Arrays.toString(e) + " "));
        System.out.println();

        // --- Test 2: Disconnected graph ---
        System.out.println("\n--- Test 2: Disconnected (vertex 4 isolated) ---");
        int[][] edges2 = {{0,1,1},{1,2,2},{2,3,3}};  // vertex 4 has no edges
        MSTResult result2 = kruskalsMST(5, edges2);
        System.out.println("MST result: " + result2 + " (expected null — disconnected)");

        // Spanning forest
        MSTResult forest2 = kruskalsSpanningForest(5, edges2);
        System.out.println("Forest weight: " + forest2.totalWeight);

        // --- Test 3: Negative edge weights ---
        System.out.println("\n--- Test 3: Negative edge weights ---");
        int[][] edges3 = {{0,1,-5},{1,2,3},{0,2,10}};
        MSTResult result3 = kruskalsMST(3, edges3);
        System.out.println("MST weight: " + result3.totalWeight + " (expected -5+3=-2)");

        // --- Test 4: All same weights ---
        System.out.println("\n--- Test 4: All edges weight 1 ---");
        int[][] edges4 = {{0,1,1},{0,2,1},{1,2,1},{1,3,1}};
        MSTResult result4 = kruskalsMST(4, edges4);
        System.out.println("MST weight: " + result4.totalWeight + " (expected 3: any spanning tree)");

        // --- Test 5: Single edge graph ---
        System.out.println("\n--- Test 5: 2 vertices, 1 edge ---");
        int[][] edges5 = {{0,1,7}};
        MSTResult result5 = kruskalsMST(2, edges5);
        System.out.println("MST weight: " + result5.totalWeight + " (expected 7)");

        // --- Test 6: Union-Find properties ---
        System.out.println("\n--- Test 6: Union-Find standalone test ---");
        UnionFind uf = new UnionFind(5);
        System.out.println("Components initially: " + uf.getComponentCount() + " (expected 5)");
        uf.union(0, 1); uf.union(2, 3); uf.union(0, 3);
        System.out.println("Components after unions: " + uf.getComponentCount() + " (expected 2)");
        System.out.println("0 connected to 3? " + uf.isConnected(0, 3) + " (expected true)");
        System.out.println("0 connected to 4? " + uf.isConnected(0, 4) + " (expected false)");

        System.out.println("\n==============================================");
        System.out.println("  All Kruskal's tests completed.");
        System.out.println("==============================================");
    }
}
