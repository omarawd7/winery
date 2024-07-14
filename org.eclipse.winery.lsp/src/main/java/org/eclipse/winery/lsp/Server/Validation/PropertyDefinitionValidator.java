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

public class PropertyDefinitionValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    
    public ArrayList<DiagnosticsSetter> validatePropertyDefinition(Map<String, Object> PropertyDefinitionsMap, Map<String, Mark> positions, String YamlContent, String[] lines) {
        Set<String> validPropertyDefinitionKeywords = Set.of(
            "description", "metadata", "required", "Default", "value", "validationClause", "keySchema", "entrySchema"
        );
        for (String PropertyDefinitionKey : PropertyDefinitionsMap.keySet()) {
            Object propertyDefinition = PropertyDefinitionsMap.get(PropertyDefinitionKey);
            if (propertyDefinition instanceof Map) {
                for (String key : ((Map<String, Object>) propertyDefinition).keySet()) {
                    if (!validPropertyDefinitionKeywords.contains(key)) {
                        Mark mark = positions.get(key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid property definition keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
                    }
                }
            }
        }
        return diagnostics;
    }

    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter PropertyDefinitionDiagnostic = new DiagnosticsSetter();
        PropertyDefinitionDiagnostic.setErrorMessage(message);
        PropertyDefinitionDiagnostic.setErrorContext("Not Valid Keywords");
        PropertyDefinitionDiagnostic.setErrorColumn(column);
        PropertyDefinitionDiagnostic.setErrorEndColumn(endColumn);
        PropertyDefinitionDiagnostic.setErrorLine(line);
        diagnostics.add(PropertyDefinitionDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter PropertyDefinitionDiagnostic = new DiagnosticsSetter();
        PropertyDefinitionDiagnostic.setErrorMessage(message);
        PropertyDefinitionDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            PropertyDefinitionDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            PropertyDefinitionDiagnostic.setErrorLine(-1);
        }
        PropertyDefinitionDiagnostic.setErrorColumn(1);
        diagnostics.add(PropertyDefinitionDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter PropertyDefinitionDiagnostic = new DiagnosticsSetter();
        PropertyDefinitionDiagnostic.setErrorMessage(message);
        PropertyDefinitionDiagnostic.setErrorContext("Parsing Error");
        PropertyDefinitionDiagnostic.setErrorLine(countLines(content));
        PropertyDefinitionDiagnostic.setErrorColumn(1);
        diagnostics.add(PropertyDefinitionDiagnostic);
    }
    
    private int countLines(String content) {
        return (int) content.lines().count();
    }

}
