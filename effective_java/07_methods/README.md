# Chapter 8: Methods (Items 49 – 56)

Best practices for method signatures, parameter validation, defensive copies, and return types.

---

## 📌 Item 49: Check parameters for validity

Validate method parameters at entry (**fail-fast** principle) before executing logic. Use `Objects.requireNonNull()` or throw `IllegalArgumentException`.

```java
public BigInteger mod(BigInteger m) {
    if (m.signum() <= 0) {
        throw new ArithmeticException("Modulus must be positive: " + m);
    }
    // Perform calculation...
}
```

---

## 📌 Item 50: Make defensive copies when needed

If a class has mutable parameters passed into constructor or returned from methods, make defensive copies of those objects!

```java
public final class Period {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        // Defensive copy before parameter validation!
        this.start = new Date(start.getTime());
        this.end   = new Date(end.getTime());

        if (this.start.after(this.end)) {
            throw new IllegalArgumentException(this.start + " after " + this.end);
        }
    }

    public Date getStart() {
        return new Date(start.getTime()); // Return defensive copy!
    }
}
```

---

## 📌 Item 51: Design method signatures carefully

- Choose method names carefully.
- Don't go crazy with convenient methods (keep interface focused).
- Avoid long parameter lists (maximum 4 parameters).
- Favor interfaces over concrete class types for parameters.
- Favor 2-element enum types over `boolean` parameters for readability (`FixIt.FAST` vs `true`).

---

## 📌 Item 52: Use overloading judiciously

Selection among overloaded methods is static (chosen at **compile time** based on declared type).
Selection among overridden methods is dynamic (chosen at **runtime** based on actual object instance).

```java
// ❌ Pitfall: Collection classifier chooses overloaded method statically!
public static String classify(Set<?> s) { return "Set"; }
public static String classify(List<?> l) { return "List"; }

Collection<?>[] collections = { new HashSet<>(), new ArrayList<>() };
for (Collection<?> c : collections) {
    System.out.println(classify(c)); // PRINTS "Unknown Collection" twice if general overload matched!
}
```

---

## 📌 Item 53: Use varargs judiciously

Require at least 1 explicit parameter when varargs cannot be empty:

```java
public static int min(int firstArg, int... remainingArgs) {
    int min = firstArg;
    for (int arg : remainingArgs) {
        if (arg < min) min = arg;
    }
    return min;
}
```

---

## 📌 Item 54: Return empty arrays or collections, not nulls

Never return `null` in place of an empty array or collection. Returning `null` forces every client caller to add null-check checks.

```java
// ✅ Clean & Safe
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```

---

## 📌 Item 55: Return `Optional` judiciously

`Optional<T>` forces the caller to explicitly handle empty results.

### Rules for `Optional`
1. Never return a null `Optional` (always return `Optional.empty()`).
2. Never wrap container types (`List`, `Map`, `Set`, `Array`) in an `Optional`.
3. Don't use `Optional` as map keys, record fields, or collection elements.

---

## 📌 Item 56: Write doc comments for all exposed API elements

Document every public class, interface, constructor, method, and field using Javadoc. Describe pre-conditions, post-conditions, and side effects (`@param`, `@return`, `@throws`).
