# C++ Standard Template Library (STL)

## 1. Containers
Containers manage collections of data. They handle memory allocation and destruction automatically.

### Sequence Containers
- **`std::vector`**: Dynamic array. $O(1)$ random access, amortized $O(1)$ push back. The default choice.
- **`std::deque`**: Double-ended queue. Chunks of arrays. $O(1)$ front/back insertion.
- **`std::list`**: Doubly-linked list. $O(1)$ insertion/deletion if iterator is known, but poor cache locality.
- **`std::array`**: Static contiguous array, size known at compile-time.

### Associative Containers (Node-Based)
Implemented via Red-Black Trees. $O(\log N)$ operations. Elements are ordered.
- **`std::map`** & **`std::set`**
- **`std::multimap`** & **`std::multiset`**

### Unordered Associative Containers (Hash Tables)
Implemented via Chaining Hash Tables. Expected $O(1)$ operations.
- **`std::unordered_map`** & **`std::unordered_set`**

## 2. Iterators
Iterators act as pointers to elements in a container, bridging containers and algorithms.
Categories:
1. Input Iterator (Read once)
2. Output Iterator (Write once)
3. Forward Iterator (Read/Write multiple times)
4. Bidirectional Iterator (Forward + Backward)
5. Random Access Iterator (Pointer arithmetic)
6. Contiguous Iterator (Memory is guaranteed sequential, e.g., `vector`, `array`)

## 3. Algorithms (`<algorithm>`)
The STL provides a rich set of algorithms that operate on iterators, not containers.
- **Sorting**: `std::sort` (Introsort: Quicksort + Heapsort).
- **Searching**: `std::binary_search`, `std::lower_bound` ($O(\log N)$).
- **Transforming**: `std::transform` (Map).
- **Reductions**: `std::accumulate`, `std::reduce` (Fold).

```cpp
std::vector<int> v = {4, 1, 3, 2};
std::sort(v.begin(), v.end()); // 1, 2, 3, 4
auto it = std::lower_bound(v.begin(), v.end(), 3); // Iterator to 3
```

## 4. Allocators
Allocators decouple memory management from object construction/destruction.
`std::allocator<T>` is the default, relying on global `new`/`delete`.
Custom allocators (e.g., Arena/Pool allocators) can be plugged into containers for extreme performance optimizations (e.g., reducing fragmentation or avoiding syscalls).
