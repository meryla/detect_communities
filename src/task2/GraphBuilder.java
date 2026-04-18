package task2;

import java.util.Map;

/**
 * Offline step 1 of Task 2.
 *
 * Reads the raw pair counts produced by {@link PairCounter} and builds a
 * {@link DirectedGraph} that contains only edges whose count >= THRESHOLD.
 */
public class GraphBuilder {

    /**
     * Builds and returns the filtered directed graph.
     *
     * For every pair (A → B) with count >= {@link PairCounter#THRESHOLD},
     * an edge A → B is added to the graph.
     *
     * @param pairCounts raw counts from PairCounter (first author → co-author → count)
     * @return the filtered directed graph
     */
    public static DirectedGraph build(Map<String, Map<String, Integer>> pairCounts) {
        DirectedGraph graph = new DirectedGraph();

        for (Map.Entry<String, Map<String, Integer>> outerEntry : pairCounts.entrySet()) {
            String firstAuthor = outerEntry.getKey();

            for (Map.Entry<String, Integer> innerEntry : outerEntry.getValue().entrySet()) {
                String coAuthor = innerEntry.getKey();
                int count       = innerEntry.getValue();

                if (count >= PairCounter.THRESHOLD) {
                    // This edge passed the threshold: add it to the graph.
                    graph.addEdge(firstAuthor, coAuthor);
                }
            }
        }

        return graph;
    }
}
