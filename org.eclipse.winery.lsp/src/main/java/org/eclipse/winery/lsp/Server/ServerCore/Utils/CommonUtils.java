package org.eclipse.winery.lsp.Server.ServerCore.Utils;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

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
    

}
