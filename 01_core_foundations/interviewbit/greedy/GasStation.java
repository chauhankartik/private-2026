package interviewbit.greedy;

/**
 * InterviewBit: Gas Station
 *
 * Problem Description:
 * Given two integer arrays A and B of size N.
 * There are N gas stations along a circular route, where the amount of gas at station i is A[i].
 * You have a car with an unlimited gas tank and it costs B[i] of gas to travel from station i to its next station (i+1).
 * You begin the journey with an empty tank at one of the gas stations.
 *
 * Return the minimum starting gas station's index if you can travel around the circuit once, otherwise return -1.
 * You can only travel in one direction: i -> i+1, i+2, ... n-1, 0, 1, 2...
 *
 * Time Complexity: O(N)
 * Space Complexity: O(1)
 */
public class GasStation {

    /**
     * Finds the minimum starting gas station index to complete the circuit once.
     *
     * @param A gas available at each station
     * @param B gas cost to travel to the next station
     * @return 0-based starting index if circuit can be completed, else -1
     */
    public int canCompleteCircuit(final int[] A, final int[] B) {
        if (A == null || B == null || A.length == 0 || B.length == 0 || A.length != B.length) {
            return -1;
        }

        int totalGas = 0;
        int totalCost = 0;
        int currentTank = 0;
        int startIndex = 0;

        for (int i = 0; i < A.length; i++) {
            totalGas += A[i];
            totalCost += B[i];
            currentTank += A[i] - B[i];

            // If current tank drops below 0, station startIndex to i cannot be starting points
            if (currentTank < 0) {
                startIndex = i + 1;
                currentTank = 0; // Reset tank for the next candidate starting point
            }
        }

        // If total gas available is at least total cost, a valid circuit exists
        return (totalGas >= totalCost) ? startIndex : -1;
    }

    public static void main(String[] args) {
        GasStation solver = new GasStation();

        // Test Case 1: Valid starting point at index 3
        int[] A1 = {1, 2, 3, 4, 5};
        int[] B1 = {3, 4, 5, 1, 2};
        int result1 = solver.canCompleteCircuit(A1, B1);
        System.out.println("Test Case 1 Expected: 3 | Actual: " + result1);
        assert result1 == 3 : "Test Case 1 Failed";

        // Test Case 2: Impossible circuit
        int[] A2 = {2, 3, 4};
        int[] B2 = {3, 4, 3};
        int result2 = solver.canCompleteCircuit(A2, B2);
        System.out.println("Test Case 2 Expected: -1 | Actual: " + result2);
        assert result2 == -1 : "Test Case 2 Failed";

        // Test Case 3: Single station with enough gas
        int[] A3 = {5};
        int[] B3 = {3};
        int result3 = solver.canCompleteCircuit(A3, B3);
        System.out.println("Test Case 3 Expected: 0 | Actual: " + result3);
        assert result3 == 0 : "Test Case 3 Failed";

        // Test Case 4: Single station with insufficient gas
        int[] A4 = {2};
        int[] B4 = {3};
        int result4 = solver.canCompleteCircuit(A4, B4);
        System.out.println("Test Case 4 Expected: -1 | Actual: " + result4);
        assert result4 == -1 : "Test Case 4 Failed";

        // Test Case 5: Exact gas matches exact cost
        int[] A5 = {3, 1, 1};
        int[] B5 = {1, 2, 2};
        int result5 = solver.canCompleteCircuit(A5, B5);
        System.out.println("Test Case 5 Expected: 0 | Actual: " + result5);
        assert result5 == 0 : "Test Case 5 Failed";

        System.out.println("All test cases passed successfully!");
    }
}
