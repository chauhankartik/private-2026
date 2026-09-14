# Chapter 7: Lambdas and Streams (Items 42 – 48)

Best practices for functional programming features introduced in Java 8.

---

## 📌 Item 42: Prefer lambdas to anonymous classes

Lambdas eliminate boilerplate when passing code as arguments for single-abstract-method (SAM) interfaces.

```java
// ❌ Verbose Anonymous Class
Collections.sort(words, new Comparator<String>() {
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
});

// ✅ Concise Lambda with Inferred Types
Collections.sort(words, (s1, s2) -> Integer.compare(s1.length(), s2.length()));
```

---

## 📌 Item 43: Prefer method references to lambdas

Method references (`String::length`) are usually shorter and clearer than lambdas (`s -> s.length()`).

| Method Reference Type | Syntax | Lambda Equivalent |
|---|---|---|
| Static | `Integer::parseInt` | `str -> Integer.parseInt(str)` |
| Bound Instance | `Instant.now()::isAfter` | `t -> Instant.now().isAfter(t)` |
| Unbound Instance | `String::toLowerCase` | `str -> str.toLowerCase()` |
| Constructor | `TreeMap::new` | `() -> new TreeMap<>()` |

---

## 📌 Item 44: Favor the use of standard functional interfaces

Reuse `java.util.function` standard interfaces instead of creating custom functional interfaces:

| Interface | Method Signature | Example Use |
|---|---|---|
| `UnaryOperator<T>` | `T apply(T t)` | `String::toLowerCase` |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | `BigInteger::add` |
| `Predicate<T>` | `boolean test(T t)` | `Collection::isEmpty` |
| `Function<T,R>` | `R apply(T t)` | `Arrays::asList` |
| `Supplier<T>` | `T get()` | `Instant::now` |
| `Consumer<T>` | `void accept(T t)` | `System.out::println` |

---

## 📌 Item 45: Use streams judiciously

Streams can make code concise, but overusing streams makes code unreadable and hard to maintain.

```java
// ❌ OVER-ENGINEERED UNREADABLE STREAM
public static void printAnagrams(List<String> words, int minGroupSize) {
    words.stream().collect(groupingBy(word -> word.chars().sorted()
         .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString()))
         .values().stream().filter(group -> group.size() >= minGroupSize)
         .forEach(group -> System.out.println(group.size() + ": " + group));
}
```
**Golden Rule**: Refactor complex transformations into helper methods to preserve code clarity.

---

## 📌 Item 46: Prefer side-effect-free functions in streams

Stream operations should be pure functions. `forEach` should only be used to report computation results, not to perform state mutation.

```java
// ❌ Bad: Using forEach to mutate external state
Map<String, Long> freq = new HashMap<>();
words.forEach(word -> freq.merge(word.toLowerCase(), 1L, Long::sum));

// ✅ Good: Using Collector pipeline
Map<String, Long> freq = words.stream()
    .collect(groupingBy(String::toLowerCase, counting()));
```

---

## 📌 Item 47: Prefer Collection to Stream as a return type

`Collection<E>` implements `Iterable<E>` and provides `.stream()`. Returning a `Collection` or `List` gives callers the flexibility to loop using for-each OR process via streams.

---

## 📌 Item 48: Use caution when making streams parallel

Parallel streams (`.parallel()`) can lead to performance degradation or incorrect results unless:
- The data source splits easily (`ArrayList`, `HashSet`, `HashMap`, primitive ranges).
- Per-element computation cost is high.
- Operations are stateless and associative.
