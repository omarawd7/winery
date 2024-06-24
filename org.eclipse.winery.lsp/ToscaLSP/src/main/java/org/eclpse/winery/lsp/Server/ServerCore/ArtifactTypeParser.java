package org.eclpse.winery.lsp.Server.ServerCore;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArtifactTypeParser {
    private ArtifactType artifacttype ;
    private int ErrorLine;
    private int ErrorColumn;
    private String ErrorMessage;

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public int getErrorLine() {
        return ErrorLine;
    }

    public int getErrorColumn() {
        return ErrorColumn;
    }

    public String getErrorProblem() {
        return ErrorProblem;
    }

    public String getErrorContext() {
        return ErrorContext;
    }

    private String ErrorProblem;
    private String ErrorContext;
    public ArtifactType getArtifacttype() {
        return artifacttype;
    }

    public void parseYAMLFile(Path path){
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(ArtifactType.class, options);
        Yaml yaml = new Yaml(constructor);      
        // Load the YAML file
        try (InputStream inputStream = Files.newInputStream(path)) {
            // Parse the YAML file and map it to the ToscaProfile class
            artifacttype = yaml.load(inputStream);
            
        } catch (IOException e) {
        throw new RuntimeException("Failed to read the file: " + e.getMessage(), e);
        } catch (MarkedYAMLException e) {
        // Extract line and column information from the YAML exception
         ErrorLine = e.getProblemMark().getLine() + 1; // Line numbers are 0-based
         ErrorColumn = e.getProblemMark().getColumn() + 1; // Column numbers are 0-based
         ErrorProblem = e.getProblem();
         ErrorContext = e.getContext();

         ErrorMessage = String.format("YAML Parsing error at line %d, column %d: %s. Context: %s",
            ErrorLine, ErrorColumn, ErrorProblem, ErrorContext);
// diagnose detected.
            return;
        
    } catch (Exception e) {
        throw new RuntimeException("Unexpected error occurred while parsing YAML: " + e.getMessage(), e);
    }
    }
    
    
   public static void main(String[] args) {
       ArtifactTypeParser parser = new ArtifactTypeParser();
        Path AtifactTypeFilePath=Path.of("C:/Users/LAPTOP/Documents/GitHub/winery/org.eclipse.winery.lsp/ToscaLSP/src/test/resources/testArtifactType");
       parser.parseYAMLFile(AtifactTypeFilePath);
        System.out.println("Artifact type:  "+parser.getArtifacttype());
       System.out.println("Artifact type:  "+parser.getErrorProblem());
    }
}
