package org.eclipse.winery.lsp.Server.ServerCore.Utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtils {
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String MD_LINE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    private CommonUtils() {
    }

    public static Path uriToPath(String pathUri) {
        URI uri = URI.create(pathUri);
        return Paths.get(uri);
    }

    public static boolean isKeyword(String name) {
        return name.equals("int") || name.equals("string");
    }
    
    public static int getEndColumn(String YamlContent, int line, int column, String[] lines) {
        int endColumn = -1;

        if (line != -1 && column != -1) {
            if (line - 1 < lines.length) {
                String lineContent = lines[line - 1];
                // Find the colon after the column index
                endColumn = lineContent.indexOf(":", column) - 1;
            }
        }
        return endColumn;
    }
  
    public static int getEndColumnForValueError(String YamlContent, int line, int column, String[] lines) {
        if (line < 0 || line >= lines.length) {
            return column;
        }
        return lines[line - 1].length();
    }

    public static boolean isTypeMatch(String type, Object value) {
        return switch (type) {
            case "string" -> value instanceof String;
            case "int", "integer" -> value instanceof Integer;
            case "float" -> value instanceof Float;
            case "boolean" -> value instanceof Boolean;
            //TODO support more types
            default -> false;
        };
    }

    public static boolean isToscaFile(Path path) {
        String fileName = path.toString();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml") || fileName.endsWith(".tosca");
    }
}
