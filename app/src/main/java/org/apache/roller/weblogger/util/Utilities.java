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

package org.apache.roller.weblogger.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.activation.FileTypeMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.util.RollerConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * General purpose utilities, not for use in templates.
 */
public class Utilities {
    /** The <code>Log</code> instance for this class. */
    private static final Log mLogger = LogFactory.getLog(Utilities.class);

    public static final String TAG_SPLIT_CHARS = " ,\n\r\f\t";

    // Comment HTML that survives sanitizeToHTMLSubset: a few formatting tags,
    // and links to http, https and mailto addresses or to the same site
    private static final String HTML_SUBSET_LINK_TAG = "a";
    private static final Set<String> HTML_SUBSET_TAGS = Set.of(
            HTML_SUBSET_LINK_TAG, "b", "blockquote", "br", "code", "em", "i",
            "li", "ol", "p", "pre", "strong", "ul");
    // Common inline tags outside the subset: the tag is removed and its text
    // kept. Any other tag, such as <script> or <List>, is shown as text.
    private static final Set<String> HTML_SUBSET_DROPPED_TAGS = Set.of(
            "abbr", "acronym", "big", "cite", "del", "dfn", "font", "img",
            "ins", "kbd", "mark", "q", "s", "samp", "small", "span", "strike",
            "sub", "sup", "tt", "u", "var");
    private static final int HTML_SUBSET_TAG_MAX_LENGTH = 10;
    private static final Safelist HTML_SUBSET = new Safelist() {
                @Override
                public boolean isSafeAttribute(String tagName, Element el,
                        Attribute attr) {
                    return super.isSafeAttribute(tagName, el, attr)
                            || (HTML_SUBSET_LINK_TAG.equals(tagName)
                                && "href".equals(attr.getKey())
                                && isRelativeLink(attr.getValue()));
                }
            }
            .addTags(HTML_SUBSET_TAGS.toArray(new String[0]))
            .addAttributes(HTML_SUBSET_LINK_TAG, "href")
            .addProtocols(HTML_SUBSET_LINK_TAG, "href", "http", "https",
                    "mailto");

    // ------------------------------------------------------------------------
    /** Strip jsessionid off of a URL */
    public static String stripJsessionId(String url) {
        // Strip off jsessionid found in referer URL
        int startPos = url.indexOf(";jsessionid=");
        if (startPos != -1) {
            int endPos = url.indexOf('?', startPos);
            if (endPos == -1) {
                url = url.substring(0, startPos);
            } else {
                url = url.substring(0, startPos)
                        + url.substring(endPos, url.length());
            }
        }
        return url;
    }

    // ------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML. The default behaviour is to escape
     * ampersands.
     */
    public static String escapeHTML(String s) {
        return escapeHTML(s, true);
    }

    // ------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     * 
     * @param escapeAmpersand
     *            Optionally escape ampersands (&amp;).
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
        return StringEscapeUtils.unescapeHtml4(str);
    }

    // ------------------------------------------------------------------------
    /**
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;". Replace any HTML tags with a space.
     */
    public static String removeHTML(String str) {
        return removeHTML(str, true);
    }

    /**
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;". Optionally replace HTML tags with a space.
     * 
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace) {
        if (str == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(str.length());
        int start = 0;
        int beginTag = str.indexOf('<');
        int endTag = 0;
        if (beginTag == -1) {
            return str;
        }

        while (beginTag >= start) {
            if (beginTag > 0) {
                ret.append(str.substring(start, beginTag));
            }
            endTag = str.indexOf('>', beginTag);

            // if endTag found move "cursor" forward
            if (endTag > -1) {
                // replace each tag with a space (looks better)
                if (addSpace && beginTag > 0) {
                    ret.append(" ");
                }
                start = endTag + 1;
                beginTag = str.indexOf('<', start);
            }
            // if no endTag found, drop the rest if this '<' starts a tag,
            // which a browser would still parse as markup; otherwise keep the
            // '<' as text and look at the next one
            else {
                if (startsTag(str, beginTag)) {
                    break;
                }
                ret.append('<');
                start = beginTag + 1;
                beginTag = str.indexOf('<', start);
                if (beginTag == -1) {
                    ret.append(str.substring(start));
                    break;
                }
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length()) {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }

    /**
     * True if the unclosed '&lt;' at the given index opens a comment, a
     * declaration, an end tag, or a tag with attributes: a tag name, then
     * whitespace or '/', and an '=' later on. Text such as "x&lt;y" or
     * "a&lt;b and b&lt;c" is not a tag.
     */
    private static boolean startsTag(String str, int index) {
        int i = index + 1;
        if (i >= str.length()) {
            return false;
        }
        char next = str.charAt(i);
        if (next == '/' || next == '!' || next == '?') {
            return true;
        }
        if (!isAsciiLetter(next)) {
            return false;
        }
        while (i < str.length() && (isAsciiLetter(str.charAt(i))
                || Character.isDigit(str.charAt(i)) || str.charAt(i) == '-')) {
            i++;
        }
        if (i >= str.length()) {
            return false;
        }
        char after = str.charAt(i);
        return (Character.isWhitespace(after) || after == '/')
                && str.indexOf('=', i) > -1;
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // ------------------------------------------------------------------------
    /**
     * Run both removeHTML and escapeHTML on a string.
     * 
     * @param s
     *            String to be run through removeHTML and escapeHTML.
     * @return String with HTML removed and HTML special characters escaped.
     */
    public static String removeAndEscapeHTML(String s) {
        if (s == null) {
            return "";
        } else {
            return Utilities.escapeHTML(Utilities.removeHTML(s));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Autoformat.
     */
    public static String autoformat(String s) {
        return StringUtils.replace(s, "\n", "<br />");
    }

    /**
     * Adds nofollow to the rel attribute of every link in the HTML. The HTML
     * is parsed with jsoup, so only a link's own rel attribute is changed,
     * and the result is jsoup's serialization of the parsed fragment.
     */
    public static String addNofollow(String html) {
        if (html == null || !StringUtils.containsIgnoreCase(html, "<a")) {
            return html;
        }
        Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings().prettyPrint(false);
        for (Element link : doc.body().select("a[href]")) {
            String rel = link.attr("rel").trim();
            boolean hasNofollow = false;
            for (String token : StringUtils.split(rel)) {
                hasNofollow |= "nofollow".equalsIgnoreCase(token);
            }
            if (!hasNofollow) {
                link.attr("rel", rel.isEmpty() ? "nofollow" : rel + " nofollow");
            }
        }
        return doc.body().html();
    }

    // ------------------------------------------------------------------------
    /**
     * Replaces occurences of non-alphanumeric characters with an underscore.
     */
    public static String replaceNonAlphanumeric(String str) {
        return replaceNonAlphanumeric(str, '_');
    }

    // ------------------------------------------------------------------------
    /**
     * Replaces occurrences of non-alphanumeric characters with a supplied char.
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            if (Character.isLetterOrDigit(testChars[i])) {
                ret.append(testChars[i]);
            } else {
                ret.append(subst);
            }
        }
        return ret.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Remove occurences of non-alphanumeric characters.
     */
    public static String removeNonAlphanumeric(String str) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++) {
            // MR: Allow periods in page links
            if (Character.isLetterOrDigit(testChars[i]) || testChars[i] == '.') {
                ret.append(testChars[i]);
            }
        }
        return ret.toString();
    }

    // ------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringArrayToString(String[] stringArray, String delim) {
        StringBuilder bldr = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            if (bldr.length() > 0) {
                bldr.append(delim).append(stringArray[i]);
            } else {
                bldr.append(stringArray[i]);
            }
        }
        return bldr.toString();
    }

    // ------------------------------------------------------------------------
    /** Convert string array to string with delimeters. */
    public static String stringListToString(List<String> stringList,
            String delim) {
        StringBuilder bldr = new StringBuilder();
        for (String s : stringList) {
            if (bldr.length() > 0) {
                bldr.append(delim).append(s);
            } else {
                bldr.append(s);
            }
        }
        return bldr.toString();
    }

    // --------------------------------------------------------------------------
    /** Convert string with delimeters to string array. */
    public static String[] stringToStringArray(String instr, String delim) {
        return StringUtils.split(instr, delim);
    }

    // --------------------------------------------------------------------------
    /** Convert string with delimiters to string list.
     */
    public static List<String> stringToStringList(String instr, String delim) {
        List<String> stringList = new ArrayList<>();
        String[] str = StringUtils.split(instr, delim);
        stringList.addAll(Arrays.asList(str));
        return stringList;
    }

    // --------------------------------------------------------------------------
    /** Convert string to integer array. */
    public static int[] stringToIntArray(String instr, String delim) {
        String[] str = StringUtils.split(instr, delim);
        int intArray[] = new int[str.length];
        int i = 0;
        for (String string : str) {
            int nInt = Integer.parseInt(string);
            intArray[i++] = nInt;
        }
        return intArray;
    }

    // -------------------------------------------------------------------
    /** Convert integer array to a string. */
    public static String intArrayToString(int[] intArray) {
        StringBuilder bldr = new StringBuilder();
        for (int s : intArray) {
            if (bldr.length() > 0) {
                bldr.append(",").append(Integer.toString(s));
            } else {
                bldr.append(Integer.toString(s));
            }
        }
        return bldr.toString();
    }

    // ------------------------------------------------------------------------
    public static void copyFile(File from, File to) throws IOException {
        InputStream in;
        OutputStream out;

        try {
            in = new FileInputStream(from);
        } catch (IOException ex) {
            throw new IOException("Utilities.copyFile: opening input stream '"
                    + from.getPath() + "', " + ex.getMessage());
        }

        try {
            out = new FileOutputStream(to);
        } catch (Exception ex) {
            try {
                in.close();
            } catch (IOException ex1) {
            }
            throw new IOException("Utilities.copyFile: opening output stream '"
                    + to.getPath() + "', " + ex.getMessage());
        }

        copyInputToOutput(in, out, from.length());
    }

    /**
     * Reads an inputstream into a string
     */
    public static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Utility method to copy an input stream to an output stream. Wraps both
     * streams in buffers. Ensures right numbers of bytes copied.
     */
    public static void copyInputToOutput(InputStream input,
            OutputStream output, long byteCount) throws IOException {
        int bytes;
        long length;

        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);

        byte[] buffer;
        buffer = new byte[RollerConstants.EIGHT_KB_IN_BYTES];

        for (length = byteCount; length > 0;) {
            bytes = (int) (length > RollerConstants.EIGHT_KB_IN_BYTES ? RollerConstants.EIGHT_KB_IN_BYTES : length);

            try {
                bytes = in.read(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException("Reading input stream, "
                        + ex.getMessage());
            }

            if (bytes < 0) {
                break;
            }

            length -= bytes;

            try {
                out.write(buffer, 0, bytes);
            } catch (IOException ex) {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex1) {
                }
                throw new IOException("Writing output stream, "
                        + ex.getMessage());
            }
        }

        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    public static void copyInputToOutput(InputStream input, OutputStream output)
            throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte buffer[] = new byte[RollerConstants.EIGHT_KB_IN_BYTES];
        for (int count = 0; count != -1;) {
            count = in.read(buffer, 0, RollerConstants.EIGHT_KB_IN_BYTES);
            if (count != -1) {
                out.write(buffer, 0, count);
            }
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
     * resulting encrypted password. If exception, the plain credentials string
     * is returned
     * 
     * @param password
     *            Password or other credentials to use in authenticating this
     *            username
     * @param algorithm
     *            Algorithm used to do the digest
     * 
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md;

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

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords as
     * cookies.
     * 
     * This is weak encoding in that anyone can use the decodeString routine to
     * reverse the encoding.
     * 
     * @param str
     *            String to encode
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
     *            String to decode
     * @return decoded string
     * @throws IOException
     */
    public static String decodeString(String str) throws IOException {
        String value = new String(Base64.decodeBase64(str.getBytes()));
        return (value);
    }

    /**
     * Strips HTML and truncates.
     */
    public static String truncate(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
            }

            // the string was truncated, so we append the appendToEnd String
            str2 = str2 + appendToEnd;
        }

        return str2;
    }

    /**
     * Turns stored entry text, such as a title, into a short excerpt that is
     * safe to print as HTML text: markup is removed, entities are decoded, the
     * text is cut at a word boundary between <code>lower</code> and
     * <code>upper</code> characters (with <code>appendToEnd</code> added when
     * it is cut), and the result is HTML-encoded.
     */
    public static String plainTextExcerpt(String str, int lower, int upper,
            String appendToEnd) {
        String text = unescapeHTML(removeHTML(str, false));
        if (upper < lower) {
            upper = lower;
        }
        if (text.length() > upper) {
            int loc = text.lastIndexOf(' ', upper);
            if (loc >= lower) {
                text = text.substring(0, loc);
            } else {
                text = text.substring(0, upper);
            }
            text = text + appendToEnd;
        }
        return escapeHTML(text);
    }

    /**
     * This method based on code from the String taglib at Apache Jakarta:
     * http:/
     * /cvs.apache.org/viewcvs/jakarta-taglibs/string/src/org/apache/taglibs
     * /string/util/StringW.java?rev=1.16&content-type=text/vnd.viewcvs-markup
     * Copyright (c) 1999 The Apache Software Foundation. Author:
     * timster@mac.com
     * 
     * @param str
     * @param lower
     * @param upper
     * @param appendToEnd
     * @return
     */
    public static String truncateNicely(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);
        boolean diff = (str2.length() < str.length());

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
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

                // get last "word" in truncated string (add 1 to loc to
                // eliminate space
                String str3 = str2.substring(loc + 1);

                // find this fragment in original str, from 'loc' position
                loc = str.indexOf(str3, loc) + str3.length();

                // get truncated string from original str, given new 'loc'
                str2 = str.substring(0, loc);

                // get all the HTML from original str after loc
                str3 = extractHTML(str.substring(loc));

                // remove any tags which generate visible HTML
                // This call is unecessary, all HTML has already been stripped
                // str3 = removeVisibleHTMLTags(str3);

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

    public static String truncateText(String str, int lower, int upper,
            String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str, false);

        // quickly adjust the upper if it is set lower than 'lower'
        if (upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if (str2.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if (loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
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
        str = str.replace("<br>", "");
        str = str.replace("<br/>", "");
        str = str.replace("<br />", "");
        str = str.replace("<p></p>", "");
        str = str.replace("<p/>", "");
        str = str.replace("<p />", "");
        return str;
    }

    /**
     * Need need to get rid of any user-visible HTML tags once all text has been
     * removed such as &lt;BR&gt;. This sounds like a better approach than
     * removing all HTML tags and taking the chance to leave some tags
     * un-closed.
     * 
     * WARNING: this method has serious performance problems a
     * 
     * @author Alexis Moussine-Pouchkine
     *         (alexis.moussine-pouchkine@france.sun.com)
     * @author Lance Lavandowska
     * @param str
     *            the String object to modify
     * @return the new String object without the HTML "visible" tags
     */
    private static String removeVisibleHTMLTags(String str) {
        str = stripLineBreaks(str);
        StringBuilder result = new StringBuilder(str);
        StringBuilder lcresult = new StringBuilder(str.toLowerCase());

        // <img should take care of smileys, others to add?
        String[] visibleTags = { "<img" };
        int stringIndex;
        for (int j = 0; j < visibleTags.length; j++) {
            while ((stringIndex = lcresult.indexOf(visibleTags[j])) != -1) {
                if (visibleTags[j].endsWith(">")) {
                    result.delete(stringIndex,
                            stringIndex + visibleTags[j].length());
                    lcresult.delete(stringIndex,
                            stringIndex + visibleTags[j].length());
                } else {
                    // need to delete everything up until next closing '>', for
                    // <img for instance
                    int endIndex = result.indexOf(">", stringIndex);
                    if (endIndex > -1) {
                        // only delete it if we find the end! If we don't the
                        // HTML may be messed up, but we
                        // can't safely delete anything.
                        result.delete(stringIndex, endIndex + 1);
                        lcresult.delete(stringIndex, endIndex + 1);
                    }
                }
            }
        }

        // TODO: This code is buggy by nature. It doesn't deal with nesting of
        // tags properly.
        // remove certain elements with open & close tags, more available?
        String[] openCloseTags = { "li", "a", "div", "h1", "h2", "h3", "h4" };
        for (int j = 0; j < openCloseTags.length; j++) {
            // could this be better done with a regular expression?
            String closeTag = "</" + openCloseTags[j] + ">";
            int lastStringIndex = 0;
            while ((stringIndex = lcresult.indexOf("<" + openCloseTags[j],
                    lastStringIndex)) > -1) {
                lastStringIndex = stringIndex;
                // Try to find the matching closing tag (ignores possible
                // nesting!)
                int endIndex = lcresult.indexOf(closeTag, stringIndex);
                if (endIndex > -1) {
                    // If we found it delete it.
                    result.delete(stringIndex, endIndex + closeTag.length());
                    lcresult.delete(stringIndex, endIndex + closeTag.length());
                } else {
                    // Try to see if it is a self-closed empty content tag, i.e.
                    // closed with />.
                    endIndex = lcresult.indexOf(">", stringIndex);
                    int nextStart = lcresult.indexOf("<", stringIndex + 1);
                    if (endIndex > stringIndex
                            && lcresult.charAt(endIndex - 1) == '/'
                            && (endIndex < nextStart || nextStart == -1)) {
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
     * 
     * @param str
     * @return
     */
    public static String extractHTML(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(str.length());
        int start = 0;
        int beginTag = str.indexOf('<');
        int endTag = 0;
        if (beginTag == -1) {
            return str;
        }

        while (beginTag >= start) {
            endTag = str.indexOf('>', beginTag);

            // if endTag found, keep tag
            if (endTag > -1) {
                ret.append(str.substring(beginTag, endTag + 1));

                // move start forward and find another tag
                start = endTag + 1;
                beginTag = str.indexOf('<', start);
            }
            // if no endTag found, break
            else {
                break;
            }
        }
        return ret.toString();
    }

    public static String hexEncode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return RegexUtil.encode(str);
    }

    public static String encodeEmail(String str) {
        return str != null ? RegexUtil.encodeEmail(str) : null;
    }

    /**
     * @param string
     * @return
     */
    public static int stringToInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            mLogger.debug("Invalid Integer:" + string);
        }
        return 0;
    }

    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    public static String toBase64(byte[] aValue) {

        final String strBase64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        int byte1;
        int byte2;
        int byte3;
        int iByteLen = aValue.length;
        StringBuilder tt = new StringBuilder();

        for (int i = 0; i < iByteLen; i += 3) {
            boolean bByte2 = (i + 1) < iByteLen;
            boolean bByte3 = (i + 2) < iByteLen;
            byte1 = aValue[i] & 0xFF;
            byte2 = (bByte2) ? (aValue[i + 1] & 0xFF) : 0;
            byte3 = (bByte3) ? (aValue[i + 2] & 0xFF) : 0;

            tt.append(strBase64Chars.charAt(byte1 / 4));
            tt.append(strBase64Chars
                    .charAt((byte2 / 16) + ((byte1 & 0x3) * 16)));
            tt.append(((bByte2) ? strBase64Chars.charAt((byte3 / 64)
                    + ((byte2 & 0xF) * 4)) : '='));
            tt.append(((bByte3) ? strBase64Chars.charAt(byte3 & 0x3F) : '='));
        }

        return tt.toString();
    }

    /**
     * Removes characters that are not allowed in a tag name: whitespace,
     * control characters, comma, and the HTML-significant characters
     * <code>" &amp; ' &lt; &gt; `</code>. Templates can then print tag names
     * as they are.
     *
     * @param tag the tag name
     * @return the tag name without invalid characters
     */
    public static String stripInvalidTagCharacters(String tag) {
        if (tag == null) {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder();
        char[] charArray = tag.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];

            switch (c) {
            case '"':
            case '&':
            case '\'':
            case ',':
            case '<':
            case '>':
            case '`':
                continue;
            default:
                break;
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
     * @param tags String holding space separated list of tags
     * @return List of strings, one string for each tag
     */
    public static List<String> splitStringAsTags(String tags) {
        String[] tagsarr = StringUtils.split(tags, TAG_SPLIT_CHARS);
        if (tagsarr == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(tagsarr);
    }

    /**
     * Reduces HTML, such as a comment, to the subset of HTML a weblog page
     * shows: &lt;b&gt;, &lt;i&gt;, &lt;em&gt;, &lt;strong&gt;, &lt;code&gt;,
     * &lt;blockquote&gt;, &lt;br&gt;, &lt;p&gt;, &lt;pre&gt;, lists, and links
     * to http, https and mailto addresses or to a relative address such as
     * "/downloads". Common inline tags outside the subset, such as
     * &lt;abbr&gt;, &lt;span&gt; and &lt;img&gt;, are removed and their text
     * kept. Everything else is shown as text: other tags, tags that never
     * end, any other "&lt;", and any "&amp;" that doesn't start a character
     * reference. jsoup's Cleaner then removes every attribute but a link's
     * href and HTML-encodes the text.
     *
     * @param html the HTML to reduce
     * @return the HTML subset, or null if html is null
     */
    public static String sanitizeToHTMLSubset(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(encodeNonSubsetTags(encodeBareAmpersands(html)), "",
                HTML_SUBSET, new Document.OutputSettings().prettyPrint(false));
    }

    /**
     * Like sanitizeToHTMLSubset, for text that is already HTML-encoded, such
     * as a comment passed through escapeHTML: the encoded tags in the subset
     * become tags again, and the other encoded tags stay text.
     *
     * @param s the HTML-encoded text
     * @return the HTML subset, or null if s is null
     */
    public static String transformToHTMLSubset(String s) {
        if (s == null) {
            return null;
        }
        return sanitizeToHTMLSubset(StringEscapeUtils.unescapeHtml4(s));
    }

    /**
     * HTML-encodes every "&lt;" that doesn't start a tag of the HTML subset
     * or one of the inline tags it removes, so that an HTML parser reads it
     * as text instead of dropping it.
     */
    private static String encodeNonSubsetTags(String html) {
        int lt = html.indexOf('<');
        if (lt == -1) {
            return html;
        }
        StringBuilder sb = new StringBuilder(html.length() + 16);
        int copied = 0;
        while (lt != -1) {
            sb.append(html, copied, lt);
            int end = subsetTagEnd(html, lt);
            if (end == -1) {
                sb.append("&lt;");
                copied = lt + 1;
            } else {
                sb.append(html, lt, end);
                copied = end;
            }
            lt = html.indexOf('<', copied);
        }
        return sb.append(html, copied, html.length()).toString();
    }

    /**
     * HTML-encodes every "&amp;" that doesn't start a character reference,
     * such as "&amp;amp;", "&amp;#126;" or "&amp;#60", so that an HTML parser
     * reads text such as "?a=1&amp;copy=2" as typed instead of decoding the
     * entity names it allows without a semicolon.
     */
    private static String encodeBareAmpersands(String html) {
        int amp = html.indexOf('&');
        if (amp == -1) {
            return html;
        }
        StringBuilder sb = new StringBuilder(html.length() + 16);
        int copied = 0;
        while (amp != -1) {
            sb.append(html, copied, amp);
            sb.append(isCharacterReference(html, amp) ? "&" : "&amp;");
            copied = amp + 1;
            amp = html.indexOf('&', copied);
        }
        return sb.append(html, copied, html.length()).toString();
    }

    /**
     * Whether the "&amp;" at the given index starts a name that ends with
     * ";", or a decimal or hexadecimal number, which browsers read with or
     * without its ";". The scan stops after a few dozen characters, longer
     * than any entity name.
     */
    private static boolean isCharacterReference(String html, int amp) {
        int length = html.length();
        int i = amp + 1;
        boolean numeric = i < length && html.charAt(i) == '#';
        boolean hex = false;
        if (numeric) {
            i++;
            hex = i < length && (html.charAt(i) == 'x' || html.charAt(i) == 'X');
            if (hex) {
                i++;
            }
        }
        int start = i;
        int max = Math.min(length, start + 32);
        while (i < max) {
            char c = html.charAt(i);
            boolean digit = c >= '0' && c <= '9';
            boolean hexLetter = (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!(numeric ? digit || (hex && hexLetter) : digit || isAsciiLetter(c))) {
                break;
            }
            i++;
        }
        if (numeric) {
            return i > start;
        }
        return i > start && i < length && html.charAt(i) == ';'
                && isAsciiLetter(html.charAt(start));
    }

    /**
     * Whether a link's address is relative, such as "/downloads", "page.html",
     * "?id=1" or "#comments", and so stays on the same site. A scheme before
     * the first "/", "?" or "#" makes it absolute, and an address that starts
     * with "//" names another host. Whitespace, control characters and "\"
     * are refused, since browsers drop or reinterpret them before reading the
     * scheme.
     */
    private static boolean isRelativeLink(String href) {
        if (href == null || href.isEmpty() || href.startsWith("//")) {
            return false;
        }
        boolean beforePath = true;
        for (int i = 0; i < href.length(); i++) {
            char c = href.charAt(i);
            if (c <= ' ' || c == 0x7f || c == '\\') {
                return false;
            }
            if (c == '/' || c == '?' || c == '#') {
                beforePath = false;
            } else if (c == ':' && beforePath) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the end of the tag that starts with the "&lt;" at the given index,
     * when it is a tag of the HTML subset or one of the inline tags it
     * removes. An end tag is its name, then optional whitespace or "/", then
     * "&gt;". A start tag may also have attributes, each with a value, and
     * ends at the first "&gt;" outside a quoted value. The scan never goes past the next "&lt;", so
     * scanning from every "&lt;" in a text takes linear time.
     *
     * @return the index after the tag's "&gt;", or -1 if this isn't a tag of
     *         the subset
     */
    private static int subsetTagEnd(String html, int lt) {
        int length = html.length();
        int i = lt + 1;
        boolean endTag = i < length && html.charAt(i) == '/';
        if (endTag) {
            i++;
        }
        int nameStart = i;
        while (i < length && i - nameStart <= HTML_SUBSET_TAG_MAX_LENGTH
                && isAsciiLetter(html.charAt(i))) {
            i++;
        }
        String name = html.substring(nameStart, i).toLowerCase(Locale.ROOT);
        if (!(HTML_SUBSET_TAGS.contains(name) || HTML_SUBSET_DROPPED_TAGS.contains(name))
                || i == length
                || !(isHtmlSpace(html.charAt(i)) || html.charAt(i) == '/'
                        || html.charAt(i) == '>')) {
            return -1;
        }
        boolean attributes = !endTag;

        // where the scan is, as an HTML parser sees it: between attributes,
        // in an attribute's name, after the name, after its "=", or in a
        // value that has no quotes
        final int between = 0;
        final int inName = 1;
        final int afterName = 2;
        final int beforeValue = 3;
        final int inValue = 4;
        int state = between;
        char quote = 0;
        for (; i < length; i++) {
            char c = html.charAt(i);
            if (c == '<') {
                return -1;
            }
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                    state = between;
                }
                continue;
            }
            if (c == '>') {
                // an attribute without a value is more likely to be text,
                // such as "x<a and b>y", than part of a link
                return (state == inName || state == afterName) ? -1 : i + 1;
            }
            boolean space = isHtmlSpace(c);
            switch (state) {
            case between:
                if (!space && c != '/') {
                    if (!attributes) {
                        return -1;
                    }
                    state = inName;
                }
                break;
            case inName:
                if (c == '=') {
                    state = beforeValue;
                } else if (space) {
                    state = afterName;
                } else if (c == '/') {
                    return -1;
                }
                break;
            case afterName:
                if (c == '=') {
                    state = beforeValue;
                } else if (!space) {
                    return -1;
                }
                break;
            case beforeValue:
                if (c == '"' || c == '\'') {
                    quote = c;
                } else if (!space) {
                    state = inValue;
                }
                break;
            default:
                if (space) {
                    state = between;
                }
                break;
            }
        }
        return -1;
    }

    private static boolean isHtmlSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    public static String getContentTypeFromFileName(String fileName) {

        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();

        // TODO: figure out why PNG is missing from Java MIME types
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap) map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {
            }
        }

        return map.getContentType(fileName);
    }

    /**
     * Validate the form of an email address.
     * 
     * <P>
     * Return <tt>true</tt> only if
     * <ul>
     * <li> <tt>aEmailAddress</tt> can successfully construct an
     * {@link javax.mail.internet.InternetAddress}
     * <li>when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains
     * two tokens which satisfy
     * </ul>
     * <P>
     * The second condition arises since local email addresses, simply of the
     * form "<tt>albert</tt>", for example, are valid for
     * {@link javax.mail.internet.InternetAddress}, but almost always undesired.
     */
    public static boolean isValidEmailAddress(String aEmailAddress) {
        if (aEmailAddress == null) {
            return false;
        }
        boolean result = true;
        try {
            // See if its valid
            new InternetAddress(aEmailAddress);
            if (!hasNameAndDomain(aEmailAddress)) {
                result = false;
            }
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Checks for name and domain.
     * 
     * @param aEmailAddress
     *            the a email address
     * @return true, if successful
     */
    private static boolean hasNameAndDomain(String aEmailAddress) {
        String[] tokens = aEmailAddress.split("@");
        return tokens.length == 2 && StringUtils.isNotEmpty(tokens[0])
                && StringUtils.isNotEmpty(tokens[1]);
    }

}
