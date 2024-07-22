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

import org.eclipse.winery.lsp.Server.ServerCore.DataModels.SchemaDefinition;
import org.eclipse.winery.lsp.Server.ServerCore.TOSCADataTypes.ToscaString;

import java.util.Map;
import java.util.Optional;

public class SchemaDefenitionParser {
    public static SchemaDefinition parseSchemaDefinition(Map<String, Object> Schema) {
        if (Schema == null) { return null; }
        ToscaString type = new ToscaString((String) Schema.get("type"));
        Optional<ToscaString> description = Optional.ofNullable(new ToscaString((String) Schema.get("description")));
        Optional<Object> validation = Optional.ofNullable(Schema.get("validation"));
        Optional<SchemaDefinition> keySchema = Optional.empty();
        Optional<SchemaDefinition> entrySchema = Optional.empty();
        try {
            keySchema = Optional.ofNullable(parseSchemaDefinition((Map<String, Object>) Schema.getOrDefault("key_schema",null)));
            entrySchema = Optional.ofNullable(parseSchemaDefinition((Map<String, Object>) Schema.getOrDefault("entrySchema",null)));
        } catch (Exception e) {
            System.err.println("Error parsing Schema");
        }
        return new SchemaDefinition(
            type,
            description,
            validation,
            keySchema,
            entrySchema
        );
    }

}
