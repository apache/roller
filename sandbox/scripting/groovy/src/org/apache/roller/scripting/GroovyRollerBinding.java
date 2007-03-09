/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.scripting;

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.rendering.model.UtilitiesModel;

/**
 * Binds Roller model objects to Groovy context.
 */
public class GroovyRollerBinding extends Binding {
    private static Log log = LogFactory.getLog(GroovyRollerBinding.class);
    private Binding binding;
    private MarkupBuilder html;
    private Writer writer;
    
    public GroovyRollerBinding(Map model, Writer writer) {
        this.writer = writer;
        binding = new Binding(model);
        binding.setVariable("utils", new UtilitiesModel()); 
    }
        
    public void setVariable(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Can't bind variable to null key.");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Can't bind variable to blank key name. [length=0]");
        }
        if ("out".equals(name)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
        }
        if ("html".equals(name)) {
            throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'."); 
        }
        binding.setVariable(name, value);
    }

    public Map getVariables() {
        return binding.getVariables();
    }

    public Object getVariable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("No variable with null key name.");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("No variable with blank key name. [length=0]");
        }
        try {
            if ("out".equals(name)) {
                return writer;
            }
            if ("html".equals(name)) {
                if (html == null) {
                    html = new MarkupBuilder(writer);
                }
                return html;
            }
        } catch (Exception e) {
            String message = "Failed to get writer or output stream from response.";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }

        return binding.getVariable(name);
    }    
}
