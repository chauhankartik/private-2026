# Chapter 2: Creating and Destroying Objects (Items 1 – 9)

Focuses on when and how to create objects, when and how to avoid creating them, how to ensure they are destroyed in a timely manner, and how to manage cleanup actions preceding their destruction.

---

## 📌 Item 1: Consider static factory methods instead of constructors

### Deep Dive & Core Mechanics
Constructors have a fundamental limitation: they have no names, cannot control instance creation caching, and must return an exact type instance. Static factory methods are public static methods that return an instance of the class.

### 5 Main Advantages
1. **They have names**: Unlike constructors, static factory method names (`BigInteger.probablePrime`) express what instance is returned.
2. **They are not required to create a new object each time**: Caching instances (`Boolean.valueOf(boolean)`, `Integer.valueOf(int)`) enables flyweight pattern and immutable instance control.
3. **They can return an object of any subtype of their return type**: Enables interface-based frameworks (`Collections.unmodifiableList`, `List.of`).
4. **The class of returned object can vary from call to call as a function of parameters**: `EnumSet.noneOf()` returns `RegularEnumSet` (≤64 elements, backed by a single `long`) or `JumboEnumSet` (>64 elements, backed by `long[]`), invisible to the caller.
5. **The class of returned object need not exist when the class containing the method is written**: Service Provider Frameworks (e.g. JDBC `DriverManager.getConnection`).

### Disadvantage & Trade-off
- Classes without `public` or `protected` constructors cannot be subclassed (which actually encourages composition over inheritance!).
- They are hard for programmers to find in API docs compared to constructors (must adhere to standard naming conventions).

### 🛠 Code Comparison

#### ❌ Bad Practice (Constructors with Ambiguous Parameter Lists)
```java
public class User {
    private String name;
    private String email;

    // Which constructor creates an admin vs regular user? Ambiguous!
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
```

#### ✅ Good Practice (Named Static Factory Methods)
```java
public class User {
    private final String name;
    private final String email;
    private final boolean isAdmin;

    private User(String name, String email, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public static User createRegularUser(String name, String email) {
        return new User(name, email, false);
    }

    public static User createAdmin(String name, String email) {
        return new User(name, email, true);
    }
}
```

---

## 📌 Item 2: Consider a builder when faced with many constructor parameters

### Deep Dive & Core Mechanics
Telescoping constructors (overloaded constructors with 1, 2, 3, 4 parameters) are hard to write, hard to read, and error-prone when parameter types match (`String name, String address, String email`). JavaBeans pattern (`new Object()`, then calling setters) leaves the object in an inconsistent transient state and prevents immutability.

The **Builder Pattern** combines the safety of telescoping constructors with the readability of JavaBeans.

### 🛠 Code Comparison

#### ✅ Good Practice (Fluent Builder Pattern with Immutability)
```java
public class NutritionFacts {
    private final int servingSize;  // (mL)     required
    private final int servings;     // (per container) required
    private final int calories;     // (per serving) optional
    private final int fat;          // (g/serving)  optional

    public static class Builder {
        // Required parameters
        private final int servingSize;
        private final int servings;

        // Optional parameters - initialized to default values
        private int calories = 0;
        private int fat = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            this.calories = val;
            return this;
        }

        public Builder fat(int val) {
            this.fat = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        this.servingSize = builder.servingSize;
        this.servings     = builder.servings;
        this.calories     = builder.calories;
        this.fat          = builder.fat;
    }
}

// Usage:
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
    .calories(100)
    .fat(0)
    .build();
```

---

## 📌 Item 3: Enforce the singleton property with a private constructor or an enum type

### Deep Dive & Core Mechanics
A Singleton is a class that is instantiated exactly once. Prior to Java 5, Singletons were implemented using private constructors + public static instance fields or factory methods. However, reflection (`AccessibleObject.setAccessible`) and deserialization can bypass private constructors.

**The single-element enum approach is the golden standard for singletons.**

### 🛠 Code Comparison

#### ✅ Best Practice (Enum Singleton — Reflection Proof & Serialization Safe)
```java
public enum DatabaseConnectionPool {
    INSTANCE;

    private final ConnectionPool pool = new ConnectionPool();

    public Connection getConnection() {
        return pool.acquire();
    }
}
```

---

## 📌 Item 4: Enforce noninstantiability with a private constructor

Utility classes (such as `java.lang.Math` or `java.util.Arrays`) group static methods and fields. They are not designed to be instantiated.

### 🛠 Code Comparison

```java
public class StringUtils {
    // Suppress default constructor for noninstantiability
    private StringUtils() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
```

---

## 📌 Item 5: Prefer dependency injection to hardcoding resources

Hardcoding resources (e.g. `private static final Lexicon dictionary = new EnglishLexicon();`) inside a class renders it non-reusable and impossible to test with mocks (`MockLexicon`).

### 🛠 Code Comparison

#### ✅ Good Practice (Constructor Dependency Injection)
```java
public class SpellChecker {
    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }
}
```

---

## 📌 Item 6: Avoid creating unnecessary objects

Reuse immutable objects or expensive objects instead of instantiating new identical instances.

### Key Anti-Patterns to Avoid
1. `String s = new String("bikini");` // Creates 2 String instances every call!
2. Hidden Auto-Boxing in loops:
```java
// ❌ Horrible performance! Creates 2^31 Long instances!
Long sum = 0L;
for (long i = 0; i <= Integer.MAX_VALUE; i++) {
    sum += i; // Unboxes sum, adds i, boxes back to new Long!
}

// ✅ Correct (Primitive primitive)
long sum = 0L;
for (long i = 0; i <= Integer.MAX_VALUE; i++) {
    sum += i;
}
```

---

## 📌 Item 7: Eliminate obsolete object references

Memory leaks in Java occur when an object reference is retained unwittingly (unintentional key retention).

### Common Memory Leak Sources
1. Custom memory management (e.g., custom `Stack` array holding pop elements):
```java
public Object pop() {
    if (size == 0) throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // Eliminate obsolete reference for GC!
    return result;
}
```
2. Caches (`WeakHashMap` for keys whose lifetime is determined by external references).
3. Listeners & Callbacks.

---

## 📌 Item 8: Avoid finalizers and cleaners

Finalizers (`finalize()`) and Cleaners (`java.lang.ref.Cleaner`) are unpredictable, dangerous, and unnecessary. They do not guarantee prompt execution (GC may never run).

**Solution**: Have your class implement `AutoCloseable` and require clients to invoke `.close()` via try-with-resources.

---

## 📌 Item 9: Prefer try-with-resources to try-finally

Prior to Java 7, `try-finally` was the standard for closing resources (`InputStream`, `Connection`). If both the try block and finally block throw exceptions, the finally exception suppresses and overwrites the primary failure traceback.

### 🛠 Code Comparison

#### ✅ Modern Best Practice (Try-with-Resources)
```java
public static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```
If `readLine()` throws an exception and closing `br` also throws, the closing exception is attached to the primary exception as a **suppressed exception** (`Throwable.getSuppressed()`).
