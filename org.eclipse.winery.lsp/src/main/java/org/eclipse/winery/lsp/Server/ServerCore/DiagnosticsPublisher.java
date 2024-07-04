package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;

import java.nio.file.Path;
import java.util.Collections;
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
        ArtifactTypeParser artifactTypeParser = new ArtifactTypeParser();
        artifactTypeParser.parseYAMLFile(path);
        if (artifactTypeParser.getErrorMessage() != null) {
            Diagnostic diag =SetDiagnostics(artifactTypeParser);
            List<Diagnostic> diagnostics = Collections.singletonList(diag);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
    }
    
    public Diagnostic SetDiagnostics(ArtifactTypeParser artifactTypeParser) {
        Diagnostic diag = new Diagnostic();
        diag.setSeverity(DiagnosticSeverity.Error);
        diag.setMessage(artifactTypeParser.getErrorMessage());
        diag.setRange(new Range(
            new Position(artifactTypeParser.getErrorLine() - 1, artifactTypeParser.getErrorColumn() - 1),
            new Position(artifactTypeParser.getErrorLine() - 1, artifactTypeParser.getErrorColumn())
        ));
        return diag;
    }
    
}
