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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.ArtifactType;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.PropertyDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCAFunctions.FunctionParser;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.ValidatingUtils;
import org.yaml.snakeyaml.error.Mark;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PropertyDefinitionValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    private final LSContext context;
    
    public PropertyDefinitionValidator(LSContext context) {
        this.context = context;
    }

    public ArrayList<DiagnosticsSetter> validatePropertyDefinitions(Map<String, Object> propertyDefinitionsMap, Map<String, Mark> positions, String YamlContent, String[] lines, String parentArtifactType, String derivedFrom) {
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
                        handelInvalidPropertyDefinitionKeyword(positions, YamlContent, lines, key);
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
                    } else if (key.equals("validation") ) {
                        checkValidation(positions, YamlContent, lines, parentArtifactType, PropertyDefinitionKey, key, (Map<?, ?>) propertyDefinition);
                    }
                }
            }  
            else {
                //Handle refines property from derived type
                refinePropertyFromDerivedType(positions, YamlContent, lines, parentArtifactType, derivedFrom, PropertyDefinitionKey, propertyDefinition);
            }
        }
        return diagnostics;
    }

    private void refinePropertyFromDerivedType(Map<String, Mark> positions, String YamlContent, String[] lines, String parentArtifactType, String derivedFrom, String PropertyDefinitionKey, Object propertyDefinition) {
        if (derivedFrom == null ) { 
            Mark mark = positions.get(PropertyDefinitionKey);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
            handleNotValidKeywords("The property " + PropertyDefinitionKey + " is not found" , line, column, endColumn);
        } 
        else { 
            //handle if the property has a fixed value
            if (getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey).value().isPresent()) {
                Mark mark = positions.get(PropertyDefinitionKey);
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                handleNotValidKeywords("This property has a fixed value of " + getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey).value().get() , line, column, endColumn);              
            } else { 
                // Search for the property in the derived artifact type
                try { 
                    // get a copy from the derived property and add the value 
                    PropertyDefinition newPropertyDefinitionObject = setNewPropertyValue(parentArtifactType, PropertyDefinitionKey, propertyDefinition, derivedFrom);
                    //Validate the value
                    if (newPropertyDefinitionObject.validation().isPresent()) {
                        if (isValidPropertyDefinitionsValue(newPropertyDefinitionObject.validation().get(), propertyDefinition, newPropertyDefinitionObject.type().getValue(), positions, YamlContent, lines)) {
                            Mark mark = positions.get(PropertyDefinitionKey);
                            int line = mark != null ? mark.getLine() + 1 : -1;
                            int column = mark != null ? mark.getColumn() + 1 : -1;
                            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                            handleNotValidKeywords("The value " + propertyDefinition + " did not pass the validation " , line, column, endColumn);
                        }
                    }
                } catch (Exception e) {
                    Mark mark = positions.get(PropertyDefinitionKey);
                    int line = mark != null ? mark.getLine() + 1 : -1;
                    int column = mark != null ? mark.getColumn() + 1 : -1;
                    int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                    handleNotValidKeywords(e.getMessage(), line, column, endColumn);
                }
            } 
        }
    } 
 
    private PropertyDefinition setNewPropertyValue(String parentArtifactType, String PropertyDefinitionKey, Object newValue, String derivedFrom) {
        PropertyDefinition newPropertyDefinitionObject ;
        newPropertyDefinitionObject = getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey).clone();
        newPropertyDefinitionObject = newPropertyDefinitionObject.withValue(newValue);
        ArtifactType newArtifactType = context.getToscaFile().artifactTypes().get().get(parentArtifactType).addOrOverridePropertyDefinition(PropertyDefinitionKey , newPropertyDefinitionObject);
        TOSCAFile toscaFile = context.getToscaFile().overrideTOSCAFile(parentArtifactType , newArtifactType);
        context.setToscaFile(toscaFile);
        return newPropertyDefinitionObject;
    }

    private void checkValidation(Map<String, Mark> positions, String YamlContent, String[] lines, String parentArtifactType, String PropertyDefinitionKey, String key, Map<?, ?> propertyDefinition) {
        FunctionParser functionParser = new FunctionParser();
        //parsing the validation function
        try {
            if (CommonUtils.isFunction(String.valueOf(propertyDefinition.get("validation")))) {
                functionParser.parseFunctionCall(String.valueOf(propertyDefinition.get(key)));
                //setting the validation variable in Tosca object
                setValidationStack(parentArtifactType, PropertyDefinitionKey, functionParser);
                //validating the property definition fixed value by applying the validation functions entered by the user
                if (propertyDefinition.containsKey("value")) {
                    if (isValidPropertyDefinitionsValue(functionParser.getFunctionStack(), propertyDefinition.get("value"), (String) propertyDefinition.get("type"), positions, YamlContent, lines)) {
                        Mark mark = positions.get("value");
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                        handleNotValidKeywords("The value " + propertyDefinition.get("value") + " did not pass the validation ", line, column, endColumn);
                    }
                }
            }    
        } catch (Exception e) {
            Mark mark = positions.get("validation");
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
            handleNotValidKeywords(e.getMessage() , line, column, endColumn);
        }
    }
    
    private PropertyDefinition getPropertyDefinitionObject(String ArtifactType, String PropertyDefinitionKey) {
        return context.getToscaFile().artifactTypes().get().get(ArtifactType).properties().get().get(PropertyDefinitionKey);
    }

    private void setValidationStack(String parentArtifactType, String PropertyDefinitionKey, FunctionParser functionParser) {
        PropertyDefinition newPropertyDefinition = context.getToscaFile().artifactTypes().get().get(parentArtifactType).properties().get().get(PropertyDefinitionKey).withValidation(functionParser.getFunctionStack());
        ArtifactType newArtifactType = context.getToscaFile().artifactTypes().get().get(parentArtifactType).overridePropertyDefinition(PropertyDefinitionKey , newPropertyDefinition);
        TOSCAFile toscaFile = context.getToscaFile().overrideTOSCAFile(parentArtifactType , newArtifactType);
        context.setToscaFile(toscaFile);
    }
    
    public boolean isValidPropertyDefinitionsValue(Stack<Map<String,List<String>>> TheValidation, Object value, String type, Map<String, Mark> positions, String yamlContent, String[] lines) {
        Object result = null;
        Stack<Map<String,List<String>>> validation = (Stack<Map<String, List<String>>>) TheValidation.clone();
        if (validation.isEmpty()) {
            throw new IllegalArgumentException("Validation function stack is not present");
        }
        Map<String,Object> FunctionValues = new HashMap<>();
        while (!validation.empty()) {
            Map<String, List<String>> item = validation.peek();
            String function = "";
            List<String> parameters = List.of();
            for (Map.Entry<String, List<String>> entry : item.entrySet()) {
                function = entry.getKey();
                parameters = entry.getValue();
                break;
            }
            if (ValidatingUtils.validFunction(function)) {
                if (ValidatingUtils.isParametersContainsFunction(parameters)) {
                    parameters = ValidatingUtils.replaceFunctionsByValue(FunctionValues, parameters);
                }
                try {
                    if (function.equals("$value")) {
                        result = value;
                    } else {
                        result = ValidatingUtils.callBooleanFunction(function,parameters,(String) type, context );
                    }
                    FunctionValues.put(function,result);
                } catch (Exception e) {
                    Mark mark = positions.get("validation");
                    int line = mark != null ? mark.getLine() + 1 : -1;
                    int column = mark != null ? mark.getColumn() + 1 : -1;
                    int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                    handleNotValidKeywords(e.getMessage() , line, column, endColumn);
                }
            }
            validation.pop(); // Remove the processed item
        }
        return !((boolean) result);    
    }
    
    private void handelInvalidPropertyDefinitionKeyword(Map<String, Mark> positions, String YamlContent, String[] lines, String key) {
        Mark mark = positions.get(key);
        int line = mark != null ? mark.getLine() + 1 : -1;
        int column = mark != null ? mark.getColumn() + 1 : -1;
        int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);
        handleNotValidKeywords("Invalid property definition keyword: " + key + " at line " + line + ", column " + column, line, column, endColumn);
    }

    public void ValidateSchemaDefinition(Map<String, Mark> positions, String YamlContent, String[] lines, Map<?, ?> propertyDefinition) {
        Object entrySchema = propertyDefinition.get("entry_schema");
        if (entrySchema instanceof Map) {
            ValidateSchemaDefinition validateSchemaDefinition = new ValidateSchemaDefinition(context.getToscaFile());
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
