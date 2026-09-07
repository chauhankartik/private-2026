# Chapter 1: Docker Engine Architecture & Linux Kernel Primitives — Deep Dive Notes

> **Core Theme:** A container is NOT a virtual machine! A container is simply a standard Linux process isolated by **Namespaces** and resource-restricted by **Control Groups (cgroups)**.

---

## 1. Containers vs. Virtual Machines

```
          VIRTUAL MACHINES                                CONTAINERS
┌──────────────────────────────────┐        ┌──────────────────────────────────┐
│ App A    │ App B    │ App C      │        │ App A    │ App B    │ App C      │
├──────────┼──────────┼────────────┤        ├──────────┼──────────┼────────────┤
│ Guest OS │ Guest OS │ Guest OS   │        │ Bins/Libs│ Bins/Libs│ Bins/Libs  │
├──────────┴──────────┴────────────┤        ├──────────┴──────────┴────────────┤
│ Hypervisor (KVM / VMware)        │        │ Docker Engine (containerd / runC)│
├──────────────────────────────────┤        ├──────────────────────────────────┤
│ Host Infrastructure & OS Kernel  │        │ Host Linux Kernel                │
└──────────────────────────────────┘        └──────────────────────────────────┘
```

- **Virtual Machines:** Virtualize hardware. Each VM runs a full guest operating system (Slow startup 30s–60s, heavy RAM usage 1GB+).
- **Containers:** Virtualize operating system. Containers share the single host Linux kernel (Fast startup <1s, lightweight MB footprint).

---

## 2. OCI Engine Architecture (`dockerd` $\to$ `containerd` $\to$ `runC`)

Modern Docker decouples high-level management from low-level container execution according to the **OCI (Open Container Initiative)** specification.

```
[ Docker CLI / API ]
         │
         ▼
[ dockerd Daemon ] ──► Manages Images, Volumes, Networks, and REST API
         │
         ▼
[ containerd ] ──────► High-Level Runtime (Image pull, storage layer prep)
         │
         ▼
[ containerd-shim ] ──► Prevents container death if containerd restarts
         │
         ▼
[ runC ] ────────────► Low-Level OCI Runtime (Spawns process via Kernel syscalls)
```

1. **`dockerd`:** Top-level daemon providing HTTP API, volume management, and image builds.
2. **`containerd`:** CNCF project handling image distribution, storage, and container lifecycle.
3. **`runC`:** Lightweight CLI tool that invokes Linux kernel system calls (`clone()`, `unshare()`) to spawn isolated container processes.
4. **`containerd-shim`:** Serves as parent process of the container process to keep file descriptors (`stdin`/`stdout`) open during engine upgrades (`live-restore`).

---

## 3. Linux Kernel Namespaces (Isolation Primitives)

Namespaces restrict **what a containerized process can SEE**.

| Namespace | Syscall Flag | What it Isolates |
| :--- | :--- | :--- |
| **PID** | `CLONE_NEWPID` | Process IDs (Container process sees itself as PID 1 inside container). |
| **NET** | `CLONE_NEWNET` | Network devices, IP addresses, port numbers, routing tables, `/proc/net`. |
| **MNT** | `CLONE_NEWMNT` | Mount points and filesystem root (`/`). |
| **IPC** | `CLONE_NEWIPC` | System V IPC objects and POSIX message queues. |
| **UTS** | `CLONE_NEWUTS` | Hostname and NIS domain name. |
| **USER**| `CLONE_NEWUSER`| User and Group ID mappings (Container root UID 0 maps to unprivileged UID 10001 on host). |

---

## 4. Control Groups (cgroups v1 / v2) (Resource Controls)

While Namespaces dictate what a process can *see*, **cgroups** dictate how much host resources a process can **USE**.

### 1. Memory Control (`memory.limit_in_bytes`):
- Enforces maximum RAM boundary (`docker run -m 512m`).
- **OOM Killer:** If container memory usage exceeds limit + swap, the Linux kernel Out-Of-Memory (OOM) Killer terminates the container process (`Exit Code 137`).

### 2. CPU Allocation (`cpu.cfs_quota_us` & `cpu.cfs_period_us`):
- Uses Completely Fair Scheduler (CFS).
- Setting `--cpus 1.5` configures `quota = 150000us` and `period = 100000us` (Processes can execute for 150ms of CPU time per 100ms wall-clock period).
