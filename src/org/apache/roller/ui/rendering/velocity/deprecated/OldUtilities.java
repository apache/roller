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
package org.apache.roller.ui.rendering.velocity.deprecated;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.util.Utilities;


/**
 * Utility methods needed by old Roller 2.X macros/templates.
 * Deprecated because they are either redundant or unnecesary.
 */
public class OldUtilities {    
    
    /** The <code>Log</code> instance for this class. */
    private static Log mLogger = LogFactory.getLog(OldUtilities.class);
              
    //------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     * The default behaviour is to escape ampersands.
     */
    public static String escapeHTML(String s) {
        return escapeHTML(s, true);
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
     
    public static String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml(str);
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
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String encodedStr = encoder.encodeBuffer(str.getBytes());
        
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
        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
        String value = new String(dec.decodeBuffer(str));
        
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
     * @author Alexis Moussine-Pouchkine <alexis.moussine-pouchkine@france.sun.com>
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
        
    public static String hexEncode(String str) {
        if (StringUtils.isEmpty(str)) return str;
        
        return RegexUtil.encode(str);
    }
    
    public static String encodeEmail(String str) {
        return str!=null ? RegexUtil.encodeEmail(str) : null;
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
    
    private static String replace(String string, Pattern pattern, String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
    }    
}
