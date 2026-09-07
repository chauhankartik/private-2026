/**
 * ============================================================
 *  PATTERN 6 — DISJOINT SET UNION-FIND
 *  Problem 3 (Hard): Number of Islands II   LC 305
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an m×n grid (initially all water), process a list of land additions
 *    (positions). After each addition, return the current number of islands.
 *    An island is a connected group of '1's (4-directional connectivity).
 *
 *  EXAMPLE:
 *    m=3, n=3, positions=[[0,0],[0,1],[1,2],[2,1]]
 *    After [0,0]: 1 island
 *    After [0,1]: 1 island  (merged with [0,0])
 *    After [1,2]: 2 islands
 *    After [2,1]: 3 islands
 *    Output: [1,1,2,3]
 *
 *  CONSTRAINTS:
 *    1 <= m, n <= 10^4
 *    1 <= positions.length <= 10^4
 *    positions[i] = [ri, ci] where 0 <= ri < m, 0 <= ci < n
 *
 *  APPROACH 1: Union-Find with 2D→1D index mapping and active[] tracking
 *    Time:  O(L × α(M × N)) ≈ O(L)   — L = number of positions
 *    Space: O(M × N)
 *
 *  APPROACH 2: BFS rebuild (naive) — recount after each addition
 *    Time:  O(L × M × N)   — too slow for large grids, shown for contrast
 *    Space: O(M × N)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Number_of_Islands_II {

    // =========================================================
    // APPROACH 1 — DYNAMIC UNION-FIND
    // =========================================================

    /**
     * Processes land additions and returns island count after each.
     *
     * ALGORITHM:
     *   1. Initialize Union-Find over all M×N cells.
     *   2. Use isLand[row][col] to track which cells are active land.
     *   3. For each position (row, col):
     *      a. If already land: skip (duplicate position).
     *      b. Mark as land. Increment island count.
     *      c. Check all 4 neighbors. For each land neighbor:
     *         union(current, neighbor). If union succeeds (different components):
     *         decrement island count (two islands merged into one).
     *      d. Record current island count in result list.
     *
     * 2D → 1D INDEX MAPPING:
     *   Cell (row, col) → index = row × numColumns + col
     *   This maps all grid cells to a linear Union-Find array.
     *
     * @param totalRows    grid height
     * @param totalColumns grid width
     * @param positions    list of land additions [row, col]
     * @return list of island counts after each addition
     *
     * Time:  O(L × α(M × N)) ≈ O(L)
     * Space: O(M × N)
     */
    public List<Integer> numIslands2(int totalRows, int totalColumns, int[][] positions) {
        List<Integer> islandCountHistory = new ArrayList<>();
        if (positions == null || positions.length == 0) return islandCountHistory;

        int totalCells = totalRows * totalColumns;
        int[] parent   = new int[totalCells];
        int[] rank     = new int[totalCells];
        boolean[] isLand = new boolean[totalCells];
        Arrays.fill(parent, -1);   // -1 = water (not yet land)

        int currentIslandCount = 0;

        // 4-directional movement: up, down, left, right
        int[] rowDelta = {-1, 1, 0, 0};
        int[] colDelta = {0, 0, -1, 1};

        for (int[] position : positions) {
            int newRow = position[0];
            int newCol = position[1];
            int cellIndex = newRow * totalColumns + newCol;

            if (isLand[cellIndex]) {
                // Duplicate position: island count unchanged
                islandCountHistory.add(currentIslandCount);
                continue;
            }

            // Activate this cell as land
            isLand[cellIndex] = true;
            parent[cellIndex] = cellIndex;   // initialize as its own component
            rank[cellIndex]   = 0;
            currentIslandCount++;            // new island added

            // Check 4 neighbors — merge with any adjacent land cells
            for (int direction = 0; direction < 4; direction++) {
                int neighborRow = newRow + rowDelta[direction];
                int neighborCol = newCol + colDelta[direction];

                if (neighborRow < 0 || neighborRow >= totalRows ||
                    neighborCol < 0 || neighborCol >= totalColumns) continue;

                int neighborIndex = neighborRow * totalColumns + neighborCol;

                if (!isLand[neighborIndex]) continue;   // water cell — skip

                // Union the two land cells
                int rootCurrent  = findWithCompression(parent, cellIndex);
                int rootNeighbor = findWithCompression(parent, neighborIndex);

                if (rootCurrent != rootNeighbor) {
                    // Merge two separate islands → island count decreases by 1
                    unionByRank(parent, rank, rootCurrent, rootNeighbor);
                    currentIslandCount--;
                }
            }

            islandCountHistory.add(currentIslandCount);
        }

        return islandCountHistory;
    }

    private int findWithCompression(int[] parent, int x) {
        if (parent[x] != x) parent[x] = findWithCompression(parent, parent[x]);
        return parent[x];
    }

    private void unionByRank(int[] parent, int[] rank, int rootA, int rootB) {
        if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
        parent[rootB] = rootA;
        if (rank[rootA] == rank[rootB]) rank[rootA]++;
    }

    // =========================================================
    // APPROACH 2 — NAIVE BFS REBUILD (for contrast — too slow)
    // =========================================================

    /**
     * Naive approach: maintain the grid state and run full BFS after each addition.
     * Demonstrates why Union-Find is essential for the online (dynamic) version.
     *
     * Time:  O(L × M × N)  — L additions, each triggers O(M×N) BFS
     * Space: O(M × N)
     */
    public List<Integer> numIslands2Naive(int totalRows, int totalColumns, int[][] positions) {
        List<Integer> result = new ArrayList<>();
        char[][] grid = new char[totalRows][totalColumns];
        for (char[] row : grid) Arrays.fill(row, '0');

        int[] rowDelta = {-1, 1, 0, 0};
        int[] colDelta = {0, 0, -1, 1};

        for (int[] pos : positions) {
            grid[pos[0]][pos[1]] = '1';

            // Count islands from scratch using BFS
            boolean[][] visited = new boolean[totalRows][totalColumns];
            int islandCount = 0;

            for (int r = 0; r < totalRows; r++) {
                for (int c = 0; c < totalColumns; c++) {
                    if (grid[r][c] == '1' && !visited[r][c]) {
                        islandCount++;
                        // BFS to mark entire island
                        Queue<int[]> bfsQueue = new LinkedList<>();
                        bfsQueue.offer(new int[]{r, c});
                        visited[r][c] = true;
                        while (!bfsQueue.isEmpty()) {
                            int[] cell = bfsQueue.poll();
                            for (int d = 0; d < 4; d++) {
                                int nr = cell[0] + rowDelta[d];
                                int nc = cell[1] + colDelta[d];
                                if (nr >= 0 && nr < totalRows && nc >= 0 && nc < totalColumns
                                    && grid[nr][nc] == '1' && !visited[nr][nc]) {
                                    visited[nr][nc] = true;
                                    bfsQueue.offer(new int[]{nr, nc});
                                }
                            }
                        }
                    }
                }
            }
            result.add(islandCount);
        }
        return result;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Number_of_Islands_II solver = new Problem_3_Hard_Number_of_Islands_II();

        System.out.println("==============================================");
        System.out.println("  Number of Islands II — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: LC 305 Example ---
        System.out.println("\n--- Test 1: 3×3 grid, 4 additions ---");
        int[][] pos1 = {{0,0},{0,1},{1,2},{2,1}};
        System.out.println("Union-Find: " + solver.numIslands2(3, 3, pos1) + " (expected [1,1,2,3])");
        System.out.println("Naive BFS:  " + solver.numIslands2Naive(3, 3, pos1));

        // --- Test 2: Single cell ---
        System.out.println("\n--- Test 2: 1×1 grid ---");
        int[][] pos2 = {{0,0}};
        System.out.println("Union-Find: " + solver.numIslands2(1, 1, pos2) + " (expected [1])");

        // --- Test 3: All cells filled sequentially ---
        System.out.println("\n--- Test 3: 2×2 grid, all cells ---");
        int[][] pos3 = {{0,0},{1,1},{0,1},{1,0}};
        System.out.println("Union-Find: " + solver.numIslands2(2, 2, pos3) + " (expected [1,2,2,1])");
        System.out.println("Naive BFS:  " + solver.numIslands2Naive(2, 2, pos3));

        // --- Test 4: Duplicate positions ---
        System.out.println("\n--- Test 4: Duplicate position [0,0] ---");
        int[][] pos4 = {{0,0},{0,0},{0,1}};
        System.out.println("Union-Find: " + solver.numIslands2(2, 2, pos4) + " (expected [1,1,1])");

        // --- Test 5: Row of cells (linear island) ---
        System.out.println("\n--- Test 5: 1×5 grid, left to right ---");
        int[][] pos5 = {{0,0},{0,2},{0,4},{0,1},{0,3}};
        System.out.println("Union-Find: " + solver.numIslands2(1, 5, pos5) + " (expected [1,2,3,2,1])");
        System.out.println("Naive BFS:  " + solver.numIslands2Naive(1, 5, pos5));

        System.out.println("\n==============================================");
        System.out.println("  All Number of Islands II tests completed.");
        System.out.println("==============================================");
    }
}
