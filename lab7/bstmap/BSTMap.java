package bstmap;

import afu.org.checkerframework.checker.units.qual.K;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private Node root;

    private int size;

    private class Node {
        private K key;
        private V value;
        private Node left, right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    };

    public BSTMap() {
        this.size = 0;
    }

    private Node clear(Node node) {
        if (node == null) {
            return null;
        }
        node.left = clear(node.left);
        node.right = clear(node.right);
        return null;
    }

    @Override
    public void clear() {
        root = clear(root);
        this.size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls containsKey() with a null key");
        }
        return get(root, key) != null;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        return (get(root, key) != null) ? get(root, key).value : null;
    }

    private Node get(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node;
        } else if (cmp < 0) {
            return get(node.left, key);
        } else {
            return get(node.right, key);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value) {
        if (node == null) {
            this.size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            node.value = value;
        } else if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.right = put(node.right, key, value);
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        V value = get(key);
        root = remove(key, root);
        return value;
    }

    @Override
    public V remove(K key, V value) {
        V value1 = get(key);
        root = remove1(key, value, root);
        return value1;
    }

    private Node remove(K key, Node node) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            size --;
            V value = node.value;
            if (node.left == null && node.right == null) {
                node = null;
            } else if (node.left == null) {
               node = node.right;
            } else if (node.right == null) {
                node = node.left;
            } else {
                Node tmp = findSuccessor(node);
                node.value = tmp.value;
                node.key = tmp.key;
                node.right = remove(tmp.key, node.right);
            }
            return node;
        } else if (cmp < 0) {
            return remove(key, node.left);
        } else {
            return remove(key, node.right);
        }
    }

    private Node remove1(K key, V value, Node node) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0 && node.value.equals(value)) {
            size --;
            if (node.left == null && node.right == null) {
                node = null;
            } else if (node.left == null) {
                node = node.right;
            } else if (node.right == null) {
                node = node.left;
            } else {
                Node tmp = findSuccessor(node);
                node.value = tmp.value;
                node.key = tmp.key;
                node.right = remove(tmp.key, node.right);
            }
            return node;
        } else if (cmp < 0) {
            return remove(key, node.left);
        } else {
            return remove(key, node.right);
        }
    }

    private Node findSuccessor(Node node) {
        node = node.right;
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
    // store Value but use Key to compare and make it in order

}
