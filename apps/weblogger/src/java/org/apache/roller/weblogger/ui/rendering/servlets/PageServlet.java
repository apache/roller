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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.HitCountQueue;
import org.apache.roller.weblogger.business.referrers.IncomingReferrer;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManager;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.StaticThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.rendering.util.InvalidRequestException;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.model.ModelLoader;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.util.BlacklistChecker;


/**
 * Provides access to weblog pages.
 *
 * @web.servlet name="PageServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/page/*"
 */
public class PageServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(PageServlet.class);
    
    // for referrer processing
    private boolean processReferrers = true;
    private static Pattern robotPattern = null;
    
    // for caching
    private boolean excludeOwnerPages = false;
    private WeblogPageCache weblogPageCache = null;
    private SiteWideCache siteWideCache = null;
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing PageServlet");
        
        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.excludeOwnerEditPages");
        
        // get a reference to the weblog page cache
        this.weblogPageCache = WeblogPageCache.getInstance();
        
        // get a reference to the site wide cache
        this.siteWideCache = SiteWideCache.getInstance();
        
        // see if built-in referrer processing is enabled
        this.processReferrers = 
                RollerConfig.getBooleanProperty("referrers.processing.enabled");
        
        log.info("Referrer processing enabled = "+this.processReferrers);
        
        // check for possible robot pattern
        String robotPatternStr = RollerConfig.getProperty("referrer.robotCheck.userAgentPattern");
        if (robotPatternStr != null && robotPatternStr.length() > 0) {
            // Parse the pattern, and store the compiled form.
            try {
                robotPattern = Pattern.compile(robotPatternStr);
            } catch (Exception e) {
                // Most likely a PatternSyntaxException; log and continue as if it is not set.
                log.error("Error parsing referrer.robotCheck.userAgentPattern value '" +
                        robotPatternStr + "'.  Robots will not be filtered. ", e);
            }
        }
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        // do referrer processing, if it's enabled
        // NOTE: this *must* be done first because it triggers a hibernate flush
        // which will close the active session and cause lazy init exceptions otherwise
        if(this.processReferrers) {
            boolean spam = this.processReferrer(request);
            if(spam) {
                log.debug("spammer, giving 'em a 403");
                if(!response.isCommitted()) response.reset();
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        
        Weblog weblog = null;
        boolean isSiteWide = false;
        
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
            
            weblog = pageRequest.getWeblog();
            if(weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "+
                        pageRequest.getWeblogHandle());
            }
            
            // is this the site-wide weblog?
            isSiteWide = RollerRuntimeConfig.isSiteWideWeblog(pageRequest.getWeblogHandle());
            
        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // determine the lastModified date for this content
        long lastModified = System.currentTimeMillis();
        if(isSiteWide) {
            lastModified = siteWideCache.getLastModified().getTime();
        } else if (weblog.getLastModified() != null) {
            lastModified = weblog.getLastModified().getTime();
        }

        // 304 Not Modified handling.
        // We skip this for logged in users to avoid the scenerio where a user
        // views their weblog, logs in, then gets a 304 without the 'edit' links
        if(!pageRequest.isLoggedIn()) {
            if (ModDateHeaderUtil.respondIfNotModified(request,response,lastModified)) {
                return;
            } else {
                // set last-modified date
                ModDateHeaderUtil.setLastModifiedHeader(response,lastModified);
            }
        }

                
        // generate cache key
        String cacheKey = null;
        if(isSiteWide) {
            cacheKey = siteWideCache.generateKey(pageRequest);
        } else {
            cacheKey = weblogPageCache.generateKey(pageRequest);
        }
        
        // cached content checking
        if((!this.excludeOwnerPages || !pageRequest.isLoggedIn()) &&
                request.getAttribute("skipCache") == null) {
            
            CachedContent cachedContent = null;
            if(isSiteWide) {
                cachedContent = (CachedContent) siteWideCache.get(cacheKey);
            } else {
                cachedContent = (CachedContent) weblogPageCache.get(cacheKey, lastModified);
            }
            
            if(cachedContent != null) {
                log.debug("HIT "+cacheKey);
                
                // allow for hit counting
                if(!isSiteWide) {
                    this.processHit(weblog, request.getRequestURL().toString(), request.getHeader("referer"));
                }
        
                response.setContentLength(cachedContent.getContent().length);
                response.setContentType(cachedContent.getContentType());
                response.getOutputStream().write(cachedContent.getContent());
                return;
                
            } else {
                log.debug("MISS "+cacheKey);
            }
        }
        
        
        // figure out what template to use
        ThemeTemplate page = null;
        
        // If this is a popup request, then deal with it specially
        // TODO: do we really need to keep supporting this?
        if (request.getParameter("popup") != null) {
            try {
                // Does user have a popupcomments page?
                page = weblog.getPageByName("_popupcomments");
            } catch(Exception e ) {
                // ignored ... considered page not found
            }
            
            // User doesn't have one so return the default
            if(page == null) {
                page = new StaticThemeTemplate("templates/weblog/popupcomments.vm", "velocity");
            }
            
        // If request specified the page, then go with that
        } else if("page".equals(pageRequest.getContext())) {
            page = pageRequest.getWeblogPage();
            
        // If request specified tags section index, then look for custom template
        } else if("tags".equals(pageRequest.getContext()) &&
                pageRequest.getTags() == null) {
            try {
                page = weblog.getPageByAction(ThemeTemplate.ACTION_TAGSINDEX);
            } catch(Exception e) {
                log.error("Error getting weblog page for action 'tagsIndex'", e);
            }
            
            // if we don't have a custom tags page then 404, we don't let
            // this one fall through to the default template
            if(page == null) {
                if(!response.isCommitted()) response.reset();
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
        // If this is a permalink then look for a permalink template
        } else if(pageRequest.getWeblogAnchor() != null) {
            try {
                page = weblog.getPageByAction(ThemeTemplate.ACTION_PERMALINK);
            } catch(Exception e) {
                log.error("Error getting weblog page for action 'permalink'", e);
            }
        }
        
        // if we haven't found a page yet then try our default page
        if(page == null) {
            try {
                page = weblog.getDefaultPage();
            } catch(Exception e) {
                log.error("Error getting default page for weblog = "+
                        weblog.getHandle(), e);
            }
        }
        
        // Still no page?  Then that is a 404
        if (page == null) {
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        log.debug("page found, dealing with it");
        
        
        // validation.  make sure that request input makes sense.
        boolean invalid = false;
        if(pageRequest.getWeblogPageName() != null && page.isHidden()) {
            invalid = true;
        }
        if(pageRequest.getLocale() != null) {
            
            // locale view only allowed if weblog has enabled it
            if(!pageRequest.getWeblog().isEnableMultiLang()) {
                invalid = true;
            }
            
        }
        if(pageRequest.getWeblogAnchor() != null) {
            
            // permalink specified.
            // entry must exist, be published before current time, and locale must match
            WeblogEntry entry = pageRequest.getWeblogEntry();
            if(entry == null) {
                invalid = true;
            } else if (pageRequest.getLocale() != null && 
                    !entry.getLocale().startsWith(pageRequest.getLocale())) {
                invalid = true;
            } else if (!entry.isPublished()) {
                invalid = true;
            } else if (new Date().before(entry.getPubTime())) {
                invalid = true;
            }
            
        } else if(pageRequest.getWeblogCategoryName() != null) {
            
            // category specified.  category must exist.
            if(pageRequest.getWeblogCategory() == null) {
                invalid = true;
            }
            
        } else if(pageRequest.getTags() != null && pageRequest.getTags().size() > 0) {
            
            try {
                // tags specified.  make sure they exist.
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                invalid = !wmgr.getTagComboExists(pageRequest.getTags(), (isSiteWide) ? null : weblog);
            } catch (WebloggerException ex) {
                invalid = true;
            }
        }

        
        if(invalid) {
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // allow for hit counting
        if(!isSiteWide) {
            this.processHit(weblog, request.getRequestURL().toString(), request.getHeader("referer"));
        }
        

        // looks like we need to render content
        
        // set the content type
        String contentType = "text/html; charset=utf-8";
        if (StringUtils.isNotEmpty(page.getOutputContentType())) {
            contentType = page.getOutputContentType() + "; charset=utf-8";
        } else {
            String mimeType = RollerContext.getServletContext().getMimeType(page.getLink()); 
            if (mimeType != null) {
                // we found a match ... set the content type
                contentType = mimeType + "; charset=utf-8";
            } else {
                contentType = "text/html; charset=utf-8";
            }
        }

        HashMap model = new HashMap();
        try {
            PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", false, 8192, true);
            
            // special hack for menu tag
            request.setAttribute("pageRequest", pageRequest);
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            initData.put("requestParameters", request.getParameterMap());
            initData.put("weblogRequest", pageRequest);
            initData.put("pageContext", pageContext);
            
            // if this was a comment posting, check for comment form
            WeblogEntryCommentForm commentForm = 
                    (WeblogEntryCommentForm) request.getAttribute("commentForm");
            if(commentForm != null) {
                initData.put("commentForm", commentForm);
            }
            
            // Load models for pages
            String pageModels = RollerConfig.getProperty("rendering.pageModels");
            ModelLoader.loadModels(pageModels, model, initData, true);
            
            // Load special models for site-wide blog
            if(RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                String siteModels = RollerConfig.getProperty("rendering.siteModels");
                ModelLoader.loadModels(siteModels, model, initData, true);
            }

            // Load weblog custom models
            ModelLoader.loadCustomModels(weblog, model, initData);
            
            // ick, gotta load pre-3.0 model stuff as well :(
            ModelLoader.loadOldModels(model, request, response, pageContext, pageRequest);
            
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
            log.error("Couldn't find renderer for page "+page.getId(), e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content.  use size of about 24K for a standard page
        CachedContent rendererOutput = new CachedContent(24567, contentType);
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
        
        // cache rendered content.  only cache if user is not logged in?
        if((!this.excludeOwnerPages || !pageRequest.isLoggedIn()) &&
                request.getAttribute("skipCache") == null) {
            log.debug("PUT "+cacheKey);
            
            // put it in the right cache
            if(isSiteWide) {
                siteWideCache.put(cacheKey, rendererOutput);
            } else {
                weblogPageCache.put(cacheKey, rendererOutput);
            }
        } else {
            log.debug("SKIPPED "+cacheKey);
        }
        
        log.debug("Exiting");
    }
        
    
    /**
     * Handle POST requests.
     *
     * We have this here because the comment servlet actually forwards some of
     * its requests on to us to render some pages with cusom messaging.  We
     * may want to revisit this approach in the future and see if we can do
     * this in a different way, but for now this is the easy way.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // make sure caching is disabled
        request.setAttribute("skipCache", "true");
        
        // handle just like a GET request
        this.doGet(request, response);
    }
    
    
    /**
     * Notify the hit tracker that it has an incoming page hit.
     */
    private void processHit(Weblog weblog, String url, String referrer) {
        
        HitCountQueue counter = HitCountQueue.getInstance();
        counter.processHit(weblog, url, referrer);
    }
    
    
    /**
     * Process the incoming request to extract referrer info and pass it on
     * to the referrer processing queue for tracking.
     *
     * @returns true if referrer was spam, false otherwise
     */
    private boolean processReferrer(HttpServletRequest request) {
        
        log.debug("processing referrer for "+request.getRequestURI());
        
        // bleh!  because ref processing does a flush it will close
        // our hibernate session and cause lazy init exceptions on
        // objects we have fetched, so we need to use a separate
        // page request object for this
        WeblogPageRequest pageRequest;
        try {
            pageRequest = new WeblogPageRequest(request);
        } catch (InvalidRequestException ex) {
            return false;
        }
        
        // if this came from site-wide frontpage then skip it
        if(RollerRuntimeConfig.isSiteWideWeblog(pageRequest.getWeblogHandle())) {
            return false;
        }
        
        // if this came from a robot then don't process it
        if (robotPattern != null) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 0 && 
                    robotPattern.matcher(userAgent).matches()) {
                log.debug("skipping referrer from robot");
                return false;
            }
        }
        
        String referrerUrl = request.getHeader("Referer");
        StringBuffer reqsb = request.getRequestURL();
        if (request.getQueryString() != null) {
            reqsb.append("?");
            reqsb.append(request.getQueryString());
        }
        String requestUrl = reqsb.toString();
        
        log.debug("referrer = "+referrerUrl);
        
        // if this came from persons own blog then don't process it
        String selfSiteFragment = "/"+pageRequest.getWeblogHandle();
        if (referrerUrl != null && referrerUrl.indexOf(selfSiteFragment) != -1) {
            log.debug("skipping referrer from own blog");
            return false;
        }
        
        // validate the referrer
        if (pageRequest != null && pageRequest.getWeblogHandle() != null) {
            
            // Base page URLs, with and without www.
            String basePageUrlWWW =
                    RollerRuntimeConfig.getAbsoluteContextURL() + "/" + pageRequest.getWeblogHandle();
            String basePageUrl = basePageUrlWWW;
            if ( basePageUrlWWW.startsWith("http://www.") ) {
                // chop off the http://www.
                basePageUrl = "http://"+basePageUrlWWW.substring(11);
            }
            
            // ignore referrers coming from users own blog
            if (referrerUrl == null ||
                    (!referrerUrl.startsWith(basePageUrl) &&
                    !referrerUrl.startsWith(basePageUrlWWW))) {
                
                // validate the referrer
                if ( referrerUrl != null ) {
                    // treat editor referral as direct
                    int lastSlash = requestUrl.indexOf("/", 8);
                    if (lastSlash == -1) lastSlash = requestUrl.length();
                    String requestSite = requestUrl.substring(0, lastSlash);
                    
                    if (referrerUrl.matches(requestSite + ".*\\.rol.*")) {
                        referrerUrl = null;
                    } else if(BlacklistChecker.checkReferrer(pageRequest.getWeblog(), referrerUrl)) {
                        return true;
                    }
                }

            } else {
                log.debug("Ignoring referer = "+referrerUrl);
                return false;
            }
        }
        
        // referrer is valid, lets record it
        try {
            IncomingReferrer referrer = new IncomingReferrer();
            referrer.setReferrerUrl(referrerUrl);
            referrer.setRequestUrl(requestUrl);
            referrer.setWeblogHandle(pageRequest.getWeblogHandle());
            referrer.setWeblogAnchor(pageRequest.getWeblogAnchor());
            referrer.setWeblogDateString(pageRequest.getWeblogDate());
            
            ReferrerQueueManager refQueue =
                    RollerFactory.getRoller().getReferrerQueueManager();
            refQueue.processReferrer(referrer);
        } catch(Exception e) {
            log.error("Error processing referrer", e);
        }
        
        return false;
    }
    
}
