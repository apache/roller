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

package org.apache.roller.weblogger.ui.rendering.velocity.deprecated;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.ThemeTemplateWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.weblogger.util.URLUtilities;

/**
 * Load Velocity Context with Weblogger objects, values, and custom plugins.
 * 
 * NOTE: This class has been deprecated and should no longer be used.  It is
 *       left here so that old weblogs which rely on it will continue to
 *       function properly.  This should only be used by weblog pages.
 */
public class ContextLoader {

    public static final String WEBLOG_KEY             = "weblog";
    public static final String ANCHOR_KEY             = "entry";
    public static final String ANCHOR_KEY_OLD         = "anchor";
    public static final String USERNAME_KEY           = "username";

    public static final String PAGELINK_KEY           = "pagelink";
    public static final String EXCERPTS_KEY           = "excerpts";
    public static final String WEBLOGENTRY_COUNT      = "count";
    public static final String WEBLOGCATEGORYNAME_KEY = "cat";
    public static final String WEBLOGENTRIES_KEY      = "entries";
    public static final String WEBLOGDAY_KEY          = "day";
    
    public static final String WEBLOGENTRYID_KEY      = "entryid";
    
    public static final String WEBLOGCATEGORYID_KEY   = "categoryId";
    public static final String PINGTARGETID_KEY       = "pingtargetId";
    public static final String REFERERID_KEY          = "refId";
    public static final String WEBLOGCOMMENTID_KEY    = "commentId";
    public static final String WEBSITEID_KEY          = "websiteId";
    public static final String BOOKMARKID_KEY         = "bookmarkId";
    public static final String FOLDERID_KEY           = "folderId";
    public static final String PARENTID_KEY           = "parentId";
    public static final String NEWSFEEDID_KEY         = "feedId";
    public static final String PAGEID_KEY             = "pageId";
    public static final String LOGIN_COOKIE           = "sessionId";    
    public static final String OWNING_WEBSITE         = "OWNING_WEBSITE";    

    private static Log mLogger = LogFactory.getLog(ContextLoader.class);
    
    
    /**
     * Setup the a Velocity context by loading it with objects, values, and
     * RollerPagePlugins needed for Weblogger page execution.
     */
    public static void setupContext(
            Map                 ctx,
            HttpServletRequest  request,
            HttpServletResponse response,
            PageContext pageContext,
            WeblogPageRequest pageRequest) throws WebloggerException {
        
        mLogger.debug("setupContext( ctx = "+ctx+")");
        
        Weblog weblog = null;
        WeblogEntry entry = null;
        WeblogCategory category = null;
        ThemeTemplate page = null;
        WeblogBookmarkFolder folder = null;  // don't even know how this is involved :/
        Date date = null;
        boolean isDay = false;
        boolean isMonth = false;
        String locale = null;
        
        // get data from page request
        locale = pageRequest.getLocale();
        weblog = pageRequest.getWeblog();
        entry = pageRequest.getWeblogEntry();
        category = pageRequest.getWeblogCategory();
        page = pageRequest.getWeblogPage();
        if(page == null) {
            page = weblog.getTheme().getDefaultTemplate();
        }
        
        // setup date, isDay, and isMonth
        if(pageRequest.getWeblogDate() != null) {
            
            Date now = new Date();
            if(pageRequest.getWeblogDate().length() == 8) {
                isDay = true;
                try {
                    date = DateUtil.get8charDateFormat().parse(pageRequest.getWeblogDate());
                    if(date.after(now)) {
                        date = now;
                    }
                } catch(Exception e) {
                    // bleh
                }
            } else if(pageRequest.getWeblogDate().length() == 6) {
                isMonth = true;
                try {
                    date = DateUtil.get6charDateFormat().parse(pageRequest.getWeblogDate());
                    if(date.after(now)) {
                        date = now;
                    }
                } catch(Exception e) {
                    // bleh
                }
            } else {
                isMonth = true;
            }
        }
        
        try {
            // Add old page model object to context
            OldWeblogPageModel pageModel = new OldWeblogPageModel();
            pageModel.init(request,
                    weblog,
                    entry,
                    category,
                    date,
                    isDay,
                    isMonth,
                    locale);
            ctx.put("pageModel", pageModel);
            
            // along with old pages list :/
            ctx.put("pages", pageModel.getPages());
            
        } catch (Exception e) {
            throw new WebloggerException("ERROR creating Page Model",e);
        }
        
        // Add page helper to context
        OldPageHelper pageHelper = new OldPageHelper(request, 
                response, 
                ctx, 
                weblog, 
                (date == null) ? new Date() : date, 
                folder, 
                page.getName(), 
                pageContext,
                pageRequest);
        ctx.put("pageHelper", pageHelper);
        
        // Load standard Weblogger objects and values into the context
        loadWeblogValues(ctx, weblog, pageRequest.getLocaleInstance(), request);
        loadPathValues(ctx, request, weblog, locale);
        loadRssValues(ctx, request, weblog, category);
        loadUtilityObjects(ctx, request, weblog, page);
        loadRequestParamKeys(ctx);
        loadStatusMessage(ctx, request);
        
        // If single entry is specified, load comments too
        if (entry != null) {
            loadCommentValues(ctx, request, entry);
        }
    }
    
    
    /**
     * Load website object and related objects.
     */
    private static void loadWeblogValues(
            Map ctx,
            Weblog weblog,
            Locale locale,
            HttpServletRequest request) throws WebloggerException {
        
        // weblog cannot be null
        if(weblog == null)
            return;
        
        Weblogger mRoller = WebloggerFactory.getWeblogger();
        Map props = mRoller.getPropertiesManager().getProperties();
        
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
        ctx.put("website",WeblogWrapper.wrap(weblog) );
        
        String siteName = ((RuntimeConfigProperty)props.get("site.name")).getValue();
        if ("Roller-based Site".equals(siteName)) siteName = "Main";
        ctx.put("siteName", siteName);
        
        String siteShortName = ((RuntimeConfigProperty)props.get("site.shortName")).getValue();
        ctx.put("siteShortName", siteShortName);
        
        // add language of the session (using locale specified by request)
        ctx.put("viewLocale", locale);
        mLogger.debug("context viewLocale = "+ctx.get( "viewLocale"));
        
        // alternative display pages - customization
        ThemeTemplate entryPage = weblog.getTheme().getTemplateByName("_entry");
        if (entryPage != null) {
            ctx.put("entryPage",ThemeTemplateWrapper.wrap(entryPage));
        }
        // TODO: ATLAS: no templates use this, should be safe to remove
        // Template descPage = weblog.getPageByName("_desc");
        //if (descPage != null) {
        //ctx.put("descPage", TemplateWrapper.wrap(descPage));
        //}
        
        boolean commentsEnabled =
                WebloggerRuntimeConfig.getBooleanProperty("users.comments.enabled");
        boolean trackbacksEnabled =
                WebloggerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled");
        boolean linkbacksEnabled =
                WebloggerRuntimeConfig.getBooleanProperty("site.linkbacks.enabled");
        
        ctx.put("commentsEnabled",   new Boolean(commentsEnabled) );
        ctx.put("trackbacksEnabled", new Boolean(trackbacksEnabled) );
        ctx.put("linkbacksEnabled",  new Boolean(linkbacksEnabled) );
    }
    
    
    /**
     * Load comments for one weblog entry and related objects.
     */
    private static void loadCommentValues(
            
            Map ctx,
            HttpServletRequest request,WeblogEntry entry) throws WebloggerException {
        
        mLogger.debug("Loading comment values");
        
        String escapeHtml =
                WebloggerRuntimeConfig.getProperty("users.comments.escapehtml");
        String autoFormat =
                WebloggerRuntimeConfig.getProperty("users.comments.autoformat");
        ctx.put("isCommentPage",     Boolean.TRUE);
        ctx.put("escapeHtml",        new Boolean(escapeHtml) );
        ctx.put("autoformat",        new Boolean(autoFormat) );
        
        // Make sure comment form object is available in context
        WeblogEntryCommentForm commentForm =
                (WeblogEntryCommentForm) request.getAttribute("commentForm");
        if ( commentForm == null ) {
            commentForm = new WeblogEntryCommentForm();
            
            // Set fields to spaces to please Velocity
            commentForm.setName("");
            commentForm.setEmail("");
            commentForm.setUrl("");
            commentForm.setContent("");
        }
        ctx.put("commentForm",commentForm);
        
        // Either put a preview comment in to context
        if(commentForm.isPreview()) {
            ArrayList list = new ArrayList();
            list.add(commentForm.getPreviewComment());
            ctx.put("previewComments", list);
        }
        
        if (entry.getStatus().equals(WeblogEntry.PUBLISHED)) {
            ctx.put("entry",WeblogEntryWrapper.wrap(entry));
        }
    }
    
    
    /**
     * Load objects needed for RSS and Atom newsfeed generation.
     */
    private static void loadRssValues(
            Map ctx,
            HttpServletRequest request,
            Weblog website,
            WeblogCategory category)
            throws WebloggerException {
        
        mLogger.debug("Loading rss values");
        
        int entryLength = -1;
        String sExcerpts = request.getParameter("excerpts");
        if ( sExcerpts!=null && sExcerpts.equalsIgnoreCase("true")) {
            entryLength = 150;
        }
        ctx.put("entryLength",  new Integer(entryLength));
        
        // Display same number of entries in feed as displayed on page
        int entryCount = website.getEntryDisplayCount();
        
        // But don't exceed installation-wide maxEntries settings
        int defaultEntries =
                WebloggerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
        if (entryCount < 1) entryCount = defaultEntries;
        if (entryCount > defaultEntries) entryCount = defaultEntries;
        ctx.put("entryCount",  new Integer(entryCount));
        
        String catname = null;
        String catPath = null;
        if (category != null ) {
            catname = category.getName();
            catPath = category.getPath();
        }
        ctx.put("catname", (catname!=null) ? catname : "");
        ctx.put("catPath", (catPath != null) ? catPath : "");
        ctx.put("updateTime", website.getLastModified());
        ctx.put("now", new Date());
    }
    
    
    /**
     * Load useful utility objects for string and date formatting.
     */
    private static void loadUtilityObjects(
            Map ctx,
            HttpServletRequest request,
            Weblog website,
            ThemeTemplate page) throws WebloggerException {
        
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
        
        ctx.put("page",ThemeTemplateWrapper.wrap(page));
        ctx.put("utilities",       new OldUtilities() );
        ctx.put("stringUtils",     new OldStringUtils() );
        ctx.put("rollerVersion",   WebloggerFactory.getWeblogger().getVersion() );
        ctx.put("rollerBuildTime", WebloggerFactory.getWeblogger().getBuildTime() );
        ctx.put("rollerBuildUser", WebloggerFactory.getWeblogger().getBuildUser() );
        ctx.put("newsfeedCache",   NewsfeedCache.getInstance() );
        
        ctx.put("requestParameters", request.getParameterMap());
    }
    
    
    /**
     * Load URL paths useful in page templates.
     */
    private static void loadPathValues(
            Map ctx,
            HttpServletRequest request,
            Weblog   website,
            String locale) throws WebloggerException {
        
        mLogger.debug("Loading path values");
        
        String url = null;
        if (website != null  && !"zzz_none_zzz".equals(website.getHandle())) {
            url = URLUtilities.getWeblogURL(website, locale, true);
        } else {
            url= WebloggerRuntimeConfig.getAbsoluteContextURL();
        }
        ctx.put("websiteURL", url);
        ctx.put("baseURL",    WebloggerRuntimeConfig.getRelativeContextURL() );
        ctx.put("absBaseURL", WebloggerRuntimeConfig.getAbsoluteContextURL() );
        ctx.put("ctxPath",    WebloggerRuntimeConfig.getRelativeContextURL() );
        ctx.put("uploadPath", ContextLoader.figureResourcePath());
        
        try {
            URL absUrl = new URL(WebloggerRuntimeConfig.getAbsoluteContextURL());
            ctx.put("host", absUrl.getHost());
        } catch (MalformedURLException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    /**
     * Determine URL path to Weblogger upload directory.
     */
    private static String figureResourcePath() {
        
        // legacy junk.  this no longer makes any sense as of 3.0, but oh well
        return "/resources";
    }
    
    
    /**
     * If there is an ERROR or STATUS message in the session,
     * place it into the Context for rendering later.
     */
    private static void loadStatusMessage(Map ctx, HttpServletRequest req) {
        
        mLogger.debug("Loading status message");
        
        HttpSession session = req.getSession(false);
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
    
    
    private static void loadRequestParamKeys(Map ctx) {
        
        mLogger.debug("Loading request param keys");
        
        // Since Velocity *requires* accessor methods, these values from
        // RollerRequest are not available to it, put them into the context
        ctx.put("USERNAME_KEY",           USERNAME_KEY);
        ctx.put("WEBSITEID_KEY",          WEBSITEID_KEY);
        ctx.put("FOLDERID_KEY",           FOLDERID_KEY);
        ctx.put("NEWSFEEDID_KEY",         NEWSFEEDID_KEY);
        ctx.put("PAGEID_KEY",             PAGEID_KEY);
        ctx.put("PAGELINK_KEY",           PAGELINK_KEY);
        ctx.put("ANCHOR_KEY",             ANCHOR_KEY);
        ctx.put("EXCERPTS_KEY",           EXCERPTS_KEY);
        ctx.put("BOOKMARKID_KEY",         BOOKMARKID_KEY);
        ctx.put("REFERERID_KEY",          REFERERID_KEY);
        ctx.put("WEBLOGENTRYID_KEY",      WEBLOGENTRYID_KEY);
        ctx.put("WEBLOGCATEGORYNAME_KEY", WEBLOGCATEGORYNAME_KEY);
        ctx.put("WEBLOGCATEGORYID_KEY",   WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGENTRIES_KEY",      WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGDAY_KEY",          WEBLOGDAY_KEY);
        ctx.put("WEBLOGCOMMENTID_KEY",    WEBLOGCOMMENTID_KEY);
    }
       
}
