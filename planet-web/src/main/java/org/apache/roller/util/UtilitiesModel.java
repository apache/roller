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

package org.apache.roller.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import org.apache.roller.planet.PlanetException;
//import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
//import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
//import org.apache.roller.ui.core.RollerSession;
//import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.planet.util.Utilities;

/**
 * Model which provides access to a set of general utilities.
 */
public class UtilitiesModel { // implements Model {
    
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
    
    private TimeZone tz = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "utils";
    }
    
    
//    /** Init page model based on request */
//    public void init(Map initData) throws PlanetException {
//        
//        // extract request object
//        this.request = (HttpServletRequest) initData.get("request");        
//
//        // extract timezone if available
//        WeblogRequest weblogRequest = (WeblogRequest)initData.get("weblogRequest");
//        if (weblogRequest != null && weblogRequest.getWeblog() != null) {
//            tz = weblogRequest.getWeblog().getTimeZoneInstance();
//        }
//    }
//     
//    
//    //---------------------------------------------------- Authentication utils 
//    
//    public boolean isUserAuthorizedToAuthor(WebsiteDataWrapper weblog) {
//        try {
//            RollerSession rses = RollerSession.getRollerSession(request);
//            if (rses != null && rses.getAuthenticatedUser() != null) {
//                return rses.isUserAuthorizedToAuthor(weblog.getPojo());
//            }
//        } catch (Exception e) {
//            log.warn("ERROR: checking user authorization", e);
//        }
//        return false;
//    }
//    
//    public boolean isUserAuthorizedToAdmin(WebsiteDataWrapper weblog) {
//        try {
//            RollerSession rses = RollerSession.getRollerSession(request);
//            if (rses != null && rses.getAuthenticatedUser() != null) {
//                return rses.isUserAuthorizedToAdmin(weblog.getPojo());
//            }
//        } catch (Exception e) {
//            log.warn("ERROR: checking user authorization", e);
//        }
//        return false;
//    }
//    
//    public boolean isUserAuthenticated() {
//        return (request.getUserPrincipal() != null);
//    }
        
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
        if(d == null || fmt == null)
            return fmt;
        
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(d);
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public static String formatDate(Date d, String fmt, TimeZone tzOverride) {
        if(d == null || fmt == null)
            return fmt;
        
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        format.setTimeZone(tzOverride);
        return format.format(d);
    }
    
    /**
     * Format date in ISO-8601 format.
     */
    public static String formatIso8601Date(Date d) {
        return DateUtil.formatIso8601(d);
    }
    
    /**
     * Format date in ISO-8601 format.
     */
    public static String formatIso8601Day(Date d) {
        return DateUtil.formatIso8601Day(d);
    }
    
    /**
     * Return a date in RFC-822 format.
     */
    public static String formatRfc822Date(Date date) {
        return DateUtil.formatRfc822(date);
    }
    
    /**
     * Return a date in RFC-822 format.
     */
    public static String format8charsDate(Date date) {
        return DateUtil.format8chars(date);
    }

    //------------------------------------------------------------ String utils
    
    public static boolean isEmpty(String str) {
        if (str == null) return true;
        return "".equals(str.trim());
    }
    
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    public static String[] split(String str1, String str2) {
        return StringUtils.split(str1, str2);
    }
    
    
    public static boolean equals(String str1, String str2) {
        return StringUtils.equals(str1, str2);
    }
    
    public static boolean isAlphanumeric(String str) {
        return StringUtils.isAlphanumeric(str);
    }
    
    public static String[] stripAll(String[] strs) {
        return StringUtils.stripAll(strs);
    }
    
    public static String left(String str, int length) {
        return StringUtils.left(str, length);
    }
    
    public static String escapeHTML(String str) {
        return StringEscapeUtils.escapeHtml(str);
    }
    
    public static String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml(str);
    }
    
    public static String escapeXML(String str) {
        return StringEscapeUtils.escapeXml(str);
    }
    
    public static String unescapeXML(String str) {
        return StringEscapeUtils.unescapeXml(str);
    }
    
    public static String replace(String src, String target, String rWith) {
        return StringUtils.replace(src, target, rWith);
    }
    
    public static String replace(String src, String target, String rWith, int maxCount) {
        return StringUtils.replace(src, target, rWith, maxCount);
    }
    
    private static String replace(String string, Pattern pattern, String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
    }
    
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".  Replace
     * any HTML tags with a space.
     */
    public static String removeHTML(String str) {
        return removeHTML(str, true);
    }
    
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".
     * Optionally replace HTML tags with a space.
     */
    public static String removeHTML(String str, boolean addSpace) {
        return Utilities.removeHTML(str, addSpace);
    }
        
    /**
     * Autoformat.
     */
    public static String autoformat(String s) {
        String ret = StringUtils.replace(s, "\n", "<br />");
        return ret;
    }
    /**
     * Strips HTML and truncates.
     */
    public static String truncate(
            String str, int lower, int upper, String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);
        
        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }
        
        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if(str2.length() > upper) {
            // the magic location int
            int loc;
            
            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);
            
            // now we'll see if the location is greater than the lower limit
            if(loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
                loc = upper;
            }
            
            // the string was truncated, so we append the appendToEnd String
            str2 = str2 + appendToEnd;
        }
        
        return str2;
    }
    
    public static String truncateNicely(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateNicely(str, lower, upper, appendToEnd);
    }
    
    public static String truncateText(String str, int lower, int upper, String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);
        boolean diff = (str2.length() < str.length());
        
        // quickly adjust the upper if it is set lower than 'lower'
        if(upper < lower) {
            upper = lower;
        }
        
        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if(str2.length() > upper) {
            // the magic location int
            int loc;
            
            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);
            
            // now we'll see if the location is greater than the lower limit
            if(loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
                loc = upper;
            }
            // the string was truncated, so we append the appendToEnd String
            str = str2 + appendToEnd;
        }
        return str;
    }    
    
    public static String hexEncode(String str) {
        if (StringUtils.isEmpty(str)) return str;
        
        return RegexUtil.encode(str);
    }
    
    public static String encodeEmail(String str) {
        return str!=null ? RegexUtil.encodeEmail(str) : null;
    }
    
    /**
     * URL encoding.
     * @param s a string to be URL-encoded
     * @return URL encoding of s using character encoding UTF-8; null if s is null.
     */
    public static final String encode(String s) {
        try {
            if (s != null)
                return URLEncoder.encode(s, "UTF-8");
            else
                return s;
        } catch (UnsupportedEncodingException e) {
            // Java Spec requires UTF-8 be in all Java environments, so this should not happen
            return s;
        }
    }
    
    /**
     * URL decoding.
     * @param s a URL-encoded string to be URL-decoded
     * @return URL decoded value of s using character encoding UTF-8; null if s is null.
     */
    public static final String decode(String s) {
        try {
            if (s != null)
                return URLDecoder.decode(s, "UTF-8");
            else
                return s;
        } catch (UnsupportedEncodingException e) {
            // Java Spec requires UTF-8 be in all Java environments, so this should not happen
            return s;
        }
    }
        
    /**
     * Code (stolen from Pebble) to add rel="nofollow" string to all links in HTML.
     */
    public static String addNofollow(String html) {
        if (html == null || html.length() == 0) {
            return html;
        }
        Matcher m = mLinkPattern.matcher(html);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String link = html.substring(start, end);
            buf.append(html.substring(0, start));
            if (link.indexOf("rel=\"nofollow\"") == -1) {
                buf.append(
                        link.substring(0, link.length() - 1) + " rel=\"nofollow\">");
            } else {
                buf.append(link);
            }
            html = html.substring(end, html.length());
            m = mLinkPattern.matcher(html);
        }
        buf.append(html);
        return buf.toString();
    }
    
    /**
     * Transforms the given String into a subset of HTML displayable on a web
     * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
     * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags).
     *
     * @param s   the String to transform
     * @return    the transformed String
     */
    public static String transformToHTMLSubset(String s) {
        
        if (s == null) {
            return null;
        }
        
        s = replace(s, OPENING_B_TAG_PATTERN, "<b>");
        s = replace(s, CLOSING_B_TAG_PATTERN, "</b>");
        s = replace(s, OPENING_I_TAG_PATTERN, "<i>");
        s = replace(s, CLOSING_I_TAG_PATTERN, "</i>");
        s = replace(s, OPENING_BLOCKQUOTE_TAG_PATTERN, "<blockquote>");
        s = replace(s, CLOSING_BLOCKQUOTE_TAG_PATTERN, "</blockquote>");
        s = replace(s, BR_TAG_PATTERN, "<br />");
        s = replace(s, OPENING_P_TAG_PATTERN, "<p>");
        s = replace(s, CLOSING_P_TAG_PATTERN, "</p>");
        s = replace(s, OPENING_PRE_TAG_PATTERN, "<pre>");
        s = replace(s, CLOSING_PRE_TAG_PATTERN, "</pre>");
        s = replace(s, OPENING_UL_TAG_PATTERN, "<ul>");
        s = replace(s, CLOSING_UL_TAG_PATTERN, "</ul>");
        s = replace(s, OPENING_OL_TAG_PATTERN, "<ol>");
        s = replace(s, CLOSING_OL_TAG_PATTERN, "</ol>");
        s = replace(s, OPENING_LI_TAG_PATTERN, "<li>");
        s = replace(s, CLOSING_LI_TAG_PATTERN, "</li>");
        s = replace(s, QUOTE_PATTERN, "\"");
        
        // HTTP links
        s = replace(s, CLOSING_A_TAG_PATTERN, "</a>");
        Matcher m = OPENING_A_TAG_PATTERN.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String link = s.substring(start, end);
            link = "<" + link.substring(4, link.length() - 4) + ">";
            s = s.substring(0, start) + link + s.substring(end, s.length());
            m = OPENING_A_TAG_PATTERN.matcher(s);
        }
        
        // escaped angle brackets
        s = s.replaceAll("&amp;lt;", "&lt;");
        s = s.replaceAll("&amp;gt;", "&gt;");
        s = s.replaceAll("&amp;#", "&#");
        
        return s;
    }
    
    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    public static String toBase64(byte[] aValue) {
        
        final String m_strBase64Chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        
        int byte1;
        int byte2;
        int byte3;
        int iByteLen = aValue.length;
        StringBuffer tt = new StringBuffer();
        
        for (int i = 0; i < iByteLen; i += 3) {
            boolean bByte2 = (i + 1) < iByteLen;
            boolean bByte3 = (i + 2) < iByteLen;
            byte1 = aValue[i] & 0xFF;
            byte2 = (bByte2) ? (aValue[i + 1] & 0xFF) : 0;
            byte3 = (bByte3) ? (aValue[i + 2] & 0xFF) : 0;
            
            tt.append(m_strBase64Chars.charAt(byte1 / 4));
            tt.append(m_strBase64Chars.charAt((byte2 / 16) + ((byte1 & 0x3) * 16)));
            tt.append(((bByte2) ? m_strBase64Chars.charAt((byte3 / 64) + ((byte2 & 0xF) * 4)) : '='));
            tt.append(((bByte3) ? m_strBase64Chars.charAt(byte3 & 0x3F) : '='));
        }
        
        return tt.toString();
    }
       
}
