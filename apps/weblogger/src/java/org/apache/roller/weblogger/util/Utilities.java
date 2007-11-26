package org.apache.roller.weblogger.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.roller.util.RegexUtil;

        
/**
 * General purpose utilities, not for use in templates.
 */
public class Utilities {
    /** The <code>Log</code> instance for this class. */
    private static Log mLogger = LogFactory.getLog(Utilities.class);
    
    public final static String TAG_SPLIT_CHARS = " ,\n\r\f\t";
      
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
    
    
    //------------------------------------------------------------------------
    /** Strip jsessionid off of a URL */
    public static String stripJsessionId( String url ) {
        // Strip off jsessionid found in referer URL
        int startPos = url.indexOf(";jsessionid=");
        if ( startPos != -1 ) {
            int endPos = url.indexOf("?",startPos);
            if ( endPos == -1 ) {
                url = url.substring(0,startPos);
            } else {
                url = url.substring(0,startPos)
                + url.substring(endPos,url.length());
            }
        }
        return url;
    }
    
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
        // got to do amp's first so we don't double escape
        if (escapeAmpersand) {
            s = StringUtils.replace(s, "&", "&amp;");
        }
        s = StringUtils.replace(s, "&nbsp;", " ");
        s = StringUtils.replace(s, "\"", "&quot;");
        s = StringUtils.replace(s, "<", "&lt;");
        s = StringUtils.replace(s, ">", "&gt;");
        return s;
    }
     
    public static String unescapeHTML(String str) {
        return StringEscapeUtils.unescapeHtml(str);
    }
    
    //------------------------------------------------------------------------
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
     *
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace) {
        if (str == null) return "";
        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;
        
        while (beginTag >= start) {
            if (beginTag > 0) {
                ret.append(str.substring(start, beginTag));
                
                // replace each tag with a space (looks better)
                if (addSpace) ret.append(" ");
            }
            endTag = str.indexOf(">", beginTag);
            
            // if endTag found move "cursor" forward
            if (endTag > -1) {
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, get rest of str and break
            else {
                ret.append(str.substring(beginTag));
                break;
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length()) {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }
    
    //------------------------------------------------------------------------
    /** Run both removeHTML and escapeHTML on a string.
     * @param s String to be run through removeHTML and escapeHTML.
     * @return String with HTML removed and HTML special characters escaped.
     */
    public static String removeAndEscapeHTML( String s ) {
        if ( s==null ) return "";
        else return Utilities.escapeHTML( Utilities.removeHTML(s) );
    }
    
    //------------------------------------------------------------------------
    /**
     * Autoformat.
     */
    public static String autoformat(String s) {
        String ret = StringUtils.replace(s, "\n", "<br />");
        return ret;
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
    
    
    //------------------------------------------------------------------------
    /**
     * Replaces occurences of non-alphanumeric characters with an underscore.
     */
    public static String replaceNonAlphanumeric(String str) {
        return replaceNonAlphanumeric(str, '_');
    }
    
    //------------------------------------------------------------------------
    /**
     * Replaces occurences of non-alphanumeric characters with a
     * supplied char.
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuffer ret = new StringBuffer(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            if (Character.isLetterOrDigit(testChars[i])) {
                ret.append(testChars[i]);
            } else {
                ret.append( subst );
            }
        }
        return ret.toString();
    }
    
    //------------------------------------------------------------------------
    /**
     * Remove occurences of non-alphanumeric characters.
     */
    public static String removeNonAlphanumeric(String str) {
        StringBuffer ret = new StringBuffer(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            // MR: Allow periods in page links
            if (Character.isLetterOrDigit(testChars[i]) ||
                    testChars[i] == '.') {
                ret.append(testChars[i]);
            }
        }
        return ret.toString();
    }
    
    //------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringArrayToString(String[] stringArray, String delim) {
        String ret = "";
        for (int i = 0; i < stringArray.length; i++) {
            if (ret.length() > 0)
                ret = ret + delim + stringArray[i];
            else
                ret = stringArray[i];
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringListToString(List<String> stringList, String delim) {
        String ret = "";
        for (String s : stringList) {
            if (ret.length() > 0)
                ret = ret + delim + s;
            else
                ret = s;
        }
        return ret;
    }
    
    //--------------------------------------------------------------------------
    /** Convert string with delimeters to string array. */
    public static String[] stringToStringArray(String instr, String delim)
    throws NoSuchElementException, NumberFormatException {
        StringTokenizer toker = new StringTokenizer(instr, delim);
        String stringArray[] = new String[toker.countTokens()];
        int i = 0;
        
        while (toker.hasMoreTokens()) {
            stringArray[i++] = toker.nextToken();
        }
        return stringArray;
    }
    
    //--------------------------------------------------------------------------
    /** Convert string with delimeters to string list. */
    public static List<String> stringToStringList(String instr, String delim)
    throws NoSuchElementException, NumberFormatException {
        StringTokenizer toker = new StringTokenizer(instr, delim);
        List<String> stringList = new ArrayList<String>();
        while (toker.hasMoreTokens()) {
            stringList.add(toker.nextToken());
        }
        return stringList;
    }
    
    //--------------------------------------------------------------------------
    /** Convert string to integer array. */
    public static int[] stringToIntArray(String instr, String delim)
    throws NoSuchElementException, NumberFormatException {
        StringTokenizer toker = new StringTokenizer(instr, delim);
        int intArray[] = new int[toker.countTokens()];
        int i = 0;
        
        while (toker.hasMoreTokens()) {
            String sInt = toker.nextToken();
            int nInt = Integer.parseInt(sInt);
            intArray[i++] = new Integer(nInt).intValue();
        }
        return intArray;
    }
    
    //-------------------------------------------------------------------
    /** Convert integer array to a string. */
    public static String intArrayToString(int[] intArray) {
        String ret = "";
        for (int i = 0; i < intArray.length; i++) {
            if (ret.length() > 0)
                ret = ret + "," + Integer.toString(intArray[i]);
            else
                ret = Integer.toString(intArray[i]);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    public static void copyFile(File from, File to) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        
        try {
            in = new FileInputStream(from);
        } catch (IOException ex) {
            throw new IOException(
                    "Utilities.copyFile: opening input stream '"
                    + from.getPath()
                    + "', "
                    + ex.getMessage());
        }
        
        try {
            out = new FileOutputStream(to);
        } catch (Exception ex) {
            try {
                in.close();
            } catch (IOException ex1) {
            }
            throw new IOException(
                    "Utilities.copyFile: opening output stream '"
                    + to.getPath()
                    + "', "
                    + ex.getMessage());
        }
        
        copyInputToOutput(in, out, from.length());
    }
    
    //------------------------------------------------------------------------
    /**
     * Utility method to copy an input stream to an output stream.
     * Wraps both streams in buffers. Ensures right numbers of bytes copied.
     */
    public static void copyInputToOutput(
            InputStream input,
            OutputStream output,
            long byteCount)
            throws IOException {
        int bytes;
        long length;
        
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        
        byte[] buffer;
        buffer = new byte[8192];
        
        for (length = byteCount; length > 0;) {
            bytes = (int) (length > 8192 ? 8192 : length);
            
            try {
                bytes = in.read(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException(
                        "Reading input stream, " + ex.getMessage());
            }
            
            if (bytes < 0)
                break;
            
            length -= bytes;
            
            try {
                out.write(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException(
                        "Writing output stream, " + ex.getMessage());
            }
        }
        
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }
    
    //------------------------------------------------------------------------
    public static void copyInputToOutput(
            InputStream input,
            OutputStream output)
            throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte buffer[] = new byte[8192];
        for (int count = 0; count != -1;) {
            count = in.read(buffer, 0, 8192);
            if (count != -1)
                out.write(buffer, 0, count);
        }
        
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }
    
    /**
     * Encode a string using algorithm specified in web.xml and return the
     * resulting encrypted password. If exception, the plain credentials
     * string is returned
     *
     * @param password Password or other credentials to use in authenticating
     *        this username
     * @param algorithm Algorithm used to do the digest
     *
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();
        
        MessageDigest md = null;
        
        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            mLogger.error("Exception: " + e);
            return password;
        }
        
        md.reset();
        
        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);
        
        // now calculate the hash
        byte[] encodedPassword = md.digest();
        
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            
            buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }
        
        return buf.toString();
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
        Base64 base64 = new Base64();
        String encodedStr = new String(base64.encodeBase64(str.getBytes()));        
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
        Base64 base64 = new Base64();
        String value = new String(base64.decodeBase64(str.getBytes()));        
        return (value);
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
    
    /**
     * This method based on code from the String taglib at Apache Jakarta:
     * http://cvs.apache.org/viewcvs/jakarta-taglibs/string/src/org/apache/taglibs/string/util/StringW.java?rev=1.16&content-type=text/vnd.viewcvs-markup
     * Copyright (c) 1999 The Apache Software Foundation.
     * Author: timster@mac.com
     *
     * @param str
     * @param lower
     * @param upper
     * @param appendToEnd
     * @return
     */
    public static String truncateNicely(String str, int lower, int upper, String appendToEnd) {
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
            
            // HTML was removed from original str
            if (diff) {
                
                // location of last space in truncated string
                loc = str2.lastIndexOf(' ', loc);
                
                // get last "word" in truncated string (add 1 to loc to eliminate space
                String str3 = str2.substring(loc+1);
                
                // find this fragment in original str, from 'loc' position
                loc = str.indexOf(str3, loc) + str3.length();
                
                // get truncated string from original str, given new 'loc'
                str2 = str.substring(0, loc);
                
                // get all the HTML from original str after loc
                str3 = extractHTML(str.substring(loc));
                
                // remove any tags which generate visible HTML
                // This call is unecessary, all HTML has already been stripped
                //str3 = removeVisibleHTMLTags(str3);
                
                // append the appendToEnd String and
                // add extracted HTML back onto truncated string
                str = str2 + appendToEnd + str3;
            } else {
                // the string was truncated, so we append the appendToEnd String
                str = str2 + appendToEnd;
            }
            
        }
        
        return str;
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
     * Extract (keep) JUST the HTML from the String.
     * @param str
     * @return
     */
    public static String extractHTML(String str) {
        if (str == null) return "";
        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;
        
        while (beginTag >= start) {
            endTag = str.indexOf(">", beginTag);
            
            // if endTag found, keep tag
            if (endTag > -1) {
                ret.append( str.substring(beginTag, endTag+1) );
                
                // move start forward and find another tag
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, break
            else {
                break;
            }
        }
        return ret.toString();
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
     * @param string
     * @return
     */
    public static int stringToInt(String string) {
        try {
            return Integer.valueOf(string).intValue();
        } catch (NumberFormatException e) {
            mLogger.debug("Invalid Integer:" + string);
        }
        return 0;
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
    
    /**
     * @param tag
     * @return
     */
    public static String stripInvalidTagCharacters(String tag) {
        if (tag == null)
            throw new NullPointerException();

        StringBuffer sb = new StringBuffer();
        char[] charArray = tag.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];

            // fast-path exclusions quotes and commas are obvious
            switch (c) {
            case 34: // "
            case 44: // ,
                continue;
            }

            if ((33 <= c && c <= 126) || Character.isUnicodeIdentifierPart(c)
                    || Character.isUnicodeIdentifierStart(c)) {
                sb.append(charArray[i]);
            }
        }
        return sb.toString();
    }
        
    public static String normalizeTag(String tag, Locale locale) {
        tag = Utilities.stripInvalidTagCharacters(tag);
        return locale == null ? tag.toLowerCase() : tag.toLowerCase(locale);        
    }
    
    /**
     * @param tags
     * @return
     */
    public static List splitStringAsTags(String tags)  {
        String[] tagsarr = StringUtils.split(tags, TAG_SPLIT_CHARS);
        if(tagsarr == null)
            return Collections.EMPTY_LIST;
        return Arrays.asList(tagsarr);
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
    
}
