# Queue & Deque — Theory & Pattern Guide
> **Study goal:** Master every queue variant, BFS pattern, and monotonic deque
> technique needed for Google/FAANG interviews.

---

## 1. Core Data Structure

```
Queue (FIFO — First In, First Out):

  enqueue →  [ A | B | C | D ]  → dequeue
             back            front

  Operations:
    offer(e)  / enqueue → add to back    O(1)
    poll()    / dequeue → remove front   O(1)
    peek()              → view front     O(1)
    isEmpty()           → check empty    O(1)
    size()              → element count  O(1)
```

```java
// Java Queue implementations
Queue<Integer> q = new LinkedList<>();     // most common in interviews
Queue<Integer> q = new ArrayDeque<>();     // faster (no node allocation)

// Key methods
q.offer(1);      // add to back (returns false if capacity-limited, won't throw)
q.add(1);        // add to back (throws exception if capacity-limited)
q.poll();        // remove & return front (returns null if empty)
q.remove();      // remove & return front (throws if empty)
q.peek();        // view front without removing (returns null if empty)
```

---

## 2. Queue Variants

### Standard Queue (FIFO)
```
Use: BFS, task scheduling, buffering
Java: Queue<E> → LinkedList or ArrayDeque
```

### Deque (Double-Ended Queue)
```
Insert/remove from BOTH ends in O(1).

  offerFirst →  [ A | B | C | D ]  ← offerLast
  pollFirst  →  [ A | B | C | D ]  → pollLast

Use: Sliding window max/min, palindrome check, work stealing
Java: Deque<E> → ArrayDeque (preferred) or LinkedList
```

```java
Deque<Integer> dq = new ArrayDeque<>();
dq.offerFirst(1);   dq.offerLast(2);
dq.pollFirst();     dq.pollLast();
dq.peekFirst();     dq.peekLast();
```

### Priority Queue (Min/Max Heap)
```
Remove the element with highest/lowest priority in O(log n).
Insert in O(log n). Peek in O(1).

Use: Dijkstra's, k-th largest, merge k sorted, task scheduling with priority
Java: PriorityQueue<E> (min-heap by default)
```

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();         // natural order
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]); // custom
```

### Circular Queue
```
Fixed-size queue using array with wrap-around.
front and rear pointers move circularly: (index + 1) % capacity.

Use: Bounded buffer, OS scheduling, streaming data
```

---

## 3. Queue Implementations

### Array-Based Circular Queue
```java
class CircularQueue {
    int[] data;
    int front, rear, size, capacity;

    CircularQueue(int k) {
        data = new int[k];
        capacity = k;
        front = 0; rear = -1; size = 0;
    }

    boolean enqueue(int val) {
        if (size == capacity) return false;
        rear = (rear + 1) % capacity;
        data[rear] = val;
        size++;
        return true;
    }

    int dequeue() {
        if (size == 0) return -1;
        int val = data[front];
        front = (front + 1) % capacity;
        size--;
        return val;
    }
}
```

### Stack-Based Queue (Two Stacks)
```java
// Amortized O(1) per operation
class MyQueue {
    Stack<Integer> inStack = new Stack<>();    // for push
    Stack<Integer> outStack = new Stack<>();   // for pop/peek

    void push(int x) { inStack.push(x); }

    int pop() {
        if (outStack.isEmpty())
            while (!inStack.isEmpty()) outStack.push(inStack.pop());
        return outStack.pop();
    }

    int peek() {
        if (outStack.isEmpty())
            while (!inStack.isEmpty()) outStack.push(inStack.pop());
        return outStack.peek();
    }
}
// WHY AMORTIZED O(1): each element is moved from inStack to outStack
// at most ONCE. Over n operations, total transfers = n → O(1) amortized.
```

---

## 4. The Five Core Patterns

### Pattern 1: BFS (Breadth-First Search)
```
Queue is the ENGINE of BFS.
Process nodes level by level. First reach = shortest path (unweighted).

Template:
  Queue<Node> q = new LinkedList<>();
  q.offer(start);
  visited.add(start);
  while (!q.isEmpty()) {
      Node curr = q.poll();
      for (Node neighbor : curr.neighbors) {
          if (!visited.contains(neighbor)) {
              visited.add(neighbor);
              q.offer(neighbor);
          }
      }
  }
```
Used for: shortest path (unweighted), level-order traversal, multi-source BFS,
rotten oranges, word ladder, 01-BFS.

### Pattern 2: Level-Order Processing
```
Process all nodes at the SAME level together.
Snapshot q.size() before the inner loop.

  while (!q.isEmpty()) {
      int size = q.size();          // ← snapshot
      for (int i = 0; i < size; i++) {
          Node node = q.poll();
          // process node
          // offer children
      }
      // end of one level
  }
```
Used for: tree level-order, right side view, zigzag, min depth, BFS distance.

### Pattern 3: Monotonic Deque (Sliding Window Max/Min)
```
Maintain a deque where elements are in monotonic order.
Remove dominated elements from the back before inserting.
Front of deque = current window's max (or min).

  Deque<Integer> dq = new ArrayDeque<>();  // stores indices
  for (int i = 0; i < n; i++) {
      while (!dq.isEmpty() && dq.peekFirst() < i - k + 1)
          dq.pollFirst();                   // remove expired
      while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i])
          dq.pollLast();                    // remove dominated
      dq.offerLast(i);
      if (i >= k - 1) result = nums[dq.peekFirst()];
  }
```
Used for: sliding window maximum, jump game VI, constrained subsequence sum,
shortest subarray with sum ≥ k (with prefix sums).

### Pattern 4: Priority Queue (Greedy Selection)
```
Always process the "best" element next.
PQ gives O(log n) insert and O(log n) extract-min/max.

Used for: Dijkstra's, merge k sorted lists, k-th largest element,
task scheduler, meeting rooms, median stream.
```

### Pattern 5: Queue Simulation
```
Simulate real-world queue processes: task scheduling, round-robin,
recent counter, data stream processing.

Used for: design hit counter, moving average, task scheduler, Dota2 senate.
```

---

## 5. BFS vs DFS Decision Guide

| Criteria | BFS (Queue) | DFS (Stack/Recursion) |
|---|---|---|
| Shortest path (unweighted) | ✓ Guaranteed | ✗ Not guaranteed |
| Shortest path (weighted) | Dijkstra (PQ) | ✗ |
| Memory | O(width) — can be huge | O(height/depth) — usually smaller |
| Level-by-level processing | ✓ Natural | ✗ Awkward |
| Cycle detection (directed) | Topological sort (Kahn's) | DFS coloring |
| Connected components | Both work | Both work |
| Topological sort | Kahn's (BFS) | DFS + reverse postorder |

**Rule of thumb:**
- Need **shortest path** or **level processing** → BFS
- Need to **explore all paths** or **backtrack** → DFS
- Need **topological sort** → either (Kahn's BFS or DFS)

---

## 6. Common Bugs

```java
// Bug 1: Not checking isEmpty() before poll/peek
q.poll();  // returns null if empty — NPE if you unbox Integer

// Bug 2: Not marking visited BEFORE offering to queue (causes duplicates)
// WRONG:
q.offer(neighbor);
visited.add(neighbor);   // another thread/iteration might offer the same node
// CORRECT:
visited.add(neighbor);   // mark visited FIRST
q.offer(neighbor);

// Bug 3: Forgetting to snapshot q.size() for level-order
// WRONG: for (int i = 0; i < q.size(); i++) — size changes during loop!
// CORRECT: int size = q.size(); for (int i = 0; i < size; i++)

// Bug 4: Using PriorityQueue and assuming it's sorted
// PQ only guarantees the HEAD is min/max. Internal order is NOT sorted.
// To get sorted order, poll() all elements.

// Bug 5: Modifying priority after insertion in PQ
// Java PQ doesn't re-heapify on update. Remove + re-insert instead.
// Or use a "lazy deletion" approach (mark as stale, skip when polled).

// Bug 6: Using LinkedList Deque when ArrayDeque is better
// ArrayDeque: no node allocation, cache-friendly, faster
// LinkedList: only needed if you require null elements or list operations
```

---

## 7. Interview Frequency

| # | Problem | Pattern | Freq |
|---|---|---|---|
| 1 | BFS Tree Level Order | Level-Order BFS | ⭐⭐⭐⭐⭐ |
| 2 | Implement Queue using Stacks | Design | ⭐⭐⭐⭐⭐ |
| 3 | Sliding Window Maximum | Monotonic Deque | ⭐⭐⭐⭐⭐ |
| 4 | Rotten Oranges | Multi-source BFS | ⭐⭐⭐⭐⭐ |
| 5 | Number of Islands (BFS) | Grid BFS | ⭐⭐⭐⭐ |
| 6 | Task Scheduler | Greedy + Queue | ⭐⭐⭐⭐ |
| 7 | Design Circular Queue | Design | ⭐⭐⭐⭐ |
| 8 | Walls and Gates | Multi-source BFS | ⭐⭐⭐⭐ |
| 9 | Word Ladder | BFS + HashSet | ⭐⭐⭐⭐ |
| 10 | Design Hit Counter | Queue/Deque | ⭐⭐⭐ |
| 11 | Shortest Path in Binary Matrix | 8-dir BFS | ⭐⭐⭐ |
| 12 | Kth Largest in Stream | PQ (min-heap) | ⭐⭐⭐ |

---

*Next:*
- `01_easy.java` — Implement queue, recent counter, first unique, moving average, number of students
- `02_medium.java` — Rotten oranges, task scheduler, design circular queue, Dota2 senate, walls & gates
- `03_hard.java` — Sliding window max, shortest subarray sum ≥ K, jump game VI, word ladder
- `04_google_level.java` — Design hit counter, snake game, shortest path with obstacles, max sliding window with updates
