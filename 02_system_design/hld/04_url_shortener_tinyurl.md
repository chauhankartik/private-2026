# System Design 3: Scalable URL Shortener (TinyURL / Bitly)

## 1. Problem Statement & Requirements

Design a URL shortening service (like TinyURL or Bitly) that converts long web URLs into short 7-character aliases.

### Functional Requirements:
* Shorten a long URL (e.g., `https://example.com/deep/path/page.html` $\to$ `https://tiny.url/abc123X`).
* Redirect a short URL to the original long URL with sub-100ms latency.
* Custom alias support (e.g., `https://tiny.url/my-custom-link`).

### Non-Functional Requirements:
* Scale: 500 Million new URLs created per month ($100:1$ Read/Write Ratio).
* High Availability: 99.99% availability for redirects.
* Short Key Durability: Short URLs must remain valid permanently.

---

## 2. Capacity Estimation Math

* **Write QPS:** 500M short URLs / month = $\frac{5 \times 10^8}{30 \times 86400} \approx 200 \text{ Writes/sec}$.
* **Read QPS (100:1 Ratio):** $200 \times 100 = 20,000 \text{ Read QPS}$.
* **5-Year Storage Capacity:** 500M URLs/month $\times 12 \times 5 = 30 \text{ Billion Records}$. If each record is $500 \text{ Bytes}$:
  $$\text{Storage Required} = 30 \times 10^9 \times 500 \text{ Bytes} = 15 \text{ Terabytes}$$

---

## 3. Key Generation Strategies: Base62 vs KGS

```
 Length 7 Base62 Character Set: [a-z, A-Z, 0-9] (62 unique characters)
 Total Unique Short Keys = 62^7 = 3.52 Trillion Combinations!
```

### Approach 1: Auto-Incrementing Counter + Base62 Encoding
* Convert an auto-incrementing 64-bit integer ID to a Base62 string (`ID = 10,000,000,000` $\to$ `Base62(ID) = "aB3x9Z"`).
* **Cons:** Predictable (users can guess adjacent short URLs by changing characters).

### Approach 2: Key Generation Service (KGS) with ZooKeeper (Recommended)
* A standalone **Key Generation Service (KGS)** pre-generates random 7-character Base62 keys in advance and stores them in a `KeyDB` table.

```
 +------------------------+      Pre-generates keys       +------------------------+
 | Key Generation Service |------------------------------>| KeyDB (Available Keys) |
 | (KGS Worker Nodes)     |                               | (Unused Key Buffer)    |
 +-----------+------------+                               +-----------+------------+
             |                                                        |
             | Fetches Key Ranges via ZooKeeper Lock Token            v
             +--------------------------------------------------------+
```
* **ZooKeeper Lock Management:** KGS servers acquire blocks of 10,000 keys at a time from KeyDB. If a KGS node crashes, its assigned unused key block is discarded, avoiding duplicate assignments without runtime lock contention.

---

## 4. High-Level System Blueprint & Redirect Mechanics

```
 Client Browser
      |
      v GET /abc123X
 +----------------------------------+
 | CDN / Edge Router                |
 +----------------+-----------------+
                  |
                  v Cache Miss
 +----------------+-----------------+
 | API Application Service          |
 +----------------+-----------------+
                  |
    Check Cache   | Fetch Long URL
                  v
 +----------------+-----------------+         Cache Miss         +----------------------------------+
 | Redis Cache Cluster              |--------------------------->| SQL/NoSQL Database Shard Cluster |
 | (Hot 20% URLs in RAM)            |                            | (Key: abc123X -> Long URL)       |
 +----------------------------------+                            +----------------------------------+
```

### HTTP 301 Permanent vs HTTP 302 Temporary Redirect Trade-off:
* **HTTP 301 Permanent Redirect:** Browser caches the redirect locally. Subsequent requests bypass TinyURL servers entirely (**Reduces server load**, but **destroys click analytics tracking**).
* **HTTP 302 Temporary Redirect:** Browser always routes through TinyURL servers (**Enables real-time click analytics & geo-tracking**, but increases read QPS load).
