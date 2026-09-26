/**
 * ============================================================
 *  PATTERN 4 — ALL-PAIRS SHORTEST PATH
 *  Problem 2 (Medium): Evaluate Division   LC 399
 * ============================================================
 *
 *  PROBLEM STATEMENT:
 *    Given equations like ["a/b=2.0", "b/c=3.0"] and queries like ["a/c", "b/a"],
 *    return the results of each query. Return -1.0 if the answer doesn't exist.
 *
 *  EXAMPLE:
 *    equations=[["a","b"],["b","c"]], values=[2.0,3.0]
 *    queries=[["a","c"],["b","a"],["a","e"],["a","a"],["x","x"]]
 *    Output: [6.0, 0.5, -1.0, 1.0, -1.0]
 *
 *  CONSTRAINTS:
 *    1 <= equations.length <= 20
 *    equations[i].length == 2; values[i] in (0.0, max_double)
 *    1 <= queries.length <= 20
 *
 *  APPROACH 1: Build a weighted directed graph + BFS per query
 *    Time:  O(E + Q × (V + E))  — E equations, Q queries, V variables
 *    Space: O(V + E)
 *
 *  APPROACH 2: Floyd-Warshall on ratio matrix (all-pairs precomputation)
 *    Time:  O(N³ + Q)  — N = number of unique variables
 *    Space: O(N²)
 * ============================================================
 */
import java.util.*;

public class Problem_2_Medium_Evaluate_Division {

    // =========================================================
    // APPROACH 1 — WEIGHTED GRAPH + BFS PER QUERY
    // =========================================================

    /**
     * Models the division equations as a weighted directed graph.
     * a/b = 2.0 becomes:
     *   Edge a→b with weight 2.0  (a/b = 2)
     *   Edge b→a with weight 0.5  (b/a = 1/2)
     *
     * For each query a/b:
     *   BFS from a to b, multiplying weights along the path.
     *   If b is unreachable → return -1.0.
     *
     * @param equations list of [numerator, denominator] pairs
     * @param values    corresponding division values
     * @param queries   list of queries [numerator, denominator]
     * @return results for each query
     *
     * Time:  O(E + Q × (V + E))
     * Space: O(V + E)
     */
    public double[] calcEquation(
            List<List<String>> equations,
            double[] values,
            List<List<String>> queries) {

        // Build graph: variable → {neighbor variable, weight}
        Map<String, List<double[]>> graph = new HashMap<>();

        for (int i = 0; i < equations.size(); i++) {
            String numeratorVar   = equations.get(i).get(0);
            String denominatorVar = equations.get(i).get(1);
            double ratio          = values[i];

            graph.computeIfAbsent(numeratorVar,   k -> new ArrayList<>())
                 .add(new double[]{0, ratio});             // placeholder for index
            graph.computeIfAbsent(denominatorVar, k -> new ArrayList<>())
                 .add(new double[]{0, 1.0 / ratio});       // reciprocal

            // Rebuild with string-based neighbor reference
            // (We'll use String-keyed map instead of index)
        }

        // Cleaner string-keyed adjacency structure
        Map<String, Map<String, Double>> adjacencyMap = new HashMap<>();
        for (int i = 0; i < equations.size(); i++) {
            String numVar   = equations.get(i).get(0);
            String denVar   = equations.get(i).get(1);
            double ratio    = values[i];

            adjacencyMap.computeIfAbsent(numVar, k -> new HashMap<>()).put(denVar, ratio);
            adjacencyMap.computeIfAbsent(denVar, k -> new HashMap<>()).put(numVar, 1.0 / ratio);

            // Self-loops: a/a = 1.0
            adjacencyMap.get(numVar).put(numVar, 1.0);
            adjacencyMap.get(denVar).put(denVar, 1.0);
        }

        double[] results = new double[queries.size()];

        for (int queryIdx = 0; queryIdx < queries.size(); queryIdx++) {
            String numeratorQuery   = queries.get(queryIdx).get(0);
            String denominatorQuery = queries.get(queryIdx).get(1);

            if (!adjacencyMap.containsKey(numeratorQuery) ||
                !adjacencyMap.containsKey(denominatorQuery)) {
                results[queryIdx] = -1.0;  // unknown variable
            } else {
                results[queryIdx] = bfsForRatio(adjacencyMap, numeratorQuery, denominatorQuery);
            }
        }
        return results;
    }

    /**
     * BFS from startVariable to endVariable, multiplying edge weights along the path.
     * Returns the product (= division result), or -1.0 if unreachable.
     */
    private double bfsForRatio(
            Map<String, Map<String, Double>> adjacencyMap,
            String startVariable,
            String endVariable) {

        if (startVariable.equals(endVariable)) return 1.0;

        Set<String> visitedVariables = new HashSet<>();
        Queue<double[]> bfsQueue = new LinkedList<>();  // stores {encodedVariable, currentProduct}
        // Use a paired queue since we need both the variable name and accumulated product
        Queue<String> variableQueue = new LinkedList<>();
        Queue<Double> productQueue  = new LinkedList<>();

        variableQueue.offer(startVariable);
        productQueue.offer(1.0);
        visitedVariables.add(startVariable);

        while (!variableQueue.isEmpty()) {
            String currentVariable  = variableQueue.poll();
            double accumulatedProduct = productQueue.poll();

            Map<String, Double> neighbors = adjacencyMap.get(currentVariable);
            if (neighbors == null) continue;

            for (Map.Entry<String, Double> neighborEntry : neighbors.entrySet()) {
                String neighborVariable = neighborEntry.getKey();
                double edgeWeight       = neighborEntry.getValue();

                if (neighborVariable.equals(endVariable)) {
                    return accumulatedProduct * edgeWeight;
                }

                if (!visitedVariables.contains(neighborVariable)) {
                    visitedVariables.add(neighborVariable);
                    variableQueue.offer(neighborVariable);
                    productQueue.offer(accumulatedProduct * edgeWeight);
                }
            }
        }
        return -1.0;  // endVariable not reachable from startVariable
    }

    // =========================================================
    // APPROACH 2 — FLOYD-WARSHALL ON RATIO MATRIX
    // =========================================================

    /**
     * Precomputes all-pairs ratios using Floyd-Warshall.
     * After precomputation, each query is O(1).
     * Optimal when there are many queries (Q >> N).
     *
     * RATIO MATRIX:
     *   ratioMatrix[i][j] = value of var[i] / var[j]
     *   ratioMatrix[i][j] = 0.0 if the ratio is unknown
     *
     * FLOYD-WARSHALL FOR MULTIPLICATION:
     *   Instead of dist[i][j] = min(dist[i][k] + dist[k][j]):
     *   ratio[i][j] = max(ratio[i][j], ratio[i][k] × ratio[k][j])
     *
     * @return results for each query
     *
     * Time:  O(N³ + Q)
     * Space: O(N²)
     */
    public double[] calcEquationFloydWarshall(
            List<List<String>> equations,
            double[] values,
            List<List<String>> queries) {

        // Assign integer indices to each unique variable
        Map<String, Integer> variableIndex = new HashMap<>();
        int nextIndex = 0;
        for (List<String> eq : equations) {
            for (String variable : eq) {
                if (!variableIndex.containsKey(variable)) {
                    variableIndex.put(variable, nextIndex++);
                }
            }
        }

        int totalVariables = nextIndex;
        double[][] ratioMatrix = new double[totalVariables][totalVariables];

        // Initialize: ratioMatrix[i][i] = 1.0 (a/a = 1)
        for (int i = 0; i < totalVariables; i++) ratioMatrix[i][i] = 1.0;

        // Fill known ratios from equations
        for (int eqIdx = 0; eqIdx < equations.size(); eqIdx++) {
            int numIdx = variableIndex.get(equations.get(eqIdx).get(0));
            int denIdx = variableIndex.get(equations.get(eqIdx).get(1));
            double ratio = values[eqIdx];
            ratioMatrix[numIdx][denIdx] = ratio;
            ratioMatrix[denIdx][numIdx] = 1.0 / ratio;
        }

        // Floyd-Warshall: propagate ratios through intermediates
        for (int intermediate = 0; intermediate < totalVariables; intermediate++) {
            for (int fromVar = 0; fromVar < totalVariables; fromVar++) {
                for (int toVar = 0; toVar < totalVariables; toVar++) {
                    if (ratioMatrix[fromVar][intermediate] > 0 && ratioMatrix[intermediate][toVar] > 0) {
                        ratioMatrix[fromVar][toVar] = ratioMatrix[fromVar][intermediate] * ratioMatrix[intermediate][toVar];
                    }
                }
            }
        }

        // Answer queries
        double[] results = new double[queries.size()];
        for (int qIdx = 0; qIdx < queries.size(); qIdx++) {
            String numVar = queries.get(qIdx).get(0);
            String denVar = queries.get(qIdx).get(1);

            if (!variableIndex.containsKey(numVar) || !variableIndex.containsKey(denVar)) {
                results[qIdx] = -1.0;
            } else {
                int numIdx = variableIndex.get(numVar);
                int denIdx = variableIndex.get(denVar);
                results[qIdx] = ratioMatrix[numIdx][denIdx] == 0.0 ? -1.0 : ratioMatrix[numIdx][denIdx];
            }
        }
        return results;
    }

    // =========================================================
    // APPROACH 3 — COMPREHENSIVE TEST SUITE
    // =========================================================
    public static void main(String[] args) {
        Problem_2_Medium_Evaluate_Division solver = new Problem_2_Medium_Evaluate_Division();

        System.out.println("==============================================");
        System.out.println("  Evaluate Division — Test Suite");
        System.out.println("==============================================");

        // --- Test 1: Classic LC 399 example ---
        System.out.println("\n--- Test 1: Classic example ---");
        List<List<String>> eq1 = List.of(List.of("a","b"), List.of("b","c"));
        double[] val1 = {2.0, 3.0};
        List<List<String>> q1 = List.of(
            List.of("a","c"), List.of("b","a"), List.of("a","e"),
            List.of("a","a"), List.of("x","x")
        );
        double[] bfsResult1  = solver.calcEquation(eq1, val1, q1);
        double[] fwResult1   = solver.calcEquationFloydWarshall(eq1, val1, q1);
        System.out.println("BFS results: " + Arrays.toString(bfsResult1));
        System.out.println("F-W results: " + Arrays.toString(fwResult1));
        System.out.println("Expected:    [6.0, 0.5, -1.0, 1.0, -1.0]");

        // --- Test 2: Single equation ---
        System.out.println("\n--- Test 2: Single equation a/b=2 ---");
        List<List<String>> eq2 = List.of(List.of("a","b"));
        double[] val2 = {2.0};
        List<List<String>> q2 = List.of(List.of("b","a"), List.of("a","b"), List.of("a","c"));
        System.out.println("BFS: " + Arrays.toString(solver.calcEquation(eq2, val2, q2)));
        System.out.println("F-W: " + Arrays.toString(solver.calcEquationFloydWarshall(eq2, val2, q2)));
        System.out.println("Expected: [0.5, 2.0, -1.0]");

        // --- Test 3: Same variable ---
        System.out.println("\n--- Test 3: x/x query ---");
        List<List<String>> eq3 = List.of(List.of("x","y"));
        double[] val3 = {3.0};
        List<List<String>> q3 = List.of(List.of("x","x"), List.of("y","y"));
        System.out.println("F-W: " + Arrays.toString(solver.calcEquationFloydWarshall(eq3, val3, q3)));
        System.out.println("Expected: [1.0, 1.0]");

        System.out.println("\n==============================================");
        System.out.println("  All Evaluate Division tests completed.");
        System.out.println("==============================================");
    }
}
