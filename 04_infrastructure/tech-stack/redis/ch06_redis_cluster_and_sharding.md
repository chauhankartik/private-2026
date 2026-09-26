# Chapter 6: Distributed Scale-Out (Redis Cluster) — Deep Dive Notes

> **Core Theme:** Scaling Redis horizontally across multiple master nodes using **16,384 Hash Slots**, **Gossip Protocols**, and **Smart Client Redirections**.

---

## 1. Hash Slots Topology (16,384 Slots)

Unlike traditional consistent hashing rings, Redis Cluster partitions keyspace into exactly **16,384 Hash Slots**.

$$\text{Hash Slot} = \text{CRC16}(\text{Key}) \pmod{16384}$$

```
                16,384 HASH SLOTS DISTRIBUTION
                
Node A (Master 1) ──► Owns Slots     0 ...  5460  (5,461 slots)
Node B (Master 2) ──► Owns Slots  5461 ... 10922  (5,462 slots)
Node C (Master 3) ──► Owns Slots 10923 ... 16383  (5,461 slots)
```

### Multi-Key Operations & Hash Tags `{...}`:
Redis Cluster restricts multi-key operations (`MGET`, `SUNION`, Transactions) to keys residing in the **same hash slot**.

- **Solution (Hash Tags):** Wrap substring in curly braces `{}`. Redis calculates CRC16 hash ONLY for text inside `{}`.
- *Example:* `{user:100}.profile` and `{user:100}.orders` are guaranteed to map to the exact same hash slot!

---

## 2. Client Routing & Redirections (`MOVED` vs. `ASK`)

Redis Cluster does NOT use a central proxy server by default. Clients connect directly to cluster nodes.

```
Client ─── GET user:100 (Hashes to Slot 7000) ──► Node A (Owns Slots 0..5460)
                                                        │
Client ◄─── -MOVED 7000 192.168.1.50:6379 ──────────────┘
```

### 1. `MOVED` Redirection (Permanent Slot Assignment):
- Returned when a client sends a query for slot $S$ to Node $A$, but slot $S$ permanently belongs to Node $B$.
- **Smart Client Action:** Client updates its local `Slot -> Node` cache and retries query on Node $B$.

### 2. `ASK` Redirection (Temporary Resharding Redirection):
- Returned during active slot migration when slot $S$ is in `MIGRATING` status on Node $A$ and `IMPORTING` status on Node $B$.
- **Smart Client Action:** Client sends `ASKING` command to Node $B$ followed by query, but does NOT update its permanent local slot cache!

---

## 3. Node Communication (Gossip Protocol)

Cluster nodes communicate over a dedicated **Cluster Bus** port (`main_port + 10000`, e.g. 16379) using a binary **Gossip Protocol**.

### Gossip Messages:
- `MEET`: Invites a new node to join cluster.
- `PING` / `PONG`: Heartbeats carrying node status, slot configuration, and replica status.
- `FAIL`: Broadcast when node failure is confirmed by majority masters.
