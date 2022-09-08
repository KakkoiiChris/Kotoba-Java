import kakkoiichris.kotoba.Console;
import kakkoiichris.kotoba.Font;
import kakkoiichris.kotoba.Glyph;

// Christian Alexander, 9/8/2022package PACKAGE_NAME;
public class Test {
    public static void main(String[] args) {
        var config = new Console.Config().font(new Font("/font/Consolas24.bff"));
        
        var console = new Console(config);
        
        console.open();
        
        console.setEffect(Glyph.Effect.Color.red);
        
        while (true) {
            var key = console.readKey(true);
            
            if (key.isEmpty()) {
                break;
            }
            
            console.writeLine(key.get().getKeyChar());
        }
        
        console.pause();
        
        console.close();
    }
}
