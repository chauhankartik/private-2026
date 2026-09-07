# Java Collections Framework — Complete Theory & API Guide
> **Study goal:** One-stop reference. Internals, every useful method, complexity proofs, and interview insights.

---

## 1. The Core Hierarchy

```
Iterable<E>
 └── Collection<E>
      ├── SequencedCollection<E>   (Java 21+ Unified Ordered Hierarchy)
      │    ├── List<E>          — Ordered, allows duplicates, index-based access
      │    │    ├── ArrayList
      │    │    ├── LinkedList   (also implements Deque)
      │    │    └── Vector       (legacy, synchronized — avoid)
      │    │         └── Stack   (legacy — use ArrayDeque instead)
      │    ├── Deque<E>         — Double-ended queue
      │    │    ├── ArrayDeque   (preferred for stack/queue)
      │    │    └── LinkedList
      │    └── SequencedSet<E>  — Ordered Set preserving iteration
      │         ├── LinkedHashSet (insertion order)
      │         └── SortedSet / TreeSet (comparator order)
      │
      └── Set<E>           — No duplicates
           └── HashSet             (unordered)

Map<K,V>               — Key-value pairs (NOT part of Collection)
 ├── SequencedMap<K,V> (Java 21+ Sequenced Key-Value Mapping)
 │    ├── LinkedHashMap             (insertion/access order)
 │    └── SortedMap / TreeMap       (sorted keys, NavigableMap)
 ├── HashMap                   (unordered)
 ├── Hashtable                 (legacy, synchronized — avoid)
 └── ConcurrentHashMap         (thread-safe, high throughput)
```

### Key Interfaces Summary

| Interface | Ordered? | Duplicates? | Null? | Key Method |
|---|---|---|---|---|
| `List` | Yes (index) | Yes | Yes | `get(i)`, `set(i, e)` |
| `Set` | No* | No | One null (HashSet) | `add(e)` returns boolean |
| `Queue` | FIFO | Yes | No (PQ) | `offer()`, `poll()`, `peek()` |
| `Deque` | Both ends | Yes | No | `offerFirst/Last`, `pollFirst/Last` |
| `Map` | No* | Keys: No, Values: Yes | One null key (HashMap) | `put(k,v)`, `get(k)` |

*\* TreeSet/TreeMap are sorted. LinkedHashSet/LinkedHashMap maintain insertion order.*

---

## 2. ArrayList — Deep Dive

### Internals
- **Backing structure:** `Object[] elementData` — a plain resizable array.
- **Default initial capacity:** `10` (when first element is added).
- **Growth formula:** `newCapacity = oldCapacity + (oldCapacity >> 1)` → **1.5x growth**.
- **Why 1.5x?** Balance between wasted space (2x wastes ~50%) and frequent copies (1.25x copies too often). 1.5x wastes ~33% on average.

### Amortized O(1) Proof for add()
```
Each resize copies all n elements → cost n.
But resize happens only after n insertions since the last resize.
Using the aggregate method:
  Total cost for n insertions = n + n/2 + n/4 + ... + 1 ≈ 2n
  Amortized cost per insertion = 2n / n = O(1)
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
List<String> list = new ArrayList<>();              // empty, default capacity 10
List<String> list = new ArrayList<>(100);           // pre-sized (avoids resizing)
List<String> list = new ArrayList<>(otherList);     // copy constructor
List<String> list = List.of("a", "b", "c");         // immutable (Java 9+)
List<String> list = List.copyOf(mutableList);       // immutable copy (Java 10+)
List<String> list = new ArrayList<>(List.of("a"));  // mutable from immutable

// ─── Adding ─────────────────────────────────────────────────
list.add("x");               // append to end         — O(1) amortized
list.add(0, "x");            // insert at index       — O(n) shifts elements right
list.addAll(otherList);       // append all            — O(m) where m = other size
list.addAll(2, otherList);    // insert all at index   — O(n + m)

// ─── Accessing ──────────────────────────────────────────────
list.get(0);                  // by index              — O(1)
list.indexOf("x");            // first occurrence      — O(n) linear scan
list.lastIndexOf("x");        // last occurrence       — O(n) reverse scan
list.contains("x");           // membership check      — O(n) calls equals()
list.isEmpty();               // check empty           — O(1)
list.size();                  // element count         — O(1)

// ─── Modifying ──────────────────────────────────────────────
list.set(0, "y");             // replace at index      — O(1)
list.remove(0);               // remove by index       — O(n) shifts left
list.remove("x");             // remove first match    — O(n) find + shift
list.removeIf(s -> s.isEmpty()); // conditional remove — O(n)
list.replaceAll(String::toUpperCase); // transform all — O(n)
list.clear();                 // remove all            — O(n)

// ─── Sorting ────────────────────────────────────────────────
Collections.sort(list);                           // natural order (Comparable)
list.sort(Comparator.naturalOrder());             // same, Java 8+
list.sort(Comparator.reverseOrder());             // descending
list.sort(Comparator.comparingInt(String::length)); // by length

// ─── Sublist (View, not copy!) ──────────────────────────────
List<String> sub = list.subList(1, 3);  // [1, 3) — changes reflect in original!
sub.clear();  // removes elements at index 1,2 from the ORIGINAL list

// ─── Conversion ─────────────────────────────────────────────
String[] arr = list.toArray(new String[0]);       // List → Array
Object[] arr2 = list.toArray();                   // returns Object[]
List<String> fromArr = Arrays.asList(arr);        // Array → fixed-size List (view!)
List<String> fromArr2 = new ArrayList<>(Arrays.asList(arr)); // Array → mutable List

// ─── Iteration ──────────────────────────────────────────────
for (String s : list) { }                         // enhanced for-loop
list.forEach(System.out::println);                // Java 8 forEach
ListIterator<String> it = list.listIterator();    // bidirectional iterator
while (it.hasNext()) {
    String s = it.next();
    it.set(s.toUpperCase());  // modify during iteration (safe!)
    // it.remove();           // also safe
    // it.add("new");         // insert before next element
}

// ─── Streams ────────────────────────────────────────────────
list.stream().filter(s -> s.length() > 3).collect(Collectors.toList());
list.stream().map(String::toUpperCase).toList();  // Java 16+
```

### Complexity Table

| Operation | Time | Why |
|---|---|---|
| `get(i)` | **O(1)** | Direct array index |
| `add(e)` (end) | **O(1) amortized** | Append + rare resize |
| `add(i, e)` (middle) | **O(n)** | Shift elements right via `System.arraycopy` |
| `remove(i)` | **O(n)** | Shift elements left |
| `contains(e)` | **O(n)** | Linear scan with `equals()` |
| `indexOf(e)` | **O(n)** | Linear scan |
| `set(i, e)` | **O(1)** | Direct array write |
| `size()` | **O(1)** | Stored as field |
| `sort()` | **O(n log n)** | Timsort (hybrid merge+insertion sort) |

### Interview Pitfalls

1. **`Arrays.asList()` returns a fixed-size list.** Calling `add()` or `remove()` on it throws `UnsupportedOperationException`. Always wrap it: `new ArrayList<>(Arrays.asList(...))`.

2. **`remove(int)` vs `remove(Object)`:** For `List<Integer>`, `list.remove(1)` removes the element at index 1, NOT the value 1. To remove value 1: `list.remove(Integer.valueOf(1))`.

3. **Pre-size when you know the count:** `new ArrayList<>(n)` avoids ~log₁.₅(n) resize-and-copy cycles. For 1M elements, that's ~40 resizes avoided.

4. **`subList` is a VIEW.** Modifying the sublist modifies the original. Structural changes to the original after creating a sublist invalidate the sublist (throws `ConcurrentModificationException`).

---

## 3. LinkedList — Deep Dive

### Internals
- **Backing structure:** Doubly-linked list of `Node` objects.
- **Node layout:** Each node holds `item`, `prev`, `next` — **3 references + object header per element**.
- **Memory overhead:** ~40 bytes per node on 64-bit JVM (vs ~4 bytes per slot in ArrayList).
- **Implements:** `List<E>`, `Deque<E>`, `Queue<E>` — it's a Swiss army knife, but master of none.

```
 null ← [A] ⇄ [B] ⇄ [C] ⇄ [D] → null
          ↑ head              ↑ tail
          (first)             (last)
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
LinkedList<String> ll = new LinkedList<>();
LinkedList<String> ll = new LinkedList<>(otherCollection);

// ─── List Operations (index-based) ─────────────────────────
ll.get(3);                   // O(n) — traverses from head or tail (whichever closer)
ll.set(3, "x");              // O(n) — find node, then replace
ll.add(3, "x");              // O(n) — find position, O(1) link adjustment
ll.remove(3);                // O(n) — find position, O(1) unlink

// ─── Deque Operations (the real reason to use LinkedList) ───
ll.addFirst("x");            // O(1) — prepend
ll.addLast("x");             // O(1) — append (same as add())
ll.removeFirst();            // O(1) — pop from front, throws if empty
ll.removeLast();             // O(1) — pop from back, throws if empty
ll.getFirst();               // O(1) — peek front, throws if empty
ll.getLast();                 // O(1) — peek back, throws if empty

// ─── Queue Operations (FIFO) ───────────────────────────────
ll.offer("x");               // O(1) — enqueue (addLast), returns true
ll.poll();                    // O(1) — dequeue (removeFirst), returns null if empty
ll.peek();                    // O(1) — front element, returns null if empty

// ─── Stack Operations (LIFO) ───────────────────────────────
ll.push("x");                // O(1) — addFirst
ll.pop();                     // O(1) — removeFirst

// ─── Iteration & Removal ───────────────────────────────────
Iterator<String> it = ll.iterator();
while (it.hasNext()) {
    if (it.next().equals("remove_me")) {
        it.remove();          // O(1) — unlink node directly (no shifting!)
    }
}
// This is the ONE scenario where LinkedList removal is truly O(1):
// you already have a reference to the position via the iterator.

// ─── Descending Iteration ──────────────────────────────────
Iterator<String> desc = ll.descendingIterator();  // tail → head
while (desc.hasNext()) { desc.next(); }
```

### Complexity Table

| Operation | Time | Why |
|---|---|---|
| `addFirst/Last` | **O(1)** | Direct pointer update |
| `removeFirst/Last` | **O(1)** | Unlink head/tail node |
| `get(i)` | **O(n)** | Traverse from closer end (max n/2 steps) |
| `add(i, e)` | **O(n)** | Find position O(n), then link O(1) |
| `remove(i)` | **O(n)** | Find position O(n), then unlink O(1) |
| `contains(e)` | **O(n)** | Linear scan |
| `iterator.remove()` | **O(1)** | Already at the node |
| `size()` | **O(1)** | Stored as field |

### When to Actually Use LinkedList

Almost **never**. Here's the honest truth:

| Scenario | Better Choice | Why |
|---|---|---|
| Stack (LIFO) | `ArrayDeque` | Cache locality, no node overhead |
| Queue (FIFO) | `ArrayDeque` | Same reasons |
| Random access list | `ArrayList` | O(1) vs O(n) get |
| Frequent mid-list insert/remove | `ArrayList` | Surprisingly faster due to `System.arraycopy` using CPU memcpy |
| You need `null` elements in a Deque | `LinkedList` | ArrayDeque cannot store null |
| Constant-time iterator removal while traversing | `LinkedList` | Iterator.remove() is truly O(1) |

---

## 4. ArrayDeque — Deep Dive

### Internals
- **Backing structure:** Circular array (`Object[]`).
- **Pointers:** `head` and `tail` indices that wrap around using bitwise AND.
- **Default capacity:** 16 (must be power of 2).
- **Growth:** Doubles when full (`newCapacity = oldCapacity << 1`).
- **Null restriction:** Cannot store `null` — `null` is used as sentinel for `poll()`/`peek()`.

```
Array:  [ _ | _ | C | D | E | F | _ | _ ]
                  ↑ head          ↑ tail
  
After addFirst("B"):
Array:  [ _ | B | C | D | E | F | _ | _ ]
              ↑ head              ↑ tail

Wrap-around after addFirst("A"), addFirst("Z"):
Array:  [ A | B | C | D | E | F | Z | _ ]
              ↑ tail              ↑ head
  (head wrapped to index 6, Z placed there)
```

### Why Circular Array is Genius
- `addFirst`: `head = (head - 1) & (array.length - 1)` — **one bitwise op**, no shifting.
- `addLast`: `tail = (tail + 1) & (array.length - 1)` — same.
- Power-of-2 size ensures `& (length - 1)` works as modulo — cheaper than `%`.

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
Deque<String> dq = new ArrayDeque<>();           // default capacity 16
Deque<String> dq = new ArrayDeque<>(100);        // pre-sized
Deque<String> dq = new ArrayDeque<>(collection); // from collection

// ─── As a Stack (LIFO) — USE THIS, not java.util.Stack ─────
dq.push("x");                // O(1) — addFirst
dq.pop();                     // O(1) — removeFirst, throws if empty
dq.peek();                    // O(1) — peekFirst, returns null if empty

// ─── As a Queue (FIFO) — USE THIS, not LinkedList ──────────
dq.offer("x");               // O(1) — addLast
dq.poll();                    // O(1) — removeFirst, returns null if empty
dq.peek();                    // O(1) — peekFirst, returns null if empty

// ─── Double-ended Operations ────────────────────────────────
// Throwing versions (use when empty = bug):
dq.addFirst("x");            // O(1) — throws NoSuchElementException if full (never for ArrayDeque)
dq.addLast("x");             // O(1)
dq.removeFirst();            // O(1) — throws if empty
dq.removeLast();             // O(1) — throws if empty
dq.getFirst();               // O(1) — throws if empty
dq.getLast();                 // O(1) — throws if empty

// Safe versions (return null/false):
dq.offerFirst("x");          // O(1) — returns true
dq.offerLast("x");           // O(1) — returns true
dq.pollFirst();              // O(1) — returns null if empty
dq.pollLast();               // O(1) — returns null if empty
dq.peekFirst();              // O(1) — returns null if empty
dq.peekLast();               // O(1) — returns null if empty

// ─── Bulk / Utility ─────────────────────────────────────────
dq.size();                    // O(1)
dq.isEmpty();                 // O(1)
dq.contains("x");            // O(n) — linear scan
dq.clear();                   // O(n)
dq.toArray();                 // O(n)
```

### Complexity Table

| Operation | Time | Why |
|---|---|---|
| `push / pop` | **O(1)** | Head pointer adjustment |
| `offer / poll` | **O(1)** | Tail/head pointer adjustment |
| `peek` (first/last) | **O(1)** | Direct array access |
| `contains(e)` | **O(n)** | Linear scan |
| `size()` | **O(1)** | Computed from head/tail |

### Interview Insight: ArrayDeque vs LinkedList vs Stack

| | `ArrayDeque` | `LinkedList` | `Stack` (legacy) |
|---|---|---|---|
| Backing | Circular array | Doubly-linked nodes | `Vector` (synced array) |
| Cache perf | ★★★ Excellent | ★ Poor | ★★ OK (synced overhead) |
| Memory/element | ~4-8 bytes | ~40 bytes | ~4-8 bytes |
| Null elements | ✗ No | ✓ Yes | ✓ Yes |
| Thread-safe | ✗ No | ✗ No | ✓ Yes (but slow) |
| **Verdict** | **Default choice** | Last resort | Never use |

> **Google-level answer:** "I'll use ArrayDeque as my stack/queue. It's backed by a circular array with O(1) amortized push/pop via bitwise index wrapping. It has superior cache locality over LinkedList — contiguous memory means fewer L1/L2 cache misses. The only trade-off is it can't store nulls, which is rarely needed."

---

## 5. HashMap — Deep Dive

### Internals

- **Backing structure:** `Node<K,V>[] table` — an array of buckets.
- **Default capacity:** 16, **Load factor:** 0.75.
- **Threshold:** `capacity * loadFactor`. When `size > threshold`, table doubles.
- **Hash spreading:** `hash = (h = key.hashCode()) ^ (h >>> 16)` — mixes high bits into low bits to reduce collisions when table size is small.

#### Bucket Evolution (Java 8+)
```
Empty → Single Node → Linked List → Red-Black Tree
                              ↑ treeify when:
                                chain length ≥ 8 AND table capacity ≥ 64
                              ↓ untreeify when:
                                tree size ≤ 6 (during resize)
```

#### Why Load Factor = 0.75?
```
Poisson distribution at α=0.75:
  0 nodes: 0.47    ← almost half buckets empty
  1 node:  0.35
  2 nodes: 0.13
  8+ nodes: 0.00000006  ← treeification almost never happens
```

#### Resize (Rehash) Process
```
1. New table of size 2 * oldCapacity
2. Each node goes to either: same index OR same index + oldCapacity
   (determined by: hash & oldCapacity == 0 or not)
3. Cost: O(n) total, but amortized O(1) per put
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
Map<String, Integer> map = new HashMap<>();              // capacity 16, LF 0.75
Map<String, Integer> map = new HashMap<>(100);           // pre-sized
Map<String, Integer> map = new HashMap<>(otherMap);      // copy
Map<String, Integer> map = Map.of("a", 1, "b", 2);      // immutable (Java 9+)

// ─── Basic CRUD ─────────────────────────────────────────────
map.put("key", 1);            // insert/update, returns old value or null
map.get("key");               // returns value or null
map.getOrDefault("key", 0);   // ★ ALWAYS prefer over get()
map.containsKey("key");       // O(1) avg
map.containsValue(42);        // O(n) — scans ALL values! Avoid!
map.remove("key");            // returns old value
map.remove("key", 42);        // remove only if value matches (Java 8+)
map.size();                   // O(1)
map.isEmpty();                // O(1)

// ─── Java 8 Power Methods ★★★ ──────────────────────────────

// 1. putIfAbsent — Don't overwrite existing
map.putIfAbsent("key", 1);
// Use: store first occurrence index

// 2. merge — Combine old + new ★★★ (best for frequency counting)
map.merge("key", 1, Integer::sum);
// = map.put(k, map.containsKey(k) ? map.get(k) + 1 : 1);

// 3. computeIfAbsent — Lazy initialization ★★★ (best for grouping)
Map<String, List<String>> groups = new HashMap<>();
groups.computeIfAbsent(sortedKey, k -> new ArrayList<>()).add(word);

// 4. computeIfPresent — Update only if exists
map.computeIfPresent("key", (k, v) -> v + 1);

// 5. compute — Always compute (insert or update)
map.compute("key", (k, v) -> v == null ? 1 : v + 1);

// 6. replace — Update only if exists
map.replace("key", 99);
map.replace("key", 1, 99);   // CAS-like: only if current value is 1

// 7. replaceAll — Transform all values
map.replaceAll((k, v) -> v * 2);

// ─── Iteration ──────────────────────────────────────────────
// Entry set (most common)
for (Map.Entry<String, Integer> e : map.entrySet()) {
    e.getKey(); e.getValue();
}
for (String key : map.keySet()) { }       // keys only
for (int val : map.values()) { }          // values only
map.forEach((k, v) -> System.out.println(k + "=" + v));  // Java 8

// ─── Views (LIVE — changes reflect in original map) ────────
Set<String> keys = map.keySet();
Collection<Integer> vals = map.values();
Set<Map.Entry<String, Integer>> entries = map.entrySet();
```

### Complexity Table

| Operation | Average | Worst (Java 8+) |
|---|---|---|
| `put(k,v)` | **O(1)** amortized | O(log n) per bucket |
| `get(k)` | **O(1)** | O(log n) per bucket |
| `remove(k)` | **O(1)** | O(log n) per bucket |
| `containsKey(k)` | **O(1)** | O(log n) per bucket |
| `containsValue(v)` | **O(n)** | O(n) |
| Iterate all entries | **O(n + capacity)** | — |

### Interview Pitfalls

1. **Mutable keys break everything.** If `hashCode()` changes after insertion, the entry is lost in the wrong bucket.

2. **`containsValue()` is O(n).** Need fast value lookups? Maintain a reverse map.

3. **Iteration order NOT guaranteed.** Use `LinkedHashMap` for insertion order.

4. **`keySet()` is a LIVE VIEW.** `map.keySet().remove("x")` removes from the map.

5. **Pre-sizing formula:** `new HashMap<>(n * 4 / 3 + 1)` for `n` entries avoids all resizes.

---

## 6. HashSet — Deep Dive

### Internals
- **It IS a HashMap.** Source code proof:
```java
public class HashSet<E> {
    private HashMap<E, Object> map;
    private static final Object PRESENT = new Object();  // dummy

    public boolean add(E e)         { return map.put(e, PRESENT) == null; }
    public boolean remove(Object o) { return map.remove(o) == PRESENT; }
    public boolean contains(Object o) { return map.containsKey(o); }
}
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
Set<String> set = new HashSet<>();
Set<String> set = new HashSet<>(100);            // pre-sized
Set<String> set = new HashSet<>(collection);     // deduplicates
Set<String> set = Set.of("a", "b", "c");         // immutable (Java 9+)

// ─── Core Operations ────────────────────────────────────────
set.add("x");           // O(1) — returns TRUE if new, FALSE if duplicate ★
set.remove("x");        // O(1) — returns true if removed
set.contains("x");      // O(1)
set.size();             // O(1)
set.isEmpty();          // O(1)

// ─── add() Return Value Trick ★★★ ───────────────────────────
// Single O(1) op for duplicate detection. Do NOT do contains() + add().
if (!set.add(element)) { /* duplicate! */ }

// ─── Bulk Set Operations ────────────────────────────────────
Set<String> a = new HashSet<>(List.of("x", "y", "z"));
Set<String> b = new HashSet<>(List.of("y", "z", "w"));

// Union (a ∪ b)
Set<String> union = new HashSet<>(a);
union.addAll(b);              // {"x", "y", "z", "w"}

// Intersection (a ∩ b)
Set<String> intersect = new HashSet<>(a);
intersect.retainAll(b);       // {"y", "z"}

// Difference (a - b)
Set<String> diff = new HashSet<>(a);
diff.removeAll(b);            // {"x"}

// ─── Conditional Removal ────────────────────────────────────
set.removeIf(s -> s.length() > 3);

// ─── Conversion ─────────────────────────────────────────────
List<String> list = new ArrayList<>(set);                // Set → List
String[] arr = set.toArray(new String[0]);               // Set → Array
Set<String> fromArr = new HashSet<>(Arrays.asList(arr)); // Array → Set
int uniqueCount = new HashSet<>(someList).size();        // Count uniques (one-liner)
```

### HashSet vs LinkedHashSet vs TreeSet

| | `HashSet` | `LinkedHashSet` | `TreeSet` |
|---|---|---|---|
| Order | None | Insertion order | Sorted |
| Backing | HashMap | LinkedHashMap | TreeMap |
| `add/remove/contains` | O(1) | O(1) | O(log n) |
| Null | 1 null ok | 1 null ok | No null |
| **Use when** | Default | Need iteration order | Need sorting/ranges |

### Interview Patterns

```java
// Pattern 1: Duplicate detection
if (!seen.add(num)) return true;

// Pattern 2: O(1) lookup table
Set<String> dict = new HashSet<>(wordList);
if (dict.contains(word)) { /* valid */ }

// Pattern 3: Count uniques
int unique = new HashSet<>(list).size();

// Pattern 4: Set difference for missing elements
Set<Integer> setA = new HashSet<>(listA);
for (int n : listB) { if (!setA.contains(n)) { /* missing */ } }
```

---

## 7. TreeMap — Deep Dive

### Internals

- **Backing structure:** Red-Black Tree (self-balancing BST).
- **Ordering:** Keys sorted by natural order (`Comparable`) or a provided `Comparator`.
- **Null keys:** NOT allowed (throws `NullPointerException` — comparisons fail).
- **Null values:** Allowed.

#### Red-Black Tree Properties
```
1. Every node is either RED or BLACK.
2. Root is always BLACK.
3. Every leaf (NIL) is BLACK.
4. If a node is RED, both its children are BLACK (no red-red).
5. Every path from root to leaf has the same number of BLACK nodes.

These properties guarantee:
  Height ≤ 2 * log₂(n + 1)
  → All operations O(log n) WORST case (not amortized!)
```

#### Why Not AVL Tree?
```
AVL is strictly balanced (height diff ≤ 1) → faster lookups.
Red-Black is approximately balanced → fewer rotations on insert/delete.
Java chose Red-Black because insert/delete-heavy workloads are common.
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
TreeMap<Integer, String> tm = new TreeMap<>();                  // natural order
TreeMap<Integer, String> tm = new TreeMap<>(Comparator.reverseOrder()); // custom
TreeMap<Integer, String> tm = new TreeMap<>(otherMap);          // copy (re-sorted)
TreeMap<Integer, String> tm = new TreeMap<>(otherSortedMap);    // copy (keeps order)

// ─── Basic CRUD (same as HashMap but O(log n)) ─────────────
tm.put(5, "five");            // O(log n)
tm.get(5);                    // O(log n) — returns null if absent
tm.getOrDefault(5, "N/A");   // O(log n)
tm.containsKey(5);            // O(log n)
tm.remove(5);                 // O(log n)
tm.size();                    // O(1) — stored as field

// ─── Boundary Queries ★★★ (WHY you choose TreeMap) ─────────

// Floor/Ceiling — find nearest key
tm.floorKey(7);               // largest key ≤ 7   (or null)
tm.ceilingKey(3);             // smallest key ≥ 3  (or null)
tm.lowerKey(5);               // largest key < 5   (strictly less)
tm.higherKey(5);              // smallest key > 5  (strictly greater)

// Same but returning full entries
tm.floorEntry(7);             // Map.Entry with largest key ≤ 7
tm.ceilingEntry(3);           // Map.Entry with smallest key ≥ 3
tm.lowerEntry(5);
tm.higherEntry(5);

// First/Last (min/max key)
tm.firstKey();                // smallest key — throws if empty
tm.lastKey();                 // largest key — throws if empty
tm.firstEntry();              // entry with smallest key
tm.lastEntry();               // entry with largest key

// Poll (remove + return min/max)
tm.pollFirstEntry();          // remove and return min entry — O(log n)
tm.pollLastEntry();           // remove and return max entry — O(log n)

// ─── Range Views ★★★ (the killer feature) ───────────────────

// subMap — keys in [fromKey, toKey)
SortedMap<Integer, String> sub = tm.subMap(3, 7);     // keys 3,4,5,6
// With inclusive flags (NavigableMap):
NavigableMap<Integer, String> sub = tm.subMap(3, true, 7, true); // keys 3,4,5,6,7

// headMap — keys < toKey
SortedMap<Integer, String> head = tm.headMap(5);      // keys < 5
NavigableMap<Integer, String> head = tm.headMap(5, true); // keys ≤ 5

// tailMap — keys ≥ fromKey
SortedMap<Integer, String> tail = tm.tailMap(5);      // keys ≥ 5
NavigableMap<Integer, String> tail = tm.tailMap(5, false); // keys > 5

// CRITICAL: These are VIEWS — backed by the original map!
// Changes to the view change the original, and vice versa.
sub.put(4, "four");  // modifies tm
tm.put(5, "FIVE");   // visible through sub

// ─── Reverse & Descending ───────────────────────────────────
NavigableMap<Integer, String> desc = tm.descendingMap();  // reversed view
NavigableSet<Integer> descKeys = tm.descendingKeySet();   // reversed key set

// ─── Iteration (always in sorted order) ─────────────────────
for (Map.Entry<Integer, String> e : tm.entrySet()) { }     // ascending
for (Integer key : tm.descendingKeySet()) { }               // descending
tm.forEach((k, v) -> System.out.println(k + "=" + v));     // ascending
```

### Complexity Table

| Operation | Time | Notes |
|---|---|---|
| `put / get / remove` | **O(log n)** | Red-Black Tree ops |
| `containsKey` | **O(log n)** | Tree search |
| `firstKey / lastKey` | **O(log n)** | Walk to leftmost/rightmost |
| `floorKey / ceilingKey` | **O(log n)** | Tree search |
| `subMap / headMap / tailMap` | **O(log n)** | View creation is O(log n), iteration is O(k) |
| `pollFirstEntry / pollLastEntry` | **O(log n)** | Find + delete + rebalance |
| Iterate all entries | **O(n)** | In-order traversal |

### Interview Use Cases

```java
// Use Case 1: Calendar booking (find overlapping intervals)
// "Is there an event that overlaps with [start, end)?"
TreeMap<Integer, Integer> calendar = new TreeMap<>(); // start → end
Integer prev = calendar.floorKey(start);   // event starting before
Integer next = calendar.ceilingKey(start); // event starting after
if (prev != null && calendar.get(prev) > start) return false; // overlap
if (next != null && next < end) return false;                  // overlap

// Use Case 2: Time-based key-value store
// "Get the value at the largest timestamp ≤ given timestamp"
TreeMap<Integer, String> timestore = new TreeMap<>();
Map.Entry<Integer, String> entry = timestore.floorEntry(timestamp);
return entry == null ? "" : entry.getValue();

// Use Case 3: Counting sort / rank queries
// "How many elements are ≤ x?"
TreeMap<Integer, Integer> freq = new TreeMap<>();
int count = freq.headMap(x, true).values().stream().mapToInt(i -> i).sum();

// Use Case 4: Sliding window max/min
// Maintain a TreeMap of window elements with frequencies
TreeMap<Integer, Integer> window = new TreeMap<>();
// window.firstKey() = min, window.lastKey() = max at all times
```

---

## 8. TreeSet — Deep Dive

### Internals
- **It IS a TreeMap.** Same pattern as HashSet → HashMap:
```java
public class TreeSet<E> {
    private transient NavigableMap<E, Object> m;   // TreeMap
    private static final Object PRESENT = new Object();

    public boolean add(E e)         { return m.put(e, PRESENT) == null; }
    public boolean remove(Object o) { return m.remove(o) == PRESENT; }
    public boolean contains(Object o) { return m.containsKey(o); }
}
```
- **Ordering:** Elements must implement `Comparable` or provide a `Comparator`.
- **Null:** NOT allowed.

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
TreeSet<Integer> ts = new TreeSet<>();                         // natural order
TreeSet<Integer> ts = new TreeSet<>(Comparator.reverseOrder()); // descending
TreeSet<Integer> ts = new TreeSet<>(collection);               // sorted copy

// ─── Core Set Operations (O(log n) each) ────────────────────
ts.add(5);                    // insert — O(log n)
ts.remove(5);                 // remove — O(log n)
ts.contains(5);               // membership — O(log n)
ts.size();                    // O(1)
ts.isEmpty();                 // O(1)

// ─── Boundary Queries ★★★ ───────────────────────────────────
ts.floor(7);                  // largest element ≤ 7    (or null)
ts.ceiling(3);                // smallest element ≥ 3   (or null)
ts.lower(5);                  // largest element < 5    (strictly less)
ts.higher(5);                 // smallest element > 5   (strictly greater)

// First / Last (min / max)
ts.first();                   // smallest — throws if empty
ts.last();                    // largest — throws if empty

// Poll (remove + return)
ts.pollFirst();               // remove and return min — O(log n)
ts.pollLast();                // remove and return max — O(log n)

// ─── Range Views ★★★ ────────────────────────────────────────
NavigableSet<Integer> sub = ts.subSet(3, true, 7, true);  // {3,4,5,6,7}
SortedSet<Integer> head = ts.headSet(5);                   // elements < 5
SortedSet<Integer> tail = ts.tailSet(5);                   // elements ≥ 5

// These are VIEWS — backed by the original set
sub.add(4);     // modifies ts
ts.remove(5);   // reflected in sub

// ─── Descending ─────────────────────────────────────────────
NavigableSet<Integer> desc = ts.descendingSet();           // reversed view
Iterator<Integer> descIt = ts.descendingIterator();        // reversed iterator

// ─── Iteration (always sorted) ──────────────────────────────
for (int x : ts) { }                     // ascending
for (int x : ts.descendingSet()) { }     // descending
ts.forEach(System.out::println);
```

### Complexity Table

| Operation | Time |
|---|---|
| `add / remove / contains` | **O(log n)** |
| `first / last` | **O(log n)** |
| `floor / ceiling / lower / higher` | **O(log n)** |
| `pollFirst / pollLast` | **O(log n)** |
| `subSet / headSet / tailSet` | **O(log n)** view creation |
| Iterate all | **O(n)** |

### floor/ceiling/lower/higher Cheat Sheet

```
Given set: {2, 5, 8, 12, 15}    Query: 8

  lower(8)   = 5     ← strictly less than
  floor(8)   = 8     ← less than or equal (INCLUDES 8)
  ceiling(8) = 8     ← greater than or equal (INCLUDES 8)
  higher(8)  = 12    ← strictly greater than

Given set: {2, 5, 8, 12, 15}    Query: 9 (not in set)

  lower(9)   = 8     ← largest < 9
  floor(9)   = 8     ← largest ≤ 9
  ceiling(9) = 12    ← smallest ≥ 9
  higher(9)  = 12    ← smallest > 9

Given set: {2, 5, 8, 12, 15}    Query: 1 (below all)

  lower(1)   = null
  floor(1)   = null
  ceiling(1) = 2
  higher(1)  = 2
```

### Interview Patterns

```java
// Pattern 1: Maintaining sorted unique elements with O(log n) operations
TreeSet<Integer> ts = new TreeSet<>();
ts.add(5); ts.add(3); ts.add(8);
int min = ts.first();  // 3
int max = ts.last();   // 8

// Pattern 2: Contains Duplicate III (LeetCode 220)
// "Are there two elements within index distance k whose values differ by at most t?"
TreeSet<Long> window = new TreeSet<>();
for (int i = 0; i < nums.length; i++) {
    Long floor = window.floor((long) nums[i] + t);
    if (floor != null && floor >= (long) nums[i] - t) return true;
    window.add((long) nums[i]);
    if (window.size() > k) window.remove((long) nums[i - k]);
}

// Pattern 3: Count elements in a range
int count = ts.subSet(lo, true, hi, true).size();
// WARNING: .size() on a subSet view is O(n) — it counts by iterating!
// For O(log n) rank queries, consider an augmented BST or BIT.

// Pattern 4: Ordered sliding window (max - min)
TreeMap<Integer, Integer> windowMap = new TreeMap<>();  // val → freq
// windowMap.lastKey() - windowMap.firstKey() = current window range
```


## 9. PriorityQueue — Deep Dive

### Internals

- **Backing structure:** Binary Min-Heap implemented as `Object[]` array.
- **Default capacity:** 11.
- **Growth:** Doubles when size < 64, else grows by 50%.
- **Null:** NOT allowed (comparisons fail).
- **Ordering:** Natural order (`Comparable`) or provided `Comparator`.

#### Heap Array Layout
```
Array:  [10, 15, 30, 40, 50, 100, 40]

Tree view:
              10          ← index 0 (root = minimum)
            /    \
          15      30      ← indices 1, 2
         /  \   /  \
        40  50 100  40    ← indices 3, 4, 5, 6

Parent of i:     (i - 1) / 2
Left child of i:  2 * i + 1
Right child of i: 2 * i + 2
```

#### Sift-Up (on offer/add)
```
1. Place new element at the end of the array (next leaf position).
2. Compare with parent. If smaller, swap up.
3. Repeat until heap property restored or root reached.
Time: O(log n) — at most height of tree swaps.
```

#### Sift-Down (on poll/remove)
```
1. Remove root (min element).
2. Move last element to root position.
3. Compare with smaller child. If larger, swap down.
4. Repeat until heap property restored or leaf reached.
Time: O(log n) — at most height of tree swaps.
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
// Min-Heap (default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();

// Max-Heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
// or: new PriorityQueue<>(Comparator.reverseOrder());

// Pre-sized
PriorityQueue<Integer> pq = new PriorityQueue<>(100);

// Custom Comparator (e.g., sort by second element of array)
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);

// From collection (heapify in O(n))
PriorityQueue<Integer> pq = new PriorityQueue<>(existingCollection);

// ─── Core Operations ────────────────────────────────────────
pq.offer(5);                  // O(log n) — add + sift-up
pq.add(5);                    // O(log n) — same as offer (throws on capacity limit, but PQ has none)
pq.peek();                    // O(1) — view min without removing (null if empty)
pq.element();                 // O(1) — same but throws if empty
pq.poll();                    // O(log n) — remove + return min (null if empty)
pq.remove();                  // O(log n) — same but throws if empty

// ─── Search & Remove (EXPENSIVE) ────────────────────────────
pq.contains(5);               // O(n) — linear scan through array
pq.remove(5);                 // O(n) — find O(n) + sift O(log n)
// WARNING: remove(Object) removes ONE occurrence, not all.
// For removing by value efficiently, consider TreeMap instead.

// ─── Bulk Operations ────────────────────────────────────────
pq.size();                    // O(1)
pq.isEmpty();                 // O(1)
pq.clear();                   // O(n)
pq.toArray();                 // O(n) — NOT in sorted order!

// ─── Iteration (NOT sorted!) ────────────────────────────────
for (int x : pq) { }         // Iterates in ARBITRARY order, NOT sorted!
// If you need sorted iteration:
while (!pq.isEmpty()) {
    System.out.println(pq.poll());  // polls in sorted order
}
```

### Complexity Table

| Operation | Time | Why |
|---|---|---|
| `offer(e)` / `add(e)` | **O(log n)** | Sift-up |
| `peek()` | **O(1)** | Array index 0 |
| `poll()` | **O(log n)** | Replace root + sift-down |
| `remove(Object)` | **O(n)** | Linear search + sift |
| `contains(Object)` | **O(n)** | Linear search |
| `size()` | **O(1)** | Stored field |
| Heapify (constructor from collection) | **O(n)** | Bottom-up heap build |

### Why Heapify is O(n), Not O(n log n)

```
Intuitive proof:
- Leaves (n/2 nodes) need 0 swaps.
- Level above leaves (n/4 nodes) need at most 1 swap each.
- Level above that (n/8 nodes) need at most 2 swaps each.
- ...
- Root (1 node) needs at most log(n) swaps.

Total = n/4 * 1 + n/8 * 2 + n/16 * 3 + ... + 1 * log(n)
      = n * Σ(k / 2^(k+1)) for k = 1 to log(n)
      = n * (converges to 2)
      = O(n)
```

### Interview Patterns

```java
// Pattern 1: Top K elements — O(n log k) with size-k min-heap
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
for (int num : nums) {
    minHeap.offer(num);
    if (minHeap.size() > k) minHeap.poll();  // evict smallest
}
// minHeap now contains the k largest elements

// Pattern 2: Kth Largest Element — same as above, peek at end
// After processing all nums: minHeap.peek() = kth largest

// Pattern 3: Merge K Sorted Lists — min-heap of list heads
PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
for (ListNode head : lists) if (head != null) pq.offer(head);
while (!pq.isEmpty()) {
    ListNode node = pq.poll();
    // add node to result
    if (node.next != null) pq.offer(node.next);
}

// Pattern 4: Median from Data Stream — two heaps
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
// maxHeap stores smaller half, minHeap stores larger half
// Balance: maxHeap.size() == minHeap.size() or maxHeap.size() == minHeap.size() + 1
// Median = maxHeap.peek() or avg of both peeks

// Pattern 5: Task Scheduler / Greedy frequency-based
// Build freq map, put all freqs in maxHeap, greedily pick highest freq task
```

### Interview Pitfall: Iteration Order

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 3, 2, 4));

// WRONG — prints in arbitrary heap order, NOT 1,2,3,4,5
for (int x : pq) System.out.print(x + " "); // might print: 1 2 3 5 4

// CORRECT — polls in sorted order
while (!pq.isEmpty()) System.out.print(pq.poll() + " "); // prints: 1 2 3 4 5
```

---

## 10. LinkedHashMap — Deep Dive

### Internals

- **Extends:** `HashMap` — inherits all hash table behavior.
- **Extra structure:** Doubly-linked list threading through all entries.
- **Each entry** has `before` and `after` pointers in addition to the HashMap `next` pointer.

```
Hash Table Buckets:      Doubly-Linked List (insertion order):
 [0] → EntryA              HEAD ⇄ EntryA ⇄ EntryC ⇄ EntryB ⇄ TAIL
 [1] → EntryB                     (first)                (last)
 [2] → (empty)
 [3] → EntryC

Each Entry lives in BOTH structures simultaneously.
```

#### Two Ordering Modes
```java
// Mode 1: Insertion order (default)
new LinkedHashMap<>();                    // or (capacity, loadFactor, false)

// Mode 2: Access order (LRU mode) ★★★
new LinkedHashMap<>(capacity, 0.75f, true);
// Every get() or put() moves the entry to the TAIL of the linked list.
// HEAD = least recently used, TAIL = most recently used.
```

### Complete API Reference

```java
// ─── Creation ───────────────────────────────────────────────
// Insertion-order (default)
Map<String, Integer> lhm = new LinkedHashMap<>();
Map<String, Integer> lhm = new LinkedHashMap<>(32);            // pre-sized
Map<String, Integer> lhm = new LinkedHashMap<>(otherMap);      // copy

// Access-order (for LRU cache) ★★★
Map<String, Integer> lhm = new LinkedHashMap<>(16, 0.75f, true);

// ─── All HashMap methods work exactly the same ──────────────
lhm.put("a", 1);
lhm.get("a");
lhm.getOrDefault("a", 0);
lhm.containsKey("a");
lhm.remove("a");
lhm.merge("a", 1, Integer::sum);
lhm.computeIfAbsent("a", k -> 42);
// ... every HashMap method is inherited

// ─── The difference: ITERATION ORDER IS GUARANTEED ──────────
lhm.put("banana", 2);
lhm.put("apple", 1);
lhm.put("cherry", 3);

for (String key : lhm.keySet()) {
    System.out.print(key + " ");  // banana apple cherry (insertion order!)
}
// HashMap would print in arbitrary order.

// ─── Access Order Demo ──────────────────────────────────────
Map<String, Integer> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
accessOrder.put("a", 1);  // order: a
accessOrder.put("b", 2);  // order: a, b
accessOrder.put("c", 3);  // order: a, b, c
accessOrder.get("a");     // ACCESS moves "a" to tail → order: b, c, a
accessOrder.put("b", 99); // PUT moves "b" to tail   → order: c, a, b

// ─── LRU Cache Pattern ★★★ (Simplest Implementation) ───────
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);  // access-order = true
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;  // auto-evict when over capacity
    }
}
// Usage:
// LRUCache<Integer, String> cache = new LRUCache<>(3);
// cache.put(1, "a"); cache.put(2, "b"); cache.put(3, "c");
// cache.get(1);       // access "1" → moves to tail
// cache.put(4, "d");  // evicts "2" (eldest = least recently used)
```

### Complexity Table

| Operation | Time | Notes |
|---|---|---|
| `put / get / remove` | **O(1)** amortized | Same as HashMap + O(1) linked list maintenance |
| `containsKey` | **O(1)** | Same as HashMap |
| Iterate all entries | **O(n)** | Follows linked list (no wasted capacity slots!) |

**Key advantage over HashMap iteration:** HashMap iterates over `O(n + capacity)` — it scans empty buckets too. LinkedHashMap iterates over `O(n)` — it follows the linked list directly.

### LinkedHashMap vs HashMap vs TreeMap

| | `HashMap` | `LinkedHashMap` | `TreeMap` |
|---|---|---|---|
| Order | None | Insertion or Access | Sorted by key |
| `put/get` | O(1) | O(1) | O(log n) |
| Iteration | O(n + capacity) | **O(n)** ★ | O(n) |
| Memory overhead | Low | Medium (2 extra ptrs/entry) | High (tree nodes) |
| **Use when** | Default | Need order or LRU | Need sorting/ranges |

### Interview Talking Points

> **When asked about LRU Cache:**
> "The simplest Java implementation is extending LinkedHashMap with `accessOrder=true` and overriding `removeEldestEntry`. This gives O(1) get/put with automatic eviction in ~10 lines of code. For a from-scratch implementation, I'd use a HashMap + custom doubly-linked list — same O(1) complexity but shows deeper understanding."

> **When asked about insertion-order iteration:**
> "LinkedHashMap maintains a doubly-linked list threading through all entries. It costs 2 extra pointers per entry but gives O(n) iteration without scanning empty buckets, unlike HashMap's O(n + capacity)."

---

## 11. The equals() and hashCode() Contract

### The Rule (Memorize This)

```
1. If a.equals(b) is TRUE  → a.hashCode() MUST == b.hashCode()
2. If a.hashCode() == b.hashCode() → a.equals(b) may be true OR false (collision)
3. If a.equals(b) is FALSE → hashCodes CAN be equal (but shouldn't be, for performance)
```

### Why This Matters — The Disaster Scenario

```java
class Employee {
    String name;
    int id;

    // ❌ ONLY overrides equals, NOT hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee e = (Employee) o;
        return id == e.id && Objects.equals(name, e.name);
    }
    // hashCode() inherited from Object → returns memory address
}

Employee e1 = new Employee("Alice", 1);
Employee e2 = new Employee("Alice", 1);

e1.equals(e2);              // true ✓

Set<Employee> set = new HashSet<>();
set.add(e1);
set.contains(e2);           // FALSE! ← BUG

Map<Employee, String> map = new HashMap<>();
map.put(e1, "engineer");
map.get(e2);                // null! ← BUG

// WHY?
// e1.hashCode() != e2.hashCode() (different memory addresses)
// HashMap looks in the WRONG bucket for e2 → never finds e1
```

### Correct Implementation

```java
class Employee {
    String name;
    int id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee e = (Employee) o;
        return id == e.id && Objects.equals(name, e.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);  // ★ Uses same fields as equals()
    }
}

// Now:
// e1.equals(e2) → true
// e1.hashCode() == e2.hashCode() → true (same bucket)
// set.contains(e2) → true ✓
// map.get(e2) → "engineer" ✓
```

### Rules for a Good hashCode()

```
1. Use the SAME fields as equals().
2. Consistent: must return same value if object hasn't changed.
3. Use Objects.hash(field1, field2, ...) — good enough for interviews.
4. For performance-critical code: manual prime multiplication.
```

```java
// Manual hashCode (what Objects.hash does internally, roughly)
@Override
public int hashCode() {
    int result = 17;                    // arbitrary non-zero prime
    result = 31 * result + id;          // 31 because: 31 * i == (i << 5) - i (JIT optimizes)
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
}
```

### Interview Quick Answer

> "The contract states: if two objects are equal via `equals()`, they must have the same `hashCode()`. If you break this, HashMap and HashSet silently malfunction — the object goes into the right bucket on `put`, but `get` looks in a different bucket because the hash differs. Always override both together, using the same fields."

---

## 12. Comparable vs Comparator

### Comparable — Natural Ordering (built into the class)

```java
// The class itself defines its default sort order
class Student implements Comparable<Student> {
    String name;
    int grade;

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.grade, other.grade); // ascending by grade
    }
    // Return: negative (this < other), 0 (equal), positive (this > other)
}

// Usage — no Comparator needed
List<Student> students = new ArrayList<>();
Collections.sort(students);                    // uses compareTo()
TreeSet<Student> sorted = new TreeSet<>(students); // uses compareTo()
```

### Comparator — Custom/External Ordering

```java
// Define ordering outside the class — flexible, multiple orderings

// Lambda (most common in interviews)
students.sort((a, b) -> a.name.compareTo(b.name));       // by name
students.sort((a, b) -> Integer.compare(b.grade, a.grade)); // by grade DESC

// Comparator.comparing (cleanest Java 8+)
students.sort(Comparator.comparing(s -> s.name));         // by name
students.sort(Comparator.comparingInt(s -> s.grade));     // by grade (avoids boxing)

// Chained comparators
students.sort(
    Comparator.comparingInt((Student s) -> s.grade)
              .reversed()                                  // descending grade
              .thenComparing(s -> s.name)                  // then ascending name
);

// Pre-built comparators
Comparator.naturalOrder();       // ascending (Comparable)
Comparator.reverseOrder();       // descending
Comparator.nullsFirst(comp);     // nulls before non-nulls
Comparator.nullsLast(comp);      // nulls after non-nulls
```

### Comparable vs Comparator Summary

| | `Comparable` | `Comparator` |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T other)` | `compare(T a, T b)` |
| Modifies class? | Yes (implements interface) | No (external) |
| # of orderings | 1 (natural order) | Unlimited |
| Use when | Default ordering makes sense | Need multiple or ad-hoc orderings |

### Interview Pitfall: compareTo Consistency with equals

```
If compareTo() returns 0 but equals() returns false, TreeSet/TreeMap
will treat them as the same key (TreeSet uses compareTo, not equals).
This can cause silent data loss.

Rule: If a.compareTo(b) == 0, then a.equals(b) should also be true.
Exception: It's "strongly recommended" but not enforced by the compiler.
```

---

## 13. Fail-Fast vs Fail-Safe Iterators

### Fail-Fast (ArrayList, HashMap, HashSet, LinkedList, TreeMap, TreeSet)

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));

// ❌ THROWS ConcurrentModificationException
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);  // structural modification during iteration!
    }
}
```

#### How It Works Internally

```
ArrayList has a field: int modCount = 0;

Every structural change (add, remove, clear) increments modCount.

When an Iterator is created, it snapshots: expectedModCount = modCount.

On every iterator.next() call:
  if (modCount != expectedModCount)
      throw new ConcurrentModificationException();
```

#### Safe Removal Patterns

```java
// Pattern 1: Iterator.remove() — PRE-Java 8
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) {
        it.remove();  // Safe! Updates modCount AND expectedModCount
    }
}

// Pattern 2: removeIf() — Java 8+ (CLEANEST)
list.removeIf(s -> s.equals("b"));

// Pattern 3: Collect indices, then remove in reverse
List<Integer> toRemove = new ArrayList<>();
for (int i = 0; i < list.size(); i++) {
    if (list.get(i).equals("b")) toRemove.add(i);
}
for (int i = toRemove.size() - 1; i >= 0; i--) {
    list.remove((int) toRemove.get(i));  // reverse order avoids index shifting issues
}

// Pattern 4: Copy-and-filter (functional style)
list = list.stream().filter(s -> !s.equals("b")).collect(Collectors.toList());
```

#### Safe Removal for Maps

```java
Map<String, Integer> map = new HashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// ❌ THROWS ConcurrentModificationException
for (String key : map.keySet()) {
    if (map.get(key) > 1) map.remove(key);
}

// ✓ Pattern 1: Iterator
Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
while (it.hasNext()) {
    if (it.next().getValue() > 1) it.remove();
}

// ✓ Pattern 2: removeIf on entrySet (Java 8+)
map.entrySet().removeIf(e -> e.getValue() > 1);

// ✓ Pattern 3: values().removeIf
map.values().removeIf(v -> v > 1);
```

### Fail-Safe / Weakly Consistent (ConcurrentHashMap, CopyOnWriteArrayList)

```java
// ConcurrentHashMap — safe to modify during iteration
ConcurrentHashMap<String, Integer> cmap = new ConcurrentHashMap<>();
cmap.put("a", 1); cmap.put("b", 2);

for (String key : cmap.keySet()) {
    cmap.remove(key);  // No exception! Weakly consistent iterator.
}
// But: iterator may or may not reflect concurrent modifications.

// CopyOnWriteArrayList — iterator works on a SNAPSHOT
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b"));
for (String s : cowList) {
    cowList.add("c");  // No exception! Modifies a NEW internal array.
}
// Iterator still sees ["a", "b"] — it was created before the add.
// cowList is now ["a", "b", "c"].
```

### Summary Table

| Collection | Iterator Type | On Modification During Iteration |
|---|---|---|
| `ArrayList` | Fail-Fast | `ConcurrentModificationException` |
| `LinkedList` | Fail-Fast | `ConcurrentModificationException` |
| `HashMap` | Fail-Fast | `ConcurrentModificationException` |
| `HashSet` | Fail-Fast | `ConcurrentModificationException` |
| `TreeMap` / `TreeSet` | Fail-Fast | `ConcurrentModificationException` |
| `ConcurrentHashMap` | Weakly Consistent | No exception, may miss updates |
| `CopyOnWriteArrayList` | Snapshot | No exception, sees old data |

---

## 14. Collections Utility Class — Quick Reference

```java
// ─── Sorting ────────────────────────────────────────────────
Collections.sort(list);                           // natural order
Collections.sort(list, Comparator.reverseOrder()); // custom
Collections.reverse(list);                        // in-place reverse

// ─── Searching ──────────────────────────────────────────────
Collections.binarySearch(sortedList, key);        // O(log n) — list MUST be sorted!
Collections.frequency(collection, element);       // count occurrences — O(n)
Collections.disjoint(c1, c2);                     // true if no common elements

// ─── Min / Max ──────────────────────────────────────────────
Collections.min(collection);                      // by natural order
Collections.max(collection);                      // by natural order
Collections.min(collection, comparator);          // custom

// ─── Fill / Copy / Swap ─────────────────────────────────────
Collections.fill(list, value);                    // set all elements
Collections.copy(dest, src);                      // dest.size() must >= src.size()
Collections.swap(list, i, j);                     // swap two elements
Collections.shuffle(list);                        // random permutation
Collections.rotate(list, distance);               // rotate right by distance

// ─── Immutable Wrappers ─────────────────────────────────────
List<String> immutable = Collections.unmodifiableList(list);    // read-only VIEW
Set<String> immutable = Collections.unmodifiableSet(set);
Map<K,V> immutable = Collections.unmodifiableMap(map);
// Note: Java 9+ List.of(), Set.of(), Map.of() are preferred.

// ─── Synchronized Wrappers (avoid — use concurrent collections) ──
List<String> synced = Collections.synchronizedList(list);
Map<K,V> synced = Collections.synchronizedMap(map);
// Locks entire collection on every operation. Poor throughput.
// Prefer: ConcurrentHashMap, CopyOnWriteArrayList

// ─── Singleton / Empty ──────────────────────────────────────
List<String> empty = Collections.emptyList();     // immutable empty
Set<String> empty = Collections.emptySet();
Map<K,V> empty = Collections.emptyMap();
List<String> single = Collections.singletonList("x"); // immutable single-element
```

---

*This concludes the Java Collections theory guide. Proceed to the problem files:*

- → [01_easy.java](01_easy.java) — Easy problems applying basic collection patterns
- → [02_medium.java](02_medium.java) — Medium problems with iterators, heaps, and TreeMap
- → [03_hard.java](03_hard.java) — Hard problems: LFU Cache, RandomizedSet, Custom HashMap
- → [04_google_level.java](04_google_level.java) — Google-level: TimeMap, SnapshotArray, MaxStack
- → [05_cheatsheet.md](05_cheatsheet.md) — Quick reference & interview cheatsheet
