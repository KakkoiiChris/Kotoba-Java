package kakkoiichris.kotoba;

public class Util {
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
    
    public static int blend(int srgb, int drgb, double alpha) {
        var sr = getRed(srgb) / 255.0;
        var sg = getGreen(srgb) / 255.0;
        var sb = getBlue(srgb) / 255.0;
        
        var dr = getRed(drgb) / 255.0;
        var dg = getGreen(drgb) / 255.0;
        var db = getBlue(drgb) / 255.0;
        
        var br = (int) (((alpha * sr) + ((1.0 - alpha) * dr)) * 0xFF);
        var bg = (int) (((alpha * sg) + ((1.0 - alpha) * dg)) * 0xFF);
        var bb = (int) (((alpha * sb) + ((1.0 - alpha) * db)) * 0xFF);
        
        return (br << 16) | (bg << 8) | bb;
    }
    
    public static double seconds() {
        return System.nanoTime() / 1E9;
    }
    
    public static double millis() {
        return System.currentTimeMillis();
    }
    
    public static double nanos() {
        return System.nanoTime();
    }
}
