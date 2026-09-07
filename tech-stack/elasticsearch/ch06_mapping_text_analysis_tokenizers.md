# Chapter 6: Mapping, Text Analysis Pipelines & Tokenizers

## 1. The Text Analysis Pipeline Architecture

Before raw string text is indexed into Lucene inverted index segments, it passes through a multi-stage **Text Analysis Pipeline** managed by an **Analyzer**.

```
 Raw Input Text: "<h2>The 2 QUICK Brown Foxes!</h2>"
                      |
                      v
          1. Character Filters
          (Strips HTML Tags: "The 2 QUICK Brown Foxes!")
                      |
                      v
          2. Tokenizer
          (Splits into Tokens: ["The", "2", "QUICK", "Brown", "Foxes"])
                      |
                      v
          3. Token Filters
          (Lowercase: ["the", "2", "quick", "brown", "foxes"])
          (Stopwords: ["2", "quick", "brown", "foxes"])
          (Stemmer:   ["2", "quick", "brown", "fox"])
                      |
                      v
 Indexed Lucene Tokens: ["2", "quick", "brown", "fox"]
```

---

## 2. Analyzer Components & Configuration

An Analyzer consists of three modular processing blocks:

### 1. Character Filters (0 or more)
Modifies raw characters before tokenization.
* `html_strip`: Strips HTML elements (`<b>`, `<i>`).
* `mapping`: Replaces characters based on defined key-value pairs (e.g., `&` $\to$ `and`).
* `pattern_replace`: Applies Regular Expression search and replace.

### 2. Tokenizers (Exactly 1)
Splits character streams into discrete tokens.
* `standard`: Grammar-based tokenization (removes punctuation, splits on whitespace).
* `whitespace`: Splits strictly on whitespace boundaries.
* `keyword`: No-op tokenizer (emits entire input string as a single token).
* `ngram` / `edge_ngram`: Emits character $N$-grams for prefix auto-complete search.

### 3. Token Filters (0 or more)
Transforms, adds, or deletes tokens emitted by the tokenizer.
* `lowercase`: Normalizes tokens to lowercase.
* `stop`: Removes common stopwords ("the", "is", "at").
* `stemmer`: Reduces words to root forms (Porter / Snowball stemmer: "running" $\to$ "run").
* `synonym`: Expands tokens using defined synonym lists ("quick" $\to$ ["quick", "fast"]).

---

## 3. Data Field Types: `text` vs `keyword`

Choosing between `text` and `keyword` field types fundamentally alters indexing data structures and query capabilities:

```
+-----------------------------------------------------------------------+
| Comparison: Text vs Keyword Field Types                               |
+-------------------+--------------------+------------------------------+
| Metric            | text Field         | keyword Field                |
+-------------------+--------------------+------------------------------+
| Analysis Pipeline | Passes through     | Bypasses Analyzer            |
|                   | Analysis Pipeline  | (Exact String Preservation)  |
+-------------------+--------------------+------------------------------+
| Use Case          | Full-text search,  | Exact match filtering,       |
|                   | relevance scoring  | aggregations, sorting        |
+-------------------+--------------------+------------------------------+
| Query Type        | `match` query      | `term` query                 |
+-------------------+--------------------+------------------------------+
| Inverted Index    | Analyzed Tokens    | Exact Raw String Value       |
+-------------------+--------------------+------------------------------+
| Columnar Storage  | Fielddata (Off)    | DocValues (On by default)    |
+-------------------+--------------------+------------------------------+
```

---

## 4. Mapping Explosion Prevention Rules

Dynamic mapping allows indexing documents without explicit schemas. However, untamed dynamic mapping leads to **Mapping Explosion**, where thousands of unique field mappings consume Master node JVM Heap RAM.

```yaml
# Recommended index settings to enforce mapping safeguards
index.mapping.total_fields.limit: 1000
index.mapping.depth.limit: 20
index.mapping.nested_fields.limit: 50
```
