# Chapter 7: Custom Operators, Autoscaling & Low-Level Diagnostics

Extending Kubernetes via the Operator Pattern and troubleshooting production cluster issues are core competencies for Staff Engineers.

---

## 1. Custom Resource Definitions (CRDs) & Operator Pattern

An **Operator** is an application-specific controller that extends the Kubernetes API to automate the lifecycle of complex stateful applications (e.g., PostgreSQL clusters, Kafka brokers).

```
+-------------------------------------------------------------------+
| Custom Resource Definition (CRD)                                  |
| Defines schema for: postgresqls.database.example.com              |
+-------------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------------+
| Custom Operator Controller Loop                                   |
|                                                                   |
| [ SharedInformer ]                                                |
|       | (List / Watch CRD events)                                 |
|       v                                                           |
| [ Workqueue ]                                                     |
|       |                                                           |
|       v                                                           |
| [ Reconciler Loop ]                                               |
|       ├── 1. Reads Custom Resource Spec (desired state)           |
|       ├── 2. Inspects cluster resources (current state)           |
|       ├── 3. Executes actions (creates StatefulSets, PVs, Backups)|
|       └── 4. Updates Custom Resource Status subresource           |
+-------------------------------------------------------------------+
```

---

## 2. Kubernetes Autoscaling Architecture

```
+------------------+     Queries     +--------------------+     Adjusts     +------------------+
| metrics-server   | <-------------> | HPA Controller     | ------------->  | Deployment       |
| / KEDA Scaler    |                 | (Horizontal)       |  Replica Count  | (Replicas: N)    |
+------------------+                 +--------------------+                 +------------------+

+------------------+                 +--------------------+     Adjusts     +------------------+
| VPA Recommender  | ------------->  | VPA Controller     | ------------->  | PodSpec          |
| (Metrics Analysis)                 | (Vertical)         |  Requests/Limits| (CPU/Mem Requests|
+------------------+                 +--------------------+                 +------------------+
```

### 2.1 HPA vs VPA vs KEDA

| Autoscaler | Target Dimension | Metric Source | Primary Use Case |
| :--- | :--- | :--- | :--- |
| **HPA** (Horizontal Pod Autoscaler) | Scales **Replica Count** (Horizontal out) | Resource metrics (CPU/Mem) or Prometheus custom metrics | Stateless web apps & API servers |
| **VPA** (Vertical Pod Autoscaler) | Scales **CPU/Memory Requests** (Vertical up/down) | Historical resource usage trends | Single-instance stateful apps or workloads with un-predictable sizing |
| **KEDA** (Event-Driven Autoscaler) | Scales **Replicas 0 $\leftrightarrow$ N** | External event sources (Kafka lag, RabbitMQ depth, SQS) | Event-driven microservices & worker consumers |

---

## 3. Master Diagnostic & Troubleshooting Playbook

### 3.1 Common Pod Failure Modes & Root Causes

```
+-------------------------------------------------------------------------------------+
| Diagnostic Flowchart                                                                |
|                                                                                     |
| Is Pod in Pending?                                                                  |
|   ├── YES -> Run `kubectl describe pod`. Check Events for scheduling failures       |
|   │          (Insufficient cpu/memory, Taint/Toleration mismatch, PVC unbound).    |
|                                                                                     |
| Is Pod in CrashLoopBackOff?                                                         |
|   ├── YES -> Run `kubectl logs <pod-name> --previous`.                              |
|   │          Inspect application crash log / stacktrace prior to restart.           |
|                                                                                     |
| Is Pod in OOMKilled (Exit Code 137)?                                                |
|   ├── YES -> Application container memory exceeded cgroup `limits.memory`.          |
|              Increase memory limits or fix application heap memory leak.            |
|                                                                                     |
| Is Pod in ImagePullBackOff / ErrImagePull?                                          |
|   ├── YES -> Image tag invalid, registry auth failed, or network partition to repo. |
+-------------------------------------------------------------------------------------+
```

### 3.2 Low-Level Node Diagnostics using `crictl`
When `kubectl` commands stall due to `kubelet` or API server issues, SSH directly onto the node and run `crictl`:

```bash
# List all active Pod sandboxes on node
crictl pods

# List running containers
crictl ps -a

# Inspect raw container execution logs bypassing kubelet
crictl logs <container-id>

# Inspect local CRI runtime status
crictl info
```

### 3.3 `etcd` Disaster Recovery
To recover a corrupted control plane, restore `etcd` from a previous snapshot:

```bash
# Save snapshot
ETCDCTL_API=3 etcdctl snapshot save /tmp/etcd-backup.db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key

# Restore snapshot to data directory
ETCDCTL_API=3 etcdctl snapshot restore /tmp/etcd-backup.db \
  --data-dir=/var/lib/etcd-restored
```

---

## 4. Staff Engineer Production SLA Framework
1. **Never Combine HPA & VPA on CPU/Memory:** Using HPA and VPA simultaneously on the same resource metrics creates conflicting control loop updates. Use HPA for CPU/Memory and VPA in `Off` mode for recommendations only.
2. **Implement KEDA for Event Consumers:** Scale worker consumer deployments to `0` when Kafka topic lag is zero to optimize cloud infrastructure costs.
3. **Automate `etcd` Snapshots:** Schedule hourly automated `etcd` snapshots with offsite S3 retention. Verify restore procedures quarterly.
