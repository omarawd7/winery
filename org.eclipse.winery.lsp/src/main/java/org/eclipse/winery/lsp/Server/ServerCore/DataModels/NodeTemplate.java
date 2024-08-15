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
 * Node Template Definition
 * For more details on the TOSCA specification, visit:
 * <a href="https://docs.oasis-open.org/tosca/TOSCA/v2.0/csd06/TOSCA-v2.0-csd06.html#72-node-template">Node Template Definition</a>
 */
package org.eclipse.winery.lsp.Server.ServerCore.DataModels;

import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.*;
import java.util.Map;
import java.util.Optional;

public record NodeTemplate(ToscaString type,
                           Optional<ToscaString> description,
                           Optional<ToscaList<String>> directives,
                           Optional<ToscaMap<String, Object>> metadata,
                           Optional<Map<String, PropertyDefinition>> properties,
                           Optional<ToscaMap<String, AttributeDefinition>> attributes,
                           Optional<ToscaMap<String, CapabilityType>> capabilities,
                           Optional<RequirementAssignment> requirements,
                           Optional<ToscaMap<String, InterfaceAssignment>> interfaces,
                           Optional<ToscaMap<String, ArtifactDefinition>> artifacts,
                           Optional<ToscaInteger> count, //TODO must be non negative
                           Optional<Object> node_filter, //TODO look for TOSCA spec 8.8 condition_clause to replace the object type with a stack of functions or something
                           Optional<ToscaString> copy) { }
