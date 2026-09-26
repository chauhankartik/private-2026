# Hashing — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "pair that sums to" / "complement" | Two-Sum HashMap |
| "subarray sum equals k" | Prefix Sum + HashMap |
| "subarray with equal X and Y" | Transform (0→-1 trick) + Prefix Sum |
| "longest subarray" + positive nums | Sliding Window (no HashMap needed) |
| "longest subarray" + mixed nums | Prefix Sum + HashMap (first seen index) |
| "anagram" / "permutation" | Frequency array or sorted key |
| "group by some property" | `computeIfAbsent` + canonical key |
| "at most k distinct" | Sliding window + freq map |
| "exactly k distinct" | atMost(k) - atMost(k-1) |
| "top K frequent" | Bucket sort O(n) or min-heap O(n log k) |
| "cycle detection" | HashSet of visited states |
| "O(1) get/put + eviction" | HashMap + Doubly Linked List (LRU) |
| "substring search fast" | Rolling Hash (Rabin-Karp) |
| "minimum from ends" | Complement: maximize middle window |

---

## Java HashMap Idioms (Memorize These)

```java
// 1. Frequency count (the most common pattern)
map.merge(key, 1, Integer::sum);
// equivalent to: map.put(key, map.getOrDefault(key, 0) + 1);

// 2. Group-by (very common)
map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

// 3. Get with default (always prefer over get() + null check)
map.getOrDefault(key, 0);

// 4. Conditional put (don't overwrite first occurrence)
map.putIfAbsent(key, index);

// 5. Iterate entries
for (Map.Entry<K, V> e : map.entrySet()) { e.getKey(); e.getValue(); }

// 6. Frequency array for lowercase chars (O(1) space)
int[] freq = new int[26];
freq[c - 'a']++;

// 7. LinkedHashMap for insertion-order + LRU base
Map<K, V> lhm = new LinkedHashMap<>(capacity, 0.75f, true);

// 8. TreeMap for sorted keys (O(log n) per op)
TreeMap<K, V> tm = new TreeMap<>();
tm.floorKey(k);   // largest key ≤ k
tm.ceilingKey(k); // smallest key ≥ k
```

---

## Complexity Quick Reference

| Operation | HashMap avg | HashMap worst | TreeMap |
|---|---|---|---|
| get / put / remove | O(1) | O(n)* | O(log n) |
| contains | O(1) | O(n)* | O(log n) |
| iterate | O(n+m) | O(n+m) | O(n) |

*Java 8+: per-bucket worst case is O(log n) due to Red-Black Tree conversion at chain length 8.

---

## Prefix Sum Template

```java
// Count subarrays with sum = k
Map<Integer, Integer> prefixCount = new HashMap<>();
prefixCount.put(0, 1);   // ← NEVER FORGET THIS LINE
int sum = 0, count = 0;
for (int num : nums) {
    sum += num;
    count += prefixCount.getOrDefault(sum - k, 0);
    prefixCount.merge(sum, 1, Integer::sum);
}
```

---

## Sliding Window Template

```java
// Longest window with at most k distinct
Map<Character, Integer> freq = new HashMap<>();
int left = 0, maxLen = 0;
for (int right = 0; right < s.length(); right++) {
    freq.merge(s.charAt(right), 1, Integer::sum);          // expand
    while (freq.size() > k) {                              // constraint broken
        char lc = s.charAt(left++);
        freq.merge(lc, -1, Integer::sum);
        if (freq.get(lc) == 0) freq.remove(lc);           // shrink
    }
    maxLen = Math.max(maxLen, right - left + 1);           // update
}
```

---

## Problem → Pattern Map (for Google interviews)

```
Two Sum          → Two-Sum HashMap
Group Anagrams   → Bucketing by sorted key
Longest Consec.  → HashSet + "start only at true start"
Subarray Sum=k   → Prefix Sum + HashMap
Min Window Sub.  → Sliding Window + Two frequency maps
LRU Cache        → HashMap + Doubly Linked List
Top K Frequent   → Frequency Map + Bucket Sort
```

---

## Interview Communication Script

When you start solving any hashing problem:

> "Let me classify this problem. I see we need [fast lookups / counting / grouping].
> My first instinct is to use a HashMap. Let me check:
> - Average O(1) per operation — acceptable?  ✓
> - Do I need sorted order? If yes, TreeMap O(log n).
> - Is the key space bounded (e.g., 26 chars)? If yes, use an array — faster + O(1) space.
> Now let me think about the pattern..."

---

## Complexity Proof Keywords for Interviews

When asked "why is this O(n)?", use these arguments:

- **Amortized O(1):** "Each element enters and leaves the window at most once → 2n operations total."
- **SUHA O(1):** "Under simple uniform hashing assumption, expected chain length is α ≈ load factor ≤ 0.75."
- **Bucket sort O(n):** "Frequencies are bounded by n, so we use n buckets. Building and scanning is O(n)."
- **Load factor resizing:** "HashMap doubles capacity at α=0.75, and amortized insertion is O(1) by potential method."

---

*Files in this module:*
- `00_theory.md` — Internals, proofs, Java APIs
- `01_easy.java` — 7 easy problems with follow-ups
- `02_medium.java` — 8 medium problems with optimal analysis
- `03_hard.java` — 6 hard problems (LRU, Rolling Hash, Min Window)
- `04_google_level.java` — 6 Google-level multi-topic problems
- `05_cheatsheet.md` — This file
