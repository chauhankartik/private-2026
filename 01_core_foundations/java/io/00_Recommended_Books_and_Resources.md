# Java File I/O & NIO.2 Recommended Books & Resources

A curated list of essential textbooks, official specifications, OpenJDK source code pointers, and OS-level documentation for mastering Java I/O and high-performance file processing.

---

## Must-Read Textbooks & References

1. **"Java I/O" (2nd Edition) by Elliotte Rusty Harold**
   - *Focus:* Comprehensive reference for classic `java.io` stream hierarchies, filter streams, character encoding, and object serialization.
   - *Key Chapters:* Chapter 3 (Data Streams), Chapter 4 (Filter Streams), Chapter 10 (Cryptographic Streams), Chapter 18 (Object Serialization).

2. **"Java NIO" by Ron Hitchens**
   - *Focus:* Definitive deep-dive into non-blocking I/O, `ByteBuffer` mechanics, channel multiplexing, and memory-mapped file I/O.
   - *Key Chapters:* Chapter 2 (Buffers), Chapter 3 (Channels), Chapter 4 (Selectors), Chapter 5 (Filesystem & Path API).

3. **"Effective Java" (3rd Edition) by Joshua Bloch**
   - *Focus:* Serialization security, defensive object creation, and modern alternatives.
   - *Key Items:*
     - **Item 85:** Prefer alternatives to Java serialization.
     - **Item 86:** Implement `Serializable` with great caution.
     - **Item 87:** Use a custom serialized form.
     - **Item 88:** Write `readObject` methods defensively.
     - **Item 89:** For instance control, prefer enum types to `readResolve`.
     - **Item 90:** Consider serialization proxies instead of serialized instances.

4. **"Operating System Concepts" by Silberschatz, Galvin, Gagne**
   - *Focus:* OS kernel memory management, Page Cache, System Call interfaces, and DMA (Direct Memory Access).
   - *Key Sections:* I/O Systems (Chapter 13), File-System Implementation & Page Cache (Chapter 11).

---

## Official Specifications & Linux Kernel Documentation

1. **Linux Kernel Manual Pages (`man 2` & `man 7`):**
   - `man 2 mmap`: Map files or devices into memory (`mmap`, `munmap`, `msync`).
   - `man 2 sendfile`: Transfer data between file descriptors without copying to user space.
   - `man 2 fsync`: Synchronize a file's in-core state with storage device.
   - `man 7 inotify`: Monitoring filesystem events (foundation of Java `WatchService` on Linux).

2. **Java Language & API Specifications:**
   - [Java SE `java.io` Package Specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/package-summary.html)
   - [Java SE `java.nio.file` Package Specification (NIO.2)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/package-summary.html)
   - [Java Object Serialization Specification](https://docs.oracle.com/en/java/javase/21/docs/specs/serialization/index.html)

---

## OpenJDK Source Code Pointers

To understand how Java delegates I/O calls to native Linux kernel syscalls, inspect these OpenJDK source paths:

- `src/java.base/share/classes/java/io/FileInputStream.java` — Native method invocations (`read0()`, `open0()`).
- `src/java.base/share/classes/java/nio/Buffer.java` — Invariant bounds check implementations.
- `src/java.base/share/classes/java/nio/DirectByteBuffer.java` — Direct off-heap memory allocation via `Unsafe.allocateMemory()` / `Cleaner`.
- `src/java.base/unix/classes/sun/nio/ch/FileChannelImpl.java` — Native zero-copy implementations using `transferTo0()` and Linux `sendfile(2)`.
- `src/java.base/unix/classes/sun/nio/fs/LinuxWatchService.java` — Native Linux `inotify_init()` and `inotify_add_watch()` integrations.
