# Pattern 4 — Monotonic Deques and Sliding Windows

## The Concept: Real-World Domain Analogy
**Weather station sliding window**: A weather station tracks the maximum temperature
over the last K days. As each new day arrives, the oldest day exits the window and
the new reading enters. A naïve approach scans K elements per day: O(N × K).
A monotonic deque maintains the candidates for maximum in O(N) total.

**Video stream frame buffer**: A video codec analyzes frames in a rolling window
of size K to detect scene changes. The max/min pixel intensity must be tracked
as the window slides. The deque discards frames that can never be the max.

---

## The Mechanical Property (Deque Monotonic Invariant)

```
MONOTONIC DECREASING DEQUE (for sliding window MAXIMUM):
  Front (head): the CURRENT WINDOW MAXIMUM (largest element's index).
  Back  (tail): the most recently added index.

  INVARIANT: Indices in deque are in INCREASING order (left to right).
             Values at those indices are in DECREASING order (left to right).
             → Front always holds the MAX of the current window.

TWO MAINTENANCE RULES:
  1. REMOVE STALE (window expiry) from FRONT:
     While deque not empty AND deque.front <= (currentIndex - windowSize):
       deque.removeFirst()   // front is outside the window — evict it

  2. REMOVE DOMINATED (monotonic cleanup) from BACK:
     While deque not empty AND arr[deque.back] <= arr[currentIndex]:
       deque.removeLast()    // back element is ≤ new element → can never be max
                             // (new element arrives later AND is larger)
  Then: deque.addLast(currentIndex)
  Then: if currentIndex >= windowSize - 1: record arr[deque.front] as window max.
```

---

## Pointer / Index Invariants

### WHY STORE INDICES (not values) IN THE DEQUE

```
PROBLEM: You need to know if the front element has "expired" (slid out of window).
         If you store VALUES, you cannot tell which POSITION they came from.

EXAMPLE: window=[3,1,3], windowSize=3.
         Two 3s — which one is "current"? You need the index to decide expiry.

INVARIANT:
  deque.peekFirst() = INDEX of the current window maximum.
  arr[deque.peekFirst()] = the actual maximum value.
  deque.peekFirst() >= currentIndex - windowSize + 1 (always in window).
  deque.peekLast() = INDEX of the most recently added element.
```

### Array-backed deque for extreme performance:
```java
int[] deque = new int[N];  // circular or linear (linear ok for single-pass problems)
int dequeHead = 0;         // points to front element
int dequeTail = -1;        // points to last added element

// Add to back:   deque[++dequeTail] = index;
// Remove front:  dequeHead++;
// Remove back:   dequeTail--;
// Front value:   deque[dequeHead]
// Back value:    deque[dequeTail]
// Is empty:      dequeHead > dequeTail
```
This avoids all object allocation — the deque is a plain `int[]` window on another array.

---

## Space-Time Tradeoffs

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute-force: scan each window | O(N × K) | O(1) | Acceptable only for K ≤ 50 |
| Segment tree / sparse table | O(N log N) | O(N log N) | Preprocessing for range max |
| **Monotonic deque** | **O(N)** | **O(K)** | Each element in/out once |
| Priority queue (max-heap) | O(N log K) | O(K) | Handles lazy deletion |

**Deque vs. Priority Queue for sliding window:**
- Deque: O(N) time, O(K) space — strictly better.
- Priority Queue: O(N log K) — simpler code when K changes or elements repeat with tracked positions.
- Use deque when you need linear time; use priority queue when K varies per query.

---

## Core Templates

### Template 1: Sliding Window Maximum
```
deque = empty (stores indices)
for i from 0 to N-1:
  1. Evict expired: while !deque.empty && deque.front < i - K + 1: deque.removeFirst()
  2. Maintain mono: while !deque.empty && arr[deque.back] <= arr[i]: deque.removeLast()
  3. Add current:   deque.addLast(i)
  4. Record result: if i >= K-1: result[i-K+1] = arr[deque.front]
```

### Template 2: Subarray Sum Within Bounds (using prefix sums + monotonic deque)
```
Build prefix sum array.
Use monotonic increasing deque on prefix sums.
For each i: record/evict/compare.
```

---

## Complexity Summary

| Pattern | Time | Space |
|---|---|---|
| Sliding window maximum | O(N) | O(K) |
| Longest subarray bounded by limit | O(N) | O(N) |
| Constrained subsequence sum | O(N) | O(N) |
