// Christian Alexander, 12/11/2022
package kakkoiichris.kotoba.data.json.parser;

import kakkoiichris.kotoba.data.json.lexer.Location;

import java.util.Map;
import java.util.Optional;

public record Object(Location location, Map<String, Node> members) implements Node {
    public Node get(String name) {
        return members.get(name);
    }
    
    public Optional<Node> tryGet(String name) {
        return Optional.ofNullable(members.get(name));
    }
}
