/**
 * ============================================================
 *  JAVA CONCURRENCY — COMPLETABLEFUTURE (ASYNC PIPELINES)
 * ============================================================
 *
 * Topics:
 *   1. supplyAsync / runAsync — starting an async computation
 *   2. thenApply / thenAccept / thenRun — chaining stages
 *   3. thenCompose — flatMap (chain futures)
 *   4. thenCombine / allOf / anyOf — combining futures
 *   5. exceptionally / handle / whenComplete — error handling
 *   6. Custom executor, timeouts (Java 9+)
 *   7. Real-world: parallel API calls with aggregation
 *
 * Run: javac 03_completablefuture.java && java CompletableFutureDemo
 * ============================================================
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

public class CompletableFutureDemo {

    static final ExecutorService pool = Executors.newFixedThreadPool(4,
        r -> { Thread t = new Thread(r); t.setName("cf-pool-" + t.getId()); return t; }
    );

    public static void main(String[] args) throws Exception {
        demo1_supplyAsync_runAsync();
        demo2_chaining_thenApply_thenAccept();
        demo3_thenCompose_flatMap();
        demo4_combining_futures();
        demo5_error_handling();
        demo6_timeout_executor();
        demo7_realWorld_parallelApiCalls();
        pool.shutdown();
    }

    // =========================================================
    // Demo 1: supplyAsync / runAsync
    // =========================================================
    static void demo1_supplyAsync_runAsync() throws Exception {
        separator("Demo 1: supplyAsync & runAsync");

        // supplyAsync — returns a value (Supplier)
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            sleep(50);
            return "Hello from supplyAsync";
        });

        // runAsync — no return value (Runnable)
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> {
            sleep(30);
            System.out.println("  runAsync completed on: " + Thread.currentThread().getName());
        });

        // completedFuture — already-done value (useful in tests)
        CompletableFuture<String> done = CompletableFuture.completedFuture("immediate");

        // failedFuture — already-failed (Java 9+)
        CompletableFuture<String> failed = CompletableFuture.failedFuture(
            new RuntimeException("pre-failed")
        );

        cf2.join();   // wait for runAsync
        System.out.println("  supplyAsync result: " + cf1.get());
        System.out.println("  completedFuture:    " + done.getNow("default")); // getNow = no blocking
        System.out.println("  isDone:  " + done.isDone());
        System.out.println("  isCompletedExceptionally: " + failed.isCompletedExceptionally());
    }

    // =========================================================
    // Demo 2: Chaining — thenApply / thenAccept / thenRun
    // =========================================================
    /**
     * thenApply(fn)     → transform result (like Stream.map) — returns CF<U>
     * thenAccept(fn)    → consume result, no return (like forEach) — returns CF<Void>
     * thenRun(fn)       → run something after, doesn't see result — returns CF<Void>
     *
     * Async variants (thenApplyAsync etc.) run the next stage on the ForkJoinPool
     * (or a custom executor). Without Async, it may run on the completing thread.
     */
    static void demo2_chaining_thenApply_thenAccept() throws Exception {
        separator("Demo 2: thenApply → thenAccept → thenRun");

        CompletableFuture.supplyAsync(() -> "  user-42", pool)      // fetch
            .thenApply(userId -> "Profile[" + userId.trim() + "]")  // transform
            .thenApply(String::toUpperCase)                          // transform again
            .thenAccept(result -> System.out.println("  Result: " + result))   // consume
            .thenRun(() -> System.out.println("  Pipeline complete."))         // side effect
            .join();

        // Async variants — next stage runs on pool, not completing thread
        CompletableFuture<String> async = CompletableFuture
            .supplyAsync(() -> "data", pool)
            .thenApplyAsync(s -> s.toUpperCase(), pool);   // explicit executor

        System.out.println("  thenApplyAsync: " + async.get());
    }

    // =========================================================
    // Demo 3: thenCompose — FlatMap (chaining futures)
    // =========================================================
    /**
     * thenApply(fn)   → fn returns a plain value  → CF<CF<T>> (nested — NOT what you want)
     * thenCompose(fn) → fn returns a CF<T>        → CF<T> (flat — correct)
     *
     * Same as flatMap() in streams/Optional.
     */
    static void demo3_thenCompose_flatMap() throws Exception {
        separator("Demo 3: thenCompose (flatMap)");

        // Simulated async services
        CompletableFuture<String> fetchUser(int id) {
        }
        // ↑ can't define methods inside methods — use lambdas below

        Function<Integer, CompletableFuture<String>> fetchUser =
            id -> CompletableFuture.supplyAsync(() -> {
                sleep(30);
                return "User[" + id + "]";
            }, pool);

        Function<String, CompletableFuture<String>> fetchProfile =
            user -> CompletableFuture.supplyAsync(() -> {
                sleep(30);
                return user + " → Profile{dept=Eng}";
            }, pool);

        // WRONG: thenApply → results in CF<CF<String>>
        CompletableFuture<CompletableFuture<String>> nested =
            fetchUser.apply(1).thenApply(fetchProfile);

        // RIGHT: thenCompose → results in CF<String> (flat)
        String result = fetchUser.apply(1)
            .thenCompose(fetchProfile)
            .get();
        System.out.println("  thenCompose result: " + result);

        // Chaining three stages
        String full = fetchUser.apply(42)
            .thenCompose(fetchProfile)
            .thenCompose(profile ->
                CompletableFuture.supplyAsync(() -> profile + " | last-login: today", pool))
            .get();
        System.out.println("  3-stage chain: " + full);
    }

    // =========================================================
    // Demo 4: Combining Multiple Futures
    // =========================================================
    static void demo4_combining_futures() throws Exception {
        separator("Demo 4: thenCombine / allOf / anyOf");

        // ── thenCombine — combine TWO futures when both complete ──
        CompletableFuture<String> userCF    = CompletableFuture.supplyAsync(() -> { sleep(50); return "Alice"; }, pool);
        CompletableFuture<Integer> scoreCF  = CompletableFuture.supplyAsync(() -> { sleep(80); return 95;     }, pool);

        String combined = userCF.thenCombine(scoreCF, (user, score) -> user + " scored " + score).get();
        System.out.println("  thenCombine: " + combined);

        // ── allOf — wait for ALL futures, no combined value ───────
        CompletableFuture<String> t1 = CompletableFuture.supplyAsync(() -> { sleep(30); return "A"; }, pool);
        CompletableFuture<String> t2 = CompletableFuture.supplyAsync(() -> { sleep(60); return "B"; }, pool);
        CompletableFuture<String> t3 = CompletableFuture.supplyAsync(() -> { sleep(10); return "C"; }, pool);

        // allOf returns CF<Void> — collect results manually
        CompletableFuture.allOf(t1, t2, t3).join();
        List<String> allResults = List.of(t1.join(), t2.join(), t3.join());
        System.out.println("  allOf results: " + allResults);

        // Idiom: stream of CFs → collect all results once allOf completes
        List<CompletableFuture<String>> futures = List.of(
            CompletableFuture.supplyAsync(() -> "X", pool),
            CompletableFuture.supplyAsync(() -> "Y", pool),
            CompletableFuture.supplyAsync(() -> "Z", pool)
        );
        List<String> gathered = CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
            .get();
        System.out.println("  allOf stream gather: " + gathered);

        // ── anyOf — return FIRST to complete ──────────────────────
        Object first = CompletableFuture.anyOf(
            CompletableFuture.supplyAsync(() -> { sleep(100); return "slow"; }, pool),
            CompletableFuture.supplyAsync(() -> { sleep(10);  return "fast"; }, pool)
        ).get();
        System.out.println("  anyOf first: " + first);  // "fast"
    }

    // =========================================================
    // Demo 5: Error Handling
    // =========================================================
    /**
     * exceptionally(fn) → recover from exception, returns a fallback value
     * handle(fn)        → called whether success OR failure; can inspect both
     * whenComplete(fn)  → side-effect on completion; doesn't change the result
     */
    static void demo5_error_handling() throws Exception {
        separator("Demo 5: Error Handling");

        // ── exceptionally — fallback value ────────────────────────
        String result1 = CompletableFuture
            .supplyAsync(() -> { throw new RuntimeException("DB down"); })
            .exceptionally(ex -> "fallback-value")
            .thenApply(v -> "processed: " + v)
            .get().toString();
        System.out.println("  exceptionally: " + result1);

        // ── handle — always called, can recover or re-throw ───────
        String result2 = CompletableFuture
            .<String>supplyAsync(() -> { throw new RuntimeException("timeout"); })
            .handle((value, ex) -> {
                if (ex != null) {
                    System.out.println("  handle: caught " + ex.getCause().getMessage());
                    return "recovered";
                }
                return value;
            })
            .get();
        System.out.println("  handle result: " + result2);

        // ── whenComplete — logging / metrics (doesn't change value) ─
        CompletableFuture.supplyAsync(() -> "OK", pool)
            .whenComplete((value, ex) -> {
                if (ex != null) System.out.println("  whenComplete: FAILED " + ex.getMessage());
                else            System.out.println("  whenComplete: SUCCESS " + value);
            })
            .join();

        // ── Propagating exceptions through the chain ──────────────
        // If a stage throws, all downstream stages skip UNLESS exceptionally/handle catches it
        CompletableFuture<String> pipeline = CompletableFuture
            .supplyAsync(() -> "step1")
            .thenApply(s -> { throw new RuntimeException("step2 fails"); })
            .thenApply(s -> "step3 — will NOT run")
            .exceptionally(ex -> "caught at end: " + ex.getCause().getMessage());

        System.out.println("  propagation: " + pipeline.get());
    }

    // =========================================================
    // Demo 6: Timeout & Custom Executor (Java 9+)
    // =========================================================
    static void demo6_timeout_executor() throws Exception {
        separator("Demo 6: Timeout (Java 9+) & Custom Executor");

        // orTimeout — complete exceptionally if not done in time (Java 9+)
        try {
            String result = CompletableFuture
                .supplyAsync(() -> { sleep(500); return "too slow"; }, pool)
                .orTimeout(100, TimeUnit.MILLISECONDS)
                .get();
        } catch (ExecutionException e) {
            System.out.println("  orTimeout: " + e.getCause().getClass().getSimpleName());
        }

        // completeOnTimeout — provide a default value if timed out (Java 9+)
        String result = CompletableFuture
            .supplyAsync(() -> { sleep(500); return "too slow"; }, pool)
            .completeOnTimeout("default", 100, TimeUnit.MILLISECONDS)
            .get();
        System.out.println("  completeOnTimeout: " + result);  // "default"

        // Custom executor — control which thread pool stages run on
        ExecutorService ioPool = Executors.newCachedThreadPool(
            r -> { Thread t = new Thread(r); t.setName("io-" + t.getId()); return t; }
        );
        CompletableFuture.supplyAsync(() -> "fetched", ioPool)     // on ioPool
            .thenApplyAsync(s -> s.toUpperCase(), pool)            // on CPU pool
            .thenAcceptAsync(s -> System.out.println("  Custom executor result: " + s), pool)
            .join();
        ioPool.shutdown();
    }

    // =========================================================
    // Demo 7: Real-World — Parallel API Calls with Aggregation
    // =========================================================
    /**
     * Scenario: A product page needs data from 3 microservices in parallel:
     *   - Product service    (50ms)
     *   - Inventory service  (80ms)
     *   - Pricing service    (60ms)
     *
     * Sequential: 50 + 80 + 60 = 190ms
     * Parallel:   max(50, 80, 60) = 80ms
     */
    static void demo7_realWorld_parallelApiCalls() throws Exception {
        separator("Demo 7: Real-World — Parallel Microservice Calls");

        record ProductInfo(String name, int stock, double price) {}

        // Simulate 3 slow service calls
        CompletableFuture<String> productCF = CompletableFuture.supplyAsync(() -> {
            sleep(50);
            return "MacBook Pro 14";
        }, pool);

        CompletableFuture<Integer> inventoryCF = CompletableFuture.supplyAsync(() -> {
            sleep(80);
            return 42;
        }, pool);

        CompletableFuture<Double> pricingCF = CompletableFuture.supplyAsync(() -> {
            sleep(60);
            return 199999.0;
        }, pool);

        long start = System.currentTimeMillis();

        // Aggregate: wait for all three
        ProductInfo info = productCF
            .thenCombine(inventoryCF, (name, stock) -> new Object[]{name, stock})
            .thenCombine(pricingCF, (pair, price) ->
                new ProductInfo((String) pair[0], (Integer) pair[1], price))
            .get();

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("  Product: " + info.name());
        System.out.println("  Stock:   " + info.stock());
        System.out.println("  Price:   ₹" + info.price());
        System.out.printf("  Elapsed: %dms (parallel vs ~190ms sequential)%n", elapsed);

        // Alternative using allOf + join pattern
        System.out.println("\n  allOf + stream aggregation pattern:");
        List<CompletableFuture<String>> serviceCalls = List.of(
            CompletableFuture.supplyAsync(() -> { sleep(30); return "userService: OK"; }, pool),
            CompletableFuture.supplyAsync(() -> { sleep(50); return "orderService: OK"; }, pool),
            CompletableFuture.supplyAsync(() -> { sleep(20); return "paymentService: OK"; }, pool)
        );
        List<String> allOk = CompletableFuture
            .allOf(serviceCalls.toArray(new CompletableFuture[0]))
            .thenApply(v -> serviceCalls.stream().map(CompletableFuture::join).toList())
            .get();
        allOk.forEach(r -> System.out.println("  " + r));
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
