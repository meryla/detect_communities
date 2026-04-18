package task2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple directed graph backed by adjacency lists.
 *
 * Nodes are author names (String).
 * Edges are stored in two directions so that both the graph and its
 * reverse are available without rebuilding (needed by Kosaraju's algorithm).
 */
public class DirectedGraph {

    /*
     * adjList.get(u) = list of vertices v such that u → v is an edge.
     */
    private final Map<String, List<String>> adjList;

    /*
     * reverseAdj.get(v) = list of vertices u such that u → v is an edge.
     * This is the transpose graph, needed for Kosaraju pass 2.
     */
    private final Map<String, List<String>> reverseAdj;

    public DirectedGraph() {
        this.adjList    = new HashMap<>();
        this.reverseAdj = new HashMap<>();
    }

    /**
     * Ensures a vertex exists in the graph (with empty neighbour lists).
     */
    public void addVertex(String v) {
        adjList.putIfAbsent(v, new ArrayList<>());
        reverseAdj.putIfAbsent(v, new ArrayList<>());
    }

    /**
     * Adds a directed edge from → to.
     * Both vertices are created automatically if not already present.
     */
    public void addEdge(String from, String to) {
        addVertex(from);
        addVertex(to);
        adjList.get(from).add(to);
        reverseAdj.get(to).add(from);
    }

    /** @return all vertices of this graph */
    public Set<String> getVertices() {
        return adjList.keySet();
    }

    /** @return neighbours of v in the original graph (u such that v → u) */
    public List<String> getNeighbours(String v) {
        return adjList.getOrDefault(v, Collections.emptyList());
    }

    /** @return neighbours of v in the reverse graph (u such that u → v) */
    public List<String> getReverseNeighbours(String v) {
        return reverseAdj.getOrDefault(v, Collections.emptyList());
    }

    /** @return total number of vertices */
    public int vertexCount() {
        return adjList.size();
    }

    /** @return total number of directed edges */
    public int edgeCount() {
        int total = 0;
        for (List<String> neighbours : adjList.values()) {
            total += neighbours.size();
        }
        return total;
    }
}
