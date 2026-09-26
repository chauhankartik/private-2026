# Stanford CS244B — Advanced Distributed Systems

A masterclass on advanced distributed systems based on Stanford University's graduate-level CS244B course taught by Professor David Mazières and Professor Philip Levis. Covers Peer-to-Peer Distributed Hash Tables (Chord, Kademlia), Practical Byzantine Fault Tolerance (PBFT), Federated Byzantine Agreement (Stellar Consensus Protocol), Total Order Atomic Broadcast, and Shamir's Secret Sharing.

---

## 📐 Table of Contents

| Section | Subject | Key Concepts |
| :--- | :--- | :--- |
| **00. Cheatsheet** | [Stanford CS244B Cheatsheet](00_cs244b_cheatsheet.md) | Theoretical Foundations Matrix, FLP Impossibility Proof, Consensus Quorum Math ($2F+1$ vs $3F+1$), Cryptographic Primitives |
| **01. P2P DHTs & Consistent Hashing** | [Chord DHT & Kademlia](01_p2p_dhts_and_consistent_hashing/01_chord_dht_and_kademlia.md) | Chord $O(\log N)$ Finger Tables, successor/predecessor stabilization, Kademlia XOR distance metric, BitTorrent DHT |
| **02. BFT & Stellar Consensus** | [PBFT & Federated Byzantine Agreement](02_bft_and_stellar_consensus/02_pbft_and_federated_byzantine_agreement.md) | Castro & Liskov PBFT (Pre-Prepare, Prepare, Commit, View Change), Stellar Consensus Protocol (SCP / FBA Quorum Slices) |
| **03. Atomic Broadcast & Secret Sharing** | [Atomic Broadcast & Secret Sharing](03_atomic_broadcast_and_secret_sharing/03_total_order_broadcast_and_shamir_secret_sharing.md) | Total Order / Atomic Broadcast equivalence to Consensus, Shamir $(k, n)$ Secret Sharing polynomial interpolation over $GF(p)$ |

---

## 🌐 Stanford CS244B System Architecture & Consensus Topology

```mermaid
flowchart TD
    subgraph P2PMesh ["Peer-to-Peer Overlay Network (Chord DHT)"]
        Node0["Node 0 (Finger Table: [1, 2, 4, 8])"]
        Node4["Node 4 (Finger Table: [5, 6, 8, 12])"]
        Node8["Node 8 (Finger Table: [9, 10, 12, 0])"]
        Node0 <--->|Finger Routing $O(\log N)$| Node4 <---> Node8 <---> Node0
    end

    subgraph BFTMesh ["Byzantine Fault Tolerant Mesh (PBFT / Stellar SCP)"]
        LeaderPBFT["Primary Node (Pre-Prepare)"]
        Replica1["Replica 1 (Prepare / Commit)"]
        Replica2["Replica 2 (Prepare / Commit)"]
        Replica3["Replica 3 (Prepare / Commit)"]
        
        LeaderPBFT <---> Replica1 & Replica2 & Replica3
    end

    subgraph CryptoTier ["Threshold Cryptography Tier"]
        SecretShare["Shamir $(k, n)$ Secret Sharing"]
        Reconstruct["Lagrange Interpolation Core"]
        SecretShare --> Reconstruct
    end

    P2PMesh --> BFTMesh --> CryptoTier
```

---

## 🎯 Core Theoretical Principles of Stanford CS244B

1. **FLP Impossibility Result (Fischer, Lynch, Paterson 1985)**: In an asynchronous network, no deterministic consensus protocol can guarantee both Safety and Liveness in the presence of even a single unannounced crash fault.
2. **Byzantine Fault Quorum Bound ($N \ge 3F + 1$)**: To tolerate up to $F$ Byzantine (arbitrary or malicious) failing nodes, a system must contain at least $3F + 1$ total nodes, and quorums must be of size $2F + 1$.
3. **Equivalence of Atomic Broadcast and Consensus**: A system that implements Total Order Atomic Broadcast can implement Distributed Consensus, and vice versa.
4. **Federated Byzantine Agreement (Open Membership)**: Unlike closed BFT systems where all nodes are statically known, SCP allows individual nodes to choose their own **Quorum Slices**, forming global consensus through slice overlap.
