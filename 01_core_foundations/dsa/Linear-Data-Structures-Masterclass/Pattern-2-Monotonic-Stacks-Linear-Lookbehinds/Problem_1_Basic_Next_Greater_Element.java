/**
 * ============================================================
 *  PATTERN 2 — MONOTONIC STACKS AND LINEAR LOOKBEHINDS
 *  Problem 1 (Basic): Next Greater Element   LC 496 / 503
 * ============================================================
 *
 *  PROBLEM STATEMENT (LC 496):
 *    Given two distinct integer arrays nums1 and nums2 (nums1 is a subset of nums2),
 *    for each element in nums1, find the Next Greater Element in nums2.
 *    The NGE for a query element q is the first element to the RIGHT of q in nums2
 *    that is greater than q. Return -1 if none exists.
 *
 *  EXAMPLE:
 *    nums1=[4,1,2], nums2=[1,3,4,2]
 *    Output: [-1,3,-1]   (4's NGE=-1, 1's NGE=3, 2's NGE=-1)
 *
 *  CONSTRAINTS:
 *    1 <= nums1.length (= M) <= nums2.length (= N) <= 1000
 *    All elements are distinct.
 *    All elements of nums1 appear in nums2.
 *
 *  APPROACH 1: Monotonic stack + HashMap precomputation
 *    Time:  O(N + M)  — O(N) to build NGE map, O(M) to answer queries
 *    Space: O(N)      — stack + HashMap
 *
 *  APPROACH 2: Array-based stack with manual index mapping (no HashMap)
 *    Time:  O(N + M)
 *    Space: O(N)      — int[] stack, O(N) NGE array indexed by position
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Next_Greater_Element {

    // =========================================================
    // APPROACH 1 — MONOTONIC STACK + HASHMAP
    // =========================================================

    /**
     * Finds the next greater element for each query in nums1 using nums2 as source.
     *
     * ALGORITHM (precompute all NGEs in nums2):
     *   Maintain a monotonic DECREASING stack (stores elements waiting for their NGE).
     *   For each element in nums2 (left to right):
     *     While stack is not empty AND stack.top < currentElement:
     *       stack.top's NGE = currentElement.  Record in ngeMap.
     *       Pop from stack.
     *     Push currentElement onto stack.
     *   After scanning: any remaining elements in stack have NGE = -1.
     *
     *   Then: for each query in nums1, look up ngeMap.
     *
     * WHY MONOTONIC DECREASING STACK?
     *   The stack holds elements in decreasing order (top is smallest of remaining).
     *   When a new element Y is larger than the top X, Y is X's first greater element.
     *   We pop X immediately — it's "resolved."
     *
     * @param queryElements  nums1: subset of sourceArray whose NGEs we want
     * @param sourceArray    nums2: the array in which we find NGEs
     * @return NGE array for each element of queryElements
     *
     * Time:  O(N + M)
     * Space: O(N)
     */
    public int[] nextGreaterElementDeque(int[] queryElements, int[] sourceArray) {
        if (queryElements == null || sourceArray == null) return new int[0];

        // Precompute: value → its NGE in sourceArray
        Map<Integer, Integer> ngeMap = new HashMap<>();
        Deque<Integer> monotonicDecreasingStack = new ArrayDeque<>();

        for (int sourceElement : sourceArray) {
            // Pop all elements from stack that are SMALLER than current (their NGE found)
            while (!monotonicDecreasingStack.isEmpty()
                   && monotonicDecreasingStack.peek() < sourceElement) {
                int resolvedElement = monotonicDecreasingStack.pop();
                ngeMap.put(resolvedElement, sourceElement);
            }
            monotonicDecreasingStack.push(sourceElement);
        }
        // Remaining stack elements have no NGE
        while (!monotonicDecreasingStack.isEmpty()) {
            ngeMap.put(monotonicDecreasingStack.pop(), -1);
        }

        // Answer queries
        int[] results = new int[queryElements.length];
        for (int queryIndex = 0; queryIndex < queryElements.length; queryIndex++) {
            results[queryIndex] = ngeMap.getOrDefault(queryElements[queryIndex], -1);
        }
        return results;
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED STACK (PRIMITIVES, NO HASHMAP FOR LC 503 CIRCULAR)
    // =========================================================

    /**
     * Computes Next Greater Element for every element of a CIRCULAR array (LC 503).
     * Each element's NGE is the first element to its right (wrapping around) that is greater.
     *
     * CIRCULAR TRICK: Run the stack algorithm TWICE over the array (2N passes total).
     *   First pass: resolves most elements.
     *   Second pass: handles wrap-around (elements near the end whose NGE is near the start).
     *
     * ARRAY STACK: stores INDICES (not values) for O(1) array access.
     *
     * @param circularArray input array treated as circular
     * @return nge[i] = Next Greater Element for circularArray[i], or -1 if none
     *
     * Time:  O(N)  — each index pushed and popped at most twice (2 passes)
     * Space: O(N)  — result array + int[] stack (no HashMap needed for this variant)
     */
    public int[] nextGreaterElementCircular(int[] circularArray) {
        if (circularArray == null || circularArray.length == 0) return new int[0];

        int arrayLength = circularArray.length;
        int[] nextGreaterResult = new int[arrayLength];
        Arrays.fill(nextGreaterResult, -1);   // default: no NGE found

        // Array-backed stack storing INDICES
        int[] indexStack     = new int[arrayLength];
        int stackTopPointer  = -1;

        // Two passes: indices 0..N-1 (first pass) then 0..N-1 (second pass for wrap)
        for (int pass = 0; pass < 2; pass++) {
            for (int currentIndex = 0; currentIndex < arrayLength; currentIndex++) {
                int currentValue = circularArray[currentIndex];

                // Resolve all elements in stack that are smaller than currentValue
                while (stackTopPointer >= 0
                       && circularArray[indexStack[stackTopPointer]] < currentValue) {
                    int resolvedIndex = indexStack[stackTopPointer--];   // POP
                    nextGreaterResult[resolvedIndex] = currentValue;
                }

                // Only push during FIRST pass (second pass only resolves, doesn't add new)
                if (pass == 0) {
                    indexStack[++stackTopPointer] = currentIndex;   // PUSH index
                }
            }
        }

        return nextGreaterResult;
    }

    /**
     * Simple NGE for a single flat array (no subset queries, no circular).
     * Demonstrates the raw monotonic stack pattern clearly.
     *
     * Time: O(N), Space: O(N)
     */
    public int[] nextGreaterElementSingleArray(int[] inputArray) {
        if (inputArray == null || inputArray.length == 0) return new int[0];
        int length = inputArray.length;
        int[] result     = new int[length];
        int[] indexStack = new int[length];
        int stackTop     = -1;
        Arrays.fill(result, -1);

        for (int i = 0; i < length; i++) {
            while (stackTop >= 0 && inputArray[indexStack[stackTop]] < inputArray[i]) {
                result[indexStack[stackTop--]] = inputArray[i];
            }
            indexStack[++stackTop] = i;
        }
        return result;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Next_Greater_Element solver = new Problem_1_Basic_Next_Greater_Element();

        System.out.println("========================================");
        System.out.println("  Next Greater Element — Test Suite");
        System.out.println("========================================");

        System.out.println("\n--- LC 496: Subset NGE ---");
        System.out.println(Arrays.toString(solver.nextGreaterElementDeque(
            new int[]{4,1,2}, new int[]{1,3,4,2}))
            + " (expected [-1, 3, -1])");
        System.out.println(Arrays.toString(solver.nextGreaterElementDeque(
            new int[]{2,4}, new int[]{1,2,3,4}))
            + " (expected [3, -1])");
        System.out.println(Arrays.toString(solver.nextGreaterElementDeque(
            new int[]{1}, new int[]{1,2,3}))
            + " (expected [2])");

        System.out.println("\n--- LC 503: Circular NGE ---");
        System.out.println(Arrays.toString(solver.nextGreaterElementCircular(
            new int[]{1,2,1}))
            + " (expected [2, -1, 2])");
        System.out.println(Arrays.toString(solver.nextGreaterElementCircular(
            new int[]{1,2,3,4,3}))
            + " (expected [2,3,4,-1,4])");
        System.out.println(Arrays.toString(solver.nextGreaterElementCircular(
            new int[]{5,4,3,2,1}))
            + " (expected [-1,5,5,5,5])");

        System.out.println("\n--- Single array NGE ---");
        System.out.println(Arrays.toString(solver.nextGreaterElementSingleArray(
            new int[]{4,5,2,25}))
            + " (expected [5, 25, 25, -1])");
        System.out.println(Arrays.toString(solver.nextGreaterElementSingleArray(
            new int[]{13,7,6,12}))
            + " (expected [-1, 12, 12, -1])");

        System.out.println("\n--- Edge cases ---");
        System.out.println(Arrays.toString(solver.nextGreaterElementSingleArray(
            new int[]{1}))
            + " (expected [-1])");
        System.out.println(Arrays.toString(solver.nextGreaterElementCircular(
            new int[]{1,1,1,1}))
            + " (expected [-1,-1,-1,-1])");

        System.out.println("\n========================================");
        System.out.println("  All NGE tests completed.");
        System.out.println("========================================");
    }
}
