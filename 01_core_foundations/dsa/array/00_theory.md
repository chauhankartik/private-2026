# Arrays — Theory, Internals & Complexity Proofs
> **Study goal:** Understand arrays at the hardware level, not just the API level.  
> Google interviewers expect you to reason about cache locality, amortized costs, and when arrays beat hash maps.

---

## 1. What Is an Array?

An **array** is a contiguous block of memory storing elements of the same type.
Given a base address `B` and element size `S`:

```
Address(arr[i]) = B + i × S
```

This formula is why **random access is O(1)** — a single arithmetic operation,
no pointer chasing required.

### Why arrays are fast (hardware perspective)
| Property | Benefit |
|---|---|
| **Contiguous memory** | CPU cache prefetcher loads adjacent elements automatically |
| **No pointer overhead** | No next/prev pointers like linked lists (saves 8-16 bytes per element) |
| **Cache line locality** | One 64-byte cache line holds 16 ints — sequential scan is ~100× faster than linked list |
| **Predictable access** | Branch predictor and prefetcher work optimally on linear access |

**Interview insight:** "I'll use an array instead of a HashMap here because the key space is bounded
by [26 / 128 / n], so the array gives me better cache locality and avoids hashing overhead."

---

## 2. Static vs Dynamic Arrays

### Static Array (`int[]` in Java)
- Fixed size, allocated on the heap (Java) or stack (C/C++)
- Cannot resize — must allocate new array + copy

### Dynamic Array (`ArrayList<E>` in Java, `vector<T>` in C++)
- Wraps a static array internally
- **Doubles** capacity when full (Java grows by ~50%: `newCapacity = oldCapacity + (oldCapacity >> 1)`)
- Provides `add()`, `remove()`, `get()`, `set()` operations

```
ArrayList internals:
┌─────────────────────────────────────┐
│  [0] [1] [2] [3] [4] [_] [_] [_]   │  size=5, capacity=8
└─────────────────────────────────────┘
         ↑ elementData array (Object[])
```

---

## 3. Time Complexity — Proof for Each Operation

| Operation | Static Array | ArrayList | Why? |
|---|---|---|---|
| `get(i)` / `set(i, v)` | O(1) | O(1) | Address arithmetic: `B + i × S` |
| `add(end)` | N/A | O(1) amortized | See §4 for amortized proof |
| `add(i, val)` (insert) | N/A | O(n) | Must shift elements [i..n-1] right |
| `remove(i)` | N/A | O(n) | Must shift elements [i+1..n-1] left |
| `contains(v)` | O(n) | O(n) | Linear scan — no ordering guarantee |
| `sort()` | O(n log n) | O(n log n) | Java uses dual-pivot quicksort / Timsort |
| `binarySearch()` | O(log n) | O(log n) | Requires sorted input |

### Proof: Why insert at position i is O(n)
```
Before: [a, b, c, d, e, _, _]   insert 'x' at index 2
                                 shift c, d, e each one position right
After:  [a, b, x, c, d, e, _]   → 3 shifts = n - i operations
Average case: i = n/2 → n/2 shifts → O(n)
Worst case:   i = 0 → n shifts → O(n)
```

---

## 4. Amortized Analysis of Dynamic Array Resizing

### Theorem: `ArrayList.add()` at end is **O(1) amortized**

**Aggregate method proof:**
- Suppose we insert n elements. Resizing occurs at sizes 1, 2, 4, 8, ..., 2^k where 2^k ≤ n.
- Cost of i-th insertion:
  - 1 (for the insertion itself)
  - If i is a power of 2: additional i-1 copies for resizing
- Total cost for n insertions:
  ```
  T(n) = n + (1 + 2 + 4 + 8 + ... + 2^k)
       = n + (2^(k+1) - 1)
       ≤ n + 2n - 1
       = 3n - 1
  ```
- Amortized cost per insertion: T(n)/n = (3n-1)/n ≈ **3 = O(1)**

**Banker's method proof:**
- Charge 3 units per insertion:
  - 1 unit: pay for the insertion
  - 2 units: save as "credit" on the element
- When array doubles from m to 2m:
  - Need to copy m elements (cost m)
  - Each of the m elements since last resize has 2 credits saved
  - Total credits = 2m ≥ m → sufficient to pay for the copy ✓

---

## 5. Space Complexity

| Structure | Space |
|---|---|
| `int[n]` | O(n) = 4n bytes (32-bit ints) |
| `Integer[n]` (boxed) | O(n) = ~16n bytes (object header + padding) |
| `ArrayList<Integer>` | O(n) + overhead (capacity > size, boxed Integers) |
| Frequency array `int[26]` | O(1) — bounded constant |
| 2D matrix `int[m][n]` | O(m × n) |

**Interview insight:** Java `int[]` is ~4× more memory-efficient than `ArrayList<Integer>`
due to boxing overhead. For hot loops, prefer primitive arrays.

---

## 6. Key Java APIs to Know

```java
// === Primitive array operations ===
int[] arr = new int[n];                       // initialized to 0
int[] arr = {1, 2, 3, 4};                     // literal initialization
int[] copy = Arrays.copyOf(arr, newLength);   // copy with resize
System.arraycopy(src, srcPos, dst, dstPos, length); // fast native copy

// === Sorting ===
Arrays.sort(arr);                              // dual-pivot quicksort O(n log n)
Arrays.sort(arr, fromIndex, toIndex);          // sort subrange
Arrays.sort(objArr, Comparator.comparingInt(a -> a[0])); // custom comparator

// === Searching ===
Arrays.binarySearch(arr, key);                 // returns index or -(insertion point)-1
// *** REQUIRES sorted array. Undefined behavior on unsorted. ***

// === Filling & Comparison ===
Arrays.fill(arr, val);                         // fill all elements
Arrays.equals(arr1, arr2);                     // deep element comparison
Arrays.deepEquals(arr2D_1, arr2D_2);           // for 2D arrays

// === Conversion ===
List<Integer> list = Arrays.asList(boxedArr);  // array → list (fixed size!)
int[] fromList = list.stream().mapToInt(Integer::intValue).toArray();

// === ArrayList ===
List<Integer> al = new ArrayList<>();
al.add(val);                                   // append — O(1) amortized
al.add(index, val);                            // insert — O(n)
al.get(index);                                 // access — O(1)
al.set(index, val);                            // update — O(1)
al.remove(index);                              // remove — O(n)
al.size();
al.isEmpty();
Collections.sort(al);                          // Timsort — O(n log n), stable
Collections.reverse(al);
```

---

## 7. Core Patterns in Array Problems

| Pattern | Description | Key Insight |
|---|---|---|
| **Two Pointers** | Scan from both ends or slow/fast | Reduce O(n²) to O(n) on sorted/partitioned data |
| **Sliding Window** | Fixed or variable size window | "Best subarray" with some constraint |
| **Prefix Sum / Product** | Precompute cumulative values | Range query in O(1) after O(n) setup |
| **Kadane's Algorithm** | Maximum subarray sum | `maxEndingHere = max(num, maxEndingHere + num)` |
| **Binary Search on Answer** | Search sorted search space | "Minimize the maximum" / "Can we achieve X?" |
| **Dutch National Flag** | 3-way partition | Sort with 3 categories in one pass |
| **Cyclic Sort / In-place Hashing** | Place element at its index | "Find missing/duplicate in [1,n]" |
| **Merge Intervals** | Sort + sweep | Overlapping intervals → merge/count |
| **Matrix Traversal** | Spiral, diagonal, layer-by-layer | Boundary tracking with 4 pointers |
| **Monotonic Stack/Deque** | Maintain sorted order in stack | "Next greater", "sliding window max" |

---

## 8. When NOT to Use an Array

| Situation | Better Alternative |
|---|---|
| Frequent insertions/deletions at arbitrary positions | LinkedList (but rarely in interviews) |
| Need O(1) lookup by value | HashMap / HashSet |
| Need sorted order with O(log n) insert/delete | TreeMap / TreeSet |
| Need FIFO/LIFO semantics | Queue / Stack (Deque) |
| Sparse data (most indices unused) | HashMap<Integer, V> |
| Need O(1) min/max with updates | Monotonic deque or segment tree |

---

## 9. Interview Complexity Vocabulary

When discussing arrays in interviews, use this framing:

> *"Arrays give O(1) random access via address arithmetic and excellent cache locality  
> due to contiguous memory layout. For this problem I'll use [two pointers / sliding window /  
> prefix sums] to avoid the O(n²) brute force. The key observation is [pattern-specific insight].  
> This gives O(n) time and O(1) space since I'm operating in-place."*

This signals deep understanding of both theory and practice.

---

## 10. Array vs LinkedList — The Full Picture

| Criterion | Array | LinkedList |
|---|---|---|
| Access by index | O(1) | O(n) |
| Insert at head | O(n) | O(1) |
| Insert at tail | O(1) amortized | O(1) with tail pointer |
| Insert at middle | O(n) | O(1) if node known, O(n) to find |
| Cache performance | Excellent | Poor (random memory jumps) |
| Memory overhead | Low | High (+16 bytes per node for pointers) |

**Bottom line:** In 99% of interview problems, arrays (or ArrayList) beat LinkedList.
LinkedList is relevant only for LRU Cache (DLL + HashMap) and specific pointer problems.

---

*Next: → [01_easy.java](01_easy.java) — Easy problems applying these patterns*
