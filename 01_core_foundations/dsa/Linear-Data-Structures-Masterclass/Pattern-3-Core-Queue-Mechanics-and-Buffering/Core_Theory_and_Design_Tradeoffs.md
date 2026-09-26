# Pattern 3 — Core Queue Mechanics and Buffering

## The Concept: Real-World Domain Analogy
**Traffic intersection queue**: Cars arrive at a red light and form a line. When the
light turns green, cars leave in the ORDER they arrived — the first car in is the first
car out. No car can "skip" the queue. This is FIFO — First In, First Out.

**Network packet buffer**: A router receives packets and queues them for forwarding.
Packets are transmitted in arrival order to preserve sequencing. Dropping the front
of the queue = dequeuing the oldest packet.

**Print spooler**: Jobs sent to a printer are queued. The job submitted first prints
first, regardless of job size. FIFO ensures fairness.

---

## The Mechanical Property (FIFO Invariant)

```
QUEUE INVARIANT:
  The element enqueued EARLIEST is always the one dequeued first.
  Front = OLDEST element (next to leave).
  Rear  = NEWEST element (just arrived).

  enqueue(x) / offer(x): add x to the REAR.   O(1)
  dequeue() / poll():     remove from FRONT.   O(1)
  peek():                 read FRONT, no remove. O(1)
  isEmpty():              check size == 0.      O(1)
```

---

## Pointer / Index Invariants

### Circular Array Queue (`int[] buffer, int front, int rear, int size`)
```
front: index of the NEXT element to dequeue (the OLDEST)
rear:  index where the NEXT element will be enqueued (the slot AFTER the newest)
size:  current number of elements

EMPTY:  size == 0  (front and rear positions are meaningless)
FULL:   size == capacity

ENQUEUE: buffer[rear] = x; rear = (rear + 1) % capacity; size++;
DEQUEUE: int val = buffer[front]; front = (front + 1) % capacity; size--; return val;

WHY CIRCULAR?
  Without wrapping, rear would eventually exceed the array length even if front
  has advanced (wasting space). Modulo arithmetic recycles the vacated front slots.
```

**Invariant:** `front == rear` is ambiguous (could mean EMPTY or FULL). Use `size` counter
or the "wasted slot" trick (`capacity - 1` usable slots, full when `(rear+1)%capacity == front`).

### Dual-Stack Queue (`Stack<Integer> inbox, Stack<Integer> outbox`)
```
inbox  = where new elements are pushed (O(1) each)
outbox = where dequeues come from (O(1) amortized each)

ENQUEUE: inbox.push(x)
DEQUEUE: if outbox is empty, pour ALL of inbox into outbox (reversal = FIFO order)
         return outbox.pop()

AMORTIZED ANALYSIS:
  Each element is pushed to inbox once, poured to outbox once, popped from outbox once.
  Total = 3 operations per element → O(1) amortized per dequeue.
```

---

## Space-Time Tradeoffs

| Structure | Enqueue | Dequeue | Space | Notes |
|---|---|---|---|---|
| `LinkedList<Integer>` | O(1) | O(1) | High (Node allocation) | Pointer chasing, GC pressure |
| `ArrayDeque<Integer>` | O(1) amortized | O(1) | Medium (boxed) | Best general-purpose |
| Circular array `int[]` | O(1) | O(1) | Minimal (primitives) | Fixed size, no GC |
| Dual-stack queue | O(1) amortized | O(1) amortized | O(N) | Interview classic |

**When to use circular `int[]`:** Fixed-size streaming problems, sliding window buffers,
producer-consumer with known max backlog.

---

## BFS and Level-Order Traversal Pattern

```
Queue is the FOUNDATION of BFS:
  1. Enqueue the starting vertex/node.
  2. While queue is not empty:
     a. Dequeue current node.
     b. Process current node.
     c. Enqueue all unvisited neighbors.

BFS LEVEL TRACKING (for shortest path):
  Record queue.size() at the START of each level.
  Process exactly that many nodes before incrementing level counter.
```

---

## Complexity Summary

| Pattern | Time | Space |
|---|---|---|
| Circular buffer enqueue/dequeue | O(1) | O(capacity) |
| Dual-stack queue amortized | O(1) amortized | O(N) |
| BFS shortest path | O(V + E) | O(V) |
| Level-order tree traversal | O(N) | O(W) — W = max width |
