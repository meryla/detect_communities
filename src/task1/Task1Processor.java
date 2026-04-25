package task1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
 * This class handles publications as they come (one by one),
 * and groups authors together using Union-Find.
 */
public class Task1Processor {

    // Union-Find to keep track of which authors are connected (same group)
    private final UnionFind uf;

    // after how many publications we want to print stats
    private final int printEvery;

    // keeps track of how many publications we've seen so far
    private long publicationCount;
    // constructor 
    public Task1Processor(int printEvery) {
        this.uf = new UnionFind(); // create the union-find structure
        this.printEvery = printEvery;
        this.publicationCount = 0;
    }

    /*
     * Cleans up the list of authors for one paper
     * - removes null values
     * - removes empty names
     * - removes duplicates
     */
    private List<String> cleanAuthors(List<String> authors) {

        // if list is null, just return empty list
        if (authors == null) {
            return new ArrayList<>();
        }

        // use LinkedHashSet to remove duplicates but keep order
        Set<String> cleaned = new LinkedHashSet<>();

        for (String author : authors) {

            // skip if it's null
            if (author == null) {
                continue;
            }

            // trim spaces around the name
            String trimmed = author.trim();

            // only keep it if it's not empty
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }

        // turn it back into a list
        return new ArrayList<>(cleaned);
    }

    /*
     * Processes one publication.
     * Basically makes sure all its authors end up in the same group.
     */
    public void processPublication(List<String> rawAuthors) {

        publicationCount++; // increase total count

        // first clean the authors list
        List<String> authors = cleanAuthors(rawAuthors);

        // if no authors, nothing to do
        if (authors.isEmpty()) {
            printIntermediateIfNeeded();
            return;
        }

        // make sure each author exists in union-find
        for (String author : authors) {
            uf.addIfAbsent(author);
        }

        /*
         * Connect all authors together.
         * We just take the first one and link everyone else to it.
         */
        String firstAuthor = authors.get(0);

        for (int i = 1; i < authors.size(); i++) {
            uf.union(firstAuthor, authors.get(i));
        }

        // print stats if needed
        printIntermediateIfNeeded();
    }

    /*
     * we prints some info every 'printEvery' publications:
     * - total number of groups
     * - sizes of the biggest 10 groups
     */
    private void printIntermediateIfNeeded() {

        if (printEvery > 0 && publicationCount % printEvery == 0) {

            // get all community sizes
            List<Integer> sizes = new ArrayList<>(uf.getRootSizes().values());

            // sort from biggest to smallest
            sizes.sort(Collections.reverseOrder());

            System.out.println("After " + publicationCount + " publications:");
            System.out.println("Number of communities: " + uf.getComponentCount());
            System.out.println("Top 10 community sizes:");

            // print up to 10 (or less if not enough)
            int limit = Math.min(10, sizes.size());
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + (i + 1) + ". " + sizes.get(i));
            }

            System.out.println();
        }
    }

    /*
     * Builds a histogram where:
     * key = size of a group
     * value = how many groups have that size
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
     * Writes the histogram to a file.
     */
    public void writeHistogram(String outputPath) throws IOException {

        Map<Integer, Integer> histogram = buildHistogram();

        // sort the sizes
        List<Integer> sizes = new ArrayList<>(histogram.keySet());
        Collections.sort(sizes);

        // write everything to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            writer.write("# size count");
            writer.newLine();

            for (Integer size : sizes) {
                writer.write(size + " " + histogram.get(size));
                writer.newLine();
            }
        }
    }

    // returns how many groups we have in total
    public int getCommunityCount() {
        return uf.getComponentCount();
    }
}