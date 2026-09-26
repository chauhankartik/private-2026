/**
 * ============================================================
 *  PATTERN 1 — CORE STACK MECHANICS AND PARSING
 *  Problem 1 (Basic): Valid Parentheses   LC 20
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a string s containing only '(', ')', '{', '}', '[', ']',
 *    determine if the input string is valid. A string is valid if:
 *      1. Open brackets are closed by the same type of brackets.
 *      2. Open brackets are closed in the correct order.
 *      3. Every close bracket has a corresponding open bracket.
 *
 *  CONSTRAINTS:
 *    1 <= s.length (= N) <= 10^4
 *    s consists only of bracket characters.
 *
 *  APPROACH 1: Deque-backed stack (ArrayDeque<Character>)
 *    Time:  O(N) — single pass through string
 *    Space: O(N) — worst case stack holds all opening brackets (e.g., "((((")
 *
 *  APPROACH 2: Array-backed stack with primitive char[] — zero GC overhead
 *    Time:  O(N)
 *    Space: O(N) — bounded char[] of size N
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Valid_Parentheses {

    // =========================================================
    // APPROACH 1 — DEQUE-BACKED STACK (STANDARD COLLECTION)
    // =========================================================

    /**
     * Validates balanced brackets using an ArrayDeque as a stack.
     *
     * ALGORITHM:
     *   Scan each character left to right.
     *   If it's an OPENER ('(', '{', '[') → push it.
     *   If it's a CLOSER:
     *     - If stack is empty → no matching opener → INVALID.
     *     - If stack top does NOT match this closer → wrong order → INVALID.
     *     - Else → pop the matched opener and continue.
     *   After scanning: valid only if stack is EMPTY (all openers matched).
     *
     * @param inputString bracket string to validate
     * @return true if all brackets are correctly matched and ordered
     *
     * Time:  O(N)
     * Space: O(N) — up to N/2 openers on the stack
     */
    public boolean isValidDeque(String inputString) {
        if (inputString == null || inputString.isEmpty()) return true;

        Deque<Character> bracketStack = new ArrayDeque<>();

        for (int charIndex = 0; charIndex < inputString.length(); charIndex++) {
            char currentChar = inputString.charAt(charIndex);

            if (isOpeningBracket(currentChar)) {
                bracketStack.push(currentChar);
            } else {
                // Closing bracket: stack must be non-empty and top must match
                if (bracketStack.isEmpty()) return false;
                char topOpener = bracketStack.pop();
                if (!isMatchingPair(topOpener, currentChar)) return false;
            }
        }

        // Valid only if every opener has been matched and popped
        return bracketStack.isEmpty();
    }

    private boolean isOpeningBracket(char c) {
        return c == '(' || c == '{' || c == '[';
    }

    private boolean isMatchingPair(char opener, char closer) {
        return (opener == '(' && closer == ')')
            || (opener == '{' && closer == '}')
            || (opener == '[' && closer == ']');
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED STACK (ZERO GC, PRIMITIVE)
    // =========================================================

    /**
     * Validates brackets using a raw char[] array as a stack.
     * No object allocation, no boxing — optimal for performance-critical paths.
     *
     * STACK INVARIANT:
     *   arrayStack[0 .. stackTopPointer] holds the current open brackets.
     *   stackTopPointer == -1 → stack is EMPTY.
     *   stackTopPointer == N-1 → stack is FULL (can't happen since closers pop).
     *
     * @param inputString bracket string to validate
     * @return true if valid
     *
     * Time:  O(N)
     * Space: O(N) — fixed char[] of length N (the maximum possible stack depth)
     */
    public boolean isValidArray(String inputString) {
        if (inputString == null || inputString.isEmpty()) return true;

        int stringLength = inputString.length();
        char[] arrayStack = new char[stringLength];   // max stack depth = N
        int stackTopPointer = -1;                      // -1 = empty

        for (int charIndex = 0; charIndex < stringLength; charIndex++) {
            char currentChar = inputString.charAt(charIndex);

            switch (currentChar) {
                case '(':
                case '{':
                case '[':
                    arrayStack[++stackTopPointer] = currentChar;  // PUSH
                    break;

                case ')':
                    if (stackTopPointer == -1 || arrayStack[stackTopPointer] != '(') return false;
                    stackTopPointer--;  // POP
                    break;

                case '}':
                    if (stackTopPointer == -1 || arrayStack[stackTopPointer] != '{') return false;
                    stackTopPointer--;
                    break;

                case ']':
                    if (stackTopPointer == -1 || arrayStack[stackTopPointer] != '[') return false;
                    stackTopPointer--;
                    break;

                default:
                    // Non-bracket character: ignore (problem says only brackets, but be defensive)
                    break;
            }
        }

        return stackTopPointer == -1;  // stack must be empty for full validity
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Valid_Parentheses solver = new Problem_1_Basic_Valid_Parentheses();

        System.out.println("========================================");
        System.out.println("  Valid Parentheses — Test Suite");
        System.out.println("========================================");

        // Helper to test both approaches
        java.util.function.BiConsumer<String, Boolean> test = (input, expected) -> {
            boolean dequeResult = solver.isValidDeque(input);
            boolean arrayResult = solver.isValidArray(input);
            String status = (dequeResult == expected && arrayResult == expected) ? "PASS" : "FAIL";
            System.out.printf("[%s] Input: %-20s | Deque: %-5b | Array: %-5b | Expected: %b%n",
                status, "\"" + input + "\"", dequeResult, arrayResult, expected);
        };

        System.out.println("\n--- Valid Cases ---");
        test.accept("()", true);
        test.accept("()[]{}", true);
        test.accept("{[()]}", true);
        test.accept("((((((()))))))", true);
        test.accept("", true);   // empty string is vacuously valid

        System.out.println("\n--- Invalid Cases ---");
        test.accept("(]", false);
        test.accept("([)]", false);        // wrong order
        test.accept("(((",  false);        // unclosed openers
        test.accept(")))",  false);        // no matching openers
        test.accept("{[}]", false);        // interleaved mismatch
        test.accept("]",    false);        // starts with closer

        System.out.println("\n--- Edge Cases ---");
        test.accept("(", false);           // single unmatched opener
        test.accept(")", false);           // single unmatched closer
        test.accept("{}", true);
        test.accept("[]", true);

        System.out.println("\n--- Stress: Deeply nested valid ---");
        StringBuilder nested = new StringBuilder();
        int depth = 1000;
        for (int i = 0; i < depth; i++) nested.append('(');
        for (int i = 0; i < depth; i++) nested.append(')');
        boolean deepResult = solver.isValidDeque(nested.toString());
        System.out.println("Depth-1000 nested: " + deepResult + " (expected true)");

        System.out.println("\n========================================");
        System.out.println("  All tests completed.");
        System.out.println("========================================");
    }
}
