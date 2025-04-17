package kakkoiichris.kotoba.util;

import java.util.Optional;

public class Convert {
    public static Optional<Integer> toInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        }
        catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> toInt(String s, int radix) {
        try {
            return Optional.of(Integer.parseInt(s, radix));
        }
        catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Double> toDouble(String s) {
        try {
            return Optional.of(Double.parseDouble(s));
        }
        catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> toBoolean(String s) {
        if (s.equalsIgnoreCase("true")) {
            return Optional.of(true);
        }

        if (s.equalsIgnoreCase("false")) {
            return Optional.of(false);
        }

        return Optional.empty();
    }
}
