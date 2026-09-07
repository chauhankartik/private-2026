# Chapter 5: Packfiles, Garbage Collection & Network Protocols — Deep Dive Notes

> **Core Theme:** How Git compresses millions of historical files into compact **Packfiles (`.pack`)**, executes **Garbage Collection (`git gc`)**, and transfers data across network protocols.

---

## 1. Loose Objects vs. Packfiles

When you commit code, Git creates individual **Loose Objects** on disk.

- **The Loose Object Problem:** Storing 500,000 loose objects as individual files degrades filesystem performance due to inode exhaustion and directory scan overhead.
- **The Solution (Packfiles):** Git packs loose objects into a single contiguous binary archive called a **Packfile (`.pack`)** accompanied by a 256-entry fan-out Index (`.idx`).

```
.git/objects/pack/
├── pack-a1b2c3d4...pack   (Concatenated compressed objects & deltas)
└── pack-a1b2c3d4...idx    (Binary fan-out table mapping SHA-1 -> Byte Offset)
```

---

## 2. Delta Compression in Packfiles

While Git's conceptual data model treats every commit as a full snapshot, its storage engine uses **Delta Compression** to save disk space.

```
File Version 1 (100 KB) ──► Stored as FULL BLOB Base
File Version 2 (101 KB) ──► Stored as DELTA ("Insert 1 KB at offset 5000")
File Version 3 (102 KB) ──► Stored as DELTA ("Delete 2 lines at offset 100")
```

- Git scans historical objects using a sliding window.
- If it finds two objects with similar content (even across different filenames or branches), it stores one as a **Base Object** and the other as a compressed **Offset Delta**.
- **Result:** Reduces repository disk footprint by 90%+!

---

## 3. Garbage Collection (`git gc`)

`git gc` cleans up and optimizes the local repository.

```bash
# Force full repository repacking and dangling object pruning:
git gc --prune=now --aggressive
```

### What `git gc` Does Under the Hood:
1. **Reachability Analysis:** Traverses DAG starting from all references in `.git/refs/` and `.git/HEAD`.
2. **Dangling Object Identification:** Finds loose objects that are unreachable by any branch or tag reference (e.g., discarded rebase commits).
3. **Packing:** Packs all reachable loose objects into a new `.pack` file with Delta Compression.
4. **Pruning:** Deletes dangling objects that are older than `gc.pruneExpire` (Default: 14 days).

---

## 4. Network Transfer Protocols (`fetch` vs. `pull`)

### 1. Negotiation Phase:
When you run `git fetch`:
1. Client sends `want <SHA-1>` list (commits present on remote but missing locally).
2. Client sends `have <SHA-1>` list (commits already present locally).
3. Remote server calculates the minimal set of DAG nodes missing on client.

### 2. Packfile Transfer:
- Remote server generates a dynamic **Packfile Stream** on-the-fly containing only missing commits/blobs and streams it to client.
- `git pull` = `git fetch` + `git merge` (or `git rebase`).
