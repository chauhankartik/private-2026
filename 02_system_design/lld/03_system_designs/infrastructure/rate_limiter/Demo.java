package rate_limiter;

import rate_limiter.Enums.RateLimitAlgorithm;
import rate_limiter.RateLimiterModels.RateLimitResult;
import rate_limiter.RateLimiterModels.RateLimiterConfig;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 🚀 RATE LIMITER SYSTEM — LLD INTERVIEW DEMO (5 Core Algorithms)");
        System.out.println("======================================================================\n");

        RateLimiterService service = RateLimiterService.getInstance();

        // -------------------------------------------------------------------------
        // SCENARIO 1: REGISTER CLIENTS WITH DIFFERENT ALGORITHMS
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 1: Configuring Rate Limiters for 5 Clients...");

        // Client 1: Token Bucket (Capacity 5, Refill 2/sec)
        service.registerClient("client_token_bucket", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.TOKEN_BUCKET)
                .setCapacity(5)
                .setRefillTokensPerSecond(2.0)
                .build());

        // Client 2: Leaky Bucket (Capacity 3, Leak 1/sec)
        service.registerClient("client_leaky_bucket", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.LEAKY_BUCKET)
                .setCapacity(3)
                .setRefillTokensPerSecond(1.0)
                .build());

        // Client 3: Fixed Window (Max 4 requests per 1000ms)
        service.registerClient("client_fixed_window", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.FIXED_WINDOW)
                .setCapacity(4)
                .setWindowSizeMillis(1000)
                .build());

        // Client 4: Sliding Window Log (Max 3 requests per 1000ms)
        service.registerClient("client_sliding_log", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.SLIDING_WINDOW_LOG)
                .setCapacity(3)
                .setWindowSizeMillis(1000)
                .build());

        // Client 5: Sliding Window Counter (Max 4 requests per 1000ms)
        service.registerClient("client_sliding_counter", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.SLIDING_WINDOW_COUNTER)
                .setCapacity(4)
                .setWindowSizeMillis(1000)
                .build());

        System.out.println("✅ All 5 algorithms configured.\n");

        // -------------------------------------------------------------------------
        // SCENARIO 2: TEST TOKEN BUCKET (BURST & REFILL)
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 2: Testing Token Bucket (Burst of 7 requests, Capacity=5)...");
        for (int i = 1; i <= 7; i++) {
            RateLimitResult result = service.checkRequest("client_token_bucket");
            System.out.printf("  Req #%d -> Allowed: %b | Status: %s | Remaining: %d%n",
                    i, result.isAllowed(), result.getStatus(), result.getRemainingTokens());
        }

        System.out.println("⏳ Sleeping 1.5s for Token Bucket refill...");
        Thread.sleep(1500);

        RateLimitResult refillResult = service.checkRequest("client_token_bucket");
        System.out.printf("  Req after refill -> Allowed: %b | Status: %s | Remaining: %d%n\n",
                refillResult.isAllowed(), refillResult.getStatus(), refillResult.getRemainingTokens());

        // -------------------------------------------------------------------------
        // SCENARIO 3: TEST LEAKY BUCKET
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 3: Testing Leaky Bucket (Capacity=3)...");
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = service.checkRequest("client_leaky_bucket");
            System.out.printf("  Req #%d -> Allowed: %b | Status: %s | Remaining Capacity: %d%n",
                    i, result.isAllowed(), result.getStatus(), result.getRemainingTokens());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 4: TEST FIXED WINDOW COUNTER
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 4: Testing Fixed Window Counter (Limit=4/sec)...");
        for (int i = 1; i <= 6; i++) {
            RateLimitResult result = service.checkRequest("client_fixed_window");
            System.out.printf("  Req #%d -> Allowed: %b | Remaining: %d | RetryAfter: %dms%n",
                    i, result.isAllowed(), result.getRemainingTokens(), result.getRetryAfterMillis());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 5: TEST SLIDING WINDOW LOG
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 5: Testing Sliding Window Log (Limit=3/sec)...");
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = service.checkRequest("client_sliding_log");
            System.out.printf("  Req #%d -> Allowed: %b | Status: %s%n",
                    i, result.isAllowed(), result.getStatus());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 6: TEST SLIDING WINDOW COUNTER
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 6: Testing Sliding Window Counter (Limit=4/sec)...");
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = service.checkRequest("client_sliding_counter");
            System.out.printf("  Req #%d -> Allowed: %b | Status: %s%n",
                    i, result.isAllowed(), result.getStatus());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 7: MULTI-THREADED CONCURRENT LOAD TEST
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 7: Testing Multithreaded Concurrency (10 threads hitting Token Bucket)...");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        service.registerClient("concurrent_client", new RateLimiterConfig.Builder()
                .setAlgorithm(RateLimitAlgorithm.TOKEN_BUCKET)
                .setCapacity(20)
                .setRefillTokensPerSecond(5.0)
                .build());

        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    if (service.allowRequest("concurrent_client")) {
                        allowedCount.incrementAndGet();
                    } else {
                        rejectedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Fire all threads simultaneously
        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.printf("  📊 Concurrent Results: Total Requests: 50 | Allowed: %d | Rejected: %d%n",
                allowedCount.get(), rejectedCount.get());

        if (allowedCount.get() <= 20) {
            System.out.println("✅ Multithreaded Thread-Safety Verified! Rate limiter enforced capacity strictly.");
        } else {
            System.out.println("❌ Race Condition Detected! Capacity exceeded.");
        }

        System.out.println("\n======================================================================");
        System.out.println(" 🚀 RATE LIMITER SYSTEM DEMO COMPLETED SUCCESSFULLY");
        System.out.println("======================================================================");
    }
}
