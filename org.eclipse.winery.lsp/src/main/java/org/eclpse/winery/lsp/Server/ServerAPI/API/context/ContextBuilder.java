package org.eclpse.winery.lsp.Server.ServerAPI.API.context;

public class ContextBuilder {
    public static BaseOperationContext baseContext(LSContext serverContext) {
        return new BaseOperationContextImpl(serverContext);
    }

}
