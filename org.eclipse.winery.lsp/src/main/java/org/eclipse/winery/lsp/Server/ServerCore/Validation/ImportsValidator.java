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

package org.eclipse.winery.lsp.Server.ServerCore.Validation;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.Parsing.TOSCAFileParser;
import org.eclipse.winery.lsp.Server.ServerCore.Utils.CommonUtils;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportsValidator implements DiagnosesHandler {
    public ArrayList<DiagnosticsSetter> diagnostics = new ArrayList<>();
    private LSContext context;
    
    public ImportsValidator(LSContext context) {
     this.context = context;
    }

    public ArrayList<DiagnosticsSetter> validateImports(List<Object> importsList, Map<String, Mark> positions, String yamlContent, String[] lines) {
        Set<String> validImportsKeywords = Set.of(
            "url", "profile", "repository", "namespace", "description", "metadata"  
        );
        for (Object importElement: importsList) {
            if (importElement instanceof Map) {
                for (String importsKey : ((Map<String, Object>) importElement).keySet()) {
                    if (!validImportsKeywords.contains(importsKey)) {
                        Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + importsKey);
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                        handleNotValidKeywords("Invalid imports keyword: " + importsKey + " at line " + line + ", column " + column, line, column, endColumn);
                    }
                    if (((Map<?, ?>) importElement).containsKey("url") && ((Map<?, ?>) importElement).containsKey("profile") ) {
                        Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "url");
                        int line = mark != null ? mark.getLine() + 1 : -1;
                        int column = mark != null ? mark.getColumn() + 1 : -1;
                        int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                        handleNotValidKeywords("Import statement must include either a url or a profile, but not both.", line, column, endColumn);
    
                        mark = context.getContextDependentConstructorPositions().get("imports" + "." + "profile");
                        line = mark != null ? mark.getLine() + 1 : -1;
                        column = mark != null ? mark.getColumn() + 1 : -1;
                        endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                        handleNotValidKeywords("Import statement must include either a url or a profile, but not both.", line, column, endColumn);
                     } else if (((Map<?, ?>) importElement).get(importsKey) != null && importsKey.equals("url")) {
                        validateURL(yamlContent, lines, (Map<?, ?>) importElement, importsKey);
                    } else if (((Map<?, ?>) importElement).get(importsKey) != null && importsKey.equals("profile")) {
                        validateProfile(yamlContent, lines, (Map<?, ?>) importElement, importsKey);
                    }
                }
                if (((Map<?, ?>) importElement).containsKey("repository") && !((Map<?, ?>) importElement).containsKey("url") ) {
                    Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "repository");
                    int line = mark != null ? mark.getLine() + 1 : -1;
                    int column = mark != null ? mark.getColumn() + 1 : -1;
                    int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                    handleNotValidKeywords("The repository name can only be used when a url is specified.", line, column, endColumn);
                }
            } else {
                Mark mark = context.getContextDependentConstructorPositions().get("imports");
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                handleNotValidKeywords("Invalid imports", line, column, endColumn);
            }
        }
        return diagnostics;
    }

    private void validateProfile(String yamlContent, String[] lines, Map<?, ?> importElement, String importsKey) {
        if (importElement.get(importsKey) instanceof String) {
            String profileValue = ((String) importElement.get(importsKey));
            TOSCAFileParser toscaFileParser = new TOSCAFileParser();
            try {
                Boolean isFileExist = false;
                for (Path ToscaFilePath: context.getDirectoryFilePaths()) {
                    if (CommonUtils.isToscaFile(ToscaFilePath)) {
                    toscaFileParser.ParseTOSCAFile(ToscaFilePath,context.getClient());

                    if ( toscaFileParser.getToscaFile() != null && toscaFileParser.getToscaFile().profile().isPresent() && toscaFileParser.getToscaFile().profile().get().getValue().equals(profileValue)) {
                        if (context.getImportedToscaFiles().containsKey(context.getCurrentToscaFilePath())) {
                            context.getImportedToscaFiles().get(context.getCurrentToscaFilePath()).clear();
                        }
                        context.getImportedToscaFiles().put(context.getCurrentToscaFilePath() ,Map.of(profileValue,toscaFileParser.getToscaFile()));
                        isFileExist = true;
                        if (importElement.get("namespace") != null) {
                            if (importElement.get("namespace") instanceof String) {
                                String namespace = (String) importElement.get("namespace");
                                if (context.getNamespaceDefinitions().containsKey(context.getCurrentToscaFilePath())) {
                                    context.getNamespaceDefinitions().get(context.getCurrentToscaFilePath()).clear();
                                }
                                context.getNamespaceDefinitions().put(context.getCurrentToscaFilePath() ,Map.of(namespace, toscaFileParser.getToscaFile()));
                            }
                        }
                        break;
                    }
                }
                }
                if (!isFileExist) {
                    Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "profile" + "." + importElement.get(importsKey));
                    int line = mark != null ? mark.getLine() + 1 : -1;
                    int column = mark != null ? mark.getColumn() + 1 : -1;
                    int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);
                    handleNotValidKeywords("Tosca File not found.", line, column, endColumn); 
                }
            } catch (Exception e) {
                Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "profile");
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
                handleNotValidKeywords(e.getMessage(), line, column, endColumn);
            }
        } else {
            Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "profile");
            int line = mark != null ? mark.getLine() + 1 : -1;
            int column = mark != null ? mark.getColumn() + 1 : -1;
            int endColumn = CommonUtils.getEndColumn(yamlContent, line, column, lines);
            handleNotValidKeywords("Profile value should be string", line, column, endColumn);
        }
    }

    private void validateURL(String yamlContent, String[] lines, Map<?, ?> importElement, String importsKey) {
        if (importElement.get(importsKey) instanceof String) {
            String url = (String) importElement.get(importsKey);
            Path currentFilePath = context.getCurrentToscaFilePath();
            TOSCAFileParser toscaFileParser = new TOSCAFileParser();
            try {
                Path ImportedToscaFilePath = currentFilePath.getParent().resolve(url);
                if (CommonUtils.isToscaFile(ImportedToscaFilePath)) {
                    toscaFileParser.ParseTOSCAFile(ImportedToscaFilePath,context.getClient());
                    context.getToscaFilesPath().put(currentFilePath, toscaFileParser.getToscaFile());
                    if (context.getImportedToscaFiles().containsKey(context.getCurrentToscaFilePath())) {
                        context.getImportedToscaFiles().get(context.getCurrentToscaFilePath()).clear();
                    }
                    context.getImportedToscaFiles().put(currentFilePath ,Map.of(url,toscaFileParser.getToscaFile()));
                    if (importElement.get("namespace") != null) {
                    if (importElement.get("namespace") instanceof String) {
                        String namespace = (String) importElement.get("namespace");
                        if (context.getNamespaceDefinitions().containsKey(context.getCurrentToscaFilePath())) {
                            context.getNamespaceDefinitions().get(context.getCurrentToscaFilePath()).clear();
                        }
                        context.getNamespaceDefinitions().put(context.getCurrentToscaFilePath() ,Map.of(namespace, toscaFileParser.getToscaFile()));
                    }
                }
                }
            } catch (Exception e) {
                Mark mark = context.getContextDependentConstructorPositions().get("imports" + "." + "url");
                int line = mark != null ? mark.getLine() + 1 : -1;
                int column = mark != null ? mark.getColumn() + 1 : -1;
                int endColumn = CommonUtils.getEndColumnForValueError(yamlContent, line, column, lines);
                handleNotValidKeywords(e.getMessage(), line, column, endColumn);
            }
        }
    }

    @Override
    public void handleNotValidKeywords(String message, int line, int column, int endColumn) {
        DiagnosticsSetter ArtifactTypeDiagnostic = new DiagnosticsSetter();
        ArtifactTypeDiagnostic.setErrorMessage(message);
        ArtifactTypeDiagnostic.setErrorContext("Not Valid Keywords");
        ArtifactTypeDiagnostic.setErrorColumn(column);
        ArtifactTypeDiagnostic.setErrorEndColumn(endColumn);
        ArtifactTypeDiagnostic.setErrorLine(line);
        diagnostics.add(ArtifactTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, Path path) {
        DiagnosticsSetter ArtifactTypeDiagnostic = new DiagnosticsSetter();
        ArtifactTypeDiagnostic.setErrorMessage(message);
        ArtifactTypeDiagnostic.setErrorContext("Parsing Error");
        try {
            long lineCount = Files.lines(path).count();
            ArtifactTypeDiagnostic.setErrorLine((int) lineCount);
        } catch (IOException e) {
            ArtifactTypeDiagnostic.setErrorLine(-1);
        }
        ArtifactTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(ArtifactTypeDiagnostic);
    }

    @Override
    public void handleDiagnosticsError(String message, String content) {
        DiagnosticsSetter ArtifactTypeDiagnostic = new DiagnosticsSetter();
        ArtifactTypeDiagnostic.setErrorMessage(message);
        ArtifactTypeDiagnostic.setErrorContext("Parsing Error");
        ArtifactTypeDiagnostic.setErrorLine(countLines(content));
        ArtifactTypeDiagnostic.setErrorColumn(1);
        diagnostics.add(ArtifactTypeDiagnostic);
    }

    private int countLines(String content) {
        return (int) content.lines().count();
    }

}
