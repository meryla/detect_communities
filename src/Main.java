import task1.Task1Processor;
import task2.Task2Processor;
import utils.AuthorUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for the DBLP community detection project.
 * INFO-F-203 – Algorithmique 2, 2025/2026.
 *
 * Usage:
 *   java -Xmx4g Main <dblp-xml.gz> <dblp.dtd> [--limit=N]
 *
 * Output files (written to the output/ directory):
 *   output/task1_histogram.txt  – community size histogram  (Task 1)
 *   output/task2_histogram.txt  – SCC size histogram        (Task 2)
 *   output/task2_top10.txt      – top-10 SCC details        (Task 2)
 */
public class Main {

    // Print intermediate Task 1 state every N publications.
    private static final int PRINT_EVERY = 100_000;

    // Output file paths.
    private static final String TASK1_HISTOGRAM = "output/task1_histogram.txt";
    private static final String TASK2_HISTOGRAM = "output/task2_histogram.txt";
    private static final String TASK2_TOP10     = "output/task2_top10.txt";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -Xmx4g Main <dblp-xml.gz> <dblp.dtd> [--limit=N]");
            System.err.println("Example: java -Xmx4g Main dblp-2026-01-01.xml.gz data/dblp.dtd");
            return;
        }

        String xmlPath = args[0];
        String dtdPath = args[1];

        // Optional publication limit (useful for quick tests).
        int limit = Integer.MAX_VALUE;
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--limit=")) {
                limit = Integer.parseInt(args[i].substring("--limit=".length()));
            }
        }

        // Disable XML entity expansion limits (safe for local DBLP DTD).
        System.setProperty("jdk.xml.entityExpansionLimit",        "0");
        System.setProperty("jdk.xml.totalEntitySizeLimit",        "0");
        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit",   "0");
        System.setProperty("jdk.xml.maxParameterEntitySizeLimit", "0");

        // Initialise both task processors.
        Task1Processor task1 = new Task1Processor(PRINT_EVERY);
        Task2Processor task2 = new Task2Processor();

        System.out.println("=================================================");
        System.out.println(" DBLP Community Detection — INFO-F-203 2025/2026");
        System.out.println("=================================================");
        System.out.println("XML  : " + xmlPath);
        System.out.println("DTD  : " + dtdPath);
        if (limit != Integer.MAX_VALUE) System.out.println("Limit: " + limit);
        System.out.println();

        // ------------------------------------------------------------------ //
        //  Streaming / online phase
        // ------------------------------------------------------------------ //
        long pubCount = 0;
        long startTime = System.currentTimeMillis();

        try (DblpPublicationGenerator generator =
                     new DblpPublicationGenerator(Path.of(xmlPath), Path.of(dtdPath), 256)) {

            Optional<DblpPublicationGenerator.Publication> opt;
            while (pubCount < limit && (opt = generator.nextPublication()).isPresent()) {

                pubCount++;
                List<String> rawAuthors = opt.get().authors;

                // Clean once; share the cleaned list between both tasks.
                List<String> authors = AuthorUtils.cleanAuthors(rawAuthors);

                // Task 1: union-find (works for any number of authors >= 1).
                task1.processPublication(authors);

                // Task 2: pair counting (only meaningful with >= 2 authors;
                //         Task2Processor handles the size guard internally).
                task2.processPublication(authors);
            }

        } catch (Exception e) {
            System.err.println("Error during parsing: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        long parsingMs = System.currentTimeMillis() - startTime;
        System.out.printf("%nParsing done: %,d publications in %.1f s%n",
                pubCount, parsingMs / 1000.0);

        // ------------------------------------------------------------------ //
        //  Final Task 1 output
        // ------------------------------------------------------------------ //
        System.out.println("\n--- Task 1: Final results ---");
        System.out.println("Communities: " + task1.getCommunityCount());

        try {
            task1.writeHistogram(TASK1_HISTOGRAM);
            System.out.println("Histogram written to: " + TASK1_HISTOGRAM);
        } catch (Exception e) {
            System.err.println("Could not write Task 1 histogram: " + e.getMessage());
        }

        // ------------------------------------------------------------------ //
        //  Offline phase: Task 2
        // ------------------------------------------------------------------ //
        System.out.println("\n--- Task 2: Offline analysis ---");
        long t2Start = System.currentTimeMillis();

        task2.runOfflineAnalysis();

        try {
            task2.writeHistogram(TASK2_HISTOGRAM);
            System.out.println("Histogram written to: " + TASK2_HISTOGRAM);

            task2.writeTop10(TASK2_TOP10);
            System.out.println("Top-10 details written to: " + TASK2_TOP10);
        } catch (Exception e) {
            System.err.println("Could not write Task 2 output: " + e.getMessage());
        }

        long t2Ms = System.currentTimeMillis() - t2Start;
        System.out.printf("Task 2 offline analysis: %.1f s%n", t2Ms / 1000.0);

        long totalMs = System.currentTimeMillis() - startTime;
        System.out.printf("%nTotal time: %.1f s%n", totalMs / 1000.0);
        System.out.println("Done.");
    }
}