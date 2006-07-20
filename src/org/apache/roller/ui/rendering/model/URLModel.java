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
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WebsiteData;


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
    
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws RollerException {
        
        // need a weblog to base the weblog specific urls off of
        weblog = (WebsiteData) initData.get("weblog");
        if(weblog == null) {
            throw new RollerException("Expected 'weblog' init param!");
        }
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
    
    
    public String file(String filePath) {
        return weblog.getURL()+"/resource/"+filePath;
    }
    
    
    public String getRsd() {
        return weblog.getURL()+"/rsd";
    }
    
    
    public FeedURLS getFeed() {
        return new FeedURLS();
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
