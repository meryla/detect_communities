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

    private final UnionFind uf;
    private final int printEvery;
    private long publicationCount;

    public Task1Processor(int printEvery) {
        this.uf = new UnionFind();
        this.printEvery = printEvery;
        this.publicationCount = 0;
    }

    /*
     * Cleans the author list of one publication.
     *
     * Why this method exists:
     * - remove null author names
     * - remove empty strings
     * - remove duplicates inside the same publication
     *
     * LinkedHashSet is used because:
     * - it removes duplicates
     * - it keeps the original order
     */
    private List<String> cleanAuthors(List<String> authors) {
        if (authors == null) {
            return new ArrayList<>();
        }

        Set<String> cleaned = new LinkedHashSet<>();

        for (String author : authors) {
            if (author == null) {
                continue;
            }

            String trimmed = author.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }

        return new ArrayList<>(cleaned);
    }

    /*
     * Processes one publication online.
     *
     * All coauthors of a publication must end up in the same community.
     */
    public void processPublication(List<String> rawAuthors) {
        publicationCount++;

        List<String> authors = cleanAuthors(rawAuthors);

        // No author: nothing to do.
        if (authors.isEmpty()) {
            printIntermediateIfNeeded();
            return;
        }

        // Add all authors first, so even a single-author publication is represented.
        for (String author : authors) {
            uf.addIfAbsent(author);
        }

        /*
         * To merge all authors of one publication into one community,
         * it is enough to choose one author as a base and union it with all others.
         *
         * Example: [A, B, C, D]
         * union(A, B), union(A, C), union(A, D)
         */
        String firstAuthor = authors.get(0);
        for (int i = 1; i < authors.size(); i++) {
            uf.union(firstAuthor, authors.get(i));
        }

        printIntermediateIfNeeded();
    }

    /*
     * Prints the number of communities and the sizes of the 10 largest communities.
     * This helps show the online evolution of the result.
     */
    private void printIntermediateIfNeeded() {
        if (printEvery > 0 && publicationCount % printEvery == 0) {
            List<Integer> sizes = new ArrayList<>(uf.getRootSizes().values());
            sizes.sort(Collections.reverseOrder());

            System.out.println("After " + publicationCount + " publications:");
            System.out.println("Number of communities: " + uf.getComponentCount());
            System.out.println("Top 10 community sizes:");

            int limit = Math.min(10, sizes.size());
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + (i + 1) + ". " + sizes.get(i));
            }

            System.out.println();
        }
    }

    /*
     * Builds the histogram:
     * key   = community size
     * value = number of communities having that size
     */
    public Map<Integer, Integer> buildHistogram() {
        Map<Integer, Integer> histogram = new HashMap<>();

        for (int communitySize : uf.getRootSizes().values()) {
            histogram.put(communitySize, histogram.getOrDefault(communitySize, 0) + 1);
        }

        return histogram;
    }

    /*
     * Writes the histogram to a file.
     */
    public void writeHistogram(String outputPath) throws IOException {
        Map<Integer, Integer> histogram = buildHistogram();

        List<Integer> sizes = new ArrayList<>(histogram.keySet());
        Collections.sort(sizes);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# size count");
            writer.newLine();

            for (Integer size : sizes) {
                writer.write(size + " " + histogram.get(size));
                writer.newLine();
            }
        }
    }

    public int getCommunityCount() {
        return uf.getComponentCount();
    }
}