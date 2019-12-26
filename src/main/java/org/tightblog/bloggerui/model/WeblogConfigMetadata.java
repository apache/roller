/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.bloggerui.model;

import org.tightblog.domain.SharedTheme;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeblogConfigMetadata {
    private Map<String, SharedTheme> sharedThemeMap;
    private Map<String, String> editFormats;
    private Map<String, String> locales;
    private Map<String, String> timezones;
    private Map<String, String> commentOptions;
    private Map<String, String> spamOptions;
    private Map<String, String> commentDayOptions;

    private String absoluteSiteURL;
    private boolean usersOverrideAnalyticsCode;
    private boolean usersCommentNotifications;

    public Map<String, String> getEditFormats() {
        if (editFormats == null) {
            editFormats = new LinkedHashMap<>();
        }
        return editFormats;
    }

    public Map<String, String> getLocales() {
        if (locales == null) {
            locales = new LinkedHashMap<>();
        }
        return locales;
    }

    public Map<String, String> getTimezones() {
        if (timezones == null) {
            timezones = new LinkedHashMap<>();
        }
        return timezones;
    }

    public Map<String, String> getCommentOptions() {
        if (commentOptions == null) {
            commentOptions = new LinkedHashMap<>();
        }
        return commentOptions;
    }

    public Map<String, String> getSpamOptions() {
        if (spamOptions == null) {
            spamOptions = new LinkedHashMap<>();
        }
        return spamOptions;
    }

    public Map<String, String> getCommentDayOptions() {
        if (commentDayOptions == null) {
            commentDayOptions = new LinkedHashMap<>();
        }
        return commentDayOptions;
    }

    public Map<String, SharedTheme> getSharedThemeMap() {
        if (sharedThemeMap == null) {
            sharedThemeMap = new LinkedHashMap<>();
        }
        return sharedThemeMap;
    }

    public String getAbsoluteSiteURL() {
        return absoluteSiteURL;
    }

    public void setAbsoluteSiteURL(String absoluteSiteURL) {
        this.absoluteSiteURL = absoluteSiteURL;
    }

    public boolean isUsersOverrideAnalyticsCode() {
        return usersOverrideAnalyticsCode;
    }

    public boolean isUsersCommentNotifications() {
        return usersCommentNotifications;
    }

    public void setUsersOverrideAnalyticsCode(boolean usersOverrideAnalyticsCode) {
        this.usersOverrideAnalyticsCode = usersOverrideAnalyticsCode;
    }

    public void setUsersCommentNotifications(boolean usersCommentNotifications) {
        this.usersCommentNotifications = usersCommentNotifications;
    }
}
