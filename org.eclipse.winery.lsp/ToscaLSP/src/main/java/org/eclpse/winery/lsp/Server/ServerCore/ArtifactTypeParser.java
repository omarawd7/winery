package org.eclpse.winery.lsp.Server.ServerCore;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArtifactTypeParser {
    public static ArtifactType parseYAMLFile( Path path){
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(ArtifactType.class, options);
        Yaml yaml = new Yaml(constructor);      
        // Load the YAML file
        try (InputStream inputStream = Files.newInputStream(path)) {
            // Parse the YAML file and map it to the ToscaProfile class
            ArtifactType artifacttype = (ArtifactType) yaml.load(inputStream);
            return artifacttype;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
   public static void main(String[] args) {
        Path AtifactTypeFilePath=Path.of("C:/Users/LAPTOP/Documents/GitHub/winery/org.eclipse.winery.lsp/ToscaLSP/src/test/resources/testArtifactType");
        ArtifactType artifactType=parseYAMLFile(AtifactTypeFilePath);
        System.out.println(artifactType);
    }
}
