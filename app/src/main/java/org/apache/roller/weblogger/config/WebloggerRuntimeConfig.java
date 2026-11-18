/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefsParser;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;


/**
 * This class acts as a convenience gateway for getting property values
 * via the PropertiesManager.  We do this because most calls to the
 * PropertiesManager are just to get the value of a specific property and
 * thus the caller doesn't need the full RuntimeConfigProperty object.
 * 
 * We also provide some methods for converting to different data types.
 */
public final class WebloggerRuntimeConfig {
    
    private static Log log = LogFactory.getLog(WebloggerRuntimeConfig.class);
    
    private static String RUNTIME_CONFIG = "/org/apache/roller/weblogger/config/runtimeConfigDefs.xml";
    private static RuntimeConfigDefs configDefs = null;
    
    // special case for our context urls
    private static String relativeContextURL = null;

    // absolute url derived from the request being handled on this thread, used
    // only while site.absoluteurl is unset and never kept past that request
    private static final ThreadLocal<String> requestAbsoluteContextURL =
            new ThreadLocal<>();
    private static final AtomicBoolean absoluteURLWarningLogged =
            new AtomicBoolean(false);
    
    
    // prevent instantiations
    private WebloggerRuntimeConfig() {}
    
    
    /**
     * Retrieve a single property from the PropertiesManager ... returns null
     * if there is an error
     **/
    public static String getProperty(String name) {
        
        String value = null;
        
        try {
            PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty prop = pmgr.getProperty(name);
            if(prop != null) {
                value = prop.getValue();
            }
        } catch(Exception e) {
            log.warn("Trouble accessing property: "+name, e);
        }
        
        log.debug("fetched property ["+name+"="+value+"]");

        return value;
    }
    
    
    /**
     * Retrieve a property as a boolean ... defaults to false if there is an error
     **/
    public static boolean getBooleanProperty(String name) {
        
        // get the value first, then convert
        String value = WebloggerRuntimeConfig.getProperty(name);
        
        if (value == null) {
            return false;
        }

        return Boolean.valueOf(value);
    }
    
    
    /**
     * Retrieve a property as an int ... defaults to -1 if there is an error
     **/
    public static int getIntProperty(String name) {
        
        // get the value first, then convert
        String value = WebloggerRuntimeConfig.getProperty(name);
        
        if (value == null) {
            return -1;
        }
        
        int intval = -1;
        try {
            intval = Integer.parseInt(value);
        } catch(Exception e) {
            log.warn("Trouble converting to int: "+name, e);
        }
        
        return intval;
    }
    
    
    public static RuntimeConfigDefs getRuntimeConfigDefs() {
        
        if(configDefs == null) {
            
            // unmarshall the config defs file
            try {
                InputStream is = 
                        WebloggerRuntimeConfig.class.getResourceAsStream(RUNTIME_CONFIG);
                
                RuntimeConfigDefsParser parser = new RuntimeConfigDefsParser();
                configDefs = parser.unmarshall(is);
                
            } catch(Exception e) {
                // error while parsing :(
                log.error("Error parsing runtime config defs", e);
            }
            
        }
        
        return configDefs;
    }
    
    
    /**
     * Get the runtime configuration definitions XML file as a string.
     *
     * This is basically a convenience method for accessing this file.
     * The file itself contains meta-data about what configuration
     * properties we change at runtime via the UI and how to setup
     * the display for editing those properties.
     */
    public static String getRuntimeConfigDefsAsString() {
        
        log.debug("Trying to load runtime config defs file");
        
        try {
            StringWriter configString = new StringWriter();
            try (InputStreamReader reader = new InputStreamReader(WebloggerConfig.class.getResourceAsStream(RUNTIME_CONFIG))) {
                reader.transferTo(configString);
            }
            return configString.toString();
        } catch(Exception e) {
            log.error("Error loading runtime config defs file", e);
        }
        
        return "";
    }
    
    
    /**
     * Set the absolute url of the request being handled on this thread. The
     * InitFilter sets this at the start of each request and clears it with
     * clearRequestAbsoluteContextURL() when the request is done.
     *
     * This value is *not* persisted or shared with other threads.
     */
    public static void setRequestAbsoluteContextURL(String url) {
        requestAbsoluteContextURL.set(url);
    }


    /**
     * Forget the absolute url of the request handled on this thread.
     */
    public static void clearRequestAbsoluteContextURL() {
        requestAbsoluteContextURL.remove();
    }


    /**
     * True if getAbsoluteContextURL() has returned the current request's url
     * on this thread. Output built from that url depends on the request's Host
     * header, so the rendering caches don't store it.
     */
    /**
     * The cache key to use for content rendered for the current request. While
     * site.absoluteurl is unset, links are built from the request's own URL, so
     * the key includes it: content cached for one host name is only served to
     * requests for that host name.
     */
    public static String cacheKey(String key) {
        String absURL = getProperty("site.absoluteurl");
        if (absURL != null && !absURL.isBlank()) {
            return key;
        }
        String requestURL = requestAbsoluteContextURL.get();
        return requestURL == null ? key : key + "|" + requestURL;
    }

    /**
     * Saves site.absoluteurl from the current request when it is unset and the
     * request comes from a global administrator, which is the case the first
     * time an administrator opens the Roller UI after an upgrade.
     */
    public static void saveAbsoluteContextURLForAdmin(User user) {
        String absURL = getProperty("site.absoluteurl");
        if ((absURL != null && !absURL.isBlank()) || user == null
                || !user.hasGlobalPermission("admin")) {
            return;
        }
        try {
            if (saveAbsoluteContextURLIfUnset(requestAbsoluteContextURL.get())) {
                WebloggerFactory.getWeblogger().flush();
            }
        } catch (WebloggerException e) {
            log.warn("Could not save site.absoluteurl", e);
        }
    }


    /**
     * Get the absolute url to this site.
     *
     * This method will just return the value of the "site.absoluteurl"
     * property if it is set. Otherwise it returns the url of the request
     * being handled on this thread, or null when there is no such request
     * (for example in background tasks).
     */
    public static String getAbsoluteContextURL() {
        
        // db prop takes priority if it exists
        String absURL = getProperty("site.absoluteurl");
        if(absURL != null && !absURL.isBlank()) {
            return absURL;
        }

        if (absoluteURLWarningLogged.compareAndSet(false, true)) {
            log.warn("The site's absolute URL (site.absoluteurl) is not set, so "
                    + "links in emails and feeds will use the host name of each "
                    + "request. Set \"Absolute URL to site\" under Server "
                    + "Administration > Configuration.");
        }

        return requestAbsoluteContextURL.get();
    }


    /**
     * Save the given url as site.absoluteurl if that property is empty.
     * Called when the site is being set up, by the installer and by the
     * registration of the first administrator.
     *
     * @return true if the url was saved; the caller must flush
     */
    public static boolean saveAbsoluteContextURLIfUnset(String url)
            throws WebloggerException {

        if (url == null || url.isBlank()) {
            return false;
        }
        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty prop = pmgr.getProperty("site.absoluteurl");
        if (prop != null && prop.getValue() != null && !prop.getValue().isBlank()) {
            return false;
        }
        if (prop == null) {
            prop = new RuntimeConfigProperty("site.absoluteurl", url);
        } else {
            prop.setValue(url);
        }
        pmgr.saveProperty(prop);
        log.info("Set site.absoluteurl to " + url);
        return true;
    }
    
    
    /**
     * Special method which sets the non-persisted relative url to this site.
     *
     * This property is *not* persisted in any way.
     */
    public static void setRelativeContextURL(String url) {
        relativeContextURL = url;
    }
    
    
    public static String getRelativeContextURL() {
        return relativeContextURL;
    }
    
    
    /**
     * Convenience method for Roller classes trying to determine if a given
     * weblog handle represents the front page blog.
     */
    public static boolean isFrontPageWeblog(String weblogHandle) {
        
        String frontPageHandle = getProperty("site.frontpage.weblog.handle");
        
        return (frontPageHandle.equals(weblogHandle));
    }
    
    
    /**
     * Convenience method for Roller classes trying to determine if a given
     * weblog handle represents the front page blog configured to render
     * site-wide data.
     */
    public static boolean isSiteWideWeblog(String weblogHandle) {
        
        boolean siteWide = getBooleanProperty("site.frontpage.weblog.aggregated");
        
        return (isFrontPageWeblog(weblogHandle) && siteWide);
    }
    
}
