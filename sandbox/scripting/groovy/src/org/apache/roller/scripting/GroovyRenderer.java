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
import groovy.lang.GroovyShell;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.Template;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RenderingException;

/**
 * Renderer that evaluates template as Groovy script.
 */
public class GroovyRenderer implements Renderer {
    private static Log log = LogFactory.getLog(GroovyRenderer.class);
    private Template template = null;
    
    public GroovyRenderer(Template template) {
        this.template = template;
    }
    
    public void render(Map model, Writer writer) throws RenderingException {
        try {
            Binding binding = new GroovyRollerBinding(model, writer);
            GroovyShell shell = new GroovyShell(binding);
            
            long startTime = System.currentTimeMillis();
            
            shell.evaluate(template.getContents());  
            
            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime)/1000;
            log.debug("Rendered ["+template.getId()+"] in "+renderTime+" secs"); 
            
        } catch (Exception ex) {
            throw new RenderingException("Error during rendering", ex);
        }
    }
}

    
