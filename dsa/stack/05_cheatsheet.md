# Stack — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "valid parentheses / brackets / matching" | Stack — push open, pop on close |
| "next greater / next smaller element" | Monotonic Stack |
| "daily temperatures / warmer day" | Monotonic Decreasing Stack (indices) |
| "stock span / consecutive days ≤" | Monotonic Decreasing Stack (span pairs) |
| "largest rectangle in histogram" | Monotonic Increasing Stack (indices) |
| "trapping rain water" | Monotonic Stack or Two Pointers |
| "evaluate expression / RPN / postfix" | Stack — operand/operator processing |
| "calculator with +, -, *, /" | Stack — sign/precedence handling |
| "decode / nested brackets / k[string]" | Two stacks (count + string) |
| "remove duplicates / cancel adjacent" | Stack as buffer |
| "asteroid collision / cancel pairs" | Stack simulation |
| "simplify Unix path" | Stack parsing (/., /.., components) |
| "min/max stack in O(1)" | Stack as History (auxiliary stack) |
| "undo / redo / browser back/forward" | Stack as History |
| "iterative DFS / tree traversal" | Explicit Stack (replaces recursion) |
| "132 pattern / find subsequence" | Monotonic Stack (reverse traversal) |
| "remove k digits for smallest number" | Monotonic Increasing Stack (greedy) |
| "lexicographically smallest subsequence" | Monotonic Stack + last-occurrence check |
| "sum of subarray minimums/maximums" | Monotonic Stack — contribution technique |
| "number of visible people" | Monotonic Decreasing Stack |

---

## The Five Core Templates (Memorize These)

### Template 1: Monotonic Stack — Next Greater Element

```java
Deque<Integer> stack = new ArrayDeque<>();  // stores INDICES
int[] result = new int[n];
Arrays.fill(result, -1);

for (int i = 0; i < n; i++) {
    while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
        result[stack.pop()] = nums[i];  // nums[i] is the NGE
    }
    stack.push(i);
}
```

### Template 2: Valid Parentheses Matching

```java
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
```

### Template 3: Expression Evaluation (+ - * /)

```java
Deque<Integer> stack = new ArrayDeque<>();
int num = 0;
char prevSign = '+';

for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
    if (Character.isDigit(c)) num = num * 10 + (c - '0');
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
for (int v : stack) result += v;
```

### Template 4: Largest Rectangle in Histogram

```java
Deque<Integer> stack = new ArrayDeque<>();
int maxArea = 0;

for (int i = 0; i <= n; i++) {
    int h = (i == n) ? 0 : heights[i];  // sentinel at end
    while (!stack.isEmpty() && heights[stack.peek()] > h) {
        int height = heights[stack.pop()];
        int width = stack.isEmpty() ? i : i - stack.peek() - 1;
        maxArea = Math.max(maxArea, height * width);
    }
    stack.push(i);
}
```

### Template 5: Contribution Technique (Sum of Subarray Mins)

```java
// Find PSE (Previous Smaller Element) and NSE (Next Smaller-or-Equal)
int[] left = new int[n], right = new int[n];
Deque<Integer> stack = new ArrayDeque<>();

for (int i = 0; i < n; i++) {
    while (!stack.isEmpty() && arr[stack.peek()] >= arr[i]) stack.pop();
    left[i] = stack.isEmpty() ? i + 1 : i - stack.peek();
    stack.push(i);
}
stack.clear();
for (int i = n - 1; i >= 0; i--) {
    while (!stack.isEmpty() && arr[stack.peek()] > arr[i]) stack.pop();
    right[i] = stack.isEmpty() ? n - i : stack.peek() - i;
    stack.push(i);
}

long sum = 0;
for (int i = 0; i < n; i++)
    sum += (long) arr[i] * left[i] * right[i];
```

---

## Java Stack API Quick Reference

```java
// Stack (LIFO) — use Deque interface with ArrayDeque
Deque<E> stack = new ArrayDeque<>();
stack.push(e);     // add to top (front of deque)
stack.pop();       // remove from top (throws if empty)
stack.peek();      // view top (returns null if empty)
stack.isEmpty();   // check if empty
stack.size();      // number of elements

// AVOID: Stack<E> class (legacy, synchronized, extends Vector)
// WHY: ArrayDeque is faster (no synchronization, no node allocation)
```

**IMPORTANT: Deque as Stack**
```
ArrayDeque.push() = addFirst() → adds to FRONT
ArrayDeque.pop()  = removeFirst() → removes from FRONT
ArrayDeque.peek() = peekFirst() → views FRONT

This gives correct LIFO behavior.
Iteration order: TOP to BOTTOM (front to back).
```

---

## Complexity Quick Reference

| Problem Type | Time | Space |
|---|---|---|
| Valid Parentheses | O(n) | O(n) |
| Next Greater / Smaller Element | O(n) | O(n) |
| Daily Temperatures | O(n) | O(n) |
| Largest Rectangle in Histogram | O(n) | O(n) |
| Trapping Rain Water (two pointers) | O(n) | O(1) |
| Trapping Rain Water (stack) | O(n) | O(n) |
| Expression Evaluation (RPN) | O(n) | O(n) |
| Basic Calculator I / II | O(n) | O(n) |
| Decode String | O(maxK × n) | O(n) |
| Sum of Subarray Minimums | O(n) | O(n) |
| Min Stack (all operations) | O(1) | O(n) |
| FreqStack (push/pop) | O(1) | O(n) |
| Remove K Digits | O(n) | O(n) |
| Remove Duplicate Letters | O(n) | O(1) |

---

## Problem → Pattern Map (for Google interviews)

```
Valid Parentheses / Bracket Matching      → Stack (push expected close)
Min Stack / Max Stack                     → Auxiliary stack tracking min/max
Daily Temperatures                        → Monotonic Decreasing Stack
Stock Span                                → Monotonic Decreasing Stack (span)
Next Greater Element I/II                 → Monotonic Decreasing Stack
Largest Rectangle in Histogram            → Monotonic Increasing Stack
Maximal Rectangle                         → Histogram per row + above
Trapping Rain Water                       → Two Pointers or Monotonic Stack
Basic Calculator I                        → Stack + sign + parentheses
Basic Calculator II                       → Stack + precedence (*/first)
Evaluate RPN                              → Operand stack
Decode String                             → Two stacks (count + string)
Asteroid Collision                        → Stack simulation (collision)
Remove K Digits                           → Monotonic Increasing Stack
Remove Duplicate Letters                  → Monotonic Stack + last-index
132 Pattern                               → Reverse + Monotonic Decreasing
Longest Valid Parentheses                 → Stack with indices
Sum of Subarray Minimums                  → Contribution (PSE + NSE)
Sum of Subarray Ranges                    → Double contribution (min + max)
Visible People in a Queue                 → Monotonic Decreasing + counting
Maximum Frequency Stack                   → freq map + group-by-freq stacks
```

---

## Monotonic Stack — Which Direction?

```
NEXT GREATER ELEMENT → Monotonic DECREASING stack (bottom→top)
  Pop when current > top (top found its next greater)
  Remaining in stack: no next greater → -1

NEXT SMALLER ELEMENT → Monotonic INCREASING stack (bottom→top)
  Pop when current < top (top found its next smaller)
  Remaining in stack: no next smaller → -1

LARGEST RECTANGLE → Monotonic INCREASING stack
  Pop when current < top (top can't extend right anymore)
  Width = current_index - stack.peek() - 1

REMOVE K DIGITS → Monotonic INCREASING stack
  Pop when current < top AND k > 0 (remove larger left digits)
```

---

## Interview Communication Script

When you start solving any stack problem:

> "I recognize this as a [monotonic stack / expression eval / matching] problem
> because [we need next greater / need to evaluate / need to match brackets].
> - **Invariant**: The stack maintains [decreasing/increasing] order of [values/indices].
> - **Push**: When [condition]. **Pop**: When [condition].
> - **Answer extraction**: [When we pop / from remaining stack / final sum].
> The time complexity is O(n) because each element is pushed and popped
> at most once. The space is O(n) for the stack."

For contribution technique problems:

> "I need to calculate how many subarrays have arr[i] as the minimum.
> I'll find Previous Smaller Element (left boundary) and Next Smaller-or-Equal
> Element (right boundary) using two passes of a monotonic increasing stack.
> Contribution of arr[i] = arr[i] × left[i] × right[i].
> I use strict < on left and ≤ on right to handle duplicates."

---

*Files in this module:*
- `00_theory.md` — Stack variants, implementations, five patterns, common bugs
- `01_easy.java` — 8 easy problems (parentheses, min stack, next greater, duplicates)
- `02_medium.java` — 8 medium problems (daily temps, decode, asteroid, 132 pattern)
- `03_hard.java` — 6 hard problems (histogram, calculator, rain water, subarray mins)
- `04_google_level.java` — 5 Google-level (FreqStack, visible people, duplicate letters)
- `05_cheatsheet.md` — This file
- `06_top50_problems.md` — Top 50 problems in intuition-building order
