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

package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.StaticTemplate;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.util.cache.CachedContent;

/**
 * Generates Really Simple Discovery (RSD) listing for a given weblog allowing
 * blog writing clients to see API services provided by Roller. The list of
 * services is maintained in Roller velocity file rsd.vm.
 * 
 * Spec: http://cyber.law.harvard.edu/blogs/gems/tech/rsd.html
 * 
 * This servlet supports 304 If-Modified-Since checking, but does not do any
 * level of content caching.
 */
public class RSDServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(RSDServlet.class);

    /**
     * Init method for this servlet
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);

        log.info("Initializing RSDServlet");
    }

    /**
     * Handle GET requests for weblog pages.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("Entering");
        Weblog weblog;
        WeblogRequest weblogRequest;

        try {
            weblogRequest = new WeblogRequest(request);

            // now make sure the specified weblog really exists
            weblog = weblogRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("Unable to lookup weblog: "
                        + weblogRequest.getWeblogHandle());
            }

        } catch (Exception e) {
            // invalid rsd request format or weblog doesn't exist
            log.debug("error creating weblog request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Respond with 304 Not Modified if it is not modified.
        long lastModified = System.currentTimeMillis();
        if (weblog.getLastModified() != null) {
            lastModified = weblog.getLastModified().getTime();
        }
        if (ModDateHeaderUtil.respondIfNotModified(request, response,
                lastModified, weblogRequest.getDeviceType())) {
            return;
        }

        // set last-modified date
        ModDateHeaderUtil.setLastModifiedHeader(response, lastModified,
                weblogRequest.getDeviceType());

        // set the content type
        response.setContentType("application/rsd+xml; charset=utf-8");

        // populate the model
        HashMap<String, Object> model = new HashMap<>();
        model.put("website", weblog);
        model.put("absBaseURL", WebloggerRuntimeConfig.getAbsoluteContextURL());

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            Template template = new StaticTemplate("weblog/rsd.vm", TemplateLanguage.VELOCITY);
            renderer = RendererManager.getRenderer(template,
                    DeviceType.standard);
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for rsd template", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(RollerConstants.FOUR_KB_IN_BYTES);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for rsd template", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // post rendering process

        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());

        log.debug("Exiting");
    }

}
