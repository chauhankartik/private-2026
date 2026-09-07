# Docker Recommended Reading List & Reference Manuals

A curated list of books, Linux kernel man pages, specifications, and open-source codebase pointers for mastering containerization, low-level isolation primitives, and runtime architectures.

---

## 📚 Recommended Books

1. **_Docker Deep Dive_** — Nigel Poulton
   * **Why Read It:** The definitive guide to Docker architecture, `runc`, `containerd`, image layering, storage drivers, and Docker networking mechanics.
   * **Key Focus:** Comprehensive operational and conceptual understanding of modern container runtimes.

2. **_Container Security: Fundamental Technology Concepts that Protect Containerized Applications_** — Liz Rice (O'Reilly)
   * **Why Read It:** Essential reading for Staff Engineers. Deeply explains Linux process isolation primitives: Namespaces, Cgroups, Seccomp profiles, Capabilities, AppArmor, and rootless containers.
   * **Key Focus:** Security boundaries and low-level Linux isolation constructs.

3. **_The Linux Programming Interface_** — Michael Kerrisk (No Starch Press)
   * **Why Read It:** The bible of Linux system programming. Chapters on `clone()`, `unshare()`, `setns()`, `pivot_root()`, and `cgroups` explain exactly what happens under the hood when a container starts.
   * **Key Focus:** System call primitives underlying Docker.

---

## 📄 Specifications & Standards

1. **[Open Container Initiative (OCI) Runtime Specification](https://github.com/opencontainers/runtime-spec)**
   * **Topics:** `config.json` schema, container lifecycle operations (`create`, `start`, `kill`, `delete`), state transitions.
2. **[OCI Image Format Specification](https://github.com/opencontainers/image-spec)**
   * **Topics:** Content Addressable Identifiers (SHA256 digests), Image Index, Manifest, Configuration JSON, Tarball Rootfs Layers.
3. **[Linux Kernel Documentation: Namespaces & Cgroups v2](https://www.kernel.org/doc/html/latest/admin-guide/cgroup-v2.html)**
   * **Topics:** Unified cgroup v2 hierarchy, memory controller, CPU bandwidth controller, process isolation man pages (`namespaces(7)`, `cgroups(7)`).

---

## 💻 Source Code References & Linux Man Pages

* **Linux Man Pages:** `man 7 namespaces`, `man 7 cgroups`, `man 2 clone`, `man 2 unshare`, `man 2 pivot_root`.
* **[runc (OCI Runtime Reference Implementation)](https://github.com/opencontainers/runc):** Examine `libcontainer/` for Go code configuring Linux namespaces, cgroups, and mounting OverlayFS.
* **[containerd](https://github.com/containerd/containerd):** Industry-standard core container runtime managing snapshotters, task execution, and shim processes (`containerd-shim-runc-v2`).
