# Chapter 1: Control Plane Architecture, `etcd` Quorum & Controller Loops

The Kubernetes Control Plane acts as the brain of the cluster, maintaining the desired state of all workloads, managing node membership, and handling cluster-wide scheduling.

---

## 1. `kube-apiserver` Architecture & Request Pipeline

The `kube-apiserver` is the central, stateless front-door for all administrative and operational requests within a Kubernetes cluster.

```
Client (kubectl / Controller)
            |
            v
+-------------------------------------------------------------------+
| kube-apiserver Pipeline                                           |
|                                                                   |
| [1. Authentication]  --> X.509 Certs, ServiceAccount JWT, OIDC    |
|         |                                                         |
|         v                                                         |
| [2. Authorization]   --> RBAC (Role / ClusterRole evaluation)     |
|         |                                                         |
|         v                                                         |
| [3. Mutating Webhooks] -> Modifies object (e.g. injects sidecars) |
|         |                                                         |
|         v                                                         |
| [4. Schema Validation] -> Validates OpenAPI schema & field types  |
|         |                                                         |
|         v                                                         |
| [5. Validating Webhooks]-> Rejects invalid states (OPA/Kyverno)   |
+-------------------------------------------------------------------+
            |
            v  Atomic Compare-And-Swap (CAS)
+-------------------------------------------------------------------+
| etcd Storage Engine                                               |
+-------------------------------------------------------------------+
```

### 1.1 Optimistic Concurrency Control (`resourceVersion`)
Kubernetes avoids pessimistic locking on cluster metadata. Instead, every object stored in `etcd` contains a `metadata.resourceVersion` integer matching etcd's current 64-bit modification revision.
* When a controller sends a update (`PUT`), `kube-apiserver` verifies that the incoming `resourceVersion` matches the current revision in `etcd`.
* If another process modified the object in the interim, the update returns `HTTP 409 Conflict`, forcing the caller to re-fetch, re-apply changes, and retry.

---

## 2. `etcd` Key-Value Store & Raft Consensus

`etcd` is a strongly-consistent, distributed key-value store implementing the **Raft Consensus Algorithm**.

### 2.1 Raft Quorum & Leader Election
* **Quorum Formula:** $Q = \lfloor N/2 \rfloor + 1$ (A 3-node cluster tolerates 1 node failure; a 5-node cluster tolerates 2 node failures).
* **Write Path:** All cluster writes pass through the Raft Leader, which appends the operation to its write-ahead log and broadcasts `AppendEntries` RPCs to followers. A write commits once a quorum of nodes acknowledges log persistence.

### 2.2 MVCC & Watch Mechanism
* **MVCC Storage:** `etcd` never overwrites key-value pairs in-place. Every mutation increments a global 64-bit `revision` counter, storing a new key revision.
* **Compaction:** Historic key revisions must be periodically purged (`etcdctl compact`) to prevent database file exhaustion (`db-size` default 2GB / 8GB limit).
* **Watch API:** Controllers subscribe to key prefixes (e.g., `/registry/pods/`) using gRPC HTTP/2 multiplexed streams. `etcd` pushes mutation events in real-time, eliminating expensive polling.

---

## 3. `kube-scheduler` Execution Pipeline

The `kube-scheduler` watches for newly created Pods that lack a `.spec.nodeName` and assigns them to the most suitable worker node.

```
Unscheduled Pod ---> [ Scheduling Queue (PriorityQueue) ]
                                   |
                                   v
+-------------------------------------------------------------------+
| kube-scheduler Pipeline (Per Pod)                                 |
|                                                                   |
| 1. Filtering Phase (Predicates)                                   |
|    - NodeResourcesFit (CPU/Memory capacity check)                 |
|    - NodeName / NodeAffinity evaluation                           |
|    - PodToleratesNodeTaints                                       |
|    => Produces Candidate Node Set                                 |
|                                                                   |
| 2. Scoring Phase (Priorities)                                     |
|    - NodeResourcesBalancedAllocation (Spreads CPU/Mem load)       |
|    - ImageLocality (Prefers nodes with cached container images)   |
|    => Ranks Candidate Nodes (0 to 100 points)                     |
|                                                                   |
| 3. Binding Phase                                                  |
|    - Sends Binding object to kube-apiserver (sets nodeName)       |
+-------------------------------------------------------------------+
```

---

## 4. `kube-controller-manager` & Controller Architecture

The `kube-controller-manager` runs a collection of decoupled, asynchronous control loops (e.g., DeploymentController, StatefulSetController, NodeLifecycleController).

### 4.1 Informer & Workqueue Pattern (`client-go`)
To avoid overloading `kube-apiserver` with continuous GET/LIST requests, every controller utilizes the **SharedInformer** pattern:

```
kube-apiserver
    |
    | (List / Watch Stream)
    v
[ Reflector ]  ===> Writes to ===> [ DeltaFIFO Queue ]
                                         |
                                         v
                                [ SharedInformer ]
                                 /              \
                                /                \
         Updates Local Cache   /                  \ Triggers Event Handlers
                              v                    v
                       [ Indexer Cache ]    [ Workqueue ]
                                                   |
                                                   v
                                          [ Reconciler Loop ]
```

1. **Reflector:** Establishes a `ListAndWatch` connection to `kube-apiserver` for a specific resource type and streams delta updates into a `DeltaFIFO` queue.
2. **SharedInformer:** Consumes `DeltaFIFO`, updates the thread-safe in-memory `Indexer` cache, and dispatches event notifications (`AddFunc`, `UpdateFunc`, `DeleteFunc`).
3. **Workqueue:** Stores object keys (`<namespace>/<name>`) requiring reconciliation, supporting rate-limiting and exponential backoff retries.
4. **Reconciler Loop:** Worker threads pop keys from the Workqueue, query the local `Indexer` cache, compare Current State against Desired State, and execute mutations via `kube-apiserver`.

---

## 5. Staff Engineer Control Plane Tuning SLA
1. **`etcd` IOPS SLA:** `etcd` requires low-latency disk writes (Fsync < 10ms). Always host `etcd` on dedicated NVMe SSD drives. Disk latency spikes directly trigger Raft leader elections and cluster outages.
2. **API Server Rate-Limiting (APF):** Configure **API Priority and Fairness (APF)** in `kube-apiserver` to isolate management traffic from background controller streams, preventing control plane starvation during traffic spikes.
3. **Informer Resync Intervals:** Avoid setting small `resyncPeriod` values in custom Informers. Periodic resyncs trigger full worker reconciliations, increasing CPU and memory consumption.
