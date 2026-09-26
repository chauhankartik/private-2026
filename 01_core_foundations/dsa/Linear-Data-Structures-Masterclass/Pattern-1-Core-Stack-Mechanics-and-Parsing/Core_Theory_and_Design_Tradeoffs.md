# Pattern 1 — Core Stack Mechanics and Parsing

## The Concept: Real-World Domain Analogy
**Text editor undo/redo history**: Every character you type is pushed onto an undo stack.
When you press Ctrl+Z, the most recent action is popped and reversed. You cannot undo
action #3 without first undoing actions #5, #4. This is LIFO — Last In, First Out.

**Execution call stack in JVM**: When method A calls B which calls C, the JVM pushes
stack frames. C finishes first and pops, then B, then A. Recursion **is** a stack.

**Balanced bracket parsing in compilers**: A compiler tokenizes `({[]})` left-to-right.
Each opening bracket is pushed. Each closing bracket must match the top of the stack
(its corresponding opener). A mismatch → syntax error.

---

## The Mechanical Property (LIFO Invariant)

```
STACK INVARIANT:
  The element pushed MOST RECENTLY is always the one accessible for pop/peek.
  No random access. Only the TOP is ever observable or removable.

  push(x): place x on top.    O(1)
  pop():   remove top.        O(1)
  peek():  read top, no remove. O(1)
  isEmpty(): check size == 0.  O(1)
```

**Why LIFO is powerful for parsing:**
Opening brackets must be matched by closing brackets in REVERSE order. The stack
perfectly mirrors this reversal — the most recent unmatched opener is always at top.

---

## Pointer / Index Invariants

### Array-backed stack (`int[] stack, int top = -1`)
```
top == -1          → stack is EMPTY (no valid index to read)
top == capacity-1  → stack is FULL (bounded array, check before push)
stack[top]         → current top element (always valid when top >= 0)
stack[top - k]     → k elements below top (random access, but don't abuse it)

PUSH: stack[++top] = newElement;   // pre-increment top, then write
POP:  return stack[top--];         // read top, then post-decrement
PEEK: return stack[top];           // read without modifying top
```

**Storing INDICES instead of VALUES:**
For problems like "next greater element" or "min stack," we often push the ARRAY
INDEX, not the value. Why? Because:
- Distance between positions = `currentIndex - stack[top]`
- We can always recover the value via `inputArray[indexOnStack]`
- Preserves positional information lost if we stored raw values

### Deque-backed stack (`Deque<Integer> stack = new ArrayDeque<>()`)
```
stack.push(x)   ≡ addFirst(x)     → adds to HEAD
stack.pop()     ≡ removeFirst()   → removes from HEAD
stack.peek()    ≡ peekFirst()     → reads HEAD
```
ArrayDeque uses a resizable circular array internally. No node allocation per element.

---

## Space-Time Tradeoffs: Array vs. Node-Linked

| Property | `int[] + int top` | `ArrayDeque<Integer>` | `LinkedList<Integer>` |
|---|---|---|---|
| Memory per element | 4 bytes (primitive) | ~16 bytes (Integer boxed) | ~48 bytes (node + boxed) |
| Cache locality | Excellent (contiguous) | Good (circular array) | Poor (random pointer chasing) |
| Resizing | Manual or fixed | Automatic doubling | Not applicable (linked) |
| GC pressure | None | Low (rare resize) | High (Node alloc per push) |
| Overflow risk | Yes (bounded array) | No | No |
| Best use | Performance-critical, known N | General purpose | Avoid for stack patterns |

**Key insight**: For competitive programming and interview settings with N ≤ 10^5,
a `int[N] + int topPointer` stack is 4× more memory-efficient than a boxed `Deque<Integer>`
and avoids all boxing/unboxing overhead. Use it when stack depth is bounded by input size.

**When to use `Deque` over array:**
- Unknown maximum depth
- You need to store complex objects (not just ints)
- Code clarity matters more than micro-optimization

---

## Complexity Summary

| Operation | Array Stack | ArrayDeque Stack |
|---|---|---|
| push | O(1) | O(1) amortized |
| pop | O(1) | O(1) |
| peek | O(1) | O(1) |
| isEmpty | O(1) | O(1) |
| Space | O(N) primitives | O(N) boxed |

---

## Key Parsing Patterns That Use Stacks

```
1. BRACKET MATCHING: push openers; on closer, check top matches.
2. EXPRESSION EVALUATION: two stacks — operand stack + operator stack.
3. INFIX → POSTFIX (Shunting Yard): operators pushed based on precedence.
4. RECURSIVE DEPTH SIMULATION: convert recursive DFS to iterative with explicit stack.
5. HISTORY TRACKING (Undo): push state changes, pop on undo.
6. MIN/MAX TRACKING: auxiliary stack mirrors main stack, tracks running min/max.
```
