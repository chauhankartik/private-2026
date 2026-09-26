/**
 * ============================================================
 *  STACK — EASY PROBLEMS
 * ============================================================
 *
 *  E1.  Valid Parentheses                          LC 20
 *  E2.  Min Stack                                  LC 155
 *  E3.  Implement Queue using Stacks               LC 232
 *  E4.  Baseball Game                              LC 682
 *  E5.  Next Greater Element I                     LC 496
 *  E6.  Backspace String Compare                   LC 844
 *  E7.  Remove All Adjacent Duplicates in String   LC 1047
 *  E8.  Make The String Great                      LC 1544
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class StackEasy {

    // =========================================================
    // E1. Valid Parentheses  LC 20
    // Pattern: MATCHING — Stack for bracket pairing
    // =========================================================
    /**
     * Given a string with '(', ')', '{', '}', '[', ']', determine
     * if the input string is valid (properly opened and closed).
     *
     * Use a stack:
     *   On open bracket → push the corresponding CLOSING bracket.
     *   On close bracket → pop and check if it matches.
     *   At end → stack must be empty.
     *
     * WHY PUSH THE CLOSING BRACKET:
     *   Instead of pushing '(' and checking "is top '(' when we see ')'?",
     *   push ')' directly. Then on ')', just check stack.pop() == ')'.
     *   Cleaner code, one comparison instead of a mapping lookup.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: What if the string can contain non-bracket characters?
     *   Simply skip any character that isn't a bracket.
     */
    public boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') stack.push(')');
            else if (c == '{') stack.push('}');
            else if (c == '[') stack.push(']');
            else {
                if (stack.isEmpty() || stack.pop() != c) return false;
            }
        }
        return stack.isEmpty();
    }

    // =========================================================
    // E2. Min Stack  LC 155
    // Pattern: STACK AS HISTORY — O(1) getMin
    // =========================================================
    /**
     * Design a stack supporting push, pop, top, and getMin in O(1).
     *
     * Use TWO stacks: main stack + min stack.
     * Min stack tracks the current minimum at each level.
     * On push: push value to main, push min(value, current min) to min stack.
     * On pop: pop from both stacks.
     * getMin: peek min stack.
     *
     * WHY THIS WORKS:
     *   Each element in the min stack records "what was the minimum
     *   when this element was the top". Since we push/pop in sync,
     *   the min stack always reflects the current minimum.
     *
     * Time:  O(1) for all operations
     * Space: O(n) — two stacks of size n
     *
     * Follow-up: Can you do it with O(1) extra space?
     *   Store (value - currentMin) in main stack. If top < 0,
     *   the actual value was the new min. Recover using math.
     *   Tricky with overflow — use long.
     */
    static class MinStack {
        private Deque<Integer> stack = new ArrayDeque<>();
        private Deque<Integer> minStack = new ArrayDeque<>();

        public void push(int val) {
            stack.push(val);
            minStack.push(minStack.isEmpty() ? val : Math.min(val, minStack.peek()));
        }

        public void pop() {
            stack.pop();
            minStack.pop();
        }

        public int top() {
            return stack.peek();
        }

        public int getMin() {
            return minStack.peek();
        }
    }

    // =========================================================
    // E3. Implement Queue using Stacks  LC 232
    // Pattern: DESIGN — Two Stacks (amortized O(1))
    // =========================================================
    /**
     * Implement FIFO queue using only two LIFO stacks.
     *
     * Push to inStack. On pop/peek, if outStack is empty,
     * transfer all from inStack → outStack. Pop from outStack.
     *
     * WHY AMORTIZED O(1):
     *   Each element is transferred from inStack to outStack at most ONCE.
     *   Over n operations: n pushes + n transfers + n pops = 3n = O(n) total.
     *   Per operation: O(1) amortized.
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
    // E4. Baseball Game  LC 682
    // Pattern: SIMULATION — Stack as record keeper
    // =========================================================
    /**
     * Process operations: integer (add score), '+' (sum of last two),
     * 'D' (double last), 'C' (remove last). Return total sum.
     *
     * Stack naturally tracks the record.
     * '+' → peek two, push sum. 'D' → peek, push double.
     * 'C' → pop last. Integer → push value.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: What if 'C' could remove the last k records?
     *   Pop k times from stack.
     */
    public int calPoints(String[] operations) {
        Deque<Integer> stack = new ArrayDeque<>();

        for (String op : operations) {
            switch (op) {
                case "+":
                    int top = stack.pop();
                    int newTop = top + stack.peek();
                    stack.push(top);
                    stack.push(newTop);
                    break;
                case "D":
                    stack.push(2 * stack.peek());
                    break;
                case "C":
                    stack.pop();
                    break;
                default:
                    stack.push(Integer.parseInt(op));
            }
        }

        int sum = 0;
        for (int score : stack) sum += score;
        return sum;
    }

    // =========================================================
    // E5. Next Greater Element I  LC 496
    // Pattern: MONOTONIC STACK (decreasing)
    // =========================================================
    /**
     * Two arrays: nums1 is a subset of nums2.
     * For each nums1[i], find the next greater element in nums2
     * (to the right of nums1[i]'s position in nums2).
     *
     * Build a map of (value → nextGreater) for all elements in nums2
     * using a monotonic decreasing stack.
     *
     * Traverse nums2 left to right:
     *   While stack is non-empty and top < current → pop, record answer.
     *   Push current.
     *
     * Then look up each nums1[i] in the map.
     *
     * Time:  O(n + m) where n = nums1.length, m = nums2.length
     * Space: O(m) for the map
     *
     * Follow-up: Next Greater Element II (LC 503) — circular array?
     *   Traverse the array twice (indices 0 to 2n-1, use i % n).
     */
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nge = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();

        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                nge.put(stack.pop(), num);
            }
            stack.push(num);
        }

        int[] result = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            result[i] = nge.getOrDefault(nums1[i], -1);
        }
        return result;
    }

    // =========================================================
    // E6. Backspace String Compare  LC 844
    // Pattern: SIMULATION — Stack or Two Pointers
    // =========================================================
    /**
     * '#' means backspace. Compare two strings after applying backspaces.
     *
     * Stack approach: process each string with a stack.
     *   On '#' → pop (if non-empty). On char → push.
     *   Compare resulting stacks.
     *
     * O(1) space approach: traverse both strings from RIGHT to LEFT.
     *   Count '#' characters. Skip characters while count > 0.
     *   Compare characters when both have a valid (non-skipped) char.
     *
     * Time:  O(n + m)
     * Space: O(n + m) for stack, O(1) for two-pointer
     *
     * Follow-up: What if '#' deletes the next k characters?
     *   Accumulate delete count and skip accordingly.
     */
    public boolean backspaceCompare(String s, String t) {
        return buildString(s).equals(buildString(t));
    }

    private String buildString(String str) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : str.toCharArray()) {
            if (c == '#') {
                if (!stack.isEmpty()) stack.pop();
            } else {
                stack.push(c);
            }
        }
        return stack.toString();
    }

    // =========================================================
    // E7. Remove All Adjacent Duplicates in String  LC 1047
    // Pattern: STACK AS BUFFER
    // =========================================================
    /**
     * Repeatedly remove adjacent duplicate pairs until no more exist.
     *
     * Use a stack: for each character, if it matches the top → pop.
     * Otherwise → push. The stack holds the result.
     *
     * WHY ONE PASS SUFFICES:
     *   When we pop a pair, the new top may form a new pair with
     *   the next character. The stack handles this naturally —
     *   no need for multiple passes.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Remove All Adjacent Duplicates in String II (LC 1209)?
     *   Track (char, count) pairs. When count reaches k, remove.
     */
    public String removeDuplicates(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (!stack.isEmpty() && stack.peek() == c) {
                stack.pop();
            } else {
                stack.push(c);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char c : stack) sb.append(c);
        return sb.reverse().toString();
    }

    // =========================================================
    // E8. Make The String Great  LC 1544
    // Pattern: STACK AS BUFFER
    // =========================================================
    /**
     * Remove pairs of adjacent characters where one is upper and one
     * is lower of the same letter. Repeat until no more such pairs.
     *
     * Same pattern as removing adjacent duplicates, but the condition
     * is: same letter, different case.
     * Check: Math.abs(stack.peek() - c) == 32  (ASCII distance a↔A = 32)
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: What if "bad" pairs are any two chars with abs diff == k?
     *   Change the condition to Math.abs(stack.peek() - c) == k.
     */
    public String makeGood(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (!stack.isEmpty() && Math.abs(stack.peek() - c) == 32) {
                stack.pop();
            } else {
                stack.push(c);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char c : stack) sb.append(c);
        return sb.reverse().toString();
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        StackEasy sol = new StackEasy();

        System.out.println("═══ E1: Valid Parentheses ═══");
        System.out.println(sol.isValid("()[]{}"));     // true
        System.out.println(sol.isValid("(]"));          // false
        System.out.println(sol.isValid("([)]"));        // false
        System.out.println(sol.isValid("{[]}"));        // true

        System.out.println("\n═══ E2: Min Stack ═══");
        MinStack ms = new MinStack();
        ms.push(-2); ms.push(0); ms.push(-3);
        System.out.println("getMin: " + ms.getMin());  // -3
        ms.pop();
        System.out.println("top:    " + ms.top());     // 0
        System.out.println("getMin: " + ms.getMin());  // -2

        System.out.println("\n═══ E3: Queue using Stacks ═══");
        MyQueue mq = new MyQueue();
        mq.push(1); mq.push(2); mq.push(3);
        System.out.println("peek: " + mq.peek());  // 1
        System.out.println("pop:  " + mq.pop());   // 1
        System.out.println("pop:  " + mq.pop());   // 2
        System.out.println("empty: " + mq.empty()); // false

        System.out.println("\n═══ E4: Baseball Game ═══");
        System.out.println(sol.calPoints(
            new String[]{"5","2","C","D","+"}));  // 30
        System.out.println(sol.calPoints(
            new String[]{"5","-2","4","C","D","9","+","+"}));  // 27

        System.out.println("\n═══ E5: Next Greater Element I ═══");
        System.out.println(Arrays.toString(
            sol.nextGreaterElement(new int[]{4,1,2}, new int[]{1,3,4,2})));
        // [-1, 3, -1]
        System.out.println(Arrays.toString(
            sol.nextGreaterElement(new int[]{2,4}, new int[]{1,2,3,4})));
        // [3, -1]

        System.out.println("\n═══ E6: Backspace String Compare ═══");
        System.out.println(sol.backspaceCompare("ab#c", "ad#c"));  // true
        System.out.println(sol.backspaceCompare("ab##", "c#d#"));  // true
        System.out.println(sol.backspaceCompare("a#c", "b"));      // false

        System.out.println("\n═══ E7: Remove Adjacent Duplicates ═══");
        System.out.println(sol.removeDuplicates("abbaca"));  // "ca"
        System.out.println(sol.removeDuplicates("azxxzy"));  // "ay"

        System.out.println("\n═══ E8: Make The String Great ═══");
        System.out.println(sol.makeGood("leEeetcode"));  // "leetcode"
        System.out.println(sol.makeGood("abBAcC"));      // ""
        System.out.println(sol.makeGood("s"));            // "s"
    }
}
