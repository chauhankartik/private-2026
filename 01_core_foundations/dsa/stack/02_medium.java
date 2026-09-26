/**
 * ============================================================
 *  STACK — MEDIUM PROBLEMS
 * ============================================================
 *
 *  M1.  Daily Temperatures                         LC 739
 *  M2.  Evaluate Reverse Polish Notation           LC 150
 *  M3.  Decode String                              LC 394
 *  M4.  Asteroid Collision                         LC 735
 *  M5.  Remove K Digits                            LC 402
 *  M6.  Online Stock Span                          LC 901
 *  M7.  Simplify Path                              LC 71
 *  M8.  132 Pattern                                LC 456
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class StackMedium {

    // =========================================================
    // M1. Daily Temperatures  LC 739
    // Pattern: MONOTONIC STACK (decreasing — stores indices)
    // =========================================================
    /**
     * Given daily temperatures, for each day find how many days
     * you have to wait for a warmer temperature. Return 0 if never.
     *
     * Brute: for each day, scan right for the next warmer day — O(n²).
     *
     * Optimal: MONOTONIC DECREASING STACK (indices, decreasing temps).
     *   Traverse left to right.
     *   While stack is non-empty and current temp > temp at stack top:
     *     Pop → the current day is the answer for that popped day.
     *   Push current index.
     *
     * WHY MONOTONIC DECREASING:
     *   We want the NEXT GREATER temperature.
     *   Stack holds indices of days waiting for an answer.
     *   When a warmer day arrives, it resolves all cooler days on the stack.
     *
     * Time:  O(n) — each index pushed and popped at most once
     * Space: O(n) — stack + result array
     *
     * Follow-up: What if we want the PREVIOUS warmer day instead?
     *   Traverse right to left with the same monotonic stack.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();  // indices

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && temperatures[stack.peek()] < temperatures[i]) {
                int prevDay = stack.pop();
                result[prevDay] = i - prevDay;
            }
            stack.push(i);
        }

        return result;
    }

    // =========================================================
    // M2. Evaluate Reverse Polish Notation  LC 150
    // Pattern: EXPRESSION EVALUATION — Stack-based
    // =========================================================
    /**
     * Evaluate arithmetic expression in Reverse Polish Notation (postfix).
     * Tokens: numbers and operators (+, -, *, /).
     *
     * Use a stack:
     *   Number → push.
     *   Operator → pop two operands, compute, push result.
     *   Order: b = pop, a = pop, push a OP b.
     *
     * WHY RPN IS STACK-FRIENDLY:
     *   RPN eliminates the need for parentheses and precedence rules.
     *   Each operator immediately follows its operands.
     *   The stack naturally holds operands waiting for their operator.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Convert infix to RPN (Shunting Yard algorithm)?
     *   Use an operator stack with precedence rules.
     */
    public int evalRPN(String[] tokens) {
        Deque<Integer> stack = new ArrayDeque<>();

        for (String token : tokens) {
            switch (token) {
                case "+": { int b = stack.pop(), a = stack.pop(); stack.push(a + b); break; }
                case "-": { int b = stack.pop(), a = stack.pop(); stack.push(a - b); break; }
                case "*": { int b = stack.pop(), a = stack.pop(); stack.push(a * b); break; }
                case "/": { int b = stack.pop(), a = stack.pop(); stack.push(a / b); break; }
                default:  stack.push(Integer.parseInt(token));
            }
        }

        return stack.pop();
    }

    // =========================================================
    // M3. Decode String  LC 394
    // Pattern: NESTED STACK PARSING
    // =========================================================
    /**
     * Decode encoded string: "3[a2[c]]" → "accaccacc".
     * k[encoded_string] means repeat encoded_string k times.
     *
     * Use TWO stacks: one for counts, one for strings.
     * On digit: build the number (multi-digit possible).
     * On '[':  push current string and count to stacks, reset.
     * On ']':  pop count and previous string,
     *          append current string × count to previous string.
     * On char: append to current string.
     *
     * WHY TWO STACKS:
     *   Nesting means we need to pause the current context (string + count),
     *   process the inner bracket, then resume. This is exactly stack behavior.
     *
     * Time:  O(maxK × n) where maxK = max repeat count, n = output length
     * Space: O(n)
     *
     * Follow-up: What about nested encoding like "2[a3[b]c]"?
     *   This solution handles it naturally — stacks track nesting depth.
     */
    public String decodeString(String s) {
        Deque<Integer> countStack = new ArrayDeque<>();
        Deque<StringBuilder> strStack = new ArrayDeque<>();
        StringBuilder current = new StringBuilder();
        int k = 0;

        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                k = k * 10 + (c - '0');
            } else if (c == '[') {
                countStack.push(k);
                strStack.push(current);
                current = new StringBuilder();
                k = 0;
            } else if (c == ']') {
                int count = countStack.pop();
                StringBuilder prev = strStack.pop();
                String repeated = current.toString();
                for (int i = 0; i < count; i++) prev.append(repeated);
                current = prev;
            } else {
                current.append(c);
            }
        }

        return current.toString();
    }

    // =========================================================
    // M4. Asteroid Collision  LC 735
    // Pattern: SIMULATION — Stack-based collision resolution
    // =========================================================
    /**
     * Asteroids in a row. Positive = moving right, negative = moving left.
     * When they collide: larger survives, equal = both destroyed.
     * Find the state after all collisions.
     *
     * Collision happens ONLY when stack top is positive (→) and
     * current asteroid is negative (←). Same direction = no collision.
     *
     * For each asteroid:
     *   While stack top > 0 and current < 0 (collision zone):
     *     If |current| > top: pop (top destroyed), continue checking.
     *     If |current| == top: pop (both destroyed), current also gone.
     *     If |current| < top: current destroyed.
     *   If current survived, push it.
     *
     * Time:  O(n) — each asteroid pushed/popped at most once
     * Space: O(n)
     *
     * Follow-up: What if asteroids have different speeds?
     *   Faster asteroids catch up to slower ones.
     *   Need a simulation with time tracking.
     */
    public int[] asteroidCollision(int[] asteroids) {
        Deque<Integer> stack = new ArrayDeque<>();

        for (int ast : asteroids) {
            boolean survived = true;

            while (!stack.isEmpty() && ast < 0 && stack.peek() > 0) {
                if (stack.peek() < -ast) {
                    stack.pop();  // top destroyed, continue checking
                } else if (stack.peek() == -ast) {
                    stack.pop();  // both destroyed
                    survived = false;
                    break;
                } else {
                    survived = false;  // current destroyed
                    break;
                }
            }

            if (survived) stack.push(ast);
        }

        int[] result = new int[stack.size()];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = stack.pop();
        }
        return result;
    }

    // =========================================================
    // M5. Remove K Digits  LC 402
    // Pattern: MONOTONIC STACK (increasing — greedy removal)
    // =========================================================
    /**
     * Remove k digits from a number string to make it the smallest.
     *
     * Greedy: scan left to right. Whenever a digit is smaller than
     * the previous digit, remove the previous (larger) digit.
     * This is a monotonic INCREASING stack.
     *
     * WHY REMOVE LARGER DIGITS FROM THE LEFT:
     *   Leftmost digits have the highest place value.
     *   Removing a large digit from the left reduces the number
     *   more than removing from the right.
     *   Monotonic increasing stack ensures digits are non-decreasing.
     *
     * After processing: if k > 0, remove from the end (all non-decreasing).
     * Strip leading zeros.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Remove K digits to make the LARGEST number?
     *   Monotonic DECREASING stack (remove smaller digits).
     */
    public String removeKdigits(String num, int k) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char digit : num.toCharArray()) {
            while (!stack.isEmpty() && k > 0 && stack.peek() > digit) {
                stack.pop();
                k--;
            }
            stack.push(digit);
        }

        // Remove remaining k digits from the end
        while (k > 0) {
            stack.pop();
            k--;
        }

        // Build result and strip leading zeros
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) sb.append(stack.pollLast());

        // Remove leading zeros
        while (sb.length() > 0 && sb.charAt(0) == '0') sb.deleteCharAt(0);

        return sb.length() == 0 ? "0" : sb.toString();
    }

    // =========================================================
    // M6. Online Stock Span  LC 901
    // Pattern: MONOTONIC STACK (decreasing — span calculation)
    // =========================================================
    /**
     * For each day's stock price, find the span: number of consecutive
     * days (ending today) where price was ≤ today's price.
     *
     * Brute: for each day, scan backwards — O(n²).
     *
     * Optimal: monotonic decreasing stack of (price, span) pairs.
     * On each new price: pop all entries with price ≤ current price,
     * accumulate their spans. Push (current price, accumulated span).
     *
     * WHY THIS IS CORRECT:
     *   When we pop an entry, its entire span is "absorbed" into
     *   the current day's span. Those popped days are no longer
     *   relevant — any future day with a higher price will look
     *   past them anyway.
     *
     * Time:  O(1) amortized per call (each entry pushed/popped once)
     * Space: O(n)
     *
     * Follow-up: What if you need the span for the last k days only?
     *   Use a monotonic deque with window size limit.
     */
    static class StockSpanner {
        private Deque<int[]> stack;  // [price, span]

        StockSpanner() {
            stack = new ArrayDeque<>();
        }

        public int next(int price) {
            int span = 1;
            while (!stack.isEmpty() && stack.peek()[0] <= price) {
                span += stack.pop()[1];
            }
            stack.push(new int[]{price, span});
            return span;
        }
    }

    // =========================================================
    // M7. Simplify Path  LC 71
    // Pattern: STACK PARSING — Unix path simplification
    // =========================================================
    /**
     * Simplify Unix-style absolute path.
     * '.' = current dir (skip). '..' = parent dir (pop).
     * Multiple '/' = single '/'. Trailing '/' removed.
     *
     * Split by '/'. Process each component:
     *   "" or "." → skip.
     *   ".." → pop from stack (go up).
     *   else → push (enter directory).
     *
     * Build result from stack contents.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: What if '..' above root should be an error?
     *   Check if stack is empty before popping for '..'.
     */
    public String simplifyPath(String path) {
        Deque<String> stack = new ArrayDeque<>();
        String[] parts = path.split("/");

        for (String part : parts) {
            if (part.equals("") || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!stack.isEmpty()) stack.pop();
            } else {
                stack.push(part);
            }
        }

        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.insert(0, "/" + stack.pop());
        }

        return sb.length() == 0 ? "/" : sb.toString();
    }

    // =========================================================
    // M8. 132 Pattern  LC 456
    // Pattern: MONOTONIC STACK — Reverse traversal
    // =========================================================
    /**
     * Find i < j < k such that nums[i] < nums[k] < nums[j] (132 pattern).
     *
     * Traverse from RIGHT to LEFT.
     * Maintain a monotonic DECREASING stack (candidates for "3" in 132).
     * Track the largest popped value as "s2" (the "2" in 132).
     *
     * When we pop elements from the stack (because current > top),
     * those popped elements become candidates for "s2" (the "2").
     * We track the maximum "s2" seen.
     *
     * If current < s2, we found the "1" in 132. Return true.
     *
     * WHY RIGHT TO LEFT:
     *   We fix "3" (stack top) and "2" (largest popped) first.
     *   Then we scan for "1" (something smaller than "2").
     *   Going right to left: stack stores potential "3"s.
     *   When a larger "3" pops smaller values, those become "2" candidates.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: 123 Pattern (increasing triple)?
     *   Easier — track min so far, then find any j < k where min < nums[k].
     */
    public boolean find132pattern(int[] nums) {
        int n = nums.length;
        Deque<Integer> stack = new ArrayDeque<>();  // candidates for "3" (middle)
        int s2 = Integer.MIN_VALUE;  // largest "2" (right element of pattern)

        for (int i = n - 1; i >= 0; i--) {
            if (nums[i] < s2) return true;  // found "1" < "2"

            while (!stack.isEmpty() && nums[i] > stack.peek()) {
                s2 = Math.max(s2, stack.pop());  // popped becomes "2"
            }

            stack.push(nums[i]);  // current is candidate for "3"
        }

        return false;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        StackMedium sol = new StackMedium();

        System.out.println("═══ M1: Daily Temperatures ═══");
        System.out.println(Arrays.toString(
            sol.dailyTemperatures(new int[]{73,74,75,71,69,72,76,73})));
        // [1,1,4,2,1,1,0,0]

        System.out.println("\n═══ M2: Eval RPN ═══");
        System.out.println(sol.evalRPN(
            new String[]{"2","1","+","3","*"}));  // 9
        System.out.println(sol.evalRPN(
            new String[]{"4","13","5","/","+"})); // 6
        System.out.println(sol.evalRPN(
            new String[]{"10","6","9","3","+","-11","*","/","*","17","+","5","+"})); // 22

        System.out.println("\n═══ M3: Decode String ═══");
        System.out.println(sol.decodeString("3[a]2[bc]"));     // "aaabcbc"
        System.out.println(sol.decodeString("3[a2[c]]"));      // "accaccacc"
        System.out.println(sol.decodeString("2[abc]3[cd]ef")); // "abcabccdcdcdef"

        System.out.println("\n═══ M4: Asteroid Collision ═══");
        System.out.println(Arrays.toString(
            sol.asteroidCollision(new int[]{5,10,-5})));    // [5,10]
        System.out.println(Arrays.toString(
            sol.asteroidCollision(new int[]{8,-8})));       // []
        System.out.println(Arrays.toString(
            sol.asteroidCollision(new int[]{10,2,-5})));    // [10]
        System.out.println(Arrays.toString(
            sol.asteroidCollision(new int[]{-2,-1,1,2})));  // [-2,-1,1,2]

        System.out.println("\n═══ M5: Remove K Digits ═══");
        System.out.println(sol.removeKdigits("1432219", 3));  // "1219"
        System.out.println(sol.removeKdigits("10200", 1));    // "200"
        System.out.println(sol.removeKdigits("10", 2));       // "0"

        System.out.println("\n═══ M6: Online Stock Span ═══");
        StockSpanner sp = new StockSpanner();
        System.out.println(sp.next(100));  // 1
        System.out.println(sp.next(80));   // 1
        System.out.println(sp.next(60));   // 1
        System.out.println(sp.next(70));   // 2
        System.out.println(sp.next(60));   // 1
        System.out.println(sp.next(75));   // 4
        System.out.println(sp.next(85));   // 6

        System.out.println("\n═══ M7: Simplify Path ═══");
        System.out.println(sol.simplifyPath("/home/"));           // "/home"
        System.out.println(sol.simplifyPath("/../"));             // "/"
        System.out.println(sol.simplifyPath("/home//foo/"));      // "/home/foo"
        System.out.println(sol.simplifyPath("/a/./b/../../c/"));  // "/c"

        System.out.println("\n═══ M8: 132 Pattern ═══");
        System.out.println(sol.find132pattern(new int[]{1,2,3,4}));    // false
        System.out.println(sol.find132pattern(new int[]{3,1,4,2}));    // true
        System.out.println(sol.find132pattern(new int[]{-1,3,2,0}));   // true
        System.out.println(sol.find132pattern(new int[]{1,0,1,-4,3,2})); // true
    }
}
