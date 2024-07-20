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
package org.eclipse.winery.lsp.Server.ServerCore.Parsing;

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.ArtifactType;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.PropertyDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;

import java.util.*;
import java.util.stream.Collectors;

public class ToscaFileConstructor {
    private static final Map<String, ArtifactType> artifactTypesNamesMap = new HashMap<>();
    public static TOSCAFile ConstructToscaFile(Map<String, Object> yamlMap) {
        String toscaDefinitionsVersion = (String) yamlMap.get("tosca_definitions_version");
        Optional<String> description = Optional.ofNullable((String) yamlMap.get("description"));
        Optional<Map<String, Object>> metadata = Optional.ofNullable((Map<String, Object>) yamlMap.get("metadata"));
        Optional<Object> dslDefinitions = Optional.ofNullable(yamlMap.get("dsl_definitions"));

        Optional<Map<String, ArtifactType>> artifactTypes = Optional.ofNullable(parseArtifactTypes((Map<String, Object>) yamlMap.get("artifact_types")));

        Optional<Map<String, Object>> dataTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("data_types"));
        Optional<Map<String, Object>> capabilityTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("capability_types"));
        Optional<Map<String, Object>> interfaceTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("interface_types"));
        Optional<Map<String, Object>> relationshipTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("relationship_types"));
        Optional<Map<String, Object>> nodeTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("node_types"));
        Optional<Map<String, Object>> groupTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("group_types"));
        Optional<Map<String, Object>> policyTypes = Optional.ofNullable((Map<String, Object>) yamlMap.get("policy_types"));
        Optional<Map<String, Object>> repositories = Optional.ofNullable((Map<String, Object>) yamlMap.get("repositories"));
        Optional<Map<String, Object>> functions = Optional.ofNullable((Map<String, Object>) yamlMap.get("functions"));
        Optional<String> profile = Optional.ofNullable((String) yamlMap.get("profile"));
        Optional<List<Object>> imports = Optional.ofNullable((List<Object>) yamlMap.get("imports"));
        Optional<Object> serviceTemplate = Optional.ofNullable(yamlMap.get("service_template"));

        return new TOSCAFile(
            toscaDefinitionsVersion,
            description,
            metadata,
            dslDefinitions,
            artifactTypes,
            dataTypes,
            capabilityTypes,
            interfaceTypes,
            relationshipTypes,
            nodeTypes,
            groupTypes,
            policyTypes,
            repositories,
            functions,
            profile,
            imports,
            serviceTemplate
        );
    }

    private static Map<String, ArtifactType> parseArtifactTypes(Map<String, Object> artifactTypesMap) {
        if (artifactTypesMap == null) {
            return Collections.emptyMap();
        }
        return artifactTypesMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    ArtifactType artifactType = parseArtifactType((Map<String, Object>) e.getValue());
                    artifactTypesNamesMap.put(e.getKey(), artifactType);
                    return artifactType;
                } 
            ));
    }

    private static ArtifactType parseArtifactType(Map<String, Object> artifactTypeMap) {
        if (artifactTypeMap == null) {
            return null;
        }

        Optional<ArtifactType> derivedFrom = null;
        try {
            derivedFrom = Optional.ofNullable(getArtifactType((String) artifactTypeMap.get("derived_from")));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        Optional<String> version = Optional.ofNullable((String) artifactTypeMap.get("version"));
        Optional<Map<String, String>> metadata = Optional.ofNullable((Map<String, String>) artifactTypeMap.get("metadata"));
        Optional<String> description = Optional.ofNullable((String) artifactTypeMap.get("description"));
        Optional<String> mimeType = Optional.ofNullable((String) artifactTypeMap.get("mime_type"));
        Optional<List<String>> fileExt = Optional.ofNullable((List<String>) artifactTypeMap.get("file_ext"));
        Optional<Map<String, PropertyDefinition>> properties = Optional.ofNullable(parseProperties((Map<String, Object>) artifactTypeMap.get("properties")));

        return new ArtifactType(
            derivedFrom,
            version,
            metadata,
            description,
            mimeType,
            fileExt,
            properties
        );
    }

    private static ArtifactType getArtifactType(String derivedFrom) {
        return artifactTypesNamesMap.getOrDefault(derivedFrom, null);
    }

    private static Map<String, PropertyDefinition> parseProperties(Map<String, Object> propertiesMap) {
        if (propertiesMap == null) {
            return Collections.emptyMap();
        }
        return propertiesMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> parsePropertyDefinition((Map<String, Object>) e.getValue())
            ));
    }

    private static PropertyDefinition parsePropertyDefinition(Map<String, Object> propertyDefinitionMap) {
        if (propertyDefinitionMap == null) {
            return null;
        }
        String type = (String) propertyDefinitionMap.get("type");
        Optional<String> description = Optional.ofNullable((String) propertyDefinitionMap.get("description"));
        Optional<Map<String, Object>> metadata = Optional.ofNullable((Map<String, Object>) propertyDefinitionMap.get("metadata"));
        Optional<Boolean> required = Optional.ofNullable((Boolean) propertyDefinitionMap.get("required"));
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
