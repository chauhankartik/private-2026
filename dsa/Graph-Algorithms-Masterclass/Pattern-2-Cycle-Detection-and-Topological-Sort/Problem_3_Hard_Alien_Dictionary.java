/**
 * ============================================================
 *  PATTERN 2 — CYCLE DETECTION AND TOPOLOGICAL SORT
 *  Problem 3 (Hard): Alien Dictionary   LC 269
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a sorted list of alien language words, derive the character ordering
 *    of the alien language. Return a string of the unique characters in topological
 *    order. If impossible (cycle), return "". If multiple valid orders, return any.
 *
 *  EXAMPLE:
 *    words = ["wrt","wrf","er","ett","rftt"]
 *    Output: "wertf"
 *    (Compare adjacent words: wrt→wrf gives t<f, wrf→er gives w<e, er→ett gives r<t, ett→rftt gives e<r)
 *
 *  CONSTRAINTS:
 *    1 <= words.length <= 100
 *    1 <= words[i].length <= 100
 *    All characters are lowercase English letters.
 *
 *  APPROACH 1: Build character DAG from adjacent word comparisons + Kahn's BFS
 *    Time:  O(N × L + V + E)  — N words of length L; V=26 chars, E<=25 edges
 *    Space: O(V + E) = O(1)   — at most 26 characters
 *
 *  APPROACH 2: Same graph + DFS topological sort
 *    Time:  O(N × L + V + E)
 *    Space: O(V + E)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Alien_Dictionary {

    // =========================================================
    // APPROACH 1 — GRAPH BUILDING + KAHN'S BFS
    // =========================================================

    /**
     * Derives the alien language character ordering using topological sort.
     *
     * GRAPH CONSTRUCTION:
     *   For each pair of adjacent words (words[i], words[i+1]):
     *     Find the FIRST position where the characters differ.
     *     That gives one ordering constraint: words[i][pos] → words[i+1][pos].
     *   INVALID: if words[i] is a PREFIX of words[i+1] but appears AFTER it
     *   (e.g., "ab" appears before "a" → impossible → return "").
     *
     * @param words sorted list of words in alien language
     * @return topological character order, or "" if impossible/invalid
     *
     * Time:  O(N × L + V + E)  where V ≤ 26, E ≤ 25
     * Space: O(V + E) = O(1)   — constant character universe size
     */
    public String alienOrder(String[] words) {
        if (words == null || words.length == 0) return "";

        // Collect all unique characters present in the words
        Set<Character> presentCharacters = new LinkedHashSet<>();
        for (String word : words) {
            for (char ch : word.toCharArray()) presentCharacters.add(ch);
        }

        int totalCharacters = presentCharacters.size();

        // Map character to index 0..totalCharacters-1
        Map<Character, Integer> charToIndex = new HashMap<>();
        int idx = 0;
        for (char ch : presentCharacters) charToIndex.put(ch, idx++);

        // Build adjacency list and inDegree array
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < totalCharacters; i++) adjacencyList.add(new ArrayList<>());
        int[] inDegree = new int[totalCharacters];

        // Set to avoid duplicate edges (multiple word pairs may give same constraint)
        Set<Long> addedEdges = new HashSet<>();

        for (int wordIndex = 0; wordIndex < words.length - 1; wordIndex++) {
            String currentWord = words[wordIndex];
            String nextWord    = words[wordIndex + 1];
            int shorterLength  = Math.min(currentWord.length(), nextWord.length());

            boolean foundDifference = false;

            for (int charPos = 0; charPos < shorterLength; charPos++) {
                char fromChar = currentWord.charAt(charPos);
                char toChar   = nextWord.charAt(charPos);

                if (fromChar != toChar) {
                    int fromIndex = charToIndex.get(fromChar);
                    int toIndex   = charToIndex.get(toChar);
                    long edgeKey  = (long) fromIndex * 26 + toIndex;

                    if (addedEdges.add(edgeKey)) {   // add edge only once
                        adjacencyList.get(fromIndex).add(toIndex);
                        inDegree[toIndex]++;
                    }
                    foundDifference = true;
                    break;
                }
            }

            // CRITICAL EDGE CASE: "abc" appears before "ab" → invalid (prefix appears after full word)
            if (!foundDifference && currentWord.length() > nextWord.length()) {
                return "";   // impossible
            }
        }

        // Kahn's BFS topological sort
        Queue<Integer> zeroInDegreeQueue = new LinkedList<>();
        for (int i = 0; i < totalCharacters; i++) {
            if (inDegree[i] == 0) zeroInDegreeQueue.offer(i);
        }

        // Reverse mapping: index → character
        char[] indexToChar = new char[totalCharacters];
        for (Map.Entry<Character, Integer> entry : charToIndex.entrySet()) {
            indexToChar[entry.getValue()] = entry.getKey();
        }

        StringBuilder alienOrderResult = new StringBuilder();

        while (!zeroInDegreeQueue.isEmpty()) {
            int currentCharIndex = zeroInDegreeQueue.poll();
            alienOrderResult.append(indexToChar[currentCharIndex]);

            for (int dependentCharIndex : adjacencyList.get(currentCharIndex)) {
                if (--inDegree[dependentCharIndex] == 0) {
                    zeroInDegreeQueue.offer(dependentCharIndex);
                }
            }
        }

        // If not all characters included → cycle exists
        return alienOrderResult.length() == totalCharacters ? alienOrderResult.toString() : "";
    }

    // =========================================================
    // APPROACH 2 — SAME GRAPH + DFS TOPOLOGICAL SORT
    // =========================================================

    /**
     * Alternative DFS-based implementation.
     * Uses 3-color DFS to detect cycles and collect post-order.
     *
     * Time:  O(N × L + V + E)
     * Space: O(V) for color[] and result stack
     */
    public String alienOrderDFS(String[] words) {
        if (words == null || words.length == 0) return "";

        // Collect characters and assign indices
        Set<Character> chars = new LinkedHashSet<>();
        for (String w : words) for (char c : w.toCharArray()) chars.add(c);
        int total = chars.size();

        Map<Character, Integer> charIdx = new HashMap<>();
        int i = 0;
        for (char c : chars) charIdx.put(c, i++);

        List<Set<Integer>> adjacencyList = new ArrayList<>();
        for (int j = 0; j < total; j++) adjacencyList.add(new LinkedHashSet<>());

        for (int wi = 0; wi < words.length - 1; wi++) {
            String curr = words[wi], next = words[wi + 1];
            int shorter = Math.min(curr.length(), next.length());
            boolean found = false;
            for (int p = 0; p < shorter; p++) {
                if (curr.charAt(p) != next.charAt(p)) {
                    adjacencyList.get(charIdx.get(curr.charAt(p))).add(charIdx.get(next.charAt(p)));
                    found = true;
                    break;
                }
            }
            if (!found && curr.length() > next.length()) return "";
        }

        int[] color = new int[total];
        Deque<Integer> finishStack = new ArrayDeque<>();
        boolean[] cycleFound = {false};

        for (int start = 0; start < total; start++) {
            if (color[start] == 0) {
                dfsAlien(adjacencyList, start, color, finishStack, cycleFound);
                if (cycleFound[0]) return "";
            }
        }

        char[] indexToChar = new char[total];
        for (Map.Entry<Character, Integer> e : charIdx.entrySet()) indexToChar[e.getValue()] = e.getKey();

        StringBuilder sb = new StringBuilder();
        while (!finishStack.isEmpty()) sb.append(indexToChar[finishStack.pop()]);
        return sb.toString();
    }

    private void dfsAlien(List<Set<Integer>> adj, int curr, int[] color,
                           Deque<Integer> stack, boolean[] cycleFound) {
        if (cycleFound[0]) return;
        color[curr] = 1;
        for (int neighbor : adj.get(curr)) {
            if (color[neighbor] == 1) { cycleFound[0] = true; return; }
            if (color[neighbor] == 0) dfsAlien(adj, neighbor, color, stack, cycleFound);
        }
        color[curr] = 2;
        stack.push(curr);
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Alien_Dictionary solver = new Problem_3_Hard_Alien_Dictionary();

        System.out.println("==============================================");
        System.out.println("  Alien Dictionary — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic example ---
        System.out.println("\n--- Test 1: wrt, wrf, er, ett, rftt ---");
        String[] w1 = {"wrt","wrf","er","ett","rftt"};
        System.out.println("Kahn's: " + solver.alienOrder(w1) + " (expected wertf)");
        System.out.println("DFS:    " + solver.alienOrderDFS(w1));

        // --- Test 2: Cycle ---
        System.out.println("\n--- Test 2: z,x (valid) ---");
        String[] w2 = {"z","x"};
        System.out.println("Kahn's: " + solver.alienOrder(w2) + " (expected zx)");

        // --- Test 3: Invalid (prefix before base) ---
        System.out.println("\n--- Test 3: abc before ab (INVALID) ---");
        String[] w3 = {"abc","ab"};
        System.out.println("Kahn's: \"" + solver.alienOrder(w3) + "\" (expected empty string)");
        System.out.println("DFS:    \"" + solver.alienOrderDFS(w3) + "\" (expected empty string)");

        // --- Test 4: Cycle in constraints ---
        System.out.println("\n--- Test 4: z,x,z (cycle z→x→z) ---");
        String[] w4 = {"z","x","z"};
        System.out.println("Kahn's: \"" + solver.alienOrder(w4) + "\" (expected empty)");

        // --- Test 5: Single word ---
        System.out.println("\n--- Test 5: Single word 'abc' ---");
        String[] w5 = {"abc"};
        System.out.println("Result: " + solver.alienOrder(w5) + " (any permutation of a,b,c is valid)");

        // --- Test 6: All same words ---
        System.out.println("\n--- Test 6: All same words ['aa','aa'] ---");
        String[] w6 = {"aa","aa"};
        System.out.println("Result: " + solver.alienOrder(w6) + " (expected 'a' — no ordering constraints)");

        System.out.println("\n==============================================");
        System.out.println("  All Alien Dictionary tests completed.");
        System.out.println("==============================================");
    }
}
