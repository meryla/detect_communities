package task1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Task1Processor {

    // We use UnionFind to maintain and merge communities efficiently.
    private final UnionFind uf;

    // We store how often we want to print intermediate results.
    private final int printEvery;

    // We keep track of how many publications we have processed so far.
    private long publicationCount;

    // We initialize our processor with a given print frequency.
    public Task1Processor(int printEvery) {
        this.uf = new UnionFind();
        this.printEvery = printEvery;
        this.publicationCount = 0;
    }

    /*
     * We clean the list of authors for a single publication.
     *
     * What we do here:
     * - remove null author names
     * - remove empty strings
     * - remove duplicates within the same publication
     *
     * We use LinkedHashSet because:
     * - it automatically removes duplicates
     * - it preserves the original order of authors
     */
    private List<String> cleanAuthors(List<String> authors) {
        // If the input list is null, we return an empty list to avoid issues.
        if (authors == null) {
            return new ArrayList<>();
        }

        // We use a set to handle duplicates while keeping order.
        Set<String> cleaned = new LinkedHashSet<>();

        // We iterate through each author in the list.
        for (String author : authors) {

            // We skip null values.
            if (author == null) {
                continue;
            }

            // We trim whitespace from the author name.
            String trimmed = author.trim();

            // We only keep non-empty names.
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }

        // We convert the set back into a list before returning.
        return new ArrayList<>(cleaned);
    }

    /*
     * We process a single publication in an online manner.
     *
     * The goal is that all coauthors of the same publication
     * end up in the same community.
     */
    public void processPublication(List<String> rawAuthors) {

        // We increment the number of processed publications.
        publicationCount++;

        // We clean the raw author list before processing.
        List<String> authors = cleanAuthors(rawAuthors);

        // If there are no valid authors, we do nothing but still check printing.
        if (authors.isEmpty()) {
            printIntermediateIfNeeded();
            return;
        }

        // We first ensure that every author exists in UnionFind.
        // This also handles single-author publications.
        for (String author : authors) {
            uf.addIfAbsent(author);
        }

        /*
         * To connect all authors of the same publication,
         * we pick the first author as a base and union it with the others.
         *
         * Example: [A, B, C, D]
         * We perform:
         * union(A, B), union(A, C), union(A, D)
         */
        String firstAuthor = authors.get(0);
        for (int i = 1; i < authors.size(); i++) {
            uf.union(firstAuthor, authors.get(i));
        }

        // After processing, we check if we should print intermediate results.
        printIntermediateIfNeeded();
    }

    /*
     * We print intermediate statistics about the communities.
     *
     * This allows us to observe how the communities evolve over time.
     */
    private void printIntermediateIfNeeded() {

        // We only print if printing is enabled and we reached the interval.
        if (printEvery > 0 && publicationCount % printEvery == 0) {

            // We collect all community sizes.
            List<Integer> sizes = new ArrayList<>(uf.getRootSizes().values());

            // We sort sizes in descending order.
            sizes.sort(Collections.reverseOrder());

            System.out.println("After " + publicationCount + " publications:");
            System.out.println("Number of communities: " + uf.getComponentCount());
            System.out.println("Top 10 community sizes:");

            // We print up to the 10 largest communities.
            int limit = Math.min(10, sizes.size());
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + (i + 1) + ". " + sizes.get(i));
            }

            System.out.println();
        }
    }

    /*
     * We build a histogram of community sizes.
     *
     * key   = community size
     * value = number of communities with that size
     */
    public Map<Integer, Integer> buildHistogram() {

        // We store the histogram in a map.
        Map<Integer, Integer> histogram = new HashMap<>();

        // We iterate over all community sizes.
        for (int communitySize : uf.getRootSizes().values()) {

            // We count how many times each size appears.
            histogram.put(communitySize, histogram.getOrDefault(communitySize, 0) + 1);
        }

        return histogram;
    }

    /*
     * We write the histogram to a file.
     */
    public void writeHistogram(String outputPath) throws IOException {

        // We first build the histogram.
        Map<Integer, Integer> histogram = buildHistogram();

        // We sort the sizes to write them in order.
        List<Integer> sizes = new ArrayList<>(histogram.keySet());
        Collections.sort(sizes);

        // We write the data to the file using BufferedWriter.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            // We write a header for readability.
            writer.write("# size count");
            writer.newLine();

            // We write each size and its count.
            for (Integer size : sizes) {
                writer.write(size + " " + histogram.get(size));
                writer.newLine();
            }
        }
    }

    // We expose the current number of communities.
    public int getCommunityCount() {
        return uf.getComponentCount();
    }
}