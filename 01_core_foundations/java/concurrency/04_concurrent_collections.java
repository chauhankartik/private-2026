/**
 * ============================================================
 *  JAVA CONCURRENCY — CONCURRENT COLLECTIONS
 * ============================================================
 *
 * Topics:
 *   1. ConcurrentHashMap — thread-safe map, segment/CAS internals
 *   2. CopyOnWriteArrayList — read-heavy thread-safe list
 *   3. BlockingQueue — producer-consumer decoupling
 *   4. ConcurrentLinkedQueue — lock-free FIFO
 *   5. DelayQueue & PriorityBlockingQueue — ordered, timed access
 *
 * Run: javac 04_concurrent_collections.java && java ConcurrentCollectionsDemo
 * ============================================================
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ConcurrentCollectionsDemo {

    public static void main(String[] args) throws Exception {
        demo1_concurrentHashMap();
        demo2_copyOnWriteArrayList();
        demo3_blockingQueue_producerConsumer();
        demo4_concurrentLinkedQueue();
        demo5_delayQueue();
        demo6_which_collection_when();
    }

    // =========================================================
    // Demo 1: ConcurrentHashMap
    // =========================================================
    /**
     * ConcurrentHashMap internals:
     *   Java 7: 16 segments, each with its own lock (segment-level locking)
     *   Java 8+: Node array + CAS for insertions; synchronized only on individual bins
     *
     * Key differences from synchronized HashMap:
     *   - putIfAbsent, compute, merge — atomic compound operations
     *   - null keys/values NOT allowed (unlike HashMap)
     *   - size() is approximate under concurrent modification
     *   - Iteration is weakly consistent (won't throw ConcurrentModificationException)
     */
    static void demo1_concurrentHashMap() throws Exception {
        separator("Demo 1: ConcurrentHashMap");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // ── Basic thread-safe operations ──────────────────────────
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            pool.submit(() -> map.put("key-" + taskId, taskId));
        }
        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("  After 10 concurrent puts, size: " + map.size());

        // ── Atomic compound operations (avoids check-then-act race) ─
        // putIfAbsent — atomically insert if key not present
        map.putIfAbsent("new-key", 100);   // safe; no race with concurrent insert
        System.out.println("  putIfAbsent: " + map.get("new-key"));  // 100

        // compute — atomically read-modify-write
        map.put("counter", 0);
        map.compute("counter", (k, v) -> v == null ? 1 : v + 1);
        map.compute("counter", (k, v) -> v == null ? 1 : v + 1);
        System.out.println("  compute (×2): " + map.get("counter"));  // 2

        // computeIfAbsent — insert computed default if missing (lazy init pattern)
        map.computeIfAbsent("new", k -> k.length() * 10);
        System.out.println("  computeIfAbsent('new'): " + map.get("new"));  // 30

        // computeIfPresent — update only if key exists
        map.computeIfPresent("counter", (k, v) -> v * 100);
        System.out.println("  computeIfPresent: " + map.get("counter"));  // 200

        // merge — update or set (great for frequency counting)
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        ConcurrentHashMap<String, Integer> freq = new ConcurrentHashMap<>();
        for (String word : words) {
            freq.merge(word, 1, Integer::sum);  // if absent: put 1, if present: add 1
        }
        System.out.println("  Word frequency: " + freq);

        // ── Bulk operations (Java 8+) ─────────────────────────────
        // forEach, search, reduce — parallel, threshold-based
        freq.forEach(2, (k, v) ->   // parallelism threshold=2
            System.out.println("  forEach: " + k + "=" + v));

        Integer maxFreq = freq.reduceValues(2, Integer::max);
        System.out.println("  Max frequency: " + maxFreq);

        // ── ConcurrentHashMap as a Set ────────────────────────────
        Set<String> concurrentSet = ConcurrentHashMap.newKeySet();
        concurrentSet.add("A"); concurrentSet.add("B"); concurrentSet.add("A");
        System.out.println("  newKeySet (no dups): " + concurrentSet);
    }

    // =========================================================
    // Demo 2: CopyOnWriteArrayList
    // =========================================================
    /**
     * On every WRITE (add, set, remove):
     *   1. Acquires a lock.
     *   2. Makes a FULL COPY of the underlying array.
     *   3. Applies the modification to the copy.
     *   4. Replaces the reference atomically.
     *
     * On every READ:
     *   No lock. Reads the snapshot that existed when iteration began.
     *   Iteration will NEVER throw ConcurrentModificationException.
     *
     * Use when: reads >> writes (event listeners, config lists, observer registries).
     * Avoid when: writes are frequent — each write copies the entire array (O(n)).
     */
    static void demo2_copyOnWriteArrayList() throws Exception {
        separator("Demo 2: CopyOnWriteArrayList");

        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(List.of("A", "B", "C"));

        // Iterator sees a SNAPSHOT — doesn't see concurrent modifications
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            for (String s : list) {
                System.out.println("  [Reader] " + s);
                sleep(50);
            }
        });

        pool.submit(() -> {
            sleep(60);
            list.add("D");    // won't affect the reader's snapshot
            System.out.println("  [Writer] Added D — reader won't see it mid-iteration");
        });

        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("  Final list: " + list);

        // No ConcurrentModificationException — even if you modify during iteration
        // (ArrayList would throw this)
        for (String s : list) {
            if (s.equals("B")) list.add("B-extra");  // safe with COWAL
        }
        System.out.println("  After mid-iteration add: " + list);

        // addIfAbsent — atomic "add only if not present"
        boolean added = list.addIfAbsent("A");  // already there
        System.out.println("  addIfAbsent('A'): " + added);  // false
    }

    // =========================================================
    // Demo 3: BlockingQueue — Producer-Consumer
    // =========================================================
    /**
     * BlockingQueue implementations:
     *   ArrayBlockingQueue(n)    — bounded, FIFO, array-backed
     *   LinkedBlockingQueue(n?)  — optionally bounded, FIFO, linked nodes
     *   PriorityBlockingQueue    — unbounded, ordered by priority
     *   SynchronousQueue         — zero capacity: handoff between threads
     *   DelayQueue               — elements available only after delay
     *
     * Key methods:
     *   put(e)    — blocks if full (ArrayBlockingQueue)
     *   take()    — blocks if empty
     *   offer(e, time, unit) — try to put, timeout
     *   poll(time, unit)     — try to take, timeout
     *   offer(e)  — non-blocking, returns false if full
     *   peek()    — inspect head without removing
     */
    static void demo3_blockingQueue_producerConsumer() throws Exception {
        separator("Demo 3: BlockingQueue — Producer-Consumer");

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        // Producer — puts tasks onto the queue
        Thread producer = new Thread(() -> {
            String[] tasks = {"task-1", "task-2", "task-3", "task-4", "DONE"};
            for (String task : tasks) {
                try {
                    queue.put(task);   // blocks if queue is full (capacity=5)
                    System.out.println("  [Producer] Queued: " + task
                        + " | size=" + queue.size());
                    Thread.sleep(40);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "producer");

        // Consumer — takes tasks from the queue
        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    String task = queue.take();   // blocks if empty
                    if ("DONE".equals(task)) {
                        System.out.println("  [Consumer] Received DONE — stopping");
                        break;
                    }
                    System.out.println("  [Consumer] Processing: " + task);
                    Thread.sleep(100);            // slower than producer → queue fills up
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join(); consumer.join();

        // ── offer with timeout — non-blocking attempt ─────────────
        BlockingQueue<Integer> bounded = new ArrayBlockingQueue<>(2);
        bounded.put(1); bounded.put(2);
        boolean offered = bounded.offer(3, 100, TimeUnit.MILLISECONDS);  // full!
        System.out.println("\n  offer to full queue (with timeout): " + offered);  // false

        // ── SynchronousQueue — direct handoff (no buffering) ─────
        System.out.println("  SynchronousQueue: zero-capacity handoff (used by newCachedThreadPool)");
    }

    // =========================================================
    // Demo 4: ConcurrentLinkedQueue
    // =========================================================
    /**
     * Lock-free FIFO queue using Michael & Scott CAS-based algorithm.
     * Operations: O(1) amortized.
     * Non-blocking — never blocks, fails gracefully (poll() returns null).
     *
     * Use for: high-throughput, low-latency message passing.
     *          When producers/consumers don't need to wait for each other.
     */
    static void demo4_concurrentLinkedQueue() throws Exception {
        separator("Demo 4: ConcurrentLinkedQueue (Lock-Free)");

        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

        // Multiple producers add concurrently
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            final int base = i * 10;
            pool.submit(() -> {
                for (int j = 0; j < 5; j++) queue.offer(base + j);
            });
        }
        pool.shutdown(); pool.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("  Produced " + queue.size() + " items (no locks!)");

        // Drain (non-blocking)
        int consumed = 0;
        Integer item;
        while ((item = queue.poll()) != null) consumed++;   // poll() returns null if empty
        System.out.println("  Consumed: " + consumed);

        // vs BlockingQueue: CLQ = no waiting, BQ = can wait
        System.out.println("  ConcurrentLinkedQueue: never blocks; use for best-effort messaging");
        System.out.println("  BlockingQueue:         back-pressure capable; use for flow control");
    }

    // =========================================================
    // Demo 5: DelayQueue
    // =========================================================
    /**
     * Elements are only retrievable after their delay expires.
     * Used for: task scheduling, retry-with-backoff, session expiry, cache eviction.
     *
     * Elements must implement Delayed interface:
     *   getDelay(TimeUnit) — remaining time until available
     *   compareTo(Delayed) — ordering among elements
     */
    static void demo5_delayQueue() throws Exception {
        separator("Demo 5: DelayQueue — Timed Element Release");

        class DelayedTask implements Delayed {
            final String name;
            final long   releaseAt;   // absolute nanoTime

            DelayedTask(String name, long delayMs) {
                this.name      = name;
                this.releaseAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMs);
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return unit.convert(releaseAt - System.nanoTime(), TimeUnit.NANOSECONDS);
            }

            @Override
            public int compareTo(Delayed other) {
                return Long.compare(this.releaseAt, ((DelayedTask) other).releaseAt);
            }
        }

        DelayQueue<DelayedTask> queue = new DelayQueue<>();
        queue.put(new DelayedTask("Long task",   300));
        queue.put(new DelayedTask("Short task",  100));
        queue.put(new DelayedTask("Medium task", 200));

        System.out.println("  Polling in delay order (not insertion order):");
        for (int i = 0; i < 3; i++) {
            DelayedTask task = queue.take();   // blocks until delay expires
            System.out.println("  Released: " + task.name);
        }
    }

    // =========================================================
    // Demo 6: Which Collection When?
    // =========================================================
    static void demo6_which_collection_when() {
        separator("Demo 6: Choosing the Right Concurrent Collection");
        System.out.println("""
          ┌─────────────────────────────┬──────────────────────────────────────────────────────┐
          │ Collection                  │ Best For                                             │
          ├─────────────────────────────┼──────────────────────────────────────────────────────┤
          │ ConcurrentHashMap           │ Thread-safe key-value store; high-concurrency cache   │
          │ ConcurrentHashMap.newKeySet │ Thread-safe Set                                      │
          │ CopyOnWriteArrayList        │ Read-heavy list; event listener registries            │
          │ CopyOnWriteArraySet         │ Read-heavy set; observer registries                  │
          │ ArrayBlockingQueue          │ Bounded producer-consumer; back-pressure              │
          │ LinkedBlockingQueue         │ Unbounded (or bounded) producer-consumer              │
          │ PriorityBlockingQueue       │ Priority-ordered task queue                          │
          │ SynchronousQueue            │ Direct handoff; Exchanger-like; newCachedThreadPool   │
          │ ConcurrentLinkedQueue       │ Lock-free FIFO; high-throughput message passing       │
          │ ConcurrentLinkedDeque       │ Lock-free Deque; work-stealing queues                │
          │ DelayQueue                  │ Scheduled tasks; retry queues; session eviction       │
          │ LinkedTransferQueue         │ Combine LinkedBlocking + Synchronous behaviors       │
          └─────────────────────────────┴──────────────────────────────────────────────────────┘
          
          Common MISTAKE: wrapping a non-thread-safe collection with Collections.synchronizedList()
          The wrapper only synchronizes individual method calls — compound operations (check-then-act)
          are STILL not atomic. Use the concurrent variants above instead.
          
          Example of the bug:
            // WRONG — even with synchronized wrapper
            if (!list.contains(x)) list.add(x);  // two separate synchronized calls — race in between
          
            // RIGHT — use CopyOnWriteArrayList.addIfAbsent() or ConcurrentHashMap.putIfAbsent()
        """);
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
