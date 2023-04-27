// Christian Alexander, 12/9/2022
package kakkoiichris.kotoba.data.json;

import kakkoiichris.kotoba.data.json.lexer.Lexer;
import kakkoiichris.kotoba.data.json.lexer.Location;
import kakkoiichris.kotoba.data.json.parser.Object;
import kakkoiichris.kotoba.data.json.parser.Parser;

import java.util.Map;

public class Json<X> {
    private final JsonConverter<X> converter;
    
    private Object root;
    
    private Json(Object root, JsonConverter<X> converter) {
        this.root = root;
        this.converter = converter;
    }
    
    public static <X> Json<X> empty(JsonConverter<X> converter) {
        var root = new Object(new Location(0, 0), Map.of());
        
        return new Json<>(root, converter);
    }
    
    public static <X> Json<X> of(String source, JsonConverter<X> converter) {
        var lexer = new Lexer(source);
        
        var parser = new Parser(lexer);
        
        var root = parser.parse();
        
        return new Json<>(root, converter);
    }
    
    public Object getRoot() {
        return root;
    }
    
    public X load() {
        return converter.load(root);
    }
    
    public void save(X x) {
        root = converter.save(x);
    }
}
