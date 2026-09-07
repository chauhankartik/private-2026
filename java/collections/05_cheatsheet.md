# Java Collections Cheatsheet & Conversions

## 1. Quick Decision Matrix

| Requirement | Use Collection | Why? |
|---|---|---|
| Basic resizable array | `ArrayList` | Cache friendly, O(1) random access |
| Unique elements, no order | `HashSet` | O(1) ops, backed by HashMap |
| Unique elements, sorted | `TreeSet` | O(log n) ops, backed by Red-Black Tree |
| Unique elements, insertion order | `LinkedHashSet` | Predictable iteration order |
| Key-value pairs, no order | `HashMap` | O(1) ops |
| Key-value pairs, sorted keys | `TreeMap` | O(log n) ops, useful for ranges (`floorKey`) |
| Key-value pairs, insertion order | `LinkedHashMap` | Good for LRU caches |
| Queue / Stack | `ArrayDeque` | Faster than `LinkedList` & `Stack` |
| Priority / Min-Max Heap | `PriorityQueue` | O(log n) insert/delete, O(1) peek |
| Concurrent Map | `ConcurrentHashMap` | Lock-free reads, fine-grained write locks |
| Thread-safe read-heavy List | `CopyOnWriteArrayList`| Mutative ops create copies, reads are fast/lock-free |

---

## 2. Important Map APIs (Java 8+)

```java
Map<String, Integer> map = new HashMap<>();

// 1. Get with default (Avoids null checks)
int val = map.getOrDefault("key", 0);

// 2. Put only if absent (Returns existing value if present, or null if absent)
map.putIfAbsent("key", 1);

// 3. Compute if absent (Great for grouping/bucketing, Lazy initialization)
Map<String, List<Integer>> group = new HashMap<>();
group.computeIfAbsent("category", k -> new ArrayList<>()).add(1);

// 4. Merge (Great for frequency counting)
map.merge("apple", 1, Integer::sum);
// Equivalent to: map.put("apple", map.getOrDefault("apple", 0) + 1);
```

---

## 3. Essential Conversions

### Arrays ↔ Collections

```java
// Array to List (Fixed size, backed by original array)
String[] arr = {"a", "b"};
List<String> list = Arrays.asList(arr); 

// Array to List (Modifiable, creates a new list)
List<String> modifiableList = new ArrayList<>(Arrays.asList(arr));

// List to Array
String[] newArr = list.toArray(new String[0]);
```

### Primitives ↔ Collections (Java 8 Streams)

```java
// int[] to List<Integer>
int[] primArr = {1, 2, 3};
List<Integer> intList = Arrays.stream(primArr)
                              .boxed()
                              .collect(Collectors.toList());

// List<Integer> to int[]
int[] primArr2 = intList.stream()
                        .mapToInt(Integer::intValue)
                        .toArray();
```

### Between Collections

```java
// List to Set (Deduplication)
List<String> list = Arrays.asList("a", "b", "a");
Set<String> set = new HashSet<>(list); // {"a", "b"}

// Set to List (For sorting or index access)
List<String> listFromSet = new ArrayList<>(set);
```

---

## 4. Sorting and Custom Comparators

```java
List<String> list = new ArrayList<>(Arrays.asList("apple", "banana", "kiwi"));

// 1. Natural order
Collections.sort(list); // or list.sort(null);

// 2. Custom Comparator (Lambda)
list.sort((a, b) -> Integer.compare(a.length(), b.length()));

// 3. Comparator combinators (Java 8+)
list.sort(Comparator.comparingInt(String::length)
                    .thenComparing(Comparator.naturalOrder()));

// 4. Reverse order
list.sort(Collections.reverseOrder());
```

---

## 5. PriorityQueue (Min/Max Heaps)

```java
// Min-Heap (Default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();

// Max-Heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

// Custom Object Heap (e.g., int[] {id, distance})
PriorityQueue<int[]> customHeap = new PriorityQueue<>((a, b) -> a[1] - b[1]);
```
