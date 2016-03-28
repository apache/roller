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
package org.apache.roller.weblogger.pojos;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * A Theme specific implementation of a Template.
 * 
 * A ThemeTemplate represents a template which is part of a Theme.
 */
public interface ThemeTemplate extends Template {

    @XmlType
    @XmlEnum
    public enum ComponentType {
        @XmlEnumValue("weblog") WEBLOG("Weblog", "text/html", true),
        @XmlEnumValue("permalink") PERMALINK("Permalink", "text/html", true),
        @XmlEnumValue("search") SEARCH("Search", "text/html", true),
        @XmlEnumValue("tagsIndex") TAGSINDEX("Tag Index", "text/html", true),
        @XmlEnumValue("stylesheet") STYLESHEET("Stylesheet", "text/css", false),
        @XmlEnumValue("custom") CUSTOM("Custom", null, false);

        private final String readableName;

        private final String contentType;

        private final boolean singleton;

        ComponentType(String readableName, String contentType, boolean singleton) {
            this.readableName = readableName;
            this.contentType = contentType;
            this.singleton = singleton;
        }

        public String getReadableName() {
            return readableName;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isSingleton() {
            return singleton;
        }
    }

    /**
     * A read-only copy for usage within templates, with fields limited
     * to just those we wish to provide to those templates.
     */
    ThemeTemplate templateCopy();

    /**
     * The action this template is defined for.
     */
    ComponentType getAction();
    
    
    /**
     * The url link value for this Template.  If this template is not
     * private this is the url that it can be accessed at.
     */
    String getLink();
    
    
    /**
     * Is the Template hidden?  A hidden template cannot be accessed directly.
     */
    boolean isHidden();
    
    
    /**
     * Is the Template to be included in the navbar?
     */
    boolean isNavbar();

}
