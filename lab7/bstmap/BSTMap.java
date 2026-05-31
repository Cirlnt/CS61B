package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    //public class BSTMap<K,T> implements Map61B<K,V> {
    public K[] keys;
    public V[] values;
    public int size;  //size为BST的最大数字,current才是每个结点对应的index
    public int root = 1;
    public int curr;


    //index从1开始，所以索引为0的值不管，也容易初始化
    //这里构造的是一个结点
    public BSTMap(){
        keys = (K[]) new Comparable[8];
        values = (V[]) new Object[8];;
        values[0] = null;
        keys[0] = null;
        size = 0;
        curr = 1;
    }


    @Override
    public void clear(){
        size = 0;
        curr = 1;
        for (int i = 0; i < keys.length; i++){
            keys[i] = null;
            values[i] = null;
        }
    }

    @Override
    public boolean containsKey(K key){
        if (key == null){
            return false;
        }
//        Set<K> keySet = keySet();
//        for (K k : keySet){
//            if (key.equals(k)){
//                return true;
//            }
//        }
//        return false;
        curr = SearchRoot(key);
        // 没找到就返回false，防止空指针
        return keys[curr] != null && keys[curr].equals(key);
    }

    @Override
    public V get(K key){
        if (!containsKey(key)){
            return null;
        }
//        curr = SearchRoot(key);
        return values[curr];
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value){
        curr = root;
        if (size != 0){
            curr = SearchRoot(key);
        }
        if (curr >= keys.length){
            reverse(2);
        }
        if (keys[curr] == null){
            size++;
        }
        keys[curr] = key;
        values[curr] = value;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<K>();
        for (int i = 0; i < keys.length; i++){
            if (keys[i] != null){
                set.add(keys[i]);
            }
        }
        return set;
    }

    @Override
    public V remove(K key){
        if (key == null){
            return null;
        }
        curr = SearchRoot(key);
        if (curr >= keys.length){
            return null;
        }
        V value = values[curr];
        keys[curr] = null;
        values[curr] = null;
        size--;
        if (keys[root] == null){
            ChangeRoot();
        }
        return value;
    }

    @Override
    public V remove(K key, V value){
        if (key == null){
            return null;
        }
        curr = SearchRoot(key);
        if (curr >= keys.length){
            return null;
        }
        V Value = values[curr];
        if (Value.equals(value)){
            keys[curr] = null;
            values[curr] = null;
            size--;
        }
        if (keys[root] == null){
            ChangeRoot();
        }
        return value;
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public int SearchRoot(K key) {
        curr = root;
        //终止条件：当前节点为空 或者 当前节点就是目标key
        while (keys[curr] != null&&!keys[curr].equals(key)){
            if (keys[curr].compareTo(key) > 0) {
                curr *= 2;
            }
            else{
//            if (keys[curr].compareTo(key) < 0) {
                curr = curr * 2 + 1;
            }
            if (curr >= keys.length){
                reverse(2);
            }
        }
        return curr;
    }

    public boolean isLeaf(int curr){
        //满足条件：curr的左结点和右结点都不存在
        int left = curr*2;
        int right = curr*2+1;
        boolean leftEmpty = (left >= keys.length) || keys[left] == null;
        boolean rightEmpty = (right >= keys.length) || keys[right] == null;
        return leftEmpty && rightEmpty;
    }

    public void reverse(int number){
        K[] NewKeys = (K[]) new Comparable[(keys.length)*number];
        V[] NewValues = (V[]) new Object[(keys.length)*number];
        System.arraycopy(keys, 0, NewKeys, 0, keys.length);
        keys = NewKeys;
        values = NewValues;
    }

    public void ChangeRoot(){
        int rootIdx = 1;
        //这里的Root为左子树的最大值
        int LMax = root*2;
        // 左子树为空，改用右节点顶替
        if (LMax >= keys.length || keys[LMax] == null) {
            int rightNode = rootIdx * 2 + 1;
            if (rightNode < keys.length) {
                keys[rootIdx] = keys[rightNode];
                values[rootIdx] = values[rightNode];
                keys[rightNode] = null;
                values[rightNode] = null;
            }
            return;
        }
        //一直往右子树看，直到为null
        while (true){
            int right = LMax*2+1;
            if (right >= keys.length || keys[right] == null) {
                break;
            }
            LMax = right;
        }
        keys[1] =  keys[LMax];
        values[1] = values[LMax];
        //========= 修复：递归清空/承接多层左子树 =========
        clearLeftSubTree(LMax);

    }

    // 清空指定节点，并把它的左子树依次上移
    private void clearLeftSubTree(int idx) {
        int left = idx * 2;
        // 没有左子树，直接置空当前节点
        if (left >= keys.length || keys[left] == null) {
            keys[idx] = null;
            values[idx] = null;
            return;
        }
        // 左孩子顶替当前位置
        keys[idx] = keys[left];
        values[idx] = values[left];
        // 递归处理原左孩子的位置（继续承接它的子树）
        clearLeftSubTree(left);
        clearRightSubTree(idx*2+1);
    }

    private void clearRightSubTree(int idx) {
        int right = idx * 2+1;
        // 没有左子树，直接置空当前节点
        if (right >= keys.length || keys[right] == null) {
            keys[idx] = null;
            values[idx] = null;
            return;
        }
        // 左孩子顶替当前位置
        keys[idx] = keys[right];
        values[idx] = values[right];
        // 递归处理原左孩子的位置（继续承接它的子树）
        clearRightSubTree(right);
    }



}
