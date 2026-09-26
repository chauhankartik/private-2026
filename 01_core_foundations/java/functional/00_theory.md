# Java Functional Programming & Stream API Exhaustive Theory Guide

---

## 1. Fundamentals of Functional Programming in Java

Functional Programming (FP) is a paradigm that treats computation as the evaluation of mathematical functions while avoiding state mutation and side effects.

### Core Pillars of FP:
1. **First-Class & Higher-Order Functions:** Functions can be assigned to variables, passed as arguments, and returned from other functions.
2. **Pure Functions:** Given the same inputs, a pure function always produces the exact same output and causes zero side-effects (no mutating global state, I/O, or instance fields).
3. **Referential Transparency:** An expression can be replaced with its evaluated result value without changing the program's behavior.
4. **Immutability:** Data structures cannot be modified after creation. Operations return new immutable copies.

---

## 2. Lambdas vs Anonymous Classes: `invokedynamic` Bytecode

Prior to Java 8, behavior parameterization required **Anonymous Inner Classes**. Java 8 introduced **Lambdas** using the `invokedynamic` JVM bytecode instruction.

```
Anonymous Inner Class (Java 7):
  Compiled to: OuterClass$1.class (Generates physical .class file on disk)
  Memory: Instantiates new outer-referencing object on heap per call.

Lambda Expression (Java 8+):
  Compiled to: invokedynamic call site invoking LambdaMetafactory
  Memory: Dynamic synthetic method created via MethodHandles at runtime (Zero .class file generation overhead).
```

### Variable Capture & "Effectively Final" Rule
Lambdas can capture variables from their enclosing scope. However:
- Captured local variables must be **effectively final** (never reassigned after initialization).
- *Reason:* Lambdas capture the **value** of primitive/reference variables, not the variable location. Allowing mutation would introduce subtle race conditions across concurrent threads.

---

## 3. The Functional Interface Taxonomy (`java.util.function`)

A **Functional Interface** contains exactly one abstract method (`SAM` - Single Abstract Method). Annotating with `@FunctionalInterface` causes `javac` to enforce this constraint.

```
                    +------------------------------------+
                    |        FUNCTIONAL INTERFACES       |
                    +------------------------------------+
                     /         |               |        \
                    v          v               v         v
             Function<T,R>  Predicate<T>  Consumer<T>  Supplier<T>
              T -> R        T -> boolean   T -> void   () -> T
```

### Core Functional Interfaces:

| Interface | Method Signature | Conceptual Operation |
| :--- | :--- | :--- |
| `Function<T, R>` | `R apply(T t)` | Transformation: Map input `T` to output `R` |
| `BiFunction<T, U, R>` | `R apply(T t, U u)` | Transformation: Map two inputs `T, U` to output `R` |
| `Predicate<T>` | `boolean test(T t)` | Condition Evaluation: Filter check returning boolean |
| `Consumer<T>` | `void accept(T t)` | Action Execution: Consume input `T` with side-effect |
| `Supplier<T>` | `T get()` | Factory / Producer: Return value `T` lazily |
| `UnaryOperator<T>` | `T apply(T t)` | Specialized `Function<T, T>` (Same input and output type) |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | Specialized `BiFunction<T, T, T>` (Combine two `T` values) |

### Primitive Specializations
To prevent auto-boxing performance overhead (`int` \(\to\) `Integer`), Java provides unboxed functional interfaces:
- `IntFunction<R>`, `ToIntFunction<T>`, `IntPredicate`, `IntConsumer`, `IntSupplier`, `IntUnaryOperator`, `IntBinaryOperator`.
- Similar variants exist for `Long` and `Double`.

---

## 4. Method References (`::`)

Method references provide compact, readable shorthand syntax for lambdas that simply invoke an existing method.

| Type | Lambda Syntax | Method Reference Syntax |
| :--- | :--- | :--- |
| **Static Method** | `(x) -> Math.abs(x)` | `Math::abs` |
| **Instance Method of Arbitrary Object** | `(str) -> str.toUpperCase()` | `String::toUpperCase` |
| **Instance Method of Existing Object** | `(x) -> consolePrinter.println(x)` | `consolePrinter::println` |
| **Constructor Reference** | `() -> new ArrayList<>()` | `ArrayList::new` |

---

## 5. Stream API Architecture & Execution Mechanics

A `Stream<T>` is a sequence of elements supporting sequential and parallel aggregate operations. Streams do **not** store data; they convey elements from a source through a pipeline of operations.

### Stream Pipeline Phases:
1. **Source:** Collections, Arrays, I/O Channels, Generator Functions (`Stream.iterate()`).
2. **Intermediate Operations (Lazy):** Transform a stream into another stream (`map`, `filter`, `flatMap`, `sorted`). Executed lazily when a terminal operation is invoked.
3. **Terminal Operation (Eager):** Triggers pipeline execution and produces a result or side-effect (`collect`, `reduce`, `forEach`, `count`, `findFirst`).

### Stateless vs Stateful Operations
- **Stateless Operations (`filter`, `map`, `flatMap`):** Each element is processed independently without retaining state from previously processed elements. Ideal for parallelization.
- **Stateful Operations (`sorted`, `distinct`, `limit`, `skip`):** Require knowledge of all or previous elements before producing a result. Requires buffering in memory.

---

## 6. Parallel Streams & Custom Spliterators

Parallel streams partition work across CPU cores using the JVM **Common ForkJoinPool**.

### Spliterator Contract (`java.util.Spliterator`)
A `Spliterator` ("Splittable Iterator") traverses and partitions elements of a data source for parallel execution:
- **`boolean tryAdvance(Consumer<? super T> action)`:** Consumes the next single element sequentially.
- **`Spliterator<T> trySplit()`:** Splits the source into two independent `Spliterator` chunks for parallel worker threads.
- **`long estimateSize()`:** Returns estimated remaining element count.
- **`int characteristics()`:** Returns bitmask flags indicating characteristics (`ORDERED`, `DISTINCT`, `SORTED`, `SIZED`, `NONNULL`, `IMMUTABLE`, `CONCURRENT`, `SUBSIZED`).

---

## 7. Custom Collectors API (`Collector<T, A, R>`)

A `Collector` encapsulates the reduction strategy into a mutable container:

1. **`Supplier<A> supplier()`:** Factory creating the initial mutable result container `A`.
2. **`BiConsumer<A, T> accumulator()`:** Incorporates a new element `T` into the result container `A`.
3. **`BinaryOperator<A> combiner()`:** Merges two partial result containers `A` during parallel processing.
4. **`Function<A, R> finisher()`:** Transforms intermediate container `A` into final result `R`.
5. **`Set<Characteristics> characteristics()`:** Optimization flags (`CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`).

---

## 8. Monadic `Optional<T>` & Anti-Patterns

`Optional<T>` is a monadic container that represents the presence or absence of a non-null value.

### Defensive `Optional` Guidelines:
- **DO NOT** use `Optional.get()` without `isPresent()` check. Prefer `orElse()`, `orElseGet()`, `orElseThrow()`.
- **DO NOT** use `Optional` for method parameters or class fields (incurs 16–24 bytes object wrapper overhead per instance). Use `Optional` exclusively as a **return type** for methods that may legitimately return no value.
- **DO** use `orElseGet(Supplier)` over `orElse(defaultVal)` when default value creation is computationally expensive (e.g. database/network calls).
