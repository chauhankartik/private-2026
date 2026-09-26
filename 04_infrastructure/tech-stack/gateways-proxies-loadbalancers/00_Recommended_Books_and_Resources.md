# API Gateways, Reverse Proxies & Load Balancers Recommended Reading List

A curated list of authoritative books, official architecture manuals, and open-source codebase pointers for mastering proxy architectures, kernel socket tuning, load balancing algorithms, and Envoy xDS control planes.

---

## 📚 Recommended Books

1. **_Envoy in Action_** — Christian Posta & Datawire Team (Manning Publications)
   * **Why Read It:** The definitive guide to Envoy Proxy architecture. Explains listeners, clusters, routes, filters, dynamic configuration via xDS APIs, gRPC load balancing, and Istio Service Mesh integration.
   * **Key Focus:** Modern C++ edge & sidecar proxy engineering.

2. **_NGINX Cookbook: Advanced Recipes for High Performance Load Balancing_** — Derek DeJonghe (O'Reilly)
   * **Why Read It:** Comprehensive practical guide to NGINX reverse proxying, Layer 7 load balancing, SSL/TLS termination, HTTP/2 Server Push, rate limiting, and OpenResty Lua scripting.

3. **_System Design Interview – An Insider's Guide (Volume 1 & 2)_** — Alex Xu
   * **Why Read It:** Excellent chapters on Rate Limiters, API Gateway Architecture, Distributed Unique ID Generators, and Consistent Hashing Load Balancers.

4. **_The Linux Programming Interface_** — Michael Kerrisk (No Starch Press)
   * **Why Read It:** Essential background on non-blocking I/O, `epoll(7)`, `kqueue(2)`, socket options (`SO_REUSEPORT`, `TCP_NODELAY`), and process execution models underlying modern proxies.

---

## 📄 Official Architecture Documentation & Manuals

1. **[Envoy Proxy Architecture & Configuration Manual](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/arch_overview)**
   * **Topics:** Threading model, Listeners, Network/HTTP Filters, Cluster Manager, xDS Dynamic Discovery Services (LDS, RDS, CDS, EDS), Memory management.
2. **[HAProxy Architecture Guide](https://www.haproxy.org/download/2.8/doc/architecture.txt)**
   * **Topics:** Single-process event loop, Layer 4 vs Layer 7 proxying, sticky tables, runtime socket administration, health checking strategies.
3. **[NGINX Architecture Specification](https://www.nginx.com/resources/wiki/start/topics/architecture/)**
   * **Topics:** Master-worker process design, non-blocking I/O event loop, connection pooling, shared memory slabs (`zone`).

---

## 💻 Source Code References (C / C++ Repositories)

Explore core engine components in open-source proxy repositories:

* **`nginx/nginx` (`src/core/ngx_epoll_module.c`):** NGINX Linux `epoll` event loop module implementation.
* **`haproxy/haproxy` (`src/ev_epoll.c` & `src/stream.c`):** HAProxy low-latency event loop and stream processing engine.
* **`envoyproxy/envoy` (`source/common/event/dispatcher_impl.cc`):** Envoy libevent dispatcher handling thread-per-core event loops.
* **`Kong/kong` (`kong/plugins/`):** OpenResty Lua plugin architecture for Kong API Gateway.
