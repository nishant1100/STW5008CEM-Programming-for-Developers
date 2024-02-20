import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Colony {

    private static final int NUM_ANTS = 10;
    private static final double ALPHA = 1.0; // Pheromone importance
    private static final double BETA = 2.0; // Heuristic importance
    private static final double RHO = 0.5; // Pheromone evaporation rate
    private static final double Q = 100; // Pheromone deposit factor

    private int numCities;
    private double[][] distances;
    private double[][] pheromones;

    public Colony(double[][] distances) {
        this.numCities = distances.length;
        this.distances = distances;
        this.pheromones = new double[numCities][numCities];
        initializePheromones();
    }

    private void initializePheromones() {
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromones[i][j] = 0.1; // Initial pheromone level
            }
        }
    }

    public int[] solve(int maxIterations) {
        int[] bestTour = null;
        double bestTourLength = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int[][] antTours = new int[NUM_ANTS][numCities];

            for (int ant = 0; ant < NUM_ANTS; ant++) {
                int[] tour = constructTour();
                antTours[ant] = tour;

                double tourLength = calculateTourLength(tour);
                if (tourLength < bestTourLength) {
                    bestTourLength = tourLength;
                    bestTour = Arrays.copyOf(tour, numCities);
                }
            }

            updatePheromones(antTours);
        }

        return bestTour;
    }

    private int[] constructTour() {
        int[] tour = new int[numCities];
        boolean[] visited = new boolean[numCities];
        Random random = new Random();

        // Start from a random city
        int startCity = random.nextInt(numCities);
        tour[0] = startCity;
        visited[startCity] = true;

        // Construct the rest of the tour
        for (int i = 1; i < numCities; i++) {
            int currentCity = tour[i - 1];
            int nextCity = selectNextCity(currentCity, visited);
            tour[i] = nextCity;
            visited[nextCity] = true;
        }

        return tour;
    }

    private int selectNextCity(int currentCity, boolean[] visited) {
        Random random = new Random();
        List<Integer> candidateCities = new ArrayList<>();

        // Calculate probabilities for selecting next city
        double totalProbability = 0;
        for (int i = 0; i < numCities; i++) {
            if (!visited[i]) {
                double probability = Math.pow(pheromones[currentCity][i], ALPHA) *
                        Math.pow(1.0 / distances[currentCity][i], BETA);
                totalProbability += probability;
                candidateCities.add(i);
            }
        }

        // Choose next city based on probabilities
        double randomValue = random.nextDouble() * totalProbability;
        double cumulativeProbability = 0;
        for (int city : candidateCities) {
            double probability = Math.pow(pheromones[currentCity][city], ALPHA) *
                    Math.pow(1.0 / distances[currentCity][city], BETA);
            cumulativeProbability += probability;
            if (cumulativeProbability >= randomValue) {
                return city;
            }
        }

        // This should not happen, but return a random city as a fallback
        return candidateCities.get(random.nextInt(candidateCities.size()));
    }

    private double calculateTourLength(int[] tour) {
        double tourLength = 0;
        for (int i = 0; i < numCities; i++) {
            int city1 = tour[i];
            int city2 = tour[(i + 1) % numCities];
            tourLength += distances[city1][city2];
        }
        return tourLength;
    }

    private void updatePheromones(int[][] antTours) {
        // Evaporate pheromones
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromones[i][j] *= (1 - RHO);
            }
        }

        // Deposit pheromones
        for (int ant = 0; ant < NUM_ANTS; ant++) {
            double tourLength = calculateTourLength(antTours[ant]);
            for (int i = 0; i < numCities; i++) {
                int city1 = antTours[ant][i];
                int city2 = antTours[ant][(i + 1) % numCities];
                pheromones[city1][city2] += Q / tourLength;
                pheromones[city2][city1] += Q / tourLength; // Symmetric matrix
            }
        }
    }

    public static void main(String[] args) {
        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };
        Colony aco = new Colony(distances);
        int[] bestTour = aco.solve(1000);
        System.out.println("Best tour length: " + aco.calculateTourLength(bestTour));
        System.out.println("Best tour: " + Arrays.toString(bestTour));
    }

 {
    
}
}