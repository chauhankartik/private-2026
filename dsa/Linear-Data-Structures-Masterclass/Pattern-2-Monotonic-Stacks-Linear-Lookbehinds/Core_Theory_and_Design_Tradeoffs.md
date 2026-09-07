# Pattern 2 — Monotonic Stacks and Linear Lookbehinds

## The Concept: Real-World Domain Analogy
**Mountain visibility problem**: You are standing at position i looking left. You want
to find the NEAREST position j < i where the mountain (bar) is taller than you.
A naïve scan is O(N²). A monotonic stack tracks the "visible skyline" — only the
bars you can still see — in O(N) total.

**Stock market "nearest day with higher price"**: For each trading day, find the last
previous day where the stock was more expensive. A monotonic decreasing stack maintains
a running record of "relevant" past highs in a single left-to-right pass.

---

## The Mechanical Property (Monotonic Invariant)

```
MONOTONIC INCREASING STACK: elements from bottom to top are in strictly increasing order.
MONOTONIC DECREASING STACK: elements from bottom to top are in strictly decreasing order.

PUSH PROTOCOL (for a monotonic DECREASING stack):
  While stack is not empty AND stack[top] <= newElement:
    POP the top (it's now "dominated" — can never be the answer for any future position)
  PUSH newElement

KEY INVARIANT:
  At the moment a value X is POPPED from the stack during insertion of value Y:
    Y is the NEXT GREATER ELEMENT for X (if decreasing stack)
    Y is the NEXT SMALLER ELEMENT for X (if increasing stack)
  This gives us O(1) per element: each element is pushed once and popped once.
  Total work = O(N).
```

---

## Pointer / Index Invariants

### WHY STORE INDICES INSTEAD OF VALUES

```java
// WRONG approach — storing raw values:
monoStack.push(height[i]);
// When we pop, we know the HEIGHT but NOT WHERE it was.
// We cannot compute the WIDTH of a histogram bar without position.

// CORRECT approach — storing indices:
monoStack.push(i);   // store index
// Access value anytime: height[monoStack.peek()]
// Access distance: currentIndex - monoStack.peek() - 1
```

**Invariant maintained when storing indices:**
```
monoStack.peek()           = index of the most recent "relevant" element
height[monoStack.peek()]   = value at that relevant element
currentIndex - monoStack.peek() - 1 = span/distance (for histogram width)
```

### SENTINEL TRICK
Push index `-1` (with value 0 or -∞) before processing to eliminate empty-stack checks:
```java
monoStack.push(-1);   // sentinel: "the bar before position 0 has height 0"
// Now stack is never empty during right boundary calculation.
```

---

## Space-Time Tradeoffs

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute force nested loop | O(N²) | O(1) | No preprocessing |
| Monotonic stack | O(N) | O(N) | Each element pushed/popped once |
| Segment tree (range max) | O(N log N) | O(N) | Overkill for single-pass NGE |

**Array-backed mono stack advantages:**
- Avoid boxing: `int[] monoStack = new int[N]; int stackTop = -1;`
- Push: `monoStack[++stackTop] = index;`
- Pop: `int poppedIndex = monoStack[stackTop--];`
- Zero GC overhead — critical in tight loops over N = 10^5

---

## Core Monotonic Stack Templates

### Template 1: Next Greater Element (NGE)
```
For each i from 0 to N-1:
  While stack not empty AND arr[stack.top] < arr[i]:
    result[stack.pop()] = arr[i]   // current i is NGE for the popped index
  stack.push(i)
While stack not empty:
  result[stack.pop()] = -1  // no NGE found
```

### Template 2: Previous Greater Element (PGE)
```
For each i from 0 to N-1:
  While stack not empty AND arr[stack.top] <= arr[i]:
    stack.pop()   // these can never be PGE for future elements
  result[i] = stack.empty ? -1 : arr[stack.top]
  stack.push(i)
```

### Template 3: Histogram Max Rectangle
```
Push a sentinel 0-height bar at both ends.
For each bar i:
  While height[stack.top] > height[i]:
    height_bar = height[stack.pop()]
    width = i - stack.top - 1   // from right boundary i to left boundary stack.top
    maxArea = max(maxArea, height_bar × width)
  stack.push(i)
```

---

## Complexity Summary

| Pattern | Time | Space |
|---|---|---|
| Next/Prev Greater Element | O(N) | O(N) |
| Daily Temperatures | O(N) | O(N) |
| Largest Rectangle Histogram | O(N) | O(N) |
| Trapping Rain Water (stack) | O(N) | O(N) |
