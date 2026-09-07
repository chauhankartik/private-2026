package functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

/**
 * Functional Interfaces & Lambda Expressions Masterclass
 *
 * Demonstrates:
 * 1. Standard Built-in Functional Interfaces (Function, Predicate, Consumer, Supplier, UnaryOperator)
 * 2. Unboxed Primitive Specializations (IntFunction, ToIntFunction, IntPredicate)
 * 3. Method References (Static, Instance arbitrary, Instance existing, Constructor)
 * 4. Custom @FunctionalInterface definitions
 * 5. Function Composition (andThen, compose)
 */
class FunctionalInterfacesDemo {

    @FunctionalInterface
    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Standard Built-in Functional Interfaces ===");
        demonstrateBuiltInInterfaces();

        System.out.println("\n=== 2. Primitive Specializations (Zero Auto-boxing Overhead) ===");
        demonstratePrimitiveSpecializations();

        System.out.println("\n=== 3. Method References (4 Forms) ===");
        demonstrateMethodReferences();

        System.out.println("\n=== 4. Function Composition (andThen vs compose) ===");
        demonstrateFunctionComposition();

        System.out.println("\n=== 5. Custom TriFunction Functional Interface ===");
        demonstrateCustomFunctionalInterface();

        System.out.println("\n[SUCCESS] Functional Interfaces & Lambdas demonstration completed cleanly.");
    }

    private static void demonstrateBuiltInInterfaces() {
        // Function<T, R>
        Function<String, Integer> stringLength = String::length;
        System.out.println("  Function String::length -> 'Hello': " + stringLength.apply("Hello"));

        // Predicate<T>
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("  Predicate isEven -> 4: " + isEven.test(4) + ", 5: " + isEven.test(5));

        // Consumer<T>
        Consumer<String> printer = str -> System.out.println("  Consumer Output: " + str);
        printer.accept("Executed via Consumer interface");

        // Supplier<T>
        Supplier<Double> randomSupplier = Math::random;
        System.out.printf("  Supplier Math::random -> %.4f%n", randomSupplier.get());

        // UnaryOperator<T>
        UnaryOperator<String> sanitize = str -> str.trim().toLowerCase();
        System.out.println("  UnaryOperator Sanitize -> '  JAVA  ': '" + sanitize.apply("  JAVA  ") + "'");
    }

    private static void demonstratePrimitiveSpecializations() {
        // IntPredicate avoids Integer auto-boxing
        IntPredicate isPrime = n -> {
            if (n <= 1) return false;
            for (int i = 2; i * i <= n; i++) {
                if (n % i == 0) return false;
            }
            return true;
        };

        System.out.println("  IntPredicate isPrime(17): " + isPrime.test(17));

        // ToIntFunction<String>
        ToIntFunction<String> parseInteger = Integer::parseInt;
        int parsed = parseInteger.applyAsInt("2026");
        System.out.println("  ToIntFunction Integer::parseInt -> " + parsed);
    }

    private static void demonstrateMethodReferences() {
        // 1. Static Method Reference (Class::staticMethod)
        Function<Double, Double> absFunc = Math::abs;

        // 2. Instance Method of Arbitrary Object (Class::instanceMethod)
        Function<String, String> upperFunc = String::toUpperCase;

        // 3. Instance Method of Existing Object (instance::instanceMethod)
        String prefix = "DEBUG: ";
        Function<String, String> logger = prefix::concat;

        // 4. Constructor Reference (Class::new)
        Supplier<List<String>> listSupplier = ArrayList::new;

        System.out.println("  1. Static Reference Math::abs(-42.5): " + absFunc.apply(-42.5));
        System.out.println("  2. Arbitrary Instance String::toUpperCase('hello'): " + upperFunc.apply("hello"));
        System.out.println("  3. Existing Instance prefix::concat('Error'): " + logger.apply("Error"));
        System.out.println("  4. Constructor Reference ArrayList::new: " + listSupplier.get().getClass().getSimpleName());
    }

    private static void demonstrateFunctionComposition() {
        Function<Integer, Integer> multiplyByTwo = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;

        // andThen: multiplyByTwo -> addTen ((x * 2) + 10)
        Function<Integer, Integer> multiplyThenAdd = multiplyByTwo.andThen(addTen);
        System.out.println("  multiplyByTwo.andThen(addTen) [x=5]: " + multiplyThenAdd.apply(5) + "  // (5*2)+10 = 20");

        // compose: addTen -> multiplyByTwo ((x + 10) * 2)
        Function<Integer, Integer> addThenMultiply = multiplyByTwo.compose(addTen);
        System.out.println("  multiplyByTwo.compose(addTen) [x=5]: " + addThenMultiply.apply(5) + "  // (5+10)*2 = 30");
    }

    private static void demonstrateCustomFunctionalInterface() {
        TriFunction<Integer, Integer, Integer, String> formattedSum =
                (a, b, c) -> String.format("Sum of (%d + %d + %d) = %d", a, b, c, (a + b + c));

        System.out.println("  Custom TriFunction Output: " + formattedSum.apply(10, 20, 30));
    }
}
