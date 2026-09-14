# Recursion & Backtracking — Theory, Internals & Pruning Strategies
> **Study goal:** Master recursion at the call-stack level and backtracking at the search-tree pruning level.
> Google interviewers expect you to formally analyze state-space tree depth/branching factor, handle duplicate elements cleanly, and optimize state using bitmasks.

---

## 1. What Is Recursion & How Does the JVM Execute It?

**Recursion** is a programming paradigm where a function calls itself to solve smaller instances of the exact same problem.

### Call Stack Execution Mechanics
Every recursive call pushes a new **Activation Record (Stack Frame)** onto the JVM Thread Stack containing:
1. Local variables and parameters.
2. Return address (instruction pointer).
3. Operand stack state.

```
JVM Thread Stack Execution for factorial(3):

│  factorial(1) -> returns 1           │  [Top of Stack - Popped]
├──────────────────────────────────────┤
│  factorial(2) -> waits for (1) * 2   │  [Active Frame]
├──────────────────────────────────────┤
│  factorial(3) -> waits for (2) * 3   │  [Waiting Frame]
├──────────────────────────────────────┤
│  main()                              │  [Base Frame]
└──────────────────────────────────────┘
```

- **StackOverflowError:** Occurs when recursion depth exceeds thread stack size (`-Xss`, typically 1024KB), failing to reach a base case.
- **Tail Call Optimization (TCO):** Compiler optimization where the final action of a function is a recursive call, reusing the existing stack frame. *Note: Java HotSpot JVM does NOT support TCO natively.*

---

## 2. What Is Backtracking?

**Backtracking** is a systematic algorithm for exploring all potential configuration choices in a **State Space Tree** using Depth-First Search (DFS).

When a candidate path violates problem constraints (or after exploring a full path), the algorithm **backtracks** (undoes the last choice) and tries the next available choice.

```
                    State Space Tree (Subsets of [1, 2]):

                                  root ()
                                /         \
                         Include 1       Exclude 1
                          /    \          /    \
                       (1,2)   (1)      (2)    ()
```

---

## 3. The Golden Backtracking Blueprint

Every backtracking algorithm follows the exact same 3-step paradigm:

```java
void backtrack(State state, Choice[] choices) {
    // 1. BASE CASE / SOLUTION FOUND
    if (isSolution(state)) {
        result.add(new ArrayList<>(state)); // Deep copy state!
        return;
    }

    for (Choice choice : choices) {
        // 2. PRUNING / CONSTRAINT CHECK
        if (!isValid(choice, state)) continue;

        // 3. CHOOSE (Apply choice to state)
        state.add(choice);

        // 4. EXPLORE (Recurse to next depth)
        backtrack(state, remainingChoices);

        // 5. UN-CHOOSE / BACKTRACK (Undo choice to restore state)
        state.remove(state.size() - 1);
    }
}
```

> [!IMPORTANT]
> **Deep Copy Rule:** In Java, collections (like `ArrayList`) are passed by reference. When adding a valid path to `result`, you MUST store a deep copy: `result.add(new ArrayList<>(currentPath))`. Storing `result.add(currentPath)` will store empty lists because backtracking mutates `currentPath` back to empty!

---

## 4. Duplicate Element Handling (The `i > start && nums[i] == nums[i-1]` Trick)

When input arrays contain duplicate numbers (e.g., `Subsets II`, `Permutations II`, `Combination Sum II`), naive backtracking generates duplicate solution paths.

### Sorting + Skip Sister Branch Strategy:
1. **Sort the array first:** `Arrays.sort(nums)`.
2. **Skip duplicate choices at the same recursion depth:**
   ```java
   for (int i = start; i < nums.length; i++) {
       // Skip duplicates: if current element equals previous element AT THE SAME DEPTH LEVEL
       if (i > start && nums[i] == nums[i - 1]) continue;

       current.add(nums[i]);
       backtrack(i + 1, current);
       current.remove(current.size() - 1);
   }
   ```

---

## 5. Pruning Techniques (Optimizing $O(B^D)$ Complexity)

The time complexity of raw backtracking is $O(B^D)$ where $B$ is the branching factor and $D$ is maximum tree depth. Pruning cuts unviable branches early:

1. **Constraint Pruning (Bounding Functions):** Abort recursion immediately if `currentSum > target` (e.g., Combination Sum).
2. **Symmetry Breaking:** Enforce non-decreasing choice order (`i = start`) to prevent permutation duplicates when finding combinations.
3. **Bitmasking (Fast Diagonal/Row/Col Checks):** Use bit flags `(cols & (1 << c))` for $O(1)$ collision checks (e.g., N-Queens, Sudoku).
4. **Sorted Early Termination:** Sort elements descending when fitting into bins (e.g., Partition to K Equal Sum Subsets / Matchsticks to Square). Placing larger elements first fails faster on unviable branches!

---

## 6. Decision Matrix: Backtracking vs Dynamic Programming vs Greedy

```
                        Does the problem ask for ALL solutions or ONE optimal?
                                   /                       \
                            ALL SOLUTIONS                  ONE OPTIMAL
                                 /                             \
                          Use Backtracking             Are there overlapping
                       Time: O(2ⁿ) or O(N!)           subproblems & optimal
                                                      substructure?
                                                      /           \
                                                    YES            NO
                                                    /                \
                                           Use DP (O(N²))     Use Greedy (O(N log N))
```
