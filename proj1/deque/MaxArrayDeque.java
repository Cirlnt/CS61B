package deque;

import java.util.ArrayDeque;
import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comparator;


    // 1. 必须加上这个无参构造器！否则 new MaxArrayDeque<>() 会报错
    public MaxArrayDeque() {
        super();
        this.comparator = null; // 默认比较器为空
    }
    
    public MaxArrayDeque(Comparator<T> integerComparator) {
        super();
        this.comparator = integerComparator;
    }


    public T max() {
        if (this.isEmpty()) {
            return null;
        }
        T maxVal = null;
        for (T item : this) {
            if (maxVal == null || comparator.compare(item, maxVal) > 0) {
                maxVal = item;
            }
        }
        return maxVal;
    }

    public T max(Comparator<T> c) {
        if (this.isEmpty()) {
            return null;
        }
        T maxVal = null;
        for (T item : this) {
            if (maxVal == null || c.compare(item, maxVal) > 0) {
                maxVal = item;
            }
        }
        return maxVal;
    }
}