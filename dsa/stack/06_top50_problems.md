# Stack — Top 50 Problems (Intuition-Building Order)
> **How to use this list:** Work through each phase in order.
> Each phase builds on the previous one. Don't skip ahead until the pattern clicks.

---

## Phase 1: Stack Fundamentals (Master LIFO and design)

*Goal: Get comfortable with stack operations, implement from scratch.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 1 | Min Stack | 155 | Medium | Two stacks: main + auxiliary tracking min |
| 2 | Implement Queue using Stacks | 232 | Easy | Two stacks, amortized O(1) transfer |
| 3 | Implement Stack using Queues | 225 | Easy | Queue rotation on push |
| 4 | Baseball Game | 682 | Easy | Stack as record keeper, simulation |
| 5 | Design Browser History | 1472 | Medium | Two stacks: back + forward |

---

## Phase 2: Parentheses & Matching (Stack for pairing)

*Goal: Master "push open, match on close" pattern.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 6 | Valid Parentheses | 20 | Easy | Push expected close, pop and compare |
| 7 | Minimum Add to Make Parentheses Valid | 921 | Medium | Count unmatched opens and closes |
| 8 | Minimum Remove to Make Valid Parentheses | 1249 | Medium | Mark invalid indices, rebuild string |
| 9 | Remove Outermost Parentheses | 1021 | Easy | Track depth, skip depth-0 brackets |
| 10 | Longest Valid Parentheses | 32 | Hard | Stack of indices, base tracking |
| 11 | Score of Parentheses | 856 | Medium | Stack: () = 1, (A) = 2×A, AB = A+B |

---

## Phase 3: Expression Evaluation (Parsing with stacks)

*Goal: Handle operators, precedence, and nested expressions.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 12 | Evaluate Reverse Polish Notation | 150 | Medium | Operand stack, pop two on operator |
| 13 | Basic Calculator | 224 | Hard | Stack saves (result, sign) on '(' |
| 14 | Basic Calculator II | 227 | Medium | Precedence: * / immediate, + - deferred |
| 15 | Decode String | 394 | Medium | Two stacks (count + string), nested |
| 16 | Number of Atoms | 726 | Hard | Stack of maps, multiply on ')' |

---

## Phase 4: Monotonic Stack — Next Greater/Smaller

*Goal: Master "pop when invariant breaks, record answer for popped".*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 17 | Next Greater Element I | 496 | Easy | Decreasing stack + HashMap |
| 18 | Next Greater Element II | 503 | Medium | Circular array: traverse 0 to 2n-1 |
| 19 | Daily Temperatures | 739 | Medium | Decreasing stack of indices, gap = i - popped |
| 20 | Online Stock Span | 901 | Medium | Decreasing stack of (price, span) pairs |
| 21 | 132 Pattern | 456 | Medium | Reverse traversal, track max popped "s2" |
| 22 | Next Greater Node in Linked List | 1019 | Medium | Convert to array, apply monotonic stack |

---

## Phase 5: Monotonic Stack — Histogram & Area

*Goal: Master "width = right_boundary - left_boundary - 1" calculation.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 23 | Largest Rectangle in Histogram | 84 | Hard | Increasing stack, sentinel bar, area on pop |
| 24 | Maximal Rectangle | 85 | Hard | Build histogram per row, apply #23 |
| 25 | Trapping Rain Water | 42 | Hard | Decreasing stack or two pointers |
| 26 | Container With Most Water | 11 | Medium | Two pointers (not stack, but related) |

---

## Phase 6: Stack Simulation (Process events with stack)

*Goal: Model "collision", "cancel", and "undo" with stack.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 27 | Asteroid Collision | 735 | Medium | Right(+) vs Left(-), compare sizes |
| 28 | Backspace String Compare | 844 | Easy | '#' = pop, compare final stacks |
| 29 | Remove All Adjacent Duplicates in String | 1047 | Easy | Matching top = pop (cancel pair) |
| 30 | Remove All Adjacent Duplicates in String II | 1209 | Medium | Stack of (char, count), pop when count == k |
| 31 | Make The String Great | 1544 | Easy | Same letter, different case = cancel |
| 32 | Removing Stars From a String | 2390 | Medium | '*' = pop previous character |

---

## Phase 7: Stack as History / Design

*Goal: Use stack to track state over time.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 33 | Maximum Frequency Stack | 895 | Hard | freq map + group-by-freq stacks |
| 34 | Design Browser History | 1472 | Medium | Two stacks: back and forward |
| 35 | Simplify Path | 71 | Medium | Parse components, '..' = pop |
| 36 | Exclusive Time of Functions | 636 | Medium | Stack of (id, start), compute durations |

---

## Phase 8: DFS with Explicit Stack

*Goal: Replace recursion with stack for tree/graph DFS.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 37 | Binary Tree Preorder Traversal | 144 | Easy | Push right first, then left |
| 38 | Binary Tree Inorder Traversal | 94 | Easy | Go left to end, then process, then right |
| 39 | Binary Tree Postorder Traversal | 145 | Easy | Preorder (root-right-left), then reverse |
| 40 | Flatten Binary Tree to Linked List | 114 | Medium | Stack-based preorder, relink nodes |
| 41 | Decode String (iterative) | 394 | Medium | Two stacks replaces recursion |

---

## Phase 9: Advanced Monotonic Stack (Contribution technique)

*Goal: Count subarrays where element is min/max using PSE/NSE.*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 42 | Sum of Subarray Minimums | 907 | Medium | PSE + NSE, contribution = val × left × right |
| 43 | Sum of Subarray Ranges | 2104 | Medium | sumMax - sumMin, double contribution |
| 44 | Sum of Total Strength of Wizards | 2281 | Hard | Contribution + prefix sum of prefix sums |
| 45 | Remove K Digits | 402 | Medium | Increasing stack, greedy removal |
| 46 | Remove Duplicate Letters | 316 | Medium | Increasing stack + last-index + inStack check |

---

## Phase 10: Google-Level Multi-Technique

*Goal: Combine stack with other patterns (greedy, hashing, design).*

| # | Problem | LC# | Difficulty | Key Concept |
|---|---|---|---|---|
| 47 | Number of Visible People in a Queue | 1944 | Hard | Decreasing stack, count pops + blocker |
| 48 | Car Fleet | 853 | Medium | Sort by position, stack by arrival time |
| 49 | Maximum Width Ramp | 962 | Medium | Decreasing stack + binary search / reverse scan |
| 50 | Odd Even Jump | 975 | Hard | Monotonic stack + TreeMap + DP |

---

## Study Roadmap

```
Week 1 (Days 1-2):  Phase 1 — Stack fundamentals (#1-#5)
Week 1 (Days 3-4):  Phase 2 — Parentheses & matching (#6-#11)
Week 1 (Day 5):     Phase 3 — Expression evaluation (#12-#16)
Week 2 (Days 1-2):  Phase 4 — Monotonic stack basics (#17-#22)
Week 2 (Days 3-4):  Phase 5 — Histogram & area (#23-#26)
Week 2 (Day 5):     Phase 6 — Stack simulation (#27-#32)
Week 3 (Days 1-2):  Phase 7 — Stack as history (#33-#36)
Week 3 (Day 3):     Phase 8 — Explicit DFS (#37-#41)
Week 3 (Days 4-5):  Phase 9 — Contribution technique (#42-#46)
Week 4 (Days 1-2):  Phase 10 — Google-level (#47-#50)
```

### How to Practice Each Problem

```
1. Read the problem. Classify it (monotonic/matching/eval/simulation/history).
2. IDENTIFY THE INVARIANT: what order does the stack maintain? why?
3. DETERMINE DIRECTION: left-to-right or right-to-left? why?
4. KNOW WHAT TO STORE: indices or values? pairs (val, count)?
5. Code the solution WITHOUT looking at hints.
6. Verify: Can you explain why each element is pushed/popped once?
7. Do the follow-ups. They build deeper intuition.
```

---

## Quick Pattern Checklist (Before Coding)

- [ ] Am I looking for **next greater / next smaller** element? → **Monotonic Stack**
- [ ] Am I **matching brackets / parentheses**? → **Stack (push expected close)**
- [ ] Am I **evaluating an expression** with operators? → **Stack (operand/operator)**
- [ ] Am I **simulating collisions / cancellations**? → **Stack Simulation**
- [ ] Do I need **O(1) min/max** alongside push/pop? → **Auxiliary Min/Max Stack**
- [ ] Am I computing **area** in a histogram? → **Monotonic Increasing Stack**
- [ ] Am I counting **subarrays where element is min/max**? → **Contribution Technique**
- [ ] Am I **decoding nested structures** like k[...]? → **Two Stacks (count + string)**
- [ ] Am I doing **iterative DFS / tree traversal**? → **Explicit Stack**
- [ ] Do I need the **lexicographically smallest** result? → **Monotonic Stack + Greedy**
