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

import org.apache.commons.lang.time.DateUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * General purpose utilities, not for use in templates.
 */
public class Utilities {

    private static Logger log = LoggerFactory.getLogger(Utilities.class);

    public static final String TAG_SPLIT_CHARS = " ,\n\r\f\t";

    public static final String FORMAT_6CHARS = "yyyyMM";
    public static final String FORMAT_8CHARS = "yyyyMMdd";
    public static final int PERCENT_100 = 100;
    public static final int EIGHT_KB_IN_BYTES = 8192;
    public static final int TWENTYFOUR_KB_IN_BYTES = 24576;
    public static final int ONE_MB_IN_BYTES = 1024 * 1024;

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Removes html tags from input.
     *
     * @param str String potentially containing html tags
     * @return plain text string, all HTML tags removed.
     */
    public static String removeHTML(String str) {
        if (str == null) {
            return null;
        } else {
            return Jsoup.clean(str, HTMLSanitizer.Level.NONE.getWhitelist());
        }
    }

    /**
     * Replacement for Collectors.toMap which returns a HashMap
     * LinkedHashMap useful for keeping the Map in insertion order
     * http://stackoverflow.com/a/29090335/1207540
     */
    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedHashMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }

    public static String insertLineBreaksIfMissing(String text) {
        log.debug("starting value: {}", text);

        // first check if text already has <br> and/or <p> tags, if so, assume it has already been formatted.
        if (text.contains("<br>") || text.contains("<p>")) {
            return text;
        }

        /*
         * setup a buffered reader and iterate through each line inserting html as needed
         * NOTE: We consider a paragraph to be 2 endlines with no text between them
         */
        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new StringReader(text));

            String line;
            boolean insidePara = false;
            while ((line = br.readLine()) != null) {

                if (!insidePara && line.trim().length() > 0) {
                    // start of a new paragraph
                    buf.append("<p>");
                    buf.append(line);
                    insidePara = true;
                } else if (insidePara && line.trim().length() == 0) {
                    // end of a paragraph
                    buf.append("</p>");
                    insidePara = false;
                } else {
                    buf.append(" ").append(line);
                }
            }

            // if the text ends without an empty line then we need to
            // terminate the last paragraph now
            if (insidePara) {
                buf.append("</p>");
            }

        } catch (Exception e) {
            log.warn("trouble rendering text.", e);
        }

        log.debug("ending value:\n {}", buf.toString());
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
     * HTML textarea elements have a form submission value whose linebreaks are
     * standardized to \r\n and an API value (used in scripting) standardized to \n.
     * The former is used when a reader submits a comment (and serialized to the DB
     * as such), the latter when the blogger edits the comment with the UI.  This
     * method converts the latter back to the former for DB serialization and subsequent
     * browser rendering.
     * <p>
     * Each readline() call below strips off the \n, necessitating the addition of
     * the "\r\n" after each line.
     * <p>
     * See: https://www.w3.org/TR/html5/forms.html#concept-textarea-api-value
     */
    public static String apiValueToFormSubmissionValue(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public static String truncateText(String str, int lower, int upper, String appendToEnd) {
        // strip markup from the string
        String str2 = removeHTML(str);

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
     *
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
            } else {
                // if no endTag found, break
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
            tt.append(strBase64Chars.charAt((byte2 / 16) + ((byte1 & 0x3) * 16)));
            tt.append(((bByte2) ? strBase64Chars.charAt((byte3 / 64) + ((byte2 & 0xF) * 4)) : '='));
            tt.append(((bByte3) ? strBase64Chars.charAt(byte3 & 0x3F) : '='));
        }

        return tt.toString();
    }

    /**
     * Removes non-alphanumerics from tags.
     *
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
            if (c == 34 || c == 44) {
                continue;
            }

            if ((33 <= c && c <= 126) || Character.isUnicodeIdentifierPart(c) ||
                    Character.isUnicodeIdentifierStart(c)) {
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
        if (tags != null && tags.size() > 0) {
            String tag;
            Iterator tagsIT = tags.iterator();

            // do first tag
            tag = (String) tagsIT.next();
            tagsString.append(encode(tag));

            // do rest of tags, joining them with a '+'
            while (tagsIT.hasNext()) {
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
            jaxbUnmarshaller.setEventHandler((event) -> {
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
     * @param request                - the request
     * @param response               - the response
     * @param lastModifiedTimeMillis - the last modified time millis
     * @param deviceType             - standard or mobile, null to not check
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
        if (eTag != null && previousToken != null && eTag.equals(previousToken) &&
                lastModifiedTimeMillis <= sinceDate ||
                (eTag == null || previousToken == null) && lastModifiedTimeMillis <= sinceDate) {

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

    public static void testDataSource(DataSource mySource) throws WebloggerException {
        try {
            Connection testcon = mySource.getConnection();
            testcon.close();
        } catch (Exception e) {
            String errorMsg =
                    "ERROR: unable to obtain database connection. " +
                            "Likely problem: bad connection parameters or database unavailable.";
            log.error(errorMsg);
            throw new WebloggerException(errorMsg, e);
        }
    }

}
