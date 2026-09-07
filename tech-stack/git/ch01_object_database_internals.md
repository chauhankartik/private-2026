# Chapter 1: Git Object Model & Storage Primitives — Deep Dive Notes

> **Core Theme:** Git is fundamentally a **Content-Addressable Key-Value Database** sitting on top of the filesystem. How Git stores all data using 4 core object types: **Blobs**, **Trees**, **Commits**, and **Tags**.

---

## 1. Git as a Content-Addressable Database

Unlike traditional VCS tools (CVS, Subversion) that store file diffs (`delta` changesets per file), Git stores **full snapshots** of the repository over time.

- **Database Key:** 40-character hexadecimal string representing a 160-bit SHA-1 hash (or 256-bit SHA-256 hash).
- **Database Value:** zlib-compressed binary payload stored on disk inside `.git/objects/xx/yyyy...` (where `xx` is the first 2 characters of the hash, and `yyyy...` is the remaining 38 characters).

---

## 2. The Four Core Git Object Types

```
[ Commit Object ] (SHA: e4f8...)
  ├── Tree: a1b2... (Pointers to root directory)
  ├── Parent: c3d4... (Pointer to previous commit)
  ├── Author: Alice <alice@example.com>
  └── Message: "Initial commit"
        │
        ▼
[ Tree Object ] (SHA: a1b2...)
  ├── 100644 blob f9e8...  main.c
  ├── 100644 blob 3a2b...  README.md
  └── 040000 tree d4c3...  src/  ────────┐
                                         ▼
                             [ Sub-Tree Object ] (SHA: d4c3...)
                                 └── 100644 blob e7f6...  utils.c
```

### 1. Blob (Binary Large Object)
Stores **raw file content ONLY**.
- A Blob does **NOT** store the filename, file permissions (mode bits), timestamp, or directory path!
- If two identical 10MB files exist in different folders under different names, Git creates **only ONE Blob object** ($O(1)$ automatic de-duplication!).

### 2. Tree Object
Represents a **directory**.
- Contains a list of entries mapping: `[mode_bits] [type (blob|tree)] [SHA-1 hash] [filename]`.
- Points to Blobs (for files) or other nested Trees (for subdirectories).

### 3. Commit Object
Represents a **point-in-time repository snapshot**.
- Contains:
  1. Pointer to top-level root **Tree** SHA-1 hash.
  2. Pointer to parent **Commit** SHA-1 hash (Zero parents for root commit; 1 parent for standard commit; 2+ parents for merge commit).
  3. Author name, Committer name, PGP signature, timestamp, and commit message.

### 4. Tag Object (Annotated Tag)
Represents a permanent pointer to a specific commit (e.g. `v1.0.0`), containing tagger metadata and optional GPG signature.

---

## 3. Low-Level Object Serialization Format

Before compressing and saving an object to disk, Git prefixes the content with a header:

$$\text{Object Payload} = \text{zlib\_compress}\left(\texttt{"<type> <size>}\backslash\text{0}\texttt{<content>"}\right)$$

### Example (Inspecting Raw Objects with Plumbing Commands):
```bash
# Create a raw blob manually using low-level plumbing commands:
echo "Hello Git" | git hash-object -w --stdin
# Output: ceb425b03239a5840d51381395b001a8b13994d5

# Inspect object type:
git cat-file -t ceb425b03239a5840d51381395b001a8b13994d5
# Output: blob

# Inspect object content:
git cat-file -p ceb425b03239a5840d51381395b001a8b13994d5
# Output: Hello Git
```
