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

import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;
import org.apache.roller.weblogger.ui.struts2.core.GlobalConfig;
import org.apache.roller.weblogger.util.I18nMessages;

/**
 * Model which provides access to i18n application resources and
 * weblogger config properties.
 */
public class ServerModel implements Model {

    I18nMessages messages = null;

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    /** Template context name to be used for model */
    @Override
    public String getModelName() {
        return "server";
    }


    /** Init page model, requires a WeblogRequest object */
    @Override
    public void init(Map initData) {
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");

        if (weblogRequest == null) {
            throw new IllegalStateException("expected weblogRequest from init data");
        }

        // get messages util based on desired locale
        this.messages = I18nMessages.getMessages(weblogRequest.getLocaleInstance());
    }

    /** Return message string */
    public String msg(String key) {
        return messages.getString(key);
    }


    /** Return parameterized message string */
    public String msg(String key, List args) {
        return messages.getString(key, args);
    }

    public String getSiteName() {
        return propertiesManager.getStringProperty("site.name");
    }
    
    public boolean getCommentHtmlAllowed() {
        return propertiesManager.getBooleanProperty("users.comments.htmlenabled");
    }
    
    public boolean getCommentEmailNotify() {
        return propertiesManager.getBooleanProperty("users.comments.emailnotify");
    }

    public String getSystemVersion() {
        return WebloggerStaticConfig.getProperty("weblogger.version", "Unknown");
    }

}
