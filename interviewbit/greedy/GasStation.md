# InterviewBit: Gas Station

## Problem Statement

Given two integer arrays `A` and `B` of size $N$. There are $N$ gas stations along a circular route, where the amount of gas at station $i$ is `A[i]`.

You have a car with an unlimited gas tank and it costs `B[i]` of gas to travel from station $i$ to its next station $(i+1)$. You begin the journey with an empty tank at one of the gas stations.

Return the minimum starting gas station's index if you can travel around the circuit once, otherwise return `-1`.

You can only travel in one direction: $i \to i+1, i+2, \dots, N-1, 0, 1, 2, \dots$. Completing the circuit means starting at $i$ and ending up at $i$ again.

---

## Input / Output Format

- **Input:**
  - `A`: An integer array representing the amount of gas at each station.
  - `B`: An integer array representing the cost of gas to travel to the next station.
- **Output:**
  - An integer representing the 0-based minimum starting gas station index, or `-1` if no valid start index exists.

### Example 1
```text
Input:
A = [1, 2, 3, 4, 5]
B = [3, 4, 5, 1, 2]

Output:
3

Explanation:
Start at station 3 (index 3). Initial tank = 0.
- Station 3: Fill A[3] = 4 gas. Cost to station 4 is B[3] = 1. Tank balance = 4 - 1 = 3.
- Station 4: Fill A[4] = 5 gas. Total tank = 3 + 5 = 8. Cost to station 0 is B[4] = 2. Tank balance = 8 - 2 = 6.
- Station 0: Fill A[0] = 1 gas. Total tank = 6 + 1 = 7. Cost to station 1 is B[0] = 3. Tank balance = 7 - 3 = 4.
- Station 1: Fill A[1] = 2 gas. Total tank = 4 + 2 = 6. Cost to station 2 is B[1] = 4. Tank balance = 6 - 4 = 2.
- Station 2: Fill A[2] = 3 gas. Total tank = 2 + 3 = 5. Cost to station 3 is B[2] = 5. Tank balance = 5 - 5 = 0.
Circuit completed! Start index is 3.
```

### Example 2
```text
Input:
A = [2, 3, 4]
B = [3, 4, 3]

Output:
-1

Explanation:
Total gas = 2 + 3 + 4 = 9. Total cost = 3 + 4 + 3 = 10.
Total gas (9) < Total cost (10), so completing the circuit is impossible.
```

---

## Problem Analysis & Mathematical Proof

### 1. Global Feasibility Condition
A valid circuit around all $N$ stations requires the total gas available across all stations to be at least the total gas cost required:
$$\sum_{i=0}^{N-1} A[i] \ge \sum_{i=0}^{N-1} B[i]$$

- **If $\sum A[i] < \sum B[i]$**: It is mathematically impossible to complete the loop from any station. Return `-1`.
- **If $\sum A[i] \ge \sum B[i]$**: At least one valid starting station is guaranteed to exist.

### 2. Greedy Elimination Property (Single Pass Selection)
Define the net gas gain at station $i$ as:
$$\Delta[i] = A[i] - B[i]$$

Suppose we start at candidate index $S$ with an empty tank ($\text{tank} = 0$) and travel up to station $K$ ($K \ge S$). If at station $K$, our tank balance drops below zero when attempting to reach $K+1$:
$$\sum_{j=S}^{K} \Delta[j] < 0$$

#### Proof: No station between $S$ and $K$ (inclusive) can be a valid starting point.
- Consider any intermediate station $m$ where $S \le m \le K$.
- Since we successfully reached station $m$ starting from $S$, the accumulated tank at station $m$ must have been non-negative:
  $$\sum_{j=S}^{m-1} \Delta[j] \ge 0$$
- We know:
  $$\sum_{j=S}^{K} \Delta[j] = \left(\sum_{j=S}^{m-1} \Delta[j]\right) + \left(\sum_{j=m}^{K} \Delta[j]\right) < 0$$
- Subtracting $\sum_{j=S}^{m-1} \Delta[j]$ (which is $\ge 0$) from both sides gives:
  $$\sum_{j=m}^{K} \Delta[j] < 0 - \sum_{j=S}^{m-1} \Delta[j] \le 0$$
- This implies starting at station $m$ with an empty tank will also result in a deficit at or before station $K$.

#### Greedy Deduction:
Whenever $\text{currentTank} < 0$ at station $i$:
1. Eliminate all starting indices from $S$ to $i$.
2. Reset candidate starting index to $i + 1$.
3. Reset `currentTank = 0`.

---

## Java Implementation

```java
package interviewbit.greedy;

public class GasStation {

    public int canCompleteCircuit(final int[] A, final int[] B) {
        if (A == null || B == null || A.length == 0 || B.length == 0 || A.length != B.length) {
            return -1;
        }

        int totalGas = 0;
        int totalCost = 0;
        int currentTank = 0;
        int startIndex = 0;

        for (int i = 0; i < A.length; i++) {
            totalGas += A[i];
            totalCost += B[i];
            currentTank += A[i] - B[i];

            if (currentTank < 0) {
                startIndex = i + 1;
                currentTank = 0;
            }
        }

        return (totalGas >= totalCost) ? startIndex : -1;
    }
}
```

---

## Complexity Analysis

| Metric | Complexity | Explanation |
| :--- | :--- | :--- |
| **Time Complexity** | $\mathcal{O}(N)$ | Single linear iteration through array `A` and `B`. |
| **Space Complexity** | $\mathcal{O}(1)$ | Constant extra space for scalar variables (`totalGas`, `totalCost`, `currentTank`, `startIndex`). |

---

## Edge Cases Handled

1. **`A.length != B.length` or empty/null arrays:** Returns `-1`.
2. **Single station $N = 1$:** Correctly checks if `A[0] >= B[0]`.
3. **Exact balance $\sum A[i] == \sum B[i]$:** Handles exact zero-margin circuits.
4. **Multiple valid starts:** Returns the minimum index due to greedy resetting.
