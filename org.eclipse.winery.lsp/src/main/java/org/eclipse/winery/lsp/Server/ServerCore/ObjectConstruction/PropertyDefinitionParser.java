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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
        String type = (String) propertyDefinitionMap.get("type");
        Optional<String> description = Optional.ofNullable((String) propertyDefinitionMap.get("description"));
        Optional<Map<String, Object>> metadata = Optional.ofNullable((Map<String, Object>) propertyDefinitionMap.get("metadata"));
        Boolean required = (Boolean) propertyDefinitionMap.getOrDefault("required", true);
        Optional<Object> Default = Optional.ofNullable(propertyDefinitionMap.get("default"));
        Optional<Object> value = Optional.ofNullable(propertyDefinitionMap.get("value"));
        Optional<Object> validation = Optional.ofNullable(propertyDefinitionMap.get("validation"));
        Optional<Object> keySchema = Optional.ofNullable(propertyDefinitionMap.get("key_schema"));
        Optional<Object> entrySchema = Optional.ofNullable(propertyDefinitionMap.get("entry_schema"));

        return new PropertyDefinition(
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
}
