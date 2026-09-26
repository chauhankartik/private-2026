# Chapter 4: Classes and Interfaces (Items 15 – 25)

Principles for designing classes and interfaces to make them useful, robust, flexible, and clean.

---

## 📌 Item 15: Minimize the accessibility of classes and members

### The Information Hiding / Encapsulation Principle
Decouples components, allowing them to be developed, tested, optimized, and modified independently.

- **Rule of Thumb**: Make each class or member as inaccessible as possible.
- Access levels: `private` < `package-private` (default) < `protected` < `public`.
- **Security Danger**: Public static final array fields are mutable!
```java
// ❌ SECURITY RISK: Clients can mutate array contents!
public static final Thing[] VALUES = { ... };

// ✅ SAFE FIX 1: Private array + Unmodifiable List
private static final Thing[] PRIVATE_VALUES = { ... };
public static final List<Thing> VALUES = List.of(PRIVATE_VALUES);

// ✅ SAFE FIX 2: Private array + Public clone method
public static final Thing[] values() {
    return PRIVATE_VALUES.clone();
}
```

---

## 📌 Item 16: In public classes, use accessor methods, not public fields

Direct public fields (`public double x; public double y;`) expose implementation details and prevent future class refactoring or invariant enforcement.

```java
// ✅ Proper Encapsulation
public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
```

---

## 📌 Item 17: Minimize mutability

Immutable classes are easier to design, implement, and use than mutable classes. They are less prone to error and far more secure.

### 5 Rules for Immutability
1. Don't provide methods that modify the object's state (no setters).
2. Ensure that the class cannot be extended (`final` class or private constructors).
3. Make all fields `final`.
4. Make all fields `private`.
5. Ensure exclusive access to any mutable components (make defensive copies).

### Modern Java 14+ Record Alternative
```java
// Modern Java equivalent of an immutable value class:
public record Complex(double re, double im) {
    // Automatically creates final fields, canonical constructor,
    // equals(), hashCode(), and toString()!
}
```

---

## 📌 Item 18: Favor composition over inheritance

Inheritance breaks encapsulation across package boundaries. If a superclass updates its implementation in a future release, the subclass can silently break.

### Fragile Base Class Example
```java
// ❌ Inheritance Fragility: Overriding addAll calls add(), causing double-counting!
public class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c); // Internally super.addAll calls add()! Count becomes 2 * c.size()!
    }
}
```

### ✅ Solution: Decorator / Wrapper Pattern (Composition)
```java
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s; }

    public boolean add(E e) { return s.add(e); }
    public boolean addAll(Collection<? extends E> c) { return s.addAll(c); }
    // Forward all other Set methods...
}

public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) { super(s); }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }
}
```

---

## 📌 Item 19: Design and document for inheritance or else prohibit it

If a class is designed for inheritance:
- You must document its self-use of overridable methods.
- Constructors must **never** invoke overridable methods (superclass constructor runs before subclass constructor, causing uninitialized field access).

If not designed for inheritance: **Mark the class `final`** or make all constructors package-private/private.

---

## 📌 Item 20: Prefer interfaces to abstract classes

Interfaces allow single-inheritance classes to implement multiple mix-ins (`Comparable`, `Serializable`). Abstract classes enforce rigid single-inheritance class trees.

---

## 📌 Item 21: Design interfaces for posterity

Java 8 introduced `default` methods on interfaces. Be aware that default methods can fail at runtime if the default logic violates assumptions made by pre-existing concrete implementers.

---

## 📌 Item 22: Use interfaces only to define types

### Constant Interface Anti-Pattern
```java
// ❌ ANTI-PATTERN: Constant Interface pollutes sub-types!
public interface PhysicalConstants {
    static final double AVOGADROS_NUMBER = 6.022_140_857e23;
}
```
**Fix**: Use noninstantiable utility classes (Item 4) or Enum types.

---

## 📌 Item 23: Prefer class hierarchies to tagged classes

Tagged classes (using a `switch(shapeType)` field inside a single class) are verbose, error-prone, and inefficient.

### Modern Java 17+ Sealed Classes
```java
// Sealed Classes enforce clean hierarchies and exhaustive pattern matching:
public sealed interface Shape permits Circle, Rectangle { }

public record Circle(double radius) implements Shape { }
public record Rectangle(double length, double width) implements Shape { }
```

---

## 📌 Item 24: Favor static member classes over nonstatic

If a nested class does not require access to its enclosing instance, **always add the `static` modifier**.
Nonstatic inner classes contain an implicit hidden reference to the outer instance, leading to memory leaks!

---

## 📌 Item 25: Limit source files to a single top-level class

Never put multiple top-level classes in a single `.java` file. It can result in multiple definitions for the same class depending on compilation file order.
