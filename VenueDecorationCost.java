public class VenueDecorationCost {
    public static int minCost(int[][] costs) {
        if (costs == null || costs.length == 0 || costs[0].length == 0)
            return 0;
        
        int n = costs.length;
        int k = costs[0].length;
        
        // dp[i][j] represents the minimum cost to decorate venues up to index i with theme j
        int[][] dp = new int[n][k];
        
        // Initialize the first row with the costs of decorating the first venue
        for (int j = 0; j < k; j++) {
            dp[0][j] = costs[0][j];
        }
        
        // Start filling dp from the second venue onwards
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < k; j++) {
                // Find the minimum cost of decorating venue i with theme j considering the previous venue
                int minPrevCost = Integer.MAX_VALUE;
                for (int prevTheme = 0; prevTheme < k; prevTheme++) {
                    if (prevTheme != j) {
                        minPrevCost = Math.min(minPrevCost, dp[i - 1][prevTheme]);
                    }
                }
                dp[i][j] = costs[i][j] + minPrevCost;
            }
        }
        
        // Find the minimum cost from the last row
        int minCost = Integer.MAX_VALUE;
        for (int j = 0; j < k; j++) {
            minCost = Math.min(minCost, dp[n - 1][j]);
        }
        
        return minCost;
    }
    
    public static void main(String[] args) {
        int[][] costs = {{1, 3, 2}, {4, 6, 8}, {3, 1, 5}};
        System.out.println("Minimum cost: " + minCost(costs));
    }
}


