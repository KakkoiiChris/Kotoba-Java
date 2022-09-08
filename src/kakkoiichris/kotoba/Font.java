package kakkoiichris.kotoba;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Font {
    private final int rows;
    private final int cols;
    private final int height;
    
    private final char firstChar;
    
    private final CharacterInfo[] chars;
    
    public Font(String path) {
        var data = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        
        // Skip Format Bitmap Font File Version ID
        try {
            data.readShort();
            
            var fontImageWidth = fixed(data.readInt());
            var fontImageHeight = fixed(data.readInt());
            
            var cellWidth = fixed(data.readInt());
            var cellHeight = fixed(data.readInt());
            
            rows = fontImageHeight / cellHeight;
            cols = fontImageWidth / cellWidth;
            
            height = cellHeight;
            
            // Skip bytes-per-pixel, always 8-bit grayscale
            data.readByte();
            
            firstChar = (char) data.readUnsignedByte();
            
            var characterWidths = new int[256];
            
            for (var i = 0; i < characterWidths.length; i++) {
                characterWidths[i] = data.readUnsignedByte();
            }
            
            var allValues = new double[fontImageHeight][fontImageWidth];
            
            for (var value : allValues) {
                for (var i = 0; i < value.length; i++) {
                    value[i] = data.readUnsignedByte() / (double) 0xFF;
                }
            }
            
            var row = 0;
            var col = 0;
            
            var charValues = new ArrayList<CharacterInfo>();
            
            for (var c = firstChar; c <= '\u00FF'; c++) {
                var values = new ArrayList<Double>();
                
                var width = characterWidths[c];
                
                for (var y = 0; y < height; y++) {
                    for (var x = 0; x < width; x++) {
                        var vr = (row * cellHeight) + y;
                        var vc = (col * cellWidth) + x;
                        
                        values.add(allValues[vr][vc]);
                    }
                }
                
                if (++col == cols) {
                    col = 0;
                    row++;
                }
                
                charValues.add(new CharacterInfo(width, values.stream().mapToDouble(Double::doubleValue).toArray()));
            }
            
            chars = (CharacterInfo[]) charValues.toArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private int fixed(int color) {
        var byte0 = (color >> 24) & 0xFF;
        var byte1 = (color >> 16) & 0xFF;
        var byte2 = (color >> 8) & 0xFF;
        var byte3 = color & 0xFF;
        
        return (byte3 << 24) | (byte2 << 16) | (byte1 << 8) | byte0;
    }
    
    public CharacterInfo get(char c) {
        return chars[((c >= firstChar) ? c : ' ') + firstChar];
    }
    
    public int widthOfGlyphs(List<Glyph> glyphs, int space){
        var fullWidth = 0;
        
        var iterator = glyphs.iterator();
        
        while (iterator.hasNext()) {
            var c = iterator.next().getChar();
            
            var width = get(c).width;
            
            fullWidth += width + space;
        }
        
        return fullWidth;
    }
    
    public int widthOfChars(List<Character> chars, int space) {
        var fullWidth = 0;
        
        var iterator = chars.iterator();
        
        while (iterator.hasNext()) {
            var c = iterator.next();
            
            var width = get(c).width;
            
            fullWidth += width + space;
        }
        
        return fullWidth;
    }
    
    public record CharacterInfo(int width, double[] values) {
    }
}
