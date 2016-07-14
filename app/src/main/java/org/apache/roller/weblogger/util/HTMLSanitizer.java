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
package org.apache.roller.weblogger.util;

import org.jsoup.safety.Whitelist;

public final class HTMLSanitizer {

    private final static Whitelist NoneWhitelist = Whitelist.none();

    private final static Whitelist LimitedWhitelist = Whitelist.simpleText().addTags("br", "p");

    private final static Whitelist BasicWhitelist = Whitelist.basic();

    private final static Whitelist BasicImagesWhitelist = Whitelist.basicWithImages();

    private final static Whitelist RelaxedWhitelist = Whitelist.relaxed();

    private final static Whitelist RelaxedIframesWhitelist = Whitelist.relaxed()
        .addTags("iframe")
        .addAttributes("iframe", "width", "height", "src", "style", "allowfullscreen")
        .addProtocols("iframe", "src", "http", "https");

    public enum Level {
        NONE(0, "configForm.htmlsanitizer.none", NoneWhitelist),
        LIMITED(1, "configForm.htmlsanitizer.limited", LimitedWhitelist),
        BASIC(2, "configForm.htmlsanitizer.basic", BasicWhitelist),
        BASIC_IMAGES(3, "configForm.htmlsanitizer.basicimages", BasicImagesWhitelist),
        RELAXED(4, "configForm.htmlsanitizer.relaxed", RelaxedWhitelist),
        RELAXED_IFRAMES(5, "configForm.htmlsanitizer.relaxediframes", RelaxedIframesWhitelist),
        OFF(6, "configForm.htmlsanitizer.off", null);

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
