package hashmap;

import java.util.*;
/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!

    private double maxLoad;

    private int size;

    private int capacity;

    /** Constructors */
    public MyHashMap() {
        capacity = 16;
        buckets = createTable(capacity);
        size = 0;
        maxLoad = 0.75;
    }

    public MyHashMap(int initialSize) {
        capacity = initialSize;
        buckets = createTable(capacity);
        size = 0;
        maxLoad = 0.75;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        capacity = initialSize;
        buckets = createTable(capacity);
        this.maxLoad = maxLoad;
        size = 0;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        size ++;
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] buckets = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            buckets[i] = createBucket();
        }
        return buckets;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        size = 0;
        for (Collection<Node> bucket : buckets) {
            bucket.clear();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        int k = Math.abs(key.hashCode() % capacity);
        for (Node node : buckets[k]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    private Node getNode(K key) {
        int k = Math.abs(key.hashCode() % capacity);
        for (Node node : buckets[k]) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int k = Math.abs(key.hashCode() % capacity);
        for (Node node : buckets[k]) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        buckets[k].add(createNode(key, value));
        resize();
    }

    public void resize(){
        double n = (double) size / (double) capacity;
        if (n <= maxLoad){
            return;
        }
        this.size = 0;
        this.capacity = capacity*2;
        Collection<Node>[] origin = this.buckets;
        buckets = createTable(this.capacity);
        for (Collection<Node> bucket : origin) {
            for (Node node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int k = Math.abs(key.hashCode() % capacity);
        for (Node node : buckets[k]) {
            if (node.key.equals(key)) {
                size --;
                V value = node.value;
                buckets[k].remove(node);
                return value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int k = Math.abs(key.hashCode() % capacity);
        for (Node node : buckets[k]) {
            if (node.key.equals(key) && node.value.equals(value)) {
                V v = node.value;
                buckets[k].remove(node);
                return v;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new iter();
    }

    private class iter implements Iterator <K> {
        private int bucketIndex;

        public iter(){
            bucketIndex = 0;
        }

        @Override
        public boolean hasNext() {
            if (buckets[bucketIndex].iterator().hasNext()) {
                return true;
            } else if (bucketIndex < capacity) {
                bucketIndex++;
                return hasNext();
            }
            return false;
        }

        @Override
        public K next() {
            if (hasNext()) {
                return buckets[bucketIndex].iterator().next().key;
            }
            throw new NoSuchElementException();
        }
    }
}
