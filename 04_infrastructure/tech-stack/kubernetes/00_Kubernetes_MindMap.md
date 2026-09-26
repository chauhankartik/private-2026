# Kubernetes Architecture Taxonomy & Interactive Mind Map

Kubernetes (K8s) is a production-grade open-source container orchestration engine designed to automate deployment, scaling, and management of containerized applications across distributed clusters.

---

## 🧠 Interactive Kubernetes Architecture Mind Map

```mermaid
mindmap
  root(("Kubernetes Architecture"))
    "01 Control Plane & etcd"
      "kube-apiserver - REST API & Optimistic Concurrency"
      "etcd - Raft Consensus & MVCC Key-Value Store"
      "kube-scheduler - Filtering & Scoring Pipelines"
      "kube-controller-manager - Informer & Workqueue Loops"
    "02 Worker Node & CRI"
      "kubelet - SyncLoop & PLEG (Pod Lifecycle Event Generator)"
      "CRI - Container Runtime Interface (gRPC)"
      "containerd & runc - OCI Container Execution"
      "kube-proxy - ClusterIP Service Routing"
    "03 Pod Lifecycle & Scheduling"
      "Pod Phases - Pending Running Succeeded Failed"
      "QoS Classes - Guaranteed Burstable BestEffort"
      "Resource Requests & Limits - cgroups & oom_score_adj"
      "Affinities Taints & Tolerations"
    "04 Networking & CNI"
      "IP-per-Pod Networking Model"
      "CNI Plugins - Calico (BGP/VXLAN) vs Cilium (eBPF)"
      "Service Modes - iptables vs IPVS vs eBPF"
      "Ingress Controller & Gateway API"
    "05 Storage & CSI"
      "PV PVC & StorageClass"
      "CSI Spec - Controller & Node Services"
      "Volume Lifecycle - Attach Detach Mount Unmount"
    "06 Security & Policy"
      "Authentication - X509 OIDC ServiceAccounts"
      "RBAC - Roles & RoleBindings"
      "Admission Controllers - Mutating & Validating Webhooks"
      "NetworkPolicies - Ingress & Egress Rules"
    "07 Operators & Diagnostics"
      "CRDs - Custom Resource Definitions"
      "Operator Pattern - client-go Reconciler Loop"
      "Autoscaling - HPA VPA KEDA"
      "Troubleshooting - crictl & API Audit Logs"
```

---

## 📊 Core Component Matrix

| Component | Layer | Primary Responsibility | Critical Subsystems |
| :--- | :--- | :--- | :--- |
| **`kube-apiserver`** | Control Plane | Front door for K8s API; authenticates, authorizes, admits, and persists state | Authn/Authz chain, Admission Webhooks, `resourceVersion` locking |
| **`etcd`** | Control Plane | Consistent, highly-available key-value store for cluster metadata | Raft consensus algorithm, MVCC storage, Watch streams |
| **`kube-scheduler`** | Control Plane | Assigns unscheduled Pods to optimal worker nodes | Filtering (Predicates), Scoring (Priorities), Cache sync |
| **`kube-controller-manager`** | Control Plane | Runs core controller loops (Deployment, StatefulSet, Node) | SharedInformer, DeltaFIFO, Workqueue, Reconciler loop |
| **`kubelet`** | Worker Node | Primary node agent ensuring containers defined in PodSpecs are running | PodManager, PLEG, SyncLoop, Probe Manager, CRI client |
| **`kube-proxy`** | Worker Node | Implements Kubernetes Service abstraction on each node | `iptables` rules, IPVS ipset, or Cilium eBPF kernel bypass |
| **`containerd` / `runc`** | Worker Node | Low-level OCI container lifecycle execution and image management | OCI spec execution, `containerd-shim`, cgroup management |
