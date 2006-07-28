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

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerContext;
import org.apache.struts.util.RequestUtils;


/**
 * Model which provides access to application config data like site
 * config properties.
 */
public class ConfigModel implements Model {
    
    private static Log log = LogFactory.getLog(ConfigModel.class);
    
    
    /** Hand-picked list of runtime properties to be made available */
    private static List allowedProperties =
            Arrays.asList(new String[] {
        "users.registration.enabled",
        "users.registration.url",
        "users.comments.enabled",
        "users.trackbacks.enabled",
        "site.linkbacks.enabled",
        "site.frontpage.weblog.handle",
        "site.frontpage.weblog.aggregated"
    });
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "config";
    }
    
    
    /** Init model */
    public void init(Map map) throws RollerException {
        // no-op
    }
    
    
    public String getSiteName() {
        return getProperty("site.name");
    }
    
    public String getSiteShortName() {
        return getProperty("site.shortName");
    }
    
    public String getSiteDescription() {
        return getProperty("site.description");
    }
    
    public String getSiteEmail() {
        return getProperty("site.adminemail");
    }
    
    public int getFeedSize() {
        return getIntProperty("site.newsfeeds.defaultEntries");
    }
    
    public int getFeedMaxSize() {
        return getIntProperty("site.newsfeeds.maxEntries");
    }
    
    public boolean getCommentAutoFormat() {
        return getBooleanProperty("users.comments.autoformat");
    }
    
    public boolean getCommentEscapeHtml() {
        return getBooleanProperty("users.comments.escapehtml");
    }
    
    public boolean getCommentEmailNotify() {
        return getBooleanProperty("users.comments.emailnotify");
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
    
    
    private String getProperty(String name) {
        return RollerRuntimeConfig.getProperty(name);
    }
    
    
    private int getIntProperty(String name) {
        return RollerRuntimeConfig.getIntProperty(name);
    }
    
    
    private boolean getBooleanProperty(String name) {
        return RollerRuntimeConfig.getBooleanProperty(name);
    }
    
}

