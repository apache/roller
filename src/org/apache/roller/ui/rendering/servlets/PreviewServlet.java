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

package org.apache.roller.ui.rendering.servlets;

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
import org.apache.roller.RollerException;
import org.apache.roller.ThemeNotFoundException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ThemeManager;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.util.cache.CachedContent;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.ui.rendering.model.ModelLoader;
import org.apache.roller.ui.rendering.util.WeblogPreviewRequest;


/**
 * Responsible for rendering weblog page previews.
 *
 * @web.servlet name="PreviewServlet" load-on-startup="7"
 * @web.servlet-mapping url-pattern="/preview/*"
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
        
        Theme previewTheme = null;
        WebsiteData weblog = null;
        
        WeblogPreviewRequest previewRequest = null;
        try {
            previewRequest = new WeblogPreviewRequest(request);
            
            // lookup weblog specified by preview request
            UserManager uMgr = RollerFactory.getRoller().getUserManager();
            weblog = uMgr.getWebsiteByHandle(previewRequest.getWeblogHandle());
            
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        previewRequest.getWeblogHandle());
            }
        } catch (Exception e) {
            // some kind of error parsing the request
            log.error("error creating preview request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // try getting the preview theme
        log.debug("preview theme = "+previewRequest.getTheme());
        if(previewRequest.getTheme() != null) {
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                previewTheme = themeMgr.getTheme(previewRequest.getTheme());
                
            } catch(ThemeNotFoundException tnfe) {
                // bogus theme specified ... don't worry about it
                // possibly "custom", but we'll handle that below
            } catch(RollerException re) {
                
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Error doing theme preview", re);
                return;
            }
        }
        
        // construct page context
        PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
        
        // construct a temporary Website object for this request
        // and set the EditorTheme to our previewTheme
        WebsiteData tmpWebsite = new WebsiteData();
        tmpWebsite.setData(weblog);
        if(previewTheme != null && previewTheme.isEnabled()) {
            tmpWebsite.setEditorTheme(previewTheme.getName());
        } else if(previewRequest.getTheme().equals(Theme.CUSTOM)) {
            tmpWebsite.setEditorTheme(Theme.CUSTOM);
        }
        
        Template page = null;
        try {
            page = tmpWebsite.getDefaultPage();
            
            if(page == null) {
                throw new RollerException("Weblog's default page was null");
            }
        } catch(RollerException re) {
            // couldn't get page
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Error getting default page for preview", re);
            return;
        }
        
        log.debug("preview page found, dealing with it");
        
        // set the content type
        String pageLink = page.getLink();
        String mimeType = RollerContext.getServletContext().getMimeType(pageLink);
        if(mimeType != null) {
            // we found a match ... set the content type
            response.setContentType(mimeType+"; charset=utf-8");
        } else {
            response.setContentType("text/html; charset=utf-8");
        }
        
        // looks like we need to render content
        Map model = new HashMap();
        try {
            RollerContext rollerContext = RollerContext.getRollerContext();
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            
            // Feeds get the weblog specific page model
            ModelLoader.loadWeblogModels(model, initData);
            
            // special handling for site wide feed
            if (rollerContext.isSiteWideWeblog(tmpWebsite.getHandle())) {
                ModelLoader.loadSiteModels(model, initData);
            }
            
            // add helpers
            ModelLoader.loadUtilityHelpers(model);
            ModelLoader.loadWeblogHelpers(pageContext, model);
            ModelLoader.loadPluginHelpers(tmpWebsite, model);

            // Feeds get weblog's custom models too
            ModelLoader.loadCustomModels(tmpWebsite, model, initData);
            
            // ick, gotta load pre-3.0 model stuff as well :(
            ModelLoader.loadOldModels(response, request, model);
            
        } catch (RollerException ex) {
            log.error("ERROR loading model for page", ex);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        
        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            renderer = RendererManager.getRenderer("velocityWeblogPage", page.getId());
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
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());
        
        log.debug("Exiting");
    }
}
