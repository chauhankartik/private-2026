# 03. Sharded KV Service, ZooKeeper & Google Spanner

This chapter covers MIT 6.5840 Lab 4 (Sharded Fault-Tolerant Key-Value Storage), Apache ZooKeeper linearizability, and Google Spanner's TrueTime distributed transactions.

---

## 🔀 MIT 6.5840 Lab 4: Sharded KV Architecture

Lab 4 splits key-value storage across multiple Raft replica groups. A centralized **Shard Controller** (also backed by Raft) manages configuration mapping: `ShardID -> ReplicaGroupID`.

```mermaid
sequenceDiagram
    autonumber
    participant Master as Shard Controller (ShardMaster)
    participant Group1 as Raft Group 100 (Primary for Shards 1..5)
    participant Group2 as Raft Group 101 (Primary for Shards 6..10)

    Note over Master: New Replica Group 101 joins cluster (`Join(101)`)
    Master->>Master: Compute New Balanced Config: Group 100 gets 1..5, Group 101 gets 6..10
    
    Note over Group1,Group2: Shard Data Migration Phase
    Group2->>Group1: Fetch Shard State Data for Shards 6..10 (`FetchShardData RPC`)
    Group1-->>Group2: Shard Data Payload (Map[Key]Value + Duplicate Table)
    
    Group2->>Group2: Apply Migrated Shard Data & Enable Servicing Shards 6..10
    Group1->>Group1: Delete Handed-off Shards 6..10 Data
```

---

## 🌐 Google Spanner: TrueTime & 2PC Over Paxos

Spanner provides globally linearizable read-write transactions using **Two-Phase Commit (2PC)** across independent Paxos groups and physical atomic clocks via **TrueTime**.

```mermaid
flowchart TD
    subgraph Client ["Transaction Client"]
        Tx["Read-Write Transaction"]
    end

    subgraph PaxosGroupA ["Paxos Group A (Accounts Shard)"]
        LeaderA["Paxos Leader A (2PC Coordinator)"]
        ReplicaA1["Paxos Replica A1"]
        ReplicaA2["Paxos Replica A2"]
        LeaderA <---> ReplicaA1 & ReplicaA2
    end

    subgraph PaxosGroupB ["Paxos Group B (Inventory Shard)"]
        LeaderB["Paxos Leader B (2PC Participant)"]
        ReplicaB1["Paxos Replica B1"]
        ReplicaB2["Paxos Replica B2"]
        LeaderB <---> ReplicaB1 & ReplicaB2
    end

    subgraph TrueTime ["TrueTime API (GPS + Atomic Clocks)"]
        TT["TT.now() -> [Earliest, Latest] (Uncertainty ε <= 1ms)"]
    end

    Tx -->|Write Account| LeaderA
    Tx -->|Write Inventory| LeaderB
    LeaderA <--->|2PC Prepare / Commit| LeaderB
    LeaderA -.->|Commit Wait Rule| TrueTime
```

### ⏱️ TrueTime Commit Wait Rule
To guarantee that transaction $T_2$ receives a commit timestamp $s_2 > s_1$ if $T_2$ starts after $T_1$ commits:
1. Leader picks commit timestamp $s = \text{TT.now}().\text{latest}$.
2. Leader waits until $\text{TT.now}().\text{earliest} > s$ before releasing client locks.

---

## 🐹 Production Go Implementation: Shard Controller Rebalancer Engine

```go
package shardmaster

import (
	"sort"
)

const NShards = 10

type Config struct {
	Num    int              // Config number
	Shards [NShards]int     // ShardID -> GID
	Groups map[int][]string // GID -> Replica Group Servers
}

/**
 * Rebalances shard assignments evenly across all active Replica Groups (GIDs).
 */
func RebalanceShards(groups map[int][]string, currentShards [NShards]int) [NShards]int {
	var newShards [NShards]int
	gids := make([]int, 0, len(groups))

	for gid := range groups {
		gids = append(gids, gid)
	}

	// Sort GIDs to ensure deterministic rebalancing across all nodes
	sort.Ints(gids)

	if len(gids) == 0 {
		// No active groups: unassign all shards (GID 0)
		return newShards
	}

	// Count current shard distribution per GID
	gidShardCount := make(map[int]int)
	for _, gid := range gids {
		gidShardCount[gid] = 0
	}

	for _, gid := range currentShards {
		if _, exists := gidShardCount[gid]; exists {
			gidShardCount[gid]++
		}
	}

	targetPerGroup := NShards / len(gids)
	remainder := NShards % len(gids)

	newShards = currentShards

	// 1. Unassign shards from removed groups or groups exceeding quota
	for i := 0; i < NShards; i++ {
		gid := newShards[i]
		count, exists := gidShardCount[gid]
		
		if !exists || count > targetPerGroup+1 || (count > targetPerGroup && remainder == 0) {
			newShards[i] = 0 // Unassign
			if exists {
				gidShardCount[gid]--
			}
		}
	}

	// 2. Assign unassigned shards (GID 0) to under-allocated groups
	for i := 0; i < NShards; i++ {
		if newShards[i] == 0 {
			// Find group with minimum assigned shards
			minGid := gids[0]
			minCount := gidShardCount[minGid]

			for _, gid := range gids {
				if gidShardCount[gid] < minCount {
					minGid = gid
					minCount = gidShardCount[gid]
				}
			}

			newShards[i] = minGid
			gidShardCount[minGid]++
		}
	}

	return newShards
}
```
