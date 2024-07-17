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
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;
import java.util.stream.Collectors;

public class CompletionItemGetter {
    public List<CompletionItem> getArtifactTypes() {
        List<String> artifactTypes = List.of(
            "Root",
            "File",
            "Deployment",
            "Deployment.Image",
            "Deployment.Image.VM",
            "Implementation",
            "Implementation.Bash",
            "Implementation.Python"
        );

        return artifactTypes.stream()
            .map(type -> {
                CompletionItem item = new CompletionItem(type);
                item.setKind(CompletionItemKind.Value);
                return item;
            })
            .collect(Collectors.toList());
    }
}
