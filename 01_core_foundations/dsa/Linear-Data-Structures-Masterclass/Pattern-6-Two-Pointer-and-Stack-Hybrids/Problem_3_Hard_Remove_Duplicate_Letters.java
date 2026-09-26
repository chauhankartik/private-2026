/**
 * ============================================================
 *  PATTERN 6 — TWO-POINTER AND STACK HYBRIDS
 *  Problem 3 (Hard): Remove Duplicate Letters   LC 316
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a string s, remove duplicate letters so that every letter appears
 *    exactly once. Return the result that is SMALLEST in lexicographic order
 *    among all possible results.
 *
 *  EXAMPLE:
 *    s="bcabc"    → "abc"
 *    s="cbacdcbc" → "acdb"
 *
 *  CONSTRAINTS:
 *    1 <= s.length (= N) <= 10^4
 *    s consists of lowercase English letters only.
 *
 *  KEY INSIGHT — GREEDY MONOTONIC STACK:
 *    Build the result character by character using a monotonic increasing stack.
 *    At each character c, greedily pop larger characters from the stack IF:
 *      1. The top character is LEXICOGRAPHICALLY LARGER than c (popping improves order).
 *      2. The top character still HAS REMAINING OCCURRENCES later in the string
 *         (we can still include it later without losing it).
 *    If either condition fails, keep the top.
 *    Track characters already IN the result (don't add duplicates).
 *
 *  APPROACH 1: Monotonic stack with frequency array and inResult boolean array
 *    Time:  O(N)  — each character pushed/popped at most once; frequency counting O(N)
 *    Space: O(26) — stack, frequency, and inResult arrays bounded by alphabet size
 *
 *  APPROACH 2: Array-backed char[] stack (zero GC overhead)
 *    Time:  O(N)
 *    Space: O(26 + result_length) — char[] + frequency[] + boolean[]
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Remove_Duplicate_Letters {

    // =========================================================
    // APPROACH 1 — MONOTONIC STACK WITH DEQUE
    // =========================================================

    /**
     * Removes duplicate letters and returns lexicographically smallest result.
     *
     * ALGORITHM:
     *   1. Count remaining occurrences: remainingCount[c] = how many times c still
     *      appears at position >= currentIndex (maintained by decrementing as we scan).
     *   2. Track inResultStack[c] = whether character c is already in the result stack.
     *
     *   For each character c at position i:
     *     a. Decrement remainingCount[c] (we've passed this occurrence).
     *     b. If c is already in result: SKIP (don't add duplicate).
     *     c. GREEDY POP: While stack not empty AND top > c AND remainingCount[top] > 0:
     *          Pop top. Mark top as NOT in result.
     *          (We're removing a larger character that still appears later → safe to remove now)
     *     d. Push c. Mark c as in result.
     *
     * GREEDY CORRECTNESS:
     *   If top > c and top can be re-included later (remainingCount > 0):
     *     Keeping top would give a larger result ("...top...c...") vs. placing c first.
     *     Replacing top with c here and re-adding top later is always lexicographically better.
     *
     * @param s input string with lowercase letters
     * @return lexicographically smallest string with each letter appearing once
     *
     * Time:  O(N)  — single pass, each char pushed/popped at most once
     * Space: O(26) — constant extra space (alphabet-bounded arrays)
     */
    public String removeDuplicateLettersDeque(String s) {
        if (s == null || s.isEmpty()) return "";

        // Count total occurrences (remainingCount[c] = occurrences from current position onward)
        int[] remainingCount = new int[26];
        for (char character : s.toCharArray()) {
            remainingCount[character - 'a']++;
        }

        boolean[] inResultStack = new boolean[26];   // true if character is already in result
        Deque<Character> monotonicStack = new ArrayDeque<>();

        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            char currentChar = s.charAt(charIndex);
            int currentCharCode = currentChar - 'a';

            // Decrement remaining count (we're consuming this occurrence)
            remainingCount[currentCharCode]--;

            // Skip if already in result (adding again would create duplicate)
            if (inResultStack[currentCharCode]) continue;

            // GREEDY POP: remove characters from top that are:
            //   (1) lexicographically larger than currentChar, AND
            //   (2) still have future occurrences (safe to remove now)
            while (!monotonicStack.isEmpty()) {
                char topChar = monotonicStack.peek();
                if (topChar > currentChar && remainingCount[topChar - 'a'] > 0) {
                    monotonicStack.pop();
                    inResultStack[topChar - 'a'] = false;   // top is no longer in result
                } else {
                    break;
                }
            }

            monotonicStack.push(currentChar);
            inResultStack[currentCharCode] = true;
        }

        // Build result string (stack bottom to top = left to right)
        char[] resultChars = new char[monotonicStack.size()];
        int    fillIndex   = monotonicStack.size() - 1;
        for (char c : monotonicStack) {
            resultChars[fillIndex--] = c;
        }
        return new String(resultChars);
    }

    // =========================================================
    // APPROACH 2 — ARRAY-BACKED CHAR[] STACK (ZERO GC)
    // =========================================================

    /**
     * Same greedy algorithm with a raw char[] as the monotonic stack.
     *
     * ARRAY STACK INVARIANT:
     *   charStack[0 .. stackTopPointer] = the current result candidates.
     *   charStack[stackTopPointer] = the most recently added character (top of stack).
     *   stackTopPointer == -1 → empty result.
     *
     * MAXIMUM RESULT LENGTH = 26 (at most one of each letter).
     * So char[] of size 26 is always sufficient.
     *
     * @param s input string
     * @return lexicographically smallest deduplicated string
     *
     * Time:  O(N)
     * Space: O(26) — char[26] stack + int[26] remainingCount + boolean[26] inStack
     */
    public String removeDuplicateLettersArray(String s) {
        if (s == null || s.isEmpty()) return "";

        int[] remainingCount = new int[26];
        for (char character : s.toCharArray()) {
            remainingCount[character - 'a']++;
        }

        boolean[] inStack        = new boolean[26];
        char[]    charStack      = new char[26];     // max 26 distinct letters
        int       stackTopPointer = -1;

        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            char currentChar    = s.charAt(charIndex);
            int  currentCode    = currentChar - 'a';

            remainingCount[currentCode]--;

            if (inStack[currentCode]) continue;

            // GREEDY POP
            while (stackTopPointer >= 0
                   && charStack[stackTopPointer] > currentChar
                   && remainingCount[charStack[stackTopPointer] - 'a'] > 0) {
                inStack[charStack[stackTopPointer] - 'a'] = false;
                stackTopPointer--;   // POP
            }

            charStack[++stackTopPointer] = currentChar;   // PUSH
            inStack[currentCode] = true;
        }

        return new String(charStack, 0, stackTopPointer + 1);
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Remove_Duplicate_Letters solver =
            new Problem_3_Hard_Remove_Duplicate_Letters();

        System.out.println("========================================");
        System.out.println("  Remove Duplicate Letters — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<String> testAll = (s) -> {
            String deque = solver.removeDuplicateLettersDeque(s);
            String arr   = solver.removeDuplicateLettersArray(s);
            boolean ok   = deque.equals(arr);
            System.out.printf("s=%-20s | Deque=%-10s Array=%-10s %s%n",
                "\""+s+"\"", "\""+deque+"\"", "\""+arr+"\"", ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 316 examples ---");
        testAll.accept("bcabc");       // expected "abc"
        testAll.accept("cbacdcbc");    // expected "acdb"

        System.out.println("\n--- Single character ---");
        testAll.accept("a");           // expected "a"
        testAll.accept("aaa");         // expected "a"

        System.out.println("\n--- Already sorted, no duplicates ---");
        testAll.accept("abcde");       // expected "abcde"

        System.out.println("\n--- All same character ---");
        testAll.accept("bbbbb");       // expected "b"

        System.out.println("\n--- Reverse sorted with duplicates ---");
        testAll.accept("zyxwvu");      // expected "zyxwvu" (each appears once, must keep all)

        System.out.println("\n--- Mix requiring aggressive popping ---");
        testAll.accept("cbbd");        // expected "cbd"
        testAll.accept("abacb");       // expected "abc"

        System.out.println("\n--- All 26 letters ---");
        String allLetters = "zyxwvutsrqponmlkjihgfedcba";
        testAll.accept(allLetters);    // each appears once → unchanged

        System.out.println("\n--- Duplicate letters with greedy tradeoff ---");
        testAll.accept("eccbacba");    // expected "eabcde"? let's verify
        testAll.accept("bdba");        // expected "bda"? verify

        System.out.println("\n--- Empty string ---");
        testAll.accept("");            // expected ""

        System.out.println("\n========================================");
        System.out.println("  All Remove Duplicate Letters tests done.");
        System.out.println("========================================");
    }
}
