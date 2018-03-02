package sgw.core.util;

import java.util.HashMap;

public class TrieNode<E> {

    private E value;
    private HashMap<String, TrieNode<E>> children;

    public TrieNode() {
        this(null);
    }

    public TrieNode(E obj) {
        this.value = obj;
        children = new HashMap<>();
    }

    public void addChild(String key, TrieNode child) {
        children.put(key, child);
    }

    public HashMap<String, TrieNode<E>> getChildren() {
        return children;
    }

    /**
     * Get child, create one if not exist.
     * @param key
     * @return
     */
    public TrieNode getChild(String key) {
        if (!children.containsKey(key))
            children.put(key, new TrieNode());
        return children.get(key);
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;

    }
}
