/**
 * ============================================================
 *  JAVA CONCURRENCY — THREADS, SYNCHRONIZED, VOLATILE, ATOMIC
 * ============================================================
 *
 * Topics:
 *   1. Creating threads (Thread, Runnable, Callable)
 *   2. Race condition — demonstrating the problem
 *   3. synchronized — fixing it
 *   4. volatile — visibility guarantee
 *   5. AtomicInteger / AtomicReference — lock-free update
 *   6. wait() / notify() — classic producer-consumer
 *
 * Run: javac 01_threads_sync.java && java ThreadsDemo
 * ============================================================
 */
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ThreadsDemo {

    public static void main(String[] args) throws Exception {
        demo1_creatingThreads();
        demo2_raceCondition();
        demo3_synchronized();
        demo4_volatile();
        demo5_atomic();
        demo6_waitNotify();
    }

    // =========================================================
    // Demo 1: Creating Threads
    // =========================================================
    static void demo1_creatingThreads() throws Exception {
        separator("Demo 1: Creating Threads");

        // ── Way 1: Extend Thread ──────────────────────────────────
        Thread t1 = new Thread("worker-1") {
            @Override public void run() {
                System.out.println("  [" + getName() + "] Extend Thread — running");
            }
        };

        // ── Way 2: Runnable lambda ────────────────────────────────
        Thread t2 = new Thread(() ->
            System.out.println("  [" + Thread.currentThread().getName() + "] Runnable lambda"),
            "worker-2"
        );

        // ── Way 3: Callable + Future ──────────────────────────────
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> future = exec.submit(() -> {
            System.out.println("  [Callable] Computing...");
            Thread.sleep(50);
            return 42;
        });

        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("  [Future] result = " + future.get(2, TimeUnit.SECONDS));
        exec.shutdown();

        // ── Thread properties ─────────────────────────────────────
        Thread current = Thread.currentThread();
        System.out.println("  Current thread: name=" + current.getName()
            + " id=" + current.getId()
            + " priority=" + current.getPriority()
            + " daemon=" + current.isDaemon()
            + " state=" + current.getState());
    }

    // =========================================================
    // Demo 2: Race Condition — The Problem
    // =========================================================
    /**
     * count++ is NOT atomic. It's three operations:
     *   1. READ  count from memory
     *   2. ADD   1
     *   3. WRITE back to memory
     *
     * If two threads do this simultaneously, both may read the SAME value,
     * both add 1, both write the same result → one increment is lost.
     */
    static void demo2_raceCondition() throws Exception {
        separator("Demo 2: Race Condition (unsynchronized counter)");

        int[] count = {0};  // array so lambda can capture and mutate it

        Runnable increment = () -> {
            for (int i = 0; i < 10_000; i++) {
                count[0]++;   // ✗ NOT thread-safe
            }
        };

        Thread t1 = new Thread(increment);
        Thread t2 = new Thread(increment);
        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("  Expected: 20000");
        System.out.println("  Actual:   " + count[0] + " (likely LESS — race condition!)");
        System.out.println("  Explanation: count++ is READ → ADD → WRITE (3 non-atomic ops)");
    }

    // =========================================================
    // Demo 3: synchronized — The Fix
    // =========================================================
    static void demo3_synchronized() throws Exception {
        separator("Demo 3: synchronized — Fixing the Race");

        // ── 3a. Synchronized method ───────────────────────────────
        class SyncCounter {
            private int count = 0;
            synchronized void increment()   { count++; }   // intrinsic lock on 'this'
            synchronized int  get()         { return count; }
        }

        SyncCounter counter = new SyncCounter();
        Runnable inc = () -> { for (int i = 0; i < 10_000; i++) counter.increment(); };

        Thread t1 = new Thread(inc); Thread t2 = new Thread(inc);
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("  Synchronized method:   " + counter.get() + " (always 20000 ✓)");

        // ── 3b. Synchronized block (finer granularity) ───────────
        class BlockCounter {
            private int count = 0;
            private final Object lock = new Object();

            void increment() {
                // Only the increment is locked — not the whole method
                synchronized (lock) { count++; }
                // ... other non-critical work runs freely
            }
            int get() { synchronized (lock) { return count; } }
        }

        BlockCounter bc = new BlockCounter();
        Runnable inc2 = () -> { for (int i = 0; i < 10_000; i++) bc.increment(); };
        Thread t3 = new Thread(inc2); Thread t4 = new Thread(inc2);
        t3.start(); t4.start(); t3.join(); t4.join();
        System.out.println("  Synchronized block:    " + bc.get() + " (always 20000 ✓)");

        // ── 3c. Synchronized on a class (static methods) ─────────
        System.out.println("  synchronized(MyClass.class) → static shared lock across all instances");

        // ── 3d. Reentrancy — a thread can re-acquire its own lock ─
        class Reentrant {
            synchronized void outer() {
                System.out.println("  outer() — acquired lock");
                inner();           // same thread — re-acquires same lock (reentrancy)
            }
            synchronized void inner() {
                System.out.println("  inner() — reacquired same lock ✓");
            }
        }
        new Reentrant().outer();
    }

    // =========================================================
    // Demo 4: volatile — Visibility Guarantee
    // =========================================================
    /**
     * volatile guarantees:
     *   1. Every WRITE is immediately flushed to main memory.
     *   2. Every READ fetches from main memory (not CPU cache).
     *   3. No instruction reordering around volatile access.
     *
     * volatile does NOT guarantee:
     *   Atomicity — volatile int i; i++ is still a race condition.
     *   Use AtomicInteger for read-modify-write operations.
     */
    static void demo4_volatile() throws Exception {
        separator("Demo 4: volatile — Visibility");

        // Without volatile, the JVM may keep 'running' in a CPU register.
        // The writer thread's change may never be visible to the reader thread.
        class StopFlag {
            volatile boolean running = true;   // ← volatile required here
        }

        StopFlag flag = new StopFlag();

        Thread reader = new Thread(() -> {
            int count = 0;
            while (flag.running) { count++; }  // will actually stop with volatile
            System.out.println("  Reader stopped after " + count + " iterations");
        });

        Thread writer = new Thread(() -> {
            try { Thread.sleep(20); } catch (InterruptedException e) {}
            flag.running = false;
            System.out.println("  Writer set running=false");
        });

        reader.start(); writer.start();
        reader.join(); writer.join();

        // ── volatile write/read happens-before ────────────────────
        System.out.println("\n  volatile happens-before guarantee:");
        System.out.println("  → All writes BEFORE 'flag.running=false' are visible");
        System.out.println("  → to threads that READ 'flag.running' and see false.");
    }

    // =========================================================
    // Demo 5: AtomicInteger / AtomicReference — Lock-Free
    // =========================================================
    /**
     * java.util.concurrent.atomic uses CPU-level CAS (Compare-And-Swap)
     * instructions. No locking, no context switches — very fast.
     *
     * CAS: "Set value to NEW only if current value == EXPECTED."
     *   If another thread changed it, retry (spin).
     *
     * Use for:
     *   - Simple counters, flags, references
     *   - When contention is low (high contention → use LongAdder instead)
     */
    static void demo5_atomic() throws Exception {
        separator("Demo 5: AtomicInteger — Lock-Free Counter");

        AtomicInteger counter = new AtomicInteger(0);

        Runnable inc = () -> { for (int i = 0; i < 10_000; i++) counter.incrementAndGet(); };
        Thread t1 = new Thread(inc); Thread t2 = new Thread(inc);
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("  AtomicInteger result: " + counter.get() + " (always 20000 ✓)");

        // ── Key methods ──────────────────────────────────────────
        AtomicInteger ai = new AtomicInteger(10);
        System.out.println("\n  AtomicInteger methods:");
        System.out.println("  get():               " + ai.get());
        System.out.println("  getAndIncrement():   " + ai.getAndIncrement() + " (was 10, now 11)");
        System.out.println("  incrementAndGet():   " + ai.incrementAndGet()  + " (was 11, now 12)");
        System.out.println("  addAndGet(5):        " + ai.addAndGet(5)       + " (was 12, now 17)");
        System.out.println("  compareAndSet(17,0): " + ai.compareAndSet(17, 0) + " → " + ai.get());
        System.out.println("  compareAndSet(99,1): " + ai.compareAndSet(99, 1) + " (failed, still " + ai.get() + ")");

        // ── AtomicReference — atomic pointer swap ─────────────────
        AtomicReference<String> ref = new AtomicReference<>("original");
        boolean swapped = ref.compareAndSet("original", "updated");
        System.out.println("\n  AtomicReference.compareAndSet: " + swapped + " → " + ref.get());

        // ── LongAdder — better than AtomicLong under high contention ─
        LongAdder adder = new LongAdder();
        Runnable add = () -> { for (int i = 0; i < 100_000; i++) adder.increment(); };
        Thread t3 = new Thread(add); Thread t4 = new Thread(add);
        t3.start(); t4.start(); t3.join(); t4.join();
        System.out.println("  LongAdder sum: " + adder.sum() + " (200000 ✓)");
        System.out.println("  (LongAdder maintains per-thread cells → less contention than AtomicLong)");
    }

    // =========================================================
    // Demo 6: wait() / notify() — Classic Producer-Consumer
    // =========================================================
    /**
     * wait() / notify() are defined on Object (every object is a monitor).
     * Rules:
     *   - MUST be called inside a synchronized block on the same object.
     *   - wait() releases the lock and suspends the thread.
     *   - notify() / notifyAll() wakes waiting threads (they re-acquire the lock).
     *   - ALWAYS check the condition in a WHILE loop (not if) — spurious wakeups!
     *
     * Modern replacement: Condition (from ReentrantLock) — see 02_locks_executors.java
     */
    static void demo6_waitNotify() throws Exception {
        separator("Demo 6: wait() / notify() — Producer-Consumer");

        final Object monitor = new Object();
        final int[] item     = {-1};       // shared data
        final boolean[] ready = {false};

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                synchronized (monitor) {
                    while (ready[0]) {            // wait if consumer hasn't taken yet
                        try { monitor.wait(); } catch (InterruptedException e) { break; }
                    }
                    item[0] = i * 10;
                    ready[0] = true;
                    System.out.println("  [Producer] Produced: " + item[0]);
                    monitor.notifyAll();          // wake the consumer
                }
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                synchronized (monitor) {
                    while (!ready[0]) {           // wait until something is produced
                        try { monitor.wait(); } catch (InterruptedException e) { break; }
                    }
                    System.out.println("  [Consumer] Consumed: " + item[0]);
                    ready[0] = false;
                    monitor.notifyAll();          // wake the producer
                }
            }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join(); consumer.join();

        System.out.println("\n  Note: Use BlockingQueue in production — it handles all this internally.");
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
