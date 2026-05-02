package deque;

import java.util.Iterator; // 1. 导入 Iterator 接口
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    class Node {
        T item;
        Node next;
        Node prev;

        Node(T item, Node next, Node prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }

    }

    private Node head;
    private Node tail;
    public int size;


    public LinkedListDeque() {
        head = new Node(null, null, null);
        tail = new Node(null, null, null);
        head.next = tail;
        head.prev = tail;
        tail.next = head;
        tail.prev = head;
        size = 0;
    }


    //    将数据加入到最前方
    @Override
    public void addFirst(T item) {
        Node cur = new Node(item, head.next, head);
        head.next.prev = cur; //原来的
        head.next = cur;
        size++;
    }

    //        将数据加入到最后方
    @Override
    public void addLast(T item) {
        Node cur = new Node(item, tail, tail.prev);
        tail.prev.next = cur;
        tail.prev = cur;
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

    //    按顺序打印队列中的项目，从头到尾， 中间只隔着一个空隙。所有物品打印完毕后，打印一行新行。
    @Override
    public void printDeque() {
        Node cur = head.next;
        while (cur.item != null) {
            System.out.print(cur.item + " ");
            cur = cur.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node cur = head.next;
        head.next = cur.next;
        cur.next.prev = head;
        size--;
        return cur.item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node cur = tail.prev;
        tail.prev = cur.prev;
        cur.prev.next = tail;
        size--;
        return cur.item;
    }

    @Override
    public T get(int index) {
        Node cur = head.next;
        while (index != 0) {
            cur = cur.next;
            index--;
        }
        if (index > size) {
            return null;
        }
        return cur.item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return getHelper(head.next, index);
    }

    private T getHelper(Node node, int index) {
        if (index == 0) {
            return node.item;
        }
        return getHelper(node.next, index - 1);
    }


    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node cur;

        public LinkedListDequeIterator() {
            cur = head.next;
        }

        @Override
        public boolean hasNext() {
            return cur.next != null;
        }

        @Override
        public T next() {
            return cur.next.item;
        }

    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deque<?> deque = (Deque<?>) o;
        if (size != deque.size()) {
            return false;
        }

        Iterator<T> myIter = this.iterator();
        Iterator<?> otherIter = ((Iterable<?>) o).iterator();

        while (myIter.hasNext() && otherIter.hasNext()) {
            T myItem = myIter.next();
            Object otherItem = otherIter.next();
            if (!myIter.next().equals(otherIter.next())) {
                return false;
            }
            if (myItem == null && otherItem == null) continue;
            if (myItem == null || otherItem == null) return false;
        }
        return true;
    }
}



