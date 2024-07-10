package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.ContextBuilder;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;

import java.nio.file.Path;
import java.util.List;

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
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        if (isToscaFile(uriPath)) {
            context.clientLogManager().showInfoMessage("TOSCA file opened");
            DiagnosticsPublisher diagnosticspublisher = DiagnosticsPublisher.getInstance(serverContext);
            diagnosticspublisher.publishDiagnostics(context, uriPath);
        }
    }
   
    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        if (isToscaFile(uriPath)) {
            DiagnosticsPublisher diagnosticspublisher = DiagnosticsPublisher.getInstance(serverContext);
            List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
            if (!changes.isEmpty()) {
                String content = changes.get(0).getText(); 
                diagnosticspublisher.publishDiagnostics(context, uriPath, content);
            }
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // Currently no Implementation needed
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // Currently no Implementation needed
    }

    private boolean isToscaFile(Path path) {
        String fileName = path.toString();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml") || fileName.endsWith(".tosca");
    }
}
