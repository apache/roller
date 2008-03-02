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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RenderingException;

/**
 * Renderer that evaluates Roller Template as Groovy script.
 *
 * <p>Implementation notes</p>
 * 
 * <p>Executes template using GroovyShell. I'd much prefer to use the 
 * GroovyScriptEngine, but it doesn't seem flexible enough as it returns each 
 * resource as a URLConnection.</p>
 *
 * <p>Check the Groovy Servlet code for an example of GroovyScriptEngine:</br />
 * http://svn.codehaus.org/groovy/trunk/groovy/groovy-core/src/main/groovy/servlet/
 * </p>
 */
public class GroovletRenderer implements Renderer {
    private static Log log = LogFactory.getLog(GroovletRenderer.class);
    private WeblogTemplate template = null;
    
    public GroovletRenderer(WeblogTemplate template) {
        this.template = template;
    }
    
    public void render(Map model, Writer writer) throws RenderingException {
        try {
            long startTime = System.currentTimeMillis();            
            Binding binding = new GroovyRollerBinding(model, writer);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(template.getContents());              
            long endTime = System.currentTimeMillis();

            long renderTime = (endTime - startTime)/1000;
            log.debug("Rendered ["+template.getId()+"] in "+renderTime+" secs"); 
            
        } catch (Throwable ex) {
            log.debug("Executing Groovy script", ex);
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

    
