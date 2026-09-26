/**
 * ============================================================
 *  PATTERN 7 — STATEFUL STREAM PROCESSING
 *  Problem 1 (Basic): Moving Average from Data Stream   LC 346
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a stream of integers and a window size, calculate the moving average
 *    of all integers in the sliding window.
 *
 *  EXAMPLE:
 *    MovingAverage(3)
 *    next(1) → 1.0        (window: [1])
 *    next(10) → 5.5       (window: [1,10])
 *    next(3) → 4.67       (window: [1,10,3])
 *    next(5) → 6.0        (window: [10,3,5])
 *
 *  CONSTRAINTS:
 *    1 <= size (window) <= 1000
 *    -10^5 <= val <= 10^5
 *    At most 10^4 calls to next().
 *
 *  APPROACH 1: Circular buffer int[] + running sum (O(1) per call, O(size) space)
 *    Overwrite the oldest element slot each time. Maintain a running sum — subtract
 *    the outgoing element, add the incoming element.
 *    Time:  O(1) per next() call
 *    Space: O(size) primitives
 *
 *  APPROACH 2: Queue-backed sliding window (ArrayDeque as bounded FIFO)
 *    Time:  O(1) per next() call (amortized)
 *    Space: O(size) — Queue holds at most `size` Integer objects
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Moving_Average {

    // =========================================================
    // APPROACH 1 — CIRCULAR BUFFER (OPTIMAL, ZERO GC)
    // =========================================================

    /**
     * Moving average using a circular buffer and a maintained running sum.
     *
     * CIRCULAR BUFFER INVARIANT:
     *   windowBuffer[writeIndex % windowSize] is the NEXT slot to write.
     *   elementCount = min(totalElementsSeen, windowSize).
     *   runningWindowSum = sum of all current window elements.
     *
     * ON next(x):
     *   1. If buffer is full: subtract the slot we're ABOUT to overwrite (oldest).
     *   2. Write x to buffer[writeIndex % windowSize].
     *   3. Add x to runningWindowSum.
     *   4. Advance writeIndex.
     *   5. Return runningWindowSum / min(writeIndex, windowSize).
     *
     * WHY RUNNING SUM INSTEAD OF RE-SUMMING?
     *   Re-summing K elements per next() = O(K) time.
     *   Maintaining running sum: subtract outgoing, add incoming = O(1) time.
     *   This is the "sliding window sum" optimization.
     */
    static class MovingAverageCircularBuffer {
        private final int[]  windowBuffer;     // circular storage for the last K values
        private final int    windowSize;       // K (maximum window capacity)
        private long         runningWindowSum; // current sum of elements in window
        private int          writeIndex;       // next slot to overwrite (wraps via modulo)
        private int          elementCount;     // elements currently in window (≤ windowSize)

        public MovingAverageCircularBuffer(int windowSize) {
            if (windowSize <= 0) throw new IllegalArgumentException("Window size must be > 0");
            this.windowSize       = windowSize;
            this.windowBuffer     = new int[windowSize];
            this.runningWindowSum = 0L;
            this.writeIndex       = 0;
            this.elementCount     = 0;
        }

        /**
         * Adds a new value to the stream and returns the updated moving average.
         *
         * @param newValue the next integer in the data stream
         * @return current moving average of the last min(totalAdded, windowSize) values
         *
         * Time:  O(1)
         * Space: O(1) per call (fixed circular buffer, no allocation)
         */
        public double next(int newValue) {
            // Determine the slot we will write to
            int targetSlot = writeIndex % windowSize;

            // If buffer is full: subtract the element being overwritten (the oldest)
            if (elementCount == windowSize) {
                runningWindowSum -= windowBuffer[targetSlot];
            } else {
                elementCount++;   // window growing — not yet full
            }

            // Write new value
            windowBuffer[targetSlot] = newValue;
            runningWindowSum        += newValue;
            writeIndex++;

            return (double) runningWindowSum / elementCount;
        }

        public int currentWindowSize() { return elementCount; }
        public long  currentSum()       { return runningWindowSum; }
    }

    // =========================================================
    // APPROACH 2 — QUEUE-BACKED SLIDING WINDOW
    // =========================================================

    /**
     * Moving average using an ArrayDeque as a bounded FIFO queue.
     *
     * QUEUE INVARIANT:
     *   windowQueue.size() <= windowSize at all times.
     *   windowQueue.peek() = OLDEST element in the current window (next to evict).
     *   windowQueue.peekLast() = NEWEST element just added.
     *
     * ON next(x):
     *   If queue.size() == windowSize: dequeue (remove oldest), subtract from sum.
     *   Enqueue x, add to sum.
     *   Return sum / queue.size().
     *
     * COMPARISON WITH CIRCULAR BUFFER:
     *   ArrayDeque: cleaner code, O(1) amortized, but Integer boxing adds GC pressure.
     *   Circular buffer: raw int[], zero boxing, zero GC — preferred for performance.
     */
    static class MovingAverageQueue {
        private final Deque<Integer> windowQueue;
        private final int            windowSize;
        private long                 runningWindowSum;

        public MovingAverageQueue(int windowSize) {
            if (windowSize <= 0) throw new IllegalArgumentException("Window size must be > 0");
            this.windowSize       = windowSize;
            this.windowQueue      = new ArrayDeque<>(windowSize);
            this.runningWindowSum = 0L;
        }

        /**
         * Time:  O(1)
         * Space: O(size) — queue holds at most windowSize Integer objects
         */
        public double next(int newValue) {
            // Evict oldest if window is at capacity
            if (windowQueue.size() == windowSize) {
                runningWindowSum -= windowQueue.poll();   // remove front (oldest)
            }
            windowQueue.offer(newValue);
            runningWindowSum += newValue;
            return (double) runningWindowSum / windowQueue.size();
        }

        public int currentWindowSize() { return windowQueue.size(); }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Moving Average from Data Stream — Test Suite");
        System.out.println("========================================");

        // Helper: add stream to both implementations and compare
        java.util.function.BiConsumer<Integer, int[]> testStream = (windowSize, stream) -> {
            MovingAverageCircularBuffer circBuf = new MovingAverageCircularBuffer(windowSize);
            MovingAverageQueue          queueMA = new MovingAverageQueue(windowSize);
            System.out.println("\nWindow size = " + windowSize + ", Stream = " + Arrays.toString(stream));
            for (int value : stream) {
                double circResult  = circBuf.next(value);
                double queueResult = queueMA.next(value);
                boolean ok = Math.abs(circResult - queueResult) < 1e-9;
                System.out.printf("  next(%5d) → CircBuf=%.4f  Queue=%.4f  %s%n",
                    value, circResult, queueResult, ok ? "✓" : "FAIL");
            }
        };

        System.out.println("\n--- LC 346 Example ---");
        testStream.accept(3, new int[]{1, 10, 3, 5});
        // Expected: 1.0, 5.5, 4.667, 6.0

        System.out.println("\n--- Window size 1 (always last element) ---");
        testStream.accept(1, new int[]{5, 10, 3, 8});
        // Expected: 5.0, 10.0, 3.0, 8.0

        System.out.println("\n--- Window larger than stream ---");
        testStream.accept(10, new int[]{1, 2, 3});
        // Expected: 1.0, 1.5, 2.0

        System.out.println("\n--- Negative values ---");
        testStream.accept(3, new int[]{-5, 10, -3, 6});
        // Expected: -5.0, 2.5, 0.667, 4.333

        System.out.println("\n--- Large values ---");
        testStream.accept(2, new int[]{100000, -100000, 100000, -100000});
        // Expected: 100000.0, 0.0, 0.0, 0.0

        System.out.println("\n--- Single element stream ---");
        MovingAverageCircularBuffer single = new MovingAverageCircularBuffer(5);
        System.out.println("next(42) = " + single.next(42) + " (expected 42.0)");

        System.out.println("\n--- Invalid window size defensive check ---");
        try {
            new MovingAverageCircularBuffer(0);
            System.out.println("FAIL: should have thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: IllegalArgumentException for window size 0");
        }

        System.out.println("\n========================================");
        System.out.println("  All Moving Average tests done.");
        System.out.println("========================================");
    }
}
