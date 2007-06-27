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
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.model.ModelLoader;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPreviewRequest;


/**
 * Responsible for rendering weblog page previews.
 *
 * This servlet is used as part of the authoring interface to provide previews
 * of what a weblog will look like with a given theme.  It is not available
 * outside of the authoring interface.
 */
public class PreviewServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(PreviewServlet.class);
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing PreviewServlet");
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        Weblog weblog = null;
        
        WeblogPreviewRequest previewRequest = null;
        try {
            previewRequest = new WeblogPreviewRequest(request);
            
            // lookup weblog specified by preview request
            weblog = previewRequest.getWeblog();
            if(weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "+
                        previewRequest.getWeblogHandle());
            }
        } catch (Exception e) {
            // some kind of error parsing the request or getting weblog
            log.debug("error creating preview request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // try getting the preview theme
        log.debug("preview theme = "+previewRequest.getThemeName());
        Theme previewTheme = previewRequest.getTheme();
        
        // construct a temporary Website object for this request
        // and set the EditorTheme to our previewTheme
        Weblog tmpWebsite = new Weblog();
        tmpWebsite.setData(weblog);
        if(previewTheme != null && previewTheme.isEnabled()) {
            tmpWebsite.setEditorTheme(previewTheme.getId());
        } else if(WeblogTheme.CUSTOM.equals(previewRequest.getThemeName())) {
            tmpWebsite.setEditorTheme(WeblogTheme.CUSTOM);
        }
        
        // we've got to set the weblog in our previewRequest because that's
        // the object that gets referenced during rendering operations
        previewRequest.setWeblog(tmpWebsite);
        
        // do we need to force a specific locale for the request?
        if(previewRequest.getLocale() == null && !weblog.isShowAllLangs()) {
            previewRequest.setLocale(weblog.getLocale());
        }
        
        Template page = null;
        try {
            // we just want to show the default view
            page = tmpWebsite.getTheme().getDefaultTemplate();
            
            if(page == null) {
                throw new WebloggerException("No default page for weblog: "+tmpWebsite.getHandle());
            }
        } catch(WebloggerException re) {
            // couldn't get page
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Error getting default page for preview", re);
            return;
        }
        
        log.debug("preview page found, dealing with it");
        
        // set the content type
        String pageLink = previewRequest.getWeblogPageName();
        String mimeType = pageLink !=  null ? RollerContext.getServletContext().getMimeType(pageLink) : null;        
        String contentType = "text/html; charset=utf-8";
        if(mimeType != null) {
            // we found a match ... set the content type
            contentType = mimeType+"; charset=utf-8";
        } else if ("_css".equals(previewRequest.getWeblogPageName())) {
            // TODO: store content-type for each page so this hack is unnecessary
            contentType = "text/css; charset=utf-8";
        }
        
        // looks like we need to render content
        Map model = new HashMap();
        try {
            PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", false, 8192, true);
            
            // special hack for menu tag
            request.setAttribute("pageRequest", previewRequest);
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            initData.put("weblogRequest", previewRequest);
            initData.put("pageContext", pageContext);
            
            // Load models for page previewing
            String pageModels = WebloggerConfig.getProperty("rendering.previewModels");
            ModelLoader.loadModels(pageModels, model, initData, true);
            
            // Load special models for site-wide blog
            if(RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                String siteModels = WebloggerConfig.getProperty("rendering.siteModels");
                ModelLoader.loadModels(siteModels, model, initData, true);
            }

            // Load weblog custom models
            ModelLoader.loadCustomModels(weblog, model, initData);
            
            // ick, gotta load pre-3.0 model stuff as well :(
            ModelLoader.loadOldModels(model, request, response, pageContext, previewRequest);
            
        } catch (WebloggerException ex) {
            log.error("ERROR loading model for page", ex);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        
        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            renderer = RendererManager.getRenderer(page);
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page "+page.getId(), e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content.  use default size of about 24K for a standard page
        CachedContent rendererOutput = new CachedContent(24567);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());
            
            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page "+page.getId(), e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // post rendering process
        
        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentType(contentType);
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());
        
        log.debug("Exiting");
    }
    
}
