package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HistogramWriter {

    /*
     * Writes a histogram "community size -> number of communities"
     * to a text file.
     *
     * We use TreeMap so the sizes appear sorted in increasing order.
     */
    public static void writeHistogram(Map<Integer, Integer> histogram, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# size count");
            writer.newLine();

            TreeMap<Integer, Integer> sorted = new TreeMap<>(histogram);
            for (Map.Entry<Integer, Integer> entry : sorted.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.newLine();
            }
        }
    }
}