package functional;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Custom Collector & Custom Spliterator Implementation Masterclass
 *
 * Demonstrates:
 * 1. Implementing Custom Collector<T, A, R> interface (Rolling Statistics Collector)
 * 2. Implementing Custom Parallel Spliterator<T> (trySplit, tryAdvance, characteristics)
 */
class CustomCollectorSpliteratorDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Custom Collector: Rolling Statistics (Mean, Min, Max, Count) ===");
        demonstrateCustomCollector();

        System.out.println("\n=== 2. Custom Spliterator: Custom Parallel Array Chunking ===");
        demonstrateCustomSpliterator();

        System.out.println("\n[SUCCESS] Custom Collector & Spliterator demonstration completed cleanly.");
    }

    /**
     * 1. Custom Collector Implementation: Rolling Statistics
     */
    static class RollingStats {
        private int count = 0;
        private double sum = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;

        public void accept(double val) {
            count++;
            sum += val;
            min = Math.min(min, val);
            max = Math.max(max, val);
        }

        public RollingStats combine(RollingStats other) {
            RollingStats merged = new RollingStats();
            merged.count = this.count + other.count;
            merged.sum = this.sum + other.sum;
            merged.min = Math.min(this.min, other.min);
            merged.max = Math.max(this.max, other.max);
            return merged;
        }

        public double average() {
            return count == 0 ? 0 : sum / count;
        }

        @Override
        public String toString() {
            return String.format("Stats{count=%d, avg=%.2f, min=%.2f, max=%.2f}", count, average(), min, max);
        }
    }

    public static class RollingStatsCollector implements Collector<Double, RollingStats, RollingStats> {
        @Override
        public Supplier<RollingStats> supplier() {
            return RollingStats::new;
        }

        @Override
        public BiConsumer<RollingStats, Double> accumulator() {
            return RollingStats::accept;
        }

        @Override
        public BinaryOperator<RollingStats> combiner() {
            return RollingStats::combine;
        }

        @Override
        public Function<RollingStats, RollingStats> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(Characteristics.IDENTITY_FINISH);
        }
    }

    private static void demonstrateCustomCollector() {
        List<Double> values = Arrays.asList(10.5, 20.0, 5.5, 40.0, 14.0);

        RollingStats stats = values.stream()
                .collect(new RollingStatsCollector());

        System.out.println("  Custom Collector Output: " + stats);
    }

    /**
     * 2. Custom Spliterator Implementation for Array Batches
     */
    static class ArrayBatchSpliterator<T> implements Spliterator<T> {
        private final T[] array;
        private int origin; // Current index
        private final int fence;  // One past last index

        public ArrayBatchSpliterator(T[] array, int origin, int fence) {
            this.array = array;
            this.origin = origin;
            this.fence = fence;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (origin < fence) {
                action.accept(array[origin++]);
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            int lo = origin;
            int mid = (lo + fence) >>> 1;
            if (lo >= mid) return null; // Too small to split
            origin = mid;
            return new ArrayBatchSpliterator<>(array, lo, mid);
        }

        @Override
        public long estimateSize() {
            return fence - origin;
        }

        @Override
        public int characteristics() {
            return ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
        }
    }

    private static void demonstrateCustomSpliterator() {
        String[] data = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta"};

        Spliterator<String> customSpliterator = new ArrayBatchSpliterator<>(data, 0, data.length);

        // Convert Spliterator to Parallel Stream using StreamSupport
        Stream<String> parallelStream = StreamSupport.stream(customSpliterator, true);

        System.out.println("  Processing elements via Custom Parallel Spliterator:");
        parallelStream.forEach(item ->
                System.out.printf("    Thread [%-20s] processed: %s%n", Thread.currentThread().getName(), item)
        );
    }
}
