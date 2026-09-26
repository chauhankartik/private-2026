// =============================================================================
// FILE: 08_Demo.java
// TOPIC: In-Memory Cache LLD — Complete Demo (Interview Walkthrough)
//
// Demonstrates 8 scenarios:
//   1. LRU cache — basic hit/miss + eviction order
//   2. LFU cache — frequency-based eviction
//   3. FIFO cache — insertion-order eviction
//   4. TTL (per-entry expiry)
//   5. TTL + background sweeper
//   6. Cache statistics (hit rate, eviction count)
//   7. Concurrent access (multi-thread safety)
//   8. Cache as a read-through pattern
//
// RUN: javac -d out cache/*.java && java -cp out cache.Demo
// =============================================================================

package cache;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=".repeat(70));
        System.out.println("  IN-MEMORY CACHE LLD — Complete Demo");
        System.out.println("=".repeat(70));

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 1: LRU Cache — eviction order demonstration
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 1: LRU Cache (capacity=3)");
        System.out.println("-".repeat(50));

        LRUEvictionPolicy<String> lruPolicy = new LRUEvictionPolicy<>();
        Cache<String, Integer> lruCache = new InMemoryCache.Builder<String, Integer>()
                .capacity(3)
                .evictionPolicy(lruPolicy)
                .build();

        lruCache.put("A", 1);   // [A]
        lruCache.put("B", 2);   // [B, A]
        lruCache.put("C", 3);   // [C, B, A]
        System.out.println("After put A,B,C: " + lruPolicy.orderMruToLru());

        lruCache.get("A");      // access A → moves to MRU: [A, C, B]
        System.out.println("After get(A):   " + lruPolicy.orderMruToLru());

        lruCache.put("D", 4);   // capacity full → evict LRU (B): [D, A, C]
        System.out.println("After put(D):   " + lruPolicy.orderMruToLru());

        System.out.println("get(B)=" + lruCache.get("B") + " (evicted — miss)");
        System.out.println("get(A)=" + lruCache.get("A") + " (still present — hit)");
        System.out.println("get(C)=" + lruCache.get("C") + " (still present — hit)");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 2: LFU Cache — frequency-based eviction
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 2: LFU Cache (capacity=3)");
        System.out.println("-".repeat(50));

        LFUEvictionPolicy<String> lfuPolicy = new LFUEvictionPolicy<>();
        Cache<String, String> lfuCache = new InMemoryCache.Builder<String, String>()
                .capacity(3)
                .evictionPolicy(lfuPolicy)
                .build();

        lfuCache.put("X", "val-X");   // freq: X=1
        lfuCache.put("Y", "val-Y");   // freq: Y=1
        lfuCache.put("Z", "val-Z");   // freq: Z=1
        System.out.println("State: " + lfuPolicy.debugState());

        lfuCache.get("X");   // freq: X=2
        lfuCache.get("X");   // freq: X=3
        lfuCache.get("Y");   // freq: Y=2
        System.out.println("After accesses (X×3, Y×2, Z×1): " + lfuPolicy.debugState());

        // Z has freq=1 → it should be evicted when W is inserted
        lfuCache.put("W", "val-W");
        System.out.println("After put(W) — Z evicted (lowest freq): " + lfuPolicy.debugState());
        System.out.println("get(Z)=" + lfuCache.get("Z") + " (evicted — miss)");
        System.out.println("get(X)=" + lfuCache.get("X") + " (still present — hit)");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 3: FIFO Cache — insertion-order eviction
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 3: FIFO Cache (capacity=3)");
        System.out.println("-".repeat(50));

        FIFOEvictionPolicy<Integer> fifoPolicy = new FIFOEvictionPolicy<>();
        Cache<Integer, String> fifoCache = new InMemoryCache.Builder<Integer, String>()
                .capacity(3)
                .evictionPolicy(fifoPolicy)
                .build();

        fifoCache.put(1, "one");    // queue: [1]
        fifoCache.put(2, "two");    // queue: [1, 2]
        fifoCache.put(3, "three");  // queue: [1, 2, 3]
        System.out.println("After put 1,2,3: " + fifoPolicy.insertionOrder());

        fifoCache.get(1);  // FIFO: access doesn't change order (unlike LRU)
        System.out.println("After get(1) — order unchanged: " + fifoPolicy.insertionOrder());

        fifoCache.put(4, "four");   // evicts 1 (first inserted)
        System.out.println("After put(4): " + fifoPolicy.insertionOrder());
        System.out.println("get(1)=" + fifoCache.get(1) + " (evicted even though accessed — FIFO)");
        System.out.println("get(2)=" + fifoCache.get(2) + " (present — hit)");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 4: TTL — Per-entry expiry
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 4: TTL Expiry (200ms TTL)");
        System.out.println("-".repeat(50));

        Cache<String, String> ttlCache = new InMemoryCache.Builder<String, String>()
                .capacity(10)
                .evictionPolicy(new LRUEvictionPolicy<>())
                .build();

        ttlCache.put("session:user-42", "token-abc", 200);  // expires in 200ms
        ttlCache.put("permanent-key", "always-here");        // no expiry

        System.out.println("Before expiry: session=" + ttlCache.get("session:user-42"));
        System.out.println("Before expiry: permanent=" + ttlCache.get("permanent-key"));

        Thread.sleep(300);  // wait for TTL to expire

        System.out.println("After 300ms:   session=" + ttlCache.get("session:user-42")
                + " (expired → empty)");
        System.out.println("After 300ms:   permanent=" + ttlCache.get("permanent-key")
                + " (still alive)");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 5: Background TTL Sweeper
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 5: Background TTL Sweeper (sweep every 150ms)");
        System.out.println("-".repeat(50));

        InMemoryCache<String, String> sweptCache = new InMemoryCache.Builder<String, String>()
                .capacity(10)
                .evictionPolicy(new LRUEvictionPolicy<>())
                .ttlCleanupIntervalMs(150)   // sweep expired entries every 150ms
                .build();

        sweptCache.put("k1", "v1", 100);
        sweptCache.put("k2", "v2", 100);
        sweptCache.put("k3", "v3", 100);

        System.out.println("Before expiry: size=" + sweptCache.size());  // 3
        Thread.sleep(300);  // entries expire + sweeper runs
        System.out.println("After 300ms:   size=" + sweptCache.size()   // 0 (swept)
                + " (background sweeper removed expired entries)");
        System.out.println("Stats: " + sweptCache.getStats());
        sweptCache.shutdown();

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 6: Cache Statistics
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 6: Cache Statistics");
        System.out.println("-".repeat(50));

        Cache<String, String> statsCache = new InMemoryCache.Builder<String, String>()
                .capacity(3)
                .evictionPolicy(new LRUEvictionPolicy<>())
                .build();

        statsCache.put("a", "1");
        statsCache.put("b", "2");
        statsCache.put("c", "3");

        statsCache.get("a");   // hit
        statsCache.get("a");   // hit
        statsCache.get("b");   // hit
        statsCache.get("x");   // miss — never inserted
        statsCache.get("y");   // miss — never inserted

        statsCache.put("d", "4");  // evicts LRU (c) → 1 eviction

        CacheStats s = statsCache.getStats();
        System.out.println("Hits:      " + s.getHits());          // 3
        System.out.println("Misses:    " + s.getMisses());         // 2
        System.out.println("Evictions: " + s.getEvictions());      // 1
        System.out.printf ("Hit Rate:  %.1f%%%n", s.hitRate() * 100); // 60.0%

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 7: Multi-threaded Concurrent Access
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 7: Concurrent Access (10 threads, 1000 ops)");
        System.out.println("-".repeat(50));

        Cache<Integer, String> concCache = new InMemoryCache.Builder<Integer, String>()
                .capacity(20)
                .evictionPolicy(new LRUEvictionPolicy<>())
                .build();

        int threadCount = 10;
        int opsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = (threadId * opsPerThread + i) % 30;  // key space 0–29
                        if (i % 3 == 0) {
                            concCache.put(key, "value-" + key);
                        } else {
                            concCache.get(key);   // may hit or miss
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Thread error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("All threads completed. Errors: " + errorCount.get()
                + " (expected 0)");
        System.out.println("Final cache stats: " + concCache.getStats());

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 8: Read-Through Pattern (Cache-Aside simulation)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 8: Cache-Aside (Read-Through) Pattern");
        System.out.println("-".repeat(50));

        Cache<Integer, String> readThroughCache = new InMemoryCache.Builder<Integer, String>()
                .capacity(5)
                .evictionPolicy(new LRUEvictionPolicy<>())
                .build();

        // Simulate DB lookup with cache-aside pattern
        System.out.println("Fetching userId=1: " + fetchUser(1, readThroughCache));  // DB hit
        System.out.println("Fetching userId=1: " + fetchUser(1, readThroughCache));  // Cache hit
        System.out.println("Fetching userId=2: " + fetchUser(2, readThroughCache));  // DB hit
        System.out.println("Final stats: " + readThroughCache.getStats());

        System.out.println("\n▶ All scenarios completed successfully.");
    }

    // ─── Cache-aside helper ───────────────────────────────────────────────────

    /**
     * Cache-aside (read-through) pattern:
     *   1. Check cache first.
     *   2. On miss: query the source of truth (DB), populate cache, return.
     *   3. On hit: return cached value directly.
     */
    private static String fetchUser(int userId, Cache<Integer, String> cache) {
        Optional<String> cached = cache.get(userId);
        if (cached.isPresent()) {
            System.out.print("  [CACHE HIT]  ");
            return cached.get();
        }
        // Cache miss — simulate DB query
        System.out.print("  [DB QUERY]   ");
        String user = "User{id=" + userId + ", name=User_" + userId + "}";
        cache.put(userId, user, 5000);   // cache for 5 seconds
        return user;
    }
}


// =============================================================================
// ■ EXPECTED OUTPUT SUMMARY
// =============================================================================
//
// SCENARIO 1 (LRU):
//   Order tracks MRU→LRU correctly; B evicted on put(D) since B was LRU.
//
// SCENARIO 2 (LFU):
//   Z (freq=1) evicted when W inserted; X (freq=3) and Y (freq=2) survive.
//
// SCENARIO 3 (FIFO):
//   Key 1 evicted on put(4) despite being accessed — FIFO ignores access.
//
// SCENARIO 4 (TTL):
//   Session key returns empty after 300ms; permanent key unaffected.
//
// SCENARIO 5 (Sweeper):
//   Size drops to 0 after background sweeper runs on expired entries.
//
// SCENARIO 6 (Stats):
//   Hit rate = 3/(3+2) = 60%. 1 eviction when D pushed out C.
//
// SCENARIO 7 (Concurrency):
//   0 errors — ReadWriteLock ensures safe concurrent access.
//
// SCENARIO 8 (Cache-aside):
//   First fetch: DB QUERY + cache populate. Second fetch: CACHE HIT.
