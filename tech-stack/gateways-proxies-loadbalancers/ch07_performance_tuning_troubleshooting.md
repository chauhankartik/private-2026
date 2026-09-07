# Chapter 7: Operational Performance Tuning & Diagnostics

High-throughput edge proxies require Linux kernel socket tuning, file descriptor optimizations, and zero-downtime hot reloading procedures.

---

## 1. Linux Kernel Socket & System Tuning (`sysctl.conf`)

Before a proxy can handle 1,000,000 concurrent connections, the underlying Linux kernel must be tuned to expand socket queues and file descriptor limits.

```ini
# /etc/sysctl.d/99-proxy-tuning.conf

# Max socket listen backlog queue size (Default 128 is too small!)
net.core.somaxconn = 65535

# Max half-open TCP SYN backlog queue
net.ipv4.tcp_max_syn_backlog = 65535

# Ephemeral port range for outbound proxy-to-backend connections
net.ipv4.ip_local_port_range = 1024 65535

# Reuse TIME_WAIT sockets for outbound connections
net.ipv4.tcp_tw_reuse = 1

# Disable Nagle's algorithm (sets TCP_NODELAY, eliminating 40ms buffering delay)
net.ipv4.tcp_low_latency = 1
```

### 1.1 Open File Descriptor Limits (`limits.conf`)
Every socket connection consumes 1 File Descriptor (FD). Set process limits in `/etc/security/limits.conf`:

```text
nobody       soft    nofile          1048576
nobody       hard    nofile          1048576
```

---

## 2. Zero-Downtime Hot Reload Mechanics

How do NGINX and HAProxy reload configuration changes without dropping active TCP connections?

```
[ Master Process ] ──> Receives SIGHUP / reload signal
         |
         ├── 1. Validates new config syntax (nginx -t)
         ├── 2. Spawns NEW Worker Processes (Running new configuration)
         ├── 3. Sends SIGQUIT (Graceful Shutdown) to OLD Worker Processes
         |
         v
[ New Workers ]  ---> Accepts ALL incoming new client connections (SO_REUSEPORT)
[ Old Workers ]  ---> Stops accepting new connections; finishes active requests, then exits!
```

---

## 3. Diagnostic & Troubleshooting Playbook

### 3.1 Common HTTP Proxy Error Codes & Root Causes

```
+---------------------------------------------------------------------------------------+
| Error Diagnostic Guide                                                                |
|                                                                                       |
| HTTP 502 Bad Gateway                                                                  |
|   ├── Cause: Proxy reached backend IP, but backend refused TCP connection or crashed.|
|   └── Fix: Verify backend service process health, port binding, and firewall rules.  |
|                                                                                       |
| HTTP 504 Gateway Timeout                                                              |
|   ├── Cause: Backend accepted connection but failed to respond within proxy timeout.|
|   └── Fix: Check backend database queries or increase `proxy_read_timeout` (NGINX).   |
|                                                                                       |
| HTTP 499 Client Closed Request (NGINX specific)                                       |
|   ├── Cause: Client browser closed TCP connection before NGINX received response.     |
|   └── Fix: Optimize slow backend response latencies.                                  |
+---------------------------------------------------------------------------------------+
```

---

## 4. Staff Engineer Operational Tuning SLA
1. **Always Set `worker_rlimit_nofile`:** In NGINX, configure `worker_rlimit_nofile 1048576;` to ensure worker processes can open enough file descriptors matching kernel settings.
2. **Monitor Proxy Admin Metrics:** Export metrics to Prometheus:
   * NGINX: `nginx_ingress_controller_requests`
   * HAProxy: `haproxy_backend_current_queue`
   * Envoy: `envoy_cluster_upstream_cx_active` & `envoy_cluster_upstream_rq_timeout`
3. **Tune Keep-Alive Idle Timeouts:** Set backend `keepalive` connection pools (`keepalive 64;` in NGINX upstream blocks) to reuse established TCP connections to backend microservices, eliminating continuous TCP handshake overhead.
