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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.PropertyDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class ArtifactTypeParser {
    private static final Map<String, ArtifactType> artifactTypesNamesMap = new HashMap<>();
    public static Map<String, ArtifactType> parseArtifactTypes(Map<String, Object> artifactTypesMap) {
        if (artifactTypesMap == null) {
            return Collections.emptyMap();
        }
        return artifactTypesMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    ArtifactType artifactType = ArtifactTypeParser.parseArtifactType((Map<String, Object>) e.getValue());
                    artifactTypesNamesMap.put(e.getKey(), artifactType);
                    return artifactType;
                }
            ));
    }
    
    public static ArtifactType parseArtifactType(Map<String, Object> artifactTypeMap) {
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
        Optional<Map<String, PropertyDefinition>> properties = Optional.ofNullable(PropertyDefinitionParser.parseProperties((Map<String, Object>) artifactTypeMap.get("properties")));

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

    public static ArtifactType getArtifactType(String derivedFrom) {
        return artifactTypesNamesMap.getOrDefault(derivedFrom, null);
    }

}
