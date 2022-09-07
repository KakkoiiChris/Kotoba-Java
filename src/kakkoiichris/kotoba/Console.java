package kakkoiichris.kotoba;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Console {
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
