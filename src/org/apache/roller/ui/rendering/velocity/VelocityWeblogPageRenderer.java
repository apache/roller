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

package org.apache.roller.ui.rendering.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;


/**
 * Renderer for weblog page velocity templates.
 *
 * This is a special renderer apart from the standard velocity renderer because
 * we need some additional rendering logic to deal with page decorating.
 */
public class VelocityWeblogPageRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityWeblogPageRenderer.class);
    
    private String resourceId = null;
    private Template resourceTemplate = null;
    private Exception parseException = null;
    
    
    public VelocityWeblogPageRenderer(String resource) throws Exception {
        
        this.resourceId = resource;
        
        try {
            // make sure that we can locate the template
            // if we can't then this will throw an exception
            resourceTemplate = RollerVelocity.getTemplate(this.resourceId, "UTF-8");
        } catch(ParseErrorException ex) {
            // in the case of a parsing error we want to render an
            // error page instead so the user knows what was wrong
            parseException = ex;
            
            // need to lookup error page template
            resourceTemplate = RollerVelocity.getTemplate("templates/error-page.vm");
        }
    }
    
    
    public void render(Map model, Writer out) throws Exception {
        
        if(parseException != null) {
            
            Context ctx = new VelocityContext(model);
            ctx.put("exception", parseException);
            ctx.put("exceptionSource", resourceId);
            ctx.put("utils", new UtilitiesModel());
            
            // render output to Writer
            resourceTemplate.merge(ctx, out);
            
            // and we're done
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        // the template we are going to render
        // we already looked this up at construction time
        Template tmpl = this.resourceTemplate;
        
        // convert model to Velocity Context
        Context ctx = new VelocityContext(model);
        
        // TODO: this is poor form, we should not need to access the pojo from the wrapper
        WebsiteData weblog = null;
        WebsiteDataWrapper websiteWrapper = (WebsiteDataWrapper) model.get("website");
        if(websiteWrapper != null) {
            weblog = websiteWrapper.getPojo();
        }
        
        if (weblog != null) {
            Template decorator = null;
            try {
                // look for decorator
                decorator = findDecorator(weblog);
            } catch(Exception e) {
                // error finding decorator
                log.warn("Could not find a decorator to apply");
            }
            
            if(decorator != null) {
                // render current template
                StringWriter sw = new StringWriter();
                tmpl.merge(ctx, sw);
                
                // put rendered template into context
                ctx.put("decorator_body", sw.toString());
                tmpl = decorator;
            }
        }
        
        // render output to Writer
        tmpl.merge(ctx, out);
        
        long endTime = System.currentTimeMillis();
        long renderTime = (endTime - startTime)/1000;
        
        log.debug("Rendered ["+this.resourceId+"] from weblog "+
                weblog.getHandle()+" in "+renderTime+" secs");
    }
    
    
    /**
     * Load the decorator template and apply it.  If there is no user specified
     * decorator then the default decorator is applied.
     */
    private Template findDecorator(WebsiteData website) throws Exception {
        
        Template decorator = null;
        org.apache.roller.pojos.Template decorator_template = null;
        
        try {
            // see if user defined a custom decorator
            decorator_template = website.getPageByName("_decorator");
            
            decorator = RollerVelocity.getTemplate(decorator_template.getId(), "UTF-8");
        } catch (Exception e) {
            // it may not exist, so this is okay
        }
        
        // couldn't find Template, load default "no-op" decorator
        if (decorator == null) {
            decorator = RollerVelocity.getTemplate("templates/weblog/noop_decorator.vm", "UTF-8");
        }
        
        return decorator;
    }
    
}
