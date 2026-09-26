# Java Collections Framework Recommended Books & Resources

A curated reading list of essential textbooks, OpenJDK source code pointers, and JVM memory layout references for mastering the Java Collections Framework (JCF).

---

## Must-Read Textbooks & References

1. **"Effective Java" (3rd Edition) by Joshua Bloch**
   - *Focus:* Designing clean, robust collection contracts, custom comparators, equality implementation, and concurrency choices.
   - *Key Items:*
     - **Item 10:** Obey the general contract when overriding `equals`.
     - **Item 11:** Always override `hashCode` when you override `equals`.
     - **Item 14:** Consider implementing `Comparable`.
     - **Item 18:** Favor composition over inheritance (Wrapper collections).
     - **Item 79:** Avoid excessive synchronization.
     - **Item 81:** Prefer concurrency utilities to `wait` and `notify`.

2. **"Java Generics and Collections" by Maurice Naftalin & Philip Wadler**
   - *Focus:* Deep dive into type safety, subtyping covariance/contravariance, collections architecture, and custom container implementations.
   - *Key Chapters:* Chapter 11 (Collections Core Interfaces), Chapter 12 (Sets), Chapter 13 (Maps), Chapter 14 (Queues), Chapter 15 (Lists), Chapter 17 (Concurrent Collections).

3. **"Java Concurrency in Practice" by Brian Goetz et al.**
   - *Focus:* Lock striping, lock-free data structures, memory visibility, and thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `BlockingQueue`).
   - *Key Chapters:* Chapter 5 (Building Blocks), Chapter 11 (Performance and Scalability - Lock Striping), Chapter 15 (Non-blocking Synchronization).

4. **"Algorithms" (4th Edition) by Robert Sedgewick & Kevin Wayne**
   - *Focus:* Data structure algorithms powering Java Collections (Red-Black Trees, Priority Queues, Binary Heaps, Hash Tables).
   - *Key Sections:* Section 3.3 (Balanced Search Trees / Red-Black Trees), Section 3.4 (Hash Tables), Section 2.4 (Priority Queues).

---

## OpenJDK Source Code Pointers

To inspect the production C++/Java implementations in OpenJDK:

- `src/java.base/share/classes/java/util/HashMap.java` — `hash()` bit-spreading, `putVal()`, `treeifyBin()`, `resize()`.
- `src/java.base/share/classes/java/util/concurrent/ConcurrentHashMap.java` — `spread()`, `initTable()`, CAS operations via `Unsafe`/`VarHandle`, `treeifyBin()`, `transfer()` resize step.
- `src/java.base/share/classes/java/util/ArrayDeque.java` — `doubleCapacity()`, bitwise head/tail ring buffer indexing `(tail + 1) & (elements.length - 1)`.
- `src/java.base/share/classes/java/util/PriorityQueue.java` — `siftUp()`, `siftDown()` binary min-heap array rebalancing.
- `src/java.base/share/classes/java/util/EnumSet.java` & `RegularEnumSet.java` — Bit vector representation backed by a single `long` bitmask for enums with \(\le 64\) elements.
