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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.util.Utilities;

/**
 * This class is for reading the application's runtime properties.  At installation
 * time, these properties are initially loaded from the runtimeConfigDefs.xml into the
 * properties database table, from where they are subsequently read during runtime.
 * In contrast to the static properties in WebloggerConfig, these values may be changed
 * during runtime on the system administration page and take effect immediately.
 *
 * This class acts as a convenience gateway for getting property values
 * via the PropertiesManager.  We do this because most calls to the
 * PropertiesManager are just to get the value of a specific property and
 * thus the caller doesn't need the full RuntimeConfigProperty object.
 * 
 * We also provide some methods for converting to different data types.
 */
public final class WebloggerRuntimeConfig {
    
    private static Log log = LogFactory.getLog(WebloggerRuntimeConfig.class);
    
    private static RuntimeConfigDefs configDefs = null;
    
    // special case for our context urls
    private static String relativeContextURL = null;
    private static String absoluteContextURL = null;
    
    
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
            try {
                configDefs = (RuntimeConfigDefs) Utilities.jaxbUnmarshall(
                        "/org/apache/roller/weblogger/config/runtimeConfigDefs.xsd",
                        "/org/apache/roller/weblogger/config/runtimeConfigDefs.xml",
                        false,
                        RuntimeConfigDefs.class);
            } catch(Exception e) {
                // error while parsing :(
                log.error("Error parsing runtime config defs", e);
            }
        }
        return configDefs;
    }

    /**
     * Special method which sets the non-persisted absolute url to this site.
     *
     * This property is *not* persisted in any way.
     */
    public static void setAbsoluteContextURL(String url) {
        absoluteContextURL = url;
    }
    
    
    /**
     * Get the absolute url to this site.
     *
     * This method will just return the value of the "site.absoluteurl"
     * property if it is set, otherwise it will return the non-persisted
     * value which is set by the InitFilter.
     */
    public static String getAbsoluteContextURL() {
        
        // db prop takes priority if it exists
        String absURL = getProperty("site.absoluteurl");
        if(absURL != null && absURL.trim().length() > 0) {
            return absURL;
        }
        
        return absoluteContextURL;
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
