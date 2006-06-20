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
package org.apache.roller.ui.rendering.velocity.deprecated;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.TemplateWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.authoring.struts.formbeans.CommentFormEx;
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.rendering.model.PageModel;
import org.apache.roller.ui.rendering.model.PlanetPageModel;
import org.apache.roller.ui.rendering.model.SitePageModel;
import org.apache.roller.ui.rendering.newsfeeds.NewsfeedCache;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.util.Utilities;
import org.apache.struts.util.RequestUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;

/**
 * Load Velocity Context with Roller objects, values, and custom plugins.
 * @author llavandowska
 * @author David M Johnson
 */
public class ContextLoader {
    
    private static Log mLogger = LogFactory.getLog(ContextLoader.class);
    
    private static final String TOOLBOX_KEY =
            "org.apache.roller.presentation.velocity.toolbox";
    
    private static final String TOOLBOX_MANAGER_KEY =
            "org.apache.roller.presentation.velocity.toolboxManager";
    
    private RollerRequest mRollerReq = null;
    
    
    /**
     * Setup the a Velocity context by loading it with objects, values, and
     * RollerPagePlugins needed for Roller page execution.
     */
    public static void setupContext(
            Map ctx, 
            RollerRequest rreq, 
            HttpServletResponse response )
            throws RollerException {
        
        mLogger.debug("setupContext( ctx = "+ctx+")");
        
        HttpServletRequest request = rreq.getRequest();
        RollerContext rollerCtx = RollerContext.getRollerContext( );
        
        try {
            // Add default page model object to context
            String pageModelClassName =
                RollerConfig.getProperty("velocity.pagemodel.classname");
            Class pageModelClass = Class.forName(pageModelClassName);
            PageModel pageModel = (PageModel)pageModelClass.newInstance();
            pageModel.init(rreq.getRequest(), new HashMap());            
            ctx.put(pageModel.getModelName(), pageModel);
            
            // Add other page models
            // TODO: ATLAS make page models configurable            
            PageModel sitePageModel = new SitePageModel();
            ctx.put(sitePageModel.getModelName(), sitePageModel);            
            PageModel planetPageModel = new PlanetPageModel();
            ctx.put(planetPageModel.getModelName(), planetPageModel);
            
            // TODO: ATLAS: figure out another way to do this:
            //ctx.put("pages", pageModel.getPages());
            
        } catch (Exception e) {
            throw new RollerException("ERROR creating Page Model",e);
        }
        
        // Add Velocity page helper to context
        OldPageHelper pageHelper = new OldPageHelper(request, response, ctx);
        Roller roller = RollerFactory.getRoller();
        ctx.put("pageHelper", pageHelper);
                
        // Load standard Roller objects and values into the context
        WebsiteData website = 
            loadWeblogValues(ctx, rreq, rollerCtx );
        loadPathValues(       ctx, rreq, rollerCtx, website );
        loadRssValues(        ctx, rreq, website );
        loadUtilityObjects(   ctx, rreq, rollerCtx, website );
        loadRequestParamKeys( ctx);
        loadStatusMessage(    ctx, rreq );
        
        // If single entry is specified, load comments too
        if ( rreq.getWeblogEntry() != null ) {
            loadCommentValues( ctx, rreq, rollerCtx );
        }
        
        // add Velocity Toolbox tools to context
        loadToolboxContext(request, response, ctx);
    }
        
    /**
     * Load website object and related objects.
     */
    protected static WebsiteData loadWeblogValues(
            Map ctx, 
            RollerRequest rreq, 
            RollerContext rollerCtx )
            throws RollerException {
                
        Roller mRoller = RollerFactory.getRoller();
        Map props = mRoller.getPropertiesManager().getProperties();
        
        WebsiteData weblog = rreq.getWebsite();            
        if (weblog == null && rreq.getRequest().getParameter("entry") != null) {
            String handle = rreq.getRequest().getParameter("entry");
            weblog = RollerFactory.getRoller().getUserManager().getWebsiteByHandle(handle);
        }
        if (weblog == null && rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE) != null) {
            weblog = (WebsiteData)rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE);
        } 
        
        if (weblog != null) {
            ctx.put("userName",         weblog.getHandle());
            ctx.put("fullName",         weblog.getName() );
            ctx.put("emailAddress",     weblog.getEmailAddress() );
            ctx.put("encodedEmail",     RegexUtil.encode(weblog.getEmailAddress()));
            ctx.put("obfuscatedEmail",  RegexUtil.obfuscateEmail(weblog.getEmailAddress()));
            
            // setup Locale for future rendering
            ctx.put("locale", weblog.getLocaleInstance());
            
            // setup Timezone for future rendering
            ctx.put("timezone", weblog.getTimeZoneInstance());
            ctx.put("timeZone", weblog.getTimeZoneInstance());
        } else {
            // create dummy website for use in site-wide feeds
            weblog = new WebsiteData();
            weblog.setAllowComments(Boolean.FALSE);
            weblog.setHandle("zzz_none_zzz");
            weblog.setName(
                ((RollerPropertyData)props.get("site.name")).getValue());
            weblog.setDescription(
                ((RollerPropertyData)props.get("site.description")).getValue());
            weblog.setEntryDisplayCount(
                RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries"));
            ctx.put("handle",   weblog.getHandle() );
            ctx.put("userName", weblog.getHandle() );
            ctx.put("fullName", weblog.getHandle());
            ctx.put("locale",   Locale.getDefault());
            ctx.put("timezone", TimeZone.getDefault());
            ctx.put("timeZone", TimeZone.getDefault());
            ctx.put("emailAddress",
                ((RollerPropertyData)props.get("site.adminemail")).getValue());           
        }
        ctx.put("website", WebsiteDataWrapper.wrap(weblog) );
        
        String siteName = ((RollerPropertyData)props.get("site.name")).getValue();
        if ("Roller-based Site".equals(siteName)) siteName = "Main";
        ctx.put("siteName", siteName);
        
        String siteShortName = ((RollerPropertyData)props.get("site.shortName")).getValue();
        ctx.put("siteShortName", siteShortName);
        
        // add language of the session (using locale of viewer set by Struts)
        ctx.put("viewLocale",
                LanguageUtil.getViewLocale(rreq.getRequest()));
        mLogger.debug("context viewLocale = "+ctx.get( "viewLocale"));
               
        // if there is an "_entry" page, only load it once
        // but don't do it for dummy website
        if (weblog != null && !"zzz_none_zzz".equals(weblog.getHandle())) {
            // alternative display pages - customization
            Template entryPage = weblog.getPageByName("_entry");
            if (entryPage != null) {
                ctx.put("entryPage", TemplateWrapper.wrap(entryPage));
            }            
            // TODO: ATLAS: no templates use this, should be safe to remove 
            // Template descPage = weblog.getPageByName("_desc");
            //if (descPage != null) {
                //ctx.put("descPage", TemplateWrapper.wrap(descPage));
            //}
        }

        boolean commentsEnabled =
            RollerRuntimeConfig.getBooleanProperty("users.comments.enabled");
        boolean trackbacksEnabled =
            RollerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled");
        boolean linkbacksEnabled =
            RollerRuntimeConfig.getBooleanProperty("site.linkbacks.enabled");
        
        ctx.put("commentsEnabled",   new Boolean(commentsEnabled) );
        ctx.put("trackbacksEnabled", new Boolean(trackbacksEnabled) );
        ctx.put("linkbacksEnabled",  new Boolean(linkbacksEnabled) );
        
        return weblog;
    }
            
    /**
     * Load comments for one weblog entry and related objects.
     */
    protected static void loadCommentValues(
            Map       ctx, 
            RollerRequest rreq, 
            RollerContext rollerCtx )
            throws RollerException {
        
        mLogger.debug("Loading comment values");
        
        HttpServletRequest request = rreq.getRequest();
        
        String escapeHtml =
            RollerRuntimeConfig.getProperty("users.comments.escapehtml");
        String autoFormat =
            RollerRuntimeConfig.getProperty("users.comments.autoformat");
        ctx.put("isCommentPage",     Boolean.TRUE);
        ctx.put("escapeHtml",        new Boolean(escapeHtml) );
        ctx.put("autoformat",        new Boolean(autoFormat) );                
        
        // Make sure comment form object is available in context
        CommentFormEx commentForm =
                (CommentFormEx) request.getAttribute("commentForm");
        if ( commentForm == null ) {
            commentForm = new CommentFormEx();
            
            // Set fields to spaces to please Velocity
            commentForm.setName("");
            commentForm.setEmail("");
            commentForm.setUrl("");
            commentForm.setContent("");
        }
        ctx.put("commentForm",commentForm);
        
        // Either put a preview comment in to context
        if ( request.getAttribute("previewComments")!=null ) {
            ArrayList list = new ArrayList();
            CommentData cd = new CommentData();
            commentForm.copyTo(cd, request.getLocale());
            list.add(CommentDataWrapper.wrap(cd));
            ctx.put("previewComments",list);
        }
        
        WeblogEntryData entry = rreq.getWeblogEntry();
        if (entry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
            ctx.put("entry", WeblogEntryDataWrapper.wrap(entry));
        }
    }
    
    /**
     * Load objects needed for RSS and Atom newsfeed generation.
     */
    protected static void loadRssValues(
            Map ctx, 
            RollerRequest rreq, 
            WebsiteData website) 
            throws RollerException {
        
        mLogger.debug("Loading rss values");
        
        HttpServletRequest request = rreq.getRequest();
        
        int entryLength = -1;
        String sExcerpts = request.getParameter("excerpts");
        if ( sExcerpts!=null && sExcerpts.equalsIgnoreCase("true")) {
            entryLength = 150;
        }
        ctx.put("entryLength",  new Integer(entryLength));
        
        // Display same number of entries in feed as displayed on page
        int entryCount = website.getEntryDisplayCount();
        
        // But don't exceed installation-wide maxEntries settings
        int maxEntries = 
            RollerRuntimeConfig.getIntProperty("site.newsfeeds.maxEntries");
        int defaultEntries = 
            RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
        if (entryCount < 1) entryCount = defaultEntries;
        if (entryCount > maxEntries) entryCount = maxEntries;
        ctx.put("entryCount",  new Integer(entryCount));
        
        String catname = null;
        String catPath = null;
        if ( rreq.getWeblogCategory() != null ) {
            catname = rreq.getWeblogCategory().getName();
            catPath = rreq.getWeblogCategory().getPath();
        }
        ctx.put("catname", (catname!=null) ? catname : "");
        ctx.put("catPath", (catPath != null) ? catPath : "");
        ctx.put("updateTime", request.getAttribute("updateTime"));
        ctx.put("now", new Date());
    }
    
    /**
     * Load useful utility objects for string and date formatting.
     */
    protected static void loadUtilityObjects(
            Map ctx, 
            RollerRequest rreq,                                             
            RollerContext rollerCtx, 
            WebsiteData website)
            throws RollerException {
        
        mLogger.debug("Loading utility objects");
        
        // date formatter for macro's set this up with the Locale to make 
        // sure we can reuse it with other patterns in the macro's
        Locale viewLocale = (Locale) ctx.get("viewLocale");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", viewLocale);
        if (website != null) {
            sdf.setTimeZone(website.getTimeZoneInstance());
        }
        // add formatter to context
        ctx.put("dateFormatter", sdf );
        
        // Note: in the macro's, the formats are taken from the ResourceBundles.
        // Only the plainFormat is specified here, because it is used to render
        // the Entry Day link.
        ctx.put("plainFormat", "yyyyMMdd");
        
        ctx.put("page",            TemplateWrapper.wrap(rreq.getPage()));
        ctx.put("utilities",       new Utilities() );
        ctx.put("stringUtils",     new OldStringUtils() );
        ctx.put("rollerVersion",   rollerCtx.getRollerVersion() );
        ctx.put("rollerBuildTime", rollerCtx.getRollerBuildTime() );
        ctx.put("rollerBuildUser", rollerCtx.getRollerBuildUser() );
        ctx.put("newsfeedCache",   NewsfeedCache.getInstance() );
        
        ctx.put("requestParameters", rreq.getRequest().getParameterMap());
    }
        
    /**
     * Load URL paths useful in page templates.
     */
    protected static void loadPathValues(
            Map ctx,  RollerRequest rreq, 
            RollerContext rollerCtx, 
            WebsiteData   website)
            throws RollerException {
        
        mLogger.debug("Loading path values");
        
        HttpServletRequest request = rreq.getRequest();
        String url = null;
        if (website != null  && !"zzz_none_zzz".equals(website.getHandle())) {
            url = Utilities.escapeHTML(
                      rollerCtx.getAbsoluteContextUrl(request) 
                          + "/page/" + website.getHandle());
        } else {
            url= Utilities.escapeHTML(rollerCtx.getAbsoluteContextUrl(request));
        }
        ctx.put("websiteURL", url);
        ctx.put("baseURL",    rollerCtx.getContextUrl( request ) );
        ctx.put("absBaseURL", rollerCtx.getAbsoluteContextUrl( request ) );
        ctx.put("ctxPath",    request.getContextPath() );
        ctx.put("uploadPath", ContextLoader.figureResourcePath( rreq ) );
        
        try {
            URL absUrl = RequestUtils.absoluteURL(request, "/");
            ctx.put("host", absUrl.getHost());
        } catch (MalformedURLException e) {
            throw new RollerException(e);
        }
    }
      
    /**
     * Determine URL path to Roller upload directory.
     */
    private static String figureResourcePath(RollerRequest rreq) {
        
        String uploadurl = null;
        try {
            uploadurl = RollerFactory.getRoller().getFileManager().getUploadUrl();
        } catch(Exception e) {}
        
        return uploadurl;
    }
    
    /**
     * If there is an ERROR or STATUS message in the session,
     * place it into the Context for rendering later.
     */
    private static void loadStatusMessage(Map ctx, RollerRequest rreq) {
        
        mLogger.debug("Loading status message");
        
        HttpSession session = rreq.getRequest().getSession(false);
        String msg = null;
        if (session != null)
            msg = (String)session.getAttribute(RollerSession.ERROR_MESSAGE);
        if (msg != null) {
            ctx.put("errorMessage", msg);
            session.removeAttribute(RollerSession.ERROR_MESSAGE);
        }
        
        if (session != null)
            msg = (String)session.getAttribute(RollerSession.STATUS_MESSAGE);
        if (msg != null) {
            ctx.put("statusMessage", msg);
            session.removeAttribute(RollerSession.STATUS_MESSAGE);
        }
    }
        
    protected static void loadRequestParamKeys(Map ctx) {
        
        mLogger.debug("Loading request param keys");
        
        // Since Velocity *requires* accessor methods, these values from
        // RollerRequest are not available to it, put them into the context
        ctx.put("USERNAME_KEY",           RollerRequest.USERNAME_KEY);
        ctx.put("WEBSITEID_KEY",          RollerRequest.WEBSITEID_KEY);
        ctx.put("FOLDERID_KEY",           RollerRequest.FOLDERID_KEY);
        ctx.put("NEWSFEEDID_KEY",         RollerRequest.NEWSFEEDID_KEY);
        ctx.put("PAGEID_KEY",             RollerRequest.PAGEID_KEY);
        ctx.put("PAGELINK_KEY",           RollerRequest.PAGELINK_KEY);
        ctx.put("ANCHOR_KEY",             RollerRequest.ANCHOR_KEY);
        ctx.put("EXCERPTS_KEY",           RollerRequest.EXCERPTS_KEY);
        ctx.put("BOOKMARKID_KEY",         RollerRequest.BOOKMARKID_KEY);
        ctx.put("REFERERID_KEY",          RollerRequest.REFERERID_KEY);
        ctx.put("WEBLOGENTRYID_KEY",      RollerRequest.WEBLOGENTRYID_KEY);
        ctx.put("WEBLOGCATEGORYNAME_KEY", RollerRequest.WEBLOGCATEGORYNAME_KEY);
        ctx.put("WEBLOGCATEGORYID_KEY",   RollerRequest.WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGENTRIES_KEY",      RollerRequest.WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGDAY_KEY",          RollerRequest.WEBLOGDAY_KEY);
        ctx.put("WEBLOGCOMMENTID_KEY",    RollerRequest.WEBLOGCOMMENTID_KEY);
    }
    
    public static ToolboxContext loadToolboxContext(
            HttpServletRequest request,                                                     
            HttpServletResponse response,                                                    
            Map ctx) {
        
        mLogger.debug("Loading toolbox context");
        
        ServletContext servletContext = RollerContext.getServletContext();
        
        // get the toolbox manager
        ServletToolboxManager toolboxManager =
                (ServletToolboxManager)servletContext.getAttribute(TOOLBOX_MANAGER_KEY);
        if (toolboxManager==null) {
            String file = RollerConfig.getProperty("velocity.toolbox.file");
            mLogger.debug("Creating new toolboxContext using config-file: "+file);
            toolboxManager = ServletToolboxManager.getInstance(servletContext, file);
            servletContext.setAttribute(TOOLBOX_MANAGER_KEY, toolboxManager);
        }
        
        // load a toolbox context
        // TODO 3.0: is this okay?
        ChainedContext chainedContext =
                new ChainedContext(new VelocityContext(ctx), request, response, servletContext);
        ToolboxContext toolboxContext =
                toolboxManager.getToolboxContext(chainedContext);
        
        if (toolboxContext != null) {
            // add MessageTool to VelocityContext
            ctx.put("text", toolboxContext.internalGet("text"));
            
            /*
            Object[] keys = toolboxContext.internalGetKeys();
            for (int i=0;i<keys.length;i++) {
                String key = (String)keys[i];
                System.out.println("key = "+key);
                Object tool = toolboxContext.get(key);
                System.out.println("tool = "+tool);
                ctx.put(key, tool);
            }
             */
        }
        
        return toolboxContext;
    }
    
}
