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
 * Artifact Type Record
 *
 * For more details on the TOSCA specification, visit:
 * https://docs.oasis-open.org/tosca/TOSCA/v2.0/csd06/TOSCA-v2.0-csd06.html#121-artifact-type
 */
package org.eclipse.winery.lsp.Server.ServerCore.DataModels;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ArtifactType (
    Optional<String> derived_from,
    Optional<String> version,
    Optional<Map<String, String>> metadata,
    Optional<String> description,
    Optional<String> mime_type,
    Optional<List<String>> file_ext,
    Optional<Map<String, PropertyDefinition>> properties
) {
}

record PropertyDefinition () {
//TODO properties for the PropertyDefinition record
}
