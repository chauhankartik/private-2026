/**
 * ============================================================
 *  PATTERN 6 — TWO-POINTER AND STACK HYBRIDS
 *  Problem 2 (Medium): Trapping Rain Water   LC 42
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given n non-negative integers representing an elevation map where each bar
 *    has width 1, compute how much rainwater it can trap after raining.
 *
 *  EXAMPLE:
 *    height=[0,1,0,2,1,0,1,3,2,1,2,1]  → 6
 *    height=[4,2,0,3,2,5]              → 9
 *
 *  CONSTRAINTS:
 *    n = height.length, 1 <= n <= 2 × 10^4
 *    0 <= height[i] <= 10^5
 *
 *  WATER AT POSITION i = min(maxLeftHeight[i], maxRightHeight[i]) - height[i]
 *    (bounded by shorter wall, net of the bar's own height)
 *
 *  APPROACH 1: Precomputed left/right max arrays
 *    Time:  O(N), Space: O(N)
 *
 *  APPROACH 2: Two-pointer scan — O(1) space
 *    Observation: if leftMax < rightMax, the trapped water at leftPointer is
 *    determined entirely by leftMax (rightMax is guaranteed ≥ leftMax).
 *    No need to precompute right max array.
 *    Time: O(N), Space: O(1)
 *
 *  APPROACH 3: Monotonic stack
 *    Horizontal layer-by-layer computation of trapped water.
 *    Time: O(N), Space: O(N)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Trapping_Rain_Water {

    // =========================================================
    // APPROACH 1 — PRECOMPUTED LEFT/RIGHT MAX ARRAYS
    // =========================================================

    /**
     * Computes trapped rainwater using precomputed left-max and right-max arrays.
     *
     * PRECOMPUTATION:
     *   leftMaxHeight[i] = max(height[0], height[1], ..., height[i])
     *   rightMaxHeight[i] = max(height[i], height[i+1], ..., height[N-1])
     *
     * WATER AT i = max(0, min(leftMaxHeight[i], rightMaxHeight[i]) - height[i])
     *   If min(walls) <= height[i]: no water (bar itself is above the water level).
     *   The max(0, ...) guard handles this case.
     *
     * @param height elevation map
     * @return total units of rainwater trapped
     *
     * Time:  O(N)  — three linear passes
     * Space: O(N)  — two auxiliary int[] arrays
     */
    public int trapPrecomputedArrays(int[] height) {
        if (height == null || height.length < 3) return 0;

        int totalBars = height.length;
        int[] leftMaxHeight  = new int[totalBars];
        int[] rightMaxHeight = new int[totalBars];

        // Build leftMaxHeight — left to right pass
        leftMaxHeight[0] = height[0];
        for (int i = 1; i < totalBars; i++) {
            leftMaxHeight[i] = Math.max(leftMaxHeight[i - 1], height[i]);
        }

        // Build rightMaxHeight — right to left pass
        rightMaxHeight[totalBars - 1] = height[totalBars - 1];
        for (int i = totalBars - 2; i >= 0; i--) {
            rightMaxHeight[i] = Math.max(rightMaxHeight[i + 1], height[i]);
        }

        // Compute total water
        int totalTrappedWater = 0;
        for (int position = 0; position < totalBars; position++) {
            int waterLevel = Math.min(leftMaxHeight[position], rightMaxHeight[position]);
            int waterAtPosition = waterLevel - height[position];   // always >= 0 by construction
            totalTrappedWater += waterAtPosition;
        }

        return totalTrappedWater;
    }

    // =========================================================
    // APPROACH 2 — TWO-POINTER O(1) SPACE (OPTIMAL)
    // =========================================================

    /**
     * Computes trapped rainwater with O(1) space using two converging pointers.
     *
     * KEY INSIGHT:
     *   Water at position i = min(maxLeft[i], maxRight[i]) - height[i].
     *   We DON'T need the full right-max array. Here's why:
     *
     *   Maintain: leftPointer (moving right) and rightPointer (moving left).
     *   Track:    currentLeftMax (max seen from left up to leftPointer)
     *             currentRightMax (max seen from right up to rightPointer)
     *
     *   IF currentLeftMax <= currentRightMax:
     *     The LIMITING factor at leftPointer is currentLeftMax (right side is >= it).
     *     Water at leftPointer = currentLeftMax - height[leftPointer].
     *     Move leftPointer right.
     *   ELSE:
     *     The limiting factor at rightPointer is currentRightMax.
     *     Water at rightPointer = currentRightMax - height[rightPointer].
     *     Move rightPointer left.
     *
     * CORRECTNESS:
     *   When currentLeftMax <= currentRightMax, maxRight[leftPointer] >= currentRightMax
     *   >= currentLeftMax = maxLeft[leftPointer]. So water = leftMax - height[leftPointer].
     *   The symmetric argument holds for rightPointer.
     *
     * @param height elevation map
     * @return total trapped water
     *
     * Time:  O(N)  — single pass
     * Space: O(1)  — only 4 integer variables
     */
    public int trapTwoPointer(int[] height) {
        if (height == null || height.length < 3) return 0;

        int leftPointer       = 0;
        int rightPointer      = height.length - 1;
        int currentLeftMax    = 0;
        int currentRightMax   = 0;
        int totalTrappedWater = 0;

        while (leftPointer < rightPointer) {
            if (height[leftPointer] <= height[rightPointer]) {
                // Left side is the limiting factor
                currentLeftMax = Math.max(currentLeftMax, height[leftPointer]);
                totalTrappedWater += currentLeftMax - height[leftPointer];
                leftPointer++;
            } else {
                // Right side is the limiting factor
                currentRightMax = Math.max(currentRightMax, height[rightPointer]);
                totalTrappedWater += currentRightMax - height[rightPointer];
                rightPointer--;
            }
        }

        return totalTrappedWater;
    }

    // =========================================================
    // APPROACH 3 — MONOTONIC STACK (HORIZONTAL WATER LAYERS)
    // =========================================================

    /**
     * Computes trapped water using a monotonic stack — horizontal layer approach.
     *
     * CONCEPT:
     *   The stack holds indices of bars in monotonic DECREASING height order.
     *   When we encounter a bar taller than the stack top:
     *     A "basin" has been found: floor = popped top, left wall = new stack top, right wall = current i.
     *     Water width = currentIndex - newStackTop - 1
     *     Water height = min(height[newStackTop], height[currentIndex]) - height[poppedTop]
     *     Accumulate.
     *
     * This approach computes water in HORIZONTAL SLICES (by elevation), not per column.
     *
     * Time:  O(N)
     * Space: O(N) — stack holds at most N indices
     */
    public int trapMonotonicStack(int[] height) {
        if (height == null || height.length < 3) return 0;

        Deque<Integer> monotonicStack = new ArrayDeque<>();
        int totalTrappedWater = 0;

        for (int currentIndex = 0; currentIndex < height.length; currentIndex++) {
            int currentHeight = height[currentIndex];

            // While current bar is taller than stack top: a basin is formed
            while (!monotonicStack.isEmpty() && height[monotonicStack.peek()] < currentHeight) {
                int basinFloorIndex  = monotonicStack.pop();
                int basinFloorHeight = height[basinFloorIndex];

                if (monotonicStack.isEmpty()) break;  // no left wall

                int leftWallIndex  = monotonicStack.peek();
                int leftWallHeight = height[leftWallIndex];

                int basinWidth      = currentIndex - leftWallIndex - 1;
                int waterHeight     = Math.min(leftWallHeight, currentHeight) - basinFloorHeight;
                totalTrappedWater  += waterHeight * basinWidth;
            }

            monotonicStack.push(currentIndex);
        }

        return totalTrappedWater;
    }

    // =========================================================
    // APPROACH 4 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Trapping_Rain_Water solver = new Problem_2_Medium_Trapping_Rain_Water();

        System.out.println("========================================");
        System.out.println("  Trapping Rain Water — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<int[]> testAll = (h) -> {
            int arr   = solver.trapPrecomputedArrays(h);
            int ptr   = solver.trapTwoPointer(h);
            int stack = solver.trapMonotonicStack(h);
            boolean ok = (arr == ptr && ptr == stack);
            System.out.printf("height=%-35s | Arrays=%d 2Ptr=%d Stack=%d %s%n",
                Arrays.toString(h), arr, ptr, stack, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 42 examples ---");
        testAll.accept(new int[]{0,1,0,2,1,0,1,3,2,1,2,1});  // expected 6
        testAll.accept(new int[]{4,2,0,3,2,5});               // expected 9

        System.out.println("\n--- No water (monotonic) ---");
        testAll.accept(new int[]{1,2,3,4,5});  // expected 0
        testAll.accept(new int[]{5,4,3,2,1});  // expected 0

        System.out.println("\n--- Flat --- ");
        testAll.accept(new int[]{3,3,3,3});    // expected 0

        System.out.println("\n--- Valley ---");
        testAll.accept(new int[]{3,0,3});      // expected 3
        testAll.accept(new int[]{3,0,0,3});    // expected 6

        System.out.println("\n--- Pyramid ---");
        testAll.accept(new int[]{0,1,2,3,2,1,0});  // expected 0

        System.out.println("\n--- Single valley ---");
        testAll.accept(new int[]{2,0,2});  // expected 2

        System.out.println("\n--- Edge: length < 3 ---");
        System.out.println("Length 1: " + solver.trapTwoPointer(new int[]{5}) + " (expected 0)");
        System.out.println("Length 2: " + solver.trapTwoPointer(new int[]{3,1}) + " (expected 0)");

        System.out.println("\n========================================");
        System.out.println("  All Trapping Rain Water tests done.");
        System.out.println("========================================");
    }
}
