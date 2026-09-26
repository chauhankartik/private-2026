# Git Architecture Mind Map & Decision Matrix

> **Purpose:** Structural decision-making for choosing Git workflows, conflict resolution algorithms, repository performance configurations, and Monorepo scaling.

---

## 📊 Feature & Architecture Summary Matrix

| Mechanism | Low-Level Representation | Trade-off / Consideration | Key Command / Config |
| :--- | :--- | :--- | :--- |
| **Object Database** | SHA-1 / SHA-256 zlib compressed files | Loose objects cause filesystem degradation | `git gc --prune=now` |
| **Staging Area** | `.git/index` binary cache table | Tracks stat info & SHA hashes for fast diffs | `git status` / `git add` |
| **Branch Pointer** | 41-byte text file in `.git/refs/heads/` | Branching has zero memory/CPU cost ($O(1)$) | `git branch` / `git switch` |
| **3-Way Merge** | Lowest Common Ancestor (LCA) merge base | Creates merge commits (`--no-ff`) preserving history | `git merge --no-ff` |
| **Rebase** | Replays commit diffs onto new base SHA | Rewrites commit history SHA-1 hashes (Never rebase pushed public branches!) | `git rebase -i` |
| **Packfiles** | `.pack` delta compression + `.idx` index | High CPU compression during `git gc` | `git repack -a -d` |
| **Reflog Safety Net** | Monotonic log in `.git/logs/refs/` | Keeps deleted references for 90 days | `git reflog` / `git reset --hard` |
| **Monorepo Scaling** | Sparse Checkout & Commit Graph | Reduces working tree size & DAG traversal time | `git sparse-checkout set <dir>` |
