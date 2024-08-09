/**
 * This class is designed to carry information between method calls.
 * <p>
 * The usage of this class was intended to encapsulate and transport data 
 * needed for various operations within the system, ensuring that 
 * all required information is consistently and reliably available 
 * throughout the execution flow.
 * </p>
 *
 */
package org.eclipse.winery.lsp.Server.ServerAPI.API.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.yaml.snakeyaml.error.Mark;

import java.util.Map;
import java.util.Optional;

public interface LSContext {
    
    <V> void put(LSContext.Key<V> key, V value);

    <V> V get(LSContext.Key<V> key);

    <V> void put(Class<V> clazz, V value);

    <V> V get(Class<V> clazz);

    void setClient(LanguageClient client);

    LanguageClient getClient();

    void setClientCapabilities(ClientCapabilities capabilities);

    Optional<ClientCapabilities> getClientCapabilities();

    String getFileContent(String uri);
    
    void setFileContent(String uri, String content);

    TOSCAFile getToscaFile();
    
    void setToscaFile(TOSCAFile toscaFile);

    void setCotextDependentPositions(Map<String, Mark> contextDependentConstructorPositions);
    
    Map<String, Mark> getContextDependentConstructorPositions() ;
    
    class Key<K> { }

}
