# Chapter 4: Partitioning & Consistent Hashing (Token Ring & vnodes)

## 1. Consistent Hashing & Murmur3 Partitioner

Cassandra uses consistent hashing to assign data partitions across nodes in a cluster ring topology without central directory lookups.

### Murmur3 Partitioner Token Space
The default partitioner (`Murmur3Partitioner`) maps every partition key to a 64-bit integer token value in the range:

$$\text{Token Range}: [-2^{63}, +2^{63} - 1]$$

$$\text{Token} = \text{Murmur3Hash}(\text{Partition Key})$$

```
                            Node A (Token: -9000000000000000000)
                         /                                        \
                        /                                          \
   Node D (Token: 4500000000000000000)                       Node B (Token: -4500000000000000000)
                        \                                          /
                         \                                        /
                            Node C (Token: 0)
```

* Data routing: A row with partition key token $T$ is assigned to the first node whose assigned token is $\ge T$.

---

## 2. Virtual Nodes (vnodes) Architecture

In early Cassandra versions, each physical server was assigned a single token value. This caused major operational pain during cluster expansion: adding a new node required manually recalculating and streaming token ranges from existing physical nodes.

```
Physical Server 1  ===>  [ vnode 1 (Token X) ][ vnode 17 (Token Y) ][ vnode 42 (Token Z) ]
Physical Server 2  ===>  [ vnode 2 (Token A) ][ vnode 88 (Token B) ][ vnode 99 (Token C) ]
```

### Advantages of vnodes (`num_tokens: 128`):
1. **Automatic Data Distribution:** Each physical server manages multiple small virtual nodes randomly scattered across the token ring.
2. **Fast Rebalancing:** When a new server joins the cluster, it claims small virtual node tokens from *every* existing physical server simultaneously, enabling parallel streaming over all network interfaces.
3. **Hotspot Relief:** Prevents uneven disk utilization caused by single contiguous token assignments.

---

## 3. CQL Primary Keys, Partition Keys & Clustering Columns

Data placement and physical disk ordering in Cassandra are strictly governed by the table schema's **Primary Key** specification.

```sql
CREATE TABLE user_sensor_data (
    user_id uuid,
    sensor_type text,
    reading_timestamp timestamp,
    metric_value double,
    PRIMARY KEY ((user_id, sensor_type), reading_timestamp)
) WITH CLUSTERING ORDER BY (reading_timestamp DESC);
```

### Primary Key Anatomy:
1. **Partition Key (`(user_id, sensor_type)`):**
   * Compound partition key hashed by Murmur3 to determine which cluster node(s) own the row payload.
   * All rows sharing the same partition key reside together on the exact same physical disk partition.
2. **Clustering Columns (`reading_timestamp`):**
   * Determines the physical sorting order of rows *within* a disk partition.
   * Enables high-speed sequential disk reads for range queries targeting a single partition:
     ```sql
     SELECT * FROM user_sensor_data 
     WHERE user_id = e2f... AND sensor_type = 'TEMP' 
       AND reading_timestamp >= '2026-01-01';
     ```
