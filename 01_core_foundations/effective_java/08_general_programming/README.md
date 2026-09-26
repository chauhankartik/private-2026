# Chapter 9: General Programming (Items 57 – 68)

Fundamental Java programming practices: variables, control structures, primitive vs boxed types, strings, and performance.

---

## 📌 Item 57: Minimize the scope of local variables

Declare variables where they are first used. Prefer `for` loops over `while` loops to restrict loop index variable scope:

```java
// ✅ Loop index scope restricted strictly to loop block:
for (Iterator<Element> i = c.iterator(); i.hasNext(); ) {
    Element e = i.next();
}
```

---

## 📌 Item 58: Prefer for-each loops to traditional for loops

For-each loops eliminate index variables, iterator boilerplate, and nested loop iterator bugs.

---

## 📌 Item 59: Know and use the libraries

Don't reinvent the wheel. Use standard library routines (`java.util.Random`, `java.util.Objects`, `java.lang.Math`). They are reviewed by world-class engineers and continuously optimized across JDK releases.

---

## 📌 Item 60: Avoid float and double if exact answers are required

`float` and `double` use binary floating-point arithmetic (IEEE 754), which produces inexact rounding results (`0.1 + 0.2 != 0.3`).

**Use `BigDecimal`, `int`, or `long` (representing values in cents) for monetary calculations.**

```java
BigDecimal funds = new BigDecimal("1.00");
BigDecimal itemPrice = new BigDecimal("0.10");
BigDecimal remaining = funds.subtract(itemPrice); // Exact!
```

---

## 📌 Item 61: Prefer primitive types to boxed primitives

- Primitives have values only; Boxed primitives have identity distinct from their values (`==` compares reference identity!).
- Boxed primitives can be `null` (causes hidden `NullPointerException` on unboxing).
- Primitives are far more memory and time efficient.

---

## 📌 Item 62: Avoid strings where other types are more appropriate

Strings are poor substitutes for value types, enum types, aggregate types, or thread-local keys.

---

## 📌 Item 63: Beware the performance of string concatenation

Using `+` to concatenate $N$ strings in a loop requires $O(N^2)$ time because strings are immutable and copied every iteration.

**Use `StringBuilder` instead**:
```java
StringBuilder sb = new StringBuilder(numItems() * 16);
for (int i = 0; i < numItems(); i++) {
    sb.append(itemAt(i));
}
return sb.toString();
```

---

## 📌 Item 64: Refer to objects by their interfaces

Declare variables, parameters, and fields using interface types (`List<String> list = new ArrayList<>()`). This allows changing underlying implementations without affecting caller logic.

---

## 📌 Item 65: Prefer interfaces to reflection

Reflection loses compile-time type checking, requires tedious exception handling, and executes 10-100x slower.

---

## 📌 Item 66: Use native methods judiciously

Java Native Interface (JNI) adds memory corruption hazards, platform dependency, and bridge overhead.

---

## 📌 Item 67: Optimize judiciously

- **Don't sacrifice good architecture for premature optimization.**
- Measure performance before and after every optimization using profiling tools (JMH).

---

## 📌 Item 68: Adhere to generally accepted naming conventions

Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes/interfaces, UPPER_CASE for constants).
