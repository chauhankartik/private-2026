# Chapter 6: Enums and Annotations (Items 34 – 41)

Enums provide type-safe constant groups; Annotations allow clean metadata attachment.

---

## 📌 Item 34: Use enums instead of int constants

`int` constants (`public static final int APPLE_FUJI = 0;`) provide zero type safety and no namespace isolation.

Enums in Java are full-fledged classes that permit fields, methods, interfaces, and constant-specific behavior.

### Constant-Specific Method Implementation
```java
public enum Operation {
    PLUS("+")   { public double apply(double x, double y) { return x + y; } },
    MINUS("-")  { public double apply(double x, double y) { return x - y; } },
    TIMES("*")  { public double apply(double x, double y) { return x * y; } },
    DIVIDE("/") { public double apply(double x, double y) { return x / y; } };

    private final String symbol;
    Operation(String symbol) { this.symbol = symbol; }

    public abstract double apply(double x, double y);
}
```

---

## 📌 Item 35: Use instance fields instead of ordinals

Never derive associated values from `.ordinal()`. If enum constants are reordered, code using `.ordinal()` breaks silently!

```java
// ❌ Dangerous: Dependent on declaration order!
public enum Ensemble {
    SOLO, DUET, TRIO;
    public int numberOfMusicians() { return ordinal() + 1; }
}

// ✅ Safe: Stored in private final instance field!
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3);
    private final int numberOfMusicians;
    Ensemble(int size) { this.numberOfMusicians = size; }
    public int numberOfMusicians() { return numberOfMusicians; }
}
```

---

## 📌 Item 36: Use EnumSet instead of bit fields

Legacy bit fields (`public static final int STYLE_BOLD = 1 << 0;`) require bitwise OR operations (`STYLE_BOLD | STYLE_ITALIC`).

`EnumSet` provides bit-vector speed ($O(1)$) with full type safety.

```java
public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }

// Usage:
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
```

---

## 📌 Item 37: Use EnumMap instead of ordinal indexing

Never use `ordinal()` to index into an array. Use `EnumMap`, which internally uses a high-performance array while maintaining type safety.

```java
Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle = new EnumMap<>(Plant.LifeCycle.class);
```

---

## 📌 Item 38: Emulate extensible enums with interfaces

Enums cannot be extended, but enum types can implement interfaces. This allows clients to write new enums that implement the base interface (e.g., custom extensible opcode operations).

---

## 📌 Item 39: Prefer annotations to naming patterns

Do not use naming patterns (like requiring test method names to start with `test...`). Annotations (`@Test`) enforce compile-time checks and accept parameters cleanly.

---

## 📌 Item 40: Consistently use the Override annotation

Always annotate methods overriding superclass declarations with `@Override`. It alerts the compiler if parameter signatures mismatch (preventing accidental overloading).

---

## 📌 Item 41: Use marker interfaces to define types

Marker interfaces (`Serializable`, `Cloneable`) contain no methods. Use them when you want to define a type that can be checked at compile time. Use marker annotations when the marker is intended for methods or fields.
