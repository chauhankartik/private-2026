# Layers 1-2: Link & Physical Layer — Ethernet, WiFi, ARP & Switches

The **Link Layer** moves frames across individual physical links connecting neighboring network nodes.

---

## 📌 Multiple Access Protocols: CSMA/CD vs CSMA/CA

### 1. CSMA/CD (Carrier Sense Multiple Access / Collision Detection — Wired Ethernet)
- **Carrier Sense**: Listen before transmitting.
- **Collision Detection**: If two hosts transmit simultaneously, detect voltage spike, stop immediately, send jamming signal, and wait using **Binary Exponential Backoff** ($K \times 512$ bit times).

```mermaid
sequenceDiagram
    participant HostA as Host A
    participant Channel as Ethernet Cable
    participant HostB as Host B

    HostA->>Channel: Transmit Frame
    HostB->>Channel: Transmit Frame simultaneously
    Note over Channel: COLLISION DETECTED! (Voltage Spike)
    HostA->>Channel: Send Jamming Signal & Abort
    HostB->>Channel: Send Jamming Signal & Abort
    HostA->>HostA: Exponential Backoff: Wait K * 512 bit times
    HostB->>HostB: Exponential Backoff: Wait K * 512 bit times
```

### 2. CSMA/CA (Collision Avoidance — 802.11 Wireless WiFi)
Wireless signals cannot detect collisions while transmitting (transmitter drowns out incoming signals). Uses **RTS/CTS (Request to Send / Clear to Send)** reservation frames.

---

## 📌 Address Resolution Protocol (ARP)

ARP translates IP addresses (Layer 3) to MAC addresses (Layer 2).

```mermaid
sequenceDiagram
    participant HostA as Host A (IP: 10.0.0.1, MAC: AA:AA)
    participant Switch as Layer 2 Switch
    participant HostB as Host B (IP: 10.0.0.2, MAC: BB:BB)

    HostA->>Switch: Broadcast ARP Request: "Who has 10.0.0.2? Tell AA:AA" (Dst MAC: FF:FF:FF:FF:FF:FF)
    Switch->>HostB: Floods Broadcast ARP Request to all ports
    HostB->>HostB: Recognizes its own IP! Updates ARP Table
    HostB->>Switch: Unicast ARP Reply: "10.0.0.2 is at BB:BB" (Dst MAC: AA:AA)
    Switch->>HostA: Forwards Unicast ARP Reply
    HostA->>HostA: Caches (10.0.0.2 -> BB:BB) in ARP Table!
```

---

## 📌 CRC Error Detection (Polynomial Long Division)

Given Data $D = 101001$ and Generator Polynomial $G = 1101$ ($r = 3$ bits):
1. Append $r=3$ zeros to $D$: $D' = 101001000$.
2. Divide $D'$ by $G$ using modulo-2 arithmetic (XOR subtraction):
   - $101001000 \oplus 1101 = \dots \implies \text{Remainder } R = 011$.
3. Transmitted Frame = $D \cdot 2^r \oplus R = 101001011$.
4. Receiver divides Frame by $G$. If remainder is $000$, no bit errors occurred!
