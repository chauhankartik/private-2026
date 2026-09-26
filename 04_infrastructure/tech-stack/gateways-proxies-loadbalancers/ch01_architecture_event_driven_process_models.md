# Chapter 1: Process Architecture & Event-Driven Non-Blocking I/O

Modern high-performance proxies handle millions of concurrent connections by replacing thread-per-connection execution models with event-driven, non-blocking I/O event loops.

---

## 1. Process Models: Thread-per-Connection vs Master-Worker Event Loop

```
[ Thread-Per-Connection Model ] (Legacy Apache / Tomcat)
Client 1 ───> Thread 1 (Blocks on I/O) ───\
Client 2 ───> Thread 2 (Blocks on I/O) ────+──> High RAM Footprint & CPU Context-Switching
Client 3 ───> Thread 3 (Blocks on I/O) ───/

===================================================================

[ Master-Worker Event-Driven Model ] (NGINX / Envoy / HAProxy)
Client 1 ──┐
Client 2 ──┼──> [ Master Process ] ──> [ Worker Process (Event Loop) ] ──> epoll/kqueue ($O(1)$)
Client 3 ──┘                                (Single Thread Handles 100,000 Connections!)
```

### 1.1 Comparison

| Dimension | Thread-per-Connection Model | Master-Worker Event-Driven Model |
| :--- | :--- | :--- |
| **Concurrency Mechanism** | Allocates 1 OS Thread per client connection. | **Single Worker Thread handles thousands of active connections**. |
| **Memory Footprint** | Heavy (~1MB to 2MB thread stack per connection). | **Ultra Light** (Kilobytes per connection buffer). |
| **Scaling Limit** | Stalls at ~10,000 connections (The C10K Problem). | Scales easily to **1,000,000+ connections** (The C10M Problem). |
| **CPU Overhead** | High kernel context-switching latency. | Low context switching; high CPU cache locality. |

---

## 2. Kernel I/O Multiplexing Primitives: `epoll`, `kqueue` & `io_uring`

Proxies rely on OS kernel multiplexing primitives to monitor non-blocking file descriptors (`FDs`).

```
[ Select / Poll ]   ---> O(N) Array Scan. Kernel scans ALL 100,000 FDs to find 1 active socket.
[ epoll / kqueue ]  ---> O(1) Event Notification. Kernel returns ONLY active ready sockets via Ready List.
[ io_uring ]       ---> Asynchronous Ring Buffer (SQ/CQ). Zero syscall overhead via shared memory rings.
```

### 2.1 Mechanical Evolution

| Primitive | OS Support | Complexity | Mechanism |
| :--- | :--- | :--- | :--- |
| **`select(2)`** | POSIX Universal | $O(N)$ | Linear array scan; restricted to 1,024 file descriptors (`FD_SETSIZE`). |
| **`poll(2)`** | POSIX Universal | $O(N)$ | Array scan without fixed 1,024 descriptor limit. |
| **`epoll(7)`** | Linux Kernel 2.6+ | **$O(1)$** | Kernel uses Red-Black tree + Ready List. Notifies user-space only of active FDs. |
| **`kqueue(2)`** | BSD / macOS | **$O(1)$** | Kernel event notification filter mechanism equivalent to `epoll`. |
| **`io_uring`** | Linux 5.1+ | **$O(1)$ Syscall-less**| Submission & Completion Ring Buffers shared between User Space & Kernel RAM. |

---

## 3. `SO_REUSEPORT` Kernel Socket Load Distribution

In classic multi-worker setups, a single master socket accepts connections, or workers compete on a shared lock (`accept_mutex`), causing lock contention.

```
Without SO_REUSEPORT:
Incoming TCP SYN ---> [ Shared Master Socket ] ---> Lock Contention across Workers!

With SO_REUSEPORT:
Incoming TCP SYN ---> [ Linux Kernel Hash ] ──┬──> Worker 1 Socket Queue
                                             ├──> Worker 2 Socket Queue
                                             └──> Worker 3 Socket Queue
```

* **`SO_REUSEPORT` Socket Option:** Enables multiple worker processes to bind to the exact same IP and Port (`0.0.0.0:80`).
* **Kernel Balancing:** The Linux kernel hashes incoming 4-tuple TCP `SYN` packets (`src_ip`, `src_port`, `dst_ip`, `dst_port`) and distributes new connections directly into individual worker socket queues, completely eliminating inter-worker lock contention.

---

## 4. Staff Engineer I/O Architecture Rules
1. **Set Worker Processes to CPU Core Count:** In NGINX, set `worker_processes auto;`. Match worker counts to physical CPU cores to maximize CPU cache locality and eliminate worker context-switching.
2. **Enable `SO_REUSEPORT` for High-Volume Ingress:** Add `reuseport` to your NGINX `listen 80 reuseport;` or HAProxy configuration to allow the Linux kernel to distribute connection accept load evenly across workers.
3. **Use Edge-Triggered `epoll` Mode (`EPOLLET`):** Ensure custom proxy event loops use edge-triggered `epoll` notifications to receive events only when socket state changes, minimizing redundant read notifications.
