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

import java.util.List;

public class AutoCompletionHandler {
    LSContext lsContext;

    public AutoCompletionHandler(LSContext lsContext) {
        this.lsContext = lsContext;
    }

    public List<CompletionItem> handel(String line, Position position) {
        return ArtifactTypeCompletion(line, position);
    }

    private List<CompletionItem> ArtifactTypeCompletion(String line, Position position) {
        if (line.contains("derived_from:")) {
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getAvailableArtifactTypes(lsContext);
        }
        //TODO add context dependent
        if (line.trim().isEmpty()) { // Auto complete the TOSCAFile Keywords when press space 
            CompletionItemGetter completionItemGetter = new CompletionItemGetter();
            return completionItemGetter.getTOSCAFileKeywords(position);
        }
        return List.of();
    }
}
