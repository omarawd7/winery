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

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.CapabilityDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.CapabilityType;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NodeTypeValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    LSContext context;
    
    public NodeTypeValidator(LSContext context) {
    this.context = context;
    }

    public ArrayList<DiagnosticsSetter> validateNodeTypes(Map<String, Object> nodeTypesMap, Map<String, Mark> positions, String yamlContent, String[] lines) {
        Set<String> validNodeTypeKeywords = Set.of(
            "derived_from", "version", "metadata", "description", "properties", "attributes", "capabilities", "requirements","interfaces", "artifacts"
        );
        for (String nodeTypeKey : nodeTypesMap.keySet()) {
            Object nodeType = nodeTypesMap.get(nodeTypeKey);
            String nodeTypePath = "node_types" + "." + nodeTypeKey;
            if (nodeType instanceof Map) {
                for (String key : ((Map<String, Object>) nodeType).keySet()) {
                    if (!validNodeTypeKeywords.contains(key)) {
                        Mark mark = context.getContextDependentConstructorPositions().get(nodeTypePath + "." + key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid node type keyword: " + key , line, column, endColumn);
                    }
                    //Check if the derived_from keyword exists, that it contains a valid node type parent
                    else if (key.equals("derived_from") && !nodeTypesMap.containsKey(((Map<?, ?>) nodeType).get(key))) {
                        Mark mark = context.getContextDependentConstructorPositions().get(nodeTypePath + "." + ((Map<?, ?>) nodeType).get(key));
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid derived_from value, \"" + ((Map<?, ?>) nodeType).get(key) + "\" is not a parent type ", line, column,endColumn);
                    } else if (key.equals("properties")) {
                        Object PropertyDefinitions = ((Map<?, ?>) nodeType).get(key);
                        if (PropertyDefinitions instanceof Map) {
                            PropertyDefinitionValidator propertyDefinitionValidator = new PropertyDefinitionValidator(context);
                            ArrayList<DiagnosticsSetter> PropertyDefinitionDiagnostics;
                            if (((Map<?, ?>) nodeType).containsKey("derived_from")) {
                                PropertyDefinitionDiagnostics = propertyDefinitionValidator.validatePropertyDefinitions((Map<String, Object>) PropertyDefinitions, positions, yamlContent, lines, nodeTypeKey, "node_types", (String) ((Map<?, ?>) nodeType).get("derived_from"));
                            } else {
                                PropertyDefinitionDiagnostics = propertyDefinitionValidator.validatePropertyDefinitions((Map<String, Object>) PropertyDefinitions, positions, yamlContent, lines, nodeTypeKey, "node_types", null);
                            }
                            diagnostics.addAll(PropertyDefinitionDiagnostics);
                        }
                    } else if (key.equals("capabilities")) {
                        Object capabilityDefinitions = ((Map<?, ?>) nodeType).get(key);
                        if (capabilityDefinitions instanceof Map) {
                            CapabilityDefinitionValidator capabilityDefinitionValidator = new CapabilityDefinitionValidator(context);
                            ArrayList<DiagnosticsSetter> capabilityDefinitionDiagnostics;
                            capabilityDefinitionDiagnostics = capabilityDefinitionValidator.validateCapabilityDefinitions((Map<String, Object>) capabilityDefinitions, yamlContent, lines, nodeTypePath + "." + "capabilities", nodeTypeKey );
                            diagnostics.addAll(capabilityDefinitionDiagnostics);
                        } else if (capabilityDefinitions instanceof String capabilityType) {
                            if (!context.getCurrentToscaFile().capabilityTypes().isEmpty() && context.getCurrentToscaFile().capabilityTypes().get().containsKey(capabilityType)) {
                                // will name the capability definition the same as the provided capability name.
                                CapabilityType capabilityTypeObject = context.getCurrentToscaFile().capabilityTypes().get().get(capabilityType);
                                if (context.getCurrentToscaFile().nodeTypes().isPresent() && context.getCurrentToscaFile().nodeTypes().get().getValue().containsKey(nodeTypeKey) && context.getCurrentToscaFile().nodeTypes().get().getValue().get(nodeTypeKey).capabilities().isPresent()) {
                                    context.getCurrentToscaFile().nodeTypes().get().getValue().get(nodeTypeKey).capabilities().get().getValue().put(capabilityType, new CapabilityDefinition(capabilityTypeObject, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())) ;
                                }
                            } //TODO check if it exists in another file
                            else {
                                Mark mark = context.getContextDependentConstructorPositions().get(nodeTypePath + "." + ((Map<?, ?>) nodeType).get(key));
                                int line = mark != null ? mark.getLine() + 1 : -1;
                                int column = mark != null ? mark.getColumn() + 1 : -1;
                                int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

                                handleNotValidKeywords("Invalid capability: " + ((Map<?, ?>) nodeType).get(key), line, column,endColumn);
                            }
                        } else {
                            Mark mark = context.getContextDependentConstructorPositions().get(nodeTypePath + "." + ((Map<?, ?>) nodeType).get(key));
                            int line = mark != null ? mark.getLine() + 1 : -1;
                            int column = mark != null ? mark.getColumn() + 1 : -1;
                            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

                            handleNotValidKeywords("Invalid capability: " + ((Map<?, ?>) nodeType).get(key), line, column,endColumn);
                        }
                    }
            }
        }
    }
        return diagnostics;
    }

    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter nodeTypeDiagnostic = new DiagnosticsSetter();
        nodeTypeDiagnostic.setErrorMessage(message);
        nodeTypeDiagnostic.setErrorContext("Not Valid Keywords");
        nodeTypeDiagnostic.setErrorColumn(column);
        nodeTypeDiagnostic.setErrorEndColumn(endColumn);
        nodeTypeDiagnostic.setErrorLine(line);
        diagnostics.add(nodeTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter nodeTypeDiagnostic = new DiagnosticsSetter();
        nodeTypeDiagnostic.setErrorMessage(message);
        nodeTypeDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            nodeTypeDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            nodeTypeDiagnostic.setErrorLine(-1);
        }
        nodeTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(nodeTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter nodeTypeDiagnostic = new DiagnosticsSetter();
        nodeTypeDiagnostic.setErrorMessage(message);
        nodeTypeDiagnostic.setErrorContext("Parsing Error");
        nodeTypeDiagnostic.setErrorLine(countLines(content));
        nodeTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(nodeTypeDiagnostic);
    }

    private int countLines(String content) {
        return (int) content.lines().count();
    }

}
