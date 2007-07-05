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

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;


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
    
    private Weblog weblog = null;
    private String locale = null;
    
    private URLStrategy urlStrategy = null;
    
    /** TODO: remove dependency on pageContext */
    private PageContext pageContext = null;
    
    
    public URLModel() {}
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws WebloggerException {
        
        // need a weblog request so that we can know the weblog and locale
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(weblogRequest == null) {
            throw new WebloggerException("Expected 'weblogRequest' init param!");
        }
        
        this.weblog = weblogRequest.getWeblog();
        this.locale = weblogRequest.getLocale();
        
        // url strategy 
        urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
        
        // need page context as well :(
        pageContext = (PageContext) initData.get("pageContext");
    }
    
    
    /** Relative URL of Roller, e.g. /roller */
    public String getSite() {
        return WebloggerRuntimeConfig.getRelativeContextURL();
    }
    
    
    /** Absolute URL of Roller, e.g. http://localhost:8080/roller */
    public String getAbsoluteSite() {
        return WebloggerRuntimeConfig.getAbsoluteContextURL();
    }
    
    
    /** URL for logging in */  
    public String getLogin() {
        return urlStrategy.getLoginURL(false);
    }
    
    
    /** URL for logging out */
    public String getLogout() {
        return urlStrategy.getLogoutURL(false);
    }
    
    
    /** URL for a specific UI action */
    public String action(String action, String namespace) {
        if(namespace != null) {
            if("/roller-ui".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            } else if("/roller-ui/authoring".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, weblog.getHandle(), null, true);
            } else if("/roller-ui/admin".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            }
        }
        return null;
    }
    
    
    public String getCommentAuthenticator() {
        return getSite()+"/CommentAuthenticatorServlet";
    }
    
    
    public String themeResource(String theme, String filePath) {
        return getSite()+"/themes/"+theme+"/"+filePath;
    }
        
    public String themeResource(String theme, String filePath, boolean absolute) {
        if (absolute) {
            return getAbsoluteSite()+"/themes/"+theme+"/"+filePath;
        }
        return themeResource(theme, filePath);
    }
        
    public String getHome() {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, null, -1, true);
    }
    
    
    public String home(int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, null, pageNum, true);
    }
    
    
    public String home(String customLocale) {
        return urlStrategy.getWeblogCollectionURL(weblog, customLocale, null, null, null, -1, true);
    }
    
    
    public String home(String customLocale, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, customLocale, null, null, null, pageNum, true);
    }
    
    
    public String entry(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, locale, anchor, true);
    }
    
    public String comment(String anchor, String timeStamp) {
        return urlStrategy.getWeblogCommentURL(weblog, locale, anchor, timeStamp, true);
    }
    
    
    public String comments(String anchor) {
        return urlStrategy.getWeblogCommentsURL(weblog, locale, anchor, true);
    }
    
    
    public String trackback(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, locale, anchor, true);
    }

    
    public String date(String dateString) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, dateString, null, -1, true);
    }
    
    
    public String date(String dateString, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, dateString, null, pageNum, true);
    }
    
    
    public String category(String catPath) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, catPath, null, null, -1, true);
    }
    
    
    public String category(String catPath, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, catPath, null, null, pageNum, true);
    }
    
    
    public String tag(String tag) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, Arrays.asList(new String[]{tag}) , -1, true);
    }
    
    
    public String tag(String tag, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, Arrays.asList(new String[]{tag}), pageNum, true);
    }    
    
    
    public String tags(List tags) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, tags , -1, true);
    }
    
    
    public String tags(List tags, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, null, null, tags, -1, true);
    }
    
    
    public String collection(String dateString, String catPath) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, catPath, dateString, null, -1, true);
    }
    
    
    public String collection(String dateString, String catPath, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, locale, catPath, dateString, null, pageNum, true);
    }
    
    
    public String getSearch() {
        return urlStrategy.getWeblogSearchURL(weblog, locale, null, null, -1, false);
    }
    
    
    public String search(String query, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, locale, query, null, pageNum, false);
    }
    
    
    public String search(String query, String catPath, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, locale, query, catPath, pageNum, false);
    }
    
    public String absoluteSearch(String query, String catPath, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, locale, query, catPath, pageNum, true);
    }        
    
    public String page(String pageLink) {
        return urlStrategy.getWeblogPageURL(weblog, locale, pageLink, null, null, null, null, -1, true);
    }
    
    
    public String page(String pageLink, String dateString, String catPath, int pageNum) {
        return urlStrategy.getWeblogPageURL(weblog, locale, pageLink, null, catPath, dateString, null, pageNum, true);
    }
    
    
    public String resource(String filePath) {
        return urlStrategy.getWeblogResourceURL(weblog, filePath, true);
    }
    
    
    public String getRsd() {
        return urlStrategy.getWeblogRsdURL(weblog, true);
    }
    
    
    public FeedURLS getFeed() {
        return new FeedURLS();
    }
    
    
    /** URL for editing a weblog entry */
    public String editEntry(String anchor) {
        try {
            // need to determine entryId from anchor
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            WeblogEntry entry = wmgr.getWeblogEntryByAnchor(weblog, anchor);
            if(entry != null) {
                return urlStrategy.getEntryEditURL(weblog.getHandle(), entry.getId(), false);
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up entry by anchor - "+anchor, ex);
        }
        return null;
    } 
    
    
    /** URL for creating a new weblog entry */
    public String getCreateEntry() {
        return urlStrategy.getEntryAddURL(weblog.getHandle(), false);
    }
    
    
    /** URL for editing weblog settings */
    public String getEditSettings() {
        return urlStrategy.getWeblogConfigURL(weblog.getHandle(), false);
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
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "rss", null, null, null, false, true);
        }
        
        public String rss(String catPath, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "rss", catPath, null, null, excerpts, true);
        }
        
        public String rssByTags(List tags, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "rss", null, null, tags, excerpts, true);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "atom", null, null, null, false, true);
        }
        
        public String atom(String catPath, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "atom", catPath, null, null, excerpts, true);
        }
        
        public String search(String term, String catPath) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "atom", catPath, term, null, false, true);
        }        
        
        public String atomByTags(List tags, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "entries", "atom", null, null, tags, excerpts, true);
        }
    }
    
    public class CommentFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "comments", "rss", null, null, null, false, true);
        }
        
        public String rss(String catPath, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "comments", "rss", catPath, null, null, excerpts, true);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "comments", "atom", null, null, null, false, true);
        }
        
        public String atom(String catPath, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, locale, "comments", "atom", catPath, null, null, excerpts, true);
        }
        
    }
    
}
