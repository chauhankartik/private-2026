# Chapter 3: Erasure Coding Mechanics & Multi-DC Replication Math

## 1. Replication vs Erasure Coding

Distributed object stores achieve durability through two data protection techniques: **Multi-Copy Replication** or **Erasure Coding**.

```
 Traditional 3x Replication Scheme (300% Storage Cost):
 Data Object (100MB)  ===>  [ Copy 1: 100MB ] + [ Copy 2: 100MB ] + [ Copy 3: 100MB ]  (Total: 300MB)

 Reed-Solomon 8+4 Erasure Coding Scheme (150% Storage Cost):
 Data Object (100MB)  ===>  Divided into 8 Data Chunks (12.5MB each)
                            Encoded into 4 Parity Chunks (12.5MB each)
                            Total Storage: 12 x 12.5MB = 150MB across 12 independent drives!
```

---

## 2. Reed-Solomon ($K+M$) Erasure Coding Math

**Reed-Solomon (RS)** coding is an error-correcting algorithm that transforms a data payload into $K$ data chunks and $M$ parity chunks.

### Mathematics over Galois Fields ($GF(2^8)$ or $GF(2^{16})$)
1. An incoming binary object is split into $K$ equal-sized vectors $D = [d_1, d_2, \dots, d_K]^T$.
2. The encoder multiplies vector $D$ by a $(K+M) \times K$ Generator Matrix $G$ constructed from Vandermonde or Cauchy matrices:

$$C = G \cdot D = \begin{bmatrix} I_{K \times K} \\ A_{M \times K} \end{bmatrix} \cdot \begin{bmatrix} d_1 \\ d_2 \\ \vdots \\ d_K \end{bmatrix} = \begin{bmatrix} d_1 \\ \vdots \\ d_K \\ p_1 \\ \vdots \\ p_M \end{bmatrix}$$

```
 Data Chunks (K=4)    Parity Chunks (M=2)
 [ Chunk 1 ]         [ Parity 1 ]
 [ Chunk 2 ]   ===>  [ Parity 2 ]
 [ Chunk 3 ]
 [ Chunk 4 ]
```

### Durability & Fault Tolerance Rules:
* **Storage Factor Formula:**
  $$\text{Storage Factor} = \frac{K + M}{K}$$
* **Fault Tolerance Guarantee:** Any system with $K$ data chunks and $M$ parity chunks can reconstruct the original payload from **any arbitrary $K$ surviving chunks**. Up to $M$ disk drive failures can occur simultaneously without data loss.

---

## 3. Storage Efficiency & Failure Tolerance Comparison

| Scheme | Data Chunks ($K$) | Parity Chunks ($M$) | Storage Factor | Storage Overhead (%) | Max Node Failures Supported |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **3x Replication** | 1 | 2 (copies) | **3.0x** | **200%** | 2 failures |
| **RS 4+2** | 4 | 2 | **1.5x** | **50%** | 2 failures |
| **RS 8+4** | 8 | 4 | **1.5x** | **50%** | 4 failures |
| **RS 16+4** | 16 | 4 | **1.25x** | **25%** | 4 failures |

---

## 4. Degraded Read Overhead & Network Trade-Offs

While Erasure Coding saves petabytes of raw storage capacity, it introduces operational trade-offs during drive failures:

```
 Normal Read Path (All drives healthy):
 Client reads K data chunks directly ---> Reconstructs payload (Zero CPU parity math).

 Degraded Read Path (1 Drive Failed):
 Client fetches (K - 1) surviving data chunks + 1 Parity chunk across network
                                |
                                v
 Client/Proxy executes Matrix Inversion in CPU: D = G_surviving^-1 * C_surviving
 (Incurs network bandwidth amplification & CPU matrix math latency spikes!)
```

### Operational Guidance:
* Use **Erasure Coding ($K+M$)** for cold data lakes, warm object storage, and datasets $> 10 \text{ MB}$.
* Use **3x Replication** for ultra-hot small metadata objects, database WAL logs, and latency-sensitive small files ($< 1 \text{ MB}$).
