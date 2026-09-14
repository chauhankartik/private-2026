# Stanford EE382C: 30-Second Microarchitecture Cheatsheet

This cheatsheet aggregates core quantitative equations, sizing bounds, state transition invariants, and trade-off matrices for advanced computer architecture and parallel microarchitecture.

---

## 1. Quantitative CPU Performance & Pipeline Math

### Iron Law of Processor Performance
$$\text{Execution Time} = \text{Instruction Count} \times \text{CPI} \times \text{Clock Cycle Time} = \frac{\text{Instruction Count} \times \text{CPI}}{f_{\text{clock}}}$$

### Actual CPI Accounting
$$\text{CPI}_{\text{actual}} = \text{CPI}_{\text{ideal}} + \text{Stalls}_{\text{structural}} + \text{Stalls}_{\text{data}} + \text{Stalls}_{\text{mispredict}} + \text{Stalls}_{\text{cache\_miss}}$$

where:
$$\text{Stalls}_{\text{mispredict}} = \text{Branch Frequency} \times \text{Misprediction Rate} \times \text{Penalty}_{\text{flush\_cycles}}$$
$$\text{Stalls}_{\text{cache\_miss}} = \text{Memory Accesses per Inst} \times \text{Miss Rate} \times \text{Miss Penalty}_{\text{cycles}}$$

### Reorder Buffer (ROB) Sizing & Window Depth
To sustain a target instruction-level parallelism ($\text{ILP}_{\text{target}}$) with execution latency $L_{\text{exec}}$:
$$\text{ROB}_{\text{min\_entries}} \ge \text{Issue Width} \times \text{Max Memory Penalty Latency}$$
For example, for a 4-wide superscalar core covering a 200-cycle DRAM access penalty:
$$\text{ROB}_{\text{depth}} \ge 4 \times 200 = 800 \text{ entries}$$

---

## 2. Memory Hierarchy & Non-Blocking Cache Math

### Average Memory Access Time (AMAT) with Multi-Level Caches
$$\text{AMAT} = t_{\text{L1\_hit}} + \text{MR}_{\text{L1}} \times \left( t_{\text{L2\_hit}} + \text{MR}_{\text{L2\_local}} \times \left( t_{\text{L3\_hit}} + \text{MR}_{\text{L3\_local}} \times t_{\text{DRAM}} \right) \right)$$

### Miss Status Holding Register (MSHR) Capacity Bound
To fully saturate memory bus bandwidth $BW_{\text{mem}}$ (bytes/sec) at DRAM latency $L_{\text{dram}}$ (sec) with cache line size $B_{\text{line}}$ (bytes):
$$\text{MSHR}_{\text{count}} \ge \frac{BW_{\text{mem}} \times L_{\text{dram}}}{B_{\text{line}}}$$
*Example:* $BW_{\text{mem}} = 100 \text{ GB/s}$, $L_{\text{dram}} = 60 \text{ ns}$, $B_{\text{line}} = 64 \text{ B}$:
$$\text{MSHR}_{\text{count}} \ge \frac{100 \times 10^9 \times 60 \times 10^{-9}}{64} = \frac{6000}{64} \approx 94 \text{ MSHRs}$$

---

## 3. Interconnection Network & Router Math (William Dally Model)

### Zero-Load Latency ($T_0$)
$$\text{T}_0 = H \cdot t_r + \frac{L}{B}$$
where:
* $H$: Number of hops along the path.
* $t_r$: Single router latency (cycles).
* $L$: Packet length in bits.
* $B$: Link bandwidth (bits/cycle).

### Wormhole Routing Credit-Based Flow Control Latency
$$\text{Credit Return Latency} = 2 \times t_{\text{prop}} + t_{\text{router\_processing}}$$
Buffer allocation per Virtual Channel (VC) must satisfy:
$$\text{Buffer Depth}_{\text{VC}} \ge \text{Credit Return Latency} \times \text{Link Throughput (flits/cycle)}$$

### Network Topologies & Bisection Bandwidth

| Topology | Node Count ($N$) | Diameter | Bisection Width | Link Degrees |
| :--- | :--- | :--- | :--- | :--- |
| **2D Mesh ($k \times k$)** | $k^2$ | $2(k-1)$ | $k$ | 4 |
| **2D Torus ($k \times k$)** | $k^2$ | $2 \lfloor k/2 \rfloor$ | $2k$ | 4 |
| **Hypercube ($n$-dim)** | $2^n$ | $n$ | $2^{n-1}$ | $n$ |
| **Fat-Tree ($k$-ary)** | $k^3 / 4$ | $2 \log_k(N)$ | $\frac{N \cdot B}{2}$ | $k$ |

---

## 4. Domain-Specific Accelerator Math (Systolic Arrays & TPUs)

### Roofline Model Bound
$$\text{Attainable Performance (GFLOPS)} = \min \left( \text{Peak Performance}, \text{Operational Intensity} \times \text{Memory Bandwidth} \right)$$
where Operational Intensity ($\text{OI}$) is measured in $\text{FLOPs / Byte}$.

### Systolic Array GEMM Throughput ($N \times N$ Matrix Multiply)
For a 2D Systolic Array of size $M \times M$ processing an $N \times N$ matrix:
$$\text{Total MAC Operations} = 2 N^3$$
$$\text{Total Execution Cycles} = 3N + M - 2$$
$$\text{PE Utilization} = \frac{2 N^3}{2 M^2 \times (3N + M - 2)} \approx 100\% \quad (\text{for } N \gg M)$$

---

## 5. Quick Reference Decision Matrix

```
Requirement                                     Recommended Architecture Technique
------------------------------------------------------------------------------------------------------
Unpredictable control flow with high latency    TAGE Branch Predictor + Superscalar OoO (Tomasulo + ROB)
High concurrency memory load instructions       Non-blocking cache with MSHRs & Stride/Markov Prefetcher
Scalable multi-socket cache coherence          Distributed Directory-Based Coherence Protocol (MOESI)
Prevent Head-of-Line (HoL) router stall         Wormhole Routing with Virtual Channels (VCs) & Credit Control
High throughput tensor matrix multiplication    Systolic Array Accelerator (Weight-Stationary / Output-Stationary)
Multi-chiplet memory & cache expansion          CXL (Compute Express Link 3.0) / NVLink Interconnect
```
