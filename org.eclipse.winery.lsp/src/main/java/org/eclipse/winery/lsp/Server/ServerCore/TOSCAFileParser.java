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
import org.eclipse.winery.lsp.Server.ServerCore.DataModels.TOSCAFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TOSCAFileParser implements Parser {
    
    private Map<String, Mark>  ConstructorPositions;
    private TOSCAFile toscaFile;

    public Map<String, Mark> getConstructorPositions() {
        return ConstructorPositions;
    }

    public TOSCAFile getToscaFile() {
        return toscaFile;
    }

    @Override
    public Map<String, Object>  ParseTOSCAFile(Path path) throws IOException {
            String yamlContent = Files.readString(path);
            ToscaFileConstructor constructor = new ToscaFileConstructor();
            Yaml yaml = new Yaml(constructor);
            Map<String, Object> yamlMap = yaml.load(yamlContent);
            ConstructorPositions = constructor.getPositions();
            toscaFile = yaml.loadAs(yamlContent, TOSCAFile.class);
            return yamlMap;
    }

    @Override
    public Map<String, Object> ParseTOSCAFile(String content) {
            LoaderOptions options = new LoaderOptions();
            ToscaFileConstructor constructor = new ToscaFileConstructor();
            Yaml yaml = new Yaml(constructor);
            Map<String, Object> yamlMap = yaml.load(content);
            ConstructorPositions = constructor.getPositions();
            toscaFile = yaml.loadAs(content, TOSCAFile.class);
            return yamlMap;
    }
    
    public static void main(String[] args) {

    }
}
