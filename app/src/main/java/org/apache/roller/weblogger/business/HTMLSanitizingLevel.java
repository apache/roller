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
package org.apache.roller.weblogger.business;

public enum HTMLSanitizingLevel {
    NONE("None (Remove all HTML)"),
    BASIC("Basic (Allow links and basic formatting -- pre, code, cite, etc.)"),
    BASICWITHIMAGES("Basic plus image tags"),
    RELAXED("Relaxed (Allows tables, headings, divs)"),
    RELAXEDWITHIFRAME("Relaxed plus iframes (Social media widgets, videos, scripting potential)"),
    OFF("No sanitizing at all (Allows scripts, forms.  Not recommended)");

    private String description;

    HTMLSanitizingLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
