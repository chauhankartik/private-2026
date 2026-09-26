/**
 * ============================================================
 *  STACK — GOOGLE-LEVEL PROBLEMS
 * ============================================================
 *
 *  G1.  Basic Calculator II                        LC 227  ★ Google
 *  G2.  Maximum Frequency Stack                    LC 895  ★ Google
 *  G3.  Number of Visible People in a Queue        LC 1944 ★ Google
 *  G4.  Remove Duplicate Letters                   LC 316  ★ Google
 *  G5.  Sum of Subarray Ranges                     LC 2104 ★ Google
 *
 *  Each problem includes:
 *   - Why Google asks it
 *   - Brute → optimal trace
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class StackGoogleLevel {

    // =========================================================
    // G1. Basic Calculator II  LC 227  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests expression parsing with operator precedence.
     *   Interviewers look for clean single-pass handling of * and /
     *   without using two stacks or recursion.
     *
     * Evaluate expression with +, -, *, / and non-negative integers.
     * No parentheses. Follow standard precedence (* / before + -).
     *
     * Single-pass approach:
     *   Track prevSign (initially '+'). Build current number.
     *   When we hit an operator or end of string:
     *     If prevSign is '+': push number.
     *     If prevSign is '-': push -number.
     *     If prevSign is '*': pop, push pop * number.
     *     If prevSign is '/': pop, push pop / number.
     *   Update prevSign to current operator. Reset number.
     *   At end: sum all stack elements.
     *
     * WHY THIS HANDLES PRECEDENCE:
     *   * and / are evaluated IMMEDIATELY (pop and push result).
     *   + and - are DEFERRED (push signed value for later).
     *   Final sum handles all deferred additions/subtractions.
     *
     * Time:  O(n)
     * Space: O(n) — stack; can be O(1) with running sum optimization
     *
     * Follow-up: Basic Calculator III (+ - * / and parentheses)?
     *   Add recursion: on '(' recurse, on ')' return.
     *   Or push state to stack like Basic Calculator I.
     */
    public int calculate(String s) {
        Deque<Integer> stack = new ArrayDeque<>();
        int num = 0;
        char prevSign = '+';

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
            }

            if ((!Character.isDigit(c) && c != ' ') || i == s.length() - 1) {
                switch (prevSign) {
                    case '+': stack.push(num); break;
                    case '-': stack.push(-num); break;
                    case '*': stack.push(stack.pop() * num); break;
                    case '/': stack.push(stack.pop() / num); break;
                }
                prevSign = c;
                num = 0;
            }
        }

        int result = 0;
        for (int val : stack) result += val;
        return result;
    }

    // =========================================================
    // G2. Maximum Frequency Stack  LC 895  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests design with multiple data structures working in concert.
     *   Requires understanding of frequency-based ordering and
     *   LIFO tie-breaking — a common pattern in caching systems.
     *
     * FreqStack: push(val), pop() returns the most frequent element.
     * If tie, return the one closest to the top (most recently pushed).
     *
     * Design:
     *   Map<Integer, Integer> freq — tracks frequency of each value.
     *   Map<Integer, Deque<Integer>> groupByFreq — groups values by frequency.
     *   int maxFreq — current maximum frequency.
     *
     * push(val):
     *   Increment freq[val]. Update maxFreq.
     *   Push val onto groupByFreq[freq[val]].
     *
     * pop():
     *   Pop from groupByFreq[maxFreq] (LIFO gives most recent).
     *   Decrement freq[popped]. If groupByFreq[maxFreq] is empty, maxFreq--.
     *
     * WHY THIS IS CORRECT:
     *   groupByFreq groups by frequency level. Popping from the highest
     *   frequency group always gives the most frequent element.
     *   Within a frequency group, the Deque gives LIFO order,
     *   which breaks ties by recency (most recently pushed = top).
     *
     *   When an element's frequency decreases, it "falls" to a lower group.
     *   But we DON'T need to remove it from the lower group — it was
     *   already pushed there when it had that frequency.
     *
     * Time:  O(1) for push and pop
     * Space: O(n)
     *
     * Follow-up: What if pop should return the LEAST frequent element?
     *   Track minFreq instead. Need to handle min updates carefully.
     *   Similar to LFU cache design.
     */
    static class FreqStack {
        private Map<Integer, Integer> freq;
        private Map<Integer, Deque<Integer>> groupByFreq;
        private int maxFreq;

        FreqStack() {
            freq = new HashMap<>();
            groupByFreq = new HashMap<>();
            maxFreq = 0;
        }

        public void push(int val) {
            int f = freq.merge(val, 1, Integer::sum);
            maxFreq = Math.max(maxFreq, f);
            groupByFreq.computeIfAbsent(f, k -> new ArrayDeque<>()).push(val);
        }

        public int pop() {
            Deque<Integer> group = groupByFreq.get(maxFreq);
            int val = group.pop();
            freq.merge(val, -1, Integer::sum);
            if (group.isEmpty()) maxFreq--;
            return val;
        }
    }

    // =========================================================
    // G3. Number of Visible People in a Queue  LC 1944  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests monotonic stack mastery with a non-trivial counting twist.
     *   The "visibility" concept maps to understanding next greater elements
     *   with counting — a pattern Google uses in system design interviews.
     *
     * People in a line. Person i can see person j (j > i) if all people
     * between them are shorter than BOTH heights[i] and heights[j].
     * Count visible people for each person.
     *
     * Traverse RIGHT to LEFT with a monotonic DECREASING stack.
     * For each person i:
     *   Pop all people from the stack shorter than heights[i] — they are
     *   visible to person i. Count them.
     *   If stack is still non-empty, the top is also visible (the first
     *   taller person that blocks further view). Count +1.
     *   Push heights[i].
     *
     * WHY DECREASING STACK:
     *   After processing, the stack holds people in decreasing order.
     *   A short person behind a tall person is invisible to anyone left.
     *   Popping shorter people: they're the ones person i can see.
     *   The remaining top (if exists): the first blocker, also visible.
     *
     * Time:  O(n) — each person pushed and popped at most once
     * Space: O(n)
     *
     * Follow-up: What if people can see in both directions?
     *   Run the algorithm left-to-right AND right-to-left, combine.
     */
    public int[] canSeePersonsCount(int[] heights) {
        int n = heights.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();  // monotonic decreasing

        for (int i = n - 1; i >= 0; i--) {
            int count = 0;

            // Pop all shorter people — they are visible to person i
            while (!stack.isEmpty() && stack.peek() < heights[i]) {
                stack.pop();
                count++;
            }

            // If stack non-empty, the top person is also visible (blocker)
            if (!stack.isEmpty()) count++;

            result[i] = count;
            stack.push(heights[i]);
        }

        return result;
    }

    // =========================================================
    // G4. Remove Duplicate Letters  LC 316  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Combines monotonic stack with greedy decision-making.
     *   Must produce lexicographically smallest result while
     *   keeping exactly one of each character — tests constraint handling.
     *
     * Remove duplicate letters so that every letter appears once
     * and the result is the smallest in lexicographic order.
     *
     * Use a monotonic INCREASING stack (lexicographic order).
     * For each character:
     *   While stack top > current char AND stack top appears later:
     *     Pop stack top (we'll add it later at a better position).
     *   If current char is already in stack: skip.
     *   Otherwise: push current char.
     *
     * WHY "APPEARS LATER" CHECK:
     *   We can only remove a character if it occurs again later.
     *   If it's the LAST occurrence, we must keep it even if it
     *   breaks the monotonic order.
     *
     * Time:  O(n)
     * Space: O(1) — at most 26 characters in stack
     *
     * Follow-up: Smallest Subsequence of Distinct Characters (LC 1081)?
     *   Exact same problem with different wording.
     */
    public String removeDuplicateLetters(String s) {
        int[] lastIndex = new int[26];
        for (int i = 0; i < s.length(); i++) {
            lastIndex[s.charAt(i) - 'a'] = i;
        }

        boolean[] inStack = new boolean[26];
        Deque<Character> stack = new ArrayDeque<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStack[c - 'a']) continue;  // already in result

            // Pop larger characters that appear later
            while (!stack.isEmpty() && stack.peek() > c
                   && lastIndex[stack.peek() - 'a'] > i) {
                inStack[stack.pop() - 'a'] = false;
            }

            stack.push(c);
            inStack[c - 'a'] = true;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : stack) sb.append(c);
        return sb.reverse().toString();
    }

    // =========================================================
    // G5. Sum of Subarray Ranges  LC 2104  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Extends Sum of Subarray Minimums with a max component.
     *   Tests the contribution technique twice — once for min, once for max.
     *   Google loves problems where the brute-force solution is obvious
     *   but the optimal requires deep algorithmic insight.
     *
     * Sum of (max(subarray) - min(subarray)) for all subarrays.
     * = sum of max(subarray) - sum of min(subarray).
     *
     * Brute: enumerate all subarrays — O(n²).
     *
     * Optimal: compute sum of subarray maximums and sum of subarray minimums
     * separately using the CONTRIBUTION TECHNIQUE with monotonic stacks.
     *
     * For sum of minimums:
     *   left[i] = distance to Previous Smaller Element (strict <).
     *   right[i] = distance to Next Smaller-or-Equal Element (<=).
     *   contribution of i as min = arr[i] × left[i] × right[i].
     *
     * For sum of maximums:
     *   left[i] = distance to Previous Greater Element (strict >).
     *   right[i] = distance to Next Greater-or-Equal Element (>=).
     *   contribution of i as max = arr[i] × left[i] × right[i].
     *
     * Answer = sumMax - sumMin.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Sum of Subarray Ranges for a circular array?
     *   Double the array and adjust boundaries.
     */
    public long subArrayRanges(int[] nums) {
        int n = nums.length;
        return sumSubarrayMaxs(nums, n) - sumSubarrayMins(nums, n);
    }

    private long sumSubarrayMins(int[] arr, int n) {
        int[] left = new int[n];
        int[] right = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] >= arr[i])
                stack.pop();
            left[i] = stack.isEmpty() ? i + 1 : i - stack.peek();
            stack.push(i);
        }
        stack.clear();
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && arr[stack.peek()] > arr[i])
                stack.pop();
            right[i] = stack.isEmpty() ? n - i : stack.peek() - i;
            stack.push(i);
        }

        long sum = 0;
        for (int i = 0; i < n; i++)
            sum += (long) arr[i] * left[i] * right[i];
        return sum;
    }

    private long sumSubarrayMaxs(int[] arr, int n) {
        int[] left = new int[n];
        int[] right = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] <= arr[i])
                stack.pop();
            left[i] = stack.isEmpty() ? i + 1 : i - stack.peek();
            stack.push(i);
        }
        stack.clear();
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && arr[stack.peek()] < arr[i])
                stack.pop();
            right[i] = stack.isEmpty() ? n - i : stack.peek() - i;
            stack.push(i);
        }

        long sum = 0;
        for (int i = 0; i < n; i++)
            sum += (long) arr[i] * left[i] * right[i];
        return sum;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        StackGoogleLevel sol = new StackGoogleLevel();

        System.out.println("═══ G1: Basic Calculator II ═══");
        System.out.println(sol.calculate("3+2*2"));       // 7
        System.out.println(sol.calculate(" 3/2 "));        // 1
        System.out.println(sol.calculate(" 3+5 / 2 "));   // 5
        System.out.println(sol.calculate("14-3/2"));       // 13

        System.out.println("\n═══ G2: Maximum Frequency Stack ═══");
        FreqStack fs = new FreqStack();
        fs.push(5); fs.push(7); fs.push(5); fs.push(7); fs.push(4); fs.push(5);
        System.out.println(fs.pop());  // 5 (freq 3)
        System.out.println(fs.pop());  // 7 (freq 2, more recent than 5)
        System.out.println(fs.pop());  // 5 (freq 2)
        System.out.println(fs.pop());  // 4 (freq 1, most recent)

        System.out.println("\n═══ G3: Visible People in a Queue ═══");
        System.out.println(Arrays.toString(
            sol.canSeePersonsCount(new int[]{10,6,8,5,11,9})));
        // [3,1,2,1,1,0]
        System.out.println(Arrays.toString(
            sol.canSeePersonsCount(new int[]{5,1,2,3,10})));
        // [4,1,1,1,0]

        System.out.println("\n═══ G4: Remove Duplicate Letters ═══");
        System.out.println(sol.removeDuplicateLetters("bcabc"));     // "abc"
        System.out.println(sol.removeDuplicateLetters("cbacdcbc"));  // "acdb"

        System.out.println("\n═══ G5: Sum of Subarray Ranges ═══");
        System.out.println(sol.subArrayRanges(new int[]{1,2,3}));    // 4
        System.out.println(sol.subArrayRanges(new int[]{1,3,3}));    // 4
        System.out.println(sol.subArrayRanges(new int[]{4,-2,-3,4,1})); // 59
    }
}
