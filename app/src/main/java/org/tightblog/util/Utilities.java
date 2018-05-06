/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.DeviceUtils;

import javax.servlet.http.HttpServletRequest;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * General purpose utilities, not for use in templates.
 */
public final class Utilities {

    private Utilities() {
    }

    private static Logger log = LoggerFactory.getLogger(Utilities.class);

    public static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern(Utilities.FORMAT_6CHARS);
    public static final DateTimeFormatter YMD_FORMATTER = DateTimeFormatter.ofPattern(Utilities.FORMAT_8CHARS);
    public static final String FORMAT_6CHARS = "yyyyMM";
    public static final String FORMAT_8CHARS = "yyyyMMdd";
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
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedHashMap(
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
     * Exception: apostrophes are removed with no replacement
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuilder ret = new StringBuilder(str.length());
        char[] testChars = str.toCharArray();
        for (char aChar : testChars) {
            if (Character.isLetterOrDigit(aChar)) {
                ret.append(aChar);
            } else if (aChar != '\'') {
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
                // no it wasn't, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
            }
            // the string was truncated, so we append the appendToEnd String
            str2 = str2 + appendToEnd;
        }
        return str2;
    }

    /**
     * Removes non-alphanumerics from tags.
     *
     * @param tag tag to strip invalid chars from
     * @param locale to determine lower-case, default if null
     * @return lower case tag with alphanumerics removed
     */
    public static String normalizeTag(String tag, Locale locale) {
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
        tag = sb.toString();
        return locale == null ? tag.toLowerCase() : tag.toLowerCase(locale);
    }

    /**
     * Compose a map of key=value params into a query string.
     */
    public static String getQueryString(Map<String, String> params) {
        if (params == null) {
            return "";
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
            if (encodedStr != null) {
                encodedStr = URLEncoder.encode(str, "UTF-8");
            }
        } catch (UnsupportedEncodingException ignored) {
        }
        return encodedStr;
    }

    /**
     * URL decode a string using UTF-8.
     */
    public static String decode(String str) {
        String decodedStr = str;
        try {
            if (decodedStr != null) {
                decodedStr = URLDecoder.decode(str, "UTF-8");
            }
        } catch (UnsupportedEncodingException ignored) {
        }
        return decodedStr;
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
            jaxbUnmarshaller.setEventHandler(event -> {
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

    public static DeviceType getDeviceType(HttpServletRequest request) {
        Device currentDevice = DeviceUtils.getCurrentDevice(request);
        if (currentDevice != null) {
            if (currentDevice.isMobile()) {
                return DeviceType.MOBILE;
            }
            if (currentDevice.isTablet()) {
                return DeviceType.TABLET;
            }
        }
        return DeviceType.NORMAL;
    }

    /**
     * Parse date as either 6-char or 8-char format.  Use current date if date not provided
     * in URL (e.g., a permalink), more than 30 days in the future, or not valid
     */
    public static LocalDate parseURLDate(String dateString) {
        LocalDate ret = null;

        try {
            if (dateString != null && StringUtils.isNumeric(dateString)) {
                if (dateString.length() == 8) {
                    ret = LocalDate.parse(dateString, Utilities.YMD_FORMATTER);
                } else if (dateString.length() == 6) {
                    YearMonth tmp = YearMonth.parse(dateString, Utilities.YM_FORMATTER);
                    ret = tmp.atDay(1);
                }
            }
        } catch (DateTimeParseException ignored) {
            ret = null;
        }

        // make sure the requested date is not more than a month in the future
        if (ret == null || ret.isAfter(LocalDate.now().plusDays(30))) {
            ret = LocalDate.now();
        }

        return ret;
    }
}
