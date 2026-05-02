package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public static void main(String[] args) {
        // 1. 定义37个按键的字符串（注意：这里直接用 String，而不是 String[]）
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        int N = keyboard.length();

        // 2. 创建 37 根琴弦的数组
        GuitarString[] guitarStrings = new GuitarString[N];

        // 3. 循环初始化每一根琴弦的频率
        for (int i = 0; i < N; i++) {
            // 频率公式：440 * 2^((i-24)/12)
            double frequency = 440.0 * Math.pow(2, (i - 24) / 12.0);
            guitarStrings[i] = new GuitarString(frequency);
        }

        // 4. 开启音频流
//        StdAudio.play();

        // 5. 主循环
        while (true) {
            // 检查是否有按键按下
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                // 找出这个按键在 keyboard 字符串中的索引位置
                int index = keyboard.indexOf(key);

                // 如果按键有效（即 index 在 0 到 36 之间），就拨动对应的琴弦
                if (index != -1) {
                    guitarStrings[index].pluck();
                }
            }

            // 6. 声音叠加与播放（核心！）
            double sample = 0.0;
            for (int i = 0; i < N; i++) {
                sample += guitarStrings[i].sample(); // 叠加所有琴弦当前的声音
                guitarStrings[i].tic();              // 【绝对不能少】让每根琴弦都向前震动一步！
            }
            StdAudio.play(sample);
        }
    }
}