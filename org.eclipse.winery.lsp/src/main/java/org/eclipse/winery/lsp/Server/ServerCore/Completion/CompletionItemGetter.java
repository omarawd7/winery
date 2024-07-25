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
package org.eclipse.winery.lsp.Server.ServerCore.Completion;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CompletionItemGetter {
    public List<CompletionItem> getAvailableArtifactTypes(LSContext lsContext) {
        List<String> artifactTypes = new ArrayList<>();
        for (String key : lsContext.getToscaFile().artifactTypes().get().keySet()) {
            artifactTypes.add(" " + key);
        }
        return artifactTypes.stream()
            .map(type -> {
                CompletionItem item = new CompletionItem(type);
                item.setKind(CompletionItemKind.Value);
                return item;
            })
            .collect(toList());
    }

    public List<CompletionItem> getTOSCAFileKeywords(Position position) {
        List<String> keywords = List.of(
            "tosca_definitions_version:", "description:", "metadata:", "dsl_definitions:",
            "artifact_types:", "data_types:", "capability_types:", "interface_types:",
            "relationship_types:", "node_types:", "group_types:", "policy_types:",
            "repositories:", "functions:", "profile:", "imports:", "service_template:"
        );

        return keywords.stream()
            .map(keyword -> {
                CompletionItem item = new CompletionItem(keyword);
                item.setKind(CompletionItemKind.Keyword);
                // Create a TextEdit to remove the trailing space
                TextEdit textEdit = new TextEdit(
                    new Range(
                        new Position(position.getLine(), Math.max(0, position.getCharacter() - 1)), // Ensure character index is not negative
                        new Position(position.getLine(), position.getCharacter())
                    ),
                    keyword
                );
                item.setTextEdit(Either.forLeft(textEdit));
                return item;
            })
            .collect(toList());
    }
}

