# Information Retrieval & Search Engines: 30-Second Cheatsheet

This cheatsheet aggregates core ranking formulas, index compression algorithms, evaluation metrics, and link analysis equations.

---

## 1. Scoring & Ranking Formulas

### TF-IDF Weighting
$$\text{TF-IDF}(t, d, D) = \left( 1 + \log_{10} \text{tf}_{t,d} \right) \times \log_{10} \left( \frac{N}{\text{df}_t} \right) \quad (\text{for } \text{tf}_{t,d} > 0)$$

### Okapi BM25 Ranking Formula
$$\text{Score}_{\text{BM25}}(D, Q) = \sum_{t \in Q} \text{IDF}(t) \cdot \frac{\text{tf}_{t,D} \cdot (k_1 + 1)}{\text{tf}_{t,D} + k_1 \cdot \left( 1 - b + b \cdot \frac{|D|}{\text{avgdl}} \right)}$$
$$\text{IDF}(t) = \ln \left( \frac{N - \text{df}_t + 0.5}{\text{df}_t + 0.5} + 1 \right)$$
* **Tuning Constants:** $k_1 \approx 1.2 \dots 2.0$ (Term frequency saturation), $b \approx 0.75$ (Document length normalization).

---

## 2. Inverted Index Compression Math

### Variable Byte (VB) Encoding
* **Continuity Bit**: High bit (`0x80`) = 1 indicates more bytes follow; 0 indicates final byte.
* **Payload Bits**: Lower 7 bits store the integer payload in $d$-gap (difference from previous DocID) format.

```
Integer: 5 (0x05)      -> VB Byte: [0000 0101] (0x05)
Integer: 130 (0x82)    -> VB Bytes: [1000 0001] [0000 0010] (0x81 0x02)
```

### Skip Pointers Intersection Complexity
With skip pointers placed every $\sqrt{P}$ entries in a postings list of length $P$:
$$\text{Intersection Time} = O(\sqrt{P}_1 + \sqrt{P}_2) \quad (\text{VS Naive } O(P_1 + P_2))$$

---

## 3. Search Engine Evaluation Metrics

### Mean Average Precision (MAP)
$$\text{MAP} = \frac{1}{|Q|} \sum_{q \in Q} \text{AP}(q), \quad \text{AP} = \frac{\sum_{k=1}^N P@k \cdot \text{rel}(k)}{\text{Total Relevant Documents}}$$

### Normalized Discounted Cumulative Gain (NDCG@K)
$$\text{DCG}@K = \sum_{i=1}^K \frac{2^{\text{rel}_i} - 1}{\log_2(i + 1)}, \quad \text{NDCG}@K = \frac{\text{DCG}@K}{\text{IDCG}@K}$$
where $\text{IDCG}@K$ is the ideal DCG calculated over perfect monotonic descending relevance labels.

---

## 4. Link Analysis & Deduplication Math

### PageRank Power Iteration Vector Equation
$$\mathbf{p}^{(t+1)} = \alpha \mathbf{M} \mathbf{p}^{(t)} + \frac{1 - \alpha}{N} \mathbf{1}$$
* $\alpha \approx 0.85$ (Damping factor / probability of following hyper-links vs random teleportation).

### SimHash 64-Bit Document Deduplication
Two documents $D_1$ and $D_2$ are near-duplicates if their 64-bit SimHash fingerprints differ by a Hamming distance $h \le 3$:
$$\text{Hamming Distance}(S_1, S_2) = \text{popcount}(S_1 \oplus S_2) \le 3$$
