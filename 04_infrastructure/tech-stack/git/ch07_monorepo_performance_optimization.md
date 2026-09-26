# Chapter 7: Monorepo Scaling & Performance Optimization — Deep Dive Notes

> **Core Theme:** Scaling Git for enterprise monorepos containing millions of files and gigabytes of DAG history using **Sparse Checkout**, **Partial Clones**, **Commit Graphs**, and **FSMonitor**.

---

## 1. The Monorepo Performance Problem

As a Git repository grows to millions of files and millions of commits:
1. `git status` takes 30+ seconds due to scanning 1,000,000 files during `stat()`.
2. `git log` and branch merging slow down due to traversing massive DAG graphs.
3. `git clone` downloads tens of gigabytes of historical blobs over network.

---

## 2. Sparse Checkout & Partial Clones

### 1. Sparse Checkout (`git sparse-checkout`)
Restricts the local working directory to only the specific microservice folders you work on, hiding all other monorepo directories while preserving full Git history integrity.

```bash
git sparse-checkout init --cone
git sparse-checkout set services/payment-service shared/utils
# Working directory now contains ONLY payment-service and shared/utils!
```

### 2. Partial Clones (`--filter=blob:none`)
Eliminates downloading full repository history blobs during clone.

```bash
git clone --filter=blob:none git@github.com:company/monorepo.git
```
- **Blobless Clone:** Downloads 100% of commit and tree objects (so `git log`, `git checkout`, and branch operations work offline), but downloads file blobs **lazily on-demand** only when files are checked out or edited!

---

## 3. Commit Graph File (`commit-graph`)

Git pre-computes DAG relationship metadata and generation numbers into a single binary file at `.git/objects/info/commit-graph`.

```bash
# Generate commit graph file locally:
git commit-graph write --reachable --changed-paths
```

### Performance Impact:
- Speeds up topological commit traversals (`git log --graph`, `git merge-base`, ancestral reachability queries) by **10x to 100x** on large repositories!

---

## 4. FSMonitor (Filesystem Monitor Daemon)

Integrates Linux `inotify` / macOS `FSEvents` daemon directly into Git's index engine.

```bash
git config core.fsmonitor true
```

- Instead of scanning 1,000,000 files during `git status`, Git queries the FSMonitor daemon: *"Which files changed since timestamp X?"*
- FSMonitor returns the 3 modified file paths instantly, reducing `git status` latency from 30 seconds to **15 milliseconds**!
