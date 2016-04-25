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

import org.springframework.mobile.device.DeviceType;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * A single template of a given RenditionType.
 */
public interface TemplateRendition {

    @XmlType
    @XmlEnum
    enum RenditionType {
        @XmlEnumValue("normal") NORMAL,
        @XmlEnumValue("tablet") TABLET,
        @XmlEnumValue("mobile") MOBILE;
    }

    @XmlType
    @XmlEnum
    public enum TemplateLanguage {
        @XmlEnumValue("velocity") VELOCITY("Velocity");

        private final String readableName;

        TemplateLanguage(String readableName) {
            this.readableName = readableName;
        }

        public String getReadableName() {
            return readableName;
        }
    }

    String getTemplate();

    TemplateLanguage getTemplateLanguage();

    RenditionType getRenditionType();

    void setTemplate(String template);

    void setTemplateLanguage(TemplateLanguage templateLanguage);

    void setRenditionType(RenditionType type);
}
