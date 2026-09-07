/**
 * ============================================================
 *  PATTERN 5 — PRIORITY QUEUES AND HEAP MECHANICS
 *  Problem 2 (Medium): Top K Frequent Elements   LC 347
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array nums and an integer k, return the k most frequent elements.
 *    The answer may be returned in any order.
 *
 *  EXAMPLE:
 *    nums=[1,1,1,2,2,3], k=2  → [1,2]
 *    nums=[1], k=1             → [1]
 *
 *  CONSTRAINTS:
 *    1 <= nums.length (= N) <= 10^5
 *    -10^4 <= nums[i] <= 10^4
 *    k is guaranteed to be in [1, number of unique elements]
 *    The answer is unique (no tie at position k).
 *
 *  APPROACH 1: Frequency HashMap + Min-Heap of size K (by frequency)
 *    Time:  O(N log K)   — N to count, N log K to build heap
 *    Space: O(N)         — HashMap + heap
 *
 *  APPROACH 2: Bucket Sort — frequency as bucket index
 *    Time:  O(N)         — linear! No heap, no sorting
 *    Space: O(N)         — frequency map + bucket array of N+1 lists
 *
 *  APPROACH 3: Array-backed min-heap (int[] keyed by frequency)
 *    Time:  O(N log K)
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Top_K_Frequent_Elements {

    // =========================================================
    // APPROACH 1 — FREQUENCY MAP + MIN-HEAP BY FREQUENCY
    // =========================================================

    /**
     * Finds top K frequent elements using a frequency map and bounded min-heap.
     *
     * HEAP COMPARATOR: min-heap ordered by frequency (root = element with LOWEST frequency).
     *   We maintain at most K elements. When the heap exceeds K elements:
     *     If new element's frequency > heap root's frequency:
     *       Remove root (lowest-frequency in top-K) and insert new element.
     *     Else: new element is not in top-K → skip.
     *   After processing: heap contains exactly the K most frequent elements.
     *
     * @param nums input array
     * @param k    number of top-frequent elements to return
     * @return array of the k most frequent elements
     *
     * Time:  O(N log K)
     * Space: O(N)  — frequency HashMap + O(K) heap
     */
    public int[] topKFrequentMinHeap(int[] nums, int k) {
        if (nums == null || nums.length == 0) return new int[0];

        // Step 1: Count frequencies
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int element : nums) {
            frequencyMap.merge(element, 1, Integer::sum);
        }

        // Step 2: Min-heap of size K, ordered by frequency (min-heap root = least frequent)
        // Entry: int[]{element, frequency}
        PriorityQueue<int[]> topKHeap = new PriorityQueue<>(
            k, Comparator.comparingInt(entry -> entry[1])   // compare by frequency
        );

        for (Map.Entry<Integer, Integer> freqEntry : frequencyMap.entrySet()) {
            int element   = freqEntry.getKey();
            int frequency = freqEntry.getValue();

            topKHeap.offer(new int[]{element, frequency});

            if (topKHeap.size() > k) {
                topKHeap.poll();   // remove least frequent — it's not in top-K
            }
        }

        // Step 3: Extract results from heap
        int[] topKElements = new int[k];
        int resultIndex = 0;
        while (!topKHeap.isEmpty()) {
            topKElements[resultIndex++] = topKHeap.poll()[0];
        }
        return topKElements;
    }

    // =========================================================
    // APPROACH 2 — BUCKET SORT (OPTIMAL O(N))
    // =========================================================

    /**
     * Finds top K frequent elements in LINEAR time using bucket sort.
     *
     * KEY INSIGHT:
     *   Frequency is bounded by N (an element can appear at most N times).
     *   Create N+1 buckets: bucket[f] = list of elements with frequency exactly f.
     *   Traverse buckets from highest frequency (N) downward, collecting until K found.
     *
     * BUCKET INDEXING:
     *   bucket[0] is unused (no element can have frequency 0).
     *   bucket[N] holds elements that appear N times (i.e., all elements same).
     *   Traversal: for f from N down to 1, add bucket[f] contents to result.
     *
     * @param nums input array
     * @param k    top K count
     * @return top K frequent elements
     *
     * Time:  O(N)  — no sorting, no heap
     * Space: O(N)  — frequency map + bucket array
     */
    public int[] topKFrequentBucketSort(int[] nums, int k) {
        if (nums == null || nums.length == 0) return new int[0];
        int totalElements = nums.length;

        // Step 1: Count frequencies
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int element : nums) {
            frequencyMap.merge(element, 1, Integer::sum);
        }

        // Step 2: Bucket sort — bucket[frequency] = list of elements with that frequency
        @SuppressWarnings("unchecked")
        List<Integer>[] frequencyBuckets = new List[totalElements + 1];

        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            int element   = entry.getKey();
            int frequency = entry.getValue();
            if (frequencyBuckets[frequency] == null) {
                frequencyBuckets[frequency] = new ArrayList<>();
            }
            frequencyBuckets[frequency].add(element);
        }

        // Step 3: Collect top K from highest frequency downward
        int[] topKElements = new int[k];
        int collectedCount = 0;

        for (int frequency = totalElements; frequency >= 1 && collectedCount < k; frequency--) {
            if (frequencyBuckets[frequency] == null) continue;
            for (int element : frequencyBuckets[frequency]) {
                if (collectedCount == k) break;
                topKElements[collectedCount++] = element;
            }
        }

        return topKElements;
    }

    // =========================================================
    // APPROACH 3 — QUICKSELECT ON UNIQUE ELEMENTS BY FREQUENCY
    // =========================================================

    /**
     * Top K frequent using Quickselect on the unique elements array (sorted by freq).
     * O(N) average, O(N²) worst — better than heap when k is close to total unique count.
     *
     * Time: O(N) average, Space: O(N)
     */
    public int[] topKFrequentQuickSelect(int[] nums, int k) {
        if (nums == null || nums.length == 0) return new int[0];

        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int n : nums) freqMap.merge(n, 1, Integer::sum);

        // Convert to array of unique elements
        int[] uniqueElements = new int[freqMap.size()];
        int idx = 0;
        for (int key : freqMap.keySet()) uniqueElements[idx++] = key;

        // Quickselect to find (size - k)-th smallest frequency boundary
        int targetIdx = uniqueElements.length - k;
        partitionByFreq(uniqueElements, 0, uniqueElements.length - 1, targetIdx, freqMap);

        return Arrays.copyOfRange(uniqueElements, targetIdx, uniqueElements.length);
    }

    private void partitionByFreq(int[] arr, int lo, int hi, int target,
                                  Map<Integer, Integer> freqMap) {
        if (lo >= hi) return;
        int pivotIdx = lo + new Random().nextInt(hi - lo + 1);
        swapArr(arr, pivotIdx, hi);
        int pivotFreq = freqMap.get(arr[hi]);
        int storeIdx = lo;
        for (int i = lo; i < hi; i++) {
            if (freqMap.get(arr[i]) < pivotFreq) swapArr(arr, i, storeIdx++);
        }
        swapArr(arr, storeIdx, hi);
        if (storeIdx > target)       partitionByFreq(arr, lo, storeIdx - 1, target, freqMap);
        else if (storeIdx < target)  partitionByFreq(arr, storeIdx + 1, hi, target, freqMap);
    }

    private void swapArr(int[] arr, int a, int b) {
        int tmp = arr[a]; arr[a] = arr[b]; arr[b] = tmp;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Top_K_Frequent_Elements solver =
            new Problem_2_Medium_Top_K_Frequent_Elements();

        System.out.println("========================================");
        System.out.println("  Top K Frequent Elements — Test Suite");
        System.out.println("========================================");

        // Sort results for deterministic comparison
        java.util.function.Consumer<int[]> sortedPrint = arr -> {
            Arrays.sort(arr);
            System.out.print(Arrays.toString(arr));
        };

        java.util.function.BiConsumer<int[], Integer> testAll = (nums, k) -> {
            int[] heap   = solver.topKFrequentMinHeap(nums, k);
            int[] bucket = solver.topKFrequentBucketSort(nums, k);
            int[] qs     = solver.topKFrequentQuickSelect(nums, k);
            Arrays.sort(heap); Arrays.sort(bucket); Arrays.sort(qs);
            boolean ok = Arrays.equals(heap, bucket) && Arrays.equals(bucket, qs);
            System.out.println("Input k=" + k + ": " + Arrays.toString(nums));
            System.out.print("  Heap="); sortedPrint.accept(heap);
            System.out.print("  Bucket="); sortedPrint.accept(bucket);
            System.out.print("  QS="); sortedPrint.accept(qs);
            System.out.println("  " + (ok ? "✓" : "FAIL"));
        };

        System.out.println("\n--- LC 347 examples ---");
        testAll.accept(new int[]{1,1,1,2,2,3}, 2);    // expected [1,2]
        testAll.accept(new int[]{1}, 1);               // expected [1]

        System.out.println("\n--- All same frequency ---");
        testAll.accept(new int[]{1,2,3,4}, 2);         // any 2 valid

        System.out.println("\n--- High frequency element ---");
        testAll.accept(new int[]{1,1,1,1,2,2,3}, 1);  // expected [1]

        System.out.println("\n--- Negative values ---");
        testAll.accept(new int[]{-1,-1,2,2,2,3}, 2);  // expected [2,-1]

        System.out.println("\n--- k = unique count (return all) ---");
        testAll.accept(new int[]{5,4,3,2,1,1,2,3}, 4);

        System.out.println("\n--- Single unique element ---");
        testAll.accept(new int[]{7,7,7,7}, 1);         // expected [7]

        System.out.println("\n========================================");
        System.out.println("  All Top K Frequent tests done.");
        System.out.println("========================================");
    }
}
