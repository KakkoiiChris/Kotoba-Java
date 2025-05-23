package kakkoiichris.kotoba;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import static kakkoiichris.kotoba.util.ColorMath.*;

public class Raster {
    private final int[] pixels;
    private final int width;
    private final int height;
    
    public Raster(BufferedImage image) {
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
    }
    
    public void clear(int c) {
        Arrays.fill(pixels, c);
    }
    
    public int get(int x, int y) {
        if (0 <= x && x < width && 0 <= y && y < height) {
            return pixels[x + y * width];
        }
        
        return 0;
    }
    
    public void put(int x, int y, int c, double a) {
        if (0 <= x && x < width && 0 <= y && y < height) {
            pixels[x + y * width] = switch ((int) (a * 2)) {
                case 0 -> pixels[x + y * width];
                
                case 1 -> blend(c, pixels[x + y * width], a);
                
                default -> c;
            };
        }
    }
    
    public void invertRect(int x, int y, int w, int h) {
        for (var oy = 0; oy < h; oy++) {
            var yy = y + oy;
            
            if (yy < 0 || yy >= height) continue;
            
            for (var ox = 0; ox < w; ox++) {
                var xx = x + ox;
                
                if (xx < 0 || xx >= width) continue;
                
                pixels[xx + yy * width] = getInverse(pixels[xx + yy * width]);
            }
        }
    }
}