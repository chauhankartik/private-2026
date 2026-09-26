# System Design 2: Distributed Rate Limiter

## 1. Problem Statement & Requirements

Design a distributed, low-latency API rate limiter to protect backend microservices against denial-of-service (DoS) attacks, brute-force attacks, and resource starvation.

### Functional Requirements:
* Limit requests based on IP address, User ID, or API Key (e.g., $100 \text{ requests / minute}$).
* Return standard HTTP `429 Too Many Requests` status code with rate limit headers when limits are exceeded.

### Non-Functional Requirements:
* Ultra-Low Latency: Rate limit evaluation must add $< 2\text{ms}$ overhead to API calls.
* Distributed Accuracy: Rate limits must be consistently enforced across a cluster of stateless API Gateways.
* High Availability: Rate limiter failures must fail open (degrade gracefully without blocking valid user traffic).

---

## 2. Rate Limiting Algorithm Comparison

```
+-----------------------------------------------------------------------------------+
| Rate Limiting Algorithms                                                          |
+-------------------+-----------------------+-------------------+-------------------+
| Algorithm         | Memory Footprint      | Burst Capacity    | Accuracy / Spike  |
+-------------------+-----------------------+-------------------+-------------------+
| Token Bucket      | Low (2 numbers/user)  | Supported         | Smooth Rate       |
| Leaky Bucket      | Low (Fixed Queue)     | Fixed Rate Output | Constant Flow     |
| Fixed Window      | Lowest (Counter/user) | Allowed           | Boundary Spikes   |
| Sliding Log       | High (All timestamps) | Controlled        | Perfect Accuracy  |
| Sliding Counter   | Low (Current/Prev cnt)| Supported         | Near-Perfect      |
+-------------------+-----------------------+-------------------+-------------------+
```

### Preferred Algorithm: Token Bucket / Sliding Window Counter
* **Token Bucket:** A bucket holds up to $N$ tokens. Refilled at rate $R$ tokens/sec. A request consumes 1 token. If no tokens exist, request is rejected.

---

## 3. High-Level Architecture Blueprint

```
 Client Request (HTTP GET /api/v1/orders)
        |
        v
 +---------------------------------------------------+
 | API Gateway Layer (NGINX / Envoy / Spring Cloud)   |
 | Executes Rate Limiter Middleware Filter           |
 +-------------------------+-------------------------+
                           |
             Eval Rate     | Redis Lua Script Execution
             Limit Key     | (Atomic INCR & EXPIRE)
                           v
 +---------------------------------------------------+
 | Centralized In-Memory Cache (Redis Cluster)       |
 | Key: "rate:user_101:2026090717" -> Value: 42       |
 +---------------------------------------------------+
```

---

## 4. Deep Dive: Atomic Redis Evaluation (Lua Script)

Using separate Redis `GET` and `INCR` commands causes race conditions in multi-threaded API gateways.

### Atomic Redis Lua Script Solution:
```lua
-- Key 1: rate_limit_key (e.g., "rate:user_101")
-- ARG1: limit (100)
-- ARG2: window_seconds (60)

local current = redis.call('GET', KEYS[1])
if current and tonumber(current) >= tonumber(ARGV[1]) then
    return 0 -- Limit exceeded (Reject request)
else
    current = redis.call('INCR', KEYS[1])
    if tonumber(current) == 1 then
        redis.call('EXPIRE', KEYS[1], ARGV[2]) -- Set TTL on new key creation
    end
    return 1 -- Request allowed
end
```

### Rate Limit HTTP Response Headers:
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1700000060
Retry-After: 35
```
