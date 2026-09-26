# Relevant Search: 30-Second Relevance Engineering Cheatsheet

This cheatsheet aggregates core query tuning strategies, function score decay math, phrase proximity equations, hybrid RRF formulas, and relevance evaluation metrics.

---

## 1. Multi-Match Query Types Comparison

| Strategy Mode | Best Use Case | Score Aggregation Behavior |
| :--- | :--- | :--- |
| **`best_fields` (DisMax)** | Searching same text across different fields (e.g., `title`, `overview`) | Takes maximum field score + `tie_breaker * (other_scores)`. |
| **`most_fields`** | Searching fields analyzed differently (e.g., `stemmed`, `unstemmed`, `shingle`) | Sums scores across all matching fields. |
| **`cross_fields`** | Entity matching across multiple fields (e.g., `first_name` + `last_name`) | Treats fields as one large virtual field; analyzes per-term IDF. |
| **`phrase` / `phrase_prefix`** | Exact sequence matching | Runs `match_phrase` query across each field, using `best_fields`. |

---

## 2. Function Score Decay Math

For numeric/date/geo fields, **Decay Functions** scale document scores smoothly based on distance from an ideal origin point:

### Gaussian Decay Function
$$S_{\text{decay}}(d) = \exp \left( -\frac{\max(0, |x - x_0| - \text{offset})^2}{2 \sigma^2} \right)$$
where $\sigma^2 = -\frac{\text{scale}^2}{2 \ln(\text{decay})}$.

```
Score Multiplier
 1.0 |======== [ Offset ] ........
     |                           \
     |                            \ (Gaussian Smooth Curve)
     |                             \
 0.5 |                              +------------ (Decay Value at Scale)
     |                               \
 0.0 +--------------------------------------------------> Distance |x - x0|
```

---

## 3. Reciprocal Rank Fusion (RRF) Formula

When combining sparse BM25 keyword rankings ($r_{\text{sparse}}$) and dense vector $k$-NN rankings ($r_{\text{dense}}$):

$$S_{\text{RRF}}(d) = \frac{1}{k + r_{\text{sparse}}(d)} + \frac{1}{k + r_{\text{dense}}(d)}$$

* Standard smoothing constant $k = 60$. Prevents top-ranked items from dominating score distributions.

---

## 4. Phrase Matching & Slop Edit Distance

A `match_phrase` query with `slop = s` allows terms to be separated by at most $s$ position edits (including out-of-order swaps):

* **Exact Match (`slop = 0`)**: `"data pipeline"` matches `"data pipeline"`.
* **Proximity Match (`slop = 1`)**: `"data pipeline"` matches `"data streaming pipeline"`.
* **Out-of-Order Swap (`slop = 2`)**: `"data pipeline"` matches `"pipeline data"`.

---

## 5. Relevance Evaluation Metrics (QuePID & Judgments)

### Mean Reciprocal Rank (MRR)
$$\text{MRR} = \frac{1}{|Q|} \sum_{i=1}^{|Q|} \frac{1}{\text{rank}_i}$$

### Normalized Discounted Cumulative Gain (NDCG@K)
$$\text{NDCG}@K = \frac{\text{DCG}@K}{\text{IDCG}@K}, \quad \text{DCG}@K = \sum_{i=1}^K \frac{2^{\text{rel}_i} - 1}{\log_2(i + 1)}$$
