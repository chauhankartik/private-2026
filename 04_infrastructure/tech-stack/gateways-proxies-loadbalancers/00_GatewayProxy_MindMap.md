# API Gateways, Reverse Proxies & Load Balancers Architecture Taxonomy & Interactive Mind Map

API Gateways, Reverse Proxies, and Load Balancers form the traffic management backbone of modern distributed systems, handling incoming client traffic, protocol translation, load distribution, security enforcement, and service discovery.

---

## 🧠 Interactive Gateway & Proxy Architecture Mind Map

```mermaid
mindmap
  root(("Gateways & Proxies"))
    "01 Process & I/O Architecture"
      "Master Worker Event Driven Architecture"
      "Linux epoll & BSD kqueue Multiplexing"
      "Non-Blocking Sockets & Event Loops"
      "SO REUSEPORT Kernel Load Balancing"
    "02 L4 vs L7 Load Balancing"
      "Layer 4 TCP UDP Pass-Through Proxying"
      "Layer 7 HTTP gRPC Application Proxying"
      "Algorithms - Round Robin & Least Connections"
      "Consistent Hashing & Maglev Hash Ring"
    "03 SSL TLS & Protocol Engine"
      "TLS Termination & Session Resumption"
      "ALPN Protocol Negotiation"
      "HTTP2 Multiplexing & Binary Framing"
      "gRPC Frame Level Load Balancing"
    "04 API Gateway Core Features"
      "Dynamic Path & Header Routing"
      "Rate Limiting - Token & Leaky Bucket"
      "Circuit Breaking & Timeout Budgets"
      "JWT Authentication Offloading"
    "05 Proxy Implementation Comparison"
      "NGINX - C Master Worker Model"
      "HAProxy - Ultra Low Latency Engine"
      "Envoy - C Plus Plus Thread-Per-Core Proxy"
      "Kong - Lua OpenResty API Gateway"
    "06 Service Mesh & xDS"
      "Sidecar Pattern - Istio Envoy"
      "Envoy xDS Dynamic Discovery APIs"
      "mTLS Identity & Certificate Rotation"
      "SPIFFE SPIRE Identity Attestation"
    "07 Performance & Operations"
      "Linux Socket Tuning - SOMAXCONN"
      "File Descriptor Limits - ulimit"
      "Zero Downtime Hot Reloads"
      "Metrics - Prometheus Admin Endpoint"
```

---

## 📊 Core Component Matrix

| Proxy Technology | Architectural Model | Primary Use Case | Key Strength |
| :--- | :--- | :--- | :--- |
| **NGINX** | C Master/Worker Event-Loop Process Model | Web Server, Reverse Proxy, Static File Serving | Proven stability, low RAM footprint, vast community |
| **HAProxy** | C Single-Process Event-Driven Engine | High-Throughput Layer 4 / Layer 7 Load Balancer | Sub-millisecond latency, extreme L4 TCP throughput |
| **Envoy Proxy** | C++ Thread-per-Core Asynchronous Engine | Service Mesh Sidecar, Modern API Gateway | Native gRPC/HTTP2, dynamic xDS API control plane |
| **Kong Gateway** | Lua / OpenResty wrapper over NGINX | Modular Enterprise API Gateway | Rich plugin ecosystem, RESTful admin configuration API |
| **Traefik** | Go Event-Driven Microservice Proxy | Cloud-Native Kubernetes Ingress Controller | Automatic dynamic service discovery (K8s, Docker) |
