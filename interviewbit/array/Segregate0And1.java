package interviewbit.array;

public class Segregate0And1 {
    public int[] solve(int[] A) {
        int i = 0, j = A.length - 1;
        while (i < j) {
            while (A[i] == 0 && i < j) {
                i++;
            }
            while (A[j] == 1 && i < j) {
                j--;
            }

            if (i < j) {
                int temp = A[i];
                A[i] = A[j];
                A[j] = temp;
                i++;
                j--;
            }
        }
        return A;
    }

    public static void main(String[] args) {

    }
}