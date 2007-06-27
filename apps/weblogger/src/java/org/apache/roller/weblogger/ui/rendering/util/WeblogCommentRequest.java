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

package org.apache.roller.weblogger.ui.rendering.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Represents a request to post a weblog entry comment.
 */
public class WeblogCommentRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogCommentRequest.class);
    
    private static final String COMMENT_SERVLET = "/roller-ui/rendering/comment";
    
    // lightweight attributes
    private String name = null;
    private String email = null;
    private String url = null;
    private String content = null;
    private boolean notify = false;
    private String weblogAnchor = null;
    
    // heavyweight attributes
    private WeblogEntry weblogEntry = null;
    
    
    public WeblogCommentRequest() {}
    
    
    public WeblogCommentRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        String servlet = request.getServletPath();
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // was this request bound for the comment servlet?
        if(servlet == null || !COMMENT_SERVLET.equals(servlet)) {
            throw new InvalidRequestException("not a weblog comment request, "+
                    request.getRequestURL());
        }
        
        
        /*
         * parse path info.  we expect ...
         *
         * /entry/<anchor> - permalink
         */
        if(pathInfo != null && pathInfo.trim().length() > 0) {
            
            // we should only ever get 2 path elements
            String[] pathElements = pathInfo.split("/");
            if(pathElements.length == 2) {
                
                String context = pathElements[0];
                if("entry".equals(context)) {
                    try {
                        this.weblogAnchor = 
                                URLDecoder.decode(pathElements[1], "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        // should never happen
                        log.error(ex);
                    }
                    
                } else {
                    throw new InvalidRequestException("bad path info, "+
                            request.getRequestURL());
                }
                
            } else {
                throw new InvalidRequestException("bad path info, "+
                        request.getRequestURL());
            }
            
        } else {
            // bad request
            throw new InvalidRequestException("bad path info, "+
                    request.getRequestURL());
        }
        
        
        /*
         * parse request parameters
         *
         * the only params we currently care about are:
         *   name - comment author
         *   email - comment email
         *   url - comment referring url
         *   content - comment contents
         *   notify - if commenter wants to receive notifications
         */
        if(request.getParameter("name") != null) {
            this.name = Utilities.removeHTML(request.getParameter("name"));
        }
        
        if(request.getParameter("email") != null) {
            this.email = Utilities.removeHTML(request.getParameter("email"));
        }
        
        if(request.getParameter("url") != null) {
            this.url = Utilities.removeHTML(request.getParameter("url"));
        }
        
        if(request.getParameter("content") != null) {
            this.content = request.getParameter("content");
        }
        
        if(request.getParameter("notify") != null) {
            this.notify = true;
        }
        
        if(log.isDebugEnabled()) {
            log.debug("name = "+this.name);
            log.debug("email = "+this.email);
            log.debug("url = "+this.url);
            log.debug("content = "+this.content);
            log.debug("notify = "+this.notify);
            log.debug("weblogAnchor = "+this.weblogAnchor);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public void setWeblogAnchor(String weblogAnchor) {
        this.weblogAnchor = weblogAnchor;
    }

    public WeblogEntry getWeblogEntry() {
        
        if(weblogEntry == null && weblogAnchor != null) {
            try {
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                weblogEntry = wmgr.getWeblogEntryByAnchor(getWeblog(), weblogAnchor);
            } catch (WebloggerException ex) {
                log.error("Error getting weblog entry "+weblogAnchor, ex);
            }
        }
        
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry weblogEntry) {
        this.weblogEntry = weblogEntry;
    }
    
}
