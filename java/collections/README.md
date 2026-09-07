# Java Collections Framework (JCF) Masterclass Suite

Welcome to the **Java Collections Framework (JCF) Masterclass Suite**. This module delivers a staff/principal-engineer level deep dive into Java's data structures—covering core memory representation, internal bucket indexing algorithms (`(h ^ (h >>> 16)) & (n-1)`), treeification mechanics, JDK 21 Sequenced Collections, concurrent lock-free collections, fail-fast vs fail-safe iterators, cache locality implications, and production-grade custom collection implementations.

---

## Module Sitemap

| File | Type | Description |
| :--- | :--- | :--- |
| **[`README.md`](README.md)** | Index | Master module index, sitemap, time/space complexity matrix, and JDK 21 Sequenced Collections overview. |
| **[`00_Collections_MindMap.md`](00_Collections_MindMap.md)** | Mind Map | Interactive visual Mermaid diagram mapping `List`, `Set`, `Map`, `Queue`, `Deque`, Concurrent Collections, and Custom Data Structures. |
| **[`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)** | Resources | Recommended textbooks (*Effective Java*, *Java Generics and Collections*) and OpenJDK source pointers (`HashMap`, `ConcurrentHashMap`, `ArrayDeque`). |
| **[`00_theory.md`](00_theory.md)** | Theory | Exhaustive theoretical guide on HashMap internal hashing, treeification thresholds, Red-Black tree rebalancing, bitwise masks, and GC reference types. |
| **[`01_list_arraylist_linkedlist_vector.java`](01_list_arraylist_linkedlist_vector.java)** | Code | `ArrayList` dynamic resizing (`1.5x`), `LinkedList` doubly-linked node pointer memory overhead, `Vector`/`Stack` legacy locks, and `subList` mutations. |
| **[`02_map_hashmap_treemap_linkedhashmap.java`](02_map_hashmap_treemap_linkedhashmap.java)** | Code | `HashMap` bucket resizing, Red-Black tree conversion (`TreeNode`), `LinkedHashMap` access-order LRU eviction, `TreeMap` range queries, `IdentityHashMap`, and `WeakHashMap`. |
| **[`03_set_hashset_treeset_enumset.java`](03_set_hashset_treeset_enumset.java)** | Code | `HashSet` backing `HashMap`, `TreeSet` backing `TreeMap`, `EnumSet` bitwise bitmask representations (`RegularEnumSet` vs `JumboEnumSet`), and `CopyOnWriteArraySet`. |
| **[`04_queue_deque_priorityqueue_arraydeque.java`](04_queue_deque_priorityqueue_arraydeque.java)** | Code | `ArrayDeque` circular ring buffer bitwise masking `(tail + 1) & (elements.length - 1)` (cache-friendly, zero node allocations), `PriorityQueue` binary min-heap array mechanics, `SynchronousQueue`, and `DelayQueue`. |
| **[`05_concurrent_collections_failfast_failsafe.java`](05_concurrent_collections_failfast_failsafe.java)** | Code | `ConcurrentHashMap` (lock-free CAS + synchronized bin heads, transfer step resizing), `CopyOnWriteArrayList` (snapshot copy-on-write), `ConcurrentSkipListMap` (lock-free skip list), and Fail-Fast `modCount` vs Fail-Safe iterators. |
| **[`06_custom_collections_lru_custom_hashmap.java`](06_custom_collections_lru_custom_hashmap.java)** | Code | Production-grade custom structures: Custom LRU Cache using `LinkedHashMap` & custom doubly-linked list + HashMap, Custom Bounded Blocking Queue using `ReentrantLock` & `Condition`, and Custom Open-Addressing Linear Probing Map. |

---

## Time & Memory Complexity Matrix

| Collection Class | Access (Random) | Search | Insertion | Deletion | Space Overhead per Element | Cache Locality |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`ArrayList`** | \(O(1)\) | \(O(N)\) | \(O(1)\) amortized | \(O(N)\) | Minimal (contiguous `Object[]` array) | **Excellent (Sequential CPU Cache line prefetching)** |
| **`LinkedList`** | \(O(N)\) | \(O(N)\) | \(O(1)\) at ends | \(O(1)\) at node | High (24 bytes Node object + 2 pointers) | **Poor (Pointer chasing across heap memory)** |
| **`ArrayDeque`** | \(O(1)\) at ends | \(O(N)\) | \(O(1)\) amortized | \(O(1)\) | Minimal (circular `Object[]` buffer) | **Excellent** |
| **`HashSet` / `HashMap`** | N/A | \(O(1)\) avg, \(O(\log N)\) max | \(O(1)\) avg, \(O(\log N)\) max | \(O(1)\) avg, \(O(\log N)\) max | 32 bytes per `Node<K,V>` | Moderate (hash table bucket arrays) |
| **`TreeSet` / `TreeMap`** | N/A | \(O(\log N)\) | \(O(\log N)\) | \(O(\log N)\) | 40 bytes per `Entry<K,V>` (Red-Black node) | Poor (Node pointer traversing) |
| **`LinkedHashMap`** | N/A | \(O(1)\) | \(O(1)\) | \(O(1)\) | 48 bytes per `Entry<K,V>` (HashMap node + before/after pointers) | Moderate |
| **`ConcurrentHashMap`** | N/A | \(O(1)\) lock-free | \(O(1)\) fine-grained lock | \(O(1)\) fine-grained lock | 32 bytes per `Node<K,V>` | Moderate |
| **`PriorityQueue`** | N/A | \(O(N)\) | \(O(\log N)\) | \(O(\log N)\) poll | Minimal (binary heap in `Object[]` array) | **Good** |

---

## Modern Java 21 Sequenced Collections (`java.util`)

Java 21 introduced a unified hierarchy for ordered collections to resolve historical fragmentation:

```
                      Collection
                          |
                  SequencedCollection
                 /        |        \
             List       Deque    SequencedSet
                                       |
                                  SortedSet / LinkedHashSet
```

### Key Sequenced APIs:
- `SequencedCollection<E>`: `addFirst(e)`, `addLast(e)`, `getFirst()`, `getLast()`, `removeFirst()`, `removeLast()`, `reversed()`.
- `SequencedSet<E>`: Set view preserving order with `reversed()` returning a reversed `SequencedSet`.
- `SequencedMap<K,V>`: `firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `pollLastEntry()`, `putFirst(k,v)`, `putLast(k,v)`, `sequencedKeySet()`, `sequencedValues()`, `sequencedEntrySet()`, `reversed()`.
