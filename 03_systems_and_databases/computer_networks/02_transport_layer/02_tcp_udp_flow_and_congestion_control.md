# Layer 4: Transport Layer — TCP, UDP, Flow & Congestion Control

The **Transport Layer** provides logical end-to-end communication between processes running on different hosts.

---

## 📌 TCP vs UDP Header Comparison

### TCP Segment Header (20 Bytes Overhead)
```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          Source Port          |       Destination Port        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                        Sequence Number                        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     Acknowledgment Number                     |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| Data |Reserved|N|C|E|U|A|P|R|S|F|            Window             |
| Offset|        |S|W|C|R|C|S|S|Y|I|            (rwnd)             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|           Checksum            |         Urgent Pointer        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

---

## 📌 Reliable Data Transfer (RDT 3.0) & Sliding Window

To handle lossy networks, TCP uses **Sequence Numbers**, **Cumulative ACKs**, and **Retransmission Timers**.

```mermaid
sequenceDiagram
    participant Sender as TCP Sender
    participant Receiver as TCP Receiver

    Sender->>Receiver: Segment 1 (Seq=100, Len=100)
    Sender->>Receiver: Segment 2 (Seq=200, Len=100) -> LOST IN NETWORK!
    Receiver-->>Sender: ACK 200 (Expecting Seq=200)
    
    Sender->>Receiver: Segment 3 (Seq=300, Len=100)
    Receiver-->>Sender: ACK 200 (Duplicate ACK 1)
    
    Sender->>Receiver: Segment 4 (Seq=400, Len=100)
    Receiver-->>Sender: ACK 200 (Duplicate ACK 2)
    
    Sender->>Receiver: Segment 5 (Seq=500, Len=100)
    Receiver-->>Sender: ACK 200 (Duplicate ACK 3 -> FAST RETRANSMIT TRIPPED!)
    
    Note over Sender: 3 Duplicate ACKs received! Retransmit Segment 2 immediately!
    Sender->>Receiver: Fast Retransmit: Segment 2 (Seq=200, Len=100)
    Receiver-->>Sender: Cumulative ACK 600 (Everything up to 600 received!)
```

---

## 📌 TCP Congestion Control Dynamics (AIMD, Tahoe, Reno)

TCP dynamically adjusts its **Congestion Window ($cwnd$)** to prevent network buffer overflow.

```mermaid
graph TD
    Start["New Connection: Slow Start Phase (cwnd = 1 MSS)"] --> Double["cwnd doubles every RTT (Exponential Growth)"]
    Double --> CheckThresh{"Does cwnd >= ssthresh?"}
    
    CheckThresh -- Yes --> CongAvoid["Congestion Avoidance (Linear Growth: cwnd = cwnd + 1 per RTT)"]
    
    CongAvoid --> LossEvent{"Loss Event Occurs"}
    Double --> LossEvent
    
    LossEvent -- Timeout --> Tahoe["TCP Tahoe: ssthresh = cwnd/2, cwnd = 1 MSS (Reset to Slow Start)"]
    LossEvent -- 3 Duplicate ACKs --> Reno["TCP Reno: ssthresh = cwnd/2, cwnd = ssthresh + 3 (Fast Recovery)"]
    
    Tahoe --> Start
    Reno --> CongAvoid
```
