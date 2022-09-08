package kakkoiichris.kotoba;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sin;

public class Glyph {
    private final char c;
    private final Effect effect;
    private final boolean inverted;
    
    private int color = 0;
    private int offsetX = 0;
    private int offsetY = 0;
    
    public Glyph(char c, Effect effect, boolean inverted) {
        this.c = c;
        this.effect = effect;
        this.inverted = inverted;
    }
    
    public char getChar() {
        return c;
    }
    
    public Effect getEffect() {
        return effect;
    }
    
    public boolean isInverted() {
        return inverted;
    }
    
    public int getColor() {
        return color;
    }
    
    public int getOffsetX() {
        return offsetX;
    }
    
    public int getOffsetY() {
        return offsetY;
    }
    
    public void update(double delta) {
        effect.apply(this, delta);
    }
    
    public interface Effect {
        record Color(int rgb) implements Effect {
            public static final Color red = new Color(0xF75DB3);
            public static final Color orange = new Color(0xF9642D);
            public static final Color yellow = new Color(0xFFF13A);
            public static final Color green = new Color(0x6CB94B);
            public static final Color blue = new Color(0x405AB9);
            public static final Color purple = new Color(0xC76EDF);
            public static final Color white = new Color(0xFFFFFF);
            public static final Color silver = new Color(0xC4C4C4);
            public static final Color gray = new Color(0x7A7A7A);
            public static final Color soot = new Color(0x504D4B);
            public static final Color black = new Color(0x2E2C2B);
            
            public static Color random() {
                return new Color((int) (Math.random() * 0xFFFFFF));
            }
            
            @Override
            public void apply(Glyph glyph, double delta) {
                glyph.color = rgb;
            }
            
            @Override
            public Effect copy() {
                return new Color(rgb);
            }
        }
        
        class Cycle implements Effect {
            private final double speed;
            private final int[] colors;
            
            private double i = 0.0;
            
            public Cycle(double speed, int... colors) {
                this.speed = speed;
                this.colors = colors;
            }
            
            @Override
            public void apply(Glyph glyph, double delta) {
                glyph.color = colors[(int) i % colors.length];
                
                i += speed * delta;
            }
            
            @Override
            public Effect copy() {
                return new Cycle(speed, colors);
            }
        }
        
        class Random implements Effect {
            private static Random instance;
            
            private Random() {
            }
            
            public static Random get() {
                if (instance == null) {
                    instance = new Random();
                }
                
                return instance;
            }
            
            public void apply(Glyph glyph, double delta) {
                glyph.color = (int) (Math.random() * 0xFFFFFF);
            }
            
            public Effect copy() {
                return instance;
            }
        }
        
        record Jitter(int x, int y) implements Effect {
            @Override
            public void apply(Glyph glyph, double delta) {
                glyph.offsetX = (int) (Math.random() * ((x * 2) + 1)) - x;
                glyph.offsetY = (int) (Math.random() * ((y * 2) + 1)) - y;
            }
            
            @Override
            public Effect copy() {
                return new Jitter(x, y);
            }
        }
        
        static final class Wave implements Effect {
            private final double amplitude;
            private final double frequency;
            private final double speed;
            private final boolean vertical;
            
            public Wave(double amplitude, double frequency, double speed, boolean vertical) {
                this.amplitude = amplitude;
                this.frequency = frequency;
                this.speed = speed;
                this.vertical = vertical;
            }
            
            private double phase = 0.0;
            
            @Override
            public void apply(Glyph glyph, double delta) {
                if (vertical) {
                    glyph.offsetY = (int) (amplitude * sin(frequency*phase));
                }
                else {
                    glyph.offsetX = (int) (amplitude * sin(frequency*phase));
                }
                
                phase += speed * delta;
            }
            
            @Override
            public Effect copy() {
                return new Wave(amplitude, frequency, speed, vertical);
            }
        }
        
        class Multi implements Effect {
            private final List<Effect> effects = new ArrayList<>();
            
            public Multi(Effect... effects) {
                this.effects.addAll(List.of(effects));
            }
            
            @Override
            public Multi and(Effect effect) {
                if (effect instanceof Multi multi) {
                    effects.addAll(multi.effects);
                }
                else {
                    effects.add(effect);
                }
                
                return this;
            }
            
            @Override
            public void apply(Glyph glyph, double delta) {
                effects.forEach(effect -> effect.apply(glyph, delta));
            }
            
            @Override
            public Effect copy() {
                return new Multi((Effect[]) effects.stream().map(Effect::copy).toArray());
            }
        }
        
        default Multi and(Effect effect) {
            return new Multi(this, effect);
        }
        
        void apply(Glyph glyph, double delta);
        
        Effect copy();
    }
}
