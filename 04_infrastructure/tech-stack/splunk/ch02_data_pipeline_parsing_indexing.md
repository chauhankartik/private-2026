# Chapter 2: Splunk Data Pipeline & Event Processing

The Splunk Data Pipeline processes raw machine data through four sequential execution stages, transforming un-structured byte streams into searchable, timestamped events.

---

## 1. The 4 Pipeline Stages

```
Raw Machine Logs (File / Sockets / HEC)
       |
       v
[ Stage 1: Input Stage ] (inputs.conf)
  ├── Reads raw byte stream in 64KB blocks
  ├── Assigns initial metadata: host, source, sourcetype, index
  └── Enqueues raw data into utf8Queue / parsingQueue
       |
       v
[ Stage 2: Parsing Stage ] (props.conf & transforms.conf)
  ├── Line Breaking: Splits byte stream into discrete events (LINE_BREAKER)
  ├── Timestamp Extraction: Identifies & normalizes event time (_time)
  ├── Anonymization: Executes SEDCMD regex masks (SSNs, Credit Cards)
  └── Enqueues events into indexQueue
       |
       v
[ Stage 3: Indexing Stage ] (indexes.conf)
  ├── Compresses raw text into rawdata journal (journal.gz)
  ├── Generates inverted keyword index (.tsidx)
  └── Flushes data blocks to Hot storage buckets
       |
       v
[ Stage 4: Search Stage ] (Search Head Execution)
  ├── Evaluates SPL query against .tsidx files
  ├── Executes search-time field extractions (rex, KV_MODE)
  └── Joins lookups, evaluates eval expressions & renders UI
```

---

## 2. Parsing Pipeline Mechanics (`props.conf`)

Properly configuring line breaking and timestamp extraction in `props.conf` is essential to prevent event fragmentation and indexing lag.

```ini
# $SPLUNK_HOME/etc/system/local/props.conf
[my_custom_application_log]
# 1. Line Breaking Configuration
SHOULD_LINEMERGE = false
LINE_BREAKER = ([\r\n]+)\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}
TRUNCATE = 10000

# 2. Timestamp Extraction Configuration
TIME_PREFIX = ^\[
TIME_FORMAT = %Y-%m-%d %H:%M:%S.%3N
MAX_TIMESTAMP_LOOKAHEAD = 30
TZ = UTC
```

### 2.1 `LINE_BREAKER` Best Practices
* Always set `SHOULD_LINEMERGE = false`. The legacy `SHOULD_LINEMERGE = true` setting uses CPU-heavy regex buffering.
* `LINE_BREAKER` uses a regex capturing group `()` to match the line boundary, replacing old lines instantly with zero buffering overhead.

### 2.2 Timestamp Extraction
* `TIME_PREFIX`: Regex directing Splunk to the exact byte offset where the timestamp begins.
* `TIME_FORMAT`: `strptime` format string (e.g. `%Y-%m-%d %H:%M:%S.%3N`).
* If Splunk cannot locate a timestamp within `MAX_TIMESTAMP_LOOKAHEAD` characters, it defaults to the file modification time (`modtime`), corrupting event chronology!

---

## 3. Data Anonymization via `SEDCMD`

Sensitive PII data (Credit Cards, SSNs) must be masked at the Parsing stage before data is written to disk in the Indexing stage.

```ini
# props.conf
[payment_gateway_log]
TRANSFORMS-mask_ssn = mask_ssn_transform

# transforms.conf
[mask_ssn_transform]
REGEX = (social_security=\d{3}-\d{2}-)\d{4}
FORMAT = $1XXXX
COMPRESS_SHA256 = true
```

Alternatively, use `SEDCMD` in `props.conf` for inline regex replacement:
```ini
SEDCMD-mask_cc = s/\b(?:\d[ -]*?){13,16}\b/XXXX-XXXX-XXXX-XXXX/g
```

---

## 4. Configuration File Layering & Precedence

Splunk resolves configuration files based on directory location and context:

```
System Defaults  ---> $SPLUNK_HOME/etc/system/default/
App Defaults     ---> $SPLUNK_HOME/etc/apps/<app_name>/default/
App Local        ---> $SPLUNK_HOME/etc/apps/<app_name>/local/
System Local     ---> $SPLUNK_HOME/etc/system/local/ (HIGHEST PRECEDENCE!)
```

Use `splunk btool` to inspect resolved settings:
```bash
# Displays fully merged props.conf settings with source file origins
$SPLUNK_HOME/bin/splunk btool props list my_custom_application_log --debug
```

---

## 5. Staff Engineer Data Pipeline Rules
1. **Never Index Unsplit Event Streams:** Unconfigured sourcetypes cause Splunk to default to 256-line event merging (`SHOULD_LINEMERGE = true`), polluting indexes with corrupt multiline blobs.
2. **Always Explicitly Define `TZ` (Timezone):** Never rely on system auto-detection for timezones in `props.conf`. Explicitly declare `TZ = UTC` or target timezone.
3. **Use Search-Time Extractions Over Indexed Extractions:** Extracting custom fields at search time keeps `.tsidx` index files small, reducing disk space consumption and indexing overhead.
