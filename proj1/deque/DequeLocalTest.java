package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;

public class DequeLocalTest {

    @Test
    public void randomizedTest() {
        // 你的实现
        Deque<Integer> myDeque = new LinkedListDeque<>();
        // 官方实现作为参照物 (Oracle)
        java.util.Deque<Integer> correctDeque = new java.util.ArrayDeque<>();

        Random random = new Random();
        int N = 100000; // 测试次数

        for (int i = 0; i < N; i++) {
            int operation = random.nextInt(5); // 生成 0-4 的随机数

            if (operation == 0) {
                // --- addFirst ---
                int val = random.nextInt();
                myDeque.addFirst(val);
                correctDeque.addFirst(val);
            } else if (operation == 1) {
                // --- addLast ---
                int val = random.nextInt();
                myDeque.addLast(val);
                correctDeque.addLast(val);
            } else if (operation == 2) {
                // --- removeFirst ---
                if (!myDeque.isEmpty()) {
                    Integer myVal = myDeque.removeFirst();
                    Integer correctVal = correctDeque.removeFirst();
                    assertEquals("removeFirst 结果不一致", correctVal, myVal);
                }
            } else if (operation == 3) {
                // --- removeLast ---
                if (!myDeque.isEmpty()) {
                    Integer myVal = myDeque.removeLast();
                    Integer correctVal = correctDeque.removeLast();
                    assertEquals("removeLast 结果不一致", correctVal, myVal);
                }
            } else if (operation == 4) {
                // --- get ---
                if (!myDeque.isEmpty()) {
                    int idx = random.nextInt(myDeque.size());
                    Integer myVal = myDeque.get(idx);
                    // 官方 ArrayDeque 转数组来获取随机索引的值
                    Integer correctVal = (Integer) correctDeque.toArray()[idx];
                    assertEquals("get 结果不一致", correctVal, myVal);
                }
            }

            // 每次操作后都检查 size 是否一致
            assertEquals("Size 不一致", correctDeque.size(), myDeque.size());
        }
    }
}