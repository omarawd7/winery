package org.eclpse.winery.lsp.Server.ServerCore;

import java.util.List;

public class ArtifactType {
  
    public ArtifactType() {
    }

    public ArtifactType( String description, String derived_from, String mime_type, List<String> file_ext) {
        this.description = description;
        this.derived_from = derived_from;
        this.mime_type = mime_type;
        this.file_ext = file_ext;
    }

    private String description;
    private String derived_from;
    private String mime_type;
    private List<String> file_ext;

    // Getters and Setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDerived_from() {
        return derived_from;
    }

    public void setDerived_from(String derived_from) {
        this.derived_from = derived_from;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public List<String> getFile_ext() {
        return file_ext;
    }

    public void setFile_ext(List<String> file_ext) {
        this.file_ext = file_ext;
    }

    @Override
    public String toString() {
        return "ArtifactType{" +
            "description='" + description + '\'' +
            ", derived_from='" + derived_from + '\'' +
            ", mime_type='" + mime_type + '\'' +
            ", file_ext=" + file_ext +
            '}';
    }
}
