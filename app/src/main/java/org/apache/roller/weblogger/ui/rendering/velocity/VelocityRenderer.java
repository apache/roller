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
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RenderingException;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;

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
    private Exception velocityException = null;

    public VelocityRenderer(Template template,
            MobileDeviceRepository.DeviceType deviceType) throws Exception {

        // the Template we are supposed to render
        this.renderTemplate = template;
        this.deviceType = deviceType;

        try {
            // make sure that we can locate the template
            // if we can't then this will throw an exception
            velocityTemplate = RollerVelocity.getTemplate(template.getId(),
                    deviceType, "UTF-8");

        } catch (ResourceNotFoundException ex) {
            // velocity couldn't find the resource so lets log a warning
            log.warn("Error creating renderer for " + template.getId()
                    + " due to [" + ex.getMessage() + "]");

            // then just rethrow so that the caller knows this instantiation
            // failed
            throw ex;

        } catch (ParseErrorException ex) {
            // in the case of a parsing error we want to render an
            // error page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup error page template
            velocityTemplate = RollerVelocity.getTemplate("error-page.vm",
                    deviceType);

        } catch (MethodInvocationException ex) {

            // in the case of a invocation error we want to render an
            // error page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup error page template
            velocityTemplate = RollerVelocity.getTemplate("error-page.vm",
                    deviceType);

        } catch (VelocityException ex) {

            // in the case of a parsing error including a macro we want to
            // render an error page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup error page template
            velocityTemplate = RollerVelocity.getTemplate("error-page.vm",
                    deviceType);

        } catch (Exception ex) {
            // some kind of generic/unknown exception, dump it to the logs
            log.error(
                    "Unknown exception creatting renderer for "
                            + template.getId(), ex);

            // throw if back to the caller
            throw ex;
        }
    }

    /**
     * @see org.apache.roller.weblogger.ui.rendering.Renderer#render(java.util.Map,
     *      java.io.Writer)
     */
    public void render(Map<String, Object> model, Writer out)
            throws RenderingException {

        try {

            if (velocityException != null) {

                // Render exception
                renderException(model, out, null);

                // and we're done
                return;
            }

            long startTime = System.currentTimeMillis();

            // convert model to Velocity Context
            Context ctx = new VelocityContext(model);

            if (velocityDecorator != null) {

                /**
                 * We only allow decorating once, so the process isn't fully
                 * recursive. This is just to keep it simple.
                 */

                // render base template to a temporary StringWriter
                StringWriter sw = new StringWriter();
                velocityTemplate.merge(ctx, sw);

                // put rendered template into context
                ctx.put("decorator_body", sw.toString());

                log.debug("Applying decorator " + velocityDecorator.getName());

                // now render decorator to our output writer
                velocityDecorator.merge(ctx, out);

            } else {

                // no decorator, so just merge template to our output writer
                velocityTemplate.merge(ctx, out);
            }

            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime) / RollerConstants.SEC_IN_MS;

            log.debug("Rendered [" + renderTemplate.getId() + "] in "
                    + renderTime + " secs");

        } catch (ParseErrorException ex) {

            // in the case of a parsing error including a page we want to render
            // an error on the page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup parse error template
            renderException(model, out, "error-parse.vm");

        } catch (MethodInvocationException ex) {

            // in the case of a parsing error including a page we want to render
            // an error on the page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup parse error template
            renderException(model, out, "error-parse.vm");

        } catch (VelocityException ex) {

            // in the case of a parsing error including a macro we want to
            // render an error page instead so the user knows what was wrong
            velocityException = ex;

            // need to lookup parse error template
            renderException(model, out, "error-parse.vm");

        } catch (Exception ex) {
            // wrap and rethrow so caller can deal with it
            throw new RenderingException("Error during rendering", ex);
        }
    }

    /**
     * Render Velocity Exception.
     * 
     * @param model
     *            the model
     * @param out
     *            the out
     * @param template
     *            the template. Null if using existing template name
     * 
     * @throws RenderingException
     *             the rendering exception
     */
    private void renderException(Map<String, Object> model, Writer out,
            String template) throws RenderingException {

        try {

            if (template != null) {
                // need to lookup error page template
                velocityTemplate = RollerVelocity.getTemplate(template,
                        deviceType);
            }

            Context ctx = new VelocityContext(model);
            ctx.put("exception", velocityException);
            ctx.put("exceptionSource", renderTemplate.getId());
            ctx.put("exceptionDevice", deviceType);
            ctx.put("utils", new UtilitiesModel());

            // render output to Writer
            velocityTemplate.merge(ctx, out);

        } catch (Exception e) {
            // wrap and rethrow so caller can deal with it
            throw new RenderingException("Error during rendering", e);
        }

    }

}
