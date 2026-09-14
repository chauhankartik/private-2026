# UC Berkeley CS267: 30-Second Parallel Computing Cheatsheet

This cheatsheet aggregates core parallel scaling metrics, communication complexity bounds, MPI collectives, sparse matrix formulas, and octree partitioning rules.

---

## 1. Speedup, Efficiency & Scaling Laws

### Speedup ($S_P$) and Efficiency ($E_P$)
$$S_P = \frac{T_1}{T_P}, \quad E_P = \frac{S_P}{P} = \frac{T_1}{P \cdot T_P}$$

### Strong Scaling vs Weak Scaling
* **Strong Scaling**: Problem size remains fixed while processor count $P$ increases ($T_P$ decreases).
* **Weak Scaling**: Problem size scales linearly with processor count $P$ ($T_P$ remains constant).

---

## 2. Communication Complexity Lower Bounds (Demmel Bounds)

For dense matrix multiplication ($N \times N$ matrices) on $P$ processors with fast local memory capacity $M$:
* **Minimum Words Transferred**: 
  $$W = \Omega\left( \frac{N^3}{P \sqrt{M}} \right)$$
* **Minimum Messages Transferred (Latencies)**: 
  $$L = \Omega\left( \frac{N^3}{P M^{3/2}} \right)$$

---

## 3. MPI Collectives Time Complexity Matrix ($P$ Processors, $m$ Bytes)

| Collective Primitives | Tree Algorithm Latency | Ring Algorithm Bandwidth | Total Complexity |
| :--- | :--- | :--- | :--- |
| **`MPI_Bcast`** | $O(\alpha \log P)$ | $O(\beta m)$ | $O(\alpha \log P + \beta m)$ |
| **`MPI_Reduce` / `MPI_Allreduce`** | $O((\alpha + \beta m) \log P)$ | $O(\alpha P + \beta m)$ | $O(\alpha \log P + \beta m)$ |
| **`MPI_Alltoall`** | $O(\alpha \log P)$ | $O(\beta m P)$ | $O(\alpha P + \beta m P)$ |

*Note:* $\alpha = \text{latency (seconds per message)}$, $\beta = \text{inverse bandwidth (seconds per byte)}$.

---

## 4. Sparse Matrix Compressed Row Storage (CSR) Math

For a sparse matrix $A$ of size $M \times N$ with $NNZ$ non-zero elements:

### Memory Footprint
$$\text{Storage (bytes)} = NNZ \times \text{sizeof(val)} + NNZ \times \text{sizeof(col\_ind)} + (M + 1) \times \text{sizeof(row\_ptr)}$$

### Floating Point Intensity (FLOPs / Byte)
$$\text{Arithmetic Intensity} = \frac{2 \times NNZ}{12 \times NNZ + 4 \times (M + 1)} \approx \frac{1}{6} \text{ FLOPs / Byte}$$
*Consequence:* SpMV is severely memory-bandwidth bound on all modern architectures!

---

## 5. Barnes-Hut N-Body Complexity

* **Direct All-Pairs Calculation**: $\Theta(N^2)$ force evaluations.
* **Barnes-Hut Octree Calculation**: $\Theta(N \log N)$ force evaluations using Multipole Acceptance Criterion ($\theta = d / r$).
* **Fast Multipole Method (FMM)**: $\Theta(N)$ force evaluations using local multipole expansions.
