# Kubernetes Recommended Reading List & Technical References

A curated list of authoritative books, Kubernetes Enhancement Proposals (KEPs), official documentation, and open-source codebase pointers for mastering Kubernetes control plane, runtime, networking, and custom operators.

---

## 📚 Recommended Books

1. **_Programming Kubernetes: Developing Cloud-Native Applications_** — Michael Hausenblas & Stefan Schimanski (O'Reilly)
   * **Why Read It:** The definitive deep-dive into the Kubernetes API, `client-go` architecture, Informers, Workqueues, Custom Resource Definitions (CRDs), and building production-grade Custom Controllers & Operators.
   * **Key Focus:** Operator Pattern and Kubernetes API extensibility.

2. **_Kubernetes Up & Running (3rd Edition)_** — Brendan Burns, Joe Beda, Kelsey Hightower, & Lachlan Evenson (O'Reilly)
   * **Why Read It:** Written by the original creators of Kubernetes. Covers core primitives, pod lifecycle, storage abstractions, service discovery, and zero-downtime rolling updates.

3. **_Kubernetes Patterns: Reusable Elements for Designing Cloud-Native Applications_** — Bilgin Ibryam & Roland Huß (O'Reilly)
   * **Why Read It:** Catalogs foundational cloud-native patterns: Sidecar, Init Container, Ambassador, Adapter, Controller, Operator, and Structural Patterns.

4. **_Learning eBPF: Programming the Linux Kernel for Observability, Networking, and Security_** — Liz Rice (O'Reilly)
   * **Why Read It:** Crucial for understanding modern CNI plugins (Cilium), high-performance `kube-proxy` replacements, and kernel-level network security.

---

## 📄 Key Specifications & KEPs (Kubernetes Enhancement Proposals)

1. **[KEP-2591: Gateway API Specification](https://github.com/kubernetes-sigs/gateway-api)**
   * **Topics:** Modern successor to Ingress. Role-oriented routing API (`GatewayClass`, `Gateway`, `HTTPRoute`, `GRPCRoute`).
2. **[Container Runtime Interface (CRI) Specification](https://github.com/kubernetes/cri-api)**
   * **Topics:** gRPC protocol contracts (`RuntimeService` and `ImageService`) connecting `kubelet` with container runtimes (`containerd`, `CRI-O`).
3. **[Container Storage Interface (CSI) Specification](https://github.com/container-storage-interface/spec)**
   * **Topics:** Standardized storage interface for block/file storage volume provisioning, attachment, staging, and mounting.

---

## 💻 Source Code References (Go Repository)

Explore core components in the [Kubernetes GitHub Repository](https://github.com/kubernetes/kubernetes):

* **`pkg/kubelet/kubelet.go` & `pkg/kubelet/pleg/`:** `kubelet` SyncLoop and Pod Lifecycle Event Generator (PLEG).
* **`pkg/scheduler/`:** `kube-scheduler` framework (`framework.go`, filtering & scoring plugins).
* **`pkg/controller/`:** Core controller implementations (`deployment/`, `statefulset/`, `nodeipam/`).
* **`staging/src/k8s.io/client-go/`:** `tools/cache/` (SharedInformer, DeltaFIFO, Indexer) and `util/workqueue/` (RateLimitingQueue).
