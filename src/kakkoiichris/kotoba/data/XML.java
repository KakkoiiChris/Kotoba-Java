// Christian Alexander, 4/14/2023
package kakkoiichris.kotoba.data;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;

public class XML {
    private final String filePath;
    
    private Document document;
    
    public XML(String filePath) {
        this.filePath = filePath;
    }
    
    public boolean readResource() {
        try {
            var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            document = builder.parse(getClass().getResourceAsStream(filePath));
            
            document.normalize();
        }
        catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    public boolean read() {
        try {
            var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            document = builder.parse(new FileInputStream(filePath));
            
            document.normalize();
        }
        catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    public boolean write() {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();
            
            var source = new DOMSource(document);
            
            var result = new StreamResult(new File(filePath));
            
            transformer.transform(source, result);
        }
        catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
        
        return true;
    }
}
