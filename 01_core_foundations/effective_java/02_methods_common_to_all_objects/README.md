# Chapter 3: Methods Common to All Objects (Items 10 – 14)

Covers the proper implementation of non-final `Object` methods (`equals`, `hashCode`, `toString`, `clone`) and the `Comparable.compareTo` method.

---

## 📌 Item 10: Obey the general contract when overriding `equals`

### General Contract Rules
1. **Reflexive**: For any non-null reference `x`, `x.equals(x)` must return `true`.
2. **Symmetric**: For any non-null references `x` and `y`, `x.equals(y)` must return `true` if and only if `y.equals(x)` returns `true`.
3. **Transitive**: If `x.equals(y)` and `y.equals(z)`, then `x.equals(z)`.
4. **Consistent**: Multiple invocations of `x.equals(y)` return consistent results if no information used in equals comparisons is modified.
5. **Non-nullity**: For any non-null reference `x`, `x.equals(null)` must return `false`.

### Key Pitfall: Inheritance vs Equality
There is no way to extend an instantiable class and add a value component while preserving the `equals` contract (violates symmetry or transitivity).

**Golden Rule**: Favor composition over inheritance (Item 18) when adding value components to existing types.

### 🛠 Modern Best Practice (Recipe for `equals`)

```java
public final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = (short) areaCode;
        this.prefix   = (short) prefix;
        this.lineNum  = (short) lineNum;
    }

    @Override
    public boolean equals(Object o) {
        // 1. Use the == operator to check if argument is reference to this object
        if (o == this) return true;

        // 2. Use instanceof to check if argument has correct type
        if (!(o instanceof PhoneNumber)) return false;

        // 3. Cast argument to correct type
        PhoneNumber pn = (PhoneNumber) o;

        // 4. Compare key fields
        return pn.lineNum == lineNum && pn.prefix == prefix && pn.areaCode == areaCode;
    }
}
```

---

## 📌 Item 11: Always override `hashCode` when you override `equals`

### The Fundamental Contract
> **If two objects are equal according to `equals(Object)`, calling `hashCode()` on each of them must produce the same integer result.**

If this rule is violated, HashMap, HashSet, and ConcurrentHashMap will fail to find keys stored in the map!

### Recipe for `hashCode`
```java
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```
*Why 31?* Prime number, multiplication by 31 can be optimized by compiler to bit shift and subtraction: `31 * i == (i << 5) - i`.

---

## 📌 Item 12: Always override `toString`

Providing a good `toString()` implementation makes your class far more pleasant to use and diagnostic logs far easier to debug.

```java
@Override
public String toString() {
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

---

## 📌 Item 13: Override `clone` judiciously

`Cloneable` interface is widely considered a broken design in Java. It contains no methods, yet alters the behavior of `Object.clone()`.

### Hazards
1. Doesn't invoke constructors.
2. Shallow copies fields (mutable references share internal pointers).
3. Throws checked `CloneNotSupportedException`.

### Golden Rule
**Use Copy Constructors or Copy Static Factories instead of `clone()`!**
```java
// Copy Constructor
public TreeSet(Collection<? extends E> c) { ... }

// Copy Factory
public static HashSet<E> newCopy(HashSet<E> original) { ... }
```

---

## 📌 Item 14: Consider implementing `Comparable`

Implementing `Comparable<T>` allows your class to interoperate with all generic sorting algorithms (`Arrays.sort`, `Collections.sort`) and sorted collections (`TreeSet`, `TreeMap`).

### Pitfall to Avoid (Integer Overflow)
```java
// ❌ Dangerous! Can overflow if difference exceeds Integer.MAX_VALUE!
public int compareTo(PhoneNumber pn) {
    return this.areaCode - pn.areaCode;
}

// ✅ Safe & Modern (Comparator Construction Methods)
private static final Comparator<PhoneNumber> COMPARATOR =
    Comparator.comparingInt((PhoneNumber pn) -> pn.areaCode)
              .thenComparingInt(pn -> pn.prefix)
              .thenComparingInt(pn -> pn.lineNum);

@Override
public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```
