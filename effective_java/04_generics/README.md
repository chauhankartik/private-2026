# Chapter 5: Generics (Items 26 – 33)

Generics enable compile-time type safety and eliminate explicit casts.

---

## 📌 Item 26: Don't use raw types

Using raw types (e.g. `List` instead of `List<String>`) disables compile-time type checks and delays `ClassCastException` to runtime.

```java
// ❌ Raw type allows inserting invalid types!
List rawList = new ArrayList();
rawList.add("Hello");
rawList.add(42); // Compiles fine, crashes at runtime when casting!

// ✅ Parameterized type catches errors at compile time!
List<String> safeList = new ArrayList<>();
```

---

## 📌 Item 27: Eliminate unchecked warnings

Always eliminate all unchecked compiler warnings. If a warning cannot be eliminated but is proven safe, suppress it using `@SuppressWarnings("unchecked")` on the narrowest possible scope and add a comment explaining why it is safe.

---

## 📌 Item 28: Prefer lists to arrays

Arrays are **covariant** (`Sub[]` is a subtype of `Super[]`) and **reified** (know their element types at runtime).
Generics are **invariant** (`List<Sub>` is NOT a subtype of `List<Super>`) and **erased** (type parameters removed at compile time).

```java
// ❌ Fails at RUNTIME with ArrayStoreException!
Object[] objectArray = new Long[1];
objectArray[0] = "I don't fit in!"; 

// ✅ Fails at COMPILE TIME!
// List<Object> ol = new ArrayList<Long>(); // Incompatible types!
```

---

## 📌 Item 29: Favor generic types

Parameterize custom collection classes (`Stack<E>`) to avoid client-side casting.

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public Stack() {
        // Elements array cast is safe because elements array is private
        elements = (E[]) new Object[16];
    }

    public void push(E e) { elements[size++] = e; }
    public E pop() { return elements[--size]; }
}
```

---

## 📌 Item 30: Favor generic methods

Parameterize utility methods for flexible type safety.

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

---

## 📌 Item 31: Use bounded wildcards to increase API flexibility (PECS)

### The PECS Rule
> **PECS = Producer Extends, Consumer Super**

- If an input parameter produces `T` instances for use by your method, use `? extends T`.
- If an input parameter consumes `T` instances from your method, use `? super T`.

```java
public class Stack<E> {
    // Producer: src produces items of type E to be pushed onto stack
    public void pushAll(Iterable<? extends E> src) {
        for (E e : src) {
            push(e);
        }
    }

    // Consumer: dst consumes items of type E popped from stack
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty()) {
            dst.add(pop());
        }
    }
}
```

---

## 📌 Item 32: Combine generics and varargs judiciously

Varargs creates an internal array (`T[]`). Combining generics and varargs can cause heap pollution.
Use `@SafeVarargs` ONLY if the method does not store anything into the array and does not leak references to the array.

```java
@SafeVarargs
public static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}
```

---

## 📌 Item 33: Consider typesafe heterogeneous containers

Standard maps bind key and value types uniformly (`Map<K, V>`). When flexible keys are required (e.g. database row column lookup), use `Class<T>` as the key.

```java
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), type.cast(instance));
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
}

// Usage:
Favorites f = new Favorites();
f.putFavorite(String.class, "Java");
f.putFavorite(Integer.class, 2026);
String s = f.getFavorite(String.class);
```
