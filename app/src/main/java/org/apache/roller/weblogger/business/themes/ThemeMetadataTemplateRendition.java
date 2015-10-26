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
package org.apache.roller.weblogger.business.themes;

import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;

import javax.xml.bind.annotation.XmlAttribute;

public class ThemeMetadataTemplateRendition {
    private TemplateLanguage templateLanguage = TemplateLanguage.VELOCITY;
    private String contentsFile = null;
    private RenditionType type = RenditionType.STANDARD;

    public TemplateLanguage getTemplateLanguage() {
        return templateLanguage;
    }

    public void setTemplateLanguage(TemplateLanguage templateLanguage) {
        this.templateLanguage = templateLanguage;
    }

    public String getContentsFile() {
        return contentsFile;
    }

    public void setContentsFile(String contentsFile) {
        this.contentsFile = contentsFile;
    }

    public RenditionType getType() {
        return type;
    }

    @XmlAttribute
    public void setType(RenditionType type) {
        this.type = type;
    }
}
