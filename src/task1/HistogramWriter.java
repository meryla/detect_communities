package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HistogramWriter {

    /*
     * We created this method to write a histogram of
     * "community size -> number of communities" into a text file.
     *
     * We use a TreeMap so that the keys (sizes) are automatically
     * sorted in increasing order before writing them.
     */
    public static void writeHistogram(Map<Integer, Integer> histogram, String outputPath) throws IOException {

        // We use a BufferedWriter wrapped around a FileWriter to efficiently
        // write text to the output file. The try-with-resources ensures
        // the writer is automatically closed after we're done.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            // First, we write a header line to make the file easier to understand.
            writer.write("# size count");
            writer.newLine();

            // We convert the input histogram into a TreeMap so that
            // the entries are sorted by key (community size).
            TreeMap<Integer, Integer> sorted = new TreeMap<>(histogram);

            // We iterate through each entry of the sorted map.
            for (Map.Entry<Integer, Integer> entry : sorted.entrySet()) {

                // For each entry, we write "size count" on a single line.
                writer.write(entry.getKey() + " " + entry.getValue());

                // Then we move to the next line for the next entry.
                writer.newLine();
            }
        }
    }
}