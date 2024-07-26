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
package org.eclipse.winery.lsp.Server.ServerCore.TOSCAFunctions;

import org.yaml.snakeyaml.Yaml;
import java.util.List;
import java.util.Map;

public class FunctionParser {
    private static final Yaml yaml = new Yaml();
    private  String key;
    private List<Object> arguments;

    public String getKey() {
        return key;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public  void parseFunctionCall(String line) {
        line = line.trim();
        if (line.startsWith("{") && line.endsWith("}")) {
            line = line.substring(1, line.length() - 1).trim();

            if (line.startsWith("$")) {
                if (line.length() > 1 && line.charAt(1) == '$') {
                    // Escape: Discard the first $
                    line = line.substring(1);
                    System.out.println("Escaped line: " + line);
                    return;
                } else {
                    // It's a function call
                    int colonIndex = line.indexOf(':');
                    if (colonIndex != -1) {
                        key = line.substring(0, colonIndex).trim();
                        String value = line.substring(colonIndex + 1).trim();

                        // Check if it's the only key in a YAML map using SnakeYAML
                        Map<String, Object> map = yaml.load("{" + key + ": " + value + "}");
                        if (map.size() == 1) {
                            arguments = parseArguments(value);
                            System.out.println("Function call: " + key);
                            System.out.println("Arguments: " + arguments);
                        } else {
                            throw new IllegalArgumentException("Malformed function: more than one key in the map");
                        }
                    } else {
                        System.out.println("Function call without arguments: " + line);
                    }
                }
            } else {
                System.out.println("Not a function call: " + line);
            }
        } else {
            throw new IllegalArgumentException("Malformed input: missing curly brackets");
        }
    }

    private static List<Object> parseArguments(String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            return yaml.load(value);
        } else {
            throw new IllegalArgumentException("Malformed arguments: not a valid list");
        }
    }

    public static void main(String[] args) {
        FunctionParser fp  = new FunctionParser();
        try {
            fp.parseFunctionCall("{ $get_attribute: [ my_server, private_address ] }");
            fp.parseFunctionCall("{ $valid_values: [ $value, [ 1, 2, 4, 8 ] ] }");
            fp.parseFunctionCall("{ $$escaped_line }");
            fp.parseFunctionCall("{ not_a_function }");
            fp.parseFunctionCall("{ $greater_or_equal: [ $value, 0 ] }");
            fp.parseFunctionCall("{ $greater_or_equal: [ { $value: [] }, 0 ] }");
            fp.parseFunctionCall("{ $value }");
            fp.parseFunctionCall("{ $value }");
            fp.parseFunctionCall("{ $value:[]}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
