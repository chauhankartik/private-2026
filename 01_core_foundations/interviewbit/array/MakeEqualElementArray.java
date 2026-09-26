package interviewbit.array;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MakeEqualElementArray {
    public int solve(int[] A, int B) {
        int n = A.length;
        Set<Integer> distinct = new HashSet<>();
        for (int i = 0; i < n; i++) {
            distinct.add(A[i]);
            if (distinct.size() > 3) {
                return 0;
            }
        }
        if (distinct.size() == 1)
            return 1;

        Integer[] arr = distinct.toArray(new Integer[0]);

        if (distinct.size() == 2) {
            int diff = Math.abs(arr[0] - arr[1]);
            if (diff == B || diff == 2 * B) {
                return 1;
            }
            return 0;
        }
        if (distinct.size() == 3) {
            Arrays.sort(arr);
            int low = arr[0];
            int mid = arr[1];
            int high = arr[2];

            if (mid - low == B && high - mid == B) {
                return 1;
            }
            return 0;
        }
        return -1;
    }
}
