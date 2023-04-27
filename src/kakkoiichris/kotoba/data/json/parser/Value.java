// Christian Alexander, 12/11/2022
package kakkoiichris.kotoba.data.json.parser;

import kakkoiichris.kotoba.data.json.lexer.Location;

public record Value(Location location, java.lang.Object value) implements Node {
}