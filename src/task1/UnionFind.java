package task1;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {

    /*
     * parent.get(x) gives the parent of x in the Union-Find tree.
     * If parent.get(x).equals(x), then x is the root of its set.
     *
     * We use String because authors are identified by their names in DBLP.
     */
    private final Map<String, String> parent;

    /*
     * size.get(root) = number of authors in the community represented by root.
     * This value is only correct for roots.
     */
    private final Map<String, Integer> size;

    /*
     * Number of connected components (communities) currently known.
     */
    private int componentCount;

    public UnionFind() {
        this.parent = new HashMap<>();
        this.size = new HashMap<>();
        this.componentCount = 0;
    }

    /*
     * Adds a new author if not already present.
     * Initially, each author is alone in its own set.
     */
    public void addIfAbsent(String author) {
        if (!parent.containsKey(author)) {
            parent.put(author, author);
            size.put(author, 1);
            componentCount++;
        }
    }

    /*
     * Finds the representative (root) of the set containing author.
     *
     * Path compression:
     * after finding the root, we make nodes point more directly to it.
     * This speeds up future operations.
     */
    public String find(String author) {
        String p = parent.get(author);
        if (!p.equals(author)) {
            parent.put(author, find(p));
        }
        return parent.get(author);
    }

    /*
     * Merges the communities of a and b.
     *
     * Union by size:
     * attach the smaller tree below the larger one.
     * This helps keep trees shallow and operations fast.
     */
    public void union(String a, String b) {
        addIfAbsent(a);
        addIfAbsent(b);

        String rootA = find(a);
        String rootB = find(b);

        // Already in the same community: nothing to do.
        if (rootA.equals(rootB)) {
            return;
        }

        int sizeA = size.get(rootA);
        int sizeB = size.get(rootB);

        // Ensure rootA is the larger root.
        if (sizeA < sizeB) {
            String tmpRoot = rootA;
            rootA = rootB;
            rootB = tmpRoot;

            int tmpSize = sizeA;
            sizeA = sizeB;
            sizeB = tmpSize;
        }

        // Attach smaller rootB under larger rootA.
        parent.put(rootB, rootA);
        size.put(rootA, sizeA + sizeB);

        // rootB is no longer a root, so its size is not needed anymore.
        size.remove(rootB);

        componentCount--;
    }

    /*
     * Returns the current number of communities.
     */
    public int getComponentCount() {
        return componentCount;
    }

    /*
     * Returns a map root -> size for all current communities.
     * We return a copy to avoid exposing internal structures.
     */
    public Map<String, Integer> getRootSizes() {
        return new HashMap<>(size);
    }
}