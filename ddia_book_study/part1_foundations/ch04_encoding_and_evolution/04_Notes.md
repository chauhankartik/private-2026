# Chapter 4: Encoding and Evolution — Deep Dive Notes

> **Core Theme:** Applications change over time. How data structures are converted to bytes (**Serialization / Encoding**), and how binary formats (Protobuf, Thrift, Avro) guarantee **Forward and Backward Compatibility** across rolling application upgrades and distributed nodes.

---

## 1. Formats for Encoding Data

Programs typically work with data in two representations:
1. **In-Memory Data Structures:** Optimized for fast CPU access via pointers, objects, arrays, and hash maps.
2. **Byte Sequences (Serialized Data):** Self-contained sequence of bytes (e.g. JSON document, Protocol Buffer byte array) suitable for writing to disk or sending over network.

Translating in-memory structures to byte sequences is called **Encoding (Serialization / Marshalling)**; the reverse is **Decoding (Deserialization / Unmarshalling)**.

---

## 2. Textual vs. Binary Encodings

### Textual Formats: JSON, XML, CSV
- **Pros:** Human-readable, widely supported, standard web interoperability.
- **Flaws & Edge Cases:**
  1. **Number Ambiguity:** XML/JSON cannot distinguish integers from floating-point numbers without a schema. Large integers ($> 2^{53} - 1$) lose precision in JavaScript numbers (IEEE 754 float).
  2. **Binary Strings:** Do not support raw binary data without base64 encoding (which introduces a 33% space overhead).
  3. **Schema Enforcement:** Schema support (XML Schema, JSON Schema) is optional and complex.

---

## 3. Compact Binary Formats: Protobuf, Thrift, and Avro

Binary formats drastically reduce payload size and eliminate string parsing overhead.

### 1. Protocol Buffers (Protobuf) & Apache Thrift
Both use **Field Tags** (integer numbers) instead of string field names to identify fields in encoded payload.

#### Example Schema:
```protobuf
message Person {
    required string userName = 1;
    optional int64  favoriteNumber = 2;
    repeated string interests = 3;
}
```

#### How Tag Encoding Works:
- Payload stores field tag number (e.g. `1`) + data type ID (e.g. string/varint) + actual value.
- Field names (`userName`) are **NEVER** transmitted over the wire.
- **Varints (Variable-Length Integers):** Uses 1 to 10 bytes depending on magnitude. Uses MSB (Most Significant Bit) as continuation bit.

```
Protobuf Binary Payload Layout:
[ Field Tag & Type Byte ] [ Length Byte (if string) ] [ Value Bytes ]
```

#### Schema Evolution Rules for Protobuf/Thrift:
- **Rule 1: Field Tags MUST NOT be changed.** Renaming a field in schema is safe; changing tag number breaks compatibility!
- **Backward Compatibility:** New code can read data written by old code (new fields receive default values or are missing).
- **Forward Compatibility:** Old code can read data written by new code (old code simply ignores unrecognized field tags).
- **DataType Rules:** Changing field data types can truncate numbers or throw decoding errors.

---

### 2. Apache Avro
Avro does **NOT** include field names OR field tags in the encoded byte sequence!

#### How Avro Encodes Data:
An Avro payload is just concatenated values (e.g., length-prefixed string + varint + boolean) with zero metadata headers!

```
Avro Encoded Record (No Tags, No Field Names):
┌──────────────────────────────┬──────────────────┬─────────────────┐
│ String Length + Bytes        │ Varint (Int)     │ Boolean Byte    │
└──────────────────────────────┴──────────────────┴─────────────────┘
```

#### Reader’s Schema vs. Writer’s Schema:
- **Writer’s Schema:** Schema used by the application that encoded/wrote the data.
- **Reader’s Schema:** Schema used by the application decoding/reading the data.
- **Avro Schema Resolution:** The Avro library compares Reader’s Schema and Writer’s Schema side-by-side by matching field names.
  - If Writer has a field not in Reader $\to$ Reader ignores it.
  - If Reader expects a field missing in Writer $\to$ Reader fills in default value.

#### How the Reader Knows the Writer’s Schema:
1. **Large Files (Container Files):** Write Writer’s Schema once at the head of the file (e.g., Hadoop HDFS / Avro files).
2. **Database / Stream Records (Kafka):** Include a short 4-byte Schema ID prefix in every record. Resolve Schema ID against a central **Schema Registry**.
3. **Network RPC:** Negotiate schema during connection handshake.

---

## 4. Binary Encoding Trade-Off Matrix

| Feature | JSON / XML | Protocol Buffers / Thrift | Apache Avro |
| :--- | :--- | :--- | :--- |
| **Human Readable** | Yes | No | No |
| **Payload Size** | Large (Includes keys as strings) | Small (Field Tags as varints) | Smallest (Zero metadata/tags in payload) |
| **Schema Requirement** | Optional | Required (`.proto` / `.thrift` files) | Required (`.avsc` JSON schema) |
| **Field Renaming** | Breaks clients if keys change | Safe (Uses integer tags) | Safe (Uses field aliases in Reader Schema) |
| **Code Generation** | Optional | Required / Standard | Optional (Dynamic schema parsing supported) |

---

## 5. Modes of Dataflow

Data flows between processes through three primary mechanisms:

### 1. Dataflow Through Databases
- Writer writes data using current schema version. Reader reads data at a future time.
- **Database Schema Evolution:** A single database may contain records written 5 years ago alongside records written 5 seconds ago.
- **Preserving Unknown Fields:** If an old version of an app reads a record, updates a field, and writes it back, it must NOT erase newly introduced fields written by newer app versions.

### 2. Dataflow Through Services: REST and RPC
- **REST:** HTTP-based, uses JSON/XML, standard verbs (`GET`, `POST`, `PUT`, `DELETE`).
- **RPC (Remote Procedure Call):** Tries to make remote network calls look like local function calls.
  - Examples: **gRPC** (Protobuf over HTTP/2), Apache Thrift RPC, Java RMI.
  - **Problems with Location Transparency:** Network calls are fundamentally different from local function calls (unpredictable latency, packet loss, timeouts, retries, idempotent requirements).

### 3. Asynchronous Message-Passing Systems
- Uses **Message Brokers** (Apache Kafka, RabbitMQ, NATS).
- **Producers** send messages to topics; **Consumers** subscribe to topics.
- **Benefits:** Decouples producer and consumer in time, handles transient consumer downtime, buffers bursts of load.
