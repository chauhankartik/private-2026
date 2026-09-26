# Chapter 6: Advanced Tools: Reflog, Bisect, Worktrees & Hooks — Deep Dive Notes

> **Core Theme:** Advanced developer productivity tools: **`git reflog` safety net**, **`git bisect` binary search debugging**, **`git worktree` parallel checkouts**, and **Git Hooks**.

---

## 1. `git reflog`: The Safety Net

Even if you execute `git reset --hard` or accidentally delete a branch, Git **never deletes commits immediately**.

Every reference update is logged sequentially in `.git/logs/HEAD`.

```bash
git reflog
# Output:
# e4f8a3b HEAD@{0}: reset: moving to HEAD~3
# 9d8c7b6 HEAD@{1}: commit: Add new feature
# c3d4e5f HEAD@{2}: checkout: moving from main to feature
```

### Recovering "Lost" Commits:
If you accidentally lost commits via `git reset --hard`, inspect `git reflog` to find the original commit SHA-1 (`HEAD@{1}`), then restore it:

```bash
git reset --hard HEAD@{1} # Restores repository to exact state before reset!
```

---

## 2. `git bisect`: $O(\log N)$ Binary Search Debugging

`git bisect` uses binary search across the DAG graph to pinpoint the exact commit that introduced a bug or regression.

```
Commit History Timeline (1,000 Commits):
[ Good Commit C1 ] ─────────────────────► [ Bad Commit C1000 ]
                               │
                      Test Midpoint (C500)
```

### Automated Bisect Workflow:
```bash
git bisect start
git bisect bad                 # Current HEAD is broken
git bisect good v1.0.0         # Version 1.0.0 was known good

# Run automated test script across binary search iterations:
git bisect run npm test
# Output: 8d2c4b... is the first bad commit (Found in 10 steps instead of 1000!)
```

---

## 3. `git worktree`: Parallel Branch Workspaces

Traditional `git checkout` forces you to stash or commit unfinished work when switching to another branch.

`git worktree` allows you to checkout multiple branches simultaneously into **separate directories on disk**, all sharing the same single `.git` repository folder!

```bash
# Add a parallel working tree directory for hotfix branch:
git worktree add ../project-hotfix hotfix-branch

# Work inside ../project-hotfix independently without disturbing main workspace!
# Once done, remove worktree:
git worktree remove ../project-hotfix
```

---

## 4. Git Hooks (`.git/hooks/`)

Git hooks are executable scripts triggered automatically by key Git events.

### 1. Client-Side Hooks:
- **`pre-commit`:** Runs before commit object is created (Executes linters, code formatters, unit tests).
- **`commit-msg`:** Validates commit message format (e.g. Enforces `JIRA-123: Description`).
- **`pre-push`:** Runs prior to uploading packfiles to remote server.

### 2. Server-Side Hooks:
- **`pre-receive`:** Runs on remote server before updating refs (Enforces branch protection rules, blocks pushes containing secrets).
- **`post-receive`:** Runs after remote refs are updated (Triggers CI/CD deployment pipelines).
