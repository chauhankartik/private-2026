# Layer 3: Network Layer (Control Plane) — Routing Algorithms, OSPF & BGP

The **Control Plane** determines the end-to-end paths taken by packets from source to destination across network routers.

---

## 📌 Link-State (Dijkstra) vs Distance-Vector (Bellman-Ford)

```mermaid
flowchart TD
    subgraph Link-State (OSPF)
        LS1["Every router broadcasts link costs to ALL routers (Link State Advertisements / LSAs)"]
        LS2["Every router builds full global network topology graph"]
        LS3["Runs Dijkstra's Algorithm locally to calculate shortest paths"]
    end

    subgraph Distance-Vector (RIP / EIGRP)
        DV1["Every router exchanges distance vectors ONLY with immediate neighbors"]
        DV2["Iterative Bellman-Ford updates: d_x(y) = min_v { c(x,v) + d_v(y) }"]
        DV3["Subject to Count-to-Infinity problem (Fixed via Poison Reverse)"]
    end
```

---

## 📌 Dijkstra's Shortest Path Algorithm (Link-State)

Given a network graph with node link costs $c(x,y)$:

```python
import heapq

def dijkstra(graph, start_node):
    # distances dictionary with infinity as default
    distances = {node: float('inf') for node in graph}
    distances[start_node] = 0
    priority_queue = [(0, start_node)]

    while priority_queue:
        current_distance, current_node = heapq.heappop(priority_queue)

        if current_distance > distances[current_node]:
            continue

        for neighbor, weight in graph[current_node].items():
            distance = current_distance + weight
            if distance < distances[neighbor]:
                distances[neighbor] = distance
                heapq.heappush(priority_queue, (distance, neighbor))

    return distances
```

---

## 📌 BGP (Border Gateway Protocol) & Inter-AS Routing

**BGP** is the de facto routing protocol of the global Internet, coordinating paths across Autonomous Systems (ASes).

```mermaid
flowchart LR
    subgraph AS 100 (ISP A)
        R1["Router 1a"] <--> R2["eBGP Router 1b"]
    end

    subgraph AS 200 (ISP B)
        R3["eBGP Router 2a"] <--> R4["Router 2b"]
    end

    R2 <== eBGP Session ===> R3
    
    note["BGP advertises prefixes with attributes: \n- AS-PATH: [AS200, AS100]\n- NEXT-HOP: 192.0.2.1\n- Policy: Route preference based on peering contracts"]
```
