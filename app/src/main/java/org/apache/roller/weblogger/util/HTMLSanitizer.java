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

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * Cleans HTML written by weblog users when weblog admins aren't trusted
 * (weblogAdminsUntrusted=true), using jsoup's Cleaner. Formatting, list,
 * heading, table and image markup is kept; scripts, style sheets, forms,
 * frames, plugins, event handlers and other attributes are removed, keeping
 * the text inside removed tags.
 */
public final class HTMLSanitizer {

    public static final Boolean xssEnabled =
            WebloggerConfig.getBooleanProperty("weblogAdminsUntrusted", Boolean.FALSE);

    private static final Safelist SAFELIST = new WeblogSafelist();

    private HTMLSanitizer() {
    }

    /**
     * Cleans HTML before it's shown on a page.
     *
     * @param html the HTML to clean
     * @return the cleaned HTML, or null if html is null
     */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, "", SAFELIST,
                new Document.OutputSettings().prettyPrint(false));
    }

    /** Cleans the HTML if weblog admins aren't trusted, and returns it as is otherwise. */
    public static String conditionallySanitize(String html) {
        if (xssEnabled && html != null) {
            return sanitize(html);
        }
        return html;
    }

    /** The markup weblog users may write when weblog admins aren't trusted. */
    private static final class WeblogSafelist extends Safelist {

        WeblogSafelist() {
            addTags("a", "b", "big", "blockquote", "br", "code", "dd", "del", "div", "dl",
                    "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img", "kbd",
                    "li", "ol", "p", "pre", "s", "small", "span", "strike", "strong", "sub",
                    "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "ul");
            addAttributes(":all", "align", "class", "dir", "id", "lang", "style", "title");
            addAttributes("a", "href", "name");
            addAttributes("img", "alt", "border", "height", "src", "width");
            addAttributes("ol", "start", "type");
            addAttributes("table", "border", "cellpadding", "cellspacing", "summary", "width");
            addAttributes("td", "colspan", "height", "rowspan", "valign", "width");
            addAttributes("th", "colspan", "height", "rowspan", "valign", "width");
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
            switch (name) {
                case "width":
                case "height":
                    return isDimension(value);
                case "style":
                    return isSafeStyle(value);
                default:
                    return true;
            }
        }

        /** A number of pixels or a percentage. */
        private static boolean isDimension(String value) {
            String v = value.trim();
            if (v.endsWith("%")) {
                v = v.substring(0, v.length() - 1);
            }
            if (v.isEmpty() || v.length() > 6) {
                return false;
            }
            for (int i = 0; i < v.length(); i++) {
                if (!Character.isDigit(v.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * A style that can't load anything or run script: no url(), image-set(),
         * expression(), @import, behaviors, or escapes and comments that could
         * hide them.
         */
        private static boolean isSafeStyle(String value) {
            String v = value.toLowerCase(Locale.ROOT);
            return !(v.contains("url(") || v.contains("image-set(") || v.contains("expression(")
                    || v.contains("javascript:") || v.contains("@import") || v.contains("behavior")
                    || v.contains("-moz-binding") || v.contains("\\") || v.contains("/*"));
        }
    }
}
