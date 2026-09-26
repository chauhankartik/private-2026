# Chapter 3: Docker Networking Internals — Deep Dive Notes

> **Core Theme:** How Docker connects isolated container Network Namespaces using **Virtual Ethernet Pairs (`veth`)**, **Linux Bridges (`docker0`)**, and **`iptables` NAT Rules**.

---

## 1. Five Docker Network Drivers

| Driver | Description | Isolation Level | Use Case |
| :--- | :--- | :--- | :--- |
| **`bridge`** | Default driver. Virtual bridge on single host with NAT. | High | Standard web services on single host |
| **`host`** | Removes network isolation. Container shares host IP & ports directly. | None | Ultra-high throughput microservices |
| **`overlay`** | VXLAN tunnel connecting containers across multiple hosts. | Medium | Multi-host Swarm / Kubernetes clusters |
| **`macvlan`** | Assigns MAC address directly from physical host network switch. | Medium | Legacy apps requiring direct subnet IP |
| **`none`** | Disables all networking (Only `lo` loopback interface). | Total | Secure offline batch processing |

---

## 2. Bridge Networking Deep Dive (`veth` Pairs & `docker0`)

When a container starts on a `bridge` network:

```
Host Network Namespace                          Container Network Namespace
┌──────────────────────────────────────┐        ┌─────────────────────────┐
│ docker0 Bridge (172.17.0.1/16)       │        │ eth0 (172.17.0.2/16)    │
│   │                                  │        │   │                     │
│   └── vethA1B2C3 (Virtual Interface) ┼──veth──┼───┘                     │
└──────────────────────────────────────┘        └─────────────────────────┘
```

1. **`veth` Pair Creation:** Kernel creates a Virtual Ethernet Pair (`veth`) acting as a virtual patch cable with two endpoints.
2. One endpoint is placed inside the host's `docker0` Linux Bridge.
3. The opposite endpoint is moved into the container's isolated **NET Namespace** and renamed `eth0`.
4. Container is assigned an IP (e.g. `172.17.0.2`) by Docker's internal IPAM (IP Address Management).

---

## 3. Port Forwarding & `iptables` NAT Rules

When publishing a port (`docker run -p 8080:80 nginx`):

```bash
# Inspection of Linux iptables NAT PREROUTING Chain:
iptables -t nat -L DOCKER -n
# Target    Prot  Opt  Source       Destination   Ports
# DNAT      tcp   --   0.0.0.0/0    0.0.0.0/0     tcp dpt:8080 to:172.17.0.2:80
```

- Packet entering host on port `8080` hits Linux kernel `iptables` `PREROUTING` chain.
- Kernel executes **Destination NAT (DNAT)**, rewriting target IP/port to container IP `172.17.0.2:80`.

---

## 4. Container DNS Resolution (`127.0.0.11`)

- **Default Bridge (`docker0`):** Does NOT support automatic container name resolution (Must use legacy `--link`).
- **User-Defined Bridge (`docker network create my_net`):** Runs an **Embedded DNS Server** at IP `127.0.0.11`.
  - Queries for service names (e.g. `http://db:5432`) resolve automatically to target container IP.
