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

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.ArtifactType;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaList;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaMap;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;

import java.util.*;

public class ToscaFileConstructor {
    public static TOSCAFile ConstructToscaFile(Map<String, Object> yamlMap) {
        ToscaString toscaDefinitionsVersion = new ToscaString ((String) yamlMap.get("tosca_definitions_version"));
        Optional<ToscaString> description = Optional.of(new ToscaString( (String) yamlMap.get("description")));
        Optional<ToscaMap<String, Object>> metadata = Optional.of(new ToscaMap<>(((Map<String, Object>) yamlMap.get("metadata"))));
        Optional<Object> dslDefinitions = Optional.ofNullable(yamlMap.get("dsl_definitions"));
        Optional<Map<String, ArtifactType>> artifactTypes = Optional.ofNullable(ArtifactTypeParser.parseArtifactTypes((Map<String, Object>) yamlMap.get("artifact_types")));

        Optional<ToscaMap<String, Object>> dataTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("data_types")));
        Optional<ToscaMap<String, Object>> capabilityTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("capability_types")));
        Optional<ToscaMap<String, Object>> interfaceTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("interface_types")));
        Optional<ToscaMap<String, Object>> relationshipTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("relationship_types")));
        Optional<ToscaMap<String, Object>> nodeTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("node_types")));
        Optional<ToscaMap<String, Object>> groupTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("group_types")));
        Optional<ToscaMap<String, Object>> policyTypes = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("policy_types")));
        Optional<ToscaMap<String, Object>> repositories = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("repositories")));
        Optional<ToscaMap<String, Object>> functions = Optional.of(new ToscaMap<>((Map<String, Object>) yamlMap.get("functions")));
        Optional<ToscaString> profile = Optional.of(new ToscaString((String) yamlMap.get("profile")));
        Optional<ToscaList<Object>> imports = Optional.of(new ToscaList<>((List<Object>) yamlMap.get("imports")));
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
    
}
