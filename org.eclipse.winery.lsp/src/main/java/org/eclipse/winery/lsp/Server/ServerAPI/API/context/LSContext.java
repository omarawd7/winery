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

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    TOSCAFile getCurrentToscaFile();
    
    void setCurrentToscaFile(TOSCAFile currentToscaFile);

    void setCotextDependentPositions(Map<String, Mark> contextDependentConstructorPositions);
    
    Map<String, Mark> getContextDependentConstructorPositions() ;

    Set<Path> getDirectoryFilePaths();

    void setDirectoryFilePaths(Set<Path> directoryFilePaths) ;

    Path getCurrentToscaFilePath();
    
    void setCurrentToscaFilePath(Path currentToscaFilePath);

    Map<String, TOSCAFile> getNamespaceDefinitions();

    void setNamespaceDefinitions(Map<String, TOSCAFile> namespaceDefinitions);
    
    Map<String, Path> getProfilePaths();

    void setProfilePaths(Map<String, Path> profilePaths);
    
    Map<String, TOSCAFile> getImportedToscaFiles();
    
    void setImportedToscaFiles(Map<String, TOSCAFile> importedToscaFiles);

    Map<Path, TOSCAFile> getToscaFilesPath();

    void setToscaFilesPath(Map<Path, TOSCAFile> toscaFilesPath);
    
    class Key<K> { }

}
