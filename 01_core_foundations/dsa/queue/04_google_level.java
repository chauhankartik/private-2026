/**
 * ============================================================
 *  QUEUE — GOOGLE-LEVEL PROBLEMS
 * ============================================================
 *
 *  G1.  Design Hit Counter                         LC 362  ★ Google
 *  G2.  Design Snake Game                          LC 353  ★ Google
 *  G3.  Minimum Cost to Make at Least One Valid Path LC 1368 ★ Google
 *  G4.  Shortest Path to Get All Keys              LC 864  ★ Google
 *  G5.  Maximum Number of Events That Can Be Attended LC 1353 ★ Google
 *
 *  Each problem includes:
 *   - Why Google asks it
 *   - Brute → optimal trace
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class QueueGoogleLevel {

    // =========================================================
    // G1. Design Hit Counter  LC 362  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests system design thinking at algorithm level.
     *   Simple queue solution, but follow-ups probe distributed systems knowledge:
     *   "What if we get 10 million hits per second?" → circular buffer + bucketing.
     *
     * Count hits in the last 300 seconds (5 minutes).
     * hit(timestamp) → record a hit.
     * getHits(timestamp) → return count of hits in [timestamp-299, timestamp].
     *
     * --- Queue approach ---
     * Queue stores timestamps. On getHits, dequeue expired timestamps.
     *
     * --- Circular buffer approach (better for high throughput) ---
     * int[300] times, int[300] hits.
     * times[t % 300] = t, hits[t % 300] = count at that second.
     * On hit: if times[t%300] != t, reset count. Increment count.
     * On getHits: sum hits[i] where times[i] is within [t-299, t].
     *
     * Time:  O(1) per hit, O(300) = O(1) per getHits
     * Space: O(300) = O(1)
     *
     * Follow-up: What if timestamps arrive out of order?
     *   Use a sorted structure or accept slight inaccuracy.
     * Follow-up: Distributed system with multiple servers?
     *   Each server tracks local counts → aggregate at query time.
     */
    static class HitCounter {
        private int[] times;
        private int[] hits;

        HitCounter() {
            times = new int[300];
            hits = new int[300];
        }

        public void hit(int timestamp) {
            int idx = timestamp % 300;
            if (times[idx] != timestamp) {
                times[idx] = timestamp;
                hits[idx] = 1;
            } else {
                hits[idx]++;
            }
        }

        public int getHits(int timestamp) {
            int total = 0;
            for (int i = 0; i < 300; i++) {
                if (timestamp - times[i] < 300) {
                    total += hits[i];
                }
            }
            return total;
        }
    }

    // =========================================================
    // G2. Design Snake Game  LC 353  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests deque design and edge-case handling.
     *   Snake body = deque (add head to front, remove tail from back).
     *   Collision detection: check if new head is in the body.
     *   Google interviewers look for clean state management.
     *
     * Snake on a grid. Moves in direction, eats food, grows.
     * Return the score after each move, or -1 if game over.
     *
     * Body stored as a DEQUE of positions:
     *   Head = deque front. Tail = deque back.
     *   Move: compute new head position.
     *   If food at new position: don't remove tail (grow).
     *   Else: remove tail (no growth).
     *   If new head collides with body or wall: game over.
     *
     * Collision check: HashSet of body positions for O(1) lookup.
     *
     * Time:  O(1) per move
     * Space: O(snake_length + food_count)
     *
     * Follow-up: What if the grid wraps around (torus)?
     *   New head = (row + dr + height) % height, same for col.
     */
    static class SnakeGame {
        private int width, height;
        private int[][] food;
        private int foodIdx;
        private int score;
        private Deque<int[]> body;  // front = head, back = tail
        private Set<String> bodySet;

        SnakeGame(int width, int height, int[][] food) {
            this.width = width;
            this.height = height;
            this.food = food;
            this.foodIdx = 0;
            this.score = 0;
            this.body = new ArrayDeque<>();
            this.bodySet = new HashSet<>();
            body.offerFirst(new int[]{0, 0});
            bodySet.add("0,0");
        }

        public int move(String direction) {
            int[] head = body.peekFirst();
            int newRow = head[0], newCol = head[1];

            switch (direction) {
                case "U": newRow--; break;
                case "D": newRow++; break;
                case "L": newCol--; break;
                case "R": newCol++; break;
            }

            // Check wall collision
            if (newRow < 0 || newRow >= height || newCol < 0 || newCol >= width)
                return -1;

            // Check food
            boolean ateFood = foodIdx < food.length
                && food[foodIdx][0] == newRow && food[foodIdx][1] == newCol;

            if (!ateFood) {
                // Remove tail (no growth)
                int[] tail = body.pollLast();
                bodySet.remove(tail[0] + "," + tail[1]);
            } else {
                score++;
                foodIdx++;
            }

            // Check body collision (AFTER removing tail, BEFORE adding head)
            if (bodySet.contains(newRow + "," + newCol))
                return -1;

            // Add new head
            body.offerFirst(new int[]{newRow, newCol});
            bodySet.add(newRow + "," + newCol);

            return score;
        }
    }

    // =========================================================
    // G3. Min Cost to Make at Least One Valid Path  LC 1368
    //     ★ Google — 0-1 BFS
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests 0-1 BFS — a technique less known than Dijkstra but more
     *   efficient for graphs with only 0 and 1 edge weights.
     *   Google loves problems where you choose the right algorithm variant.
     *
     * Grid with arrows (1=right, 2=left, 3=down, 4=up).
     * Following an arrow = cost 0. Changing direction = cost 1.
     * Find min cost path from (0,0) to (m-1,n-1).
     *
     * This is a graph with edge weights 0 (follow arrow) or 1 (change).
     *
     * 0-1 BFS: Use a DEQUE instead of a priority queue.
     *   - 0-weight edges: add to FRONT of deque.
     *   - 1-weight edges: add to BACK of deque.
     *   This gives correct shortest-path order (like Dijkstra) in O(V+E).
     *
     * WHY DEQUE WORKS:
     *   Nodes with 0-cost edges are processed before 1-cost edges.
     *   This maintains the BFS property: deque is sorted by distance.
     *   For general weights (0 and 1 only), this is equivalent to Dijkstra
     *   but O(V+E) instead of O(V log V).
     *
     * Time:  O(m × n)
     * Space: O(m × n)
     *
     * Follow-up: What if costs are 0, 1, 2, or 3?
     *   Use Dijkstra (priority queue) — 0-1 BFS only works for {0, 1} weights.
     */
    public int minCost(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;

        Deque<int[]> dq = new ArrayDeque<>();
        dq.offerFirst(new int[]{0, 0});

        // direction mapping: 1=right, 2=left, 3=down, 4=up
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!dq.isEmpty()) {
            int[] cell = dq.pollFirst();
            int r = cell[0], c = cell[1];

            for (int d = 0; d < 4; d++) {
                int nr = r + dirs[d][0], nc = c + dirs[d][1];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;

                // Cost 0 if this direction matches the arrow, else cost 1
                int cost = (grid[r][c] == d + 1) ? 0 : 1;
                int newDist = dist[r][c] + cost;

                if (newDist < dist[nr][nc]) {
                    dist[nr][nc] = newDist;
                    if (cost == 0) dq.offerFirst(new int[]{nr, nc});
                    else dq.offerLast(new int[]{nr, nc});
                }
            }
        }

        return dist[m - 1][n - 1];
    }

    // =========================================================
    // G4. Shortest Path to Get All Keys  LC 864  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests BFS with BITMASK state — combining graph search with
     *   combinatorial state tracking. Classic systems-thinking problem.
     *
     * Grid with walls (#), keys (a-f), locks (A-F), start (@), empty (.).
     * Can only pass through a lock if you have its key.
     * Find shortest path to collect ALL keys.
     *
     * State: (row, col, keys_bitmask)
     *   keys_bitmask: bit i = 1 means we have key 'a'+i.
     *   Max 6 keys → 2^6 = 64 possible key states.
     *
     * BFS on (row, col, keys) state space.
     * Total states: m × n × 2^K (K = number of keys).
     *
     * Transitions:
     *   Wall → skip.
     *   Lock → skip if we don't have the key.
     *   Key → update bitmask with new key.
     *   Empty → move freely.
     *
     * Goal: reach any cell with keys_bitmask = (1 << K) - 1 (all keys).
     *
     * Time:  O(m × n × 2^K)
     * Space: O(m × n × 2^K)
     *
     * Follow-up: What if there are more than 6 key types?
     *   Bitmask becomes expensive. Consider A* with heuristic.
     */
    public int shortestPathAllKeys(String[] grid) {
        int m = grid.length, n = grid[0].length();
        int totalKeys = 0;
        int startR = 0, startC = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                char c = grid[i].charAt(j);
                if (c == '@') { startR = i; startC = j; }
                if (c >= 'a' && c <= 'f') totalKeys++;
            }
        }

        int allKeys = (1 << totalKeys) - 1;
        boolean[][][] visited = new boolean[m][n][allKeys + 1];
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{startR, startC, 0});
        visited[startR][startC][0] = true;
        int steps = 0;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                int[] state = q.poll();
                int r = state[0], c = state[1], keys = state[2];

                if (keys == allKeys) return steps;

                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;

                    char cell = grid[nr].charAt(nc);
                    if (cell == '#') continue;  // wall

                    // Lock without key → skip
                    if (cell >= 'A' && cell <= 'F'
                        && (keys & (1 << (cell - 'A'))) == 0) continue;

                    int newKeys = keys;
                    if (cell >= 'a' && cell <= 'f')
                        newKeys |= (1 << (cell - 'a'));  // pick up key

                    if (!visited[nr][nc][newKeys]) {
                        visited[nr][nc][newKeys] = true;
                        q.offer(new int[]{nr, nc, newKeys});
                    }
                }
            }
            steps++;
        }

        return -1;
    }

    // =========================================================
    // G5. Maximum Number of Events That Can Be Attended  LC 1353
    //     ★ Google — Greedy + Priority Queue
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests greedy scheduling with a priority queue.
     *   Similar to meeting room problems but with a twist:
     *   you can attend each event on ANY day within its range.
     *
     * Events[i] = [startDay, endDay]. Attend at most one event per day.
     * Maximize total events attended.
     *
     * Greedy approach:
     *   1. Sort events by start day.
     *   2. For each day (from 1 to maxDay):
     *      a. Add all events starting on this day to a MIN-HEAP (by end day).
     *      b. Remove expired events (end day < current day) from heap.
     *      c. Attend the event with the EARLIEST end day (greedily).
     *
     * WHY EARLIEST END DAY:
     *   Events ending sooner have fewer remaining options.
     *   Attending them first maximizes flexibility for later days.
     *   This is the classic "interval scheduling" greedy.
     *
     * Time:  O(n log n) — sorting + heap operations
     * Space: O(n)
     *
     * Follow-up: Maximum Events That Can Be Attended II (LC 1751) with value?
     *   DP + binary search: dp[i][k] = max value attending ≤ k events from first i.
     */
    public int maxEvents(int[][] events) {
        Arrays.sort(events, (a, b) -> a[0] - b[0]);
        PriorityQueue<Integer> pq = new PriorityQueue<>();  // min-heap of end days
        int count = 0, idx = 0, n = events.length;

        // Process each day
        for (int day = 1; day <= 100000; day++) {
            // Add all events starting today
            while (idx < n && events[idx][0] == day) {
                pq.offer(events[idx][1]);
                idx++;
            }

            // Remove expired events
            while (!pq.isEmpty() && pq.peek() < day) {
                pq.poll();
            }

            // Attend the event ending soonest
            if (!pq.isEmpty()) {
                pq.poll();
                count++;
            }

            // Early exit: no more events to process
            if (idx >= n && pq.isEmpty()) break;
        }

        return count;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        QueueGoogleLevel sol = new QueueGoogleLevel();

        System.out.println("═══ G1: Hit Counter ═══");
        HitCounter hc = new HitCounter();
        hc.hit(1); hc.hit(2); hc.hit(3);
        System.out.println(hc.getHits(4));    // 3
        hc.hit(300);
        System.out.println(hc.getHits(300));  // 4
        System.out.println(hc.getHits(301));  // 3 (hit at t=1 expired)

        System.out.println("\n═══ G2: Snake Game ═══");
        SnakeGame sg = new SnakeGame(3, 2, new int[][]{{1,2},{0,1}});
        System.out.println(sg.move("R"));  // 0
        System.out.println(sg.move("D"));  // 0
        System.out.println(sg.move("R"));  // 1 (ate food at [1,2])
        System.out.println(sg.move("U"));  // 1
        System.out.println(sg.move("L"));  // 2 (ate food at [0,1])

        System.out.println("\n═══ G3: Min Cost Valid Path (0-1 BFS) ═══");
        System.out.println(sol.minCost(new int[][]{
            {1,1,1,1},{2,2,2,2},{1,1,1,1},{2,2,2,2}}));  // 3
        System.out.println(sol.minCost(new int[][]{{1,1,3},{3,2,2},{1,1,4}}));  // 0

        System.out.println("\n═══ G4: Shortest Path All Keys ═══");
        System.out.println(sol.shortestPathAllKeys(new String[]{"@.a..","###.#","b.A.B"})); // 8
        System.out.println(sol.shortestPathAllKeys(new String[]{"@..aA","..B#.","....b"})); // 6

        System.out.println("\n═══ G5: Max Events Attended ═══");
        System.out.println(sol.maxEvents(new int[][]{{1,2},{2,3},{3,4}}));          // 3
        System.out.println(sol.maxEvents(new int[][]{{1,2},{2,3},{3,4},{1,2}}));    // 4
        System.out.println(sol.maxEvents(new int[][]{{1,4},{4,4},{2,2},{3,4},{1,1}})); // 4
    }
}
