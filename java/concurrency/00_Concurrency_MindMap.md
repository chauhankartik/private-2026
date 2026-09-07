# Java Concurrency Architecture Mind Map

This document presents a structured visual breakdown of Java concurrency primitives, memory model mechanics, and Project Loom Virtual Threads.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((Java Concurrency Mastery))
    Java Memory Model JMM
      Happens Before Guarantee
      Volatile Memory Barrier
      Instruction Reordering
      Cache Coherency MESI Protocol
    Synchronization Primitives
      Monitors synchronized
      Explicit Locks ReentrantLock
      ReadWrite Lock Optimistic StampedLock
    Execution Engines
      ThreadPoolExecutor Core Max Queue
      ScheduledExecutorService
      CompletableFuture Async Composition
    Java 21 Project Loom
      Virtual Threads Lightweight
      Carrier Thread Scheduling
      Continuation Unmounting
      Pinning Hazard Detection
      StructuredTaskScope Concurrency
    Lock Free Algorithms
      Compare And Swap CAS
      AtomicInteger AtomicReference
      LongAdder Cell Striping
      ABA Problem Stamp Versioning
      Michael Scott Lock Free Queue
    Advanced Synchronizers
      CountDownLatch One Shot
      CyclicBarrier Reusable
      Phaser Dynamic Registration
      Semaphore Permit Rate Limit
    ForkJoin and Reactive Flow
      ForkJoinPool Work Stealing Deque
      Java 9 Flow Backpressure
```

---

## 2. Component Reference Table

| Primitive | Primary Use Case | Performance Hazard / Caveat |
| :--- | :--- | :--- |
| **`synchronized`** | Basic intrinsic monitor locking | Lock contention under high concurrency; causes Virtual Thread **Pinning** if IO blocking inside |
| **`ReentrantLock`** | Advanced locking (fairness, `tryLock()`, conditions) | Must manually unlock in `finally` block to prevent thread deadlocks |
| **`Volatile`** | Guarantees field visibility across threads | Guarantees **visibility** only, NOT atomicity (`volatile count++` is not thread-safe) |
| **`LongAdder`** | High-concurrency numeric counter accumulation | Higher memory footprint than `AtomicLong` due to internal cell-striping array |
| **`Virtual Threads`** | Massive I/O-bound concurrency ($100\text{K+}$ threads) | Avoid using for heavy CPU-bound math calculations (does not increase CPU cores) |
| **`Phaser`** | Multi-phase dynamic worker synchronization | Complex phase state tracking; risk of deadlock if a registered thread crashes |
