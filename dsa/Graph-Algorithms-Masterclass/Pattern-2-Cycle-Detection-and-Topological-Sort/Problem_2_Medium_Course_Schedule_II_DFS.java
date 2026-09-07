/**
 * ============================================================
 *  PATTERN 2 — CYCLE DETECTION AND TOPOLOGICAL SORT
 *  Problem 2 (Medium): Course Schedule II — DFS Topo Sort   LC 210
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Return the ordering of courses you should take to finish all numCourses.
 *    If impossible (cycle exists), return an empty array.
 *    prerequisites[i] = [a, b] means b must come before a.
 *
 *  CONSTRAINTS:
 *    1 <= numCourses <= 2000
 *    0 <= prerequisites.length <= 5000
 *
 *  APPROACH 1: DFS with explicit finish-time stack (post-order reversal)
 *    Time:  O(V + E)
 *    Space: O(V + E) — adjacency list + color[] + result stack
 *
 *  APPROACH 2: Kahn's BFS with lex-smallest ordering (min-heap queue)
 *    Time:  O((V + E) log V)  — heap operations instead of O(1) queue
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Course_Schedule_II_DFS {

    // =========================================================
    // APPROACH 1 — DFS TOPOLOGICAL SORT (REVERSE POST-ORDER)
    // =========================================================

    /**
     * Computes a valid course order using DFS topological sort.
     *
     * DFS TOPO SORT INSIGHT:
     *   When DFS on vertex u FINISHES (all of u's descendants fully explored),
     *   push u onto the result stack. Popping the stack gives topological order.
     *   Why? u finishes AFTER all vertices that depend on u (u must come before them).
     *   Pushing u last and reversing = u appears first (before its dependents).
     *
     * CYCLE DETECTION:
     *   If DFS encounters a GRAY (onStack) vertex → back edge → cycle → return [].
     *
     * @param numCourses    number of courses (vertices)
     * @param prerequisites [a, b] means b before a
     * @return valid course order array, or empty array if cycle
     *
     * Time:  O(V + E)
     * Space: O(V + E)
     */
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        if (numCourses <= 0) return new int[0];

        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adjacencyList.add(new ArrayList<>());

        for (int[] pre : prerequisites) {
            int dependentCourse    = pre[0];
            int prerequisiteCourse = pre[1];
            adjacencyList.get(prerequisiteCourse).add(dependentCourse);
        }

        // 0 = WHITE (unvisited), 1 = GRAY (in stack), 2 = BLACK (done)
        int[] color = new int[numCourses];
        Deque<Integer> finishStack = new ArrayDeque<>();
        boolean[] hasCycle = {false};

        for (int course = 0; course < numCourses; course++) {
            if (color[course] == 0) {
                dfsTopoSort(adjacencyList, course, color, finishStack, hasCycle);
                if (hasCycle[0]) return new int[0];
            }
        }

        // Pop stack into result array (reverse post-order = topological order)
        int[] topologicalOrder = new int[numCourses];
        int index = 0;
        while (!finishStack.isEmpty()) {
            topologicalOrder[index++] = finishStack.pop();
        }
        return topologicalOrder;
    }

    /**
     * DFS post-order traversal with cycle detection.
     * When all neighbors of currentCourse are processed, push to finishStack.
     */
    private void dfsTopoSort(
            List<List<Integer>> adjacencyList,
            int currentCourse,
            int[] color,
            Deque<Integer> finishStack,
            boolean[] hasCycle) {

        if (hasCycle[0]) return;

        color[currentCourse] = 1;   // GRAY: in DFS stack

        for (int neighborCourse : adjacencyList.get(currentCourse)) {
            if (color[neighborCourse] == 1) {
                hasCycle[0] = true;   // back edge → cycle
                return;
            }
            if (color[neighborCourse] == 0) {
                dfsTopoSort(adjacencyList, neighborCourse, color, finishStack, hasCycle);
            }
        }

        color[currentCourse] = 2;          // BLACK: fully processed
        finishStack.push(currentCourse);   // push AFTER all descendants
    }

    // =========================================================
    // APPROACH 2 — KAHN'S BFS WITH LEX-SMALLEST ORDER (MIN-HEAP)
    // =========================================================

    /**
     * Kahn's BFS with a min-heap to produce the lex-smallest topological order.
     * (Standard Kahn's with a regular Queue produces valid but non-lex-smallest order.)
     *
     * WHEN TO USE MIN-HEAP:
     *   Problem says "If multiple valid orderings exist, return the one with
     *   smallest course number taken first." → Use PriorityQueue instead of Queue.
     *
     * Time:  O((V + E) log V)  — each vertex enqueue/dequeue costs O(log V)
     * Space: O(V + E)
     */
    public int[] findOrderLexSmallest(int numCourses, int[][] prerequisites) {
        if (numCourses <= 0) return new int[0];

        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adjacencyList.add(new ArrayList<>());
        int[] inDegree = new int[numCourses];

        for (int[] pre : prerequisites) {
            adjacencyList.get(pre[1]).add(pre[0]);
            inDegree[pre[0]]++;
        }

        // Min-heap → always processes the course with the smallest index first
        PriorityQueue<Integer> minHeapQueue = new PriorityQueue<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) minHeapQueue.offer(i);
        }

        int[] order = new int[numCourses];
        int index = 0;

        while (!minHeapQueue.isEmpty()) {
            int currentCourse = minHeapQueue.poll();
            order[index++] = currentCourse;

            for (int dependent : adjacencyList.get(currentCourse)) {
                if (--inDegree[dependent] == 0) {
                    minHeapQueue.offer(dependent);
                }
            }
        }

        return index == numCourses ? order : new int[0];
    }

    // =========================================================
    // BONUS: Iterative DFS topological sort (avoids recursion stack overflow)
    // =========================================================

    /**
     * Iterative DFS-based topological sort using an explicit stack.
     * Safe for very large graphs (V = 10^5) without recursion depth limits.
     *
     * IMPLEMENTATION NOTE:
     *   We simulate post-order by using a "visited" flag on second pop.
     *   First time a vertex is popped: push it again + push all unvisited neighbors.
     *   Second time (neighbors exhausted): add to result.
     *
     * Time:  O(V + E)
     * Space: O(V + E) — explicit stack replaces JVM call stack
     */
    public int[] findOrderIterativeDFS(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adjacencyList.add(new ArrayList<>());
        for (int[] pre : prerequisites) adjacencyList.get(pre[1]).add(pre[0]);

        // 0 = unvisited, 1 = on stack (visiting), 2 = done
        int[] state = new int[numCourses];
        Deque<Integer> resultStack = new ArrayDeque<>();
        Deque<Integer> dfsStack = new ArrayDeque<>();

        for (int start = 0; start < numCourses; start++) {
            if (state[start] != 0) continue;

            dfsStack.push(start);

            while (!dfsStack.isEmpty()) {
                int current = dfsStack.peek();

                if (state[current] == 0) {
                    state[current] = 1;   // mark as in-progress

                    boolean pushed = false;
                    for (int neighbor : adjacencyList.get(current)) {
                        if (state[neighbor] == 1) return new int[0];  // cycle
                        if (state[neighbor] == 0) {
                            dfsStack.push(neighbor);
                            pushed = true;
                        }
                    }
                    // If no neighbors pushed, we'll process current on next iteration
                } else if (state[current] == 1) {
                    state[current] = 2;
                    dfsStack.pop();
                    resultStack.push(current);
                } else {
                    dfsStack.pop();  // already fully processed
                }
            }
        }

        int[] order = new int[numCourses];
        int idx = 0;
        while (!resultStack.isEmpty()) order[idx++] = resultStack.pop();
        return order;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Course_Schedule_II_DFS solver = new Problem_2_Medium_Course_Schedule_II_DFS();

        System.out.println("==============================================");
        System.out.println("  Course Schedule II — DFS Topo Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Simple chain ---
        System.out.println("\n--- Test 1: Chain 0→1→2→3 ---");
        int[][] pre1 = {{1,0},{2,1},{3,2}};
        System.out.println("DFS order:  " + Arrays.toString(solver.findOrder(4, pre1)));
        System.out.println("Lex order:  " + Arrays.toString(solver.findOrderLexSmallest(4, pre1)));
        System.out.println("Iter order: " + Arrays.toString(solver.findOrderIterativeDFS(4, pre1)));

        // --- Test 2: Cycle ---
        System.out.println("\n--- Test 2: Cycle (0→1→0) ---");
        int[][] pre2 = {{1,0},{0,1}};
        System.out.println("DFS:  " + Arrays.toString(solver.findOrder(2, pre2)) + " (expected [])");
        System.out.println("Lex:  " + Arrays.toString(solver.findOrderLexSmallest(2, pre2)) + " (expected [])");

        // --- Test 3: Diamond DAG (multiple valid orders) ---
        System.out.println("\n--- Test 3: Diamond 0→{1,2}→3 ---");
        int[][] pre3 = {{1,0},{2,0},{3,1},{3,2}};
        System.out.println("DFS order (one valid): " + Arrays.toString(solver.findOrder(4, pre3)));
        System.out.println("Lex-smallest:          " + Arrays.toString(solver.findOrderLexSmallest(4, pre3)));

        // --- Test 4: No prerequisites ---
        System.out.println("\n--- Test 4: 4 courses, no prerequisites ---");
        System.out.println("DFS:  " + Arrays.toString(solver.findOrder(4, new int[0][])));
        System.out.println("Lex:  " + Arrays.toString(solver.findOrderLexSmallest(4, new int[0][])));

        // --- Test 5: Self-loop ---
        System.out.println("\n--- Test 5: Self-loop [0,0] ---");
        System.out.println("DFS:  " + Arrays.toString(solver.findOrder(2, new int[][]{{0,0}})) + " (expected [])");

        System.out.println("\n==============================================");
        System.out.println("  All Course Schedule II tests completed.");
        System.out.println("==============================================");
    }
}
