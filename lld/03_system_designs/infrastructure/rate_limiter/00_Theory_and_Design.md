# Rate Limiter System — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Strategy, Factory Method, Singleton, Facade, Decorator  
> **SOLID Coverage:** All 5 principles applied  
> **Key Technical Challenge:** High-throughput thread safety ($O(1)$ lookup), low latency overhead (< 1ms), and 5 classic rate limiting algorithms.

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design a Rate Limiter system to protect services from API abuse, DDoS attacks, and resource starvation. It should support:
> - Configurable rate limits per user/IP/API key or API endpoint.
> - Multiple Rate Limiting Algorithms:
>   1. **Token Bucket** (Supports bursty traffic, smooth refill)
>   2. **Leaky Bucket** (Smooth output rate, queue overflow rejection)
>   3. **Fixed Window Counter** (Simple time-window counters)
>   4. **Sliding Window Log** (Exact timestamp logs for 100% accuracy)
>   5. **Sliding Window Counter** (Memory-efficient approximation of sliding log)
> - Real-time request evaluation (`allowRequest(clientId)` returns boolean or metadata).
> - Thread-safe concurrent execution under high RPS (Requests Per Second).
> - Plug-and-play architecture (easily add new algorithms or rules)."

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| Is rate limiting **client-side or server-side / API Gateway**? | Dictates whether it's an embedded library or standalone service facade. |
| Is **bursty traffic** allowed or must output rate be **strictly uniform**? | Token Bucket allows bursts; Leaky Bucket forces constant rate. |
| What is the **acceptable latency overhead** per request? | Sliding Log uses $O(N)$ space per window; Token Bucket uses $O(1)$ space and low overhead. |
| Should we support **rule updates at runtime**? | Dynamic rules configuration via `RateLimiterConfig`. |
| Is the system **single-node in-memory** or **distributed (Redis)**? | Focus on thread-safe in-memory primitives (`AtomicLong`, `ReentrantLock`, `ConcurrentHashMap`) for LLD, with interface abstraction for distributed storage. |

---

## 3. Algorithm Comparison Matrix

| Algorithm | Time Complexity | Memory Overhead | Handles Bursts? | Accuracy | Primary Use Case |
|---|---|---|---|---|---|
| **Token Bucket** | $O(1)$ | $O(1)$ | ✅ Yes | High | General API Rate Limiting (AWS API Gateway) |
| **Leaky Bucket** | $O(1)$ | $O(Queue Capacity)$ | ❌ No (Smoothes) | High | E-Commerce checkout / Traffic shaping |
| **Fixed Window** | $O(1)$ | $O(1)$ | ✅ Yes (Boundary spike issue) | Low/Medium | Simple IP rate limiting |
| **Sliding Window Log** | $O(Log N)$ | $O(N)$ (Logs per window) | ✅ Yes | 100% Perfect | Financial transactions / Strict security |
| **Sliding Window Counter** | $O(1)$ | $O(1)$ | ✅ Yes | ~99% (Weighted approx) | High-scale microservices (Cloudflare) |

---

## 4. Class Diagram (UML Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          RateLimiterService (Facade/Singleton)                   │
│  - clientLimiterMap: Map<String, IRateLimiter>                                  │
│  - rulesConfig: RateLimiterConfig                                              │
│  + allowRequest(clientId): boolean                                             │
│  + checkRequest(clientId): RateLimitResult                                     │
└─────────────────────────────────────────────────────────────────────────────────┘
                                         │
                                         ▼ uses
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           IRateLimiter (Strategy Interface)                     │
│  + allowRequest(): boolean                                                     │
│  + checkRequest(): RateLimitResult                                             │
└─────────────────────────────────────────────────────────────────────────────────┘
           ▲                        ▲                        ▲
           │                        │                        │
┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐
│  TokenBucketLimiter  │ │  LeakyBucketLimiter  │ │ SlidingWindowCounter │
│  - capacity          │ │  - queueCapacity     │ │  - currentWindowCount│
│  - tokens (Atomic)   │ │  - leakRate          │ │  - prevWindowCount   │
│  - lastRefillTime    │ │  - queue (Blocking)  │ │  - windowSizeMillis  │
└──────────────────────┘ └──────────────────────┘ └──────────────────────┘
```

---

## 5. Design Patterns Applied

| Pattern | Component | Why It Was Chosen |
|---|---|---|
| **Strategy** | `IRateLimiter` | Decouples rate-limiting algorithms from service invocation. Switch between Token Bucket, Leaky Bucket, and Sliding Window seamlessly. |
| **Factory Method** | `RateLimiterFactory` | Instantiates appropriate algorithm instance based on configuration rules. |
| **Facade / Singleton** | `RateLimiterService` | Provides a unified, thread-safe entry point for checking incoming API requests. |
| **Builder** | `RateLimiterConfig` | Enables clean, readable configuration of rate limits (max requests, window size, refill rate). |
