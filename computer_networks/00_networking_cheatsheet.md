# Computer Networks 30-Second Interview Cheatsheet

---

## ⚡ Core Networking Equations & Quick Reference

### 1. CIDR Subnetting Formulas
$$\text{Total Addresses} = 2^{32 - \text{Prefix Length}}$$
$$\text{Usable Host Addresses} = 2^{32 - \text{Prefix Length}} - 2 \quad \text{(Excluding Network ID and Broadcast ID)}$$

#### Subnet Mask Quick Reference Table

| Subnet Prefix | Netmask | Total IP Addresses | Usable Host IPs |
|---|---|---|---|
| `/24` | `255.255.255.0` | 256 | 254 |
| `/25` | `255.255.255.128` | 128 | 126 |
| `/26` | `255.255.255.192` | 64 | 62 |
| `/27` | `255.255.255.224` | 32 | 30 |
| `/28` | `255.255.255.240` | 16 | 14 |
| `/30` | `255.255.255.252` | 4 | 2 (Point-to-Point Link) |
| `/32` | `255.255.255.255` | 1 | 1 (Single Host Loopback) |

---

## 📊 TCP 3-Way Handshake & Teardown State Diagram

```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> LISTEN : Passive Open (Server)
    CLOSED --> SYN_SENT : Active Open (Client sends SYN, Seq=x)
    LISTEN --> SYN_RCVD : Receives SYN, sends SYN+ACK (Seq=y, Ack=x+1)
    SYN_SENT --> ESTABLISHED : Receives SYN+ACK, sends ACK (Ack=y+1)
    SYN_RCVD --> ESTABLISHED : Receives ACK (Ack=y+1)

    ESTABLISHED --> FIN_WAIT_1 : Active Close (Sends FIN)
    FIN_WAIT_1 --> FIN_WAIT_2 : Receives ACK for FIN
    FIN_WAIT_2 --> TIME_WAIT : Receives FIN, sends ACK
    TIME_WAIT --> CLOSED : 2MSL Timer Expires (60s)
```

---

## 🧠 Transport Layer Congestion Control Summary

$$\text{Effective Window Size} = \min(\text{Congestion Window } cwnd, \text{Receive Window } rwnd)$$

- **Slow Start**: $cwnd$ doubles every RTT ($cwnd = cwnd \times 2$) until $cwnd \ge ssthresh$.
- **Congestion Avoidance (AIMD)**: Additive Increase ($cwnd = cwnd + 1$ per RTT).
- **Multiplicative Decrease**: On packet loss:
  - **Triple Duplicate ACK (Fast Retransmit)**: Set $ssthresh = cwnd / 2$, $cwnd = ssthresh + 3$ (TCP Reno).
  - **Timeout Event**: Set $ssthresh = cwnd / 2$, $cwnd = 1$ MSS (TCP Tahoe).
