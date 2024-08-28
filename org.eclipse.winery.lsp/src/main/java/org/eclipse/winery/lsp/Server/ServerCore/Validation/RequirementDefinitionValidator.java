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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RequirementDefinitionValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    LSContext context;

    public RequirementDefinitionValidator(LSContext context) {
        this.context = context;
    }
    
    public ArrayList<DiagnosticsSetter> validateRequirementDefinitions(List<?> requirementDefinitions, String yamlContent, String[] lines, String requirementDefinitionPath, String parent) {
        Set<String> validRequirementDefinitionKeywords = Set.of(
            "description", "metadata", "relationship", "node", "capability", "node_filter", "count_range");
        for (Object requirementDefinitionElement: requirementDefinitions) {
            if (requirementDefinitionElement instanceof Map) {
                for (String requirementDefinition : ((Map<String, Object>) requirementDefinitionElement).keySet()) {
                    if (((Map<String, Object>) requirementDefinitionElement).get(requirementDefinition) instanceof Map) {
                        requirementDefinitionPath += "." + requirementDefinition;
                        validateRequiredKeys((Map<String, Object>) ((Map<String, Object>) requirementDefinitionElement).get(requirementDefinition),yamlContent, lines, requirementDefinitionPath);
                     for (String key : ((Map<String, Object>) ((Map<String, Object>) requirementDefinitionElement).get(requirementDefinition)).keySet()) {
                         if (!validRequirementDefinitionKeywords.contains(key)) {
                             Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPath + "." + key);
                             int line = mark != null ? mark.getLine() + 1 : -1;
                             int column = mark != null ? mark.getColumn() + 1 : -1;
                             int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                             handleNotValidKeywords("Invalid requirement definition keyword: " + requirementDefinition + " at line " + line + ", column " + column, line, column, endColumn);
                         } else if (key.equals("capability")) {
                             if (requirementDefinitionPath.contains("node_types")) {
                                 validateCapabilityFromNodeTypeParent(yamlContent, lines, key, (Map<String, Object>) ((Map<String, Object>) requirementDefinitionElement).get(requirementDefinition), requirementDefinitionPath, parent, requirementDefinition);
                             }
                         } else if (key.equals("relationship")) {
                             if (requirementDefinitionPath.contains("node_types")) {
                                 validateRelationshipFromNodeTypeParent(yamlContent, lines, key, (Map<String, Object>) ((Map<String, Object>) requirementDefinitionElement).get(requirementDefinition), requirementDefinitionPath, parent, requirementDefinition);
                             }
                         }
                     }   
                    } else {
                        //TODO handle refinements
                    }
                    
                }
            }
        }
        return diagnostics;
    }

    private void validateRelationshipFromNodeTypeParent(String yamlContent, String[] lines, String key, Map<String, Object> requirementDefinition, String requirementDefinitionPathWithName, String parent, String requirementDefinitionsKey) {
        try {
            if (context.getCurrentToscaFile().relationshipTypes().isPresent() && context.getCurrentToscaFile().relationshipTypes().get().getValue().containsKey((requirementDefinition).get(key))) {
                //TODO set the found relationship type for the requirement
                return;
            }
            if ( !context.getCurrentToscaFile().imports().isEmpty()) {
                Collection<Map<String, TOSCAFile>> imports = context.getImportedToscaFiles().get(context.getCurrentToscaFilePath());
                for (Map<String, TOSCAFile> mapOfImportedFiles : imports) {
                    for (TOSCAFile file : mapOfImportedFiles.values()) {
                        if (file != null && !file.relationshipTypes().isEmpty() && file.relationshipTypes().get().getValue().containsKey((requirementDefinition).get(key))) {
                            //TODO set the found relationship type for the requirement
                            return;
                        }
                    }
                }
                Collection<Map<String, TOSCAFile>> namespaces = context.getNamespaceDefinitions().get(context.getCurrentToscaFilePath());
                for (Map<String, TOSCAFile> mapOfNamespaces : namespaces) {
                    for (String namespacesKey : mapOfNamespaces.keySet()) {
                        if ( (requirementDefinition).get(key) instanceof String) {
                            String[] parts = ((String) (requirementDefinition).get(key)).split(":");
                            if (parts.length == 2) {
                                String typeWithoutNamespace = parts[1].trim();
                                String namespace = parts[0].trim();
                                if (namespacesKey.equals(namespace)) {
                                    TOSCAFile file = mapOfNamespaces.getOrDefault(namespace, null);
                                    if (file != null && !file.relationshipTypes().isEmpty() && file.relationshipTypes().get().getValue().containsKey(typeWithoutNamespace)) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPathWithName + "." + ((Map<?, ?>) requirementDefinition).get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

            handleNotValidKeywords("Invalid relationship type value, \"" + ((Map<?, ?>) requirementDefinition).get(key) + "\" is not exist.", line, column,endColumn);
        } catch (Exception e) {
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPathWithName + "." + ((Map<?, ?>) requirementDefinition).get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

            handleNotValidKeywords(e.getMessage(), line, column,endColumn);
        }
    }

    private void validateCapabilityFromNodeTypeParent(String yamlContent, String[] lines, String key, Map<String, Object> requirementDefinition, String requirementDefinitionPathWithName, String parent, String requirementDefinitionsKey) {
        try {
            if (context.getCurrentToscaFile().capabilityTypes().isPresent() && context.getCurrentToscaFile().capabilityTypes().get().containsKey((requirementDefinition).get(key))) {
                //TODO set the found capability type for the requirement
                return;
            }
            if ( !context.getCurrentToscaFile().imports().isEmpty()) {
                Collection<Map<String, TOSCAFile>> imports = context.getImportedToscaFiles().get(context.getCurrentToscaFilePath());
                for (Map<String, TOSCAFile> mapOfImportedFiles : imports) {
                    for (TOSCAFile file : mapOfImportedFiles.values()) {
                        if (file != null && !file.capabilityTypes().isEmpty() && file.capabilityTypes().get().containsKey((requirementDefinition).get(key))) {
                            //TODO set the found capability type for the requirement
                            return;
                        }
                    }
                }
                Collection<Map<String, TOSCAFile>> namespaces = context.getNamespaceDefinitions().get(context.getCurrentToscaFilePath());
                for (Map<String, TOSCAFile> mapOfNamespaces : namespaces) {
                    for (String namespacesKey : mapOfNamespaces.keySet()) {
                        if ( (requirementDefinition).get(key) instanceof String) {
                            String[] parts = ((String) (requirementDefinition).get(key)).split(":");
                            if (parts.length == 2) {
                                String typeWithoutNamespace = parts[1].trim();
                                String namespace = parts[0].trim();
                                if (namespacesKey.equals(namespace)) {
                                    TOSCAFile file = mapOfNamespaces.getOrDefault(namespace, null);
                                    if (file != null && !file.capabilityTypes().isEmpty() && file.capabilityTypes().get().containsKey(typeWithoutNamespace)) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPathWithName + "." + ((Map<?, ?>) requirementDefinition).get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

            handleNotValidKeywords("Invalid capability type value, \"" + ((Map<?, ?>) requirementDefinition).get(key) + "\" is not exist.", line, column,endColumn);
        } catch (Exception e) {
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPathWithName + "." + ((Map<?, ?>) requirementDefinition).get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);

            handleNotValidKeywords(e.getMessage(), line, column,endColumn);
        }
    }
    
    public void validateRequiredKeys(Map<String, Object> yamlMap, String content, String[] lines, String requirementDefinitionPath) {
        if (!yamlMap.containsKey("capability")) {
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPath);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(content, line, column, lines);
            handleNotValidKeywords("Requirement definition Missing required key: capability ", line, column, endColumn);
        } else if (!yamlMap.containsKey("relationship")) {
            Mark mark = context.getContextDependentConstructorPositions().get(requirementDefinitionPath);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(content, line, column, lines);
            handleNotValidKeywords("Requirement definition Missing required key: relationship ", line, column, endColumn);
        }
    }
    
    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter requirementDefinitionDiagnostic = new DiagnosticsSetter();
        requirementDefinitionDiagnostic.setErrorMessage(message);
        requirementDefinitionDiagnostic.setErrorContext("Not Valid Keywords");
        requirementDefinitionDiagnostic.setErrorColumn(column);
        requirementDefinitionDiagnostic.setErrorEndColumn(endColumn);
        requirementDefinitionDiagnostic.setErrorLine(line);
        diagnostics.add(requirementDefinitionDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter requirementDefinitionDiagnostic = new DiagnosticsSetter();
        requirementDefinitionDiagnostic.setErrorMessage(message);
        requirementDefinitionDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            requirementDefinitionDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            requirementDefinitionDiagnostic.setErrorLine(-1);
        }
        requirementDefinitionDiagnostic.setErrorColumn(1);
        diagnostics.add(requirementDefinitionDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter requirementDefinitionDiagnostic = new DiagnosticsSetter();
        requirementDefinitionDiagnostic.setErrorMessage(message);
        requirementDefinitionDiagnostic.setErrorContext("Parsing Error");
        requirementDefinitionDiagnostic.setErrorLine(countLines(content));
        requirementDefinitionDiagnostic.setErrorColumn(1);
        diagnostics.add(requirementDefinitionDiagnostic);
    }

    private int countLines(String content) {
        return (int) content.lines().count();
    }

}
