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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.*;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCAFunctions.FunctionParser;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.ValidatingUtils;
import org.tinylog.Logger;
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

    public ArrayList<DiagnosticsSetter> validatePropertyDefinitions(Map<String, Object> propertyDefinitionsMap, Map<String, Mark> positions, String YamlContent, String[] lines, String parentName, String prevPath,String derivedFrom) {
        Set<String> validPropertyDefinitionKeywords = Set.of(
            "type", "description", "metadata", "required", "default", "value","validation", "key_schema", "entry_schema"
        );
        for (String PropertyDefinitionKey : propertyDefinitionsMap.keySet()) {
            String propertyPath =  prevPath + "." + parentName + "." + "properties" + "." + PropertyDefinitionKey;
            Object propertyDefinition = propertyDefinitionsMap.get(PropertyDefinitionKey);
            if (propertyDefinition instanceof Map) {
                if (propertyPath.contains("artifact_types") || propertyPath.contains( "capability_types") ||  propertyPath.contains( "node_types") || propertyPath.contains( "relationship_types")) {
                    handlePropertyDefinition(positions, YamlContent, lines, parentName, PropertyDefinitionKey, propertyDefinition, propertyPath, validPropertyDefinitionKeywords);
                } 
            }  
            else {
                //Handle property assignment
                try {
                    handlePropertyAssignment(positions, YamlContent, lines, parentName, derivedFrom, PropertyDefinitionKey, propertyDefinition , propertyPath);
                } catch (Exception e) {
                    Mark mark = context.getContextDependentConstructorPositions().get(propertyPath);
                    int line = mark != null ? mark.getLine() + 1 : -1;
                    int column = mark != null ? mark.getColumn() + 1 : -1;
                    int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                    handleNotValidKeywords(e.getMessage(), line, column, endColumn);
                }
            }
        }
        return diagnostics;
    }

    private void handlePropertyDefinition(Map<String, Mark> positions, String YamlContent, String[] lines, String parentName, String PropertyDefinitionKey, Object propertyDefinition, String propertyPath, Set<String> validPropertyDefinitionKeywords) {
        validateRequiredKeys((Map<String, Object>) propertyDefinition, YamlContent, lines, propertyPath);
        if (((Map<?, ?>) propertyDefinition).containsKey("type") && ((Map<?, ?>) propertyDefinition).get("type").equals("map")) {
            ValidateEntrySchema(YamlContent, lines, PropertyDefinitionKey, propertyPath);
            validateKeySchema(YamlContent, lines, PropertyDefinitionKey, propertyPath);
        } else if (((Map<?, ?>) propertyDefinition).containsKey("type") && ((Map<?, ?>) propertyDefinition).get("type").equals("list")) {
            ValidateEntrySchema(YamlContent, lines, PropertyDefinitionKey, propertyPath);
        }
        for (String key : ((Map<String, Object>) propertyDefinition).keySet()) {
            if (!validPropertyDefinitionKeywords.contains(key)) {
             try {
                 handelInvalidPropertyDefinitionKeyword(YamlContent, lines, key, propertyPath); 
             } catch (Exception e) {
                 Logger.error("the error message: ", e);
             }
            } else if (key.equals("default")) {
                validateDefaultValue(YamlContent, lines, propertyPath, key, (Map<?, ?>) propertyDefinition);
            } else if (key.equals("entry_schema")) {
                ValidateSchemaDefinition(positions, YamlContent, lines, (Map<?, ?>) propertyDefinition, propertyPath,key);
            } else if (key.equals("key_schema")) {
                ValidateSchemaDefinition(positions, YamlContent, lines, (Map<?, ?>) propertyDefinition, propertyPath,key);
            } else if (key.equals("validation") ) {
                checkValidation(positions, YamlContent, lines, parentName, PropertyDefinitionKey, key, (Map<?, ?>) propertyDefinition, propertyPath);
            } else if (key.equals("value")) {
                validateFixedValue(YamlContent, lines, propertyPath, key, (Map<?, ?>) propertyDefinition);
            }
        }
    }

    private void validateFixedValue(String yamlContent, String[] lines, String path, String key, Map<?,?> propertyDefinition) {
        if (propertyDefinition.containsKey("type")) {
            String type = (String) propertyDefinition.get("type");
            String valueText =   context.getContextDependentConstructorPositions().get(path + "." + key).get_snippet().trim();
            int ind = valueText.lastIndexOf(":");
            Object fixedValue =  valueText.substring(ind + 1, valueText.length() - 1).trim();
            if (!CommonUtils.isTypeMatch(type, fixedValue)) {
                Mark mark = context.getContextDependentConstructorPositions().get(path + "." + propertyDefinition.get(key));
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);
                handleNotValidKeywords("Fixed value " + fixedValue + " does not match type: " + type, line, column, endColumn);
            }    
        } else {
            Mark mark = context.getContextDependentConstructorPositions().get(path + "." + propertyDefinition.get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);
            handleNotValidKeywords("The property type is missing", line, column, endColumn);
        }
        
    }

    private void validateDefaultValue(String YamlContent, String[] lines, String path , String key, Map<?, ?> propertyDefinition) {
        if (propertyDefinition.containsKey("type") && propertyDefinition.get("type") instanceof String  &&  propertyDefinition.containsKey(key) && !CommonUtils.isTypeMatch((String) propertyDefinition.get("type"), propertyDefinition.get(key).toString())) {
            Mark mark = context.getContextDependentConstructorPositions().get(path  + "." + propertyDefinition.get(key));
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
            handleNotValidKeywords("Default value type does not match type: " + propertyDefinition.get("type") + " at line " + line + ", column " + column, line, column, endColumn);
        }
    }

    private void handlePropertyAssignment(Map<String, Mark> positions, String YamlContent, String[] lines, String parent, String derivedFrom, String PropertyDefinitionKey, Object propertyValue, String path) {
        handleNodeTemplateAssignment(YamlContent, lines, parent, PropertyDefinitionKey, propertyValue, path);
    }

    private void handleNodeTemplateAssignment(String YamlContent, String[] lines, String parent, String PropertyDefinitionKey, Object propertyValue, String path) {
        if (path.contains("node_templates")) {
            if (context.getCurrentToscaFile() != null && context.getCurrentToscaFile().serviceTemplate().isPresent() && context.getCurrentToscaFile().serviceTemplate().get().nodeTemplates() != null && context.getCurrentToscaFile().serviceTemplate().get().nodeTemplates().getValue().containsKey(parent) && context.getCurrentToscaFile().serviceTemplate().get().nodeTemplates().getValue().get(parent).type().properties().containsKey(PropertyDefinitionKey)) {
                PropertyDefinition derivedProperty = context.getCurrentToscaFile().serviceTemplate().get().nodeTemplates().getValue().get(parent).type().properties().get(PropertyDefinitionKey);
                if (derivedProperty != null && derivedProperty.value().isEmpty()) {
                    PropertyDefinition newProperty = new PropertyDefinition(derivedProperty.type(), derivedProperty.description(),derivedProperty.metadata(), derivedProperty.required(), derivedProperty.Default(), Optional.ofNullable(propertyValue),derivedProperty.validation(), derivedProperty.keySchema(), derivedProperty.entrySchema());
                    context.getCurrentToscaFile().serviceTemplate().get().nodeTemplates().getValue().get(parent).properties().put(PropertyDefinitionKey,newProperty);
                    if (!newProperty.type().equals("")) {
                        //TODO validate the value
                    } else {
                        Mark mark = context.getContextDependentConstructorPositions().get(path);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                        handleNotValidKeywords("The type of the property " + PropertyDefinitionKey + " is not found" , line, column, endColumn);

                    }
                } else if (derivedProperty != null && !derivedProperty.value().isPresent()) {
                    //handle if the property has a fixed value
                        Mark mark = context.getContextDependentConstructorPositions().get(path);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                    handleNotValidKeywords("This property has a fixed value of " + derivedProperty.value().get() , line, column, endColumn);
                }
            } else {
                Mark mark = context.getContextDependentConstructorPositions().get(path);
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
                handleNotValidKeywords("This property is not found" , line, column, endColumn);
            }
        }
    }

    private PropertyDefinition setNewPropertyValue(String parent, String PropertyDefinitionKey, Object newValue, String derivedFrom, String path) {
        PropertyDefinition newPropertyDefinitionObject ;
        if (path.contains("artifact_types")) {
            newPropertyDefinitionObject = getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey, path).clone();
            newPropertyDefinitionObject = newPropertyDefinitionObject.withValue(newValue);
            ArtifactType newArtifactType = context.getCurrentToscaFile().artifactTypes().get(parent).addOrOverridePropertyDefinition(PropertyDefinitionKey , newPropertyDefinitionObject);
            TOSCAFile toscaFile = context.getCurrentToscaFile().updateArtifactTypes(parent , newArtifactType);
            context.setCurrentToscaFile(toscaFile);
            return newPropertyDefinitionObject;
        } else if (path.contains("node_types")) {
            newPropertyDefinitionObject = getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey, path).clone();
            newPropertyDefinitionObject = newPropertyDefinitionObject.withValue(newValue);
            NodeType nodeType = context.getCurrentToscaFile().nodeTypes().getValue().get(parent).overridePropertyDefinition(PropertyDefinitionKey , newPropertyDefinitionObject);
            TOSCAFile toscaFile = context.getCurrentToscaFile().updateNodeTypes(parent , nodeType);
            context.setCurrentToscaFile(toscaFile);
            return newPropertyDefinitionObject;
        } else if (path.contains("relationship_types")) {
            newPropertyDefinitionObject = getPropertyDefinitionObject(derivedFrom, PropertyDefinitionKey, path).clone();
            newPropertyDefinitionObject = newPropertyDefinitionObject.withValue(newValue);
            context.getCurrentToscaFile().relationshipTypes().getValue().get(parent).properties().put(PropertyDefinitionKey , newPropertyDefinitionObject);
            return newPropertyDefinitionObject;
        }
        throw new IllegalArgumentException("The propertyDefinition " + derivedFrom + "." + PropertyDefinitionKey + " does not exist");
    }

    private void checkValidation(Map<String, Mark> positions, String YamlContent, String[] lines, String parent, String PropertyDefinitionKey, String key, Map<?, ?> propertyValue, String path) {
        FunctionParser functionParser = new FunctionParser();
        //parsing the validation function
        try {
            if (CommonUtils.isFunction(String.valueOf(propertyValue.get(key)))) {
                functionParser.parseFunctionCall(String.valueOf(propertyValue.get(key)));
                //setting the validation variable in Tosca object
                try {
                    setValidationStack(parent, PropertyDefinitionKey, functionParser , path);    
                } catch (Exception e) {
                    throw new IllegalStateException("setValidationStack: " + e);
                }
                
                //validating the property definition fixed value by applying the validation functions entered by the user
                if (propertyValue.containsKey("value") && propertyValue.containsKey("type") ) {
                    validateValueBasedOnValidationStack(positions, YamlContent, lines, parent, PropertyDefinitionKey, propertyValue, path, functionParser);
                }
            }    
        } catch (Exception e) {
            Logger.error("Could not parse the validation", e);
        }
    }

    private void validateValueBasedOnValidationStack(Map<String, Mark> positions, String YamlContent, String[] lines, String parent, String PropertyDefinitionKey, Map<?, ?> propertyValue, String path, FunctionParser functionParser) {
        if (isValidPropertyDefinitionsValue(functionParser.getFunctionStack(), propertyValue.get("value"), (String) propertyValue.get("type"), positions, YamlContent, lines, parent, PropertyDefinitionKey, path)) {
            Mark mark = context.getContextDependentConstructorPositions().get(path + "."  + "value");
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
            handleNotValidKeywords("The value " + propertyValue.get("value") + " did not pass the validation ", line, column, endColumn);
        }
    }

    private PropertyDefinition getPropertyDefinitionObject(String derivedFrom, String PropertyDefinitionKey, String path) {
       if (path.contains("artifact_types")) {
           PropertyDefinition propertyDefinition = context.getCurrentToscaFile().artifactTypes().get(derivedFrom).properties().get(PropertyDefinitionKey);
           if (propertyDefinition != null) {
           return propertyDefinition;
           }
       } else if (path.contains("node_types")) {
            PropertyDefinition propertyDefinition = context.getCurrentToscaFile().nodeTypes().getValue().get(derivedFrom).properties().get(PropertyDefinitionKey);
            if (propertyDefinition != null) {
            return propertyDefinition;
            }
       } else if (path.contains("relationship_types")) {
           PropertyDefinition propertyDefinition = context.getCurrentToscaFile().relationshipTypes().getValue().get(derivedFrom).properties().get(PropertyDefinitionKey);
           if (propertyDefinition != null) {
               return propertyDefinition;
           }
       }
        throw new IllegalArgumentException("The propertyDefinition " + derivedFrom + "." + PropertyDefinitionKey + " does not exist");
    }

    private void setValidationStack(String parent, String PropertyDefinitionKey, FunctionParser functionParser, String path) {
        if (path.contains("artifact_types")) {
            PropertyDefinition newPropertyDefinition = context.getCurrentToscaFile().artifactTypes().get(parent).properties().get(PropertyDefinitionKey).withValidation(functionParser.getFunctionStack());
            context.getCurrentToscaFile().artifactTypes().get(parent).properties().put(PropertyDefinitionKey, newPropertyDefinition);
        } else if (path.contains("node_types")) {
            PropertyDefinition newPropertyDefinition = context.getCurrentToscaFile().nodeTypes().getValue().get(parent).properties().get(PropertyDefinitionKey).withValidation(functionParser.getFunctionStack());
            context.getCurrentToscaFile().nodeTypes().getValue().get(parent).properties().put(PropertyDefinitionKey, newPropertyDefinition);
        }
         else if (path.contains("relationship_types")) {
            PropertyDefinition newPropertyDefinition = context.getCurrentToscaFile().relationshipTypes().getValue().get(parent).properties().get(PropertyDefinitionKey).withValidation(functionParser.getFunctionStack());
            context.getCurrentToscaFile().relationshipTypes().getValue().get(parent).properties().put(PropertyDefinitionKey, newPropertyDefinition);
        }
    }
    
    public boolean isValidPropertyDefinitionsValue(Stack<Map<String,List<String>>> TheValidation, Object value, String type, Map<String, Mark> positions, String yamlContent, String[] lines, String parentArtifactType, String PropertyDefinitionKey, String path) {
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
                    Logger.error("Could not parse the validation", e);
                }
            }
            validation.pop(); // Remove the processed item
        }
        return !((boolean) result);    
    }
    
    private void handelInvalidPropertyDefinitionKeyword(String YamlContent, String[] lines, String key, String  propertyPath) {
        try {
            Mark mark = context.getContextDependentConstructorPositions().get(propertyPath + "." + key);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(YamlContent, line, column, lines);
            handleNotValidKeywords("Invalid property definition keyword: " + key, line, column, endColumn);    
        } catch (Exception e) {
            Logger.error("the error message: ", e);
        }
    }

    public void ValidateSchemaDefinition(Map<String, Mark> positions, String YamlContent, String[] lines, Map<?, ?> propertyValue, String path  , String key) {
        Object entrySchema = propertyValue.get("entry_schema");
        try {
            if (entrySchema instanceof Map) {
                SchemaDefinitionValidator schemaDefinitionValidator = new SchemaDefinitionValidator(context);
                String schemaPath = path + "." + key;
                ArrayList<DiagnosticsSetter> SchemaDefinitionDiagnostics = schemaDefinitionValidator.validateSchemaDefinitions((Map<String, Object>) entrySchema, positions, YamlContent, lines, schemaPath );
                diagnostics.addAll(SchemaDefinitionDiagnostics);
            }
        } catch (Exception e) {
            Logger.error("the error message: ", e);
        }
       
    }

    public void ValidateEntrySchema(String YamlContent, String[] lines, String PropertyDefinitionKey, String path) {
        if (!context.getContextDependentConstructorPositions().containsKey(path + "." + "entry_schema")) {
            Mark mark = context.getContextDependentConstructorPositions().get(path);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(YamlContent, line, column, lines);
            handleNotValidKeywords("Missing entry_schema at property: " + PropertyDefinitionKey, line, column, endColumn);
        }
    }

    public void validateKeySchema(String YamlContent, String[] lines, String PropertyDefinitionKey, String path) {
        if (!context.getContextDependentConstructorPositions().containsKey(path + "." + "entry_schema")) {
            Mark mark = context.getContextDependentConstructorPositions().get(path);
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

    public void validateRequiredKeys(Map<String, Object> yamlMap, String yamlContent, String[] lines, String propertyPath) {
        if (!yamlMap.containsKey("type")) {
            Mark mark = context.getContextDependentConstructorPositions().get(propertyPath);
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);
            handleNotValidKeywords("Property Definition Missing required key: type" , line, column, endColumn);
        }
    }
    
    private int countLines(String content) {
        return (int) content.lines().count();
    }
}
