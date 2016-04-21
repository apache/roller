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

import java.util.Map;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;

/**
 * Model which provides access to application config data like site
 * config properties.
 */
public class ConfigModel implements Model {
    
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    /** Template context name to be used for model */
    @Override
    public String getModelName() {
        return "config";
    }
    
    
    /** Init model, uses no objects */
    @Override
    public void init(Map map) {
        // no-op
    }

    public String getSiteName() {
        return propertiesManager.getStringProperty("site.name");
    }
    
    public String getSiteEmail() {
        return propertiesManager.getStringProperty("site.adminemail");
    }

    public boolean getRegistrationEnabled() {
        return propertiesManager.getBooleanProperty("users.registration.enabled");
    }
    
    public String getRegistrationURL() {
        return propertiesManager.getStringProperty("users.registration.url");
    }

    public boolean getFeedStyle() {
        return propertiesManager.getBooleanProperty("site.newsfeeds.styledFeeds");
    }

    public boolean getCommentHtmlAllowed() {
        return propertiesManager.getBooleanProperty("users.comments.htmlenabled");
    }
    
    public boolean getCommentEmailNotify() {
        return propertiesManager.getBooleanProperty("users.comments.emailnotify");
    }

    public boolean getTrackbacksEnabled() {
        return propertiesManager.getBooleanProperty("users.trackbacks.enabled");
    }
    
    public String getSystemVersion() {
        return WebloggerStaticConfig.getProperty("weblogger.version", "Unknown");
    }

    public String getDefaultAnalyticsTrackingCode() {
        return propertiesManager.getStringProperty("analytics.default.tracking.code");
    }

    public boolean getAnalyticsOverrideAllowed() {
        return propertiesManager.getBooleanProperty("analytics.code.override.allowed");
    }

}
