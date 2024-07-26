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

import org.eclipse.winery.lsp.Server.ServerCore.TOSCAFunctions.FunctionParser;
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
    
    public ArrayList<DiagnosticsSetter> validatePropertyDefinitions(Map<String, Object> propertyDefinitionsMap, Map<String, Mark> positions, String YamlContent, String[] lines) {
        Set<String> validPropertyDefinitionKeywords = Set.of(
            "type", "description", "metadata", "required", "default", "value","validation", "key_schema", "entry_schema"
        );
        for (String PropertyDefinitionKey : propertyDefinitionsMap.keySet()) {
            Object propertyDefinition = propertyDefinitionsMap.get(PropertyDefinitionKey);
            if (propertyDefinition instanceof Map) {
                validateRequiredKeys((Map<String, Object>) propertyDefinition,YamlContent);
                if (((Map<?, ?>) propertyDefinition).containsKey("type") && ((Map<?, ?>) propertyDefinition).get("type").equals("map")) {
                    ValidateEntrySchema(positions, YamlContent, lines, PropertyDefinitionKey, (Map<?, ?>) propertyDefinition);
                    validateKeySchema(positions, YamlContent, lines, PropertyDefinitionKey, (Map<?, ?>) propertyDefinition);
                } else if (((Map<?, ?>) propertyDefinition).containsKey("type") && ((Map<?, ?>) propertyDefinition).get("type").equals("list")) {
                    ValidateEntrySchema(positions, YamlContent, lines, PropertyDefinitionKey, (Map<?, ?>) propertyDefinition);
                }
                for (String key : ((Map<String, Object>) propertyDefinition).keySet()) {
                    if (!validPropertyDefinitionKeywords.contains(key)) {
                        Mark mark = positions.get(key);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);
                        handleNotValidKeywords("Invalid property definition keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
                    } else if (key.equals("default")) {
                        String type = (String) ((Map<?, ?>) propertyDefinition).get("type");
                        Object defaultValue = ((Map<?, ?>) propertyDefinition).get(key);
                        if (!CommonUtils.isTypeMatch(type, defaultValue)) {
                            Mark mark = positions.get(((Map<?, ?>) propertyDefinition).get(key));
                            int line = mark != null ? mark.getLine() + 1 : -1;
                            int column = mark != null ? mark.getColumn() + 1 : -1;
                            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);

                            handleNotValidKeywords("default value type does not match type: " + type + " at line " + line + ", column " + column, line, column, endColumn);
                        }
                    } else if (key.equals("entry_schema")) {
                        ValidateSchemaDefinition(positions, YamlContent, lines, (Map<?, ?>) propertyDefinition);
                    } else if (key.equals("key_schema")) {
                        ValidateSchemaDefinition(positions, YamlContent, lines, (Map<?, ?>) propertyDefinition);
                    } else if (key.equals("validation")) {
                        FunctionParser functionParser = new FunctionParser();
                        //parsing the validation function 
                        try {
                            functionParser.parseFunctionCall((String) ((Map<?, ?>) propertyDefinition).get(key));    
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //TODO call the TOSCA function tha have been parsed 
                        
                    }
                }
            }
        }
        return diagnostics;
    }

    public void ValidateSchemaDefinition(Map<String, Mark> positions, String YamlContent, String[] lines, Map<?, ?> propertyDefinition) {
        Object entrySchema = propertyDefinition.get("entry_schema");
        if (entrySchema instanceof Map) {
            ValidateSchemaDefinition validateSchemaDefinition = new ValidateSchemaDefinition();
            ArrayList<DiagnosticsSetter> SchemaDefinitionDiagnostics = validateSchemaDefinition.validateSchemaDefinitions((Map<String, Object>) entrySchema, positions, YamlContent, lines);
            diagnostics.addAll(SchemaDefinitionDiagnostics); 
        }
    }

    public void ValidateEntrySchema(Map<String, Mark> positions, String YamlContent, String[] lines, String PropertyDefinitionKey, Map<?, ?> propertyDefinition) {
        if (! propertyDefinition.containsKey("entry_schema")) {
            Mark mark = positions.get(PropertyDefinitionKey);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);
            handleNotValidKeywords("Missing entry_schema at property: " + PropertyDefinitionKey, line, column, endColumn);
        }
    }

    public void validateKeySchema(Map<String, Mark> positions, String YamlContent, String[] lines, String PropertyDefinitionKey, Map<?, ?> propertyDefinition) {
        if (! propertyDefinition.containsKey("key_schema")) {
            Mark mark = positions.get(PropertyDefinitionKey);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);
            handleNotValidKeywords("Missing key_schema at property: " + PropertyDefinitionKey, line, column, endColumn);
        }
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

    public void validateRequiredKeys(Map<String, Object> yamlMap, String content) {
        if (!yamlMap.containsKey("type")) {
            handleDiagnosticsError("Property Definition Missing required key: type", content);
        }
    }
    
    private int countLines(String content) {
        return (int) content.lines().count();
    }

}
