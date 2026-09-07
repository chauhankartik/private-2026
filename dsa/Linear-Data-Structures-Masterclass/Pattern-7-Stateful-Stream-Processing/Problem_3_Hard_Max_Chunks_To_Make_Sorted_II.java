/**
 * ============================================================
 *  PATTERN 7 — STATEFUL STREAM PROCESSING
 *  Problem 3 (Hard): Max Chunks To Make Sorted II   LC 768
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given an integer array arr (may contain duplicates), you can split it into
 *    some number of chunks (non-empty contiguous subarrays). After sorting each
 *    chunk individually and concatenating them, the result should equal the sorted
 *    array. Return the MAXIMUM number of chunks.
 *
 *  EXAMPLE:
 *    arr=[5,4,3,2,1]      → 1   (must sort the entire array as one chunk)
 *    arr=[2,1,3,4,4]      → 4   (chunks: [2,1],[3],[4],[4])
 *
 *  CONSTRAINTS:
 *    1 <= arr.length (= N) <= 2000
 *    0 <= arr[i] <= 10^8
 *    arr may contain duplicates (unlike LC 769 which has no duplicates).
 *
 *  CHUNK BOUNDARY CONDITION (for arrays WITH duplicates):
 *    Position i can end a chunk if:
 *      max(arr[0..i]) <= min(arr[i+1..N-1])
 *    The maximum of the current chunk must NOT exceed the minimum of everything
 *    yet to be processed (otherwise sorting this chunk alone would place elements
 *    in the wrong position relative to later chunks).
 *
 *  APPROACH 1: Prefix max from left + suffix min from right, O(N) space
 *    Time:  O(N)
 *    Space: O(N) — prefixMax[] + suffixMin[]
 *
 *  APPROACH 2: Monotonic stack on max values — O(N) time, O(N) space
 *    Maintain a monotonic INCREASING stack of "chunk max values."
 *    When a new element is smaller than the current chunk max, it must merge backward
 *    into the previous chunk. Pop until stable, tracking merged max.
 *    Time:  O(N)
 *    Space: O(N)
 *
 *  APPROACH 3: Array-backed stack (zero GC version of approach 2)
 *    Time:  O(N)
 *    Space: O(N) primitives
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Max_Chunks_To_Make_Sorted_II {

    // =========================================================
    // APPROACH 1 — PREFIX MAX + SUFFIX MIN ARRAYS
    // =========================================================

    /**
     * Finds max chunks using precomputed prefix max and suffix min arrays.
     *
     * CHUNK BOUNDARY INVARIANT:
     *   Position i is a VALID chunk boundary if:
     *     prefixMax[i] <= suffixMin[i + 1]
     *   i.e., no element in [0..i] is greater than any element in [i+1..N-1].
     *   If this holds, sorting [0..i] won't "bleed" any element past position i.
     *
     * Count all valid chunk boundaries + 1 (the last position is always a boundary).
     *
     * @param arr input array (may contain duplicates)
     * @return maximum number of chunks
     *
     * Time:  O(N)  — 3 linear passes: prefix max, suffix min, boundary counting
     * Space: O(N)  — two auxiliary int[] arrays
     */
    public int maxChunksPrefixSuffix(int[] arr) {
        if (arr == null || arr.length == 0) return 0;
        int totalElements = arr.length;

        // Build prefixMax: prefixMax[i] = max(arr[0], arr[1], ..., arr[i])
        int[] prefixMax = new int[totalElements];
        prefixMax[0] = arr[0];
        for (int i = 1; i < totalElements; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], arr[i]);
        }

        // Build suffixMin: suffixMin[i] = min(arr[i], arr[i+1], ..., arr[N-1])
        int[] suffixMin = new int[totalElements];
        suffixMin[totalElements - 1] = arr[totalElements - 1];
        for (int i = totalElements - 2; i >= 0; i--) {
            suffixMin[i] = Math.min(suffixMin[i + 1], arr[i]);
        }

        // Count valid chunk boundaries
        // Position N-1 is always a boundary (end of array = end of last chunk)
        int chunkCount = 0;
        for (int boundary = 0; boundary < totalElements - 1; boundary++) {
            if (prefixMax[boundary] <= suffixMin[boundary + 1]) {
                chunkCount++;   // valid split: left chunk max ≤ right side min
            }
        }
        chunkCount++;   // always include the final chunk ending at N-1

        return chunkCount;
    }

    // =========================================================
    // APPROACH 2 — MONOTONIC STACK OF CHUNK MAX VALUES
    // =========================================================

    /**
     * Finds max chunks using a monotonic INCREASING stack of chunk maximums.
     *
     * KEY INSIGHT:
     *   Each "chunk" is represented by its maximum value on the stack.
     *   When a new element arrives that is SMALLER than the current chunk max:
     *     It cannot start a new chunk — it must be merged into a PREVIOUS chunk.
     *     Specifically, it merges into the LAST chunk whose max is ≤ current element.
     *     We pop all chunks with max > current element, tracking the MERGED MAX.
     *     Then push the merged max as the new chunk's representative.
     *
     * WHY MONOTONIC INCREASING?
     *   Each entry on the stack = max of one chunk.
     *   Stack bottom-to-top: increasing chunk maxes.
     *   A new element smaller than the top means it belongs to an earlier chunk
     *   (the boundary between them would violate: left max > right element).
     *
     * STACK SIZE AT END = NUMBER OF CHUNKS.
     *
     * @param arr input array
     * @return maximum number of chunks
     *
     * Time:  O(N)  — each element pushed and popped at most once
     * Space: O(N)  — stack holds at most N chunk max values
     */
    public int maxChunksMonotonicStack(int[] arr) {
        if (arr == null || arr.length == 0) return 0;

        Deque<Integer> chunkMaxStack = new ArrayDeque<>();   // stores max of each chunk

        for (int currentElement : arr) {
            if (chunkMaxStack.isEmpty() || currentElement >= chunkMaxStack.peek()) {
                // Current element is ≥ all chunk maxes → start a NEW chunk
                chunkMaxStack.push(currentElement);
            } else {
                // Current element is SMALLER than the current chunk max.
                // We must MERGE backward. The merged chunk's max = the current stack top
                // (since all popped entries had smaller or equal maxes that we're absorbing
                //  into the chunk that WAS topped by the highest max we saw).
                int mergedChunkMax = chunkMaxStack.pop();   // save the current chunk max

                // Pop all earlier chunks whose max > currentElement
                // (they must merge with current since currentElement falls "within" them)
                while (!chunkMaxStack.isEmpty() && chunkMaxStack.peek() > currentElement) {
                    chunkMaxStack.pop();
                }

                // Push the merged chunk's max (not currentElement — the max is the highest
                // element we've seen across all merged chunks)
                chunkMaxStack.push(mergedChunkMax);
            }
        }

        // Number of entries on stack = number of valid chunks
        return chunkMaxStack.size();
    }

    // =========================================================
    // APPROACH 3 — ARRAY-BACKED STACK (ZERO GC)
    // =========================================================

    /**
     * Same monotonic stack algorithm with a raw int[] stack.
     *
     * ARRAY STACK INVARIANT:
     *   chunkMaxArray[0 .. stackTopPointer] = current chunk maxes (increasing order).
     *   stackTopPointer == -1 → no chunks formed yet.
     *
     * Time:  O(N)
     * Space: O(N) — int[] of size N (stack depth bounded by chunk count ≤ N)
     */
    public int maxChunksMonotonicStackArray(int[] arr) {
        if (arr == null || arr.length == 0) return 0;

        int   totalElements    = arr.length;
        int[] chunkMaxArray    = new int[totalElements];
        int   stackTopPointer  = -1;

        for (int currentElement : arr) {
            if (stackTopPointer == -1 || currentElement >= chunkMaxArray[stackTopPointer]) {
                // New chunk starts
                chunkMaxArray[++stackTopPointer] = currentElement;
            } else {
                // Merge backward
                int mergedChunkMax = chunkMaxArray[stackTopPointer--];   // POP and save

                while (stackTopPointer >= 0 && chunkMaxArray[stackTopPointer] > currentElement) {
                    stackTopPointer--;   // POP (merge)
                }

                chunkMaxArray[++stackTopPointer] = mergedChunkMax;   // PUSH merged max
            }
        }

        return stackTopPointer + 1;   // stack size = number of chunks
    }

    // =========================================================
    // APPROACH 4 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Max_Chunks_To_Make_Sorted_II solver =
            new Problem_3_Hard_Max_Chunks_To_Make_Sorted_II();

        System.out.println("========================================");
        System.out.println("  Max Chunks To Make Sorted II — Test Suite");
        System.out.println("========================================");

        java.util.function.Consumer<int[]> testAll = (arr) -> {
            int ps    = solver.maxChunksPrefixSuffix(arr);
            int ms    = solver.maxChunksMonotonicStack(arr);
            int msa   = solver.maxChunksMonotonicStackArray(arr);
            boolean ok = (ps == ms && ms == msa);
            System.out.printf("arr=%-30s | PrefixSuffix=%d MonoStack=%d ArrayStack=%d %s%n",
                Arrays.toString(arr), ps, ms, msa, ok ? "✓" : "FAIL");
        };

        System.out.println("\n--- LC 768 examples ---");
        testAll.accept(new int[]{5,4,3,2,1});    // expected 1
        testAll.accept(new int[]{2,1,3,4,4});    // expected 4

        System.out.println("\n--- Already sorted ---");
        testAll.accept(new int[]{1,2,3,4,5});    // expected 5

        System.out.println("\n--- All same ---");
        testAll.accept(new int[]{3,3,3,3});      // expected 4 (each element is its own chunk)

        System.out.println("\n--- Two elements ---");
        testAll.accept(new int[]{1,2});           // expected 2
        testAll.accept(new int[]{2,1});           // expected 1

        System.out.println("\n--- Duplicates requiring merges ---");
        testAll.accept(new int[]{3,2,4,2,3});    // expected 2
        testAll.accept(new int[]{1,0,0,0,1});    // expected 2

        System.out.println("\n--- Single element ---");
        testAll.accept(new int[]{42});           // expected 1

        System.out.println("\n--- Large increasing then small ---");
        testAll.accept(new int[]{1,2,3,1});      // expected 1 (the 1 forces merge)

        System.out.println("\n--- Complex case ---");
        testAll.accept(new int[]{6,3,4,5,7,1,2,8});  // verify

        System.out.println("\n========================================");
        System.out.println("  All Max Chunks tests done.");
        System.out.println("========================================");
    }
}
