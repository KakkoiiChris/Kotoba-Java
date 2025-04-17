package kakkoiichris.kotoba.util;

public class ColorMath {
    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    public static int getInverse(int rgb) {
        return 0xFFFFFF - rgb;
    }

    public static int blend(int srcRGB, int dstRGB, double alpha) {
        var sr = getRed(srcRGB) / 255.0;
        var sg = getGreen(srcRGB) / 255.0;
        var sb = getBlue(srcRGB) / 255.0;

        var dr = getRed(dstRGB) / 255.0;
        var dg = getGreen(dstRGB) / 255.0;
        var db = getBlue(dstRGB) / 255.0;

        var br = (int) (((alpha * sr) + ((1.0 - alpha) * dr)) * 0xFF);
        var bg = (int) (((alpha * sg) + ((1.0 - alpha) * dg)) * 0xFF);
        var bb = (int) (((alpha * sb) + ((1.0 - alpha) * db)) * 0xFF);

        return (br << 16) | (bg << 8) | bb;
    }
}