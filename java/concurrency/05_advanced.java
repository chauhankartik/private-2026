/**
 * ============================================================
 *  JAVA CONCURRENCY — ADVANCED SYNCHRONIZATION & VIRTUAL THREADS
 * ============================================================
 *
 * Topics:
 *   1. CountDownLatch — one-time "all ready" gate
 *   2. CyclicBarrier — reusable rendezvous point
 *   3. Semaphore — limit concurrent access
 *   4. Phaser — flexible multi-phase coordination
 *   5. Exchanger — two-thread data swap
 *   6. Virtual Threads (Java 21 / Project Loom)
 *   7. ThreadLocal — per-thread context storage
 *
 * Run: javac 05_advanced.java && java AdvancedConcurrencyDemo
 * ============================================================
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class AdvancedConcurrencyDemo {

    public static void main(String[] args) throws Exception {
        demo1_countDownLatch();
        demo2_cyclicBarrier();
        demo3_semaphore();
        demo4_phaser();
        demo5_exchanger();
        demo6_virtualThreads();
        demo7_threadLocal();
    }

    // =========================================================
    // Demo 1: CountDownLatch
    // =========================================================
    /**
     * A one-time gate: main thread waits until N other threads signal "done."
     *
     * Use cases:
     *   - "Wait for all services to initialize before accepting requests."
     *   - "Fire a starting gun: all worker threads wait until ready."
     *   - "Wait for all parallel tasks to finish."
     *
     * Key difference from CyclicBarrier:
     *   - CountDownLatch is ONE-WAY (count only decrements).
     *   - Cannot be reset — use CyclicBarrier for repeatable barriers.
     */
    static void demo1_countDownLatch() throws Exception {
        separator("Demo 1: CountDownLatch");

        // ── Pattern 1: main waits for workers ─────────────────────
        int numWorkers = 4;
        CountDownLatch done = new CountDownLatch(numWorkers);

        System.out.println("Starting " + numWorkers + " workers...");
        for (int i = 1; i <= numWorkers; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep(id * 50L);
                    System.out.println("  [Worker-" + id + "] Finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();   // decrement — even if exception thrown
                }
            }, "worker-" + i).start();
        }

        done.await();   // blocks until count reaches 0
        System.out.println("  All workers done! Proceeding...");

        // ── Pattern 2: starting gun ────────────────────────────────
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch allDone  = new CountDownLatch(3);
        long[] startTimes       = new long[3];

        for (int i = 0; i < 3; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startGun.await();   // all threads wait here
                    startTimes[id] = System.currentTimeMillis();
                    System.out.println("  [Racer-" + id + "] Started at " + startTimes[id] % 10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allDone.countDown();
                }
            }).start();
        }
        Thread.sleep(100);
        System.out.println("  GO!");
        startGun.countDown();   // release ALL waiting threads at once
        allDone.await();
        System.out.println("  Race finished.");

        // ── await with timeout ─────────────────────────────────────
        CountDownLatch timedLatch = new CountDownLatch(1);
        boolean reached = timedLatch.await(100, TimeUnit.MILLISECONDS);
        System.out.println("  await(100ms) — reached zero: " + reached);  // false (timed out)
    }

    // =========================================================
    // Demo 2: CyclicBarrier
    // =========================================================
    /**
     * All N threads wait at the barrier until ALL have arrived.
     * Then they ALL proceed simultaneously.
     * Unlike CountDownLatch, CyclicBarrier is REUSABLE — resets after each cycle.
     *
     * Use cases:
     *   - Parallel computation phases (partition → process → merge → repeat)
     *   - Multi-player game synchronization
     *   - Batch processing pipelines
     */
    static void demo2_cyclicBarrier() throws Exception {
        separator("Demo 2: CyclicBarrier");

        int numThreads = 3;
        // Optional barrier action runs when all threads arrive (before they're released)
        CyclicBarrier barrier = new CyclicBarrier(numThreads,
            () -> System.out.println("  [Barrier] All arrived — starting next phase!\n"));

        // Simulate 2 phases of computation
        for (int phase = 1; phase <= 2; phase++) {
            final int p = phase;
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                final int id = i;
                threads[i] = new Thread(() -> {
                    try {
                        long workTime = (id + 1) * 50L;
                        Thread.sleep(workTime);
                        System.out.println("  [Phase-" + p + "][Thread-" + id + "] Done ("
                            + workTime + "ms). Waiting at barrier...");
                        barrier.await();   // wait for everyone — barrier resets after each call
                        System.out.println("  [Phase-" + p + "][Thread-" + id + "] Continuing.");
                    } catch (Exception e) { Thread.currentThread().interrupt(); }
                });
                threads[i].start();
            }
            for (Thread t : threads) t.join();
        }
    }

    // =========================================================
    // Demo 3: Semaphore
    // =========================================================
    /**
     * A Semaphore with N permits allows at most N threads to be in the
     * critical section simultaneously.
     *
     * acquire() — take a permit (blocks if none available)
     * release() — return a permit
     *
     * Use cases:
     *   - Rate limiting (max N concurrent HTTP requests)
     *   - Connection pooling (max N DB connections)
     *   - Resource throttling (max N file handles open)
     */
    static void demo3_semaphore() throws Exception {
        separator("Demo 3: Semaphore — Connection Pool Simulation");

        int MAX_CONNECTIONS = 3;
        Semaphore sem = new Semaphore(MAX_CONNECTIONS, true);  // fair=true
        AtomicInteger activeConns = new AtomicInteger(0);
        AtomicInteger maxSeen     = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                try {
                    System.out.println("  [Client-" + id + "] Requesting connection...");
                    sem.acquire();   // block until a permit is available
                    int active = activeConns.incrementAndGet();
                    maxSeen.updateAndGet(m -> Math.max(m, active));
                    System.out.println("  [Client-" + id + "] Got connection! Active=" + active);
                    Thread.sleep(100);   // use the "connection"
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    activeConns.decrementAndGet();
                    sem.release();   // return permit — must be in finally
                    System.out.println("  [Client-" + id + "] Released connection.");
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) t.join();
        System.out.println("  Max concurrent connections observed: " + maxSeen.get()
            + " (limit=" + MAX_CONNECTIONS + ")");

        // tryAcquire — non-blocking
        boolean got = sem.tryAcquire(50, TimeUnit.MILLISECONDS);
        System.out.println("  tryAcquire: " + got + " (all permits available now)");
        if (got) sem.release();

        // Semaphore as mutex (1 permit = binary semaphore)
        Semaphore mutex = new Semaphore(1);   // like a lock, but releasable by different thread
    }

    // =========================================================
    // Demo 4: Phaser — Multi-Phase Coordination
    // =========================================================
    /**
     * Phaser is a flexible CyclicBarrier that:
     *   - Supports a dynamic number of parties (can register/deregister at runtime)
     *   - Supports multiple named phases
     *   - Can be hierarchical (tree of Phasers for large-scale parallelism)
     *
     * Use cases: multi-round computation, simulation steps, test setup/teardown.
     */
    static void demo4_phaser() throws Exception {
        separator("Demo 4: Phaser");

        Phaser phaser = new Phaser(1);   // 1 = the main thread as party

        for (int i = 0; i < 3; i++) {
            final int id = i;
            phaser.register();    // add a new party dynamically

            new Thread(() -> {
                for (int phase = 0; phase < 2; phase++) {
                    try { Thread.sleep((id + 1) * 30L); } catch (InterruptedException e) {}
                    System.out.println("  [T-" + id + "] Phase " + phaser.getPhase() + " done");
                    phaser.arriveAndAwaitAdvance();  // arrive + wait for all to arrive
                }
                phaser.arriveAndDeregister();   // remove self from future phases
            }).start();
        }

        for (int phase = 0; phase < 2; phase++) {
            phaser.arriveAndAwaitAdvance();   // main thread participates in each phase
            System.out.println("  [Main] Phase " + phase + " complete. Next: " + phaser.getPhase());
        }
        phaser.arriveAndDeregister();   // main thread done
        System.out.println("  Phaser terminated: " + phaser.isTerminated());
    }

    // =========================================================
    // Demo 5: Exchanger
    // =========================================================
    /**
     * Exchanger allows exactly TWO threads to swap objects at a synchronization point.
     * Thread A calls exchange(a) — blocks until Thread B calls exchange(b).
     * Then A gets b, B gets a.
     *
     * Use cases: pipeline stages (filler fills a buffer, drainer processes it),
     *            genetic algorithm crossover, two-party data sharing.
     */
    static void demo5_exchanger() throws Exception {
        separator("Demo 5: Exchanger — Two-Thread Data Swap");

        Exchanger<List<Integer>> exchanger = new Exchanger<>();

        Thread filler = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                List<Integer> buffer = new ArrayList<>();
                for (int j = 0; j < 3; j++) buffer.add(i * 10 + j);
                System.out.println("  [Filler]  Prepared: " + buffer);
                try {
                    List<Integer> empty = exchanger.exchange(buffer);  // swap with drainer
                    System.out.println("  [Filler]  Got empty buffer: " + empty);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "filler");

        Thread drainer = new Thread(() -> {
            List<Integer> myEmpty = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                try {
                    List<Integer> full = exchanger.exchange(myEmpty);   // swap with filler
                    System.out.println("  [Drainer] Processing: " + full);
                    myEmpty = new ArrayList<>();   // cleared — ready to exchange again
                    Thread.sleep(30);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "drainer");

        filler.start(); drainer.start();
        filler.join(); drainer.join();
    }

    // =========================================================
    // Demo 6: Virtual Threads (Java 21 — Project Loom)
    // =========================================================
    /**
     * PLATFORM THREADS (traditional):
     *   - 1:1 mapping to OS threads
     *   - ~1MB stack per thread
     *   - Expensive to create and context-switch
     *   - Hard limit: ~thousands of threads per JVM
     *
     * VIRTUAL THREADS (Java 21+):
     *   - Many:few mapping (millions of virtual threads → few carrier threads)
     *   - Tiny stack (grows/shrinks dynamically)
     *   - Cheap to create (just heap objects)
     *   - When a virtual thread blocks (I/O, lock), it's UNMOUNTED from its carrier
     *     and the carrier picks up another virtual thread
     *
     * RESULT: You can write blocking I/O code (Thread.sleep, DB query) in a
     * virtual thread and the JVM makes it non-blocking under the hood.
     * No need to use async/reactive callbacks just for scalability.
     *
     * Limitations:
     *   - synchronized blocks PIN the virtual thread to its carrier (avoid in hot paths)
     *   - ThreadLocal works but can cause memory pressure (millions of threads)
     *   - Not a replacement for non-blocking I/O in all cases — just simpler to write
     */
    static void demo6_virtualThreads() throws Exception {
        separator("Demo 6: Virtual Threads (Java 21)");

        // ── Creating virtual threads ───────────────────────────────
        Thread vt1 = Thread.ofVirtual().name("vt-1").start(() ->
            System.out.println("  [" + Thread.currentThread() + "] Virtual thread running")
        );
        vt1.join();

        // ── Virtual thread executor (recommended way) ──────────────
        try (ExecutorService vtExec = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submit 100 "blocking" tasks — would need 100 platform threads otherwise
            // With virtual threads: uses only ~4 carrier threads (one per CPU core)
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final int id = i;
                futures.add(vtExec.submit(() -> {
                    Thread.sleep(50);   // blocks the virtual thread, NOT the carrier
                    return "vt-" + id + " done on " + Thread.currentThread().getName();
                }));
            }
            for (Future<String> f : futures) System.out.println("  " + f.get());
        }   // auto-closes and shuts down

        // ── Thread.ofVirtual() builder ────────────────────────────
        Thread.Builder.OfVirtual vtBuilder = Thread.ofVirtual().name("pool-", 0);
        Thread vt2 = vtBuilder.start(() ->
            System.out.println("  [" + Thread.currentThread().getName() + "] Builder-created"));
        vt2.join();

        // ── Comparing platform vs virtual thread ──────────────────
        Thread platform = Thread.ofPlatform().start(() -> {});
        Thread virtual  = Thread.ofVirtual().start(() -> {});
        platform.join(); virtual.join();
        System.out.println("  Platform thread isVirtual: " + platform.isVirtual());  // false
        System.out.println("  Virtual  thread isVirtual: " + virtual.isVirtual());   // true

        // ── Scale test: 10,000 virtual threads ────────────────────
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(10_000);
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                exec.submit(() -> {
                    Thread.sleep(10);   // each blocks for 10ms
                    latch.countDown();
                    return null;
                });
            }
        }
        latch.await();
        System.out.printf("  10,000 virtual threads (each sleeping 10ms): %dms total%n",
            System.currentTimeMillis() - start);
        // Typically ~10-20ms — most overlap. Platform threads would be much slower.
    }

    // =========================================================
    // Demo 7: ThreadLocal — Per-Thread Context
    // =========================================================
    /**
     * ThreadLocal<T> provides a per-thread variable.
     * Each thread sees its OWN copy — fully isolated.
     *
     * Use cases:
     *   - User session context in web requests (Spring SecurityContextHolder)
     *   - DB transaction/connection context (Spring @Transactional)
     *   - Per-thread SimpleDateFormat (it's not thread-safe)
     *   - MDC (Mapped Diagnostic Context) in SLF4J logging
     *
     * WARNING: Always call remove() after you're done.
     *   Thread pools REUSE threads — ThreadLocal values persist between tasks.
     *   Failure to remove → memory leak + stale data bugs.
     */
    static void demo7_threadLocal() throws Exception {
        separator("Demo 7: ThreadLocal — Per-Thread Context");

        ThreadLocal<String> userContext = ThreadLocal.withInitial(() -> "anonymous");

        ExecutorService pool = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 5; i++) {
            final String userId = "user-" + i;
            pool.submit(() -> {
                userContext.set(userId);   // set for THIS thread only
                try {
                    processRequest(userContext);
                } finally {
                    userContext.remove();   // MUST remove — thread is reused in pool!
                }
            });
        }

        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);

        // ── InheritableThreadLocal — child threads inherit parent's value ──
        InheritableThreadLocal<String> inherited = new InheritableThreadLocal<>();
        inherited.set("parent-value");
        Thread child = new Thread(() ->
            System.out.println("  Child inherited: " + inherited.get())  // "parent-value"
        );
        child.start(); child.join();
        System.out.println("  Parent still has: " + inherited.get());

        // ── ScopedValue (Java 21 preview) — modern replacement ─────
        // ScopedValue is the preferred alternative to ThreadLocal for virtual threads.
        // It's immutable, garbage-collected automatically, and doesn't need remove().
        System.out.println("\n  ScopedValue (Java 21) — better than ThreadLocal for virtual threads:");
        System.out.println("  ScopedValue.where(KEY, value).run(() -> { KEY.get(); })");
        System.out.println("  Immutable; auto-collected; scoped to structured concurrency tasks.");
    }

    static void processRequest(ThreadLocal<String> ctx) {
        try { Thread.sleep(30); } catch (InterruptedException e) {}
        System.out.println("  [" + Thread.currentThread().getName() + "] Processing for: " + ctx.get());
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
