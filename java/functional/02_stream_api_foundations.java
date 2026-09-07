package functional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stream API Foundations & Execution Mechanics Masterclass
 *
 * Demonstrates:
 * 1. Stream Creation Sources (of, Arrays.stream, iterate, generate)
 * 2. Intermediate Operations (map, filter, distinct, sorted, peek)
 * 3. Terminal Operations (reduce, collect, min, max, anyMatch, allMatch)
 * 4. Lazy Evaluation & Short-Circuiting Execution Proof
 */
class StreamFoundationsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Stream Creation & Sources ===");
        demonstrateStreamCreation();

        System.out.println("\n=== 2. Intermediate Operations & Pipelines ===");
        demonstrateIntermediateOperations();

        System.out.println("\n=== 3. Reduction & Aggregation Terminal Operations ===");
        demonstrateTerminalOperations();

        System.out.println("\n=== 4. Proof of Lazy Evaluation & Short-Circuiting ===");
        demonstrateLazyEvaluationProof();

        System.out.println("\n[SUCCESS] Stream API foundations demonstration completed cleanly.");
    }

    private static void demonstrateStreamCreation() {
        // Stream.of
        Stream<String> streamOf = Stream.of("Java", "Kotlin", "Scala");
        System.out.println("  Stream.of count: " + streamOf.count());

        // Infinite Stream via Stream.iterate (bounded by limit)
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2)
                .limit(5)
                .collect(Collectors.toList());
        System.out.println("  Stream.iterate Powers of Two: " + powersOfTwo);

        // Primitive IntStream
        int sumInts = IntStream.rangeClosed(1, 10).sum();
        System.out.println("  IntStream.rangeClosed(1, 10).sum(): " + sumInts);
    }

    private static void demonstrateIntermediateOperations() {
        List<String> names = Arrays.asList("alice", "bob", "charlie", "bob", "david", "alexandra");

        List<String> result = names.stream()
                .filter(name -> name.startsWith("a"))
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        System.out.println("  Filtered, Mapped, Distinct & Sorted names: " + result);
    }

    private static void demonstrateTerminalOperations() {
        List<Integer> numbers = Arrays.asList(5, 12, 8, 20, 15);

        // 1. reduce (Sum elements)
        int sum = numbers.stream()
                .reduce(0, Integer::sum);
        System.out.println("  reduce(0, Integer::sum): " + sum);

        // 2. min / max
        Optional<Integer> maxVal = numbers.stream().max(Integer::compareTo);
        System.out.println("  stream().max():          " + maxVal.orElse(-1));

        // 3. Matchers
        boolean hasGreaterThan15 = numbers.stream().anyMatch(n -> n > 15);
        boolean allPositive = numbers.stream().allMatch(n -> n > 0);
        System.out.printf("  anyMatch(>15): %b, allMatch(>0): %b%n", hasGreaterThan15, allPositive);
    }

    private static void demonstrateLazyEvaluationProof() {
        List<String> items = Arrays.asList("one", "two", "three", "four", "five");

        System.out.println("  Building stream pipeline with peek() logging...");
        Stream<String> pipeline = items.stream()
                .filter(s -> {
                    System.out.println("    [Filter Evaluated]: " + s);
                    return s.length() > 3;
                })
                .map(s -> {
                    System.out.println("    [Map Evaluated]:    " + s);
                    return s.toUpperCase();
                });

        System.out.println("  Notice: No operations logged yet! Pipeline is lazy until terminal operation.");
        System.out.println("  Invoking terminal operation findFirst() [Short-circuiting]:");

        Optional<String> firstResult = pipeline.findFirst();
        System.out.println("  Terminal Result: " + firstResult.orElse(""));
        System.out.println("  >>> Key Insight: Stream stopped evaluating after processing 'three', skipping 'four' and 'five' completely!");
    }
}
