package utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AuthorUtils {

    /*
     * Cleans the author list of one publication.
     *
     * What it does:
     * - removes null values
     * - removes empty strings
     * - removes duplicates while preserving the original order
     *
     * LinkedHashSet is useful here because:
     * - it removes duplicates
     * - it keeps insertion order
     */
    public static List<String> cleanAuthors(List<String> authors) {
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
}