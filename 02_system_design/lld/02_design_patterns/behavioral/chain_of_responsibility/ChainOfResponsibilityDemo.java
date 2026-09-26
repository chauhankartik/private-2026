package chain_of_responsibility;

/**
 * Production-grade Java demonstration of the Chain of Responsibility Pattern.
 * Scenario: HTTP Middleware Pipeline (Authentication -> Rate Limiting -> Data Validation).
 */
public class ChainOfResponsibilityDemo {

    public static class HttpRequest {
        public final String userToken;
        public final int requestCount;
        public final String payload;

        public HttpRequest(String userToken, int requestCount, String payload) {
            this.userToken = userToken;
            this.requestCount = requestCount;
            this.payload = payload;
        }
    }

    // --- Abstract Handler ---
    public static abstract class MiddlewareHandler {
        private MiddlewareHandler next;

        public MiddlewareHandler linkWith(MiddlewareHandler next) {
            this.next = next;
            return next;
        }

        public abstract boolean check(HttpRequest request);

        protected boolean checkNext(HttpRequest request) {
            if (next == null) {
                return true; // Reached end of pipeline successfully!
            }
            return next.check(request);
        }
    }

    // --- Concrete Handler 1: Authentication ---
    public static class AuthenticationHandler extends MiddlewareHandler {
        @Override
        public boolean check(HttpRequest request) {
            if (request.userToken == null || !"valid_token".equals(request.userToken)) {
                System.out.println("Pipeline REJECTED: 401 Unauthorized (Invalid Token)");
                return false;
            }
            System.out.println("Pipeline PASSED: Authentication succeeded.");
            return checkNext(request);
        }
    }

    // --- Concrete Handler 2: Rate Limiter ---
    public static class RateLimiterHandler extends MiddlewareHandler {
        private final int maxLimit;

        public RateLimiterHandler(int maxLimit) {
            this.maxLimit = maxLimit;
        }

        @Override
        public boolean check(HttpRequest request) {
            if (request.requestCount > maxLimit) {
                System.out.println("Pipeline REJECTED: 429 Too Many Requests (Exceeded limit " + maxLimit + ")");
                return false;
            }
            System.out.println("Pipeline PASSED: Rate Limiting check passed.");
            return checkNext(request);
        }
    }

    // --- Concrete Handler 3: Validation ---
    public static class PayloadValidationHandler extends MiddlewareHandler {
        @Override
        public boolean check(HttpRequest request) {
            if (request.payload == null || request.payload.trim().isEmpty()) {
                System.out.println("Pipeline REJECTED: 400 Bad Request (Payload is empty)");
                return false;
            }
            System.out.println("Pipeline PASSED: Payload Validation succeeded.");
            return checkNext(request);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Chain of Responsibility Pattern Demo ===");

        // Build Pipeline Chain: Auth -> RateLimit -> Validation
        MiddlewareHandler pipeline = new AuthenticationHandler();
        pipeline.linkWith(new RateLimiterHandler(5))
                .linkWith(new PayloadValidationHandler());

        // Test 1: Valid Request
        System.out.println("\n--- Test 1: Valid Request ---");
        HttpRequest req1 = new HttpRequest("valid_token", 2, "{\"orderId\": 101}");
        boolean result1 = pipeline.check(req1);
        System.out.println("Final Result: " + (result1 ? "SUCCESS 200" : "FAILED"));

        // Test 2: Invalid Token
        System.out.println("\n--- Test 2: Invalid Token ---");
        HttpRequest req2 = new HttpRequest("invalid_token", 1, "{\"orderId\": 102}");
        pipeline.check(req2);

        // Test 3: Exceeded Rate Limit
        System.out.println("\n--- Test 3: Rate Limit Exceeded ---");
        HttpRequest req3 = new HttpRequest("valid_token", 10, "{\"orderId\": 103}");
        pipeline.check(req3);
    }
}
