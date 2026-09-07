# Chapter 4: Search Execution Path, Query vs Filter Context & Scoring

## 1. Two-Phase Search Execution Path (Scatter-Gather)

Executing a search request (`POST /index/_search?from=0&size=10`) across distributed shards follows a 2-phase **Scatter-Gather** workflow.

```
 Client Application                                             Coordinating Node                                           Target Data Shards
        |                                                              |                                                           |
        | ----------------- 1. Execute Search Query ------------------>|                                                           |
        |                                                              | ---------- 2. Phase 1: Query Phase (Scatter) ----------->|
        |                                                              |            (Broadcast Query Match Request)                |
        |                                                              |                                                           |
        |                                                              |                                                 Executes Lucene Query
        |                                                              |                                                 Calculates BM25 Scores
        |                                                              |                                                 Returns Top-K Doc IDs + Scores
        |                                                              |<---------- 3. Return Priority Queue Hits -----------------|
        |                                                              |                                                           |
        |                                                        Priority Queue Merge
        |                                                        Extracts Top 10 Doc IDs
        |                                                              |                                                           |
        |                                                              | ---------- 4. Phase 2: Fetch Phase --------------------->|
        |                                                              |            (Requests full _source for 10 Doc IDs)          |
        |                                                              |                                                           |
        |                                                              |<---------- 5. Return Hydrated JSON Documents --------------|
        |                                                              |                                                           |
        |<----------------- 6. Return Search Hits JSON ----------------|
        v                                                              v
```

---

## 2. Query Context vs Filter Context

Elasticsearch distinguishes between scoring relevance and exact matching using **Query Context** and **Filter Context**.

```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "distributed search" } }
      ],
      "filter": [
        { "term": { "status": "ACTIVE" } },
        { "range": { "createdAt": { "gte": "2026-01-01" } } }
      ]
    }
  }
}
```

### Context Comparison Matrix:

| Metric | Query Context (`must`, `should`) | Filter Context (`filter`, `must_not`) |
| :--- | :--- | :--- |
| **Question Asked** | *How well does this document match the query clause?* | *Does this document match the criteria? (Yes/No)* |
| **Score Computation** | Computes floating-point BM25 relevance `_score`. | Does **not** compute relevance scores (`_score = 0`). |
| **Caching** | **Non-Cacheable** (scores vary dynamically). | **Cached in RAM** via Node Query Cache (Roaring Bitsets). |
| **Execution Speed** | Moderate (Requires term frequency math). | **Ultra-Fast** (Bitwise `AND` / `OR` bitset intersections). |

---

## 3. Practical BM25 Scoring Mathematics

Lucene uses the **Okapi BM25** probabilistic scoring algorithm to measure full-text document relevance:

$$\text{Score}(D, Q) = \sum_{i=1}^{n} \text{IDF}(q_i) \cdot \frac{f(q_i, D) \cdot (k_1 + 1)}{f(q_i, D) + k_1 \cdot \left(1 - b + b \cdot \frac{|D|}{\text{avgdl}}\right)}$$

### Term Breakdown & Mechanics:
1. **Inverse Document Frequency ($\text{IDF}(q_i)$):** Measures term rarity across the entire index. Rare terms (e.g., "Lucene") receive high weight; common terms (e.g., "the") receive near-zero weight.
   $$\text{IDF}(q) = \ln\left(1 + \frac{N - n(q) + 0.5}{n(q) + 0.5}\right)$$
2. **Term Frequency Saturation ($f(q_i, D)$):** Controls how quickly score gains diminish as term frequency increases. Tuned via parameter $k_1$ (default $1.2$).
3. **Field Length Normalization ($|D| / \text{avgdl}$):** Short fields (e.g., product titles) containing the query term score higher than long document bodies containing the same term. Tuned via parameter $b$ (default $0.75$).
