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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NodeTemplatesValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    LSContext context;

    public NodeTemplatesValidator(LSContext context) {
        this.context = context;
    }
    
    public ArrayList<DiagnosticsSetter> validateNodeTemplates(Map<String, Object> nodeTemplatesMap, Map<String, Mark> positions, String yamlContent, String[] lines, String parent) {
        Set<String> validNodeTemplateKeywords = Set.of(
            "type", "description", "metadata", "directives", "properties", "attributes", "requirements", "capabilities", "interfaces", "artifacts", "count", "node_filter", "copy"
        );
        String nodeTemplatePath = parent + "." + "node_templates";
        for (String nodeTemplateKey : nodeTemplatesMap.keySet()) {
            Object nodeTemplate = nodeTemplatesMap.get(nodeTemplateKey);
            String nodeTemplatePathWithName = parent + "." + "node_templates" + "." + nodeTemplateKey;
            if (nodeTemplate instanceof Map) {
                validateRequiredKeys((Map<String, Object>) nodeTemplate,yamlContent, lines, nodeTemplatePathWithName);
                for (String key : ((Map<String, Object>) nodeTemplate).keySet()) {
                    if (!validNodeTemplateKeywords.contains(key)) {
                        Mark mark = context.getContextDependentConstructorPositions().get(nodeTemplatePathWithName + "." + key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);

                        handleNotValidKeywords("Invalid node template keyword: " + key, line, column, endColumn);
                    }
                    //Check if the type keyword exists, and contains existing node type
                    else if (key.equals("type")) {
                        validateType(yamlContent, lines, key, nodeTemplate, nodeTemplatePathWithName);
                    }
                    else if (key.equals("properties")) {
                        Object PropertyDefinitions = ((Map<?, ?>) nodeTemplate).get(key);
                        if (PropertyDefinitions instanceof Map) {
                            PropertyDefinitionValidator propertyDefinitionValidator = new PropertyDefinitionValidator(context);
                            ArrayList<DiagnosticsSetter> PropertyDefinitionDiagnostics;
                            PropertyDefinitionDiagnostics = propertyDefinitionValidator.validatePropertyDefinitions((Map<String, Object>) PropertyDefinitions, positions, yamlContent, lines, nodeTemplateKey, nodeTemplatePath, null);
                            diagnostics.addAll(PropertyDefinitionDiagnostics);
                        }
                    }
                }
            }
        }
        return diagnostics;
    }
    
    private void validateType(String yamlContent, String[] lines, String key, Object nodeTemplate, String nodeTemplatePathWithName) {
            // checks if it exists in the same file or not
            if (context.getCurrentToscaFile().nodeTypes().isPresent() && context.getCurrentToscaFile().nodeTypes().get().getValue().containsKey(((Map<String, Object>) nodeTemplate).get(key))) {
                return; //TODO construct the node template object with the found node type
            }
        validateTypeInImportedFiles(yamlContent, lines, key, nodeTemplate, nodeTemplatePathWithName);

    }

    private void validateTypeInImportedFiles(String yamlContent, String[] lines, String key, Object nodeTemplate, String nodeTemplatePathWithName) {
        try {
            if (!context.getCurrentToscaFile().imports().isEmpty()) {
                Collection<Map<String, TOSCAFile>> imports = context.getImportedToscaFiles().get(context.getCurrentToscaFilePath());
                for (Map<String, TOSCAFile> mapOfImportedFiles : imports) {
                    for (TOSCAFile file : mapOfImportedFiles.values()) {
                        if (file != null && !file.nodeTypes().isEmpty() && !file.nodeTypes().get().getValue().isEmpty() && file.nodeTypes().get().getValue().containsKey(((Map<String, Object>) nodeTemplate).get(key))) {
                            //TODO construct the node template object with the found node type
                            return;
                        }
                    }
                }
                    Collection<Map<String, TOSCAFile>> namespaces = context.getNamespaceDefinitions().get(context.getCurrentToscaFilePath());
                    for (Map<String, TOSCAFile> mapOfNamespaces : namespaces) {
                        for (String namespacesKey : mapOfNamespaces.keySet()) {
                            if ( ((Map<String, Object>) nodeTemplate).get(key) instanceof String) {
                                String[] parts = ((String) ((Map<String, Object>) nodeTemplate).get(key)).split(":");
                                if (parts.length == 2) {
                                    String typeWithoutNamespace = parts[1].trim();
                                    String namespace = parts[0].trim();
                                    if (namespacesKey.equals(namespace)) {
                                        TOSCAFile file = mapOfNamespaces.getOrDefault(namespace, null);
                                        if (file != null && !file.nodeTypes().get().getValue().isEmpty() && file.nodeTypes().get().getValue().containsKey(typeWithoutNamespace)) {
                                            return;
                                        }
                                    }
                                }
                            }

                        }
                    }
            }
                Mark mark = context.getContextDependentConstructorPositions().get(nodeTemplatePathWithName + "." + ((Map<?, ?>) nodeTemplate).get(key));
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

                handleNotValidKeywords("Invalid node type value, \"" + ((Map<?, ?>) nodeTemplate).get(key) + "\" is not exist.", line, column,endColumn);
        } catch (Exception e) {
            Mark mark = context.getContextDependentConstructorPositions().get(nodeTemplatePathWithName + "." + ((Map<?, ?>) nodeTemplate).get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

            handleNotValidKeywords(e.getMessage(), line, column,endColumn);
        }
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

    public void validateRequiredKeys(Map<String, Object> yamlMap, String content, String[] lines, String nodeTemplatePath) {
        if (!yamlMap.containsKey("type")) {
            Mark mark = context.getContextDependentConstructorPositions().get(nodeTemplatePath);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(content, line, column, lines);
            handleNotValidKeywords("Node template Missing required key: type ", line, column, endColumn);
        }
    }
}
