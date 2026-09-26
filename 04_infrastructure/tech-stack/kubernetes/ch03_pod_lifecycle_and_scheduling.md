# Chapter 3: Pod Lifecycle, QoS Classes & Advanced Scheduling

A **Pod** is the smallest deployable atomic execution unit in Kubernetes, wrapping one or more co-located application containers sharing a network namespace (`IP`) and IPC.

---

## 1. Pod Lifecycle & Initialization Sequence

```
[ Pod Creation ]
       |
       v
[ Pending Phase ] ---> Image Pulling & Network Sandbox Creation
       |
       v
[ Init Containers ] (Executed Sequentially to Completion)
  ├── InitContainer 1 (Exit 0)
  └── InitContainer 2 (Exit 0)
       |
       v
[ Native Sidecars ] (restartPolicy: Always, Starts First, Stays Alive)
       |
       v
[ Main Containers ] (Started Concurrently)
       |
       +---> [ startupProbe ]   (Blocks liveness/readiness probes until success)
       +---> [ readinessProbe ] (Toggles Pod IP inclusion in Service Endpoints)
       +---> [ livenessProbe ]  (Triggers CRI container restart on failure)
       |
       v
[ Running Phase ] ---> [ Succeeded Phase ] (Exit 0) OR [ Failed Phase ] (Exit Non-Zero)
```

### 1.1 Pod Phases
* **`Pending`:** Accepted by API Server, but waiting for scheduler assignment or container image downloads.
* **`Running`:** Bound to a node; all init containers have completed, and at least one main container is running.
* **`Succeeded`:** All containers in the Pod terminated successfully with exit code `0`.
* **`Failed`:** All containers terminated, with at least one container exiting with a non-zero exit code.
* **`Unknown`:** `kubelet` state on node cannot be communicated to control plane (e.g., node network partition).

---

## 2. Resource Requests, Limits & Kernel Enforcement

Kubernetes defines compute resource requirements per container in two specifications: **Requests** (used for scheduling) and **Limits** (enforced by Linux cgroups).

```yaml
resources:
  requests:
    cpu: "500m"      # 0.5 CPU core
    memory: "512Mi"  # 512 Megabytes
  limits:
    cpu: "1000m"     # 1 CPU core
    memory: "1Gi"    # 1 Gigabyte
```

### 2.1 CPU Enforcement (CFS Throttling)
* **CPU Requests:** Converted to cgroup CPU shares (`cpu.weight` in cgroups v2). Determines relative CPU time during node contention.
* **CPU Limits:** Enforced via Completely Fair Scheduler (CFS) quota (`cpu.cfs_quota_us` / `cpu.cfs_period_us`). Exceeding CPU limit does **NOT** kill the pod; it causes **CPU Throttling**, slowing execution down.

### 2.2 Memory Enforcement (OOMKilled)
* **Memory Limits:** Enforced via cgroups hard memory limits (`memory.max`).
* **Breach Behavior:** If container memory usage exceeds its limit, the Linux kernel Out-Of-Memory (OOM) Killer fires a `SIGKILL` (`exit code 137`), transitioning the container to **OOMKilled** state.

---

## 3. Quality of Service (QoS) Classes & Eviction

Kubernetes assigns every Pod a **QoS Class** based on its request/limit configuration. Under node memory pressure, `kubelet` uses QoS classes and kernel `oom_score_adj` to evict pods.

```
OOM Eviction Priority (First to be killed -> Last to be killed)

   [ BestEffort ]      ===> oom_score_adj = 1000  (First target during OOM)
         |
         v
   [ Burstable ]       ===> oom_score_adj = 2 to 999
         |
         v
   [ Guaranteed ]      ===> oom_score_adj = -997  (Last target during OOM)
```

| QoS Class | Qualification Requirement | `oom_score_adj` Value | Eviction Priority |
| :--- | :--- | :--- | :--- |
| **`Guaranteed`** | Every container has CPU & Memory requests **equal to limits**. | `-997` (System reserved) | Protected; killed only if node OS runs out of memory. |
| **`Burstable`** | Requests < Limits, or at least one container has requests defined. | `1000 - (memory_request / node_mem * 1000)` | Medium; evicted when node experiences memory pressure. |
| **`BestEffort`** | No requests or limits specified for any container. | `1000` | High; first pods killed during node memory exhaustion. |

---

## 4. Advanced Scheduling Constructs

### 4.1 Node Affinity & Anti-Affinity
Constrains which nodes a pod can be scheduled on based on node labels:
* `requiredDuringSchedulingIgnoredDuringExecution`: Hard constraint (must match).
* `preferredDuringSchedulingIgnoredDuringExecution`: Soft constraint (weighted preference).

### 4.2 Taints & Tolerations
Allows nodes to repel pods unless the pod explicitly tolerates the taint.
* **Taint Syntax:** `key=value:Effect`
* **Taint Effects:**
  * `NoSchedule`: Prevents new non-tolerating pods from scheduling.
  * `PreferNoSchedule`: Avoids scheduling if alternatives exist.
  * `NoExecute`: Evicts existing running pods that lack matching tolerations.

### 4.3 Topology Spread Constraints & PodDisruptionBudgets (PDB)
* **Topology Spread Constraints:** Ensures pods are evenly distributed across availability zones, regions, or nodes (`maxSkew: 1`).
* **PodDisruptionBudget (PDB):** Defines minimum available instances (`minAvailable: 80%`) during voluntary disruptions (e.g., `kubectl drain` during node upgrades).

---

## 5. Staff Engineer Pod Tuning SLA
1. **Never Omit Memory Requests/Limits:** Pods without memory limits default to `BestEffort`, risking random OOM termination during cluster memory pressure.
2. **Beware CPU Limit Throttling Spikes:** Setting overly aggressive CPU limits on multi-threaded runtimes (e.g., Java JVM / Go GC) causes severe latency spikes due to CFS quota throttling. Consider using CPU requests without hard limits if latency SLA is strict.
3. **Configure StartupProbes for Heavy Apps:** Use `startupProbe` with long failure thresholds for slow-booting applications to prevent `livenessProbe` from prematurely killing pods during startup.
