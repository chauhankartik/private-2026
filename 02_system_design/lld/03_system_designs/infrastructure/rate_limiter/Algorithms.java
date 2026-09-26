package rate_limiter;

import rate_limiter.Enums.RateLimitStatus;
import rate_limiter.RateLimiterModels.RateLimitResult;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Algorithms {

    // -------------------------------------------------------------------------
    // 1. TOKEN BUCKET ALGORITHM
    // -------------------------------------------------------------------------
    public static class TokenBucketLimiter implements IRateLimiter {
        private final long capacity;
        private final double refillTokensPerSecond;
        private double availableTokens;
        private long lastRefillTimestamp;

        public TokenBucketLimiter(long capacity, double refillTokensPerSecond) {
            this.capacity = capacity;
            this.refillTokensPerSecond = refillTokensPerSecond;
            this.availableTokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        private synchronized void refill() {
            long now = System.currentTimeMillis();
            double secondsPassed = (now - lastRefillTimestamp) / 1000.0;
            if (secondsPassed > 0) {
                availableTokens = Math.min(capacity, availableTokens + (secondsPassed * refillTokensPerSecond));
                lastRefillTimestamp = now;
            }
        }

        @Override
        public synchronized boolean allowRequest() {
            return checkRequest().isAllowed();
        }

        @Override
        public synchronized RateLimitResult checkRequest() {
            refill();
            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return new RateLimitResult(true, RateLimitStatus.ALLOWED, (long) availableTokens, 0);
            } else {
                long retryAfter = (long) Math.ceil((1.0 - availableTokens) / refillTokensPerSecond * 1000.0);
                return new RateLimitResult(false, RateLimitStatus.REJECTED_CAPACITY_EXCEEDED, 0, retryAfter);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. LEAKY BUCKET ALGORITHM
    // -------------------------------------------------------------------------
    public static class LeakyBucketLimiter implements IRateLimiter {
        private final long capacity;
        private final double leakRatePerSecond;
        private double currentWaterLevel;
        private long lastLeakTimestamp;

        public LeakyBucketLimiter(long capacity, double leakRatePerSecond) {
            this.capacity = capacity;
            this.leakRatePerSecond = leakRatePerSecond;
            this.currentWaterLevel = 0;
            this.lastLeakTimestamp = System.currentTimeMillis();
        }

        private synchronized void leak() {
            long now = System.currentTimeMillis();
            double secondsPassed = (now - lastLeakTimestamp) / 1000.0;
            if (secondsPassed > 0) {
                currentWaterLevel = Math.max(0, currentWaterLevel - (secondsPassed * leakRatePerSecond));
                lastLeakTimestamp = now;
            }
        }

        @Override
        public synchronized boolean allowRequest() {
            return checkRequest().isAllowed();
        }

        @Override
        public synchronized RateLimitResult checkRequest() {
            leak();
            if (currentWaterLevel + 1.0 <= capacity) {
                currentWaterLevel += 1.0;
                long remainingSpace = (long) (capacity - currentWaterLevel);
                return new RateLimitResult(true, RateLimitStatus.ALLOWED, remainingSpace, 0);
            } else {
                long retryAfter = (long) Math.ceil((currentWaterLevel + 1.0 - capacity) / leakRatePerSecond * 1000.0);
                return new RateLimitResult(false, RateLimitStatus.REJECTED_QUEUE_FULL, 0, retryAfter);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. FIXED WINDOW COUNTER ALGORITHM
    // -------------------------------------------------------------------------
    public static class FixedWindowLimiter implements IRateLimiter {
        private final long maxRequests;
        private final long windowSizeMillis;
        private long windowStartTimestamp;
        private final AtomicLong requestCount = new AtomicLong(0);

        public FixedWindowLimiter(long maxRequests, long windowSizeMillis) {
            this.maxRequests = maxRequests;
            this.windowSizeMillis = windowSizeMillis;
            this.windowStartTimestamp = System.currentTimeMillis();
        }

        @Override
        public synchronized boolean allowRequest() {
            return checkRequest().isAllowed();
        }

        @Override
        public synchronized RateLimitResult checkRequest() {
            long now = System.currentTimeMillis();
            if (now - windowStartTimestamp >= windowSizeMillis) {
                windowStartTimestamp = now;
                requestCount.set(0);
            }

            long currentCount = requestCount.incrementAndGet();
            if (currentCount <= maxRequests) {
                return new RateLimitResult(true, RateLimitStatus.ALLOWED, maxRequests - currentCount, 0);
            } else {
                long retryAfter = windowSizeMillis - (now - windowStartTimestamp);
                return new RateLimitResult(false, RateLimitStatus.REJECTED_CAPACITY_EXCEEDED, 0, retryAfter);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. SLIDING WINDOW LOG ALGORITHM
    // -------------------------------------------------------------------------
    public static class SlidingWindowLogLimiter implements IRateLimiter {
        private final long maxRequests;
        private final long windowSizeMillis;
        private final ConcurrentLinkedQueue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();

        public SlidingWindowLogLimiter(long maxRequests, long windowSizeMillis) {
            this.maxRequests = maxRequests;
            this.windowSizeMillis = windowSizeMillis;
        }

        @Override
        public synchronized boolean allowRequest() {
            return checkRequest().isAllowed();
        }

        @Override
        public synchronized RateLimitResult checkRequest() {
            long now = System.currentTimeMillis();
            long windowBoundary = now - windowSizeMillis;

            // Evict outdated logs
            while (!requestTimestamps.isEmpty() && requestTimestamps.peek() <= windowBoundary) {
                requestTimestamps.poll();
            }

            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.add(now);
                return new RateLimitResult(true, RateLimitStatus.ALLOWED, maxRequests - requestTimestamps.size(), 0);
            } else {
                Long oldestTimestamp = requestTimestamps.peek();
                long retryAfter = (oldestTimestamp != null) ? (oldestTimestamp + windowSizeMillis - now) : windowSizeMillis;
                return new RateLimitResult(false, RateLimitStatus.REJECTED_CAPACITY_EXCEEDED, 0, Math.max(0, retryAfter));
            }
        }
    }

    // -------------------------------------------------------------------------
    // 5. SLIDING WINDOW COUNTER ALGORITHM
    // -------------------------------------------------------------------------
    public static class SlidingWindowCounterLimiter implements IRateLimiter {
        private final long maxRequests;
        private final long windowSizeMillis;
        private long currentWindowStart;
        private long previousWindowCount;
        private long currentWindowCount;

        public SlidingWindowCounterLimiter(long maxRequests, long windowSizeMillis) {
            this.maxRequests = maxRequests;
            this.windowSizeMillis = windowSizeMillis;
            this.currentWindowStart = System.currentTimeMillis();
            this.previousWindowCount = 0;
            this.currentWindowCount = 0;
        }

        @Override
        public synchronized boolean allowRequest() {
            return checkRequest().isAllowed();
        }

        @Override
        public synchronized RateLimitResult checkRequest() {
            long now = System.currentTimeMillis();
            long timeIntoCurrentWindow = now - currentWindowStart;

            // Advance window if elapsed
            if (timeIntoCurrentWindow >= windowSizeMillis) {
                long windowsPassed = timeIntoCurrentWindow / windowSizeMillis;
                if (windowsPassed == 1) {
                    previousWindowCount = currentWindowCount;
                } else {
                    previousWindowCount = 0;
                }
                currentWindowCount = 0;
                currentWindowStart = now - (timeIntoCurrentWindow % windowSizeMillis);
                timeIntoCurrentWindow = now - currentWindowStart;
            }

            double previousWeight = (windowSizeMillis - timeIntoCurrentWindow) / (double) windowSizeMillis;
            double estimatedRequests = (previousWindowCount * previousWeight) + currentWindowCount;

            if (estimatedRequests + 1.0 <= maxRequests) {
                currentWindowCount++;
                long remaining = (long) (maxRequests - (estimatedRequests + 1.0));
                return new RateLimitResult(true, RateLimitStatus.ALLOWED, Math.max(0, remaining), 0);
            } else {
                long retryAfter = windowSizeMillis - timeIntoCurrentWindow;
                return new RateLimitResult(false, RateLimitStatus.REJECTED_CAPACITY_EXCEEDED, 0, retryAfter);
            }
        }
    }
}
