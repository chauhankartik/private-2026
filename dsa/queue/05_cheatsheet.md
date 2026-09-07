# Queue & Deque — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "shortest path in unweighted graph/grid" | BFS (Queue) |
| "shortest path with 0/1 weights" | 0-1 BFS (Deque) |
| "level-by-level / layer-by-layer" | BFS with level snapshot |
| "rotten oranges / fire spreading / multi-source" | Multi-source BFS |
| "word transformation one letter at a time" | BFS State-Space Search |
| "sliding window maximum / minimum" | Monotonic Deque |
| "DP with look-back of k" | DP + Monotonic Deque |
| "simulate a process / round-robin" | Queue Simulation |
| "design bounded buffer / circular array" | Circular Queue |
| "first unique / last seen in stream" | Queue + HashMap |
| "task scheduling with cooldown" | Greedy/Math or PQ + Queue |
| "k-th largest / smallest in stream" | Priority Queue (Min-Heap) |
| "merge k sorted lists" | Priority Queue (Min-Heap) |
| "collect keys / states with bitmask" | BFS + Bitmask State |
| "obstacles you can eliminate" | BFS with 3D State |
| "attend events / schedule meetings" | Greedy + PQ |

---

## The Five Core Templates (Memorize These)

### Template 1: Standard BFS (Shortest Path)

```java
Queue<Node> q = new LinkedList<>();
Set<Node> visited = new HashSet<>();
q.offer(start);
visited.add(start);     // ← mark BEFORE offering
int steps = 0;

while (!q.isEmpty()) {
    int size = q.size();              // ← snapshot for level
    for (int i = 0; i < size; i++) {
        Node curr = q.poll();
        if (curr == target) return steps;
        for (Node next : neighbors(curr)) {
            if (!visited.contains(next)) {
                visited.add(next);    // ← mark BEFORE offering
                q.offer(next);
            }
        }
    }
    steps++;
}
return -1;  // unreachable
```

### Template 2: Multi-Source BFS

```java
Queue<int[]> q = new LinkedList<>();
// Enqueue ALL sources simultaneously
for (int[] source : sources) {
    q.offer(source);
    visited[source[0]][source[1]] = true;
}
int dist = 0;

while (!q.isEmpty()) {
    int size = q.size();
    for (int i = 0; i < size; i++) {
        int[] cell = q.poll();
        for (int[] d : dirs) {
            int nr = cell[0]+d[0], nc = cell[1]+d[1];
            if (inBounds(nr, nc) && !visited[nr][nc]) {
                visited[nr][nc] = true;
                result[nr][nc] = dist + 1;
                q.offer(new int[]{nr, nc});
            }
        }
    }
    dist++;
}
```

### Template 3: Monotonic Deque (Sliding Window Max)

```java
Deque<Integer> dq = new ArrayDeque<>();  // stores INDICES

for (int i = 0; i < n; i++) {
    // 1. Remove expired
    while (!dq.isEmpty() && dq.peekFirst() < i - k + 1)
        dq.pollFirst();
    // 2. Remove dominated (for MAX: remove smaller)
    while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i])
        dq.pollLast();
    // 3. Add current
    dq.offerLast(i);
    // 4. Front = answer
    if (i >= k - 1) result[i-k+1] = nums[dq.peekFirst()];
}
```

### Template 4: 0-1 BFS (Deque-based Dijkstra)

```java
Deque<int[]> dq = new ArrayDeque<>();
int[][] dist = new int[m][n];
for (int[] row : dist) Arrays.fill(row, INF);
dist[0][0] = 0;
dq.offerFirst(new int[]{0, 0});

while (!dq.isEmpty()) {
    int[] cell = dq.pollFirst();
    for (neighbor : neighbors) {
        int cost = (matches_arrow) ? 0 : 1;
        if (dist[r][c] + cost < dist[nr][nc]) {
            dist[nr][nc] = dist[r][c] + cost;
            if (cost == 0) dq.offerFirst(neighbor);  // front
            else           dq.offerLast(neighbor);   // back
        }
    }
}
```

### Template 5: Queue using Two Stacks

```java
Stack<Integer> inStack, outStack;

void push(int x) { inStack.push(x); }

int pop() {
    if (outStack.isEmpty())
        while (!inStack.isEmpty()) outStack.push(inStack.pop());
    return outStack.pop();
}
// Amortized O(1): each element transferred at most once.
```

---

## Java Queue API Quick Reference

```java
// Queue (FIFO)
Queue<E> q = new LinkedList<>();       // or new ArrayDeque<>()
q.offer(e);        // add to back (returns false if full)
q.poll();          // remove from front (returns null if empty)
q.peek();          // view front (returns null if empty)

// Deque (Double-Ended)
Deque<E> dq = new ArrayDeque<>();
dq.offerFirst(e);  dq.offerLast(e);   // add to front/back
dq.pollFirst();    dq.pollLast();      // remove from front/back
dq.peekFirst();    dq.peekLast();      // view front/back

// Priority Queue (Heap)
PriorityQueue<E> pq = new PriorityQueue<>();                    // min-heap
PriorityQueue<E> pq = new PriorityQueue<>(Comparator.reverseOrder()); // max-heap
pq.offer(e);       // insert O(log n)
pq.poll();         // remove min/max O(log n)
pq.peek();         // view min/max O(1)
```

**IMPORTANT: ArrayDeque vs LinkedList**
```
ArrayDeque: faster (no node allocation), cache-friendly, no null elements.
LinkedList: supports null, implements List interface, more memory overhead.
For interviews: use ArrayDeque for Deque/Stack, LinkedList for Queue.
```

---

## Complexity Quick Reference

| Problem Type | Time | Space |
|---|---|---|
| BFS (unweighted graph/grid) | O(V + E) or O(m×n) | O(V) or O(m×n) |
| Multi-source BFS | O(m × n) | O(m × n) |
| 0-1 BFS | O(V + E) | O(V) |
| Monotonic Deque (window max/min) | O(n) | O(k) |
| DP + Monotonic Deque | O(n) | O(n) |
| BFS + Bitmask (K keys) | O(m×n×2^K) | O(m×n×2^K) |
| BFS + Extra State (K obstacles) | O(m×n×K) | O(m×n×K) |
| Queue using Two Stacks | O(1) amortized | O(n) |
| Circular Queue/Deque | O(1) per op | O(k) |
| PQ-based scheduling | O(n log n) | O(n) |

---

## Problem → Pattern Map (for Google interviews)

```
Rotten Oranges / Walls & Gates     → Multi-source BFS
01 Matrix / As Far from Land       → Multi-source BFS
Shortest Path Binary Matrix        → BFS (8-dir grid)
Open the Lock                      → BFS (state space)
Word Ladder                        → BFS (string state space)
Sliding Window Maximum             → Monotonic Deque (decreasing)
Jump Game VI                       → DP + Monotonic Deque
Constrained Subsequence Sum        → DP + Monotonic Deque
Shortest Subarray Sum ≥ K         → Prefix Sum + Monotonic Deque (increasing)
Queue from Stacks                  → Two Stacks (amortized O(1))
Design Circular Queue              → Array with front/rear pointers
Hit Counter                        → Circular Buffer (time % 300)
Snake Game                         → Deque (body) + HashSet (collision)
Min Cost Valid Path                → 0-1 BFS (Deque)
Shortest Path All Keys             → BFS + Bitmask State
Task Scheduler                     → Math/Greedy or PQ + Queue
Max Events Attended                → Greedy + Min-Heap
```

---

## Multi-Source BFS vs Single-Source BFS

```
SINGLE-SOURCE:  One starting point. Find shortest path to targets.
  → Start: q.offer(source)
  → Use case: shortest path from A to B.

MULTI-SOURCE:   Multiple starting points. Find nearest source for each cell.
  → Start: q.offer(source) for ALL sources
  → Equivalent to: virtual super-source connected to all real sources.
  → Use case: nearest gate, nearest zero, rotten orange propagation.

KEY INSIGHT: Multi-source BFS computes all distances in ONE pass.
  Naive: BFS from each cell → O(n² × m²).
  Optimal: BFS from all sources → O(n × m).
```

---

## Monotonic Deque — Which Direction?

```
SLIDING WINDOW MAXIMUM → Monotonic DECREASING deque
  Remove from back: elements SMALLER than current (they'll never be max)
  Front = current maximum

SLIDING WINDOW MINIMUM → Monotonic INCREASING deque
  Remove from back: elements LARGER than current (they'll never be min)
  Front = current minimum

SHORTEST SUBARRAY ≥ K → Monotonic INCREASING deque on PREFIX SUMS
  Remove from back: prefix sums LARGER than current (dominated)
  Remove from front: when condition satisfied (used up)
```

---

## Interview Communication Script

When you start solving any queue/BFS problem:

> "I recognize this as a BFS problem because we need the shortest path
> in an unweighted graph / we need level-by-level processing.
> - **State**: Each node in the BFS is [position / (position + extra)].
> - **Neighbors**: [4-directional / 8-directional / character changes].
> - **Visited**: [2D array / 3D with extra state / HashSet].
> The time complexity is O(V + E) because BFS visits each node once.
> The space is O(V) for the queue and visited set."

For monotonic deque problems:

> "I need the maximum/minimum in a sliding window efficiently.
> I'll use a monotonic deque that stores indices in [decreasing/increasing]
> order of values. Each element is pushed and popped at most once,
> giving O(n) total time."

---

*Files in this module:*
- `00_theory.md` — Queue variants, implementations, five patterns, BFS vs DFS
- `01_easy.java` — 8 easy problems (queue design, simulation, deque)
- `02_medium.java` — 9 medium problems (multi-source BFS, circular queue, scheduling)
- `03_hard.java` — 6 hard problems (monotonic deque, word ladder, DP+deque)
- `04_google_level.java` — 5 Google-level (0-1 BFS, bitmask BFS, hit counter)
- `05_cheatsheet.md` — This file
- `06_top50_problems.md` — Top 50 problems in intuition-building order
