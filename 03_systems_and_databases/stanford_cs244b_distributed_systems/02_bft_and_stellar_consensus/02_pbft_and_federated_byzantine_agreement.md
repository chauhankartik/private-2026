# 02. PBFT & Federated Byzantine Agreement (Stellar SCP)

This chapter covers Practical Byzantine Fault Tolerance (PBFT) and the Stellar Consensus Protocol (SCP) / Federated Byzantine Agreement (FBA).

---

## 🛡️ PBFT 3-Phase Message Protocol

Castro and Liskov's **PBFT** protocol achieves consensus in a closed network of $N \ge 3F + 1$ nodes facing up to $F$ Byzantine (arbitrary or malicious) faulty nodes.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client Request
    participant Primary as Primary (Node 0)
    participant R1 as Replica 1
    participant R2 as Replica 2
    participant R3 as Replica 3 (Faulty)

    Client->>Primary: 1. Request (m, timestamp, client_id)
    
    Note over Primary: Phase 1: Pre-Prepare Phase
    Primary->>R1: PRE-PREPARE <v, n, d>, m
    Primary->>R2: PRE-PREPARE <v, n, d>, m
    Primary->>R3: PRE-PREPARE <v, n, d>, m

    Note over R1,R3: Phase 2: Prepare Phase (All broadcast PREPARE messages)
    R1->>Primary: PREPARE <v, n, d, i_1>
    R1->>R2: PREPARE <v, n, d, i_1>
    R2->>Primary: PREPARE <v, n, d, i_2>
    R2->>R1: PREPARE <v, n, d, i_2>
    
    Note over R1,R2: Prepared Certificate Achieved! (2F + 1 matching PREPARE msgs)

    Note over R1,R2: Phase 3: Commit Phase (All broadcast COMMIT messages)
    R1->>Primary: COMMIT <v, n, d, i_1>
    R1->>R2: COMMIT <v, n, d, i_1>
    R2->>Primary: COMMIT <v, n, d, i_2>
    R2->>R1: COMMIT <v, n, d, i_2>

    Note over R1,R2: Committed-Local Certificate Achieved! Execute Command
    R1-->>Client: Reply <v, t, client_id, result_1>
    R2-->>Client: Reply <v, t, client_id, result_2>
```

---

## 🌟 Federated Byzantine Agreement (Stellar Consensus Protocol)

Unlike closed BFT protocols requiring a central authority to define all $N$ participants, **FBA/SCP** allows open membership where every node defines its own **Quorum Slices**.

```mermaid
flowchart TD
    subgraph QuorumSlices ["Stellar Node v1 Quorum Slices"]
        NodeV1["Node v1"]
        SliceA["Quorum Slice A: {v1, Bank_A, Bank_B}"]
        SliceB["Quorum Slice B: {v1, Bank_A, CoreNode_C}"]
    end

    NodeV1 --> SliceA
    NodeV1 --> SliceB
```

> **Quorum Intersection Property**: Two quorum slices intersect if they share at least one honest node, preventing system-wide split-brain partitioning.

---

## 🐹 Production Go Implementation: PBFT 3-Phase State Machine

```go
package pbft

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"sync"
)

type MessageType int

const (
	PrePrepare MessageType = iota
	Prepare
	Commit
)

type PBFTMessage struct {
	Type           MessageType
	View           int
	SequenceNum    int
	Digest         string
	NodeID         int
	RequestPayload string
}

type PBFTNode struct {
	mu             sync.Mutex
	NodeID         int
	F              int // Max faulty nodes tolerated
	N              int // Total nodes (N >= 3F + 1)
	View           int
	sequence       int
	prepareMsgs    map[string]map[int]bool // Digest -> NodeID -> Received
	commitMsgs     map[string]map[int]bool // Digest -> NodeID -> Received
	isPreCommitted map[string]bool
	isCommitted    map[string]bool
}

func NewPBFTNode(nodeID int, f int) *PBFTNode {
	return &PBFTNode{
		NodeID:         nodeID,
		F:              f,
		N:              3*f + 1,
		View:           0,
		prepareMsgs:    make(map[string]map[int]bool),
		commitMsgs:     make(map[string]map[int]bool),
		isPreCommitted: make(map[string]bool),
		isCommitted:    make(map[string]bool),
	}
}

func (n *PBFTNode) CalculateDigest(payload string) string {
	hash := sha256.Sum256([]byte(payload))
	return hex.EncodeToString(hash[:])
}

func (n *PBFTNode) HandlePrePrepare(msg PBFTMessage) *PBFTMessage {
	n.mu.Lock()
	defer n.mu.Unlock()

	expectedDigest := n.CalculateDigest(msg.RequestPayload)
	if msg.Digest != expectedDigest || msg.View != n.View {
		return nil // Reject invalid pre-prepare
	}

	n.isPreCommitted[msg.Digest] = true

	// Generate Prepare message to broadcast
	return &PBFTMessage{
		Type:        Prepare,
		View:        n.View,
		SequenceNum: msg.SequenceNum,
		Digest:      msg.Digest,
		NodeID:      n.NodeID,
	}
}

func (n *PBFTNode) HandlePrepare(msg PBFTMessage) *PBFTMessage {
	n.mu.Lock()
	defer n.mu.Unlock()

	if msg.View != n.View {
		return nil
	}

	if _, exists := n.prepareMsgs[msg.Digest]; !exists {
		n.prepareMsgs[msg.Digest] = make(map[int]bool)
	}
	n.prepareMsgs[msg.Digest][msg.NodeID] = true

	// Check if Prepared Certificate threshold reached: 2F matching PREPARE messages
	if len(n.prepareMsgs[msg.Digest]) >= 2*n.F && n.isPreCommitted[msg.Digest] {
		// Generate Commit message to broadcast
		return &PBFTMessage{
			Type:        Commit,
			View:        n.View,
			SequenceNum: msg.SequenceNum,
			Digest:      msg.Digest,
			NodeID:      n.NodeID,
		}
	}
	return nil
}

func (n *PBFTNode) HandleCommit(msg PBFTMessage) bool {
	n.mu.Lock()
	defer n.mu.Unlock()

	if msg.View != n.View {
		return false
	}

	if _, exists := n.commitMsgs[msg.Digest]; !exists {
		n.commitMsgs[msg.Digest] = make(map[int]bool)
	}
	n.commitMsgs[msg.Digest][msg.NodeID] = true

	// Check if Committed-Local Certificate threshold reached: 2F + 1 matching COMMIT messages
	if len(n.commitMsgs[msg.Digest]) >= 2*n.F+1 && !n.isCommitted[msg.Digest] {
		n.isCommitted[msg.Digest] = true
		fmt.Printf("[PBFT NODE %d] Command with Digest %s COMMITTED and EXECUTED!\n", n.NodeID, msg.Digest[:8])
		return true
	}
	return false
}
```
