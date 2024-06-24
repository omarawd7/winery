package org.eclpse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclpse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclpse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiagnosticsPublisher {
    private final LanguageClient client;
    private Map<String, List<Diagnostic>> previousDiagnostics = new ConcurrentHashMap<>();
    private static final LSContext.Key<DiagnosticsPublisher> DIAGNOSTICS_PUBLISHER_KEY = new LSContext.Key<>();
    
    public static DiagnosticsPublisher getInstance(LSContext serverContext) {
        DiagnosticsPublisher diagnosticsPublisher = serverContext.get(DIAGNOSTICS_PUBLISHER_KEY);
        if (diagnosticsPublisher == null) {
            diagnosticsPublisher = new DiagnosticsPublisher(serverContext);
        }
        return diagnosticsPublisher;
    }
    private DiagnosticsPublisher(LSContext serverContext) {
        serverContext.put(DIAGNOSTICS_PUBLISHER_KEY, this);
        this.client = serverContext.getClient();
    }
    
    public DiagnosticsPublisher(LanguageClient client) {
        this.client = client;
    }
    public void publishDiagnostics(BaseOperationContext context, Path path) {
        
    }
    public void SetDiagnostics(BaseOperationContext context, Path path) {

    }
    
}
