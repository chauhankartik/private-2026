# Chapter 2: Layer 4 vs Layer 7 Load Balancing & Algorithms

Load balancers distribute network traffic across backend pools to optimize resource utilization, maximize throughput, and prevent server overload.

---

## 1. Layer 4 vs Layer 7 Proxying

```
[ Client TCP Connection ]
          |
          v
+-------------------------------------------------------------------+
| Layer 4 Load Balancer (TCP/UDP Proxying)                          |
| - Inspects ONLY IP Header & TCP/UDP Port                          |
| - Does NOT parse HTTP headers, paths, or payload                  |
| - Fast packet forwarding / NAT / Direct Server Return (DSR)       |
+-------------------------------------------------------------------+
          |
          v
+-------------------------------------------------------------------+
| Layer 7 Load Balancer (HTTP/gRPC Application Proxying)            |
| - Terminates Client TCP Connection                                |
| - Parses HTTP Method, Headers, URI Path, Cookies & gRPC Metadata  |
| - Content-Based Routing: /users -> User Svc, /orders -> Order Svc |
| - Opens separate Backend TCP Connection                           |
+-------------------------------------------------------------------+
```

### 1.1 Architectural Comparison

| Dimension | Layer 4 (Transport Layer) | Layer 7 (Application Layer) |
| :--- | :--- | :--- |
| **OSI Layer** | Layer 4 (TCP / UDP). | Layer 7 (HTTP, HTTPS, gRPC, WebSocket). |
| **Connection Handling**| Direct TCP packet pass-through or NAT. | **Terminates client TCP connection**; initiates backend TCP connection. |
| **Inspection Depth** | IP addresses & Port numbers. | Full HTTP Headers, Cookies, Request Body, gRPC methods. |
| **Routing Flexibility**| Routing based on IP / Port only. | Content-based routing (URL paths, headers, domain hostnames). |
| **Overhead & Speed** | Extremely fast, minimal CPU & RAM. | Higher CPU/RAM (Parses HTTP strings & unwraps TLS). |

---

## 2. Load Balancing Algorithms

```
                       [ Incoming Requests ]
                                 |
         +-----------------------+-----------------------+
         |                       |                       |
  Stateless Cycling      Resource Aware          Stateful / Sticky
         |                       |                       |
         v                       v                       v
  - Round Robin           - Least Connections     - Consistent Hashing
  - Weighted Round Robin  - Weighted Least Conn   - Maglev Hash Ring
```

### 2.1 Algorithm Breakdown

| Algorithm | Mechanism | Best Use Case |
| :--- | :--- | :--- |
| **Round Robin** | Cycles requests sequentially through backend pool ($1 \to 2 \to 3 \to 1$). | Homogeneous backend servers with uniform request workloads. |
| **Weighted Round Robin**| Assigns integer weights; higher weight servers receive proportionally more requests. | Heterogeneous servers with varying CPU/RAM capacities. |
| **Least Connections** | Routes new request to server with **fewest active connections**. | Long-lived connections or variable request execution times. |
| **Consistent Hashing** | Hashes request key (`User-ID` or `IP`) onto a **Hash Ring**. | Stateful caching layers; adding/removing a node reshuffles only $1/N$ keys. |
| **Maglev Hashing** | Google's lookup table hashing algorithm; guarantees uniform distribution. | Edge routers requiring zero disruption during node deployments. |

---

## 3. Active vs Passive Health Probing

To maintain high availability, load balancers continuously verify backend health:

```
[ Active Probing ]   ---> Periodic background health checks (e.g. GET /health every 5s).
                          Removes unhealthy nodes BEFORE live user traffic hits.

[ Passive Probing ]  ---> Monitors live client request failures (Circuit Breaking).
                          Temporarily ejects backend if 5 consecutive 5xx errors occur.
```

---

## 4. Staff Engineer Load Balancing Rules
1. **Use Layer 4 for Ultra-High Throughput Ingress:** Place L4 load balancers (e.g. AWS NLB, HAProxy L4, Maglev) at the outer edge to handle raw TCP packet volume, routing traffic to downstream L7 Envoy/NGINX proxies.
2. **Use Consistent Hashing for Distributed Caches:** When load-balancing requests to a cluster of caching servers, use Consistent Hashing based on cache key to maximize cache hit ratios.
3. **Combine Active and Passive Health Checks:** Configure passive health checking (e.g., ejecting node on 5 consecutive 503s) alongside active background `/health` probes to detect failed instances instantly.
