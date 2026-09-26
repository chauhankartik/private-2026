# Chapter 3: Mapping Strategies & Inheritance Models

Object-Relational Mapping (ORM) maps object-oriented constructs (associations, embedded components, polymorphic inheritance) to relational database schemas.

---

## 1. Primary Key Generation Strategies & Batching Traps

Choosing the correct `@GeneratedValue` strategy directly determines JDBC batch insert performance.

```
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
@SequenceGenerator(name = "order_seq", sequenceName = "order_sequence", allocationSize = 50)
private Long id;
```

### 1.1 Generation Strategies Comparison

| Strategy | DB Mechanism | Batch Insert Support | Performance SLA |
| :--- | :--- | :--- | :--- |
| **`SEQUENCE`** | Database Sequence object (PostgreSQL, Oracle). | **Supported** (via `pooled-lo` allocation optimizer). | **Optimal**. Allocates blocks of 50 IDs per DB roundtrip. |
| **`IDENTITY`** | Auto-Increment Column (MySQL `AUTO_INCREMENT`). | **DISABLED**. | **Poor**. Forces immediate `INSERT` on `persist()` to fetch ID. |
| **`TABLE`** | Simulated sequence via DB lookup table. | Supported. | Very Poor (High row lock contention). |
| **`UUID`** | 128-bit UUID generated locally in RAM. | **Supported**. | Excellent (Zero DB network roundtrips for ID). |

---

## 2. Component Mappings: `@Embeddable` & `@Embedded`

Value objects without an independent database identity are mapped using `@Embeddable`.

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String zipCode;
}

@Entity
public class User {
    @Id @GeneratedValue
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "city", column = @Column(name = "home_city"))
    })
    private Address homeAddress;
}
```

---

## 3. Inheritance Mapping Strategies

JPA supports three primary strategies for mapping class hierarchies to database tables:

```
                          [ Payment (Base Class) ]
                                 /        \
            [ CreditCardPayment ]          [ BankTransferPayment ]
```

### 3.1 `SINGLE_TABLE` (Default)
All classes in the hierarchy map to a **single unified table** containing a `@DiscriminatorColumn`.

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    payment_type VARCHAR(31) NOT NULL, -- Discriminator ('CREDIT', 'BANK')
    amount DECIMAL(19,2),
    card_number VARCHAR(16),           -- CreditCard only (Must be NULLABLE!)
    routing_number VARCHAR(20)         -- BankTransfer only (Must be NULLABLE!)
);
```
* **Pros:** Fastest query performance (Zero JOINs required for polymorphic queries).
* **Cons:** Subclass-specific columns **must be nullable**, breaking `NOT NULL` database constraints.

### 3.2 `JOINED`
Base class maps to a primary table; each subclass maps to a separate table joined via foreign key.

```sql
CREATE TABLE payments (id BIGINT PRIMARY KEY, amount DECIMAL(19,2));
CREATE TABLE credit_card_payments (id BIGINT PRIMARY KEY REFERENCES payments(id), card_number VARCHAR(16) NOT NULL);
CREATE TABLE bank_transfer_payments (id BIGINT PRIMARY KEY REFERENCES payments(id), routing_number VARCHAR(20) NOT NULL);
```
* **Pros:** Fully normalized schema; permits `NOT NULL` constraints on subclass fields.
* **Cons:** Polymorphic queries require `OUTER JOIN` across all subclass tables.

### 3.3 `TABLE_PER_CLASS`
Each concrete subclass maps to a complete standalone table containing all inherited + specific attributes.
* **Pros:** No JOINs needed when querying specific concrete subclasses.
* **Cons:** Polymorphic queries on base class (`SELECT p FROM Payment p`) execute expensive `UNION ALL` subqueries across every subclass table.

---

## 4. Staff Engineer Mapping SLA Framework
1. **Never Use `IDENTITY` Generation for Batch Workloads:** In MySQL/MariaDB, `IDENTITY` forces immediate `INSERT` execution on `persist()`, bypassing `hibernate.jdbc.batch_size`. Use `UUID` or `SEQUENCE` (where supported) for batch inserts.
2. **Prefer `SINGLE_TABLE` for Performance-Critical Hierarchies:** Choose `SINGLE_TABLE` when polymorphic queries are frequent and performance is paramount.
3. **Use Pooled Sequence Optimizers:** Set `allocationSize = 50` or `100` on sequence generators to fetch ID blocks, reducing sequence query overhead by 98%.
