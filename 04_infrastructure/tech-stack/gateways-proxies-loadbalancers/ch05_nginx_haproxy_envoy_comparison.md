# Chapter 5: NGINX vs HAProxy vs Envoy vs Kong Deep-Dive Comparison

Choosing the correct proxy engine requires evaluating process architecture, memory footprint, configuration management models, and protocol support.

---

## 1. Architectural Comparison Matrix

| Architectural Metric | NGINX | HAProxy | Envoy Proxy | Kong Gateway |
| :--- | :--- | :--- | :--- | :--- |
| **Language & Engine** | C (Master/Worker) | C (Single-Process / Multi-Threaded) | **C++17 (Thread-per-Core Event Loop)** | **Lua / OpenResty (Built on NGINX)** |
| **Primary Use Case** | Web Server & Reverse Proxy | Ultra-Low Latency L4/L7 Load Balancer | **Service Mesh Sidecar & Cloud API Gateway** | Enterprise API Gateway |
| **Config Model** | Static text files (`nginx -s reload`) | Static text files + Runtime Socket | **Dynamic Control Plane (xDS gRPC APIs)** | REST API / PostgreSQL / Declarative YAML |
| **gRPC / HTTP2** | Good (gRPC proxying via `grpc_pass`) | Good (H2 support) | **Native First-Class (Frame-level routing)** | Good (via NGINX core) |
| **Observability** | Basic (`stub_status`) | High (`/haproxy?stats` & Stats Socket) | **Unmatched (Prometheus stats, OTel traces)**| High (Plugin-based metrics) |
| **Extensibility** | C modules / Lua (OpenResty) | Lua scripts | **C++ filters / Wasm (WebAssembly)** | **Lua Plugins** |

---

## 2. Process & Threading Models

```
[ NGINX Master-Worker ]
Master Process ───> Spawns N Worker Processes (1 per CPU Core)
                    Workers handle non-blocking event loops independently.

[ HAProxy Single-Process ]
Single Event Loop ──> Process handles epoll events for all connections.
                      Ultra-low CPU overhead & microsecond latency.

[ Envoy Thread-per-Core ]
Main Thread ───────> Spawns N Worker Threads (1 per CPU Core)
                    Each thread runs a non-blocking EventLoop.
                    SO_REUSEPORT balances sockets across worker threads.
```

---

## 3. Technology Selection Guide

### 3.1 Choose NGINX if:
* You need a unified Web Server + Reverse Proxy for serving static assets and proxying HTTP microservices.
* Static file-based configuration fits your deployment pipelines.

### 3.2 Choose HAProxy if:
* You require maximum Layer 4 (TCP) or Layer 7 (HTTP) load balancing throughput with sub-millisecond latency.
* You need advanced TCP health checks and runtime socket administration.

### 3.3 Choose Envoy Proxy if:
* You are building a **Cloud-Native Kubernetes / Service Mesh** architecture (e.g. Istio) requiring dynamic configuration updates without process reboots.
* Your architecture relies heavily on **gRPC, HTTP/2, and OpenTelemetry distributed tracing**.

### 3.4 Choose Kong Gateway if:
* You need an enterprise API Gateway with out-of-the-box plugins for Rate Limiting, OAuth2, Authentication, and Developer Portals.

---

## 4. Staff Engineer Selection SLA
1. **Standardize on Envoy for Cloud-Native Edge & Mesh:** Deploy Envoy for modern microservices to leverage dynamic xDS APIs, native gRPC frame balancing, and built-in OpenTelemetry tracing.
2. **Use HAProxy for Extreme TCP Throughput:** Deploy HAProxy at Layer 4 when forwarding millions of raw TCP connections per second to backend clusters.
3. **Avoid Over-Engineering with Kong Unless Plugins are Required:** If you only need simple path routing and SSL termination, standard NGINX or Envoy is significantly lighter and faster than running the full Kong/OpenResty/Postgres stack.
