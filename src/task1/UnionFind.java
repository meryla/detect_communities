package task1;

import java.util.HashMap;
import java.util.Map;

/*
 * This class is a Union-Find (Disjoint Set) implementation.
 * It helps group authors into communities.
 */
public class UnionFind {

    // parent[x] tells who the parent of x is
    // if parent[x] == x, then x is the root of a group
    private final Map<String, String> parent;

    // size[root] stores how big each community is
    // only makes sense for root nodes
    private final Map<String, Integer> size;

    // keeps track of how many separate groups we have
    private int componentCount;

    public UnionFind() {
        this.parent = new HashMap<>();
        this.size = new HashMap<>();
        this.componentCount = 0;
    }

    /*
     * Adds a new author if they are not already in the structure.
     * At the start, each author is in their own group.
     */
    public void addIfAbsent(String author) {
        if (!parent.containsKey(author)) {
            parent.put(author, author); // parent is itself at first
            size.put(author, 1);        // group size starts at 1
            componentCount++;           // new group added
        }
    }

    /*
     * Finds the root (representative) of the author's group.
     *
     * Uses path compression:
     * makes future lookups faster by shortening the tree.
     */
    public String find(String author) {
        String p = parent.get(author);

        // if it's not the root, go up and compress the path
        if (!p.equals(author)) {
            parent.put(author, find(p));
        }

        return parent.get(author);
    }

    /*
     * Joins the groups of a and b.
     *
     * Uses union by size:
     * smaller group gets attached under the bigger one.
     */
    public void union(String a, String b) {

        // make sure both authors exist
        addIfAbsent(a);
        addIfAbsent(b);

        String rootA = find(a);
        String rootB = find(b);

        // if they are already in the same group, do nothing
        if (rootA.equals(rootB)) {
            return;
        }

        int sizeA = size.get(rootA);
        int sizeB = size.get(rootB);

        // make sure rootA is always the bigger group
        if (sizeA < sizeB) {

            // swap roots
            String tmpRoot = rootA;
            rootA = rootB;
            rootB = tmpRoot;

            // swap sizes too
            int tmpSize = sizeA;
            sizeA = sizeB;
            sizeB = tmpSize;
        }

        // attach smaller rootB under bigger rootA
        parent.put(rootB, rootA);

        // update the size of the new combined group
        size.put(rootA, sizeA + sizeB);

        // rootB is no longer a root, so remove it from size map
        size.remove(rootB);

        // total number of groups goes down by 1
        componentCount--;
    }

    /*
     * Returns how many separate groups exist right now
     */
    public int getComponentCount() {
        return componentCount;
    }

    /*
     * Returns a copy of the root = size map
     * (so external code doesn't mess with internal data)
     */
    public Map<String, Integer> getRootSizes() {
        return new HashMap<>(size);
    }
}