import java.util.ArrayList;
import java.util.List;

public class SecretSharing {

    public static List<Integer> whoKnowsSecret(int n, int[][] intervals, int firstPerson) {
        boolean[] knowsSecret = new boolean[n]; // Initialize array to track who knows the secret

        for (int[] interval : intervals) {
            for (int i = interval[0]; i <= interval[1]; i++) {
                knowsSecret[i] = true; // Mark individuals who receive the secret during each interval
            }
        }

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (knowsSecret[i] || i == firstPerson) {
                result.add(i); // Add individuals who know the secret or person 0
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int n = 5;
        int[][] intervals = {{0, 2}, {1, 3}, {2, 4}};
        int firstPerson = 0;
        List<Integer> whoKnows = whoKnowsSecret(n, intervals, firstPerson);
        System.out.println("Individuals who know the secret: " + whoKnows);
    }
}
