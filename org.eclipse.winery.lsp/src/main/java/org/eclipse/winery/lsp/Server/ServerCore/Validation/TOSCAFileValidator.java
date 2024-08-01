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
package org.eclipse.winery.lsp.Server.ServerCore.Validation;

import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TOSCAFileValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    LSContext context;
    
    public void validate(Map<String, Object> yamlMap, LSContext context, String content, Map<String, Mark> positions ) {
        this.context = context;
        // Validate required keywords
        validateRequiredKeys(yamlMap, content);
        // Validate keywords and capture their positions
        validateKeywords(yamlMap, positions, content);
    }

    public void validateRequiredKeys(Map<String, Object> yamlMap, String content) {
        if (!yamlMap.containsKey("tosca_definitions_version")) {
            handleDiagnosticsError("Missing required key: tosca_definitions_version", content);
        }
    }

    public void validateKeywords(Map<String, Object> yamlMap, Map<String, Mark> positions, String YamlContent) {
        Set<String> validKeywords = Set.of(
            "tosca_definitions_version", "description", "metadata", "dsl_definitions",
            "artifact_types", "data_types", "capability_types", "interface_types",
            "relationship_types", "node_types", "group_types", "policy_types",
            "repositories", "functions", "profile", "imports", "service_template"
        );
        // Split the content into lines
        String[] lines = YamlContent.split("\n");
        for (String key : yamlMap.keySet()) {
            if (!validKeywords.contains(key)) {
                Mark mark = positions.get(key);
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumn("", line, column, lines);
                handleNotValidKeywords("Invalid keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
            } else if (key.equals("artifact_types")) {
                ValidateArtifactTypes(yamlMap, positions, YamlContent, key, lines);
            }
        }
        
    }

    private void ValidateArtifactTypes(Map<String, Object> yamlMap, Map<String, Mark> positions, String YamlContent, String key, String[] lines) {
        Object artifactTypes = yamlMap.get(key);
        if (artifactTypes instanceof Map) {
            ArtifactTypeValidator artifactTypeValidator = new ArtifactTypeValidator(context);
            ArrayList<DiagnosticsSetter> ArtifactTypeDiagnostics = artifactTypeValidator.validateArtifactTypes((Map<String, Object>) artifactTypes, positions, YamlContent, lines);
            diagnostics.addAll(ArtifactTypeDiagnostics);
        }
    }

    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter toscaFileDiagnostic = new DiagnosticsSetter();
        toscaFileDiagnostic.setErrorMessage(message);
        toscaFileDiagnostic.setErrorContext("Not Valid Keywords");
        toscaFileDiagnostic.setErrorColumn(column);
        toscaFileDiagnostic.setErrorEndColumn(endColumn);
        toscaFileDiagnostic.setErrorLine(line);
        diagnostics.add(toscaFileDiagnostic);
    }
    
    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter toscaFileDiagnostic = new DiagnosticsSetter();
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
    
    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter toscaFileDiagnostic = new DiagnosticsSetter();
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
