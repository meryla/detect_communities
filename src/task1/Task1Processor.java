package task1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
 * This class processes publications one by one (online),
 * grouping authors into communities using Union-Find.
 */
public class Task1Processor {

    // Union-Find structure to manage connected components (communities)
    private final UnionFind uf;

    // How often we print intermediate results
    private final int printEvery;

    // Counts how many publications we processed so far
    private long publicationCount;

    public Task1Processor(int printEvery) {
        this.uf = new UnionFind(); // initialize union-find
        this.printEvery = printEvery;
        this.publicationCount = 0;
    }

    /*
     * Cleans the author list of one publication.
     * - removes nulls
     * - removes empty strings
     * - removes duplicates
     */
    private List<String> cleanAuthors(List<String> authors) {

        // if input is null, just return empty list
        if (authors == null) {
            return new ArrayList<>();
        }

        // LinkedHashSet removes duplicates and keeps order
        Set<String> cleaned = new LinkedHashSet<>();

        for (String author : authors) {

            // skip null entries
            if (author == null) {
                continue;
            }

            // remove extra spaces
            String trimmed = author.trim();

            // ignore empty names
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }

        // convert back to list
        return new ArrayList<>(cleaned);
    }

    /*
     * Processes a single publication.
     * All authors of the publication should end up in the same community.
     */
    public void processPublication(List<String> rawAuthors) {

        publicationCount++; // increment counter

        // clean the author list first
        List<String> authors = cleanAuthors(rawAuthors);

        // if no authors, nothing to do
        if (authors.isEmpty()) {
            printIntermediateIfNeeded();
            return;
        }

        // ensure every author exists in union-find
        for (String author : authors) {
            uf.addIfAbsent(author);
        }

        /*
         * Merge all authors into one group.
         * We take the first author and connect everyone else to it.
         */
        String firstAuthor = authors.get(0);

        for (int i = 1; i < authors.size(); i++) {
            uf.union(firstAuthor, authors.get(i));
        }

        // maybe print intermediate results
        printIntermediateIfNeeded();
    }

    /*
     * Prints stats every 'printEvery' publications:
     * - number of communities
     * - sizes of top 10 largest communities
     */
    private void printIntermediateIfNeeded() {

        if (printEvery > 0 && publicationCount % printEvery == 0) {

            // get sizes of all communities
            List<Integer> sizes = new ArrayList<>(uf.getRootSizes().values());

            // sort descending
            sizes.sort(Collections.reverseOrder());

            System.out.println("After " + publicationCount + " publications:");
            System.out.println("Number of communities: " + uf.getComponentCount());
            System.out.println("Top 10 community sizes:");

            // print top 10 (or less if not enough)
            int limit = Math.min(10, sizes.size());
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + (i + 1) + ". " + sizes.get(i));
            }

            System.out.println();
        }
    }

    /*
     * Builds a histogram:
     * key = community size
     * value = how many communities have that size
     */
    public Map<Integer, Integer> buildHistogram() {

        Map<Integer, Integer> histogram = new HashMap<>();

        for (int communitySize : uf.getRootSizes().values()) {

            // increase count for this size
            histogram.put(
                communitySize,
                histogram.getOrDefault(communitySize, 0) + 1
            );
        }

        return histogram;
    }

    /*
     * Writes the histogram into a file.
     */
    public void writeHistogram(String outputPath) throws IOException {

        Map<Integer, Integer> histogram = buildHistogram();

        // sort sizes
        List<Integer> sizes = new ArrayList<>(histogram.keySet());
        Collections.sort(sizes);

        // write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            writer.write("# size count");
            writer.newLine();

            for (Integer size : sizes) {
                writer.write(size + " " + histogram.get(size));
                writer.newLine();
            }
        }
    }

    // returns total number of communities
    public int getCommunityCount() {
        return uf.getComponentCount();
    }
}