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

package org.eclipse.winery.lsp.Server.Validation;

import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ArtifactTypeValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();

    public ArrayList<DiagnosticsSetter> validateArtifactTypes(Map<String, Object> artifactTypesMap, Map<String, Mark> positions, String YamlContent, String[] lines) {
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
                        int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid artifact type keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
                    }
                    //Check if the derived_from keyword exists, that it contains a valid Artifact type parent
                    else if (key.equals("derived_from") && !artifactTypesMap.containsKey(((Map<?, ?>) artifactType).get(key))) {
                        Mark mark = positions.get(((Map<?, ?>) artifactType).get(key));
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid derived_from value " + key + " at line " + line + ", column " + column, line, column,endColumn);
                    }
                }
            }
        }
        return diagnostics;
    }
    
    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter ArtifactTypeDiagnostic = new DiagnosticsSetter();
        ArtifactTypeDiagnostic.setErrorMessage(message);
        ArtifactTypeDiagnostic.setErrorContext("Not Valid Keywords");
        ArtifactTypeDiagnostic.setErrorColumn(column);
        ArtifactTypeDiagnostic.setErrorEndColumn(endColumn);
        ArtifactTypeDiagnostic.setErrorLine(line);
        diagnostics.add(ArtifactTypeDiagnostic);
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
