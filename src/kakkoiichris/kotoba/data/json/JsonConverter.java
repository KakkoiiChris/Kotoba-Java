// Christian Alexander, 12/10/2022
package kakkoiichris.kotoba.data.json;

import kakkoiichris.kotoba.data.json.parser.Object;

public interface JsonConverter<X> {
    X load(Object object);
    
    Object save(X x);
}
