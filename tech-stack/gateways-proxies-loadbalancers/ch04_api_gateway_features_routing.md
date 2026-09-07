# Chapter 4: API Gateway Core Features & Routing Engines

An **API Gateway** acts as the single, centralized entry point for client applications, insulating clients from internal microservice architectures while executing traffic management and security policies.

---

## 1. Architectural Distinctions: Load Balancer vs Reverse Proxy vs API Gateway

```
[ Load Balancer ]    ──> Distributes traffic across homogeneous backend pools (L4 / L7).
[ Reverse Proxy ]    ──> Intercepts requests, offloads TLS, caches responses, hides backend IPs.
[ API Gateway ]      ──> Advanced Reverse Proxy handling API routing, rate limiting, auth, & resilience.
```

---

## 2. Dynamic Routing & Transformation Engine

API Gateways evaluate incoming request metadata against a list of declarative route rules:

```yaml
# Route Rule Configuration Example (Envoy / Gateway API style)
routes:
- match:
    prefix: "/api/v1/users"
    headers:
    - name: "X-App-Version"
      exact_match: "v2"
  route:
    cluster: "user_service_v2"
    prefix_rewrite: "/users"
```

* **Path & Header Matching:** Routes traffic based on URI prefixes, HTTP methods (`GET`/`POST`), `Host` headers, or custom headers (`X-Tenant-ID`).
* **Path Rewriting & Header Mutation:** Strips edge URL prefixes before passing requests to internal microservices and injects tracing/security headers (`X-Forwarded-For`, `X-User-ID`).

---

## 3. Traffic Shaping: Rate Limiting Algorithms

API Gateways enforce rate limits to protect backend microservices from denial-of-service (DoS) attacks and thundering herd spikes.

```
[ Token Bucket Algorithm ]                             [ Leaky Bucket Algorithm ]
  Tokens added at fixed rate R                           Requests enter bucket
  Bucket capacity B (Allows Bursts up to B)             Leaks at fixed smooth output rate
        |                                                      |
        v                                                      v
  Capacity full? Drops excess requests                   Bucket full? Rejects excess with HTTP 429
```

### 3.1 Distributed Rate Limiting Implementation
* **Single Instance Proxy:** Uses Linux shared memory slabs (e.g. NGINX `limit_req_zone`).
* **Distributed Gateway Cluster:** Uses centralized Redis with atomic Lua scripts or sliding window counters to synchronize rate limit state across multiple gateway instances.

---

## 4. Resilience: Circuit Breaking State Machine

The API Gateway Circuit Breaker prevents cascading failures by stopping traffic to failing backend services.

```
                        Normal Operations (Requests Pass)
                                  ┌──────────┐
                                  │  CLOSED  │
                                  └──────────┘
                                       │
                                       │ Error Rate > Threshold (e.g. 50% 5xx)
                                       v
   Sample Traffic Succeeds        ┌──────────┐
  ┌────────────────────────────── │   OPEN   │ (Short-circuits all requests;
  │                               └──────────┘  returns HTTP 503 instantly)
  │                                    │
  v                                    │ Sleep Window Expires (e.g. 30 seconds)
┌───────────┐                          v
│ HALF-OPEN │ <────────────────────────┘
└───────────┘
```

---

## 5. Staff Engineer Gateway Rules
1. **Offload Authentication at the Edge:** Validate JWT signatures and OAuth2 tokens at the API Gateway edge. Inject verified claims (`X-User-Id`, `X-User-Roles`) as HTTP headers into internal microservice requests.
2. **Always Configure Timeouts & Retries:** Never allow an API Gateway route to run without explicit timeout limits (`timeout: 3s`) and retry policies (`retry_on: "5xx", num_retries: 2`).
3. **Prefer Token Bucket for User APIs:** Use Token Bucket rate limiting for public user APIs to accommodate natural human interaction bursts while preventing continuous abuse.
