package org.eclipse.winery.lsp.Server.ServerAPI.API.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Optional;

public interface LSContext {
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
    
    <V> void put(LSContext.Key<V> key, V value);

    <V> V get(LSContext.Key<V> key);

    <V> void put(Class<V> clazz, V value);

    <V> V get(Class<V> clazz);

    void setClient(LanguageClient client);

    LanguageClient getClient();

    void setClientCapabilities(ClientCapabilities capabilities);

    Optional<ClientCapabilities> getClientCapabilities();

    class Key<K> {
    }

}
