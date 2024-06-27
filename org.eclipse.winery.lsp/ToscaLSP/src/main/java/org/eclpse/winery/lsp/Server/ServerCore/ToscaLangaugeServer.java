package org.eclpse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;

public class ToscaLangaugeServer implements LanguageClientAware, LanguageServer {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private final ToscaLSContentImpl serverContext;
    private LanguageClient client;
    private boolean shutdownInitiated = false;

    public ToscaLangaugeServer(TextDocumentService textDocumentService, WorkspaceService workspaceService, ToscaLSContentImpl serverContext) {
        this.textDocumentService = textDocumentService;
        this.workspaceService = workspaceService;
        this.serverContext = serverContext;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        InitializeResult result = new InitializeResult(new ServerCapabilities());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void initialized(InitializedParams params) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("Tosca Language Server Initiated!");
        // No additional initialization needed
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        this.shutdownInitiated = true;
        return CompletableFuture.completedFuture(null);
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
