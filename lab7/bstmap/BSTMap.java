package bstmap;

import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
//public class BSTMap<K,T> implements Map61B<K,V> {
    public K[] keys;
    public V[] values;
    public int size;  //size为BST的最大数字,current才是每个结点对应的index
    public int root = 1; //也就是curr
    public BSTMap<K,V> LTree;
    public BSTMap<K,V> RTree;

    //这里构造的是一个结点
    public BSTMap(int index){
        keys = (K[]) new Object[index];
        values = (V[]) new Object[index];
        int curr = index;
    }

    public BSTMap(){
        keys = null;
        values = null;
        size = 0;
    }

    @Override
    public void clear(){
        keys = null;
        values = null;
    }

    @Override
    public boolean containsKey(K key){
      // 这里使用剪枝，如果小于进入左子树，大于进入右子树
        if (keys[root].compareTo(key) == 0){
            return true;
        }
        if (keys[root].compareTo(key) < 0){
            if (LTree.containsKey(key)){
                return true;
            }
        }
        if (keys[root].compareTo(key) > 0){
            if (RTree.containsKey(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key){
        if (!containsKey(key)){
            return null;
        }
        root = SearchRoot(key);
        return values[root];
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value){
        root = SearchRoot(key);
        if (root > size){
            size++;
        }
        values[root] = value;
        keys[root] = key;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V remove(K key){
        throw new UnsupportedOperationException();
    }

    public V remove(K key, V value){
        throw new UnsupportedOperationException("Not supported yet.");
    }




    public int SearchRoot(K key) {
        while (keys[root].compareTo(key) != 0||!HaveLeaves(root)){
            if (keys[root].compareTo(key) < 0) {
                root *= 2;
            }
            if (keys[root].compareTo(key) > 0) {
                root = root * 2;
                root++;
            }
        }
        return root;
    }

    public boolean HaveLeaves(int root){
        return LTree != null && RTree != null;
    }


}
