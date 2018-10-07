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
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.requests.WeblogRequest;
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
    private ZoneId zoneId;

    private Locale locale;

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    @Autowired
    private MessageSource messages;

    @Value("${weblogger.version:Unknown}")
    private String systemVersion;

    /**
     * Template context name to be used for model
     */
    @Override
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
        WebloggerProperties props = persistenceStrategy.getWebloggerProperties();
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
     * Provides required Atom date format e.g. '2011-12-03T10:15:30+01:00'
     * see https://tools.ietf.org/html/rfc4287#section-3.3
     * see: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
     */
    public String formatIsoOffsetDateTime(Temporal dt) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId).format(dt);
    }

    /**
     * Strips HTML and truncates.
     */
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }
}
