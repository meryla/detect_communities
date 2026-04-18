package task2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Online component of Task 2.
 *
 * For each publication [A, B, C, ...], the first author A gets directed
 * edges toward every other co-author:  A → B,  A → C, ...
 *
 * We count how many times each ordered pair (A → B) appears across all
 * publications.  Only pairs with count >= THRESHOLD will later become
 * edges in the filtered directed graph.
 *
 * This class is the only part of Task 2 that must run online (during
 * the streaming phase).  Everything else (graph construction, SCC,
 * diameter) is done offline once parsing is complete.
 */
public class PairCounter {

    // An edge A → B is kept only when its count reaches this value.
    public static final int THRESHOLD = 6;

    /*
     * pairCount.get(A).get(B) = number of publications where A was first
     * author and B was another co-author.
     *
     * Outer key = first author A
     * Inner key = co-author B
     * Value     = count
     */
    private final Map<String, Map<String, Integer>> pairCount;

    public PairCounter() {
        this.pairCount = new HashMap<>();
    }

    /**
     * Process one publication online.
     *
     * Only publications with at least 2 authors produce pair counts.
     * Duplicate co-author names within a single publication are counted
     * only once (rare in DBLP but explicitly handled).
     *
     * @param authors cleaned, ordered author list (authors.get(0) is first author)
     */
    public void processPublication(List<String> authors) {
        if (authors == null || authors.size() < 2) {
            return;
        }

        String firstAuthor = authors.get(0);

        // Use a local set to avoid double-counting duplicates in the same publication.
        Set<String> seenCoAuthors = new HashSet<>();
        seenCoAuthors.add(firstAuthor); // prevent self-loop A → A

        Map<String, Integer> firstAuthorCounts =
                pairCount.computeIfAbsent(firstAuthor, k -> new HashMap<>());

        for (int i = 1; i < authors.size(); i++) {
            String coAuthor = authors.get(i);
            if (seenCoAuthors.contains(coAuthor)) {
                continue; // duplicate in this publication, skip
            }
            seenCoAuthors.add(coAuthor);
            firstAuthorCounts.merge(coAuthor, 1, Integer::sum);
        }
    }

    /**
     * Returns the raw pair counts map.
     * Called once by {@link GraphBuilder} after the streaming phase ends.
     */
    public Map<String, Map<String, Integer>> getPairCounts() {
        return pairCount;
    }
}
