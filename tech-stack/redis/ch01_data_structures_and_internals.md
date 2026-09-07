# Chapter 1: Redis Data Structures & Low-Level C Internals — Deep Dive Notes

> **Core Theme:** Redis is an in-memory key-value data structure server. How Redis achieves $O(1)$ and $O(\log N)$ performance using specialized C structs: SDS, Dict, SkipList, Quicklist, and Intset.

---

## 1. SDS (Simple Dynamic String)

Standard C strings are null-terminated (`\0`) character arrays. Redis replaces them with **SDS (Simple Dynamic String)** (`sds.h`).

```c
struct __attribute__ ((__packed__)) sdshdr8 {
    uint8_t len;        // Used bytes count (excluding null terminator)
    uint8_t alloc;      // Total allocated capacity
    unsigned char flags; // Header type (sdshdr5, 8, 16, 32, 64)
    char buf[];         // Raw byte array
};
```

### Why SDS is Superior to C Strings:
1. **$O(1)$ Length Retrieval:** `len` field provides instant string length without $O(N)$ `strlen()` scanning.
2. **Binary Safety:** Can store arbitrary binary data (images, Protobuf bytes, encrypted keys) containing `\0` characters.
3. **Buffer Overflow Prevention:** SDS API checks available capacity (`alloc - len`) before concatenation and resizes automatically.
4. **Pre-allocation & Lazy Freeing:** Reduces allocation calls by allocating 100% extra buffer space when string is $< 1\text{MB}$.

---

## 2. Dict & Incremental Rehash

Redis `HASH` objects and the top-level key-value namespace use **Dict** (`dict.h`), an open-addressing hashtable array using chaining for collisions.

```
dict
 ├── ht[0] (Primary Hashtable)
 ├── ht[1] (Secondary Hashtable - Used during Rehash)
 └── rehashidx (-1 if not rehashing, >= 0 during rehash)
```

### Incremental / Progressive Rehash Algorithm:
If a hashtable needs to expand (e.g. load factor $> 1$), resizing a 10-million key table in one batch would freeze the server for seconds!

1. Redis allocates a new, larger hashtable in `ht[1]`.
2. Sets `rehashidx = 0`.
3. **Incremental Migration:** On every incoming CRUD operation, Redis migrates 1 bucket from `ht[0]` to `ht[1]` inline.
4. **Background Cron:** The server cron job also migrates buckets for 1ms per iteration.
5. Once `ht[0]` is empty, `ht[0]` is swapped with `ht[1]`, and `rehashidx` is reset to `-1`.

---

## 3. Sorted Sets (ZSET): SkipList + Hashtable

Redis `ZSET` allows $O(\log N)$ insertion, removal, and range queries (e.g. `ZRANGEBYSCORE`).

### Dual-Structure Design:
- **Dict (Hashtable):** Maps `element -> score` for $O(1)$ score lookups (`ZSCORE`).
- **SkipList (zskiplist):** Multi-level linked list maintaining elements sorted by score.

```
Level 3:  [Node 1] ───────────────────────────────────────────► [Node 100]
Level 2:  [Node 1] ─────────────────► [Node 50] ──────────────► [Node 100]
Level 1:  [Node 1] ──► [Node 25] ──► [Node 50] ──► [Node 75] ──► [Node 100]
```

### Why SkipList over Red-Black Trees?
1. Range queries (`ZRANGE`) simply traverse the level-1 forward pointers sequentially.
2. Concurrent implementation and memory allocation are simpler than AVL / Red-Black Tree rebalancing.

---

## 4. Quicklist, ZipList, and Listpack

To conserve memory, small collections avoid heap pointers by using contiguous byte arrays:

- **ZipList / Listpack:** A single contiguous block of memory holding small lists or hashes without individual pointer overhead.
- **Quicklist:** A doubly linked list where each node is a compressed **ZipList** (Combines low memory allocation overhead with fast node traversal).

---

## 5. Summary of Redis Internal Struct Mappings

| High-Level Data Type | Small Dataset Representation | Large Dataset Representation |
| :--- | :--- | :--- |
| **String** | `sdshdr8` / `sdshdr16` | `sdshdr32` / `sdshdr64` |
| **List** | Quicklist / Listpack | Quicklist (Doubly-linked ZipLists) |
| **Hash** | Listpack / ZipList ($< 512$ entries) | Dict (Hashtable) |
| **Set** | Intset (Sorted int array) | Dict (Hashtable with null values) |
| **Sorted Set (ZSET)** | Listpack ($< 128$ items) | SkipList + Dict |
