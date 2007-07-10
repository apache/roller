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

package org.apache.roller.weblogger.ui.rendering.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.ParsedRequest;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Model which provides access to a set of general utilities.
 */
public class UtilitiesModel implements Model {
    
    private static Log log = LogFactory.getLog(UtilitiesModel.class); 
    
    private static Pattern mLinkPattern =
            Pattern.compile("<a href=.*?>", Pattern.CASE_INSENSITIVE);    
    private static final Pattern OPENING_B_TAG_PATTERN = 
            Pattern.compile("&lt;b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_B_TAG_PATTERN = 
            Pattern.compile("&lt;/b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_I_TAG_PATTERN = 
            Pattern.compile("&lt;i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_I_TAG_PATTERN = 
            Pattern.compile("&lt;/i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_BLOCKQUOTE_TAG_PATTERN = 
            Pattern.compile("&lt;blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_BLOCKQUOTE_TAG_PATTERN = 
            Pattern.compile("&lt;/blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern BR_TAG_PATTERN = 
            Pattern.compile("&lt;br */*&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_P_TAG_PATTERN = 
            Pattern.compile("&lt;p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_P_TAG_PATTERN = 
            Pattern.compile("&lt;/p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_PRE_TAG_PATTERN = 
            Pattern.compile("&lt;pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_PRE_TAG_PATTERN = 
            Pattern.compile("&lt;/pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_UL_TAG_PATTERN = 
            Pattern.compile("&lt;ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_UL_TAG_PATTERN = 
            Pattern.compile("&lt;/ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_OL_TAG_PATTERN = 
            Pattern.compile("&lt;ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_OL_TAG_PATTERN = 
            Pattern.compile("&lt;/ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_LI_TAG_PATTERN = 
            Pattern.compile("&lt;li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_LI_TAG_PATTERN = 
            Pattern.compile("&lt;/li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_A_TAG_PATTERN = 
            Pattern.compile("&lt;/a&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_A_TAG_PATTERN = 
            Pattern.compile("&lt;a href=.*?&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTE_PATTERN = 
            Pattern.compile("&quot;", Pattern.CASE_INSENSITIVE);
    
    private ParsedRequest parsedRequest = null;
    private Weblog weblog = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "utils";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws WebloggerException {      
        
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
                return weblog.getPojo().hasUserPermissions(
                        parsedRequest.getUser(), WeblogPermission.AUTHOR);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }
    
    public boolean isUserAuthorizedToAdmin(WeblogWrapper weblog) {
        try {
            if (parsedRequest.getAuthenticUser() != null) {
                return weblog.getPojo().hasUserPermissions(
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
        
        if(d == null || fmt == null)
            return fmt;
        
        SimpleDateFormat format = new SimpleDateFormat(fmt, weblog.getLocaleInstance());
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
    
    public String escapeHTML(String str) {
        return StringEscapeUtils.escapeHtml(str);
    }
    
    public String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml(str);
    }
    
    public String escapeXML(String str) {
        return StringEscapeUtils.escapeXml(str);
    }
    
    public String unescapeXML(String str) {
        return StringEscapeUtils.unescapeXml(str);
    }
    
    public String escapeJavaScript(String str) {
        return StringEscapeUtils.escapeJavaScript(str);
    }
    
    public String unescapeJavaScript(String str) {
        return StringEscapeUtils.unescapeJavaScript(str);
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
        if (StringUtils.isEmpty(str)) return str;
        
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
    
    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    public String toBase64(byte[] aValue) {
        return Utilities.toBase64(aValue);
    }
       
}
