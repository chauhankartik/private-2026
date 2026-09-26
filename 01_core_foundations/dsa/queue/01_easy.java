/**
 * ============================================================
 *  QUEUE — EASY PROBLEMS
 * ============================================================
 *
 *  E1.  Implement Queue using Stacks               LC 232
 *  E2.  Number of Recent Calls                     LC 933
 *  E3.  First Unique Character in a String         LC 387
 *  E4.  Moving Average from Data Stream            LC 346
 *  E5.  Number of Students Unable to Eat Lunch     LC 1700
 *  E6.  Implement Stack using Queues               LC 225
 *  E7.  Time Needed to Buy Tickets                 LC 2073
 *  E8.  Reveal Cards In Increasing Order           LC 950
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class QueueEasy {

    // =========================================================
    // E1. Implement Queue using Stacks  LC 232
    // Pattern: DESIGN — Two Stacks (amortized O(1))
    // =========================================================
    /**
     * Implement FIFO queue using only two LIFO stacks.
     *
     * TWO APPROACHES:
     * (a) Costly push: on push, transfer all from s1→s2, push new, transfer back.
     *     Push O(n), Pop O(1).
     * (b) Costly pop (BETTER): push to inStack. On pop/peek, if outStack is empty,
     *     transfer all from inStack → outStack. Pop from outStack.
     *     Push O(1), Pop AMORTIZED O(1).
     *
     * WHY AMORTIZED O(1):
     *   Each element is transferred from inStack to outStack at most ONCE.
     *   Over n operations: n pushes + n transfers + n pops = 3n = O(n) total.
     *   Per operation: O(1) amortized.
     *
     * INTERVIEW KEY: Explain "amortized" clearly.
     *   "While a single pop CAN take O(n), it only happens after n O(1) pushes.
     *   Spreading that cost over all operations gives O(1) per operation."
     *
     * Follow-up: Can you implement it with only one stack?
     *   Yes — use recursion stack as the second stack.
     *   On pop: recursively pop all, return bottom, push rest back.
     *   But this is O(n) per pop with no amortization benefit.
     */
    static class MyQueue {
        private Deque<Integer> inStack  = new ArrayDeque<>();
        private Deque<Integer> outStack = new ArrayDeque<>();

        public void push(int x) {
            inStack.push(x);
        }

        public int pop() {
            ensureOutStack();
            return outStack.pop();
        }

        public int peek() {
            ensureOutStack();
            return outStack.peek();
        }

        public boolean empty() {
            return inStack.isEmpty() && outStack.isEmpty();
        }

        private void ensureOutStack() {
            if (outStack.isEmpty()) {
                while (!inStack.isEmpty()) {
                    outStack.push(inStack.pop());
                }
            }
        }
    }

    // =========================================================
    // E2. Number of Recent Calls  LC 933
    // Pattern: QUEUE — Sliding Time Window
    // =========================================================
    /**
     * Count requests in the last 3000 milliseconds.
     * ping(t) adds a request at time t and returns the count of
     * requests in [t - 3000, t].
     *
     * Use a queue to store timestamps.
     * On each ping: add t to queue, then remove all timestamps < t - 3000.
     *
     * WHY QUEUE:
     *   Timestamps arrive in increasing order.
     *   Old timestamps are always at the front → remove from front.
     *   This is a natural FIFO sliding window.
     *
     * Time:  O(1) amortized per ping (each timestamp enqueued/dequeued once)
     * Space: O(W) where W = max requests in a 3000ms window
     *
     * Follow-up: What if timestamps could arrive out of order?
     *   Use a TreeMap or sorted structure instead of a simple queue.
     */
    static class RecentCounter {
        private Queue<Integer> q = new LinkedList<>();

        public int ping(int t) {
            q.offer(t);
            while (q.peek() < t - 3000) {
                q.poll();
            }
            return q.size();
        }
    }

    // =========================================================
    // E3. First Unique Character in a String  LC 387
    // Pattern: QUEUE + FREQUENCY COUNT
    // =========================================================
    /**
     * Find the index of the first non-repeating character.
     *
     * Approach 1: Two-pass frequency count — O(n) time, O(1) space.
     *   Pass 1: count frequency of each character.
     *   Pass 2: find first char with freq == 1.
     *
     * Approach 2: Queue-based (useful for streaming variant).
     *   Enqueue (char, index) pairs.
     *   Track frequency. Poll from front while front's freq > 1.
     *   Answer = front of queue (or -1 if empty).
     *
     * Time:  O(n)
     * Space: O(1) — at most 26 characters
     *
     * Follow-up: First unique character in a data STREAM?
     *   Use the queue approach: maintain queue + freq map.
     *   On each new character: update freq, poll stale front entries.
     *   Front of queue = current first unique.
     */
    public int firstUniqChar(String s) {
        int[] freq = new int[26];
        for (char c : s.toCharArray()) freq[c - 'a']++;
        for (int i = 0; i < s.length(); i++) {
            if (freq[s.charAt(i) - 'a'] == 1) return i;
        }
        return -1;
    }

    // Queue-based streaming version
    public int firstUniqCharQueue(String s) {
        int[] freq = new int[26];
        Queue<int[]> q = new LinkedList<>();  // [charIndex, position]

        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i) - 'a';
            freq[c]++;
            q.offer(new int[]{c, i});
            // Remove non-unique characters from front
            while (!q.isEmpty() && freq[q.peek()[0]] > 1) {
                q.poll();
            }
        }

        return q.isEmpty() ? -1 : q.peek()[1];
    }

    // =========================================================
    // E4. Moving Average from Data Stream  LC 346
    // Pattern: QUEUE — Fixed-Size Sliding Window
    // =========================================================
    /**
     * Calculate the moving average of the last `size` values.
     *
     * Maintain a queue of at most `size` elements.
     * Track running sum. On overflow: subtract front, poll.
     *
     * Time:  O(1) per next()
     * Space: O(size)
     *
     * Follow-up: What if you need the moving MEDIAN?
     *   Use two heaps (max-heap for lower half, min-heap for upper half)
     *   + a HashMap for lazy deletion of expired elements.
     *   Much harder — LC 480 (Sliding Window Median).
     */
    static class MovingAverage {
        private Queue<Integer> q = new LinkedList<>();
        private int maxSize;
        private double sum = 0;

        MovingAverage(int size) {
            this.maxSize = size;
        }

        public double next(int val) {
            q.offer(val);
            sum += val;
            if (q.size() > maxSize) {
                sum -= q.poll();
            }
            return sum / q.size();
        }
    }

    // =========================================================
    // E5. Number of Students Unable to Eat Lunch  LC 1700
    // Pattern: QUEUE SIMULATION
    // =========================================================
    /**
     * Students in a queue, sandwiches in a stack.
     * Student at front takes the top sandwich if it matches preference.
     * Otherwise, goes to the back of the queue.
     * Stop when no student wants the top sandwich.
     *
     * Brute force: simulate the process — but might loop forever?
     * No — stop when ALL remaining students don't want the top sandwich.
     *
     * Optimal insight: we don't need to simulate the queue.
     * Count how many students want type 0 and type 1.
     * Process sandwiches from top. If nobody wants the current type → stop.
     * Remaining students = answer.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: What if there were k sandwich types?
     *   Same approach: count array of size k.
     */
    public int countStudents(int[] students, int[] sandwiches) {
        int count0 = 0, count1 = 0;
        for (int s : students) {
            if (s == 0) count0++;
            else count1++;
        }

        for (int sandwich : sandwiches) {
            if (sandwich == 0) {
                if (count0 == 0) return count0 + count1;  // nobody wants type 0
                count0--;
            } else {
                if (count1 == 0) return count0 + count1;  // nobody wants type 1
                count1--;
            }
        }

        return 0;
    }

    // =========================================================
    // E6. Implement Stack using Queues  LC 225
    // Pattern: DESIGN — Queue to Stack Conversion
    // =========================================================
    /**
     * Implement LIFO stack using only queues.
     *
     * Approach: On push, offer to queue, then rotate (poll + offer)
     * size-1 times to bring the new element to the front.
     *
     * push O(n), pop O(1), top O(1).
     *
     * Alternative: costly pop instead of costly push.
     *
     * Time:  Push O(n), Pop O(1)
     * Space: O(n)
     *
     * Follow-up: Can you do it with O(1) push and O(n) pop?
     *   Yes — on pop, transfer all but last to a temp queue, return last.
     */
    static class MyStack {
        private Queue<Integer> q = new LinkedList<>();

        public void push(int x) {
            q.offer(x);
            // Rotate: bring the new element to the front
            for (int i = 0; i < q.size() - 1; i++) {
                q.offer(q.poll());
            }
        }

        public int pop() {
            return q.poll();
        }

        public int top() {
            return q.peek();
        }

        public boolean empty() {
            return q.isEmpty();
        }
    }

    // =========================================================
    // E7. Time Needed to Buy Tickets  LC 2073
    // Pattern: QUEUE SIMULATION / MATH
    // =========================================================
    /**
     * People in a line, each wants tickets[i] tickets.
     * Each person buys 1 ticket per turn, then goes to the back.
     * How many seconds until person at index k finishes?
     *
     * Simulation: use a queue and simulate — O(sum(tickets)) time.
     *
     * Math approach — O(n):
     * For each person i:
     *   If i <= k: they process min(tickets[i], tickets[k]) times before k finishes.
     *   If i > k:  they process min(tickets[i], tickets[k] - 1) times.
     *   (People after k get one fewer turn in the last round.)
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Follow-up: What if people can buy up to m tickets per turn?
     *   Adjust: each person processes ceil(tickets[i] / m) rounds.
     */
    public int timeRequiredToBuy(int[] tickets, int k) {
        int time = 0;
        for (int i = 0; i < tickets.length; i++) {
            if (i <= k) {
                time += Math.min(tickets[i], tickets[k]);
            } else {
                time += Math.min(tickets[i], tickets[k] - 1);
            }
        }
        return time;
    }

    // =========================================================
    // E8. Reveal Cards In Increasing Order  LC 950
    // Pattern: DEQUE SIMULATION (reverse process)
    // =========================================================
    /**
     * Reveal cards one by one: reveal top, move next to bottom, repeat.
     * Reorder the deck so revealed cards are in increasing order.
     *
     * KEY INSIGHT — REVERSE SIMULATION:
     * Sort the values. Then simulate the process in reverse.
     * Start with the largest value. For each value (largest to smallest):
     *   Move bottom card to top (reverse of "move top to bottom").
     *   Place current value on top.
     *
     * Time:  O(n log n) — sorting
     * Space: O(n)
     *
     * Follow-up: What if you reveal every k-th card instead of every 2nd?
     *   Same approach: reverse simulation with k-1 rotations instead of 1.
     */
    public int[] deckRevealedIncreasing(int[] deck) {
        int n = deck.length;
        Arrays.sort(deck);
        Deque<Integer> dq = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            if (!dq.isEmpty()) {
                dq.offerFirst(dq.pollLast());  // move bottom to top
            }
            dq.offerFirst(deck[i]);  // place current value on top
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = dq.pollFirst();
        }
        return result;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        QueueEasy sol = new QueueEasy();

        System.out.println("═══ E1: Queue using Stacks ═══");
        MyQueue mq = new MyQueue();
        mq.push(1); mq.push(2); mq.push(3);
        System.out.println("peek: " + mq.peek());  // 1
        System.out.println("pop:  " + mq.pop());   // 1
        System.out.println("pop:  " + mq.pop());   // 2
        System.out.println("empty: " + mq.empty()); // false

        System.out.println("\n═══ E2: Recent Counter ═══");
        RecentCounter rc = new RecentCounter();
        System.out.println(rc.ping(1));     // 1
        System.out.println(rc.ping(100));   // 2
        System.out.println(rc.ping(3001));  // 3
        System.out.println(rc.ping(3002));  // 3

        System.out.println("\n═══ E3: First Unique Character ═══");
        System.out.println(sol.firstUniqChar("leetcode"));      // 0 ('l')
        System.out.println(sol.firstUniqChar("loveleetcode"));  // 2 ('v')
        System.out.println(sol.firstUniqCharQueue("aabb"));     // -1

        System.out.println("\n═══ E4: Moving Average ═══");
        MovingAverage ma = new MovingAverage(3);
        System.out.println(ma.next(1));   // 1.0
        System.out.println(ma.next(10));  // 5.5
        System.out.println(ma.next(3));   // 4.666...
        System.out.println(ma.next(5));   // 6.0

        System.out.println("\n═══ E5: Students Unable to Eat ═══");
        System.out.println(sol.countStudents(
            new int[]{1,1,0,0}, new int[]{0,1,0,1}));  // 0
        System.out.println(sol.countStudents(
            new int[]{1,1,1,0,0,1}, new int[]{1,0,0,0,1,1}));  // 3

        System.out.println("\n═══ E6: Stack using Queues ═══");
        MyStack ms = new MyStack();
        ms.push(1); ms.push(2); ms.push(3);
        System.out.println("top: " + ms.top());   // 3
        System.out.println("pop: " + ms.pop());   // 3
        System.out.println("pop: " + ms.pop());   // 2

        System.out.println("\n═══ E7: Time to Buy Tickets ═══");
        System.out.println(sol.timeRequiredToBuy(new int[]{2,3,2}, 2));   // 6
        System.out.println(sol.timeRequiredToBuy(new int[]{5,1,1,1}, 0)); // 8

        System.out.println("\n═══ E8: Reveal Cards Increasing ═══");
        System.out.println(Arrays.toString(
            sol.deckRevealedIncreasing(new int[]{17,13,11,2,3,5,7})));
        // [2, 13, 3, 11, 5, 17, 7]
    }
}
