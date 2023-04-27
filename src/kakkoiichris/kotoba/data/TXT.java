// Christian Alexander, 4/14/2023
package kakkoiichris.kotoba.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TXT {
    private final String filePath;
    
    private final List<String> lines = new ArrayList<>();
    
    private boolean isResource = false;
    
    public TXT(String filePath) {
        this.filePath = filePath;
    }
    
    public List<String> getLines() {
        return lines;
    }
    
    public String getText() {
        return String.join("\n", lines);
    }
    
    public boolean readResource() {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(filePath))))) {
            lines.clear();
            
            lines.addAll(reader.lines().toList());
            
            isResource = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    public boolean read() {
        if (!isResource) {
            lines.clear();
            
            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
                lines.addAll(reader.lines().toList());
            }
            catch (IOException e) {
                e.printStackTrace();
                
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean write() {
        if (!isResource) {
            try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
                lines.forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            catch (IOException e) {
                e.printStackTrace();
                
                return false;
            }
            
            return true;
        }
        
        return false;
    }
}
