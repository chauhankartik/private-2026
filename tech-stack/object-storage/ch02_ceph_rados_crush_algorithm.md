# Chapter 2: Ceph RADOS & The CRUSH Algorithm

## 1. Ceph RADOS System Architecture

**Ceph** is an open-source distributed storage system backed by **RADOS** (Reliable Autonomic Distributed Object Store). RADOS manages cluster hardware nodes, data placement, self-healing, and dynamic rebalancing across thousands of storage drives.

```
                               +-----------------------------+
                               | Ceph MON Cluster (Paxos)    |
                               | (Cluster Map Authority)     |
                               +--------------+--------------+
                                              |
                   Pushes updated Cluster Map | (On topology change)
                                              v
 +-----------------------------------------------------------------------------------------+
 | RADOS Gateway (RGW) / Client Application                                               |
 | Computes object location locally using CRUSH(ClusterMap, PlacementGroup, ObjectName)  |
 +--------------+-----------------------------+-----------------------------+--------------+
                |                             |                             |
                | Direct Read/Write           | Direct Read/Write           | Direct Read/Write
                v                             v                             v
       +-----------------+           +-----------------+           +-----------------+
       | OSD 1 (NVMe)    |           | OSD 2 (NVMe)    |           | OSD 3 (NVMe)    |
       | (BlueStore)     |           | (BlueStore)     |           | (BlueStore)     |
       +-----------------+           +-----------------+           +-----------------+
```

### Core Ceph Daemons:
1. **Object Storage Daemons (OSDs):** Storage nodes managing individual physical disk drives. Uses **BlueStore** (a custom C++ storage engine writing directly to raw block devices, bypassing Linux filesystems).
2. **Monitors (MONs):** Maintain master cluster state maps (OSD Map, PG Map, CRUSH Map) using **Paxos consensus**. MONs do *not* serve object data to clients.
3. **Managers (MGRs):** Collect cluster metrics, memory stats, and operational state.

---

## 2. Placement Groups (PGs) & Two-Stage Mapping

To manage billions of objects efficiently without maintaining gigabytes of tracking tables, RADOS introduces **Placement Groups (PGs)**.

```
 Object Name: "user_avatar_101.png"
               |
               v 1. Jenkins Hash & Modulo PG Count
 Placement Group ID: PG 3.1f  (Pool ID 3, PG 0x1f)
               |
               v 2. CRUSH Algorithm Calculation (Zero Directory Lookup)
 Target OSD Array: [ OSD 12 (Primary), OSD 45 (Replica), OSD 89 (Replica) ]
```

### Why Placement Groups?
* Mapping millions of individual objects directly to OSDs would require enormous metadata tables.
* Grouping objects into a fixed number of PGs (e.g., 200 PGs per OSD) bounds memory tracking costs while ensuring even data distribution.

---

## 3. The CRUSH Algorithm (Controlled Replication Under Scalable Hashing)

Traditional distributed systems rely on central lookup tables to track file locations (e.g., HDFS NameNode). In contrast, Ceph uses **CRUSH**, a deterministic pseudo-random placement algorithm executed directly on client nodes.

### CRUSH Inputs & Placement Calculation
When a client reads or writes object $O$ in pool $P$:

$$\text{OSD List} = \text{CRUSH}\left(\text{ClusterMap}, \text{CRUSH\_Rule}, \text{Hash}(O) \pmod{\text{PG\_Count}}\right)$$

Where:
* `ClusterMap`: Hierarchical tree representation of physical topology (Datacenter $\to$ Room $\to$ Rack $\to$ Host $\to$ OSD Drive).
* `CRUSH_Rule`: Rules defining failure domains (e.g., *"Select 3 OSDs located in distinct Racks"*).

```
                            Datacenter Root
                           /               \
                   Rack 1                     Rack 2
                  /      \                   /      \
             Host A      Host B         Host C      Host D
            /      \    /      \       /      \    /      \
          OSD1   OSD2 OSD3   OSD4    OSD5   OSD6 OSD7   OSD8
```

### Key Advantages of CRUSH:
1. **Zero Central Lookup Bottleneck:** Clients compute target disk addresses locally in microseconds without querying central directory servers.
2. **Failure Domain Isolation:** Guarantees replicas are physically separated across independent power supplies, network switches, or server racks.
3. **Minimal Rebalancing Weight Adjustment:** When an OSD drive is added or fails, CRUSH moves only $\frac{1}{N}$ of total cluster data, avoiding massive cluster-wide data shuffles.
