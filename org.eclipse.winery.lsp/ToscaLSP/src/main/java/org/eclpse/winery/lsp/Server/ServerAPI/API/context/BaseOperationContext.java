package org.eclpse.winery.lsp.Server.ServerAPI.API.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;

public interface BaseOperationContext {
    ClientCapabilities clientCapabilities();

    LanguageClient getClient();
}
