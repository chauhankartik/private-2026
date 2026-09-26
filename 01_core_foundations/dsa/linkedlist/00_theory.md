# Linked List — Theory & Pattern Guide
> **Study goal:** Master linked list patterns for Google/FAANG interviews.
> Every pattern here maps directly to a problem category.

---

## 1. Core Data Structure

```
Singly Linked:   [val|next] → [val|next] → [val|next] → null
Doubly Linked:   null ← [prev|val|next] ↔ [prev|val|next] → null
Circular:        [val|next] → [val|next] → [val|next] ─┐
                    └────────────────────────────────────┘
```

```java
// Singly
class ListNode {
    int val;
    ListNode next;
    ListNode(int v) { val = v; }
}

// Doubly (used in LRU Cache, browser history)
class DNode {
    int val;
    DNode prev, next;
}
```

---

## 2. Time & Space Complexities

| Operation | Singly LL | Array |
|---|---|---|
| Access by index | O(n) | O(1) |
| Search | O(n) | O(n) |
| Insert at head | O(1) | O(n) |
| Insert at tail (with tail ptr) | O(1) | O(1) amortized |
| Insert in middle | O(n) find + O(1) insert | O(n) shift |
| Delete head | O(1) | O(n) |
| Delete tail | O(n) | O(1) |

---

## 3. The Five Core Patterns

### Pattern 1: Two Pointers (Fast & Slow)
```
slow →  1 → 2 → 3 → 4 → 5
fast →  1 → 2 → 3 → 4 → 5
```
- **Detect cycle**: fast moves 2x, slow moves 1x → they meet inside cycle
- **Find middle**: fast reaches end → slow is at middle
- **Kth from end**: fast advances K steps first, then both move → slow at Kth from end

### Pattern 2: Reverse
```
Before: 1 → 2 → 3 → 4 → 5 → null
After:  5 → 4 → 3 → 2 → 1 → null
```
- Iterative (3 pointers: prev, curr, next)
- Recursive (tail-recursive unwind)
- Reverse a sublist [m, n]

### Pattern 3: Merge & Sort
```
List A: 1 → 3 → 5
List B: 2 → 4 → 6
Merged: 1 → 2 → 3 → 4 → 5 → 6
```
- Merge two sorted lists (pointer dance)
- Merge K sorted lists (min-heap of K heads)
- Sort a linked list (merge sort — O(n log n) with O(log n) stack)

### Pattern 4: Dummy Node
```
dummy → [original list]
```
- Always use a dummy when the head may change (deletion, insertion before head)
- `return dummy.next` at the end

### Pattern 5: In-Place Manipulation
```
Rotate right by k: 1 → 2 → 3 → 4 → 5 → (rotate 2) → 4 → 5 → 1 → 2 → 3
```
- Count length → find new tail → re-link

---

## 4. Floyd's Cycle Detection Algorithm

```
Phase 1 — Detect:
  slow = head, fast = head
  while fast != null && fast.next != null:
    slow = slow.next
    fast = fast.next.next
    if slow == fast: CYCLE FOUND

Phase 2 — Find entry point:
  Reset slow = head (keep fast at meeting point)
  Move both ONE step at a time
  When they meet: that's the cycle entry
  
WHY PHASE 2 WORKS:
  Let: distance to cycle entry = F
       cycle length = C
       meeting point distance from entry = a
  At meeting: slow traveled F + a
              fast traveled F + a + nC (full loops)
  Since fast = 2 × slow: F + a + nC = 2(F + a)
                          → F = nC - a
  Reset slow to head: both travel F more steps
  fast (from meeting) travels nC - a more steps → back to entry
  slow (from head) travels F = nC - a steps → also at entry ✓
```

---

## 5. Interview Patterns by Frequency

| # | Problem | Pattern | Frequency |
|---|---|---|---|
| 1 | Reverse Linked List | Reverse | ⭐⭐⭐⭐⭐ |
| 2 | Detect Cycle | Fast/Slow | ⭐⭐⭐⭐⭐ |
| 3 | Find Cycle Entry | Fast/Slow Phase 2 | ⭐⭐⭐⭐ |
| 4 | Merge Two Sorted Lists | Merge | ⭐⭐⭐⭐⭐ |
| 5 | Find Middle | Fast/Slow | ⭐⭐⭐⭐ |
| 6 | Remove Nth from End | Fast/Slow + Dummy | ⭐⭐⭐⭐ |
| 7 | Palindrome Check | Fast/Slow + Reverse | ⭐⭐⭐ |
| 8 | Merge K Sorted Lists | Min-Heap | ⭐⭐⭐⭐ |
| 9 | LRU Cache | Doubly LL + HashMap | ⭐⭐⭐⭐⭐ |
| 10 | Copy List with Random Pointer | Cloning | ⭐⭐⭐ |
| 11 | Reorder List | Fast/Slow + Reverse + Merge | ⭐⭐⭐ |
| 12 | Add Two Numbers | Carry arithmetic | ⭐⭐⭐ |
| 13 | Flatten Multilevel DLL | DFS/Recursion | ⭐⭐⭐ |
| 14 | Sort List | Merge Sort on LL | ⭐⭐⭐ |

---

## 6. Common Bugs to Watch

```java
// Bug 1: Null pointer — always guard fast.next
while (fast != null && fast.next != null)  // ✓
while (fast.next != null && fast.next.next != null)  // also valid

// Bug 2: Losing the next node before redirect
curr.next = prev;   // ✗ — lost next!
// Fix:
ListNode next = curr.next;
curr.next = prev;
prev = curr;
curr = next;   // ✓

// Bug 3: Off-by-one in "Kth from end"
// Move fast K steps (not K-1) if you want K-1 gap between slow and fast
// Dry-run on a 5-node list with k=2 to verify

// Bug 4: Forgetting to set tail.next = null after detach
// Causes cycles in rotate/reorder problems

// Bug 5: Even/odd length in "find middle"
// fast reaches null (even) vs fast.next reaches null (odd) — be consistent
```

---

*Next:*
- `01_easy.java` — Reverse, find middle, detect cycle, merge sorted, remove Nth
- `02_medium.java` — LRU Cache, reorder list, copy with random, add numbers, rotate
- `03_hard.java` — Merge K sorted, sort list, flatten multilevel, reverse k-group
- `04_google_level.java` — Google tag problems with optimization traces
