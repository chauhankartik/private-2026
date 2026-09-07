# Chapter 5: Container Security, Seccomp & Capabilities — Deep Dive Notes

> **Core Theme:** Containers share the host Linux kernel. How **Linux Capabilities**, **Seccomp Syscall Filtering**, **AppArmor**, and **Rootless Engines** enforce Defense-in-Depth container security.

---

## 1. Linux Capabilities (`cap-drop` / `cap-add`)

Traditionally, Linux divides process permissions into Root (UID 0 - superuser) vs Non-Root (unprivileged). **Linux Capabilities** partition root privileges into ~40 granular units.

### Key Linux Capabilities:
- `CAP_SYS_ADMIN`: Equivalent to full root access (Mount filesystems, configure network, load kernel modules).
- `CAP_NET_ADMIN`: Configure network interfaces, IP routing tables, `iptables`.
- `CAP_NET_BIND_SERVICE`: Bind sockets to privileged ports below 1024 (`< 1024`).
- `CAP_SYS_TIME`: Modify host system clock.

```bash
# Security Best Practice: Drop ALL capabilities, re-add ONLY minimum needed:
docker run --cap-drop ALL --cap-add NET_BIND_SERVICE my_web_app
```

---

## 2. Seccomp (Secure Computing Mode)

Seccomp is a Linux kernel feature that restricts which **System Calls (Syscalls)** a process can execute.

```
Container Process ──► Syscall (e.g. reboot()) ──► [ Linux Seccomp Filter ] ──► REJECTED (EPERM Error)
```

- Linux defines ~300+ syscalls (e.g. `read()`, `write()`, `reboot()`, `ptrace()`).
- Docker's default Seccomp profile blocks **44 dangerous syscalls** (including `kexec_load`, `reboot`, `swapoff`, `sysfs`).

---

## 3. Read-Only Containers & Rootless Engine

### 1. Read-Only Root Filesystem (`--read-only`):
Mounts container's root filesystem as read-only. Prevents malware from downloading or executing scripts inside `upperdir` layer.

```bash
docker run --read-only --tmpfs /tmp --tmpfs /run my_app
```

### 2. Rootless Docker Engine:
- Runs `dockerd` daemon and containers completely inside an unprivileged user namespace (`UID 1000`).
- Even if a attacker breaks out of container process, they have zero root privileges on host OS!

---

## 4. Container Hardening Summary Checklist

| Security Mechanism | Flag / Configuration | Defense Provided |
| :--- | :--- | :--- |
| **Drop Capabilities** | `--cap-drop ALL --cap-add ...` | Prevents unauthorized kernel operations |
| **Seccomp Profile** | `--security-opt seccomp=custom.json` | Blocks dangerous system calls |
| **Read-Only FS** | `--read-only --tmpfs /tmp` | Prevents runtime malware persistence |
| **No New Privileges** | `--security-opt no-new-privileges:true` | Blocks `setuid` privilege escalation |
| **Non-Root User** | `USER 10001:10001` in Dockerfile | Limits process access if compromised |
