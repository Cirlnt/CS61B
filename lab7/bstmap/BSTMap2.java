package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap2<K extends Comparable<K>, V> implements Map61B<K, V>{
    public K[] keys;
    public V[] values;
    public int size;
    public int root = 1;
    public int curr;

    // 修复1：构造方法给初始容量，不再用size初始化，解决一开始直接数组越界
    public BSTMap2(){
        int initCap = 16;
        keys = (K[]) new Comparable[initCap];
        values = (V[]) new Object[initCap];
        values[0] = null;
        keys[0] = null;
        size = 0;
        curr = 1;
    }

    @Override
    public void clear(){
        size = 0;
        curr = 1;
        // 清空数组所有残留旧数据，擦除之前put的内容
        for (int i = 0; i < keys.length; i++) {
            keys[i] = null;
            values[i] = null;
        }
    }

    @Override
    public boolean containsKey(K key){
        if(key == null) return false;
        curr = SearchRoot(key);
        // 没找到就返回false，防止空指针
        return keys[curr] != null && keys[curr].equals(key);
    }

    @Override
    public V get(K key){
        if (!containsKey(key)){
            return null;
        }
        return values[curr];
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value){
        curr = root;
        // 树不为空，去找插入位置
        if (size != 0){
            curr = SearchRoot(key);
        }

        // 需要扩容：当前下标快要超过数组长度，直接翻倍
        if(curr >= keys.length){
            reverse(2);
        }

        // 新节点
        if(keys[curr] == null){
            size++;
        }
        keys[curr] = key;
        values[curr] = value;
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

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    // 修复2：修复查找死循环、左右方向写反
    public int SearchRoot(K key) {
        curr = root;
        // 终止条件：当前节点为空 或者 当前节点就是目标key
        while (keys[curr] != null && !keys[curr].equals(key)){
            int cmp = keys[curr].compareTo(key);
            if (cmp > 0) {
                // 当前key更大，去左子树 2*curr
                curr = curr * 2;
            } else {
                // 当前key更小，去右子树 2*curr+1
                curr = curr * 2 + 1;
            }
            // 自动扩容，防止查找过程越界
            if(curr >= keys.length){
                reverse(2);
            }
        }
        return curr;
    }

    // 修复3：修复叶子节点判断（之前完全写反+互递归死循环，彻底删掉containsKey）
    public boolean isLeaf(int curr){
        int left = curr*2;
        int right = curr*2+1;
        // 安全判断：先判断下标是否越界，再判断是否为空
        boolean leftEmpty = (left >= keys.length) || keys[left] == null;
        boolean rightEmpty = (right >= keys.length) || keys[right] == null;
        // 左右都空 = 叶子节点
        return leftEmpty && rightEmpty;
    }

    // 修复4：扩容方法，复制旧数据，不再清空数组
    public void reverse(int number){
        int newLen = keys.length * number;
        K[] NewKeys = (K[]) new Comparable[newLen];
        V[] NewValues = (V[]) new Object[newLen];
        // 把旧数组全部复制过来
        for (int i = 0; i < keys.length; i++) {
            NewKeys[i] = keys[i];
            NewValues[i] = values[i];
        }
        keys = NewKeys;
        values = NewValues;
    }

}
