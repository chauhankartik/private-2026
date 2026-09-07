# Java Generics — Complete Theory & API Guide
> **Study goal:** Understand generics from syntax to type erasure, wildcards,
> bounded types, and how the JDK Collections framework uses them.

---

## 1. What Are Generics?

Generics let you write **type-safe, reusable code** without casting.

```java
// Without generics (pre-Java 5) — not type-safe
List list = new ArrayList();
list.add("hello");
String s = (String) list.get(0);  // cast required — ClassCastException at RUNTIME

// With generics — type checked at COMPILE time
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0);   // no cast needed — compiler enforces type
list.add(42);             // ← COMPILE ERROR — type safety at compile time ✓
```

Generics move type errors from **runtime** (`ClassCastException`) to **compile time**.

---

## 2. Where Generics Appear

```java
// Generic CLASS
class Box<T> { T value; }

// Generic INTERFACE
interface Comparable<T> { int compareTo(T other); }

// Generic METHOD (type parameter declared before return type)
<T> List<T> repeat(T item, int times) { ... }

// Generic CONSTRUCTOR (rare)
<T> MyClass(T value) { ... }
```

---

## 3. Type Parameters — Conventions

| Letter | Convention | Example |
|---|---|---|
| `T` | Type (general) | `class Box<T>` |
| `E` | Element (collections) | `class List<E>` |
| `K` | Key (maps) | `class Map<K,V>` |
| `V` | Value (maps) | `class Map<K,V>` |
| `R` | Return type | `Function<T,R>` |
| `N` | Number | `class Counter<N extends Number>` |
| `S,U,V` | Second, third, fourth types | `BiFunction<T,U,R>` |

---

## 4. Bounded Type Parameters

```java
// Upper bound: T must be a Number or subclass
<T extends Number> double sum(List<T> list) { ... }
// Usage: sum(List.of(1, 2, 3))  ✓   sum(List.of("a"))  ✗

// Multiple bounds: T must extend A AND implement B and C
<T extends Comparable<T> & Cloneable & Serializable> void sort(T[] arr) { ... }
// First bound can be a class; rest must be interfaces

// No bound = implicitly <T extends Object>
```

---

## 5. Wildcards — The Hardest Part

Wildcards (`?`) represent an unknown type. They appear in TYPE POSITIONS, not type declarations.

### Unbounded Wildcard: `<?>`
```java
void printList(List<?> list) { ... }
// Can read as Object; cannot add (except null)
```

### Upper-Bounded Wildcard: `<? extends T>`
```java
double sum(List<? extends Number> list) {
    return list.stream().mapToDouble(Number::doubleValue).sum();
}
// Can READ elements as Number; CANNOT add (type unknown)
```

### Lower-Bounded Wildcard: `<? super T>`
```java
void addNumbers(List<? super Integer> list) {
    list.add(1); list.add(2);  // can add Integer
}
// Can WRITE Integers; can only read as Object
```

---

## 6. PECS — Producer Extends, Consumer Super

The mnemonic for choosing wildcards:

```
If the generic structure PRODUCES values for you → use ? extends T
If the generic structure CONSUMES values you give it → use ? super T
```

```java
// Collections.copy(dest, src):
//   src PRODUCES elements → ? extends T
//   dest CONSUMES elements → ? super T
<T> void copy(List<? super T> dest, List<? extends T> src)

// Real examples:
List<Integer> ints    = List.of(1, 2, 3);
List<Number> numbers  = new ArrayList<>();
List<Object> objects  = new ArrayList<>();

Collections.copy(numbers, ints);  // ✓  dest=? super Integer, src=? extends Integer
Collections.copy(objects, ints);  // ✓  works because Object super Integer
```

---

## 7. Type Erasure

Generics are a **compile-time** feature. At runtime, type parameters are **erased**.

```java
// Compile time:        Runtime (after erasure):
List<String>      →    List
List<Integer>     →    List
Box<T>            →    Box (T replaced by Object or first bound)
Box<T extends Comparable<T>> → Box (T replaced by Comparable)
```

### Consequences:

```java
// You CANNOT do at runtime:
new T()                          // ✗ don't know T's constructor
new T[10]                        // ✗ can't create generic array
list instanceof List<String>     // ✗ type info erased — always List at runtime
T.class                          // ✗ no such thing

// You CAN do (preserved in bytecode declarations):
class Box<T> { T value; }
T getGenericType() via Reflection (getGenericReturnType())  // ✓ preserved in signatures
```

### Reifiable vs Non-Reifiable Types

| Type | Reifiable? | Meaning |
|---|---|---|
| `int`, `String`, `Object` | ✓ | Type fully available at runtime |
| `List<String>` | ✗ | Type argument erased — just `List` at runtime |
| `List<?>` | ✓ | Unbounded wildcard is reifiable |
| `String[]` | ✓ | Array component type is reifiable |

---

## 8. Generic Arrays — Why They're Restricted

```java
// Arrays are COVARIANT: String[] is a subtype of Object[]
Object[] arr = new String[3];
arr[0] = 42;  // ArrayStoreException at runtime — type check in array

// Generics are INVARIANT: List<String> is NOT a subtype of List<Object>
List<Object> list = new ArrayList<String>();  // ✗ COMPILE ERROR (good!)

// This is why you can't create generic arrays:
T[] arr = new T[10];  // ✗ — if allowed, type erasure would break array store check
// Use: Object[] arr = new Object[10]; and cast when needed
// Or better: List<T> — which works fine with generics
```

---

## 9. Common Generic Patterns in JDK

```java
// java.util.function — all generic
Function<String, Integer>      → R apply(T t)
BiFunction<T, U, R>            → R apply(T t, U u)
Predicate<T>                   → boolean test(T t)
Supplier<T>                    → T get()
Consumer<T>                    → void accept(T t)

// java.util.Optional
Optional<T>.map(Function<T,R>) → Optional<R>
Optional<T>.flatMap(Function<T, Optional<R>>) → Optional<R>

// java.util.Comparator
Comparator.comparing(Function<T, U>)  → Comparator<T>
Comparator<T>.thenComparing(...)       → Comparator<T>

// Streams
Stream<T>.map(Function<T,R>)           → Stream<R>
Stream<T>.filter(Predicate<T>)         → Stream<T>
Stream<T>.collect(Collector<T,A,R>)    → R
```

---

## 10. Generics vs Raw Types

```java
// Raw type (pre-generics, or accidental): avoid in new code
List raw = new ArrayList();       // unchecked warning
raw.add("hello");
raw.add(42);                      // no compile error — dangerous!

// Proper generic type: always prefer
List<String> typed = new ArrayList<>();
typed.add("hello");
typed.add(42);                    // ← compile error ✓

// Raw types disable generic type checking for the ENTIRE expression
// Even if the raw List internally holds Strings, you lose all type info
```

---

## 11. Interview Question Summary

| Question | Answer |
|---|---|
| What is type erasure? | Generic type parameters are removed at runtime; `List<String>` becomes `List` |
| Why can't you do `new T()`? | T is erased at runtime; no type info to call the constructor |
| `List<Dog>` vs `List<Animal>`? | Not subtypes; generics are invariant (use `? extends Animal`) |
| When to use `? extends`? | Reading from a generic structure (producer) |
| When to use `? super`? | Writing to a generic structure (consumer) |
| What is reification? | A type whose info is fully available at runtime (array, non-generic class) |
| Heap pollution? | When a variable of parameterized type refers to an object of a different type (`@SafeVarargs`) |

---

*Next:*
- `01_basics.java` — Generic classes, methods, constructors, bounded types
- `02_wildcards.java` — Unbounded, upper-bounded, lower-bounded, PECS
- `03_advanced.java` — Type erasure, bridge methods, generic arrays, real-world patterns
