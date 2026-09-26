# Chapter 2: Data Models and Query Languages — Deep Dive Notes

> **Core Theme:** How data models shape how we think about problems. Relational vs. Document vs. Graph Data Models, schema-on-read vs schema-on-write, and Declarative vs Imperative query languages.

---

## 1. Relational Model vs. Document Model

```
                    RELATIONAL MODEL                       DOCUMENT MODEL
             ┌─────────────┬─────────────┐          ┌───────────────────────────┐
             │ user_id (PK)│ name        │          │ {                         │
             ├─────────────┼─────────────┤          │   "user_id": 101,         │
             │ 101         │ Alice       │          │   "name": "Alice",        │
             └─────────────┴─────────────┘          │   "positions": [          │
                           │ 1:N Foreign Key        │     {"title": "Staff SWE"}│
                           ▼                        │   ]                       │
             ┌─────────────┬─────────────┐          │ }                         │
             │ pos_id (PK) │ title       │          └───────────────────────────┘
             └─────────────┴─────────────┘          (Self-contained JSON tree)
```

### 1. Relational Model (SQL):
- Organizes data into relations (tables) and tuples (rows).
- **Best for:** Many-to-One and Many-to-Many (N:M) relationships, strict normalization, joins.

### 2. Document Model (NoSQL - MongoDB, CouchDB):
- Organizes data as self-contained tree structures (JSON/BSON documents).
- **Best for:** One-to-Many (1:N) hierarchical data where documents are mostly independent and fetched as a single unit (Impedance Mismatch reduction).

---

## 2. Decision Framework: Document vs. Relational

| Feature | Relational Model (Postgres, MySQL) | Document Model (MongoDB, CouchDB) |
|---|---|---|
| **Data Structure** | Normalized tables connected via Foreign Keys | Denormalized nested JSON trees |
| **Schema Enforcement** | **Schema-on-Write** (Strict DB schema validation on insert) | **Schema-on-Read** (Implicit schema interpreted by application code) |
| **Joins Support** | 🚀 Native, highly optimized join algorithms | 🐢 Weak/Emulated (`$lookup` / application-side joins) |
| **Locality of Reference** | 🐢 Rows stored separately across tables | 🚀 High (Entire record loaded in single continuous disk read) |
| **N:M Relationships** | 🚀 Excellent (Junction tables) | 🐢 Poor (Data duplication or fragmented references) |

---

## 3. Declarative vs. Imperative Query Languages

### Imperative (e.g. C, Java, raw loops):
- You tell the computer **how** to perform the computation step-by-step.
- *Example:* `for (User u : users) { if (u.age > 21) { result.add(u); } }`
- **Disadvantage:** Harder to optimize parallel execution automatically.

### Declarative (e.g. SQL, CSS, Cypher):
- You specify the **pattern of data you want**, leaving the database query optimizer to determine the execution plan (index scans, hash joins, parallel execution).
- *Example:* `SELECT * FROM users WHERE age > 21;`

---

## 4. Graph-Like Data Models

Used when relationships between objects are complex, interconnected, and many-to-many (N:M).

### 1. Property Graph Model (Neo4j / Cypher):
- **Vertices (Nodes):** Unique ID, outgoing/incoming edges, key-value properties.
- **Edges (Relationships):** Unique ID, start vertex, end vertex, label, key-value properties.
- **Cypher Example:**
  ```cypher
  MATCH (person:Person)-[:LIVES_IN]->(city:City)
  WHERE city.name = 'Bangalore'
  RETURN person.name
  ```

### 2. Triple-Store Model (Semantic Web / SPARQL):
- Stores statements as 3-tuples: `(Subject, Predicate, Object)`.
- *Example:* `(Alice, lives_in, Bangalore)`
