package java.concurrency;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * ForkJoin Framework & Work-Stealing Pool Architecture.
 * Demonstrates ForkJoinPool, RecursiveTask<T>, Work-Stealing Deque mechanics,
 * and Divide-and-Conquer parallel matrix/array summation.
 */
public class ForkJoinWorkStealingDemo {

    public static void main(String[] args) {
        System.out.println("=== ForkJoin Work-Stealing Parallel Summation ===");

        // Create sample array of 10,000,000 integers
        int[] data = new int[10_000_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1;
        }

        // Target parallelism: Number of available CPU cores
        int parallelism = Runtime.getRuntime().availableProcessors();
        System.out.println("Available CPU Cores (ForkJoin Pool Parallelism): " + parallelism);

        ForkJoinPool pool = new ForkJoinPool(parallelism);

        long startTime = System.currentTimeMillis();
        ParallelArraySumTask mainTask = new ParallelArraySumTask(data, 0, data.length);
        long totalSum = pool.invoke(mainTask);
        long endTime = System.currentTimeMillis();

        System.out.println("Computed Parallel Sum: " + totalSum);
        System.out.println("Execution Time: " + (endTime - startTime) + " ms");
        pool.shutdown();
    }

    /**
     * RecursiveTask computing array sum via Divide-and-Conquer parallelism.
     * Work-Stealing Engine: When a worker thread finishes its own task queue,
     * it steals tasks from the TAIL of another busy worker thread's deque!
     */
    private static class ParallelArraySumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 100_000; // Sequential threshold limit
        private final int[] array;
        private final int start;
        private final int end;

        public ParallelArraySumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                // Base Case: Compute sequentially if workload below threshold
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                // Divide & Conquer: Split task into left and right sub-tasks
                int mid = start + length / 2;

                ParallelArraySumTask leftSubtask = new ParallelArraySumTask(array, start, mid);
                ParallelArraySumTask rightSubtask = new ParallelArraySumTask(array, mid, end);

                // Fork left sub-task asynchronously into current worker thread's deque
                leftSubtask.fork();

                // Compute right sub-task directly in current thread
                long rightResult = rightSubtask.compute();

                // Join left sub-task result (waits or steals work if necessary)
                long leftResult = leftSubtask.join();

                return leftResult + rightResult;
            }
        }
    }
}
