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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.util.RegexUtil;
import org.apache.roller.planet.util.Utilities;


/**
 * Utility methods needed by old Roller 2.X macros/templates.
 * Deprecated because they are either redundant or unnecesary.
 */
public class OldUtilities {    
    
    /** The <code>Log</code> instance for this class. */
    private static Log mLogger = LogFactory.getLog(OldUtilities.class);
    
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
    
    public static String replace(String src, String target, String rWith) {
        return StringUtils.replace(src, target, rWith);
    }
    
    public static String replace(String src, String target, String rWith, int maxCount) {
        return StringUtils.replace(src, target, rWith, maxCount);
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
     * Return date for current time.
     */
    public static Date getNow() {
        return new Date();
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public static String formatDate(Date d, String fmt) {
        SimpleDateFormat format = new SimpleDateFormat(fmt);
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
    
    private static String replace(String string, Pattern pattern, String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
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
    
    
    //------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     * @param escapeAmpersand Optionally escape
     * ampersands (&amp;).
     */
    public static String escapeHTML(String s, boolean escapeAmpersand) {
        return Utilities.escapeHTML(s, escapeAmpersand);
    }
                
    //------------------------------------------------------------------------
    /**
     * Replace occurrences of str1 in string str with str2
     */
    public static String stringReplace(String str, String str1, String str2) {
        String ret = StringUtils.replace(str,str1,str2);
        return ret;
    }
    
    //------------------------------------------------------------------------
    /**
     * Replace occurrences of str1 in string str with str2
     * @param str String to operate on
     * @param str1 String to be replaced
     * @param str2 String to be used as replacement
     * @param maxCount Number of times to replace, 0 for all
     */
    public static String stringReplace(
            String str,
            String str1,
            String str2,
            int maxCount) {
        String ret = StringUtils.replace(str,str1,str2,maxCount);
        return ret;
    }
        

    
    /**
     * Encode a string using Base64 encoding. Used when storing passwords
     * as cookies.
     *
     * This is weak encoding in that anyone can use the decodeString
     * routine to reverse the encoding.
     *
     * @param str
     * @return String
     * @throws IOException
     */
    public static String encodeString(String str) throws IOException {
        String encodedStr = new String(Base64.encodeBase64(str.getBytes()));  
        return (encodedStr.trim());
    }
    
    /**
     * Decode a string using Base64 encoding.
     *
     * @param str
     * @return String
     * @throws IOException
     */
    public static String decodeString(String str) throws IOException {
        String value = new String(Base64.decodeBase64(str.getBytes()));        
        return (value); 
    }
               
    /**
     * @param str
     * @return
     */
    private static String stripLineBreaks(String str) {
        // TODO: use a string buffer, ignore case !
        str = str.replaceAll("<br>", "");
        str = str.replaceAll("<br/>", "");
        str = str.replaceAll("<br />", "");
        str = str.replaceAll("<p></p>", "");
        str = str.replaceAll("<p/>","");
        str = str.replaceAll("<p />","");
        return str;
    }
    
    /**
     * Need need to get rid of any user-visible HTML tags once all text has been
     * removed such as &lt;BR&gt;. This sounds like a better approach than removing
     * all HTML tags and taking the chance to leave some tags un-closed.
     *
     * WARNING: this method has serious performance problems a
     *
     * @author Alexis Moussine-Pouchkine (alexis.moussine-pouchkine@france.sun.com)
     * @author Lance Lavandowska
     * @param str the String object to modify
     * @return the new String object without the HTML "visible" tags
     */
    private static String removeVisibleHTMLTags(String str) {
        str = stripLineBreaks(str);
        StringBuffer result = new StringBuffer(str);
        StringBuffer lcresult = new StringBuffer(str.toLowerCase());
        
        // <img should take care of smileys
        String[] visibleTags = {"<img"}; // are there others to add?
        int stringIndex;
        for ( int j = 0 ;  j < visibleTags.length ; j++ ) {
            while ( (stringIndex = lcresult.indexOf(visibleTags[j])) != -1 ) {
                if ( visibleTags[j].endsWith(">") )  {
                    result.delete(stringIndex, stringIndex+visibleTags[j].length() );
                    lcresult.delete(stringIndex, stringIndex+visibleTags[j].length() );
                } else {
                    // need to delete everything up until next closing '>', for <img for instance
                    int endIndex = result.indexOf(">", stringIndex);
                    if (endIndex > -1) {
                        // only delete it if we find the end!  If we don't the HTML may be messed up, but we
                        // can't safely delete anything.
                        result.delete(stringIndex, endIndex + 1 );
                        lcresult.delete(stringIndex, endIndex + 1 );
                    }
                }
            }
        }
        
        // TODO:  This code is buggy by nature.  It doesn't deal with nesting of tags properly.
        // remove certain elements with open & close tags
        String[] openCloseTags = {"li", "a", "div", "h1", "h2", "h3", "h4"}; // more ?
        for (int j = 0; j < openCloseTags.length; j++) {
            // could this be better done with a regular expression?
            String closeTag = "</"+openCloseTags[j]+">";
            int lastStringIndex = 0;
            while ( (stringIndex = lcresult.indexOf( "<"+openCloseTags[j], lastStringIndex)) > -1) {
                lastStringIndex = stringIndex;
                // Try to find the matching closing tag  (ignores possible nesting!)
                int endIndex = lcresult.indexOf(closeTag, stringIndex);
                if (endIndex > -1) {
                    // If we found it delete it.
                    result.delete(stringIndex, endIndex+closeTag.length());
                    lcresult.delete(stringIndex, endIndex+closeTag.length());
                } else {
                    // Try to see if it is a self-closed empty content tag, i.e. closed with />.
                    endIndex = lcresult.indexOf(">", stringIndex);
                    int nextStart = lcresult.indexOf("<", stringIndex+1);
                    if (endIndex > stringIndex && lcresult.charAt(endIndex-1) == '/' && (endIndex < nextStart || nextStart == -1)) {
                        // Looks like it, so remove it.
                        result.delete(stringIndex, endIndex + 1);
                        lcresult.delete(stringIndex, endIndex + 1);
                        
                    }
                }
            }
        }
        
        return result.toString();
    }
    
    
    /**
     * Converts a character to HTML or XML entity.
     *
     * @param ch The character to convert.
     * @param xml Convert the character to XML if set to true.
     * @author Erik C. Thauvin
     *
     * @return The converted string.
     */
    public static final String charToHTML(char ch, boolean xml) {
        int c;
        
        // Convert left bracket
        if (ch == '<') {
            return ("&lt;");
        }
        
        // Convert left bracket
        else if (ch == '>') {
            return ("&gt;");
        }
        
        // Convert ampersand
        else if (ch == '&') {
            return ("&amp;");
        }
        
        // Commented out to eliminate redundant numeric character codes (ROL-507)
        // High-ASCII character
        //else if (ch >= 128)
        //{
        //c = ch;
        //return ("&#" + c + ';');
        //}
        
        // Convert double quote
        else if (xml && (ch == '"')) {
            return ("&quot;");
        }
        
        // Convert single quote
        else if (xml && (ch == '\'')) {
            return ("&#39;");
        }
        
        // No conversion
        else {
            // Return character as string
            return (String.valueOf(ch));
        }
    }
    
    /**
     * Converts a text string to HTML or XML entities.
     *
     * @author Erik C. Thauvin
     * @param text The string to convert.
     * @param xml Convert the string to XML if set to true.
     *
     * @return The converted string.
     */
    public static final String textToHTML(String text, boolean xml) {
        if (text == null) return "null";
        final StringBuffer html = new StringBuffer();
        
        // Loop thru each characters of the text
        for (int i = 0; i < text.length(); i++) {
            // Convert character to HTML/XML
            html.append(charToHTML(text.charAt(i), xml));
        }
        
        // Return HTML/XML string
        return html.toString();
    }
    
    /**
     * Converts a text string to HTML or XML entities.
     *
     * @param text The string to convert.
     * @author Erik C. Thauvin
     * @return The converted string.
     */
    public static final String textToHTML(String text) {
        return textToHTML(text, false);
    }
    
    /**
     * Converts a text string to XML entities.
     *
     * @param text The string to convert.
     * @author Erik C. Thauvin
     * @return The converted string.
     */
    public static final String textToXML(String text) {
        return textToHTML(text, true);
    }
    
    /**
     * Converts a text string to HTML or XML entities.
     * @param text The string to convert.
     * @return The converted string.
     */
    public static final String textToCDATA(String text) {
        if (text == null) return "null";
        final StringBuffer html = new StringBuffer();
        
        // Loop thru each characters of the text
        for (int i = 0; i < text.length(); i++) {
            // Convert character to HTML/XML
            html.append(charToCDATA(text.charAt(i)));
        }
        
        // Return HTML/XML string
        return html.toString();
    }
    
    /**
     * Converts a character to CDATA character.
     * @param ch The character to convert.
     * @return The converted string.
     */
    public static final String charToCDATA(char ch) {
        int c;
        
        if (ch >= 128) {
            c = ch;
            
            return ("&#" + c + ';');
        }
        
        // No conversion
        else {
            // Return character as string
            return (String.valueOf(ch));
        }
    }
    
}
