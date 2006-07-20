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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RequestConstants;
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
    
    /** TODO 3.0: remove dependency on pageContext */
    private PageContext pageContext = null;
    
    public URLModel() {        
    }

    public URLModel(WebsiteData weblog) {
        this.weblog = weblog;
    }
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws RollerException {
        
        // need a weblog to base the weblog specific urls off of
        weblog = (WebsiteData) initData.get("weblog");
        if(weblog == null) {
            throw new RollerException("Expected 'weblog' init param!");
        }
        
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
    
    
    public String getHome() {
        return weblog.getURL();
    }
    
    
    public String home(int pageNum) {
        String url = getHome();
        if(pageNum > 0)
            url += "?page="+pageNum;
        return url;
    }
    
    
    public String entry(String anchor, String catPath) {
        String ret = weblog.getURL()+"/entry/"+anchor;
        if (catPath != null) {
            ret += "?cat="+catPath;
        }
        return ret;
    }
    
    public String trackback(String anchor) {
        return weblog.getURL()+"/entry/"+anchor;
    }

    public String date(String dateString) {
        return weblog.getURL()+"/date/"+dateString;
    }
    
    
    public String date(String dateString, int pageNum) {
        String url = date(dateString);
        if(pageNum > 0)
            url += "?page="+pageNum;
        return url;
    }
    
    
    public String category(String catPath) {
        String cat = catPath;
        if (cat.length() > 1 && cat.startsWith("/")) cat = cat.substring(1);
        try {
            cat = URLEncoder.encode(catPath, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("url encoding problem", ex);
        }
        return weblog.getURL()+"/category/"+cat;
    }
    
    
    public String category(String catPath, int pageNum) {
        String url = category(catPath);
        if(pageNum > 0)
            url += "?page="+pageNum;
        return url;
    }
    
    
    public String collection(String dateString, String catPath) {
        String url = null;
        if(dateString != null && catPath != null) {
            url = weblog.getURL();
            url += "?date="+dateString;
            url += "&cat="+catPath;
        } else if(dateString != null) {
            url = date(dateString);
        } else if(catPath != null) {
            url = category(catPath);
        }
        return url;
    }
    
    
    public String collection(String dateString, String catPath, int pageNum) {
        String url = null;
        if(dateString != null && catPath != null) {
            url = collection(dateString, catPath);
            if(pageNum > 0)
                url += "&page="+pageNum;
        } else if(dateString != null) {
            url = date(dateString, pageNum);
        } else if(catPath != null) {
            url = category(catPath, pageNum);
        }
        return url;
    }
    
    
    public String getSearch() {
        return weblog.getURL()+"/search";
    }
    
    public String search(String query, int pageNum) {
        return weblog.getURL()+"/search?q="+query+"&page="+pageNum;
    }
    
    public String search(String query, String catPath, int pageNum) {
        String ret = weblog.getURL()+"/search?q="+query+"&page="+pageNum;
        if (catPath != null) {
            ret += "?cat="+catPath;
        }
        return ret;
    }
    
    public String page(String pageLink) {
        return weblog.getURL()+"/page/"+pageLink;
    }
    
    
    public String page(String pageLink, String dateString, String catPath, int pageNum) {
        String url = page(pageLink);
        String qString = "?";
        if(dateString != null) {
            qString += "date="+dateString;
        }
        if(catPath != null) {
            if(!qString.endsWith("?"))
                qString += "&";
            qString += "cat="+catPath;
        }
        if(pageNum > 0) {
            if(!qString.endsWith("?"))
                qString += "&";
            qString += "page="+pageNum;
        }
        return url;
    }
    
    
    public String resource(String filePath) {
        return weblog.getURL()+"/resource/"+filePath;
    }
    
    
    public String themeResource(String filePath) {
        return getSite()+RollerRuntimeConfig.getProperty("users.themes.path")+"/"+weblog.getEditorTheme()+"/"+filePath;
    }
    
    /**
     * TODO 3.0: eliminate the need for themeName
     */
    public String themeResource(String themeName, String filePath) {
        return getSite()+RollerRuntimeConfig.getProperty("users.themes.path")+"/"+themeName+"/"+filePath;
    }
    
    
    public String getRsd() {
        return weblog.getURL()+"/rsd";
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
            return weblog.getURL()+"/feed/entries/rss";
        }
        
        public String rss(String catPath, boolean excerpts) {
            String url = getRss();
            if(catPath != null) {
                String cat = catPath;
                try {
                    cat = URLEncoder.encode(catPath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error encoding url", ex);
                }
                url += "?cat="+cat;
                
                if(excerpts) {
                    url += "&excerpts=true";
                }
            } else if(excerpts) {
                url += "?excerpts=true";
            }
            return url;
        }
        
        public String getAtom() {
            return weblog.getURL()+"/feed/entries/atom";
        }
        
        public String atom(String catPath, boolean excerpts) {
            String url = getAtom();
            if(catPath != null) {
                String cat = catPath;
                try {
                    cat = URLEncoder.encode(catPath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error encoding url", ex);
                }
                url += "?cat="+cat;
                
                if(excerpts) {
                    url += "&excerpts=true";
                }
            } else if(excerpts) {
                url += "?excerpts=true";
            }
            return url;
        }
    }
    
    public class CommentFeedURLS {
        
        public String getRss() {
            return weblog.getURL()+"/feed/comments/rss";
        }
        
        public String rss(String catPath, boolean excerpts) {
            String url = getRss();
            if(catPath != null) {
                String cat = catPath;
                try {
                    cat = URLEncoder.encode(catPath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error encoding url", ex);
                }
                url += "?cat="+cat;
                
                if(excerpts) {
                    url += "&excerpts=true";
                }
            } else if(excerpts) {
                url += "?excerpts=true";
            }
            return url;
        }
        
        public String getAtom() {
            return weblog.getURL()+"/feed/comments/atom";
        }
        
        public String atom(String catPath, boolean excerpts) {
            String url = getAtom();
            if(catPath != null) {
                String cat = catPath;
                try {
                    cat = URLEncoder.encode(catPath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error encoding url", ex);
                }
                url += "?cat="+cat;
                
                if(excerpts) {
                    url += "&excerpts=true";
                }
            } else if(excerpts) {
                url += "?excerpts=true";
            }
            return url;
        }
        
    }
    
}
