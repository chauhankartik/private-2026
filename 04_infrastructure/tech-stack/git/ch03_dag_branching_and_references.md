# Chapter 3: DAG Branching & References — Deep Dive Notes

> **Core Theme:** History in Git is a **Directed Acyclic Graph (DAG)** of immutable commits. Why branching in Git is an $O(1)$ operation that costs 41 bytes on disk.

---

## 1. What is a Git Reference (Ref)?

A reference is simply a pointer to a commit in the DAG graph. All references live as plain text files inside `.git/refs/`.

```
.git/refs/
├── heads/
│   ├── main       (Text file containing: "e4f8a3b2c1...")
│   └── feature    (Text file containing: "9d8c7b6a5f...")
├── remotes/
│   └── origin/
│       └── main   (Remote tracking reference)
└── tags/
    └── v1.0.0     (Tag pointer)
```

### Why Branching in Git is Blazingly Fast ($O(1)$):
In legacy VCS systems (SVN), creating a branch requires copying the entire project folder on the server.
- In Git, creating a new branch (`git branch feature`) creates a **41-byte text file** inside `.git/refs/heads/feature` containing the 40-character SHA-1 commit hash of `HEAD`.
- It consumes zero network bandwidth and virtually zero disk space!

---

## 2. The `HEAD` Pointer & Detached `HEAD` State

`.git/HEAD` is a reference file pointing to where you currently are in history.

### 1. Standard Symbolic Reference (`.git/HEAD`):
```text
ref: refs/heads/main
```
`HEAD` points to the `main` branch file $\to$ `main` points to commit `e4f8a3...`.

### 2. Detached `HEAD` State:
Occurs when you checkout a specific commit SHA or tag directly (`git checkout e4f8a3`):
```text
e4f8a3b2c1d098765432101234567890abcdef12
```
- `HEAD` points **directly to a Commit SHA-1**, not a branch name!
- **Warning:** Any new commits created in a Detached `HEAD` state will become **Dangling Commits** (unreachable by any branch ref) and will eventually be permanently deleted by `git gc` unless tagged or assigned to a branch!

---

## 3. Modern Navigation: `git switch` vs. `git restore`

Historically, `git checkout` performed overloaded duties: switching branches, creating branches, discarding working tree changes, and restoring index files. Git 2.23+ split `checkout` into two explicit commands:

### 1. `git switch` (Managing Branches):
- `git switch feature`: Switch to existing branch `feature`.
- `git switch -c new-feature`: Create and switch to new branch `new-feature`.

### 2. `git restore` (Managing Files):
- `git restore file.txt`: Discard working tree changes (restore file from Index).
- `git restore --staged file.txt`: Unstage file (restore Index entry from `HEAD`).
