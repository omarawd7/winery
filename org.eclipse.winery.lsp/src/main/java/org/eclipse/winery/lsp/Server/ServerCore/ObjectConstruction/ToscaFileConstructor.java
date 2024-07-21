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
import java.util.*;

public class ToscaFileConstructor {
    public static TOSCAFile ConstructToscaFile(Map<String, Object> yamlMap) {
        String toscaDefinitionsVersion = (String) yamlMap.get("tosca_definitions_version");
        Optional<String> description = Optional.ofNullable((String) yamlMap.get("description"));
        Optional<Map<String, Object>> metadata = Optional.ofNullable((Map<String, Object>) yamlMap.get("metadata"));
        Optional<Object> dslDefinitions = Optional.ofNullable(yamlMap.get("dsl_definitions"));

        Optional<Map<String, ArtifactType>> artifactTypes = Optional.ofNullable(ArtifactTypeParser.parseArtifactTypes((Map<String, Object>) yamlMap.get("artifact_types")));

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
    
}
