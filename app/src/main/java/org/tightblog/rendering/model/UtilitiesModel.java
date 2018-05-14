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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WebloggerStaticConfig;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.requests.WeblogRequest;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.Utilities;

import java.time.LocalDateTime;
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
    private I18nMessages messages;

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

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
        zoneId = (weblogRequest == null) ? ZoneId.systemDefault() : weblogRequest.getWeblog().getZoneId();
        messages = I18nMessages.getMessages(
                (weblogRequest == null) ? Locale.ENGLISH : weblogRequest.getWeblog().getLocaleInstance());
    }

    /**
     * Return message string
     */
    public String msg(String key) {
        try {
            return messages.getString(key);
        } catch (NullPointerException e) {
            return "???" + key + "???";
        }
    }

    /**
     * Return parameterized message string
     */
    public String msg(String key, Object... args) {
        return messages.getString(key, args);
    }

    public String autoformat(String s) {
        return StringUtils.replace(s, "\n", "<br/>");
    }

    public boolean getCommentEmailNotify() {
        WebloggerProperties props = persistenceStrategy.getWebloggerProperties();
        return props.isUsersCommentNotifications();
    }

    public String getSystemVersion() {
        return WebloggerStaticConfig.getProperty("weblogger.version", "Unknown");
    }

    //-------------------------------------------------------------- Date utils

    /**
     * Return date for current time.
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * Format Temporal object (e.g., LocalDate, LocalTime, or LocalDateTime) using provided
     * DateTimeFormatter-supported format string.  Will not parse timezones.
     */
    public String formatDate(Temporal dt, String fmt) {
        if (dt == null || fmt == null) {
            return fmt;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt).withZone(zoneId);
        return dtf.format(dt);
    }

    /**
     * Format LocalDateTime or LocalTime object using provided DateTimeFormatter-supported
     * format string. This variant needed when desired to output a timezone.
     */
    public String formatDateTime(Temporal dt, String fmt) {
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

    //------------------------------------------------------------ String utils

    // isEmpty = empty (size = 0) or null
    public boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    public String left(String str, int length) {
        return StringUtils.left(str, length);
    }

    public String escapeHTML(String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }

    public String escapeXML(String str) {
        return StringEscapeUtils.escapeXml10(str);
    }

    /**
     * Strips HTML and truncates.
     */
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }

    /**
     * URL encoding.
     *
     * @param s a string to be URL-encoded
     * @return URL encoding of s using character encoding UTF-8; null if s is null.
     */
    public final String encode(String s) {
        return (s == null) ? null : Utilities.encode(s);
    }

}
