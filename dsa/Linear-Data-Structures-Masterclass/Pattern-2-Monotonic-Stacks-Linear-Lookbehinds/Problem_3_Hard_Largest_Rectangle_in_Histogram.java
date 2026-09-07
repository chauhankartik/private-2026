/**
 * ============================================================
 *  PATTERN 2 — MONOTONIC STACKS AND LINEAR LOOKBEHINDS
 *  Problem 3 (Hard): Largest Rectangle in Histogram   LC 84
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an array of non-negative integers representing the heights of bars in a
 *    histogram (each bar has width 1), find the area of the largest rectangle.
 *
 *  EXAMPLE:
 *    heights=[2,1,5,6,2,3]  → 10  (bars 2-3 of height 5 and 6: 5×2=10... wait)
 *    Correct: bars at index 2,3 have heights 5,6. Width=2, height=5 → area=10.
 *
 *  CONSTRAINTS:
 *    1 <= heights.length (= N) <= 10^5
 *    0 <= heights[i] <= 10^4
 *
 *  KEY INSIGHT:
 *    For bar i to be the SHORTEST bar in a rectangle, the rectangle extends left
 *    until the first bar shorter than i (= left boundary) and right until the first
 *    bar shorter than i (= right boundary).
 *    Area for bar i = heights[i] × (rightBoundary - leftBoundary - 1).
 *    Monotonic stack finds both boundaries in O(N) total.
 *
 *  APPROACH 1: Monotonic INCREASING stack — finds left and right smaller boundaries
 *    Time:  O(N)
 *    Space: O(N)
 *
 *  APPROACH 2: Same algorithm with array-backed stack + SENTINEL trick
 *    Time:  O(N)
 *    Space: O(N) — int[] stack, no bounds checks needed with sentinel
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Largest_Rectangle_in_Histogram {

    // =========================================================
    // APPROACH 1 — DEQUE-BACKED MONOTONIC INCREASING STACK
    // =========================================================

    /**
     * Finds the largest rectangle area using a monotonic increasing stack.
     *
     * ALGORITHM:
     *   Scan bars left to right. Stack holds INDICES of bars in INCREASING height order.
     *
     *   When current bar height < stack.top height:
     *     The stack top bar (call it index j, height h) is now "bounded on the right"
     *     by the current position i (first bar shorter than h to the right of j).
     *     The left boundary of j's rectangle = the new stack top after popping j.
     *
     *     Width  = currentIndex - newStackTop - 1
     *     Height = heights[j]
     *     Area   = height × width
     *
     *   Push current index. Continue.
     *
     * SENTINEL TRICK (end of array):
     *   After scanning all bars, remaining stack elements haven't been resolved.
     *   Add a virtual bar of height 0 at position N — this flushes all remaining bars.
     *   Width of flushed bar j = N - newStackTop - 1 (right boundary = N).
     *
     * @param barHeights histogram bar heights
     * @return maximum rectangle area
     *
     * Time:  O(N) — each index pushed once, popped once
     * Space: O(N) — stack holds at most N indices
     */
    public int largestRectangleAreaDeque(int[] barHeights) {
        if (barHeights == null || barHeights.length == 0) return 0;

        int totalBars  = barHeights.length;
        int maximumArea = 0;

        // Monotonic INCREASING stack (top has SMALLEST height among pending bars)
        Deque<Integer> monotonicStack = new ArrayDeque<>();
        monotonicStack.push(-1);   // SENTINEL: left boundary for bars with no shorter bar to their left

        for (int currentBarIndex = 0; currentBarIndex <= totalBars; currentBarIndex++) {
            // Virtual sentinel bar of height 0 at position N flushes remaining stack
            int currentBarHeight = (currentBarIndex == totalBars) ? 0 : barHeights[currentBarIndex];

            // Pop and compute area for all bars taller than current (their right boundary = currentBarIndex)
            while (monotonicStack.peek() != -1
                   && barHeights[monotonicStack.peek()] >= currentBarHeight) {

                int resolvedBarIndex  = monotonicStack.pop();
                int resolvedBarHeight = barHeights[resolvedBarIndex];

                // Left boundary = monotonicStack.peek() (the next element below the popped one)
                // Right boundary = currentBarIndex
                int rectangleWidth = currentBarIndex - monotonicStack.peek() - 1;
                int rectangleArea  = resolvedBarHeight * rectangleWidth;
                maximumArea = Math.max(maximumArea, rectangleArea);
            }

            monotonicStack.push(currentBarIndex);
        }

        return maximumArea;
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED STACK WITH SENTINEL
    // =========================================================

    /**
     * Same algorithm with a raw int[] stack.
     *
     * SENTINEL INDEX -1 at stack[0]:
     *   indexStack[0] = -1 always (never popped).
     *   stackTopPointer starts at 0 (pointing to sentinel).
     *
     * WHY SENTINEL ELIMINATES EMPTY-CHECK:
     *   Without sentinel: must check "is stack empty?" before reading left boundary.
     *   With sentinel: sentinel always provides a left boundary = -1.
     *   Rectangle from start of array: currentBarIndex - (-1) - 1 = currentBarIndex.
     *   Perfect — the width spans from position 0 to currentBarIndex-1.
     *
     * @param barHeights histogram heights
     * @return maximum rectangle area
     *
     * Time:  O(N)
     * Space: O(N+2) primitives — int[] with sentinel, no boxing
     */
    public int largestRectangleAreaArray(int[] barHeights) {
        if (barHeights == null || barHeights.length == 0) return 0;

        int totalBars   = barHeights.length;
        int maximumArea = 0;

        // indexStack[0] = -1 (sentinel, permanent left boundary)
        int[] indexStack    = new int[totalBars + 2];
        int stackTopPointer = 0;
        indexStack[0]       = -1;   // sentinel

        for (int currentBarIndex = 0; currentBarIndex <= totalBars; currentBarIndex++) {
            int currentBarHeight = (currentBarIndex == totalBars) ? 0 : barHeights[currentBarIndex];

            // Pop bars taller than current and compute their max rectangle
            while (stackTopPointer > 0
                   && barHeights[indexStack[stackTopPointer]] >= currentBarHeight) {

                int resolvedIndex  = indexStack[stackTopPointer--];   // POP
                int resolvedHeight = barHeights[resolvedIndex];
                int leftBoundary   = indexStack[stackTopPointer];      // new top after pop
                int rectangleWidth = currentBarIndex - leftBoundary - 1;
                int area           = resolvedHeight * rectangleWidth;
                maximumArea = Math.max(maximumArea, area);
            }

            indexStack[++stackTopPointer] = currentBarIndex;   // PUSH
        }

        return maximumArea;
    }

    /**
     * BONUS — Divide and Conquer approach for conceptual clarity.
     * Time: O(N log N) average, O(N²) worst case (sorted heights).
     * Space: O(log N) to O(N) recursion stack.
     *
     * For each range [left, right]: find the minimum bar (= height limit for full range).
     * Compute: minHeight × (right - left + 1).
     * Recurse on left subrange and right subrange.
     */
    public int largestRectangleDivideConquer(int[] barHeights) {
        if (barHeights == null || barHeights.length == 0) return 0;
        return divideConquer(barHeights, 0, barHeights.length - 1);
    }

    private int divideConquer(int[] heights, int leftBound, int rightBound) {
        if (leftBound > rightBound) return 0;
        if (leftBound == rightBound) return heights[leftBound];

        int minIndex = leftBound;
        for (int i = leftBound + 1; i <= rightBound; i++) {
            if (heights[i] < heights[minIndex]) minIndex = i;
        }

        int fullRangeArea = heights[minIndex] * (rightBound - leftBound + 1);
        int leftSubArea   = divideConquer(heights, leftBound, minIndex - 1);
        int rightSubArea  = divideConquer(heights, minIndex + 1, rightBound);

        return Math.max(fullRangeArea, Math.max(leftSubArea, rightSubArea));
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Largest_Rectangle_in_Histogram solver =
            new Problem_3_Hard_Largest_Rectangle_in_Histogram();

        System.out.println("========================================");
        System.out.println("  Largest Rectangle in Histogram — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<int[]> testAll = (heights) -> {
            int d  = solver.largestRectangleAreaDeque(heights);
            int ar = solver.largestRectangleAreaArray(heights);
            int dc = solver.largestRectangleDivideConquer(heights);
            System.out.printf("Input: %-30s | Deque: %d | Array: %d | DC: %d%n",
                Arrays.toString(heights), d, ar, dc);
        };

        System.out.println("\n--- Standard cases ---");
        testAll.accept(new int[]{2,1,5,6,2,3});   // expected 10
        testAll.accept(new int[]{2,4});             // expected 4
        testAll.accept(new int[]{1,1,1,1});         // expected 4

        System.out.println("\n--- All same height ---");
        testAll.accept(new int[]{5,5,5,5,5});       // expected 25

        System.out.println("\n--- Ascending staircase ---");
        testAll.accept(new int[]{1,2,3,4,5});       // expected 9 (3×3)

        System.out.println("\n--- Descending staircase ---");
        testAll.accept(new int[]{5,4,3,2,1});       // expected 9

        System.out.println("\n--- Single bar ---");
        testAll.accept(new int[]{7});               // expected 7

        System.out.println("\n--- Zero height bars ---");
        testAll.accept(new int[]{0,5,0,5,0});       // expected 5

        System.out.println("\n--- Large plateau in middle ---");
        testAll.accept(new int[]{1,6,6,6,6,1});     // expected 24 (4×6)

        System.out.println("\n--- Two bars ---");
        testAll.accept(new int[]{1,2});             // expected 2
        testAll.accept(new int[]{2,1});             // expected 2

        System.out.println("\n========================================");
        System.out.println("  All Histogram tests completed.");
        System.out.println("========================================");
    }
}
