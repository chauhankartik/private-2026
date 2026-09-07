/**
 * ============================================================
 *  PATTERN 1 — CORE STACK MECHANICS AND PARSING
 *  Problem 3 (Hard): Basic Calculator   LC 224
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Implement a basic calculator to evaluate a simple string expression s.
 *    The expression may contain:
 *      - Non-negative integers
 *      - Operators: '+', '-'
 *      - Parentheses: '(', ')'
 *      - Spaces (ignored)
 *    No multiplication or division. No invalid input.
 *
 *  EXAMPLES:
 *    "1 + 1"       → 2
 *    " 2-1 + 2 "   → 3
 *    "(1+(4+5+2)-3)+(6+8)" → 23
 *
 *  CONSTRAINTS:
 *    1 <= s.length (= N) <= 3 × 10^5
 *    s contains digits, '+', '-', '(', ')', and spaces.
 *    s is guaranteed to be a valid expression.
 *    The answer is guaranteed to fit in a 32-bit signed integer.
 *
 *  KEY INSIGHT:
 *    When we encounter '(', we don't know yet how deep the nesting goes.
 *    Stack stores the (result, sign) STATE at the moment we entered each '('.
 *    When we hit ')', we unwind: add the inner result × current_sign to the
 *    outer result that was saved on the stack.
 *
 *  APPROACH 1: Deque stack storing (partial result, sign before '(') pairs
 *    Time:  O(N)
 *    Space: O(N) — stack depth bounded by nesting level
 *
 *  APPROACH 2: Array-based stack with int[] storing paired values
 *    Time:  O(N)
 *    Space: O(N) — two int[] arrays (results + signs)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Basic_Calculator {

    // =========================================================
    // APPROACH 1 — DEQUE STACK (RESULT + SIGN PAIRS)
    // =========================================================

    /**
     * Evaluates a basic arithmetic expression with + - and parentheses.
     *
     * STACK STATE MACHINE:
     *   currentResult: accumulator for the current parenthesis level.
     *   currentSign:   the sign (+1 or -1) of the NEXT number to be added.
     *
     *   ON '(':
     *     Push (currentResult, currentSign) — save outer scope.
     *     Reset currentResult = 0, currentSign = +1 for new inner scope.
     *
     *   ON ')':
     *     Finalize inner result.
     *     Pop (outerResult, outerSign) from stack.
     *     currentResult = outerResult + outerSign × currentResult.
     *
     *   ON digit: accumulate multi-digit number, then apply currentSign.
     *   ON '+': currentSign = +1.
     *   ON '-': currentSign = -1.
     *
     * @param expression the arithmetic expression string
     * @return integer result of evaluation
     *
     * Time:  O(N) — single pass
     * Space: O(N) — stack depth = nesting depth
     */
    public int calculateDeque(String expression) {
        if (expression == null || expression.isEmpty()) return 0;

        Deque<Integer> stateStack = new ArrayDeque<>();   // alternates: [result, sign, result, sign, ...]
        int currentResult = 0;
        int currentSign   = 1;       // +1 for positive, -1 for negative
        int currentNumber = 0;       // accumulator for multi-digit numbers
        int expressionLength = expression.length();

        for (int charIndex = 0; charIndex < expressionLength; charIndex++) {
            char currentChar = expression.charAt(charIndex);

            if (Character.isDigit(currentChar)) {
                // Accumulate multi-digit number: e.g., '1', '2' → 12
                currentNumber = currentNumber * 10 + (currentChar - '0');

            } else if (currentChar == '+' || currentChar == '-') {
                // Finalize the number we just accumulated
                currentResult += currentSign * currentNumber;
                currentNumber  = 0;
                currentSign    = (currentChar == '+') ? 1 : -1;

            } else if (currentChar == '(') {
                // Save outer scope: push currentResult, then push currentSign
                stateStack.push(currentResult);
                stateStack.push(currentSign);
                // Enter new inner scope
                currentResult = 0;
                currentSign   = 1;
                currentNumber = 0;

            } else if (currentChar == ')') {
                // Finalize the last number inside parentheses
                currentResult += currentSign * currentNumber;
                currentNumber  = 0;
                // Pop outer scope and apply the sign that preceded '('
                int signBeforeParenthesis = stateStack.pop();
                int outerResult           = stateStack.pop();
                currentResult = outerResult + signBeforeParenthesis * currentResult;
            }
            // Space characters: skip (no action needed)
        }

        // Handle any trailing number (expression doesn't end with operator)
        currentResult += currentSign * currentNumber;
        return currentResult;
    }

    // =========================================================
    // APPROACH 2 — PAIRED ARRAY STACKS (ZERO BOXING)
    // =========================================================

    /**
     * Same algorithm using two parallel int[] arrays for zero GC overhead.
     *
     * ARRAY STRUCTURE:
     *   resultArray[stackTop]:  saved currentResult before each '('
     *   signArray[stackTop]:    saved currentSign before each '('
     *   stackTop == -1 → no active parenthesis scope on stack
     *
     * Maximum nesting depth = N/2 (alternating '(' and ')' — worst case).
     * So array size N/2 + 1 suffices. We use N for safety.
     *
     * Time:  O(N)
     * Space: O(N) — two int[] of length N (primitive, no boxing)
     */
    public int calculateArray(String expression) {
        if (expression == null || expression.isEmpty()) return 0;

        int expressionLength = expression.length();
        int maxNestingDepth  = expressionLength / 2 + 1;

        int[] resultArray  = new int[maxNestingDepth];  // outer result stack
        int[] signArray    = new int[maxNestingDepth];  // outer sign stack
        int stackTop = -1;

        int currentResult = 0;
        int currentSign   = 1;
        int currentNumber = 0;

        for (int charIndex = 0; charIndex < expressionLength; charIndex++) {
            char currentChar = expression.charAt(charIndex);

            if (currentChar >= '0' && currentChar <= '9') {
                currentNumber = currentNumber * 10 + (currentChar - '0');

            } else if (currentChar == '+' || currentChar == '-') {
                currentResult += currentSign * currentNumber;
                currentNumber  = 0;
                currentSign    = (currentChar == '+') ? 1 : -1;

            } else if (currentChar == '(') {
                // PUSH to both arrays
                stackTop++;
                resultArray[stackTop] = currentResult;
                signArray[stackTop]   = currentSign;
                // Reset inner scope
                currentResult = 0;
                currentSign   = 1;
                currentNumber = 0;

            } else if (currentChar == ')') {
                currentResult += currentSign * currentNumber;
                currentNumber  = 0;
                // POP from both arrays
                int outerResult = resultArray[stackTop];
                int outerSign   = signArray[stackTop];
                stackTop--;
                currentResult = outerResult + outerSign * currentResult;
            }
        }

        currentResult += currentSign * currentNumber;
        return currentResult;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Basic_Calculator solver = new Problem_3_Hard_Basic_Calculator();

        System.out.println("========================================");
        System.out.println("  Basic Calculator — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<String, Integer> test = (expr, expected) -> {
            int dequeResult  = solver.calculateDeque(expr);
            int arrayResult  = solver.calculateArray(expr);
            String status = (dequeResult == expected && arrayResult == expected) ? "PASS" : "FAIL";
            System.out.printf("[%s] %-35s | Deque: %5d | Array: %5d | Expected: %d%n",
                status, "\"" + expr + "\"", dequeResult, arrayResult, expected);
        };

        System.out.println("\n--- Basic cases ---");
        test.accept("1 + 1",              2);
        test.accept(" 2-1 + 2 ",          3);
        test.accept("1",                  1);
        test.accept("0",                  0);

        System.out.println("\n--- Parentheses ---");
        test.accept("(1+(4+5+2)-3)+(6+8)",  23);
        test.accept("(1)",                   1);
        test.accept("(1+2)+(3+4)",           10);
        test.accept("((2+3))",               5);

        System.out.println("\n--- Negative numbers via unary minus ---");
        test.accept("-1",                   -1);
        test.accept("-(3)",                 -3);
        test.accept("-(-3)",                 3);
        test.accept("1-(-3)",               4);

        System.out.println("\n--- Multi-digit numbers ---");
        test.accept("100 + 200",            300);
        test.accept("999 - 1",              998);
        test.accept("(100+200)-(50+50)",    200);

        System.out.println("\n--- Deeply nested ---");
        test.accept("(((1+2)+3)+4)",        10);
        test.accept("((((10))))",           10);

        System.out.println("\n--- Sign changes ---");
        test.accept("1+2-3+4-5+6",         5);
        test.accept("-(1+1)",              -2);

        System.out.println("\n========================================");
        System.out.println("  All Calculator tests completed.");
        System.out.println("========================================");
    }
}
