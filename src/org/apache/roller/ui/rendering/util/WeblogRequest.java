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

package org.apache.roller.ui.rendering.util;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WebsiteData;


/**
 * Represents a request to a weblog.
 * 
 * This is a fairly generic parsed request which is only trying to figure out
 * the elements of a weblog request which apply to all weblogs.  We try to 
 * determine the weblogHandle, if a locale was specified, and then what extra 
 * path info remains.  The basic format is like this ...
 * 
 * /<weblogHandle>[/locale][/extra/path/info]
 * 
 * All weblog urls require a weblogHandle, so we ensure that part of the url is
 * properly specified.  locale is always optional, so we do our best to see
 * if a locale is specified.  and path info is always optional.
 *
 * NOTE: this class purposely exposes a getPathInfo() method which provides the
 * path info specified by the request that has not been parsed by this
 * particular class.  this makes it relatively easy for subclasses to extend
 * this class and simply pick up where it left off in the parsing process.
 */
public class WeblogRequest extends ParsedRequest {
    
    private static Log log = LogFactory.getLog(WeblogRequest.class);
    
    // lightweight attributes
    private String weblogHandle = null;
    private String locale = null;
    private String pathInfo = null;
    
    // heavyweight attributes
    private WebsiteData weblog = null;
    private Locale localeInstance = null;
    
    
    public WeblogRequest() {}
    
    
    public WeblogRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        super(request);
        
        String path = request.getPathInfo();
        
        log.debug("parsing path "+path);
        
        // first, cleanup extra slashes and extract the weblog weblogHandle
        if(path != null && path.trim().length() > 1) {
            
            // strip off the leading slash
            path = path.substring(1);
            
            // strip off trailing slash if needed
            if(path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            
            String[] pathElements = path.split("/", 2);
            if(pathElements[0].trim().length() > 0) {
                this.weblogHandle = pathElements[0];
            } else {
                // no weblogHandle in path info
                throw new InvalidRequestException("not a weblog request, "+
                        request.getRequestURL());
            }
            
            // if there is more left of the path info then hold onto it
            if(pathElements.length == 2) {
                path = pathElements[1];
            } else {
                path = null;
            }
        }
        
        // second, check if we have a locale, everything else is extra path info
        if(path != null && path.trim().length() > 0) {
            
            String[] pathElements = path.split("/", 2);
            if(this.isLocale(pathElements[0])) {
                this.locale = pathElements[0];
                
                // everything else is path info
                if(pathElements.length == 2) {
                    this.pathInfo = pathElements[1];
                }
            } else {
                // no locale, just extra path info
                this.pathInfo = path;
            }
        }
        
        if(log.isDebugEnabled()) {
            log.debug("handle = "+this.weblogHandle);
            log.debug("locale = "+this.locale);
            log.debug("pathInfo = "+this.pathInfo);
        }
    }
    

    /**
     * Convenience method which determines if the given string is a valid
     * locale string.
     */
    private boolean isLocale(String potentialLocale) {
        
        boolean isLocale = false;
        
        // we only support 2 or 5 character locale strings, so check that first
        if(potentialLocale != null && 
                (potentialLocale.length() == 2 || potentialLocale.length() == 5)) {
            
            // now make sure that the format is proper ... e.g. "en_US"
            // we are not going to be picky about capitalization
            String[] langCountry = potentialLocale.split("_");
            if(langCountry.length == 1 && 
                    langCountry[0] != null && langCountry[0].length() == 2) {
                isLocale = true;
                
            } else if(langCountry.length == 2 && 
                    langCountry[0] != null && langCountry[0].length() == 2 && 
                    langCountry[1] != null && langCountry[1].length() == 2) {
                
                isLocale = true;
            }
        }
        
        return isLocale;
    }
    
    
    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }
    
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public WebsiteData getWeblog() {
        
        if(weblog == null && weblogHandle != null) {
            try {
                UserManager umgr = RollerFactory.getRoller().getUserManager();
                weblog = umgr.getWebsiteByHandle(weblogHandle, Boolean.TRUE);
            } catch (RollerException ex) {
                log.error("Error looking up weblog "+weblogHandle, ex);
            }
        }
        
        return weblog;
    }

    public void setWeblog(WebsiteData weblog) {
        this.weblog = weblog;
    }
    
    
    /**
     * Get the Locale instance to be used for this request.
     *
     * The Locale is determined via these rules ...
     *   1. if a locale is explicitly specified, then it is used
     *   2. if no locale is specified, then use the weblog default locale
     */
    public Locale getLocaleInstance() {
        
        if(localeInstance == null && locale != null) {
            String[] langCountry = locale.split("_");
            if(langCountry.length == 1) {
                localeInstance = new Locale(langCountry[0]);
            } else if(langCountry.length == 2) {
                localeInstance = new Locale(langCountry[0], langCountry[1]);
            }
        } else if(localeInstance == null) {
            localeInstance = getWeblog().getLocaleInstance();
        }
        
        return localeInstance;
    }

    public void setLocaleInstance(Locale localeInstance) {
        this.localeInstance = localeInstance;
    }
    
}
