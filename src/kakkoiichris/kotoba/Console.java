package kakkoiichris.kotoba;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class Console {
    private final Frame frame;
    private final Buffer buffer;
    
    private boolean closed = true;
    
    public Console(Config config) {
        frame = new Frame(config.title);
        buffer = new Buffer(config);
        
        frame.setLayout(new BorderLayout());
        frame.add(buffer, BorderLayout.CENTER);
        frame.setFocusable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setFocusTraversalKeysEnabled(false);
        frame.setIconImage(config.icon);
        frame.setBackground(new Color(config.background));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }
    
    public String getTitle() {
        return frame.getTitle();
    }
    
    public void setTitle(String title) {
        frame.setTitle(title);
    }
    
    public Image getIcon() {
        return frame.getIconImage();
    }
    
    public void setIcon(Image icon) {
        frame.setIconImage(icon);
    }
    
    public boolean isOpen() {
        return frame.isVisible();
    }
    
    public Glyph.Effect getEffect() {
        return buffer.getEffect();
    }
    
    public void setEffect(Glyph.Effect effect) {
        buffer.setEffect(effect);
    }
    
    public boolean isInverted() {
        return buffer.isInverted();
    }
    
    public void setInverted(boolean inverted) {
        buffer.setInverted(inverted);
    }
    
    public boolean isRulesEnabled() {
        return buffer.isRulesEnabled();
    }
    
    public void setRulesEnabled(boolean rulesEnabled) {
        buffer.setRulesEnabled(rulesEnabled);
    }
    
    public void open() {
        if (!closed) {
            return;
        }
        
        closed = false;
        
        frame.setVisible(true);
        
        buffer.open();
    }
    
    public void close() {
        if (closed) {
            return;
        }
        
        closed = true;
        
        buffer.close();
        
        frame.dispose();
    }
    
    public void addRules(Glyph.Rule... rules) {
        buffer.addRules(rules);
    }
    
    public boolean hasRule(String name) {
        return buffer.hasRule(name);
    }
    
    public void removeRules(String... names) {
        buffer.removeRules(names);
    }
    
    public void clear() {
        if (closed) {
            return;
        }
        
        buffer.clear();
    }
    
    public void pause(double seconds) {
        if (closed) {
            return;
        }
        
        try {
            Thread.sleep((long) (seconds * 1000));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void pause() {
        if (closed) {
            return;
        }
        
        buffer.write("Press enter to continue...");
        
        KeyEvent event;
        
        do {
            event = buffer.readKey(true);
        }
        while (event.getKeyCode() != KeyEvent.VK_ENTER);
    }
    
    public Optional<KeyEvent> readKey(boolean onPress) {
        if (closed) {
            return Optional.empty();
        }
        
        return Optional.of(buffer.readKey(onPress));
    }
    
    public Optional<String> readToken() {
        if (closed) {
            return Optional.empty();
        }
        
        return Optional.of(buffer.read());
    }
    
    public Optional<String> readText() {
        if (closed) {
            return Optional.empty();
        }
        
        return Optional.of(buffer.readText());
    }
    
    public Optional<String> readOption(String... options) {
        if (closed) {
            return Optional.empty();
        }
        
        for (var i = 0; i < options.length; i++) {
            var option = options[i];
            
            buffer.write("(%d) %s%n".formatted(i, option));
        }
        
        int choice;
        
        while (true) {
            buffer.write("> ");
            
            var input = buffer.readText();
            
            try {
                choice = Integer.parseInt(input) - 1;
            }
            catch (NumberFormatException e) {
                buffer.write("Please enter a number.\n");
                
                continue;
            }
            
            if (0 < choice && choice < options.length) {
                buffer.write("Please enter a valid choice.\n");
                
                continue;
            }
            
            break;
        }
        
        return Optional.of(options[choice]);
    }
    
    public void write(Object x) {
        if (closed) {
            return;
        }
        
        buffer.write(x.toString());
    }
    
    public void writeFormat(String format, Object... args) {
        if (closed) {
            return;
        }
        
        buffer.write(String.format(format, args));
    }
    
    public void writeLine(Object x) {
        if (closed) {
            return;
        }
        
        buffer.write("%s%n".formatted(x));
    }
    
    public void writeLine() {
        writeLine("");
    }
    
    public static class Config {
        private String title = "Kotoba - Dynamic RGB ASCII Console";
        private Image icon = ImageIO.read(Objects.requireNonNull(Config.class.getResourceAsStream("/img/icon.png")));
        private int width = 800;
        private int height = 600;
        private int foreground = Color.black.getRGB();
        private int background = Color.white.getRGB();
        private Font font = new Font("/font/Fixedsys16.bff");
        private int xSpace = 0;
        private int ySpace = 0;
        private int tabSize = 4;
        private double frameRate = 60.0;
        private double scrollSpeed = 0.25;
        private int scrollAmount = 1;
        private int scrollBarWidth = 8;
        private double cursorSpeed = 0.5;
        private String inputDelimiter = " ";
        
        public Config() throws IOException {
        }
        
        public String getTitle() {
            return title;
        }
        
        public Config title(String title) {
            this.title = title;
            return this;
        }
        
        public Image getIcon() {
            return icon;
        }
        
        public Config icon(Image icon) {
            this.icon = icon;
            return this;
        }
        
        public int getWidth() {
            return width;
        }
        
        public Config width(int width) {
            this.width = width;
            return this;
        }
        
        public int getHeight() {
            return height;
        }
        
        public Config height(int height) {
            this.height = height;
            return this;
        }
        
        public int getForeground() {
            return foreground;
        }
        
        public Config foreground(int foreground) {
            this.foreground = foreground;
            return this;
        }
        
        public int getBackground() {
            return background;
        }
        
        public Config background(int background) {
            this.background = background;
            return this;
        }
        
        public Font getFont() {
            return font;
        }
        
        public Config font(Font font) {
            this.font = font;
            return this;
        }
        
        public int getXSpace() {
            return xSpace;
        }
        
        public Config xSpace(int xSpace) {
            this.xSpace = xSpace;
            return this;
        }
        
        public int getYSpace() {
            return ySpace;
        }
        
        public Config ySpace(int ySpace) {
            this.ySpace = ySpace;
            return this;
        }
        
        public int getTabSize() {
            return tabSize;
        }
        
        public Config tabSize(int tabSize) {
            this.tabSize = tabSize;
            return this;
        }
        
        public double getFrameRate() {
            return frameRate;
        }
        
        public Config frameRate(double frameRate) {
            this.frameRate = frameRate;
            return this;
        }
        
        public double getScrollSpeed() {
            return scrollSpeed;
        }
        
        public Config scrollSpeed(double scrollSpeed) {
            this.scrollSpeed = scrollSpeed;
            return this;
        }
        
        public int getScrollAmount() {
            return scrollAmount;
        }
        
        public Config scrollAmount(int scrollAmount) {
            this.scrollAmount = scrollAmount;
            return this;
        }
        
        public int getScrollBarWidth() {
            return scrollBarWidth;
        }
        
        public Config scrollBarWidth(int scrollBarWidth) {
            this.scrollBarWidth = scrollBarWidth;
            return this;
        }
        
        public double getCursorSpeed() {
            return cursorSpeed;
        }
        
        public Config cursorSpeed(double cursorSpeed) {
            this.cursorSpeed = cursorSpeed;
            return this;
        }
        
        public String getInputDelimiter() {
            return inputDelimiter;
        }
        
        public Config inputDelimiter(String inputDelimiter) {
            this.inputDelimiter = inputDelimiter;
            return this;
        }
    }
}
