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
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.model.Model;
import org.apache.roller.weblogger.ui.rendering.model.ModelLoader;
import org.apache.roller.weblogger.ui.rendering.model.SearchResultsModel;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.weblogger.util.cache.CachedContent;


/**
 * Handles search queries for weblogs.
 */
public class SearchServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(SearchServlet.class);
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing SearchServlet");
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        Weblog weblog = null;
        WeblogSearchRequest searchRequest = null;
        
        // first off lets parse the incoming request and validate it
        try {
            searchRequest = new WeblogSearchRequest(request);
            
            // now make sure the specified weblog really exists
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(searchRequest.getWeblogHandle(), Boolean.TRUE);
            
        } catch(Exception e) {
            // invalid search request format or weblog doesn't exist
            log.debug("error creating weblog search request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // lookup template to use for rendering
        ThemeTemplate page = null;
        try {
            // first try looking for a specific search page
            page = weblog.getTheme().getTemplateByAction(ThemeTemplate.ACTION_SEARCH);
            
            // if not found then fall back on default page
            if(page == null) {
                page = weblog.getTheme().getDefaultTemplate();
            }
            
            // if still null then that's a problem
            if(page == null) {
                throw new WebloggerException("Could not lookup default page "+
                        "for weblog "+weblog.getHandle());
            }
        } catch(Exception e) {
            log.error("Error getting default page for weblog "+
                    weblog.getHandle(), e);
        }
        
        // set the content type
        response.setContentType("text/html; charset=utf-8");
        
        // looks like we need to render content
        Map model = new HashMap();
        try {
            PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", false, 8192, true);
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            initData.put("pageContext", pageContext);
            
            // this is a little hacky, but nothing we can do about it
            // we need the 'weblogRequest' to be a pageRequest so other models
            // are properly loaded, which means that searchRequest needs its
            // own custom initData property aside from the standard weblogRequest.
            // possible better approach is make searchRequest extend pageRequest.
            WeblogPageRequest pageRequest = new WeblogPageRequest();
            pageRequest.setWeblogHandle(searchRequest.getWeblogHandle());
            pageRequest.setWeblogCategoryName(searchRequest.getWeblogCategoryName());
            initData.put("weblogRequest", pageRequest);
            initData.put("searchRequest", searchRequest);
            
            // Load models for pages
            String searchModels = RollerConfig.getProperty("rendering.searchModels");
            ModelLoader.loadModels(searchModels, model, initData, true);
            
            // Load special models for site-wide blog
            if(RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                String siteModels = RollerConfig.getProperty("rendering.siteModels");
                ModelLoader.loadModels(siteModels, model, initData, true);
            }

            // Load weblog custom models
            ModelLoader.loadCustomModels(weblog, model, initData);
            
            // ick, gotta load pre-3.0 model stuff as well :(
            ModelLoader.loadOldModels(model, request, response, pageContext, pageRequest);
            
            // manually add search model again to support pre-3.0 weblogs
            Model searchModel = new SearchResultsModel();
            searchModel.init(initData);
            model.put("searchResults", searchModel);
            
        } catch (WebloggerException ex) {
            log.error("Error loading model objects for page", ex);
            
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
            log.error("Couldn't find renderer for rsd template", e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content
        CachedContent rendererOutput = new CachedContent(4096);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());
            
            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for rsd template", e);
            
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
