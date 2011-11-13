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

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RenderingException;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * Renderer that renders using the Velocity template engine.
 */
public class VelocityRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityRenderer.class);
    
    // the original template we are supposed to render
    private Template renderTemplate = null;
	private MobileDeviceRepository.DeviceType deviceType = null;
    
    // the velocity templates
    private org.apache.velocity.Template velocityTemplate = null;
    private org.apache.velocity.Template velocityDecorator = null;
    
    // a possible exception
    private Exception parseException = null;
    
    
    public VelocityRenderer(Template template, 
			MobileDeviceRepository.DeviceType deviceType) throws Exception {
        
        // the Template we are supposed to render
        this.renderTemplate = template;
		this.deviceType = deviceType;
        
        try {
            // make sure that we can locate the template
            // if we can't then this will throw an exception
            velocityTemplate = RollerVelocity.getTemplate(template.getId(), deviceType, "UTF-8");
           
            // if this is a ThemeTemplate than look for a decorator too
            if(template instanceof ThemeTemplate) {
                ThemeTemplate templ = (ThemeTemplate) template;
                
                Template decorator = templ.getDecorator();
                if(decorator != null) {
                    velocityDecorator = RollerVelocity.getTemplate(decorator.getId(), "UTF-8");
                }
            }

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
            velocityTemplate = RollerVelocity.getTemplate("templates/error-page.vm", deviceType);
            
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
                ctx.put("utils", new UtilitiesModel());
                
                // render output to Writer
                velocityTemplate.merge(ctx, out);
                
                // and we're done
                return;
            }
            
            long startTime = System.currentTimeMillis();
            
            // convert model to Velocity Context
            Context ctx = new VelocityContext(model);
            
            if (velocityDecorator != null) {
                
                /**
                 * We only allow decorating once, so the process isn't
                 * fully recursive.  This is just to keep it simple.
                 */
                
                // render base template to a temporary StringWriter
                StringWriter sw = new StringWriter();
                velocityTemplate.merge(ctx, sw);
                
                // put rendered template into context
                ctx.put("decorator_body", sw.toString());
                
                log.debug("Applying decorator "+velocityDecorator.getName());
                
                // now render decorator to our output writer
                velocityDecorator.merge(ctx, out);
                
            } else {
                
                // no decorator, so just merge template to our output writer
                velocityTemplate.merge(ctx, out);
            }
            
            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime)/1000;
            
            log.debug("Rendered ["+renderTemplate.getId()+"] in "+renderTime+" secs");
            
        } catch (Exception ex) {
            // wrap and rethrow so caller can deal with it
            throw new RenderingException("Error during rendering", ex);
        }
    }

}
