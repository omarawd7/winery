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
/**
 * Node Type
 * For more details on the TOSCA specification, visit:
 * <a href="https://docs.oasis-open.org/tosca/TOSCA/v2.0/csd06/TOSCA-v2.0-csd06.html#71-node-type">Node Type</a>
 */

package org.eclipse.winery.lsp.Server.ServerCore.DataModels;

import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaMap;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;
import java.util.Map;
import java.util.Optional;

public record NodeType(Optional<CapabilityType> derivedFrom,
                       Optional<ToscaString> version,
                       Optional<ToscaMap<String, String>> metadata,
                       Optional<ToscaString> description,
                       Optional<Map<String, PropertyDefinition>> properties,
                       Optional<ToscaMap<String, AttributeDefinition>> attributes,
                       Optional<ToscaMap<String, CapabilityType>> capabilities,
                       Optional<RequirementDefinition> requirements, 
                       Optional<ToscaMap<String,  InterfaceDefinition>> interfaces,
                       Optional<ToscaMap<String, ArtifactDefinition>> artifacts
                        
                        ) {
}
