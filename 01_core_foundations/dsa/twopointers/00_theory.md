# Two Pointers — Theory, Internals & Complexity Proofs
> **Study goal:** Understand *why* two pointers works, when to use it, and how to pick the right variant.
> Google interviewers expect you to immediately identify two-pointer problems and articulate the O(n) proof.

---

## 1. What Is the Two Pointers Technique?

Two Pointers uses **two indices** (`left` and `right`) that move through data in a coordinated way.
Instead of examining every pair (O(n²)), we exploit **monotonicity** to eliminate candidates in O(1).

```
Opposite-end pointers (squeeze inward):
Array:  [1, 2, 3, 4, 6]    Target sum = 6
         L              R
         ↑              ↑

Step 1:  sum = 1 + 6 = 7 > 6  → move R left
Step 2:  sum = 1 + 4 = 5 < 6  → move L right
Step 3:  sum = 2 + 4 = 6 == 6 → FOUND ✓
```

```
Same-direction pointers (fast/slow):
Array:  [1, 1, 2, 3, 3, 4]    Remove duplicates in-place
         S
         F

slow = write head, fast = read head
When arr[fast] != arr[slow], write arr[fast] to arr[++slow]
```

**Key insight:** Each pointer moves at most n times → at most 2n steps total → **O(n)**.

---

## 2. When to Use Two Pointers

### The Signal
> If the problem involves a **sorted array (or can be sorted)** and asks for **pairs, triplets, or partitions**
> satisfying some condition (sum, difference, product), think two pointers.

### Decision Tree
```
Is data sorted (or can it be sorted without losing generality)?
 ├── NO
 │    ├── Does it involve a linked list with cycle/midpoint detection?
 │    │    └── YES → Fast/Slow pointers (Floyd's algorithm)
 │    ├── Does it involve in-place array modification (partition)?
 │    │    └── YES → Same-direction read/write pointers
 │    └── Does it involve a palindrome check?
 │         └── YES → Opposite-end pointers
 │
 └── YES
      ├── Finding a PAIR with target sum?
      │    └── Opposite-end squeeze (left from start, right from end)
      ├── Finding TRIPLETS/QUADRUPLETS?
      │    └── Fix outer element(s) + two-pointer on remainder
      ├── Removing duplicates / partitioning?
      │    └── Same-direction read/write pointers
      └── Two sorted arrays (merge, intersection)?
           └── Merge-style two pointers
```

### Two Pointers vs Other Patterns

| Problem Type | Pattern | Why |
|---|---|---|
| Pair sum = target in sorted array | Two Pointers (opposite) | Sorted → monotonic → can eliminate half |
| Pair sum = target in unsorted array | HashMap | No monotonicity to exploit |
| Subarray sum = target (positive values) | Sliding Window | Contiguous subarray |
| Subarray sum = target (mixed +/-) | Prefix Sum + HashMap | Non-monotonic |
| Detect cycle in linked list | Fast/Slow Pointers | Not about pairs/sums |
| k-th element of two sorted arrays | Binary Search | O(log n) possible |
| Remove duplicates in-place | Same-direction Two Pointers | Read/write heads |
| Container with Most Water | Opposite Two Pointers | Greedy: move smaller height |

---

## 3. Time & Space Complexity Proofs

### Theorem: Opposite-End Two Pointers is O(n)

**Proof (Aggregate Method):**
```
Let L = left pointer (starts at 0), R = right pointer (starts at n-1).
- L can only move RIGHT: at most n moves.
- R can only move LEFT:  at most n moves.
- L and R never cross (algorithm terminates when L >= R).
- Total pointer movements ≤ n (for L) + n (for R) = 2n.
- Each movement does O(1) work.
- Total work = O(2n) = O(n).
```

**Why can we always move one pointer?**
```
Array sorted: a[0] ≤ a[1] ≤ ... ≤ a[n-1]
At any step: sum = a[L] + a[R]
- If sum > target: a[R] is too large. Any pair with a[R] and a[j] where j < R
  also has sum > target (since a[j] ≥ a[L] ≥ a[L]). So we can SAFELY eliminate R.
- If sum < target: a[L] is too small. Any pair with a[L] and a[j] where j > L
  also has sum < target (since a[j] ≤ a[R] ≤ a[R]). So we can SAFELY eliminate L.
- If sum == target: FOUND.
```

> **Interview script:** "Left and right pointers each move at most n times and never reverse,
> so we have at most 2n movements, each O(1) — giving O(n) total time."

### Theorem: 3Sum is O(n²)

```
Outer loop: O(n) choices for the fixed element nums[i].
Inner two-pointer pass: O(n) per fixed element.
Total: O(n) × O(n) = O(n²).
Cannot do better than O(n²) for 3Sum in the comparison model (proven lower bound).
```

### Space Complexity

| Variant | Space | Why |
|---|---|---|
| Opposite-end pair sum | O(1) | Only two indices |
| 3Sum / 4Sum | O(1) extra (O(n) for sort) | Only indices + loop vars |
| Fast/Slow (linked list) | O(1) | Two pointers in-place |
| Merge two sorted arrays | O(n) | Output array |
| Dutch National Flag (partition) | O(1) | In-place swap |

---

## 4. The Five Core Templates

### Template 1: Opposite-End — Find Pair with Target Sum

```java
/**
 * Use when: sorted array, find pair summing to target.
 * Pattern: Start from both ends, squeeze inward.
 */
int[] twoSum(int[] nums, int target) {
    int left = 0, right = nums.length - 1;

    while (left < right) {
        int sum = nums[left] + nums[right];
        if (sum == target) {
            return new int[]{left, right};
        } else if (sum < target) {
            left++;   // need larger sum → advance left
        } else {
            right--;  // need smaller sum → retreat right
        }
    }
    return new int[]{-1, -1};  // not found
}
```

### Template 2: Fix + Two Pointers (3Sum / kSum)

```java
/**
 * Use when: find triplets/quadruplets summing to target.
 * Pattern: Sort, fix outer elements, run two-pointer on rest.
 * Critical: Skip duplicates at EVERY level.
 */
List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();

    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue; // skip duplicate fixed element

        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                while (left < right && nums[left] == nums[left + 1]) left++;   // skip dup
                while (left < right && nums[right] == nums[right - 1]) right--; // skip dup
                left++;
                right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
```

### Template 3: Same-Direction — Read/Write (Remove Duplicates / Partition)

```java
/**
 * Use when: modify array in-place (remove elements, partition).
 * Pattern: slow = write head, fast = read head.
 *          Only advance slow when a "valid" element is found.
 */
int removeDuplicates(int[] nums) {
    if (nums.length == 0) return 0;
    int slow = 0;  // write pointer (last written position)

    for (int fast = 1; fast < nums.length; fast++) {
        if (nums[fast] != nums[slow]) {
            slow++;
            nums[slow] = nums[fast]; // write valid element
        }
    }
    return slow + 1; // new length
}
```

### Template 4: Fast/Slow — Linked List Cycle / Midpoint

```java
/**
 * Use when: detect cycle, find midpoint, or find k-th from end.
 * Pattern: slow moves 1 step, fast moves 2 steps.
 *          They meet iff there's a cycle (Floyd's algorithm).
 */
boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;  // cycle detected
    }
    return false;
}

// Find midpoint (for merge sort, palindrome check):
ListNode findMid(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow; // slow is at midpoint
}
```

### Template 5: Merge Two Sorted Arrays / Pointers in Two Arrays

```java
/**
 * Use when: combine, intersect, or compare two sorted sequences.
 * Pattern: One pointer per array, advance the smaller one.
 */
int[] mergeSorted(int[] a, int[] b) {
    int i = 0, j = 0, k = 0;
    int[] result = new int[a.length + b.length];

    while (i < a.length && j < b.length) {
        if (a[i] <= b[j]) {
            result[k++] = a[i++];
        } else {
            result[k++] = b[j++];
        }
    }
    while (i < a.length) result[k++] = a[i++];
    while (j < b.length) result[k++] = b[j++];
    return result;
}
```

---

## 5. The Duplicate Skipping Pattern

A critical and commonly missed detail in 3Sum / kSum is **skipping duplicates at every level**.
Failing to do this produces duplicate triplets in output.

```
nums = [-2, -2, 0, 0, 2, 2]    Target = 0

Without duplicate skipping:
  Fix i=0 (nums[0]=-2): finds (-2, 0, 2) ← correct
  Fix i=1 (nums[1]=-2): finds (-2, 0, 2) ← DUPLICATE!

With duplicate skipping:
  Fix i=0: process normally, add (-2, 0, 2)
  Fix i=1: nums[1] == nums[0], SKIP

After finding a valid triplet, also skip duplicates for left/right:
  left and right both advance past equal neighbors.
```

**Rule of thumb:**
- Outer loop: `if (i > 0 && nums[i] == nums[i-1]) continue;`
- After match: advance left/right past duplicates *before* the final `left++; right--;`

---

## 6. Advanced Technique: Dutch National Flag (3-Way Partition)

Used in **Sort Colors** (LC 75). Partitions an array into 3 groups in O(n), O(1) space.

```java
/**
 * Three pointers: low (0-boundary), mid (current), high (2-boundary).
 * Elements left of low are 0s, right of high are 2s, between are 1s.
 *
 * Invariants:
 *   [0 .. low-1]  = all 0s
 *   [low .. mid-1] = all 1s
 *   [mid .. high]  = unsorted
 *   [high+1 .. n-1] = all 2s
 */
void sortColors(int[] nums) {
    int low = 0, mid = 0, high = nums.length - 1;

    while (mid <= high) {
        if (nums[mid] == 0) {
            swap(nums, low++, mid++);  // 0 goes to front, advance both
        } else if (nums[mid] == 1) {
            mid++;                     // 1 is in place
        } else {
            swap(nums, mid, high--);   // 2 goes to back, DON'T advance mid (re-check swapped)
        }
    }
}
```

**Why NOT advance mid after swapping with high?**
```
When we swap nums[mid] with nums[high], the element coming from high
has NOT been classified yet. We must re-examine it at nums[mid].
When we swap nums[mid] with nums[low], the element coming from low is
guaranteed to be a 1 (since low..mid-1 are all 1s), so mid can advance.
```

---

## 7. Common Pitfalls

| Pitfall | Fix |
|---|---|
| Using two pointers on unsorted array for pair sum | Sort first (O(n log n)) or use HashMap |
| Not skipping duplicates in 3Sum | Skip at outer loop: `if (i>0 && nums[i]==nums[i-1]) continue` |
| Not skipping inner duplicates after match | Advance `left`/`right` past equal neighbors |
| Forgetting `left < right` guard when skipping | Always check `left < right` inside skip loops |
| Fast/slow: checking `fast != null` but not `fast.next` | Must check both: `fast != null && fast.next != null` |
| Dutch Flag: advancing `mid` after swap with `high` | Don't advance mid; re-examine the swapped element |
| Integer overflow in sum comparison | Use `long` or rewrite as `target - a[left]` |

---

## 8. Two Pointers vs Sliding Window — Know the Difference

```
Two Pointers:
  - Data need NOT be contiguous between the two pointers
  - Classic use: sorted arrays, pairs/triplets, in-place operations
  - Window can SHRINK from BOTH ends independently

Sliding Window:
  - Window is always CONTIGUOUS (left to right)
  - Classic use: subarray optimization (sum, length, count)
  - Left pointer only advances to shrink; right pointer only advances to grow
```

> **One-liner test:** "If the problem is about non-contiguous pairs/indices or in-place
> modification, think Two Pointers. If it's about a contiguous subarray/substring, think Sliding Window."

---

## 9. Interview Complexity Vocabulary

> *"I recognize this as a two-pointer problem because the array is sorted (or can be sorted),
> and I need to find pairs satisfying a constraint. I'll place one pointer at each end and squeeze inward.
> When the sum is too large, I move right left (decreasing the larger element).
> When the sum is too small, I move left right (increasing the smaller element).
> This gives O(n) time — each pointer moves at most n steps — and O(1) space."*

---

*Next: → [01_easy.java](01_easy.java) — Easy problems applying these patterns*
