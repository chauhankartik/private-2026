# Recommended Books & Resources: Elasticsearch & Apache Lucene

A curated collection of authoritative books, search architecture reference manuals, official engineering documentation, and open-source GitHub repositories.

---

## 1. Essential Books

1. **Elasticsearch: The Definitive Guide**  
   *Authors:* Clinton Gormley, Zachary Tong  
   *Focus:* The seminal reference book covering Elasticsearch distributed architecture, Lucene mechanics, mapping, text analysis pipelines, aggregations, and cluster operations.

2. **Relevant Search: With applications for Solr and Elasticsearch**  
   *Authors:* Doug Turnbull, John Berryman  
   *Focus:* Search relevance engineering, TF-IDF and BM25 scoring mechanics, query boosting, precision vs recall optimization, and custom analyzer design.

3. **Information Retrieval: Implementing and Evaluating Search Engines**  
   *Authors:* Stefan Büttcher, Charles L. A. Clarke, Gordon V. Cormack  
   *Focus:* Fundamental computer science algorithms for search engines: Inverted indexes, posting list compression (FOR, Variable Byte), dictionary representations (FST), and probabilistic scoring models.

---

## 2. Official Documentation & Open Source Codebases

* **[Elasticsearch Reference Manual](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)** — Official guide for cluster setup, mapping, search APIs, aggregations, and ILM.
* **[Apache Lucene Core Documentation](https://lucene.apache.org/core/)** — Low-level Java indexing library documentation covering `IndexWriter`, `DirectoryReader`, `Codec`, and segment file formats.
* **[Elasticsearch Repository (GitHub)](https://github.com/elastic/elasticsearch)** — Open-source source code for Elasticsearch server engine.
* **[Apache Lucene Repository (GitHub)](https://github.com/apache/lucene)** — Java source code repository for Apache Lucene core search library.

---

## 3. Source Code Exploration Guide (Apache Lucene & Elasticsearch)

When reading the `apache/lucene` & `elastic/elasticsearch` codebases:
* **Lucene Inverted Index Writing:** `lucene/core/src/java/org/apache/lucene/index/IndexWriter.java`
* **Lucene FST Implementation:** `lucene/core/src/java/org/apache/lucene/util/fst/FST.java`
* **Posting Lists & Block Tree Terms:** `lucene/core/src/java/org/apache/lucene/codecs/blocktree/`
* **Elasticsearch Engine Write Service:** `server/src/main/java/org/elasticsearch/index/engine/InternalEngine.java`
* **Elasticsearch Master Cluster State:** `server/src/main/java/org/elasticsearch/cluster/service/MasterService.java`
