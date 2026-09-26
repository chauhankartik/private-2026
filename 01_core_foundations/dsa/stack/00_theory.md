# Stack — Theory & Pattern Guide
> **Study goal:** Master every stack pattern and monotonic stack technique
> needed for Google/FAANG interviews.

---

## 1. Core Data Structure

```
Stack (LIFO — Last In, First Out):

  push →  [ D ]   ← top
          [ C ]
          [ B ]
          [ A ]   ← bottom

  Operations:
    push(e)        → add to top        O(1)
    pop()          → remove from top   O(1)
    peek() / top() → view top          O(1)
    isEmpty()      → check empty       O(1)
    size()         → element count     O(1)
```

```java
// Java Stack implementations — USE Deque, NOT legacy Stack class
Deque<Integer> stack = new ArrayDeque<>();    // ✓ PREFERRED (fastest)
// Stack<Integer> stack = new Stack<>();      // ✗ AVOID (synchronized, slow)

// Key methods (Deque used as stack)
stack.push(1);       // add to top
stack.pop();         // remove & return top (throws if empty)
stack.peek();        // view top without removing (returns null if empty)
stack.isEmpty();     // check if empty
stack.size();        // number of elements

// IMPORTANT: ArrayDeque.push() adds to the FRONT (offerFirst).
// ArrayDeque.pop() removes from the FRONT (pollFirst).
// This is correct LIFO behavior.
```

---

## 2. Stack Variants

### Standard Stack (LIFO)
```
Use: Expression evaluation, DFS, undo/redo, backtracking
Java: Deque<E> → ArrayDeque (preferred)
```

### Min Stack (O(1) getMin)
```
Track the minimum alongside each element.
Two approaches:
  (a) Two stacks: main stack + min stack (push min alongside, pop alongside)
  (b) Single stack of pairs: push (value, currentMin) together

Use: Stock price tracking, stream statistics
```

```java
// Min Stack using two stacks
Deque<Integer> stack = new ArrayDeque<>();
Deque<Integer> minStack = new ArrayDeque<>();

void push(int x) {
    stack.push(x);
    minStack.push(minStack.isEmpty() ? x : Math.min(x, minStack.peek()));
}
int pop()    { minStack.pop(); return stack.pop(); }
int getMin() { return minStack.peek(); }
```

### Monotonic Stack
```
Maintains elements in sorted order (increasing or decreasing).
Push: remove elements that violate the ordering before inserting.
Use: Next Greater Element, stock span, histogram area
```

---

## 3. Stack Implementations

### Array-Based Stack
```java
class ArrayStack {
    int[] data;
    int top;

    ArrayStack(int capacity) {
        data = new int[capacity];
        top = -1;
    }

    void push(int val) {
        if (top == data.length - 1) resize();
        data[++top] = val;
    }

    int pop() {
        if (top == -1) throw new RuntimeException("Stack empty");
        return data[top--];
    }

    int peek() { return data[top]; }
    boolean isEmpty() { return top == -1; }

    void resize() {
        data = Arrays.copyOf(data, data.length * 2);
    }
}
// Amortized O(1) push due to doubling strategy.
// Total copy cost over n pushes: 1 + 2 + 4 + ... + n = 2n - 1 = O(n).
```

### Linked-List Based Stack
```java
class LinkedStack {
    private static class Node {
        int val;
        Node next;
        Node(int v, Node n) { val = v; next = n; }
    }

    Node top = null;
    int size = 0;

    void push(int val) { top = new Node(val, top); size++; }

    int pop() {
        int val = top.val;
        top = top.next;
        size--;
        return val;
    }

    int peek() { return top.val; }
    boolean isEmpty() { return top == null; }
}
// O(1) push/pop. No capacity limit. Higher memory per element (node overhead).
```

---

## 4. The Five Core Patterns

### Pattern 1: Monotonic Stack (Next Greater / Next Smaller)
```
Maintain a stack in monotonic order.
For Next Greater Element (NGE): use a DECREASING stack.
When a new element is LARGER than the top → pop and record answer.

Template (Next Greater Element):
  Deque<Integer> stack = new ArrayDeque<>();  // stores indices
  int[] result = new int[n];
  Arrays.fill(result, -1);

  for (int i = 0; i < n; i++) {
      while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
          result[stack.pop()] = nums[i];  // nums[i] is the NGE
      }
      stack.push(i);
  }
```
Used for: Next Greater Element, Daily Temperatures, Stock Span,
Largest Rectangle in Histogram, Trapping Rain Water, Sum of Subarray Mins.

**Monotonic Increasing Stack:** stack bottom→top in increasing order.
Pops when a SMALLER element arrives → finds Next Smaller.

**Monotonic Decreasing Stack:** stack bottom→top in decreasing order.
Pops when a LARGER element arrives → finds Next Greater.

### Pattern 2: Expression Evaluation / Parsing
```
Use stack(s) to process mathematical expressions.
For nested structures (brackets, parentheses):
  On '(' → push current state to stack, reset.
  On ')' → pop state, combine with current result.
```
Used for: Valid Parentheses, Basic Calculator I/II/III, Decode String.

### Pattern 3: Simulation / Undo
```
Stack naturally supports "undo" — the last action is undone first.

Template (Collision / Cancel):
  Deque<Integer> stack = new ArrayDeque<>();
  for (int item : items) {
      boolean survived = true;
      while (!stack.isEmpty() && conflict(stack.peek(), item)) {
          if (beats(item, stack.peek())) stack.pop();
          else { survived = false; break; }
      }
      if (survived) stack.push(item);
  }
```
Used for: Asteroid Collision, Backspace String Compare, Remove Duplicates.

### Pattern 4: DFS (Iterative using Stack)
```
Replace recursion with an explicit stack.

Template (Iterative DFS):
  Deque<Node> stack = new ArrayDeque<>();
  stack.push(root);
  while (!stack.isEmpty()) {
      Node curr = stack.pop();
      process(curr);
      for (Node neighbor : curr.neighbors) {
          if (!visited.contains(neighbor)) {
              visited.add(neighbor);
              stack.push(neighbor);
          }
      }
  }
```
Used for: Tree traversals, graph DFS, flood fill, iterative backtracking.

### Pattern 5: Stack as History (State Tracking)
```
Each push records a snapshot of state at that point.
Peek gives current state. Pop reverts to previous state.
```
Used for: Min Stack, Max Stack, Browser History, Undo/Redo, Baseball Game.

---

## 5. Stack vs Queue Decision Guide

| Criteria | Stack (LIFO) | Queue (FIFO) |
|---|---|---|
| DFS traversal | ✓ Natural fit | ✗ |
| BFS traversal | ✗ | ✓ Natural fit |
| Expression evaluation | ✓ | ✗ |
| Undo/Redo | ✓ LIFO undo | ✗ |
| Parentheses matching | ✓ Push open, pop close | ✗ |
| Sliding window max/min | ✗ | ✓ Monotonic deque |
| Backtracking | ✓ Implicit or explicit | ✗ |

**Rule of thumb:**
- Need **matching/nesting** → Stack
- Need **next greater/smaller** → Monotonic Stack
- Need **expression evaluation** → Stack
- Need **DFS / backtracking** → Stack
- Need **shortest path / level-order** → Queue (BFS)

---

## 6. Common Bugs

```java
// Bug 1: Popping from empty stack
stack.pop();  // throws NoSuchElementException on ArrayDeque
// FIX: always check isEmpty() before pop/peek

// Bug 2: Using Stack<> instead of Deque<>
Stack<Integer> s = new Stack<>();  // legacy, synchronized, slow
// FIX: Deque<Integer> s = new ArrayDeque<>();

// Bug 3: Monotonic stack — storing values instead of indices
// RULE: almost always store INDICES (you can look up the value)

// Bug 4: Not handling remaining stack elements after loop
// Set their result to -1 (or handle appropriately).

// Bug 5: Parentheses matching — wrong type check
// On ')' check for '(' — also handle ']'→'[', '}'→'{'

// Bug 6: Integer overflow in calculator problems
// Use long for intermediate calculations.

// Bug 7: Monotonic stack — strict vs non-strict comparison
// < vs <= matters! Using <= can skip duplicates.
```

---

## 7. Interview Frequency

| # | Problem | Pattern | Freq |
|---|---|---|---|
| 1 | Valid Parentheses | Matching | ⭐⭐⭐⭐⭐ |
| 2 | Min Stack | Stack as History | ⭐⭐⭐⭐⭐ |
| 3 | Daily Temperatures | Monotonic Stack | ⭐⭐⭐⭐⭐ |
| 4 | Largest Rectangle in Histogram | Monotonic Stack | ⭐⭐⭐⭐⭐ |
| 5 | Evaluate Reverse Polish Notation | Expression Eval | ⭐⭐⭐⭐ |
| 6 | Trapping Rain Water | Monotonic Stack / TP | ⭐⭐⭐⭐ |
| 7 | Basic Calculator II | Expression Eval | ⭐⭐⭐⭐ |
| 8 | Decode String | Nested Parsing | ⭐⭐⭐⭐ |
| 9 | Asteroid Collision | Simulation | ⭐⭐⭐⭐ |
| 10 | Next Greater Element I/II | Monotonic Stack | ⭐⭐⭐ |
| 11 | Remove K Digits | Monotonic Stack | ⭐⭐⭐ |
| 12 | Maximum Frequency Stack | Design | ⭐⭐⭐ |

---

*Next:*
- `01_easy.java` — Valid Parentheses, Min Stack, Queue using Stacks, etc.
- `02_medium.java` — Daily Temperatures, Decode String, Asteroid Collision, etc.
- `03_hard.java` — Largest Rectangle in Histogram, Basic Calculator, etc.
- `04_google_level.java` — FreqStack, Visible People, Remove Duplicate Letters, etc.
