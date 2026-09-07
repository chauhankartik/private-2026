package rate_limiter;

import rate_limiter.Algorithms.*;
import rate_limiter.RateLimiterModels.RateLimitResult;
import rate_limiter.RateLimiterModels.RateLimiterConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterFactory {
    public static IRateLimiter createRateLimiter(RateLimiterConfig config) {
        switch (config.getAlgorithm()) {
            case TOKEN_BUCKET:
                return new TokenBucketLimiter(config.getCapacity(), config.getRefillTokensPerSecond());
            case LEAKY_BUCKET:
                return new LeakyBucketLimiter(config.getCapacity(), config.getRefillTokensPerSecond());
            case FIXED_WINDOW:
                return new FixedWindowLimiter(config.getCapacity(), config.getWindowSizeMillis());
            case SLIDING_WINDOW_LOG:
                return new SlidingWindowLogLimiter(config.getCapacity(), config.getWindowSizeMillis());
            case SLIDING_WINDOW_COUNTER:
                return new SlidingWindowCounterLimiter(config.getCapacity(), config.getWindowSizeMillis());
            default:
                return new TokenBucketLimiter(config.getCapacity(), config.getRefillTokensPerSecond());
        }
    }
}
