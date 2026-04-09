package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        AList <Integer> Ns = new AList<>();
        AList <Double> times = new AList<>();
        AList <Integer> opCounts = new AList<>();

        int N = 1000;
        while (N <= 128000) {
            // a. 创建一个新的坏策略 AList
            AList<Integer> testList = new AList<>();

            // b. 开启计时器
            Stopwatch sw = new Stopwatch();

            // c. 执行 N 次 addLast 操作
            for (int i = 0; i < N; i++) {
                testList.addLast(i);
            }

            // d. 停止计时并获取时间 (秒)
            double timeInSeconds = sw.elapsedTime();

            // e. 将数据存入我们的记录列表
            Ns.addLast(N);
            times.addLast(timeInSeconds);
            opCounts.addLast(N);

            // f. N 翻倍，准备下一次实验
            N = N * 2;
        }

        // 3. 调用打印函数 (题目中提供的工具方法)
        // 注意：这个方法通常定义在同一个文件里，或者是一个静态导入的工具类
        printTimingTable(Ns, times, opCounts);
    }

}
