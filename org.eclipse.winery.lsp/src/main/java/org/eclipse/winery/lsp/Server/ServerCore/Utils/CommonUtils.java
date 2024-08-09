package org.eclipse.winery.lsp.Server.ServerCore.Utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CommonUtils {
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
        switch (type.toLowerCase()) {
            case "integer":
                return isInteger(value);
            case "string":
                return isString(value);
            case "map":
                return isMap(value);
            case "list":
                return isList(value);
            case "float":
                return isFloat(value);
            case "boolean":
                return isBoolean(value);
                //TODO support more types
            default:
                return false;
        }
    }

    private static boolean isInteger(Object value) {
        try {
            Integer.parseInt(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isString(Object value) {
        return value instanceof String && ((String) value).trim().startsWith("\"") && ((String) value).trim().endsWith("\"");
    }

    private static boolean isMap(Object value) {
        return value instanceof Map;
    }

    private static boolean isList(Object value) {
        return value instanceof List;
    }

    private static boolean isFloat(Object value) {
        try {
            Float.parseFloat(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(Object value) {
        return "true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString());
    }
    
   public static Boolean isFunction(String line) {
        line = line.trim();
        if (line.startsWith("{") && line.endsWith("}")) {
            return true;
        }
        throw new IllegalArgumentException("Malformed input: missing curly brackets");
    }

   public static Boolean isFunctionCall(String line) {
        line = line.trim();
        line = line.substring(0, line.length() - 1).trim();
        if (line.startsWith("$")) {
            // It's a function call return true scape if false
            return line.length() <= 1 || line.charAt(1) != '$';
        }
        return false;
    }
    
    public static boolean isToscaFile(Path path) {
        String fileName = path.toString();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml") || fileName.endsWith(".tosca");
    }
}
