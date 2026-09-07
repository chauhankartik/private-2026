/**
 * ============================================================
 *  PATTERN 3 — CORE QUEUE MECHANICS AND BUFFERING
 *  Problem 1 (Basic): Implement Queue Using Stacks   LC 232
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Implement a first-in-first-out (FIFO) queue using only two stacks.
 *    Support: push(x), pop(), peek(), empty().
 *    pop() and peek() are called only when queue is non-empty.
 *
 *  CONSTRAINTS:
 *    1 <= x <= 9
 *    At most 100 calls total.
 *    pop/peek only called when non-empty.
 *
 *  KEY INSIGHT:
 *    Two stacks simulate a queue through "lazy reversal":
 *    - inbox (push stack): receives new elements O(1).
 *    - outbox (pop stack): serves pop/peek in FIFO order.
 *    When outbox is EMPTY on pop/peek request:
 *      Pour ALL of inbox into outbox (reversal gives FIFO order).
 *    Each element poured exactly once → O(1) amortized per operation.
 *
 *  APPROACH 1: Two ArrayDeque stacks (standard collection approach)
 *    Time:  O(1) amortized all operations
 *    Space: O(N)
 *
 *  APPROACH 2: Two array-backed stacks (primitive, zero GC)
 *    Time:  O(1) amortized all operations
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Implement_Queue_Using_Stacks {

    // =========================================================
    // APPROACH 1 — DUAL ARRAYDEQUE STACKS
    // =========================================================

    /**
     * Queue implementation using two ArrayDeque stacks.
     *
     * AMORTIZED ANALYSIS:
     *   Each element traverses: push to inbox (1 op) → pour to outbox (1 op) → pop from outbox (1 op).
     *   Total = 3 operations per element → O(1) amortized.
     *
     *   Worst case for single pop: O(N) when outbox is empty and all N elements are in inbox.
     *   But this costly pour only happens when outbox is empty — which can't happen again
     *   until those N elements are individually popped → O(N) pops pay for the O(N) pour.
     */
    static class QueueWithDequeStacks {
        private final Deque<Integer> inboxStack;    // where push() sends elements
        private final Deque<Integer> outboxStack;   // where pop()/peek() serve elements

        public QueueWithDequeStacks() {
            inboxStack  = new ArrayDeque<>();
            outboxStack = new ArrayDeque<>();
        }

        /**
         * Pushes element to the back of the queue.
         * Time: O(1)
         */
        public void push(int newElement) {
            inboxStack.push(newElement);
        }

        /**
         * Removes and returns the front of the queue.
         * Time: O(1) amortized (O(N) worst when outbox is empty)
         */
        public int pop() {
            ensureOutboxHasElements();
            return outboxStack.pop();
        }

        /**
         * Returns the front element without removing it.
         * Time: O(1) amortized
         */
        public int peek() {
            ensureOutboxHasElements();
            return outboxStack.peek();
        }

        public boolean empty() {
            return inboxStack.isEmpty() && outboxStack.isEmpty();
        }

        public int size() {
            return inboxStack.size() + outboxStack.size();
        }

        /**
         * Lazy transfer: pour inbox into outbox only when outbox is empty.
         * This reversal restores FIFO order — the first-pushed element is now at outbox top.
         */
        private void ensureOutboxHasElements() {
            if (outboxStack.isEmpty()) {
                while (!inboxStack.isEmpty()) {
                    outboxStack.push(inboxStack.pop());
                }
            }
        }
    }

    // =========================================================
    // APPROACH 2 — DUAL PRIMITIVE ARRAY STACKS
    // =========================================================

    /**
     * Queue using two raw int[] arrays as stacks.
     * Zero boxing, zero GC overhead.
     *
     * ARRAY STACK CONVENTION:
     *   inboxStack[0 .. inboxTop]: most recently pushed elements (inboxStack[inboxTop] = latest)
     *   outboxStack[0 .. outboxTop]: FIFO serving order (outboxStack[outboxTop] = FRONT/oldest)
     *
     * POUR OPERATION:
     *   Moves elements from inbox to outbox in reverse order.
     *   Result: outboxStack[outboxTop] = the oldest element = front of queue.
     */
    static class QueueWithArrayStacks {
        private final int[] inboxStack;
        private final int[] outboxStack;
        private int inboxTop;      // -1 = empty inbox
        private int outboxTop;     // -1 = empty outbox
        private final int capacity;

        public QueueWithArrayStacks(int capacity) {
            this.capacity = capacity;
            inboxStack  = new int[capacity];
            outboxStack = new int[capacity];
            inboxTop    = -1;
            outboxTop   = -1;
        }

        public QueueWithArrayStacks() {
            this(10_000);   // default capacity
        }

        /**
         * Pushes to inbox. O(1).
         */
        public void push(int newElement) {
            if (inboxTop == capacity - 1) throw new RuntimeException("Queue is full");
            inboxStack[++inboxTop] = newElement;
        }

        /**
         * Pops from front. O(1) amortized.
         */
        public int pop() {
            if (empty()) throw new EmptyStackException();
            ensureOutboxHasElements();
            return outboxStack[outboxTop--];   // POP from outbox
        }

        /**
         * Peeks front. O(1) amortized.
         */
        public int peek() {
            if (empty()) throw new EmptyStackException();
            ensureOutboxHasElements();
            return outboxStack[outboxTop];
        }

        public boolean empty() {
            return inboxTop == -1 && outboxTop == -1;
        }

        public int size() {
            return (inboxTop + 1) + (outboxTop + 1);
        }

        /**
         * Pours inbox (LIFO) into outbox, resulting in FIFO access from outbox.
         * Only called when outbox is empty.
         */
        private void ensureOutboxHasElements() {
            if (outboxTop == -1) {
                while (inboxTop >= 0) {
                    outboxStack[++outboxTop] = inboxStack[inboxTop--];
                }
            }
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Queue Using Stacks — Test Suite");
        System.out.println("========================================");

        // ---- Deque version ----
        System.out.println("\n--- Deque Stack Queue ---");
        QueueWithDequeStacks dequeQueue = new QueueWithDequeStacks();
        dequeQueue.push(1);
        dequeQueue.push(2);
        dequeQueue.push(3);
        System.out.println("peek: "  + dequeQueue.peek()  + " (expected 1)");
        System.out.println("pop:  "  + dequeQueue.pop()   + " (expected 1)");
        System.out.println("pop:  "  + dequeQueue.pop()   + " (expected 2)");
        dequeQueue.push(4);
        System.out.println("peek: "  + dequeQueue.peek()  + " (expected 3)");
        System.out.println("pop:  "  + dequeQueue.pop()   + " (expected 3)");
        System.out.println("pop:  "  + dequeQueue.pop()   + " (expected 4)");
        System.out.println("empty: " + dequeQueue.empty() + " (expected true)");

        // ---- Array version ----
        System.out.println("\n--- Array Stack Queue ---");
        QueueWithArrayStacks arrayQueue = new QueueWithArrayStacks();
        arrayQueue.push(10);
        arrayQueue.push(20);
        System.out.println("peek: " + arrayQueue.peek() + " (expected 10)");
        System.out.println("pop:  " + arrayQueue.pop()  + " (expected 10)");
        arrayQueue.push(30);
        System.out.println("pop:  " + arrayQueue.pop()  + " (expected 20)");
        System.out.println("pop:  " + arrayQueue.pop()  + " (expected 30)");
        System.out.println("empty: " + arrayQueue.empty() + " (expected true)");

        // ---- Interleaved push/pop ----
        System.out.println("\n--- Interleaved push/pop ---");
        QueueWithDequeStacks interleaved = new QueueWithDequeStacks();
        for (int i = 1; i <= 5; i++) interleaved.push(i);
        System.out.print("Dequeue order: ");
        while (!interleaved.empty()) System.out.print(interleaved.pop() + " ");
        System.out.println("(expected: 1 2 3 4 5)");

        // ---- Mixed interleave ----
        System.out.println("\n--- Mixed interleave ---");
        QueueWithArrayStacks mixedQueue = new QueueWithArrayStacks();
        mixedQueue.push(1);
        System.out.println("pop: " + mixedQueue.pop() + " (expected 1)");
        mixedQueue.push(2);
        mixedQueue.push(3);
        System.out.println("pop: " + mixedQueue.pop() + " (expected 2)");
        System.out.println("pop: " + mixedQueue.pop() + " (expected 3)");

        // ---- EmptyStack defensive ----
        System.out.println("\n--- EmptyStack defensive check ---");
        QueueWithArrayStacks emptyQ = new QueueWithArrayStacks();
        try {
            emptyQ.pop();
            System.out.println("FAIL: should have thrown");
        } catch (EmptyStackException e) {
            System.out.println("PASS: EmptyStackException on empty pop()");
        }

        System.out.println("\n========================================");
        System.out.println("  All Queue Using Stacks tests done.");
        System.out.println("========================================");
    }
}
