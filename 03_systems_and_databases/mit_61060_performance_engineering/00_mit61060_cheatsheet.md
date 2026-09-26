# MIT 6.1060 (6.172): 30-Second Performance Engineering Cheatsheet

This cheatsheet aggregates core performance math, bit hack primitives, SIMD vectorization rules, cache-oblivious bounds, and profiling commands.

---

## 1. Concurrency & Work/Span Analysis Math

### Work & Span Laws
* **Work ($T_1$)**: Total time to execute the program sequentially on a single thread.
* **Span ($T_\infty$)**: Longest path through the DAG (Critical Path length).
* **Parallelism ($P$)**: 
  $$P = \frac{T_1}{T_\infty}$$

### Brent's Theorem & Greedy Scheduler Bound
On $P$ processors, a greedy scheduler achieves:
$$T_P \le \frac{T_1 - T_\infty}{P} + T_\infty = \frac{T_1}{P} + T_\infty \left( 1 - \frac{1}{P} \right)$$

### Linear Speedup Condition
If $P \ll \frac{T_1}{T_\infty}$, then $T_P \approx \frac{T_1}{P}$ (Linear Speedup).

---

## 2. Memory Hierarchy & Cache-Oblivious Bounds

### Matrix Multiplication Cache Miss Complexity ($N \times N$ Matrix, Cache Size $Z$, Line Size $B$)
* **Naive 3-Loop ($i, j, k$)**:
  $$\text{Misses} = \Theta(N^3) \quad (\text{Poor spatial locality for } B > 1)$$
* **Cache-Tiled (Block Size $b = \sqrt{Z/3}$)**:
  $$\text{Misses} = \Theta\left( \frac{N^3}{B \sqrt{Z}} \right) \quad (\text{Requires tuning parameter } b)$$
* **Recursive Cache-Oblivious Divide & Conquer**:
  $$\text{Misses} = \Theta\left( \frac{N^3}{B \sqrt{Z}} \right) \quad (\text{Optimal for ALL cache levels automatically!})$$

---

## 3. Bit Hacks Reference Table

| Bit Operation | Expression / Intrinsic | Description |
| :--- | :--- | :--- |
| **Clear Lowest Set Bit** | `x & (x - 1)` | Clears rightmost set 1-bit (`0b10100` -> `0b10000`). |
| **Isolate Lowest Set Bit** | `x & (-x)` | Extracts rightmost set 1-bit (`0b10100` -> `0b00100`). |
| **Check Power of 2** | `(x & (x - 1)) == 0` | Returns `true` if $x = 2^k$ ($x > 0$). |
| **Population Count** | `__builtin_popcount(x)` | Counts total set 1-bits (Hardware `POPCNT` inst). |
| **Count Trailing Zeros** | `__builtin_ctz(x)` | Counts trailing 0-bits (Hardware `TZCNT` inst). |
| **Count Leading Zeros** | `__builtin_clz(x)` | Counts leading 0-bits (Hardware `LZCNT` inst). |

---

## 4. SIMD AVX-256 Intrinsics Matrix

```
Intrinsic Symbol                           AVX Operation Description
------------------------------------------------------------------------------------------------------
_mm256_loadu_ps(float* p)                  Load 8 unaligned 32-bit floats into 256-bit YMM register
_mm256_storeu_ps(float* p, __m256 v)       Store 256-bit YMM register to memory
_mm256_add_ps(__m256 a, __m256 b)          Parallel vector addition (8 floats in 1 cycle)
_mm256_mul_ps(__m256 a, __m256 b)          Parallel vector multiplication (8 floats in 1 cycle)
_mm256_fmadd_ps(__m256 a, b, c)            Fused Multiply-Add: (a * b) + c (Single Rounding)
```

---

## 5. Linux `perf` & Profiling Command Matrix

```bash
# 1. Record Hardware CPU Performance Counter Stats
perf stat -e cycles,instructions,cache-misses,L1-dcache-load-misses ./my_program

# 2. Record Sampling Profile for Flame Graph
perf record -F 99 -g -- ./my_program

# 3. Generate Interactive Sampling Report
perf report --stdio

# 4. Generate Flame Graph Stack Visualization (perf + FlameGraph script)
perf script | stackcollapse-perf.pl | flamegraph.pl > flamegraph.svg
```
