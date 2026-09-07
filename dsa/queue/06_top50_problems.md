# Queue & Deque — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each phase in order.
> Each phase builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Queue Fundamentals (Master FIFO and design)

*Goal: Get comfortable with queue operations, implement from scratch.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Implement Queue using Stacks | 232 | Easy | Two stacks, amortized O(1) transfer |
| 2 | Implement Stack using Queues | 225 | Easy | Rotate queue on push to bring new element to front |
| 3 | Design Circular Queue | 622 | Medium | Array with front/rear pointers, modular arithmetic |
| 4 | Design Circular Deque | 641 | Medium | Same as circular queue but insert/delete both ends |
| 5 | Number of Recent Calls | 933 | Easy | Queue as sliding time window |
| 6 | Moving Average from Data Stream | 346 | Easy | Fixed-size queue + running sum |

---

## Phase 2: Queue Simulation (Process queues step by step)

*Goal: Model real-world queue processes and reason about termination.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 7 | Number of Students Unable to Eat | 1700 | Easy | Count-based optimization, no actual simulation needed |
| 8 | Time Needed to Buy Tickets | 2073 | Easy | Math: min(tickets[i], tickets[k]) for i ≤ k |
| 9 | Reveal Cards In Increasing Order | 950 | Medium | Reverse simulation with deque |
| 10 | Dota2 Senate | 649 | Medium | Two queues, index comparison, re-enqueue at +n |
| 11 | Task Scheduler | 621 | Medium | Math formula: (maxFreq-1)×(n+1) + maxCount |
| 12 | Design Hit Counter | 362 | Medium | Circular buffer: times[t%300], hits[t%300] |

---

## Phase 3: BFS on Grids (Standard 4/8-directional)

*Goal: Internalize "BFS = shortest path in unweighted graph, level = distance".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 13 | Flood Fill | 733 | Easy | BFS/DFS from source, paint connected component |
| 14 | Number of Islands | 200 | Medium | BFS from each unvisited '1', mark visited |
| 15 | Shortest Path in Binary Matrix | 1091 | Medium | 8-directional BFS, path = cell count |
| 16 | Surrounded Regions | 130 | Medium | BFS from border O's, mark safe, flip rest |
| 17 | Pacific Atlantic Water Flow | 417 | Medium | Two multi-source BFS from each ocean edge |
| 18 | Max Area of Island | 695 | Medium | BFS/DFS, track component size |

---

## Phase 4: Multi-Source BFS (Spread from all sources simultaneously)

*Goal: Master "enqueue ALL sources at once = virtual super-source".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 19 | Rotting Oranges | 994 | Medium | Multi-source BFS from all rotten, count levels |
| 20 | 01 Matrix | 542 | Medium | Multi-source BFS from all zeros |
| 21 | Walls and Gates | 286 | Medium | Multi-source BFS from all gates (INF rooms) |
| 22 | As Far from Land as Possible | 1162 | Medium | Multi-source BFS from all land cells |
| 23 | Map of Highest Peak | 1765 | Medium | Multi-source BFS from all water cells |
| 24 | Shortest Bridge | 934 | Medium | Find island (DFS), BFS expand to second island |

---

## Phase 5: BFS State-Space Search (Non-grid state graphs)

*Goal: Model abstract states as graph nodes, BFS for shortest transformation.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 25 | Open the Lock | 752 | Medium | 10K nodes, 8 neighbors (±1 per digit), deadend avoidance |
| 26 | Word Ladder | 127 | Hard | 26×L neighbor generation per word, HashSet visited |
| 27 | Minimum Genetic Mutation | 433 | Medium | Same as word ladder with 4-char alphabet (ACGT) |
| 28 | Sliding Puzzle | 773 | Hard | BFS on board states, encode as string |
| 29 | Snakes and Ladders | 909 | Medium | BFS on 1D board with jump shortcuts |

---

## Phase 6: BFS with Extra State Dimensions

*Goal: Master adding state dimensions: (row, col, keys/obstacles/fuel).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 30 | Shortest Path with Obstacles Elimination | 1293 | Hard | 3D state: (r, c, remaining_eliminations) |
| 31 | Shortest Path to Get All Keys | 864 | Hard | BFS + bitmask: (r, c, keys_mask) |
| 32 | Minimum Moves to Reach Target with Rotations | 1210 | Hard | BFS with orientation state |
| 33 | Minimum Knight Moves | 1197 | Medium | BFS from (0,0) to (x,y) with 8 knight moves |

---

## Phase 7: Monotonic Deque (Sliding Window Max/Min)

*Goal: Master "remove dominated elements from back, expired from front".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 34 | Sliding Window Maximum | 239 | Hard | Monotonic decreasing deque, stores indices |
| 35 | Sliding Window Minimum | — | — | Same as max, flip to increasing deque |
| 36 | Longest Continuous Subarray Abs Diff ≤ Limit | 1438 | Medium | Two deques (one for max, one for min) |
| 37 | Max Value of Equation | 1499 | Hard | Rewrite equation, deque on (yi - xi) |

---

## Phase 8: DP + Monotonic Deque (Optimize DP lookback)

*Goal: Use deque to optimize "max/min over last k DP values" from O(k) to O(1).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 38 | Jump Game VI | 1696 | Medium | dp[i] = nums[i] + max(dp[i-k..i-1]) via deque |
| 39 | Constrained Subsequence Sum | 1425 | Hard | dp[i] = nums[i] + max(0, dp window) via deque |
| 40 | Shortest Subarray with Sum at Least K | 862 | Hard | Prefix sums + monotonic increasing deque |

---

## Phase 9: Priority Queue / Heap Problems

*Goal: Use PQ for greedy selection of "best" element.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 41 | Kth Largest Element in a Stream | 703 | Easy | Min-heap of size k |
| 42 | Last Stone Weight | 1046 | Easy | Max-heap, smash two largest |
| 43 | K Closest Points to Origin | 973 | Medium | Max-heap of size k, or quickselect |
| 44 | Merge k Sorted Lists | 23 | Hard | Min-heap of list heads |
| 45 | Find Median from Data Stream | 295 | Hard | Two heaps: max-heap (lower) + min-heap (upper) |

---

## Phase 10: Advanced / Multi-Technique (Google-Level)

*Goal: Combine queue/BFS with other patterns (0-1 BFS, greedy, design).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 46 | Min Cost to Make at Least One Valid Path | 1368 | Hard | 0-1 BFS with deque (0-cost: front, 1-cost: back) |
| 47 | Maximum Events That Can Be Attended | 1353 | Medium | Sort by start + min-heap of end days |
| 48 | Design Snake Game | 353 | Medium | Deque (body) + HashSet (collision detection) |
| 49 | Trapping Rain Water II | 407 | Hard | BFS + min-heap on 2D boundary |
| 50 | Cut Off Trees for Golf Event | 675 | Hard | Sort trees by height + BFS between consecutive trees |

---

## Study Roadmap

```
Week 1 (Days 1-2):  Phase 1 — Queue fundamentals (#1-#6)
Week 1 (Days 3-4):  Phase 2 — Queue simulation (#7-#12)
Week 1 (Day 5):     Phase 3 — Grid BFS (#13-#18)
Week 2 (Days 1-2):  Phase 4 — Multi-source BFS (#19-#24)
Week 2 (Days 3-4):  Phase 5 — State-space BFS (#25-#29)
Week 2 (Day 5):     Phase 6 — Extra state BFS (#30-#33)
Week 3 (Days 1-2):  Phase 7 — Monotonic deque (#34-#37)
Week 3 (Day 3):     Phase 8 — DP + deque (#38-#40)
Week 3 (Days 4-5):  Phase 9 — Priority queue (#41-#45)
Week 4 (Days 1-2):  Phase 10 — Advanced (#46-#50)
```

### How to Practice Each Problem

```
1. Read the problem. Classify it (BFS/multi-source/deque/PQ/simulation).
2. MODEL THE GRAPH: what are nodes? what are edges? what are edge weights?
3. IDENTIFY THE PATTERN: shortest path → BFS. Sliding max → deque. Best-first → PQ.
4. Define the STATE: just (r,c)? or (r,c,keys)? or (r,c,obstacles)?
5. Code the solution WITHOUT looking at hints.
6. Verify: Can you explain why BFS gives shortest path in one sentence?
7. Do the follow-ups. They build deeper intuition.
```

---

## Quick Pattern Checklist (Before Coding)

- [ ] Is this about **shortest path** in an unweighted graph? → **BFS**
- [ ] Are there **multiple starting points**? → **Multi-source BFS**
- [ ] Does the graph have **0 and 1 weights only**? → **0-1 BFS (Deque)**
- [ ] Do I need the **max/min in a sliding window**? → **Monotonic Deque**
- [ ] Is there a **DP with lookback of k**? → **DP + Monotonic Deque**
- [ ] Do I need the **k-th best / best element next**? → **Priority Queue**
- [ ] Am I **simulating a process** step by step? → **Queue Simulation**
- [ ] Does the state include **keys / obstacles / fuel**? → **BFS + Extra Dimension**
- [ ] Can I **reverse the BFS direction** for efficiency? → **BFS from targets, not sources**
