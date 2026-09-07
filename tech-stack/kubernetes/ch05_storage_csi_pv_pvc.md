# Chapter 5: Storage Architecture & Container Storage Interface (CSI)

Stateful workloads in Kubernetes require persistent storage that survives pod restarts and rescheduling across nodes.

---

## 1. Storage Abstractions: PV, PVC & StorageClass

Kubernetes decouples storage consumption from storage implementation using three primary API primitives:

```
[ Developer ]                              [ Storage Admin ]
     |                                             |
     v Writes                                      v Defines
PersistentVolumeClaim (PVC)                 StorageClass (e.g., gp3-csi)
(Requests 100Gi, RWX)                              |
     |                                             | Dynamic Provisioning
     +-------------------- Matches ----------------+
                               |
                               v
                     PersistentVolume (PV)
             (Represents actual cloud disk / block device)
```

* **PersistentVolume (PV):** Cluster-scoped storage resource representing a physical block device, cloud disk (AWS EBS, GCP PD), or network file system (NFS, Ceph).
* **PersistentVolumeClaim (PVC):** Namespace-scoped request for storage by a user, specifying size, access modes, and StorageClass.
* **StorageClass:** Defines the provisioner (CSI driver), volume parameters (e.g., IOPS, throughput), and reclaim policy (`Delete` or `Retain`).

---

## 2. Container Storage Interface (CSI) Architecture

The **CSI** is an out-of-tree gRPC specification allowing storage vendors to develop plugins without modifying core Kubernetes codebase.

```
+-------------------------------------------------------------------+
| Kubernetes Control Plane (CSI Sidecar Architecture)               |
|                                                                   |
| [ csi-provisioner ]  --> Watches PVCs, calls CreateVolume()      |
| [ csi-attacher ]     --> Watches VolumeAttachments, calls         |
|                          ControllerPublishVolume()                |
| [ csi-resizer ]      --> Watches PVC edits, calls ExpandVolume()  |
+-------------------------------------------------------------------+
                               |
                               v gRPC over UNIX Socket
+-------------------------------------------------------------------+
| Vendor CSI Driver (Controller Plugin)                             |
+-------------------------------------------------------------------+

========================== Worker Node ==============================

+-------------------------------------------------------------------+
| kubelet Node Agent                                                |
|                                                                   |
| [ node-driver-registrar ] -> Registers CSI plugin with kubelet    |
| [ kubelet Volume Manager] -> Calls NodeStageVolume() /            |
|                              NodePublishVolume() via gRPC         |
+-------------------------------------------------------------------+
                               |
                               v gRPC over UNIX Socket
+-------------------------------------------------------------------+
| Vendor CSI Driver (Node Plugin)                                   |
+-------------------------------------------------------------------+
```

---

## 3. The 4-Phase Volume Mounting Lifecycle

When a Pod with a PVC is scheduled to a worker node, Kubernetes executes a 4-phase lifecycle to make the storage accessible:

```
[ Phase 1: Provisioning ]  --> csi-provisioner calls CreateVolume()
                                Storage API creates block storage volume.
                                   |
                                   v
[ Phase 2: Attaching ]     --> csi-attacher calls ControllerPublishVolume()
                                Cloud provider attaches disk to Node VM.
                                   |
                                   v
[ Phase 3: Staging ]       --> kubelet calls NodeStageVolume()
                                Formats filesystem (ext4/xfs) & mounts device
                                to global node directory (/var/lib/kubelet/plugins/...).
                                   |
                                   v
[ Phase 4: Mounting ]      --> kubelet calls NodePublishVolume()
                                Bind-mounts global directory into Pod's 
                                container namespace (/var/lib/kubelet/pods/<pod-id>/...).
```

---

## 4. Volume Access Modes

| Access Mode | Code | Description | Typical Use Case |
| :--- | :--- | :--- | :--- |
| **ReadWriteOnce** | `RWO` | Volume can be mounted as read-write by a **single node**. | Single-node databases (Postgres, MySQL, Redis). |
| **ReadOnlyMany** | `ROX` | Volume can be mounted as read-only by **many nodes**. | Shared configuration data, static web assets. |
| **ReadWriteMany** | `RWX` | Volume can be mounted as read-write by **many nodes**. | Shared filesystems (NFS, CephFS, AWS EFS). |
| **ReadWriteOncePod**| `RWOP` | Volume can be mounted as read-write by a **single Pod** across the cluster. | Stateful workloads requiring strict exclusive access. |

---

## 5. Staff Engineer Storage Best Practices
1. **Use `WaitForFirstConsumer` Binding Mode:** In your `StorageClass`, set `volumeBindingMode: WaitForFirstConsumer`. This delays PV allocation until the Pod is scheduled, ensuring the volume is provisioned in the exact Availability Zone matching the Pod's node assignment.
2. **Handle Storage Deadlocks (`Multi-Attach error`):** When a RWO pod crashes and reschedules to another node, cloud volume detachment can stall, causing `Multi-Attach error for volume`. Tune controller timeouts and ensure `csi-attacher` sidecars are healthy.
3. **Always Set `StorageClass` Reclaim Policies:** Default to `reclaimPolicy: Delete` for ephemeral workloads, but use `Retain` for production database storage to prevent accidental data loss during PVC deletion.
