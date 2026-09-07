# Chapter 4: Kubernetes Networking Model, CNI & Services

Kubernetes imposes a strict networking model across all clusters, abstracting physical network topologies into a flat, routable Pod network.

---

## 1. The 4 Fundamental Kubernetes Networking Rules

1. **Pod-to-Pod Communication:** Every Pod gets its own unique, routable IP address. All Pods can communicate with all other Pods across nodes without Network Address Translation (NAT).
2. **Node-to-Pod Communication:** All nodes can communicate directly with all Pods on any node without NAT.
3. **Self-Identity Rule:** The IP address a Pod sees as its own is identical to the IP address all other Pods see it as.
4. **Service Abstraction:** Services provide stable Virtual IPs (`ClusterIP`) and DNS names that load balance traffic across dynamically changing Pod IPs.

---

## 2. Container Network Interface (CNI) Architecture

The **CNI** specification standardizes how container runtimes invoke external network plugins to attach container network namespaces to the host network.

```
kubelet ---> CRI (containerd)
                 |
                 v Invokes CNI Plugin binary (/opt/cni/bin/calico)
           [ CNI Plugin ]
                 |
                 ├── 1. Creates veth pair (eth0 in Pod netns <---> vethXXX in Host)
                 ├── 2. Assigns IP address via IPAM (IP Address Management)
                 └── 3. Configures Host Routing / Overlay Encapsulation
```

### 2.1 CNI Execution Flow
When a Pod is scheduled:
1. `containerd` creates a isolated Linux network namespace (`netns`).
2. `containerd` invokes the CNI plugin passing commands (`ADD`, `DEL`, `CHECK`) and JSON configuration via `stdin`.
3. The CNI plugin creates a Virtual Ethernet pair (`veth` pair), moves one end into the Pod namespace as `eth0`, and attaches the other end to the host network bridge or eBPF hook.

---

## 3. CNI Plugin Architectures: Calico vs Cilium

```
+-------------------------------------------------------------------+
| Calico Networking (BGP / VXLAN)                                   |
| - Uses Linux Routing Table + BGP Daemon (Bird) to peer host routes|
| - Optional Overlay: VXLAN or IP-in-IP encapsulation               |
| - Data Plane: iptables / IP set for NetworkPolicies               |
+-------------------------------------------------------------------+

+-------------------------------------------------------------------+
| Cilium eBPF Networking (Kernel Bypass)                            |
| - Bypasses netfilter / iptables entirely using Linux eBPF        |
| - Direct Socket Routing (sock_ops) & TC (Traffic Control) BPF     |
| - Identity-Based Security: Assigns numeric security IDs to Pods   |
+-------------------------------------------------------------------+
```

### 3.1 Calico Architecture
* Uses **BGP (Border Gateway Protocol)** to turn every Kubernetes worker node into a virtual router, advertising Pod IP subnets to adjacent nodes without encapsulation overhead.
* Supports **VXLAN** / **IP-in-IP** overlay mode for networks where underlying routers block BGP peering.

### 3.2 Cilium eBPF Architecture
* Uses **eBPF (Extended Berkeley Packet Filter)** bytecode compiled and loaded dynamically into the Linux kernel.
* **Socket-Layer Enforcement (`sock_ops`):** Short-circuits TCP socket buffers between co-located Pods, completely bypassing the TCP/IP stack for 2x throughput gains.
* **Layer 7 Policy:** Enforces identity-aware policies directly on HTTP headers, gRPC methods, and Kafka topics.

---

## 4. Kubernetes Service Abstraction & Data Planes

Services expose a set of Pods behind a stable Virtual IP (`ClusterIP`).

```
Client Pod ---> Request to Service ClusterIP (10.96.0.10:80)
                       |
                       v Intercepted at Host Kernel
         +-------------------------------------------+
         | Service Data Plane Implementation         |
         | - iptables: PREROUTING / KUBE-SERVICES    |
         | - IPVS: Hash Table Virtual Server          |
         | - Cilium: eBPF BPF_MAP_TYPE_HASH lookup   |
         +-------------------------------------------+
                       |
                       v Rewrites Destination IP (DNAT)
                 Target Pod IP (10.244.1.45:8080)
```

### 4.1 Comparison of Service Data Planes

| Dimension | `iptables` Mode | `IPVS` Mode | Cilium eBPF Mode |
| :--- | :--- | :--- | :--- |
| **Lookup Complexity** | $O(N)$ Sequential Rule Scan | $O(1)$ Hash Table Lookup | $O(1)$ eBPF Map Lookup |
| **Rule Scaling Threshold** | Degrades past ~20,000 rules | Scales to 100,000+ Services | Scales to 100,000+ Services |
| **Load Balancing** | Random probability distribution | Round Robin, Least Conn, Hash | Socket-level eBPF balancing |
| **Kernel Overhead** | Heavy `netfilter` traversal | Moderate `netfilter` traversal | **Zero `netfilter` / Direct Route** |

---

## 5. Ingress vs Gateway API

### 5.1 Ingress API (Legacy)
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
# Monolithic API definition mixing routing rules, TLS, and controller annotations
```

### 5.2 Gateway API (Modern Successor - KEP-2591)
Splits responsibilities into role-oriented resources:

```
[ Infrastructure Provider ] ===> GatewayClass (Defines LB controller impl)
                                      |
[ Cluster Administrator ]   ===> Gateway (Allocates Virtual IP & Ports)
                                      |
[ Application Developer ]   ===> HTTPRoute / GPRCRoute (Defines paths, splits & headers)
```

---

## 6. Staff Engineer Networking Tuning Checklist
1. **Migrate to eBPF (Cilium):** Replace `kube-proxy` with Cilium eBPF mode on large clusters (`> 500` nodes) to eliminate `iptables` lock contention and memory footprint.
2. **Prevent Pod CIDR Exhaustion:** Size your node Pod CIDR mask carefully (e.g., `/24` allows 254 Pod IPs per node).
3. **Adopt Gateway API:** Migrate legacy Ingress resources to Gateway API (`HTTPRoute`) to enable canary traffic splitting (e.g., 90% v1 / 10% v2) without vendor-specific annotations.
