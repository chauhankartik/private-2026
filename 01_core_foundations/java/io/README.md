# Java File I/O, NIO & NIO.2 Masterclass Suite

Welcome to the **Java File I/O, NIO & NIO.2 Masterclass Suite**. This module provides a staff/principal-engineer level deep dive into Java input/output subsystems—ranging from classic stream-based blocking I/O (`java.io`) and high-performance channels/buffers (`java.nio`) to modern non-blocking filesystem operations (`java.nio.file`), memory-mapped zero-copy transfers, asynchronous I/O, and object serialization mechanics and security.

---

## Module Sitemap

| File | Type | Description |
| :--- | :--- | :--- |
| **[`README.md`](README.md)** | Index | Master module index, sitemap, and core architectural comparison matrix. |
| **[`00_IO_MindMap.md`](00_IO_MindMap.md)** | Mind Map | Interactive Mermaid diagram mapping `java.io`, `java.nio`, `java.nio.file`, and OS-level kernel mechanics. |
| **[`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)** | Resources | Recommended textbooks (*Java I/O*, *Java NIO*, *Effective Java* Serialization) and Linux kernel man pages (`mmap`, `sendfile`). |
| **[`00_theory.md`](00_theory.md)** | Theory | Exhaustive theoretical guide on Kernel Space vs User Space, Page Cache, Streams vs Channels, Zero-Copy, and Serialization attack vectors. |
| **[`01_classic_io_streams.java`](01_classic_io_streams.java)** | Code | Classic `java.io` byte/character streams, Decorator pattern (`BufferedInputStream`), Charsets, and system call overhead mitigation. |
| **[`02_nio_channels_buffers.java`](02_nio_channels_buffers.java)** | Code | `FileChannel`, `ByteBuffer` mechanics (`allocate` vs `allocateDirect`), buffer state lifecycles (`flip`, `compact`), and scattered/gathered I/O. |
| **[`03_memory_mapped_zero_copy.java`](03_memory_mapped_zero_copy.java)** | Code | `MappedByteBuffer` (`mmap` syscall), zero-copy kernel transfers (`transferTo`/`sendfile`), and high-throughput file processing benchmarks. |
| **[`04_nio2_path_files_watchservice.java`](04_nio2_path_files_watchservice.java)** | Code | NIO.2 (`java.nio.file`) `Path`, atomic `Files` operations, directory tree walking (`FileVisitor`), POSIX permissions, and real-time `WatchService`. |
| **[`05_file_locking_async_io.java`](05_file_locking_async_io.java)** | Code | Process file synchronization with `FileLock` (Shared vs Exclusive) and non-blocking asynchronous file operations with `AsynchronousFileChannel`. |
| **[`06_serialization_deep_dive.java`](06_serialization_deep_dive.java)** | Code | Java Object Serialization internals (`Serializable`, `serialVersionUID`, `transient`, `writeObject`/`readObject`), `Externalizable`, and modern protocol alternatives. |

---

## Architectural Comparison Matrix: `java.io` vs `java.nio` vs `mmap`

| Metric / Dimension | Classic Stream I/O (`java.io`) | NIO Channels & Buffers (`java.nio`) | Memory-Mapped I/O (`MappedByteBuffer`) |
| :--- | :--- | :--- | :--- |
| **I/O Model** | Blocking Stream-Oriented | Block / Buffer-Oriented | Direct Kernel Memory Mapping (`mmap`) |
| **Data Transfer Unit** | Single byte or byte array | Fixed-capacity `ByteBuffer` | Direct byte access in virtual memory |
| **Kernel Copies** | 2 Copies (Kernel Page Cache -> User Buffer -> Application Array) | 1–2 Copies (Can bypass user-space buffer with Direct `ByteBuffer`) | **0 Copies** (Virtual memory pages mapped directly to Page Cache) |
| **Memory Region** | JVM Heap (`byte[]`) | Heap or Off-Heap (Direct Memory via `Unsafe`) | OS Virtual Address Space (Off-Heap) |
| **Concurrency** | Stream per thread (Blocking read/write) | Multi-channel thread reuse / Selectors | Thread-safe concurrent read access across processes |
| **Optimal Use Case** | Small sequential files, text parsing, simple logging | High-concurrency network/file I/O, socket processing | Multi-gigabyte file indexing, databases (e.g., RocksDB, Lucene) |

---

## Key Technical Concepts Covered

1. **OS Kernel & User-Space Mechanics:** User-to-kernel context switching overhead (`read`/`write` syscalls), OS Page Cache, dirty page writeback (`fsync`), and block device aligned reads.
2. **Buffer Lifecycle State Machine:** `capacity`, `position`, `limit`, `mark`. Understanding `flip()`, `clear()`, `rewind()`, and `compact()`.
3. **Direct Memory & Off-Heap Allocation:** Allocating native memory via `ByteBuffer.allocateDirect()`, avoidance of GC pause pressure, and explicit memory cleanup using JDK Cleaner/Unsafe.
4. **Zero-Copy Transfers (`sendfile` / `transferTo`):** Direct DMA transfers between network socket descriptors and page cache without user-space context switches.
5. **NIO.2 Directory Monitoring (`WatchService`):** Event-driven kernel file notification hooks (`inotify` on Linux, `kqueue` on macOS).
6. **Object Serialization Risks & Defensive Design:** Preventing gadget-chain RCE attacks, maintaining `serialVersionUID` contract, implementing custom read/write hooks, and preferring binary serialization schemes (Protobuf/FlatBuffers).
