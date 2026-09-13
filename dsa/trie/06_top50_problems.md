# Trie (Prefix Tree) — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Basic Trie Operations & Prefix Matching

*Goal: Master basic Trie insertion, search, and startsWith mechanics.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Implement Trie (Prefix Tree) | 208 | Medium | Basic TrieNode array implementation |
| 2 | Longest Common Prefix | 14 | Easy | Single-child Trie branch traversal |
| 3 | Replace Words | 648 | Medium | Shortest prefix root matching |
| 4 | Map Sum Pairs | 677 | Medium | Pass-through value sum tracking |
| 5 | Counting Words With a Given Prefix | 2185 | Easy | Pass-through prefix counting |
| 6 | Index Pairs of a String | 1065 | Easy | Multi-pattern substring match |
| 7 | Remove Sub-Folders from the Filesystem | 1233 | Medium | Folder path prefix hierarchy |

---

## Phase 2: Wildcard Matching & DFS Extensions

*Goal: Handle wildcard characters (e.g., '.') and one-character edit queries.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 8 | Design Add and Search Words Data Structure | 211 | Medium | DFS branching on '.' wildcard |
| 9 | Implement Magic Dictionary | 676 | Medium | 1-character edit distance search |
| 10 | Camelcase Matching | 1023 | Medium | Pattern matching with uppercase rules |
| 11 | Search Suggestions System | 1268 | Medium | Node top 3 lexicographical candidates |
| 12 | Shortest Unique Prefix | IB | Medium | Single-pass unique prefix extraction |

---

## Phase 3: Bitwise (Binary) Trie for XOR Optimization

*Goal: Master Bitwise Tries for bitwise operations and range XOR queries.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 13 | Maximum XOR of Two Numbers in an Array | 421 | Medium | 32-bit Binary Trie opposite bit query |
| 14 | Maximum XOR With an Element From Array | 1707 | Hard | Offline sorted queries + Binary Trie |
| 15 | Count Pairs With XOR in a Range | 1803 | Hard | Binary Trie subtree node counting |
| 16 | Maximum XOR Subarray | IB | Hard | Prefix XOR + Binary Trie |
| 17 | Find Xor-Sum of All Pairs Bitwise AND | 1835 | Hard | Bitwise properties + Binary Trie |

---

## Phase 4: Suffix & Reverse Trie Techniques

*Goal: Solve reverse matching, suffix matching, and stream processing.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 18 | Stream of Characters | 1032 | Hard | Reverse word Trie + stream buffer |
| 19 | Shortest Encoding of Words | 820 | Medium | Reverse word Trie leaf depth count |
| 20 | Prefix and Suffix Search | 745 | Hard | Wrapped Trie (`suffix + '{' + prefix`) |
| 21 | Display Table of Food Orders | 1418 | Medium | Reversed prefix aggregation |

---

## Phase 5: Trie + Dynamic Programming Hybrid

*Goal: Combine Trie fast lookups with DP memoization.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 22 | Word Break I | 139 | Medium | Trie-accelerated DP |
| 23 | Word Break II | 140 | Hard | Trie + Memoized DP sentence reconstruction |
| 24 | Extra Characters in a String | 2707 | Medium | Min extra chars with Trie DP |
| 25 | Concatenated Words | 472 | Hard | Sorted word insertion + Trie DP |

---

## Phase 6: Trie + Grid Backtracking (Word Search)

*Goal: Master grid DFS combined with Trie node traversal and dynamic pruning.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 26 | Word Search II | 212 | Hard | Grid DFS + Trie node reference pruning |
| 27 | Boggle Solver | IB | Hard | Classic Boggle board search |
| 28 | Word Squares | 425 | Hard | Column prefix Trie backtracking |

---

## Phase 7: Trie String System Design & Frequency Tracking

*Goal: Build stateful search engines, autocomplete systems, and encrypters.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 29 | Design Search Autocomplete System | 642 | Hard | Trie Node Top 3 PriorityQueue |
| 30 | Encrypt and Decrypt Strings | 2227 | Hard | Pre-encrypted Trie frequency map |
| 31 | Sum of Prefix Scores of Strings | 2416 | Hard | Node pass-through count summation |

---

## Phase 8: Advanced Subtree Serialization & Palindrome Tries

*Goal: Advanced Google-level structural graph/tree conversions.*

| # | Problem | LC# / IB | Difficulty | Key Concept |
|---|---|---|---|---|
| 32 | Palindrome Pairs | 336 | Hard | Reversed Trie + Palindrome prefix/suffix |
| 33 | Delete Duplicate Folders in System | 1948 | Hard | Folder Trie subtree serialization hashing |
| 34 | Hotel Reviews | IB | Medium | Trie frequency counting per review |
| 35 | Spells and Portions | 2300 | Medium | Trie / Binary search hybrid |
| 36-50 | Advanced Bitwise & Suffix Tree Extensions | Various | Hard | Multi-query XOR / Suffix Tree problems |
