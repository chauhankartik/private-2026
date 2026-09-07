# Git Architecture & Internals — Deep Dive Study Guide

> **Goal:** Master Git low-level internals, Content-Addressable Object Database (Blobs, Trees, Commits, Tags), `.git/index` staging area binary mechanics, DAG branching pointers, 3-Way Merge & Rebase algorithms, Packfile Delta Compression, and Monorepo scaling for Staff Software Engineering.

---

## 🧠 Interactive Git Architecture Mind Map

```mermaid
mindmap
  root(("Git Internals"))
    "01 Object Model Database"
      "Content Addressable Store - SHA-1 SHA-256"
      "Blob - File Content"
      "Tree - Directory Structure"
      "Commit - Author Tree Parent Msg"
      "Annotated Tag - Pointers"
    "02 Staging Area & The 3 Trees"
      "Working Directory"
      "Index File - .git/index Binary"
      "HEAD Commit"
      "git add vs git commit Mechanics"
    "03 DAG Branching & Refs"
      "References - .git/refs/heads"
      "Symbolic Ref vs Detached HEAD"
      "Branch as 41-byte Text File"
    "04 Merging & Rebasing"
      "Lowest Common Ancestor - LCA Merge Base"
      "Fast-Forward vs 3-Way Merge"
      "Rebase Commit Replay Mechanics"
      "Cherry-Pick & Revert"
    "05 Packfiles & Storage"
      "Loose Objects vs Packfiles"
      "Delta Compression - Offset Deltas"
      "git gc Garbage Collection"
    "06 Advanced Diagnostics"
      "git reflog Safety Net"
      "git bisect O(log N) Binary Search"
      "git worktree Parallel Worktrees"
      "Git Hooks - Automation"
    "07 Monorepo Scaling"
      "Sparse Checkout & Shallow Clones"
      "Commit Graph Files"
      "FSMonitor Daemon"
```

👉 **Full Mind Map & Taxonomy:** [`00_Git_MindMap.md`](00_Git_MindMap.md)

---

## 📖 Chapter Index

1. **[Ch 1: Git Object Model & Storage Primitives](ch01_object_database_internals.md)** — Content-addressable KV store, Blobs, Trees, Commits, Tags, zlib header compression.
2. **[Ch 2: The Index / Staging Area & Working Tree Mechanics](ch02_index_and_staging_area.md)** — The Three Trees, `.git/index` binary format, under the hood of `git add` & `git commit`.
3. **[Ch 3: DAG (Directed Acyclic Graph) Branching & References](ch03_dag_branching_and_references.md)** — References (`.git/refs/`), Symbolic Refs vs Detached `HEAD`, why branching is $O(1)$.
4. **[Ch 4: Merging, Rebasing & Conflict Resolution](ch04_merge_rebase_conflict_resolution.md)** — 3-Way Merge, Lowest Common Ancestor (LCA), Fast-Forward, Rebase mechanics, Cherry-Pick & Revert.
5. **[Ch 5: Packfiles, Garbage Collection & Network Protocols](ch05_packfiles_gc_and_protocols.md)** — Loose objects vs `.pack`/`.idx` Packfiles, Delta Compression, `git gc`, Smart HTTP & SSH protocols.
6. **[Ch 6: Advanced Tools: Reflog, Bisect, Worktrees & Hooks](ch06_reflog_bisect_worktree_hooks.md)** — `git reflog` recovery, `git bisect` $O(\log N)$ debugging, `git worktree`, Git Hooks (`.git/hooks/`).
7. **[Ch 7: Monorepo Scaling & Performance Optimization](ch07_monorepo_performance_optimization.md)** — Monorepo scaling, Sparse Checkout, Shallow Clones (`--depth 1`), Commit Graph.
