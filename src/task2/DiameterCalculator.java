package task2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Offline step 3 of Task 2.
 *
 * Computes the diameter of a single SCC in the filtered directed graph.
 *
 * Definition (from the project spec):
 *   diameter(C) = max_{u,v in C} d(u, v)
 *
 * where d(u, v) is the length (number of edges) of the shortest directed
 * path from u to v that stays entirely inside C.
 *
 * Algorithm:
 *   For every vertex u in the component, run a BFS restricted to the
 *   induced subgraph of C.  Track the maximum distance reached.
 *
 * Time complexity: O(|C| * (|C| + |E_C|))
 * For the top-10 communities this is acceptable; avoid running this on
 * very large SCCs if performance is critical.
 */
public class DiameterCalculator {

    /**
     * Computes the diameter of a community (SCC).
     *
     * @param component the list of author names forming the SCC
     * @param graph     the full filtered directed graph
     * @return the diameter (0 if the component has only one node)
     */
    public static int compute(List<String> component, DirectedGraph graph) {
        if (component.size() == 1) {
            return 0;
        }

        // Build a set for O(1) membership test: is a vertex in this SCC?
        Set<String> memberSet = new HashSet<>(component);

        int diameter = 0;

        for (String source : component) {
            int maxFromSource = bfsMaxDistance(source, memberSet, graph);
            if (maxFromSource > diameter) {
                diameter = maxFromSource;
            }
        }

        return diameter;
    }

    /**
     * BFS from {@code source}, restricted to vertices in {@code memberSet}.
     *
     * @return the maximum shortest-path distance from source to any
     *         reachable vertex in the component.
     */
    private static int bfsMaxDistance(String source,
                                       Set<String> memberSet,
                                       DirectedGraph graph) {

        // dist maps each visited vertex to its BFS distance from source.
        Map<String, Integer> dist = new HashMap<>();
        dist.put(source, 0);

        Deque<String> queue = new ArrayDeque<>();
        queue.add(source);

        int maxDist = 0;

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDist = dist.get(current);

            for (String neighbour : graph.getNeighbours(current)) {
                // Only follow edges that stay inside the SCC.
                if (memberSet.contains(neighbour) && !dist.containsKey(neighbour)) {
                    int d = currentDist + 1;
                    dist.put(neighbour, d);
                    if (d > maxDist) {
                        maxDist = d;
                    }
                    queue.add(neighbour);
                }
            }
        }

        return maxDist;
    }
}
