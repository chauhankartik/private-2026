# Computer Networks — Master Guide & Architecture Reference

A comprehensive, production-grade reference based on Kurose & Ross's ***Computer Networking: A Top-Down Approach*** and Tanenbaum's ***Computer Networks***.

This module organizes the 5 layers of the **TCP/IP Protocol Stack** — **Application Layer**, **Transport Layer**, **Network Data Plane**, **Network Control Plane**, and **Link/Physical Layer** — with production C & Python socket programming code, RFC header specs, and **rich Mermaid diagrams**.

---

## 📂 Layer Index & Roadmap

| Layer / Part | Module / Topic | Core Concepts | Location |
|---|---|---|---|
| **Cheatsheet** | Quick Reference | CIDR Formulas, TCP State Machine, Header Specs, Routing Math | [`00_networking_cheatsheet.md`](./00_networking_cheatsheet.md) |
| **Layer 5** | 01. Application Layer | HTTP/1.1 vs HTTP/2 vs HTTP/3 QUIC, DNS Resolution, Socket API (C/Python) | [`01_application_layer/`](./01_application_layer/01_http_dns_and_socket_programming.md) |
| **Layer 4** | 02. Transport Layer | UDP, RDT 3.0, TCP Handshake/Teardown, Flow Control (`rwnd`), Congestion Control | [`02_transport_layer/`](./02_transport_layer/02_tcp_udp_flow_and_congestion_control.md) |
| **Layer 3 (Data)** | 03. Network Data Plane | Router Architecture, LPM Trie, IPv4/IPv6, CIDR Subnetting, NAT, DHCP, ICMP | [`03_network_layer_data_plane/`](./03_network_layer_data_plane/03_ip_addressing_subnetting_and_nat.md) |
| **Layer 3 (Control)**| 04. Network Control Plane | Dijkstra Link-State, Bellman-Ford Distance-Vector, OSPF, BGP Path Vector, SDN | [`04_network_layer_control_plane/`](./04_network_layer_control_plane/04_routing_algorithms_ospf_and_bgp.md) |
| **Layers 1-2** | 05. Link & Physical Layer | CRC Math, CSMA/CD Ethernet, CSMA/CA WiFi 802.11, ARP, L2 Switches, VLANs | [`05_link_and_physical_layer/`](./05_link_and_physical_layer/05_ethernet_switches_wifi_and_arp.md) |

---

## 🎨 Visual Overview: TCP/IP Protocol Stack & Encapsulation Pipeline

```mermaid
flowchart TD
    subgraph Host A (Sender)
        L5_A["5. Application Layer (HTTP / DNS Data)"] -->|Add HTTP Header| PDU5["Data Stream"]
        PDU5 --> L4_A["4. Transport Layer (TCP / UDP)"]
        L4_A -->|Add TCP Header (Ports, Seq#)| PDU4["Segment"]
        PDU4 --> L3_A["3. Network Layer (IP)"]
        L3_A -->|Add IP Header (Src IP, Dst IP)| PDU3["Packet / Datagram"]
        PDU3 --> L2_A["2. Link Layer (Ethernet)"]
        L2_A -->|Add MAC Header & CRC Trailer| PDU2["Frame"]
        PDU2 --> L1_A["1. Physical Layer (Bits / Waves)"]
    end

    L1_A -. Physical Wire / Fiber / Waves .-> L1_B

    subgraph Host B (Receiver)
        L1_B["1. Physical Layer"] --> L2_B["2. Link Layer (Strip MAC & Verify CRC)"]
        L2_B --> L3_B["3. Network Layer (Strip IP & Match Route)"]
        L3_B --> L4_B["4. Transport Layer (Strip TCP & Reassemble)"]
        L4_B --> L5_B["5. Application Layer (Process HTTP Payload)"]
    end
```
