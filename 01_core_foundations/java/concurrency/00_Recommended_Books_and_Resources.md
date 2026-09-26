# Recommended Books & Resources: Java Concurrency & Multithreading

A curated list of authoritative books, specification documents, JDK Enhancement Proposals (JEPs), and open-source repositories for mastering Java Concurrency.

---

## 1. Essential Books

1. **Java Concurrency in Practice**  
   *Authors:* Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes, Doug Lea  
   *Focus:* The timeless bible of Java multithreading. Covers thread safety, immutability, `java.util.concurrent` (JUC), execution framework, explicit locks, and testing concurrent programs.

2. **Concurrent Programming in Java: Design Principles and Patterns (2nd Edition)**  
   *Author:* Doug Lea (Lead architect of `java.util.concurrent`)  
   *Focus:* Mathematical and architectural foundations of concurrent object-oriented design, state synchronization, and lock-free data structures.

3. **The Art of Multiprocessor Programming (2nd Edition)**  
   *Authors:* Maurice Herlihy, Nir Shavit  
   *Focus:* Low-level hardware concurrency, Compare-And-Swap (CAS), spinlocks, lock-free queues, transactional memory, and the Michael-Scott queue algorithm.

---

## 2. JDK Specifications & JEP References

* **[JSR-133: Java Memory Model and Thread Specification](https://jcp.org/en/jsr/detail?id=133)** — Canonical specification detailing the JMM `happens-before` relationship, volatile semantics, and final field immutability guarantees.
* **[JEP 444: Virtual Threads (JDK 21 Final)](https://openjdk.org/jeps/444)** — Official OpenJDK specification for lightweight Virtual Threads in the Java platform.
* **[JEP 453: Structured Concurrency (Preview)](https://openjdk.org/jeps/453)** — API for treating groups of related tasks running in different threads as a single unit of work.
* **[JEP 446: Scoped Values (Preview)](https://openjdk.org/jeps/446)** — Efficient, immutable data sharing within and across threads (modern replacement for `ThreadLocal`).

---

## 3. Recommended Codebase Exploration

* **[`java.util.concurrent` Package Source (OpenJDK)](https://github.com/openjdk/jdk/tree/master/src/java.base/share/classes/java/util/concurrent)** — Inspect source code written by Doug Lea:
  * `ConcurrentHashMap.java` (Node array + CAS + `synchronized` bucket locking)
  * `AbstractQueuedSynchronizer.java` (AQS - the core framework behind `ReentrantLock` & `Semaphore`)
  * `ForkJoinPool.java` (Work-Stealing queue implementation)
  * `VirtualThread.java` (JDK 21 Virtual Thread continuation un-mounting logic)
