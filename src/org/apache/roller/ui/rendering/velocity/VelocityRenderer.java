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

import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;


/**
 * Renderer for Velocity templates.
 */
public class VelocityRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityRenderer.class);
    
    private String resourceId = null;
    private Template resourceTemplate = null;
    private Exception parseException = null;
    
    
    public VelocityRenderer(String resource) throws Exception {
        
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
        
        // convert model to Velocity Context
        Context ctx = new VelocityContext(model);
        
        // render output to Writer
        this.resourceTemplate.merge(ctx, out);
        
        long endTime = System.currentTimeMillis();
        long renderTime = (endTime - startTime)/1000;
        
        log.debug("Rendered ["+this.resourceId+"] in "+renderTime+" secs");
    }

}
