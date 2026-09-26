# Recursion & Backtracking — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each section in order.
> Each phase builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Subsets & Combinations Foundations

*Goal: Master inclusion/exclusion and range-based combination generation.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Subsets / Power Set | 78 | Medium | Basic inclusion / exclusion |
| 2 | Combinations | 77 | Medium | Range 1..N combination of size K |
| 3 | Combination Sum I | 39 | Medium | Candidate combination with unlimited reuse |
| 4 | Letter Combinations of Phone Number | 17 | Medium | Keypad digit letter branching |
| 5 | Generate Parentheses | 22 | Medium | Open / close count constraints |
| 6 | Sum of All Subset XOR Totals | 1863 | Easy | Subset XOR tree sum |
| 7 | Binary Tree Paths | 257 | Easy | Tree root-to-leaf path collection |

---

## Phase 2: Permutations & Visited Tracking

*Goal: Master visited array tracking and fixed-length ordering.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 8 | Permutations | 46 | Medium | `boolean[] used` array tracking |
| 9 | Beautiful Arrangement | 526 | Medium | Divisibility check per position |
| 10 | Letter Case Permutation | 784 | Medium | Lowercase / uppercase branch per char |
| 11 | Sequential Digits | 1291 | Medium | Sliding window / recursive digit generation |

---

## Phase 3: Duplicate Handling & Sister Branch Pruning

*Goal: Master sorting + skipping duplicate elements at the same tree depth.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 12 | Subsets II | 90 | Medium | `i > start && nums[i] == nums[i-1]` |
| 13 | Permutations II | 47 | Medium | `!used[i-1] && nums[i] == nums[i-1]` |
| 14 | Combination Sum II | 40 | Medium | Single use + duplicate skip |
| 15 | Combination Sum III | 216 | Medium | Range 1..9 combination sum of size K |
| 16 | Palindrome Partitioning | 131 | Medium | String prefix palindrome check |

---

## Phase 4: Grid Backtracking & In-Place Cell Marking

*Goal: Master 2D matrix DFS, in-place visited marking, and path restoration.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 17 | Word Search I | 79 | Medium | Grid DFS + `board[r][c] = '#'` |
| 18 | Flood Fill | 733 | Easy | Grid DFS color replacement |
| 19 | Number of Islands | 200 | Medium | Grid DFS component sink |
| 20 | Surround Regions | 130 | Medium | Boundary-connected DFS |
| 21 | Max Area of Island | 695 | Medium | Grid DFS area accumulator |
| 22 | Path with Maximum Gold | 1219 | Medium | Grid DFS max gold path |

---

## Phase 5: Constraint Satisfaction & Bitmasking (Queens & Sudoku)

*Goal: Fast collision detection using bitmasks and arrays.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 23 | N-Queens I | 51 | Hard | Array / Bitmask diagonal tracking |
| 24 | N-Queens II | 52 | Hard | N-Queens solution count |
| 25 | Sudoku Solver | 37 | Hard | 9x9 Bitmask collision check |
| 26 | Valid Sudoku | 36 | Medium | Sudoku constraint validation |

---

## Phase 6: Partitioning & Subset Sum Matching

*Goal: Bucket partitioning, target sum matching, and descending sort pruning.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 27 | Partition to K Equal Sum Subsets | 698 | Medium | Sort descending + bucket filling |
| 28 | Matchsticks to Square | 473 | Medium | 4-side target sum bucket filling |
| 29 | Target Sum | 494 | Medium | Expression sign choice (+/-) |
| 30 | Restore IP Addresses | 93 | Medium | 4-part string segment partitioning |
| 31 | Split Array into Fibonacci Sequence | 842 | Medium | Numeric string Fibonacci partitioning |

---

## Phase 7: Expression Parsing & String Manipulation

*Goal: Parse arithmetic expressions, boolean trees, and string operators.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 32 | Expression Add Operators | 282 | Hard | Operand evaluation with multiplication |
| 33 | Remove Invalid Parentheses | 301 | Hard | Min removal count + DFS |
| 34 | Word Break II | 140 | Hard | Recursion + Memoized Partitioning |
| 35 | Parsing A Boolean Expression | 1106 | Hard | Recursive descent parser |

---

## Phase 8: Google-Level Advanced State & Geometric Backtracking

*Goal: Solve complex multi-pattern problems with physical state, spatial constraints, and extreme pruning.*

| # | Problem | LC# / CSES | Difficulty | Key Concept |
|---|---|---|---|---|
| 36 | Robot Room Cleaner | 489 | Hard | Physical direction turning & undoing |
| 37 | Word Ladder II | 126 | Hard | BFS Distance Map + DFS Path Reconstruction |
| 38 | Unique Paths III | 980 | Hard | Hamiltonian Path Grid Backtracking |
| 39 | Tiling a Rectangle with Fewest Squares | 1240 | Hard | Lowest-cell geometric grid tiling |
| 40 | Grid Paths 48-Step Optimization | CSES 1625 | Hard | Wall-split and dead-end early pruning |
| 41-50 | Advanced Hybrid Backtracking | Various | Hard | Game tree / Alpha-Beta Pruning / Treap |
