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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.ui.core.RollerContext;

/**
 * New Atlas config page model provides access to site URLs and runtime configs.
 */
public class ConfigPageModel implements PageModel {    
    private HttpServletRequest request = null;
    
    /** Hand-picked list of runtime properties to be made available */
    private static List allowedProperties = 
        Arrays.asList(new String[] {
            "site.name",
            "site.shortName",
            "site.description",
            "site.adminemail",
            "users.registration.enabled",
            "users.registration.url",
            "users.comments.enabled",
            "users.trackbacks.enabled",
            "users.comments.autoformat",
            "users.comments.escapehtml",
            "users.comments.emailnotify",
            "site.linkbacks.enabled",
            "site.newsfeeds.defaultEntries",
            "site.newsfeeds.maxEntries"
    }); 
    
    /** Creates a new instance of ConfigPageModel */
    public ConfigPageModel() {}

    /** Template context name to be used for model */
    public String getModelName() {
        return "config";
    }

    /** Init page model based on request */
    public void init(Map map) {
        this.request = (HttpServletRequest)map.get("request");
    }
        
    /** Absolute URL of Roller server, e.g. http://localhost:8080/roller */
    public String getAbsoluteContextURL() {
        return RollerContext.getRollerContext().getAbsoluteContextUrl(request);
    }
    
    /** 
     * Get Roller string runtime configuration property.
     * @return Property value or null if not found 
     */
    public String getConfigProperty(String name) {
        String ret = null;
        if (allowedProperties.contains(name)) {
            ret = RollerRuntimeConfig.getProperty(name);
        }
        return ret;
    }
    
    /** 
     * Get Roller integer runtime configuration property 
     * @return Property value or -999 if not found 
     */
    public int getConfigPropertyInt(String name) {
        int ret = -999;
        if (allowedProperties.contains(name)) {
            ret = RollerRuntimeConfig.getIntProperty(name);
        }
        return ret;
    }
    
    /** 
     * Get Roller boolean runtime configuration property.
     * @return Property value or false if not found 
     */
    public boolean getConfigPropertyBoolean(String name) {
        boolean ret = false;
        if (allowedProperties.contains(name)) {
            return RollerRuntimeConfig.getBooleanProperty(name);
        } 
        return ret;
    }
    
    /** Get Roller version string */
    public String getRollerVersion() {
        return RollerContext.getRollerContext().getRollerVersion();
    }
    
    /** Get timestamp of Roller build */
    public String getRollerBuildTimestamp() {
        return RollerContext.getRollerContext().getRollerBuildTime();
    }
    
    /** Get username who created Roller build */
    public String getRollerBuildUser() {
        return RollerContext.getRollerContext().getRollerBuildUser();
    }
}

