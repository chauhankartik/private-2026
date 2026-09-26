# Part IV: Query Processing & Cost-Based Optimization

Query processing translates SQL queries into efficient physical execution plans, evaluating joins and sort operations using minimal I/O.

---

## 📌 Volcano / Iterator Execution Model

The **Volcano Model** (Iterator Model) evaluates physical query plans tuple-at-a-time via `open()`, `next()`, and `close()`.

```mermaid
sequenceDiagram
    participant Client as Client Output
    participant Project as ProjectIterator
    participant Filter as FilterIterator (salary > 100k)
    participant Scan as IndexScanIterator (Instructor)

    Client->>Project: open()
    Project->>Filter: open()
    Filter->>Scan: open()
    
    loop Tuple Stream Pipeline
        Client->>Project: next()
        Project->>Filter: next()
        Filter->>Scan: next() -> Fetches Tuple
        Scan-->>Filter: Returns Tuple (salary=120k)
        Filter-->>Project: Passes Filter (salary > 100k)
        Project-->>Client: Returns Projected Tuple (name="Alice")
    end

    Client->>Project: close()
    Project->>Filter: close()
    Filter->>Scan: close()
```

---

## 📌 Join Algorithms Deep Dive

### 1. Grace Hash Join
Ideal for large equijoins ($R \bowtie_{R.id = S.id} S$) when neither relation fits in memory.

```mermaid
flowchart TD
    subgraph Phase 1: Partitioning Phase
        R["Outer Relation R"] --> Hash1["Hash Function h1(JoinKey)"]
        S["Inner Relation S"] --> Hash1
        
        Hash1 --> PartR["Partition R into R_0..R_k on Disk"]
        Hash1 --> PartS["Partition S into S_0..S_k on Disk"]
    end

    subgraph Phase 2: Build & Probe Phase
        PartR --> Build["Build In-Memory Hashtable for R_i using h2"]
        PartS --> Probe["Probe Hashtable with S_i using h2"]
        Probe --> Match["Output Matched Tuples"]
    end
```

### 2. External Merge Sort
Used when sorting relations larger than available RAM.
- **Pass 0**: Read $B$ pages into RAM, sort in-memory, write out $\lceil N/B \rceil$ sorted runs to disk.
- **Pass 1..N**: Perform $(B-1)$-way merge of sorted runs until 1 single sorted run remains.

---

## 📌 System R Cost-Based Dynamic Programming Optimizer

The Query Optimizer converts a Logical Query Plan into the cheapest Physical Plan using dynamic programming (Selinger Optimizer):

1. **Single-Relation Access Paths**: Determine cost of Sequential Scan vs Index Scan for every relation.
2. **2-Way Joins**: Evaluate all pairs of relations using Nested Loop, Hash Join, and Sort-Merge Join.
3. **K-Way Joins**: Build left-deep processing trees using dynamic programming:
   $$\text{Cost}(R_1 \bowtie R_2 \bowtie R_3) = \min_{S \subset \{1,2,3\}} \left( \text{Cost}(S) + \text{Cost}(\text{Join}(S, \{1,2,3\} \setminus S)) \right)$$
