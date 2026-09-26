# Arrays — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase builds on the previous one. Master the pattern before moving on.

---

## Phase 1: Two Pointers — Opposite Ends (Sorted array fundamentals)

*Goal: Use sorted order to eliminate brute-force pair scanning.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Two Sum II (sorted) | 167 | Medium | Left/right pointers converge on target |
| 2 | Valid Palindrome | 125 | Easy | Two pointers from both ends |
| 3 | Container With Most Water | 11 | Medium | Greedy: move the shorter side |
| 4 | 3Sum | 15 | Medium | Sort + fix one + two pointers |
| 5 | 3Sum Closest | 16 | Medium | Same structure, track closest |
| 6 | Trapping Rain Water | 42 | Hard | Two pointers with leftMax/rightMax |
| 7 | 4Sum | 18 | Medium | Fix two + two pointers |

---

## Phase 2: Two Pointers — Same Direction (In-place write / partition)

*Goal: Master the slow/fast pointer technique for in-place array mutation.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 8 | Remove Duplicates from Sorted Array | 26 | Easy | Slow=write, fast=read |
| 9 | Remove Duplicates II (allow 2) | 80 | Medium | Generalize: check slow-2 |
| 10 | Remove Element | 27 | Easy | Skip target values |
| 11 | Move Zeroes | 283 | Easy | Swap non-zeros forward |
| 12 | Sort Colors (Dutch National Flag) | 75 | Medium | 3-way partition with 3 pointers |
| 13 | Merge Sorted Array | 88 | Easy | Merge from the END to avoid overwrites |
| 14 | Squares of a Sorted Array | 977 | Easy | Two pointers from both ends → result from back |

---

## Phase 3: Prefix Sum / Product

*Goal: Precompute cumulative values for O(1) range queries.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 15 | Running Sum of 1D Array | 1480 | Easy | Simplest prefix sum |
| 16 | Product of Array Except Self | 238 | Medium | Prefix product × suffix product, no division |
| 17 | Subarray Sum Equals K | 560 | Medium | Prefix sum + HashMap (count complements) |
| 18 | Contiguous Array | 525 | Medium | Transform 0→-1, prefix sum + first-seen index |
| 19 | Range Sum Query (Immutable) | 303 | Easy | Prefix array, sum(l,r) = p[r+1] - p[l] |
| 20 | Range Sum Query 2D (Immutable) | 304 | Medium | 2D prefix sum with inclusion-exclusion |

---

## Phase 4: Kadane's Algorithm (Maximum subarray)

*Goal: "Extend or restart?" — the core DP-on-subarrays insight.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 21 | Maximum Subarray | 53 | Medium | Classic Kadane's |
| 22 | Maximum Subarray Sum Circular | 918 | Medium | max(Kadane, total - minKadane) |
| 23 | Maximum Product Subarray | 152 | Medium | Track both max AND min (neg×neg) |
| 24 | Best Time to Buy and Sell Stock | 121 | Easy | Running minimum (Kadane variant) |
| 25 | Best Time to Buy and Sell Stock II | 122 | Medium | Greedy: sum all positive diffs |

---

## Phase 5: Binary Search on Arrays

*Goal: Apply binary search beyond simple "find element".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 26 | Binary Search | 704 | Easy | Standard template |
| 27 | Search Insert Position | 35 | Easy | Lower bound / insertion point |
| 28 | Find First and Last Position | 34 | Medium | Two binary searches (leftmost + rightmost) |
| 29 | Search in Rotated Sorted Array | 33 | Medium | Identify sorted half, search accordingly |
| 30 | Find Minimum in Rotated Sorted Array | 153 | Medium | Binary search on rotation point |
| 31 | Koko Eating Bananas | 875 | Medium | Binary search on answer (isFeasible check) |
| 32 | Split Array Largest Sum | 410 | Hard | Binary search on answer + greedy validation |

---

## Phase 6: Interval Problems (Sort + sweep)

*Goal: Sort by start/end, then linear pass to merge/count.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 33 | Merge Intervals | 56 | Medium | Sort by start, extend or new interval |
| 34 | Insert Interval | 57 | Medium | Three-phase: before, overlapping, after |
| 35 | Non-overlapping Intervals | 435 | Medium | Sort by end, greedy removal |
| 36 | Meeting Rooms | 252 | Easy | Sort + check any overlap |
| 37 | Meeting Rooms II | 253 | Medium | Min-heap for end times or sweep line |
| 38 | Minimum Number of Arrows to Burst Balloons | 452 | Medium | Sort by end, greedy (same as #35) |

---

## Phase 7: Matrix Traversal

*Goal: Master boundary tracking and spiral/diagonal patterns.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 39 | Spiral Matrix | 54 | Medium | 4-boundary simulation |
| 40 | Rotate Image | 48 | Medium | Transpose + reverse rows |
| 41 | Set Matrix Zeroes | 73 | Medium | Use first row/col as markers |
| 42 | Search a 2D Matrix | 74 | Medium | Treat as 1D sorted array |
| 43 | Search a 2D Matrix II | 240 | Medium | Start from top-right corner |

---

## Phase 8: Advanced / Google-Level

*Goal: Combine multiple patterns, handle edge cases.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 44 | First Missing Positive | 41 | Hard | Cyclic sort: place nums[i] at index nums[i]-1 |
| 45 | Median of Two Sorted Arrays | 4 | Hard | Binary search on partition |
| 46 | Next Permutation | 31 | Medium | Lexicographic algorithm |
| 47 | Longest Increasing Subsequence | 300 | Medium | DP + binary search (patience sort) |
| 48 | Longest Consecutive Sequence | 128 | Medium | HashSet + "start only at true start" |
| 49 | Majority Element | 169 | Easy | Boyer-Moore voting |
| 50 | Majority Element II | 229 | Medium | Extended Boyer-Moore (at most 2 candidates) |

---

## Study Roadmap

```
Week 1 (Days 1-3):  Phase 1-2 — Two pointers (#1-#14)
Week 1 (Days 4-5):  Phase 3 — Prefix sum (#15-#20)
Week 2 (Days 1-2):  Phase 4 — Kadane's (#21-#25)
Week 2 (Days 3-4):  Phase 5 — Binary search (#26-#32)
Week 2 (Day 5):     Phase 6 — Intervals (#33-#38)
Week 3 (Days 1-2):  Phase 7 — Matrix (#39-#43)
Week 3 (Days 3-5):  Phase 8 — Advanced (#44-#50)
```
