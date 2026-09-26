/**
 * ============================================================
 *  JAVA INTERFACES — FUNCTIONAL INTERFACES & LAMBDAS
 *  @FunctionalInterface, built-in java.util.function types,
 *  method references, and composing functions.
 * ============================================================
 *
 * Run: javac 02_functional.java && java FunctionalDemo
 */
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ─────────────────────────────────────────────────────────────
// 1. CUSTOM FUNCTIONAL INTERFACE
// ─────────────────────────────────────────────────────────────

/**
 * A functional interface has EXACTLY ONE abstract method.
 * @FunctionalInterface annotation is optional but recommended —
 * the compiler will error if you accidentally add a second abstract method.
 */
@FunctionalInterface
interface Transformer<T, R> {
    R transform(T input);                           // the single abstract method

    // Default and static methods are ALLOWED — do not count toward "functional"
    default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
        return input -> after.transform(this.transform(input));
    }

    static <T> Transformer<T, T> identity() {
        return input -> input;
    }
}

@FunctionalInterface
interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

// ─────────────────────────────────────────────────────────────
// Domain classes
// ─────────────────────────────────────────────────────────────
record Employee(String name, String dept, double salary, int age) {}

// ─────────────────────────────────────────────────────────────
public class FunctionalDemo {

    public static void main(String[] args) {
        demo1_customFunctionalInterface();
        demo2_predicate();
        demo3_function();
        demo4_consumer();
        demo5_supplier();
        demo6_biFunction_and_operators();
        demo7_methodReferences();
        demo8_composingFunctions();
        demo9_realWorldStreams();
    }

    // =========================================================
    // Demo 1: Custom Functional Interface
    // =========================================================
    static void demo1_customFunctionalInterface() {
        separator("Demo 1: Custom @FunctionalInterface");

        // Lambda expression implementing the SAM (Single Abstract Method)
        Transformer<String, Integer> length = s -> s.length();

        // Anonymous class — equivalent, but verbose
        Transformer<String, Integer> lengthAnon = new Transformer<>() {
            @Override public Integer transform(String input) { return input.length(); }
        };

        System.out.println("  lambda:  " + length.transform("hello"));       // 5
        System.out.println("  anon:    " + lengthAnon.transform("hello"));   // 5

        // Compose transformers: String → Integer → String
        Transformer<String, String> lengthStr = length.andThen(n -> "len=" + n);
        System.out.println("  composed: " + lengthStr.transform("reflection")); // "len=10"

        // TriFunction — not in JDK, must define yourself
        TriFunction<Integer, Integer, Integer, Integer> sumThree = (a, b, c) -> a + b + c;
        System.out.println("  triFunc: " + sumThree.apply(10, 20, 30));  // 60
    }

    // =========================================================
    // Demo 2: Predicate<T> — boolean test(T t)
    // =========================================================
    static void demo2_predicate() {
        separator("Demo 2: Predicate<T>");

        Predicate<String> isLong   = s -> s.length() > 5;
        Predicate<String> startsA  = s -> s.startsWith("A");
        Predicate<Integer> isEven  = n -> n % 2 == 0;

        System.out.println("  isLong('hello'):      " + isLong.test("hello"));    // false
        System.out.println("  isLong('reflection'): " + isLong.test("reflection")); // true

        // Combining predicates
        Predicate<String> longAndStartsA = isLong.and(startsA);
        Predicate<String> longOrStartsA  = isLong.or(startsA);
        Predicate<String> notLong        = isLong.negate();

        List<String> words = List.of("Alice", "Bob", "Anaconda", "Algorithm", "Hi");
        System.out.println("  long AND starts A: " + words.stream().filter(longAndStartsA).toList());
        System.out.println("  long OR starts A:  " + words.stream().filter(longOrStartsA).toList());
        System.out.println("  NOT long:          " + words.stream().filter(notLong).toList());

        // Predicate.not() — for method references
        List<String> nonEmpty = List.of("a", "", "b", "", "c")
            .stream()
            .filter(Predicate.not(String::isEmpty))
            .toList();
        System.out.println("  non-empty: " + nonEmpty);
    }

    // =========================================================
    // Demo 3: Function<T,R> — R apply(T t)
    // =========================================================
    static void demo3_function() {
        separator("Demo 3: Function<T,R>");

        Function<String, Integer> toLength = String::length;
        Function<Integer, String> toStars  = n -> "*".repeat(n);

        System.out.println("  toLength('Java'): " + toLength.apply("Java")); // 4
        System.out.println("  toStars(5):       " + toStars.apply(5));       // "*****"

        // andThen: apply this, THEN apply after
        Function<String, String> lengthAsStars = toLength.andThen(toStars);
        System.out.println("  andThen('hello'): " + lengthAsStars.apply("hello")); // "*****"

        // compose: apply before FIRST, then this (reverse of andThen)
        Function<String, String> starsFromLength = toStars.compose(toLength);
        System.out.println("  compose('hello'): " + starsFromLength.apply("hello")); // "*****"

        // Function.identity() — returns input unchanged
        Function<String, String> identity = Function.identity();
        System.out.println("  identity('xyz'):  " + identity.apply("xyz")); // "xyz"
    }

    // =========================================================
    // Demo 4: Consumer<T> — void accept(T t)
    // =========================================================
    static void demo4_consumer() {
        separator("Demo 4: Consumer<T>");

        Consumer<String> print     = System.out::println;
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());

        print.accept("  hello from consumer");
        printUpper.accept("  hello from consumer");

        // andThen: chain consumers (both run, in order)
        Consumer<String> both = print.andThen(printUpper);
        System.out.println("  --- andThen ---");
        both.accept("  chained");

        // forEach uses Consumer
        List<Employee> employees = List.of(
            new Employee("Alice", "Eng", 90000, 30),
            new Employee("Bob",   "HR",  70000, 25)
        );

        Consumer<Employee> printEmp = e -> System.out.println("  " + e.name() + " @ " + e.dept());
        employees.forEach(printEmp);
    }

    // =========================================================
    // Demo 5: Supplier<T> — T get()
    // =========================================================
    static void demo5_supplier() {
        separator("Demo 5: Supplier<T>");

        // Lazy factory — value computed only when get() is called
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> list1 = listFactory.get();
        List<String> list2 = listFactory.get();
        list1.add("A"); list2.add("B");
        System.out.println("  list1: " + list1 + "  list2: " + list2); // independent lists

        // Config value with default
        Supplier<String> envOrDefault = () -> {
            String env = System.getenv("APP_ENV");
            return env != null ? env : "development";
        };
        System.out.println("  APP_ENV: " + envOrDefault.get());

        // Optional.orElseGet uses Supplier (lazy — only called if Optional is empty)
        Optional<String> empty = Optional.empty();
        String result = empty.orElseGet(() -> "default-value");
        System.out.println("  orElseGet: " + result);
    }

    // =========================================================
    // Demo 6: BiFunction, UnaryOperator, BinaryOperator
    // =========================================================
    static void demo6_biFunction_and_operators() {
        separator("Demo 6: BiFunction, UnaryOperator, BinaryOperator");

        // BiFunction<T,U,R> — two inputs, one output
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("  repeat('ab', 3): " + repeat.apply("ab", 3));  // "ababab"

        // UnaryOperator<T> extends Function<T,T> — same input and output type
        UnaryOperator<String> trim   = String::trim;
        UnaryOperator<String> upper  = String::toUpperCase;
        UnaryOperator<String> both   = trim.andThen(upper)::apply;  // compose
        System.out.println("  trim+upper: '" + both.apply("  hello  ") + "'"); // "HELLO"

        // List.replaceAll uses UnaryOperator
        List<String> names = new ArrayList<>(List.of("alice", "bob", "carol"));
        names.replaceAll(String::toUpperCase);
        System.out.println("  replaceAll: " + names);

        // BinaryOperator<T> extends BiFunction<T,T,T>
        BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
        System.out.println("  max(3,7): " + max.apply(3, 7));  // 7

        // reduce uses BinaryOperator
        int sum = List.of(1, 2, 3, 4, 5).stream().reduce(0, Integer::sum);
        System.out.println("  reduce sum: " + sum);  // 15
    }

    // =========================================================
    // Demo 7: Method References (shorthand for lambdas)
    // =========================================================
    static void demo7_methodReferences() {
        separator("Demo 7: Method References");

        // 1. Static method reference: ClassName::staticMethod
        Function<String, Integer> parseInt = Integer::parseInt;
        System.out.println("  parseInt('42'): " + parseInt.apply("42"));

        // 2. Instance method of a specific object: instance::method
        String prefix = "JAVA: ";
        Function<String, String> addPrefix = prefix::concat;
        System.out.println("  addPrefix: " + addPrefix.apply("Rocks"));

        // 3. Instance method of an arbitrary object of a type: ClassName::instanceMethod
        // Lambda would be: s -> s.toUpperCase()
        Function<String, String> upper = String::toUpperCase;
        System.out.println("  upper: " + upper.apply("hello"));

        // 4. Constructor reference: ClassName::new
        Supplier<ArrayList<String>>        listCtor   = ArrayList::new;
        Function<Integer, ArrayList<String>> listSized = ArrayList::new;
        System.out.println("  listCtor: " + listCtor.get().getClass().getSimpleName());
        System.out.println("  listSized(10) capacity-aware: " + listSized.apply(10).getClass().getSimpleName());

        // Common in Streams
        List<String> words = List.of("banana", "apple", "cherry");
        words.stream()
             .map(String::toUpperCase)
             .sorted(String::compareTo)
             .forEach(System.out::println);
    }

    // =========================================================
    // Demo 8: Composing Functions
    // =========================================================
    static void demo8_composingFunctions() {
        separator("Demo 8: Function Composition");

        Function<Double, Double> toCelsius    = f -> (f - 32) * 5 / 9;
        Function<Double, String> formatTemp   = c -> String.format("%.1f°C", c);
        Function<Double, String> fahrenheitToStr = toCelsius.andThen(formatTemp);

        System.out.println("  100°F = " + fahrenheitToStr.apply(100.0)); // 37.8°C
        System.out.println("  32°F  = " + fahrenheitToStr.apply(32.0));  // 0.0°C

        // Predicate composition for complex filters
        List<Employee> staff = List.of(
            new Employee("Alice", "Eng", 95000, 30),
            new Employee("Bob",   "HR",  65000, 40),
            new Employee("Carol", "Eng", 80000, 28),
            new Employee("Dave",  "Eng", 72000, 35)
        );

        Predicate<Employee> isEng    = e -> e.dept().equals("Eng");
        Predicate<Employee> highPay  = e -> e.salary() > 75000;
        Predicate<Employee> eligible = isEng.and(highPay);

        System.out.println("  Eng + salary > 75k:");
        staff.stream().filter(eligible)
             .map(Employee::name)
             .forEach(n -> System.out.println("    " + n));
    }

    // =========================================================
    // Demo 9: Real-world Streams with functional interfaces
    // =========================================================
    static void demo9_realWorldStreams() {
        separator("Demo 9: Real-World Streams");

        List<Employee> staff = List.of(
            new Employee("Alice", "Eng",  95000, 30),
            new Employee("Bob",   "HR",   65000, 40),
            new Employee("Carol", "Eng",  80000, 28),
            new Employee("Dave",  "Eng",  72000, 35),
            new Employee("Eve",   "HR",   90000, 45),
            new Employee("Frank", "Finance", 88000, 38)
        );

        // Average salary by department
        System.out.println("  Avg salary by dept:");
        staff.stream()
             .collect(Collectors.groupingBy(Employee::dept,
                      Collectors.averagingDouble(Employee::salary)))
             .forEach((dept, avg) ->
                 System.out.printf("    %-10s → %.0f%n", dept, avg));

        // Top earner per department
        System.out.println("  Top earner per dept:");
        staff.stream()
             .collect(Collectors.groupingBy(Employee::dept,
                      Collectors.maxBy(Comparator.comparingDouble(Employee::salary))))
             .forEach((dept, emp) ->
                 System.out.println("    " + dept + " → " + emp.map(Employee::name).orElse("-")));

        // Names of Eng dept, sorted, comma-joined
        String engNames = staff.stream()
            .filter(e -> e.dept().equals("Eng"))
            .sorted(Comparator.comparing(Employee::name))
            .map(Employee::name)
            .collect(Collectors.joining(", "));
        System.out.println("  Eng names: " + engNames);
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
