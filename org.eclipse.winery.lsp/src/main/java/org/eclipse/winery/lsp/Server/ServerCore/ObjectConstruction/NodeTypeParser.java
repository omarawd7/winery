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

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.*;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaList;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaMap;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;
import java.util.*;
import java.util.stream.Collectors;

public class NodeTypeParser {
    private static final Map<String, NodeType> NodeTypeNamesMap = new HashMap<>();
    public static Map<String, NodeType> parseNodeTypes(Map<String, Object> nodeTypes) {
        if (nodeTypes == null) {
            return Collections.emptyMap();
        }
        return nodeTypes.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    NodeType nodeType = new NodeType( Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
                    if (e.getValue() instanceof Map) {
                        nodeType = parseNodeType((Map<String, Object>) e.getValue());
                        NodeTypeNamesMap.put(e.getKey(), nodeType);
                    }
                    return nodeType;
                }
            ));
    }

    public static NodeType parseNodeType(Map<String, Object> nodeTypeMap) {
        if (nodeTypeMap == null) {
            return null;
        }
        
        Optional<NodeType> derivedFrom = Optional.empty();
        if (nodeTypeMap.get("derived_from") != null && nodeTypeMap.get("derived_from")  instanceof String) {
            NodeType derivedFromValue = getNodeType((String) nodeTypeMap.get("derived_from"));

            if (derivedFromValue != null) {
                derivedFrom = Optional.of(derivedFromValue);
            }
        } 
        
        Optional<ToscaString> version  = Optional.empty();
        if (nodeTypeMap.get("version") != null && nodeTypeMap.get("version") instanceof String) {
            version = Optional.of(new ToscaString((String) nodeTypeMap.get("version")));
        }
        
        Optional<ToscaMap<String, String>> metadata  = Optional.empty();
        if (nodeTypeMap.get("metadata") != null && nodeTypeMap.get("metadata") instanceof Map) {
            metadata = Optional.of(new ToscaMap<>((Map<String, String>) nodeTypeMap.get("metadata")));
        }
        
        Optional<ToscaString> description  = Optional.empty();
        if (nodeTypeMap.get("description") != null && nodeTypeMap.get("description") instanceof String) {
            description = Optional.of(new ToscaString((String) nodeTypeMap.get("description")));
        }

        Optional<Map<String, PropertyDefinition>> properties = Optional.empty();
        if (nodeTypeMap.get("properties") != null && nodeTypeMap.get("properties") instanceof Map) {
            properties = Optional.of(PropertyDefinitionParser.parseProperties((Map<String, Object>) nodeTypeMap.get("properties")));
        }
        
        Optional<ToscaMap<String, AttributeDefinition>> attributes = Optional.empty();
        if (nodeTypeMap.get("attributes") != null && nodeTypeMap.get("attributes") instanceof Map) {
            attributes = Optional.of(new ToscaMap<>(AttributeDefinitionParser.parseAttributeDefinition( (Map<String, Object>) nodeTypeMap.get("attributes"))));
        }

        Optional<ToscaMap<String, CapabilityDefinition>> capabilities = Optional.of(new ToscaMap<>(new HashMap<>()));
        if (nodeTypeMap.get("capabilities") != null && nodeTypeMap.get("capabilities") instanceof Map) {
            capabilities = Optional.of(new ToscaMap<>(CapabilityDefinitionParser.parseCapabilityDefinitions((Map<String, Object>) nodeTypeMap.get("capabilities"))));
        }

        Optional<ToscaList<RequirementDefinition>> requirements = Optional.empty();
        if (nodeTypeMap.get("requirements") != null && nodeTypeMap.get("requirements") instanceof List) {
            requirements = Optional.of(new ToscaList<>(RequirementDefinitionParser.parseRequirementDefinitions((List<Object>) nodeTypeMap.get("requirements"))));
        }

        Optional<ToscaMap<String, InterfaceDefinition>> interfaces = Optional.empty();
        if (nodeTypeMap.get("interfaces") != null && nodeTypeMap.get("interfaces") instanceof Map<?,?>) {
            interfaces = Optional.of(new ToscaMap<>(InterfaceDefinitionParser.parseInterfaceDefinitions((Map<String, Object>) nodeTypeMap.get("interfaces"))));
        }

        Optional<ToscaMap<String, ArtifactDefinition>> artifacts = Optional.empty();
        if (nodeTypeMap.get("artifacts") != null && nodeTypeMap.get("artifacts") instanceof Map<?,?>) {
            artifacts = Optional.of(new ToscaMap<>(ArtifactDefinitionParser.parseArtifactDefinition((Map<String, Object>) nodeTypeMap.get("artifacts"))));
        }
        
        return new NodeType(derivedFrom,
            version,
            metadata,
            description,
            properties,
            attributes,
            capabilities,
            requirements,
            interfaces,
            artifacts
        );
    }

    public static NodeType getNodeType(String derivedFrom) {
        return NodeTypeNamesMap.getOrDefault(derivedFrom, null);
    }

}
