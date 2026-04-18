package task2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Orchestrator for Task 2.
 *
 * Call order:
 *   1. processPublication(authors)  — for every publication, online
 *   2. runOfflineAnalysis()         — once, after streaming ends
 *   3. writeHistogram(path)         — output SCC size histogram
 *   4. writeTop10(path)             — output top-10 SCC details
 */
public class Task2Processor {

    // Online component: counts first-author → co-author pairs.
    private final PairCounter pairCounter;

    // Set after runOfflineAnalysis() is called.
    private DirectedGraph graph;
    private List<List<String>> sccs;

    public Task2Processor() {
        this.pairCounter = new PairCounter();
    }

    // -----------------------------------------------------------------------
    // Online phase
    // -----------------------------------------------------------------------

    /**
     * Must be called for every publication during the streaming phase.
     * Only publications with at least 2 authors contribute pair counts.
     *
     * @param authors cleaned, ordered author list
     */
    public void processPublication(List<String> authors) {
        pairCounter.processPublication(authors);
    }

    // -----------------------------------------------------------------------
    // Offline phase
    // -----------------------------------------------------------------------

    /**
     * Runs the full offline analysis:
     *   1. Build the filtered directed graph (threshold >= 6).
     *   2. Find all SCCs with Kosaraju's algorithm.
     *
     * Call this once, after all publications have been processed.
     */
    public void runOfflineAnalysis() {
        // Step 1: build filtered graph from pair counts.
        graph = GraphBuilder.build(pairCounter.getPairCounts());

        System.out.println("[Task 2] Filtered graph: "
                + graph.vertexCount() + " vertices, "
                + graph.edgeCount()   + " edges (threshold >= " + PairCounter.THRESHOLD + ")");

        // Step 2: find SCCs.
        sccs = KosarajuSCC.compute(graph);

        System.out.println("[Task 2] SCCs found: " + sccs.size());
    }

    // -----------------------------------------------------------------------
    // Output
    // -----------------------------------------------------------------------

    /**
     * @return all SCCs sorted by size descending (largest first).
     */
    public List<List<String>> getSccs() {
        return sccs;
    }

    /**
     * Writes the SCC size histogram to a file.
     * Format:  size<space>count  (one line per distinct size, sorted ascending).
     */
    public void writeHistogram(String outputPath) throws IOException {
        // Build histogram: size → number of SCCs of that size.
        TreeMap<Integer, Integer> histogram = new TreeMap<>();
        for (List<String> scc : sccs) {
            histogram.merge(scc.size(), 1, Integer::sum);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# size count");
            writer.newLine();
            for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.newLine();
            }
        }
    }

    /**
     * Writes the top-10 largest SCCs to a file.
     * For each SCC: its rank, size, diameter, and the list of member names.
     */
    public void writeTop10(String outputPath) throws IOException {
        int top = Math.min(10, sccs.size());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# Task 2 – Top 10 largest communities (SCCs)");
            writer.newLine();

            for (int i = 0; i < top; i++) {
                List<String> scc = sccs.get(i);

                // Diameter: BFS from every node in the induced subgraph.
                int diameter = DiameterCalculator.compute(scc, graph);

                writer.newLine();
                writer.write("## Community " + (i + 1));
                writer.newLine();
                writer.write("Size     : " + scc.size());
                writer.newLine();
                writer.write("Diameter : " + diameter);
                writer.newLine();
                writer.write("Members  :");
                writer.newLine();
                for (String author : scc) {
                    writer.write("  " + author);
                    writer.newLine();
                }

                System.out.printf("[Task 2] Community %d: size=%d, diameter=%d%n",
                        i + 1, scc.size(), diameter);
            }
        }
    }
}
