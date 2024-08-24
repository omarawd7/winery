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
