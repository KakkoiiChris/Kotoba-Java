package kakkoiichris.kotoba;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static kakkoiichris.kotoba.util.Util.nanos;
import static kakkoiichris.kotoba.util.Util.seconds;

public class Buffer extends Canvas implements Runnable, KeyListener, MouseWheelListener {
    private final int foreground;
    private final int background;
    private final Font font;
    private final int xSpace;
    private final int ySpace;
    private final int tabSize;
    private final double frameRate;
    private final double scrollSpeed;
    private final int scrollAmount;
    private final int scrollBarWidth;
    private final double cursorSpeed;
    private final String inputDelimiter;
    
    // Graphics
    private BufferedImage image;
    private Raster raster;
    
    // Output
    private final List<Glyph> output = new ArrayList<>();
    private final List<Glyph> outputBuffer = new ArrayList<>();
    private final ReentrantLock outputLock = new ReentrantLock();
    
    // Input
    private final List<Glyph> input = new ArrayList<>();
    private final List<Glyph> inputBuffer = new ArrayList<>();
    private final ArrayBlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(1);
    private final ReentrantLock inputLock = new ReentrantLock();
    private final List<String> inputScanBuffer = new ArrayList<>();
    private final List<String> inputHistory = new ArrayList<>();
    
    private int inputIndex = 0;
    private int inputHistoryIndex = -1;
    private boolean inputWaiting = false;
    
    // Keys
    private final ArrayBlockingQueue<KeyEvent> keyQueue = new ArrayBlockingQueue<>(1);
    
    private boolean keyWaiting = false;
    private boolean keyOnPress = false;
    
    // Cursor
    private double cursorBlinkTimer = seconds();
    private boolean cursorVisible = false;
    
    // Formatting
    private final Map<String, Glyph.Rule> rules = new HashMap<>();
    
    private boolean rulesEnabled = true;
    private Glyph.Effect effect;
    private boolean inverted = false;
    
    // Update Loop
    private final Thread thread = new Thread(this);
    
    private boolean running = false;
    
    private int scrollTarget = 0;
    private double scrollOffset = 0.0;
    
    public Buffer(Console.Config config) {
        this.foreground = config.getForeground();
        this.background = config.getBackground();
        this.font = config.getFont();
        this.xSpace = config.getXSpace();
        this.ySpace = config.getYSpace();
        this.tabSize = config.getTabSize();
        this.frameRate = config.getFrameRate();
        this.scrollSpeed = config.getScrollSpeed();
        this.scrollAmount = config.getScrollAmount();
        this.scrollBarWidth = config.getScrollBarWidth();
        this.cursorSpeed = config.getCursorSpeed();
        this.inputDelimiter = config.getInputDelimiter();
        
        effect = new Glyph.Effect.Color(foreground);
        
        setPreferredSize(new Dimension(config.getWidth(), config.getHeight()));
        
        image = new BufferedImage(config.getWidth(), config.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        raster = new Raster(image);
        
        addKeyListener(this);
        addMouseWheelListener(this);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                
                raster = new Raster(image);
                
                end();
            }
        });
    }
    
    public Glyph.Effect getEffect() {
        return effect;
    }
    
    public void setEffect(Glyph.Effect effect) {
        this.effect = effect;
    }
    
    public boolean isInverted() {
        return inverted;
    }
    
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
    
    public boolean isRulesEnabled() {
        return rulesEnabled;
    }
    
    public void setRulesEnabled(boolean rulesEnabled) {
        this.rulesEnabled = rulesEnabled;
    }
    
    private boolean isWaiting() {
        return inputWaiting || keyWaiting;
    }
    
    private int getLineCount() {
        return (int) output.stream().filter(glyph -> glyph.getChar() == '\n').count() + 1;
    }
    
    private int getLinesOnScreen() {
        return getHeight() / (font.getHeight() + ySpace);
    }
    
    public void open() {
        requestFocus();
        
        thread.start();
    }
    
    public void close() {
        try {
            inputQueue.put("");
            keyQueue.put(new KeyEvent(this, 0, 0, 0, KeyEvent.VK_ENTER, '\n'));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        running = false;
    }
    
    public void addRules(Glyph.Rule... rules) {
        for (var rule : rules) {
            this.rules.put(rule.name(), rule);
        }
    }
    
    public boolean hasRule(String name) {
        return rules.containsKey(name);
    }
    
    public void removeRules(String... names) {
        for (var name : names) {
            rules.remove(name);
        }
    }
    
    public void clear() {
        outputLock.lock();
        
        try {
            output.clear();
            outputBuffer.clear();
        }
        finally {
            outputLock.unlock();
        }
    }
    
    public KeyEvent readKey(boolean onPress) {
        try {
            keyWaiting = true;
            
            keyOnPress = onPress;
            
            var key = keyQueue.take();
            
            keyWaiting = false;
            
            return key;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String readToken() {
        if (inputScanBuffer.isEmpty()) {
            try {
                inputWaiting = true;
                
                var line = inputQueue.take();
                
                var tokens = line.split(inputDelimiter);
                
                inputScanBuffer.addAll(List.of(tokens));
                
                inputWaiting = false;
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        var token = inputScanBuffer.remove(0);
        
        if (!token.isBlank() && (inputHistory.isEmpty() || !token.equals(inputHistory.get(0)))) {
            inputHistory.add(0, token);
        }
        
        return token;
    }
    
    public String readLine() {
        try {
            inputWaiting = true;
            
            var line = inputQueue.take();
            
            inputWaiting = false;
            
            if (!line.isBlank() && (inputHistory.isEmpty() || !line.equals(inputHistory.get(0)))) {
                inputHistory.add(0, line);
            }
            
            return line;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void write(String string) {
        var matches = new HashMap<Glyph.Rule, List<Range>>();
        
        if (rulesEnabled) {
            for (var rule : rules.values()) {
                var ranges = new ArrayList<Range>();
                
                matches.put(rule, ranges);
                
                var matcher = rule.regex().matcher(string);
                
                while (matcher.find()) {
                    var group = matcher.toMatchResult();
                    
                    ranges.add(new Range(group.start(), group.end())); // TODO POSSIBLE -1
                }
            }
        }
        
        try {
            outputLock.lock();
            
            for (var i = 0; i < string.length(); i++) {
                var c = string.charAt(i);
                
                var thisEffect = effect;
                var thisInvert = inverted;

rules:
                for (var match : matches.entrySet()) {
                    var rule = match.getKey();
                    var ranges = match.getValue();
                    
                    for (var range : ranges) {
                        if (!range.contains(i)) {
                            continue;
                        }
                        
                        thisEffect = rule.effect();
                        thisInvert = rule.invert();
                        
                        break rules;
                    }
                }
                
                outputBuffer.add(new Glyph(c, thisInvert, thisEffect.copy()));
            }
        }
        finally {
            outputLock.unlock();
        }
    }
    
    @Override
    public void run() {
        var npu = 1E9 / frameRate;
        
        var delta = 0.0;
        var timer = 0.0;
        
        var then = nanos();
        
        var updates = 0;
        var frames = 0;
        
        running = true;
        
        while (running) {
            var now = nanos();
            var elapsed = (now - then) / npu;
            then = now;
            
            delta += elapsed;
            timer += elapsed;
            
            var changed = false;
            
            while (delta >= 1.0) {
                update(delta--);
                
                updates++;
                
                changed = true;
            }
            
            if (changed) {
                render();
                
                frames++;
            }
            
            poll();
            
            if (timer >= frameRate) {
                System.out.printf("%d U, %d F%n", updates, frames);
                
                updates = 0;
                frames = 0;
                
                timer -= frameRate;
            }
        }
    }
    
    private void update(double delta) {
        output.forEach(glyph -> glyph.update(delta));
        
        if (inputWaiting) {
            input.forEach(glyph -> glyph.update(delta));
        }
        
        if (seconds() - cursorBlinkTimer >= cursorSpeed) {
            cursorVisible = isWaiting() && !cursorVisible;
            
            cursorBlinkTimer += cursorSpeed;
        }
        
        scrollOffset += (scrollTarget - scrollOffset) * scrollSpeed;
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    private void render() {
        renderRaster();
        
        if (getBufferStrategy() == null) {
            createBufferStrategy(3);
        }
        
        var graphics = (Graphics2D) getBufferStrategy().getDrawGraphics();
        
        graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        
        graphics.setColor(new Color(255, 255, 255, 125));
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        var scrollBarMargin = scrollBarWidth / 2;
        var scrollBarMaxHeight = getHeight() - scrollBarMargin * 2;
        var scrollBarHeight = (int) (scrollBarMaxHeight * ((double) getLinesOnScreen() / max(getLineCount(), getLinesOnScreen())));
        var sbX = getWidth() - scrollBarWidth - scrollBarMargin;
        var sbY = (int) (scrollBarMargin + (scrollBarMaxHeight - scrollBarHeight) * (scrollOffset / (getLineCount() - getLinesOnScreen())));
        
        graphics.fillRoundRect(sbX, sbY, scrollBarWidth, scrollBarHeight, scrollBarWidth, scrollBarWidth);
        
        graphics.dispose();
        
        getBufferStrategy().show();
    }
    
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private void renderRaster() {
        raster.clear(background);
        
        var ox = xSpace;
        var oy = ySpace - (int) (scrollOffset * (font.getHeight() + ySpace));
        
        try {
            outputLock.lock();
            
            var iterator = output.iterator();
            
            while (iterator.hasNext()) {
                var glyph = iterator.next();
                
                var c = glyph.getChar();
                var invert = glyph.isInverted();
                var color = glyph.getColor();
                var jx = glyph.getOffsetX();
                var jy = glyph.getOffsetY();
                
                switch (c) {
                    case '\t' -> {
                        var info = font.get(' ');
                        
                        ox += (info.width() + xSpace) * tabSize;
                        
                        continue;
                    }
                    
                    case '\n' -> {
                        ox = xSpace;
                        oy += font.getHeight() + ySpace;
                        
                        continue;
                    }
                }
                
                var gx = ox + jx;
                var gy = oy + jy;
                
                var info = font.get(c);
                
                var width = info.width();
                var values = info.values();
                
                for (var y = 0; y < font.getHeight(); y++) {
                    for (var x = 0; x < width; x++) {
                        var px = gx + x;
                        var py = gy + y;
                        
                        var value = values[x + y * width];
                        
                        var alpha = (invert) ? 1.0 - value : value;
                        
                        raster.put(px, py, color, alpha);
                    }
                }
                
                ox += width + xSpace;
            }
        }
        finally {
            outputLock.unlock();
        }
        
        if (inputWaiting) {
            for (var i = 0; i < input.size(); i++) {
                var glyph = input.get(i);
                
                var c = glyph.getChar();
                var invert = glyph.isInverted();
                var color = glyph.getColor();
                var jx = glyph.getOffsetX();
                var jy = glyph.getOffsetY();
                
                var gx = ox + jx;
                var gy = oy + jy;
                
                var info = font.get(c);
                
                var width = info.width();
                var values = info.values();
                
                for (var y = 0; y < font.getHeight(); y++) {
                    for (var x = 0; x < width; x++) {
                        var px = gx + x;
                        var py = gy + y;
                        
                        var value = values[x + y * width];
                        
                        var alpha = (invert || (i == inputIndex && cursorVisible)) ? 1.0 - value : value;
                        
                        raster.put(px, py, color, alpha);
                    }
                }
                
                ox += width + xSpace;
            }
            
            if ((input.isEmpty() || (inputIndex < 0 || input.size() <= inputIndex)) && cursorVisible) {
                var info = font.get(' ');
                
                var width = info.width();
                
                raster.invertRect(ox, oy, width, font.getHeight());
            }
        }
        
        if (keyWaiting && cursorVisible) {
            var info = font.get('A');
            
            var width = info.width();
            var values = info.values();
            
            for (var y = 0; y < font.getHeight(); y++) {
                for (var x = 0; x < width; x++) {
                    var px = ox + x;
                    var py = oy + y;
                    
                    var alpha = 1.0 - values[x + y * width];
                    
                    raster.put(px, py, foreground, alpha);
                }
            }
        }
    }
    
    private void poll() {
        try {
            inputLock.lock();
            
            var iterator = inputBuffer.iterator();
            
            while (iterator.hasNext()) {
                var glyph = iterator.next();
                
                var c = glyph.getChar();
                
                if (Character.isISOControl(c)) switch (c) {
                    case '\b' -> {
                        if (0 <= inputIndex - 1 && inputIndex - 1 < input.size()) {
                            input.remove(inputIndex-- - 1);
                        }
                        else {
                            beep();
                        }
                    }
                    
                    case '\u007F' -> {
                        if (0 <= inputIndex && inputIndex < input.size()) {
                            input.remove(inputIndex);
                        }
                        else {
                            beep();
                        }
                    }
                }
                else {
                    input.add(inputIndex++, glyph);
                }
                
                iterator.remove();
            }
        }
        finally {
            inputLock.unlock();
        }
        
        try {
            outputLock.lock();
            
            var iterator = outputBuffer.iterator();
            
            var outputWritten = iterator.hasNext();
            
            while (iterator.hasNext()) {
                var glyph = iterator.next();
                
                output.add(glyph);
                
                iterator.remove();
            }
            
            if (outputWritten) {
                end();
            }
        }
        finally {
            outputLock.unlock();
        }
    }
    
    private void blinkCursor() {
        cursorBlinkTimer = seconds();
        
        cursorVisible = true;
    }
    
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
    
    private void scroll(int amount) {
        scrollTarget = max(0, min(scrollTarget + amount, getLineCount() - getLinesOnScreen()));
    }
    
    private void pageUp() {
        scroll(-getLinesOnScreen());
    }
    
    private void pageDown() {
        scroll(getLinesOnScreen());
    }
    
    private void home() {
        scroll(-getLineCount());
    }
    
    private void end() {
        scroll(getLineCount());
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        if (inputWaiting) {
            inputBuffer.add(new Glyph(e.getKeyChar(), inverted, effect.copy()));
            
            blinkCursor();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (keyWaiting && keyOnPress) {
            try {
                keyQueue.put(e);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        if (inputWaiting) {
            try {
                inputLock.lock();
                
                if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown()) {
                    try {
                        var text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                        
                        inputBuffer.addAll(Glyph.toGlyphs(text, inverted, effect));
                        
                        return;
                    }
                    catch (UnsupportedFlavorException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER -> {
                        var line = input.stream().map(glyph -> String.valueOf(glyph.getChar())).collect(Collectors.joining());
                        
                        inputBuffer.clear();
                        input.clear();
                        
                        inputIndex = 0;
                        inputHistoryIndex = -1;
                        
                        write(line + '\n');
                        
                        try {
                            inputQueue.put(line);
                        }
                        catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    
                    case KeyEvent.VK_UP -> {
                        if (!inputHistory.isEmpty()) {
                            end();
                            
                            inputHistoryIndex = min(inputHistoryIndex + 1, inputHistory.size() - 1);
                            
                            inputBuffer.clear();
                            input.clear();
                            inputIndex = 0;
                            
                            inputBuffer.addAll(Glyph.toGlyphs(inputHistory.get(inputHistoryIndex), inverted, effect));
                            
                            blinkCursor();
                        }
                        else {
                            beep();
                        }
                    }
                    
                    case KeyEvent.VK_DOWN -> {
                        if (!inputHistory.isEmpty()) {
                            end();
                            
                            inputHistoryIndex = max(inputHistoryIndex - 1, 0);
                            
                            inputBuffer.clear();
                            input.clear();
                            inputIndex = 0;
                            
                            inputBuffer.addAll(Glyph.toGlyphs(inputHistory.get(inputHistoryIndex), inverted, effect));
                            
                            blinkCursor();
                        }
                        else {
                            beep();
                        }
                    }
                    
                    case KeyEvent.VK_PAGE_UP -> pageUp();
                    
                    case KeyEvent.VK_PAGE_DOWN -> pageDown();
                    
                    case KeyEvent.VK_HOME -> {
                        if (e.isControlDown()) {
                            if (inputWaiting) {
                                end();
                                
                                inputIndex = 0;
                                blinkCursor();
                            }
                        }
                        else {
                            home();
                        }
                    }
                    
                    case KeyEvent.VK_END -> {
                        if (e.isControlDown()) {
                            if (inputWaiting) {
                                end();
                                
                                inputIndex = inputBuffer.size();
                                blinkCursor();
                            }
                        }
                        else {
                            end();
                        }
                    }
                    
                    case KeyEvent.VK_LEFT -> {
                        end();
                        
                        inputIndex = max(inputIndex - 1, 0);
                        
                        if (e.isControlDown()) {
                            while (inputIndex > 0 && input.get(inputIndex - 1).getChar() != ' ') {
                                inputIndex = max(inputIndex - 1, 0);
                            }
                        }
                        
                        blinkCursor();
                    }
                    
                    case KeyEvent.VK_RIGHT -> {
                        end();
                        
                        inputIndex = min(inputIndex + 1, input.size());
                        
                        if (e.isControlDown()) {
                            while (inputIndex < input.size() && input.get(inputIndex - 1).getChar() != ' ') {
                                inputIndex = min(inputIndex + 1, input.size());
                            }
                        }
                        
                        blinkCursor();
                    }
                    
                    case KeyEvent.VK_ESCAPE -> {
                        end();
                        
                        inputHistoryIndex = -1;
                        
                        input.clear();
                        inputBuffer.clear();
                        
                        inputIndex = 0;
                        
                        blinkCursor();
                    }
                    
                    default -> end();
                }
            }
            finally {
                inputLock.unlock();
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (keyWaiting && !keyOnPress) {
            try {
                keyQueue.put(e);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scroll(e.getWheelRotation() * scrollAmount);
    }
    
    private record Range(int start, int end) {
        public boolean contains(int i) {
            return start <= i && i < end;
        }
    }
}
