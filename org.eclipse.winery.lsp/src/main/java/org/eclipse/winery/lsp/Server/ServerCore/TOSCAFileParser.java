package org.eclipse.winery.lsp.Server.ServerCore;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TOSCAFileParser implements Parser {
    public ArrayList<TOSCAFileDiagnostics> diagnostics = new ArrayList<>();

    @Override
    public TOSCAFile ParseTOSCAFile(Path path) {
        try {
            String yamlContent = Files.readString(path);
            LoaderOptions options = new LoaderOptions();
            ToscaFileConstructor constructor = new ToscaFileConstructor();
            Yaml yaml = new Yaml(constructor);
            Map<String, Object> yamlMap = yaml.load(yamlContent);
            // Validate required keywords
            validateRequiredKeys(yamlMap, path);
            // Validate keywords and capture their positions
            validateKeywords(yamlMap, constructor.getPositions());
            return yaml.loadAs(yamlContent, TOSCAFile.class);
        } catch (IOException e) {
            handleDiagnosticsError("Failed to read TOSCA file: " + e.getMessage(), path);
        } catch (YAMLException e) {
            handleDiagnosticsError("Failed to parse TOSCA file: " + e.getMessage(), path);
        } catch (IllegalArgumentException e) {
            handleDiagnosticsError("Validation failed: " + e.getMessage(), path);
        }
        return null;
    }

    @Override
    public TOSCAFile ParseTOSCAFile(String content) {
        try {
            LoaderOptions options = new LoaderOptions();
            ToscaFileConstructor constructor = new ToscaFileConstructor();
            Yaml yaml = new Yaml(constructor);
            Map<String, Object> yamlMap = yaml.load(content);
            // Validate required keywords
            validateRequiredKeys(yamlMap, content);
            // Validate keywords and capture their positions
            validateKeywords(yamlMap, constructor.getPositions());
            return yaml.loadAs(content, TOSCAFile.class);
        } catch (YAMLException e) {
            handleDiagnosticsError("Failed to parse TOSCA file: " + e.getMessage(), content);
        } catch (IllegalArgumentException e) {
            handleDiagnosticsError("Validation failed: " + e.getMessage(), content);
        }
        return null;
    }

    private void validateRequiredKeys(Map<String, Object> yamlMap, Path path) {
        if (!yamlMap.containsKey("tosca_definitions_version")) {
            handleDiagnosticsError("Missing required key: tosca_definitions_version", path);
        }
    }

    private void validateRequiredKeys(Map<String, Object> yamlMap, String content) {
        if (!yamlMap.containsKey("tosca_definitions_version")) {
            handleDiagnosticsError("Missing required key: tosca_definitions_version", content);
        }
    }

    private void validateKeywords(Map<String, Object> yamlMap, Map<String, Mark> positions) {
        Set<String> validKeywords = Set.of(
            "tosca_definitions_version", "description", "metadata", "dsl_definitions",
            "artifact_types", "data_types", "capability_types", "interface_types",
            "relationship_types", "node_types", "group_types", "policy_types",
            "repositories", "functions", "profile", "imports", "service_template"
        );
        for (String key : yamlMap.keySet()) {
            if (!validKeywords.contains(key)) {
                Mark mark = positions.get(key);
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                handleNotValidKeywords("Invalid keyword: " + key + " at line " + line + ", column " + column, line, column);
            } else if (key.equals("artifact_types")) {
                Object artifactTypes = yamlMap.get(key);
                if (artifactTypes instanceof Map) {
                    validateArtifactTypes((Map<String, Object>) artifactTypes, positions);
                }
            }
        }
    }

    private void validateArtifactTypes(Map<String, Object> artifactTypesMap, Map<String, Mark> positions) {
        Set<String> validArtifactTypeKeywords = Set.of(
            "derived_from", "version", "metadata", "description", "mime_type", "file_ext", "properties"
        );
        for (String artifactTypeKey : artifactTypesMap.keySet()) {
            Object artifactType = artifactTypesMap.get(artifactTypeKey);
            if (artifactType instanceof Map) {
                for (String key : ((Map<String, Object>) artifactType).keySet()) {
                    if (!validArtifactTypeKeywords.contains(key)) {
                        Mark mark = positions.get(key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        handleNotValidKeywords("Invalid artifact type keyword: " + key + " at line " + line + ", column " + column, line, column);
                    }
                }
            }
        }
    }

    private void handleNotValidKeywords(String message, int line, int column) {
        TOSCAFileDiagnostics toscaFileDiagnostic = new TOSCAFileDiagnostics();
        toscaFileDiagnostic.setErrorMessage(message);
        toscaFileDiagnostic.setErrorContext("Not Valid Keywords");
        toscaFileDiagnostic.setErrorColumn(column);
        toscaFileDiagnostic.setErrorLine(line);
        diagnostics.add(toscaFileDiagnostic);
    }

    private void handleDiagnosticsError(String message, Path path) {
        TOSCAFileDiagnostics toscaFileDiagnostic = new TOSCAFileDiagnostics();
        toscaFileDiagnostic.setErrorMessage(message);
        toscaFileDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            toscaFileDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            toscaFileDiagnostic.setErrorLine(-1);
        }
        toscaFileDiagnostic.setErrorColumn(1);
        diagnostics.add(toscaFileDiagnostic);
    }

    private void handleDiagnosticsError(String message, String content) {
        TOSCAFileDiagnostics toscaFileDiagnostic = new TOSCAFileDiagnostics();
        toscaFileDiagnostic.setErrorMessage(message);
        toscaFileDiagnostic.setErrorContext("Parsing Error");
        toscaFileDiagnostic.setErrorLine(countLines(content));
        toscaFileDiagnostic.setErrorColumn(1);
        diagnostics.add(toscaFileDiagnostic);
    }

    private int countLines(String content) {
        return (int) content.lines().count();
    }

    public static void main(String[] args) {
        TOSCAFileParser parser = new TOSCAFileParser();
        Path TOSCAFilePath = Path.of("C:/Users/LAPTOP/Documents/GitHub/winery/org.eclipse.winery.lsp/src/test/resources/ToscaFileTest.yaml");
        TOSCAFile toscafile = parser.ParseTOSCAFile(TOSCAFilePath);
        System.out.println("artifact_types: " + toscafile.artifact_types());
        ArrayList<TOSCAFileDiagnostics> diagnostics = parser.diagnostics;
        for (TOSCAFileDiagnostics diagnostic : diagnostics) {
            System.out.println(diagnostic.getErrorContext());
            System.out.println(diagnostic.getErrorMessage());
            System.out.println(diagnostic.getErrorLine());
        }
    }
}
