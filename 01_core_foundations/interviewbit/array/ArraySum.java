package interviewbit.array;

public class ArraySum {

    public int[] addArrays(int[] A, int[] B) {
        int n = A.length;
        int m = B.length;

        StringBuilder str = new StringBuilder();
        int carry = 0;

        while (n > 0 || m > 0 || carry > 0) {
            int sum = carry;
            if (n > 0) {
                sum += A[n - 1];
                n--;
            }
            if (m > 0) {
                sum += B[m - 1];
                m--;
            }
            carry = sum / 10;
            str.append(sum % 10);
        }
        str.reverse();
        int[] result = new int[str.length()];
        for (int k = 0; k < str.length(); k++) {
            result[k] = str.charAt(k) - '0';
        }
        return result;
    }

    public static void main(String[] args) {

    }
}