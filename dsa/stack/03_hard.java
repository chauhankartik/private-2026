/**
 * ============================================================
 *  STACK — HARD PROBLEMS
 * ============================================================
 *
 *  H1.  Largest Rectangle in Histogram              LC 84
 *  H2.  Maximal Rectangle                           LC 85
 *  H3.  Basic Calculator                            LC 224
 *  H4.  Trapping Rain Water                         LC 42
 *  H5.  Longest Valid Parentheses                   LC 32
 *  H6.  Sum of Subarray Minimums                    LC 907
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class StackHard {

    // =========================================================
    // H1. Largest Rectangle in Histogram  LC 84
    // Pattern: MONOTONIC STACK (increasing — area calculation)
    // =========================================================
    /**
     * Find the largest rectangle that can be formed in a histogram.
     *
     * Brute: for each bar, expand left and right while bars are ≥ current
     * height. Area = height × width. O(n²).
     *
     * Optimal: MONOTONIC INCREASING STACK (indices).
     * For each bar, pop all taller bars from the stack.
     * When we pop bar at index j with height h:
     *   Width = current_index - stack.peek() - 1  (or current_index if stack empty)
     *   Area = h × width.
     * After traversal, pop remaining bars with width extending to end.
     *
     * WHY MONOTONIC INCREASING:
     *   A bar can extend left as far as the first shorter bar.
     *   The stack holds bars in increasing order.
     *   When we encounter a shorter bar, all taller bars in the stack
     *   can no longer extend right → pop them and compute their areas.
     *   The previous bar in the stack is their left boundary.
     *
     * TRICK: Add a sentinel bar of height 0 at the end to flush the stack.
     *
     * Time:  O(n) — each bar pushed and popped at most once
     * Space: O(n)
     *
     * Follow-up: Maximal Rectangle in a binary matrix (LC 85)?
     *   Build a histogram for each row and apply this algorithm.
     */
    public int largestRectangleArea(int[] heights) {
        int n = heights.length;
        Deque<Integer> stack = new ArrayDeque<>();
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            int currHeight = (i == n) ? 0 : heights[i];  // sentinel

            while (!stack.isEmpty() && heights[stack.peek()] > currHeight) {
                int h = heights[stack.pop()];
                int w = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, h * w);
            }

            stack.push(i);
        }

        return maxArea;
    }

    // =========================================================
    // H2. Maximal Rectangle  LC 85
    // Pattern: HISTOGRAM PER ROW + H1
    // =========================================================
    /**
     * Find the largest rectangle containing only 1's in a binary matrix.
     *
     * Build a histogram of heights for each row:
     *   If matrix[i][j] == '1': heights[j] += 1.
     *   If matrix[i][j] == '0': heights[j] = 0.
     *
     * For each row, apply Largest Rectangle in Histogram (H1).
     *
     * WHY HISTOGRAM APPROACH:
     *   Each row acts as the "base" of a histogram.
     *   Heights represent consecutive 1's above the base.
     *   The max rectangle in that histogram gives the max rectangle
     *   with that row as the bottom edge.
     *   Taking the max across all rows gives the global answer.
     *
     * Time:  O(m × n)
     * Space: O(n)
     *
     * Follow-up: Count the number of rectangles containing only 1's?
     *   For each bar popped, contribution = h × (w choose 2) summed.
     *   More complex — use inclusion-exclusion or DP.
     */
    public int maximalRectangle(char[][] matrix) {
        if (matrix.length == 0) return 0;
        int n = matrix[0].length;
        int[] heights = new int[n];
        int maxArea = 0;

        for (char[] row : matrix) {
            for (int j = 0; j < n; j++) {
                heights[j] = (row[j] == '1') ? heights[j] + 1 : 0;
            }
            maxArea = Math.max(maxArea, largestRectangleArea(heights));
        }

        return maxArea;
    }

    // =========================================================
    // H3. Basic Calculator  LC 224
    // Pattern: STACK — Recursive descent / sign tracking
    // =========================================================
    /**
     * Evaluate expression with +, -, (, ), and non-negative integers.
     * No multiplication or division.
     *
     * Use a stack to handle parentheses.
     * Track: result (running total), sign (+1 or -1), current number.
     *
     * On digit: build the number.
     * On '+': add number × sign to result, reset number, sign = +1.
     * On '-': add number × sign to result, reset number, sign = -1.
     * On '(': push result and sign to stack, reset both.
     * On ')': add number × sign to result.
     *         Pop sign from stack, pop previous result.
     *         result = previous_result + popped_sign × result.
     *
     * WHY PUSH RESULT AND SIGN:
     *   Parentheses create a sub-expression.
     *   The result before '(' and the sign before '(' determine
     *   how the sub-expression's value is combined.
     *   Stack saves this context for later.
     *
     * Time:  O(n)
     * Space: O(n) — stack depth proportional to nesting
     *
     * Follow-up: Basic Calculator II (LC 227) with * and /?
     *   Handle precedence: evaluate * and / immediately,
     *   defer + and - by keeping a sign.
     */
    public int calculate(String s) {
        Deque<Integer> stack = new ArrayDeque<>();
        int result = 0;
        int sign = 1;
        int num = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
            } else if (c == '+') {
                result += sign * num;
                num = 0;
                sign = 1;
            } else if (c == '-') {
                result += sign * num;
                num = 0;
                sign = -1;
            } else if (c == '(') {
                stack.push(result);
                stack.push(sign);
                result = 0;
                sign = 1;
            } else if (c == ')') {
                result += sign * num;
                num = 0;
                result *= stack.pop();   // sign before '('
                result += stack.pop();   // result before '('
            }
        }

        result += sign * num;  // don't forget last number
        return result;
    }

    // =========================================================
    // H4. Trapping Rain Water  LC 42
    // Pattern: MONOTONIC STACK or TWO POINTERS
    // =========================================================
    /**
     * Given elevation map bars, compute trapped water after rain.
     *
     * Approach 1: TWO POINTERS — O(n) time, O(1) space.
     *   leftMax and rightMax track max height from each side.
     *   Water at any position = min(leftMax, rightMax) - height.
     *   Move the pointer with the smaller max inward.
     *
     * Approach 2: MONOTONIC STACK — O(n) time, O(n) space.
     *   Maintain a decreasing stack of indices.
     *   When current bar > stack top: pop (it's a valley).
     *   Water trapped = width × min(left_wall, current) - valley_height.
     *
     * WHY TWO POINTERS WORK:
     *   If leftMax < rightMax: water at left is bounded by leftMax
     *   (regardless of what's between right pointer and rightMax).
     *   So we can safely compute water and advance left pointer.
     *
     * Time:  O(n)
     * Space: O(1) for two pointers, O(n) for stack
     *
     * Follow-up: Trapping Rain Water II (LC 407) — 3D version?
     *   Use BFS + min-heap on the boundary.
     */
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                leftMax = Math.max(leftMax, height[left]);
                water += leftMax - height[left];
                left++;
            } else {
                rightMax = Math.max(rightMax, height[right]);
                water += rightMax - height[right];
                right--;
            }
        }

        return water;
    }

    // Stack-based approach
    public int trapStack(int[] height) {
        Deque<Integer> stack = new ArrayDeque<>();
        int water = 0;

        for (int i = 0; i < height.length; i++) {
            while (!stack.isEmpty() && height[i] > height[stack.peek()]) {
                int valley = stack.pop();
                if (stack.isEmpty()) break;

                int width = i - stack.peek() - 1;
                int bounded = Math.min(height[i], height[stack.peek()]) - height[valley];
                water += width * bounded;
            }
            stack.push(i);
        }

        return water;
    }

    // =========================================================
    // H5. Longest Valid Parentheses  LC 32
    // Pattern: STACK WITH INDICES
    // =========================================================
    /**
     * Find the length of the longest valid parentheses substring.
     *
     * Stack stores INDICES. Push -1 as initial base.
     * On '(': push index.
     * On ')': pop.
     *   If stack empty: push current index as new base.
     *   If stack non-empty: length = i - stack.peek(). Update max.
     *
     * WHY PUSH -1 AS BASE:
     *   The base represents the last unmatched ')' position (or -1).
     *   Length of valid substring = current_index - base.
     *   When we pop and the stack is empty, the current ')' becomes
     *   the new base (no valid prefix possible before it).
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Can you do it in O(1) space?
     *   Two-pass approach: left-to-right count open/close, reset when
     *   close > open. Then right-to-left, reset when open > close.
     */
    public int longestValidParentheses(String s) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(-1);  // base index
        int maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                stack.push(i);
            } else {
                stack.pop();
                if (stack.isEmpty()) {
                    stack.push(i);  // new base
                } else {
                    maxLen = Math.max(maxLen, i - stack.peek());
                }
            }
        }

        return maxLen;
    }

    // =========================================================
    // H6. Sum of Subarray Minimums  LC 907
    // Pattern: MONOTONIC STACK — Contribution technique
    // =========================================================
    /**
     * Sum of min(subarray) for all contiguous subarrays.
     *
     * Brute: enumerate all subarrays, find min of each — O(n²) or O(n³).
     *
     * Optimal: CONTRIBUTION TECHNIQUE.
     * For each element arr[i], count how many subarrays have arr[i] as minimum.
     * Contribution of arr[i] = arr[i] × left[i] × right[i]
     * where left[i] = distance to previous smaller element (or boundary)
     *       right[i] = distance to next smaller-or-equal element (or boundary)
     *
     * Use monotonic increasing stack to find PSE and NSE.
     *
     * WHY "STRICTLY SMALLER" on left and "SMALLER OR EQUAL" on right:
     *   This handles duplicates correctly.
     *   For equal elements, we count them as "belonging" to the rightmost one.
     *   This avoids double-counting subarrays where the minimum appears twice.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * Follow-up: Sum of Subarray MAXIMUMS?
     *   Same technique with Next Greater / Previous Greater.
     * Follow-up: Sum of Subarray Ranges (LC 2104)?
     *   Answer = sum of max(subarray) - sum of min(subarray).
     */
    public int sumSubarrayMins(int[] arr) {
        int n = arr.length;
        long MOD = 1_000_000_007;
        int[] left = new int[n];   // distance to Previous Smaller Element
        int[] right = new int[n];  // distance to Next Smaller-or-Equal Element
        Deque<Integer> stack = new ArrayDeque<>();

        // Find Previous Smaller Element distances (strict <)
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] >= arr[i])
                stack.pop();
            left[i] = stack.isEmpty() ? i + 1 : i - stack.peek();
            stack.push(i);
        }

        stack.clear();

        // Find Next Smaller-or-Equal Element distances (<=)
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && arr[stack.peek()] > arr[i])
                stack.pop();
            right[i] = stack.isEmpty() ? n - i : stack.peek() - i;
            stack.push(i);
        }

        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum = (sum + (long) arr[i] * left[i] % MOD * right[i] % MOD) % MOD;
        }

        return (int) sum;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        StackHard sol = new StackHard();

        System.out.println("═══ H1: Largest Rectangle in Histogram ═══");
        System.out.println(sol.largestRectangleArea(new int[]{2,1,5,6,2,3}));  // 10
        System.out.println(sol.largestRectangleArea(new int[]{2,4}));          // 4

        System.out.println("\n═══ H2: Maximal Rectangle ═══");
        System.out.println(sol.maximalRectangle(new char[][]{
            {'1','0','1','0','0'},
            {'1','0','1','1','1'},
            {'1','1','1','1','1'},
            {'1','0','0','1','0'}}));  // 6

        System.out.println("\n═══ H3: Basic Calculator ═══");
        System.out.println(sol.calculate("1 + 1"));             // 2
        System.out.println(sol.calculate(" 2-1 + 2 "));         // 3
        System.out.println(sol.calculate("(1+(4+5+2)-3)+(6+8)"));  // 23

        System.out.println("\n═══ H4: Trapping Rain Water ═══");
        System.out.println(sol.trap(
            new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));  // 6
        System.out.println(sol.trap(new int[]{4,2,0,3,2,5}));  // 9
        System.out.println("Stack: " + sol.trapStack(
            new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));  // 6

        System.out.println("\n═══ H5: Longest Valid Parentheses ═══");
        System.out.println(sol.longestValidParentheses("(()"));     // 2
        System.out.println(sol.longestValidParentheses(")()())"));  // 4
        System.out.println(sol.longestValidParentheses(""));        // 0

        System.out.println("\n═══ H6: Sum of Subarray Minimums ═══");
        System.out.println(sol.sumSubarrayMins(new int[]{3,1,2,4}));  // 17
        System.out.println(sol.sumSubarrayMins(new int[]{11,81,94,43,3}));  // 444
    }
}
