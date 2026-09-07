/**
 * ============================================================
 *  JAVA GENERICS — ADVANCED
 *  Type erasure, bridge methods, generic arrays,
 *  heap pollution, and real-world generic design patterns.
 * ============================================================
 *
 * Run: javac 03_advanced.java && java AdvancedGenerics
 */
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class AdvancedGenerics {

    public static void main(String[] args) throws Exception {
        demo1_typeErasure();
        demo2_bridgeMethods();
        demo3_genericArrays();
        demo4_heapPollution();
        demo5_typeToken();
        demo6_builderPattern();
        demo7_resultType();
    }

    // =========================================================
    // Demo 1: Type Erasure — What Disappears at Runtime
    // =========================================================
    /**
     * All generic type arguments are ERASED at runtime.
     * List<String> and List<Integer> are BOTH just "List" at runtime.
     *
     * Erased to:
     *   - Object     (unbounded: <T>)
     *   - First bound (bounded: <T extends Number> → Number)
     */
    static void demo1_typeErasure() throws Exception {
        separator("Demo 1: Type Erasure");

        List<String>  strList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // At runtime, both are the same class
        System.out.println("  strList.getClass() == intList.getClass(): "
            + (strList.getClass() == intList.getClass()));   // true!
        System.out.println("  strList.getClass(): " + strList.getClass().getName());  // ArrayList

        // instanceof cannot check generic type argument
        System.out.println("  strList instanceof List: " + (strList instanceof List));  // true
        // strList instanceof List<String>  // ✗ COMPILE ERROR — type arg erased

        // getClass() returns raw type
        System.out.println("  Raw class: " + strList.getClass().getSimpleName());  // ArrayList

        // ── What IS preserved: method signatures, field types ──────
        System.out.println("\n  What IS preserved in bytecode (readable via Reflection):");

        class Repository {
            private Map<String, List<Integer>> cache;
            List<String> findAll() { return null; }
            <T extends Comparable<T>> T max(List<T> list) { return null; }
        }

        // Field generic type
        Field cacheField = Repository.class.getDeclaredField("cache");
        System.out.println("  Field 'cache' generic type: " + cacheField.getGenericType());

        // Method return type
        Method findAll = Repository.class.getDeclaredMethod("findAll");
        System.out.println("  findAll() generic return:   " + findAll.getGenericReturnType());

        // Method type parameter bound
        Method max = Repository.class.getDeclaredMethod("max", List.class);
        System.out.println("  max() type param:           " + Arrays.toString(max.getTypeParameters()));
        System.out.println("  max() type param bounds:    " +
            Arrays.toString(max.getTypeParameters()[0].getBounds()));

        // ── Erasure consequences ──────────────────────────────────
        System.out.println("""
          
          Erasure consequences:
          ✗ Cannot do: new T()          (no constructor without type)
          ✗ Cannot do: T.class          (no class literal for type param)
          ✗ Cannot do: new T[10]        (no generic array creation)
          ✗ Cannot do: x instanceof T   (type unknown at runtime)
          ✓ CAN do:    Class<T> token   (pass Class<T> explicitly for runtime type)
        """);
    }

    // =========================================================
    // Demo 2: Bridge Methods
    // =========================================================
    /**
     * When a generic class/interface is extended/implemented with a specific type,
     * the compiler generates "bridge methods" to preserve polymorphism after erasure.
     *
     * Example:
     *   interface Comparable<T> { int compareTo(T o); }
     *   class Integer implements Comparable<Integer> {
     *       public int compareTo(Integer o) { ... }
     *   }
     *
     * After erasure, Comparable's method becomes: int compareTo(Object o)
     * But Integer only has: int compareTo(Integer o)
     * The compiler generates a BRIDGE: int compareTo(Object o) { return compareTo((Integer)o); }
     * This bridge is invisible to source code but appears in bytecode.
     */
    static void demo2_bridgeMethods() throws Exception {
        separator("Demo 2: Bridge Methods");

        class StringBox implements Comparable<StringBox> {
            String value;
            StringBox(String v) { this.value = v; }
            @Override public int compareTo(StringBox other) {
                return this.value.compareTo(other.value);
            }
        }

        // Inspect all methods on StringBox
        System.out.println("  Methods on StringBox (including bridge):");
        for (Method m : StringBox.class.getDeclaredMethods()) {
            System.out.printf("  %-8s bridge=%-5b synthetic=%-5b %s%n",
                m.getReturnType().getSimpleName(),
                m.isBridge(),
                m.isSynthetic(),
                m.getName() + "(" + Arrays.stream(m.getParameterTypes())
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")"
            );
        }

        // compareTo(Object) is the bridge — it calls compareTo(StringBox) internally
        // This is how Java's polymorphism works after erasure
        System.out.println("""
          
          Bridge methods allow:
          Comparable<?> ref = new StringBox("x");
          ref.compareTo(new StringBox("y"));  // calls bridge → actual method
          
          The bridge is generated by the compiler, invisible to you.
          It matters when using Reflection — isBridge() helps you skip them.
        """);
    }

    // =========================================================
    // Demo 3: Generic Arrays — The Problem and Solutions
    // =========================================================
    /**
     * You CANNOT create arrays of generic types:
     *   new List<String>[10]   // ✗ generic array creation
     *   new T[10]              // ✗ type parameter array
     *
     * WHY: Arrays are covariant and reifiable; generics are invariant and non-reifiable.
     * Mixing them breaks the array store check:
     *
     *   List<String>[] arr = new List<String>[10];   // if this were allowed:
     *   Object[] objs = arr;                         // OK (covariant)
     *   objs[0] = new ArrayList<Integer>();          // compiles — array store check can't verify!
     *   String s = arr[0].get(0);                    // ClassCastException at runtime!
     *
     * SOLUTIONS:
     *   1. Use List<List<String>> instead of List<String>[]
     *   2. Use Object[] and cast (what ArrayList does internally)
     *   3. Pass Class<T> token and use Array.newInstance(Class, length)
     */
    @SuppressWarnings("unchecked")
    static void demo3_genericArrays() throws Exception {
        separator("Demo 3: Generic Arrays — Problems and Solutions");

        // ── Solution 1: Use a collection instead ──────────────────
        List<List<String>> matrix = new ArrayList<>();
        matrix.add(new ArrayList<>(List.of("a", "b")));
        matrix.add(new ArrayList<>(List.of("c", "d")));
        System.out.println("  Solution 1 (List<List<T>>): " + matrix);

        // ── Solution 2: Object[] with unchecked cast ──────────────
        // (What ArrayList does internally with elementData = new Object[capacity])
        Object[] raw = new Object[5];
        raw[0] = "hello"; raw[1] = "world";
        String s = (String) raw[0];   // unchecked cast — type checked at runtime
        System.out.println("  Solution 2 (Object[] cast):  " + s);

        // ── Solution 3: Class token (most type-safe) ──────────────
        String[] typedArr = createArray(String.class, 3);
        typedArr[0] = "X"; typedArr[1] = "Y"; typedArr[2] = "Z";
        System.out.println("  Solution 3 (Class token):    " + Arrays.toString(typedArr));

        // ── @SafeVarargs — heap pollution from generic varargs ────
        System.out.println("\n  @SafeVarargs with varargs:");
        List<String> merged = concat(List.of("a", "b"), List.of("c", "d"), List.of("e"));
        System.out.println("  concat: " + merged);
    }

    @SuppressWarnings("unchecked")
    static <T> T[] createArray(Class<T> type, int size) {
        // Array.newInstance creates a typed array at runtime via reflection
        return (T[]) Array.newInstance(type, size);
    }

    // @SafeVarargs: we promise we don't do anything unsafe with the varargs array
    @SafeVarargs
    static <T> List<T> concat(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists) result.addAll(list);
        return result;
    }

    // =========================================================
    // Demo 4: Heap Pollution
    // =========================================================
    /**
     * Heap pollution: a variable of parameterized type refers to
     * an object NOT of that parameterized type.
     *
     * Usually caused by mixing raw types or unchecked casts.
     * @SuppressWarnings("unchecked") or @SafeVarargs hides the warning.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void demo4_heapPollution() {
        separator("Demo 4: Heap Pollution");

        // Deliberately cause heap pollution
        List rawList = new ArrayList();
        rawList.add("hello");
        rawList.add(42);   // mixing types!

        List<String> strings = rawList;   // unchecked assignment — no error yet

        System.out.println("  Heap pollution created:");
        System.out.println("  strings.get(0): " + strings.get(0));
        try {
            String s = strings.get(1);   // ClassCastException here — surprise!
        } catch (ClassCastException e) {
            System.out.println("  strings.get(1): ClassCastException — heap pollution! "
                + e.getMessage());
        }

        System.out.println("""
          
          How to avoid heap pollution:
          - Avoid raw types in new code.
          - Don't mix raw and generic types.
          - @SafeVarargs only when the varargs array isn't exposed or modified.
          - Enable -Xlint:unchecked compiler flag to see ALL unchecked warnings.
        """);
    }

    // =========================================================
    // Demo 5: Type Token Pattern (Class<T>)
    // =========================================================
    /**
     * "Type token" = passing Class<T> as a parameter to recover generic type at runtime.
     * This is the pattern Jackson, Gson, Spring use for deserialization.
     *
     * TypeReference (a.k.a. super type token) — captures generic type in a subclass:
     *   new TypeRef<List<String>>(){} — anonymous subclass retains type in getGenericSuperclass()
     */
    static void demo5_typeToken() throws Exception {
        separator("Demo 5: Type Token Pattern (Class<T>)");

        // Simple type token
        System.out.println("  Simple Class<T> token:");
        String parsed = parseAs("42", String.class);
        System.out.println("  parseAs('42', String.class): " + parsed);

        Integer i = parseAs("42", Integer.class);
        System.out.println("  parseAs('42', Integer.class): " + i);

        // Super type token — captures List<String> at runtime via anonymous subclass
        System.out.println("\n  Super Type Token (captures generic type in bytecode):");
        TypeRef<List<String>> token = new TypeRef<List<String>>(){};
        Type capturedType = token.getType();
        System.out.println("  Captured type: " + capturedType);

        if (capturedType instanceof ParameterizedType pt) {
            System.out.println("  Raw type: " + pt.getRawType());
            System.out.println("  Type arg: " + pt.getActualTypeArguments()[0]);
        }

        // This is exactly what Jackson does:
        // mapper.readValue(json, new TypeReference<List<User>>(){})
        System.out.println("\n  Jackson equivalent: new TypeReference<List<User>>(){}");
        System.out.println("  ↑ Same pattern — anonymous subclass preserves type in bytecode");
    }

    // Simple type token: caller passes Class<T>
    @SuppressWarnings("unchecked")
    static <T> T parseAs(String input, Class<T> type) {
        if (type == Integer.class) return (T) Integer.valueOf(input);
        if (type == Double.class)  return (T) Double.valueOf(input);
        if (type == Boolean.class) return (T) Boolean.valueOf(input);
        return type.cast(input);   // default: treat input as T directly
    }

    // Super type token — captures generic type in anonymous subclass's getGenericSuperclass()
    abstract static class TypeRef<T> {
        final Type getType() {
            return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    // =========================================================
    // Demo 6: Generic Builder Pattern
    // =========================================================
    /**
     * A fluent, type-safe builder using a recursive generic bound:
     * <B extends Builder<B>> — allows subclass builders to return THEIR OWN type
     * from inherited setter methods (fluent method chaining with correct return type).
     */
    static void demo6_builderPattern() {
        separator("Demo 6: Generic Builder Pattern");

        // Simple generic builder
        User user = User.builder()
            .name("Alice")
            .email("alice@example.com")
            .age(30)
            .build();
        System.out.println("  User: " + user);

        // Generic Result builder
        Result<String> ok  = Result.<String>builder().value("Hello").success(true).build();
        Result<Integer> err = Result.<Integer>builder().success(false).error("Not found").build();
        System.out.println("  ok:  " + ok);
        System.out.println("  err: " + err);
    }

    // Simple typed builder
    record User(String name, String email, int age) {
        static Builder builder() { return new Builder(); }

        static class Builder {
            private String name, email;
            private int age;

            Builder name(String n)  { this.name = n;  return this; }
            Builder email(String e) { this.email = e; return this; }
            Builder age(int a)      { this.age = a;   return this; }
            User build() { return new User(name, email, age); }
        }
    }

    // Generic Result builder
    static class Result<T> {
        final T value;
        final boolean success;
        final String error;

        Result(T value, boolean success, String error) {
            this.value = value; this.success = success; this.error = error;
        }

        static <T> Builder<T> builder() { return new Builder<>(); }

        static class Builder<T> {
            private T value;
            private boolean success;
            private String error;

            Builder<T> value(T v)    { this.value = v;   return this; }
            Builder<T> success(boolean s) { this.success = s; return this; }
            Builder<T> error(String e)    { this.error = e;   return this; }
            Result<T> build() { return new Result<>(value, success, error); }
        }

        @Override public String toString() {
            return success ? "Ok(" + value + ")" : "Err(" + error + ")";
        }
    }

    // =========================================================
    // Demo 7: Result<T> / Either Type — Real-World Pattern
    // =========================================================
    /**
     * A type-safe Result<T> (like Rust's Result<T,E>) eliminates null returns
     * and exception-based control flow.
     * Used in: Vavr library, Arrow-kt, Spring WebFlux error handling.
     */
    static void demo7_resultType() {
        separator("Demo 7: Either<L,R> — Type-Safe Error Handling");

        Either<String, Integer> ok  = Either.right(42);
        Either<String, Integer> err = Either.left("Not found");

        System.out.println("  ok:            " + ok);
        System.out.println("  err:           " + err);
        System.out.println("  ok.isRight():  " + ok.isRight());
        System.out.println("  err.isLeft():  " + err.isLeft());

        // map — transform the right value (if present)
        Either<String, String> mapped = ok.map(n -> "Result: " + n);
        System.out.println("  ok.map:        " + mapped);

        Either<String, String> errMapped = err.map(n -> "Result: " + n);
        System.out.println("  err.map:       " + errMapped);  // unchanged

        // fold — handle both cases
        String result = ok.fold(e -> "Error: " + e, v -> "Value: " + v);
        System.out.println("  ok.fold:       " + result);

        // Real-world: parsing
        List<String> inputs = List.of("42", "abc", "100", "bad", "7");
        inputs.stream()
            .map(AdvancedGenerics::parseEither)
            .forEach(e -> System.out.println("  " + e));
    }

    static Either<String, Integer> parseEither(String s) {
        try { return Either.right(Integer.parseInt(s)); }
        catch (NumberFormatException e) { return Either.left("Invalid: " + s); }
    }

    // Generic Either type
    static class Either<L, R> {
        private final L left;
        private final R right;
        private final boolean isRight;

        private Either(L left, R right, boolean isRight) {
            this.left = left; this.right = right; this.isRight = isRight;
        }

        static <L, R> Either<L, R> left(L value)  { return new Either<>(value, null, false); }
        static <L, R> Either<L, R> right(R value) { return new Either<>(null, value, true);  }

        boolean isRight() { return isRight; }
        boolean isLeft()  { return !isRight; }

        <U> Either<L, U> map(Function<R, U> fn) {
            return isRight ? Either.right(fn.apply(right)) : Either.left(left);
        }

        <T> T fold(Function<L, T> onLeft, Function<R, T> onRight) {
            return isRight ? onRight.apply(right) : onLeft.apply(left);
        }

        @Override public String toString() {
            return isRight ? "Right(" + right + ")" : "Left(" + left + ")";
        }
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
