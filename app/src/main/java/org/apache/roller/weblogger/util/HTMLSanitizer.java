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

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * Cleans HTML written by weblog users when weblog admins aren't trusted
 * (weblogAdminsUntrusted=true), using jsoup's Cleaner. Formatting, list,
 * heading, table and image markup is kept; scripts, style sheets, forms,
 * frames, plugins, event handlers and other attributes are removed, keeping
 * the text inside removed tags. Quotes in text are encoded, so that cleaned
 * text is also safe inside an attribute value.
 */
public final class HTMLSanitizer {

    public static final Boolean xssEnabled =
            WebloggerConfig.getBooleanProperty("weblogAdminsUntrusted", Boolean.FALSE);

    private static final Cleaner CLEANER = new Cleaner(new WeblogSafelist());

    // script in a style value
    private static final Pattern FORBIDDEN_STYLE =
            Pattern.compile("(?:expression|eval|javascript)\\s*\\(");

    private HTMLSanitizer() {
    }

    /**
     * Cleans HTML before it's shown on a page.
     *
     * @param html the HTML to clean
     * @return the cleaned HTML, or null if html is null
     */
    public static String sanitize(String html) {
        return sanitize(html, false);
    }

    /**
     * Cleans a value that's stored as plain text, such as a name or an e-mail
     * address. Tags and quotes are treated as in {@link #sanitize(String)},
     * but an ampersand in the text stays an ampersand and isn't read as the
     * start of an entity.
     *
     * @param text the text to clean
     * @return the cleaned text, or null if text is null
     */
    public static String sanitizeText(String text) {
        return sanitize(text, true);
    }

    /** Cleans the HTML if weblog admins aren't trusted, and returns it as is otherwise. */
    public static String conditionallySanitize(String html) {
        if (xssEnabled && html != null) {
            return sanitize(html);
        }
        return html;
    }

    /**
     * Cleans the plain text if weblog admins aren't trusted, and returns it as
     * is otherwise.
     */
    public static String conditionallySanitizeText(String text) {
        if (xssEnabled && text != null) {
            return sanitizeText(text);
        }
        return text;
    }

    private static String sanitize(String html, boolean plainText) {
        if (html == null) {
            return null;
        }
        Document clean = CLEANER.clean(
                Jsoup.parseBodyFragment(plainText ? encodeAmpersands(html) : html, ""));
        for (Element el : clean.select("[style]")) {
            String style = safeStyle(el.attr("style"));
            if (style.isEmpty()) {
                el.removeAttr("style");
            } else {
                el.attr("style", style);
            }
        }
        clean.outputSettings().prettyPrint(false);
        return encodeText(clean.body().html(), plainText);
    }

    /**
     * Encodes the ampersands outside of tags, so that the parser reads text
     * such as "&amp;region" as it was typed and not as an entity.
     */
    private static String encodeAmpersands(String html) {
        if (html.indexOf('&') < 0) {
            return html;
        }
        StringBuilder sb = new StringBuilder(html.length() + 16);
        boolean inTag = false;
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (inTag) {
                inTag = c != '>';
            } else if (c == '<' && i + 1 < html.length()
                    && (Character.isLetter(html.charAt(i + 1)) || "/!?".indexOf(html.charAt(i + 1)) >= 0)) {
                inTag = true;
            } else if (c == '&') {
                sb.append("&amp;");
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Encodes the quotes in the text of HTML written by jsoup, which always
     * puts attribute values in double quotes and encodes angle brackets in
     * text. For plain text, also turns encoded ampersands in the text back
     * into ampersands. Tags and attribute values are left as they are.
     */
    private static String encodeText(String html, boolean plainText) {
        StringBuilder sb = new StringBuilder(html.length() + 16);
        boolean inTag = false;
        boolean inValue = false;
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (inValue) {
                inValue = c != '"';
            } else if (inTag) {
                inValue = c == '"';
                inTag = c != '>';
            } else if (c == '<') {
                inTag = true;
            } else if (c == '"') {
                sb.append("&quot;");
                continue;
            } else if (c == '\'') {
                sb.append("&#39;");
                continue;
            } else if (plainText && html.startsWith("&amp;", i)) {
                sb.append('&');
                i += "&amp;".length() - 1;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** The declarations of a style that can't run script, joined again. */
    private static String safeStyle(String style) {
        StringBuilder sb = new StringBuilder();
        for (String declaration : style.split(";")) {
            String d = declaration.trim();
            if (isSafeDeclaration(d)) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(d);
            }
        }
        return sb.toString();
    }

    /**
     * A "name: value" style declaration that can't run script: no
     * expression(), eval() or javascript, no behaviors or bindings, no url()
     * other than an http or https one, no image-set(), and no escapes or
     * comments that could hide any of those.
     */
    private static boolean isSafeDeclaration(String declaration) {
        String d = declaration.toLowerCase(Locale.ROOT);
        int colon = d.indexOf(':');
        if (colon < 1 || d.contains("\\") || d.contains("/*")) {
            return false;
        }
        String name = d.substring(0, colon).trim();
        String value = d.substring(colon + 1).trim();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '-' || c == '_')) {
                return false;
            }
        }
        if (name.isEmpty() || value.isEmpty() || name.equals("behavior")
                || name.equals("-ms-behavior") || name.equals("-moz-binding")
                || value.contains("javascript:") || value.contains("image-set(")
                || FORBIDDEN_STYLE.matcher(value).find()) {
            return false;
        }
        for (int i = value.indexOf("url"); i >= 0; i = value.indexOf("url", i + 1)) {
            int open = skipSpaces(value, i + "url".length());
            if (open < value.length() && value.charAt(open) == '(' && !isHttpUrl(value, open + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the url( that was opened just before start holds an http or
     * https URL, in quotes or not, and is closed after it.
     */
    private static boolean isHttpUrl(String value, int start) {
        int i = skipSpaces(value, start);
        char quote = 0;
        if (i < value.length() && (value.charAt(i) == '"' || value.charAt(i) == '\'')) {
            quote = value.charAt(i++);
        }
        if (!value.startsWith("http://", i) && !value.startsWith("https://", i)) {
            return false;
        }
        while (i < value.length() && " \t\n\r\f\"'()<>".indexOf(value.charAt(i)) < 0) {
            i++;
        }
        if (quote != 0) {
            if (i >= value.length() || value.charAt(i) != quote) {
                return false;
            }
            i++;
        }
        i = skipSpaces(value, i);
        return i < value.length() && value.charAt(i) == ')';
    }

    private static int skipSpaces(String value, int start) {
        int i = start;
        while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
            i++;
        }
        return i;
    }

    /** The markup weblog users may write when weblog admins aren't trusted. */
    private static final class WeblogSafelist extends Safelist {

        WeblogSafelist() {
            addTags("a", "area", "b", "big", "blockquote", "br", "code", "dd", "del", "div",
                    "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img",
                    "kbd", "li", "map", "ol", "p", "param", "pre", "s", "small", "span",
                    "strike", "strong", "sub", "sup", "table", "tbody", "td", "tfoot", "th",
                    "thead", "tr", "ul");
            addAttributes(":all", "align", "class", "dir", "id", "lang", "style", "title");
            addAttributes("a", "href", "name", "rel", "target");
            addAttributes("area", "alt", "coords", "shape");
            addAttributes("img", "alt", "border", "height", "src", "usemap", "width");
            addAttributes("li", "type", "value");
            addAttributes("map", "name");
            addAttributes("ol", "start", "type");
            addAttributes("param", "name", "value");
            addAttributes("table", "border", "cellpadding", "cellspacing", "summary", "width");
            addAttributes("td", "colspan", "height", "nowrap", "rowspan", "scope", "valign",
                    "width");
            addAttributes("th", "colspan", "height", "nowrap", "rowspan", "scope", "valign",
                    "width");
            addAttributes("ul", "type");
            addProtocols("a", "href", "http", "https", "mailto");
            addProtocols("img", "src", "http", "https");
        }

        @Override
        public boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
            String name = attr.getKey().toLowerCase(Locale.ROOT);
            String value = attr.getValue();
            // an image may also carry image data that InlineImageData accepts
            if ("img".equals(tagName) && "src".equals(name)
                    && InlineImageData.parse(value) != null) {
                return true;
            }
            if (!super.isSafeAttribute(tagName, el, attr)) {
                return false;
            }
            if ("width".equals(name) || "height".equals(name)) {
                return isDimension(value);
            }
            return true;
        }

        /** A number of pixels or a percentage, in ASCII digits. */
        private static boolean isDimension(String value) {
            String v = value.endsWith("%") ? value.substring(0, value.length() - 1) : value;
            if (v.isEmpty() || v.length() > 6) {
                return false;
            }
            for (int i = 0; i < v.length(); i++) {
                if (v.charAt(i) < '0' || v.charAt(i) > '9') {
                    return false;
                }
            }
            return true;
        }
    }
}
