/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.pojos.wrapper.Wrappers;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;
import org.apache.roller.weblogger.ui.rendering.util.ParsedRequest;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Model which provides access to a set of general utilities.
 */
public class UtilitiesModel implements Model {
    
    private static final Log log = LogFactory.getLog(UtilitiesModel.class);

    /** Most invalid date patterns remembered for logging; templates can hold any number. */
    static final int MAX_INVALID_DATE_PATTERNS = 100;

    /**
     * Invalid date patterns already logged, so a busy page logs each one once.
     * Least recently seen patterns are dropped past the limit, so templates can't
     * grow it without bound.
     */
    static final Set<String> INVALID_DATE_PATTERNS = Collections.newSetFromMap(
            Collections.synchronizedMap(new LinkedHashMap<String, Boolean>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > MAX_INVALID_DATE_PATTERNS;
                }
            }));

    private ParsedRequest parsedRequest = null;
    private Weblog weblog = null;
    
    
    /** Template context name to be used for model */
    @Override
    public String getModelName() {
        return "utils";
    }
    
    
    /** Init page model based on request */
    @Override
    public void init(Map<String, Object> initData) throws WebloggerException {      
        
        // we expect the init data to contain a parsedRequest object
        parsedRequest = (ParsedRequest) initData.get("parsedRequest");
        if(parsedRequest == null) {
            throw new WebloggerException("expected parsedRequest from init data");
        }
        
        // extract weblog object if possible
        if(parsedRequest instanceof WeblogRequest) {
            WeblogRequest weblogRequest = (WeblogRequest) parsedRequest;
            weblog = weblogRequest.getWeblog();
        }
    }
     
    
    //---------------------------------------------------- Authentication utils 
    
    public boolean isUserAuthorizedToAuthor(WeblogWrapper weblog) {
        try {
            if (parsedRequest.getAuthenticUser() != null) {
                return Wrappers.unwrap(weblog).hasUserPermission(
                        parsedRequest.getUser(), WeblogPermission.POST);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }
    
    public boolean isUserAuthorizedToAdmin(WeblogWrapper weblog) {
        try {
            if (parsedRequest.getAuthenticUser() != null) {
                return Wrappers.unwrap(weblog).hasUserPermission(
                        parsedRequest.getUser(), WeblogPermission.ADMIN);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }
        
    public boolean isUserAuthenticated() {
        return (parsedRequest.getAuthenticUser() != null);
    }
       
    public UserWrapper getAuthenticatedUser() {
        return parsedRequest.getAuthenticUser() != null 
                ? UserWrapper.wrap(parsedRequest.getUser()) : null;
    }
             
    //-------------------------------------------------------------- Date utils
    /**
     * Return date for current time.
     */
    public static Date getNow() {
        return new Date();
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public String formatDate(Date d, String fmt) {
        return formatDate(d, fmt, weblog.getTimeZoneInstance());
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public String formatDate(Date d, String fmt, TimeZone tzOverride) {
        
        if (d == null || fmt == null) {
            return fmt;
        }
        
        DateFormat format;
        try {
            format = new SimpleDateFormat(fmt, weblog.getLocaleInstance());
        } catch (IllegalArgumentException e) {
            // Usually an older theme passing a message key that no longer exists,
            // which $text.get returns as is. Format the date rather than fail the page.
            if (INVALID_DATE_PATTERNS.add(fmt)) {
                log.warn("Invalid date pattern '" + fmt + "' in a template of weblog '"
                        + weblog.getHandle() + "'; using the default format instead: " + e.getMessage());
            }
            format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT,
                    weblog.getLocaleInstance());
        }
        if(tzOverride != null) {
            format.setTimeZone(tzOverride);
        }
        
        return format.format(d);
    }
    
    /**
     * Format date in ISO-8601 format.
     */
    public String formatIso8601Date(Date d) {
        return DateUtil.formatIso8601(d);
    }
    
    /**
     * Format date in ISO-8601 format.
     */
    public String formatIso8601Day(Date d) {
        return DateUtil.formatIso8601Day(d);
    }
    
    /**
     * Return a date in RFC-822 format.
     */
    public String formatRfc822Date(Date date) {
        return DateUtil.formatRfc822(date);
    }
    
    /**
     * Return a date in 8 character format YYYYMMDD.
     */
    public String format8charsDate(Date date) {
        return DateUtil.format8chars(date);
    }

    
    //------------------------------------------------------------ String utils
    
    // isEmpty = empty (size = 0) or null
    public boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }
    
    public boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }
    
    public String[] split(String str1, String str2) {
        return StringUtils.split(str1, str2);
    }
    
    public boolean equals(String str1, String str2) {
        return StringUtils.equals(str1, str2);
    }
    
    public boolean isAlphanumeric(String str) {
        return StringUtils.isAlphanumeric(str);
    }
    
    public String[] stripAll(String[] strs) {
        return StringUtils.stripAll(strs);
    }
    
    public String left(String str, int length) {
        return StringUtils.left(str, length);
    }

    /**
     * Upper-case a value using a locale-independent mapping for templates
     * that validate protocol or identifier keys.
     */
    public String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase(Locale.ROOT);
    }
    
    public String escapeHTML(String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }
    
    public String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml4(str);
    }
    
    public String escapeXML(String str) {
        return StringEscapeUtils.escapeXml11(str);
    }
    
    public String unescapeXML(String str) {
        return StringEscapeUtils.unescapeXml(str);
    }
    
    public String escapeJavaScript(String str) {
        return StringEscapeUtils.escapeEcmaScript(str);
    }
    
    public String unescapeJavaScript(String str) {
        return StringEscapeUtils.unescapeEcmaScript(str);
    }
    
    public String replace(String src, String target, String rWith) {
        return StringUtils.replace(src, target, rWith);
    }
    
    public String replace(String src, String target, String rWith, int maxCount) {
        return StringUtils.replace(src, target, rWith, maxCount);
    }
    
    private String replace(String string, Pattern pattern, String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
    }
    
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".  Replace
     * any HTML tags with a space.
     */
    public String removeHTML(String str) {
        return removeHTML(str, true);
    }
    
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".
     * Optionally replace HTML tags with a space.
     */
    public String removeHTML(String str, boolean addSpace) {
        return Utilities.removeHTML(str, addSpace);
    }
        
    /**
     * Autoformat.
     */
    public String autoformat(String s) {
        return Utilities.autoformat(s);
    }
    
    /**
     * Strips HTML and truncates.
     */
    public String truncate(String str, int lower, int upper, String appendToEnd) {
        // this method is a dupe of truncateText() method
        return truncateText(str, lower, upper, appendToEnd);
    }
    
    public String truncateNicely(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateNicely(str, lower, upper, appendToEnd);
    }
    
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }    
    
    public String hexEncode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        
        return RegexUtil.encode(str);
    }
    
    public String encodeEmail(String str) {
        return str!=null ? RegexUtil.encodeEmail(str) : null;
    }
    
    /**
     * URL encoding.
     * @param s a string to be URL-encoded
     * @return URL encoding of s using character encoding UTF-8; null if s is null.
     */
    public final String encode(String s) {
        if(s != null) {
            return URLUtilities.encode(s);
        } else {
            return s;
        }
    }
    
    /**
     * URL decoding.
     * @param s a URL-encoded string to be URL-decoded
     * @return URL decoded value of s using character encoding UTF-8; null if s is null.
     */
    public final String decode(String s) {
        if(s != null) {
            return URLUtilities.decode(s);
        } else {
            return s;
        }
    }
        
    /**
     * Adds nofollow to the rel attribute of every link in the HTML.
     *
     * @param html the HTML to change
     * @return the HTML as parsed and written out again by jsoup
     */
    public String addNofollow(String html) {
        return Utilities.addNofollow(html);
    }
    
    /**
     * Turns HTML-encoded text, such as the output of escapeHTML, into the
     * subset of HTML that comments may use: the encoded tags &lt;b&gt;,
     * &lt;i&gt;, &lt;blockquote&gt;, &lt;br&gt;, &lt;p&gt;, &lt;pre&gt;,
     * &lt;ul&gt;, &lt;ol&gt;, &lt;li&gt; and links to http, https and mailto
     * addresses become tags again. Every other tag stays text.
     *
     * @param s   the HTML-encoded text
     * @return    the HTML subset, or null if s is null
     */
    public String transformToHTMLSubset(String s) {
        return Utilities.transformToHTMLSubset(s);
    }
    
    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    public String toBase64(byte[] aValue) {
        return Utilities.toBase64(aValue);
    }
       
}
