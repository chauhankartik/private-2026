# Pattern 5 — Priority Queues and Heap Mechanics

## The Concept: Real-World Domain Analogy
**Hospital emergency triage**: Patients arrive continuously. Each has a severity score.
The doctor always treats the MOST CRITICAL patient next — not the one who waited longest.
New critical patients "jump ahead." This is a min/max priority queue, not a FIFO queue.

**Task scheduler**: An operating system has N pending tasks with priorities. The scheduler
always runs the highest-priority ready task. When a task completes, the next-highest
is selected. A heap gives O(log N) selection and insertion — far better than O(N) scan.

**Streaming median**: A stream of numbers arrives. After each number, you need the
median. A pair of heaps (max-heap for lower half, min-heap for upper half) maintains
this in O(log N) per insertion and O(1) per query.

---

## The Mechanical Property (Heap Invariant)

```
MIN-HEAP INVARIANT:
  For every node i: heap[i] ≤ heap[left(i)] and heap[i] ≤ heap[right(i)]
  → The ROOT is always the MINIMUM element.

MAX-HEAP INVARIANT:
  For every node i: heap[i] ≥ heap[left(i)] and heap[i] ≥ heap[right(i)]
  → The ROOT is always the MAXIMUM element.

ARRAY INDEX MAPPING (1-indexed heap):
  Parent of node i  = i / 2
  Left child  of i  = 2 * i
  Right child of i  = 2 * i + 1

ARRAY INDEX MAPPING (0-indexed heap, Java's PriorityQueue):
  Parent of node i  = (i - 1) / 2
  Left child  of i  = 2 * i + 1
  Right child of i  = 2 * i + 2

OPERATIONS:
  offer(x) [HEAPIFY UP]:   Add x to bottom. Bubble up until parent ≤ x.   O(log N)
  poll()   [HEAPIFY DOWN]: Remove root. Move last element to root. Bubble down.  O(log N)
  peek():  Return root without removal.  O(1)
  size():  Return current count.         O(1)
```

---

## Pointer / Index Invariants for the Two-Heap Median Pattern

```
maxHeap (lower half): max-heap of all numbers ≤ median
minHeap (upper half): min-heap of all numbers ≥ median

BALANCE INVARIANT:
  |maxHeap.size() - minHeap.size()| ≤ 1
  maxHeap.peek() ≤ minHeap.peek()  (top of lower ≤ top of upper)

MEDIAN DERIVATION:
  If sizes equal: median = (maxHeap.peek() + minHeap.peek()) / 2.0
  If maxHeap larger: median = maxHeap.peek()
  If minHeap larger: median = minHeap.peek()

INSERTION INVARIANT:
  Always offer to maxHeap first (to check if x belongs to lower half).
  Then if maxHeap.peek() > minHeap.peek(): move maxHeap root to minHeap.
  Then rebalance sizes if needed.
```

---

## Space-Time Tradeoffs

| Operation | Binary Heap | Sorted Array | Unsorted Array | BST |
|---|---|---|---|---|
| Insert | O(log N) | O(N) | O(1) | O(log N) avg |
| Find min/max | O(1) | O(1) | O(N) | O(log N) |
| Delete min/max | O(log N) | O(N) | O(N) | O(log N) |
| Space | O(N) | O(N) | O(N) | O(N) + pointers |

**Java's `PriorityQueue<Integer>` is a MIN-heap by default.**
For MAX-heap: `new PriorityQueue<>(Collections.reverseOrder())`
or: `new PriorityQueue<>((a, b) -> b - a)` (careful with integer overflow — use `Integer.compare(b, a)`)

**Custom comparator for complex objects:**
```java
// K closest points by distance:
PriorityQueue<int[]> maxHeap = new PriorityQueue<>(
    (a, b) -> Integer.compare(b[0]*b[0]+b[1]*b[1], a[0]*a[0]+a[1]*a[1])
);
```

---

## Complexity Summary

| Pattern | Time | Space |
|---|---|---|
| Kth largest (heap of size K) | O(N log K) | O(K) |
| Top K frequent elements | O(N log K) | O(N) |
| Merge K sorted lists | O(N log K) | O(K) |
| Find median from stream | O(log N) per insert, O(1) query | O(N) |
| K closest points | O(N log K) | O(K) |
