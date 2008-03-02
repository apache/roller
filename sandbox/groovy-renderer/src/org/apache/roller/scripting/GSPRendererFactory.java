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

import groovy.text.SimpleTemplateEngine;
import groovy.text.TemplateEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererFactory;

/**
 * RendererFactory that handles Groovy Server Page (GSP) style templates 
 * w/language name "gsp"
 */
public class GSPRendererFactory implements RendererFactory {
    private static Log log = LogFactory.getLog(GroovletRendererFactory.class);
    private TemplateEngine templateEngine = new SimpleTemplateEngine();
    
    public Renderer getRenderer(Template template) {
        Renderer renderer = null;
        if(template.getTemplateLanguage() == null || template.getId() == null) {
            return null;
        }
        if("gsp".equals(template.getTemplateLanguage()) && template instanceof WeblogTemplate) {
            try {
                renderer = new GSPRenderer(templateEngine, (WeblogTemplate)template); 
            } catch(Exception ex) {
                return null;
            }
        }
        return renderer;
    }
    
}
