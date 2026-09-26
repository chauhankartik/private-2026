# Chapter 4: Search Processing Language (SPL) & Query Engine

Splunk's **Search Processing Language (SPL)** encompasses hundreds of commands, functions, and arguments for searching, filtering, transforming, and visualizing machine data.

---

## 1. SPL Command Classification & Pipeline Execution

Commands in SPL are categorized based on where and how they process events along the execution pipeline:

```
[ User Search ] ---> index=main status=500 | eval delay=duration*1000 | stats count by host
                     └─────────┬─────────┘   └──────────┬───────────┘   └────────┬────────┘
                           Index Filter           Streaming Cmd            Reporting Cmd
                                |                       |                        |
                        Executed on Indexers    Executed on Indexers     Executed on Search Head
```

### 1.1 Command Types

| Command Category | Behavior | Examples | Execution Location |
| :--- | :--- | :--- | :--- |
| **Stateless Streaming**| Operates on events individually without knowledge of prior events. | `eval`, `rex`, `where`, `fields`, `rename`, `lookup` | **Indexers** (Parallel execution across cluster) |
| **Stateful Streaming** | Operates on events sequentially while tracking memory state across events. | `dedup`, `streamstats`, `head`, `tail` | **Indexers** (Stream-ordered execution) |
| **Reporting** | Aggregates event sets into structured summary tables. | `stats`, `chart`, `timechart`, `top`, `rare` | **Search Head** (Consolidates Indexer results) |
| **Eventing** | Grouping or ordering events based on inter-event relationships. | `transaction`, `sort` | **Search Head** (Memory intensive) |

---

## 2. Essential SPL Command Patterns

### 2.1 Extraction & Evaluation (`rex` & `eval`)
```spl
index=web_logs sourcetype=access_combined
| rex field=_raw "GET /api/v1/(?<api_endpoint>[^\s\?]+)"
| eval latency_ms = response_time / 1000
| where latency_ms > 500
| stats count avg(latency_ms) as avg_latency by api_endpoint
```

### 2.2 Time-Series Aggregation (`timechart`)
```spl
index=production sourcetype=app_log status>=400
| timechart span=15m count by status
```

---

## 3. High-Speed Metrics Search: `tstats`

`tstats` is significantly faster than standard `search` because it queries **only `.tsidx` index files**, completely bypassing the rawdata journal (`journal.gz`).

```spl
-- Standard Search (Slow - Reads journal.gz)
index=firewall action=blocked | stats count by src_ip

-- tstats Search (Fast - Reads .tsidx files only!)
| tstats count WHERE index=firewall action=blocked BY src_ip
```

---

## 4. `transaction` vs `stats` Optimization

The `transaction` command groups events based on shared fields and time windows, but it is **single-threaded, memory-intensive, and caps results at 1,000 events**.

```spl
-- INEFFICIENT: transaction command
index=security
| transaction session_id maxspan=30m

-- EFFICIENT: stats replacement (10x faster execution!)
index=security
| stats min(_time) as session_start, max(_time) as session_end, count by session_id
| eval session_duration = session_end - session_start
| where session_duration <= 1800
```

---

## 5. Staff Engineer SPL Tuning Rules
1. **Filter Early at the Indexer Boundary:** Put index, sourcetype, host, and specific search keywords at the **very beginning** of the SPL pipeline to prune buckets before streaming commands execute.
2. **Replace `transaction` with `stats`:** Never use `transaction` for high-volume logs; use `stats` with `min(_time)` and `max(_time)` to group events efficiently.
3. **Beware Subsearch Result Limits:** Subsearches (`[ search ... ]`) truncate automatically at **10,000 results** or **60 seconds execution time**. Use `lookup` or `stats` joins for large datasets.
