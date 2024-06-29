package org.eclpse.winery.lsp.Server.ServerAPI.API.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclpse.winery.lsp.Server.ServerAPI.API.ClientLogManager;
public interface BaseOperationContext {
    ClientCapabilities clientCapabilities();
    ClientLogManager clientLogManager();
    LanguageClient getClient();
}
