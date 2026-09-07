# Java File I/O, NIO & NIO.2 Exhaustive Theory Guide

---

## 1. Operating System Fundamentals: Kernel vs User Space I/O

Understanding Java I/O performance requires understanding how operating systems execute I/O operations between hardware devices (HDDs, SSDs, NVMe, network interfaces) and user applications.

```
+-----------------------------------------------------------------------+
|                              USER SPACE                               |
|  Java Application Process (JVM Heap Array: byte[])                   |
+-----------------------------------------------------------------------+
                                  |
               Context Switch (read() / write() Syscall)
                                  v
+-----------------------------------------------------------------------+
|                             KERNEL SPACE                              |
|  Kernel Page Cache (OS Managed Memory Pages)                          |
+-----------------------------------------------------------------------+
                                  |
                       DMA (Direct Memory Access)
                                  v
+-----------------------------------------------------------------------+
|                            HARDWARE DEVICE                            |
|  Storage Controller (NVMe / SSD / HDD)                                |
+-----------------------------------------------------------------------+
```

### Key OS Concepts:
1. **User Space vs Kernel Space:** User applications execute in user mode with restricted memory access. Physical device I/O can only be executed by the OS Kernel in kernel mode.
2. **System Calls (`read`, `write`, `fsync`):** Transfer control from user space to kernel space. Context switches incur CPU register saves, TLB flushes, and execution cycle overhead.
3. **OS Page Cache:** The kernel buffers disk blocks in physical RAM (Page Cache). Reading a file first checks the Page Cache. If present (Cache Hit), data is copied directly to user space without disk access.
4. **Dirty Page Flushing (`fsync`):** Writes to disk update the Page Cache first (marked as "dirty"). The kernel flushes dirty pages asynchronously (`pdflush`/`flush` threads). Calling `fsync()` forces an immediate hardware flush.

---

## 2. Classic `java.io` Stream Architecture & Decorator Pattern

Classic Java I/O (`java.io`) operates on **Stream Abstractions**:
- **Byte Streams (`InputStream` / `OutputStream`):** 8-bit raw byte transfers.
- **Character Streams (`Reader` / `Writer`):** 16-bit Unicode transfers, handling character set conversion (e.g., UTF-8 to UTF-16).

### The Decorator Pattern in `java.io`

`java.io` heavily utilizes the **Decorator Design Pattern** to dynamically compose capabilities without subclass explosion:

```java
// Base Component -> FileInputStream (reads single bytes via OS syscall)
// Decorator 1    -> BufferedInputStream (adds 8KB user-space buffer to reduce syscalls)
// Decorator 2    -> DataInputStream (adds typed primitive reading: readInt, readLong)
DataInputStream dis = new DataInputStream(
    new BufferedInputStream(
        new FileInputStream("data.bin")
    )
);
```

### Why User-Space Buffering Matters
Without `BufferedInputStream`, every `read()` call on `FileInputStream` triggers a JVM-to-Kernel context switch to read a single byte:
- 1 MB file read byte-by-byte without buffer = **1,048,576 System Calls**.
- 1 MB file read with an 8 KB `BufferedInputStream` = **128 System Calls**.

---

## 3. Java NIO (`java.nio`): Buffers, Channels, and Direct Memory

Java 1.4 introduced `java.nio` (New I/O) to enable channel-based, buffer-oriented, high-throughput I/O.

### 1. Buffers: State Machine Mechanics

A `Buffer` is a contiguous block of memory with a strict internal state machine governed by four invariants:
\[ 0 \le \text{mark} \le \text{position} \le \text{limit} \le \text{capacity} \]

- **`capacity`:** Total number of elements the buffer can hold. Cannot be changed.
- **`position`:** Index of the next element to be read or written.
- **`limit`:** Index of the first element that should not be read or written.
- **`mark`:** Recorded position to reset back to via `reset()`.

```
Write Mode (Filling Buffer):
[ X | Y | Z |   |   |   ]
  ^           ^           ^
  mark        position    limit = capacity

flip() Call -> Prepares Buffer for Reading:
[ X | Y | Z |   |   |   ]
  ^           ^           ^
  position=0  limit=3     capacity=6
```

#### Essential State Transformations:
- **`flip()`:** Sets `limit = position`, then `position = 0`. Switches buffer from writing mode to reading mode.
- **`clear()`:** Sets `position = 0`, `limit = capacity`. Resets buffer for writing (does not erase data).
- **`compact()`:** Copies unread data (`position` to `limit`) to the start of the buffer, sets `position = limit - position`, `limit = capacity`. Preserves unread data while accepting new writes.
- **`rewind()`:** Sets `position = 0`, preserves `limit`. Allows re-reading the same buffer data.

---

### 2. Heap vs Direct ByteBuffers

- **Heap ByteBuffers (`ByteBuffer.allocate(size)`):** Backed by a Java `byte[]` on the JVM Heap.
  - *Pros:* Fast allocation/deallocation, subject to standard Java Garbage Collection.
  - *Cons:* During I/O syscalls, the JVM must copy Heap memory to a temporary native buffer to prevent GC pointer movement during DMA transfers.
- **Direct ByteBuffers (`ByteBuffer.allocateDirect(size)`):** Allocated directly in native C-heap memory outside the JVM Heap via `malloc()` / `Unsafe`.
  - *Pros:* Bypasses intermediate JVM heap-to-native buffer copying during DMA read/write. Ideal for long-lived, high-frequency I/O buffers.
  - *Cons:* Allocation and deallocation are expensive. Not managed by standard GC generations; freed via native `Cleaner` or `Unsafe.freeMemory()`.

---

## 4. High-Performance I/O: `mmap` & Kernel Zero-Copy

### 1. Memory-Mapped Files (`MappedByteBuffer` / `mmap`)

Memory-mapping maps a file's content directly into the virtual memory address space of the process using the OS `mmap(2)` system call.

```
Process Virtual Memory                 OS Page Cache
+---------------------+               +---------------------+
| Page 0 -> File Off 0 | ------------> | Physical Memory Page|
| Page 1 -> File Off 4k| ------------> | Physical Memory Page|
+---------------------+               +---------------------+
```

- **Mechanism:** Reading/writing to a `MappedByteBuffer` directly manipulates Page Cache memory addresses. The kernel handles page faulting and disk flushing transparently.
- **Advantages:** Bypasses read/write system calls completely. Allows random access to multi-gigabyte files without loading the entire file into physical memory.
- **Flushing:** `MappedByteBuffer.force()` triggers OS `msync(2)` to commit modified pages to disk.

---

### 2. Kernel Zero-Copy Transfers (`FileChannel.transferTo()`)

Traditional file transfer to a network socket involves **4 Context Switches** and **4 Data Copies**:

```
1. Hard Disk -> Kernel Page Cache (DMA Copy)
2. Kernel Page Cache -> User Space Buffer (CPU Copy)
3. User Space Buffer -> Socket Buffer in Kernel (CPU Copy)
4. Socket Buffer -> NIC Buffer (DMA Copy)
```

With `FileChannel.transferTo()` (utilizing Linux `sendfile(2)`), transfer requires **2 Context Switches** and **0 User-Space Copies**:

```
1. Hard Disk -> Kernel Page Cache (DMA Copy)
2. Kernel Page Cache -> NIC Buffer (DMA Copy via Descriptor Reference)
```

```
+---------------+     DMA Copy     +-------------------+
|  Storage HDD  | ---------------> | OS Kernel Page    |
+---------------+                  | Cache             |
                                   +-------------------+
                                             |
                                    Zero User-Space Copy
                                    (sendfile Syscall)
                                             |
                                             v
+---------------+     DMA Copy     +-------------------+
|  Network NIC  | <--------------- | Socket Buffer     |
+---------------+                  +-------------------+
```

---

## 5. Modern NIO.2 (`java.nio.file`) Architecture

Java 7 introduced NIO.2, completely overhauling file manipulation:

1. **`Path` API:** Object-oriented abstraction replacing legacy `java.io.File`. Immutable, supports hierarchical path resolution (`resolve()`), relativization (`relativize()`), and normalization (`normalize()`).
2. **`Files` Utility Class:** High-level static methods for atomic file creation (`copy`, `move`, `delete`), metadata inspection, and stream creation.
3. **`FileVisitor` & Directory Trees:** Memory-efficient, recursive directory traversal via `Files.walkFileTree()` or `Files.walk()`.
4. **`WatchService`:** OS-native event notification service monitoring directory modifications (`ENTRY_CREATE`, `ENTRY_DELETE`, `ENTRY_MODIFY`). Uses Linux `inotify` or macOS `kqueue`.

---

## 6. Java Object Serialization Mechanics & Security Hazards

Java Serialization converts an active object graph into a binary byte stream (`ObjectOutputStream`) and reconstructs it (`ObjectInputStream`).

### 1. Key Components:
- **`Serializable` Interface:** Marker interface enabling reflection-based serialization.
- **`serialVersionUID`:** 64-bit hash of the class structure. Crucial for version compatibility. If omitted, `javac` auto-generates one, causing `InvalidClassException` if class fields change slightly.
- **`transient` Keyword:** Excludes specific fields (e.g., passwords, transient caches) from serialized state.
- **Custom Methods (`writeObject` / `readObject`):** Overrides default serialization for custom encryption, validation, or compression.
- **`readResolve()`:** Ensures Singleton integrity during deserialization by returning the canonical singleton instance.

---

### 2. Security Vulnerabilities & Modern Alternatives

> [!CAUTION]
> **Deserialization Gadget Chains:** Deserializing untrusted data allows attackers to invoke arbitrary methods during object graph reconstruction, leading to Remote Code Execution (RCE).

Modern distributed architectures strictly avoid Java Native Serialization in favor of structured binary/text formats:
- **Protocol Buffers (Protobuf):** Strongly typed, compact, language-agnostic binary format.
- **FlatBuffers:** Zero-copy serialization format allowing direct access to serialized data without parsing overhead.
- **Jackson / Gson:** JSON-based human-readable serialization.
