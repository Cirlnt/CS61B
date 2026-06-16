package hashmap;

import java.util.*;
import java.util.function.Consumer;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Cirlnt
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    @Override
    public Iterator<K> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super K> action) {
        Map61B.super.forEach(action);
    }

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
    int size = 0;
    double maxLoad = 0.75;
    private Collection<Node>[] buckets;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(16);
    }

    public MyHashMap(int initialSize) {
        this.buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.maxLoad = maxLoad;
        this.buckets = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
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
        return new LinkedList<>();
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
        return new Collection[tableSize];
    }

    //这里buckets是整个哈希表，用数组来表示，每个索引对应一个桶，每个桶可以用不同的数据结构，里面的对象都是节点Node
    @Override
    public void clear(){
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        int hash = key.hashCode();
        // 为了防止负数并映射到数组索引范围内，做一步处理
        int index = (hash & 0x7fffffff) % buckets.length;
        if (buckets[index] == null) {
            buckets[index] = createBucket();
            return false;
        }
        for (Node node : buckets[index]) {
            if (node.key == null){
                return false;
            }
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int hash = key.hashCode();
        int index = (hash & 0x7fffffff) % buckets.length;
        if (buckets[index] == null) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value) {
        int hash = key.hashCode();
        int index = (hash & 0x7fffffff) % buckets.length;
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                    return;
                }
            }
        }
        Node node = createNode(key, value);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        buckets[index].add(node);
        size++;
        if ((double) size /(buckets.length)>maxLoad){
            reverse();
        }

    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node node : bucket) {
                    set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
//        throw new UnsupportedOperationException("Not supported yet.");
        int hash = key.hashCode();
        int index = (hash & 0x7fffffff) % buckets.length;
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    V values =  node.value;
                    node.value = null;
                    node.key = null;
                    return values;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void reverse(){
        Collection<Node>[] newBuckets = new Collection[buckets.length*2];
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] == null) {
                continue;
            }

            for (Node node : buckets[i]) {
                int hash = node.key.hashCode();
                int index = (hash & 0x7fffffff) % newBuckets.length;
                if (newBuckets[index] == null) {
                    newBuckets[index] = createBucket();
                }
                newBuckets[index].add(node);
            }
        }
        //不能用这个：System.arraycopy(buckets, 0, newBuckets, 0, buckets.length);
        buckets = newBuckets;
    }


    // Your code won't compile until you do so!

}
