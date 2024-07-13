/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *******************************************************************************/

package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.BaseOperationContext;
import org.eclipse.winery.lsp.Server.Validation.DiagnosticsSetter;
import org.eclipse.winery.lsp.Server.Validation.TOSCAFileValidator;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
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
        TOSCAFileValidator toscaFileValidator = new TOSCAFileValidator();
        try {
            Map<String, Object> yamlMap = toscaFileParser.ParseTOSCAFile(path);
            // Validate required keywords
            toscaFileValidator.validateRequiredKeys(yamlMap, path);
            // Validate keywords and capture their positions
            toscaFileValidator.validateKeywords(yamlMap,toscaFileParser.getConstructorPositions(), toscaFileParser.getYamlContent());

            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
        catch (IOException e) {
            toscaFileValidator.handleDiagnosticsError("Failed to read TOSCA file: " + e.getMessage(), path);
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        } 
        catch (YAMLException e) {
            toscaFileValidator.handleDiagnosticsError("Failed to parse TOSCA file: " + e.getMessage(), path); 
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
        catch (IllegalArgumentException e) {
            toscaFileValidator.handleDiagnosticsError("Validation failed: " + e.getMessage(), path);
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }

    }
  
    public void publishDiagnostics(BaseOperationContext context, Path path, String content) {
        TOSCAFileParser toscaFileParser = new TOSCAFileParser();
        TOSCAFileValidator toscaFileValidator = new TOSCAFileValidator();
        try {
            Map<String, Object> yamlMap = toscaFileParser.ParseTOSCAFile(content);
            // Validate required keywords
            toscaFileValidator.validateRequiredKeys(yamlMap, content);
            // Validate keywords and capture their positions
            toscaFileValidator.validateKeywords(yamlMap,toscaFileParser.getConstructorPositions(), content);
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
        catch (YAMLException e) {
            toscaFileValidator.handleDiagnosticsError("Failed to parse TOSCA file: " + e.getMessage(), path);
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
        catch (IllegalArgumentException e) {
            toscaFileValidator.handleDiagnosticsError("Validation failed: " + e.getMessage(), path);
            List<Diagnostic> diagnostics = setDiagnostics(toscaFileValidator.diagnostics);
            previousDiagnostics.put(path.toString(), diagnostics);
            client.publishDiagnostics(new PublishDiagnosticsParams(path.toUri().toString(), diagnostics));
        }
        
    }
    
    public List<Diagnostic> setDiagnostics(ArrayList<DiagnosticsSetter> diagnostics) {
        List<Diagnostic> OutputDiagnostics = new ArrayList<>();
        for (DiagnosticsSetter diagnostic : diagnostics) {
            Diagnostic diag = new Diagnostic();
            diag.setSeverity(DiagnosticSeverity.Error);
            diag.setMessage(diagnostic.getErrorMessage());
            diag.setRange(new Range(
                    new Position(diagnostic.getErrorLine() - 1, diagnostic.getErrorColumn() - 1),
                    new Position(diagnostic.getErrorLine() - 1, diagnostic.getErrorEndColumn() + 1 )
                ));                
            
            OutputDiagnostics.add(diag);
        }
        return OutputDiagnostics;
    }
}
