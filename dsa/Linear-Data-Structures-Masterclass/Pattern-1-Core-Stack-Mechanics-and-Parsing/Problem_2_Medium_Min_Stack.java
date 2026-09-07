/**
 * ============================================================
 *  PATTERN 1 — CORE STACK MECHANICS AND PARSING
 *  Problem 2 (Medium): Min Stack   LC 155
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Design a stack that supports push, pop, top, and getMin in O(1) time.
 *    Implement MinStack class with:
 *      void push(int val)   — pushes val onto the stack
 *      void pop()           — removes the top element
 *      int top()            — gets the top element
 *      int getMin()         — retrieves the minimum element in the stack
 *
 *  CONSTRAINTS:
 *    -2^31 <= val <= 2^31 - 1
 *    pop, top, getMin are called only when stack is non-empty
 *    At most 3 × 10^4 calls
 *
 *  KEY INSIGHT: Maintain a PARALLEL auxiliary min-stack alongside the main stack.
 *    minStack[top] = minimum of ALL elements from index 0 to top.
 *    Whenever we pop the main stack, we also pop the min-stack.
 *    This preserves the "what was the min before this element?" history.
 *
 *  APPROACH 1: Two-Deque implementation (main + auxiliary min-deque)
 *    Time:  O(1) all operations
 *    Space: O(2N) = O(N)
 *
 *  APPROACH 2: Single array-backed structure with paired primitive arrays
 *    Time:  O(1) all operations
 *    Space: O(2N) primitives — no boxing, no GC overhead
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Min_Stack {

    // =========================================================
    // APPROACH 1 — TWO-DEQUE IMPLEMENTATION
    // =========================================================

    /**
     * MinStack using two ArrayDeque instances.
     *
     * DUAL STACK INVARIANT:
     *   mainStack:  standard push/pop/peek behaviour.
     *   minStack:   minStack.peek() == current minimum of ALL elements in mainStack.
     *
     * ON PUSH(x):
     *   Always push x to mainStack.
     *   Push min(x, minStack.peek()) to minStack.
     *   → minStack always reflects the running minimum.
     *
     * ON POP:
     *   Pop both mainStack and minStack in sync.
     *   → minStack.peek() reverts to the minimum BEFORE the popped element.
     *
     * WHY NOT ONLY PUSH TO MINSTACK WHEN x IS A NEW MIN?
     *   Because when we pop the element that happened to be the min, we need
     *   to know what the PREVIOUS min was. If we didn't track it, it's lost.
     *   By always pushing to minStack, we preserve the full min history.
     */
    static class MinStackDeque {
        private final Deque<Integer> mainStack;
        private final Deque<Integer> auxiliaryMinStack;

        public MinStackDeque() {
            mainStack         = new ArrayDeque<>();
            auxiliaryMinStack = new ArrayDeque<>();
        }

        /**
         * Pushes val onto the stack and updates the running minimum.
         * Time: O(1)
         */
        public void push(int newValue) {
            mainStack.push(newValue);

            // Running minimum: if minStack is empty, newValue IS the minimum.
            int currentMinimum = auxiliaryMinStack.isEmpty()
                ? newValue
                : Math.min(newValue, auxiliaryMinStack.peek());
            auxiliaryMinStack.push(currentMinimum);
        }

        /**
         * Removes the top element. Both stacks pop in sync.
         * Time: O(1)
         */
        public void pop() {
            if (mainStack.isEmpty()) throw new EmptyStackException();
            mainStack.pop();
            auxiliaryMinStack.pop();
        }

        /**
         * Returns the top element without removing it.
         * Time: O(1)
         */
        public int top() {
            if (mainStack.isEmpty()) throw new EmptyStackException();
            return mainStack.peek();
        }

        /**
         * Returns the current minimum element in O(1).
         * Time: O(1)
         */
        public int getMin() {
            if (auxiliaryMinStack.isEmpty()) throw new EmptyStackException();
            return auxiliaryMinStack.peek();
        }

        public boolean isEmpty() { return mainStack.isEmpty(); }
        public int size() { return mainStack.size(); }
    }

    // =========================================================
    // APPROACH 2 — PAIRED PRIMITIVE ARRAY STACKS
    // =========================================================

    /**
     * MinStack using two raw int[] arrays — zero boxing, zero GC overhead.
     *
     * ARRAY STACK INVARIANT:
     *   valueArray[0 .. stackTopPointer]:   the pushed values (main stack)
     *   minimumArray[0 .. stackTopPointer]: running minimums (min stack)
     *   stackTopPointer == -1 → both stacks are empty.
     *
     * PUSH: increment stackTopPointer, write to both arrays.
     * POP:  decrement stackTopPointer (both arrays auto-"shrink").
     * TOP:  read valueArray[stackTopPointer].
     * MIN:  read minimumArray[stackTopPointer].
     *
     * CAPACITY: fixed at construction time.
     * For dynamic growth: would need array doubling (not implemented here —
     * constraints limit to 3×10^4 calls, so capacity=30001 suffices).
     */
    static class MinStackArray {
        private final int[] valueArray;
        private final int[] minimumArray;
        private int stackTopPointer;
        private final int capacity;

        public MinStackArray(int capacity) {
            this.capacity = capacity;
            this.valueArray   = new int[capacity];
            this.minimumArray = new int[capacity];
            this.stackTopPointer = -1;
        }

        public MinStackArray() {
            this(30_001);   // sufficient for problem constraints
        }

        /**
         * Pushes newValue and records the new running minimum.
         * Time: O(1)  Space: amortized O(1) (fixed array, no allocation)
         */
        public void push(int newValue) {
            if (stackTopPointer == capacity - 1) {
                throw new StackOverflowError("MinStack capacity exceeded: " + capacity);
            }
            stackTopPointer++;
            valueArray[stackTopPointer] = newValue;

            if (stackTopPointer == 0) {
                minimumArray[stackTopPointer] = newValue;   // first element is its own min
            } else {
                minimumArray[stackTopPointer] =
                    Math.min(newValue, minimumArray[stackTopPointer - 1]);
            }
        }

        /**
         * Pops the top element. Simply decrements the pointer.
         * Time: O(1)
         */
        public void pop() {
            if (stackTopPointer == -1) throw new EmptyStackException();
            stackTopPointer--;
        }

        /** Returns top value. Time: O(1) */
        public int top() {
            if (stackTopPointer == -1) throw new EmptyStackException();
            return valueArray[stackTopPointer];
        }

        /** Returns current minimum. Time: O(1) */
        public int getMin() {
            if (stackTopPointer == -1) throw new EmptyStackException();
            return minimumArray[stackTopPointer];
        }

        public boolean isEmpty() { return stackTopPointer == -1; }
        public int size()        { return stackTopPointer + 1; }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Min Stack — Test Suite");
        System.out.println("========================================");

        // ---- Test Deque version ----
        System.out.println("\n--- Deque MinStack ---");
        MinStackDeque dequeStack = new MinStackDeque();
        dequeStack.push(-2);
        dequeStack.push(0);
        dequeStack.push(-3);
        System.out.println("getMin after push(-2,0,-3): " + dequeStack.getMin() + " (expected -3)");
        dequeStack.pop();
        System.out.println("top after pop:              " + dequeStack.top()    + " (expected 0)");
        System.out.println("getMin after pop:           " + dequeStack.getMin() + " (expected -2)");

        // ---- Test Array version ----
        System.out.println("\n--- Array MinStack ---");
        MinStackArray arrayStack = new MinStackArray();
        arrayStack.push(5);
        arrayStack.push(3);
        arrayStack.push(7);
        arrayStack.push(1);
        System.out.println("getMin after push(5,3,7,1): " + arrayStack.getMin() + " (expected 1)");
        arrayStack.pop();  // pop 1
        System.out.println("getMin after popping 1:     " + arrayStack.getMin() + " (expected 3)");
        arrayStack.pop();  // pop 7
        System.out.println("getMin after popping 7:     " + arrayStack.getMin() + " (expected 3)");
        arrayStack.pop();  // pop 3
        System.out.println("getMin after popping 3:     " + arrayStack.getMin() + " (expected 5)");

        // ---- Edge: single element ----
        System.out.println("\n--- Single element ---");
        MinStackArray singleStack = new MinStackArray();
        singleStack.push(42);
        System.out.println("top: " + singleStack.top()    + " (expected 42)");
        System.out.println("min: " + singleStack.getMin() + " (expected 42)");

        // ---- Edge: duplicate minimums ----
        System.out.println("\n--- Duplicate minimums ---");
        MinStackDeque dupStack = new MinStackDeque();
        dupStack.push(0);
        dupStack.push(1);
        dupStack.push(0);
        System.out.println("getMin: " + dupStack.getMin() + " (expected 0)");
        dupStack.pop();
        System.out.println("getMin after pop: " + dupStack.getMin() + " (expected 0)");
        dupStack.pop();
        System.out.println("getMin after pop: " + dupStack.getMin() + " (expected 0)");

        // ---- Edge: negative values ----
        System.out.println("\n--- Negative values ---");
        MinStackArray negStack = new MinStackArray();
        negStack.push(Integer.MIN_VALUE);
        negStack.push(Integer.MAX_VALUE);
        System.out.println("getMin: " + negStack.getMin() + " (expected Integer.MIN_VALUE)");

        // ---- EmptyStack exception handling ----
        System.out.println("\n--- EmptyStack defensive check ---");
        MinStackArray emptyStack = new MinStackArray();
        try {
            emptyStack.top();
            System.out.println("FAIL: should have thrown EmptyStackException");
        } catch (EmptyStackException e) {
            System.out.println("PASS: EmptyStackException correctly thrown on top()");
        }

        System.out.println("\n========================================");
        System.out.println("  All Min Stack tests completed.");
        System.out.println("========================================");
    }
}
