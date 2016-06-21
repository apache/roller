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
package org.apache.roller.weblogger.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceUtils;

/**
 * General purpose utilities, not for use in templates.
 */
public class Utilities {
    /** The <code>Log</code> instance for this class. */
    private static Logger log = LoggerFactory.getLogger(Utilities.class);

    public static final String TAG_SPLIT_CHARS = " ,\n\r\f\t";

    private static Pattern mLinkPattern = Pattern.compile("<a href=.*>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_B_TAG_PATTERN = Pattern.compile(
            "&lt;b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_B_TAG_PATTERN = Pattern.compile(
            "&lt;/b&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_I_TAG_PATTERN = Pattern.compile(
            "&lt;i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_I_TAG_PATTERN = Pattern.compile(
            "&lt;/i&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_BLOCKQUOTE_TAG_PATTERN = Pattern
            .compile("&lt;blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_BLOCKQUOTE_TAG_PATTERN = Pattern
            .compile("&lt;/blockquote&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern BR_TAG_PATTERN = Pattern.compile(
            "&lt;br&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_P_TAG_PATTERN = Pattern.compile(
            "&lt;p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_P_TAG_PATTERN = Pattern.compile(
            "&lt;/p&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_PRE_TAG_PATTERN = Pattern.compile(
            "&lt;pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_PRE_TAG_PATTERN = Pattern.compile(
            "&lt;/pre&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_UL_TAG_PATTERN = Pattern.compile(
            "&lt;ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_UL_TAG_PATTERN = Pattern.compile(
            "&lt;/ul&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_OL_TAG_PATTERN = Pattern.compile(
            "&lt;ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_OL_TAG_PATTERN = Pattern.compile(
            "&lt;/ol&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_LI_TAG_PATTERN = Pattern.compile(
            "&lt;li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_LI_TAG_PATTERN = Pattern.compile(
            "&lt;/li&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_A_TAG_PATTERN = Pattern.compile(
            "&lt;/a&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_A_TAG_PATTERN = Pattern.compile(
            "&lt;a href=.*&gt;", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTE_PATTERN = Pattern.compile("&quot;",
            Pattern.CASE_INSENSITIVE);

    /**
     * Transforms the given String into a subset of HTML displayable on a web
     * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
     * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags if applicable).
     *
     * @param s the String to transform
     * @return the transformed String
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
        s = replace(s, BR_TAG_PATTERN, "<br>");
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
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;". Replace any HTML tags with a space.
     */
    public static String removeHTML(String str) {
        return removeHTML(str, true);
    }

    /**
     * Remove occurrences of html, defined as any text between the characters
     * "&lt;" and "&gt;".
     * 
     * @param str text to strip HTML out of
     * @param addSpace whether to replace tags with a space
     * @return String without the HTML tags
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

                // replace each tag with a space (looks better)
                if (addSpace) {
                    ret.append(" ");
                }
            }
            endTag = str.indexOf('>', beginTag);

            // if endTag found move "cursor" forward
            if (endTag > -1) {
                start = endTag + 1;
                beginTag = str.indexOf('<', start);
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

    /**
     * Code (stolen from Pebble) to add rel="nofollow" string to all links in HTML.
     */
    public static String addNofollow(String html) {
        if (html == null || html.length() == 0) {
            return html;
        }
        Matcher m = mLinkPattern.matcher(html);
        StringBuilder buf = new StringBuilder();
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String link = html.substring(start, end);
            buf.append(html.substring(0, start));
            if (link.contains("rel=\"nofollow\"")) {
                buf.append(link.substring(0, link.length() - 1));
                buf.append(" rel=\"nofollow\">");
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
     * Replaces occurrences of non-alphanumeric characters with a supplied char.
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (char aChar : testChars) {
            if (Character.isLetterOrDigit(aChar)) {
                ret.append(aChar);
            } else {
                ret.append(subst);
            }
        }
        return ret.toString();
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

    /**
     * This method based on the "truncateNicely" tag at the former Apache Jakarta taglib project.
     *
     * @param str String to parse
     * @param lower minimum acceptable length of string (not counting HTML tags)
     * @param upper maximum acceptable length (not counting HTML tags)
     * @param appendToEnd characters (potentially past maximum length) to add to
     *                    visually indicate that truncation occurred
     * @return processed string
     */
    public static String truncateHTML(String str, int lower, int upper, String appendToEnd) {
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
     * Extract (keep) JUST the HTML from the String.
     * @param str String to remove HTML from
     * @return String with HTML removed
     */
    public static String extractHTML(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder(str.length());
        int start = 0;
        int beginTag = str.indexOf('<');
        if (beginTag == -1) {
            return str;
        }

        while (beginTag >= start) {
            int endTag = str.indexOf('>', beginTag);

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
     * Removes non-alphanumerics from tags.
     * @param tag tag to strip invalid chars from
     * @return tag without invalid chars.
     */
    public static String stripInvalidTagCharacters(String tag) {
        if (tag == null) {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder();
        char[] charArray = tag.toCharArray();
        for (char c : charArray) {

            // fast-path exclusions quotes and commas are obvious
            // 34 = double-quote, 44 = comma
            switch (c) {
            case 34:
            case 44:
                continue;
            }

            if ((33 <= c && c <= 126) || Character.isUnicodeIdentifierPart(c)
                    || Character.isUnicodeIdentifierStart(c)) {
                sb.append(c);
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
    public static Set<String> splitStringAsTags(String tags) {
        String[] tagsarr = StringUtils.split(tags, TAG_SPLIT_CHARS);
        if (tagsarr == null) {
            return Collections.emptySet();
        }
        Set<String> mySet = new HashSet<>();
        Collections.addAll(mySet, tagsarr);
        return mySet;
    }

    private static String replace(String string, Pattern pattern, String replacement) {
        Matcher m = pattern.matcher(string);
        return m.replaceAll(replacement);
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
            String[] tokens = aEmailAddress.split("@");
            if (!(tokens.length == 2 && StringUtils.isNotEmpty(tokens[0]) && StringUtils.isNotEmpty(tokens[1]))) {
                result = false;
            }
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Compose a map of key=value params into a query string.
     */
    public static String getQueryString(Map<String, String> params) {
        if (params == null) {
            return null;
        }

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(queryString.length() == 0 ? "?" : "&");
            queryString.append(entry.getKey());
            queryString.append("=");
            queryString.append(entry.getValue());
        }

        return queryString.toString();
    }

    /**
     * URL encode a string using UTF-8.
     */
    public static String encode(String str) {
        String encodedStr = str;
        try {
            encodedStr = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // ignored
        }
        return encodedStr;
    }

    /**
     * URL decode a string using UTF-8.
     */
    public static String decode(String str) {
        String decodedStr = str;
        try {
            decodedStr = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // ignored
        }
        return decodedStr;
    }

    public static String getEncodedTagsString(List tags) {
        StringBuilder tagsString = new StringBuilder();
        if(tags != null && tags.size() > 0) {
            String tag;
            Iterator tagsIT = tags.iterator();

            // do first tag
            tag = (String) tagsIT.next();
            tagsString.append(encode(tag));

            // do rest of tags, joining them with a '+'
            while(tagsIT.hasNext()) {
                tag = (String) tagsIT.next();
                tagsString.append("+");
                tagsString.append(encode(tag));
            }
        }
        return tagsString.toString();
    }

    /**
     * URL encode a path string using UTF-8. The path separator '/' will not be encoded
     */
    public static String encodePath(String path) {
        int i = path.indexOf('/');
        StringBuilder sb = new StringBuilder();
        while (i != -1) {
            sb.append(encode(path.substring(0, i))).append('/');
            path = path.substring(i + 1);
            i = path.indexOf('/');
        }
        sb.append(encode(path));
        return sb.toString();
    }

    public static Object jaxbUnmarshall(String xsdPath, String xmlPath, boolean xmlFromFileSystem,
                                        Class... classesToBeBound) {

        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new StreamSource(
                    Utilities.class.getResourceAsStream(xsdPath)));

            InputStream is;
            if (xmlFromFileSystem) {
                is = new FileInputStream(xmlPath);
            } else {
                is = Utilities.class.getResourceAsStream(xmlPath);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(schema);
            jaxbUnmarshaller.setEventHandler( (event) -> {
                log.error("Parsing error: " +
                        event.getMessage() + "; Line #" +
                        event.getLocator().getLineNumber() + "; Column #" +
                        event.getLocator().getColumnNumber());
                return false;
            });
            return jaxbUnmarshaller.unmarshal(is);
        } catch (Exception ex) {
            throw new IllegalArgumentException("JAXB Unmarshalling Error", ex);
        }
    }

    /**
     * Sets the HTTP response status to 304 (NOT MODIFIED) if the request
     * contains an If-Modified-Since header that specifies a time that is at or
     * after the time specified by the value of lastModifiedTimeMillis
     * <em>truncated to second granularity</em>. Returns true if the response
     * status was set, false if not.
     *
     * @param request - the request
     * @param response - the response
     * @param lastModifiedTimeMillis - the last modified time millis
     * @param deviceType - standard or mobile, null to not check
     *
     * @return true if a response status was sent, false otherwise.
     */
    public static boolean respondIfNotModified(HttpServletRequest request, HttpServletResponse response,
                                               long lastModifiedTimeMillis, DeviceType deviceType) {

        long sinceDate;
        try {
            sinceDate = request.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException ex) {
            // this indicates there was some problem parsing the header value as a date
            return false;
        }

        // truncate to seconds
        lastModifiedTimeMillis -= (lastModifiedTimeMillis % DateUtils.MILLIS_PER_SECOND);

        if (log.isDebugEnabled()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd 'at' h:mm:ss a").withZone(ZoneId.systemDefault());
            log.debug("since date = " + formatter.format(Instant.ofEpochMilli(sinceDate)));
            log.debug("last mod date (truncated to seconds) = " + formatter.format(Instant.ofEpochMilli(lastModifiedTimeMillis)));
        }

        // Set device type for device switching
        String eTag = (deviceType == null) ? null : deviceType.name();

        String previousToken = request.getHeader("If-None-Match");
        if (eTag != null && previousToken != null && eTag.equals(previousToken)
                && lastModifiedTimeMillis <= sinceDate
                || (eTag == null || previousToken == null)
                && lastModifiedTimeMillis <= sinceDate) {

            log.debug("NOT MODIFIED {}", request.getRequestURL());

            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            // use the same date we sent when we created the eTag the first time through
            response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the Last-Modified header using the given time in milliseconds. Note
     * that because the header has the granularity of one second, the value will
     * get truncated to the nearest second that does not exceed the provided
     * value.  This will also set the Expires header to a date in the past. This forces
     * clients to revalidate the cache each time.
     */
    public static void setLastModifiedHeader(HttpServletResponse response,
                                             long lastModifiedTimeMillis, DeviceType deviceType) {

        // Save our device type for device switching. Must use caching on headers for this to work.
        if (deviceType != null) {
            String eTag = deviceType.name();
            response.setHeader("ETag", eTag);
        }

        response.setDateHeader("Last-Modified", lastModifiedTimeMillis);
        // Force clients to revalidate each time
        // See RFC 2616 (HTTP 1.1 spec) secs 14.21, 13.2.1
        response.setDateHeader("Expires", 0);
        // We may also want this (See 13.2.1 and 14.9.4)
        // response.setHeader("Cache-Control","must-revalidate");
    }

    public static DeviceType getDeviceType(HttpServletRequest request) {
        SitePreference sitePreference = SitePreferenceUtils.getCurrentSitePreference(request);
        return (sitePreference != null && sitePreference.isMobile()) ? DeviceType.MOBILE : DeviceType.NORMAL;
    }
}
