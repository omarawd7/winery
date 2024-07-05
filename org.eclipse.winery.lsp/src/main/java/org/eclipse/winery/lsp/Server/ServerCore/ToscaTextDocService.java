package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.ContextBuilder;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;

import java.nio.file.Path;

public class ToscaTextDocService implements TextDocumentService {
    private final LSContext serverContext;

    public ToscaTextDocService(LSContext serverContext) {
        this.serverContext = serverContext;
    }
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("A file was opened!");
        messageParams.setType(MessageType.Info);
        this.serverContext.getClient().logMessage(messageParams);
        
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        if (uriPath.toFile().getName().endsWith(".txt")) {
            // Here we notify that we have opened a .txt document.
            context.clientLogManager().showInfoMessage("Document opened with `.txt` extension");
            return;
        }else if (uriPath.toFile().getName().endsWith(".yaml")) {
            // Here we notify that we have opened a .txt document.
            context.clientLogManager().showInfoMessage("Document opened with `.yaml` extension");
            DiagnosticsPublisher diagnosticspublisher =DiagnosticsPublisher.getInstance(serverContext);
            diagnosticspublisher.publishDiagnostics(context,uriPath);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        if (uriPath.toFile().getName().endsWith(".txt")) {
            // Here we notify that we have opened a .txt document.
            context.clientLogManager().showInfoMessage("Document opened with `.txt` extension");
            return;
        }else if (uriPath.toFile().getName().endsWith(".yaml")) {
            // Here we notify that we have opened a .txt document.
            context.clientLogManager().showInfoMessage("Document opened with `.yaml` extension");
            DiagnosticsPublisher diagnosticspublisher =DiagnosticsPublisher.getInstance(serverContext);
            diagnosticspublisher.publishDiagnostics(context,uriPath);
        }
    }

    @Override   
    public void didClose(DidCloseTextDocumentParams params) {
        
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }
}