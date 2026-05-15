package interviewbit.array;

public class KthRowPascalTriangle {
    public int[] getRow(int k) {
        int[] res = new int[k + 1];

        res[0] = 1;

        for (int i = 1; i <= k; i++) {
            for (int j = i; j >= 1; j--) {
                res[j] = res[j] + res[j - 1];
            }
        }
        return res;
    }
}
