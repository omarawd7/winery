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
package org.eclipse.winery.lsp.Server.ServerCore.ObjectConstruction;

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.PropertyDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.SchemaDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyDefinitionParser {
    
    public static Map<String, PropertyDefinition> parseProperties(Map<String, Object> propertiesMap) {
        if (propertiesMap == null) {
            return Collections.emptyMap();
        }
        return propertiesMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> parsePropertyDefinition((Map<String, Object>) e.getValue())
            ));
    }

    public static PropertyDefinition parsePropertyDefinition(Map<String, Object> propertyDefinitionMap) {
        if (propertyDefinitionMap == null) {
            return null;
        }
        ToscaString type = new ToscaString((String) propertyDefinitionMap.get("type"));
        Optional<ToscaString> description = Optional.ofNullable(new ToscaString((String) propertyDefinitionMap.get("description")));
        Optional<ToscaMap<String, Object>> metadata = Optional.ofNullable(new ToscaMap<>((Map<String, Object>) propertyDefinitionMap.get("metadata")));
        ToscaBoolean required = new ToscaBoolean((Boolean) propertyDefinitionMap.getOrDefault("required", true));
        Optional<Object> Default = Optional.ofNullable( propertyDefinitionMap.get("default"));
        Optional<Object> value = Optional.ofNullable(propertyDefinitionMap.get("value"));
        Optional<Stack<Map<String, List<String>>>> validation = Optional.empty(); //Constructed in the PropertyDefinition validation
        Optional<SchemaDefinition> keySchema = Optional.empty();
        Optional<SchemaDefinition> entrySchema = Optional.empty();
        try {
            keySchema = Optional.ofNullable(SchemaDefenitionParser.parseSchemaDefinition((Map<String, Object>) propertyDefinitionMap.getOrDefault("key_schema",null)));
            entrySchema = Optional.ofNullable(SchemaDefenitionParser.parseSchemaDefinition((Map<String, Object>) propertyDefinitionMap.getOrDefault("entrySchema",null)));
        } catch (Exception e) {
            System.err.println("Error parsing Schema");
        }
        return new PropertyDefinition (
            type,
            description,
            metadata,
            required,
            Default,
            value,
            validation,
            keySchema,
            entrySchema
        );
    }

    private static Object getPropertyType(Object defaultValue, ToscaString type) {
        return switch (type.getValue()) {
            case "boolean" -> new ToscaBoolean((Boolean) defaultValue);
            case "string" -> new ToscaString((String) defaultValue);
            case "integer" -> new ToscaInteger((int) defaultValue);
            case "float" -> new ToscaFloat((float) defaultValue);
            case "bytes" -> new ToscaFloat((Byte) defaultValue);
            case "nil" -> ToscaNil.getInstance();
            case "timestamp" -> new ToscaTimestamp((OffsetDateTime) defaultValue);
            //Todo add scalar-unit and scalar-unit.time	
            case "version" -> new ToscaVersion((String) defaultValue);
            case "list" -> new ToscaList((List<Object>) defaultValue);
            case "map" -> new ToscaMap<>((Map<Object, Object>) defaultValue);
            default -> null;
        };
    }
}
