# Docker Architecture Mind Map & Decision Matrix

> **Purpose:** Structural decision-making for choosing Docker storage drivers, networking modes, security policies, and container image optimization strategies.

---

## 📊 Feature & Architecture Summary Matrix

| Module | Core Mechanism | Trade-off / Limitation | Production Recommended Setting |
| :--- | :--- | :--- | :--- |
| **Engine Architecture** | OCI runtime (`dockerd` $\to$ `containerd` $\to$ `runC`) | Daemon failure affects engine API | `live-restore: true` |
| **Isolation Primitive** | Linux Namespaces (PID, NET, MNT, etc.) | Shares host OS kernel | Rootless Docker / Distroless images |
| **Resource Control** | Linux cgroups v1 / v2 | OOM killer terminates container process | `--memory 512m --cpus 1.5` |
| **Storage Driver** | Overlay2 (UnionFS) | Heavy random write CoW performance penalty | Use Named Volumes for database data |
| **Networking** | Linux Bridge (`docker0`) + `iptables` NAT | NAT introduces minor latency overhead | User-defined bridge networks |
| **Image Building** | Layered filesystem build cache | Large base images contain security vulnerabilities | Multi-stage build + Distroless base |
| **Security** | Linux Capabilities + Seccomp | `PRIVILEGED` container bypasses isolation | `--cap-drop ALL --cap-add NET_BIND_SERVICE` |
