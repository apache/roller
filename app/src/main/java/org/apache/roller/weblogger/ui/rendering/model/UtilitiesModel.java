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
package org.apache.roller.weblogger.ui.rendering.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.Temporal;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Model which provides access to a set of general utilities.
 */
public class UtilitiesModel implements Model {
    private WeblogRequest weblogRequest = null;

    /** Template context name to be used for model */
    @Override
    public String getModelName() {
        return "utils";
    }

    /** Init page model, will take but does not require a WeblogRequest object. */
    @Override
    public void init(Map initData) {
        weblogRequest = (WeblogRequest) initData.get("parsedRequest");
    }

    public String autoformat(String s) {
        return StringUtils.replace(s, "\n", "<br/>");
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt);
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt).withZone(ZoneId.systemDefault());
        return dtf.format(dt);
    }
    
    /**
     * Format date as '2011-12-03T10:15:30+01:00[Europe/Paris]'
     * see: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
     */
    public String formatIsoZonedDateTime(LocalDateTime date) {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(date);
    }
    
    /**
     * Format date as '2011-12-03T10:15:30+01:00'
     * see: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
     */
    public String formatIsoOffsetDateTime(LocalDateTime date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
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

    public String right(String str, int length) {
        return StringUtils.right(str, length);
    }

    public String escapeHTML(String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }
    
    public String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml4(str);
    }

    public String escapeXML(String str) {
        return StringEscapeUtils.escapeXml10(str);
    }

    /**
     * Remove occurrences of html, defined as any text between the characters "&lt;" and "&gt;".
     */
    public String removeHTML(String str) {
        return Utilities.removeHTML(str, false);
    }

    /**
     * Truncates based on text-only but retains HTML.
     */
    public String truncateHTML(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateHTML(str, lower, upper, appendToEnd);
    }

    /**
     * Strips HTML and truncates.
     */
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }

    /**
     * URL encoding.
     * @param s a string to be URL-encoded
     * @return URL encoding of s using character encoding UTF-8; null if s is null.
     */
    public final String encode(String s) {
        return (s == null) ? null : Utilities.encode(s);
    }

    /**
     * URL decoding.
     * @param s a URL-encoded string to be URL-decoded
     * @return URL decoded value of s using character encoding UTF-8; null if s is null.
     */
    public final String decode(String s) {
        return (s == null) ? null : Utilities.decode(s);
    }
        
    /**
     * Code (stolen from Pebble) to add rel="nofollow" string to all links in HTML.
     */
    public String addNofollow(String html) {
        return Utilities.addNofollow(html);
    }
    
    /**
     * Transforms the given String into a subset of HTML displayable on a web
     * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
     * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags).
     *
     * @param s   the String to transform
     * @return    the transformed String
     */
    public String transformToHTMLSubset(String s) {
        return Utilities.transformToHTMLSubset(s);
    }
    
}
