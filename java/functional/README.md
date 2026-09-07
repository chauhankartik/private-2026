# Java Functional Programming & Stream API Masterclass Suite

Welcome to the **Java Functional Programming & Stream API Masterclass Suite**. This module provides a staff/principal-engineer level deep dive into Java's functional paradigm—ranging from lambda expressions and `invokedynamic` bytecode mechanics (`java.lang.invoke.LambdaMetafactory`) to built-in and custom Functional Interfaces, Function composition, the Stream API execution model (lazy evaluation, short-circuiting, Spliterators), advanced Collectors (`groupingBy`, `partitioningBy`, `teeing`), custom `Collector` & `Spliterator` algorithms, `Optional<T>` monadic composition, and functional design patterns (Currying, Partial Application, Strategy, Memoization).

---

## Module Sitemap

| File | Type | Description |
| :--- | :--- | :--- |
| **[`README.md`](README.md)** | Index | Master module index, sitemap, functional paradigm matrix, and Category Theory (Functors/Monads) overview. |
| **[`00_Functional_MindMap.md`](00_Functional_MindMap.md)** | Mind Map | Interactive visual Mermaid diagram mapping Functional Interfaces, Lambda Metafactory, Stream Pipelines, Collectors API, Custom Spliterators, Optionals, and FP Patterns. |
| **[`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)** | Resources | Recommended textbooks (*Functional Programming in Java*, *Modern Java in Action*) and OpenJDK source code pointers (`LambdaMetafactory`, `Stream.java`, `Collectors.java`). |
| **[`00_theory.md`](00_theory.md)** | Theory | Exhaustive theoretical guide on Lambdas vs Anonymous Inner Classes, `invokedynamic` bytecode, effectively final variable capture, Stream Sink chaining, Parallel Spliterators, and `Optional` anti-patterns. |
| **[`01_functional_interfaces_lambdas.java`](01_functional_interfaces_lambdas.java)** | Code | Built-in functional interfaces (`Function`, `Consumer`, `Supplier`, `Predicate`, `UnaryOperator`, primitive variants), Method References (4 types), and Function composition (`andThen`, `compose`). |
| **[`02_stream_api_foundations.java`](02_stream_api_foundations.java)** | Code | Stream creation, intermediate vs terminal operations (`map`, `filter`, `distinct`, `sorted`, `reduce`, `collect`), and Lazy Evaluation short-circuiting verification (`findFirst`, `limit`). |
| **[`03_advanced_streams_flatmap_collectors.java`](03_advanced_streams_flatmap_collectors.java)** | Code | `flatMap` nested graph flattening, `Collectors.groupingBy` with downstream aggregations (`counting`, `mapping`, `toSet`), `partitioningBy`, `toMap` merge functions, and `Collectors.teeing` double-aggregation. |
| **[`04_custom_collector_spliterator.java`](04_custom_collector_spliterator.java)** | Code | Advanced custom implementations: Custom `Collector<T, A, R>` (Rolling Statistics Collector) and Custom Parallel `Spliterator<T>` (`trySplit`, `tryAdvance`, `characteristics`). |
| **[`05_optional_monadic_composition.java`](05_optional_monadic_composition.java)** | Code | `Optional<T>` best practices: monadic composition with `map()`, `flatMap()`, `filter()`, Java 9+ `or()`, `ifPresentOrElse()`, `stream()`, avoiding `.get()`, and defensive design. |
| **[`06_functional_design_patterns.java`](06_functional_design_patterns.java)** | Code | Functional design patterns in Java: Function Currying, Partial Application, Lambda Strategy Pattern, Chain of Responsibility via `andThen()`, and Thread-Safe Memoization using `Map.computeIfAbsent()`. |

---

## Streams Architecture: Pipeline Execution Model

Java Stream pipelines do **not** process data collection-by-collection. Instead, they construct a linked chain of **Sink** objects evaluated lazily in a **single pass** during the terminal operation.

```
+-------------------------------------------------------------------------------+
|                             STREAM PIPELINE                                   |
| Source (Collection / Array) -> filter() -> map() -> limit() -> collect()      |
+-------------------------------------------------------------------------------+
                                       |
                   Terminal Operation Invokes Pipeline Execution
                                       v
+-------------------------------------------------------------------------------+
|                            SINK CHAIN EXECUTION                               |
| For each element:                                                             |
|   1. Source Spliterator feeds element to Sink 1 (filter)                      |
|   2. If filter passes -> Sink 1 feeds transformed element to Sink 2 (map)    |
|   3. Sink 2 feeds result to Sink 3 (limit)                                    |
|   4. Sink 3 checks short-circuit condition -> Terminal Collector accumulates  |
+-------------------------------------------------------------------------------+
```

---

## Category Theory in Java: Functors & Monads

| Category Concept | Java Abstraction | Core Operation | Laws Preserved |
| :--- | :--- | :--- | :--- |
| **Functor** | `Stream<T>`, `Optional<T>` | `map(Function<T, R>)` | Identity (`map(x -> x) == x`) & Composition (`map(f.andThen(g)) == map(f).map(g)`) |
| **Monad** | `Optional<T>`, `CompletableFuture<T>` | `flatMap(Function<T, Monad<R>>)` | Left Identity, Right Identity, Associativity |
| **Monoid** | `Collectors.reducing()`, `IntStream.sum()` | Identity value + Associative binary operator | Identity (`x + 0 == x`) & Associativity (`(a+b)+c == a+(b+c)`) |
