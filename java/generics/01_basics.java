/**
 * ============================================================
 *  JAVA GENERICS — BASICS
 *  Generic classes, interfaces, methods, bounded types.
 * ============================================================
 *
 * Run: javac 01_basics.java && java GenericsBasics
 */
import java.util.*;
import java.util.function.*;

// ─────────────────────────────────────────────────────────────
// 1. GENERIC CLASS
// ─────────────────────────────────────────────────────────────

/**
 * Box<T> — holds any single value of type T.
 * T is a type parameter: a placeholder filled in at instantiation.
 */
class Box<T> {
    private T value;

    public Box(T value)  { this.value = value; }
    public T     get()   { return value; }
    public void  set(T v){ this.value = v; }
    public boolean isEmpty() { return value == null; }

    @Override
    public String toString() { return "Box[" + value + "]"; }
}

/**
 * Pair<A, B> — generic class with TWO type parameters.
 */
class Pair<A, B> {
    final A first;
    final B second;

    Pair(A first, B second) { this.first = first; this.second = second; }

    // Swap — returns a Pair<B, A> (different type parameters!)
    Pair<B, A> swap() { return new Pair<>(second, first); }

    @Override
    public String toString() { return "(" + first + ", " + second + ")"; }
}

// ─────────────────────────────────────────────────────────────
// 2. GENERIC INTERFACE
// ─────────────────────────────────────────────────────────────

interface Transformer<T, R> {
    R transform(T input);

    // Default method using the type parameters
    default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
        return input -> after.transform(this.transform(input));
    }
}

// ─────────────────────────────────────────────────────────────
// 3. GENERIC CLASS WITH UPPER BOUND
// ─────────────────────────────────────────────────────────────

/**
 * NumberBox<T extends Number> — only accepts Number subclasses.
 * By bounding, we can call Number methods (doubleValue, intValue, etc.)
 * inside the class without casting.
 */
class NumberBox<T extends Number> {
    private T value;

    NumberBox(T value) { this.value = value; }

    // Can call .doubleValue() because T is guaranteed to be a Number
    double asDouble() { return value.doubleValue(); }
    boolean isWhole() { return value.doubleValue() == Math.floor(value.doubleValue()); }

    @Override public String toString() { return "NumberBox[" + value + "]"; }
}

// ─────────────────────────────────────────────────────────────
// 4. MULTIPLE BOUNDS
// ─────────────────────────────────────────────────────────────

/**
 * T must extend Comparable AND implement Cloneable.
 * First bound can be a class; subsequent bounds must be interfaces.
 * Syntax: <T extends ClassBound & Interface1 & Interface2>
 */
class SortedContainer<T extends Comparable<T> & Cloneable> {
    private final List<T> items = new ArrayList<>();

    void add(T item) { items.add(item); }

    T min() { return items.stream().min(Comparator.naturalOrder()).orElseThrow(); }
    T max() { return items.stream().max(Comparator.naturalOrder()).orElseThrow(); }

    List<T> sorted() {
        List<T> copy = new ArrayList<>(items);
        Collections.sort(copy);
        return copy;
    }
}

// ─────────────────────────────────────────────────────────────
// 5. RECURSIVE BOUND (Self-referential)
// ─────────────────────────────────────────────────────────────

/**
 * <T extends Comparable<T>> — T can be compared to itself.
 * This is the pattern Comparable uses, and it's used in every sorted collection.
 * Called a "recursive type bound" or "self-referential bound."
 */
class MaxFinder<T extends Comparable<T>> {
    private T max;

    void update(T candidate) {
        if (max == null || candidate.compareTo(max) > 0) max = candidate;
    }
    T getMax() { return max; }
}

// ─────────────────────────────────────────────────────────────
public class GenericsBasics {

    public static void main(String[] args) {
        demo1_genericClass();
        demo2_genericInterface();
        demo3_upperBound();
        demo4_multipleBounds();
        demo5_genericMethods();
        demo6_typeInference();
        demo7_genericConstructor();
    }

    // =========================================================
    // Demo 1: Generic Class
    // =========================================================
    static void demo1_genericClass() {
        separator("Demo 1: Generic Class");

        // Type parameter filled at instantiation
        Box<String>  strBox  = new Box<>("Hello");
        Box<Integer> intBox  = new Box<>(42);
        Box<Double>  dblBox  = new Box<>(3.14);
        Box<List<String>> listBox = new Box<>(List.of("a", "b"));  // nested generics

        System.out.println("  strBox:  " + strBox);
        System.out.println("  intBox:  " + intBox);
        System.out.println("  dblBox:  " + dblBox);
        System.out.println("  listBox: " + listBox);

        // Compile-time type safety
        String s = strBox.get();    // no cast needed
        // Integer i = strBox.get(); // ← COMPILE ERROR ✓

        // Pair with two type parameters
        Pair<String, Integer> p = new Pair<>("Alice", 30);
        System.out.println("  Pair: " + p);
        System.out.println("  Swapped: " + p.swap());  // Pair<Integer, String>

        // The empty Box (null type parameter)
        Box<String> empty = new Box<>(null);
        System.out.println("  empty.isEmpty(): " + empty.isEmpty());
    }

    // =========================================================
    // Demo 2: Generic Interface Implementation
    // =========================================================
    static void demo2_genericInterface() {
        separator("Demo 2: Generic Interface");

        // Implement with a specific type
        Transformer<String, Integer> length = s -> s.length();
        Transformer<Integer, String> stars  = n -> "*".repeat(n);

        System.out.println("  length.transform('hello'): " + length.transform("hello"));
        System.out.println("  stars.transform(5):         " + stars.transform(5));

        // Compose: String → Integer → String
        Transformer<String, String> lengthAsStars = length.andThen(stars);
        System.out.println("  composed('hello'):          " + lengthAsStars.transform("hello"));

        // Lambda is a Transformer — no implementation class needed
        Transformer<List<Integer>, Integer> sumList = list -> list.stream().mapToInt(i -> i).sum();
        System.out.println("  sumList([1,2,3,4,5]):       " + sumList.transform(List.of(1, 2, 3, 4, 5)));
    }

    // =========================================================
    // Demo 3: Upper Bounded Type Parameter
    // =========================================================
    static void demo3_upperBound() {
        separator("Demo 3: Upper Bounded Type Parameter (extends)");

        NumberBox<Integer> intBox = new NumberBox<>(7);
        NumberBox<Double>  dblBox = new NumberBox<>(3.14);
        NumberBox<Long>    lngBox = new NumberBox<>(100_000L);
        // NumberBox<String> strBox = new NumberBox<>("bad"); // ✗ COMPILE ERROR ✓

        System.out.println("  intBox.asDouble(): " + intBox.asDouble());
        System.out.println("  dblBox.isWhole():  " + dblBox.isWhole());   // false
        System.out.println("  intBox.isWhole():  " + intBox.isWhole());   // true
        System.out.println("  lngBox:            " + lngBox);

        // The bound enables calling Number methods inside NumberBox without casting
        System.out.println("\n  Why bounds matter:");
        System.out.println("  With <T extends Number>, inside NumberBox we can call:");
        System.out.println("  value.doubleValue() — valid because T is guaranteed to be a Number");
    }

    // =========================================================
    // Demo 4: Multiple Bounds & Recursive Bound
    // =========================================================
    static void demo4_multipleBounds() {
        separator("Demo 4: Multiple Bounds & Recursive Bound");

        // String is Comparable and Cloneable — satisfies the bound
        SortedContainer<String> words = new SortedContainer<>();
        words.add("banana"); words.add("apple"); words.add("cherry"); words.add("date");
        System.out.println("  sorted:  " + words.sorted());
        System.out.println("  min:     " + words.min());
        System.out.println("  max:     " + words.max());

        // Recursive bound — MaxFinder<T extends Comparable<T>>
        MaxFinder<Integer> finder = new MaxFinder<>();
        finder.update(5); finder.update(3); finder.update(8); finder.update(1);
        System.out.println("\n  MaxFinder<Integer>: " + finder.getMax());

        MaxFinder<String> strFinder = new MaxFinder<>();
        strFinder.update("cat"); strFinder.update("ant"); strFinder.update("dog");
        System.out.println("  MaxFinder<String>:  " + strFinder.getMax());
    }

    // =========================================================
    // Demo 5: Generic Methods
    // =========================================================
    /**
     * A method can be generic INDEPENDENTLY of its class.
     * Type parameter is declared before the return type: <T> ReturnType method(...)
     *
     * Use generic methods when:
     *   - The type relationship is between parameters and return type.
     *   - The method is static (can't use class type parameter).
     */
    static void demo5_genericMethods() {
        separator("Demo 5: Generic Methods");

        // A generic method — type inferred from arguments
        System.out.println("  toList(1,2,3):      " + toList(1, 2, 3));
        System.out.println("  toList('a','b','c'): " + toList("a", "b", "c"));

        // Generic method with bound
        System.out.println("  max(3,7):           " + max(3, 7));
        System.out.println("  max('x','a'):        " + max("x", "a"));

        // Generic static utility — swap two elements in an array
        Integer[] arr = {1, 2, 3, 4, 5};
        swap(arr, 0, 4);
        System.out.println("  swap([1,2,3,4,5], 0, 4): " + Arrays.toString(arr));

        // Generic method that converts between types via Function
        List<String> names  = List.of("Alice", "Bob", "Carol");
        List<Integer> lengths = map(names, String::length);
        System.out.println("  map(names, length): " + lengths);
    }

    // Varargs + generics: @SafeVarargs suppresses unchecked warning
    @SafeVarargs
    static <T> List<T> toList(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    // T must be Comparable to itself — recursive bound
    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    // Generic static utility method
    static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
    }

    // Generic transformation using Function
    static <T, R> List<R> map(List<T> list, Function<T, R> fn) {
        List<R> result = new ArrayList<>();
        for (T item : list) result.add(fn.apply(item));
        return result;
    }

    // =========================================================
    // Demo 6: Type Inference — Diamond Operator
    // =========================================================
    /**
     * The compiler infers type arguments from context.
     * Since Java 7: Box<String> box = new Box<>(...); // diamond <> infers String
     * Since Java 8: types can be inferred from method arguments in more situations.
     */
    static void demo6_typeInference() {
        separator("Demo 6: Type Inference");

        // Diamond operator — compiler infers the type
        Box<String>          b1 = new Box<>("inferred");   // <> = <String>
        Pair<String, Integer> p1 = new Pair<>("key", 42); // <> = <String, Integer>

        // Generic method — type inferred from arguments
        String  s = max("hello", "world");   // <T=String> inferred
        Integer i = max(10, 20);             // <T=Integer> inferred

        // Explicit type witness (rarely needed — when inference fails)
        List<String> list = GenericsBasics.<String>toList("a", "b", "c");

        System.out.println("  Inferred box:  " + b1);
        System.out.println("  Inferred pair: " + p1);
        System.out.println("  Inferred max:  " + s + ", " + i);
        System.out.println("  Explicit type witness: " + list);

        // var (Java 10+) — type fully inferred by compiler
        var box = new Box<>(42);        // inferred: Box<Integer>
        var pair = new Pair<>(1, "x");  // inferred: Pair<Integer, String>
        System.out.println("  var box:  " + box);
        System.out.println("  var pair: " + pair);
    }

    // =========================================================
    // Demo 7: Generic Return Types and Generics in Practice
    // =========================================================
    static void demo7_genericConstructor() {
        separator("Demo 7: Putting It Together — Generic Utility Methods");

        // First non-null value
        System.out.println("  firstNonNull(null, null, 'found'): " + firstNonNull(null, null, "found"));

        // Safe cast
        Object obj = "Hello";
        Optional<String> s = safeCast(obj, String.class);
        Optional<Integer> i = safeCast(obj, Integer.class);
        System.out.println("  safeCast String: " + s);
        System.out.println("  safeCast Integer: " + i);

        // Partition
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        Pair<List<Integer>, List<Integer>> parts = partition(nums, n -> n % 2 == 0);
        System.out.println("  partition even: " + parts.first);
        System.out.println("  partition odd:  " + parts.second);

        // Zip
        List<String> names  = List.of("Alice", "Bob");
        List<Integer> scores = List.of(95, 87);
        List<Pair<String,Integer>> zipped = zip(names, scores);
        System.out.println("  zip: " + zipped);
    }

    @SafeVarargs
    static <T> T firstNonNull(T... values) {
        for (T v : values) if (v != null) return v;
        return null;
    }

    static <T> Optional<T> safeCast(Object obj, Class<T> type) {
        return type.isInstance(obj) ? Optional.of(type.cast(obj)) : Optional.empty();
    }

    static <T> Pair<List<T>, List<T>> partition(List<T> list, Predicate<T> pred) {
        List<T> yes = new ArrayList<>(), no = new ArrayList<>();
        for (T item : list) (pred.test(item) ? yes : no).add(item);
        return new Pair<>(yes, no);
    }

    static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        List<Pair<A, B>> result = new ArrayList<>();
        int len = Math.min(as.size(), bs.size());
        for (int k = 0; k < len; k++) result.add(new Pair<>(as.get(k), bs.get(k)));
        return result;
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
