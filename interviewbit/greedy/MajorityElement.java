package interviewbit.greedy;

/**
 * InterviewBit: Majority Element
 *
 * Problem Description:
 * Given an array of size N, find the majority element.
 * The majority element is the element that appears more than floor(N/2) times.
 * You may assume that the array is non-empty and the majority element always exists in the array.
 *
 * Constraints:
 * 1 <= |A| <= 10^6
 * 1 <= A[i] <= 10^9
 *
 * Algorithm: Boyer-Moore Voting Algorithm
 * Time Complexity: O(N)
 * Space Complexity: O(1)
 */
public class MajorityElement {

    /**
     * Finds the majority element using Boyer-Moore Voting Algorithm.
     *
     * @param A array of integers
     * @return the majority element appearing > floor(N/2) times
     */
    public int majorityElement(final int[] A) {
        if (A == null || A.length == 0) {
            throw new IllegalArgumentException("Input array must be non-empty.");
        }

        int candidate = A[0];
        int count = 0;

        for (int num : A) {
            if (count == 0) {
                candidate = num;
                count = 1;
            } else if (num == candidate) {
                count++;
            } else {
                count--;
            }
        }

        return candidate;
    }

    public static void main(String[] args) {
        MajorityElement solver = new MajorityElement();

        // Test Case 1: Standard majority element
        int[] A1 = {3, 2, 3};
        int result1 = solver.majorityElement(A1);
        System.out.println("Test Case 1 Expected: 3 | Actual: " + result1);
        assert result1 == 3 : "Test Case 1 Failed";

        // Test Case 2: Even array size with dominant majority
        int[] A2 = {2, 2, 1, 1, 1, 2, 2};
        int result2 = solver.majorityElement(A2);
        System.out.println("Test Case 2 Expected: 2 | Actual: " + result2);
        assert result2 == 2 : "Test Case 2 Failed";

        // Test Case 3: Single element array
        int[] A3 = {100};
        int result3 = solver.majorityElement(A3);
        System.out.println("Test Case 3 Expected: 100 | Actual: " + result3);
        assert result3 == 100 : "Test Case 3 Failed";

        // Test Case 4: All elements identical
        int[] A4 = {5, 5, 5, 5, 5};
        int result4 = solver.majorityElement(A4);
        System.out.println("Test Case 4 Expected: 5 | Actual: " + result4);
        assert result4 == 5 : "Test Case 4 Failed";

        // Test Case 5: Large array test (10^6 elements)
        int n = 1_000_000;
        int[] A5 = new int[n];
        for (int i = 0; i < n; i++) {
            A5[i] = (i % 3 == 0) ? 999 : 42; // 42 appears ~66.6% of the time
        }
        int result5 = solver.majorityElement(A5);
        System.out.println("Test Case 5 Expected: 42 | Actual: " + result5);
        assert result5 == 42 : "Test Case 5 Failed";

        System.out.println("All Majority Element test cases passed successfully!");
    }
}
