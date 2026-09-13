# Greedy Algorithms — Theory, Internals & Proof Techniques
> **Study goal:** Master greedy algorithms at a foundational level.
> Google interviewers expect formal justification of why a locally optimal choice guarantees a globally optimal solution, as well as counter-examples showing when greedy fails.

---

## 1. What Is a Greedy Algorithm?

A **Greedy Algorithm** makes the **locally optimal choice** at each step in the hope that these local choices lead to a **globally optimal solution**.

Unlike Dynamic Programming (which systematically evaluates all subproblems and memoizes results) or Backtracking (which explores a search tree and backtracks on failure), a greedy algorithm **never reconsiders its choices**. Once a decision is made, it is permanent.

```
Decision Strategy Comparison:
┌─────────────────────┬──────────────────────────────────────────┬────────────────────────┐
│ Paradigm            │ Choice Strategy                          │ Backtracking / Revisit │
├─────────────────────┼──────────────────────────────────────────┼────────────────────────┤
│ Greedy              │ Locally optimal choice at each step     │ NO                     │
│ Dynamic Programming │ Explores all choices, memoizes subproblems│ NO (uses table)        │
│ Backtracking        │ Explores choices, prunes bad branches    │ YES                    │
└─────────────────────┴──────────────────────────────────────────┴────────────────────────┘
```

---

## 2. Core Theoretical Properties

For a greedy algorithm to yield an optimal solution, the problem must exhibit two key properties:

### 1. Greedy Choice Property
A globally optimal solution can be arrived at by making locally optimal (greedy) choices without considering future choices or subproblem solutions.
- **Key Insight:** You can commit to a choice right now because no future choice can make a different current choice superior.

### 2. Optimal Substructure
An optimal solution to the problem contains optimal solutions to its subproblems.
- If $S^*$ is an optimal solution to problem $P$, and making greedy choice $g$ reduces $P$ to subproblem $P'$, then combining $g$ with the optimal solution to $P'$ yields an optimal solution $S^*$.

---

## 3. Formal Proof Frameworks

In Google technical interviews, stating "I'm using a greedy choice because it seems intuitively right" is insufficient. You must be prepared to outline one of two formal proof techniques:

### A. Exchange Argument (Proof by Contradiction)
**Goal:** Prove that any optimal solution $O$ can be transformed into the greedy solution $G$ without degrading solution quality.

**Steps:**
1. Assume an optimal solution $O$ exists that differs from greedy solution $G$.
2. Find the first point of divergence where $O$ makes a non-greedy choice.
3. **Exchange** the non-greedy choice in $O$ with the greedy choice.
4. Prove that this exchange results in a new solution $O'$ that is **at least as good** as $O$ ($\text{cost}(O') \le \text{cost}(O)$ or $\text{val}(O') \ge \text{val}(O)$).
5. Inductively repeat until $O$ is transformed into $G$, proving $G$ is optimal.

### B. Stays Ahead Argument (Inductive Proof)
**Goal:** Prove that at every step $k$, the greedy algorithm is at least as far advanced as any other valid algorithm.

**Steps:**
1. Define a measure of progress $f(k)$ after $k$ choices (e.g., total finish time, number of items selected, current capacity).
2. **Base Case:** Prove $f_G(1) \ge f_O(1)$ for step 1.
3. **Inductive Step:** Assume $f_G(k) \ge f_O(k)$. Prove that choice $k+1$ maintains $f_G(k+1) \ge f_O(k+1)$.
4. Conclude that after $N$ steps, $G$ is globally optimal.

---

## 4. The 6 Core Greedy Patterns

### Pattern 1: Interval Scheduling / Merging (Sorting by Finish Time)
- **Problem Type:** Select max non-overlapping intervals or count minimum overlaps.
- **Greedy Rule:** Sort by **end time** `interval[1]`. Pick interval that finishes earliest.
- **Why End Time?** Finishing earlier leaves maximum available time for remaining intervals.

### Pattern 2: Two-Pointer Greedy (Pairing & Matching)
- **Problem Type:** Match elements from two arrays to maximize/minimize total utility (e.g., Assign Cookies, Boat Rescue).
- **Greedy Rule:** Sort both arrays. Use two pointers (`i`, `j`) to pair smallest/largest satisfies constraint.

### Pattern 3: PriorityQueue / Heap Greedy (Dynamic Selection)
- **Problem Type:** Dynamically track highest/lowest value item as choices unfold (e.g., Task Scheduler, Course Schedule III, IPO).
- **Greedy Rule:** Use Max-Heap or Min-Heap to query optimal element in $O(\log N)$ time per step.

### Pattern 4: Monotonic Stack Greedy (Digit & String Optimization)
- **Problem Type:** Remove $K$ digits to form smallest number, or pick lexicographically largest subsequence.
- **Greedy Rule:** Maintain a monotonic increasing stack. Remove previous larger elements while $K > 0$.

### Pattern 5: Two-Pass / Bidirectional Greedy (Constraint Propagation)
- **Problem Type:** Constraints depend on neighbors from both left and right (e.g., Candy problem, Trapping Rain Water).
- **Greedy Rule:** Pass left-to-right to satisfy left constraints, then pass right-to-left to satisfy right constraints. Combine using $\max()$.

### Pattern 6: Math & Ratio-Based Greedy (Fractional Knapsack / Efficiency)
- **Problem Type:** Select items to maximize yield per unit weight/cost (e.g., Maximum Performance of Team, Minimum Cost to Hire Workers).
- **Greedy Rule:** Sort by efficiency ratio $\frac{\text{value}}{\text{cost}}$ or fix worker quality/speed ratio.

---

## 5. Decision Framework: Greedy vs DP vs Backtracking

```
                             Is there optimal substructure?
                                    /           \
                                  NO             YES
                                 /                 \
                     Use Backtracking        Can we make local choices
                       / Exponential         without exploring future?
                                              /             \
                                            YES              NO
                                            /                  \
                                   Use Greedy (O(N log N))   Use DP (O(N²))
```

### When Greedy Fails (Classic Counter-example: 0/1 Knapsack)
- **Fractional Knapsack:** Items can be divided. **Greedy works** (sort by $\frac{\text{value}}{\text{weight}}$).
- **0/1 Knapsack:** Items cannot be divided. **Greedy FAILS!**
  - *Counter-example:* Capacity = 50. Item 1: ($v=60, w=10$, ratio 6). Item 2: ($v=100, w=20$, ratio 5). Item 3: ($v=120, w=30$, ratio 4).
  - Greedy picks Item 1 ($w=10$, remaining 40), then Item 2 ($w=20$, remaining 20). Total value = 160.
  - Optimal DP picks Item 2 + Item 3 ($w=50$, total value = 220).

---

## 6. Interview Vocabulary & Verbal Framework

When explaining a greedy approach to a Google interviewer:

> *"I observe that this problem has optimal substructure. Specifically, by picking [greedy choice criterion, e.g., earliest end time], we maximize the remaining budget/capacity for all subsequent choices.
> We can prove this via an Exchange Argument: if an optimal solution picked a different item, replacing it with our greedy pick can only improve or keep the remaining capacity equal.
> Therefore, sorting by [criterion] allows us to achieve an optimal solution in O(N log N) time and O(1) space."*
