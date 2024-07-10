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
 * TOSCA File Record
 *
 * For more details on the TOSCA specification, visit:
 * https://docs.oasis-open.org/tosca/TOSCA/v2.0/csd06/TOSCA-v2.0-csd06.html#61-keynames
 */
package org.eclipse.winery.lsp.Server.ServerCore.DataModels;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RecordBuilder
public record TOSCAFile(
    String tosca_definitions_version,
    Optional<String> description,
    Optional<Map<String, Object>> metadata,
    Optional<Object> dsl_definitions,
    Optional<Map<String, ArtifactType>> artifact_types,
    Optional<Map<String, Object>> data_types,
    Optional<Map<String, Object>> capability_types,
    Optional<Map<String, Object>> interface_types,
    Optional<Map<String, Object>> relationship_types,
    Optional<Map<String, Object>> node_types,
    Optional<Map<String, Object>> group_types,
    Optional<Map<String, Object>> policy_types,
    Optional<Map<String, Object>> repositories,
    Optional<Map<String, Object>> functions,
    Optional<String> profile,
    Optional<List<Object>> imports,
    Optional<Object> service_template) { } 
