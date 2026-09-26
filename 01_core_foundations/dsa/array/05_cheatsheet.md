# Arrays — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "sorted array" + "search" | Binary Search |
| "sorted array" + "two values that sum to" | Two Pointers (opposite ends) |
| "remove duplicates" / "in-place" | Two Pointers (slow/fast write) |
| "maximum/minimum subarray sum" | Kadane's Algorithm |
| "subarray sum equals k" (positives only) | Sliding Window |
| "subarray sum equals k" (mixed +/-) | Prefix Sum + HashMap |
| "product of array except self" | Prefix Product + Suffix Product |
| "merge sorted arrays" | Two Pointers (from end for in-place) |
| "rotate array/string" | Reverse trick (three reverses) |
| "next permutation" / "next greater" | Lexicographic algorithm / Monotonic Stack |
| "sort with only 0,1,2" / "partition" | Dutch National Flag (3 pointers) |
| "merge overlapping intervals" | Sort by start + linear sweep |
| "trapping rain water" | Two Pointers / Prefix max |
| "missing/duplicate in [1,n]" | Cyclic Sort / in-place marking |
| "sliding window max/min" | Monotonic Deque |
| "median of sorted arrays" | Binary Search on partition |
| "minimize the maximum" / "can we achieve X?" | Binary Search on Answer |
| "longest increasing subsequence" | DP + Binary Search (tails array) |
| "buy/sell stock" (one transaction) | Running minimum |
| "buy/sell stock" (unlimited) | Greedy: sum all positive diffs |
| "spiral / diagonal / layer traversal" | Boundary simulation (4 pointers) |

---

## Java Array Idioms (Memorize These)

```java
// 1. Two-pointer swap (the most common operation)
int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;

// 2. Reverse a subarray [l, r]
while (l < r) { int t = arr[l]; arr[l++] = arr[r]; arr[r--] = t; }

// 3. Binary search (built-in)
int idx = Arrays.binarySearch(arr, key);
// Returns index if found, or -(insertionPoint)-1 if not found.
// To get insertion point: if (idx < 0) idx = -(idx + 1);

// 4. Custom sort for 2D arrays (e.g., intervals by start)
Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
// *** CAREFUL: a[0] - b[0] can overflow! Use Integer.compare(a[0], b[0]) instead ***

// 5. Array to list and back
List<Integer> list = new ArrayList<>();
int[] arr = list.stream().mapToInt(Integer::intValue).toArray();

// 6. Fill array with a value
Arrays.fill(arr, val);
Arrays.fill(arr, fromIdx, toIdx, val);  // range fill

// 7. Copy array (with optional resize)
int[] copy = Arrays.copyOf(arr, newLength);
int[] range = Arrays.copyOfRange(arr, from, to);

// 8. Prefix sum (in-place)
for (int i = 1; i < n; i++) prefix[i] += prefix[i - 1];
// Range sum query: sum(l, r) = prefix[r] - (l > 0 ? prefix[l-1] : 0)

// 9. Max/Min of array
int max = Arrays.stream(arr).max().getAsInt();
int min = Arrays.stream(arr).min().getAsInt();
// Faster: manual loop (avoids boxing)

// 10. Print array for debugging
System.out.println(Arrays.toString(arr));       // 1D
System.out.println(Arrays.deepToString(arr2D)); // 2D
```

---

## Complexity Quick Reference

| Operation | Array `int[]` | ArrayList | Sorted Array |
|---|---|---|---|
| Access by index | O(1) | O(1) | O(1) |
| Search (unsorted) | O(n) | O(n) | O(log n) via binarySearch |
| Insert at end | N/A | O(1) amortized | O(n) to maintain order |
| Insert at index | N/A | O(n) | O(n) |
| Delete at index | N/A | O(n) | O(n) |
| Sort | O(n log n) | O(n log n) | already sorted |

---

## Two Pointers Template

```java
// Opposite-end pointers (sorted array, pair problems)
int left = 0, right = n - 1;
while (left < right) {
    int current = evaluate(arr[left], arr[right]);
    if (current == target) { /* found */ left++; right--; }
    else if (current < target) left++;
    else right--;
}

// Same-direction pointers (slow/fast, in-place write)
int slow = 0;
for (int fast = 0; fast < n; fast++) {
    if (condition(arr[fast])) {
        arr[slow++] = arr[fast];  // or swap(slow, fast)
    }
}
// arr[0..slow-1] = valid elements
```

---

## Kadane's Algorithm Template

```java
// Maximum subarray sum
int maxEndingHere = nums[0], maxSoFar = nums[0];
for (int i = 1; i < nums.length; i++) {
    maxEndingHere = Math.max(nums[i], maxEndingHere + nums[i]);
    maxSoFar = Math.max(maxSoFar, maxEndingHere);
}
// maxSoFar = answer

// Variant: Maximum subarray PRODUCT
// Track both max and min (negative × negative = positive)
int maxP = nums[0], minP = nums[0], result = nums[0];
for (int i = 1; i < nums.length; i++) {
    if (nums[i] < 0) { int t = maxP; maxP = minP; minP = t; } // swap
    maxP = Math.max(nums[i], maxP * nums[i]);
    minP = Math.min(nums[i], minP * nums[i]);
    result = Math.max(result, maxP);
}
```

---

## Binary Search on Answer Template

```java
// "Minimize the maximum" or "Is X achievable?" problems
long lo = lowerBound, hi = upperBound;
while (lo < hi) {
    long mid = lo + (hi - lo) / 2;
    if (isFeasible(mid)) {
        hi = mid;       // feasible → try smaller
    } else {
        lo = mid + 1;   // not feasible → need larger
    }
}
return lo;  // smallest feasible value

// isFeasible: greedy check — can we achieve the constraint with limit = mid?
// Must be MONOTONIC: if feasible for mid, then feasible for mid+1.
```

---

## Sliding Window Template (Variable Size)

```java
// Longest/shortest subarray with some property
int left = 0, best = 0;
for (int right = 0; right < n; right++) {
    // Expand: add arr[right] to window state
    
    while (/* window violates constraint */) {
        // Shrink: remove arr[left] from window state
        left++;
    }
    
    best = Math.max(best, right - left + 1);  // or Math.min for shortest
}
```

---

## Problem → Pattern Map (for Google interviews)

```
Two Sum (sorted)         → Two Pointers
3Sum                     → Sort + Two Pointers
Container With Most Water → Two Pointers (greedy)
Trapping Rain Water      → Two Pointers / Prefix max
Max Subarray             → Kadane's Algorithm
Buy/Sell Stock           → Running Minimum (Kadane variant)
Product Except Self      → Prefix/Suffix Product
Remove Duplicates        → Two Pointers (slow/fast)
Merge Sorted Array       → Two Pointers from end
Rotate Array             → Three Reverses
Next Permutation         → Lexicographic Algorithm
Sort Colors              → Dutch National Flag
Merge Intervals          → Sort + Sweep
Sliding Window Max       → Monotonic Deque
First Missing Positive   → Cyclic Sort
Median of 2 Sorted       → Binary Search on Partition
Split Array Largest Sum  → Binary Search on Answer
LIS                      → DP + Binary Search (Patience Sort)
```

---

## Interview Communication Script

When you start solving any array problem:

> "Let me classify this problem. I see we're working with an array that is
> [sorted / unsorted / has specific constraints].
> My first instinct is [pattern]. Let me verify:
> - Can I sort? Does that preserve the answer? → If yes, opens Two Pointers.
> - Is there a subarray/subsequence constraint? → Sliding Window or Kadane's.
> - Is the answer monotonic? → Binary Search on Answer.
> - Can I solve this in-place? → Two Pointer or cyclic sort.
> Now let me think about the specific approach..."

---

## Complexity Proof Keywords for Interviews

When asked "why is this O(n)?", use these arguments:

- **Two pointers:** "Left and right each move at most n times total → 2n operations."
- **Kadane's:** "Single pass, O(1) work per element. Optimal substructure: OPT(i) depends only on OPT(i-1)."
- **Sliding window:** "Each element enters and leaves the window at most once → 2n total ops."
- **Monotonic deque:** "Each element pushed once and popped at most once → 2n total deque ops."
- **Cyclic sort:** "Each swap places at least one element correctly. At most n swaps total."
- **Amortized O(1) for ArrayList.add:** "n insertions cost at most 3n total by the banker's argument."
- **Binary search on answer:** "log(range) iterations × O(n) validation = O(n log range)."

---

*Files in this module:*
- `00_theory.md` — Internals, proofs, Java APIs
- `01_easy.java` — 8 easy problems with follow-ups
- `02_medium.java` — 8 medium problems with optimal analysis
- `03_hard.java` — 6 hard problems (rain water, cyclic sort, monotonic deque, binary search)
- `04_google_level.java` — 6 Google-level multi-topic problems
- `05_cheatsheet.md` — This file
