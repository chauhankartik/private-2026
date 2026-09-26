package rate_limiter;

import rate_limiter.Enums.RateLimitAlgorithm;
import rate_limiter.Enums.RateLimitStatus;

public class RateLimiterModels {

    public static class RateLimiterConfig {
        private final RateLimitAlgorithm algorithm;
        private final long capacity;
        private final long windowSizeMillis;
        private final double refillTokensPerSecond;

        private RateLimiterConfig(Builder builder) {
            this.algorithm = builder.algorithm;
            this.capacity = builder.capacity;
            this.windowSizeMillis = builder.windowSizeMillis;
            this.refillTokensPerSecond = builder.refillTokensPerSecond;
        }

        public RateLimitAlgorithm getAlgorithm() { return algorithm; }
        public long getCapacity() { return capacity; }
        public long getWindowSizeMillis() { return windowSizeMillis; }
        public double getRefillTokensPerSecond() { return refillTokensPerSecond; }

        public static class Builder {
            private RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;
            private long capacity = 10;
            private long windowSizeMillis = 1000; // 1 second
            private double refillTokensPerSecond = 10.0;

            public Builder setAlgorithm(RateLimitAlgorithm algorithm) {
                this.algorithm = algorithm;
                return this;
            }

            public Builder setCapacity(long capacity) {
                this.capacity = capacity;
                return this;
            }

            public Builder setWindowSizeMillis(long windowSizeMillis) {
                this.windowSizeMillis = windowSizeMillis;
                return this;
            }

            public Builder setRefillTokensPerSecond(double refillTokensPerSecond) {
                this.refillTokensPerSecond = refillTokensPerSecond;
                return this;
            }

            public RateLimiterConfig build() {
                return new RateLimiterConfig(this);
            }
        }
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final RateLimitStatus status;
        private final long remainingTokens;
        private final long retryAfterMillis;

        public RateLimitResult(boolean allowed, RateLimitStatus status, long remainingTokens, long retryAfterMillis) {
            this.allowed = allowed;
            this.status = status;
            this.remainingTokens = remainingTokens;
            this.retryAfterMillis = retryAfterMillis;
        }

        public boolean isAllowed() { return allowed; }
        public RateLimitStatus getStatus() { return status; }
        public long getRemainingTokens() { return remainingTokens; }
        public long getRetryAfterMillis() { return retryAfterMillis; }

        @Override
        public String toString() {
            return String.format("RateLimitResult{allowed=%b, status=%s, remaining=%d, retryAfter=%dms}",
                    allowed, status, remainingTokens, retryAfterMillis);
        }
    }
}
