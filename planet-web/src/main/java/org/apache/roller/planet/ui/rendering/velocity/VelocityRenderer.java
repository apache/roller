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

package org.apache.roller.planet.ui.rendering.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.Template;
import org.apache.roller.planet.ui.rendering.Renderer;
import org.apache.roller.planet.ui.rendering.RenderingException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * Renderer for Velocity templates.
 */
public class VelocityRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityRenderer.class);
    
    // the original template we are supposed to render
    private Template renderTemplate = null;
    
    // the velocity templates
    private org.apache.velocity.Template velocityTemplate = null;
    
    // a possible exception
    private Exception parseException = null;
    
    
    public VelocityRenderer(Template template) throws Exception {
        
        // the Template we are supposed to render
        this.renderTemplate = template;
        
        try {
            // make sure that we can locate the template
            // if we can't then this will throw an exception
            velocityTemplate = PlanetVelocity.getTemplate(template.getId(), "UTF-8");
            
        } catch(ResourceNotFoundException ex) {
            // velocity couldn't find the resource so lets log a warning
            log.warn("Error creating renderer for "+template.getId()+
                    " due to ["+ex.getMessage()+"]");
            
            // then just rethrow so that the caller knows this instantiation failed
            throw ex;
            
        } catch(ParseErrorException ex) {
            // in the case of a parsing error we want to render an
            // error page instead so the user knows what was wrong
            parseException = ex;
            
            // need to lookup error page template
            velocityTemplate = PlanetVelocity.getTemplate("error-page.vm");
            
        } catch(Exception ex) {
            // some kind of generic/unknown exception, dump it to the logs
            log.error("Unknown exception creatting renderer for "+template.getId(), ex);
            
            // throw if back to the caller
            throw ex;
        }
    }
    
    
    public void render(Map model, Writer out) throws RenderingException {
        
        try {
            if(parseException != null) {
                
                Context ctx = new VelocityContext(model);
                ctx.put("exception", parseException);
                ctx.put("exceptionSource", renderTemplate.getId());
                
                // render output to Writer
                velocityTemplate.merge(ctx, out);
                
                // and we're done
                return;
            }
            
            long startTime = System.currentTimeMillis();
            
            // convert model to Velocity Context
            Context ctx = new VelocityContext(model);
            
            // no decorator, so just merge template to our output writer
            velocityTemplate.merge(ctx, out);

            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime)/1000;
            
            log.debug("Rendered ["+renderTemplate.getId()+"] in "+renderTime+" secs");
            
        } catch (Exception ex) {
            // wrap and rethrow so caller can deal with it
            throw new RenderingException("Error during rendering", ex);
        }
    }

}
