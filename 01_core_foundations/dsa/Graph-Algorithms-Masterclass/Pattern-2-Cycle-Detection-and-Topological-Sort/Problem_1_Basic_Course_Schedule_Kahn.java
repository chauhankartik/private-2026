/**
 * ============================================================
 *  PATTERN 2 — CYCLE DETECTION AND TOPOLOGICAL SORT
 *  Problem 1 (Basic): Course Schedule — Kahn's BFS   LC 207
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    There are numCourses courses (0 to numCourses-1). You are given an array
 *    prerequisites where prerequisites[i] = [a, b] means you must take course b
 *    before course a. Return true if you can finish all courses (no cycle exists).
 *
 *  CONSTRAINTS:
 *    1 <= numCourses <= 2000
 *    0 <= prerequisites.length <= 5000
 *    prerequisites[i].length == 2; 0 <= ai, bi < numCourses
 *
 *  APPROACH 1: Kahn's BFS — process zero-in-degree vertices
 *    Time:  O(V + E)  — build graph O(E), BFS O(V+E)
 *    Space: O(V + E)  — adjacency list + inDegree[] + queue
 *
 *  APPROACH 2: DFS with 3-color cycle detection
 *    Time:  O(V + E)
 *    Space: O(V + E)  — adjacency list + color[] + O(V) recursion stack
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Course_Schedule_Kahn {

    // =========================================================
    // APPROACH 1 — KAHN'S BFS TOPOLOGICAL SORT
    // =========================================================

    /**
     * Determines if all courses can be finished using Kahn's BFS.
     *
     * ALGORITHM:
     *   1. Build adjacency list + inDegree[] from prerequisites.
     *   2. Enqueue all courses with inDegree == 0 (no prerequisites).
     *   3. BFS: dequeue course, reduce inDegree of its dependents.
     *      Enqueue any dependent whose inDegree drops to 0.
     *   4. If processed courses == numCourses → no cycle → can finish.
     *      Otherwise → cycle exists → cannot finish.
     *
     * CYCLE DETECTION INSIGHT:
     *   In a cycle, every vertex has inDegree >= 1 (from the back edge).
     *   Cycle vertices NEVER reach inDegree = 0 → never get enqueued.
     *   After BFS, processedCount < numCourses → cycle detected.
     *
     * @param numCourses    total number of courses
     * @param prerequisites [a, b] means b must come before a
     * @return true if all courses can be completed (no cycle)
     */
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        if (numCourses <= 0) return false;
        if (prerequisites == null || prerequisites.length == 0) return true;

        // Build adjacency list and inDegree array
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int course = 0; course < numCourses; course++) {
            adjacencyList.add(new ArrayList<>());
        }

        int[] inDegree = new int[numCourses];

        for (int[] prerequisite : prerequisites) {
            int dependentCourse   = prerequisite[0];   // course that requires the other
            int prerequisiteCourse = prerequisite[1];  // course that must come first
            adjacencyList.get(prerequisiteCourse).add(dependentCourse);
            inDegree[dependentCourse]++;
        }

        // Enqueue all courses with no prerequisites (inDegree == 0)
        Queue<Integer> zeroInDegreeQueue = new LinkedList<>();
        for (int course = 0; course < numCourses; course++) {
            if (inDegree[course] == 0) {
                zeroInDegreeQueue.offer(course);
            }
        }

        int processedCourseCount = 0;

        while (!zeroInDegreeQueue.isEmpty()) {
            int currentCourse = zeroInDegreeQueue.poll();
            processedCourseCount++;

            for (int dependentCourse : adjacencyList.get(currentCourse)) {
                inDegree[dependentCourse]--;
                if (inDegree[dependentCourse] == 0) {
                    zeroInDegreeQueue.offer(dependentCourse);
                }
            }
        }

        return processedCourseCount == numCourses;
    }

    /**
     * Returns one valid topological ordering of courses, or an empty array if impossible.
     * Uses Kahn's BFS — same algorithm as canFinish, but stores the ordering.
     *
     * For lex-smallest ordering: replace LinkedList with PriorityQueue (min-heap).
     */
    public int[] findTopologicalOrder(int numCourses, int[][] prerequisites) {
        if (numCourses <= 0) return new int[0];

        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adjacencyList.add(new ArrayList<>());
        int[] inDegree = new int[numCourses];

        for (int[] pre : prerequisites) {
            adjacencyList.get(pre[1]).add(pre[0]);
            inDegree[pre[0]]++;
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int[] topologicalOrder = new int[numCourses];
        int insertIndex = 0;

        while (!queue.isEmpty()) {
            int currentCourse = queue.poll();
            topologicalOrder[insertIndex++] = currentCourse;

            for (int dependent : adjacencyList.get(currentCourse)) {
                if (--inDegree[dependent] == 0) queue.offer(dependent);
            }
        }

        return insertIndex == numCourses ? topologicalOrder : new int[0];
    }

    // =========================================================
    // APPROACH 2 — DFS WITH 3-COLOR CYCLE DETECTION
    // =========================================================
    /**
     * Uses DFS 3-coloring to detect cycles in a directed graph.
     *
     * COLORS:
     *   0 = WHITE: not yet visited
     *   1 = GRAY:  currently in DFS stack (being explored)
     *   2 = BLACK: fully processed (all descendants explored)
     *
     * A BACK EDGE (current → GRAY vertex) = cycle.
     *
     * Time:  O(V + E)
     * Space: O(V + E) — adjacency list + O(V) recursion stack
     */
    public boolean canFinishDFS(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adjacencyList.add(new ArrayList<>());
        for (int[] pre : prerequisites) {
            adjacencyList.get(pre[1]).add(pre[0]);
        }

        int[] color = new int[numCourses]; // 0=WHITE, 1=GRAY, 2=BLACK

        for (int course = 0; course < numCourses; course++) {
            if (color[course] == 0) {
                if (dfsHasCycle(adjacencyList, course, color)) {
                    return false;   // cycle found → cannot finish
                }
            }
        }
        return true;
    }

    /**
     * @return true if a cycle is detected in the DFS subtree rooted at currentCourse
     */
    private boolean dfsHasCycle(List<List<Integer>> adjacencyList, int currentCourse, int[] color) {
        color[currentCourse] = 1;   // mark GRAY: entering this vertex's DFS

        for (int neighborCourse : adjacencyList.get(currentCourse)) {
            if (color[neighborCourse] == 1) return true;  // back edge → CYCLE
            if (color[neighborCourse] == 0) {
                if (dfsHasCycle(adjacencyList, neighborCourse, color)) return true;
            }
        }

        color[currentCourse] = 2;   // mark BLACK: fully processed
        return false;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Course_Schedule_Kahn solver = new Problem_1_Basic_Course_Schedule_Kahn();

        System.out.println("==============================================");
        System.out.println("  Course Schedule — Kahn's BFS Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Simple DAG, no cycle ---
        System.out.println("\n--- Test 1: 2 courses, 0 needs 1 first ---");
        System.out.println("canFinish: " + solver.canFinish(2, new int[][]{{1,0}}) + " (expected true)");
        System.out.println("DFS:       " + solver.canFinishDFS(2, new int[][]{{1,0}}) + " (expected true)");
        System.out.println("Order:     " + Arrays.toString(solver.findTopologicalOrder(2, new int[][]{{1,0}})));

        // --- Test 2: Direct cycle 0↔1 ---
        System.out.println("\n--- Test 2: Cycle (0→1, 1→0) ---");
        System.out.println("canFinish: " + solver.canFinish(2, new int[][]{{1,0},{0,1}}) + " (expected false)");
        System.out.println("DFS:       " + solver.canFinishDFS(2, new int[][]{{1,0},{0,1}}) + " (expected false)");
        System.out.println("Order:     " + Arrays.toString(solver.findTopologicalOrder(2, new int[][]{{1,0},{0,1}})) + " (expected [])");

        // --- Test 3: Longer cycle 0→1→2→0 ---
        System.out.println("\n--- Test 3: Triangle cycle (0→1→2→0) ---");
        int[][] pre3 = {{1,0},{2,1},{0,2}};
        System.out.println("canFinish: " + solver.canFinish(3, pre3) + " (expected false)");

        // --- Test 4: No prerequisites ---
        System.out.println("\n--- Test 4: 5 courses, no prerequisites ---");
        System.out.println("canFinish: " + solver.canFinish(5, new int[0][]) + " (expected true)");
        System.out.println("Order:     " + Arrays.toString(solver.findTopologicalOrder(5, new int[0][])));

        // --- Test 5: Diamond DAG (0→1, 0→2, 1→3, 2→3) ---
        System.out.println("\n--- Test 5: Diamond DAG ---");
        int[][] pre5 = {{1,0},{2,0},{3,1},{3,2}};
        System.out.println("canFinish: " + solver.canFinish(4, pre5) + " (expected true)");
        System.out.println("Order:     " + Arrays.toString(solver.findTopologicalOrder(4, pre5)));

        // --- Test 6: Disconnected graph with one cycle ---
        System.out.println("\n--- Test 6: Disconnected, one component has cycle ---");
        // Components: {0→1→2} and {3→4, 4→3} (cycle in second part)
        int[][] pre6 = {{1,0},{2,1},{4,3},{3,4}};
        System.out.println("canFinish (6 courses): " + solver.canFinish(5, pre6) + " (expected false)");

        // --- Test 7: Self-loop ---
        System.out.println("\n--- Test 7: Self-loop (0→0) ---");
        System.out.println("canFinish: " + solver.canFinish(3, new int[][]{{0,0}}) + " (expected false)");

        System.out.println("\n==============================================");
        System.out.println("  All Course Schedule tests completed.");
        System.out.println("==============================================");
    }
}
