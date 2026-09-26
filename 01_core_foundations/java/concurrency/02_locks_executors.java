/**
 * ============================================================
 *  JAVA CONCURRENCY — LOCKS, EXECUTORS & FUTURES
 * ============================================================
 *
 * Topics:
 *   1. ReentrantLock — explicit locking with try-lock, fairness
 *   2. ReadWriteLock — concurrent reads, exclusive writes
 *   3. Condition — replacement for wait/notify
 *   4. ExecutorService — thread pool lifecycle
 *   5. Future — getting results from async tasks
 *   6. ScheduledExecutorService — delayed and periodic tasks
 *   7. Deadlock — demonstration and prevention
 *
 * Run: javac 02_locks_executors.java && java LocksDemo
 * ============================================================
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class LocksDemo {

    public static void main(String[] args) throws Exception {
        demo1_reentrantLock();
        demo2_readWriteLock();
        demo3_condition();
        demo4_executorService();
        demo5_futures();
        demo6_scheduledExecutor();
        demo7_deadlock_and_prevention();
    }

    // =========================================================
    // Demo 1: ReentrantLock
    // =========================================================
    /**
     * ReentrantLock advantages over synchronized:
     *   - tryLock() — non-blocking attempt
     *   - tryLock(time, unit) — attempt with timeout
     *   - lockInterruptibly() — can be interrupted while waiting
     *   - fairness option — longest-waiting thread gets lock first
     *   - multiple Condition objects per lock
     *   - unlock() in finally — explicit, can't forget
     *
     * Disadvantage: verbose boilerplate (must always unlock in finally).
     */
    static void demo1_reentrantLock() throws Exception {
        separator("Demo 1: ReentrantLock");

        class SafeCounter {
            private int count = 0;
            private final ReentrantLock lock = new ReentrantLock();

            void increment() {
                lock.lock();
                try {
                    count++;
                } finally {
                    lock.unlock();   // ALWAYS in finally — even if an exception is thrown
                }
            }

            // Non-blocking attempt
            boolean tryIncrement() {
                if (lock.tryLock()) {
                    try { count++; return true; }
                    finally { lock.unlock(); }
                }
                return false;  // couldn't acquire — do something else
            }

            // Attempt with timeout
            boolean timedIncrement(long ms) throws InterruptedException {
                if (lock.tryLock(ms, TimeUnit.MILLISECONDS)) {
                    try { count++; return true; }
                    finally { lock.unlock(); }
                }
                return false;  // timed out
            }

            int get() { return count; }
        }

        SafeCounter counter = new SafeCounter();
        Runnable inc = () -> { for (int i = 0; i < 10_000; i++) counter.increment(); };
        Thread t1 = new Thread(inc), t2 = new Thread(inc);
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("  ReentrantLock count: " + counter.get() + " (always 20000 ✓)");

        // tryLock demo
        boolean success = counter.tryIncrement();
        System.out.println("  tryLock succeeded: " + success);

        // Fair lock — threads acquire in FIFO order (slower but prevents starvation)
        ReentrantLock fairLock = new ReentrantLock(true);  // fair=true
        System.out.println("  Fair lock: " + fairLock.isFair()
            + " | Queue length: " + fairLock.getQueueLength());

        // Reentrancy — same thread can lock multiple times
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.lock();   // same thread — hold count becomes 2
        System.out.println("  Hold count: " + lock.getHoldCount());  // 2
        lock.unlock(); lock.unlock();   // must unlock same number of times
    }

    // =========================================================
    // Demo 2: ReadWriteLock
    // =========================================================
    /**
     * ReadWriteLock allows:
     *   - Multiple threads to read simultaneously (no blocking between readers)
     *   - Only ONE thread to write (blocks all readers and other writers)
     *
     * Use when reads >> writes (e.g., config cache, lookup table).
     * For Java 8+, consider StampedLock which supports optimistic reads.
     */
    static void demo2_readWriteLock() throws Exception {
        separator("Demo 2: ReadWriteLock");

        class Cache {
            private final Map<String, String>  map  = new HashMap<>();
            private final ReadWriteLock        lock = new ReentrantReadWriteLock();
            private final Lock readLock  = lock.readLock();
            private final Lock writeLock = lock.writeLock();

            String get(String key) {
                readLock.lock();     // multiple threads can hold read lock together
                try { return map.get(key); }
                finally { readLock.unlock(); }
            }

            void put(String key, String value) {
                writeLock.lock();   // exclusive — blocks all reads and writes
                try { map.put(key, value); }
                finally { writeLock.unlock(); }
            }

            int size() {
                readLock.lock();
                try { return map.size(); }
                finally { readLock.unlock(); }
            }
        }

        Cache cache = new Cache();
        cache.put("host", "localhost");
        cache.put("port", "8080");

        // Many readers in parallel
        ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                String val = cache.get("host");
                System.out.println("  [Reader] host=" + val);
            });
        }
        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("  Size: " + cache.size());

        // StampedLock — Java 8+ (optimistic read avoids lock acquisition)
        StampedLock sl = new StampedLock();
        long stamp = sl.tryOptimisticRead();
        // read shared data...
        if (!sl.validate(stamp)) {              // check if a write happened during read
            stamp = sl.readLock();              // fall back to full read lock
            try { /* re-read */ } finally { sl.unlockRead(stamp); }
        }
        System.out.println("  StampedLock optimistic read stamp: " + stamp);
    }

    // =========================================================
    // Demo 3: Condition — Replacement for wait()/notify()
    // =========================================================
    /**
     * Condition is the modern, explicit replacement for wait()/notify().
     * Advantages:
     *   - One ReentrantLock can have MULTIPLE Conditions.
     *   - Clearer semantics: await() / signal() / signalAll()
     *   - Can be interrupted, supports timeout
     *
     * Pattern: ALWAYS check condition in a while loop (spurious wakeups).
     */
    static void demo3_condition() throws Exception {
        separator("Demo 3: Condition — Producer-Consumer with Lock");

        class BoundedBuffer {
            private final Queue<Integer>  queue     = new LinkedList<>();
            private final int             capacity;
            private final ReentrantLock   lock      = new ReentrantLock();
            private final Condition       notFull   = lock.newCondition();
            private final Condition       notEmpty  = lock.newCondition();

            BoundedBuffer(int capacity) { this.capacity = capacity; }

            void put(int item) throws InterruptedException {
                lock.lock();
                try {
                    while (queue.size() == capacity) notFull.await();  // wait if full
                    queue.add(item);
                    System.out.println("  [Producer] Put " + item + " | size=" + queue.size());
                    notEmpty.signal();   // wake ONE consumer (not all — more efficient)
                } finally { lock.unlock(); }
            }

            int take() throws InterruptedException {
                lock.lock();
                try {
                    while (queue.isEmpty()) notEmpty.await();  // wait if empty
                    int item = queue.poll();
                    System.out.println("  [Consumer] Took " + item + " | size=" + queue.size());
                    notFull.signal();   // wake ONE producer
                    return item;
                } finally { lock.unlock(); }
            }
        }

        BoundedBuffer buffer = new BoundedBuffer(3);

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 6; i++) {
                try { buffer.put(i * 10); Thread.sleep(30); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                try { buffer.take(); Thread.sleep(80); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });

        producer.start(); consumer.start();
        producer.join(); consumer.join();
    }

    // =========================================================
    // Demo 4: ExecutorService — Thread Pool Lifecycle
    // =========================================================
    static void demo4_executorService() throws Exception {
        separator("Demo 4: ExecutorService — Thread Pools");

        // ── Fixed thread pool ─────────────────────────────────────
        System.out.println("FixedThreadPool(3) — 3 threads, unlimited queue:");
        ExecutorService fixed = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            fixed.submit(() ->
                System.out.println("  [Fixed] Task " + taskId
                    + " on " + Thread.currentThread().getName()));
        }
        fixed.shutdown();
        fixed.awaitTermination(2, TimeUnit.SECONDS);

        // ── Cached thread pool ────────────────────────────────────
        System.out.println("\nCachedThreadPool — grows/shrinks, idle threads expire in 60s:");
        ExecutorService cached = Executors.newCachedThreadPool();
        cached.submit(() -> System.out.println("  [Cached] " + Thread.currentThread().getName()));
        cached.shutdown();

        // ── ThreadPoolExecutor — full control ─────────────────────
        System.out.println("\nThreadPoolExecutor (fine-grained config):");
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
            2,                                  // corePoolSize
            4,                                  // maximumPoolSize
            60, TimeUnit.SECONDS,               // keepAliveTime (for threads > core)
            new LinkedBlockingQueue<>(10),       // work queue capacity
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy()  // rejection policy
        );
        // Rejection policies:
        //   AbortPolicy       → throw RejectedExecutionException (default)
        //   CallerRunsPolicy  → caller thread runs the task itself (backpressure)
        //   DiscardPolicy     → silently drop the task
        //   DiscardOldestPolicy → drop oldest queued task, retry

        tpe.submit(() -> System.out.println("  [TPE] " + Thread.currentThread().getName()));
        System.out.println("  Active threads: " + tpe.getActiveCount());
        System.out.println("  Queue size:     " + tpe.getQueue().size());
        tpe.shutdown();

        // ── Lifecycle methods ─────────────────────────────────────
        System.out.println("\nExecutorService lifecycle:");
        System.out.println("  shutdown()          → stop accepting, drain queue, terminate");
        System.out.println("  shutdownNow()       → interrupt running tasks, skip queued");
        System.out.println("  awaitTermination()  → block until terminated or timeout");
        System.out.println("  isShutdown()        → true after shutdown()");
        System.out.println("  isTerminated()      → true after all tasks done");
    }

    // =========================================================
    // Demo 5: Future — Async Results
    // =========================================================
    static void demo5_futures() throws Exception {
        separator("Demo 5: Future — Getting Results from Async Tasks");

        ExecutorService pool = Executors.newFixedThreadPool(3);

        // ── Submitting Callables ──────────────────────────────────
        Future<String> f1 = pool.submit(() -> {
            Thread.sleep(100);
            return "Result from Task 1";
        });
        Future<String> f2 = pool.submit(() -> {
            Thread.sleep(50);
            return "Result from Task 2";
        });
        Future<String> f3 = pool.submit(() -> {
            Thread.sleep(10);
            throw new RuntimeException("Task 3 failed!");
        });

        System.out.println("  f1.isDone() before get: " + f1.isDone());
        System.out.println("  f1.get():   " + f1.get());   // blocks until ready
        System.out.println("  f2.get():   " + f2.get(500, TimeUnit.MILLISECONDS)); // with timeout

        // ── Handling exceptions ───────────────────────────────────
        try {
            f3.get();  // throws ExecutionException wrapping the real exception
        } catch (ExecutionException e) {
            System.out.println("  f3 exception: " + e.getCause().getMessage());
        }

        // ── cancel() ─────────────────────────────────────────────
        Future<String> f4 = pool.submit(() -> {
            Thread.sleep(5000);
            return "Too slow";
        });
        f4.cancel(true);  // true = interrupt the running thread
        System.out.println("  f4.isCancelled(): " + f4.isCancelled());

        // ── invokeAll / invokeAny ─────────────────────────────────
        System.out.println("\n  invokeAll — submit all, wait for all:");
        List<Callable<String>> tasks = List.of(
            () -> "A", () -> "B", () -> "C"
        );
        List<Future<String>> results = pool.invokeAll(tasks);
        for (Future<String> r : results) System.out.println("  " + r.get());

        System.out.println("\n  invokeAny — submit all, return FIRST to complete:");
        String first = pool.invokeAny(tasks);
        System.out.println("  First: " + first);

        pool.shutdown();
    }

    // =========================================================
    // Demo 6: ScheduledExecutorService
    // =========================================================
    static void demo6_scheduledExecutor() throws Exception {
        separator("Demo 6: ScheduledExecutorService");

        ScheduledExecutorService sched = Executors.newScheduledThreadPool(2);

        // ── schedule() — one-shot after delay ─────────────────────
        ScheduledFuture<?> sf = sched.schedule(
            () -> System.out.println("  [One-shot] Ran after 100ms"),
            100, TimeUnit.MILLISECONDS
        );

        // ── scheduleAtFixedRate() — period is WALL-CLOCK time ─────
        // Runs at t=0, t=period, t=2*period... regardless of task duration
        ScheduledFuture<?> fixed = sched.scheduleAtFixedRate(
            () -> System.out.println("  [FixedRate] " + System.currentTimeMillis() % 10000),
            200, 150, TimeUnit.MILLISECONDS   // initialDelay=200ms, period=150ms
        );

        // ── scheduleWithFixedDelay() — delay is BETWEEN task completions ──
        ScheduledFuture<?> delayed = sched.scheduleWithFixedDelay(
            () -> System.out.println("  [FixedDelay] gap after last finish"),
            300, 150, TimeUnit.MILLISECONDS
        );

        Thread.sleep(700);   // let it run a few times
        fixed.cancel(false);
        delayed.cancel(false);

        sched.shutdown();
        System.out.println("\n  scheduleAtFixedRate  vs  scheduleWithFixedDelay:");
        System.out.println("  FixedRate:  period = wall-clock interval (may overlap if task > period)");
        System.out.println("  FixedDelay: period = delay after COMPLETION (no overlap possible)");
    }

    // =========================================================
    // Demo 7: Deadlock — Demonstration & Prevention
    // =========================================================
    /**
     * Deadlock: Thread A holds lock1, waits for lock2.
     *           Thread B holds lock2, waits for lock1.
     *           Both wait forever.
     *
     * Prevention strategy: ALWAYS acquire locks in the SAME GLOBAL ORDER.
     */
    static void demo7_deadlock_and_prevention() throws Exception {
        separator("Demo 7: Deadlock Demonstration & Prevention");

        Object lock1 = new Object();
        Object lock2 = new Object();

        // ─── This WOULD deadlock (disabled with tryLock to not hang the demo)
        System.out.println("Deadlock scenario (using tryLock to avoid actually deadlocking):");
        ReentrantLock l1 = new ReentrantLock();
        ReentrantLock l2 = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            l1.lock();
            System.out.println("  [T1] Acquired L1, trying L2...");
            try {
                boolean got = l2.tryLock(100, TimeUnit.MILLISECONDS);  // timeout = no deadlock
                if (got) {
                    try { System.out.println("  [T1] Got both locks!"); }
                    finally { l2.unlock(); }
                } else {
                    System.out.println("  [T1] Couldn't get L2 — backing off (deadlock prevented)");
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { l1.unlock(); }
        }, "T1");

        Thread t2 = new Thread(() -> {
            l2.lock();
            System.out.println("  [T2] Acquired L2, trying L1...");
            try {
                boolean got = l1.tryLock(100, TimeUnit.MILLISECONDS);
                if (got) {
                    try { System.out.println("  [T2] Got both locks!"); }
                    finally { l1.unlock(); }
                } else {
                    System.out.println("  [T2] Couldn't get L1 — backing off (deadlock prevented)");
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { l2.unlock(); }
        }, "T2");

        t1.start(); t2.start(); t1.join(); t2.join();

        System.out.println("\n  Deadlock prevention rules:");
        System.out.println("  1. Always acquire locks in the SAME ORDER (prevents circular wait).");
        System.out.println("  2. Use tryLock() with timeout — fail gracefully, retry with backoff.");
        System.out.println("  3. Use a single lock for multiple resources where possible.");
        System.out.println("  4. Detect with ThreadMXBean.findDeadlockedThreads() in production.");
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
