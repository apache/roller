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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;


/**
 * Renderer for weblog page velocity templates.
 *
 * This is a special renderer apart from the standard velocity renderer because
 * we need some additional rendering logic to deal with page decorating.
 */
public class VelocityWeblogPageRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityWeblogPageRenderer.class);
    
    private String resourceId = null;
    
    
    public VelocityWeblogPageRenderer(String resource) {
        
        this.resourceId = resource;
    }
    
    
    public void render(Map model, Writer out) throws Exception {
        
        // lookup the specified resource
        Template tmpl = RollerVelocity.getTemplate(this.resourceId, "UTF-8");
        
        // convert model to Velocity Context
        Context ctx = new VelocityContext(model);
        
        // if there is a decorator template then apply it
        WebsiteData website = null;
        // TODO: this is poor form, we should not need to access the pojo from the wrapper
        WebsiteDataWrapper websiteWrapper = (WebsiteDataWrapper) model.get("website");
        if(websiteWrapper != null) {
            website = websiteWrapper.getPojo();
        }
        if (website != null) {
            Template decorator = null;
            try {
                // look for decorator
                decorator = findDecorator(website, (String) ctx.get("decorator"));
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
        
        // render output to servlet response
        tmpl.merge(ctx, out);
    }
    
    
    /**
     * Load the decorator template and apply it.  If there is no user specified
     * decorator then the default decorator is applied.
     */
    private Template findDecorator(WebsiteData website, String decorator_name)
            throws Exception {
        
        Template decorator = null;
        org.apache.roller.pojos.Template decorator_template = null;
        
        // check for user-specified decorator
        if (decorator_name != null) {
            decorator_template = website.getPageByName(decorator_name);
        }
        
        // if no user-specified decorator try default page-name
        if (decorator_template == null) {
            decorator_template = website.getPageByName("_decorator");
        }
        
        // try loading Template
        if (decorator_template != null) {
            try {
                decorator = RollerVelocity.getTemplate(decorator_template.getId(), "UTF-8");
            } catch (Exception e) {
                // it may not exist, so this is okay
            }
        }
        
        // couldn't find Template, load default "no-op" decorator
        if (decorator == null) {
            decorator = RollerVelocity.getTemplate("/themes/noop_decorator.vm", "UTF-8");
        }
        
        return decorator;
    }
    
}
