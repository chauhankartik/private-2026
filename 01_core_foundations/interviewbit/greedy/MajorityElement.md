# InterviewBit: Majority Element

## Problem Statement

Given an integer array `A` of size $N$, find the majority element.

The majority element is defined as the element that appears **more than $\lfloor N / 2 \rfloor$ times**.

You may assume that the array is non-empty and that the majority element **always exists** in the array.

---

## Constraints

- $1 \le |A| \le 10^6$
- $1 \le A[i] \le 10^9$

---

## Input / Output Format

- **Input:** An integer array `A`.
- **Output:** An integer representing the majority element value.

### Example 1
```text
Input:
A = [3, 2, 3]

Output:
3

Explanation:
N = 3. floor(3 / 2) = 1.
Frequency of 3 is 2, which is > 1.
```

### Example 2
```text
Input:
A = [2, 2, 1, 1, 1, 2, 2]

Output:
2

Explanation:
N = 7. floor(7 / 2) = 3.
Frequency of 2 is 4, which is > 3.
```

---

## Algorithm Options & Trade-offs

| Approach | Algorithm | Time Complexity | Space Complexity | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Approach 1** | HashMap Frequency Count | $\mathcal{O}(N)$ | $\mathcal{O}(N)$ | Extra memory required for hash table storage. |
| **Approach 2** | Sorting | $\mathcal{O}(N \log N)$ | $\mathcal{O}(1)$ or $\mathcal{O}(N)$ | Sub-optimal time complexity. |
| **Approach 3 (Optimal)** | **Boyer-Moore Voting Algorithm** | $\mathcal{O}(N)$ | $\mathcal{O}(1)$ | Optimal linear time and constant memory. |

---

## Detailed Analysis: Boyer-Moore Voting Algorithm

### Intuition & Mathematical Proof

The Boyer-Moore Majority Vote algorithm works by **pairwise cancellation** of non-matching elements.

1. **Invariance Property:**
   - Suppose element $M$ is the majority element. Its count $c_M > \lfloor N / 2 \rfloor$.
   - The total count of all other non-majority elements combined is $N - c_M < \lfloor N / 2 \rfloor$.
   - Therefore: $c_M > N - c_M \implies c_M - (N - c_M) \ge 1$.

2. **Pairwise Pair Elimination:**
   - Every time we pair up two different numbers (e.g., $M$ and $X$ where $X \ne M$), we decrement the count of both.
   - Even in the absolute worst-case scenario where every non-majority element cancels out one occurrence of the majority element, at least $c_M - (N - c_M) \ge 1$ occurrences of $M$ will remain unpaired.

3. **Algorithm Steps:**
   - Initialize `candidate = A[0]` and `count = 0`.
   - Iterate through array `A`:
     - If `count == 0`: set `candidate = num` and `count = 1`.
     - Else if `num == candidate`: `count++`.
     - Else: `count--`.
   - Return `candidate`.

---

## Example Walkthrough

Array: `A = [2, 2, 1, 1, 1, 2, 2]`

| Step | Current Element | Candidate | Count | Action |
| :---: | :---: | :---: | :---: | :--- |
| 1 | `2` | `2` | `1` | `count == 0` -> Set candidate = 2, count = 1 |
| 2 | `2` | `2` | `2` | Match candidate -> `count++` |
| 3 | `1` | `2` | `1` | Mismatch -> `count--` |
| 4 | `1` | `2` | `0` | Mismatch -> `count--` |
| 5 | `1` | `1` | `1` | `count == 0` -> Set candidate = 1, count = 1 |
| 6 | `2` | `1` | `0` | Mismatch -> `count--` |
| 7 | `2` | `2` | `1` | `count == 0` -> Set candidate = 2, count = 1 |

Final Candidate: `2`. Frequency of `2` is $4 > \lfloor 7/2 \rfloor = 3$.

---

## Java Implementation

```java
package interviewbit.greedy;

public class MajorityElement {

    public int majorityElement(final int[] A) {
        if (A == null || A.length == 0) {
            throw new IllegalArgumentException("Input array must be non-empty.");
        }

        int candidate = A[0];
        int count = 0;

        for (int num : A) {
            if (count == 0) {
                candidate = num;
                count = 1;
            } else if (num == candidate) {
                count++;
            } else {
                count--;
            }
        }

        return candidate;
    }
}
```

---

## Complexity Analysis

- **Time Complexity:** $\mathcal{O}(N)$ — single pass over array `A` of size $N$.
- **Space Complexity:** $\mathcal{O}(1)$ — constant scalar variables (`candidate`, `count`).

---

## Edge Cases Handled

1. **Single element array ($N = 1$):** Directly returns `A[0]`.
2. **All elements identical:** `count` increments up to $N$, returning `A[0]`.
3. **Alternating patterns:** Handled correctly as majority element frequency guarantees surplus count.
4. **Large inputs ($10^6$ elements):** Runs in milliseconds with $O(1)$ space overhead.
