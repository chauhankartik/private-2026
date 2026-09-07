# DSA Mastery — Google Interview Preparation

A structured, pattern-oriented study module for mastering Data Structures & Algorithms, built for senior and Google-level technical interviews.

Each module follows the same proven structure:
- **Deep theory** with internal mechanics and complexity proofs
- **Problems categorized by difficulty** — Easy → Medium → Hard → Google-level
- **Brute force → optimal** progression with interview-ready explanations
- **Cheatsheet** for 30-second recall
- **Top 50 problem list** ordered by intuition-building progression

---

## 📂 Modules

| Module | Topics Covered | Files |
|---|---|---|
| [Array](./array/) | Two Pointers, Prefix Sum, Kadane's, Binary Search, Intervals, Matrix | 7 files |
| [Hashing](./hashing/) | Frequency Count, Complement Lookup, Prefix+HashMap, Grouping, Design | 7 files |
| [Graph](./graph/) | BFS/DFS, Topological Sort, Dijkstra, Union-Find, MST, Tarjan's | 7 files |
| [Sliding Window](./sliding_window/) | Fixed Window, Variable Window, Counting, Monotonic Deque, DP+Deque | 7 files |

---

## 📁 File Structure (Per Module)

```
<topic>/
├── 00_theory.md          # Internals, proofs, complexity tables, interview vocab
├── 01_easy.java          # 8 easy problems with brute-force → optimal + follow-ups
├── 02_medium.java        # 8 medium problems
├── 03_hard.java          # 6 hard problems
├── 04_google_level.java  # 6 Google-level multi-pattern problems
├── 05_cheatsheet.md      # Quick reference — templates, patterns, complexity
└── 06_top50_problems.md  # Curated list, ordered by intuition-building phases
```

---

## 🗺️ Learning Path

Work through modules in this order for maximum concept transfer:

```
1. Array          → Foundation of all other topics (memory, pointers, sorting)
2. Hashing        → Complements arrays; unlocks O(1) lookup patterns
3. Sliding Window → Builds on arrays + hashing; unlocks O(n) subarray patterns
4. Graph          → Brings everything together (BFS uses queues, Dijkstra uses heaps)
```

Within each module, follow the **Phase order** in `06_top50_problems.md`:

```
Phase 1 → Phase 2 → ... → Phase 7/8
(basics)                  (Google-level)
```

---

## 🎯 Problem Format

Every problem follows this structure:

```java
// =========================================================
// PROBLEM NAME
// Pattern: <pattern tag>
// LeetCode: <LC#>
// =========================================================
/**
 * Problem: [description]
 *
 * Brute Force O(...): [approach]
 *
 * Optimal O(...): [approach]
 * Why does this work? [proof/intuition]
 *
 * Time:  O(...)
 * Space: O(...)
 */
public ReturnType solution(args) { ... }

/**
 * Follow-up 1: [harder variant]
 *   → [approach]
 *
 * Follow-up 2: [another variant]
 *   → [approach]
 */
```

---

## ⚡ Complexity Quick Reference

| Structure / Algorithm | Access | Search | Insert | Delete |
|---|---|---|---|---|
| Array | O(1) | O(n) | O(n) | O(n) |
| HashMap | — | O(1) avg | O(1) avg | O(1) avg |
| TreeMap | — | O(log n) | O(log n) | O(log n) |
| BFS / DFS | — | O(V+E) | — | — |
| Dijkstra | — | O((V+E) log V) | — | — |
| Union-Find | — | O(α(n)) ≈ O(1) | O(α(n)) | — |

---

## 🧠 Interview Pattern Cheat-Sheet

| If the problem says... | Think... |
|---|---|
| "Sorted array" + "two values sum to" | Two Pointers |
| "Contiguous subarray" + "max/min/count" | Sliding Window |
| "Subarray sum = k" (mixed +/-) | Prefix Sum + HashMap |
| "Maximum subarray sum" | Kadane's Algorithm |
| "Shortest path" (unweighted) | BFS |
| "Shortest path" (weighted, non-negative) | Dijkstra |
| "Is there a cycle?" / "connected components" | Union-Find or DFS |
| "Prerequisites" / "ordering" | Topological Sort |
| "K largest/smallest" | Min/Max Heap |
| "Sliding window maximum" | Monotonic Deque |
| "Binary search on answer" | isFeasible + Binary Search |
| "Exactly K distinct" | atMost(K) - atMost(K-1) |

---

## 🗓️ Suggested Study Schedule

| Week | Focus | Goal |
|---|---|---|
| 1 | Array (Phases 1-4) | Two pointers, prefix sum, Kadane's, binary search |
| 2 | Array (Phases 5-8) + Hashing (Phases 1-4) | Intervals, matrix, freq counting, complement lookup |
| 3 | Hashing (Phases 5-8) + Sliding Window (Phases 1-3) | Design, prefix+map, fixed/variable windows |
| 4 | Sliding Window (Phases 4-7) | Freq matching, counting, monotonic deque |
| 5 | Graph (Phases 1-4) | BFS/DFS on grids, adjacency list, topo sort, Dijkstra |
| 6 | Graph (Phases 5-8) | Union-Find, MST, Tarjan's, advanced graph |
| 7 | Mixed revision | Do 2-3 Google-level problems per topic daily |

---

## 💬 Interview Communication Template

> *"Let me classify this problem. I see [constraint/keyword]. My first instinct is [pattern].
> Let me verify:*
> - *Can I use [approach]? Does that preserve the answer?*
> - *What's the constraint that makes [brute force] O(n²)? And how does [pattern] break it?*
>
> *I'll implement [optimal approach]. Time is O([...]) because [1-sentence proof].
> Space is O([...]) because [reason]."*

---

## 📖 Resources Referenced

- *Cracking the Coding Interview* — Gayle Laakmann McDowell
- *Elements of Programming Interviews in Java* — Aziz, Lee, Prakash
- LeetCode problem set (LC# referenced in every problem)
- Java Documentation — `java.util` Collections, Arrays, PriorityQueue internals
