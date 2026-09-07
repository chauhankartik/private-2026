package functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Functional Design Patterns in Java Masterclass
 *
 * Demonstrates:
 * 1. Function Currying & Partial Application
 * 2. Strategy Design Pattern using Lambdas
 * 3. Chain of Responsibility Pattern via Function.andThen()
 * 4. Thread-Safe Function Memoization via ConcurrentHashMap.computeIfAbsent()
 */
class FunctionalDesignPatternsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Function Currying & Partial Application ===");
        demonstrateCurrying();

        System.out.println("\n=== 2. Strategy Pattern using Lambdas ===");
        demonstrateStrategyPattern();

        System.out.println("\n=== 3. Chain of Responsibility via Function Composition ===");
        demonstrateChainOfResponsibility();

        System.out.println("\n=== 4. Thread-Safe Function Memoization ===");
        demonstrateMemoization();

        System.out.println("\n[SUCCESS] Functional design patterns demonstration completed cleanly.");
    }

    /**
     * 1. Function Currying (Translating a function of multiple arguments into a sequence of functions of single arguments)
     */
    private static void demonstrateCurrying() {
        // Curried Tax Calculator: rate -> (price -> totalAmount)
        Function<Double, Function<Double, Double>> taxCalculator =
                rate -> price -> price + (price * rate);

        // Partial Application: Fix tax rate at 18% (VAT / GST)
        Function<Double, Double> applyGst18 = taxCalculator.apply(0.18);

        System.out.printf("  18%% GST on $100: $%.2f%n", applyGst18.apply(100.0));
        System.out.printf("  18%% GST on $250: $%.2f%n", applyGst18.apply(250.0));
    }

    /**
     * 2. Strategy Pattern using Lambdas
     */
    @FunctionalInterface
    interface PaymentStrategy {
        boolean pay(int amount);
    }

    static class OrderService {
        public void processOrder(int amount, PaymentStrategy strategy) {
            boolean success = strategy.pay(amount);
            System.out.printf("  Order Processing ($%d): Success = %b%n", amount, success);
        }
    }

    private static void demonstrateStrategyPattern() {
        OrderService service = new OrderService();

        // Strategy 1: Credit Card via Lambda
        service.processOrder(500, amount -> {
            System.out.printf("    Executing Credit Card Payment of $%d... ", amount);
            return true;
        });

        // Strategy 2: Crypto via Lambda
        service.processOrder(1200, amount -> {
            System.out.printf("    Executing Crypto Wallet Payment of $%d... ", amount);
            return true;
        });
    }

    /**
     * 3. Chain of Responsibility Pattern via Function.andThen()
     */
    static class TextPipeline {
        public static Function<String, String> createSanitizationPipeline() {
            Function<String, String> trim = String::trim;
            Function<String, String> removeHtml = str -> str.replaceAll("<[^>]*>", "");
            Function<String, String> truncate = str -> str.length() > 20 ? str.substring(0, 20) + "..." : str;

            // Chain functions sequentially
            return trim.andThen(removeHtml).andThen(truncate);
        }
    }

    private static void demonstrateChainOfResponsibility() {
        Function<String, String> pipeline = TextPipeline.createSanitizationPipeline();
        String dirtyInput = "   <p>Hello World! Functional Programming in Java Masterclass</p>   ";

        String cleanOutput = pipeline.apply(dirtyInput);
        System.out.println("  Input:  '" + dirtyInput + "'");
        System.out.println("  Cleaned: '" + cleanOutput + "'");
    }

    /**
     * 4. Thread-Safe Memoization Decorator
     */
    static class Memoizer {
        public static <K, V> Function<K, V> memoize(Function<K, V> function) {
            Map<K, V> cache = new ConcurrentHashMap<>();
            return key -> cache.computeIfAbsent(key, function);
        }
    }

    private static void demonstrateMemoization() {
        // Expensive Fibonacci computation function
        Function<Integer, Long> fibonacci = Memoizer.memoize(n -> {
            System.out.println("    >>> Computing Fibonacci(" + n + ") expensive operation...");
            if (n <= 1) return (long) n;
            long a = 0, b = 1;
            for (int i = 2; i <= n; i++) {
                long temp = a + b;
                a = b;
                b = temp;
            }
            return b;
        });

        System.out.println("  First Call fibonacci(40): " + fibonacci.apply(40));
        System.out.println("  Second Call fibonacci(40) [Cached Memoized Result]: " + fibonacci.apply(40));
    }
}
