/**
 * ============================================================
 *  PATTERN 5 — PRIORITY QUEUES AND HEAP MECHANICS
 *  Problem 3 (Hard): Find Median from Data Stream   LC 295
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Design a data structure that supports:
 *      addNum(int num): add a number from the data stream.
 *      findMedian() double: return the median of all numbers added so far.
 *
 *  DEFINITION OF MEDIAN:
 *    For N numbers in sorted order:
 *      If N is odd:  median = middle element (index N/2).
 *      If N is even: median = average of two middle elements.
 *
 *  CONSTRAINTS:
 *    -10^5 <= num <= 10^5
 *    At most 5 × 10^4 calls to addNum and findMedian each.
 *    findMedian is guaranteed to be called after at least one addNum.
 *
 *  KEY INSIGHT — TWO-HEAP APPROACH:
 *    Maintain two heaps that partition all numbers:
 *      lowerMaxHeap: max-heap containing the LOWER half (≤ median).
 *      upperMinHeap: min-heap containing the UPPER half (≥ median).
 *
 *    BALANCE INVARIANT:
 *      |lowerMaxHeap.size() - upperMinHeap.size()| ≤ 1
 *      lowerMaxHeap.size() ≥ upperMinHeap.size() (lower half may have 1 extra)
 *
 *    MEDIAN DERIVATION (O(1)):
 *      If sizes equal: (lowerMaxHeap.peek() + upperMinHeap.peek()) / 2.0
 *      If lower is larger: lowerMaxHeap.peek()
 *
 *  APPROACH 1: Java PriorityQueue-based two-heap (standard, O(log N) add / O(1) query)
 *  APPROACH 2: Insertion sort into sorted array (O(N) add / O(1) query — simpler, worse)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Find_Median_from_Data_Stream {

    // =========================================================
    // APPROACH 1 — TWO PRIORITYQUEUE HEAPS (OPTIMAL)
    // =========================================================

    /**
     * MedianFinder using a max-heap for lower half and min-heap for upper half.
     *
     * ADD NUMBER PROTOCOL (maintains balance invariant):
     *   Step 1: Route to correct half.
     *     Always offer to lowerMaxHeap first (lets us check boundary).
     *     If num > upperMinHeap.peek(): num belongs to upper half.
     *       Offer to lowerMaxHeap, then move lowerMaxHeap.peek() to upperMinHeap.
     *     Else: num belongs to lower half — stays in lowerMaxHeap.
     *
     *   Step 2: Rebalance sizes.
     *     If upperMinHeap is larger than lowerMaxHeap:
     *       Move upperMinHeap.peek() to lowerMaxHeap.
     *
     *   Simple alternative used here: ALWAYS offer to lower, then rebalance.
     *
     * INSERTION ALGORITHM (used below — cleaner):
     *   1. Offer num to lowerMaxHeap.
     *   2. Move lowerMaxHeap.peek() to upperMinHeap.  (ensures ordering: max(lower) ≤ min(upper))
     *   3. If upperMinHeap is now larger: move upperMinHeap.peek() to lowerMaxHeap.
     *   → After these 3 steps, size invariant is maintained.
     */
    static class MedianFinderTwoHeaps {
        // Max-heap for the LOWER half (negation trick: PriorityQueue is min-heap by default,
        // so negate values to simulate max-heap)
        private final PriorityQueue<Integer> lowerMaxHeap;
        // Min-heap for the UPPER half (default PriorityQueue ordering)
        private final PriorityQueue<Integer> upperMinHeap;

        public MedianFinderTwoHeaps() {
            lowerMaxHeap = new PriorityQueue<>(Collections.reverseOrder());   // max-heap
            upperMinHeap = new PriorityQueue<>();                              // min-heap
        }

        /**
         * Adds a number to the data structure.
         *
         * INSERTION STEPS:
         *   1. num → lowerMaxHeap (may or may not stay there)
         *   2. lowerMaxHeap.peek() → upperMinHeap (cross the boundary: ensures all
         *      lower elements ≤ all upper elements, since we're moving the LARGEST lower)
         *   3. If upper became larger: upperMinHeap.peek() → lowerMaxHeap (rebalance)
         *
         * INVARIANT PROOF AFTER INSERTION:
         *   Case A — num ≤ old max(lower): num stays in lower (step 1), max(lower) moves up.
         *            Upper gains one element → if oversized, step 3 rebalances.
         *   Case B — num > old max(lower): step 1 makes num the new max(lower).
         *            Step 2 moves num to upper. Lower size unchanged. Step 3 skipped.
         *
         * Time: O(log N)
         */
        public void addNum(int num) {
            lowerMaxHeap.offer(num);
            // Enforce: max(lower) ≤ min(upper) — move lower's max to upper
            upperMinHeap.offer(lowerMaxHeap.poll());
            // Rebalance: lower half may hold 1 more than upper, never fewer
            if (upperMinHeap.size() > lowerMaxHeap.size()) {
                lowerMaxHeap.offer(upperMinHeap.poll());
            }
        }

        /**
         * Returns the current median.
         * Time: O(1)
         */
        public double findMedian() {
            if (lowerMaxHeap.isEmpty()) {
                throw new NoSuchElementException("No elements in stream");
            }
            if (lowerMaxHeap.size() > upperMinHeap.size()) {
                // Odd total: lower has one extra — median is top of lower
                return lowerMaxHeap.peek();
            } else {
                // Even total: average of both tops
                return (lowerMaxHeap.peek() + upperMinHeap.peek()) / 2.0;
            }
        }

        public int totalCount() {
            return lowerMaxHeap.size() + upperMinHeap.size();
        }
    }

    // =========================================================
    // APPROACH 2 — SORTED INSERTION (SIMPLER, FOR COMPARISON)
    // =========================================================

    /**
     * MedianFinder using a sorted list (TreeList/ArrayList kept sorted).
     * O(N) insertion but demonstrates the median retrieval concept clearly.
     * Use for small streams or educational purposes only.
     *
     * Time: O(N) per addNum, O(1) per findMedian
     * Space: O(N)
     */
    static class MedianFinderSortedList {
        private final List<Integer> sortedElements = new ArrayList<>();

        public void addNum(int num) {
            // Binary search for insertion position
            int insertionPosition = Collections.binarySearch(sortedElements, num);
            if (insertionPosition < 0) insertionPosition = -(insertionPosition + 1);
            sortedElements.add(insertionPosition, num);   // O(N) shift in ArrayList
        }

        public double findMedian() {
            if (sortedElements.isEmpty()) throw new NoSuchElementException("Empty stream");
            int totalCount = sortedElements.size();
            int midIndex   = totalCount / 2;
            if (totalCount % 2 == 1) {
                return sortedElements.get(midIndex);
            } else {
                return (sortedElements.get(midIndex - 1) + sortedElements.get(midIndex)) / 2.0;
            }
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Find Median from Data Stream — Test Suite");
        System.out.println("========================================");

        // Helper: test both finders produce same result
        java.util.function.Consumer<int[]> testStream = (stream) -> {
            MedianFinderTwoHeaps heapFinder   = new MedianFinderTwoHeaps();
            MedianFinderSortedList listFinder = new MedianFinderSortedList();
            System.out.println("Stream: " + Arrays.toString(stream));
            for (int num : stream) {
                heapFinder.addNum(num);
                listFinder.addNum(num);
                double heapMedian = heapFinder.findMedian();
                double listMedian = listFinder.findMedian();
                boolean ok = Math.abs(heapMedian - listMedian) < 1e-9;
                System.out.printf("  Added %4d | Heap median: %6.1f | List median: %6.1f %s%n",
                    num, heapMedian, listMedian, ok ? "✓" : "FAIL");
            }
            System.out.println();
        };

        System.out.println("\n--- LC 295 Example ---");
        testStream.accept(new int[]{1, 2});
        // 1→1.0, 1,2→1.5

        System.out.println("--- Odd count stream ---");
        testStream.accept(new int[]{3, 1, 2});
        // 3→3, 3,1→2.0, 3,1,2→2

        System.out.println("--- Sorted input ---");
        testStream.accept(new int[]{1, 2, 3, 4, 5});

        System.out.println("--- Reverse sorted ---");
        testStream.accept(new int[]{5, 4, 3, 2, 1});

        System.out.println("--- All same ---");
        testStream.accept(new int[]{7, 7, 7, 7});

        System.out.println("--- Negatives and positives ---");
        testStream.accept(new int[]{-5, 3, -2, 7, 0, 4});

        System.out.println("--- Single element ---");
        MedianFinderTwoHeaps single = new MedianFinderTwoHeaps();
        single.addNum(42);
        System.out.println("Single element median: " + single.findMedian() + " (expected 42.0)");

        System.out.println("\n--- Empty stream defensive check ---");
        try {
            new MedianFinderTwoHeaps().findMedian();
            System.out.println("FAIL: should throw");
        } catch (NoSuchElementException e) {
            System.out.println("PASS: NoSuchElementException on empty findMedian()");
        }

        System.out.println("\n========================================");
        System.out.println("  All Median Stream tests done.");
        System.out.println("========================================");
    }
}
