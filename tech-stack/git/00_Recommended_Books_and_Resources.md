# Git Recommended Reading List & Technical References

A curated list of books, technical papers, specs, and source code walkthroughs for mastering Git's object database, DAG structures, delta compression, and monorepo scaling.

---

## 📚 Recommended Books

1. **_Pro Git (2nd Edition)_** — Scott Chacon & Ben Straub (Apress)
   * **Why Read It:** The official, authoritative book on Git. Chapter 10 (*Git Internals*) is essential reading for understanding blobs, trees, commits, annotated tags, references, and packfiles.
   * **Key Focus:** Comprehensive operational mastery and underlying storage model.

2. **_Git from the Bottom Up_** — John Wiegley
   * **Why Read It:** A brief, highly focused conceptual PDF/book that builds Git understanding from first principles (content-addressable filesystem, Directed Acyclic Graphs, and index staging).
   * **Key Focus:** Mechanical mental model of Git data structures.

3. **_Version Control with Git_** — Jon Loeliger & Matthew McCullough (O'Reilly)
   * **Why Read It:** In-depth guide covering advanced merging techniques, octopus merges, reflog mechanics, submodules, and branch management workflows.

---

## 📄 Technical Specifications & Architecture Papers

1. **[Git Technical Documentation Repository](https://github.com/git/git/tree/master/Documentation/technical)**
   * **`pack-format.txt`:** Specification for Git `.pack` files, thin packs, and `.idx` index file format v1 & v2.
   * **`index-format.txt`:** Binary file format specification for `.git/index` (staging area binary tree).
   * **`commit-graph.txt`:** Commit-Graph binary format specification for accelerating DAG graph traversals.
2. **[Linus Torvalds' 2005 Google Tech Talk on Git](https://www.youtube.com/watch?v=4XpnKHJAok8)**
   * **Topics:** Initial architectural design principles behind Git: performance, integrity (SHA-1 hashing), independence from central servers, non-linear development.

---

## 💻 Open-Source Code References (C implementation)

Explore the core algorithms in the [Git C Source Repository](https://github.com/git/git):

* **`object.c` & `object.h`:** Header and object table management for blobs, trees, commits, tags.
* **`sha1-file.c` (or `object-file.c`):** Code writing loose objects compressed with zlib.
* **`packv2.c` & `pack-bitmap.c`:** Packfile creation, delta compression, and reachability bitmaps.
* **`read-cache.c`:** High-performance binary parser for the `.git/index` file.
