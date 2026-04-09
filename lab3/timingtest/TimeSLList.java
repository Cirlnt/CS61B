package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> Ns = new AList<>();       // 存放 N (1000, 2000, ...)
        AList<Double> times = new AList<>();     // 存放耗时 (秒)
        AList<Integer> opCounts = new AList<>(); // 存放操作次数 (固定为 10000)

        // 2. 循环进行实验
        int N = 1000;
        int maxN = 128000; // 最大测试规模
        int M = 10000;     // 每次实验执行 getLast 的次数

        while (N <= maxN) {
            // a. 创建一个新的 SLList
            SLList<Integer> testList = new SLList<>();

            // b. 【关键步骤】预先填充 N 个元素，这一步不计入计时
            // 我们使用 addFirst，这样 getLast 就能获取到最早添加的元素
            for (int i = 0; i < N; i++) {
                testList.addFirst(i);
            }

            // c. 开启计时器
            Stopwatch sw = new Stopwatch();

            // d. 执行 M 次 getLast 操作
            for (int i = 0; i < M; i++) {
                testList.getLast();
            }

            // e. 停止计时并获取时间 (秒)
            double timeInSeconds = sw.elapsedTime();

            // f. 将数据存入我们的记录列表
            Ns.addLast(N);
            times.addLast(timeInSeconds);
            opCounts.addLast(M); // 操作次数是固定的 M

            // g. N 翻倍，准备下一次实验
            N = N * 2;
        }

        // 3. 调用打印函数
        printTimingTable(Ns, times, opCounts);
    }
}

