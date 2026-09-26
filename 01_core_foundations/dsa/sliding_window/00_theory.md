# Sliding Window — Theory, Internals & Complexity Proofs
> **Study goal:** Understand *why* sliding window works, when to use it, and how to pick the right variant.
> Google interviewers expect you to immediately identify sliding window problems and articulate the O(n) proof.

---

## 1. What Is the Sliding Window Technique?

A **sliding window** maintains a contiguous subarray (or substring) that "slides" across the input.
Instead of recomputing the answer from scratch for each subarray, we **incrementally update** by:
- **Adding** the new element entering the window (right side)
- **Removing** the old element leaving the window (left side)

```
Array:  [2, 1, 5, 1, 3, 2]    Window size k = 3

Step 1:  [2, 1, 5] 1, 3, 2    sum = 8
Step 2:   2 [1, 5, 1] 3, 2    sum = 8 - 2 + 1 = 7   (remove left, add right)
Step 3:   2, 1 [5, 1, 3] 2    sum = 7 - 1 + 3 = 9
Step 4:   2, 1, 5 [1, 3, 2]   sum = 9 - 5 + 2 = 6
```

**Key insight:** Each transition costs O(1) instead of O(k), giving O(n) total instead of O(n·k).

---

## 2. When to Use Sliding Window

### The Signal
> If the problem asks about **contiguous subarrays/substrings** and involves **optimization (max/min/count)**
> with some **constraint**, think sliding window.

### Decision Tree
```
Is the problem about contiguous subarrays/substrings?
 ├── NO → Not sliding window (consider DP, backtracking, etc.)
 └── YES
      ├── Is the window size FIXED?
      │    └── YES → Fixed-size sliding window
      │         Template: slide window of size k, track state
      │
      └── Is the window size VARIABLE?
           ├── Looking for LONGEST valid window?
           │    └── Variable window (expand greedily, shrink when invalid)
           │
           ├── Looking for SHORTEST valid window?
           │    └── Variable window (shrink greedily when valid)
           │
           └── COUNTING all valid subarrays?
                └── "atMost(k) - atMost(k-1)" trick
                     or expand + count valid subarrays ending at right
```

### Sliding Window vs Other Patterns

| Problem Type | Pattern | Why |
|---|---|---|
| Subarray sum = k (positive values only) | Sliding Window | Monotonic: sum grows as window grows |
| Subarray sum = k (mixed +/-) | Prefix Sum + HashMap | Non-monotonic: can't shrink/expand predictably |
| Longest subarray with constraint | Variable Sliding Window | Expand right, shrink left when constraint breaks |
| Fixed-size subarray max/avg | Fixed Sliding Window | Window size predetermined |
| Two elements that satisfy condition | Two Pointers | Not about contiguous subarrays |
| Subarray with exactly K distinct | atMost(K) - atMost(K-1) | Convert "exactly" to "at most" |

---

## 3. Time & Space Complexity Proofs

### Theorem: Variable Sliding Window is O(n)

**Proof (Aggregate Method):**
```
Let L = left pointer, R = right pointer.
- R moves from 0 to n-1 → exactly n increments.
- L moves from 0 to at most n-1 → at most n increments.
- L never decreases (only moves right).
- Total pointer movements = n (for R) + at most n (for L) = 2n.
- Each movement does O(1) work (add/remove element from state).
- Total work = O(2n) = O(n).
```

**Proof (Amortized — per element):**
```
Each element enters the window exactly ONCE (when R passes it).
Each element leaves the window at most ONCE (when L passes it).
Total operations per element = at most 2 (one enter, one leave).
Total operations = 2n = O(n).
```

> **Interview script:** "Each element enters and leaves the window at most once,
> giving us 2n total operations — O(n) time."

### Space Complexity

| Window State | Space | Example |
|---|---|---|
| Running sum/product | O(1) | Max sum subarray of size k |
| Frequency array (26 chars) | O(1) | Longest substring without repeating (lowercase) |
| Frequency HashMap | O(min(n, alphabet)) | At most k distinct characters |
| Monotonic Deque | O(k) | Sliding window maximum |

---

## 4. The Three Core Templates

### Template 1: Fixed-Size Window

```java
/**
 * Use when: window size k is given.
 * Pattern: Initialize window [0..k-1], then slide by adding right, removing left.
 */
int fixedWindow(int[] arr, int k) {
    int n = arr.length;
    
    // 1. Build initial window [0..k-1]
    int windowSum = 0;
    for (int i = 0; i < k; i++) {
        windowSum += arr[i];
    }
    int best = windowSum;
    
    // 2. Slide: remove arr[i-k], add arr[i]
    for (int i = k; i < n; i++) {
        windowSum += arr[i] - arr[i - k];
        best = Math.max(best, windowSum);
    }
    return best;
}
```

### Template 2: Variable Window — Longest

```java
/**
 * Use when: find LONGEST subarray/substring satisfying a constraint.
 * Pattern: Expand right greedily. Shrink left only when constraint violated.
 *
 * Key: We NEVER shrink more than necessary. The window only grows or stays.
 */
int longestWindow(int[] arr) {
    int left = 0, best = 0;
    // ... initialize window state ...
    
    for (int right = 0; right < arr.length; right++) {
        // EXPAND: add arr[right] to window state
        
        while (/* window is INVALID */) {
            // SHRINK: remove arr[left] from window state
            left++;
        }
        
        // Window [left..right] is valid
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

### Template 3: Variable Window — Shortest

```java
/**
 * Use when: find SHORTEST subarray satisfying a constraint.
 * Pattern: Expand right until valid. Shrink left as much as possible while still valid.
 *
 * Key: We shrink GREEDILY to find the minimum.
 */
int shortestWindow(int[] arr, int target) {
    int left = 0, best = Integer.MAX_VALUE;
    int windowSum = 0;
    
    for (int right = 0; right < arr.length; right++) {
        // EXPAND: add arr[right]
        windowSum += arr[right];
        
        while (windowSum >= target) {  // window is VALID
            // Record answer BEFORE shrinking
            best = Math.min(best, right - left + 1);
            // SHRINK: remove arr[left]
            windowSum -= arr[left++];
        }
    }
    return best == Integer.MAX_VALUE ? 0 : best;
}
```

---

## 5. Advanced Technique: "Exactly K" = atMost(K) - atMost(K-1)

Many problems ask for subarrays with **exactly K** of something (distinct elements, odd numbers, etc.).
Direct sliding window doesn't handle "exactly K" well because both shrinking and expanding can toggle validity.

**Solution:** Convert to two "at most" subproblems:
```
count(exactly K) = count(at most K) - count(at most K - 1)
```

```java
/**
 * Template: Count subarrays with exactly K distinct elements
 */
int subarraysWithExactlyK(int[] arr, int k) {
    return atMostK(arr, k) - atMostK(arr, k - 1);
}

int atMostK(int[] arr, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    int left = 0, count = 0;
    
    for (int right = 0; right < arr.length; right++) {
        freq.merge(arr[right], 1, Integer::sum);
        
        while (freq.size() > k) {
            int leftVal = arr[left++];
            freq.merge(leftVal, -1, Integer::sum);
            if (freq.get(leftVal) == 0) freq.remove(leftVal);
        }
        
        // ALL subarrays ending at right with left in [left..right] are valid
        count += right - left + 1;
    }
    return count;
}
```

**Why `count += right - left + 1`?**
```
Window [left..right] is valid (at most K distinct).
Valid subarrays ending at right:
  [left..right], [left+1..right], ..., [right..right]
  = (right - left + 1) subarrays
```

---

## 6. Advanced Technique: Monotonic Deque for Window Min/Max

When you need the **maximum or minimum** within a sliding window, a naive approach is O(nk).
A **monotonic deque** achieves O(n):

```java
/**
 * Sliding window maximum using monotonic decreasing deque.
 * Deque stores INDICES. Front = max of current window.
 */
int[] slidingWindowMax(int[] nums, int k) {
    Deque<Integer> deque = new ArrayDeque<>();  // stores indices
    int[] result = new int[nums.length - k + 1];
    
    for (int i = 0; i < nums.length; i++) {
        // Remove elements outside the window
        while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        
        // Remove elements smaller than current (they'll never be max)
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        
        deque.offerLast(i);
        
        // Record result when window is fully formed
        if (i >= k - 1) {
            result[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    return result;
}
```

**Why O(n)?** Each element is pushed onto the deque once and popped at most once → 2n ops.

---

## 7. Common Pitfalls

| Pitfall | Fix |
|---|---|
| Using sliding window with negative numbers for "sum ≥ k" | Use prefix sum + monotonic deque (LC 862) |
| Forgetting to handle empty window edge case | Check `left <= right` or window size ≥ 1 |
| Off-by-one in fixed window (processing before window is full) | Start result collection at index `k-1` |
| Using `while` vs `if` for shrinking | `while` for shortest/valid shrink; `if` for "non-shrinking" optimization |
| "Exactly K" → trying direct sliding window | Use atMost(K) - atMost(K-1) decomposition |

---

## 8. Interview Complexity Vocabulary

> *"I recognize this as a sliding window problem because we're looking for an optimal
> contiguous subarray under a constraint. I'll use a [fixed/variable] window.
> The time complexity is O(n) because each element enters and leaves the window at most once,
> giving 2n total pointer movements. Space is O([1/k/26]) for maintaining [sum/deque/frequency]."*

---

*Next: → [01_easy.java](01_easy.java) — Easy problems applying these patterns*
