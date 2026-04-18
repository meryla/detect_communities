package task2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline step 2 of Task 2.
 *
 * Finds all Strongly Connected Components (SCCs) of a {@link DirectedGraph}
 * using Kosaraju's two-pass algorithm.
 *
 * --- How Kosaraju works ---
 *
 * Pass 1 – DFS on the original graph.
 *   Visit every unvisited vertex.  When a vertex has no more unvisited
 *   neighbours (i.e. it "finishes"), push it onto a stack.
 *   Result: the stack's top has the vertex with the latest finish time.
 *
 * Pass 2 – DFS on the REVERSE graph, in reverse finish order.
 *   Pop vertices from the stack one by one.  For each unvisited vertex,
 *   do a DFS on the reverse graph.  All vertices reachable in this DFS
 *   form one SCC.
 *
 * Both passes are implemented iteratively to avoid stack-overflow on
 * large graphs (DBLP has millions of authors).
 *
 * Time complexity: O(V + E)
 */
public class KosarajuSCC {

    /**
     * Computes all SCCs of the given graph.
     *
     * @param graph the filtered directed graph
     * @return list of SCCs; each SCC is a list of author names.
     *         The list is sorted by SCC size in descending order.
     */
    public static List<List<String>> compute(DirectedGraph graph) {

        // ------------------------------------------------------------------ //
        // Pass 1: iterative DFS on original graph → build finish-order stack  //
        // ------------------------------------------------------------------ //
        Deque<String> finishStack = new ArrayDeque<>();
        Map<String, Boolean> visited = new HashMap<>();

        for (String vertex : graph.getVertices()) {
            if (!visited.containsKey(vertex)) {
                dfsPass1(graph, vertex, visited, finishStack);
            }
        }

        // ------------------------------------------------------------------ //
        // Pass 2: iterative DFS on reverse graph → collect SCCs               //
        // ------------------------------------------------------------------ //
        List<List<String>> sccs = new ArrayList<>();
        visited.clear();

        while (!finishStack.isEmpty()) {
            String start = finishStack.pop();
            if (!visited.containsKey(start)) {
                List<String> component = new ArrayList<>();
                dfsPass2(graph, start, visited, component);
                sccs.add(component);
            }
        }

        // Sort SCCs by size descending so sccs.get(0) is the largest.
        sccs.sort((c1, c2) -> c2.size() - c1.size());

        return sccs;
    }

    // -----------------------------------------------------------------------
    // Pass 1: iterative post-order DFS on the original graph
    // -----------------------------------------------------------------------

    /*
     * We simulate the call stack explicitly with a Deque.
     * Each frame stores the vertex and an iterator over its neighbours.
     *
     * When a vertex's iterator is exhausted (no more unvisited neighbours),
     * that vertex is "finished": we push it onto finishStack.
     */
    private static void dfsPass1(DirectedGraph graph,
                                  String start,
                                  Map<String, Boolean> visited,
                                  Deque<String> finishStack) {

        // callStack holds (vertex, iterator-over-its-neighbours) pairs.
        Deque<Map.Entry<String, java.util.Iterator<String>>> callStack = new ArrayDeque<>();

        visited.put(start, true);
        callStack.push(Map.entry(start, graph.getNeighbours(start).iterator()));

        while (!callStack.isEmpty()) {
            Map.Entry<String, java.util.Iterator<String>> frame = callStack.peek();
            java.util.Iterator<String> it = frame.getValue();

            if (it.hasNext()) {
                String neighbour = it.next();
                if (!visited.containsKey(neighbour)) {
                    visited.put(neighbour, true);
                    callStack.push(
                            Map.entry(neighbour, graph.getNeighbours(neighbour).iterator()));
                }
            } else {
                // All neighbours explored → this vertex is finished.
                finishStack.push(frame.getKey());
                callStack.pop();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Pass 2: iterative DFS on the reverse graph, collecting one SCC
    // -----------------------------------------------------------------------

    /*
     * Standard iterative DFS.  All nodes reachable from `start` in the
     * reverse graph (that have not been visited yet) belong to the same SCC.
     */
    private static void dfsPass2(DirectedGraph graph,
                                  String start,
                                  Map<String, Boolean> visited,
                                  List<String> component) {

        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);
        visited.put(start, true);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            component.add(current);

            for (String neighbour : graph.getReverseNeighbours(current)) {
                if (!visited.containsKey(neighbour)) {
                    visited.put(neighbour, true);
                    stack.push(neighbour);
                }
            }
        }
    }
}
