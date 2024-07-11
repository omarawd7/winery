/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *******************************************************************************/

package org.eclipse.winery.lsp.Server.ServerCore;

import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TOSCAFileValidator {
    public ArrayList<TOSCAFileDiagnostics> diagnostics = new ArrayList<>();

    public void validateRequiredKeys(Map<String, Object> yamlMap, Path path) {
        if (!yamlMap.containsKey("tosca_definitions_version")) {
            handleDiagnosticsError("Missing required key: tosca_definitions_version", path);
        }
    }

    public void validateRequiredKeys(Map<String, Object> yamlMap, String content) {
        if (!yamlMap.containsKey("tosca_definitions_version")) {
            handleDiagnosticsError("Missing required key: tosca_definitions_version", content);
        }
    }

    public void validateKeywords(Map<String, Object> yamlMap, Map<String, Mark> positions) {
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

    public void validateArtifactTypes(Map<String, Object> artifactTypesMap, Map<String, Mark> positions) {
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

    public void handleNotValidKeywords(String message, int line, int column) {
        TOSCAFileDiagnostics toscaFileDiagnostic = new TOSCAFileDiagnostics();
        toscaFileDiagnostic.setErrorMessage(message);
        toscaFileDiagnostic.setErrorContext("Not Valid Keywords");
        toscaFileDiagnostic.setErrorColumn(column);
        toscaFileDiagnostic.setErrorLine(line);
        diagnostics.add(toscaFileDiagnostic);
    }

    public void handleDiagnosticsError(String message, Path path) {
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

    public void handleDiagnosticsError(String message, String content) {
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

}
