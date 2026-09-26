# System Design 4: Distributed Web Crawler (Google Search / Internet Archive)

## 1. Problem Statement & Requirements

Design a distributed web crawler capable of fetching, parsing, and indexing 1 Billion web pages per month.

### Functional Requirements:
* Given a list of seed URLs, continuously discover new URLs, download HTML documents, and extract outbound links.
* Filter duplicates (URL deduplication & Content deduplication).
* Enforce **Politeness Policies** (respect `robots.txt` and domain rate limits).

### Non-Functional Requirements:
* Scale: Crawl 1 Billion pages/month ($380 \text{ pages/sec}$).
* Extensibility: Support plugins for media extraction (images, PDF, video).
* Robustness: Gracefully handle bad HTML, infinite loops (spider traps), and dead servers.

---

## 2. High-Level Crawler Blueprint

```
 Seed URLs
    |
    v
 +-----------------------------------------------------------------------+
 | URL Frontier (Priority Queue + Politeness Queue)                       |
 +-----------------------------------+-----------------------------------+
                                     |
                                     v Fetch URL
 +-----------------------------------+-----------------------------------+
 | DNS Resolver Cache & Fetcher Pool (Async Non-Blocking I/O)           |
 +-----------------------------------+-----------------------------------+
                                     |
                                     v Raw HTML Payload
 +-----------------------------------+-----------------------------------+
 | Content Parser & Duplicate Content Detector (SimHash / MinHash)       |
 +-----------------------------------+-----------------------------------+
                                     |
                +--------------------+--------------------+
                |                                         |
                v Extracted Outbound URLs                 v Parsed Document Payload
 +-----------------------------------+      +-----------------------------------+
 | URL Deduplicator (Bloom Filter)   |      | Search Storage / Document Indexer |
 +-----------------------------------+      +-----------------------------------+
```

---

## 3. Deep Dive: The URL Frontier Architecture

The **URL Frontier** manages URL execution priority while guaranteeing domain politeness (never flooding a single domain server with thousands of concurrent requests).

```
 Incoming Discovered URLs
            |
            v
 +---------------------------------------------------+
 | Priority Queues (Priority Router)                 |
 | (Queue 1: High PageRank | Queue 2: Low PageRank)  |
 +-------------------------+-------------------------+
                           |
                           v Select URL
 +---------------------------------------------------+
 | Politeness Queues (Host Router via Hash)          |
 | (Domain A Queue | Domain B Queue | Domain C Queue)|
 +-------------------------+-------------------------+
                           |
                           v Queue Worker Selection
 +---------------------------------------------------+
 | Delay Selector & Host Lock Table                  |
 | (Enforces 1 second delay between same-host fetches|
 +---------------------------------------------------+
```

### URL Deduplication via Bloom Filters:
Before adding a discovered URL to the URL Frontier, it is checked against an in-memory **Bloom Filter**. If the Bloom Filter returns true, the URL has already been processed and is discarded, preventing infinite crawling loops.
