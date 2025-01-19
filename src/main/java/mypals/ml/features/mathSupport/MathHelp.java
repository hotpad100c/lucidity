package mypals.ml.features.mathSupport;

public class MathHelp {
    public static int addAlphaWithDecay(int rgb, double value) {
        // k 是一个调整因子，控制 alpha 变化的速度
        double k = 0.5; // 你可以根据需要调整 k 值

        // 使用指数衰减公式将无限大的 value 映射到 alpha 范围
        int alpha = (int) (255 * (1 - Math.exp(-k * value)));

        // 确保 alpha 在 0-255 的范围内
        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;

        // 将 alpha 值添加到 RGB 中
        int rgba = (alpha << 24) | (rgb & 0xFFFFFF);

        return rgba;
    }
}
