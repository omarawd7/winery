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

public class CapabilityTypeValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    private LSContext context;

    public CapabilityTypeValidator(LSContext context) {
        this.context = context;
    }

    public ArrayList<DiagnosticsSetter> validateCapabilityTypes(Map<String, Object> capabilityTypesMap, Map<String, Mark> positions, String YamlContent, String[] lines) {
        Set<String> validCapabilityTypeKeywords = Set.of(
            "derived_from", "version", "metadata", "description", "valid_source_node_types", "valid_relationship_types", "properties", "attributes"
        );
        for (String capabilityTypeKey : capabilityTypesMap.keySet()) {
            Object capabilityType = capabilityTypesMap.get(capabilityTypeKey);
            if (capabilityType instanceof Map) {
                for (String key : ((Map<String, Object>) capabilityType).keySet()) {
                    if (!validCapabilityTypeKeywords.contains(key)) {
                        Mark mark = context.getContextDependentConstructorPositions().get("capability_types" + "." + capabilityTypeKey + "." + key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid capability type keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
                    }
                    //Check if the derived_from keyword exists, that it contains a valid capability type parent
                    else if (key.equals("derived_from") && !capabilityTypesMap.containsKey(((Map<?, ?>) capabilityType).get(key))) {
                        Mark mark = context.getContextDependentConstructorPositions().get("capability_types" + "." + capabilityTypeKey + "." + ((Map<?, ?>) capabilityType).get(key));
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid derived_from value, \"" + ((Map<?, ?>) capabilityType).get(key) + "\" is not a parent type ", line, column,endColumn);
                    } else if (key.equals("properties")) {
                        Object PropertyDefinitions = ((Map<?, ?>) capabilityType).get(key);
                        if (PropertyDefinitions instanceof Map) {
                            PropertyDefinitionValidator propertyDefinitionValidator = new PropertyDefinitionValidator(context);
                            ArrayList<DiagnosticsSetter> PropertyDefinitionDiagnostics;
                            if (((Map<?, ?>) capabilityType).containsKey("derived_from")) {
                                PropertyDefinitionDiagnostics = propertyDefinitionValidator.validatePropertyDefinitions((Map<String, Object>) PropertyDefinitions, positions, YamlContent, lines, capabilityTypeKey, "capability_types", (String) ((Map<?, ?>) capabilityType).get("derived_from"));
                            } else {
                                PropertyDefinitionDiagnostics = propertyDefinitionValidator.validatePropertyDefinitions((Map<String, Object>) PropertyDefinitions, positions, YamlContent, lines, capabilityTypeKey, "capability_types",null);
                            }
                            diagnostics.addAll(PropertyDefinitionDiagnostics);
                        }
                    }
                }
            }
        }
        return diagnostics;
    }

    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter capabilityTypeDiagnostic = new DiagnosticsSetter();
        capabilityTypeDiagnostic.setErrorMessage(message);
        capabilityTypeDiagnostic.setErrorContext("Not Valid Keywords");
        capabilityTypeDiagnostic.setErrorColumn(column);
        capabilityTypeDiagnostic.setErrorEndColumn(endColumn);
        capabilityTypeDiagnostic.setErrorLine(line);
        diagnostics.add(capabilityTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter capabilityTypeDiagnostic = new DiagnosticsSetter();
        capabilityTypeDiagnostic.setErrorMessage(message);
        capabilityTypeDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            capabilityTypeDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            capabilityTypeDiagnostic.setErrorLine(-1);
        }
        capabilityTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(capabilityTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter capabilityTypeDiagnostic = new DiagnosticsSetter();
        capabilityTypeDiagnostic.setErrorMessage(message);
        capabilityTypeDiagnostic.setErrorContext("Parsing Error");
        capabilityTypeDiagnostic.setErrorLine(countLines(content));
        capabilityTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(capabilityTypeDiagnostic);
    }

    private int countLines(String content) {
        return (int) content.lines().count();
    }

}

