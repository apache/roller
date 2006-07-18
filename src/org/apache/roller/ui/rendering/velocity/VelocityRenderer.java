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


/**
 * Renderer for Velocity templates.
 */
public class VelocityRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityRenderer.class);
    
    private String resourceId = null;
    private Template resourceTemplate = null;
    private Exception exception = null;  
    private String exceptionSource = null;
    
    public VelocityRenderer(String resource) throws Exception {
        
        this.resourceId = resource;
        
        // make sure that we can locate the template
        // if we can't then this will throw an exception
        resourceTemplate = RollerVelocity.getTemplate(this.resourceId, "UTF-8");
    }
    
    /** Construct rendering for displaying exception */
    public VelocityRenderer(Exception exception, String exceptionSource, String resource) throws Exception {
        this(resource);        
        this.exception = exception;
        this.exceptionSource = exceptionSource;
    }
    
    
    public void render(Map model, Writer out) throws Exception {
        
        if (exception != null) {
            renderException(model, out);
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
        
    
    private void renderException(Map model, Writer out) throws Exception { 
                
        // add exception to Velocity Context and utils for formatting
        Context ctx = new VelocityContext(model);
        ctx.put("exception", exception);
        ctx.put("exceptionSource", exceptionSource);
        ctx.put("utils", new UtilitiesModel()); 
        
        // render output to Writer
        resourceTemplate.merge(ctx, out);
    }
    
    

}
