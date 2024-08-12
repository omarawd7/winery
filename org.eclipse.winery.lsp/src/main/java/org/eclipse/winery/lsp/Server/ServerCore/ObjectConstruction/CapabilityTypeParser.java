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

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.AttributeDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.CapabilityType;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.PropertyDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaList;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaMap;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityTypeParser {
    private static final Map<String, CapabilityType> CapabilityTypesNamesMap = new HashMap<>();
    public static Map<String, CapabilityType> parseCapabilityTypes(Map<String, Object> capabilityTypesMap) {
        if (capabilityTypesMap == null) {
            return Collections.emptyMap();
        }
        return capabilityTypesMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    CapabilityType capabilityType = CapabilityTypeParser.parseCapabilityType((Map<String, Object>) e.getValue());
                    CapabilityTypesNamesMap.put(e.getKey(), capabilityType);
                    return capabilityType;
                }
            ));
    }

    public static CapabilityType parseCapabilityType(Map<String, Object> capabilityTypeMap) {
        if (capabilityTypeMap == null) {
            return null;
        }

        Optional<CapabilityType> derivedFrom = Optional.empty();
        try {
            derivedFrom = Optional.ofNullable(getCapabilityType((String) capabilityTypeMap.get("derived_from")));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        Optional<ToscaString> version = Optional.of(new ToscaString((String) capabilityTypeMap.get("version")));
        Optional<ToscaMap<String, String>> metadata = Optional.of(new ToscaMap<>((Map<String, String>) capabilityTypeMap.get("metadata")));
        Optional<ToscaString> description = Optional.of(new ToscaString((String) capabilityTypeMap.get("description")));
        Optional<ToscaList<String>> valid_source_node_types = Optional.of(new ToscaList<>((List<String>) capabilityTypeMap.get("valid_source_node_types")));
        Optional<ToscaList<String>> valid_relationship_types = Optional.of(new ToscaList<>((List<String>) capabilityTypeMap.get("valid_relationship_types")));
        Optional<Map<String, PropertyDefinition>> properties = Optional.ofNullable(PropertyDefinitionParser.parseProperties((Map<String, Object>) capabilityTypeMap.get("properties")));
        Optional<Map<String, AttributeDefinition>> attributes = Optional.ofNullable((Map<String, AttributeDefinition>) capabilityTypeMap.get("attributes")); //TODO add the attribute definition parser

        return new CapabilityType(
            derivedFrom,
            version,
            metadata,
            description,
            valid_source_node_types,
            valid_relationship_types,
            properties,
            attributes
        );
    }

    public static CapabilityType getCapabilityType(String derivedFrom) {
        return CapabilityTypesNamesMap.getOrDefault(derivedFrom, null);
    }

}
