# Effective Java (3rd Edition) — 30-Second Item Cheatsheet

Quick-reference summary for all 90 Items in Joshua Bloch's *Effective Java*.

---

## 🟢 Chapter 2: Creating and Destroying Objects (Items 1 – 9)

- **Item 1: Static Factory Methods over Constructors** — Nameable, caching/flyweight support, sub-type return flexibility, non-instantiable callers.
- **Item 2: Builder Pattern for >4 Parameters** — Prevents telescoping constructor anti-pattern; maintains immutability and fluent readability.
- **Item 3: Enum Singletons** — Single-element enum (`enum Instance { INSTANCE; }`) guarantees serialization safety and reflection resistance.
- **Item 4: Noninstantiability via Private Constructor** — Utility classes with static methods must throw `AssertionError` in private constructor.
- **Item 5: Dependency Injection over Hardcoded Resources** — Pass dependencies into constructors instead of instantiating `new Service()` directly.
- **Item 6: Avoid Unnecessary Objects** — Reuse heavy objects (`Pattern.compile`, immutable constants); watch out for implicit autoboxing in loops.
- **Item 7: Eliminate Obsolete Object References** — Null out references in custom stacks/caches, use `WeakHashMap` or explicit lifecycle eviction.
- **Item 8: Avoid Finalizers & Cleaners** — Unpredictable execution, performance drop, security vulnerabilities; use `AutoCloseable` instead.
- **Item 9: Try-with-Resources over Try-Finally** — Automatic resource cleanup, preserves original exception trace (suppressed exceptions).

---

## 🔵 Chapter 3: Methods Common to All Objects (Items 10 – 14)

- **Item 10: Obey `equals()` Contract** — Reflexive, Symmetric, Transitive, Consistent, Non-null. Prefer composition over inheritance for value equality.
- **Item 11: Always Override `hashCode()` with `equals()`** — Equal objects MUST have equal hashCodes. Hash collisions destroy HashMap performance.
- **Item 12: Always Override `toString()`** — Return human-readable key attributes for debugging and logging clarity.
- **Item 13: Override `clone()` Judiciously** — Flawed design interface; prefer copy constructors (`new MyObject(other)`) or static copy factories.
- **Item 14: Implement `Comparable`** — `compareTo` consistent with `equals`. Use static `Comparator.comparingInt()` helpers; avoid integer subtraction overflow.

---

## 🟣 Chapter 4: Classes and Interfaces (Items 15 – 25)

- **Item 15: Minimize Accessibility** — Make package-private by default, private where possible. Public static final arrays are security risks (use `List.of()`).
- **Item 16: Accessors over Public Fields** — Encapsulation permits internal representation changes without breaking callers.
- **Item 17: Minimize Mutability** — Make fields `final` and `private`. Return defensive copies. In Modern Java: Use `record`.
- **Item 18: Composition over Inheritance** — Inheritance breaks encapsulation across boundaries (Fragile Base Class problem). Use Decorator/Wrapper pattern.
- **Item 19: Design/Document for Inheritance or Prohibit It** — Document self-use of overridable methods; prohibit via `final` class or private constructors.
- **Item 20: Interfaces over Abstract Classes** — Permits mix-ins, default methods, non-hierarchical type frameworks.
- **Item 21: Design Interfaces for Posterity** — Default methods can break existing implementers at runtime if contracts conflict.
- **Item 22: Interfaces to Define Types, Not Constants** — Avoid "Constant Interface" anti-pattern; use `public static final` in utility classes or enums.
- **Item 23: Class Hierarchies over Tagged Classes** — Tagged classes are verbose and error-prone. In Modern Java: Use `sealed interface` with pattern matching.
- **Item 24: Static Member Classes over Nonstatic** — Nonstatic inner classes keep an implicit reference to outer instance causing memory leaks.
- **Item 25: Single Top-Level Class per Source File** — Multiple top-level classes in one file cause duplicate symbol compilation errors.

---

## 🟡 Chapter 5: Generics (Items 26 – 33)

- **Item 26: Don't Use Raw Types** — Raw types disable compile-time type checks and lead to runtime `ClassCastException`.
- **Item 27: Eliminate Unchecked Warnings** — Suppress warnings (`@SuppressWarnings("unchecked")`) only on smallest scope with written justification.
- **Item 28: Prefer Lists to Arrays** — Arrays are covariant and reified; Lists are invariant and erased. Prefer compile-time list safety.
- **Item 29: Favor Generic Types** — Parameterize custom data structures (`Stack<E>`) to eliminate client casts.
- **Item 30: Favor Generic Methods** — Parameterize utility methods (`public static <E> Set<E> union(...)`).
- **Item 31: Use Bounded Wildcards (PECS)** — **Producer Extends, Consumer Super**. Flex API input (`? extends T`) and output (`? super T`).
- **Item 32: Generics and Varargs** — Use `@SafeVarargs` only when varargs array is not modified or leaked outside method.
- **Item 33: Typesafe Heterogeneous Containers** — Use `Class<T>` keys in map (`Map<Class<?>, Object>`) for dynamic type safety.

---

## 🟠 Chapter 6: Enums and Annotations (Items 34 – 41)

- **Item 34: Enums over `int` Constants** — Compile-time type safety, readable names, namespace isolation, behavior attachment via constant-specific methods.
- **Item 35: Instance Fields over Ordinals** — Never derive values from `.ordinal()`; store values in private final instance fields.
- **Item 36: `EnumSet` over Bit Fields** — High performance (bit-vector operations under hood) + type safety and enum iteration.
- **Item 37: `EnumMap` over Ordinal Indexing** — Fast array-backed lookup without unsafe array casts.
- **Item 38: Extensible Enums via Interfaces** — Enums cannot inherit, but can implement interfaces to emulate open type hierarchies (e.g., Opcodes).
- **Item 39: Annotations over Naming Patterns** — JUnit 4+ `@Test` annotations replace legacy `testMethodName()` constraints.
- **Item 40: Consistently Use `@Override`** — Prevents subtle bugs caused by accidentally overloading instead of overriding.
- **Item 41: Marker Interfaces to Define Types** — Marker interfaces (`Serializable`, `Cloneable`) define compile-time types; marker annotations target metadata.

---

## 🔴 Chapter 7: Lambdas and Streams (Items 42 – 48)

- **Item 42: Lambdas over Anonymous Classes** — Concise syntax for single-abstract-method types; omit explicit parameter types when inferred.
- **Item 43: Method References over Lambdas** — Cleaner code (`String::length` vs `s -> s.length()`), unless lambda is shorter/clearer.
- **Item 44: Standard Functional Interfaces** — Reuse `Function<T,R>`, `Predicate<T>`, `Supplier<T>`, `Consumer<T>` before defining custom interfaces.
- **Item 45: Streams Judiciously** — Do not over-use streams where traditional loops/recursion are clearer.
- **Item 46: Side-Effect Free Functions in Streams** — `forEach` should only report results, not mutate state. Use `Collectors.groupingBy()`, `toList()`.
- **Item 47: Collection over Stream as Return Type** — Return `Collection<T>` or `List<T>` to allow callers to loop OR stream easily.
- **Item 48: Parallel Streams Caution** — Only parallelize when data structure splits easily (`ArrayList`, range) and computation cost per item is high.

---

## 🟤 Chapter 8: Methods (Items 49 – 56)

- **Item 49: Check Parameters for Validity** — Fail-fast with `Objects.requireNonNull()`, throw `IllegalArgumentException` early.
- **Item 50: Make Defensive Copies** — Copy mutable constructor parameters and return copies of internal mutable fields.
- **Item 51: Design Signatures Carefully** — Avoid long parameter lists (>4); use helper objects, enums over booleans, interfaces over concrete types.
- **Item 52: Use Overloading Judiciously** — Overloading choice is static (compile-time); Overriding is dynamic (runtime). Avoid ambiguous overloads.
- **Item 53: Use Varargs Judiciously** — Require at least 1 explicit parameter when needed (`fn(T first, T... rest)`).
- **Item 54: Return Empty Collections, Not Nulls** — Return `Collections.emptyList()` instead of `null` to avoid client NPE checks.
- **Item 55: Return `Optional` Judiciously** — Use for methods that might return no result. Never wrap Collections in Optional; never use as field/key.
- **Item 56: Write Javadoc for Exposed APIs** — Document pre-conditions, post-conditions, side-effects, exceptions thrown (`@throws`).

---

## ⚪ Chapter 9: General Programming (Items 57 – 68)

- **Item 57: Minimize Scope of Local Variables** — Declare variables where first used; initialize immediately in for-loops.
- **Item 58: For-Each Loops over Traditional Loops** — Eliminates index bugs and iterator clutter.
- **Item 59: Know and Use Libraries** — Prefer standard library (`java.util.Random`, `java.lang.Math`, `java.util.Objects`) over custom implementations.
- **Item 60: Avoid `float` & `double` for Exact Answers** — Use `BigDecimal` or `int`/`long` (cents) for monetary calculations.
- **Item 61: Primitives over Boxed Primitives** — Primitive comparison uses value; boxed uses reference (`==`). Beware performance hit of auto-unboxing NPEs.
- **Item 62: Avoid String Abuse** — Don't use strings for enum replacements, compound keys, or thread-local storage keys.
- **Item 63: Beware String Concatenation** — Use `StringBuilder` in loops; `+` generates $O(n^2)$ allocation overhead.
- **Item 64: Refer to Objects by Interfaces** — Declare variables as `List<String> list = new ArrayList<>()`.
- **Item 65: Interfaces over Reflection** — Reflection loses compile-time safety and suffers 10-100x performance penalty. Use only for loading classes at runtime.
- **Item 66: Use Native Methods Judiciously** — JNI adds memory corruption risk, platform dependence, and bridge call overhead.
- **Item 67: Optimize Judiciously** — "Premature optimization is the root of all evil." Design good architecture first; measure before optimizing.
- **Item 68: Naming Conventions** — Follow standard Java code style conventions (camelCase, PascalCase, UPPER_CASE).

---

## 🔴 Chapter 10: Exceptions (Items 69 – 77)

- **Item 69: Exceptions ONLY for Exceptional Conditions** — Don't use try-catch blocks for normal control flow or loop bounds.
- **Item 70: Checked for Recoverable, Runtime for Programming Errors** — Checked exceptions force caller handling; Runtime exceptions denote API misuse.
- **Item 71: Avoid Unnecessary Checked Exceptions** — If client cannot recover, use unchecked exception or return `Optional`.
- **Item 72: Use Standard Exceptions** — Reuse `IllegalArgumentException`, `IllegalStateException`, `NullPointerException`, `UnsupportedOperationException`.
- **Item 73: Exception Translation / Layering** — Catch lower-level exceptions and throw higher-level domain exceptions (`IndexOutOfBoundsException` → `NoSuchElementException`).
- **Item 74: Document All Thrown Exceptions** — Document checked and unchecked exceptions with `@throws` in Javadoc.
- **Item 75: Failure-Capture Information** — Include relevant state parameters in exception detail message for root-cause diagnosis.
- **Item 76: Strive for Failure Atomicity** — Method failure should leave object state unchanged (immutable state, validation before mutation, rollback).
- **Item 77: Don't Ignore Exceptions** — Empty catch blocks mask severe runtime bugs. If intentionally ignored, log it and comment why.

---

## 🟢 Chapter 11: Concurrency (Items 78 – 84)

- **Item 78: Synchronize Shared Mutable Data** — Synchronization guarantees both mutual exclusion and visibility (`volatile` provides visibility only).
- **Item 79: Avoid Excessive Synchronization** — Never call alien (overridable/listener) methods inside synchronized blocks (deadlock risk).
- **Item 80: Executors, Tasks & Virtual Threads** — Use `ExecutorService` instead of manual `Thread` management. Java 21+: Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- **Item 81: Concurrency Utilities over `wait`/`notify`** — Use `ConcurrentHashMap`, `BlockingQueue`, `CountDownLatch`, `CompletableFuture` instead of raw monitors.
- **Item 82: Document Thread Safety** — Clearly document thread safety guarantees (immutable, thread-safe, conditionally thread-safe, non-thread-safe).
- **Item 83: Lazy Initialization Judiciously** — Initialization-on-demand holder idiom for static fields; Double-checked locking with `volatile` for instance fields.
- **Item 84: Don't Depend on Thread Scheduler** — Don't rely on `Thread.yield()` or priority tweaks for correctness.

---

## 🔵 Chapter 12: Serialization (Items 85 – 90)

- **Item 85: Alternatives to Java Serialization** — Java native serialization is unsafe (RCE attacks). Prefer JSON, Protocol Buffers, or Jackson.
- **Item 86: Implement `Serializable` with Caution** — Decreases flexibility, opens security surface, breaks encapsulation.
- **Item 87: Custom Serialized Form** — Write custom `writeObject`/`readObject` if default form couples to internal implementation detail.
- **Item 88: Defensive `readObject()`** — Treat `readObject` as a public constructor; validate invariant and make defensive copies of mutable fields.
- **Item 89: Enum Types over `readResolve` for Singletons** — `readResolve` on ordinary classes is vulnerable to attack; enum singletons are immune.
- **Item 90: Serialization Proxy Pattern** — Write a private static nested class proxy to represent serialized state safely.
