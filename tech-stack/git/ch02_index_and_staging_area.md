# Chapter 2: The Index / Staging Area & Working Tree Mechanics — Deep Dive Notes

> **Core Theme:** The **Index (`.git/index`)** is Git's secret weapon for performance. How Git compares **The Three Trees** to execute ultra-fast status diffs and commits.

---

## 1. The Three Trees of Git

Git manages repository files across three distinct states called **The Three Trees**:

```
 ┌──────────────────────┐        git add        ┌──────────────────────┐
 │   WORKING TREE       │ ────────────────────► │      THE INDEX       │
 │ (Files on Host Disk) │                       │  (Staging Area File) │
 └──────────────────────┘                       └──────────────────────┘
            ▲                                              │
            │                  git checkout                │  git commit
            └──────────────────────────────────────────────┼──────────────┐
                                                           ▼              ▼
                                                ┌──────────────────────────┐
                                                │       HEAD COMMIT        │
                                                │   (Last Committed Tree)  │
                                                └──────────────────────────┘
```

1. **Working Directory:** Physical workspace directory on your filesystem containing files you edit.
2. **Index (Staging Area):** A single binary file at `.git/index` containing the exact proposed snapshot for the *next* commit.
3. **`HEAD` Commit:** The tree snapshot referenced by the current commit pointer in history.

---

## 2. `.git/index` Binary File Format

The Index is not a directory; it is a single, highly-optimized binary cache file (`DIRC` format).

### Index Entry Table Fields:
For every tracked file in the repository, `.git/index` stores:
- `ctime` & `mtime` (Last metadata/file change timestamps).
- `dev` & `ino` (Host filesystem device and inode number).
- `mode` (Permissions: `100644` standard file, `100755` executable, `120000` symlink).
- `uid` & `gid` (Owner user ID and group ID).
- `file_size` (File length in bytes).
- **SHA-1 Hash:** Pointer to the Blob object in `.git/objects/`.
- `path_name` (Relative file path name, e.g. `src/utils.c`).

### Why `git status` is Blazingly Fast ($O(N)$):
When you run `git status`, Git does NOT compute SHA-1 hashes of all files in your project!
- It executes a rapid Linux `stat()` system call on every file.
- Compares `stat()` timestamps/size against `.git/index` cache values.
- If `stat()` values match the index entry, Git guarantees the file is **unmodified** without reading a single byte of file content!

---

## 3. What Happens Under the Hood of `git add` and `git commit`

### 1. Execution of `git add file.txt`:
1. Reads `file.txt` from Working Tree.
2. Formats `blob <size>\0<content>`, calculates SHA-1 hash (e.g. `8a3f...`).
3. Writes compressed blob object to `.git/objects/8a/3f...`.
4. Updates `.git/index` table: sets path `file.txt` $\to$ SHA-1 `8a3f...` and updates `stat()` metadata.

### 2. Execution of `git commit -m "Add file"`:
1. Reads `.git/index` entry table.
2. Writes **Tree Objects** for every directory represented in the index.
3. Writes **Commit Object**:
   - `tree`: Root Tree SHA-1 hash.
   - `parent`: Current `HEAD` commit SHA-1 hash.
   - `author` / `committer` metadata + message.
4. Moves current branch reference (`.git/refs/heads/main`) to point to the new Commit SHA-1.
