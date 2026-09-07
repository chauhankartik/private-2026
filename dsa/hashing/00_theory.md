# Hashing — Theory, Internals & Complexity Proofs
> **Study goal:** Understand *why* hashing works, not just *how* to use it.  
> Every Google interviewer expects you to reason about trade-offs.

---

## 1. What Is a Hash Function?

A **hash function** `h` maps a key from a universe `U` (potentially infinite) into a fixed-size
array of slots `m`:

```
h : U → {0, 1, 2, ..., m-1}
```

### Desirable properties
| Property | Meaning |
|---|---|
| **Deterministic** | Same key → same hash, always |
| **Uniform distribution** | Keys spread evenly across slots |
| **Fast to compute** | O(1) per hash call (for fixed-length keys) |
| **Avalanche effect** | Small change in key → large change in hash |

---

## 2. Collision Resolution Strategies

Two keys `k1 ≠ k2` such that `h(k1) = h(k2)` produce a **collision**.

### 2a. Separate Chaining (what Java HashMap uses)

Each slot holds a **linked list** (or balanced BST) of all keys mapping to it.

```
Slot 0: → [key_a] → [key_b]
Slot 1: → [key_c]
Slot 2: (empty)
Slot 3: → [key_d] → [key_e] → [key_f]
```

**Java 8+ optimization:** When a chain exceeds **8 nodes**, it converts to a  
Red-Black Tree → worst-case lookup degrades from O(n) to **O(log n)**.

### 2b. Open Addressing (linear probing, quadratic probing, double hashing)

All entries stored in the array itself. On collision, probe for the next open slot.

---

## 3. Time Complexity — Proof via Load Factor

### Definition
```
Load factor α = n / m
  where n = number of inserted keys
        m = number of slots (table size)
```

### Theorem (Average-case — Simple Uniform Hashing Assumption)
> Under SUHA (each key is equally likely to hash to any of m slots, independently),
> the expected number of elements examined during an **unsuccessful search** is:

```
E[probes] = 1 / (1 - α)   for open addressing
E[chain_length] = α        for chaining (expected list length)
```

#### Proof sketch (chaining):
- Let `n` elements be hashed into `m` slots.
- Expected number of elements in slot `h(k)` = sum over all keys `k_i ≠ k` of P(h(k_i) = h(k))
  = (n - 1) / m  ≈  α  (for large n)
- So a **successful search** examines 1 + α/2 elements on average.
- An **unsuccessful search** examines α elements on average.

### Java HashMap resizes when α > 0.75 (the threshold)
This keeps expected O(1) for all operations.

---

## 4. Space Complexity

| Structure | Space |
|---|---|
| HashMap/HashSet | O(n) — proportional to elements stored |
| Fixed array as hash | O(m) — m = table/alphabet size |
| Frequency count (26 chars) | O(1) — bounded constant |

**Interview insight:** Saying "O(n) space" for a frequency map is correct only if n is  
the unique alphabet size. For generic keys of length L, hashing itself is O(L) per op.

---

## 5. Worst-Case vs. Average-Case

| Operation | Average (SUHA) | Worst Case |
|---|---|---|
| `get(key)` | O(1) | O(n) — all keys collide |
| `put(key, val)` | O(1) amortized | O(n) |
| `containsKey` | O(1) | O(n) |
| `remove` | O(1) | O(n) |

**Google follow-up:** "Can you make HashMap worst-case O(log n)?"  
→ Yes: use **TreeMap** (Red-Black Tree). Java 8 HashMap does this per-bucket.

---

## 6. Amortized Analysis of Resizing

When load factor exceeds threshold, Java rehashes into a table of size `2m`.

**Potential method (amortized proof):**
- Let Φ (potential) = number of filled slots beyond m/2
- Each `put` costs 1 actual + increases Φ by 1 → amortized cost = 2
- Rehash at size m costs m, but Φ drops by m/2 → amortized cost per element = 2

**Result:** All operations are **O(1) amortized** in Java HashMap.

---

## 7. Key Java APIs to Know

```java
// HashMap
Map<K, V> map = new HashMap<>();
map.put(k, v);
map.get(k);                          // null if absent
map.getOrDefault(k, defaultVal);     // *** most important
map.containsKey(k);
map.remove(k);
map.entrySet()                       // iterate over entries
map.keySet()
map.values()
map.putIfAbsent(k, v);
map.computeIfAbsent(k, key -> new ArrayList<>()); // *** group-by pattern

// HashSet
Set<K> set = new HashSet<>();
set.add(k);
set.contains(k);
set.remove(k);

// Frequency counting (fixed alphabet)
int[] freq = new int[26];
freq[c - 'a']++;

// LinkedHashMap — insertion-order preserved
Map<K, V> lhm = new LinkedHashMap<>();

// TreeMap — sorted keys, O(log n)
Map<K, V> tm = new TreeMap<>();
```

---

## 8. Core Patterns in Hashing Problems

| Pattern | Description | Key Insight |
|---|---|---|
| **Frequency Count** | Count occurrences of each element | `map.getOrDefault(k, 0) + 1` |
| **Two-Sum / Complement** | Check if complement exists | Store seen values in set/map |
| **Sliding Window + Map** | Window with frequency constraint | Expand/shrink + update map |
| **Prefix Sum + Map** | Subarray sum problems | `prefixSum → index/count` |
| **Grouping / Bucketing** | Group by canonical form | Sorted key, character count key |
| **Deduplication** | Unique elements | HashSet membership check |
| **Index Tracking** | First/last seen position | Store index in map |

---

## 9. Interview Complexity Vocabulary

When discussing hashing in interviews, use this framing:

> *"Under the simple uniform hashing assumption, HashMap operations are O(1) average.  
> In the worst case with adversarial inputs and a bad hash function, it degrades to O(n).  
> Java mitigates this with load factor 0.75 and per-bucket Red-Black Trees in Java 8+,  
> giving O(log n) worst case per bucket."*

This single paragraph signals deep knowledge and distinguishes you from 90% of candidates.

---

## 10. When NOT to Use a HashMap

| Situation | Better alternative |
|---|---|
| Need sorted order | TreeMap O(log n) |
| Integer keys in small range | Array (faster, cache-friendly) |
| Need min/max efficiently | TreeMap or PriorityQueue |
| Concurrent access | ConcurrentHashMap |
| Immutable lookup table | switch/if chains or array |

---

*Next: → [01_easy.java](01_easy.java) — Easy problems applying these patterns*
