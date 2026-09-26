package rate_limiter;

import rate_limiter.RateLimiterModels.RateLimitResult;

public interface IRateLimiter {
    boolean allowRequest();
    RateLimitResult checkRequest();
}
