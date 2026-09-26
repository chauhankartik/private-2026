# Two Pointers — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "pair sum = target" in sorted array | Opposite-End Two Pointers |
| "pair sum = target" in unsorted array | HashMap (not two pointers) |
| "triplets / quadruplets summing to target" | Sort + Fix + Two Pointers |
| "closest pair / triplet sum to target" | Sort + Fix + Two Pointers + track min diff |
| "count pairs/triplets with sum < / > target" | Sort + Fix + Two Pointers + bulk count |
| "remove duplicates in-place" | Same-Direction Read/Write Pointers |
| "move zeros / partition array" | Same-Direction Read/Write Pointers |
| "sort colors / Dutch National Flag" | Three Pointers (low, mid, high) |
| "linked list cycle detection" | Fast/Slow Pointers (Floyd's) |
| "middle of linked list" | Fast/Slow Pointers |
| "k-th from end in linked list" | Fast pointer k-ahead gap |
| "palindrome check in-place" | Opposite-End Pointers |
| "valid palindrome with one deletion" | Two Pointers + helper check |
| "reverse string / part of array" | Opposite-End Swap |
| "merge two sorted arrays" | Merge-Style Two Pointers |
| "container with most water" | Opposite-End, move shorter side |
| "trapping rain water" | Opposite-End, track maxLeft/maxRight |
| "squares of sorted array" | Opposite-End, fill result from right |
| "reorder list (L0→Ln→L1...)" | Find Mid + Reverse + Merge (3-phase) |
| "minimize max pair sum" | Sort + Opposite-End Greedy Pairing |
| "intersection of two sorted arrays" | Merge-Style Two Pointers |

---

## The Five Templates (Memorize These)

### Template 1: Opposite-End — Pair Sum

```java
int left = 0, right = n - 1;
while (left < right) {
    int sum = nums[left] + nums[right];
    if      (sum == target) { /* found */ left++; right--; }
    else if (sum  < target)   left++;   // need larger
    else                      right--;  // need smaller
}
```

### Template 2: Fix + Two Pointers (3Sum / kSum)

```java
Arrays.sort(nums);
for (int i = 0; i < n - 2; i++) {
    if (i > 0 && nums[i] == nums[i-1]) continue; // skip outer dup
    int left = i + 1, right = n - 1;
    while (left < right) {
        int sum = nums[i] + nums[left] + nums[right];
        if (sum == 0) {
            // record result
            while (left < right && nums[left]  == nums[left+1])  left++;  // skip dup
            while (left < right && nums[right] == nums[right-1]) right--; // skip dup
            left++; right--;
        } else if (sum < 0) left++;
        else                right--;
    }
}
```

### Template 3: Same-Direction Read/Write

```java
int slow = 0; // write head
for (int fast = 0; fast < n; fast++) {
    if (/* nums[fast] is valid */) {
        nums[slow++] = nums[fast]; // write valid element
    }
}
// slow = new length (or new logical end)
```

### Template 4: Fast/Slow — Linked List

```java
// Cycle detection
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) { /* cycle */ break; }
}

// Find midpoint (for even-length list, slow ends at first mid)
while (fast.next != null && fast.next.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
// slow = midpoint
```

### Template 5: Dutch National Flag (Three Pointers)

```java
int low = 0, mid = 0, high = n - 1;
while (mid <= high) {
    if      (nums[mid] == 0) swap(nums, low++, mid++); // send 0 left
    else if (nums[mid] == 1) mid++;                    // 1 in place
    else                     swap(nums, mid, high--);  // send 2 right
    // DON'T advance mid after swap with high
}
```

---

## Duplicate Skipping Reference (3Sum / kSum)

```
OUTER loop skip:   if (i > 0 && nums[i] == nums[i-1]) continue;
INNER loop skip:   if (j > i+1 && nums[j] == nums[j-1]) continue;  // 4Sum
AFTER MATCH skip:
    while (left < right && nums[left]  == nums[left+1])  left++;
    while (left < right && nums[right] == nums[right-1]) right--;
    left++; right--;   // final step after skipping
```

**Why skip BEFORE the final step?**
The `while` loops advance past all equal elements, so `left++/right--` after them
moves to the next TRULY different element. The `left < right` guard prevents crossing.

---

## Complexity Quick Reference

| Problem | Time | Space |
|---|---|---|
| Two Sum II (sorted) | O(n) | O(1) |
| 3Sum | O(n²) | O(1) extra |
| 4Sum | O(n³) | O(1) extra |
| kSum (recursive) | O(n^(k-1)) | O(k) stack |
| Container With Most Water | O(n) | O(1) |
| Trapping Rain Water | O(n) | O(1) |
| Sort Colors (Dutch Flag) | O(n) | O(1) |
| Remove Duplicates In-Place | O(n) | O(1) |
| Squares of Sorted Array | O(n) | O(n) output |
| Linked List Cycle (detect) | O(n) | O(1) |
| Linked List Cycle (start) | O(n) | O(1) |
| Remove Nth From End | O(n) | O(1) |
| Merge Two Sorted Arrays (in-place) | O(m+n) | O(1) |
| Reorder List | O(n) | O(1) |

---

## Overflow Warning

When summing large integers in 3Sum / 4Sum:

```java
// WRONG (int overflow when values near Integer.MAX_VALUE)
int sum = nums[i] + nums[j] + nums[left] + nums[right];

// CORRECT
long sum = (long) nums[i] + nums[j] + nums[left] + nums[right];
```

---

## Two Pointers vs Sliding Window Decision Guide

```
Is the problem about CONTIGUOUS subarrays / substrings?
  YES → Sliding Window (left only moves forward, window is contiguous)
  NO  → Could be Two Pointers

Does the problem involve PAIRS, TRIPLETS, or IN-PLACE modification?
  YES → Two Pointers

Is data SORTED (or can be sorted without losing index info)?
  YES + pair sum → Opposite-End Two Pointers
  YES + remove dups / partition → Same-Direction Two Pointers

Linked List with cycle / midpoint?
  YES → Fast/Slow Two Pointers
```

---

## Problem → Pattern Map (for Google interviews)

```
Two Sum II                       → Opposite-End (sorted array)
Valid Palindrome                 → Opposite-End + skip non-alphanum
Reverse String                   → Opposite-End symmetric swap
Squares of Sorted Array          → Opposite-End, fill right-to-left
Container With Most Water        → Opposite-End, move shorter
Trapping Rain Water              → Opposite-End, track maxLeft/maxRight
3Sum                             → Sort + Fix + Two Pointers
3Sum Closest                     → Sort + Fix + Two Pointers + min diff
4Sum                             → Sort + Fix×2 + Two Pointers
Remove Duplicates                → Same-Direction read/write
Move Zeroes                      → Same-Direction write-head
Remove Element                   → Same-Direction write-head
Merge Sorted Array               → Three Pointers, fill right-to-left
Sort Colors                      → Three Pointers (Dutch National Flag)
Linked List Cycle I              → Fast/Slow detect
Linked List Cycle II             → Fast/Slow detect + find start (Floyd)
Middle of Linked List            → Fast/Slow (fast.next.next)
Remove Nth From End              → Fast pointer k+1 ahead gap
Reorder List                     → Find Mid + Reverse + Merge (3-phase)
Minimize Max Pair Sum            → Sort + Opposite-End greedy pairing
Intersection of Two Arrays II    → Sort + Merge-Style Two Pointers
```

---

## Interview Communication Script

When you start solving any two-pointer problem:

> "I recognize this as a two-pointer problem. Let me identify the variant:
> - Is the array sorted? → Yes/No (if no, sort it if valid, O(n log n))
> - Looking for pairs/triplets? → Opposite-end or Fix + Two-Pointer
> - In-place modification? → Same-direction read/write
> - Linked list cycle or midpoint? → Fast/Slow
> - 3-way partition? → Dutch National Flag (three pointers)
>
> Time complexity: O(n) or O(n²) for triplets. Space: O(1).
> Each pointer moves at most n times and never reverses → 2n ops → O(n)."

---

## Complexity Proof Keywords for Interviews

- **Opposite-end O(n):** "Left and right each move at most n times, only toward each other. Never reverse → at most 2n moves total."
- **3Sum O(n²):** "Outer O(n) loop, inner two-pointer O(n) → O(n²). This is optimal — 3Sum lower bound is Ω(n²)."
- **Fast/Slow O(n):** "Slow takes at most n steps. Fast takes at most 2n steps. Meeting guaranteed within n iterations."
- **Dutch National Flag O(n):** "mid and high each move at most n times; low and mid move together → at most 3n operations."

---

*Files in this module:*
- `00_theory.md` — Internals, proofs, templates, decision tree
- `01_easy.java` — 8 easy problems (pair sum, palindrome, remove dups, merge)
- `02_medium.java` — 8 medium problems (3Sum, Dutch Flag, linked list cycle)
- `03_hard.java` — 6 hard problems (trapping water, 4Sum, palindrome pairs)
- `04_google_level.java` — 6 Google-level (bitmask XOR, bracket balancing, reorder list)
- `05_cheatsheet.md` — This file
