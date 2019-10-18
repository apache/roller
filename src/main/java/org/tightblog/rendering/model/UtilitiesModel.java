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
package org.tightblog.rendering.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.rendering.requests.WeblogRequest;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.Utilities;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Map;

/**
 * Model which provides access to system messages and certain properties
 * as well as general utilities.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UtilitiesModel implements Model {

    private WebloggerPropertiesDao webloggerPropertiesDao;
    private MessageSource messages;
    private String systemVersion;

    private ZoneId zoneId;
    private Locale locale;

    @Autowired
    UtilitiesModel(WebloggerPropertiesDao webloggerPropertiesDao,
                   MessageSource messages,
                   @Value("${weblogger.version:Unknown}") String systemVersion) {
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.messages = messages;
        this.systemVersion = systemVersion;
    }

    public String getModelName() {
        return "utils";
    }

    /**
     * Init page model, will take but does not require a WeblogRequest object.
     */
    @Override
    public void init(Map<String, Object> initData) {
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        locale = weblogRequest.getWeblog().getLocaleInstance();
        zoneId = weblogRequest.getWeblog().getZoneId();
    }

    /**
     * Return message string
     */
    public String msg(String key) {
        try {
            return messages.getMessage(key, null, locale);
        } catch (NullPointerException e) {
            return "???" + key + "???";
        }
    }

    /**
     * Return parameterized message string
     */
    public String msg(String key, Object... args) {
        return messages.getMessage(key, args, locale);

    }

    public boolean getCommentEmailNotify() {
        WebloggerProperties props = webloggerPropertiesDao.findOrNull();
        return props.isUsersCommentNotifications();
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    /**
     * Format Temporal object (e.g., LocalDate, LocalTime, or LocalDateTime) using provided
     * DateTimeFormatter-supported format string.
     */
    public String formatTemporal(Temporal dt, String fmt) {
        if (dt == null || fmt == null) {
            return fmt;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt).withZone(zoneId);
        return dtf.format(dt);
    }

    /**
     * Strips HTML and truncates.
     */
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }
}
