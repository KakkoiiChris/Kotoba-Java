// Christian Alexander, 4/27/2023
package kakkoiichris.kotoba;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QuickScript {
    private final List<String> source;
    
    public QuickScript(List<String> source) {
        this.source = source;
    }
    
    public Map<String, String> run(Console console) {
        var vars = new HashMap<String, String>();
        
        var newline = true;
        
        for (var line : source) {
            if (line.startsWith("@")) {
                var tokens = Arrays.stream(line.substring(1).split("\\s+")).toList();
                
                var command = tokens.get(0).toLowerCase();
                var args = tokens.subList(1, tokens.size());
                
                switch (command) {
                    case "clear" -> console.clear();
                    
                    case "color" -> {
                        switch (args.size()) {
                            case 1 -> {
                                var rgb = Util.toInt(args.get(0), 16).orElseThrow(() -> new RuntimeException("COLOR 1 FAIL"));
                                
                                console.setColor(rgb);
                            }
                            
                            case 3 -> {
                                var rgb = args.stream().map(s -> Util.toInt(s).orElseThrow(() -> new RuntimeException("COLOR 3 FAIL"))).toList();
                                
                                var r = rgb.get(0);
                                var g = rgb.get(1);
                                var b = rgb.get(2);
                                
                                console.setColor((r << 16) | (g << 8) | b);
                            }
                        }
                    }
                    
                    case "input" -> {
                        switch (args.size()) {
                            case 1 -> vars.put(args.get(0), console.readLine().orElseThrow(() -> new RuntimeException("INPUT 1 FAIL")));
                            
                            case 2 -> {
                                var name = args.get(0);
                                var prompt = args.get(1);
                                
                                console.write(prompt);
                                
                                vars.put(name, console.readLine().orElseThrow(() -> new RuntimeException("INPUT 2 FAIL")));
                                
                                console.newLine();
                            }
                        }
                    }
                    
                    case "invert" -> {
                        var b = Util.toBoolean(args.get(0)).orElseThrow(() -> new RuntimeException("INVERT FAIL"));
                        
                        console.setInverted(b);
                    }
                    
                    case "newline" -> newline = Util.toBoolean(args.get(0)).orElseThrow(() -> new RuntimeException("NEWLINE FAIL"));
                    
                    case "pause" -> {
                        switch (args.size()) {
                            case 0 -> console.pause();
                            
                            case 1 -> {
                                var b = Util.toDouble(args.get(0)).orElseThrow(() -> new RuntimeException("PAUSE 1 FAIL"));
                                
                                console.pause(b);
                            }
                        }
                    }
                    
                    case "rule" -> {
                        var name = args.get(0);
                        var regex = Pattern.compile(args.get(1));
                        var invert = Util.toBoolean(args.get(2)).orElseThrow(() -> new RuntimeException("RULE INVERT FAIL"));
                        
                        var rule = new Glyph.Rule(
                            name,
                            regex,
                            invert,
                            Glyph.Effect.None.get()
                        );
                        
                        console.addRules(rule);
                    }
                    
                    case "rule_color" -> {
                        var name = args.get(0);
                        var rgb = Util.toInt(args.get(1), 16).orElseThrow(() -> new RuntimeException("RULE_COLOR COLOR FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_COLOR NO RULE"));
                        
                        var effect = new Glyph.Effect.Color(rgb);
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_and_color" -> {
                        var name = args.get(0);
                        var rgb = Util.toInt(args.get(1), 16).orElseThrow(() -> new RuntimeException("RULE_AND_COLOR COLOR FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_AND_COLOR NO RULE"));
                        
                        var effect = rule.effect().and(new Glyph.Effect.Color(rgb));
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_cycle" -> {
                        var name = args.get(0);
                        var speed = Util.toDouble(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_CYCLE SPEED FAIL"));
                        
                        var colors = args
                            .stream()
                            .skip(2)
                            .mapToInt(color -> Util.toInt(color, 16).orElseThrow(() -> new RuntimeException("RULE_CYCLE COLORS NOT HEX")))
                            .toArray();
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_CYCLE NO RULE"));
                        
                        var effect = new Glyph.Effect.Cycle(speed, colors);
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_and_cycle" -> {
                        var name = args.get(0);
                        var speed = Util.toDouble(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_AND_CYCLE SPEED FAIL"));
                        
                        var colors = args
                            .stream()
                            .skip(2)
                            .mapToInt(color -> Util.toInt(color, 16).orElseThrow(() -> new RuntimeException("RULE_AND_CYCLE COLORS NOT HEX")))
                            .toArray();
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_AND_CYCLE NO RULE"));
                        
                        var effect = rule.effect().and(new Glyph.Effect.Cycle(speed, colors));
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_random" -> {
                        var name = args.get(0);
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_RANDOM NO RULE"));
                        
                        var effect = Glyph.Effect.Random.get();
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_and_random" -> {
                        var name = args.get(0);
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_AND_RANDOM NO RULE"));
                        
                        var effect = rule.effect().and(Glyph.Effect.Random.get());
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_jitter" -> {
                        var name = args.get(0);
                        var x = Util.toInt(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_JITTER X FAIL"));
                        var y = Util.toInt(args.get(2)).orElseThrow(() -> new RuntimeException("RULE_JITTER Y FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_JITTER NO RULE"));
                        
                        var effect = new Glyph.Effect.Jitter(x, y);
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_and_jitter" -> {
                        var name = args.get(0);
                        var x = Util.toInt(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_AND_JITTER X FAIL"));
                        var y = Util.toInt(args.get(2)).orElseThrow(() -> new RuntimeException("RULE_AND_JITTER Y FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_AND_JITTER NO RULE"));
                        
                        var effect = rule.effect().and(new Glyph.Effect.Jitter(x, y));
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_wave" -> {
                        var name = args.get(0);
                        var amplitude = Util.toDouble(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_WAVE AMPLITUDE FAIL"));
                        var frequency = Util.toDouble(args.get(2)).orElseThrow(() -> new RuntimeException("RULE_WAVE FREQUENCY FAIL"));
                        var speed = Util.toDouble(args.get(3)).orElseThrow(() -> new RuntimeException("RULE_WAVE SPEED FAIL"));
                        var vertical = Util.toBoolean(args.get(4)).orElseThrow(() -> new RuntimeException("RULE_WAVE VERTICAL FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_WAVE NO RULE"));
                        
                        var effect = new Glyph.Effect.Wave(amplitude, frequency, speed, vertical);
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_and_wave" -> {
                        var name = args.get(0);
                        var amplitude = Util.toDouble(args.get(1)).orElseThrow(() -> new RuntimeException("RULE_AND_WAVE AMPLITUDE FAIL"));
                        var frequency = Util.toDouble(args.get(2)).orElseThrow(() -> new RuntimeException("RULE_AND_WAVE FREQUENCY FAIL"));
                        var speed = Util.toDouble(args.get(3)).orElseThrow(() -> new RuntimeException("RULE_AND_WAVE SPEED FAIL"));
                        var vertical = Util.toBoolean(args.get(4)).orElseThrow(() -> new RuntimeException("RULE_AND_WAVE VERTICAL FAIL"));
                        
                        var rule = console.getRule(name).orElseThrow(() -> new RuntimeException("RULE_AND_WAVE NO RULE"));
                        
                        var effect = new Glyph.Effect.Wave(amplitude, frequency, speed, vertical);
                        
                        console.addRules(rule.withEffect(effect));
                    }
                    
                    case "rule_remove" -> console.removeRules(args.get(0));
                    
                    case "rules" -> {
                        var enabled = Util.toBoolean(args.get(0)).orElseThrow(() -> new RuntimeException("RULES FAIL"));
                        
                        console.setRulesEnabled(enabled);
                    }
                    
                    case "rules_clear" -> console.clearRules();
                }
            }
            else {
                var output = line;
                
                var pattern = Pattern.compile("\\{(\\w+)}");
                
                while (true) {
                    var matcher = pattern.matcher(output);
                    
                    if (!matcher.find()) break;
                    
                    var name = matcher.group(1);
                    
                    if (!vars.containsKey(name)) {
                        throw new RuntimeException("NO VAR FOR MATCHER");
                    }
                    
                    output = matcher.replaceFirst(vars.get(name));
                }
                
                var isNewline = newline;
                
                if (output.endsWith("\\") && !output.endsWith("\\\\")) {
                    output = output.substring(0, output.lastIndexOf('\\'));
                    
                    isNewline = true;
                }
                
                output = output.replace("\\\\", "\\");
                
                if (isNewline) {
                    console.writeLine(output);
                }
                else {
                    console.write(output);
                }
            }
        }
        
        return vars;
    }
}
