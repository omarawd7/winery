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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.ToscaContext;

import java.util.List;

public class AutoCompletionHandler {
    LSContext lsContext;
    ToscaContext toscaContext;
    public AutoCompletionHandler(LSContext lsContext) {
        this.lsContext = lsContext;
    }

    public List<CompletionItem> handel(String line, Position position, String content) {
        toscaContext = new ToscaContext();
        toscaContext.buildContextStack(content, position.getLine());
        return handelCompletion(line, position, content);
    }

        private List<CompletionItem> handelCompletion(String line, Position position, String content) {
            if (line.startsWith(" ") && line.length() <= 2) { 
                // Auto complete the TOSCAFile Keywords when press space 
                CompletionItemGetter completionItemGetter = new CompletionItemGetter();
                return completionItemGetter.getTOSCAFileKeywords(position);
            }    
            // Artifact type auto-completion logic 
            if (artifactTypeCompletion(line, position) != null) {
                return artifactTypeCompletion(line, position);
            }
            // Capability type auto-completion logic
            if (capabilityTypeCompletion(line, position) != null) {
                return capabilityTypeCompletion(line, position);
            }
            return List.of();
        }
        
        private List<CompletionItem> artifactTypeCompletion(String line, Position position) {
        if (line.contains("derived_from:") && toscaContext.getContextStack() != null && !toscaContext.getContextStack().isEmpty() && toscaContext.getContextStack().peek().equals("artifact_types")) {
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getAvailableArtifactTypes(lsContext);
        } else if (line.startsWith("    ") && toscaContext.getContextStack() != null && !toscaContext.getContextStack().isEmpty() && (toscaContext.getContextStack().peek().equals("artifact_types"))) {
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getArtifactTypesKeyWords(position);
        }
        return null;
        }

        private List<CompletionItem> capabilityTypeCompletion(String line, Position position) {
        if (line.contains("derived_from:") && toscaContext.getContextStack() != null && toscaContext.getContextStack() != null && toscaContext.getContextStack().peek().equals("capability_types")) {
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getAvailableCapabilityTypes(lsContext);
        }
        else if (line.startsWith("    ") && toscaContext.getContextStack() != null && !toscaContext.getContextStack().isEmpty() && (toscaContext.getContextStack().peek().equals("capability_types"))) {
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getCapabilityTypesKeyWords(position);
        }
        return null;
        }
}
