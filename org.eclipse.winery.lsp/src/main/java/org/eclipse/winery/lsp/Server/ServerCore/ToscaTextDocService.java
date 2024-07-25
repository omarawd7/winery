package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.ContextBuilder;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.Completion.AutoCompletionHandler;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.eclipse.winery.lsp.Server.ServerCore.Validation.DiagnosticsPublisher;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ToscaTextDocService implements TextDocumentService {
    private final LSContext serverContext;

    public ToscaTextDocService(LSContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("A file was opened");
        messageParams.setType(MessageType.Info);
        this.serverContext.getClient().logMessage(messageParams);
        String uri = params.getTextDocument().getUri();
        Path uriPath = CommonUtils.uriToPath(uri);
        String content = params.getTextDocument().getText();
        serverContext.setFileContent(uri, content);
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        if (CommonUtils.isToscaFile(uriPath)) {
            context.clientLogManager().showInfoMessage("TOSCA file opened");
            DiagnosticsPublisher diagnosticspublisher = DiagnosticsPublisher.getInstance(serverContext);
            diagnosticspublisher.publishDiagnostics(serverContext, uriPath);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        Path uriPath = CommonUtils.uriToPath(uri);
        if (CommonUtils.isToscaFile(uriPath)) {
            DiagnosticsPublisher diagnosticspublisher = DiagnosticsPublisher.getInstance(serverContext);
            List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
            if (!changes.isEmpty()) {
                String content = changes.get(0).getText();
                diagnosticspublisher.publishDiagnostics(serverContext, uriPath, content);
                serverContext.setFileContent(uri, content);
            }
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        serverContext.setFileContent(uri, "");
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // Currently no Implementation needed
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        String content = serverContext.getFileContent(uri);
        String line = content.split("\n")[position.getLine()];
        AutoCompletionHandler autoCompletionHandler = new AutoCompletionHandler(serverContext);
        List<CompletionItem> completionItems = autoCompletionHandler.handel(line,position);
        return CompletableFuture.completedFuture(Either.forLeft(completionItems));
    }

}
