/**
 * ============================================================
 *  PATTERN 3 — CORE QUEUE MECHANICS AND BUFFERING
 *  Problem 2 (Medium): Design Circular Queue   LC 622
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Design your implementation of the circular queue. It must support:
 *      MyCircularQueue(k)    — constructor, queue size = k
 *      enQueue(value) bool   — inserts value, returns false if full
 *      deQueue() bool        — deletes front, returns false if empty
 *      Front() int           — gets front, -1 if empty
 *      Rear() int            — gets last, -1 if empty
 *      isEmpty() bool
 *      isFull() bool
 *
 *  CONSTRAINTS:
 *    1 <= k <= 1000
 *    0 <= value <= 1000
 *    At most 3000 calls.
 *
 *  KEY DESIGN: Circular buffer with modulo arithmetic.
 *    frontIndex: points to the CURRENT front (first element to dequeue).
 *    rearIndex:  points to the NEXT EMPTY SLOT (where next enQueue writes).
 *    elementCount: tracks current number of elements (distinguishes EMPTY from FULL
 *                  when frontIndex == rearIndex).
 *
 *  APPROACH 1: int[] circular buffer with explicit element count
 *    Time:  O(1) all operations
 *    Space: O(k) primitives
 *
 *  APPROACH 2: int[] with "wasted slot" fullness detection (no size counter)
 *    Time:  O(1) all operations
 *    Space: O(k+1) primitives — allocates capacity+1, uses at most k slots
 * ============================================================
 */

public class Problem_2_Medium_Design_Circular_Queue {

    // =========================================================
    // APPROACH 1 — CIRCULAR BUFFER WITH ELEMENT COUNT
    // =========================================================

    /**
     * Circular queue using a single int[] buffer with a size counter.
     *
     * INVARIANTS:
     *   circularBuffer[frontIndex] = oldest element (next to dequeue).
     *   rearIndex = next empty slot (where enQueue writes).
     *   elementCount = number of elements currently in the queue.
     *
     *   EMPTY:  elementCount == 0
     *   FULL:   elementCount == capacity
     *   REAR  (last element) = circularBuffer[(rearIndex - 1 + capacity) % capacity]
     *
     * WRAPPING:
     *   frontIndex = (frontIndex + 1) % capacity on dequeue.
     *   rearIndex  = (rearIndex  + 1) % capacity on enqueue.
     *   Both wrap around when they reach the end of the array.
     */
    static class CircularQueueWithCount {
        private final int[] circularBuffer;
        private int frontIndex;     // index of oldest element
        private int rearIndex;      // index of next empty slot
        private int elementCount;   // current queue size
        private final int capacity; // maximum capacity

        public CircularQueueWithCount(int capacity) {
            this.capacity      = capacity;
            this.circularBuffer = new int[capacity];
            this.frontIndex    = 0;
            this.rearIndex     = 0;
            this.elementCount  = 0;
        }

        /**
         * Enqueues a new value at the rear.
         * @return true if successful, false if queue is full.
         * Time: O(1)
         */
        public boolean enQueue(int newValue) {
            if (isFull()) return false;
            circularBuffer[rearIndex] = newValue;
            rearIndex = (rearIndex + 1) % capacity;   // advance with wrap
            elementCount++;
            return true;
        }

        /**
         * Dequeues the front element.
         * @return true if successful, false if queue is empty.
         * Time: O(1)
         */
        public boolean deQueue() {
            if (isEmpty()) return false;
            frontIndex = (frontIndex + 1) % capacity;  // advance front with wrap
            elementCount--;
            return true;
        }

        /** Returns front element, -1 if empty. O(1) */
        public int Front() {
            return isEmpty() ? -1 : circularBuffer[frontIndex];
        }

        /** Returns rear element (last inserted), -1 if empty. O(1) */
        public int Rear() {
            if (isEmpty()) return -1;
            // Rear element is at rearIndex - 1 (with wrap)
            int rearElementIndex = (rearIndex - 1 + capacity) % capacity;
            return circularBuffer[rearElementIndex];
        }

        public boolean isEmpty() { return elementCount == 0; }
        public boolean isFull()  { return elementCount == capacity; }
        public int size()        { return elementCount; }
    }

    // =========================================================
    // APPROACH 2 — WASTED SLOT TRICK (NO SIZE COUNTER)
    // =========================================================

    /**
     * Circular queue without a separate elementCount field.
     * Allocates (capacity + 1) slots but only uses at most capacity of them.
     * The "wasted slot" at position rearIndex is never written to — it's always empty.
     *
     * FULLNESS DETECTION (without count):
     *   EMPTY: frontIndex == rearIndex
     *   FULL:  (rearIndex + 1) % (capacity + 1) == frontIndex
     *          (advancing rearIndex would "catch up" to frontIndex)
     *
     * ADVANTAGE: Eliminates the elementCount variable entirely.
     *            Reduces race conditions in concurrent settings (single field to guard).
     *
     * DISADVANTAGE: Wastes 1 slot → effective capacity = k even though allocated k+1.
     */
    static class CircularQueueWastedSlot {
        private final int[] circularBuffer;
        private int frontIndex;
        private int rearIndex;    // rearIndex slot is ALWAYS empty (wasted)
        private final int actualCapacity;  // = k + 1 (allocated)

        public CircularQueueWastedSlot(int requestedCapacity) {
            this.actualCapacity = requestedCapacity + 1;   // allocate one extra
            this.circularBuffer  = new int[actualCapacity];
            this.frontIndex      = 0;
            this.rearIndex       = 0;
        }

        public boolean enQueue(int newValue) {
            if (isFull()) return false;
            circularBuffer[rearIndex] = newValue;
            rearIndex = (rearIndex + 1) % actualCapacity;
            return true;
        }

        public boolean deQueue() {
            if (isEmpty()) return false;
            frontIndex = (frontIndex + 1) % actualCapacity;
            return true;
        }

        public int Front() {
            return isEmpty() ? -1 : circularBuffer[frontIndex];
        }

        public int Rear() {
            if (isEmpty()) return -1;
            return circularBuffer[(rearIndex - 1 + actualCapacity) % actualCapacity];
        }

        public boolean isEmpty() { return frontIndex == rearIndex; }
        public boolean isFull()  { return (rearIndex + 1) % actualCapacity == frontIndex; }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Design Circular Queue — Test Suite");
        System.out.println("========================================");

        // ---- Test 1: LC 622 Example ----
        System.out.println("\n--- LC 622 Example (capacity=3) ---");
        CircularQueueWithCount q1 = new CircularQueueWithCount(3);
        System.out.println("enQueue(1): " + q1.enQueue(1) + " (true)");
        System.out.println("enQueue(2): " + q1.enQueue(2) + " (true)");
        System.out.println("enQueue(3): " + q1.enQueue(3) + " (true)");
        System.out.println("enQueue(4): " + q1.enQueue(4) + " (false — full)");
        System.out.println("Rear():     " + q1.Rear()     + " (3)");
        System.out.println("isFull():   " + q1.isFull()   + " (true)");
        System.out.println("deQueue():  " + q1.deQueue()  + " (true)");
        System.out.println("enQueue(4): " + q1.enQueue(4) + " (true)");
        System.out.println("Rear():     " + q1.Rear()     + " (4)");

        // ---- Test 2: Empty operations ----
        System.out.println("\n--- Empty queue operations ---");
        CircularQueueWithCount emptyQ = new CircularQueueWithCount(3);
        System.out.println("isEmpty:  " + emptyQ.isEmpty()  + " (true)");
        System.out.println("deQueue:  " + emptyQ.deQueue()  + " (false)");
        System.out.println("Front:    " + emptyQ.Front()    + " (-1)");
        System.out.println("Rear:     " + emptyQ.Rear()     + " (-1)");

        // ---- Test 3: Wrap-around ----
        System.out.println("\n--- Wrap-around test ---");
        CircularQueueWithCount wrapQ = new CircularQueueWithCount(3);
        wrapQ.enQueue(1); wrapQ.enQueue(2); wrapQ.enQueue(3);
        wrapQ.deQueue(); wrapQ.deQueue();       // free slots 0 and 1
        wrapQ.enQueue(4); wrapQ.enQueue(5);     // wrap rear around to slots 0 and 1
        System.out.println("Front: " + wrapQ.Front() + " (expected 3)");
        System.out.println("Rear:  " + wrapQ.Rear()  + " (expected 5)");
        System.out.println("Full:  " + wrapQ.isFull() + " (expected true)");

        // ---- Test 4: Wasted-slot version ----
        System.out.println("\n--- Wasted-slot variant (capacity=3) ---");
        CircularQueueWastedSlot wq = new CircularQueueWastedSlot(3);
        System.out.println("enQueue(1): " + wq.enQueue(1));
        System.out.println("enQueue(2): " + wq.enQueue(2));
        System.out.println("enQueue(3): " + wq.enQueue(3));
        System.out.println("enQueue(4): " + wq.enQueue(4) + " (false — full)");
        System.out.println("Front: " + wq.Front() + " (1)");
        System.out.println("Rear:  " + wq.Rear()  + " (3)");

        // ---- Test 5: Single-element queue ----
        System.out.println("\n--- Single element ---");
        CircularQueueWithCount singleQ = new CircularQueueWithCount(1);
        System.out.println("enQueue(99): " + singleQ.enQueue(99) + " (true)");
        System.out.println("Front: " + singleQ.Front() + " (99)");
        System.out.println("Rear:  " + singleQ.Rear()  + " (99)");
        System.out.println("isFull: " + singleQ.isFull() + " (true)");
        System.out.println("deQueue: " + singleQ.deQueue() + " (true)");
        System.out.println("isEmpty: " + singleQ.isEmpty() + " (true)");

        System.out.println("\n========================================");
        System.out.println("  All Circular Queue tests done.");
        System.out.println("========================================");
    }
}
