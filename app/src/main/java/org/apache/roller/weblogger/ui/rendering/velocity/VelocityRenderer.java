/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RenderingException;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.model.FeedModel;
import org.apache.roller.weblogger.ui.rendering.model.PageModel;
import org.apache.roller.weblogger.ui.rendering.model.SearchResultsFeedModel;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;

/**
 * Renderer that renders using the Velocity template engine.
 */
public class VelocityRenderer implements Renderer {

    private static final Log log = LogFactory.getLog(VelocityRenderer.class);

    // ids Roller gives stored templates
    private static final Pattern STORED_TEMPLATE_ID = Pattern.compile(
            "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");

    // the original template we are supposed to render
    private final Template renderTemplate;
    private final MobileDeviceRepository.DeviceType deviceType;

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

        } catch (VelocityException ex) {
            // in the case of a velocity error we want to render an
            // error page instead so the user knows what was wrong
            velocityException = ex;
            log.warn("Error loading [" + template.getId() + "]: "
                    + forLog(ex.getMessage()));

            // need to lookup error page template
            velocityTemplate = RollerVelocity.getTemplate("error-page.vm", deviceType);

        } catch (Exception ex) {
            // some kind of generic/unknown exception, dump it to the logs
            log.error(
                    "Unknown exception creating renderer for "
                            + template.getId(), ex);

            // throw if back to the caller
            throw ex;
        }
    }

    /**
     * @see org.apache.roller.weblogger.ui.rendering.Renderer#render(java.util.Map,
     *      java.io.Writer)
     */
    @Override
    public void render(Map<String, Object> model, Writer out)
            throws RenderingException {

        if (velocityException != null) {

            // Render exception
            renderException(model, out, null);

            // and we're done
            return;
        }

        // the session bounds output and time, and scopes stored templates
        // loaded by #parse and #include to the weblog being rendered
        RenderSession session = RenderSession.begin(weblogIdFor(model));
        try {

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
                velocityTemplate.merge(ctx, new RenderLimitWriter(sw, session));

                // put rendered template into context
                ctx.put("decorator_body", sw.toString());

                log.debug("Applying decorator " + velocityDecorator.getName());

                // now render decorator to our output writer
                velocityDecorator.merge(ctx, new RenderLimitWriter(out, session));

            } else {

                // no decorator, so just merge template to our output writer
                velocityTemplate.merge(ctx, new RenderLimitWriter(out, session));
            }

            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime) / RollerConstants.SEC_IN_MS;

            log.debug("Rendered [" + renderTemplate.getId() + "] in "
                    + renderTime + " secs");
            return;

        } catch (Exception ex) {
            RenderLimitException limit = findLimit(ex);
            if (limit != null) {
                // the partial output is discarded by the caller, which
                // fails the request as for any other rendering error
                log.error("Stopped rendering [" + renderTemplate.getId()
                        + "]: " + limit.getMessage());
                throw new RenderingException("Error during rendering", limit);
            }
            if (!(ex instanceof VelocityException)) {
                // wrap and rethrow so caller can deal with it
                throw new RenderingException("Error during rendering", ex);
            }

            // in the case of a velocity error including a page we want to
            // render an error page instead so the user knows what was wrong
            velocityException = ex;
            log.warn("Error rendering [" + renderTemplate.getId() + "]: "
                    + forLog(ex.getMessage()));

        } finally {
            session.end();
        }

        // need to lookup parse error template
        renderException(model, out, "error-parse.vm");
    }

    /**
     * @return the id of the weblog being rendered, from the template when it
     *         is one of the weblog's own, otherwise from the page model; null
     *         when the render is not for a weblog
     */
    private String weblogIdFor(Map<String, Object> model) {
        if (renderTemplate instanceof WeblogTemplate) {
            Weblog weblog = ((WeblogTemplate) renderTemplate).getWeblog();
            return weblog == null ? null : weblog.getId();
        }
        Object pageModel = model == null ? null : model.get("model");
        WeblogWrapper weblog = null;
        if (pageModel instanceof PageModel) {
            weblog = ((PageModel) pageModel).getWeblog();
        } else if (pageModel instanceof FeedModel) {
            weblog = ((FeedModel) pageModel).getWeblog();
        } else if (pageModel instanceof SearchResultsFeedModel) {
            weblog = ((SearchResultsFeedModel) pageModel).getWeblog();
        }
        return weblog == null ? null : weblog.getId();
    }

    /**
     * @return the exception's message as the public error page shows it, with
     *         template ids replaced by the template's name, or by "template"
     *         for other stored templates; the ids stay in the log
     */
    private String publicMessage(Exception ex) {
        String message = brief(ex.getMessage());
        if (message == null) {
            return null;
        }
        if (renderTemplate.getId() != null && renderTemplate.getName() != null) {
            message = message.replace(renderTemplate.getId(), renderTemplate.getName());
        }
        return STORED_TEMPLATE_ID.matcher(message).replaceAll("template");
    }

    /** Most characters of a template error's message that are logged or shown. */
    private static final int MAX_MESSAGE_CHARS = 1000;

    /**
     * @return the start of a message, which can quote a value from the
     *         template of any length
     */
    private static String brief(String message) {
        return message == null || message.length() <= MAX_MESSAGE_CHARS ? message
                : message.substring(0, MAX_MESSAGE_CHARS) + "...";
    }

    /** @return the start of a message, on one line */
    private static String forLog(String message) {
        String m = brief(message);
        return m == null ? null : m.replace('\r', ' ').replace('\n', ' ');
    }

    private static RenderLimitException findLimit(Throwable t) {
        for (int depth = 0; t != null && depth < 20; depth++, t = t.getCause()) {
            if (t instanceof RenderLimitException) {
                return (RenderLimitException) t;
            }
        }
        return null;
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
            ctx.put("exceptionMessage", publicMessage(velocityException));
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
