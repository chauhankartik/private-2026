# Kubernetes Architecture & Production Internals — Deep Dive Study Guide

> **Goal:** Master Kubernetes control plane mechanics, `etcd` Raft quorum, `kubelet` SyncLoop/PLEG, Container Runtime Interface (CRI), Container Network Interface (CNI / eBPF), Container Storage Interface (CSI), Pod lifecycle & QoS eviction, RBAC security, Custom Operators (`client-go`), and production diagnostic workflows for Staff Software Engineering.

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

👉 **Full Mind Map & Taxonomy:** [`00_Kubernetes_MindMap.md`](00_Kubernetes_MindMap.md)  
📚 **Recommended Books & References:** [`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)

---

## 📖 Chapter Index

1. **[Ch 1: Control Plane Architecture & `etcd` Quorum](ch01_control_plane_and_etcd.md)** — `kube-apiserver`, `resourceVersion` optimistic concurrency, `etcd` Raft consensus & watches, `kube-scheduler` filtering/scoring, `kube-controller-manager` Informer loops.
2. **[Ch 2: Worker Node Architecture, `kubelet` & CRI](ch02_worker_node_kubelet_cri.md)** — `kubelet` SyncLoop, PLEG (Pod Lifecycle Event Generator), Container Runtime Interface (CRI gRPC), `containerd` & `runc`, `kube-proxy`.
3. **[Ch 3: Pod Lifecycle, QoS Classes & Advanced Scheduling](ch03_pod_lifecycle_and_scheduling.md)** — Pod phases, CPU/Memory requests & limits, cgroup enforcement (`oom_score_adj`), QoS classes (`Guaranteed`, `Burstable`, `BestEffort`), taints, tolerations, affinities.
4. **[Ch 4: Kubernetes Networking Model, CNI & Services](ch04_networking_cni_and_services.md)** — IP-per-Pod model, CNI plugin architecture (Calico BGP/VXLAN vs Cilium eBPF), `kube-proxy` modes (`iptables` vs IPVS vs eBPF), Ingress & Gateway API.
5. **[Ch 5: Storage Architecture & Container Storage Interface (CSI)](ch05_storage_csi_pv_pvc.md)** — PV, PVC, StorageClass, dynamic provisioning, CSI gRPC endpoints (`ControllerPublishVolume`, `NodeStageVolume`, `NodePublishVolume`).
6. **[Ch 6: Security, RBAC, Admission Control & NetworkPolicies](ch06_security_rbac_networkpolicies.md)** — Authentication, RBAC (`Role`, `ClusterRole`, `RoleBinding`), Mutating/Validating Admission Webhooks, OPA Gatekeeper/Kyverno, NetworkPolicies.
7. **[Ch 7: CRDs, Operator Pattern, Autoscaling & Diagnostics](ch07_operators_crd_hpa_troubleshooting.md)** — Custom Resource Definitions (CRDs), Operator pattern (`client-go` Informers/Workqueue), HPA/VPA/KEDA, `crictl`, etcd backup/restore, CrashLoopBackOff troubleshooting.
