# Chapter 2: Storage Engines & Union File Systems (OverlayFS) — Deep Dive Notes

> **Core Theme:** Docker Images are composed of immutable, read-only layers. How **Overlay2**, **Copy-on-Write (CoW)**, and **Volume Storage Primitives** handle filesystem I/O.

---

## 1. Union File Systems & Overlay2 Architecture

Docker uses **Overlay2**, a Union File System driver that presents multiple physical directories on the host as a single unified filesystem.

```
                    OVERLAY2 DIRECTORY LAYOUT
                    
Container View ──►  [ Merged Directory ]  (Unified Mount Point)
                          ▲
             ┌────────────┴────────────┐
             │                         │
  [ Upperdir ] (Read-Write Container)  │
             │                         │
  [ Lowerdir ] (Read-Only Image Layers 1 .. N)
             │
  [ Workdir ]  (Atomic file operation staging area)
```

### The 4 Overlay2 Components:
1. **`lowerdir`:** Read-only image layers stack (Shared across all containers instantiated from the same image).
2. **`upperdir`:** Read-write ephemeral container layer (Created when container starts; destroyed when container is removed).
3. **`merged`:** The active mount point presenting combined contents of `lowerdir` and `upperdir` to the container process.
4. **`workdir`:** Internal staging directory used by Linux kernel to execute Copy-on-Write operations atomically.

---

## 2. Copy-on-Write (CoW) Mechanics & Performance Impact

When a process inside a container modifies a file:
- **Read Operation:** Reads file directly from `lowerdir` image layer ($O(1)$ fast read).
- **Write Operation (Modify Existing File):**
  1. Search for file in `lowerdir` starting from top layer down.
  2. **Copy Up:** Copy the entire file from `lowerdir` up to `upperdir`.
  3. Execute write operation on the copy inside `upperdir`.

> **Performance Warning:** Modifying a 5GB database file stored in an image layer causes Docker to copy the entire 5GB file to `upperdir` before writing 1 byte! **Databases must NEVER store data inside container layers.**

---

## 3. Persistent Storage Primitives Comparison

```
┌─────────────────────────┬─────────────────────────┬─────────────────────────┐
│     NAMED VOLUMES       │       BIND MOUNTS       │      TMPFS MOUNTS       │
│  /var/lib/docker/...    │    /home/user/project   │     Host System RAM     │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

### 1. Named Volumes (`docker volume create my_data`)
- Stored in Docker-managed directory (`/var/lib/docker/volumes/`).
- Fully isolated from host system changes; highest performance for databases (PostgreSQL, MySQL).

### 2. Bind Mounts (`-v /host/src/app:/app`)
- Maps arbitrary host file/directory directly into container.
- Ideal for local development (Live code reloading).

### 3. `tmpfs` Mounts (`--tmpfs /tmp`)
- Mounts directory directly into **Host System RAM**.
- Data is never written to disk; erased when container stops (Used for sensitive tokens & secret keys).

---

## 4. Comparison Matrix

| Property | Container Layer (Overlay2) | Named Volume | Bind Mount | `tmpfs` |
| :--- | :--- | :--- | :--- | :--- |
| **Persists after `docker rm`**| ❌ No (Deleted) | ✅ Yes | ✅ Yes | ❌ No |
| **Host System Access** | Restricted | Docker Engine Managed | Full Host Access | RAM Only |
| **I/O Performance** | Slower (CoW Overhead) | Fast (Native I/O) | Fast (Native I/O) | Ultra-Fast (RAM) |
| **Use Case** | Ephemeral temporary files | Production Databases | Dev Code Reloading | Security Tokens |
