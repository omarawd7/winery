package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.ServerInitUtils;

import java.util.concurrent.CompletableFuture;

public class ToscaLanguageServer implements LanguageClientAware, LanguageServer {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private final ToscaLSContentImpl serverContext;
    private LanguageClient client;
    private boolean shutdownInitiated = false;

    public ToscaLanguageServer(TextDocumentService textDocumentService, WorkspaceService workspaceService, ToscaLSContentImpl serverContext) {
        this.textDocumentService = textDocumentService;
        this.workspaceService = workspaceService;
        this.serverContext = serverContext;
    }

    public ToscaLanguageServer() {

        this.workspaceService = null;
        this.serverContext = new ToscaLSContentImpl();
        this.textDocumentService = new ToscaTextDocService(this.serverContext);
    }

    @Override
    public void connect(LanguageClient client) {
        
        this.client = client;
        this.serverContext.setClient(this.client);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        //setting the client capabilities
        return CompletableFuture.supplyAsync(() -> {
            serverContext.setClientCapabilities(params.
                getCapabilities());
            ServerCapabilities sCapabilities = new
                ServerCapabilities();
            TextDocumentSyncOptions documentSyncOption =
                ServerInitUtils.getDocumentSyncOption();
            CompletionOptions completionOptions =
                ServerInitUtils.getCompletionOptions();
            sCapabilities.setTextDocumentSync(documentSyncOption);
            sCapabilities.setCompletionProvider(completionOptions);
            return new InitializeResult(sCapabilities);
        });
    }

    @Override
    public void initialized(InitializedParams params) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("Tosca Language Server Initiated");
        messageParams.setType(MessageType.Info);
        this.serverContext.getClient().logMessage(messageParams);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        this.shutdownInitiated = true;
        return CompletableFuture.supplyAsync(Object::new);
    }

    @Override
    public void exit() {
        System.exit(this.shutdownInitiated ? 0 : 1);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }
}
