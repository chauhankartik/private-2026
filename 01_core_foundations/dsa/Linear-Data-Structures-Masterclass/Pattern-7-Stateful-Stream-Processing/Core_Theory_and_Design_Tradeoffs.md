# Pattern 7 — Stateful Stream Processing

## The Concept: Real-World Domain Analogy
**Algorithmic trading technical indicators**: A stock price feed arrives tick by tick.
A moving average is computed over the last K ticks — the "window" slides forward with
each new price. A circular buffer holds exactly K prices; the oldest is overwritten.

**Online stock span problem**: A financial analyst wants to know, for each day's price,
how many CONSECUTIVE previous days had prices ≤ today. This is an online problem —
you compute each answer as data streams in, not in batch.

**Log processing pipeline**: A server emits log entries continuously. You need real-time
aggregates (count, sum, max) over a rolling window without storing all historical data.

---

## The Mechanical Property

### Circular Buffer for Moving Average
```
CIRCULAR BUFFER INVARIANT:
  capacity: fixed window size K
  windowSum: running sum of the current K elements
  writeIndex = (writeIndex + 1) % capacity   // next slot to write (wraps around)
  count: number of elements inserted so far (saturates at capacity)

ON NEW ELEMENT x:
  If buffer is full: windowSum -= buffer[writeIndex]   // subtract the element being overwritten
  buffer[writeIndex] = x
  windowSum += x
  writeIndex = (writeIndex + 1) % capacity
  count = min(count + 1, capacity)
  average = windowSum / count

SPACE: O(K) — exactly K slots, reused cyclically.
```

### Stock Span (Monotonic Stack — Online)
```
STACK INVARIANT: pairs of (price, accumulatedSpan) — strictly decreasing by price.

ON NEW DAY with price p:
  currentSpan = 1   // at minimum, today spans itself
  While stack not empty AND stack.top.price <= p:
    currentSpan += stack.pop().span  // absorb the span of each dominated day
  stack.push({price: p, span: currentSpan})
  return currentSpan

WHY STORE ACCUMULATED SPANS?
  Instead of re-counting each time, we remember how many days we "leapfrogged."
  When day 5 (price 90) absorbs day 4 (price 80, span 3), we know span 3
  already accounts for days 1-3 below day 4. We add it once — O(1) per pop.
```

---

## Pointer / Index Invariants for Online Algorithms

### Online = process each element exactly once as it arrives
```
Offline: can read all elements first, then answer queries → enables preprocessing.
Online:  must answer IMMEDIATELY after each element arrives → no lookahead.

ONLINE ALGORITHM PROPERTIES:
  1. Can use data structures that grow (heap, stack, circular buffer).
  2. Cannot use future values for current computation.
  3. Space: ideally O(K) or O(log N), NOT O(N total elements seen).

AMORTIZED ANALYSIS:
  Stock span: each price is pushed once and popped at most once.
  Total pops across all N calls ≤ N → O(N) total, O(1) amortized per call.
```

### Max Chunks to Sort (chunk boundary detection)
```
INVARIANT: The k-th chunk covers positions [start, end] where:
  max(arr[start..end]) ≤ min(arr[end+1..N-1])
  Equivalently: max(arr[0..end]) == end  (for arrays containing 0..N-1 exactly once)

RUNNING MAX INVARIANT:
  If runningMax at position i equals i → this position can be a chunk boundary.
  Every element ≤ i has appeared in [0..i], so sorting [0..i] leaves [i+1..N-1] untouched.
```

---

## Space-Time Tradeoffs

| Structure | Time per Update | Space | Suitable For |
|---|---|---|---|
| Circular buffer | O(1) | O(K) | Moving average, fixed-window sum |
| Monotonic stack (stateful) | O(1) amortized | O(N) | Stock span, online NGE |
| Min-max heap pair | O(log N) | O(N) | Streaming median |
| Segment tree | O(log N) | O(N) | Range queries, arbitrary windows |

**Circular buffer key advantage**: Constant O(1) time AND O(K) space — the buffer
never grows beyond K elements. Ideal for embedded/real-time systems with memory limits.

---

## Complexity Summary

| Pattern | Time per Query | Total Time | Space |
|---|---|---|---|
| Moving average (circular buffer) | O(1) | O(N) | O(K) |
| Online stock span (stateful stack) | O(1) amortized | O(N) | O(N) |
| Max chunks to sort | O(1) per position | O(N) | O(1) |
