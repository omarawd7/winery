package org.eclipse.winery.lsp.Server.ServerCore;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerAPI.API.context.LSContext;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.yaml.snakeyaml.error.Mark;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ToscaLSContentImpl implements LSContext {
    private final Map<String, String> fileContents = new HashMap<>();
    private Map<LSContext.Key<?>, Object> props = new HashMap<>();
    private Map<Class<?>, Object> objects = new HashMap<>();
    private LanguageClient languageClient;
    private ClientCapabilities clientCapabilities;
    private TOSCAFile toscaFile;
    private Map<String, Mark> contextDependentConstructorPositions;
    
    public <V> void put(LSContext.Key<V> key, V value) {
        props.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <V> V get(LSContext.Key<V> key) {
        return (V) props.get(key);
    }

    public <V> void put(Class<V> clazz, V value) {
        objects.put(clazz, value);
    }

    @SuppressWarnings("unchecked")
    public <V> V get(Class<V> clazz) {
        return (V) objects.get(clazz);
    }
    
    @Override
    public void setClient(LanguageClient client) {
        this.languageClient = client;
    }

    @Override
    public LanguageClient getClient() {
        return this.languageClient;
    }

    @Override
    public void setClientCapabilities(ClientCapabilities capabilities) {
        this.clientCapabilities = capabilities;
    }

    @Override
    public Optional<ClientCapabilities> getClientCapabilities() {
        return Optional.ofNullable(this.clientCapabilities);
    }
    
    public void setFileContent(String uri, String content) {
        fileContents.put(uri, content);
    }

    public String getFileContent(String uri) {
        return fileContents.getOrDefault(uri, "");
    }

    @Override
    public TOSCAFile getToscaFile() {
        return toscaFile;
    }

    public void setToscaFile(TOSCAFile toscaFile) {
        this.toscaFile = toscaFile;
    }

    @Override
    public void setCotextDependentPositions(Map<String, Mark> contextDependentConstructorPositions) {
        this.contextDependentConstructorPositions = contextDependentConstructorPositions;
    }
    
    @Override
    public Map<String, Mark> getContextDependentConstructorPositions() {
        return contextDependentConstructorPositions;
    }
}
