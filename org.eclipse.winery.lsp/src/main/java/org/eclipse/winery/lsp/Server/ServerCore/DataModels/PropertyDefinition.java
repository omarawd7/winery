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
 * Property Definition
 * For more details on the TOSCA specification, visit:
 * <a href="https://docs.oasis-open.org/tosca/TOSCA/v2.0/csd06/TOSCA-v2.0-csd06.html#93-property-definition">Property Definition</a>
 */
package org.eclipse.winery.lsp.Server.ServerCore.DataModels;

import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaBoolean;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaMap;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

@RecordBuilder
public record PropertyDefinition<T>(
    T type,
    Optional<ToscaString> description,
    Optional<ToscaMap<String, Object>> metadata,
    ToscaBoolean required,
    Optional<T> Default,
    Optional<Object> value,
    Optional<Stack<Map<String, Object>>> validation,
    Optional<SchemaDefinition> keySchema,
    Optional<SchemaDefinition> entrySchema) {

    // Method to set the validation variable
    public PropertyDefinition<T> withValidation(Stack<Map<String, Object>> newValidation) {
        return new PropertyDefinition<>(
            this.type,
            this.description,
            this.metadata,
            this.required,
            this.Default,
            this.value,
            Optional.ofNullable(newValidation),
            this.keySchema,
            this.entrySchema
        );
    }
}
