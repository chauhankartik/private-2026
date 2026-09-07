# Hashing — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase introduces a new hashing pattern. Master it before moving on.

---

## Phase 1: Frequency Counting (The most common pattern)

*Goal: Build frequency maps, compare frequencies, use array[26] for alphabet.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Valid Anagram | 242 | Easy | freq[c]++ for s, freq[c]-- for t, check all zero |
| 2 | First Unique Character in a String | 387 | Easy | Frequency array, second pass for first freq==1 |
| 3 | Ransom Note | 383 | Easy | Freq count of magazine, check ransom fits |
| 4 | Find the Difference | 389 | Easy | Frequency or XOR trick |
| 5 | Sort Characters By Frequency | 451 | Medium | Frequency map → bucket sort by freq |
| 6 | Top K Frequent Elements | 347 | Medium | Frequency map + bucket sort O(n) or heap O(n log k) |
| 7 | Top K Frequent Words | 692 | Medium | Freq map + custom sort (freq desc, lexicographic asc) |
| 8 | Majority Element | 169 | Easy | HashMap freq (or Boyer-Moore for O(1) space) |

---

## Phase 2: Two-Sum / Complement Lookup

*Goal: "Have I seen the complement before?" — one-pass HashMap.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 9 | Two Sum | 1 | Easy | Store value→index, check complement |
| 10 | Two Sum II (sorted) | 167 | Medium | Two pointers (compare with hashing approach) |
| 11 | Two Sum III (data structure) | 170 | Easy | Design: add + find with HashMap |
| 12 | 4Sum II | 454 | Medium | Split into two halves, HashMap on pair sums |
| 13 | Count Pairs With Given Sum | — | Easy | For each num, count freq of (target - num) |
| 14 | Pairs of Songs With Total Durations Divisible by 60 | 1010 | Medium | Complement = (60 - t%60) % 60, freq array[60] |

---

## Phase 3: Prefix Sum + HashMap (Subarray sum problems)

*Goal: "How many previous prefix sums equal current - k?"*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 15 | Subarray Sum Equals K | 560 | Medium | prefixSum→count map, don't forget map.put(0,1) |
| 16 | Contiguous Array | 525 | Medium | Transform 0→-1, prefix sum + first-seen index |
| 17 | Binary Subarrays With Sum | 930 | Medium | Prefix sum + freq count (or sliding window) |
| 18 | Subarray Sums Divisible by K | 974 | Medium | Prefix mod K, count same remainders |
| 19 | Continuous Subarray Sum | 523 | Medium | Prefix mod K, check if same remainder seen ≥ 2 apart |
| 20 | Count Number of Nice Subarrays | 1248 | Medium | Prefix count of odds, or atMost(K) trick |

---

## Phase 4: Grouping / Bucketing (Group by canonical form)

*Goal: Convert elements to a canonical key, group into buckets.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 21 | Group Anagrams | 49 | Medium | Key = sorted string or char count |
| 22 | Group Shifted Strings | 249 | Medium | Key = difference pattern (shifts) |
| 23 | Isomorphic Strings | 205 | Easy | Bidirectional mapping (two maps) |
| 24 | Word Pattern | 290 | Easy | Same bidirectional mapping, word ↔ char |
| 25 | Find Duplicate File in System | 609 | Medium | Group by file content |
| 26 | Encode and Decode TinyURL | 535 | Medium | Design: hash → URL mapping |

---

## Phase 5: Index Tracking / First-Last Seen

*Goal: Store indices in the map to answer position-based questions.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 27 | Contains Duplicate | 217 | Easy | HashSet add returns false on dup |
| 28 | Contains Duplicate II | 219 | Easy | Sliding window HashSet of size k |
| 29 | Longest Consecutive Sequence | 128 | Medium | HashSet, only start from sequence beginnings |
| 30 | Longest Substring Without Repeating Characters | 3 | Medium | HashMap of char→last index (sliding window) |
| 31 | Maximum Size Subarray Sum Equals k | 325 | Medium | Prefix sum + first-seen index of each prefix |
| 32 | Minimum Window Substring | 76 | Hard | Two freq maps: need vs have |

---

## Phase 6: Set Operations / Deduplication

*Goal: Use sets for membership, intersection, difference.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 33 | Intersection of Two Arrays | 349 | Easy | Set intersection |
| 34 | Intersection of Two Arrays II | 350 | Easy | Frequency map, min(freq1, freq2) |
| 35 | Happy Number | 202 | Easy | Cycle detection with HashSet |
| 36 | Single Number | 136 | Easy | XOR (or HashMap, but XOR is O(1) space) |
| 37 | Single Number II | 137 | Medium | Bit counting or HashMap |
| 38 | Missing Number | 268 | Easy | HashSet or XOR or math (sum formula) |

---

## Phase 7: Design / Advanced Hashing

*Goal: Build data structures using HashMaps as the backbone.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 39 | LRU Cache | 146 | Medium | HashMap + Doubly Linked List |
| 40 | LFU Cache | 460 | Hard | HashMap + freq→LinkedHashSet |
| 41 | Insert Delete GetRandom O(1) | 380 | Medium | HashMap + ArrayList (swap-to-end delete) |
| 42 | Design HashMap | 706 | Easy | Array of linked lists (separate chaining) |
| 43 | Design HashSet | 705 | Easy | Same as above |
| 44 | Time Based Key-Value Store | 981 | Medium | HashMap → TreeMap (floorKey) |
| 45 | Snapshot Array | 1146 | Medium | List of TreeMaps per index |

---

## Phase 8: Google-Level Multi-Pattern

*Goal: Problems that combine hashing with other techniques.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 46 | Substring with Concatenation of All Words | 30 | Hard | Word-level sliding window + freq map |
| 47 | Longest Duplicate Substring | 1044 | Hard | Binary search + rolling hash (Rabin-Karp) |
| 48 | Repeated DNA Sequences | 187 | Medium | Rolling hash or substring HashSet |
| 49 | Minimum Area Rectangle | 939 | Medium | HashSet of points, check diagonal pairs |
| 50 | Brick Wall | 554 | Medium | HashMap of gap positions, maximize gaps |

---

## Study Roadmap

```
Week 1 (Days 1-2):  Phase 1 — Frequency counting (#1-#8)
Week 1 (Days 3-4):  Phase 2 — Complement lookup (#9-#14)
Week 1 (Day 5):     Phase 3 — Prefix sum + map (#15-#20)
Week 2 (Days 1-2):  Phase 4 — Grouping (#21-#26)
Week 2 (Days 3-4):  Phase 5-6 — Index tracking + sets (#27-#38)
Week 3 (Days 1-3):  Phase 7 — Design (#39-#45)
Week 3 (Days 4-5):  Phase 8 — Advanced (#46-#50)
```
