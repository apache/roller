/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
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
    
    protected Weblog weblog;

    private WeblogEntryManager weblogEntryManager;

    protected URLStrategy urlStrategy;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public URLModel() {}
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws WebloggerException {
        // need a weblog request so that we can know the weblog and locale
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(weblogRequest == null) {
            throw new WebloggerException("Expected 'weblogRequest' init param!");
        }
        
        this.weblog = weblogRequest.getWeblog();
    }
    
    
    /** Relative URL of Roller, e.g. /roller */
    public String getSite() {
        return WebloggerStaticConfig.getRelativeContextURL();
    }
    
    
    /** Absolute URL of Roller, e.g. http://localhost:8080/roller */
    public String getAbsoluteSite() {
        return WebloggerStaticConfig.getAbsoluteContextURL();
    }

    /** URL for logging in */  
    public String getLogin() {
        return urlStrategy.getLoginURL(false);
    }


    /** URL for logging out */
    public String getLogout() {
        return urlStrategy.getLogoutURL(false);
    }
    
    /** URL for registering */
    public String getRegister() {
        return urlStrategy.getRegisterURL(false);
    }
    
    
    /** URL for a specific UI action */
    public String action(String action, String namespace) {
        if(namespace != null) {
            if("/tb-ui".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            } else if("/tb-ui/authoring".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, weblog.getHandle(), null, true);
            } else if("/tb-ui/admin".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            }
        }
        return null;
    }
    
    
    public String getCommentAuthenticator() {
        return getSite()+"/CommentAuthenticatorServlet?weblog="+weblog.getHandle();
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
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, -1, true);
    }
    
    
    public String home(int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, pageNum, true);
    }
    
    
    public String home() {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, -1, true);
    }


    public String entry(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, anchor, true);
    }

    public String comment(String anchor, String timeStamp) {
        return urlStrategy.getWeblogCommentURL(weblog, anchor, timeStamp, true);
    }


    public String comments(String anchor) {
        return urlStrategy.getWeblogCommentsURL(weblog, anchor, true);
    }


    public String trackback(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, anchor, true);
    }

    
    public String date(String dateString) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, dateString, null, -1, true);
    }
    
    
    public String date(String dateString, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, dateString, null, pageNum, true);
    }
    
    
    public String category(String catName) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, null, null, -1, true);
    }
    
    
    public String category(String catName, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, null, null, pageNum, true);
    }
    
    
    public String tag(String tag) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, Arrays.asList(new String[]{tag}) , -1, true);
    }
    
    
    public String tag(String tag, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, Arrays.asList(new String[]{tag}), pageNum, true);
    }    
    
    
    public String tags(List tags) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, tags , -1, true);
    }
    
    
    public String tags(List tags, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, tags, -1, true);
    }
    
    
    public String collection(String dateString, String catName) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, dateString, null, -1, true);
    }
    
    
    public String collection(String dateString, String catName, int pageNum) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, dateString, null, pageNum, true);
    }
    
    
    public String getSearch() {
        return urlStrategy.getWeblogSearchURL(weblog, null, null, -1, false);
    }
    
    public String search(String query, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, query, null, pageNum, false);
    }
    
    
    public String search(String query, String catName, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, query, catName, pageNum, false);
    }
    
    public String absoluteSearch(String query, String catName, int pageNum) {
        return urlStrategy.getWeblogSearchURL(weblog, query, catName, pageNum, true);
    }        
    
    public String page(String theme, String pageLink) {
        return urlStrategy.getWeblogPageURL(weblog, theme, pageLink, null, null, null, null, -1, true);
    }

    public String page(String pageLink, String dateString, String catName, int pageNum) {
        return urlStrategy.getWeblogPageURL(weblog, null, pageLink, null, catName, dateString, null, pageNum, true);
    }
    
    
    public FeedURLS getFeed() {
        return new FeedURLS();
    }
    
    
    /** URL for editing a weblog entry */
    public String editEntry(String anchor) {
        try {
            // need to determine entryId from anchor
            WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, anchor);
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

        public MediaFileFeedURLS getMediaFiles() {
            return new MediaFileFeedURLS();
        }
    }
    
    public class EntryFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", null, null, null, false, true);
        }
        
        public String rss(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", catName, null, null, excerpts, true);
        }
        
        public String rssByTags(List tags, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", null, null, tags, excerpts, true);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", null, null, null, false, true);
        }
        
        public String atom(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", catName, null, null, excerpts, true);
        }
        
        public String search(String term, String catName) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", catName, term, null, false, true);
        }        
        
        public String atomByTags(List tags, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", null, null, tags, excerpts, true);
        }
    }
    
    public class CommentFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "rss", null, null, null, false, true);
        }
        
        public String rss(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "rss", catName, null, null, excerpts, true);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "atom", null, null, null, false, true);
        }
        
        public String atom(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "atom", catName, null, null, excerpts, true);
        }
        
    }
    
    public class MediaFileFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, "files", "rss", null, null, null, false, true);
        }
        
        public String rss(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "files", "rss", catName, null, null, excerpts, true);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, "files", "atom", null, null, null, false, true);
        }
        
        public String atom(String catName, boolean excerpts) {
            return urlStrategy.getWeblogFeedURL(weblog, "files", "atom", catName, null, null, excerpts, true);
        }
        
    }
}
