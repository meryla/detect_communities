package task1;

import java.util.HashMap;
import java.util.Map;

/*
 * This class implements Union-Find (Disjoint Set).
 * It is used to group authors into communities.
 */
public class UnionFind {

    // parent[x] = parent of x in the tree
    // if parent[x] == x → x is the root
    private final Map<String, String> parent;

    // size[root] = number of elements in that community
    // only valid for roots
    private final Map<String, Integer> size;

    // total number of separate communities
    private int componentCount;

    public UnionFind() {
        this.parent = new HashMap<>();
        this.size = new HashMap<>();
        this.componentCount = 0;
    }

    /*
     * Adds a new author if not already present.
     * Initially, each author is its own parent (own community).
     */
    public void addIfAbsent(String author) {
        if (!parent.containsKey(author)) {
            parent.put(author, author); // points to itself
            size.put(author, 1);        // size = 1
            componentCount++;           // new community created
        }
    }

    /*
     * Finds the root of the set containing 'author'.
     *
     * Path compression:
     * we make nodes point directly to the root to speed things up.
     */
    public String find(String author) {
        String p = parent.get(author);

        // if not root, recursively find root and compress path
        if (!p.equals(author)) {
            parent.put(author, find(p));
        }

        return parent.get(author);
    }

    /*
     * Merges the sets of a and b.
     *
     * Uses union by size:
     * smaller tree goes under the bigger one.
     */
    public void union(String a, String b) {

        // make sure both exist
        addIfAbsent(a);
        addIfAbsent(b);

        String rootA = find(a);
        String rootB = find(b);

        // if already in same set → nothing to do
        if (rootA.equals(rootB)) {
            return;
        }

        int sizeA = size.get(rootA);
        int sizeB = size.get(rootB);

        // make sure rootA is the bigger tree
        if (sizeA < sizeB) {

            // swap roots
            String tmpRoot = rootA;
            rootA = rootB;
            rootB = tmpRoot;

            // swap sizes
            int tmpSize = sizeA;
            sizeA = sizeB;
            sizeB = tmpSize;
        }

        // attach smaller rootB under larger rootA
        parent.put(rootB, rootA);

        // update size of the new root
        size.put(rootA, sizeA + sizeB);

        // rootB is no longer a root → remove its size entry
        size.remove(rootB);

        // one less community after merging
        componentCount--;
    }

    /*
     * Returns number of current communities.
     */
    public int getComponentCount() {
        return componentCount;
    }

    /*
     * Returns a copy of root → size map.
     * (we return a copy so outside code can't modify internal data)
     */
    public Map<String, Integer> getRootSizes() {
        return new HashMap<>(size);
    }
}