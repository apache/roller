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
import groovy.text.TemplateEngine;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.Template;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RenderingException;

/**
 * Renderer that compiles/executes Roller Template as a Groovy Template.
 *
 * <p>Implementation notes</p>
 * 
 * <p>Uses TemplateEngine passed from RefererFactory, which is probably the 
 * right thing to do. Need to add some caching so we don't have to recompile
 * every time a tempalte is executed.</p>
 *
 * <p>Check the TemplateServlet code for some examples of template execution:</br />
 * http://svn.codehaus.org/groovy/trunk/groovy/groovy-core/src/main/groovy/servlet/ 
 */
public class GSPRenderer implements Renderer {
    private static Log log = LogFactory.getLog(GroovletRenderer.class);
    private groovy.text.Template groovyTemplate = null;   
    private Template template = null;
    private Exception parseException = null;
    
    public GSPRenderer(TemplateEngine templateEngine, Template template) {
        this.template = template;
        try {
            // TODO: implement caching for compiled templates
            groovyTemplate = templateEngine.createTemplate(template.getContents());
        } catch (Exception ex) {
            log.debug("Creating Groovy template", ex);
            parseException = ex;
        }
    }
    
    public void render(Map model, Writer writer) throws RenderingException {
        try {
            if (parseException == null) {                

                long startTime = System.currentTimeMillis();
                Binding binding = new GroovyRollerBinding(model, writer);
                groovyTemplate.make(binding.getVariables()).writeTo(writer);
                long endTime = System.currentTimeMillis();
                
                long renderTime = (endTime - startTime)/1000;
                log.debug("Rendered ["+template.getId()+"] in "+renderTime+" secs");
            } else {
                renderThrowable(parseException, writer);
            }
            
        } catch (Exception ex) {
            log.debug("Executing Groovy template", ex);
            renderThrowable(ex, writer);
        }
    }
    
    private void renderThrowable(Throwable ex, Writer writer) {
        Binding binding = new Binding();
        binding.setVariable("ex", ex);
        binding.setVariable("out", new PrintWriter(writer));
        GroovyShell shell = new GroovyShell(binding);
        shell.evaluate(
             "s = \"<p><b>Exception</b>: ${ex}<br /><b>Message</b>: ${ex.message}</p>\";"
           +" out.println(s);"
           +" out.flush();");         
    }
}


