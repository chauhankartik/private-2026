package rate_limiter;

public class Enums {

    public enum RateLimitAlgorithm {
        TOKEN_BUCKET,
        LEAKY_BUCKET,
        FIXED_WINDOW,
        SLIDING_WINDOW_LOG,
        SLIDING_WINDOW_COUNTER
    }

    public enum RateLimitStatus {
        ALLOWED,
        REJECTED_CAPACITY_EXCEEDED,
        REJECTED_QUEUE_FULL
    }
}
