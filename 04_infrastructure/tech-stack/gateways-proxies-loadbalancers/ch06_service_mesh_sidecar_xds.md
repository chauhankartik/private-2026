# Chapter 6: Service Mesh Architecture & Dynamic Control Plane (xDS)

A **Service Mesh** manages east-west (service-to-service) network traffic across microservice architectures, delegating routing, security, and observability to co-located sidecar proxies.

---

## 1. Service Mesh Data Plane vs Control Plane

```
+-------------------------------------------------------------------+
| Control Plane (Istio / Linkerd)                                   |
| - Translates Kubernetes manifests into xDS configuration streams  |
| - Issues & rotates short-lived SPIFFE/SPIRE X.509 mTLS certs      |
+-------------------------------------------------------------------+
                               |
                               v Dynamic xDS gRPC Streams (LDS, RDS, CDS, EDS)
========================== Kubernetes Pod ===========================
+-------------------------------------------------------------------+
| Pod Boundary                                                      |
|                                                                   |
| [ Application Container ] <--- Localhost ---> [ Envoy Sidecar ]   |
| (e.g. Payment Service)                        (Data Plane Proxy)  |
+-------------------------------------------------------------------+
                                                     |
                                                     v mTLS Encryption
                                           [ Remote Envoy Sidecar ]
```

---

## 2. Envoy Dynamic Discovery APIs (The xDS Protocol)

Envoy eliminates static configuration reboots through its **xDS gRPC Discovery APIs**.

```
Control Plane (xDS Server) ─── gRPC Stream ───> Envoy Proxy (xDS Client)
```

| Discovery API | Full Name | Primary Responsibility |
| :--- | :--- | :--- |
| **`LDS`** | Listener Discovery Service | Configures network ports, IP addresses, and filter chains (e.g., listen on `0.0.0.0:15001`). |
| **`RDS`** | Route Discovery Service | Updates HTTP route tables, URI path matches, prefix rewrites, and header rules. |
| **`CDS`** | Cluster Discovery Service | Configures upstream backend clusters, load balancing algorithms, and TLS settings. |
| **`EDS`** | Endpoint Discovery Service | Updates the exact **IP addresses and ports** of healthy backend Pod instances. |

---

## 3. Mutual TLS (mTLS) & SPIFFE/SPIRE Identity

Service meshes enforce zero-trust security by encrypting all pod-to-pod communications via automatic **mTLS**.

```
[ Client Envoy Sidecar ] ──── mTLS (Mutual X.509 Verification) ────> [ Server Envoy Sidecar ]
  Identifies as:                                                      Identifies as:
  spiffe://cluster.local/ns/prod/sa/order-svc                         spiffe://cluster.local/ns/prod/sa/payment-svc
```

1. **Identity Attestation (SPIFFE):** Workloads are assigned a uniform resource identifier (`SPIFFE ID`) based on ServiceAccount and Namespace credentials.
2. **Short-Lived Certificates:** The Control Plane issues short-lived X.509 certificates (e.g. valid for 24 hours) automatically mounted into the Envoy sidecar.
3. **Transparent mTLS:** Envoy sidecars execute the TLS handshake, perform mutual certificate verification, and decrypt traffic before passing plain HTTP/gRPC to localhost containers.

---

## 4. Staff Engineer Service Mesh Guidelines
1. **Use Delta xDS (`Incremental xDS`) for Large Clusters:** Standard xDS sends full configuration snapshots on every pod change. Use Incremental xDS (`Delta xDS`) to stream only modified endpoints, reducing control plane CPU and bandwidth by 90%.
2. **Evaluate Ambient Mesh (Sidecarless):** For resource-constrained clusters, evaluate sidecarless architectures (e.g., Istio Ambient Mesh using ztunnel at L4 and Waypoint proxies at L7) to eliminate the 50MB RAM per-pod sidecar overhead.
3. **Set Strict mTLS Modes:** Configure `peerAuthentication` to `STRICT` mode to reject unencrypted plaintext traffic across cluster workloads.
