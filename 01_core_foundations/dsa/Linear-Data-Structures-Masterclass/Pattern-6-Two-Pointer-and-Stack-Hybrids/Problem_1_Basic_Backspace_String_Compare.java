/**
 * ============================================================
 *  PATTERN 6 — TWO-POINTER AND STACK HYBRIDS
 *  Problem 1 (Basic): Backspace String Compare   LC 844
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given two strings s and t, return true if they are equal when both
 *    are typed into empty text editors. '#' means a backspace character.
 *    Note: backspacing an empty text does nothing.
 *
 *  EXAMPLE:
 *    s="ab#c", t="ad#c"  → true  (both become "ac")
 *    s="ab##", t="c#d#"  → true  (both become "")
 *    s="a#c",  t="b"     → false ("c" vs "b")
 *
 *  CONSTRAINTS:
 *    1 <= s.length, t.length (= N) <= 200
 *    s and t consist of lowercase letters and '#'.
 *
 *  APPROACH 1: Stack-based simulation — simulate typing character by character
 *    Time:  O(N + M)  — process both strings
 *    Space: O(N + M)  — two stacks (or StringBuilder) for final strings
 *
 *  APPROACH 2: Two-pointer backward scan — O(1) space
 *    Scan both strings RIGHT-TO-LEFT simultaneously.
 *    Count pending backspaces. Skip characters accordingly.
 *    Compare only the "surviving" characters.
 *    Time:  O(N + M)
 *    Space: O(1)  — no stacks needed
 * ============================================================
 */
import java.util.*;

public class Problem_1_Basic_Backspace_String_Compare {

    // =========================================================
    // APPROACH 1 — STACK SIMULATION
    // =========================================================

    /**
     * Compares two strings after applying backspace operations using stacks.
     *
     * STACK INVARIANT:
     *   After processing, stack contains the characters that SURVIVE (from bottom to top).
     *   Each character push = typing a character.
     *   Each '#' = pop (backspace), if stack is non-empty.
     *
     * @param s first typed string
     * @param t second typed string
     * @return true if both strings produce the same final text
     *
     * Time:  O(N + M)
     * Space: O(N + M) — two stacks storing surviving characters
     */
    public boolean backspaceCompareStack(String s, String t) {
        if (s == null || t == null) return s == t;
        return applyBackspaces(s).equals(applyBackspaces(t));
    }

    /**
     * Simulates typing the string into a text editor using a Deque as stack.
     * Returns the final text content as a String.
     */
    private String applyBackspaces(String typedString) {
        Deque<Character> characterStack = new ArrayDeque<>();

        for (int charIndex = 0; charIndex < typedString.length(); charIndex++) {
            char currentChar = typedString.charAt(charIndex);

            if (currentChar == '#') {
                // Backspace: remove the last typed character (if any)
                if (!characterStack.isEmpty()) {
                    characterStack.pop();
                }
                // If stack is empty, backspace does nothing (problem states this is valid)
            } else {
                // Regular character: type it
                characterStack.push(currentChar);
            }
        }

        // Build result string from stack (bottom to top = left to right in final text)
        StringBuilder finalText = new StringBuilder();
        // Deque.push() adds to HEAD — so stack is reversed relative to typing order.
        // Convert to array and reverse:
        Object[] stackContents = characterStack.toArray();
        for (int i = stackContents.length - 1; i >= 0; i--) {
            finalText.append(stackContents[i]);
        }
        return finalText.toString();
    }

    /**
     * Array-backed version: uses char[] as stack for zero GC overhead.
     *
     * Time:  O(N + M)
     * Space: O(N + M) — char[] of length N and M
     */
    public boolean backspaceCompareArray(String s, String t) {
        return applyBackspacesArray(s).equals(applyBackspacesArray(t));
    }

    private String applyBackspacesArray(String typedString) {
        int length = typedString.length();
        char[] charStack = new char[length];
        int stackTop     = -1;

        for (int i = 0; i < length; i++) {
            char c = typedString.charAt(i);
            if (c == '#') {
                if (stackTop >= 0) stackTop--;   // pop (backspace)
            } else {
                charStack[++stackTop] = c;       // push character
            }
        }
        return new String(charStack, 0, stackTop + 1);
    }

    // =========================================================
    // APPROACH 2 — TWO-POINTER BACKWARD SCAN (O(1) SPACE)
    // =========================================================

    /**
     * Compares two strings using a backward two-pointer scan with O(1) space.
     *
     * BACKWARD SCAN RATIONALE:
     *   Backspaces affect PRECEDING characters. Scanning right-to-left lets us
     *   count pending backspaces first, then skip the affected characters.
     *   We never need to know what came before — only what SURVIVES.
     *
     * ALGORITHM:
     *   leftPointer = s.length() - 1  (starts at end of s)
     *   rightPointer = t.length() - 1 (starts at end of t)
     *
     *   Loop until both pointers exhaust their strings:
     *     1. Advance leftPointer left, skipping characters consumed by '#':
     *        Count pending backspaces. Skip '#' (increment skip count).
     *        Skip regular characters if pending skips > 0 (decrement skip count).
     *     2. Advance rightPointer similarly.
     *     3. Compare surviving characters at leftPointer and rightPointer.
     *
     * @param s first string
     * @param t second string
     * @return true if both produce the same text after backspacing
     *
     * Time:  O(N + M)
     * Space: O(1)  — only integer pointers and counters
     */
    public boolean backspaceCompareTwoPointer(String s, String t) {
        if (s == null || t == null) return s == t;

        int leftPointer  = s.length() - 1;
        int rightPointer = t.length() - 1;

        while (leftPointer >= 0 || rightPointer >= 0) {
            // Advance leftPointer to the next SURVIVING character in s
            leftPointer  = findNextSurvivingChar(s, leftPointer);
            // Advance rightPointer to the next SURVIVING character in t
            rightPointer = findNextSurvivingChar(t, rightPointer);

            // Both exhausted → both strings finished → equal so far
            if (leftPointer < 0 && rightPointer < 0) return true;

            // One exhausted, one hasn't → different lengths → not equal
            if (leftPointer < 0 || rightPointer < 0) return false;

            // Both have a surviving character — compare them
            if (s.charAt(leftPointer) != t.charAt(rightPointer)) return false;

            // Matched — advance both to next characters
            leftPointer--;
            rightPointer--;
        }

        return true;
    }

    /**
     * Scans backward from startIndex, skipping characters consumed by '#'.
     * @return index of the next surviving character, or -1 if none remain.
     */
    private int findNextSurvivingChar(String str, int startIndex) {
        int pendingBackspaceCount = 0;

        while (startIndex >= 0) {
            char currentChar = str.charAt(startIndex);

            if (currentChar == '#') {
                // This '#' will consume the previous surviving character
                pendingBackspaceCount++;
                startIndex--;
            } else if (pendingBackspaceCount > 0) {
                // This character is consumed by a pending backspace — skip it
                pendingBackspaceCount--;
                startIndex--;
            } else {
                // This character SURVIVES — stop here
                break;
            }
        }

        return startIndex;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_1_Basic_Backspace_String_Compare solver =
            new Problem_1_Basic_Backspace_String_Compare();

        System.out.println("========================================");
        System.out.println("  Backspace String Compare — Test Suite");
        System.out.println("========================================");

        java.util.function.BiConsumer<String, String> testAll = (s, t) -> {
            boolean stack    = solver.backspaceCompareStack(s, t);
            boolean array    = solver.backspaceCompareArray(s, t);
            boolean twoPtr   = solver.backspaceCompareTwoPointer(s, t);
            boolean ok       = (stack == array && array == twoPtr);
            System.out.printf("s=%-15s t=%-15s | Stack=%-5b Array=%-5b 2Ptr=%-5b %s%n",
                "\""+s+"\"", "\""+t+"\"", stack, array, twoPtr, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 844 examples ---");
        testAll.accept("ab#c",  "ad#c");   // true
        testAll.accept("ab##",  "c#d#");   // true
        testAll.accept("a#c",   "b");      // false

        System.out.println("\n--- Multiple consecutive backspaces ---");
        testAll.accept("a##",   "");        // true (both empty)
        testAll.accept("a###",  "");        // true (extra backspace on empty = ignore)
        testAll.accept("abc###","");        // true

        System.out.println("\n--- Backspace at start ---");
        testAll.accept("#a",    "a");       // true ('#' on empty string ignored)
        testAll.accept("##a",   "a");       // true

        System.out.println("\n--- Same string, no backspaces ---");
        testAll.accept("abc",   "abc");     // true
        testAll.accept("abc",   "abd");     // false

        System.out.println("\n--- All backspaces ---");
        testAll.accept("aaa###","");        // true
        testAll.accept("a#b#c#","");        // true

        System.out.println("\n--- Different lengths but equal after backspace ---");
        testAll.accept("a#bc",  "bc");      // true (a erased → bc = bc)

        System.out.println("\n========================================");
        System.out.println("  All Backspace Compare tests done.");
        System.out.println("========================================");
    }
}
