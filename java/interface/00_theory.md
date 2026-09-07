# Java Interfaces — Complete Theory & API Guide
> **Study goal:** Understand interfaces from the ground up — contracts, default methods,
> functional interfaces, and how modern Java uses them for lambdas and streams.

---

## 1. What Is an Interface?

An interface is a **pure contract** — it declares *what* a class must do, not *how* to do it.

```java
interface Drawable {
    void draw();          // implicitly: public abstract
    double area();        // implicitly: public abstract
}
```

Any class that `implements Drawable` must provide concrete implementations for both methods.  
The caller only knows about `Drawable` — the concrete type is hidden behind the contract.

---

## 2. Interface vs Abstract Class

| Feature | Interface | Abstract Class |
|---|---|---|
| Multiple inheritance | ✓ (a class can implement many) | ✗ (single extends only) |
| Instance fields | ✗ (only `public static final` constants) | ✓ |
| Constructors | ✗ | ✓ |
| `default` methods (Java 8+) | ✓ | ✓ |
| `static` methods (Java 8+) | ✓ | ✓ |
| `private` methods (Java 9+) | ✓ | ✓ |
| IS-A relationship | ✓ | ✓ |
| **Use when** | Defining a capability/role | Sharing state + partial implementation |

**Rule of thumb:**  
- Interface → "*can do*" / capability: `Comparable`, `Runnable`, `Serializable`  
- Abstract class → "*is a*" / shared base: `AbstractList`, `HttpServlet`

---

## 3. Interface Member Types (Java 8+)

```java
interface MyInterface {
    // 1. Abstract method (implicitly public abstract)
    void doWork();

    // 2. Default method (Java 8) — provides a body; subclasses can override
    default void log(String msg) {
        System.out.println("[LOG] " + msg);
    }

    // 3. Static method (Java 8) — belongs to the interface, not implementors
    static MyInterface noOp() { return () -> {}; }

    // 4. Private method (Java 9) — shared helper between default methods
    private void helper() { System.out.println("internal"); }

    // 5. Constant (implicitly public static final)
    int VERSION = 1;    // same as: public static final int VERSION = 1;
}
```

---

## 4. `default` Methods — Why They Exist

Before Java 8, adding a method to an interface **broke all implementations**.  
`default` methods allow adding new behavior to an interface without breaking existing code.

```java
interface Collection<E> {
    // Pre-Java 8 — would break all custom Collection implementations
    // void forEach(Consumer<? super E> action);

    // Java 8+ — safe to add because it has a default body
    default void forEach(Consumer<? super E> action) { ... }
}
```

### Default Method Conflict Resolution
When a class inherits the same `default` method from multiple interfaces:
```java
interface A { default void greet() { System.out.println("Hello from A"); } }
interface B { default void greet() { System.out.println("Hello from B"); } }

class C implements A, B {
    // MUST override — compiler forces you to resolve the ambiguity
    @Override
    public void greet() {
        A.super.greet();   // explicitly call A's version
        // or B.super.greet();
    }
}
```

---

## 5. Functional Interfaces & Lambdas

A **functional interface** has **exactly one abstract method**.  
It can be implemented with a lambda expression.

```java
@FunctionalInterface     // optional annotation — compiler enforces 1 abstract method
interface Transformer<T, R> {
    R transform(T input);

    // default and static methods are allowed — don't count as abstract
    default Transformer<T, R> andLog() { return input -> { log(input); return transform(input); }; }
    private void log(T input)          { System.out.println("transforming: " + input); }
}

// Usage with lambda
Transformer<String, Integer> strLen = s -> s.length();
System.out.println(strLen.transform("hello"));  // 5
```

### Built-in Functional Interfaces (java.util.function)

| Interface | Signature | Use case |
|---|---|---|
| `Predicate<T>` | `boolean test(T t)` | Filter / condition |
| `Function<T,R>` | `R apply(T t)` | Transform / map |
| `Consumer<T>` | `void accept(T t)` | Side effects / forEach |
| `Supplier<T>` | `T get()` | Lazy factory / provide value |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | Two-input transform |
| `UnaryOperator<T>` | `T apply(T t)` | Transform to same type |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | Combine two values |
| `Comparator<T>` | `int compare(T o1, T o2)` | Ordering |
| `Runnable` | `void run()` | No-arg, no-return |
| `Callable<V>` | `V call()` | No-arg, returns value, throws |

---

## 6. Marker Interfaces

Interfaces with **zero methods** — they tag a class with a capability.

```java
interface Serializable {}    // signals: this object can be serialized
interface Cloneable {}       // signals: clone() is permitted
interface RandomAccess {}    // signals: O(1) indexed access
```

Modern Java prefers **annotations** (`@Entity`, `@Service`) for marking, but marker interfaces still appear in the JDK for type-safety (you can use `instanceof Serializable`).

---

## 7. Interface Inheritance

Interfaces can extend other interfaces (even multiple):

```java
interface Readable  { String read(); }
interface Writable  { void write(String data); }
interface ReadWrite extends Readable, Writable {
    // inherits both abstract methods, can add more
    default void copy(ReadWrite other) { other.write(this.read()); }
}
```

---

## 8. Comparable vs Comparator

The two most important interfaces for ordering — asked in nearly every interview.

```java
// Comparable<T> — NATURAL ordering — implemented BY the class itself
class Student implements Comparable<Student> {
    int gpa;
    @Override
    public int compareTo(Student other) {
        return Double.compare(this.gpa, other.gpa);  // ascending GPA
    }
}

// Comparator<T> — CUSTOM ordering — defined OUTSIDE the class
Comparator<Student> byName = Comparator.comparing(s -> s.name);
Comparator<Student> byGpaDesc = Comparator.comparingDouble((Student s) -> s.gpa).reversed();
Comparator<Student> byNameThenGpa = byName.thenComparing(byGpaDesc);

// Usage
List<Student> list = ...;
Collections.sort(list);                      // uses Comparable (natural order)
list.sort(byNameThenGpa);                    // uses Comparator (custom order)
```

---

## 9. Common Java Interface Patterns

```java
// Strategy Pattern — swap algorithms via interface
interface SortStrategy { void sort(int[] arr); }
class QuickSort   implements SortStrategy { ... }
class MergeSort   implements SortStrategy { ... }
class Sorter {
    private SortStrategy strategy;
    void setStrategy(SortStrategy s) { this.strategy = s; }
    void sort(int[] arr) { strategy.sort(arr); }
}

// Builder via interface (fluent API)
interface Builder<T> { T build(); }

// Repository Pattern (common in Spring)
interface UserRepository {
    Optional<User> findById(long id);
    List<User> findAll();
    void save(User user);
}
// Implementation injected at runtime → easy to mock in tests
```

---

*Next → [01_basics.java](01_basics.java) — Defining interfaces, implementing, default methods*
*→ [02_functional.java](02_functional.java) — Functional interfaces, lambdas, built-ins*
*→ [03_advanced.java](03_advanced.java) — Comparable/Comparator, generics, real-world patterns*
