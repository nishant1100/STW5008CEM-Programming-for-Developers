public class EngineConstructionManager {
    public static int minTimeToBuild(int[] engines, int splitCost) {
        int totalTime = 0;
        // Iterate through each engine construction time
        for (int engineTime : engines) {
            // Check if splitting the construction time reduces the total time
            if (splitCost + engineTime / 2 < engineTime) {
                // If splitting reduces time, add split cost to total time
                totalTime += splitCost;
            } else {
                // Otherwise, add full construction time to total time
                totalTime += engineTime;
            }
        }
        // Return the total time required to build all engines
        return totalTime;
    }

    // Main method for testing
    public static void main(String[] args) {
        int[] engines = {1, 2, 3}; 
        int splitCost = 1; 

        System.out.println("Minimum time to build engines: " + minTimeToBuild(engines, splitCost));
    }
}
