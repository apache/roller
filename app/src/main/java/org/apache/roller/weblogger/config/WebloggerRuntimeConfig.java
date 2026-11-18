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

package org.apache.roller.weblogger.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefsParser;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.rendering.util.cache.PlanetCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogFeedCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;


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
    private static final ThreadLocal<RequestURLs> requestURLs =
            new ThreadLocal<>();
    private static final AtomicBoolean absoluteURLWarningLogged =
            new AtomicBoolean(false);
    private static final AtomicBoolean localURLWarningLogged =
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
     * The urls seen by the request being handled on this thread. The value of
     * site.absoluteurl is read once per request, the first time it's needed,
     * so that all links built for a request and the cache key its content is
     * stored under agree, even if an administrator sets the property while
     * the request is being handled.
     */
    private static final class RequestURLs {
        private final String requestURL;
        private String siteURL;
        private boolean siteURLRead;

        private RequestURLs(String requestURL) {
            this.requestURL = requestURL;
        }
    }


    /**
     * Set the absolute url of the request being handled on this thread. The
     * InitFilter sets this at the start of each request and clears it with
     * clearRequestAbsoluteContextURL() when the request is done.
     *
     * This value is *not* persisted or shared with other threads.
     */
    public static void setRequestAbsoluteContextURL(String url) {
        requestURLs.set(new RequestURLs(url));
    }


    /**
     * Forget the urls of the request handled on this thread.
     */
    public static void clearRequestAbsoluteContextURL() {
        requestURLs.remove();
    }


    /**
     * The value of site.absoluteurl, or null if it is unset. Within a
     * request this is the value the property had when the request first
     * needed it.
     */
    private static String configuredAbsoluteContextURL() {
        RequestURLs request = requestURLs.get();
        if (request == null) {
            return liveAbsoluteContextURL();
        }
        if (!request.siteURLRead) {
            request.siteURL = liveAbsoluteContextURL();
            request.siteURLRead = true;
        }
        return request.siteURL;
    }


    private static String liveAbsoluteContextURL() {
        String absURL = getProperty("site.absoluteurl");
        return absURL == null || absURL.isBlank() ? null : absURL;
    }


    /**
     * True if site.absoluteurl is set, so links can be built without a
     * request. Background tasks check this before building links.
     */
    public static boolean isAbsoluteContextURLSet() {
        return configuredAbsoluteContextURL() != null;
    }


    /**
     * The cache key to use for content rendered for the current request, or
     * null if the content must not be cached. While site.absoluteurl is
     * unset, links are built from the request's own URL, so the key includes
     * it: content cached for one host name is only served to requests for
     * that host name. Content rendered outside a request while the property
     * is unset is not cached, because its links are incomplete.
     */
    public static String cacheKey(String key) {
        if (key == null || configuredAbsoluteContextURL() != null) {
            return key;
        }
        RequestURLs request = requestURLs.get();
        if (request == null || request.requestURL == null) {
            return null;
        }
        return key + "|" + request.requestURL;
    }

    /**
     * Saves site.absoluteurl from the current request when it is unset and the
     * request comes from a global administrator, which is the case the first
     * time an administrator opens the Roller UI after an upgrade.
     */
    public static void saveAbsoluteContextURLForAdmin(User user) {
        if (liveAbsoluteContextURL() != null || user == null
                || !user.hasGlobalPermission("admin")) {
            return;
        }
        RequestURLs request = requestURLs.get();
        try {
            if (request != null && saveAbsoluteContextURLIfUnset(request.requestURL)) {
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
        String absURL = configuredAbsoluteContextURL();
        if (absURL != null) {
            return absURL;
        }

        if (absoluteURLWarningLogged.compareAndSet(false, true)) {
            log.warn("The site's absolute URL (site.absoluteurl) is not set, so "
                    + "links in emails and feeds will use the host name of each "
                    + "request. Set \"Absolute URL to site\" under Server "
                    + "Administration > Configuration.");
        }

        RequestURLs request = requestURLs.get();
        return request == null ? null : request.requestURL;
    }


    /**
     * Save the given url as site.absoluteurl if that property is empty.
     * Called when the site is being set up, by the installer and by the
     * registration of the first administrator.
     *
     * The property is only replaced if it is still empty when it is written,
     * so of several requests trying at once, only one saves its url. The
     * rendering caches are cleared when the url is saved, since they hold
     * content cached per host name while the property was unset.
     *
     * @return true if the url was saved; the caller must flush
     */
    public static boolean saveAbsoluteContextURLIfUnset(String url)
            throws WebloggerException {

        if (url == null || url.isBlank()) {
            return false;
        }
        if (!isPublicSiteURL(url)) {
            if (localURLWarningLogged.compareAndSet(false, true)) {
                log.warn("Not saving " + url + " as the site's absolute URL "
                        + "(site.absoluteurl) because it isn't a public host name. "
                        + "Set \"Absolute URL to site\" under Server "
                        + "Administration > Configuration.");
            }
            return false;
        }
        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty prop = pmgr.getProperty("site.absoluteurl");
        String current = prop == null ? null : prop.getValue();
        if (current != null && !current.isBlank()) {
            return false;
        }
        if (prop == null) {
            // the properties table is filled in at startup, so this only
            // happens if that failed; a second insert would fail on flush
            pmgr.saveProperty(new RuntimeConfigProperty("site.absoluteurl", url));
        } else if (!pmgr.compareAndSetProperty("site.absoluteurl", current, url)) {
            return false;
        }
        clearRenderingCaches();
        log.info("Set site.absoluteurl to " + url);
        return true;
    }
    
    
    /**
     * True if the url's host looks like a public name for the site, and so
     * may be saved automatically. Loopback and private addresses, and host
     * names without a dot (such as "localhost" or the name of a proxy's
     * backend), are left for an administrator to set.
     */
    static boolean isPublicSiteURL(String url) {
        String host;
        try {
            host = new URI(url).getHost();
        } catch (URISyntaxException e) {
            return false;
        }
        if (host == null) {
            return false;
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        if (InetAddressValidator.getInstance().isValid(host)) {
            try {
                // an address literal, so this doesn't look anything up
                InetAddress address = InetAddress.getByName(host);
                byte[] bytes = address.getAddress();
                return !(address.isLoopbackAddress() || address.isAnyLocalAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || (address instanceof Inet6Address && (bytes[0] & 0xfe) == 0xfc));
            } catch (UnknownHostException e) {
                return false;
            }
        }
        return host.indexOf('.') > 0 && !host.endsWith(".")
                && !host.endsWith(".localhost") && !host.endsWith(".local");
    }


    /**
     * Clear the caches of rendered content, whose links may have been built
     * from the url of a request.
     */
    private static void clearRenderingCaches() {
        WeblogPageCache.getInstance().clear();
        WeblogFeedCache.getInstance().clear();
        SiteWideCache.getInstance().clear();
        PlanetCache.getInstance().clear();
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
