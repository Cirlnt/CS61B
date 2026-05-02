package deque;

import java.util.Iterator; // 1. 导入 Iterator 接口
//import java.util.Objects; // 2. 导入 Objects 类，用于安全地比较元素

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    public T[] a;
    public int size = 0;
    private int head;       // 指向第一个元素
    private int tail;       // 指向下一个空位（最后一个元素的下一个位置）


    //    构建数组
    public ArrayDeque() {
        a = (T[]) new Object[8];
        head = 0;
        tail = 0;
        size = 0;
    }


    //扩大or缩小数组
    public void resize() {
        T[] temp = (T[]) new Object[a.length];
        //扩大
        if (size == a.length) {
            temp = (T[]) new Object[a.length * 2];
        }
        //缩小
        if (size <= a.length / 4) {
            temp = (T[]) new Object[a.length / 2];
        }

        int moveRight = a.length - head;
        int moveLeft = size - moveRight;
        System.arraycopy(a, head, temp, 0, moveRight);
        System.arraycopy(a, 0, temp, moveRight, moveLeft);
        a = temp;
        head = 0;
        tail = size + 1;
    }


    @Override
    public void addFirst(T item) {
        if (size == a.length) {
            resize();
        }
        head = (head - 1 + a.length) % a.length;
        a[head] = item;
        size++;

    }

    @Override
    public void addLast(T item) {
        if (size == a.length) {
            resize();
        }
        a[tail] = item;
        tail = (tail + 1) % a.length;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(a[i] + " ");
        }
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        size--;
        T end = a[head];
        a[head] = null;
        head = (head + 1) % a.length;
        return end;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        size--;
        int end = (tail - 1 + a.length) % a.length;
        T endItem = a[end];
        a[end] = null;
        return endItem;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return a[(head + index) % a.length];
    }

    // ================= 新增：iterator() 方法 =================
    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    // ================= 新增：内部迭代器类 =================
    private class ArrayDequeIterator implements Iterator<T> {
        private int cursor; // 游标，表示下一个要返回的元素的逻辑索引

        public ArrayDequeIterator() {
            cursor = 0; // 从逻辑上的第0个元素开始
        }

        @Override
        public boolean hasNext() {
            // 如果游标还没到末尾，说明还有下一个元素
            return cursor < size;
        }

        @Override
        public T next() {
            // 1. 根据逻辑索引(cursor)和head计算出在物理数组中的真实位置
            T item = a[(head + cursor) % a.length];
            // 2. 游标后移
            cursor++;
            // 3. 返回元素
            return item;
        }
    }


    // ================= 实现Equals =================
    public boolean equals(Object o) {
        if (this == o) return true;
        //if (o == null || getClass() != o.getClass()) return false;
        if (o == null) return false;
        if (!(o instanceof ArrayDeque) && !(o instanceof LinkedListDeque)){
            return false;
        }
        Deque<?> deque = (Deque<?>) o;
        if (size != deque.size()) {
            return false;
        }

        Iterator<T> myIter = this.iterator();
        Iterator<?> otherIter = ((Iterable<?>) o).iterator();

        while (myIter.hasNext()) {
            T myItem = myIter.next();
            Object otherItem = otherIter.next();
            if (myItem == null && otherItem == null) continue;
            if (myItem == null || otherItem == null) return false;
            if (!myItem.equals(otherItem)) return false;
        }
        return true;
    }
}
