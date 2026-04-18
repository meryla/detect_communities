package task1;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {

    /*
     * We use this map to store the parent of each element (author).
     * If parent.get(x).equals(x), then x is the root of its set.
     *
     * We use String because authors are identified by their names.
     */
    private final Map<String, String> parent;

    /*
     * We store the size of each community using this map.
     * The size is only valid for root nodes.
     */
    private final Map<String, Integer> size;

    /*
     * We keep track of how many separate communities (connected components) exist.
     */
    private int componentCount;

    // We initialize our data structures and start with zero communities.
    public UnionFind() {
        this.parent = new HashMap<>();
        this.size = new HashMap<>();
        this.componentCount = 0;
    }

    /*
     * We add a new author only if they are not already present.
     * Initially, each author is its own parent and forms a community of size 1.
     */
    public void addIfAbsent(String author) {
        if (!parent.containsKey(author)) {
            parent.put(author, author);
            size.put(author, 1);
            componentCount++;
        }
    }

    /*
     * We find the root (representative) of the set containing the given author.
     *
     * We apply path compression:
     * while finding the root, we update the parent pointers so that
     * future lookups become faster.
     */
    public String find(String author) {
        String p = parent.get(author);

        // If the current node is not the root, we recursively find the root
        // and update the parent to point directly to it.
        if (!p.equals(author)) {
            parent.put(author, find(p));
        }

        return parent.get(author);
    }

    /*
     * We merge the communities of two authors a and b.
     *
     * We use union by size:
     * we always attach the smaller tree under the larger one
     * to keep the structure balanced and efficient.
     */
    public void union(String a, String b) {

        // We make sure both authors exist in the structure.
        addIfAbsent(a);
        addIfAbsent(b);

        // We find the roots of both authors.
        String rootA = find(a);
        String rootB = find(b);

        // If both authors are already in the same community, we do nothing.
        if (rootA.equals(rootB)) {
            return;
        }

        int sizeA = size.get(rootA);
        int sizeB = size.get(rootB);

        // We ensure that rootA represents the larger community.
        // If not, we swap them.
        if (sizeA < sizeB) {
            String tmpRoot = rootA;
            rootA = rootB;
            rootB = tmpRoot;

            int tmpSize = sizeA;
            sizeA = sizeB;
            sizeB = tmpSize;
        }

        // We attach the smaller root (rootB) under the larger root (rootA).
        parent.put(rootB, rootA);

        // We update the size of the new combined community.
        size.put(rootA, sizeA + sizeB);

        // Since rootB is no longer a root, we remove its size entry.
        size.remove(rootB);

        // The number of communities decreases by one after merging.
        componentCount--;
    }

    /*
     * We return the current number of communities.
     */
    public int getComponentCount() {
        return componentCount;
    }

    /*
     * We return a map of root -> size for all current communities.
     *
     * We return a copy to avoid exposing and accidentally modifying
     * our internal data structures.
     */
    public Map<String, Integer> getRootSizes() {
        return new HashMap<>(size);
    }
}