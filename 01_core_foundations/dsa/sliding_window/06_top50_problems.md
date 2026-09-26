# Sliding Window — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each section builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Fixed Window Foundations (Master O(1) slide)

*Goal: Get comfortable with the "add right, remove left" mechanic.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Maximum Sum Subarray of Size K | — | Easy | Pure fixed window, running sum |
| 2 | Maximum Average Subarray I | 643 | Easy | Fixed window, average = sum/k |
| 3 | Number of Sub-arrays of Size K with Avg ≥ Threshold | 1343 | Medium | Fixed window + counting, avoid float division |
| 4 | Maximum Number of Vowels in a Substring of Given Length | 1456 | Medium | Fixed window + character counting |
| 5 | Defuse the Bomb | 1652 | Easy | Fixed window + circular array (modular index) |
| 6 | Diet Plan Performance | 1176 | Easy | Fixed window + conditional scoring |
| 7 | Minimum Difference Between Highest and Lowest of K Scores | 1984 | Easy | Sort first, then fixed window |
| 8 | Grumpy Bookstore Owner | 1052 | Medium | Fixed window "overlay" on base calculation |

---

## Phase 2: Variable Window — Longest (Expand greedily, shrink when invalid)

*Goal: Internalize "expand right always, shrink left only when broken".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 9 | Max Consecutive Ones | 485 | Easy | Simplest variable window (just a counter) |
| 10 | Longest Substring Without Repeating Characters | 3 | Medium | HashMap of last-seen index, jump left pointer |
| 11 | Max Consecutive Ones III | 1004 | Medium | Window with at most k zeros (counter-based) |
| 12 | Longest Repeating Character Replacement | 424 | Medium | maxFreq trick — don't decrement on shrink |
| 13 | Fruit Into Baskets | 904 | Medium | At most 2 distinct (freq map + shrink) |
| 14 | Longest Substring with At Most Two Distinct Characters | 159 | Medium | Same pattern as #13, string version |
| 15 | Longest Substring with At Most K Distinct Characters | 340 | Medium | Generalized K distinct |
| 16 | Longest Subarray of 1's After Deleting One Element | 1493 | Medium | At most 1 zero + must delete one element |
| 17 | Get Equal Substrings Within Budget | 1208 | Medium | Window where cost ≤ maxCost |
| 18 | Maximum Points You Can Obtain from Cards | 1423 | Medium | Reverse thinking: minimize middle subarray |

---

## Phase 3: Variable Window — Shortest (Shrink greedily when valid)

*Goal: Flip the logic — record answer when valid, then shrink to find shorter.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 19 | Minimum Size Subarray Sum | 209 | Medium | Shortest subarray with sum ≥ target (positives) |
| 20 | Minimum Window Substring | 76 | Hard | Shortest window containing all chars of t |
| 21 | Minimum Window Subsequence | 727 | Hard | Shortest window containing t as subsequence |
| 22 | Minimum Operations to Reduce X to Zero | 1658 | Medium | Reverse: find longest middle with sum = total - x |

---

## Phase 4: Fixed Window + Frequency Matching (Anagram / Permutation)

*Goal: Master the "matches counter" technique for O(1) frequency comparison.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 23 | Permutation in String | 567 | Medium | Fixed window, 26-char freq match |
| 24 | Find All Anagrams in a String | 438 | Medium | Same as #23, collect ALL positions |
| 25 | Substring with Concatenation of All Words | 30 | Hard | Word-level sliding window, multiple offsets |

---

## Phase 5: Counting Subarrays (count += right - left + 1)

*Goal: Understand why `right - left + 1` counts all valid subarrays ending at right.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 26 | Subarray Product Less Than K | 713 | Medium | Product monotonic (positives), count valid |
| 27 | Count Number of Nice Subarrays | 1248 | Medium | exactly(K) = atMost(K) - atMost(K-1) |
| 28 | Subarrays with K Different Integers | 992 | Hard | exactly(K) decomposition, freq map |
| 29 | Binary Subarrays With Sum | 930 | Medium | exactly(K) on binary array |
| 30 | Count Subarrays Where Max Element Appears at Least K Times | 2962 | Medium | Track max occurrences in window |

---

## Phase 6: Monotonic Deque (Window Max/Min in O(1))

*Goal: Master the "remove dominated elements" deque technique.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 31 | Sliding Window Maximum | 239 | Hard | Monotonic decreasing deque, stores indices |
| 32 | Sliding Window Minimum | — | — | Same as #31, flip to increasing deque |
| 33 | Jump Game VI | 1696 | Medium | DP + monotonic deque (look back k) |
| 34 | Constrained Subsequence Sum | 1425 | Hard | DP + monotonic deque (same pattern as #33) |
| 35 | Longest Continuous Subarray With Abs Diff ≤ Limit | 1438 | Medium | Two deques (one for max, one for min) |
| 36 | Max Value of Equation | 1499 | Hard | Rewrite equation, deque on (yi - xi) |
| 37 | Shortest Subarray with Sum at Least K | 862 | Hard | Prefix sums + monotonic INCREASING deque |

---

## Phase 7: Advanced / Multi-Technique (Google-Level)

*Goal: Combine sliding window with other patterns (DP, binary search, greedy).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 38 | Longest Substring with At Least K Repeating Characters | 395 | Medium | Fix unique count (1..26), run variable window |
| 39 | Minimum Number of K Consecutive Bit Flips | 995 | Hard | Greedy + flip tracking with window |
| 40 | Maximum Number of Visible People in a Queue | 1944 | Hard | Monotonic stack (window-like thinking) |
| 41 | Minimum Swaps to Group All 1's Together | 1151 | Medium | Fixed window of size = count(1s), minimize zeros |
| 42 | Minimum Swaps to Group All 1's Together II | 2134 | Medium | Circular version of #41 |
| 43 | Replace the Substring for Balanced String | 1234 | Medium | Shortest window to remove excess chars |
| 44 | Count Subarrays With Fixed Bounds | 2444 | Hard | Track last positions of minK, maxK, and invalid |
| 45 | Maximum Beauty of an Array After Applying Operation | 2779 | Medium | Sort + variable window |
| 46 | Frequency of the Most Frequent Element | 1838 | Medium | Sort + variable window + budget |
| 47 | Minimum Number of Flips to Make Binary String Alternating | 1888 | Medium | Circular + fixed window on doubled string |
| 48 | K Radius Subarray Averages | 2090 | Easy | Fixed window centered at each index |
| 49 | Maximum Sum of Distinct Subarrays With Length K | 2461 | Medium | Fixed window + HashSet for uniqueness |
| 50 | Count Complete Subarrays in an Array | 2799 | Medium | Exactly K distinct (K = total unique in array) |

---

## Study Roadmap

```
Week 1 (Days 1-2):  Phase 1 — Fixed window (#1-#8)
Week 1 (Days 3-5):  Phase 2 — Variable longest (#9-#18)
Week 2 (Days 1-2):  Phase 3 — Variable shortest (#19-#22)
Week 2 (Day 3):     Phase 4 — Freq matching (#23-#25)
Week 2 (Days 4-5):  Phase 5 — Counting (#26-#30)
Week 3 (Days 1-3):  Phase 6 — Monotonic deque (#31-#37)
Week 3 (Days 4-5):  Phase 7 — Advanced (#38-#50)
```

### How to Practice Each Problem

```
1. Read the problem. Classify it (fixed/variable/counting/deque).
2. Write the brute force approach and its complexity.
3. Apply the correct template from 00_theory.md.
4. Code the optimal solution WITHOUT looking at hints.
5. Verify: Can you explain why it's O(n) in one sentence?
6. Do the follow-ups. They build deeper intuition.
```

---

## Quick Pattern Checklist (Before Coding)

- [ ] Is this about **contiguous** subarrays/substrings?
- [ ] Is the window **fixed** or **variable** size?
- [ ] Am I looking for **longest**, **shortest**, or **count**?
- [ ] Is the constraint **monotonic** (sum with positives, distinct count)?
- [ ] Does it involve **exactly K**? → Use atMost decomposition
- [ ] Do I need **window max/min**? → Monotonic deque
- [ ] Are there **negative numbers** in a sum problem? → Prefix sum + deque, NOT basic window
