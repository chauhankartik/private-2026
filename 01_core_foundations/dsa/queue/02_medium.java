/**
 * ============================================================
 *  QUEUE — MEDIUM PROBLEMS
 * ============================================================
 *
 *  M1.  Rotting Oranges                            LC 994
 *  M2.  Design Circular Queue                      LC 622
 *  M3.  Task Scheduler                             LC 621
 *  M4.  Dota2 Senate                               LC 649
 *  M5.  Walls and Gates                            LC 286
 *  M6.  Design Circular Deque                      LC 641
 *  M7.  Open the Lock                              LC 752
 *  M8.  Shortest Path in Binary Matrix             LC 1091
 *  M9.  01 Matrix                                  LC 542
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class QueueMedium {

    // =========================================================
    // M1. Rotting Oranges  LC 994
    // Pattern: MULTI-SOURCE BFS
    // =========================================================
    /**
     * Grid with 0=empty, 1=fresh, 2=rotten.
     * Each minute, rotten oranges rot adjacent fresh oranges (4-dir).
     * Return minimum minutes to rot all oranges, or -1 if impossible.
     *
     * MULTI-SOURCE BFS: start BFS from ALL rotten oranges simultaneously.
     * This is equivalent to adding a virtual super-source connected to all
     * initial rotten oranges.
     *
     * Algorithm:
     *   1. Enqueue all initially rotten oranges + count fresh oranges.
     *   2. BFS level by level. Each level = 1 minute.
     *   3. When a fresh orange is rotted, decrement fresh count.
     *   4. If fresh == 0 at the end → return minutes.
     *      Otherwise → return -1.
     *
     * WHY MULTI-SOURCE BFS:
     *   All rotten oranges spread simultaneously.
     *   BFS from multiple sources in parallel = start with all sources in queue.
     *   This gives the minimum time for each fresh orange to be reached.
     *
     * Time:  O(m × n)
     * Space: O(m × n)
     *
     * Follow-up: What if rotting spreads in 8 directions?
     *   Add diagonal directions to the BFS expansion.
     */
    public int orangesRotting(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        Queue<int[]> q = new LinkedList<>();
        int fresh = 0;

        // Step 1: find all rotten oranges + count fresh
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 2) q.offer(new int[]{i, j});
                else if (grid[i][j] == 1) fresh++;
            }
        }

        if (fresh == 0) return 0;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        int minutes = 0;

        // Step 2: BFS level by level
        while (!q.isEmpty()) {
            int size = q.size();
            boolean rotted = false;

            for (int i = 0; i < size; i++) {
                int[] cell = q.poll();
                for (int[] d : dirs) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nr < m && nc >= 0 && nc < n && grid[nr][nc] == 1) {
                        grid[nr][nc] = 2;   // rot it
                        q.offer(new int[]{nr, nc});
                        fresh--;
                        rotted = true;
                    }
                }
            }

            if (rotted) minutes++;
        }

        return fresh == 0 ? minutes : -1;
    }

    // =========================================================
    // M2. Design Circular Queue  LC 622
    // Pattern: DESIGN — Array-Based Circular Buffer
    // =========================================================
    /**
     * Implement a circular queue with fixed capacity.
     *
     * Use an array with front and rear pointers.
     * Wrap-around: (index + 1) % capacity.
     * Track size to distinguish full vs empty (both have front == rear).
     *
     * Alternative: use (rear + 1) % capacity == front to detect full
     * (wastes one slot but avoids size tracking).
     *
     * Time:  O(1) for all operations
     * Space: O(k)
     *
     * Follow-up: Thread-safe circular queue?
     *   Use locks (ReentrantLock) or CAS (AtomicInteger for head/tail).
     */
    static class MyCircularQueue {
        private int[] data;
        private int front, rear, size, capacity;

        MyCircularQueue(int k) {
            data = new int[k];
            capacity = k;
            front = 0;
            rear = -1;
            size = 0;
        }

        public boolean enQueue(int value) {
            if (isFull()) return false;
            rear = (rear + 1) % capacity;
            data[rear] = value;
            size++;
            return true;
        }

        public boolean deQueue() {
            if (isEmpty()) return false;
            front = (front + 1) % capacity;
            size--;
            return true;
        }

        public int Front() {
            return isEmpty() ? -1 : data[front];
        }

        public int Rear() {
            return isEmpty() ? -1 : data[rear];
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public boolean isFull() {
            return size == capacity;
        }
    }

    // =========================================================
    // M3. Task Scheduler  LC 621
    // Pattern: GREEDY + QUEUE / MATH
    // =========================================================
    /**
     * Tasks with cooldown: same task must wait n intervals between executions.
     * Find minimum intervals to finish all tasks (can idle).
     *
     * Math approach (optimal):
     *   maxFreq = frequency of the most frequent task(s).
     *   maxCount = how many tasks have that max frequency.
     *   formula = (maxFreq - 1) × (n + 1) + maxCount
     *
     *   Why: lay out the most frequent task with gaps:
     *     A _ _ A _ _ A     ← (maxFreq-1) gaps of size (n+1), plus last group
     *   Fill gaps with other tasks. If all tasks fit → formula gives the answer.
     *   If tasks overflow gaps → answer = tasks.length (no idle needed).
     *
     *   Answer = max(formula, tasks.length)
     *
     * Time:  O(n)  (counting frequencies)
     * Space: O(1)  (26 letters)
     *
     * Follow-up: Return the actual schedule, not just the length?
     *   Use a max-heap + cooldown queue. Each round: pop max-freq task,
     *   decrement, add to cooldown queue with timestamp. Re-add when cooldown expires.
     */
    public int leastInterval(char[] tasks, int n) {
        int[] freq = new int[26];
        for (char t : tasks) freq[t - 'A']++;

        int maxFreq = 0, maxCount = 0;
        for (int f : freq) {
            if (f > maxFreq) { maxFreq = f; maxCount = 1; }
            else if (f == maxFreq) maxCount++;
        }

        int formula = (maxFreq - 1) * (n + 1) + maxCount;
        return Math.max(formula, tasks.length);
    }

    // =========================================================
    // M4. Dota2 Senate  LC 649
    // Pattern: TWO-QUEUE GREEDY SIMULATION
    // =========================================================
    /**
     * Two parties: Radiant (R) and Dire (D).
     * Each senator can ban one opponent. Banning happens in order.
     * Which party wins (all opponents banned)?
     *
     * Use two queues (one per party) storing INDEX positions.
     * Each round: compare front of both queues.
     * Smaller index acts first and bans the other.
     * Winner re-enters at position + n (next round).
     *
     * WHY INDEX + n:
     *   The winner senator will participate in the NEXT round.
     *   Adding n ensures they're placed after all current senators.
     *
     * Time:  O(n) — each senator eliminated once
     * Space: O(n)
     *
     * Follow-up: What if banning costs energy and each senator has different energy?
     *   Use a priority queue sorted by (energy, index).
     */
    public String predictPartyVictory(String senate) {
        Queue<Integer> radiant = new LinkedList<>();
        Queue<Integer> dire = new LinkedList<>();
        int n = senate.length();

        for (int i = 0; i < n; i++) {
            if (senate.charAt(i) == 'R') radiant.offer(i);
            else dire.offer(i);
        }

        while (!radiant.isEmpty() && !dire.isEmpty()) {
            int r = radiant.poll(), d = dire.poll();
            if (r < d) {
                radiant.offer(r + n);   // Radiant acts first, re-enters next round
            } else {
                dire.offer(d + n);      // Dire acts first
            }
        }

        return radiant.isEmpty() ? "Dire" : "Radiant";
    }

    // =========================================================
    // M5. Walls and Gates  LC 286
    // Pattern: MULTI-SOURCE BFS (from gates)
    // =========================================================
    /**
     * Grid: -1 = wall, 0 = gate, INF = empty room.
     * Fill each empty room with distance to its nearest gate.
     *
     * Brute: BFS from each empty room to find nearest gate — O(m²n²).
     * Optimal: REVERSE — BFS from all gates simultaneously.
     *
     * WHY MULTI-SOURCE BFS FROM GATES:
     *   Start BFS from all gates at the same time.
     *   Each room is visited once and assigned the shortest distance.
     *   This is equivalent to adding a super-source connected to all gates.
     *
     * Time:  O(m × n)
     * Space: O(m × n)
     *
     * Follow-up: What if some gates have different "costs" to enter?
     *   Use Dijkstra (priority queue) instead of plain BFS.
     */
    public void wallsAndGates(int[][] rooms) {
        int m = rooms.length, n = rooms[0].length;
        Queue<int[]> q = new LinkedList<>();

        // Find all gates
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (rooms[i][j] == 0) q.offer(new int[]{i, j});
            }
        }

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!q.isEmpty()) {
            int[] cell = q.poll();
            for (int[] d : dirs) {
                int nr = cell[0] + d[0], nc = cell[1] + d[1];
                if (nr >= 0 && nr < m && nc >= 0 && nc < n
                    && rooms[nr][nc] == Integer.MAX_VALUE) {
                    rooms[nr][nc] = rooms[cell[0]][cell[1]] + 1;
                    q.offer(new int[]{nr, nc});
                }
            }
        }
    }

    // =========================================================
    // M6. Design Circular Deque  LC 641
    // Pattern: DESIGN — Array-Based Circular Double-Ended Queue
    // =========================================================
    /**
     * Circular deque with fixed capacity.
     * Insert/delete from both front and rear.
     *
     * Array with front and rear pointers + size counter.
     * front decrements (wrap around), rear increments (wrap around).
     *
     * Time:  O(1) for all operations
     * Space: O(k)
     *
     * Follow-up: Implement with a doubly linked list?
     *   Each node has prev/next. Insert/delete at both ends in O(1).
     *   More memory (node objects) but no fixed capacity.
     */
    static class MyCircularDeque {
        private int[] data;
        private int front, rear, size, capacity;

        MyCircularDeque(int k) {
            data = new int[k];
            capacity = k;
            front = 0;
            rear = k - 1;
            size = 0;
        }

        public boolean insertFront(int value) {
            if (isFull()) return false;
            front = (front - 1 + capacity) % capacity;
            data[front] = value;
            size++;
            return true;
        }

        public boolean insertLast(int value) {
            if (isFull()) return false;
            rear = (rear + 1) % capacity;
            data[rear] = value;
            size++;
            return true;
        }

        public boolean deleteFront() {
            if (isEmpty()) return false;
            front = (front + 1) % capacity;
            size--;
            return true;
        }

        public boolean deleteLast() {
            if (isEmpty()) return false;
            rear = (rear - 1 + capacity) % capacity;
            size--;
            return true;
        }

        public int getFront() { return isEmpty() ? -1 : data[front]; }
        public int getRear()  { return isEmpty() ? -1 : data[rear]; }
        public boolean isEmpty() { return size == 0; }
        public boolean isFull()  { return size == capacity; }
    }

    // =========================================================
    // M7. Open the Lock  LC 752
    // Pattern: BFS — State Space Search
    // =========================================================
    /**
     * 4-digit lock (0000 to 9999). Each move: rotate one wheel ±1.
     * Find minimum moves from "0000" to target, avoiding deadends.
     *
     * Model as a GRAPH:
     *   Nodes = all 4-digit combinations (10,000 nodes).
     *   Edges = one wheel rotation (8 neighbors per node).
     *   BFS from "0000" to target.
     *
     * WHY BFS GUARANTEES SHORTEST PATH:
     *   Each edge has weight 1 (one rotation).
     *   BFS explores nodes level by level (by number of moves).
     *   First time we reach target = minimum moves.
     *
     * Deadends = walls in the graph (skip during BFS).
     *
     * Time:  O(10^4 × 4) = O(40,000) — at most 10K nodes × 8 neighbors
     * Space: O(10^4)
     *
     * Follow-up: Bidirectional BFS?
     *   Start BFS from both "0000" and target simultaneously.
     *   Meet in the middle. Halves the search space in practice.
     */
    public int openLock(String[] deadends, String target) {
        Set<String> dead = new HashSet<>(Arrays.asList(deadends));
        if (dead.contains("0000")) return -1;
        if ("0000".equals(target)) return 0;

        Queue<String> q = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        q.offer("0000");
        visited.add("0000");
        int moves = 0;

        while (!q.isEmpty()) {
            moves++;
            int size = q.size();
            for (int i = 0; i < size; i++) {
                String curr = q.poll();
                for (int j = 0; j < 4; j++) {
                    for (int d = -1; d <= 1; d += 2) {
                        char[] chars = curr.toCharArray();
                        chars[j] = (char) ('0' + (chars[j] - '0' + d + 10) % 10);
                        String next = new String(chars);
                        if (next.equals(target)) return moves;
                        if (!visited.contains(next) && !dead.contains(next)) {
                            visited.add(next);
                            q.offer(next);
                        }
                    }
                }
            }
        }

        return -1;
    }

    // =========================================================
    // M8. Shortest Path in Binary Matrix  LC 1091
    // Pattern: BFS — 8-Directional Grid
    // =========================================================
    /**
     * Shortest path from top-left to bottom-right in binary grid.
     * 0 = passable, 1 = blocked. Can move in 8 directions.
     *
     * Standard BFS on grid with 8 directions.
     * Path length = number of cells visited (including start and end).
     *
     * Time:  O(n²)
     * Space: O(n²)
     *
     * Follow-up: What if cells have different weights?
     *   Use Dijkstra (priority queue) instead of plain BFS.
     */
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;
        if (grid[0][0] == 1 || grid[n-1][n-1] == 1) return -1;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{0, 0});
        grid[0][0] = 1;  // mark visited by setting to 1
        int path = 1;

        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                int[] cell = q.poll();
                if (cell[0] == n-1 && cell[1] == n-1) return path;
                for (int[] d : dirs) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 0) {
                        grid[nr][nc] = 1;
                        q.offer(new int[]{nr, nc});
                    }
                }
            }
            path++;
        }

        return -1;
    }

    // =========================================================
    // M9. 01 Matrix  LC 542
    // Pattern: MULTI-SOURCE BFS (from zeros)
    // =========================================================
    /**
     * Find the distance of each cell to the nearest 0.
     *
     * Brute: BFS from each cell — O(m²n²).
     * Optimal: Multi-source BFS from all 0-cells simultaneously.
     *
     * Same pattern as Walls and Gates:
     *   1. Enqueue all 0-cells (distance 0).
     *   2. Set all other cells to MAX_VALUE.
     *   3. BFS: for each neighbor with larger distance, update and enqueue.
     *
     * Time:  O(m × n)
     * Space: O(m × n)
     *
     * Follow-up: What about nearest distance to any 1-cell instead?
     *   Start BFS from all 1-cells.
     */
    public int[][] updateMatrix(int[][] mat) {
        int m = mat.length, n = mat[0].length;
        Queue<int[]> q = new LinkedList<>();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (mat[i][j] == 0) {
                    q.offer(new int[]{i, j});
                } else {
                    mat[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        while (!q.isEmpty()) {
            int[] cell = q.poll();
            for (int[] d : dirs) {
                int nr = cell[0] + d[0], nc = cell[1] + d[1];
                if (nr >= 0 && nr < m && nc >= 0 && nc < n
                    && mat[nr][nc] > mat[cell[0]][cell[1]] + 1) {
                    mat[nr][nc] = mat[cell[0]][cell[1]] + 1;
                    q.offer(new int[]{nr, nc});
                }
            }
        }

        return mat;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        QueueMedium sol = new QueueMedium();

        System.out.println("═══ M1: Rotting Oranges ═══");
        System.out.println(sol.orangesRotting(new int[][]{
            {2,1,1},{1,1,0},{0,1,1}}));  // 4
        System.out.println(sol.orangesRotting(new int[][]{
            {2,1,1},{0,1,1},{1,0,1}}));  // -1

        System.out.println("\n═══ M2: Circular Queue ═══");
        MyCircularQueue cq = new MyCircularQueue(3);
        System.out.println(cq.enQueue(1));  // true
        System.out.println(cq.enQueue(2));  // true
        System.out.println(cq.enQueue(3));  // true
        System.out.println(cq.enQueue(4));  // false (full)
        System.out.println(cq.Rear());      // 3
        System.out.println(cq.isFull());    // true
        cq.deQueue();
        System.out.println(cq.enQueue(4));  // true
        System.out.println(cq.Rear());      // 4

        System.out.println("\n═══ M3: Task Scheduler ═══");
        System.out.println(sol.leastInterval(
            new char[]{'A','A','A','B','B','B'}, 2));  // 8
        System.out.println(sol.leastInterval(
            new char[]{'A','A','A','B','B','B'}, 0));  // 6

        System.out.println("\n═══ M4: Dota2 Senate ═══");
        System.out.println(sol.predictPartyVictory("RD"));    // Radiant
        System.out.println(sol.predictPartyVictory("RDD"));   // Dire
        System.out.println(sol.predictPartyVictory("RRDDD")); // Radiant

        System.out.println("\n═══ M5: Walls and Gates ═══");
        int INF = Integer.MAX_VALUE;
        int[][] rooms = {
            {INF,-1,0,INF},
            {INF,INF,INF,-1},
            {INF,-1,INF,-1},
            {0,-1,INF,INF}
        };
        sol.wallsAndGates(rooms);
        for (int[] row : rooms) System.out.println(Arrays.toString(row));
        // [3,-1,0,1], [2,2,1,-1], [1,-1,2,-1], [0,-1,3,4]

        System.out.println("\n═══ M6: Circular Deque ═══");
        MyCircularDeque cd = new MyCircularDeque(3);
        System.out.println(cd.insertLast(1));   // true
        System.out.println(cd.insertLast(2));   // true
        System.out.println(cd.insertFront(3));  // true
        System.out.println(cd.insertFront(4));  // false (full)
        System.out.println(cd.getRear());       // 2
        System.out.println(cd.isFull());        // true
        cd.deleteLast();
        System.out.println(cd.insertFront(4));  // true
        System.out.println(cd.getFront());      // 4

        System.out.println("\n═══ M7: Open the Lock ═══");
        System.out.println(sol.openLock(
            new String[]{"0201","0101","0102","1212","2002"}, "0202"));  // 6
        System.out.println(sol.openLock(
            new String[]{"8888"}, "0009"));  // 1

        System.out.println("\n═══ M9: 01 Matrix ═══");
        int[][] mat = {{0,0,0},{0,1,0},{1,1,1}};
        int[][] dist = sol.updateMatrix(mat);
        for (int[] row : dist) System.out.println(Arrays.toString(row));
        // [0,0,0], [0,1,0], [1,2,1]
    }
}
