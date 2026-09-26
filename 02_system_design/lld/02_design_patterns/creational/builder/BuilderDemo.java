package builder;

import java.util.*;

/**
 * Production-grade Java demonstration of the Builder Pattern with Fluent Method Chaining,
 * Invariant Validation, and Immutable Object Construction.
 */
public class BuilderDemo {

    public static class HttpRequest {
        private final String url;
        private final String method;
        private final Map<String, String> headers;
        private final String body;
        private final int timeoutMs;

        private HttpRequest(Builder builder) {
            this.url = builder.url;
            this.method = builder.method;
            this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
            this.body = builder.body;
            this.timeoutMs = builder.timeoutMs;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public Map<String, String> getHeaders() { return headers; }
        public String getBody() { return body; }
        public int getTimeoutMs() { return timeoutMs; }

        @Override
        public String toString() {
            return String.format("HttpRequest [Method:%s, URL:'%s', Headers:%s, Timeout:%dms, Body:'%s']",
                    method, url, headers, timeoutMs, body);
        }

        // Inner Builder Class
        public static class Builder {
            private final String url; // Mandatory
            private String method = "GET"; // Default
            private final Map<String, String> headers = new HashMap<>();
            private String body = "";
            private int timeoutMs = 5000; // Default 5s

            public Builder(String url) {
                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalArgumentException("URL is mandatory and cannot be empty!");
                }
                this.url = url;
            }

            public Builder method(String method) {
                this.method = method.toUpperCase();
                return this;
            }

            public Builder addHeader(String key, String value) {
                this.headers.put(key, value);
                return this;
            }

            public Builder body(String body) {
                this.body = body;
                return this;
            }

            public Builder timeoutMs(int timeoutMs) {
                this.timeoutMs = timeoutMs;
                return this;
            }

            public HttpRequest build() {
                // Validation Invariants
                if ("POST".equals(method) && (body == null || body.isEmpty())) {
                    System.out.println("Warning: POST request built with empty body.");
                }
                return new HttpRequest(this);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Builder Pattern Demo ===");

        // GET Request
        HttpRequest getReq = new HttpRequest.Builder("https://api.example.com/users/101")
                .addHeader("Accept", "application/json")
                .timeoutMs(2000)
                .build();
        System.out.println("\nGET Request: " + getReq);

        // POST Request
        HttpRequest postReq = new HttpRequest.Builder("https://api.example.com/users")
                .method("POST")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer token_xyz")
                .body("{\"name\": \"Alice\", \"role\": \"Admin\"}")
                .timeoutMs(10000)
                .build();
        System.out.println("\nPOST Request: " + postReq);
    }
}
