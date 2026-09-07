# Pattern 6 — Two-Pointer and Stack Hybrids

## The Concept: Real-World Domain Analogy
**Water tank filling**: Given walls of varying heights, rainwater fills between them.
You need to know how much water is trapped. Two pointers close in from both ends —
left pointer handles left-bounded trapping, right pointer handles right-bounded trapping.
This eliminates the need for precomputed left-max and right-max arrays.

**Undo in a document with "delete previous character"**: Process a string that includes
backspace characters (`#`). The output is what remains after applying all backspaces.
A stack simulates the document buffer — push characters, pop on backspace.

**Greedy lexicographic string compression**: Remove duplicate letters to get the smallest
lexicographic result. A monotonic stack tracks the "best so far" sequence while a
frequency count tells us whether we can afford to discard a character.

---

## The Mechanical Property

### Two-Pointer Convergence
```
INVARIANT: leftPointer < rightPointer at all times.
           leftPointer moves RIGHT when left side is limiting factor.
           rightPointer moves LEFT when right side is limiting factor.

SELECTION RULE:
  Move the pointer on the WEAKER side (the side with smaller value).
  The STRONGER side stays fixed — it could still be the boundary for inner positions.
  The WEAKER side is already "used up" as a boundary for this configuration.
```

### Stack-Based String Processing
```
STACK INVARIANT (for character-level processing):
  stack.top = the LAST VALID character written so far.
  On new character c:
    If c == backspace: pop the last character (if stack not empty).
    Else: push c.
  After processing: stack contents (bottom to top) = final string.
```

### Monotonic Stack + Greedy Invariant (Remove Duplicate Letters)
```
STACK INVARIANT: Characters in stack from bottom to top are in STRICTLY INCREASING
                 lexicographic order (for smallest result).

GREEDY CHOICE: Pop the top character if:
  1. top > current character (lex order would be broken)
  2. AND top still has remaining occurrences later in the string
     (remainingCount[top] > 0 — we can still include top later)

If these two conditions hold, pop; else keep top and push current.
```

---

## Pointer / Index Invariants

### Two-Pointer Rain Water (O(1) space)
```
leftMaxHeight: max height seen from left up to and including leftPointer.
rightMaxHeight: max height seen from right up to and including rightPointer.

TRAPPED WATER AT A POSITION:
  min(leftMaxHeight, rightMaxHeight) - height[position]

WHY MIN?
  Water level is limited by the SHORTER of the two bounding walls.
  The shorter wall determines the overflow point.

POINTER MOVEMENT RULE:
  If height[leftPointer] <= height[rightPointer]:
    The LEFT side is limiting. Process leftPointer.
    leftMaxHeight = max(leftMaxHeight, height[leftPointer])
    water += leftMaxHeight - height[leftPointer]
    leftPointer++
  Else: symmetric for rightPointer.
```

---

## Space-Time Tradeoffs

| Problem | Stack Approach | Two-Pointer Approach |
|---|---|---|
| Trapping Rain Water | O(N) time, O(N) space | O(N) time, O(1) space |
| Backspace String Compare | O(N) time, O(N) space | O(N) time, O(1) space (reverse scan) |
| Remove Duplicate Letters | O(N) time, O(26) space | Not directly applicable |

**O(1) space two-pointer beats stack for rain water** — prefer when space is critical.
**Stack is cleaner code** and easier to reason about correctness in interviews.

---

## Complexity Summary

| Pattern | Time | Space |
|---|---|---|
| Backspace compare (stack) | O(N) | O(N) |
| Backspace compare (two-pointer) | O(N) | O(1) |
| Trapping Rain Water (two-pointer) | O(N) | O(1) |
| Trapping Rain Water (mono stack) | O(N) | O(N) |
| Remove Duplicate Letters (mono stack) | O(N) | O(26) |
