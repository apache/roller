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

package org.apache.roller.ui.rendering.model;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.URLUtilities;
import org.apache.struts.util.RequestUtils;


/**
 * Provides access to URL building functionality.
 *
 * NOTE: we purposely go against the standard getter/setter bean standard
 * for methods that take arguments so that users get a consistent way to
 * access those methods in their templates. i.e.
 *
 * $url.category("foo")
 *
 * instead of
 *
 * $url.getCategory("foo")
 */
public class URLModel implements Model {
    
    private static Log log = LogFactory.getLog(URLModel.class);
    
    private WebsiteData weblog = null;
    private String locale = null;
    
    /** TODO 3.0: remove dependency on pageContext */
    private PageContext pageContext = null;
    
    
    public URLModel() {}
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws RollerException {
        
        // need a weblog request so that we can know the weblog and locale
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(weblogRequest == null) {
            throw new RollerException("Expected 'weblogRequest' init param!");
        }
        
        this.weblog = weblogRequest.getWeblog();
        this.locale = weblogRequest.getLocale();
        
        // need page context as well :(
        pageContext = (PageContext) initData.get("pageContext");
    }
    
    
    /** Relative URL of Roller, e.g. /roller */
    public String getSite() {
        return RollerRuntimeConfig.getRelativeContextURL();
    }
    
    
    /** Absolute URL of Roller, e.g. http://localhost:8080/roller */
    public String getAbsoluteSite() {
        return RollerRuntimeConfig.getAbsoluteContextURL();
    }
    
    
    /** URL for logging in */  
    public String getLogin() {
        String returnURL = null;
        try {
            returnURL = RequestUtils.computeURL(pageContext,
                "login-redirect", null, null, null, null, null, false);
        } catch (MalformedURLException mue) {
            log.error("ERROR forming Struts URL: ", mue);
        }
        return returnURL;
    }
    
    
    /** URL for logging out */
    public String getLogout() {
        String returnURL = null;
        try {
            returnURL = RequestUtils.computeURL(pageContext,
                "logout-redirect", null, null, null, null, null, false);
        } catch (MalformedURLException mue) {
            log.error("ERROR forming Struts URL: ", mue);
        }
        return returnURL;
    }
    
    
    public String themeResource(String theme, String filePath) {
        return getSite()+RollerRuntimeConfig.getProperty("users.themes.path")+"/"+theme+"/"+filePath;
    }
    
    
    public String getHome() {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, null, null, -1, false);
    }
    
    
    public String home(int pageNum) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, null, null, pageNum, false);
    }
    
    
    public String entry(String anchor) {
        return URLUtilities.getWeblogEntryURL(weblog, locale, anchor, true);
    }
    
    
    public String comments(String anchor) {
        return URLUtilities.getWeblogCommentsURL(weblog, locale, anchor, true);
    }
    
    
    public String trackback(String anchor) {
        return URLUtilities.getWeblogEntryURL(weblog, locale, anchor, true);
    }

    
    public String date(String dateString) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, null, dateString, -1, false);
    }
    
    
    public String date(String dateString, int pageNum) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, null, dateString, pageNum, false);
    }
    
    
    public String category(String catPath) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, catPath, null, -1, false);
    }
    
    
    public String category(String catPath, int pageNum) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, catPath, null, pageNum, false);
    }
    
    
    public String collection(String dateString, String catPath) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, catPath, dateString, -1, false);
    }
    
    
    public String collection(String dateString, String catPath, int pageNum) {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, catPath, dateString, pageNum, false);
    }
    
    
    public String getSearch() {
        return URLUtilities.getWeblogSearchURL(weblog, locale, null, null, -1, false);
    }
    
    
    public String search(String query, int pageNum) {
        return URLUtilities.getWeblogSearchURL(weblog, locale, query, null, pageNum, false);
    }
    
    
    public String search(String query, String catPath, int pageNum) {
        return URLUtilities.getWeblogSearchURL(weblog, locale, query, catPath, pageNum, false);
    }
    
    
    public String page(String pageLink) {
        return URLUtilities.getWeblogPageURL(weblog, locale, pageLink, null, null, null, -1, false);
    }
    
    
    public String page(String pageLink, String dateString, String catPath, int pageNum) {
        return URLUtilities.getWeblogPageURL(weblog, locale, pageLink, null, catPath, dateString, pageNum, false);
    }
    
    
    public String resource(String filePath) {
        return URLUtilities.getWeblogResourceURL(weblog, filePath, false);
    }
    
    
    public String getRsd() {
        return URLUtilities.getWeblogRsdURL(weblog, false);
    }
    
    
    public FeedURLS getFeed() {
        return new FeedURLS();
    }
    
    
    /** URL for editing a weblog entry */
    public String editEntry(String anchor) {
        String ret = null;
        Map params = new HashMap();
        params.put(RequestConstants.ANCHOR, anchor);
        try {
            ret = RequestUtils.computeURL(pageContext,
                "weblogEdit", null, null, null, params, null, false);
        } catch (MalformedURLException mue) {
            log.error("ERROR forming Struts URL: ", mue);
        }
        return ret;
    } 
    
    
    /** URL for creating a new weblog entry */
    public String getCreateEntry() {
        String returnURL = null;
        Map params = new HashMap();
        params.put(RequestConstants.WEBLOG, weblog.getHandle());
        try {
            returnURL = RequestUtils.computeURL(pageContext,
                "weblogCreate", null, null, null, params, null, false);
        } catch (MalformedURLException mue) {
            log.error("ERROR forming Struts URL: ", mue);
        }
        return returnURL;
    }
    
    
    /** URL for editing weblog settings */
    public String getEditSettings() {
        String returnURL = null;
        Map params = new HashMap();
        params.put(RequestConstants.WEBLOG, weblog.getHandle());        
        try {
            returnURL = RequestUtils.computeURL(pageContext,
                "editWebsite", null, null, null, params, null, false);
        } catch (MalformedURLException mue) {
            log.error("ERROR forming Struts URL: ", mue);
        }
        return returnURL;
    }
    
    
    ///////  Inner Classes  ///////
    
    public class FeedURLS {
        
        public EntryFeedURLS getEntries() {
            return new EntryFeedURLS();
        }
        
        public CommentFeedURLS getComments() {
            return new CommentFeedURLS();
        }
    }
    
    public class EntryFeedURLS {
        
        public String getRss() {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "entries", "rss", null, false, false);
        }
        
        public String rss(String catPath, boolean excerpts) {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "entries", "rss", catPath, excerpts, false);
        }
        
        public String getAtom() {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "entries", "atom", null, false, false);
        }
        
        public String atom(String catPath, boolean excerpts) {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "entries", "atom", catPath, excerpts, false);
        }
    }
    
    public class CommentFeedURLS {
        
        public String getRss() {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "comments", "rss", null, false, false);
        }
        
        public String rss(String catPath, boolean excerpts) {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "comments", "rss", catPath, excerpts, false);
        }
        
        public String getAtom() {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "comments", "atom", null, false, false);
        }
        
        public String atom(String catPath, boolean excerpts) {
            return URLUtilities.getWeblogFeedURL(weblog, locale, "comments", "atom", catPath, excerpts, false);
        }
        
    }
    
}
