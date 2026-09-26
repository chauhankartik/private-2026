# Layer 3: Network Layer (Data Plane) — IP, Subnetting & NAT

The **Data Plane** determines how datagrams arriving at router input ports are forwarded to router output ports via **Longest Prefix Match (LPM)**.

---

## 📌 Router Architecture & Longest Prefix Match (LPM)

```mermaid
flowchart TD
    subgraph Router Architecture
        InPort["Input Port (Link Layer + Frame Check)"] --> Lookup["LPM Trie Lookup Engine"]
        Lookup --> SwitchFabric["High-Speed Switching Fabric (Crossbar / Shared Memory)"]
        SwitchFabric --> OutPort["Output Port (Queueing & Buffer Management)"]
    end
```

### Forwarding Table Trie Example
If a router forwarding table has:
- `192.168.0.0/16` $\to$ Interface 1
- `192.168.1.0/24` $\to$ Interface 2
- `192.168.1.128/25` $\to$ Interface 3

A datagram for `192.168.1.135` matches **all three**, but Interface 3 is selected because `/25` is the **Longest Prefix Match**!

---

## 📌 CIDR Subnetting Math & Calculation Walkthrough

Given IP `172.16.45.100/22`:
1. **Subnet Mask**: 22 ones $\to$ `11111111.11111111.11111100.00000000` = `255.255.252.0`.
2. **Network Address**: Bitwise AND of IP and Mask:
   - `45` in binary: `00101101`
   - `252` in binary: `11111100`
   - Bitwise AND: `00101100` = `44`
   - **Network ID**: `172.16.44.0`
3. **Broadcast Address**: Set all host bits (10 bits) to 1:
   - `00101111` = `47`, `11111111` = `255`
   - **Broadcast ID**: `172.16.47.255`
4. **Host Range**: `172.16.44.1` to `172.16.47.254` (1,022 usable hosts).

---

## 📌 Network Address Translation (NAT)

NAT maps private local IP addresses (`10.0.0.0/8`, `192.168.0.0/16`) to a single public WAN IP address using port numbers.

```mermaid
sequenceDiagram
    participant Host as Internal Host (10.0.0.1:3345)
    participant NAT as NAT Router (Public IP: 138.76.29.7)
    participant Server as Web Server (128.119.40.186:80)

    Host->>NAT: Outgoing Packet (Src: 10.0.0.1:3345, Dst: 128.119.40.186:80)
    Note over NAT: Translate Src to Public IP + Unique Port 5001 & Add NAT Table Entry!
    NAT->>Server: Translated Packet (Src: 138.76.29.7:5001, Dst: 128.119.40.186:80)
    
    Server-->>NAT: Reply Packet (Src: 128.119.40.186:80, Dst: 138.76.29.7:5001)
    Note over NAT: Lookup Port 5001 in NAT Table -> Maps to 10.0.0.1:3345!
    NAT-->>Host: Translated Reply (Src: 128.119.40.186:80, Dst: 10.0.0.1:3345)
```
