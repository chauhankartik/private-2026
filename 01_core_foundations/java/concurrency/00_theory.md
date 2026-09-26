# Java Concurrency — Complete Theory & API Guide
> **Study goal:** Understand threading, synchronization, the Java Memory Model,
> and the modern `java.util.concurrent` toolkit used in production systems.

---

## 1. Why Concurrency?

Programs run tasks on a single thread by default.
Modern hardware has many cores — concurrency lets you use them all.

```
Single thread:    [Task A]──────[Task B]──────[Task C]──────► time
Multi-thread:     [Task A]──────────────────────────────────►
                  [Task B]──────────────────────────────────►
                  [Task C]──────────────────────────────────►
                              (true parallel on multi-core)
```

**Two flavors:**
- **Concurrency** — multiple tasks making progress (may interleave on one core)
- **Parallelism** — multiple tasks running simultaneously (requires multiple cores)

---

## 2. Thread Lifecycle

```
NEW ──start()──► RUNNABLE ──scheduler──► RUNNING
                    ▲                       │
                    │          sleep/wait   │
                    └──────── WAITING ◄─────┘
                                            │ notify / timeout / interrupt
                              TIMED_WAITING ◄────────────────┘
                                            │
                              BLOCKED (waiting for monitor lock)
                                            │
                              TERMINATED (run() completed or exception)
```

```java
Thread t = new Thread(() -> System.out.println("Hello"));
t.getState(); // NEW
t.start();    // → RUNNABLE → RUNNING
t.join();     // caller waits until t finishes
t.getState(); // TERMINATED
```

---

## 3. Creating Threads — Three Ways

```java
// Way 1: Extend Thread (tightly coupled — avoid)
class MyThread extends Thread {
    @Override public void run() { System.out.println("Thread: " + getName()); }
}
new MyThread().start();

// Way 2: Implement Runnable (decoupled — better)
Runnable task = () -> System.out.println("Runnable");
new Thread(task).start();

// Way 3: Callable + Future (returns a value + can throw checked exceptions)
Callable<Integer> callable = () -> 42;
ExecutorService exec = Executors.newSingleThreadExecutor();
Future<Integer> future = exec.submit(callable);
int result = future.get(); // blocks until result is ready
exec.shutdown();
```

---

## 4. The Java Memory Model (JMM)

The JMM defines when writes by one thread become **visible** to another.

### The Problem: Visibility & Reordering

```java
// Thread 1            Thread 2
boolean ready = false;
int value = 0;
                       while (!ready) {}   // may loop forever!
value = 42;            System.out.println(value); // may print 0!
ready = true;
```

Without synchronization, the JVM and CPU may **reorder** instructions and cache values in CPU registers. Thread 2 may never see the updated `ready` or `value`.

### Happens-Before Rules (what guarantees visibility)

| Action | Guarantees |
|---|---|
| `synchronized` block/method | Everything before unlock **happens-before** everything after lock |
| `volatile` write | Write **happens-before** all subsequent reads of that variable |
| `Thread.start()` | All actions before start() **happen-before** thread body |
| `Thread.join()` | Thread body **happens-before** actions after join() |
| `Future.get()` | Task body **happens-before** get() returns |

---

## 5. Synchronization Primitives

### `synchronized`
```java
class Counter {
    private int count = 0;

    synchronized void increment() { count++; }        // intrinsic lock on 'this'
    synchronized int get()        { return count; }

    // Or use a dedicated lock object (finer control)
    private final Object lock = new Object();
    void safeIncrement() {
        synchronized (lock) { count++; }
    }
}
```

### `volatile`
```java
class StopFlag {
    private volatile boolean stop = false;   // writes immediately visible to all threads
    void stop()       { stop = true; }
    void run()        { while (!stop) { /* work */ } }
}
```

**`volatile` guarantees:** visibility + no reordering around the variable.  
**`volatile` does NOT guarantee:** atomicity. `volatile int i; i++` is still a race condition (read-modify-write).

### `Atomic` classes
```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();                      // atomic read-modify-write
counter.compareAndSet(expected, newValue);      // CAS — heart of lock-free algorithms
```

---

## 6. Locks — `java.util.concurrent.locks`

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();   // ALWAYS unlock in finally
}

// Try-lock (non-blocking)
if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
    try { /* work */ } finally { lock.unlock(); }
} else {
    // couldn't acquire — do something else
}

// ReadWriteLock — many readers OR one writer
ReadWriteLock rwLock = new ReentrantReadWriteLock();
rwLock.readLock().lock();   // multiple threads can hold read lock simultaneously
rwLock.writeLock().lock();  // exclusive — blocks all readers and writers
```

---

## 7. Thread Pools — `ExecutorService`

```java
// Fixed pool — good for CPU-bound tasks
ExecutorService pool = Executors.newFixedThreadPool(4);

// Cached pool — grows/shrinks; good for short-lived I/O tasks
ExecutorService cached = Executors.newCachedThreadPool();

// Scheduled — run tasks after a delay or periodically
ScheduledExecutorService sched = Executors.newScheduledThreadPool(2);
sched.schedule(task, 1, TimeUnit.SECONDS);            // once, after 1s
sched.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS); // every 5s

// Single thread — sequential execution guaranteed
ExecutorService single = Executors.newSingleThreadExecutor();

// Work-stealing pool (Java 8) — good for fork/join style tasks
ExecutorService workStealing = Executors.newWorkStealingPool();

// Modern (Java 21) — Virtual Threads (Project Loom)
ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor();

// ALWAYS shut down to release threads
pool.shutdown();                    // stop accepting new tasks, drain remaining
pool.awaitTermination(10, TimeUnit.SECONDS);
pool.shutdownNow();                 // interrupt running tasks, skip queue
```

---

## 8. Future & CompletableFuture

```java
// Future — get() blocks, cancel() cancels
Future<String> f = executor.submit(() -> fetchFromDatabase());
String result = f.get(5, TimeUnit.SECONDS);  // timeout if slow

// CompletableFuture — async pipeline (Java 8+)
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(user -> enrichWithProfile(user))
    .thenCompose(user -> sendEmailAsync(user))     // flatMap (returns CF)
    .thenAccept(result -> log(result))
    .exceptionally(ex -> { log(ex); return null; })
    .join();   // block until done (or thenJoin() with timeout)

// Combining
CompletableFuture<A> fa = ...;
CompletableFuture<B> fb = ...;
CompletableFuture.allOf(fa, fb).join();          // wait for ALL
CompletableFuture.anyOf(fa, fb).join();          // wait for FIRST
fa.thenCombine(fb, (a, b) -> combine(a, b));     // both results
```

---

## 9. Concurrent Collections

| Collection | Use case | Key property |
|---|---|---|
| `ConcurrentHashMap` | Thread-safe map | Segment-level locking (Java 7), CAS (Java 8+) |
| `CopyOnWriteArrayList` | Read-heavy, rare writes | Writes copy the array; reads lockless |
| `BlockingQueue` | Producer-Consumer | `put()` blocks when full, `take()` blocks when empty |
| `ArrayBlockingQueue` | Bounded blocking queue | Fixed capacity |
| `LinkedBlockingQueue` | Unbounded (default) or bounded | Node-based |
| `PriorityBlockingQueue` | Priority ordering | Heap-backed, unbounded |
| `ConcurrentLinkedQueue` | Lock-free FIFO | CAS-based |
| `DelayQueue` | Scheduled release | Elements only available after delay |

---

## 10. Synchronization Aids

```java
// CountDownLatch — one-time gate: wait until N events happen
CountDownLatch latch = new CountDownLatch(3);
// 3 threads each call latch.countDown() when done
latch.await();  // main thread waits here until count reaches 0

// CyclicBarrier — reusable gate: wait until all N threads arrive
CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("All arrived!"));
// Each thread calls barrier.await() — all released together

// Semaphore — limit concurrent access (e.g., max 5 DB connections)
Semaphore sem = new Semaphore(5);
sem.acquire();   // blocks if no permits available
try { /* use resource */ } finally { sem.release(); }

// Phaser — flexible, dynamic barrier (Java 7+)
Phaser phaser = new Phaser(3);
phaser.arriveAndAwaitAdvance();  // wait for all to reach this phase
phaser.arrive();                 // arrive but don't wait

// Exchanger — two threads swap an object
Exchanger<String> exchanger = new Exchanger<>();
// Thread 1: String received = exchanger.exchange("from-thread-1");
// Thread 2: String received = exchanger.exchange("from-thread-2");
```

---

## 11. Common Pitfalls

| Pitfall | Symptom | Fix |
|---|---|---|
| **Race condition** | Inconsistent state under load | Synchronize shared state |
| **Deadlock** | All threads frozen | Always acquire locks in same order |
| **Livelock** | Threads keep responding but make no progress | Add randomized backoff |
| **Starvation** | One thread never runs | Use fair locks (`new ReentrantLock(true)`) |
| **Visibility bug** | Thread sees stale value | Add `volatile` or synchronize |
| **Missed signal** | `wait()` after `notify()` | Always check condition in a `while` loop |
| **Thread leak** | Threads never stop | Always `shutdown()` ExecutorService |

---

*Next:*
- `01_threads_sync.java` — Thread basics, synchronized, volatile, atomic
- `02_locks_executors.java` — ReentrantLock, ExecutorService, Future
- `03_completablefuture.java` — Async pipelines, combining, error handling
- `04_concurrent_collections.java` — ConcurrentHashMap, BlockingQueue, CopyOnWrite
- `05_advanced.java` — CountDownLatch, Semaphore, CyclicBarrier, Virtual Threads
