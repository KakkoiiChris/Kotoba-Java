package kakkoiichris.kotoba;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Font {
    private final int height;
    
    private final char firstChar;
    
    private final CharacterInfo[] chars;
    
    public Font(String path) {
        var data = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        
        // Skip Format Bitmap Font File Version ID
        try {
            data.readShort();
            
            var fontImageWidth = reverseEndianness(data.readInt());
            var fontImageHeight = reverseEndianness(data.readInt());
            
            var cellWidth = reverseEndianness(data.readInt());
            var cellHeight = reverseEndianness(data.readInt());
            
            int cols = fontImageWidth / cellWidth;
            
            height = cellHeight;
            
            // Skip bytes-per-pixel, always 8-bit grayscale
            data.readByte();
            
            firstChar = (char) data.readUnsignedByte();
            
            var characterWidths = new int[256];
            
            for (var i = 0; i < characterWidths.length; i++) {
                characterWidths[i] = data.readUnsignedByte();
            }
            
            var allValues = new double[fontImageWidth][fontImageHeight];
            
            for (var y = 0; y < allValues.length; y++) {
                for (var x = 0; x < allValues[y].length; x++) {
                    allValues[x][y] = data.readUnsignedByte() / (double) 0xFF;
                }
            }
            
            var row = 0;
            var col = 0;
            
            var charValues = new ArrayList<CharacterInfo>();
            
            for (var c = firstChar; c <= 255; c++) {
                var values = new ArrayList<Double>();
                
                var width = characterWidths[c];
                
                for (var y = 0; y < height; y++) {
                    for (var x = 0; x < width; x++) {
                        var vr = (row * cellHeight) + y;
                        var vc = (col * cellWidth) + x;
                        
                        values.add(allValues[vc][vr]);
                    }
                }
                
                if (++col == cols) {
                    col = 0;
                    row++;
                }
                
                charValues.add(new CharacterInfo(width, values.stream().mapToDouble(Double::doubleValue).toArray()));
            }
            
            chars = charValues.toArray(new CharacterInfo[0]);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static int reverseEndianness(int color) {
        var byte0 = (color >> 24) & 0xFF;
        var byte1 = (color >> 16) & 0xFF;
        var byte2 = (color >> 8) & 0xFF;
        var byte3 = color & 0xFF;
        
        return (byte3 << 24) | (byte2 << 16) | (byte1 << 8) | byte0;
    }
    
    public int getHeight() {
        return height;
    }
    
    public CharacterInfo get(char c) {
        if (c < firstChar || c > 0xFF) {
            c = ' ';
        }
        
        return chars[c - firstChar];
    }
    
    public record CharacterInfo(int width, double[] values) {
    }
}
