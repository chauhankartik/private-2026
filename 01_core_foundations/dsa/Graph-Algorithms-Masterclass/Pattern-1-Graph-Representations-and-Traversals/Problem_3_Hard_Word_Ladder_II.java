/**
 * ============================================================
 *  PATTERN 1 — GRAPH REPRESENTATIONS AND TRAVERSALS
 *  Problem 3 (Hard): Word Ladder II   LC 126
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given a beginWord, endWord, and a wordList dictionary, return ALL shortest
 *    transformation sequences from beginWord to endWord such that:
 *      1. Only one letter is changed at a time.
 *      2. Each transformed word must exist in the wordList.
 *    Return an empty list if no such sequence exists.
 *
 *  EXAMPLE:
 *    beginWord = "hit", endWord = "cog"
 *    wordList = ["hot","dot","dog","lot","log","cog"]
 *    Output: [["hit","hot","dot","dog","cog"], ["hit","hot","lot","log","cog"]]
 *
 *  CONSTRAINTS:
 *    1 <= beginWord.length == endWord.length (= L) <= 5
 *    1 <= wordList.length (= N) <= 500
 *    All words have the same length L.
 *
 *  APPROACH 1: BFS (level-by-level) to find shortest length + DFS backtrack to collect all paths
 *    Time:  O(N × L × 26 + N × result_paths)   — BFS: O(N×L×26), DFS: O(paths)
 *    Space: O(N × L)  — graph (parents map) + BFS queue
 *
 *  APPROACH 2: Bidirectional BFS to halve the search space
 *    Time:  O(N × L × 26) in practice (often 2-4× faster than unidirectional)
 *    Space: O(N × L)
 * ============================================================
 */
import java.util.*;

public class Problem_3_Hard_Word_Ladder_II {

    // =========================================================
    // APPROACH 1 — BFS (shortest path level) + DFS (all paths)
    // =========================================================

    /**
     * Finds all shortest transformation sequences from beginWord to endWord.
     *
     * STRATEGY:
     *   Step 1: BFS from beginWord, building a "parents" map.
     *           parents.get(word) = set of words that can transform INTO word in ONE step.
     *           BFS ensures we only record shortest-distance parents.
     *   Step 2: DFS backwards from endWord using the parents map to collect all paths.
     *
     * WHY BFS FIRST?
     *   DFS alone explores exponentially many paths and finds non-shortest ones.
     *   BFS guarantees: once we reach endWord at level k, ALL paths of length k are valid.
     *   We stop BFS after the first level that reaches endWord (don't explore level k+1).
     *
     * @param beginWord start word (not necessarily in wordList)
     * @param endWord   target word
     * @param wordList  valid transformation words
     * @return all shortest transformation sequences, or empty list if none
     *
     * Time:  O(N × L × 26)  for BFS  (N words, L characters, 26 letter choices)
     * Space: O(N × L)        for parents map + BFS structures
     */
    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        List<List<String>> allShortestPaths = new ArrayList<>();

        if (beginWord == null || endWord == null || wordList == null) return allShortestPaths;

        Set<String> dictionary = new HashSet<>(wordList);
        if (!dictionary.contains(endWord)) return allShortestPaths;

        // parents.get(word) = all words that can transition to 'word' in one step
        // and are on a shortest path
        Map<String, Set<String>> parents = new HashMap<>();

        // BFS state: track which words are discovered at the CURRENT level
        Set<String> currentLevel = new HashSet<>();
        currentLevel.add(beginWord);
        Set<String> visited = new HashSet<>();
        visited.add(beginWord);
        boolean foundEndWord = false;

        while (!currentLevel.isEmpty() && !foundEndWord) {
            Set<String> nextLevel = new HashSet<>();
            // Remove current level from dictionary AFTER processing all of currentLevel
            // (allows multiple words at the same level to share a target)
            dictionary.removeAll(currentLevel);

            for (String currentWord : currentLevel) {
                char[] wordCharacters = currentWord.toCharArray();
                int wordLength = wordCharacters.length;

                for (int charPosition = 0; charPosition < wordLength; charPosition++) {
                    char originalChar = wordCharacters[charPosition];

                    for (char substitutedChar = 'a'; substitutedChar <= 'z'; substitutedChar++) {
                        if (substitutedChar == originalChar) continue;

                        wordCharacters[charPosition] = substitutedChar;
                        String transformedWord = new String(wordCharacters);

                        if (dictionary.contains(transformedWord)) {
                            nextLevel.add(transformedWord);
                            // Record currentWord as a parent of transformedWord
                            parents.computeIfAbsent(transformedWord, k -> new HashSet<>()).add(currentWord);

                            if (transformedWord.equals(endWord)) {
                                foundEndWord = true;
                                // Don't break — other words at this level may also reach endWord
                            }
                        }
                        wordCharacters[charPosition] = originalChar;   // restore
                    }
                }
            }
            currentLevel = nextLevel;
        }

        if (!foundEndWord) return allShortestPaths;

        // DFS backwards from endWord using parents map
        List<String> currentPath = new ArrayList<>();
        currentPath.add(endWord);
        dfsBacktrack(endWord, beginWord, parents, currentPath, allShortestPaths);
        return allShortestPaths;
    }

    /**
     * DFS backtracking: builds paths from endWord back to beginWord using the parents map.
     * Reverses each completed path before adding to results.
     */
    private void dfsBacktrack(
            String currentWord,
            String beginWord,
            Map<String, Set<String>> parents,
            List<String> currentPath,
            List<List<String>> allShortestPaths) {

        if (currentWord.equals(beginWord)) {
            List<String> completedPath = new ArrayList<>(currentPath);
            Collections.reverse(completedPath);
            allShortestPaths.add(completedPath);
            return;
        }

        Set<String> parentWords = parents.get(currentWord);
        if (parentWords == null) return;

        for (String parentWord : parentWords) {
            currentPath.add(parentWord);
            dfsBacktrack(parentWord, beginWord, parents, currentPath, allShortestPaths);
            currentPath.remove(currentPath.size() - 1);   // backtrack
        }
    }

    // =========================================================
    // APPROACH 2 — BIDIRECTIONAL BFS (FASTER IN PRACTICE)
    // =========================================================

    /**
     * Bidirectional BFS: simultaneously expands from beginWord AND endWord.
     * When the two frontiers meet, we've found the shortest path level.
     *
     * WHY FASTER?
     *   Unidirectional BFS explores a ball of radius r from source: ~b^r nodes.
     *   Bidirectional explores two balls of radius r/2: ~2 × b^(r/2) nodes.
     *   For large branching factor b, this is exponentially fewer nodes.
     *
     * IMPLEMENTATION SIMPLIFICATION:
     *   Here we demonstrate the "meet in middle" BFS structure
     *   that returns shortest path length (not all paths).
     *   Full path reconstruction requires storing two parent maps (one per direction).
     *
     * @return shortest transformation length, or -1 if no path exists
     *
     * Time:  O(N × L × 26) — often 2-4× fewer nodes visited than unidirectional
     * Space: O(N × L)
     */
    public int shortestLadderLengthBidirectional(
            String beginWord, String endWord, List<String> wordList) {

        Set<String> dictionary = new HashSet<>(wordList);
        if (!dictionary.contains(endWord)) return -1;
        if (beginWord.equals(endWord)) return 1;

        Set<String> forwardFrontier = new HashSet<>();
        Set<String> backwardFrontier = new HashSet<>();
        forwardFrontier.add(beginWord);
        backwardFrontier.add(endWord);
        dictionary.remove(beginWord);
        dictionary.remove(endWord);

        int transformationLength = 1;

        while (!forwardFrontier.isEmpty() && !backwardFrontier.isEmpty()) {
            transformationLength++;

            // Always expand the smaller frontier (optimization)
            if (forwardFrontier.size() > backwardFrontier.size()) {
                Set<String> temp = forwardFrontier;
                forwardFrontier = backwardFrontier;
                backwardFrontier = temp;
            }

            Set<String> newFrontier = new HashSet<>();

            for (String currentWord : forwardFrontier) {
                char[] wordChars = currentWord.toCharArray();

                for (int pos = 0; pos < wordChars.length; pos++) {
                    char original = wordChars[pos];

                    for (char candidate = 'a'; candidate <= 'z'; candidate++) {
                        if (candidate == original) continue;
                        wordChars[pos] = candidate;
                        String nextWord = new String(wordChars);

                        if (backwardFrontier.contains(nextWord)) {
                            return transformationLength;   // frontiers met
                        }
                        if (dictionary.contains(nextWord)) {
                            newFrontier.add(nextWord);
                            dictionary.remove(nextWord);
                        }
                        wordChars[pos] = original;
                    }
                }
            }
            forwardFrontier = newFrontier;
        }
        return -1;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_3_Hard_Word_Ladder_II solver = new Problem_3_Hard_Word_Ladder_II();

        System.out.println("========================================");
        System.out.println("  Word Ladder II — Test Suite");
        System.out.println("========================================");

        // --- Test 1: Classic example ---
        System.out.println("\n--- Test 1: Classic hit→cog ---");
        List<String> dict1 = Arrays.asList("hot","dot","dog","lot","log","cog");
        List<List<String>> result1 = solver.findLadders("hit", "cog", dict1);
        System.out.println("All shortest paths (" + result1.size() + " paths):");
        for (List<String> path : result1) System.out.println("  " + path);
        System.out.println("Bidirectional BFS length: " +
            solver.shortestLadderLengthBidirectional("hit", "cog", new ArrayList<>(dict1)));

        // --- Test 2: No path exists ---
        System.out.println("\n--- Test 2: No path (endWord not in dict) ---");
        List<String> dict2 = Arrays.asList("hot","dot","dog");
        List<List<String>> result2 = solver.findLadders("hit", "cog", dict2);
        System.out.println("Paths: " + result2 + " (expected [])");

        // --- Test 3: Begin equals end ---
        System.out.println("\n--- Test 3: Single-letter words ---");
        List<String> dict3 = Arrays.asList("a","b","c");
        List<List<String>> result3 = solver.findLadders("a", "c", dict3);
        System.out.println("Paths a→c: " + result3);

        // --- Test 4: Multiple paths of same length ---
        System.out.println("\n--- Test 4: Multiple shortest paths ---");
        List<String> dict4 = Arrays.asList("hot","dog","dot","lot","log","cog","hog");
        List<List<String>> result4 = solver.findLadders("hot", "cog", dict4);
        System.out.println("Shortest paths hot→cog (" + result4.size() + " paths):");
        for (List<String> path : result4) System.out.println("  " + path);

        // --- Test 5: Direct one-step transformation ---
        System.out.println("\n--- Test 5: Direct one-step (abc→axc) ---");
        List<String> dict5 = Arrays.asList("axc","abc","acc");
        List<List<String>> result5 = solver.findLadders("abc", "axc", dict5);
        System.out.println("Paths: " + result5);

        // --- Test 6: Empty wordList ---
        System.out.println("\n--- Test 6: Empty wordList ---");
        List<List<String>> result6 = solver.findLadders("hit", "cog", new ArrayList<>());
        System.out.println("Paths (empty dict): " + result6 + " (expected [])");

        System.out.println("\n========================================");
        System.out.println("  All Word Ladder II tests completed.");
        System.out.println("========================================");
    }
}
