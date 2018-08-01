/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.tightblog.util;

import org.jsoup.safety.Whitelist;

public final class HTMLSanitizer {

    private static final Whitelist NONE_WHITELIST = Whitelist.none();

    private static final Whitelist LIMITED_WHITELIST = Whitelist.simpleText().addTags("br", "p");

    private static final Whitelist BASIC_WHITELIST = Whitelist.basic();

    private static final Whitelist BASIC_IMAGES_WHITELIST = Whitelist.basicWithImages();

    private static final Whitelist RELAXED_WHITELIST = Whitelist.relaxed();

    private static final Whitelist RELAXED_IFRAMES_WHITELIST = Whitelist.relaxed()
            .addTags("iframe")
            .addAttributes("iframe", "width", "height", "src", "style", "allowfullscreen")
            .addProtocols("iframe", "src", "http", "https");

    public enum Level {
        NONE(0, "globalConfig.htmlsanitizer.none", NONE_WHITELIST),
        LIMITED(1, "globalConfig.htmlsanitizer.limited", LIMITED_WHITELIST),
        BASIC(2, "globalConfig.htmlsanitizer.basic", BASIC_WHITELIST),
        BASIC_IMAGES(3, "globalConfig.htmlsanitizer.basicimages", BASIC_IMAGES_WHITELIST),
        RELAXED(4, "globalConfig.htmlsanitizer.relaxed", RELAXED_WHITELIST),
        RELAXED_IFRAMES(5, "globalConfig.htmlsanitizer.relaxediframes", RELAXED_IFRAMES_WHITELIST),
        OFF(6, "globalConfig.htmlsanitizer.off", null);

        private String description;

        private int sanitizingLevel;

        private Whitelist whitelist;

        Level(int sanitizingLevel, String description, Whitelist whitelist) {
            this.sanitizingLevel = sanitizingLevel;
            this.description = description;
            this.whitelist = whitelist;
        }

        public String getDescription() {
            return description;
        }

        public int getSanitizingLevel() {
            return sanitizingLevel;
        }

        public Whitelist getWhitelist() {
            return whitelist;
        }
    }

}
