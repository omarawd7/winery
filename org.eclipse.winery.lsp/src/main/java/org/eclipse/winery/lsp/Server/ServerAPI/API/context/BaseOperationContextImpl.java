package org.eclipse.winery.lsp.Server.ServerAPI.API.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerAPI.API.ClientLogManager;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.ClientLogManagerImpl;

public class BaseOperationContextImpl implements BaseOperationContext {
    private final LSContext serverContext;

    public BaseOperationContextImpl(LSContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public ClientCapabilities clientCapabilities() {
        return serverContext.getClientCapabilities().orElseThrow();
    }

    @Override
    public ClientLogManager clientLogManager() {
        return ClientLogManagerImpl.getInstance(this.serverContext);
    }

    @Override
    public LanguageClient getClient() {
        return this.serverContext.getClient();
    }
    
}
