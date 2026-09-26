# Chapter 2: Apache Lucene Inverted Index & Internal Storage Mechanics

## 1. Inverted Index Architecture

The core data structure enabling high-speed full-text search in Apache Lucene is the **Inverted Index**. Instead of mapping documents to fields (like a traditional RDBMS table), an inverted index maps analyzed **Terms** to the **Document IDs** in which those terms appear.

```
 Forward Index (Traditional Database):
   Doc 1 -> "Elasticsearch distributed search"
   Doc 2 -> "Lucene search engine"

 Inverted Index (Lucene):
   Term          Posting List (Doc IDs + Term Frequencies + Positions)
   -------------------------------------------------------------------
   distributed -> [ Doc 1 (tf=1, pos=1) ]
   engine      -> [ Doc 2 (tf=1, pos=2) ]
   lucene      -> [ Doc 2 (tf=1, pos=0) ]
   search      -> [ Doc 1 (tf=1, pos=2), Doc 2 (tf=1, pos=1) ]
```

---

## 2. Inverted Index Components: FST, Term Dictionary & Posting Lists

To execute searches in sub-millisecond speeds over billions of documents, Lucene decomposes the inverted index into three core internal data structures:

```
  +-----------------------------------------------------------------------+
  | Finite State Transducer (FST)                                         |
  | In-memory (RAM) prefix tree mapping term prefixes to disk offsets.     |
  +-----------------------------------+-----------------------------------+
                                      |
                                      v Disk Offset Lookup
  +-----------------------------------+-----------------------------------+
  | Term Dictionary (tim file)                                            |
  | Sorted block index of all unique terms across all documents.          |
  +-----------------------------------+-----------------------------------+
                                      |
                                      v Term Pointer
  +-----------------------------------+-----------------------------------+
  | Posting Lists (doc file)                                              |
  | Compressed arrays of Document IDs containing the target term.         |
  +-----------------------------------------------------------------------+
```

### 1. Term Dictionary (`.tim` File on Disk)
* A sorted block index containing every unique term analyzed across all documents within a Lucene segment.

### 2. Finite State Transducer (FST - `.tip` File in RAM)
* Loading the entire Term Dictionary into RAM would exhaust JVM memory.
* Lucene compresses term prefixes into a **Finite State Transducer (FST)** stored in RAM. The FST acts as an $O(1)$ memory prefix index, resolving target term locations within the disk Term Dictionary without scanning disk blocks.

### 3. Posting Lists & Compression Algorithms (`.doc` File)
A Posting List is an ordered array of integer document IDs. Lucene compresses posting lists using two algorithms:
* **Frame of Reference (FOR):** Groups document IDs into blocks of 128 integers. Computes delta differences between IDs ($\Delta_1 = \text{doc}_2 - \text{doc}_1$) and packs bit representations using the maximum bit-width required by the block.
* **Roaring Bitmaps:** Used for sparse bitsets. Switches between uncompressed short arrays (for low density) and bitset arrays (for high density).

---

## 3. Storage Files: `_source` vs DocValues

For every indexed document, Lucene creates two distinct storage files alongside the inverted index:

```
+-----------------------------------------------------------------------+
| Document Data Storage Formats                                         |
+-------------------+---------------------------------------------------+
| _source           | Stored raw JSON binary payload (`.fdt` file).     |
| (Row-Oriented)    | Used to hydrate full document in search response. |
+-------------------+---------------------------------------------------+
| DocValues         | Disk-based columnar data structure (`.dvd` file). |
| (Column-Oriented) | Used for sorting, aggregations, and scripting.    |
+-------------------+---------------------------------------------------+
```
* **Why DocValues?** Un-inverting an Inverted Index in RAM to extract field values for sorting millions of matching documents causes massive JVM Heap GC pauses. DocValues pre-builds a disk-based columnar payload at index time, streaming values directly into RAM off-heap.
