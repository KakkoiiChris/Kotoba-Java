// Christian Alexander, 12/11/2022
package kakkoiichris.kotoba.data.json.parser;


import kakkoiichris.kotoba.data.json.lexer.Location;

import java.util.List;

public record Array(Location location, List<Node> elements) implements Node {
    public Node get(int index) {
        return elements.get(index);
    }
}
