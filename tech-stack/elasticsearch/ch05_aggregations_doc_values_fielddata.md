# Chapter 5: Aggregations Engine & Columnar Storage Mechanics

## 1. Aggregation Engine Taxonomy

Elasticsearch provides a real-time analytical engine operating over distributed search results. Aggregations can be nested into multi-level tree structures.

```
                         Aggregation Pipeline Request
                                       |
                 +---------------------+---------------------+
                 |                                           |
                 v                                           v
      Bucket Aggregations                         Metric Aggregations
   (Partition docs into sets)                 (Compute scalar math values)
                 |                                           |
    +------------+------------+                 +------------+------------+
    |                         |                 |                         |
    v                         v                 v                         v
  Terms                   Histogram           Sum / Avg             Cardinality
($terms)             ($date_histogram)       ($stats)             (HyperLogLog++)
```

### Core Aggregation Types:
1. **Bucket Aggregations:** Group documents into discrete sets (buckets) based on field criteria (`terms`, `date_histogram`, `range`, `geo_distance`).
2. **Metric Aggregations:** Compute mathematical calculations over documents in a bucket (`sum`, `avg`, `min`, `max`, `percentiles`, `cardinality`).
3. **Pipeline Aggregations:** Execute calculations on the output of other aggregations (`derivative`, `moving_avg`, `cumulative_sum`).

---

## 2. DocValues Columnar Storage (`.dvd` Files)

By default, an inverted index maps terms to document IDs. However, executing aggregations requires **un-inverting** this relationship: mapping document IDs to field values.

```
 Inverted Index (Search):
   Term "Red" -> [ Doc 1, Doc 4, Doc 9 ]

 DocValues Columnar Storage (Aggregations & Sorting):
   Doc ID | Field "color"
   -----------------------
   Doc 1  | Red
   Doc 2  | Blue
   Doc 3  | Green
   Doc 4  | Red
```

### DocValues Properties:
* Built automatically at index time for all `keyword`, `numeric`, `date`, `boolean`, and `ip` field types.
* Persisted directly to disk (`.dvd` and `.dvm` files).
* Leverages OS Page Cache (off-heap memory), leaving JVM Heap unburdened during massive analytical queries.

---

## 3. Fielddata Heap Hazard (`text` Fields)

DocValues cannot be created for full-text analyzed `text` fields because individual words are broken into tokens by analyzers.

If an aggregation or sort is executed on a `text` field, Elasticsearch builds **Fielddata** dynamically in memory:

```
                  Client Executes Aggregation on Analyzed `text` Field
                                           |
                                           v
                 Elasticsearch Un-Inverts Lucene Index in Memory
                                           |
                                           v
            Loads Entire Dictionary into JVM Heap Memory (Fielddata Cache)
                                           |
                                           v
         HAZARD: Trigger Circuit Breaking Exception OR OutOfMemory (OOM) Crash!
```

### Fielddata Memory Protection Rules:
1. Fielddata is disabled by default on `text` fields. Attempting to aggregate on an analyzed `text` field throws a runtime error:
   `Fielddata is disabled on text fields by default. Set fielddata=true...`
2. **Best Practice:** Never enable `fielddata=true`. Use a multi-field mapping containing a `.keyword` sub-field backed by DocValues:

```json
{
  "mappings": {
    "properties": {
      "category": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
    }
  }
}
```
* Execute full-text search against `category` (analyzed `text`).
* Execute aggregations and sorting against `category.keyword` (DocValues `keyword`).
