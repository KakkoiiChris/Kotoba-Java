// Christian Alexander, 4/14/2023
package kakkoiichris.kotoba.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CSV {
    private final String filePath;
    
    private final List<Row> rows = new ArrayList<>();
    
    private boolean isResource = false;
    
    public CSV(String filePath) {
        this.filePath = filePath;
    }
    
    public boolean readResource() {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(filePath))))) {
            rows.clear();
            
            var lines = reader.lines().toList();
            
            rows.addAll(lines.stream().map(Row::parse).toList());
            
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
            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
                rows.clear();
                
                var lines = reader.lines().toList();
                
                rows.addAll(lines.stream().map(Row::parse).toList());
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
                for (var row : rows) {
                    writer.write(row.toString());
                    writer.newLine();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    public Row get(int index) {
        return rows.get(index);
    }
    
    public static class Row {
        private final List<String> data;
        
        public Row(List<String> data) {
            this.data = data;
        }
        
        public static Row parse(String line) {
            line += '\u0000';
            
            var data = new ArrayList<String>();
            
            var token = new StringBuilder();
            
            var inSingleQuotes = false;
            var inDoubleQuotes = false;
            
            for (var c : line.toCharArray()) {
                if (!inDoubleQuotes && c == '\'') {
                    inSingleQuotes = !inSingleQuotes;
                    
                    continue;
                }
                
                if (!inSingleQuotes && c == '"') {
                    inDoubleQuotes = !inDoubleQuotes;
                    
                    continue;
                }
                
                if (!inSingleQuotes && !inDoubleQuotes && (c == ',' || c == '\u0000')) {
                    data.add(token.toString());
                    
                    token = new StringBuilder();
                    
                    continue;
                }
                
                token.append(c);
            }
            
            return new CSV.Row(data);
        }
        
        public String getHeader() {
            return data.get(0);
        }
        
        public List<String> getDataWithoutHeader() {
            return data.subList(1, data.size());
        }
        
        @Override
        public String toString() {
            return String.join(",", data.stream().map(item -> {
                if (item.indexOf(',') >= 0) {
                    if (item.indexOf('\'') >= 0) {
                        item = "\"%s\"".formatted(item);
                    }
                    else {
                        item = "'%s'".formatted(item);
                    }
                }
                
                return item;
            }).toList());
        }
    }
}
