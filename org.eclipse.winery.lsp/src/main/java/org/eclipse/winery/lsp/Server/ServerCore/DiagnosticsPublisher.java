package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiagnosticsPublisher {
    private static final LSContext.Key<DiagnosticsPublisher> DIAGNOSTICS_PUBLISHER_KEY = new LSContext.Key<>();
    private final LanguageClient client;
    private Map<String, List<Diagnostic>> previousDiagnostics = new ConcurrentHashMap<>();

    public DiagnosticsPublisher(LanguageClient client) {
        this.client = client;
    }

    private DiagnosticsPublisher(LSContext serverContext) {
        serverContext.put(DIAGNOSTICS_PUBLISHER_KEY, this);
        this.client = serverContext.getClient();
    }
    
    public static DiagnosticsPublisher getInstance(LSContext serverContext) {
        DiagnosticsPublisher diagnosticsPublisher = serverContext.get(DIAGNOSTICS_PUBLISHER_KEY);
        if (diagnosticsPublisher == null) {
            diagnosticsPublisher = new DiagnosticsPublisher(serverContext);
        }
        return diagnosticsPublisher;
    }
    
    public void publishDiagnostics(BaseOperationContext context, Path path) {
        TOSCAFileParser toscaFileParser = new TOSCAFileParser();
        toscaFileParser.ParseTOSCAFile(path);
        List<Diagnostic> diagnostics = SetDiagnostics(toscaFileParser.diagnostics);
        previousDiagnostics.put(path.toString(), diagnostics);
        client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        
    }
  
    public void publishDiagnostics(BaseOperationContext context, Path path, String content) {
        TOSCAFileParser toscaFileParser = new TOSCAFileParser();
        toscaFileParser.ParseTOSCAFile(content);
        List<Diagnostic> diagnostics = SetDiagnostics(toscaFileParser.diagnostics);
        previousDiagnostics.put(path.toString(), diagnostics);
        client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        
    }
    
    public List<Diagnostic> SetDiagnostics(ArrayList<TOSCAFileDiagnostics> diagnostics) {
        List<Diagnostic> OutputDiagnostics = new ArrayList<>();
        for (TOSCAFileDiagnostics diagnostic : diagnostics) {
            Diagnostic diag = new Diagnostic();
            diag.setSeverity(DiagnosticSeverity.Error);
            diag.setMessage(diagnostic.getErrorMessage());
            diag.setRange(new Range(
                    new Position(diagnostic.getErrorLine() - 1, diagnostic.getErrorColumn() - 1),
                    new Position(diagnostic.getErrorLine() - 1, diagnostic.getErrorColumn() + 5 )
                ));                
            
            OutputDiagnostics.add(diag);
        }
        return OutputDiagnostics;
    }
}
