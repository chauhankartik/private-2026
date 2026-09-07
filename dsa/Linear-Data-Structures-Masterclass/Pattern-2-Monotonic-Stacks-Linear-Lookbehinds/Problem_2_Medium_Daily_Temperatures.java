/**
 * ============================================================
 *  PATTERN 2 — MONOTONIC STACKS AND LINEAR LOOKBEHINDS
 *  Problem 2 (Medium): Daily Temperatures   LC 739
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an array of daily temperatures, return an array answer such that
 *    answer[i] is the number of days you have to wait after the i-th day to
 *    get a warmer temperature. If there is no future warmer day, answer[i] = 0.
 *
 *  EXAMPLE:
 *    temperatures=[73,74,75,71,69,72,76,73]
 *    Output: [1,1,4,2,1,1,0,0]
 *    (Day 0: wait 1 day for 74. Day 2: wait 4 days for 76.)
 *
 *  CONSTRAINTS:
 *    1 <= temperatures.length (= N) <= 10^5
 *    30 <= temperatures[i] <= 100
 *
 *  APPROACH 1: Monotonic DECREASING stack (by temperature) storing INDICES
 *    Time:  O(N) — each index pushed and popped exactly once
 *    Space: O(N) — stack holds at most N indices
 *
 *  APPROACH 2: Array-backed int[] stack — avoids ArrayDeque boxing overhead
 *    Time:  O(N)
 *    Space: O(N) — primitive int[] stack
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Daily_Temperatures {

    // =========================================================
    // APPROACH 1 — DEQUE-BACKED MONOTONIC STACK
    // =========================================================

    /**
     * Computes "days until warmer" for each day using a monotonic decreasing stack.
     *
     * MONOTONIC STACK INVARIANT (decreasing by temperature):
     *   The stack holds INDICES of days in increasing order (left to right).
     *   Temperatures at those indices are in DECREASING order (top has smallest temp).
     *
     * RESOLUTION EVENT:
     *   When we process day i with temperature T:
     *   While stack.top has temperature < T:
     *     That day (let's call it day j, stored as index on stack) has found its
     *     "warmer day" = day i.
     *     answer[j] = i - j   (number of days waited)
     *     Pop j from stack.
     *   Push i.
     *
     * WHY STORE INDICES NOT TEMPERATURES?
     *   We need the distance: answer[j] = currentIndex - indexOfResolvingDay.
     *   Without the index of j, we cannot compute this distance.
     *
     * @param dailyTemperatures array of temperature readings
     * @return waitDays[i] = days until next warmer temperature (0 if none)
     *
     * Time:  O(N)   — each index pushed once, popped once
     * Space: O(N)   — stack holds at most N unresolved indices
     */
    public int[] dailyTemperaturesDeque(int[] dailyTemperatures) {
        if (dailyTemperatures == null || dailyTemperatures.length == 0) return new int[0];

        int totalDays = dailyTemperatures.length;
        int[] waitDays = new int[totalDays];   // default 0 (no warmer day)

        // Monotonic stack stores INDICES of days awaiting their warmer day
        Deque<Integer> unresolveDayStack = new ArrayDeque<>();

        for (int currentDayIndex = 0; currentDayIndex < totalDays; currentDayIndex++) {
            int currentTemperature = dailyTemperatures[currentDayIndex];

            // Resolve all stack entries whose temperature is BELOW today's
            while (!unresolveDayStack.isEmpty()
                   && dailyTemperatures[unresolveDayStack.peek()] < currentTemperature) {

                int coldDayIndex = unresolveDayStack.pop();
                waitDays[coldDayIndex] = currentDayIndex - coldDayIndex;
            }

            // Push today's index — it's waiting for a warmer future day
            unresolveDayStack.push(currentDayIndex);
        }

        // Remaining indices in stack: waitDays already initialized to 0 (default)
        return waitDays;
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED MONOTONIC STACK
    // =========================================================

    /**
     * Same algorithm using a raw int[] as the monotonic stack.
     * Eliminates Integer boxing overhead — important for N = 10^5.
     *
     * STACK POINTER INVARIANT:
     *   indexStack[0 .. stackTopPointer] holds unresolved day indices.
     *   stackTopPointer == -1 → stack is empty.
     *   indexStack[stackTopPointer] = index of most recently added unresolved day.
     *
     * @param dailyTemperatures temperature array
     * @return waitDays array
     *
     * Time:  O(N)
     * Space: O(N) — int[] of exactly N elements (max stack depth = N)
     */
    public int[] dailyTemperaturesArray(int[] dailyTemperatures) {
        if (dailyTemperatures == null || dailyTemperatures.length == 0) return new int[0];

        int totalDays  = dailyTemperatures.length;
        int[] waitDays = new int[totalDays];

        // Array-backed monotonic stack of INDICES
        int[] indexStack    = new int[totalDays];
        int stackTopPointer = -1;   // -1 = empty

        for (int currentDayIndex = 0; currentDayIndex < totalDays; currentDayIndex++) {
            int currentTemperature = dailyTemperatures[currentDayIndex];

            // Pop and resolve all colder days
            while (stackTopPointer >= 0
                   && dailyTemperatures[indexStack[stackTopPointer]] < currentTemperature) {

                int coldDayIndex = indexStack[stackTopPointer--];   // POP
                waitDays[coldDayIndex] = currentDayIndex - coldDayIndex;
            }

            indexStack[++stackTopPointer] = currentDayIndex;   // PUSH
        }

        return waitDays;
    }

    /**
     * BONUS — Optimized backward scan for small temperature range (30..100).
     *
     * Since temperatures are bounded [30..100], we can use a "next warmer position"
     * array where we scan RIGHT to LEFT and use jump pointers.
     *
     * For each temperature t (from 100 down to 30):
     *   Scan positions from right to left.
     *   For position i with temperature t:
     *     Look at position i+1 and jump forward using nextWarmerPosition[]
     *     until we find a position with temperature > t.
     *
     * This is O(N × range) = O(N × 70) = O(N) for this specific temperature range.
     * Avoids the stack entirely — useful when stack allocation is constrained.
     *
     * Time:  O(N × 70) = O(N)  for temperatures in [30..100]
     * Space: O(N)
     */
    public int[] dailyTemperaturesJumpOptimized(int[] dailyTemperatures) {
        if (dailyTemperatures == null || dailyTemperatures.length == 0) return new int[0];

        int totalDays  = dailyTemperatures.length;
        int[] waitDays = new int[totalDays];

        // Scan right to left
        for (int dayIndex = totalDays - 2; dayIndex >= 0; dayIndex--) {
            int nextDayIndex = dayIndex + 1;

            // Jump forward using already-computed waitDays
            while (nextDayIndex < totalDays
                   && dailyTemperatures[nextDayIndex] <= dailyTemperatures[dayIndex]) {
                if (waitDays[nextDayIndex] == 0) {
                    nextDayIndex = totalDays;   // no warmer day ahead
                    break;
                }
                nextDayIndex += waitDays[nextDayIndex];   // jump to its next warmer
            }

            if (nextDayIndex < totalDays) {
                waitDays[dayIndex] = nextDayIndex - dayIndex;
            }
        }

        return waitDays;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Daily_Temperatures solver = new Problem_2_Medium_Daily_Temperatures();

        System.out.println("========================================");
        System.out.println("  Daily Temperatures — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<int[]> testAll = (temps) -> {
            int[] dequeResult = solver.dailyTemperaturesDeque(temps);
            int[] arrayResult = solver.dailyTemperaturesArray(temps);
            int[] jumpResult  = solver.dailyTemperaturesJumpOptimized(temps);
            System.out.println("Input:  " + Arrays.toString(temps));
            System.out.println("Deque:  " + Arrays.toString(dequeResult));
            System.out.println("Array:  " + Arrays.toString(arrayResult));
            System.out.println("Jump:   " + Arrays.toString(jumpResult));
            boolean consistent = Arrays.equals(dequeResult, arrayResult)
                              && Arrays.equals(arrayResult, jumpResult);
            System.out.println("All match: " + consistent);
            System.out.println();
        };

        System.out.println("\n--- LC 739 Example ---");
        testAll.accept(new int[]{73,74,75,71,69,72,76,73});
        // Expected: [1,1,4,2,1,1,0,0]

        System.out.println("--- Strictly decreasing (all 0s) ---");
        testAll.accept(new int[]{100,90,80,70,60});
        // Expected: [0,0,0,0,0]

        System.out.println("--- Strictly increasing ---");
        testAll.accept(new int[]{30,40,50,60,100});
        // Expected: [1,1,1,1,0]

        System.out.println("--- All same temperature ---");
        testAll.accept(new int[]{72,72,72,72});
        // Expected: [0,0,0,0]

        System.out.println("--- Single element ---");
        testAll.accept(new int[]{75});
        // Expected: [0]

        System.out.println("--- Two elements ---");
        testAll.accept(new int[]{70,80});
        // Expected: [1,0]
        testAll.accept(new int[]{80,70});
        // Expected: [0,0]

        System.out.println("========================================");
        System.out.println("  All Daily Temperatures tests done.");
        System.out.println("========================================");
    }
}
