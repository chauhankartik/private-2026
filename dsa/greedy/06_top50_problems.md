# Greedy Algorithms — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Simple Sorting & Single Pass (Foundations)

*Goal: Get comfortable with sorting by a single attribute and taking elements greedily.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Assign Cookies | 455 | Easy | Two-pointer greedy matching |
| 2 | Maximum Units on a Truck | 1710 | Easy | Sort by unit yield descending |
| 3 | Array Partition I | 561 | Easy | Pair adjacent elements after sort |
| 4 | Minimum Operations to Make Array Increasing | 1827 | Easy | Single pass strictly increasing adjustment |
| 5 | Largest Number After Digit Swaps by Parity | 2231 | Easy | Separate odd/even digits and place largest |
| 6 | Maximum Sum After K Negations | 1005 | Easy | Sort, negate negative values, handle odd K |
| 7 | Can Place Flowers | 605 | Easy | Local non-adjacent placement check |

---

## Phase 2: Interval Scheduling & Overlaps

*Goal: Master sorting by end-times vs start-times for interval problems.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 8 | Non-overlapping Intervals | 435 | Medium | Sort by end time, eliminate overlaps |
| 9 | Minimum Number of Arrows to Burst Balloons | 452 | Medium | Sort by end coordinate, count arrows |
| 10 | Merge Intervals | 56 | Medium | Sort by start time, merge overlapping |
| 11 | Insert Interval | 57 | Medium | Single-pass insertion & merge |
| 12 | Meeting Rooms I | 252 | Easy | Sort by start time, check overlap |
| 13 | Meeting Rooms II | 253 | Medium | Min-Heap or Chronological sweep |
| 14 | Interval List Intersections | 986 | Medium | Two-pointer interval intersection |

---

## Phase 3: Reachability & Jump Games

*Goal: Master max boundary tracking in single-pass linear time.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 15 | Jump Game I | 55 | Medium | Track maxReach boundary |
| 16 | Jump Game II | 45 | Medium | BFS / Level boundary jumping |
| 17 | Video Stitching | 1024 | Medium | Max reach per interval window |
| 18 | Minimum Number of Taps to Water Garden | 1326 | Hard | Convert to Jump Game II formulation |
| 19 | Gas Station | 134 / IB | Medium | Single pass range elimination |

---

## Phase 4: PriorityQueue / Max-Min Heap Greedy

*Goal: Dynamic selection using heap data structures.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 20 | Task Scheduler | 621 | Medium | Frequency slots / Max-Heap eviction |
| 21 | Reorganize String | 767 | Medium | Max-Heap frequency arrangement |
| 22 | Rearrange String k Distance Apart | 358 | Hard | Max-Heap + Cooldown queue |
| 23 | Course Schedule III | 630 | Hard | Deadline sort + Max-Heap duration eviction |
| 24 | IPO (Initial Public Offering) | 502 | Hard | Dual Heap (Min Capital + Max Profit) |
| 25 | Maximum Performance of a Team | 1383 | Hard | Sort efficiency + Min-Heap speed |
| 26 | Minimum Cost to Hire K Workers | 857 | Hard | Ratio sort + Min-Heap quality |

---

## Phase 5: Two-Pass / Bidirectional Greedy

*Goal: Solve neighbor-dependent constraints using left and right sweeps.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 27 | Candy | 135 / IB | Hard | Left pass + Right pass combining |
| 28 | Trapping Rain Water | 42 | Hard | Left max + Right max / Two pointers |
| 29 | Product of Array Except Self | 238 | Medium | Left prefix product * Right suffix product |
| 30 | Partition Labels | 763 | Medium | Last index map + boundary expansion |

---

## Phase 6: Monotonic Stack / Digit & String Greedy

*Goal: Lexicographical and numerical optimization using monotonic stack.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 31 | Remove K Digits | 402 | Medium | Monotonic increasing stack |
| 32 | Remove Duplicate Letters | 316 | Medium | Monotonic stack + frequency map |
| 33 | Smallest Subsequence of Distinct Characters | 1081 | Medium | Same pattern as #32 |
| 34 | Create Maximum Number | 321 | Hard | Monotonic stack + merge subproblems |
| 35 | Largest Number | 179 / IB | Medium | Custom string sorting `(b+a).compareTo(a+b)` |

---

## Phase 7: Math, Reverse Thinking & Ratio-Based Greedy

*Goal: Transform problem space by reversing operations or optimizing ratios.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 36 | Lemonade Change | 860 | Easy | Prioritize larger bills as change |
| 37 | Bag of Tokens | 948 | Medium | Buy low (power), sell high (score) |
| 38 | Broken Calculator | 991 | Medium | Reverse thinking: divide by 2 or add 1 |
| 39 | Minimum Replacement to Sort Array | 2366 | Hard | Right-to-left element splitting |
| 40 | Minimum Deletions to Make Character Frequencies Unique | 1647 | Medium | HashSet of seen frequencies |
| 41 | Wiggle Subsequence | 376 | Medium | Track alternating peaks and valleys |
| 42 | Maximum Swap | 670 | Medium | Last index map for max digit |

---

## Phase 8: Google-Level Multi-Pattern Hybrid Greedy

*Goal: Solve complex multi-constraint interview problems.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 43 | Patching Array | 330 | Hard | Reachability range expansion |
| 44 | Construct Target Array With Multiple Sums | 1354 | Hard | Reverse engineering Max-Heap |
| 45 | Stamping The Sequence | 936 | Hard | Reverse stamp matching with wildcards |
| 46 | Minimum Cost to Connect Sticks | 1168 | Medium | Huffman coding / Min-Heap pairing |
| 47 | Reduce Array Size to The Half | 1338 | Medium | Frequency bucket sort / heap greedy |
| 48 | Maximum Profit in Job Scheduling | 1235 | Hard | DP + Binary Search (Greedy choice subproblem) |
| 49 | Split Array into Consecutive Subsequences | 659 | Medium | Frequency map + Hypothesized chain map |
| 50 | Majority Element | 169 / IB | Easy | Boyer-Moore Candidate Elimination |
