/**
 * ============================================================
 *  PATTERN 7 — STATEFUL STREAM PROCESSING
 *  Problem 2 (Medium): Online Stock Span   LC 901
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Design an algorithm that collects daily stock prices and returns the span
 *    of the stock's price for the current day. The span is the number of
 *    consecutive days (up to and including today) that the price was LESS THAN
 *    OR EQUAL TO today's price.
 *
 *  EXAMPLE:
 *    StockSpanner spanner = new StockSpanner()
 *    spanner.next(100) → 1   (no previous days)
 *    spanner.next(80)  → 1   (80 < 100, so only today)
 *    spanner.next(60)  → 1
 *    spanner.next(70)  → 2   (70 ≥ 60, so today + day3)
 *    spanner.next(60)  → 1
 *    spanner.next(75)  → 4   (75 ≥ 60,70,60 but not 80)
 *    spanner.next(85)  → 6
 *
 *  CONSTRAINTS:
 *    1 <= price <= 10^5
 *    At most 10^4 calls to next().
 *
 *  KEY INSIGHT — ACCUMULATED SPAN TRICK:
 *    Instead of storing each individual day, store (price, accumulatedSpan) pairs.
 *    When day j is dominated by (absorbed into) day i's span, we add j's accumulated
 *    span in O(1) — bypassing all the days j had already absorbed.
 *    Each day is pushed once and popped at most once → O(1) amortized per call.
 *
 *  APPROACH 1: Deque-backed stack of {price, span} pairs
 *    Time:  O(1) amortized per next() call
 *    Space: O(N) — stack grows with calls (never evicts until dominated)
 *
 *  APPROACH 2: Array-backed parallel stacks (priceStack + spanStack) — zero GC
 *    Time:  O(1) amortized
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Online_Stock_Span {

    // =========================================================
    // APPROACH 1 — DEQUE STACK OF (PRICE, ACCUMULATED SPAN) PAIRS
    // =========================================================

    /**
     * Stock spanner using a Deque of int[]{price, span} pairs.
     *
     * STACK INVARIANT (monotonic DECREASING by price):
     *   Stack stores only "active boundaries" — prices that have NOT been dominated yet.
     *   When a new price P dominates the top (top.price ≤ P):
     *     Absorb top.span into current span (leapfrog over absorbed days).
     *     Pop top (it's now subsumed by today's span).
     *   Push (P, currentSpan).
     *
     * WHY ACCUMULATE SPAN?
     *   Day 5 (price 75) absorbed day 4 (span 2, price 70).
     *   Day 70 had already absorbed day 3 (price 60, span 1).
     *   So day 75's span = 1 (today) + span(70) = 1 + 2 + 1 = 4.
     *   We add 70's span (2) because 70 "represents" both itself AND day 3.
     *   Without accumulated spans, we'd need to re-check those days.
     *
     * AMORTIZED ANALYSIS:
     *   Each day is pushed exactly once and popped at most once.
     *   Total pops across all N calls ≤ N → O(N) total → O(1) amortized per call.
     */
    static class StockSpannerDeque {
        // Stack of {price, accumulatedSpan} pairs (monotonic decreasing by price)
        private final Deque<int[]> priceSpanStack;

        public StockSpannerDeque() {
            priceSpanStack = new ArrayDeque<>();
        }

        /**
         * Returns the span of today's price.
         * Span = consecutive days ≤ todayPrice including today.
         *
         * @param todayPrice  today's stock price
         * @return span count (always ≥ 1)
         *
         * Time:  O(1) amortized
         */
        public int next(int todayPrice) {
            int accumulatedSpan = 1;  // start with just today

            // Absorb all stack entries dominated by todayPrice (price ≤ todayPrice)
            while (!priceSpanStack.isEmpty()
                   && priceSpanStack.peek()[0] <= todayPrice) {
                int[] dominatedDay = priceSpanStack.pop();
                accumulatedSpan += dominatedDay[1];  // absorb that day's full span
            }

            // Push today as a new boundary
            priceSpanStack.push(new int[]{todayPrice, accumulatedSpan});
            return accumulatedSpan;
        }

        public int totalDays() { return priceSpanStack.stream().mapToInt(e -> e[1]).sum(); }
    }

    // =========================================================
    // APPROACH 2 — PARALLEL ARRAY STACKS (ZERO GC, PRIMITIVES)
    // =========================================================

    /**
     * Stock spanner using two parallel int[] arrays (prices and spans) as a stack.
     * Eliminates int[] object allocation per entry — zero GC overhead.
     *
     * ARRAY STACK INVARIANT:
     *   priceArray[0 .. stackTopPointer]:  prices of active boundary days
     *   spanArray[0 .. stackTopPointer]:   accumulated spans of those days
     *   stackTopPointer == -1 → empty (no prior days to compare against)
     *
     * CAPACITY:
     *   Bounded by number of next() calls (at most 10^4 per constraints).
     *   We allocate 10_001 entries to be safe.
     */
    static class StockSpannerArray {
        private final int[] priceArray;  // prices at stack entries
        private final int[] spanArray;   // accumulated spans at stack entries
        private int         stackTopPointer;
        private final int   capacity;

        public StockSpannerArray(int maxCalls) {
            this.capacity         = maxCalls;
            this.priceArray       = new int[maxCalls];
            this.spanArray        = new int[maxCalls];
            this.stackTopPointer  = -1;
        }

        public StockSpannerArray() { this(10_001); }

        /**
         * Returns the stock span for today's price.
         * Time: O(1) amortized
         */
        public int next(int todayPrice) {
            int accumulatedSpan = 1;

            // Pop all dominated entries (their price ≤ todayPrice)
            while (stackTopPointer >= 0 && priceArray[stackTopPointer] <= todayPrice) {
                accumulatedSpan += spanArray[stackTopPointer];
                stackTopPointer--;   // POP
            }

            // PUSH today
            stackTopPointer++;
            priceArray[stackTopPointer] = todayPrice;
            spanArray[stackTopPointer]  = accumulatedSpan;

            return accumulatedSpan;
        }
    }

    /**
     * BONUS — Brute force O(N) per call for correctness validation.
     * Stores all prices seen and scans backward for each query.
     */
    static class StockSpannerBrute {
        private final List<Integer> allPrices = new ArrayList<>();

        public int next(int price) {
            allPrices.add(price);
            int span = 1;
            int index = allPrices.size() - 2;  // start from previous day
            while (index >= 0 && allPrices.get(index) <= price) {
                span++;
                index--;
            }
            return span;
        }
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Online Stock Span — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<int[]> testAll = (prices) -> {
            StockSpannerDeque dequeSpanner = new StockSpannerDeque();
            StockSpannerArray arraySpanner = new StockSpannerArray();
            StockSpannerBrute bruteSpanner = new StockSpannerBrute();

            System.out.println("\nPrices: " + Arrays.toString(prices));
            System.out.printf("%-12s %-12s %-12s %-12s%n", "Price", "Deque", "Array", "Brute");

            for (int price : prices) {
                int d = dequeSpanner.next(price);
                int a = arraySpanner.next(price);
                int b = bruteSpanner.next(price);
                boolean ok = (d == a && a == b);
                System.out.printf("%-12d %-12d %-12d %-12d %s%n",
                    price, d, a, b, ok ? "✓" : "FAIL");
            }
        };

        System.out.println("\n--- LC 901 Example ---");
        testAll.accept(new int[]{100, 80, 60, 70, 60, 75, 85});
        // Expected spans: [1, 1, 1, 2, 1, 4, 6]

        System.out.println("\n--- Strictly increasing (spans grow) ---");
        testAll.accept(new int[]{1, 2, 3, 4, 5});
        // Expected: [1, 2, 3, 4, 5]

        System.out.println("\n--- Strictly decreasing (all spans = 1) ---");
        testAll.accept(new int[]{5, 4, 3, 2, 1});
        // Expected: [1, 1, 1, 1, 1]

        System.out.println("\n--- All same (all spans grow linearly) ---");
        testAll.accept(new int[]{3, 3, 3, 3, 3});
        // Expected: [1, 2, 3, 4, 5]

        System.out.println("\n--- Single price ---");
        testAll.accept(new int[]{42});
        // Expected: [1]

        System.out.println("\n--- Price spike then drop ---");
        testAll.accept(new int[]{3, 1, 4, 1, 5, 9, 2, 6});

        System.out.println("\n========================================");
        System.out.println("  All Online Stock Span tests done.");
        System.out.println("========================================");
    }
}
