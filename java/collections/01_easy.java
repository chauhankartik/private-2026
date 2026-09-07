import java.util.*;

/**
 * ============================================================
 *  JAVA COLLECTIONS — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem demonstrates basic
 *  API usage and common pitfalls.
 * ============================================================
 *
 *  Problems:
 *   E1. Safe Iteration & Removal
 *   E2. Custom Sorting (Comparator & Comparable)
 *   E3. Frequency Counting
 *   E4. Array Deduplication
 *
 * ============================================================
 */
public class Easy {

    // =========================================================
    // E1. SAFE ITERATION & REMOVAL
    // Pattern: Fail-Fast Iterators
    // =========================================================
    /**
     * Problem: Given a List of integers, remove all odd numbers.
     *
     * Brute Force / Common Mistake:
     *   for (Integer n : list) {
     *       if (n % 2 != 0) list.remove(n); // THROWS ConcurrentModificationException
     *   }
     *
     * Optimal Solution: Use an Iterator's remove() method or Java 8 removeIf().
     *
     * Time Complexity: O(n) or O(n^2) depending on List type.
     *   - ArrayList: O(n^2) because each remove shifts elements O(n).
     *   - LinkedList: O(n) because remove via iterator is O(1).
     * Space Complexity: O(1)
     */
    public void removeOdds(List<Integer> list) {
        // Java 8+ approach (cleanest)
        // list.removeIf(n -> n % 2 != 0);

        // Pre-Java 8 approach (using Iterator)
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer n = iterator.next();
            if (n % 2 != 0) {
                iterator.remove(); // Safe removal
            }
        }
    }

    /**
     * Follow-up: How to do this efficiently for ArrayList in O(n) time?
     *   → Two-pointer approach. Overwrite elements in-place and truncate the end.
     */
    public void removeOddsEfficient(ArrayList<Integer> list) {
        int writeIdx = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) % 2 == 0) {
                list.set(writeIdx++, list.get(i));
            }
        }
        // Truncate the list (subList returns a view, clear removes elements from the original list)
        list.subList(writeIdx, list.size()).clear();
    }

    // =========================================================
    // E2. CUSTOM SORTING
    // Pattern: Comparator vs Comparable
    // =========================================================
    /**
     * Problem: Sort a list of strings by their length, then alphabetically.
     *
     * Key insight: Collections.sort() uses Timsort (O(n log n)).
     * We pass a custom Comparator.
     *
     * Time Complexity: O(n log n)
     * Space Complexity: O(n) for Timsort internally
     */
    public void sortStrings(List<String> list) {
        list.sort((a, b) -> {
            if (a.length() != b.length()) {
                return Integer.compare(a.length(), b.length());
            }
            return a.compareTo(b); // fallback to natural ordering
        });
        // Or using Java 8 Comparator combinators:
        // list.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
    }

    // =========================================================
    // E3. FREQUENCY COUNTING
    // Pattern: Map.getOrDefault / merge
    // =========================================================
    /**
     * Problem: Count the frequency of each word in an array of words.
     *
     * Time Complexity: O(n) where n is number of words (assuming word length is bounded)
     * Space Complexity: O(n) for the Map
     */
    public Map<String, Integer> countFrequencies(String[] words) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            // Standard approach
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            
            // Java 8 merge approach (alternative)
            // freqMap.merge(word, 1, Integer::sum);
        }
        return freqMap;
    }

    /**
     * Follow-up: What if we want the output sorted by frequency (highest first)?
     *   → Use a PriorityQueue or sort a List of Map.Entry.
     */

    // =========================================================
    // E4. ARRAY DEDUPLICATION
    // Pattern: HashSet / LinkedHashSet
    // =========================================================
    /**
     * Problem: Remove duplicate elements from an array but maintain original insertion order.
     *
     * Key insight: HashSet removes duplicates but doesn't guarantee order.
     * LinkedHashSet maintains insertion order.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public int[] removeDuplicatesPreserveOrder(int[] nums) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int num : nums) {
            set.add(num);
        }
        
        int[] result = new int[set.size()];
        int index = 0;
        for (int num : set) {
            result[index++] = num;
        }
        return result;
    }
}
