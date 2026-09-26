# Greedy Algorithms — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet for greedy problems.

---

## Pattern Recognition Table

| If the problem mentions... | Think... | Key Technique |
|---|---|---|
| "Intervals", "Max non-overlapping" | Interval Scheduling | Sort by **end time** `interval[1]` |
| "Minimum arrows to burst balloons" | Overlapping Intervals | Sort by **end coordinate** |
| "Assign items to demands" | Two-Pointer Matching | Sort both, match smallest valid |
| "Stock buy and sell II" | Sum Positive Local Differences | Sum `prices[i] - prices[i-1]` if positive |
| "Gas station circuit" | Single Pass Range Elimination | `totalGas >= totalCost` + reset start on negative tank |
| "Jump Game reachability" | Max Boundary Tracking | Track `maxReach = max(maxReach, i + nums[i])` |
| "Task scheduler with idle time" | Frequency Slot Counting | `(maxFreq - 1) * (n + 1) + maxFreqCount` |
| "Partition labels in string" | Last Index Boundary | `end = max(end, last[char])` |
| "Remove K digits for min number" | Monotonic Stack | Pop `stack.top > current_digit` while `k > 0` |
| "Candy distribution to neighbors" | Two-Pass Bidirectional | Left-to-right pass, then right-to-left pass |
| "Course schedule III / evict longest" | Max-Heap Replacement | Sort by deadline, evict max duration on overflow |
| "IPO / Max capital with prerequisites" | Dual Heap Greedy | Min-Capital Heap $\to$ Max-Profit Heap |
| "Hire K workers minimum cost" | Wage/Quality Ratio + Min-Heap | Sort by ratio, track top $k$ smallest qualities |

---

## The Four Templates (Memorize These)

### Template 1: Interval Scheduling (Sort by End Time)

```java
Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));
int count = 0, prevEnd = Integer.MIN_VALUE;

for (int[] interval : intervals) {
    if (interval[0] >= prevEnd) {
        count++;
        prevEnd = interval[1];
    }
}
```

### Template 2: Two-Pointer Matching (Sort Both)

```java
Arrays.sort(demands);
Arrays.sort(supplies);
int i = 0, j = 0;

while (i < demands.length && j < supplies.length) {
    if (supplies[j] >= demands[i]) {
        i++; // Satisfied
    }
    j++; // Consume supply
}
```

### Template 3: Monotonic Stack Digit Optimization

```java
StringBuilder stack = new StringBuilder();
for (char ch : num.toCharArray()) {
    while (stack.length() > 0 && stack.charAt(stack.length() - 1) > ch && k > 0) {
        stack.deleteCharAt(stack.length() - 1);
        k--;
    }
    stack.append(ch);
}
```

### Template 4: Dual Heap Priority Queue Greedy (e.g., IPO)

```java
Arrays.sort(projects, (a, b) -> Integer.compare(a.minRequirement, b.minRequirement));
PriorityQueue<Integer> maxProfitHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));

int i = 0;
for (int step = 0; step < k; step++) {
    while (i < n && projects[i].minRequirement <= currentBudget) {
        maxProfitHeap.offer(projects[i].profit);
        i++;
    }
    if (maxProfitHeap.isEmpty()) break;
    currentBudget += maxProfitHeap.poll();
}
```

---

## 30-Second Interview Proof Mental Checklist

1. **Greedy Choice Property:** "Making locally optimal choice $X$ now never forces a sub-optimal outcome later because [Exchange Argument]."
2. **Optimal Substructure:** "After taking $X$, the remaining state is identical to a smaller subproblem of the same form."
3. **Counter-Example Check:** "Does 0/1 Knapsack or negative weights break this? No, because elements are divisible / unconstrained / sorted."
