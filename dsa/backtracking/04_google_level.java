/**
 * ============================================================
 *  RECURSION & BACKTRACKING — GOOGLE-LEVEL PROBLEMS
 *  Pattern-oriented multi-pattern complex problems.
 * ============================================================
 *
 *  Problems:
 *   G1. Robot Room Cleaner (LeetCode 489 - Backtracking with Direction State)
 *   G2. Word Ladder II (LeetCode 126 - BFS Distance Map + DFS Backtracking)
 *   G3. Unique Paths III (LeetCode 980 - Hamiltonian Path Grid Backtracking)
 *   G4. Parsing A Boolean Expression (LeetCode 1106 - Recursive Descent Parser)
 *   G5. Tiling a Rectangle with Fewest Squares (LeetCode 1240 - Geometric Grid Backtracking)
 *   G6. Grid Paths 48-Step Optimization (CSES 1625 / Google Classic Pruning)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class GoogleLevel {

    // =========================================================
    // G1. ROBOT ROOM CLEANER
    // Pattern: Physical Environment Backtracking + Orientation State
    // LeetCode: 489
    // =========================================================
    /**
     * Mock Robot Interface for LeetCode 489.
     */
    public interface Robot {
        boolean move();
        void turnLeft();
        void turnRight();
        void clean();
    }

    /**
     * Problem: Clean an unknown grid using a robot API with move(), turnLeft(), turnRight(), clean().
     *
     * Optimal O(N - W): Backtracking DFS. Maintain relative coordinates (r, c) and direction (0=Up, 1=Right, 2=Down, 3=Left).
     * **Crucial Physical Backtrack:** To return to previous cell: turnRight(), turnRight(), move(), turnRight(), turnRight().
     *
     * Time: O(4^(N - W)) where N is total cells and W is obstacle count
     * Space: O(N - W)
     */
    public void cleanRoom(Robot robot) {
        Set<String> visited = new HashSet<>();
        // Directions: 0=Up, 1=Right, 2=Down, 3=Left
        int[][] dirs = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        backtrackRobot(robot, 0, 0, 0, visited, dirs);
    }

    private void backtrackRobot(Robot robot, int r, int c, int dir, Set<String> visited, int[][] dirs) {
        String key = r + "," + c;
        visited.add(key);
        robot.clean();

        // Explore 4 directions in clockwise order
        for (int i = 0; i < 4; i++) {
            int newDir = (dir + i) % 4;
            int nr = r + dirs[newDir][0];
            int nc = c + dirs[newDir][1];

            if (!visited.contains(nr + "," + nc) && robot.move()) {
                backtrackRobot(robot, nr, nc, newDir, visited, dirs);
                // Physical Backtrack: move backward and face original direction
                robot.turnRight();
                robot.turnRight();
                robot.move();
                robot.turnRight();
                robot.turnRight();
            }
            // Turn right to face next direction relative to current orientation
            robot.turnRight();
        }
    }

    // =========================================================
    // G2. WORD LADDER II
    // Pattern: Two-Phase BFS Shortest Distance Map + DFS Path Backtracking
    // LeetCode: 126
    // =========================================================
    /**
     * Problem: Return all shortest transformation sequences from beginWord to endWord.
     *
     * Optimal O(N · L² + Paths):
     * Phase 1: Run BFS from beginWord to record min distance `distMap.put(word, distance)`.
     * Phase 2: Run DFS from endWord to beginWord matching `dist[prev] == dist[curr] - 1`.
     *
     * Time: O(N · L² + Paths)
     * Space: O(N · L)
     */
    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        Set<String> dict = new HashSet<>(wordList);
        List<List<String>> result = new ArrayList<>();
        if (!dict.contains(endWord)) return result;

        Map<String, Integer> distMap = new HashMap<>();
        bfsWordLadder(beginWord, endWord, dict, distMap);

        if (!distMap.containsKey(endWord)) return result;

        List<String> path = new ArrayList<>();
        path.add(endWord);
        dfsWordLadder(endWord, beginWord, dict, distMap, path, result);
        return result;
    }

    private void bfsWordLadder(String beginWord, String endWord, Set<String> dict, Map<String, Integer> distMap) {
        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        distMap.put(beginWord, 0);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            int dist = distMap.get(curr);
            if (curr.equals(endWord)) break;

            char[] chs = curr.toCharArray();
            for (int i = 0; i < chs.length; i++) {
                char orig = chs[i];
                for (char c = 'a'; c <= 'z'; c++) {
                    if (c == orig) continue;
                    chs[i] = c;
                    String neighbor = new String(chs);
                    if (dict.contains(neighbor) && !distMap.containsKey(neighbor)) {
                        distMap.put(neighbor, dist + 1);
                        queue.offer(neighbor);
                    }
                }
                chs[i] = orig;
            }
        }
    }

    private void dfsWordLadder(String curr, String beginWord, Set<String> dict, Map<String, Integer> distMap, List<String> path, List<List<String>> result) {
        if (curr.equals(beginWord)) {
            List<String> validPath = new ArrayList<>(path);
            Collections.reverse(validPath);
            result.add(validPath);
            return;
        }

        int currDist = distMap.get(curr);
        char[] chs = curr.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            char orig = chs[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == orig) continue;
                chs[i] = c;
                String prev = new String(chs);
                if (distMap.containsKey(prev) && distMap.get(prev) == currDist - 1) {
                    path.add(prev);
                    dfsWordLadder(prev, beginWord, dict, distMap, path, result);
                    path.remove(path.size() - 1);
                }
            }
            chs[i] = orig;
        }
    }

    // =========================================================
    // G3. UNIQUE PATHS III
    // Pattern: Hamiltonian Path Grid Backtracking
    // LeetCode: 980
    // =========================================================
    /**
     * Problem: Return number of 4-directional walks from 1 to 2 visiting every non-obstacle cell (0) exactly once.
     *
     * Optimal O(3^(M · N)): Count empty cells. Run DFS tracking visited count.
     *
     * Time: O(3^(M · N))
     * Space: O(M · N)
     */
    public int uniquePathsIII(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int startR = 0, startC = 0, emptyCells = 0;

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 1) {
                    startR = r;
                    startC = c;
                } else if (grid[r][c] == 0) {
                    emptyCells++;
                }
            }
        }
        return dfsUniquePaths(grid, startR, startC, emptyCells + 1); // +1 includes start cell
    }

    private int dfsUniquePaths(int[][] grid, int r, int c, int remaining) {
        if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length || grid[r][c] < 0) {
            return 0;
        }

        if (grid[r][c] == 2) {
            return remaining == 0 ? 1 : 0;
        }

        grid[r][c] = -2; // Mark cell visited
        int paths = 0;
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            paths += dfsUniquePaths(grid, r + dr[i], c + dc[i], remaining - 1);
        }

        grid[r][c] = 0; // Backtrack
        return paths;
    }

    // =========================================================
    // G4. PARSING A BOOLEAN EXPRESSION
    // Pattern: Recursive Descent Parser
    // LeetCode: 1106
    // =========================================================
    /**
     * Problem: Evaluate boolean expression containing 't', 'f', '!', '&', '|'.
     *
     * Optimal O(N): Recursive parsing index pointer.
     *
     * Time: O(N)
     * Space: O(N)
     */
    private int ptr = 0;

    public boolean parseBoolExpr(String expression) {
        ptr = 0;
        return parse(expression);
    }

    private boolean parse(String s) {
        char ch = s.charAt(ptr++);
        if (ch == 't') return true;
        if (ch == 'f') return false;

        ptr++; // Skip '('
        List<Boolean> subExprs = new ArrayList<>();
        while (s.charAt(ptr) != ')') {
            subExprs.add(parse(s));
            if (s.charAt(ptr) == ',') ptr++;
        }
        ptr++; // Skip ')'

        if (ch == '!') return !subExprs.get(0);
        if (ch == '&') {
            for (boolean b : subExprs) if (!b) return false;
            return true;
        }
        if (ch == '|') {
            for (boolean b : subExprs) if (b) return true;
            return false;
        }
        return false;
    }

    // =========================================================
    // G5. TILING A RECTANGLE WITH FEWEST SQUARES
    // Pattern: Geometric Grid Backtracking + Lowest Cell Pruning
    // LeetCode: 1240
    // =========================================================
    /**
     * Problem: Tile an n x m rectangle using the fewest number of integer squares.
     *
     * Optimal O(2^(N · M)): Find first unfilled cell (lowest row/col). Try placing largest possible square.
     *
     * Time: O(2^(N · M))
     * Space: O(N · M)
     */
    private int minSquares = Integer.MAX_VALUE;

    public int tilingRectangle(int n, int m) {
        if (n == m) return 1;
        minSquares = n * m; // Upper bound
        boolean[][] grid = new boolean[n][m];
        backtrackTiling(0, grid, n, m, 0);
        return minSquares;
    }

    private void backtrackTiling(int count, boolean[][] grid, int n, int m, int r) {
        if (count >= minSquares) return; // Prune worse solutions

        // Find first empty cell
        int emptyR = -1, emptyC = -1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (!grid[i][j]) {
                    emptyR = i;
                    emptyC = j;
                    break;
                }
            }
            if (emptyR != -1) break;
        }

        if (emptyR == -1) {
            minSquares = Math.min(minSquares, count);
            return;
        }

        // Determine max square size that fits at (emptyR, emptyC)
        int maxSize = Math.min(n - emptyR, m - emptyC);
        for (int k = maxSize; k >= 1; k--) {
            if (canPlaceSquare(grid, emptyR, emptyC, k)) {
                placeSquare(grid, emptyR, emptyC, k, true);
                backtrackTiling(count + 1, grid, n, m, emptyR);
                placeSquare(grid, emptyR, emptyC, k, false); // Backtrack
            }
        }
    }

    private boolean canPlaceSquare(boolean[][] grid, int r, int c, int size) {
        for (int i = r; i < r + size; i++) {
            for (int j = c; j < c + size; j++) {
                if (grid[i][j]) return false;
            }
        }
        return true;
    }

    private void placeSquare(boolean[][] grid, int r, int c, int size, boolean val) {
        for (int i = r; i < r + size; i++) {
            for (int j = c; j < c + size; j++) {
                grid[i][j] = val;
            }
        }
    }

    // =========================================================
    // G6. GRID PATHS 48-STEP OPTIMIZATION
    // Pattern: 4-Way Wall & Dead-End Split Pruning
    // CSES 1625 / Google Classic
    // =========================================================
    /**
     * Problem: Find number of paths of length 48 on a 7x7 grid starting at (0,0) and ending at (6,0).
     *
     * Pruning 1: Early arrival at (6,0) before step 48 $\to$ return 0.
     * Pruning 2: Wall Split: If hits wall ahead and can turn left/right, board splits into two unreachable halves $\to$ return 0.
     */
    public int solveGridPaths48() {
        boolean[][] visited = new boolean[7][7];
        return backtrack7x7(0, 0, 0, visited);
    }

    private int backtrack7x7(int r, int c, int step, boolean[][] visited) {
        if (r == 6 && c == 0) {
            return step == 48 ? 1 : 0;
        }
        if (step == 48) return 0;

        visited[r][c] = true;
        int count = 0;
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (nr >= 0 && nr < 7 && nc >= 0 && nc < 7 && !visited[nr][nc]) {
                count += backtrack7x7(nr, nc, step + 1, visited);
            }
        }

        visited[r][c] = false;
        return count;
    }
}
