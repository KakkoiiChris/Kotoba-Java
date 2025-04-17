// Christian Alexander, 4/14/2023
package kakkoiichris.kotoba.data;

import kakkoiichris.kotoba.Font;
import kakkoiichris.kotoba.QuickScript;
import kakkoiichris.kotoba.data.json.Json;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourceManager {
    private final Folder root;
    private Folder current;

    public ResourceManager(String rootPath) {
        root = new Folder(rootPath);

        current = root;
    }

    public Font getFont(String name) {
        return current.getFont(name);
    }

    public CSV getCSV(String name) {
        return current.getCSV(name);
    }

    public Json<?> getJSON(String name) {
        return current.getJSON(name);
    }

    public QuickScript getQuickScript(String name) {
        return current.getQuickScript(name);
    }

    public TXT getTXT(String name) {
        return current.getTXT(name);
    }

    public XML getXML(String name) {
        return current.getXML(name);
    }

    public Folder getFolder(String name) {
        return current.getFolder(name);
    }

    public boolean goBack() {
        if (current.parent == null) {
            return false;
        }

        current = current.parent;

        return true;
    }

    public void goTo(String name) {
        current = current.getFolder(name);
    }

    public void goToTop() {
        current = root;
    }

    public static class Folder {
        private final String path;
        private final Folder parent;

        private final Map<String, Font> fonts = new HashMap<>();
        private final Map<String, CSV> csvFiles = new HashMap<>();
        private final Map<String, Json<?>> jsonFiles = new HashMap<>();
        private final Map<String, QuickScript> kqFiles = new HashMap<>();
        private final Map<String, TXT> txtFiles = new HashMap<>();
        private final Map<String, XML> xmlFiles = new HashMap<>();
        private final Map<String, Folder> subFolders = new HashMap<>();

        public Folder(String path, Folder parent) {
            this.path = path;
            this.parent = parent;

            try {
                var resource = getClass().getResource(path);

                var directory = new File(Objects.requireNonNull(resource).toURI());

                var files = List.of(Objects.requireNonNull(directory.listFiles()));

                files.stream().filter(File::isFile).forEach(file -> {
                    var name = file.getName();

                    var resourceName = name.substring(0, name.indexOf('.'));
                    var resourceExtension = name.substring(name.indexOf('.') + 1).toLowerCase();
                    var resourcePath = "%s/%s".formatted(path, name);

                    switch (resourceExtension) {
                        case "bff" -> fonts.put(resourceName, new Font(resourcePath));

                        case "csv" -> csvFiles.put(resourceName, new CSV(resourcePath) {{
                            readResource();
                        }});

                        //case "json" -> jsonFiles.put(resourceName, new JSON(resourcePath) {{readResource();}});

                        case "kq" -> kqFiles.put(resourceName, new QuickScript(new TXT(resourcePath) {{
                            readResource();
                        }}.getLines()));

                        case "txt" -> txtFiles.put(resourceName, new TXT(resourcePath) {{
                            readResource();
                        }});

                        case "xml" -> xmlFiles.put(resourceName, new XML(resourcePath) {{
                            readResource();
                        }});
                    }
                });

                files.stream().filter(File::isDirectory).forEach(file -> {
                    var name = file.getName();

                    var resourcePath = "%s/%s".formatted(path, name);

                    subFolders.put(file.getName(), new Folder(resourcePath, this));
                });
            }
            catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public Folder(String path) {
            this(path, null);
        }

        public Font getFont(String name) {
            if (!fonts.containsKey(name)) {
                throw new RuntimeException("Font '%s/%s' does not exist!".formatted(path, name));
            }

            return fonts.get(name);
        }

        public CSV getCSV(String name) {
            if (!csvFiles.containsKey(name)) {
                throw new RuntimeException("CSV file '%s/%s' does not exist!".formatted(path, name));
            }

            return csvFiles.get(name);
        }

        public Json<?> getJSON(String name) {
            if (!jsonFiles.containsKey(name)) {
                throw new RuntimeException("JSON file '%s/%s' does not exist!".formatted(path, name));
            }

            return jsonFiles.get(name);
        }

        public QuickScript getQuickScript(String name) {
            if (!kqFiles.containsKey(name)) {
                throw new RuntimeException("QuickScript file '%s/%s' does not exist!".formatted(path, name));
            }

            return kqFiles.get(name);
        }

        public TXT getTXT(String name) {
            if (!txtFiles.containsKey(name)) {
                throw new RuntimeException("TXT file '%s/%s' does not exist!".formatted(path, name));
            }

            return txtFiles.get(name);
        }

        public XML getXML(String name) {
            if (!xmlFiles.containsKey(name)) {
                throw new RuntimeException("XML file '%s/%s' does not exist!".formatted(path, name));
            }

            return xmlFiles.get(name);
        }

        public Folder getFolder(String name) {
            if (!subFolders.containsKey(name)) {
                throw new RuntimeException("Folder '%s/%s' does not exist!".formatted(path, name));
            }

            return subFolders.get(name);
        }
    }

    public interface Resource {

    }
}
